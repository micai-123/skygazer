package com.skygazer.weather.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.dto.airquality.AirQualityIndex;
import com.skygazer.weather.dto.airquality.AirQualityResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.entity.WeatherData;
import com.skygazer.weather.repository.WeatherDataRepository;
import com.skygazer.weather.service.BatchWeatherInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchWeatherInitServiceImpl implements BatchWeatherInitService {
    
    private final WeatherDataRepository weatherDataRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${weather.api.key}")
    private String apiKey;
    
    @Value("${weather.api.base-url}")
    private String baseUrl;
    
    private static final int MAX_RETRY_TIMES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long API_CALL_DELAY_MS = 500;
    
    private static final List<CityInfo> MAJOR_CITIES = Arrays.asList(
        new CityInfo("北京", "101010100", 39.90, 116.40),
        new CityInfo("上海", "101020100", 31.23, 121.47),
        new CityInfo("天津", "101030100", 39.13, 117.20),
        new CityInfo("重庆", "101040100", 29.56, 106.55),
        new CityInfo("广州", "101280101", 23.16, 113.23),
        new CityInfo("深圳", "101280601", 22.54, 114.06),
        new CityInfo("杭州", "101210101", 30.25, 120.17),
        new CityInfo("南京", "101190101", 32.06, 118.80),
        new CityInfo("成都", "101270101", 30.67, 104.07),
        new CityInfo("武汉", "101200101", 30.58, 114.30),
        new CityInfo("西安", "101110101", 34.26, 108.95),
        new CityInfo("苏州", "101190401", 31.30, 120.62),
        new CityInfo("郑州", "101180101", 34.76, 113.65),
        new CityInfo("长沙", "101250101", 28.23, 112.94),
        new CityInfo("青岛", "101120201", 36.07, 120.38),
        new CityInfo("大连", "101070201", 38.91, 121.62),
        new CityInfo("宁波", "101210401", 29.87, 121.55),
        new CityInfo("厦门", "101230201", 24.48, 118.09),
        new CityInfo("无锡", "101190201", 31.49, 120.31),
        new CityInfo("合肥", "101220101", 31.82, 117.23),
        new CityInfo("昆明", "101290101", 25.04, 102.71),
        new CityInfo("哈尔滨", "101050101", 45.80, 126.53),
        new CityInfo("济南", "101120101", 36.65, 117.12),
        new CityInfo("福州", "101230101", 26.07, 119.30),
        new CityInfo("沈阳", "101070101", 41.80, 123.43),
        new CityInfo("长春", "101060101", 43.82, 125.32),
        new CityInfo("南昌", "101240101", 28.68, 115.89),
        new CityInfo("贵阳", "101260101", 26.65, 106.63),
        new CityInfo("太原", "101100101", 37.87, 112.55),
        new CityInfo("石家庄", "101090101", 38.04, 114.51),
        new CityInfo("兰州", "101160101", 36.06, 103.83),
        new CityInfo("南宁", "101300101", 22.82, 108.32),
        new CityInfo("海口", "101310101", 20.04, 110.32),
        new CityInfo("银川", "101170101", 38.49, 106.23),
        new CityInfo("西宁", "101150101", 36.62, 101.78),
        new CityInfo("乌鲁木齐", "101130101", 43.79, 87.62),
        new CityInfo("拉萨", "101140101", 29.65, 91.13),
        new CityInfo("呼和浩特", "101080101", 40.84, 111.75)
    );
    
    @Override
    public void initializeAllCitiesWeather() {
        log.info("========== 开始批量初始化城市天气数据 ==========");
        log.info("总共需要初始化 {} 个城市", MAJOR_CITIES.size());
        
        // ========== 原和风天气API调用代码（已注释保留） ==========
        // if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_qweather_api_key_here")) {
        //     log.warn("和风天气API密钥未配置，将为所有城市设置默认天气数据");
        //     initializeWithDefaultData();
        //     return;
        // }
        // 
        // int successCount = 0;
        // int failureCount = 0;
        // 
        // for (int i = 0; i < MAJOR_CITIES.size(); i++) {
        //     CityInfo city = MAJOR_CITIES.get(i);
        //     log.info("正在处理 [{}/{}]: {}", i + 1, MAJOR_CITIES.size(), city.name);
        //     
        //     try {
        //         WeatherResponse response = fetchWeatherForCity(city.name, city.code, city.lat, city.lng);
        //         if (response != null) {
        //             successCount++;
        //             log.info("✓ {} 天气数据获取成功 - 温度: {}°C, 天气: {}", 
        //                 city.name, response.getTemperature(), response.getWeatherCondition());
        //         } else {
        //             failureCount++;
        //             log.warn("✗ {} 天气数据获取失败，使用默认数据", city.name);
        //             saveDefaultWeatherData(city.name);
        //         }
        //         
        //         if (i < MAJOR_CITIES.size() - 1) {
        //             Thread.sleep(API_CALL_DELAY_MS);
        //         }
        //         
        //     } catch (Exception e) {
        //         failureCount++;
        //         log.error("✗ {} 天气数据获取异常: {}", city.name, e.getMessage());
        //         saveDefaultWeatherData(city.name);
        //     }
        // }
        // 
        // log.info("========== 批量初始化完成 ==========");
        // log.info("成功: {} 个城市", successCount);
        // log.info("失败: {} 个城市", failureCount);
        // log.info("总计: {} 个城市", MAJOR_CITIES.size());
        // ========== 原和风天气API调用代码结束 ==========
        
        // 替代实现：使用默认数据初始化所有城市
        log.info("跳过和风天气API调用，使用默认数据初始化所有城市");
        initializeWithDefaultData();
        
        log.info("========== 批量初始化完成 ==========");
        log.info("总计: {} 个城市", MAJOR_CITIES.size());
    }
    
    @Override
    public WeatherResponse fetchWeatherForCity(String cityName, String cityCode, Double lat, Double lng) {
        // ========== 原和风天气API调用代码（已注释保留） ==========
        // for (int retry = 0; retry < MAX_RETRY_TIMES; retry++) {
        //     try {
        //         log.debug("第 {} 次尝试获取 {} 的天气数据", retry + 1, cityName);
        //         
        //         String weatherUrl = baseUrl + "/weather/now?location=" + cityCode + "&key=" + apiKey;
        //         String weatherResponseStr = webClient.get()
        //             .uri(weatherUrl)
        //             .retrieve()
        //             .bodyToMono(String.class)
        //             .block();
        //         
        //         Map<String, Object> weatherResponse = objectMapper.readValue(weatherResponseStr, Map.class);
        //         
        //         if (weatherResponse == null || !"200".equals(String.valueOf(weatherResponse.get("code")))) {
        //             log.warn("{} API返回错误码: {}", cityName, weatherResponse != null ? weatherResponse.get("code") : "null");
        //             if (retry < MAX_RETRY_TIMES - 1) {
        //                 Thread.sleep(RETRY_DELAY_MS);
        //                 continue;
        //             }
        //             return null;
        //         }
        //         
        //         Map<String, Object> nowData = (Map<String, Object>) weatherResponse.get("now");
        //         
        //         AirQualityData airQualityData = fetchAirQualityData(cityName, lat, lng);
        //         
        //         WeatherData weatherData = WeatherData.builder()
        //             .location(cityName)
        //             .temperature(new BigDecimal(String.valueOf(nowData.get("temp"))))
        //             .feelsLike(new BigDecimal(String.valueOf(nowData.get("feelsLike"))))
        //             .humidity(new BigDecimal(String.valueOf(nowData.get("humidity"))))
        //             .windSpeed(new BigDecimal(String.valueOf(nowData.get("windSpeed"))))
        //             .windDirection(String.valueOf(nowData.get("windDir")))
        //             .windScale(String.valueOf(nowData.get("windScale")))
        //             .weatherCondition(String.valueOf(nowData.get("text")))
        //             .weatherDescription(getWeatherDescription(String.valueOf(nowData.get("text"))))
        //             .airQualityIndex(airQualityData.aqi)
        //             .airQualityLevel(airQualityData.category)
        //             .pm25(airQualityData.pm25)
        //             .pm10(airQualityData.pm10)
        //             .uvIndex(5)
        //             .visibility(new BigDecimal(String.valueOf(nowData.get("vis"))))
        //             .pressure(Integer.parseInt(String.valueOf(nowData.get("pressure"))))
        //             .precipitation(new BigDecimal(String.valueOf(nowData.get("precip"))))
        //             .recordTime(LocalDateTime.now())
        //             .dataSource("qweather")
        //             .build();
        //         
        //         WeatherData savedData = weatherDataRepository.save(weatherData);
        //         
        //         return convertToResponse(savedData);
        //         
        //     } catch (Exception e) {
        //         log.error("第 {} 次获取 {} 天气数据失败: {}", retry + 1, cityName, e.getMessage());
        //         if (retry < MAX_RETRY_TIMES - 1) {
        //             try {
        //                 Thread.sleep(RETRY_DELAY_MS);
        //             } catch (InterruptedException ie) {
        //                 Thread.currentThread().interrupt();
        //                 return null;
        //             }
        //         }
        //     }
        // }
        // ========== 原和风天气API调用代码结束 ==========
        
        // 替代实现：使用默认数据
        log.info("跳过和风天气API调用，使用默认数据: {}", cityName);
        saveDefaultWeatherData(cityName);
        
        Map<String, Object> defaultData = getDefaultWeatherData(cityName);
        return WeatherResponse.builder()
            .location(cityName)
            .temperature(new BigDecimal(String.valueOf(defaultData.get("temperature"))))
            .feelsLike(new BigDecimal(String.valueOf(defaultData.get("feelsLike"))))
            .humidity(new BigDecimal(String.valueOf(defaultData.get("humidity"))))
            .windSpeed(new BigDecimal(String.valueOf(defaultData.get("windSpeed"))))
            .windDirection(String.valueOf(defaultData.get("windDirection")))
            .windScale(String.valueOf(defaultData.get("windScale")))
            .weatherCondition(String.valueOf(defaultData.get("weatherCondition")))
            .weatherDescription(String.valueOf(defaultData.get("weatherDescription")))
            .airQualityIndex((Integer) defaultData.get("airQualityIndex"))
            .airQualityLevel(String.valueOf(defaultData.get("airQualityLevel")))
            .pm25((Integer) defaultData.get("pm25"))
            .pm10((Integer) defaultData.get("pm10"))
            .uvIndex((Integer) defaultData.get("uvIndex"))
            .visibility(new BigDecimal(String.valueOf(defaultData.get("visibility"))))
            .pressure((Integer) defaultData.get("pressure"))
            .precipitation(new BigDecimal(String.valueOf(defaultData.get("precipitation"))))
            .build();
    }
    
    private AirQualityData fetchAirQualityData(String cityName, Double lat, Double lng) {
        // ========== 原和风天气API调用代码（已注释保留） ==========
        // try {
        //     if (lat == null || lng == null) {
        //         log.warn("城市 {} 缺少坐标信息，使用默认空气质量数据", cityName);
        //         return getDefaultAirQualityData();
        //     }
        //     
        //     String airBaseUrl = baseUrl.replace("/v7", "");
        //     String airUrl = String.format("%s/airquality/v1/current/%.2f/%.2f?key=%s", 
        //         airBaseUrl, lat, lng, apiKey);
        //     
        //     String airResponseStr = webClient.get()
        //         .uri(airUrl)
        //         .retrieve()
        //         .bodyToMono(String.class)
        //         .block();
        //     
        //     AirQualityResponse airResponse = objectMapper.readValue(airResponseStr, AirQualityResponse.class);
        //     
        //     if (airResponse == null || airResponse.getIndexes() == null || airResponse.getIndexes().isEmpty()) {
        //         log.warn("城市 {} 空气质量API返回数据为空，使用默认值", cityName);
        //         return getDefaultAirQualityData();
        //     }
        //     
        //     Optional<AirQualityIndex> cnMeeIndex = airResponse.getIndexes().stream()
        //         .filter(idx -> "cn-mee".equals(idx.getCode()))
        //         .findFirst();
        //     
        //     if (cnMeeIndex.isPresent()) {
        //         AirQualityIndex aqi = cnMeeIndex.get();
        //         Integer pm25 = null;
        //         Integer pm10 = null;
        //         
        //         if (airResponse.getPollutants() != null) {
        //             for (Object pollutantObj : airResponse.getPollutants()) {
        //                 Map<String, Object> pollutant = (Map<String, Object>) pollutantObj;
        //                 String code = (String) pollutant.get("code");
        //                 Map<String, Object> concentration = (Map<String, Object>) pollutant.get("concentration");
        //                 if (concentration != null && concentration.get("value") != null) {
        //                     Double value = ((Number) concentration.get("value")).doubleValue();
        //                     if ("pm2p5".equals(code)) {
        //                         pm25 = value.intValue();
        //                     } else if ("pm10".equals(code)) {
        //                         pm10 = value.intValue();
        //                     }
        //                 }
        //             }
        //         }
        //         
        //         log.debug("成功获取 {} 空气质量数据 - AQI: {}, 等级: {}", cityName, aqi.getAqi(), aqi.getCategory());
        //         return new AirQualityData(aqi.getAqi(), aqi.getCategory(), pm25, pm10);
        //     }
        //     
        //     return getDefaultAirQualityData();
        //     
        // } catch (Exception e) {
        //     log.warn("获取 {} 空气质量数据失败: {}, 使用默认值", cityName, e.getMessage());
        //     return getDefaultAirQualityData();
        // }
        // ========== 原和风天气API调用代码结束 ==========
        
        // 替代实现：使用默认空气质量数据
        log.debug("跳过和风天气API调用，使用默认空气质量数据: {}", cityName);
        return getDefaultAirQualityData();
    }
    
    @Override
    public Map<String, Object> getDefaultWeatherData(String cityName) {
        Map<String, Object> defaultData = new HashMap<>();
        defaultData.put("location", cityName);
        defaultData.put("temperature", 22);
        defaultData.put("feelsLike", 21);
        defaultData.put("humidity", 60);
        defaultData.put("windSpeed", 3);
        defaultData.put("windDirection", "微风");
        defaultData.put("windScale", "1");
        defaultData.put("weatherCondition", "晴");
        defaultData.put("weatherDescription", "天气状况：晴");
        defaultData.put("airQualityIndex", 50);
        defaultData.put("airQualityLevel", "良");
        defaultData.put("pm25", 25);
        defaultData.put("pm10", 50);
        defaultData.put("uvIndex", 5);
        defaultData.put("visibility", 10);
        defaultData.put("pressure", 1013);
        defaultData.put("precipitation", 0);
        defaultData.put("dataSource", "default");
        return defaultData;
    }
    
    private void initializeWithDefaultData() {
        log.info("开始为所有城市设置默认天气数据");
        for (CityInfo city : MAJOR_CITIES) {
            saveDefaultWeatherData(city.name);
        }
        log.info("默认天气数据设置完成");
    }
    
    private void saveDefaultWeatherData(String cityName) {
        try {
            Map<String, Object> defaultData = getDefaultWeatherData(cityName);
            
            WeatherData weatherData = WeatherData.builder()
                .location(cityName)
                .temperature(new BigDecimal(String.valueOf(defaultData.get("temperature"))))
                .feelsLike(new BigDecimal(String.valueOf(defaultData.get("feelsLike"))))
                .humidity(new BigDecimal(String.valueOf(defaultData.get("humidity"))))
                .windSpeed(new BigDecimal(String.valueOf(defaultData.get("windSpeed"))))
                .windDirection(String.valueOf(defaultData.get("windDirection")))
                .windScale(String.valueOf(defaultData.get("windScale")))
                .weatherCondition(String.valueOf(defaultData.get("weatherCondition")))
                .weatherDescription(String.valueOf(defaultData.get("weatherDescription")))
                .airQualityIndex((Integer) defaultData.get("airQualityIndex"))
                .airQualityLevel(String.valueOf(defaultData.get("airQualityLevel")))
                .pm25((Integer) defaultData.get("pm25"))
                .pm10((Integer) defaultData.get("pm10"))
                .uvIndex((Integer) defaultData.get("uvIndex"))
                .visibility(new BigDecimal(String.valueOf(defaultData.get("visibility"))))
                .pressure((Integer) defaultData.get("pressure"))
                .precipitation(new BigDecimal(String.valueOf(defaultData.get("precipitation"))))
                .recordTime(LocalDateTime.now())
                .dataSource(String.valueOf(defaultData.get("dataSource")))
                .build();
            
            weatherDataRepository.save(weatherData);
            log.debug("已为 {} 保存默认天气数据", cityName);
            
        } catch (Exception e) {
            log.error("保存 {} 默认天气数据失败: {}", cityName, e.getMessage());
        }
    }
    
    private AirQualityData getDefaultAirQualityData() {
        return new AirQualityData(50, "良", 25, 50);
    }
    
    private String getWeatherDescription(String condition) {
        return "天气状况：" + condition;
    }
    
    private WeatherResponse convertToResponse(WeatherData data) {
        return WeatherResponse.builder()
            .location(data.getLocation())
            .temperature(data.getTemperature())
            .feelsLike(data.getFeelsLike())
            .humidity(data.getHumidity())
            .windSpeed(data.getWindSpeed())
            .windDirection(data.getWindDirection())
            .windScale(data.getWindScale())
            .weatherCondition(data.getWeatherCondition())
            .weatherDescription(data.getWeatherDescription())
            .airQualityIndex(data.getAirQualityIndex())
            .airQualityLevel(data.getAirQualityLevel())
            .pm25(data.getPm25())
            .pm10(data.getPm10())
            .uvIndex(data.getUvIndex())
            .visibility(data.getVisibility())
            .pressure(data.getPressure())
            .precipitation(data.getPrecipitation())
            .recordTime(data.getRecordTime())
            .build();
    }
    
    private static class CityInfo {
        String name;
        String code;
        Double lat;
        Double lng;
        
        CityInfo(String name, String code, Double lat, Double lng) {
            this.name = name;
            this.code = code;
            this.lat = lat;
            this.lng = lng;
        }
    }
    
    private static class AirQualityData {
        Integer aqi;
        String category;
        Integer pm25;
        Integer pm10;
        
        AirQualityData(Integer aqi, String category, Integer pm25, Integer pm10) {
            this.aqi = aqi;
            this.category = category;
            this.pm25 = pm25;
            this.pm10 = pm10;
        }
    }
}
