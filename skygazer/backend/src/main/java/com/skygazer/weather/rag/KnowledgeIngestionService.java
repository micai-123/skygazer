package com.skygazer.weather.rag;

import com.skygazer.weather.config.AliyunAiProperties;
import com.skygazer.weather.exception.AIModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 知识库入库管线。
 * <p>
 * 读取 {@code classpath:knowledge/*.md} 气象文档，按段落切块后向量化写入 {@link RedisKnowledgeVectorStore}，
 * 并递增知识库版本。可被 {@code /agent/knowledge/refresh} 重复触发（先清空再写入，幂等）。
 * </p>
 */
@Slf4j
@Service
public class KnowledgeIngestionService {

    private final RedisKnowledgeVectorStore vectorStore;
    private final AliyunAiProperties props;
    private final ResourcePatternResolver resolver;

    private static final int CHUNK_MAX_CHARS = 800;

    public KnowledgeIngestionService(RedisKnowledgeVectorStore vectorStore,
                                     AliyunAiProperties props) {
        this.vectorStore = vectorStore;
        this.props = props;
        this.resolver = new PathMatchingResourcePatternResolver();
    }

    /**
     * 执行一次完整的知识库重建。
     *
     * @throws AIModelException 当向量化/写入失败时抛出，由调用方决定降级策略
     */
    public synchronized void refresh() {
        log.info("开始重建气象知识库（路径={}）", props.getKnowledgeBasePath());
        vectorStore.clear();
        List<KnowledgeDocument> docs = loadMarkdownDocuments();
        int embedded = 0;
        for (KnowledgeDocument doc : docs) {
            try {
                vectorStore.add(doc);
                embedded++;
            } catch (Exception e) {
                log.warn("文档入库失败，已跳过 id={}：{}", doc.getId(), e.getMessage());
            }
        }
        vectorStore.bumpVersion();
        log.info("知识库重建完成：扫描 {} 篇，成功向量化 {} 篇，向量库当前共 {} 篇",
                docs.size(), embedded, vectorStore.count());
    }

    /**
     * 若向量库为空则自动入库一次，保证 RAG 开箱即用（无需手动调用 refresh）。
     * 监听容器刷新事件，在独立线程中 best-effort 执行，失败仅告警、绝不阻断应用启动。
     */
    @EventListener(ContextRefreshedEvent.class)
    public void autoIngestOnStartup() {
        if (!props.isVectorStoreEnabled()) {
            return;
        }
        if (vectorStore.count() > 0) {
            log.info("气象知识库已有 {} 篇向量，跳过启动自动入库", vectorStore.count());
            return;
        }
        new Thread(() -> {
            try {
                log.info("启动自动入库：知识库为空，开始向量化气象文档…");
                refresh();
            } catch (Exception e) {
                log.warn("启动自动入库失败（对话仍可使用工具与记忆）：{}", e.getMessage());
            }
        }, "knowledge-auto-ingest").start();
    }

    private List<KnowledgeDocument> loadMarkdownDocuments() {
        List<KnowledgeDocument> result = new ArrayList<>();
        Resource[] resources;
        try {
            resources = resolver.getResources(props.getKnowledgeBasePath() + "*.md");
        } catch (Exception e) {
            log.warn("未找到知识库文档（{}）：{}", props.getKnowledgeBasePath(), e.getMessage());
            return result;
        }
        Pattern heading = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);
        for (Resource res : resources) {
            try {
                String text = res.getContentAsString(StandardCharsets.UTF_8);
                if (text.isBlank()) {
                    continue;
                }
                String filename = res.getFilename();
                String category = filename == null ? "知识" : filename.replaceAll("\\.md$", "");
                Matcher m = heading.matcher(text);
                String title = m.find() ? m.group(1).trim() : category;

                List<String> chunks = splitIntoChunks(text);
                for (int i = 0; i < chunks.size(); i++) {
                    result.add(KnowledgeDocument.builder()
                            .id(category + "-" + i + "-" + UUID.randomUUID().toString().substring(0, 8))
                            .title(title)
                            .category(category)
                            .content(chunks.get(i))
                            .metadata(java.util.Map.of("source", category))
                            .build());
                }
            } catch (Exception e) {
                log.warn("读取知识文档失败 {}：{}", res.getFilename(), e.getMessage());
            }
        }
        return result;
    }

    /** 按段落聚合，切分为约 CHUNK_MAX_CHARS 的片段，保证段落不被切断。 */
    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String para : text.split("\\n\\s*\\n")) {
            para = para.trim();
            if (para.isBlank()) {
                continue;
            }
            if (current.length() + para.length() + 2 > CHUNK_MAX_CHARS && current.length() > 0) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }
            if (current.length() > 0) {
                current.append("\n\n");
            }
            current.append(para);
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }
}
