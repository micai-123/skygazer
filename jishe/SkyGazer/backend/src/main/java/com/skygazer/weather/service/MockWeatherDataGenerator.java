package com.skygazer.weather.service;

import com.skygazer.weather.entity.WeatherData;
import com.skygazer.weather.dto.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class MockWeatherDataGenerator {

    private static final String[] WEATHER_CONDITIONS = {
        "晴", "多云", "阴", "小雨", "中雨", "大雨", "雷阵雨", 
        "小雪", "中雪", "大雪", "雾", "霾", "沙尘暴"
    };
    
    private static final String[] WIND_DIRECTIONS = {
        "东风", "南风", "西风", "北风", "东南风", "东北风", "西南风", "西北风"
    };
    
    private static final String[] CITIES = {
        "北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "武汉", 
        "西安", "重庆", "天津", "苏州", "长沙", "郑州", "青岛", "济南"
    };
    
    private final Random random = new Random();

    public WeatherData generateRealTimeWeather(String location) {
        log.info("生成模拟实时天气数据: {}", location);
        
        String weatherCondition = WEATHER_CONDITIONS[random.nextInt(WEATHER_CONDITIONS.length)];
        double temperature = generateTemperature();
        double humidity = generateHumidity();
        double windSpeed = generateWindSpeed();
        
        return WeatherData.builder()
            .location(location)
            .temperature(BigDecimal.valueOf(temperature))
            .feelsLike(BigDecimal.valueOf(temperature + random.nextDouble() * 4 - 2))
            .humidity(BigDecimal.valueOf(humidity))
            .windSpeed(BigDecimal.valueOf(windSpeed))
            .windDirection(WIND_DIRECTIONS[random.nextInt(WIND_DIRECTIONS.length)])
            .windScale(calculateWindScale(windSpeed))
            .weatherCondition(weatherCondition)
            .weatherDescription(generateWeatherDescription(weatherCondition))
            .airQualityIndex(generateAQI())
            .airQualityLevel(generateAQILevel())
            .pm25(random.nextInt(150))
            .pm10(random.nextInt(200))
            .uvIndex(random.nextInt(11))
            .visibility(BigDecimal.valueOf(random.nextDouble() * 20 + 5))
            .pressure(random.nextInt(100) + 1000)
            .precipitation(BigDecimal.valueOf(random.nextDouble() * 10))
            .recordTime(LocalDateTime.now())
            .dataSource("MOCK_DATA")
            .build();
    }

    public List<WeatherData> generateHourlyForecast(String location, int hours) {
        log.info("生成模拟小时预报数据: {}, {}小时", location, hours);
        
        List<WeatherData> forecasts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < hours; i++) {
            LocalDateTime forecastTime = now.plusHours(i);
            WeatherData forecast = generateRealTimeWeather(location);
            forecast.setRecordTime(forecastTime);
            forecasts.add(forecast);
        }
        
        return forecasts;
    }

    public List<WeatherData> generateWeeklyForecast(String location) {
        log.info("生成模拟周预报数据: {}", location);
        
        List<WeatherData> forecasts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < 7; i++) {
            LocalDateTime forecastDate = now.plusDays(i);
            WeatherData forecast = generateRealTimeWeather(location);
            forecast.setRecordTime(forecastDate);
            
            double baseTemp = forecast.getTemperature().doubleValue();
            forecast.setTemperature(BigDecimal.valueOf(baseTemp));
            
            forecasts.add(forecast);
        }
        
        return forecasts;
    }

    public Map<String, Object> generateLifestyleIndices(String location) {
        log.info("生成模拟生活指数数据: {}", location);
        
        Map<String, Object> indices = new LinkedHashMap<>();
        
        indices.put("穿衣指数", generateIndex("穿衣", "建议穿长袖衬衫、单裤等服装"));
        indices.put("紫外线指数", generateIndex("紫外线", "紫外线强度中等，建议涂擦SPF15-20防晒护肤品"));
        indices.put("洗车指数", generateIndex("洗车", "较不宜洗车，未来一天无雨，风力较小"));
        indices.put("旅游指数", generateIndex("旅游", "天气较好，适宜出游"));
        indices.put("感冒指数", generateIndex("感冒", "各项气象条件适宜，无明显降温过程，发生感冒机率较低"));
        indices.put("运动指数", generateIndex("运动", "天气较好，赶快投身大自然参与户外运动"));
        indices.put("过敏指数", generateIndex("过敏", "空气中的花粉含量较低，不易引发过敏"));
        indices.put("空气污染扩散指数", generateIndex("空气污染扩散", "气象条件有利于空气污染物扩散"));
        
        return indices;
    }

    public Map<String, Object> generateWeatherAnalysis(String location) {
        log.info("生成模拟天气分析数据: {}", location);
        
        Map<String, Object> analysis = new LinkedHashMap<>();
        
        analysis.put("currentCondition", generateRealTimeWeather(location));
        analysis.put("trend", generateTrendAnalysis());
        analysis.put("alerts", generateWeatherAlerts());
        analysis.put("suggestions", generateWeatherSuggestions());
        
        return analysis;
    }

    public Map<String, Object> generateAIAssistantData(String location) {
        log.info("生成模拟AI智慧助理数据: {}", location);
        
        Map<String, Object> aiData = new LinkedHashMap<>();
        
        aiData.put("weatherSummary", generateWeatherSummary(location));
        aiData.put("recommendations", generateAIRecommendations());
        aiData.put("insights", generateAIInsights());
        aiData.put("predictions", generateAIPredictions());
        
        return aiData;
    }

    private double generateTemperature() {
        int month = LocalDateTime.now().getMonthValue();
        double baseTemp;
        
        if (month >= 3 && month <= 5) {
            baseTemp = 15 + random.nextDouble() * 10;
        } else if (month >= 6 && month <= 8) {
            baseTemp = 25 + random.nextDouble() * 10;
        } else if (month >= 9 && month <= 11) {
            baseTemp = 10 + random.nextDouble() * 10;
        } else {
            baseTemp = -5 + random.nextDouble() * 10;
        }
        
        return BigDecimal.valueOf(baseTemp).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private double generateHumidity() {
        return BigDecimal.valueOf(40 + random.nextDouble() * 40)
            .setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private double generateWindSpeed() {
        return BigDecimal.valueOf(random.nextDouble() * 15)
            .setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private String calculateWindScale(double windSpeed) {
        if (windSpeed < 1) return "1级";
        if (windSpeed < 6) return "2级";
        if (windSpeed < 12) return "3级";
        if (windSpeed < 20) return "4级";
        if (windSpeed < 29) return "5级";
        return "6级";
    }

    private int generateAQI() {
        int aqi = random.nextInt(200);
        return Math.min(aqi, 300);
    }

    private String generateAQILevel() {
        int aqi = generateAQI();
        if (aqi <= 50) return "优";
        if (aqi <= 100) return "良";
        if (aqi <= 150) return "轻度污染";
        if (aqi <= 200) return "中度污染";
        return "重度污染";
    }

    private String generateWeatherDescription(String condition) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("晴", "天气晴朗，阳光明媚，适合户外活动");
        descriptions.put("多云", "云量较多，气温适宜，适合外出");
        descriptions.put("阴", "阴天，气温较低，注意保暖");
        descriptions.put("小雨", "小雨淅沥，出门请带伞");
        descriptions.put("中雨", "中雨，路面湿滑，注意安全");
        descriptions.put("大雨", "大雨倾盆，建议减少外出");
        descriptions.put("雷阵雨", "雷阵雨，请注意防雷避雨");
        descriptions.put("小雪", "小雪纷飞，路面可能结冰");
        descriptions.put("中雪", "中雪，注意防寒保暖");
        descriptions.put("大雪", "大雪，建议减少外出");
        descriptions.put("雾", "大雾弥漫，能见度低，注意安全");
        descriptions.put("霾", "霾天气，空气质量较差，建议戴口罩");
        descriptions.put("沙尘暴", "沙尘暴，空气质量极差，请避免外出");
        
        return descriptions.getOrDefault(condition, "天气状况正常");
    }

    private Map<String, Object> generateIndex(String name, String description) {
        Map<String, Object> index = new HashMap<>();
        String[] levels = {"极适宜", "适宜", "较适宜", "较不适宜", "不适宜", "极不适宜"};
        index.put("level", levels[random.nextInt(levels.length)]);
        index.put("description", description);
        index.put("score", random.nextInt(5) + 1);
        return index;
    }

    private Map<String, Object> generateTrendAnalysis() {
        Map<String, Object> trend = new HashMap<>();
        trend.put("temperatureTrend", "未来24小时气温将小幅波动，整体呈上升趋势");
        trend.put("humidityTrend", "湿度将保持在40%-60%之间");
        trend.put("pressureTrend", "气压稳定，无明显变化");
        trend.put("windTrend", "风力将逐渐增强，预计傍晚达到峰值");
        return trend;
    }

    private List<Map<String, Object>> generateWeatherAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        if (random.nextBoolean()) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "高温预警");
            alert.put("level", "黄色");
            alert.put("description", "预计未来24小时最高气温将达到35℃以上");
            alert.put("suggestion", "请注意防暑降温，减少户外活动");
            alerts.add(alert);
        }
        
        if (random.nextBoolean()) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "空气质量预警");
            alert.put("level", "橙色");
            alert.put("description", "预计未来24小时空气质量将达到中度污染");
            alert.put("suggestion", "建议减少户外活动，外出请佩戴口罩");
            alerts.add(alert);
        }
        
        return alerts;
    }

    private List<String> generateWeatherSuggestions() {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("建议穿着轻薄透气的衣物");
        suggestions.add("外出时请携带遮阳伞");
        suggestions.add("注意补充水分，避免中暑");
        suggestions.add("空气质量一般，敏感人群请减少外出");
        suggestions.add("紫外线较强，请做好防晒措施");
        return suggestions;
    }

    private String generateWeatherSummary(String location) {
        WeatherData weather = generateRealTimeWeather(location);
        return String.format("当前%s天气%s，气温%.1f℃，湿度%.1f%%，空气质量%s级。%s",
            location,
            weather.getWeatherCondition(),
            weather.getTemperature().doubleValue(),
            weather.getHumidity().doubleValue(),
            weather.getAirQualityLevel(),
            weather.getWeatherDescription()
        );
    }

    private List<String> generateAIRecommendations() {
        List<String> recommendations = new ArrayList<>();
        recommendations.add("根据当前天气状况，建议您选择室内运动");
        recommendations.add("今天适合进行轻度户外活动，如散步、慢跑");
        recommendations.add("建议携带雨具，以防突发降雨");
        recommendations.add("空气质量一般，敏感人群请佩戴口罩");
        recommendations.add("紫外线强度中等，建议涂抹防晒霜");
        return recommendations;
    }

    private Map<String, Object> generateAIInsights() {
        Map<String, Object> insights = new HashMap<>();
        insights.put("temperatureInsight", "当前温度处于舒适区间，适合大多数户外活动");
        insights.put("humidityInsight", "湿度适中，人体感觉舒适");
        insights.put("airQualityInsight", "空气质量良好，适合户外运动");
        insights.put("uvInsight", "紫外线强度中等，建议做好防晒措施");
        insights.put("windInsight", "风力适中，适合户外活动");
        return insights;
    }

    private Map<String, Object> generateAIPredictions() {
        Map<String, Object> predictions = new HashMap<>();
        
        predictions.put("shortTerm", "未来3小时内天气状况将保持稳定");
        predictions.put("mediumTerm", "未来24小时可能有降雨，建议携带雨具");
        predictions.put("longTerm", "未来一周气温将逐渐上升，周末可能达到高温");
        predictions.put("confidence", "85%");
        
        return predictions;
    }
}
