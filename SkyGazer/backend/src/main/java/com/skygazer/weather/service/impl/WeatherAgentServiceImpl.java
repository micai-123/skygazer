package com.skygazer.weather.service.impl;

import com.skygazer.weather.constant.PromptConstants;
import com.skygazer.weather.dto.response.AgentResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.entity.VectorKnowledge;
import com.skygazer.weather.service.KnowledgeBaseService;
import com.skygazer.weather.service.QwenChatService;
import com.skygazer.weather.service.VectorStoreService;
import com.skygazer.weather.service.WeatherAgentService;
import com.skygazer.weather.service.WeatherService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherAgentServiceImpl implements WeatherAgentService {
    
    private static final String AGENT_NAME = "天象智囊";
    private static final int DEFAULT_RETRIEVE_K = 5;
    
    private final QwenChatService qwenChatService;
    private final VectorStoreService vectorStoreService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final WeatherService weatherService;
    
    @Value("${spring.ai.openai.chat.options.model:qwen-plus}")
    private String defaultModel;
    
    @Override
    public AgentResponse query(String question, String location, String sessionId) {
        return queryWithContext(question, location, null, sessionId);
    }
    
    @Override
    public AgentResponse queryWithContext(String question, String location, String context, String sessionId) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<VectorKnowledge> relevantDocs = knowledgeBaseService.searchKnowledge(question, DEFAULT_RETRIEVE_K);
            
            String weatherContext = buildWeatherContext(location);
            
            String knowledgeContext = buildKnowledgeContext(relevantDocs);
            
            String systemPrompt = buildAgentSystemPrompt();
            
            String userPrompt = buildUserPrompt(question, weatherContext, knowledgeContext, context);
            
            String response = qwenChatService.chat(systemPrompt, userPrompt);
            
            if (response == null) {
                throw new RuntimeException("AI服务返回空响应");
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            List<String> references = relevantDocs.stream()
                .map(doc -> doc.getTitle())
                .collect(Collectors.toList());
            
            List<AgentResponse.ReferenceDetail> referenceDetails = relevantDocs.stream()
                .map(doc -> AgentResponse.ReferenceDetail.builder()
                    .id(doc.getId())
                    .title(doc.getTitle())
                    .category(doc.getCategory())
                    .snippet(truncateSnippet(doc.getContent(), 100))
                    .build())
                .collect(Collectors.toList());
            
            AgentResponse.WeatherContext weatherCtx = buildWeatherContextResponse(location);
            
            return AgentResponse.builder()
                .answer(response)
                .agentName(AGENT_NAME)
                .references(references)
                .referenceDetails(referenceDetails)
                .confidence(calculateConfidence(relevantDocs))
                .responseTimeMs(responseTime)
                .modelUsed(defaultModel)
                .sessionId(sessionId != null ? sessionId : UUID.randomUUID().toString())
                .weatherContext(weatherCtx)
                .build();
            
        } catch (Exception e) {
            log.error("智能体查询失败: {}", e.getMessage(), e);
            return AgentResponse.builder()
                .answer("抱歉，天象智囊暂时无法回答您的问题，请稍后再试。")
                .agentName(AGENT_NAME)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .sessionId(sessionId != null ? sessionId : UUID.randomUUID().toString())
                .build();
        }
    }
    
    @Override
    public Flux<String> streamQuery(String question, String location, String sessionId) {
        List<VectorKnowledge> relevantDocs = knowledgeBaseService.searchKnowledge(question, DEFAULT_RETRIEVE_K);
        
        String weatherContext = buildWeatherContext(location);
        String knowledgeContext = buildKnowledgeContext(relevantDocs);
        String systemPrompt = buildAgentSystemPrompt();
        String userPrompt = buildUserPrompt(question, weatherContext, knowledgeContext, null);
        
        return qwenChatService.streamChat(systemPrompt, userPrompt);
    }
    
    @Override
    public AgentResponse analyzeWeather(String location) {
        long startTime = System.currentTimeMillis();
        
        try {
            WeatherResponse weather = weatherService.getCurrentWeather(location);
            
            String analysisPrompt = buildWeatherAnalysisPrompt(weather);
            
            List<VectorKnowledge> relevantDocs = knowledgeBaseService.searchKnowledge(
                "天气分析 " + weather.getWeatherCondition() + " " + location, 
                DEFAULT_RETRIEVE_K
            );
            
            String knowledgeContext = buildKnowledgeContext(relevantDocs);
            
            String response = qwenChatService.chat(PromptConstants.PROFESSIONAL_SYSTEM_PROMPT, 
                analysisPrompt + "\n\n相关知识：\n" + knowledgeContext);
            
            if (response == null) {
                throw new RuntimeException("AI服务返回空响应");
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return AgentResponse.builder()
                .answer(response)
                .agentName(AGENT_NAME)
                .weatherContext(buildWeatherContextFromResponse(weather))
                .responseTimeMs(responseTime)
                .modelUsed(defaultModel)
                .sessionId(UUID.randomUUID().toString())
                .build();
            
        } catch (Exception e) {
            log.error("天气分析失败: {}", e.getMessage(), e);
            return AgentResponse.builder()
                .answer("抱歉，无法获取天气数据进行分析。")
                .agentName(AGENT_NAME)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .build();
        }
    }
    
    @Override
    public AgentResponse getActivityAdvice(String location, String activity) {
        long startTime = System.currentTimeMillis();
        
        try {
            WeatherResponse weather = weatherService.getCurrentWeather(location);
            
            List<VectorKnowledge> relevantDocs = knowledgeBaseService.searchKnowledgeByCategory(
                activity + "活动建议", 
                "activity_advice", 
                3
            );
            
            String knowledgeContext = buildKnowledgeContext(relevantDocs);
            
            String prompt = String.format("""
                用户想要进行的活动：%s
                
                当前天气情况：
                - 地点：%s
                - 天气：%s
                - 温度：%s℃（体感温度：%s℃）
                - 湿度：%s%%
                - 风速：%s m/s
                - 空气质量：%s（AQI: %d）
                - 紫外线指数：%d
                
                相关知识：
                %s
                
                请根据以上信息，为用户提供专业的活动建议，包括：
                1. 是否适合进行该活动
                2. 需要做哪些准备
                3. 注意事项
                """,
                activity,
                weather.getLocation(),
                weather.getWeatherCondition(),
                weather.getTemperature(),
                weather.getFeelsLike(),
                weather.getHumidity(),
                weather.getWindSpeed(),
                weather.getAirQualityLevel(),
                weather.getAirQualityIndex(),
                weather.getUvIndex(),
                knowledgeContext
            );
            
            String response = qwenChatService.chat(PromptConstants.DECISION_ADVISOR_SYSTEM, prompt);
            
            if (response == null) {
                throw new RuntimeException("AI服务返回空响应");
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            List<String> references = relevantDocs.stream()
                .map(VectorKnowledge::getTitle)
                .collect(Collectors.toList());
            
            return AgentResponse.builder()
                .answer(response)
                .agentName(AGENT_NAME)
                .references(references)
                .weatherContext(buildWeatherContextFromResponse(weather))
                .responseTimeMs(responseTime)
                .modelUsed(defaultModel)
                .sessionId(UUID.randomUUID().toString())
                .build();
            
        } catch (Exception e) {
            log.error("获取活动建议失败: {}", e.getMessage(), e);
            return AgentResponse.builder()
                .answer("抱歉，无法获取活动建议，请稍后再试。")
                .agentName(AGENT_NAME)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .build();
        }
    }
    
    @Override
    public AgentResponse getWeatherAlert(String location) {
        long startTime = System.currentTimeMillis();
        
        try {
            WeatherResponse weather = weatherService.getCurrentWeather(location);
            
            List<VectorKnowledge> warningDocs = knowledgeBaseService.searchKnowledgeByCategory(
                "预警", 
                "weather_warning", 
                5
            );
            
            String knowledgeContext = buildKnowledgeContext(warningDocs);
            
            String prompt = String.format("""
                当前天气情况：
                - 地点：%s
                - 天气：%s
                - 温度：%s℃
                - 风速：%s m/s
                - 降水量：%smm
                
                相关预警知识：
                %s
                
                请分析当前天气是否存在潜在风险，并提供相应的预警建议。
                """,
                weather.getLocation(),
                weather.getWeatherCondition(),
                weather.getTemperature(),
                weather.getWindSpeed(),
                weather.getPrecipitation(),
                knowledgeContext
            );
            
            String response = qwenChatService.chat(PromptConstants.PROFESSIONAL_SYSTEM_PROMPT, prompt);
            
            if (response == null) {
                throw new RuntimeException("AI服务返回空响应");
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return AgentResponse.builder()
                .answer(response)
                .agentName(AGENT_NAME)
                .weatherContext(buildWeatherContextFromResponse(weather))
                .responseTimeMs(responseTime)
                .modelUsed(defaultModel)
                .sessionId(UUID.randomUUID().toString())
                .build();
            
        } catch (Exception e) {
            log.error("获取天气预警失败: {}", e.getMessage(), e);
            return AgentResponse.builder()
                .answer("抱歉，无法获取天气预警信息。")
                .agentName(AGENT_NAME)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .build();
        }
    }
    
    private String buildAgentSystemPrompt() {
        return """
            你是"天象智囊"，一个专业的气象智能助手。
            
            你的职责是：
            1. 帮助用户理解天气信息
            2. 提供专业的气象知识和生活建议
            3. 解答用户关于天气、气候、气象现象的问题
            4. 根据天气情况给出活动建议和决策支持
            
            回答要求：
            - 结合提供的知识库内容和天气数据给出准确、专业的回答
            - 语言简洁友好，避免过于专业的术语
            - 如果问题与天气无关，礼貌地引导用户回到天气相关话题
            - 如果知识库中有相关内容，要引用并说明来源
            - 给出的建议要具体、可操作
            
            你的回答应该体现专业性、友好性和实用性。
            """;
    }
    
    private String buildWeatherContext(String location) {
        if (location == null || location.isEmpty()) {
            return "";
        }
        
        try {
            WeatherResponse weather = weatherService.getCurrentWeather(location);
            return String.format("""
                当前天气数据：
                - 地点：%s
                - 天气：%s
                - 温度：%s℃（体感温度：%s℃）
                - 湿度：%s%%
                - 风速：%s m/s
                - 空气质量：%s（AQI: %d）
                - 紫外线指数：%d
                - 降水量：%smm
                """,
                weather.getLocation(),
                weather.getWeatherCondition(),
                weather.getTemperature(),
                weather.getFeelsLike(),
                weather.getHumidity(),
                weather.getWindSpeed(),
                weather.getAirQualityLevel(),
                weather.getAirQualityIndex(),
                weather.getUvIndex(),
                weather.getPrecipitation()
            );
        } catch (Exception e) {
            log.warn("获取天气上下文失败: {}", e.getMessage());
            return "";
        }
    }
    
    private String buildKnowledgeContext(List<VectorKnowledge> documents) {
        if (documents == null || documents.isEmpty()) {
            return "暂无相关知识库内容。";
        }
        
        StringBuilder context = new StringBuilder("相关知识库内容：\n");
        for (int i = 0; i < documents.size(); i++) {
            VectorKnowledge doc = documents.get(i);
            context.append(String.format("[%d] %s（来源：%s）\n%s\n\n",
                i + 1,
                doc.getTitle(),
                doc.getCategory(),
                doc.getContent()
            ));
        }
        return context.toString();
    }
    
    private String buildUserPrompt(String question, String weatherContext, String knowledgeContext, String userContext) {
        StringBuilder prompt = new StringBuilder();
        
        if (!weatherContext.isEmpty()) {
            prompt.append(weatherContext).append("\n");
        }
        
        prompt.append(knowledgeContext).append("\n");
        
        if (userContext != null && !userContext.isEmpty()) {
            prompt.append("用户背景信息：").append(userContext).append("\n\n");
        }
        
        prompt.append("用户问题：").append(question);
        
        return prompt.toString();
    }
    
    private String buildWeatherAnalysisPrompt(WeatherResponse weather) {
        return String.format("""
            请对以下天气情况进行专业分析：
            
            地点：%s
            天气：%s
            温度：%s℃（体感温度：%s℃）
            湿度：%s%%
            风速：%s m/s
            空气质量：%s（AQI: %d）
            紫外线指数：%d
            降水量：%smm
            
            请从以下几个方面进行分析：
            1. 天气特点解读
            2. 对日常生活的影响
            3. 出行建议
            4. 健康提醒
            """,
            weather.getLocation(),
            weather.getWeatherCondition(),
            weather.getTemperature(),
            weather.getFeelsLike(),
            weather.getHumidity(),
            weather.getWindSpeed(),
            weather.getAirQualityLevel(),
            weather.getAirQualityIndex(),
            weather.getUvIndex(),
            weather.getPrecipitation()
        );
    }
    
    private AgentResponse.WeatherContext buildWeatherContextResponse(String location) {
        if (location == null || location.isEmpty()) {
            return null;
        }
        
        try {
            WeatherResponse weather = weatherService.getCurrentWeather(location);
            return buildWeatherContextFromResponse(weather);
        } catch (Exception e) {
            return null;
        }
    }
    
    private AgentResponse.WeatherContext buildWeatherContextFromResponse(WeatherResponse weather) {
        return AgentResponse.WeatherContext.builder()
            .location(weather.getLocation())
            .weatherCondition(weather.getWeatherCondition())
            .temperature(weather.getTemperature())
            .feelsLike(weather.getFeelsLike())
            .humidity(weather.getHumidity())
            .windSpeed(weather.getWindSpeed())
            .airQualityLevel(weather.getAirQualityLevel())
            .airQualityIndex(weather.getAirQualityIndex())
            .build();
    }
    
    private Double calculateConfidence(List<VectorKnowledge> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0.3;
        }
        
        double avgSimilarity = documents.stream()
            .mapToDouble(doc -> extractSimilarity(doc.getMetadata()))
            .average()
            .orElse(0.5);
        
        double sizeBonus = Math.min(documents.size() * 0.05, 0.2);
        
        return Math.min(avgSimilarity + sizeBonus, 1.0);
    }
    
    private double extractSimilarity(String metadata) {
        if (metadata == null || !metadata.contains("similarity:")) {
            return 0.5;
        }
        try {
            String[] parts = metadata.split("\\|");
            for (String part : parts) {
                if (part.startsWith("similarity:")) {
                    return Double.parseDouble(part.substring("similarity:".length()));
                }
            }
        } catch (NumberFormatException e) {
            log.warn("解析相似度失败: {}", e.getMessage());
        }
        return 0.5;
    }
    
    private String truncateSnippet(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
