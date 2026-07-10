package com.skygazer.weather.service.impl;

import com.skygazer.weather.constant.WarningType;
import com.skygazer.weather.dto.response.WarningResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.exception.BusinessException;
import com.skygazer.weather.service.WarningService;
import com.skygazer.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarningServiceImpl implements WarningService {
    
    private final WeatherService weatherService;
    
    private final Map<String, WarningResponse> warningStore = new ConcurrentHashMap<>();
    
    @Override
    public WarningResponse.WarningList getWarnings(String cityCode) {
        List<WarningResponse> warnings = warningStore.values().stream()
            .filter(w -> w.getCityCode().equals(cityCode))
            .sorted(Comparator.comparing(WarningResponse::getPriority).reversed()
                .thenComparing(WarningResponse::getIssueTime).reversed())
            .collect(Collectors.toList());
        
        int activeCount = (int) warnings.stream().filter(WarningResponse::isActive).count();
        
        Map<String, Object> summary = buildSummary(warnings);
        
        return WarningResponse.WarningList.builder()
            .cityCode(cityCode)
            .cityName(warnings.isEmpty() ? "" : warnings.get(0).getCityName())
            .totalCount(warnings.size())
            .activeCount(activeCount)
            .warnings(warnings)
            .summary(summary)
            .build();
    }
    
    @Override
    public WarningResponse.WarningList getActiveWarnings(String cityCode) {
        List<WarningResponse> warnings = warningStore.values().stream()
            .filter(w -> w.getCityCode().equals(cityCode) && w.isActive())
            .filter(w -> w.getExpireTime() == null || w.getExpireTime().isAfter(LocalDateTime.now()))
            .sorted(Comparator.comparing(WarningResponse::getPriority).reversed()
                .thenComparing(WarningResponse::getIssueTime).reversed())
            .collect(Collectors.toList());
        
        Map<String, Object> summary = buildSummary(warnings);
        
        return WarningResponse.WarningList.builder()
            .cityCode(cityCode)
            .cityName(warnings.isEmpty() ? "" : warnings.get(0).getCityName())
            .totalCount(warnings.size())
            .activeCount(warnings.size())
            .warnings(warnings)
            .summary(summary)
            .build();
    }
    
    @Override
    public WarningResponse getWarningDetail(String warningId) {
        WarningResponse warning = warningStore.get(warningId);
        if (warning == null) {
            throw BusinessException.notFound("预警信息", warningId);
        }
        return warning;
    }
    
    @Override
    public List<WarningResponse> analyzeWarnings(String cityCode, WeatherResponse weather) {
        log.info("分析预警: cityCode={}", cityCode);
        
        List<WarningResponse> warnings = new ArrayList<>();
        
        analyzeHighTempWarning(cityCode, weather, warnings);
        analyzeColdWaveWarning(cityCode, weather, warnings);
        analyzeRainstormWarning(cityCode, weather, warnings);
        analyzeStrongWindWarning(cityCode, weather, warnings);
        analyzeFogWarning(cityCode, weather, warnings);
        analyzeAirQualityWarning(cityCode, weather, warnings);
        analyzeUVWarning(cityCode, weather, warnings);
        analyzeThunderWarning(cityCode, weather, warnings);
        
        return warnings;
    }
    
    @Override
    public WarningResponse createWarning(String cityCode, String warningType, int level, String content) {
        WarningType type = WarningType.fromCode(warningType);
        if (type == null) {
            throw BusinessException.invalidParameter("warningType", warningType);
        }
        
        if (level < 0 || level >= type.getLevels().size()) {
            throw BusinessException.invalidParameter("level", String.valueOf(level));
        }
        
        String warningId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        WarningResponse warning = WarningResponse.builder()
            .id(warningId)
            .cityCode(cityCode)
            .cityName("城市")
            .warningType(type.getCode())
            .warningName(type.getName())
            .level(level + 1)
            .levelName(type.getLevel(level))
            .color(type.getColor(level))
            .title(type.getName() + type.getLevel(level) + "预警")
            .content(content)
            .suggestion(getWarningSuggestion(type, level))
            .measures(getWarningMeasures(type, level))
            .issueTime(now)
            .expireTime(now.plusHours(24))
            .source("智能分析系统")
            .priority(type.getPriority())
            .isActive(true)
            .metadata(type.toMetadataMap())
            .build();
        
        warningStore.put(warningId, warning);
        log.info("创建预警: id={}, type={}, level={}", warningId, warningType, level);
        
        return warning;
    }
    
    @Override
    public void dismissWarning(String warningId) {
        WarningResponse warning = warningStore.get(warningId);
        if (warning != null) {
            warning.setActive(false);
            log.info("解除预警: id={}", warningId);
        }
    }
    
    @Override
    public List<WarningResponse> getWarningsByType(String cityCode, String warningType) {
        return warningStore.values().stream()
            .filter(w -> w.getCityCode().equals(cityCode) && w.getWarningType().equals(warningType))
            .sorted(Comparator.comparing(WarningResponse::getIssueTime).reversed())
            .collect(Collectors.toList());
    }
    
    private void analyzeHighTempWarning(String cityCode, WeatherResponse weather, List<WarningResponse> warnings) {
        double temp = parseDouble(weather.getTemperature(), 0);
        
        if (temp >= 40) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.HIGH_TEMP, 2, 
                "预计最高气温将达到" + temp + "°C以上，请做好防暑降温工作"));
        } else if (temp >= 37) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.HIGH_TEMP, 1,
                "预计最高气温将达到" + temp + "°C，请注意防暑"));
        } else if (temp >= 35) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.HIGH_TEMP, 0,
                "预计最高气温将达到" + temp + "°C，请注意防暑降温"));
        }
    }
    
    private void analyzeColdWaveWarning(String cityCode, WeatherResponse weather, List<WarningResponse> warnings) {
        double temp = parseDouble(weather.getTemperature(), 20);
        String condition = weather.getWeatherCondition();
        
        if (temp <= -10) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.COLD_WAVE, 3,
                "预计最低气温将降至" + temp + "°C以下，请做好防寒保暖"));
        } else if (temp <= -5) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.COLD_WAVE, 2,
                "预计最低气温将降至" + temp + "°C，请注意保暖"));
        } else if (temp <= 0) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.COLD_WAVE, 1,
                "预计气温较低，请注意防寒"));
        } else if (temp <= 4 && condition != null && condition.contains("雨")) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.COLD_WAVE, 0,
                "低温雨雪天气，请注意防寒保暖"));
        }
    }
    
    private void analyzeRainstormWarning(String cityCode, WeatherResponse weather, List<WarningResponse> warnings) {
        double precipitation = parseDouble(weather.getPrecipitation(), 0);
        String condition = weather.getWeatherCondition();
        
        if (condition != null && condition.contains("暴雨")) {
            if (condition.contains("特大暴雨")) {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.RAINSTORM, 3,
                    "预计将出现特大暴雨，请做好防汛准备"));
            } else if (condition.contains("大暴雨")) {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.RAINSTORM, 2,
                    "预计将出现大暴雨，请注意防范"));
            } else {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.RAINSTORM, 1,
                    "预计将出现暴雨，请注意防范"));
            }
        } else if (precipitation > 50) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.RAINSTORM, 1,
                "预计降水量将达到" + precipitation + "mm以上，请注意防范"));
        } else if (precipitation > 25) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.RAINSTORM, 0,
                "预计有大雨，请注意防范"));
        }
    }
    
    private void analyzeStrongWindWarning(String cityCode, WeatherResponse weather, List<WarningResponse> warnings) {
        double windSpeed = parseDouble(weather.getWindSpeed(), 0);
        
        if (windSpeed >= 100) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.STRONG_WIND, 3,
                "预计风力将达到" + windSpeed + "km/h以上，请做好防范"));
        } else if (windSpeed >= 75) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.STRONG_WIND, 2,
                "预计风力将达到" + windSpeed + "km/h，请注意防范"));
        } else if (windSpeed >= 50) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.STRONG_WIND, 1,
                "预计风力较大，请注意防范"));
        } else if (windSpeed >= 30) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.STRONG_WIND, 0,
                "预计有" + weather.getWindDirection() + "风" + windSpeed + "km/h，请注意"));
        }
    }
    
    private void analyzeFogWarning(String cityCode, WeatherResponse weather, List<WarningResponse> warnings) {
        String condition = weather.getWeatherCondition();
        double visibility = parseDouble(weather.getVisibility(), 10);
        
        if (condition != null && condition.contains("雾")) {
            if (visibility < 50) {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.FOG, 2,
                    "能见度极低，不足50米，请谨慎出行"));
            } else if (visibility < 200) {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.FOG, 1,
                    "能见度较低，约" + visibility + "米，请注意行车安全"));
            } else if (visibility < 500) {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.FOG, 0,
                    "有雾，能见度约" + visibility + "米，请注意安全"));
            }
        }
    }
    
    private void analyzeAirQualityWarning(String cityCode, WeatherResponse weather, List<WarningResponse> warnings) {
        Integer aqi = weather.getAirQualityIndex();
        if (aqi == null) return;
        
        if (aqi > 300) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.AIR_QUALITY, 3,
                "空气质量严重污染，AQI指数" + aqi + "，请避免户外活动"));
        } else if (aqi > 200) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.AIR_QUALITY, 2,
                "空气质量重度污染，AQI指数" + aqi + "，请减少户外活动"));
        } else if (aqi > 150) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.AIR_QUALITY, 1,
                "空气中度污染，AQI指数" + aqi + "，敏感人群请注意"));
        } else if (aqi > 100) {
            warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.AIR_QUALITY, 0,
                "空气轻度污染，AQI指数" + aqi + "，请注意防护"));
        }
    }
    
    private void analyzeUVWarning(String cityCode, WeatherResponse weather, List<WarningResponse> warnings) {
        String condition = weather.getWeatherCondition();
        double temp = parseDouble(weather.getTemperature(), 20);
        
        if (condition != null && condition.contains("晴") && temp > 28) {
            if (temp > 35) {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.UV, 3,
                    "紫外线极强，请避免长时间户外活动"));
            } else if (temp > 32) {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.UV, 2,
                    "紫外线很强，外出请做好防护"));
            } else {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.UV, 1,
                    "紫外线较强，外出建议涂抹防晒霜"));
            }
        }
    }
    
    private void analyzeThunderWarning(String cityCode, WeatherResponse weather, List<WarningResponse> warnings) {
        String condition = weather.getWeatherCondition();
        
        if (condition != null && condition.contains("雷")) {
            if (condition.contains("强雷") || condition.contains("雷暴")) {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.THUNDER, 2,
                    "预计有强雷电天气，请注意防范"));
            } else if (condition.contains("雷阵雨")) {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.THUNDER, 1,
                    "预计有雷阵雨，请注意防雷"));
            } else {
                warnings.add(createWarningFromAnalysis(cityCode, weather, WarningType.THUNDER, 0,
                    "可能有雷电活动，请注意安全"));
            }
        }
    }
    
    private WarningResponse createWarningFromAnalysis(String cityCode, WeatherResponse weather, 
                                                       WarningType type, int level, String content) {
        String warningId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        return WarningResponse.builder()
            .id(warningId)
            .cityCode(cityCode)
            .cityName(weather.getCityName())
            .warningType(type.getCode())
            .warningName(type.getName())
            .level(level + 1)
            .levelName(type.getLevel(level))
            .color(type.getColor(level))
            .title(type.getName() + type.getLevel(level) + "预警")
            .content(content)
            .suggestion(getWarningSuggestion(type, level))
            .measures(getWarningMeasures(type, level))
            .issueTime(now)
            .expireTime(now.plusHours(24))
            .source("智能分析系统")
            .priority(type.getPriority())
            .isActive(true)
            .metadata(type.toMetadataMap())
            .build();
    }
    
    private String getWarningSuggestion(WarningType type, int level) {
        switch (type) {
            case HIGH_TEMP:
                if (level >= 2) return "尽量避免户外活动，做好防暑降温工作";
                return "注意防暑降温，避免长时间户外活动";
            case COLD_WAVE:
                if (level >= 2) return "注意保暖，减少外出";
                return "适当增添衣物，注意防寒";
            case RAINSTORM:
                if (level >= 2) return "做好防汛准备，避免外出";
                return "外出请携带雨具，注意安全";
            case STRONG_WIND:
                if (level >= 2) return "避免户外活动，加固临时建筑";
                return "外出注意安全，远离广告牌等";
            case FOG:
                return "能见度低，驾车请减速慢行";
            case AIR_QUALITY:
                if (level >= 2) return "减少户外活动，外出佩戴口罩";
                return "敏感人群减少户外活动";
            case UV:
                return "外出请涂抹防晒霜，戴遮阳帽";
            case THUNDER:
                return "避免户外活动，远离金属物体";
            default:
                return "请注意防范";
        }
    }
    
    private List<String> getWarningMeasures(WarningType type, int level) {
        switch (type) {
            case HIGH_TEMP:
                return Arrays.asList("减少户外活动", "及时补充水分", "做好防暑降温");
            case COLD_WAVE:
                return Arrays.asList("注意保暖", "减少外出", "关注老人和儿童");
            case RAINSTORM:
                return Arrays.asList("携带雨具", "注意交通安全", "远离低洼地带");
            case STRONG_WIND:
                return Arrays.asList("避免户外活动", "远离广告牌", "加固临时建筑");
            case FOG:
                return Arrays.asList("减速慢行", "开启雾灯", "保持安全距离");
            case AIR_QUALITY:
                return Arrays.asList("减少户外活动", "佩戴口罩", "关闭门窗");
            case UV:
                return Arrays.asList("涂抹防晒霜", "戴遮阳帽", "避免正午外出");
            case THUNDER:
                return Arrays.asList("避免户外活动", "远离金属物体", "不要在大树下避雨");
            default:
                return Arrays.asList("注意安全", "关注天气变化");
        }
    }
    
    private Map<String, Object> buildSummary(List<WarningResponse> warnings) {
        Map<String, Object> summary = new HashMap<>();
        
        Map<String, Long> typeCount = warnings.stream()
            .collect(Collectors.groupingBy(WarningResponse::getWarningName, Collectors.counting()));
        
        Map<String, Long> levelCount = warnings.stream()
            .collect(Collectors.groupingBy(WarningResponse::getLevelName, Collectors.counting()));
        
        int highestPriority = warnings.stream()
            .mapToInt(WarningResponse::getPriority)
            .max()
            .orElse(0);
        
        summary.put("byType", typeCount);
        summary.put("byLevel", levelCount);
        summary.put("highestPriority", highestPriority);
        summary.put("lastUpdate", LocalDateTime.now().toString());
        
        return summary;
    }
    
    private double parseDouble(String value, double defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.-]", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private double parseDouble(BigDecimal value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.doubleValue();
    }
    
    private double parseDouble(Integer value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.doubleValue();
    }
}
