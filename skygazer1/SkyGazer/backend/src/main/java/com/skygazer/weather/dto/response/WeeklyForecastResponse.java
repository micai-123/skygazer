package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyForecastResponse {
    
    private String location;
    private List<DailyData> dailyData;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyData {
        private LocalDate date;
        private String weekday;
        private BigDecimal maxTemp;
        private BigDecimal minTemp;
        private String weatherCondition;
        private String weatherIcon;
        private BigDecimal precipitation;
        private BigDecimal windSpeed;
        private String windDirection;
        private Integer uvIndex;
        private Integer humidity;
    }
}
