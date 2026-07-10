package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherMapResponse {
    
    private String layerType;
    private String updateTime;
    private List<CityWeatherPoint> cities;
    private MapMetadata metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityWeatherPoint {
        private String name;
        private String cityCode;
        private Double latitude;
        private Double longitude;
        private Double value;
        private String weather;
        private Integer humidity;
        private String wind;
        private String province;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapMetadata {
        private String layerName;
        private String layerDescription;
        private String colorScale;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private String unit;
    }
}
