package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LifeIndexResponse {
    
    private String cityCode;
    private String cityName;
    private String updateTime;
    private List<LifeIndex> indices;
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LifeIndex {
        private String code;
        private String name;
        private String description;
        private int level;
        private String levelName;
        private String color;
        private double value;
        private String suggestion;
        private List<String> tips;
        private boolean isDefault;
    }
}
