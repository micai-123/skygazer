package com.skygazer.weather.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.service.QwenChatService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class QwenChatServiceImpl implements QwenChatService {
    
    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.ai.openai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;
    
    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;
    
    @Value("${spring.ai.openai.chat.options.model:qwen-plus}")
    private String model;
    
    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private Double temperature;
    
    @Value("${spring.ai.openai.chat.options.max-tokens:2000}")
    private Integer maxTokens;
    
    public QwenChatServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters()
            .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();
    }
    
    @Override
    public String chat(String systemPrompt, String userPrompt) {
        try {
            String url = baseUrl + "/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            headers.setBearerAuth(apiKey);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messages.add(systemMessage);
            }
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("调用Qwen API: {}, model: {}", url, model);
            
            String response = restTemplate.postForObject(url, request, String.class);
            
            log.info("Qwen API响应: {}", response != null ? "成功" : "失败");
            
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText();
                }
            }
            
            log.error("API响应格式错误: {}", response);
            return null;
            
        } catch (Exception e) {
            log.error("调用Qwen API失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public Flux<String> streamChat(String systemPrompt, String userPrompt) {
        String url = baseUrl + "/chat/completions";
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
        }
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("stream", true);
        
        log.info("调用Qwen流式API: {}, model: {}", url, model);
        
        return webClient.post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToFlux(String.class)
            .doOnNext(line -> log.info("收到SSE行: {}", line))
            .filter(line -> !line.trim().isEmpty() && !"[DONE]".equals(line.trim()))
            .flatMap(jsonStr -> {
                try {
                    log.info("解析JSON: {}", jsonStr);
                    JsonNode root = objectMapper.readTree(jsonStr);
                    JsonNode choices = root.path("choices");
                    if (choices.isArray() && choices.size() > 0) {
                        JsonNode delta = choices.get(0).path("delta");
                        String content = delta.path("content").asText("");
                        if (!content.isEmpty()) {
                            log.debug("提取内容: {}", content);
                            return Flux.just(content);
                        }
                    }
                } catch (Exception e) {
                    log.error("解析流式响应失败: {}", e.getMessage());
                }
                return Flux.empty();
            })
            .doOnComplete(() -> log.info("流式响应完成"))
            .doOnError(e -> log.error("流式响应错误: {}", e.getMessage()));
    }
}
