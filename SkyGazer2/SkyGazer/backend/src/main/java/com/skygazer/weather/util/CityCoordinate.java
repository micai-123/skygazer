package com.skygazer.weather.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityCoordinate {
    
    private String name;
    private Double latitude;
    private Double longitude;
    private String locationId;
    
    private static final Map<String, CityCoordinate> CITY_COORDINATES = new HashMap<>();
    
    static {
        CITY_COORDINATES.put("北京", new CityCoordinate("北京", 39.90, 116.40, "101010100"));
        CITY_COORDINATES.put("上海", new CityCoordinate("上海", 31.23, 121.47, "101020100"));
        CITY_COORDINATES.put("广州", new CityCoordinate("广州", 23.13, 113.26, "101280101"));
        CITY_COORDINATES.put("深圳", new CityCoordinate("深圳", 22.54, 114.06, "101280601"));
        CITY_COORDINATES.put("杭州", new CityCoordinate("杭州", 30.25, 120.17, "101210101"));
        CITY_COORDINATES.put("成都", new CityCoordinate("成都", 30.57, 104.07, "101270101"));
        CITY_COORDINATES.put("武汉", new CityCoordinate("武汉", 30.58, 114.30, "101200101"));
        CITY_COORDINATES.put("西安", new CityCoordinate("西安", 34.26, 108.95, "101110101"));
        CITY_COORDINATES.put("南京", new CityCoordinate("南京", 32.06, 118.80, "101190101"));
        CITY_COORDINATES.put("重庆", new CityCoordinate("重庆", 29.56, 106.55, "101040100"));
    }
    
    public static CityCoordinate getCoordinate(String cityName) {
        return CITY_COORDINATES.get(cityName);
    }
    
    public static boolean containsCity(String cityName) {
        return CITY_COORDINATES.containsKey(cityName);
    }
}
