package com.skygazer.weather.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.constant.CityNameMapping;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * MetaWeather 免费天气 API 客户端（无需 API Key）。
 * <p>
 * 接口说明：
 * <ul>
 *   <li>城市检索：GET https://www.metaweather.com/api/location/search/?query={name}</li>
 *   <li>天气详情：GET https://www.metaweather.com/api/location/{woeid}/</li>
 * </ul>
 * 注意：MetaWeather 仅提供按天（未来约 6 天）预报，无小时级、无空气质量接口，
 * 且不支持中文城市名检索（需经 {@link CityNameMapping} 转换）。
 * </p>
 */
@Slf4j
@Component
public class MetaWeatherClient {

    private final WebClient metaWeatherWebClient;
    private final ObjectMapper objectMapper;

    @Value("${metaweather.api.base-url:https://www.metaweather.com}")
    private String baseUrl;

    public MetaWeatherClient(@Qualifier("metaWeatherWebClient") WebClient metaWeatherWebClient,
                             ObjectMapper objectMapper) {
        this.metaWeatherWebClient = metaWeatherWebClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取某城市的天气预报（含今天在内，通常 6 天）。
     * 调用失败、城市未找到或不支持中文名时返回 null，由上层回退到模拟数据。
     */
    public List<ConsolidatedWeather> fetchForecast(String location) {
        String english = CityNameMapping.toEnglish(location);
        if (english == null) {
            log.warn("MetaWeather 不支持中文城市名且未配置映射，回退模拟数据: {}", location);
            return null;
        }
        try {
            String searchJson = metaWeatherWebClient.get()
                    .uri("/api/location/search/?query={q}", english)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));
            if (searchJson == null || searchJson.isBlank()) {
                return null;
            }
            List<LocationSearchResult> results = objectMapper.readValue(searchJson,
                    new TypeReference<List<LocationSearchResult>>() {
                    });
            if (results == null || results.isEmpty()) {
                log.warn("MetaWeather 未找到城市: {}", english);
                return null;
            }
            Integer woeid = results.get(0).getWoeid();
            String detailJson = metaWeatherWebClient.get()
                    .uri("/api/location/{woeid}/", woeid)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));
            if (detailJson == null || detailJson.isBlank()) {
                return null;
            }
            LocationWeather weather = objectMapper.readValue(detailJson, LocationWeather.class);
            return weather.getConsolidatedWeather();
        } catch (Exception e) {
            log.error("调用 MetaWeather 失败，位置={}, 错误={}", location, e.getMessage());
            return null;
        }
    }

    // ===================== 字段映射工具 =====================

    private static final Map<String, String[]> STATE_MAP = Map.of(
            "sn", new String[]{"雪", "snowy"},
            "sl", new String[]{"雨夹雪", "sleet"},
            "h", new String[]{"冰雹", "hail"},
            "t", new String[]{"雷阵雨", "thunder"},
            "hr", new String[]{"大雨", "rainy"},
            "lr", new String[]{"小雨", "rainy"},
            "s", new String[]{"阵雨", "rainy"},
            "hc", new String[]{"阴", "cloudy"},
            "lc", new String[]{"多云", "cloudy"},
            "c", new String[]{"晴", "sunny"}
    );

    private static final String[] COMPASS = {"北风", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风"};

    public static String toCondition(ConsolidatedWeather c) {
        if (c == null || c.getWeatherStateAbbr() == null) {
            return "未知";
        }
        String[] mapped = STATE_MAP.get(c.getWeatherStateAbbr());
        return mapped != null ? mapped[0] : c.getWeatherStateName() != null ? c.getWeatherStateName() : "未知";
    }

    public static String toIcon(ConsolidatedWeather c) {
        if (c == null || c.getWeatherStateAbbr() == null) {
            return "unknown";
        }
        String[] mapped = STATE_MAP.get(c.getWeatherStateAbbr());
        return mapped != null ? mapped[1] : "unknown";
    }

    public static String toDescription(ConsolidatedWeather c) {
        return switch (toCondition(c)) {
            case "晴" -> "天气晴朗，阳光明媚，适合户外活动";
            case "多云" -> "云量较多，气温适宜，适合外出";
            case "阴" -> "阴天，气温较低，注意保暖";
            case "小雨" -> "小雨淅沥，出门请带伞";
            case "阵雨" -> "有阵雨，建议携带雨具";
            case "大雨" -> "大雨倾盆，建议减少外出";
            case "雷阵雨" -> "雷阵雨，请注意防雷避雨";
            case "雨夹雪" -> "雨夹雪天气，路面湿滑";
            case "雪" -> "下雪天气，注意保暖与出行安全";
            case "冰雹" -> "可能出现冰雹，请注意防护";
            default -> "天气状况：" + toCondition(c);
        };
    }

    /** MetaWeather 风速单位为 mph，转换为 km/h */
    public static double toKmh(Double mph) {
        return mph == null ? 0 : Math.round(mph * 1.60934 * 10) / 10.0;
    }

    /** MetaWeather 能见度单位为 mile，转换为 km */
    public static double toKm(Double miles) {
        return miles == null ? 0 : Math.round(miles * 1.60934 * 10) / 10.0;
    }

    public static String toCompass(Double deg) {
        if (deg == null) {
            return "未知";
        }
        int idx = (int) Math.round((deg % 360) / 45.0) % 8;
        return COMPASS[(idx + 8) % 8];
    }

    /** 根据风速(km/h)估算风力等级（近似蒲福风级） */
    public static String toWindScale(double kmh) {
        if (kmh < 1) return "1级";
        if (kmh < 6) return "2级";
        if (kmh < 12) return "3级";
        if (kmh < 20) return "4级";
        if (kmh < 29) return "5级";
        if (kmh < 39) return "6级";
        if (kmh < 50) return "7级";
        return "8级";
    }

    /** MetaWeather 无降水量数值，按天气状况估算一个参考值(mm) */
    public static BigDecimal toPrecipitation(ConsolidatedWeather c) {
        if (c == null || c.getWeatherStateAbbr() == null) {
            return BigDecimal.ZERO;
        }
        return switch (c.getWeatherStateAbbr()) {
            case "hr" -> BigDecimal.valueOf(25);
            case "lr" -> BigDecimal.valueOf(3);
            case "s" -> BigDecimal.valueOf(5);
            case "sl" -> BigDecimal.valueOf(10);
            case "sn" -> BigDecimal.valueOf(8);
            case "h" -> BigDecimal.valueOf(15);
            case "t" -> BigDecimal.valueOf(12);
            default -> BigDecimal.ZERO;
        };
    }

    // ===================== DTO =====================

    @Data
    public static class LocationSearchResult {
        private Integer woeid;
        private String title;
        private String locationType;
        @JsonProperty("latt_long")
        private String lattLong;
    }

    @Data
    public static class LocationWeather {
        @JsonProperty("consolidated_weather")
        private List<ConsolidatedWeather> consolidatedWeather;
    }

    @Data
    public static class ConsolidatedWeather {
        private Long id;
        @JsonProperty("weather_state_name")
        private String weatherStateName;
        @JsonProperty("weather_state_abbr")
        private String weatherStateAbbr;
        @JsonProperty("wind_speed")
        private Double windSpeed;
        @JsonProperty("wind_direction")
        private Double windDirection;
        @JsonProperty("wind_direction_compass")
        private String windDirectionCompass;
        @JsonProperty("min_temp")
        private Double minTemp;
        @JsonProperty("max_temp")
        private Double maxTemp;
        @JsonProperty("the_temp")
        private Double theTemp;
        @JsonProperty("air_pressure")
        private Double airPressure;
        private Integer humidity;
        private Double visibility;
        private Long predictability;
        @JsonProperty("applicable_date")
        private String applicableDate;
    }
}
