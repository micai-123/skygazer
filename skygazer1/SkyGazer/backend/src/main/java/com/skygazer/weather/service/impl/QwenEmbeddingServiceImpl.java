package com.skygazer.weather.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.service.QwenEmbeddingService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class QwenEmbeddingServiceImpl implements QwenEmbeddingService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.ai.openai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;
    
    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;
    
    @Value("${spring.ai.openai.embedding.options.model:text-embedding-v3}")
    private String embeddingModel;
    
    public QwenEmbeddingServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters()
            .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public List<Double> embed(String text) {
        try {
            String url = baseUrl + "/embeddings";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            headers.setBearerAuth(apiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", embeddingModel);
            requestBody.put("input", text);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.debug("调用Qwen Embedding API: {}", url);
            
            String response = restTemplate.postForObject(url, request, String.class);
            
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode data = root.path("data");
                if (data.isArray() && data.size() > 0) {
                    JsonNode embedding = data.get(0).path("embedding");
                    List<Double> result = new ArrayList<>();
                    for (JsonNode node : embedding) {
                        result.add(node.asDouble());
                    }
                    return result;
                }
            }
            
            log.error("Embedding API响应格式错误: {}", response);
            return null;
            
        } catch (Exception e) {
            log.error("调用Qwen Embedding API失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
