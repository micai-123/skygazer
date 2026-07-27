package com.skygazer.weather.controller;

import com.skygazer.weather.config.AliyunAiProperties;
import com.skygazer.weather.dto.request.ChatRequest;
import com.skygazer.weather.dto.response.ChatResponse;
import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.dto.response.WeatherImagePredictResponse;
import com.skygazer.weather.exception.BusinessException;
import com.skygazer.weather.exception.ErrorCode;
import com.skygazer.weather.service.AliyunAiService;
import com.skygazer.weather.service.WeatherImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Base64;
import java.util.Map;

/**
 * 通用 AI 对话接口（对应前端 aiApi）。
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AliyunAiService aiService;
    private final AliyunAiProperties props;
    private final WeatherImageService weatherImageService;

    private static final String SYSTEM_PROMPT =
            "你是由「智观天象(SkyGazer)」打造的气象AI助手，擅长解答天气、气候、气象灾害、"
                    + "穿衣与出行建议、气象数据分析等相关问题。回答应专业、简洁、友好，使用中文。";

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = aiService.chat(SYSTEM_PROMPT, buildUserMessage(request));
        return ChatResponse.builder()
                .message(answer)
                .style(request.getStyle())
                .responseTimeMs(0L)
                .modelUsed(props.getModel())
                .build();
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
        return aiService.stream(SYSTEM_PROMPT, buildUserMessage(request));
    }

    @PostMapping("/weather-story")
    public ChatResponse weatherStory(@RequestParam String location,
                                     @RequestParam(required = false, defaultValue = "诗意") String style) {
        String sys = "你是一位擅长用" + style + "风格描写天气与自然的作家，文笔优美、富有画面感。";
        String answer = aiService.chat(sys, "请为「" + location + "」当前的天气创作一段" + style + "风格的短文。");
        return ChatResponse.builder()
                .message(answer)
                .style(style)
                .modelUsed(props.getModel())
                .build();
    }

    @PostMapping("/decision-advice")
    public ChatResponse decisionAdvice(@RequestParam String location, @RequestParam String scenario) {
        String sys = "你是一位专业的气象决策顾问，能够根据天气状况评估活动适宜性并给出可执行建议。";
        String answer = aiService.chat(sys,
                "当前位置：" + location + "，计划活动：" + scenario + "。请评估天气是否适宜，并给出具体建议。");
        return ChatResponse.builder()
                .message(answer)
                .modelUsed(props.getModel())
                .build();
    }

    @PostMapping("/analyze-image")
    public ApiResponse<WeatherImagePredictResponse> analyzeImage(
            @RequestBody(required = false) Map<String, Object> body) {
        if (body == null || body.get("imageBase64") == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "缺少 imageBase64 字段");
        }
        String base64 = String.valueOf(body.get("imageBase64"));
        int comma = base64.indexOf(',');
        if (comma >= 0) {
            base64 = base64.substring(comma + 1);
        }
        byte[] bytes = Base64.getDecoder().decode(base64);
        return ApiResponse.success("识别完成", weatherImageService.predict(bytes, "upload.png"));
    }

    private String buildUserMessage(ChatRequest request) {
        StringBuilder sb = new StringBuilder();
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            sb.append("用户所在位置：").append(request.getLocation()).append("。\n");
        }
        sb.append(request.getMessage());
        if (request.getContext() != null && !request.getContext().isBlank()) {
            sb.append("\n补充上下文：").append(request.getContext());
        }
        return sb.toString();
    }
}
