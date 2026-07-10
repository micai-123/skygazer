package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponse {
    
    private String cityCode;
    private String cityName;
    private String location;
    private BigDecimal temperature;
    private BigDecimal feelsLike;
    private BigDecimal humidity;
    private BigDecimal windSpeed;
    private String windDirection;
    private String windScale;
    private String weatherCondition;
    private String weatherDescription;
    private Integer airQualityIndex;
    private String airQualityLevel;
    private Integer pm25;
    private Integer pm10;
    private Integer uvIndex;
    private BigDecimal visibility;
    private Integer pressure;
    private BigDecimal precipitation;
    private LocalDateTime recordTime;
    private String aiDescription;
}
