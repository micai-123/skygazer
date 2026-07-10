package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifestyleResponse {
    
    private String location;
    private LifestyleIndex comfort;
    private LifestyleIndex dressing;
    private LifestyleIndex uv;
    private LifestyleIndex carWashing;
    private LifestyleIndex travel;
    private LifestyleIndex sport;
    private LifestyleIndex airQuality;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LifestyleIndex {
        private String name;
        private String level;
        private String description;
        private String icon;
        private String advice;
    }
}
