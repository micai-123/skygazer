package com.skygazer.weather.constant;

import java.util.Arrays;
import java.util.List;

public class CacheConstants {
    
    private CacheConstants() {}
    
    public static final String WEATHER_CURRENT = "weather:current:";
    public static final String WEATHER_HOURLY = "weather:hourly:";
    public static final String WEATHER_WEEKLY = "weather:weekly:";
    public static final String LIFESTYLE = "weather:lifestyle:";
    public static final String AIR_QUALITY = "weather:air:";
    public static final String WEATHER_MAP = "weather:map:";
    public static final String WEATHER_ANALYSIS = "weather:analysis:";
    
    public static final String WEATHER_MAP_LAYER = "weather:map:layer:";
    public static final String WEATHER_MAP_TIMELINE = "weather:map:timeline:";
    public static final String WEATHER_MAP_HEATMAP = "weather:map:heatmap:";
    public static final String WEATHER_MAP_HOT_CITIES = "weather:map:hot:";
    
    public static final long WEATHER_CACHE_TTL = 1800;
    public static final long LIFESTYLE_CACHE_TTL = 3600;
    public static final long HOT_CITY_CACHE_TTL = 180;
    
    public static final String WEATHER_CACHE_PREFIX = "weather:city:";
    
    public static final long MAP_LAYER_CACHE_TTL = 300;
    public static final long MAP_HOT_CITY_CACHE_TTL = 180;
    public static final long MAP_TIMELINE_CACHE_TTL = 600;
    public static final long MAP_HEATMAP_CACHE_TTL = 300;
    
    public static final String DISTRICT_WEATHER_PREFIX = "weather:district:";
    public static final String DISTRICT_REFRESH_LIMIT = "weather:refresh:limit:";
    
    public static final long DISTRICT_WEATHER_CACHE_TTL = 3600;
    public static final long REFRESH_COOLDOWN_SECONDS = 180;
    
    public static final String GEOJSON_PREFIX = "geojson:";
    public static final long GEOJSON_CACHE_TTL = 86400;
    
    public static final List<String> HOT_CITIES = Arrays.asList(
        "北京", "上海", "广州", "深圳", "杭州"
    );
}
