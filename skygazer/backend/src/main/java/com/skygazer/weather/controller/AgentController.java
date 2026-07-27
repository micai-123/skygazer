package com.skygazer.weather.controller;

import com.skygazer.weather.config.AliyunAiProperties;
import com.skygazer.weather.dto.request.AgentQueryRequest;
import com.skygazer.weather.dto.response.AgentResponse;
import com.skygazer.weather.exception.AIModelException;
import com.skygazer.weather.service.AiAgentService;
import com.skygazer.weather.service.AliyunAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 气象智能体「天象智囊」接口（对应前端 agentApi）。
 * <p>
 * /query、/query/stream、/knowledge/refresh 已升级为混合智能体（RAG + 工具 + 记忆）；
 * /analyze、/activity-advice、/alert 仍走轻量对话通道，保持契约兼容。
 * </p>
 */
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final AiAgentService aiAgentService;
    private final AliyunAiService aiService;
    private final AliyunAiProperties props;

    private static final String SYSTEM_PROMPT =
            "你是「天象智囊」，智观天象平台的气象智能体。你具备专业的气象知识与数据分析能力，"
                    + "能够查询、分析天气，评估活动适宜性，并提供气象预警与生活建议。"
                    + "请以「天象智囊」的身份用中文专业、友好地回答用户问题。";

    @PostMapping("/query")
    public AgentResponse query(@RequestBody AgentQueryRequest request) {
        return aiAgentService.ask(request);
    }

    @PostMapping(value = "/query/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamQuery(@RequestBody AgentQueryRequest request) {
        return aiAgentService.streamAsk(request);
    }

    @GetMapping("/analyze")
    public AgentResponse analyzeWeather(@RequestParam String location) {
        String answer = aiService.chat(SYSTEM_PROMPT, "请对「" + location + "」的天气状况进行专业分析与解读。");
        return buildAgentResponse(answer);
    }

    @GetMapping("/activity-advice")
    public AgentResponse getActivityAdvice(@RequestParam String location, @RequestParam String activity) {
        String answer = aiService.chat(SYSTEM_PROMPT,
                "当前位置：" + location + "，计划活动：" + activity + "。请评估天气适宜性并给出建议。");
        return buildAgentResponse(answer);
    }

    @GetMapping("/alert")
    public AgentResponse getWeatherAlert(@RequestParam String location) {
        String answer = aiService.chat(SYSTEM_PROMPT,
                "请针对「" + location + "」给出当前需要关注的气象预警与生活提示。");
        return buildAgentResponse(answer);
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "status", "ok",
                "agent", "天象智囊",
                "model", aiService.getModel()
        );
    }

    /**
     * 触发知识库重建（RAG 入库管线）。失败不阻断主流程，返回友好提示。
     */
    @PostMapping("/knowledge/refresh")
    public Map<String, Object> refreshKnowledge() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            aiAgentService.refreshKnowledge();
            result.put("status", "ok");
            result.put("message", "知识库已重建并向量化完成");
        } catch (AIModelException e) {
            log.error("知识库刷新失败", e);
            result.put("status", "degraded");
            result.put("message", "知识库刷新失败（对话仍可使用工具与记忆）：" + e.getMessage());
        } catch (Exception e) {
            log.error("知识库刷新异常", e);
            result.put("status", "degraded");
            result.put("message", "知识库刷新异常：" + e.getMessage());
        }
        return result;
    }

    private AgentResponse buildAgentResponse(String answer) {
        return AgentResponse.builder()
                .answer(answer)
                .agentName("天象智囊")
                .references(Collections.emptyList())
                .sessionId(UUID.randomUUID().toString())
                .modelUsed(props.getModel())
                .build();
    }
}
