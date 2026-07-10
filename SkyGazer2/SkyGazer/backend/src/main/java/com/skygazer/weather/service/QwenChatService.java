package com.skygazer.weather.service;

import reactor.core.publisher.Flux;

public interface QwenChatService {
    String chat(String systemPrompt, String userPrompt);
    
    Flux<String> streamChat(String systemPrompt, String userPrompt);
}
