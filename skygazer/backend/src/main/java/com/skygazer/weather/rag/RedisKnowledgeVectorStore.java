package com.skygazer.weather.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.config.AliyunAiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于现有 Redis 的轻量向量知识库。
 * <p>
 * 复用项目既有的 Redis 实例（无需 RediSearch 模块），将文档向量以 JSON 形式存储于 Hash 中，
 * 检索时对全部向量做暴力余弦相似度计算。适用于课程级知识库规模（百~千级文档）。
 * </p>
 * <p>
 * 说明：Spring AI 官方的 {@code RedisVectorStore} 依赖 RediSearch 模块，而本项目的 M6 里程碑仓库未发布
 * 该模块，故此处自研一个等价实现，同样"复用现有 Redis"。如需切换为官版，仅需替换本类实现。
 * </p>
 */
@Slf4j
@Component
public class RedisKnowledgeVectorStore {

    private final StringRedisTemplate redis;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;
    private final AliyunAiProperties props;

    private static final String DOC_KEY_PREFIX = "rag:doc:";
    private static final String DOC_INDEX = "rag:docs:all";
    private static final String VERSION_KEY = "rag:version";

    public RedisKnowledgeVectorStore(StringRedisTemplate redis,
                                     EmbeddingModel embeddingModel,
                                     ObjectMapper objectMapper,
                                     AliyunAiProperties props) {
        this.redis = redis;
        this.embeddingModel = embeddingModel;
        this.objectMapper = objectMapper;
        this.props = props;
    }

    /** 向量化并写入一篇文档。 */
    public void add(KnowledgeDocument doc) {
        if (doc == null || doc.getContent() == null || doc.getContent().isBlank()) {
            return;
        }
        float[] vector = embeddingModel.embed(doc.getContent());
        Map<String, String> hash = new HashMap<>();
        hash.put("id", doc.getId());
        hash.put("title", doc.getTitle() == null ? "" : doc.getTitle());
        hash.put("category", doc.getCategory() == null ? "" : doc.getCategory());
        hash.put("content", doc.getContent());
        hash.put("metadata", toJson(doc.getMetadata()));
        hash.put("vector", toJson(vector));
        redis.opsForHash().putAll(DOC_KEY_PREFIX + doc.getId(), hash);
        redis.opsForSet().add(DOC_INDEX, doc.getId());
    }

    /** 检索与 query 最相似的前 topK 篇文档（按余弦相似度阈值过滤）。 */
    public List<RetrievedChunk> search(String query, int topK, double threshold) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        float[] queryVector;
        try {
            queryVector = embeddingModel.embed(query);
        } catch (Exception e) {
            log.warn("查询向量化失败，跳过 RAG：{}", e.getMessage());
            return List.of();
        }
        Set<String> ids = redis.opsForSet().members(DOC_INDEX);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<RetrievedChunk> scored = new ArrayList<>();
        for (String id : ids) {
            Map<Object, Object> h = redis.opsForHash().entries(DOC_KEY_PREFIX + id);
            if (h == null || h.isEmpty()) {
                continue;
            }
            float[] vec = parseVector((String) h.get("vector"));
            if (vec == null) {
                continue;
            }
            double sim = cosineSimilarity(queryVector, vec);
            if (sim >= threshold) {
                RetrievedChunk chunk = new RetrievedChunk();
                chunk.setId(id);
                chunk.setTitle((String) h.get("title"));
                chunk.setCategory((String) h.get("category"));
                chunk.setContent((String) h.get("content"));
                chunk.setScore(sim);
                chunk.setMetadata(fromJson((String) h.get("metadata")));
                scored.add(chunk);
            }
        }
        scored.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return scored.stream().limit(topK).toList();
    }

    public int count() {
        Long size = redis.opsForSet().size(DOC_INDEX);
        return size == null ? 0 : size.intValue();
    }

    /** 清空知识库（重新入库前调用）。 */
    public void clear() {
        Set<String> ids = redis.opsForSet().members(DOC_INDEX);
        if (ids != null) {
            for (String id : ids) {
                redis.delete(DOC_KEY_PREFIX + id);
            }
        }
        redis.delete(DOC_INDEX);
    }

    /** 递增知识库版本，便于幂等判断与排障。 */
    public void bumpVersion() {
        redis.opsForValue().increment(VERSION_KEY);
    }

    public String getVersion() {
        return redis.opsForValue().get(VERSION_KEY);
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0 || a.length != b.length) {
            return -1d;
        }
        double dot = 0d, na = 0d, nb = 0d;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0d || nb == 0d) {
            return 0d;
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private String toJson(Object obj) {
        try {
            return obj == null ? "{}" : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private float[] parseVector(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            Double[] arr = objectMapper.readValue(json, Double[].class);
            float[] f = new float[arr.length];
            for (int i = 0; i < arr.length; i++) {
                f[i] = arr[i].floatValue();
            }
            return f;
        } catch (Exception e) {
            return null;
        }
    }
}
