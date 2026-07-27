package com.skygazer.weather.service;

import com.skygazer.weather.dto.request.AgentQueryRequest;
import com.skygazer.weather.dto.response.AgentResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 气象混合智能体「天象智囊」服务门面。
 * <p>
 * 能力：RAG 检索增强（Redis 向量库）+ 工具调用（实时天气/预报/生活指数/预警）+ 多轮会话记忆。
 * </p>
 */
public interface AiAgentService {

    /** 非流式问答，返回含引用来源的完整响应。 */
    AgentResponse ask(AgentQueryRequest request);

    /** 流式问答，逐片段返回增量文本（SSE，使用 SseEmitter 正确序列化）。 */
    SseEmitter streamAsk(AgentQueryRequest request);

    /** 触发知识库重建（对接 /agent/knowledge/refresh）。 */
    void refreshKnowledge();
}
