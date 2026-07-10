package com.skygazer.weather.service;

import com.skygazer.weather.dto.request.ChatRequest;
import com.skygazer.weather.dto.request.ImageAnalysisRequest;
import com.skygazer.weather.dto.response.ChatResponse;
import reactor.core.publisher.Flux;

public interface AIService {
    
    ChatResponse chat(ChatRequest request);
    
    Flux<String> streamChat(ChatRequest request);
    
    ChatResponse analyzeImage(ImageAnalysisRequest request);
    
    ChatResponse generateWeatherStory(String location, String style);
    
    ChatResponse getDecisionAdvice(String location, String scenario);
}
