package com.skygazer.weather.controller;

import com.skygazer.weather.dto.request.AgentQueryRequest;
import com.skygazer.weather.dto.response.AgentResponse;
import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.service.KnowledgeBaseService;
import com.skygazer.weather.service.WeatherAgentService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class WeatherAgentController {
    
    private final WeatherAgentService weatherAgentService;
    private final KnowledgeBaseService knowledgeBaseService;
    
    @PostMapping("/query")
    public ApiResponse<AgentResponse> query(@Valid @RequestBody AgentQueryRequest request) {
        AgentResponse response = weatherAgentService.queryWithContext(
            request.getQuestion(),
            request.getLocation(),
            request.getContext(),
            request.getSessionId()
        );
        return ApiResponse.success(response);
    }
    
    @PostMapping(value = "/query/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamQuery(@Valid @RequestBody AgentQueryRequest request) {
        return weatherAgentService.streamQuery(
            request.getQuestion(),
            request.getLocation(),
            request.getSessionId()
        )
        .map(content -> ServerSentEvent.<String>builder()
            .data(content)
            .build())
        .concatWithValues(ServerSentEvent.<String>builder()
            .data("[DONE]")
            .build());
    }
    
    @GetMapping("/analyze")
    public ApiResponse<AgentResponse> analyzeWeather(@RequestParam String location) {
        AgentResponse response = weatherAgentService.analyzeWeather(location);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/activity-advice")
    public ApiResponse<AgentResponse> getActivityAdvice(
        @RequestParam String location,
        @RequestParam String activity
    ) {
        AgentResponse response = weatherAgentService.getActivityAdvice(location, activity);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/alert")
    public ApiResponse<AgentResponse> getWeatherAlert(@RequestParam String location) {
        AgentResponse response = weatherAgentService.getWeatherAlert(location);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("agentName", "天象智囊");
        status.put("version", "1.0.0");
        status.put("status", "online");
        status.put("capabilities", new String[]{
            "智能问答",
            "天气分析",
            "活动建议",
            "预警提醒"
        });
        return ApiResponse.success(status);
    }
    
    @PostMapping("/knowledge/refresh")
    public ApiResponse<Map<String, Object>> refreshKnowledge() {
        knowledgeBaseService.refreshKnowledgeBase();
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "知识库刷新成功");
        result.put("timestamp", System.currentTimeMillis());
        return ApiResponse.success(result);
    }
}
