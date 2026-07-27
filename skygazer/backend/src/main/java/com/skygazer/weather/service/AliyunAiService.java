package com.skygazer.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.config.AliyunAiProperties;
import com.skygazer.weather.exception.AIModelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 阿里云百炼 MaaS（OpenAI 兼容模式）AI 调用服务。
 * 通过 /chat/completions 提供对话能力，支持流式与非流式两种模式。
 */
@Slf4j
@Service
public class AliyunAiService {

    private final WebClient aiWebClient;
    private final AliyunAiProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final ParameterizedTypeReference<ServerSentEvent<String>> SSE_TYPE =
            new ParameterizedTypeReference<ServerSentEvent<String>>() {};

    public AliyunAiService(@Qualifier("aliyunAiWebClient") WebClient aiWebClient,
                           AliyunAiProperties props) {
        this.aiWebClient = aiWebClient;
        this.props = props;
    }

    /** 当前生效的模型名（供状态接口/日志使用） */
    public String getModel() {
        return props.getModel();
    }

    private Map<String, Object> buildBody(String system, String user, boolean stream) {
        Map<String, Object> systemMsg = Map.of("role", "system", "content", system);
        Map<String, Object> userMsg = Map.of("role", "user", "content", user);
        return Map.of(
                "model", props.getModel(),
                "messages", List.of(systemMsg, userMsg),
                "stream", stream,
                "temperature", props.getTemperature(),
                "max_tokens", props.getMaxTokens()
        );
    }

    /**
     * 非流式对话。
     *
     * @param system 系统提示词
     * @param user   用户输入
     * @return 模型完整回复文本
     */
    public String chat(String system, String user) {
        try {
            String raw = aiWebClient.post()
                    .uri("/chat/completions")
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(buildBody(system, user, false)))
                    .retrieve()
                    .onStatus(status -> status.isError(),
                            resp -> resp.bodyToMono(String.class)
                                    .flatMap(err -> Mono.error(new AIModelException("阿里云AI调用失败: " + err))))
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(raw);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode()) {
                throw new AIModelException("阿里云AI返回结果异常: " + raw);
            }
            return content.asText();
        } catch (AIModelException e) {
            throw e;
        } catch (Exception e) {
            log.error("阿里云AI对话调用异常", e);
            throw new AIModelException("调用阿里云AI模型时发生错误", e);
        }
    }

    /**
     * 流式对话，逐片段返回增量内容。
     *
     * @param system 系统提示词
     * @param user   用户输入
     * @return 内容增量片段流
     */
    public Flux<String> stream(String system, String user) {
        return aiWebClient.post()
                .uri("/chat/completions")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .body(BodyInserters.fromValue(buildBody(system, user, true)))
                .retrieve()
                .onStatus(status -> status.isError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new AIModelException("阿里云AI流式调用失败: " + err))))
                .bodyToFlux(SSE_TYPE)
                .map(sse -> {
                    String data = sse.data();
                    if (data == null || data.equals("[DONE]")) {
                        return null;
                    }
                    try {
                        JsonNode node = objectMapper.readTree(data);
                        JsonNode content = node.path("choices").path(0).path("delta").path("content");
                        return content.isMissingNode() ? null : content.asText();
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .filter(s -> s != null && !s.isEmpty());
    }
}
