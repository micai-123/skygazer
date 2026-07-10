package com.skygazer.weather.service.impl;

import com.skygazer.weather.constant.LifeIndexType;
import com.skygazer.weather.dto.response.LifeIndexResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.exception.BusinessException;
import com.skygazer.weather.service.LifeIndexService;
import com.skygazer.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LifeIndexServiceImpl implements LifeIndexService {
    
    private final WeatherService weatherService;
    
    @Override
    public LifeIndexResponse calculateLifeIndices(String cityCode) {
        return calculateLifeIndices(cityCode, LifeIndexType.getDefaultIndices().stream()
            .map(LifeIndexType::getCode)
            .collect(Collectors.toList()));
    }
    
    @Override
    public LifeIndexResponse calculateLifeIndices(String cityCode, List<String> indexTypes) {
        log.info("计算生活指数: cityCode={}, indices={}", cityCode, indexTypes);
        
        WeatherResponse weather = weatherService.getCurrentWeather(cityCode);
        if (weather == null) {
            throw BusinessException.weatherDataNotFound(cityCode);
        }
        
        List<LifeIndexResponse.LifeIndex> indices = new ArrayList<>();
        
        for (String indexType : indexTypes) {
            LifeIndexType type = LifeIndexType.fromCode(indexType);
            if (type != null) {
                indices.add(calculateSingleIndex(indexType, weather));
            }
        }
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cityCode", cityCode);
        metadata.put("indexCount", indices.size());
        metadata.put("calculationTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return LifeIndexResponse.builder()
            .cityCode(cityCode)
            .cityName(weather.getCityName())
            .updateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            .indices(indices)
            .metadata(metadata)
            .build();
    }
    
    @Override
    public LifeIndexResponse.LifeIndex calculateSingleIndex(String indexType, WeatherResponse weather) {
        LifeIndexType type = LifeIndexType.fromCode(indexType);
        if (type == null) {
            throw BusinessException.invalidParameter("indexType", indexType);
        }
        
        double normalizedValue = 0;
        String suggestion = "";
        List<String> tips = new ArrayList<>();
        
        switch (type) {
            case DRESSING:
                normalizedValue = calculateDressingIndex(weather);
                suggestion = getDressingSuggestion(normalizedValue, weather);
                tips = getDressingTips(normalizedValue, weather);
                break;
            case SPORT:
                normalizedValue = calculateSportIndex(weather);
                suggestion = getSportSuggestion(normalizedValue, weather);
                tips = getSportTips(normalizedValue, weather);
                break;
            case UV:
                normalizedValue = calculateUVIndex(weather);
                suggestion = getUVSuggestion(normalizedValue);
                tips = getUVTips(normalizedValue);
                break;
            case CAR_WASH:
                normalizedValue = calculateCarWashIndex(weather);
                suggestion = getCarWashSuggestion(normalizedValue);
                tips = getCarWashTips(normalizedValue);
                break;
            case TRAVEL:
                normalizedValue = calculateTravelIndex(weather);
                suggestion = getTravelSuggestion(normalizedValue);
                tips = getTravelTips(normalizedValue);
                break;
            case ALLERGY:
                normalizedValue = calculateAllergyIndex(weather);
                suggestion = getAllergySuggestion(normalizedValue);
                tips = getAllergyTips(normalizedValue);
                break;
            case AIR_QUALITY:
                normalizedValue = calculateAirQualityIndex(weather);
                suggestion = getAirQualitySuggestion(normalizedValue);
                tips = getAirQualityTips(normalizedValue);
                break;
            case COMFORT:
                normalizedValue = calculateComfortIndex(weather);
                suggestion = getComfortSuggestion(normalizedValue);
                tips = getComfortTips(normalizedValue);
                break;
            case FISHING:
                normalizedValue = calculateFishingIndex(weather);
                suggestion = getFishingSuggestion(normalizedValue);
                tips = getFishingTips(normalizedValue);
                break;
            case SUNGLASSES:
                normalizedValue = calculateSunglassesIndex(weather);
                suggestion = getSunglassesSuggestion(normalizedValue);
                tips = getSunglassesTips(normalizedValue);
                break;
            case UMBRELLA:
                normalizedValue = calculateUmbrellaIndex(weather);
                suggestion = getUmbrellaSuggestion(normalizedValue);
                tips = getUmbrellaTips(normalizedValue);
                break;
            case DRYING:
                normalizedValue = calculateDryingIndex(weather);
                suggestion = getDryingSuggestion(normalizedValue);
                tips = getDryingTips(normalizedValue);
                break;
            default:
                normalizedValue = 0.5;
                suggestion = "暂无建议";
                tips = Arrays.asList("暂无提示");
        }
        
        int levelIndex = type.calculateLevelIndex(normalizedValue);
        
        return LifeIndexResponse.LifeIndex.builder()
            .code(type.getCode())
            .name(type.getName())
            .description(type.getDescription())
            .level(levelIndex + 1)
            .levelName(type.getLevel(levelIndex))
            .color(type.getColor(levelIndex))
            .value(normalizedValue * 100)
            .suggestion(suggestion)
            .tips(tips)
            .isDefault(type.isDefault())
            .build();
    }
    
    @Override
    public List<LifeIndexResponse.LifeIndex> calculateAllIndices(WeatherResponse weather) {
        return Arrays.stream(LifeIndexType.values())
            .map(type -> calculateSingleIndex(type.getCode(), weather))
            .collect(Collectors.toList());
    }
    
    private double calculateDressingIndex(WeatherResponse weather) {
        double temp = parseDouble(weather.getTemperature(), 20);
        double humidity = parseDouble(weather.getHumidity(), 50);
        double windSpeed = parseDouble(weather.getWindSpeed(), 0);
        
        double windChill = temp - windSpeed * 0.3;
        double humidityEffect = humidity > 70 ? -2 : (humidity < 30 ? 1 : 0);
        double effectiveTemp = windChill + humidityEffect;
        
        if (effectiveTemp < -10) return 0;
        if (effectiveTemp < 0) return 0.15;
        if (effectiveTemp < 10) return 0.3;
        if (effectiveTemp < 18) return 0.45;
        if (effectiveTemp < 25) return 0.6;
        if (effectiveTemp < 32) return 0.75;
        return 0.9;
    }
    
    private String getDressingSuggestion(double index, WeatherResponse weather) {
        double temp = parseDouble(weather.getTemperature(), 20);
        
        if (temp < 0) return "天气寒冷，建议穿棉衣、羽绒服等厚重保暖衣物";
        if (temp < 10) return "天气较冷，建议穿毛衣、夹克等保暖衣物";
        if (temp < 18) return "天气适中，建议穿长袖衬衫、薄外套等";
        if (temp < 25) return "天气舒适，适合穿T恤、薄衬衫等轻薄衣物";
        if (temp < 32) return "天气炎热，建议穿短袖、短裤等清凉透气衣物";
        return "天气酷热，建议穿轻薄透气衣物，注意防暑";
    }
    
    private List<String> getDressingTips(double index, WeatherResponse weather) {
        List<String> tips = new ArrayList<>();
        double temp = parseDouble(weather.getTemperature(), 20);
        double humidity = parseDouble(weather.getHumidity(), 50);
        
        if (temp < 10) {
            tips.add("建议佩戴围巾、手套等保暖配件");
            tips.add("注意头部和脚部保暖");
        }
        if (temp > 28) {
            tips.add("选择浅色、宽松的衣物");
            tips.add("外出时戴遮阳帽");
        }
        if (humidity > 70) {
            tips.add("选择透气性好的面料");
        }
        if (parseDouble(weather.getWindSpeed(), 0) > 20) {
            tips.add("大风天气注意防风保暖");
        }
        
        return tips.isEmpty() ? Arrays.asList("穿着舒适即可") : tips;
    }
    
    private double calculateSportIndex(WeatherResponse weather) {
        double temp = parseDouble(weather.getTemperature(), 20);
        double humidity = parseDouble(weather.getHumidity(), 50);
        double windSpeed = parseDouble(weather.getWindSpeed(), 0);
        double precipitation = parseDouble(weather.getPrecipitation(), 0);
        
        double score = 100;
        
        if (temp < 5 || temp > 35) score -= 30;
        else if (temp < 10 || temp > 30) score -= 15;
        
        if (humidity > 80) score -= 20;
        else if (humidity > 60) score -= 10;
        
        if (windSpeed > 30) score -= 25;
        else if (windSpeed > 15) score -= 10;
        
        if (precipitation > 10) score -= 40;
        else if (precipitation > 0) score -= 20;
        
        String condition = weather.getWeatherCondition();
        if (condition != null && (condition.contains("雨") || condition.contains("雪"))) {
            score -= 30;
        }
        
        return Math.max(0, Math.min(100, score)) / 100.0;
    }
    
    private String getSportSuggestion(double index, WeatherResponse weather) {
        if (index < 0.2) return "天气条件不适合户外运动，建议室内运动";
        if (index < 0.4) return "天气条件较差，不太适合户外运动";
        if (index < 0.6) return "天气条件一般，可以进行轻度户外运动";
        if (index < 0.8) return "天气条件较好，适合户外运动";
        return "天气条件极佳，非常适合户外运动";
    }
    
    private List<String> getSportTips(double index, WeatherResponse weather) {
        List<String> tips = new ArrayList<>();
        double temp = parseDouble(weather.getTemperature(), 20);
        
        if (temp > 28) {
            tips.add("避开中午高温时段");
            tips.add("注意及时补充水分");
        }
        if (temp < 10) {
            tips.add("做好充分热身运动");
            tips.add("运动后及时保暖");
        }
        if (index < 0.4) {
            tips.add("建议选择室内运动场所");
        }
        if (parseDouble(weather.getWindSpeed(), 0) > 15) {
            tips.add("大风天气注意安全");
        }
        
        return tips.isEmpty() ? Arrays.asList("享受运动时光") : tips;
    }
    
    private double calculateUVIndex(WeatherResponse weather) {
        String condition = weather.getWeatherCondition();
        double temp = parseDouble(weather.getTemperature(), 20);
        
        double uvBase = 0.3;
        
        if (condition != null) {
            if (condition.contains("晴")) uvBase = 0.8;
            else if (condition.contains("多云")) uvBase = 0.5;
            else if (condition.contains("阴")) uvBase = 0.3;
            else if (condition.contains("雨")) uvBase = 0.2;
        }
        
        if (temp > 30) uvBase += 0.1;
        else if (temp > 25) uvBase += 0.05;
        
        return Math.min(1, uvBase);
    }
    
    private String getUVSuggestion(double index) {
        if (index < 0.2) return "紫外线很弱，无需防护";
        if (index < 0.4) return "紫外线较弱，适当防护";
        if (index < 0.6) return "紫外线中等，外出建议涂抹防晒霜";
        if (index < 0.8) return "紫外线较强，外出需做好防护";
        return "紫外线很强，尽量避免长时间户外活动";
    }
    
    private List<String> getUVTips(double index) {
        if (index < 0.3) return Arrays.asList("无需特别防护");
        if (index < 0.5) return Arrays.asList("建议涂抹SPF15+防晒霜");
        if (index < 0.7) return Arrays.asList("涂抹SPF30+防晒霜", "戴遮阳帽");
        return Arrays.asList("涂抹SPF50+防晒霜", "戴遮阳帽和太阳镜", "尽量避免10-16点外出");
    }
    
    private double calculateCarWashIndex(WeatherResponse weather) {
        double precipitation = parseDouble(weather.getPrecipitation(), 0);
        double humidity = parseDouble(weather.getHumidity(), 50);
        String condition = weather.getWeatherCondition();
        
        double score = 100;
        
        if (precipitation > 10) score -= 60;
        else if (precipitation > 0) score -= 30;
        
        if (humidity > 80) score -= 20;
        
        if (condition != null) {
            if (condition.contains("雨") || condition.contains("雪")) score -= 40;
            if (condition.contains("沙尘") || condition.contains("霾")) score -= 25;
        }
        
        return Math.max(0, Math.min(100, score)) / 100.0;
    }
    
    private String getCarWashSuggestion(double index) {
        if (index < 0.25) return "不宜洗车，近期有降水或恶劣天气";
        if (index < 0.5) return "较不宜洗车，天气条件不理想";
        if (index < 0.75) return "较适宜洗车，天气条件尚可";
        return "适宜洗车，天气条件良好";
    }
    
    private List<String> getCarWashTips(double index) {
        if (index < 0.3) return Arrays.asList("建议等待天气好转后再洗车");
        if (index < 0.6) return Arrays.asList("如需洗车，建议选择室内洗车店");
        return Arrays.asList("适合洗车，可保持车辆清洁数日");
    }
    
    private double calculateTravelIndex(WeatherResponse weather) {
        double temp = parseDouble(weather.getTemperature(), 20);
        double precipitation = parseDouble(weather.getPrecipitation(), 0);
        String condition = weather.getWeatherCondition();
        
        double score = 100;
        
        if (temp < 0 || temp > 38) score -= 25;
        else if (temp < 5 || temp > 35) score -= 10;
        
        if (precipitation > 20) score -= 35;
        else if (precipitation > 5) score -= 15;
        
        if (condition != null) {
            if (condition.contains("暴雨") || condition.contains("大雪")) score -= 40;
            else if (condition.contains("雨") || condition.contains("雪")) score -= 20;
            if (condition.contains("霾") || condition.contains("沙尘")) score -= 15;
        }
        
        return Math.max(0, Math.min(100, score)) / 100.0;
    }
    
    private String getTravelSuggestion(double index) {
        if (index < 0.25) return "天气条件差，不建议外出旅游";
        if (index < 0.5) return "天气条件一般，适合近郊游";
        if (index < 0.75) return "天气条件较好，适合外出旅游";
        return "天气条件极佳，非常适合旅游出行";
    }
    
    private List<String> getTravelTips(double index) {
        if (index < 0.3) return Arrays.asList("建议选择室内景点游览");
        if (index < 0.6) return Arrays.asList("外出注意携带雨具");
        return Arrays.asList("适合户外活动和拍照", "享受美好旅程");
    }
    
    private double calculateAllergyIndex(WeatherResponse weather) {
        double humidity = parseDouble(weather.getHumidity(), 50);
        double windSpeed = parseDouble(weather.getWindSpeed(), 0);
        String condition = weather.getWeatherCondition();
        
        double score = 0;
        
        if (humidity < 40) score += 20;
        if (windSpeed > 15) score += 25;
        
        if (condition != null) {
            if (condition.contains("晴")) score += 20;
            if (condition.contains("霾")) score += 30;
            if (condition.contains("雨")) score -= 20;
        }
        
        return Math.max(0, Math.min(100, score)) / 100.0;
    }
    
    private String getAllergySuggestion(double index) {
        if (index < 0.2) return "过敏风险极低，可正常外出活动";
        if (index < 0.4) return "过敏风险较低，敏感人群注意防护";
        if (index < 0.6) return "过敏风险中等，敏感人群需注意";
        if (index < 0.8) return "过敏风险较高，敏感人群减少外出";
        return "过敏风险极高，敏感人群尽量避免外出";
    }
    
    private List<String> getAllergyTips(double index) {
        if (index < 0.3) return Arrays.asList("无需特别防护");
        if (index < 0.6) return Arrays.asList("外出可佩戴口罩");
        return Arrays.asList("外出佩戴口罩", "回家及时清洗面部", "关闭门窗减少花粉进入");
    }
    
    private double calculateAirQualityIndex(WeatherResponse weather) {
        double windSpeed = parseDouble(weather.getWindSpeed(), 0);
        double humidity = parseDouble(weather.getHumidity(), 50);
        String condition = weather.getWeatherCondition();
        
        double score = 50;
        
        if (windSpeed > 10) score += 20;
        if (windSpeed > 20) score += 15;
        
        if (humidity < 30 || humidity > 80) score -= 15;
        
        if (condition != null) {
            if (condition.contains("霾")) score -= 30;
            if (condition.contains("雾")) score -= 20;
            if (condition.contains("雨")) score += 15;
        }
        
        return Math.max(0, Math.min(100, score)) / 100.0;
    }
    
    private String getAirQualitySuggestion(double index) {
        if (index < 0.2) return "空气扩散条件差，污染物易积聚";
        if (index < 0.4) return "空气扩散条件较差";
        if (index < 0.6) return "空气扩散条件一般";
        if (index < 0.8) return "空气扩散条件较好";
        return "空气扩散条件好，有利于污染物扩散";
    }
    
    private List<String> getAirQualityTips(double index) {
        if (index < 0.3) return Arrays.asList("减少户外活动", "外出佩戴口罩");
        if (index < 0.6) return Arrays.asList("适当减少户外剧烈运动");
        return Arrays.asList("适合户外活动", "空气流通良好");
    }
    
    private double calculateComfortIndex(WeatherResponse weather) {
        double temp = parseDouble(weather.getTemperature(), 20);
        double humidity = parseDouble(weather.getHumidity(), 50);
        double windSpeed = parseDouble(weather.getWindSpeed(), 0);
        
        double comfort = 100;
        
        if (temp < 18) comfort -= (18 - temp) * 3;
        if (temp > 26) comfort -= (temp - 26) * 4;
        
        if (humidity < 30) comfort -= 10;
        if (humidity > 70) comfort -= (humidity - 70) * 0.5;
        
        if (windSpeed > 10) comfort -= (windSpeed - 10) * 0.5;
        
        return Math.max(0, Math.min(100, comfort)) / 100.0;
    }
    
    private String getComfortSuggestion(double index) {
        if (index < 0.15) return "极不舒适，请采取相应措施";
        if (index < 0.3) return "不舒适，建议调整环境";
        if (index < 0.45) return "较不舒适，注意调节";
        if (index < 0.6) return "舒适度一般";
        if (index < 0.75) return "较舒适，体感良好";
        if (index < 0.9) return "舒适，非常适合户外活动";
        return "非常舒适，体感极佳";
    }
    
    private List<String> getComfortTips(double index) {
        if (index < 0.3) return Arrays.asList("建议使用空调或暖气调节室温");
        if (index < 0.5) return Arrays.asList("适当增减衣物");
        if (index < 0.7) return Arrays.asList("体感适中，无需特别调节");
        return Arrays.asList("体感舒适，享受好天气");
    }
    
    private double calculateFishingIndex(WeatherResponse weather) {
        double temp = parseDouble(weather.getTemperature(), 20);
        double pressure = parseDouble(weather.getPressure(), 1013);
        String condition = weather.getWeatherCondition();
        
        double score = 50;
        
        if (temp >= 15 && temp <= 28) score += 20;
        else if (temp >= 10 && temp <= 32) score += 10;
        else score -= 15;
        
        if (pressure >= 1000 && pressure <= 1020) score += 15;
        else if (pressure < 1000) score -= 10;
        
        if (condition != null) {
            if (condition.contains("晴")) score += 10;
            if (condition.contains("多云")) score += 5;
            if (condition.contains("雨")) score -= 15;
            if (condition.contains("大风")) score -= 20;
        }
        
        return Math.max(0, Math.min(100, score)) / 100.0;
    }
    
    private String getFishingSuggestion(double index) {
        if (index < 0.2) return "不宜垂钓，天气条件不佳";
        if (index < 0.4) return "较不宜垂钓";
        if (index < 0.6) return "垂钓条件一般";
        if (index < 0.8) return "适宜垂钓";
        return "非常适宜垂钓，渔获率较高";
    }
    
    private List<String> getFishingTips(double index) {
        if (index < 0.3) return Arrays.asList("建议改日垂钓");
        if (index < 0.6) return Arrays.asList("选择背风向阳处");
        return Arrays.asList("适合垂钓的好时机", "注意防晒和安全");
    }
    
    private double calculateSunglassesIndex(WeatherResponse weather) {
        String condition = weather.getWeatherCondition();
        double uvIndex = calculateUVIndex(weather);
        
        if (uvIndex > 0.6) return 0.9;
        if (uvIndex > 0.4) return 0.6;
        
        if (condition != null && condition.contains("晴")) return 0.7;
        if (condition != null && condition.contains("多云")) return 0.4;
        
        return 0.2;
    }
    
    private String getSunglassesSuggestion(double index) {
        if (index < 0.3) return "不需要佩戴太阳镜";
        if (index < 0.6) return "建议佩戴太阳镜";
        return "需要佩戴太阳镜，保护眼睛";
    }
    
    private List<String> getSunglassesTips(double index) {
        if (index < 0.3) return Arrays.asList("光线适中，无需太阳镜");
        if (index < 0.6) return Arrays.asList("外出可佩戴太阳镜");
        return Arrays.asList("强烈建议佩戴太阳镜", "选择有UV防护的镜片");
    }
    
    private double calculateUmbrellaIndex(WeatherResponse weather) {
        double precipitation = parseDouble(weather.getPrecipitation(), 0);
        String condition = weather.getWeatherCondition();
        
        if (precipitation > 10) return 0.9;
        if (precipitation > 0) return 0.7;
        
        if (condition != null) {
            if (condition.contains("雨") || condition.contains("雪")) return 0.8;
            if (condition.contains("阴")) return 0.4;
        }
        
        return 0.2;
    }
    
    private String getUmbrellaSuggestion(double index) {
        if (index < 0.3) return "不需要携带雨伞";
        if (index < 0.6) return "建议携带雨伞";
        return "需要携带雨伞，以防下雨";
    }
    
    private List<String> getUmbrellaTips(double index) {
        if (index < 0.3) return Arrays.asList("天气晴好，无需雨具");
        if (index < 0.6) return Arrays.asList("有降雨可能，建议带伞");
        return Arrays.asList("有降雨，请务必带伞", "注意防雨防滑");
    }
    
    private double calculateDryingIndex(WeatherResponse weather) {
        double humidity = parseDouble(weather.getHumidity(), 50);
        double windSpeed = parseDouble(weather.getWindSpeed(), 0);
        double precipitation = parseDouble(weather.getPrecipitation(), 0);
        String condition = weather.getWeatherCondition();
        
        double score = 100;
        
        if (humidity > 70) score -= 30;
        else if (humidity > 50) score -= 10;
        
        if (precipitation > 0) score -= 40;
        
        if (condition != null) {
            if (condition.contains("雨") || condition.contains("雪")) score -= 30;
            if (condition.contains("晴")) score += 10;
        }
        
        if (windSpeed > 10) score += 10;
        
        return Math.max(0, Math.min(100, score)) / 100.0;
    }
    
    private String getDryingSuggestion(double index) {
        if (index < 0.2) return "不宜晾晒，天气条件不佳";
        if (index < 0.4) return "较不宜晾晒";
        if (index < 0.6) return "晾晒条件一般";
        if (index < 0.8) return "适宜晾晒";
        return "极适宜晾晒，衣物干得快";
    }
    
    private List<String> getDryingTips(double index) {
        if (index < 0.3) return Arrays.asList("建议室内晾干或使用烘干机");
        if (index < 0.6) return Arrays.asList("选择通风良好的地方晾晒");
        return Arrays.asList("适合户外晾晒", "注意及时收回衣物");
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
