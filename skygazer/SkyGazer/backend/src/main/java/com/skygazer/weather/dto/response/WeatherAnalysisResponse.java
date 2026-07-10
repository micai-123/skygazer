package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherAnalysisResponse {
    
    private String location;
    private String timeRange;
    private TemperatureAnalysis temperatureAnalysis;
    private PrecipitationAnalysis precipitationAnalysis;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemperatureAnalysis {
        private List<TrendPoint> trendData;
        private BigDecimal avgTemp;
        private BigDecimal maxTemp;
        private BigDecimal minTemp;
        private String trend;
        private String description;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrecipitationAnalysis {
        private List<TrendPoint> trendData;
        private Integer avgPrecipitationProb;
        private BigDecimal totalPrecipitation;
        private String precipitationPeriod;
        private String description;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendPoint {
        private String time;
        private BigDecimal value;
    }
}
