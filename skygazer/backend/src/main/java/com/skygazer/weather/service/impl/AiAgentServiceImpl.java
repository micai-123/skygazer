package com.skygazer.weather.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.config.AliyunAiProperties;
import com.skygazer.weather.dto.request.AgentQueryRequest;
import com.skygazer.weather.dto.response.AgentResponse;
import com.skygazer.weather.exception.AIModelException;
import com.skygazer.weather.rag.KnowledgeIngestionService;
import com.skygazer.weather.rag.RedisKnowledgeVectorStore;
import com.skygazer.weather.rag.RetrievedChunk;
import com.skygazer.weather.service.AiAgentService;
import com.skygazer.weather.tool.ExternalWeatherTools;
import com.skygazer.weather.tool.LifeIndexTools;
import com.skygazer.weather.tool.WeatherTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 混合智能体实现：RAG 检索 + 工具调用 + 多轮记忆，统一编排于 Spring AI {@link ChatClient}。
 */
@Slf4j
@Service
public class AiAgentServiceImpl implements AiAgentService {

    private static final String AGENT_NAME = "天象智囊";
    private static final String MEMORY_PREFIX = "agent:memory:";
    private static final String SYSTEM_IDENTITY =
            "你是「天象智囊」，智观天象平台的气象智能体。你具备专业的气象知识与数据分析能力，"
                    + "能够查询、分析天气，评估活动适宜性，并提供气象预警与生活建议。请以专业、友好、简洁的中文回答。\n"
                    + "当需要实时天气、外部预报、生活指数或气象预警时，请主动调用提供的工具获取最新数据后再作答；"
                    + "若下方提供参考知识，请优先基于参考知识作答并适当注明来源。";

    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final Executor taskExecutor;
    private final ToolCallingManager toolCallingManager;
    private final ToolCallback[] toolCallbacks;
    private final RedisKnowledgeVectorStore vectorStore;
    private final KnowledgeIngestionService ingestionService;
    private final WeatherTools weatherTools;
    private final ExternalWeatherTools externalTools;
    private final LifeIndexTools lifeTools;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final AliyunAiProperties props;

    public AiAgentServiceImpl(ChatClient.Builder chatClientBuilder,
                              ChatModel chatModel,
                              @Qualifier("taskExecutor") Executor taskExecutor,
                              ToolCallingManager toolCallingManager,
                              RedisKnowledgeVectorStore vectorStore,
                              KnowledgeIngestionService ingestionService,
                              WeatherTools weatherTools,
                              ExternalWeatherTools externalTools,
                              LifeIndexTools lifeTools,
                              StringRedisTemplate redis,
                              ObjectMapper objectMapper,
                              AliyunAiProperties props) {
        this.chatClient = chatClientBuilder.build();
        this.chatModel = chatModel;
        this.taskExecutor = taskExecutor;
        this.toolCallingManager = toolCallingManager != null
                ? toolCallingManager : DefaultToolCallingManager.builder().build();
        this.toolCallbacks = ToolCallbacks.from(weatherTools, externalTools, lifeTools);
        this.vectorStore = vectorStore;
        this.ingestionService = ingestionService;
        this.weatherTools = weatherTools;
        this.externalTools = externalTools;
        this.lifeTools = lifeTools;
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.props = props;
    }

    @Override
    public AgentResponse ask(AgentQueryRequest request) {
        String sessionId = normalizeSession(request.getSessionId());
        long start = System.currentTimeMillis();
        try {
            List<Message> history = loadMemory(sessionId);
            String userText = buildUserText(request);
            List<RetrievedChunk> refs = retrieve(request.getQuestion());
            String system = buildSystemPrompt(refs);

            ChatResponse response = chatClient.prompt()
                    .system(system)
                    .messages(history)
                    .user(userText)
                    .tools(weatherTools, externalTools, lifeTools)
                    .call()
                    .chatResponse();

            String answer = extractContent(response);
            saveMemory(sessionId, userText, answer);

            return AgentResponse.builder()
                    .answer(answer)
                    .agentName(AGENT_NAME)
                    .references(refs.stream().map(this::toReferenceString).collect(Collectors.toList()))
                    .referenceDetails(refs.stream().map(this::toReferenceDetail).collect(Collectors.toList()))
                    .sessionId(sessionId)
                    .modelUsed(props.getModel())
                    .responseTimeMs(System.currentTimeMillis() - start)
                    .build();
        } catch (AIModelException e) {
            throw e;
        } catch (Exception e) {
            log.error("Agent 问答异常", e);
            throw new AIModelException("智能体回答失败：" + e.getMessage(), e);
        }
    }

    @Override
    public SseEmitter streamAsk(AgentQueryRequest request) {
        String sessionId = normalizeSession(request.getSessionId());
        SseEmitter emitter = new SseEmitter(120_000L);
        // 在异步线程中执行模型调用与工具循环，避免阻塞请求线程并支持真正的流式推送
        taskExecutor.execute(() -> {
            try {
                List<Message> history = loadMemory(sessionId);
                String userText = buildUserText(request);
                List<RetrievedChunk> refs = retrieve(request.getQuestion());
                String system = buildSystemPrompt(refs);

                // RAG 检索步骤（若有）
                if (refs != null && !refs.isEmpty()) {
                    emitter.send(SseEmitter.event().name("step").data(stepJson(Map.of(
                            "type", "rag",
                            "label", "知识检索",
                            "detail", "从气象知识库检索到 " + refs.size() + " 条相关来源，用于增强回答的可靠性。",
                            "status", "done"
                    ))));
                }

                // 非流式智能体循环：可靠解析工具调用（规避 M6 流式空参崩溃），工具步骤实时下发
                String answer = resolveWithTools(system, history, userText, emitter);
                if (answer == null || answer.isBlank()) {
                    answer = "抱歉，我暂时无法获取相关信息，请稍后再试。";
                }
                final String finalAnswer = answer;

                // 将最终答案分块以 SSE data 帧流式下发
                for (String chunk : chunkText(finalAnswer)) {
                    emitter.send(SseEmitter.event().data(chunk));
                }
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
                saveMemory(sessionId, userText, finalAnswer);
            } catch (Exception e) {
                log.error("Agent 流式问答异常", e);
                try {
                    emitter.send(SseEmitter.event().data("抱歉，智能体处理出错：" + e.getMessage()));
                    emitter.send(SseEmitter.event().data("[DONE]"));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * 显式智能体循环：自行驱动工具调用（关闭模型内部执行），避免 Spring AI M6 流式模式下
     * 工具参数在分片聚合阶段为空导致的 {@code toolInput cannot be null or empty} 崩溃。
     * 工具调用过程以 step 事件实时推送给前端；返回最终答案文本。
     */
    private String resolveWithTools(String system, List<Message> history, String userText,
                                  SseEmitter emitter) {
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(system));
            messages.addAll(history);
            messages.add(new UserMessage(userText));

            ToolCallingChatOptions options = ToolCallingChatOptions.builder()
                    .toolCallbacks(toolCallbacks)
                    .internalToolExecutionEnabled(false)
                    .build();

            for (int round = 0; round < 5; round++) {
                Prompt prompt = new Prompt(messages, options);
                ChatResponse response = chatModel.call(prompt);
                AssistantMessage assistant = response.getResult().getOutput();
                if (assistant == null) {
                    return null;
                }
                if (!assistant.hasToolCalls()) {
                    return assistant.getText();
                }
                // 记录工具调用步骤，便于前端展示「工作过程」
                for (AssistantMessage.ToolCall tc : assistant.getToolCalls()) {
                    emitter.send(SseEmitter.event().name("step").data(stepJson(Map.of(
                            "type", "tool",
                            "label", "调用工具：" + tc.name(),
                            "detail", "参数：" + tc.arguments(),
                            "status", "done"
                    ))));
                }
                // 把助手消息（含工具调用）加入上下文，由管理器执行工具并把结果回填
                messages.add(assistant);
                ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, response);
                messages = new ArrayList<>(result.conversationHistory());
            }
            return "抱歉，智能体未能在限定步骤内完成回答。";
        } catch (Exception e) {
            log.warn("工具调用失败，降级为无工具回答：{}", e.getMessage());
            try {
                ChatResponse response = chatClient.prompt()
                        .system(system).messages(history).user(userText).call().chatResponse();
                return extractContent(response);
            } catch (Exception ex) {
                throw new AIModelException("智能体回答失败：" + ex.getMessage(), ex);
            }
        }
    }

    /** 将正文切分为适合流式逐字展示的小块（按字符，最多 4 个）。 */
    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        int n = text.length();
        int i = 0;
        while (i < n) {
            int end = Math.min(i + 4, n);
            chunks.add(text.substring(i, end));
            i = end;
        }
        return chunks;
    }

    /** 将结构化步骤对象序列化为 JSON 字符串（作为 SSE step 事件的 data）。 */
    private String stepJson(Map<String, Object> step) {
        try {
            return objectMapper.writeValueAsString(step);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public void refreshKnowledge() {
        ingestionService.refresh();
    }

    // ===================== 内部辅助 =====================

    private String normalizeSession(String sessionId) {
        return (sessionId != null && !sessionId.isBlank()) ? sessionId : UUID.randomUUID().toString();
    }

    private String buildUserText(AgentQueryRequest request) {
        StringBuilder sb = new StringBuilder();
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            sb.append("用户所在位置：").append(request.getLocation()).append("。\n");
        }
        sb.append(request.getQuestion());
        if (request.getContext() != null && !request.getContext().isBlank()) {
            sb.append("\n补充上下文：").append(request.getContext());
        }
        return sb.toString();
    }

    private List<RetrievedChunk> retrieve(String question) {
        if (!props.isVectorStoreEnabled()) {
            return List.of();
        }
        try {
            return vectorStore.search(question, props.getRagTopK(), props.getRagSimilarityThreshold());
        } catch (Exception e) {
            log.warn("RAG 检索失败，降级为纯工具/模型回答：{}", e.getMessage());
            return List.of();
        }
    }

    private String buildSystemPrompt(List<RetrievedChunk> refs) {
        if (refs == null || refs.isEmpty()) {
            return SYSTEM_IDENTITY;
        }
        StringBuilder kb = new StringBuilder();
        kb.append("\n\n以下是气象知识库相关资料（仅供参考，回答时请注明来源标题）：\n");
        for (RetrievedChunk c : refs) {
            kb.append("【").append(c.getTitle() == null ? "知识" : c.getTitle()).append("】")
                    .append(c.getContent()).append("\n\n");
        }
        return SYSTEM_IDENTITY + kb;
    }

    private String extractContent(ChatResponse response) {
        if (response == null || response.getResult() == null
                || response.getResult().getOutput() == null) {
            throw new AIModelException("模型返回结果为空");
        }
        return response.getResult().getOutput().getText();
    }

    private String toReferenceString(RetrievedChunk c) {
        String title = c.getTitle() == null ? "知识" : c.getTitle();
        String snippet = c.getContent() == null ? "" : c.getContent();
        if (snippet.length() > 120) {
            snippet = snippet.substring(0, 120) + "…";
        }
        return "【" + title + "】" + snippet;
    }

    private AgentResponse.ReferenceDetail toReferenceDetail(RetrievedChunk c) {
        String snippet = c.getContent() == null ? "" : c.getContent();
        if (snippet.length() > 200) {
            snippet = snippet.substring(0, 200) + "…";
        }
        return AgentResponse.ReferenceDetail.builder()
                .id(c.getId())
                .title(c.getTitle())
                .category(c.getCategory())
                .snippet(snippet)
                .build();
    }

    // ---- 多轮记忆（Redis 后端，按 sessionId 隔离，窗口截断） ----

    private List<Message> loadMemory(String sessionId) {
        String raw = redis.opsForValue().get(MEMORY_PREFIX + sessionId);
        if (raw == null) {
            return List.of();
        }
        try {
            ChatMessageRecord[] arr = objectMapper.readValue(raw, ChatMessageRecord[].class);
            List<Message> msgs = new ArrayList<>();
            for (ChatMessageRecord r : arr) {
                if ("user".equals(r.role)) {
                    msgs.add(new UserMessage(r.content));
                } else if ("assistant".equals(r.role)) {
                    msgs.add(new AssistantMessage(r.content));
                }
            }
            return msgs;
        } catch (Exception e) {
            log.warn("读取会话记忆失败 session={}：{}", sessionId, e.getMessage());
            return List.of();
        }
    }

    private void saveMemory(String sessionId, String userText, String assistantText) {
        List<ChatMessageRecord> list = new ArrayList<>();
        String raw = redis.opsForValue().get(MEMORY_PREFIX + sessionId);
        if (raw != null) {
            try {
                Collections.addAll(list, objectMapper.readValue(raw, ChatMessageRecord[].class));
            } catch (Exception ignored) {
            }
        }
        list.add(new ChatMessageRecord("user", userText));
        list.add(new ChatMessageRecord("assistant", assistantText));
        int max = props.getMemoryMaxMessages();
        if (list.size() > max) {
            list = new ArrayList<>(list.subList(list.size() - max, list.size()));
        }
        try {
            redis.opsForValue().set(MEMORY_PREFIX + sessionId, objectMapper.writeValueAsString(list));
        } catch (Exception e) {
            log.warn("写入会话记忆失败 session={}：{}", sessionId, e.getMessage());
        }
    }

    /** 记忆记录（与 Spring AI Message 解耦，便于 JSON 存储）。 */
    private record ChatMessageRecord(String role, String content) {
    }
}
