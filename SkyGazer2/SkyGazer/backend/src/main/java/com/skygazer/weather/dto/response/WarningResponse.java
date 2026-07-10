package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningResponse {
    
    private String id;
    private String cityCode;
    private String cityName;
    private String warningType;
    private String warningName;
    private int level;
    private String levelName;
    private String color;
    private String title;
    private String content;
    private String suggestion;
    private List<String> measures;
    private LocalDateTime issueTime;
    private LocalDateTime expireTime;
    private String source;
    private int priority;
    private boolean isActive;
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarningList {
        private String cityCode;
        private String cityName;
        private int totalCount;
        private int activeCount;
        private List<WarningResponse> warnings;
        private Map<String, Object> summary;
    }
}
