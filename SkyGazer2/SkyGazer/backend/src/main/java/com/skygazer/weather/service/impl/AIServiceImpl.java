package com.skygazer.weather.service.impl;

import com.skygazer.weather.constant.PromptConstants;
import com.skygazer.weather.dto.request.ChatRequest;
import com.skygazer.weather.dto.request.ImageAnalysisRequest;
import com.skygazer.weather.dto.response.ChatResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.entity.InteractionLog;
import com.skygazer.weather.repository.InteractionLogRepository;
import com.skygazer.weather.service.AIService;
import com.skygazer.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    
    private final ChatClient.Builder chatClientBuilder;
    private final WeatherService weatherService;
    private final InteractionLogRepository interactionLogRepository;
    
    @Value("${spring.ai.openai.chat.options.model:qwen-plus}")
    private String defaultModel;
    
    @Value("${spring.ai.openai.image.options.model:qwen-vl-plus}")
    private String visionModel;
    
    @Override
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            String context = buildContext(request);
            String systemPrompt = buildSystemPrompt(request.getStyle());
            
            ChatClient chatClient = chatClientBuilder.build();
            
            String response = chatClient.prompt()
                .system(systemPrompt)
                .user(context + "\n\n用户问题：" + request.getMessage())
                .call()
                .content();
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            saveInteractionLog(request, response, responseTime, "chat");
            
            return ChatResponse.builder()
                .message(response)
                .style(request.getStyle())
                .responseTimeMs(responseTime)
                .modelUsed(defaultModel)
                .build();
        } catch (Exception e) {
            log.error("AI对话失败: {}", e.getMessage());
            return ChatResponse.builder()
                .message("抱歉，AI服务暂时不可用，请稍后再试。")
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .build();
        }
    }
    
    @Override
    public Flux<String> streamChat(ChatRequest request) {
        String context = buildContext(request);
        String systemPrompt = buildSystemPrompt(request.getStyle());
        
        ChatClient chatClient = chatClientBuilder.build();
        
        return chatClient.prompt()
            .system(systemPrompt)
            .user(context + "\n\n用户问题：" + request.getMessage())
            .stream()
            .content();
    }
    
    @Override
    public ChatResponse analyzeImage(ImageAnalysisRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            byte[] imageBytes = Base64.getDecoder().decode(request.getImageBase64());
            Media imageMedia = new Media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(imageBytes));
            
            UserMessage userMessage = new UserMessage(
                PromptConstants.SKY_ANALYSIS_PROMPT,
                List.of(imageMedia)
            );
            
            ChatClient chatClient = chatClientBuilder.build();
            
            String response = chatClient.prompt()
                .messages(userMessage)
                .call()
                .content();
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return ChatResponse.builder()
                .message(response)
                .responseTimeMs(responseTime)
                .modelUsed(visionModel)
                .build();
        } catch (Exception e) {
            log.error("图片分析失败: {}", e.getMessage());
            return ChatResponse.builder()
                .message("图片分析失败，请稍后再试。")
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .build();
        }
    }
    
    @Override
    public ChatResponse generateWeatherStory(String location, String style) {
        long startTime = System.currentTimeMillis();
        
        try {
            WeatherResponse weather = weatherService.getCurrentWeather(location);
            
            String prompt = buildWeatherStoryPrompt(weather, style);
            String systemPrompt = getStylePrompt(style);
            
            ChatClient chatClient = chatClientBuilder.build();
            
            String response = chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return ChatResponse.builder()
                .message(response)
                .style(style)
                .responseTimeMs(responseTime)
                .modelUsed(defaultModel)
                .build();
        } catch (Exception e) {
            log.error("生成天气故事失败: {}", e.getMessage());
            return ChatResponse.builder()
                .message("生成天气故事失败，请稍后再试。")
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .build();
        }
    }
    
    @Override
    public ChatResponse getDecisionAdvice(String location, String scenario) {
        long startTime = System.currentTimeMillis();
        
        try {
            WeatherResponse weather = weatherService.getCurrentWeather(location);
            
            String prompt = buildDecisionAdvicePrompt(weather, scenario);
            
            ChatClient chatClient = chatClientBuilder.build();
            
            String response = chatClient.prompt()
                .system(PromptConstants.DECISION_ADVISOR_SYSTEM)
                .user(prompt)
                .call()
                .content();
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return ChatResponse.builder()
                .message(response)
                .responseTimeMs(responseTime)
                .modelUsed(defaultModel)
                .build();
        } catch (Exception e) {
            log.error("获取决策建议失败: {}", e.getMessage());
            return ChatResponse.builder()
                .message("获取决策建议失败，请稍后再试。")
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .build();
        }
    }
    
    private String buildContext(ChatRequest request) {
        StringBuilder context = new StringBuilder();
        
        if (request.getLocation() != null && !request.getLocation().isEmpty()) {
            try {
                WeatherResponse weather = weatherService.getCurrentWeather(request.getLocation());
                context.append("当前位置：").append(request.getLocation()).append("\n");
                context.append("当前天气：").append(weather.getWeatherCondition())
                    .append("，温度：").append(weather.getTemperature()).append("℃\n");
                context.append("体感温度：").append(weather.getFeelsLike()).append("℃\n");
                context.append("湿度：").append(weather.getHumidity()).append("%\n");
                context.append("风速：").append(weather.getWindSpeed()).append("m/s\n");
                context.append("空气质量：").append(weather.getAirQualityLevel())
                    .append("（AQI: ").append(weather.getAirQualityIndex()).append("）\n");
            } catch (Exception e) {
                log.warn("获取天气上下文失败: {}", e.getMessage());
            }
        }
        
        if (request.getContext() != null) {
            context.append("\n用户背景信息：").append(request.getContext()).append("\n");
        }
        
        return context.toString();
    }
    
    private String buildSystemPrompt(String style) {
        if (style == null || style.isEmpty()) {
            return PromptConstants.DEFAULT_SYSTEM_PROMPT;
        }
        
        return switch (style) {
            case "poetic" -> PromptConstants.POETIC_SYSTEM_PROMPT;
            case "humorous" -> PromptConstants.HUMOROUS_SYSTEM_PROMPT;
            case "professional" -> PromptConstants.PROFESSIONAL_SYSTEM_PROMPT;
            default -> PromptConstants.DEFAULT_SYSTEM_PROMPT;
        };
    }
    
    private String getStylePrompt(String style) {
        return buildSystemPrompt(style);
    }
    
    private String buildWeatherStoryPrompt(WeatherResponse weather, String style) {
        return String.format("""
            请为以下天气情况生成一段生动的天气描述：
            
            地点：%s
            天气：%s
            温度：%s℃
            体感温度：%s℃
            湿度：%s%%
            风速：%s m/s
            空气质量：%s
            
            请用%s风格描述这个天气，让用户感受到天气的变化和特点。
            """,
            weather.getLocation(),
            weather.getWeatherCondition(),
            weather.getTemperature(),
            weather.getFeelsLike(),
            weather.getHumidity(),
            weather.getWindSpeed(),
            weather.getAirQualityLevel(),
            style != null ? style : "正常"
        );
    }
    
    private String buildDecisionAdvicePrompt(WeatherResponse weather, String scenario) {
        return String.format("""
            用户场景：%s
            
            当前天气情况：
            地点：%s
            天气：%s
            温度：%s℃
            体感温度：%s℃
            湿度：%s%%
            风速：%s m/s
            空气质量：%s（AQI: %d）
            紫外线指数：%d
            降水量：%smm
            
            请根据以上天气情况，为用户的场景提供专业的决策建议。
            """,
            scenario,
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
    
    private void saveInteractionLog(ChatRequest request, String response, long responseTime, String type) {
        try {
            InteractionLog log = InteractionLog.builder()
                .interactionType(type)
                .question(request.getMessage())
                .answer(response)
                .responseTimeMs(responseTime)
                .modelUsed(defaultModel)
                .build();
            interactionLogRepository.save(log);
        } catch (Exception e) {
            log.error("保存交互日志失败: {}", e.getMessage());
        }
    }
}
