package com.skygazer.weather.controller;

import com.skygazer.weather.dto.request.ChatRequest;
import com.skygazer.weather.dto.request.ImageAnalysisRequest;
import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.dto.response.ChatResponse;
import com.skygazer.weather.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {
    
    private final AIService aiService;
    
    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = aiService.chat(request);
        return ApiResponse.success(response);
    }
    
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest request) {
        return aiService.streamChat(request);
    }
    
    @PostMapping("/analyze-image")
    public ApiResponse<ChatResponse> analyzeImage(@Valid @RequestBody ImageAnalysisRequest request) {
        ChatResponse response = aiService.analyzeImage(request);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/weather-story")
    public ApiResponse<ChatResponse> generateWeatherStory(@RequestParam String location,
                                                          @RequestParam(defaultValue = "normal") String style) {
        ChatResponse response = aiService.generateWeatherStory(location, style);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/decision-advice")
    public ApiResponse<ChatResponse> getDecisionAdvice(@RequestParam String location,
                                                       @RequestParam String scenario) {
        ChatResponse response = aiService.getDecisionAdvice(location, scenario);
        return ApiResponse.success(response);
    }
}
