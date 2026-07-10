package com.skygazer.weather.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentResponse {
    
    private String answer;
    
    private String agentName;
    
    private List<String> references;
    
    private List<ReferenceDetail> referenceDetails;
    
    private Double confidence;
    
    private Long responseTimeMs;
    
    private String modelUsed;
    
    private String sessionId;
    
    private WeatherContext weatherContext;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReferenceDetail {
        private Long id;
        private String title;
        private String category;
        private String snippet;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeatherContext {
        private String location;
        private String weatherCondition;
        private BigDecimal temperature;
        private BigDecimal feelsLike;
        private BigDecimal humidity;
        private BigDecimal windSpeed;
        private String airQualityLevel;
        private Integer airQualityIndex;
    }
}
