package com.skygazer.weather.dto.airquality;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirQualityIndex {
    
    private String code;
    private String name;
    private Integer aqi;
    
    @JsonProperty("aqiDisplay")
    private String aqiDisplay;
    
    private String level;
    private String category;
    private Color color;
    private PrimaryPollutant primaryPollutant;
    private Health health;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Color {
        private Integer red;
        private Integer green;
        private Integer blue;
        private Double alpha;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrimaryPollutant {
        private String code;
        private String name;
        
        @JsonProperty("fullName")
        private String fullName;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Health {
        private String effect;
        private Advice advice;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Advice {
            
            @JsonProperty("generalPopulation")
            private String generalPopulation;
            
            @JsonProperty("sensitivePopulation")
            private String sensitivePopulation;
        }
    }
}
