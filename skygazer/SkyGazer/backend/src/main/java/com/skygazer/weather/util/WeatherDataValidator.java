package com.skygazer.weather.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.skygazer.weather.constant.LayerType;
import com.skygazer.weather.dto.response.WeatherMapResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class WeatherDataValidator {
    
    public boolean isValidWeatherResponse(JsonNode response) {
        if (response == null) {
            log.warn("天气响应为空");
            return false;
        }
        
        if (!response.has("code")) {
            log.warn("天气响应缺少 code 字段");
            return false;
        }
        
        String code = response.get("code").asText();
        if (!"200".equals(code)) {
            log.warn("天气响应返回错误码: {}", code);
            return false;
        }
        
        return true;
    }
    
    public boolean isValidNowWeather(JsonNode nowNode) {
        if (nowNode == null || nowNode.isNull()) {
            log.warn("实时天气数据为空");
            return false;
        }
        
        boolean hasValidData = false;
        
        if (nowNode.has("temp") && !nowNode.get("temp").isNull()) {
            hasValidData = true;
        }
        
        if (nowNode.has("text") && !nowNode.get("text").isNull()) {
            hasValidData = true;
        }
        
        if (!hasValidData) {
            log.warn("实时天气数据缺少有效字段");
        }
        
        return hasValidData;
    }
    
    public boolean isValidAirQuality(JsonNode airNode) {
        if (airNode == null || airNode.isNull()) {
            log.warn("空气质量数据为空");
            return false;
        }
        
        if (!airNode.has("aqi") || airNode.get("aqi").isNull()) {
            log.warn("空气质量数据缺少 aqi 字段");
            return false;
        }
        
        return true;
    }
    
    public boolean isValidGeoJson(JsonNode geoJson) {
        if (geoJson == null) {
            log.warn("GeoJSON 数据为空");
            return false;
        }
        
        if (!geoJson.has("features") || !geoJson.get("features").isArray()) {
            log.warn("GeoJSON 数据缺少 features 数组");
            return false;
        }
        
        JsonNode features = geoJson.get("features");
        if (features.size() == 0) {
            log.warn("GeoJSON features 数组为空");
            return false;
        }
        
        return true;
    }
    
    public boolean isValidCityWeatherPoint(WeatherMapResponse.CityWeatherPoint point, LayerType layerType) {
        if (point == null) {
            log.warn("城市天气数据点为空");
            return false;
        }
        
        if (point.getName() == null || point.getName().trim().isEmpty()) {
            log.warn("城市天气数据点缺少城市名称");
            return false;
        }
        
        if (point.getValue() == null) {
            log.warn("城市 {} 的气象数据值为空", point.getName());
            return false;
        }
        
        Double value = point.getValue();
        if (value < layerType.getMinValue() || value > layerType.getMaxValue()) {
            log.warn("城市 {} 的气象数据值 {} 超出合理范围 [{}, {}]", 
                point.getName(), value, layerType.getMinValue(), layerType.getMaxValue());
            return false;
        }
        
        return true;
    }
    
    public boolean validateWeatherMapResponse(WeatherMapResponse response, LayerType layerType) {
        if (response == null) {
            log.error("气象地图响应为空");
            return false;
        }
        
        if (response.getCities() == null || response.getCities().isEmpty()) {
            log.error("气象地图响应缺少城市数据");
            return false;
        }
        
        List<WeatherMapResponse.CityWeatherPoint> cities = response.getCities();
        int validCount = 0;
        int invalidCount = 0;
        
        for (WeatherMapResponse.CityWeatherPoint point : cities) {
            if (isValidCityWeatherPoint(point, layerType)) {
                validCount++;
            } else {
                invalidCount++;
            }
        }
        
        double validRate = (double) validCount / cities.size();
        log.info("气象数据完整性校验: 总数={}, 有效={}, 无效={}, 有效率={}%", 
            cities.size(), validCount, invalidCount, String.format("%.2f", validRate * 100));
        
        if (validRate < 0.8) {
            log.warn("气象数据有效率过低: {}%", String.format("%.2f", validRate * 100));
            return false;
        }
        
        return true;
    }
    
    public WeatherMapResponse.CityWeatherPoint fillMissingData(
            WeatherMapResponse.CityWeatherPoint point, 
            LayerType layerType,
            String cityName) {
        
        if (point == null) {
            log.warn("创建默认城市天气数据: {}", cityName);
            return WeatherMapResponse.CityWeatherPoint.builder()
                .name(cityName)
                .value(generateDefaultValue(cityName, layerType))
                .weather("未知")
                .humidity(50)
                .wind("未知")
                .build();
        }
        
        Double value = point.getValue();
        if (value == null || value < layerType.getMinValue() || value > layerType.getMaxValue()) {
            point.setValue(generateDefaultValue(cityName, layerType));
            log.warn("填充城市 {} 的缺失气象数据: {}", cityName, point.getValue());
        }
        
        if (point.getWeather() == null || point.getWeather().trim().isEmpty()) {
            point.setWeather("未知");
        }
        
        if (point.getHumidity() == null) {
            point.setHumidity(50);
        }
        
        if (point.getWind() == null || point.getWind().trim().isEmpty()) {
            point.setWind("未知");
        }
        
        return point;
    }
    
    private Double generateDefaultValue(String cityName, LayerType layerType) {
        double min = layerType.getMinValue();
        double max = layerType.getMaxValue();
        
        double mid = (min + max) / 2;
        double range = (max - min) / 4;
        
        double hash = Math.abs(Objects.hash(cityName, layerType.getCode()));
        double offset = (hash % 100) / 100.0 * range - range / 2;
        
        return BigDecimal.valueOf(mid + offset)
            .setScale(1, BigDecimal.ROUND_HALF_UP)
            .doubleValue();
    }
}
