package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyForecastResponse {
    
    private String location;
    private List<HourlyData> hourlyData;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HourlyData {
        private LocalDateTime time;
        private BigDecimal temperature;
        private String weatherCondition;
        private BigDecimal precipitation;
        private BigDecimal windSpeed;
        private String windDirection;
        private Integer humidity;
    }
}
