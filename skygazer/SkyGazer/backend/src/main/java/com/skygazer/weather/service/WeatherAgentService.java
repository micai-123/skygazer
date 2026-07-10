package com.skygazer.weather.service;

import com.skygazer.weather.dto.request.ChatRequest;
import com.skygazer.weather.dto.response.AgentResponse;
import reactor.core.publisher.Flux;

public interface WeatherAgentService {
    
    AgentResponse query(String question, String location, String sessionId);
    
    AgentResponse queryWithContext(String question, String location, String context, String sessionId);
    
    Flux<String> streamQuery(String question, String location, String sessionId);
    
    AgentResponse analyzeWeather(String location);
    
    AgentResponse getActivityAdvice(String location, String activity);
    
    AgentResponse getWeatherAlert(String location);
}
