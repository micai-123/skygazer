package com.skygazer.weather.service.impl;

import com.skygazer.weather.constant.CacheConstants;
import com.skygazer.weather.dto.request.BatchWeatherRequest;
import com.skygazer.weather.dto.response.BatchWeatherResponse;
import com.skygazer.weather.dto.response.HourlyForecastResponse;
import com.skygazer.weather.dto.response.LifestyleResponse;
import com.skygazer.weather.dto.response.WeatherAnalysisResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.dto.response.WeeklyForecastResponse;
import com.skygazer.weather.dto.airquality.AirQualityResponse;
import com.skygazer.weather.dto.airquality.AirQualityIndex;
import com.skygazer.weather.dto.airquality.Pollutant;
import com.skygazer.weather.entity.WeatherData;
import com.skygazer.weather.exception.BusinessException;
import com.skygazer.weather.repository.WeatherDataRepository;
import com.skygazer.weather.service.WeatherService;
import com.skygazer.weather.util.RedisUtil;
import com.skygazer.weather.util.CityCoordinate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {
    
    private final WeatherDataRepository weatherDataRepository;
    private final RedisUtil redisUtil;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${weather.api.key}")
    private String apiKey;
    
    @Value("${weather.api.base-url}")
    private String baseUrl;
    
    @Override
    public WeatherResponse getCurrentWeather(String location) {
        // 二级缓存架构：优先从Redis缓存获取，若未命中则从数据库获取并同步更新到Redis
        String cacheKey = CacheConstants.WEATHER_CURRENT + location;
        
        // 第一级缓存：Redis
        WeatherResponse cached = redisUtil.get(cacheKey, WeatherResponse.class);
        if (cached != null) {
            log.debug("从Redis缓存获取天气数据: {}", location);
            return cached;
        }
        
        // 第二级缓存：数据库
        WeatherData weatherData = weatherDataRepository
            .findFirstByLocationOrderByRecordTimeDesc(location)
            .orElse(null);
        
        WeatherResponse response;
        if (weatherData != null) {
            log.debug("从数据库获取天气数据: {}", location);
            response = convertToResponse(weatherData);
            // 同步更新到Redis缓存
            redisUtil.set(cacheKey, response, CacheConstants.WEATHER_CACHE_TTL);
        } else {
            // 数据库也无数据，生成模拟数据
            log.debug("数据库无数据，生成模拟数据: {}", location);
            WeatherData mockData = generateMockWeatherData(location);
            response = convertToResponse(mockData);
            // 同步更新到Redis缓存
            redisUtil.set(cacheKey, response, CacheConstants.WEATHER_CACHE_TTL);
        }
        
        return response;
    }
    
    @Override
    public HourlyForecastResponse getHourlyForecast(String location) {
        String cacheKey = CacheConstants.WEATHER_HOURLY + location;
        HourlyForecastResponse cached = redisUtil.get(cacheKey, HourlyForecastResponse.class);
        if (cached != null) {
            return cached;
        }
        
        HourlyForecastResponse response;
        
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_qweather_api_key_here")) {
            log.warn("和风天气API密钥未配置，生成72小时模拟预报数据");
            response = generateMockHourlyForecast(location);
        } else {
            response = fetchHourlyForecastFromAPI(location);
            if (response == null || response.getHourlyData() == null || response.getHourlyData().isEmpty()) {
                log.warn("从API获取小时预报失败，使用模拟数据");
                response = generateMockHourlyForecast(location);
            }
        }
        
        redisUtil.set(cacheKey, response, CacheConstants.WEATHER_CACHE_TTL);
        return response;
    }
    
    private HourlyForecastResponse fetchHourlyForecastFromAPI(String location) {
        // ========== 原和风天气API调用代码（已注释保留） ==========
        // try {
        //     String locationId = getLocationId(location);
        //     String hourlyUrl = baseUrl + "/weather/72h?location=" + locationId + "&key=" + apiKey;
        //     
        //     log.info("调用和风天气72小时预报API: {}", hourlyUrl);
        //     
        //     String responseStr = webClient.get()
        //         .uri(hourlyUrl)
        //         .retrieve()
        //         .bodyToMono(String.class)
        //         .block();
        //     
        //     log.info("72小时预报API响应: {}", responseStr != null ? responseStr.substring(0, Math.min(500, responseStr.length())) : "null");
        //     
        //     Map<String, Object> response = objectMapper.readValue(responseStr, Map.class);
        //     
        //     if (response == null || !"200".equals(String.valueOf(response.get("code")))) {
        //         log.warn("72小时预报API返回错误: {}", response != null ? response.get("code") : "null");
        //         return null;
        //     }
        //     
        //     List<Map<String, Object>> hourlyList = (List<Map<String, Object>>) response.get("hourly");
        //     
        //     if (hourlyList == null || hourlyList.isEmpty()) {
        //         log.warn("72小时预报API返回数据为空");
        //         return null;
        //     }
        //     
        //     List<HourlyForecastResponse.HourlyData> hourlyData = new ArrayList<>();
        //     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'+08:00'");
        //     
        //     for (Map<String, Object> item : hourlyList) {
        //         try {
        //             String timeStr = (String) item.get("fxTime");
        //             LocalDateTime time;
        //             
        //             try {
        //                 time = LocalDateTime.parse(timeStr, formatter);
        //             } catch (Exception e) {
        //                 time = LocalDateTime.parse(timeStr.substring(0, 19));
        //             }
        //             
        //             HourlyForecastResponse.HourlyData data = HourlyForecastResponse.HourlyData.builder()
        //                 .time(time)
        //                 .temperature(new BigDecimal(String.valueOf(item.get("temp"))))
        //                 .weatherCondition(String.valueOf(item.get("text")))
        //                 .precipitation(new BigDecimal(String.valueOf(item.get("precip"))))
        //                 .windSpeed(new BigDecimal(String.valueOf(item.get("windSpeed"))))
        //                 .windDirection(String.valueOf(item.get("windDir")))
        //                 .humidity(Integer.parseInt(String.valueOf(item.get("humidity"))))
        //                 .build();
        //             
        //             hourlyData.add(data);
        //         } catch (Exception e) {
        //             log.warn("解析小时预报数据失败: {}", e.getMessage());
        //         }
        //     }
        //     
        //     log.info("成功获取 {} 条72小时预报数据", hourlyData.size());
        //     
        //     return HourlyForecastResponse.builder()
        //         .location(location)
        //         .hourlyData(hourlyData)
        //         .build();
        //         
        // } catch (Exception e) {
        //     log.error("获取72小时预报数据失败: {}", e.getMessage(), e);
        //     return null;
        // }
        // ========== 原和风天气API调用代码结束 ==========
        
        // 替代实现：从数据库获取历史数据生成预报，若无数据则生成模拟数据
        log.info("从数据库获取小时预报数据，位置: {}", location);
        
        List<WeatherData> recentData = weatherDataRepository.findRecentByLocation(location, LocalDateTime.now().minusHours(24));
        if (recentData != null && !recentData.isEmpty()) {
            log.info("从数据库获取到 {} 条历史数据，用于生成预报", recentData.size());
            return generateHourlyForecastFromHistory(location, recentData);
        }
        
        log.info("数据库中无 {} 的历史数据，生成模拟预报数据", location);
        return generateMockHourlyForecast(location);
    }
    
    private HourlyForecastResponse generateHourlyForecastFromHistory(String location, List<WeatherData> historyData) {
        List<HourlyForecastResponse.HourlyData> hourlyData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        WeatherData latestData = historyData.get(0);
        double baseTemp = latestData.getTemperature() != null ? latestData.getTemperature().doubleValue() : 25.0;
        double baseHumidity = latestData.getHumidity() != null ? latestData.getHumidity().doubleValue() : 60.0;
        String baseCondition = latestData.getWeatherCondition() != null ? latestData.getWeatherCondition() : "晴";
        
        String[] conditions = {"晴", "晴", "多云", "多云", "阴", "小雨", "中雨", "大雨", "暴雨", "晴", "多云", "雷阵雨"};
        String[] windDirections = {"东南风", "南风", "西南风", "东风", "东北风", "北风", "西北风", "西风"};
        
        for (int i = 0; i < 72; i++) {
            LocalDateTime hourTime = now.plusHours(i);
            
            double dailyCycle = Math.sin((i % 24 - 6) * Math.PI / 12);
            double temp = baseTemp + 5 * dailyCycle + (Math.random() * 4 - 2);
            
            String condition = baseCondition;
            if (Math.random() > 0.7) {
                condition = conditions[(i / 8) % conditions.length];
            }
            
            double precipitation = 0;
            if (condition.contains("暴雨")) {
                precipitation = 30 + Math.random() * 40;
            } else if (condition.contains("大雨")) {
                precipitation = 15 + Math.random() * 15;
            } else if (condition.contains("中雨")) {
                precipitation = 5 + Math.random() * 10;
            } else if (condition.contains("雨") || condition.contains("雷阵雨")) {
                precipitation = 0.5 + Math.random() * 5;
            }
            
            hourlyData.add(HourlyForecastResponse.HourlyData.builder()
                .time(hourTime)
                .temperature(BigDecimal.valueOf(Math.round(temp)))
                .weatherCondition(condition)
                .precipitation(BigDecimal.valueOf(Math.round(precipitation)))
                .windSpeed(BigDecimal.valueOf(Math.round(1 + Math.random() * 6)))
                .windDirection(windDirections[(int)(Math.random() * windDirections.length)])
                .humidity((int) Math.round(baseHumidity + Math.random() * 20 - 10))
                .build());
        }
        
        return HourlyForecastResponse.builder()
            .location(location)
            .hourlyData(hourlyData)
            .build();
    }
    
    private HourlyForecastResponse generateMockHourlyForecast(String location) {
        List<HourlyForecastResponse.HourlyData> hourlyData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        String[] conditions = {"晴", "晴", "多云", "多云", "阴", "小雨", "中雨", "大雨", "暴雨", "晴", "多云", "雷阵雨"};
        String[] windDirections = {"东南风", "南风", "西南风", "东风", "东北风", "北风", "西北风", "西风"};
        
        double extremeWeatherChance = Math.random();
        int baseTempMin, baseTempMax;
        double precipMultiplier;
        
        if (extremeWeatherChance < 0.15) {
            baseTempMin = 35;
            baseTempMax = 42;
            precipMultiplier = 0.3;
        } else if (extremeWeatherChance < 0.25) {
            baseTempMin = -15;
            baseTempMax = 5;
            precipMultiplier = 0.5;
        } else if (extremeWeatherChance < 0.40) {
            baseTempMin = 20;
            baseTempMax = 32;
            precipMultiplier = 3.0;
        } else {
            baseTempMin = 18;
            baseTempMax = 30;
            precipMultiplier = 1.0;
        }
        
        for (int i = 0; i < 72; i++) {
            LocalDateTime hourTime = now.plusHours(i);
            
            double dailyCycle = Math.sin((i % 24 - 6) * Math.PI / 12);
            double tempRange = baseTempMax - baseTempMin;
            double temp = baseTempMin + tempRange * (0.5 + 0.3 * dailyCycle) + (Math.random() * 4 - 2);
            
            int conditionIndex;
            if (precipMultiplier > 2.0) {
                conditionIndex = 5 + (int)(Math.random() * 5);
            } else if (precipMultiplier < 0.5) {
                conditionIndex = (int)(Math.random() * 4);
            } else {
                conditionIndex = (i / 8) % conditions.length;
            }
            String condition = conditions[conditionIndex];
            
            int precipitation = 0;
            if (condition.contains("暴雨")) {
                precipitation = (int) Math.round(30 + Math.random() * 40);
            } else if (condition.contains("大雨")) {
                precipitation = (int) Math.round(15 + Math.random() * 15);
            } else if (condition.contains("中雨")) {
                precipitation = (int) Math.round(5 + Math.random() * 10);
            } else if (condition.contains("雨") || condition.contains("雷阵雨")) {
                precipitation = (int) Math.round(0.5 + Math.random() * 5);
            }
            precipitation = (int) Math.round(precipitation * precipMultiplier);
            
            // 统一气象数据类型为整数型
            hourlyData.add(HourlyForecastResponse.HourlyData.builder()
                .time(hourTime)
                .temperature(BigDecimal.valueOf((int) Math.round(temp)))
                .weatherCondition(condition)
                .precipitation(BigDecimal.valueOf(precipitation))
                .windSpeed(BigDecimal.valueOf((int) Math.round(1 + Math.random() * 6)))
                .windDirection(windDirections[(int)(Math.random() * windDirections.length)])
                .humidity((int) Math.round(40 + Math.random() * 40))
                .build());
        }
        
        return HourlyForecastResponse.builder()
            .location(location)
            .hourlyData(hourlyData)
            .build();
    }
    
    @Override
    public WeeklyForecastResponse getWeeklyForecast(String location) {
        String cacheKey = CacheConstants.WEATHER_WEEKLY + location;
        WeeklyForecastResponse cached = redisUtil.get(cacheKey, WeeklyForecastResponse.class);
        if (cached != null) {
            return cached;
        }
        
        List<WeeklyForecastResponse.DailyData> dailyData = new ArrayList<>();
        String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            dailyData.add(WeeklyForecastResponse.DailyData.builder()
                .date(date)
                .weekday(weekdays[date.getDayOfWeek().getValue() % 7])
                .maxTemp(BigDecimal.valueOf(25 + Math.random() * 10))
                .minTemp(BigDecimal.valueOf(15 + Math.random() * 10))
                .weatherCondition("晴")
                .weatherIcon("sunny")
                .precipitation(BigDecimal.ZERO)
                .windSpeed(BigDecimal.valueOf(3))
                .windDirection("东南风")
                .uvIndex(5)
                .humidity(60)
                .build());
        }
        
        WeeklyForecastResponse response = WeeklyForecastResponse.builder()
            .location(location)
            .dailyData(dailyData)
            .build();
        
        redisUtil.set(cacheKey, response, CacheConstants.WEATHER_CACHE_TTL);
        return response;
    }
    
    private WeeklyForecastResponse getExtendedDailyForecast(String location, String timeRange) {
        int days = "7d".equals(timeRange) ? 7 : 30;
        String cacheKey = CacheConstants.WEATHER_WEEKLY + location + ":" + timeRange;
        
        WeeklyForecastResponse cached = redisUtil.get(cacheKey, WeeklyForecastResponse.class);
        if (cached != null) {
            return cached;
        }
        
        List<WeeklyForecastResponse.DailyData> dailyData = new ArrayList<>();
        String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        String[] conditions = {"晴", "多云", "阴", "小雨", "中雨", "大雨", "暴雨", "雷阵雨", "晴", "多云"};
        
        double extremeWeatherChance = Math.random();
        double baseTempMin, baseTempMax, precipMultiplier;
        
        if (extremeWeatherChance < 0.15) {
            baseTempMin = 28;
            baseTempMax = 42;
            precipMultiplier = 0.3;
        } else if (extremeWeatherChance < 0.25) {
            baseTempMin = -15;
            baseTempMax = 8;
            precipMultiplier = 0.5;
        } else if (extremeWeatherChance < 0.40) {
            baseTempMin = 18;
            baseTempMax = 30;
            precipMultiplier = 3.0;
        } else {
            baseTempMin = 15;
            baseTempMax = 32;
            precipMultiplier = 1.0;
        }
        
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            
            String condition;
            if (precipMultiplier > 2.0) {
                String[] rainConditions = {"小雨", "中雨", "大雨", "暴雨", "雷阵雨"};
                condition = rainConditions[(int)(Math.random() * rainConditions.length)];
            } else if (precipMultiplier < 0.5) {
                String[] clearConditions = {"晴", "晴", "多云", "晴"};
                condition = clearConditions[(int)(Math.random() * clearConditions.length)];
            } else {
                condition = conditions[(i + (int)(Math.random() * 3)) % conditions.length];
            }
            
            double maxTempBase = baseTempMin + (baseTempMax - baseTempMin) * (0.6 + 0.4 * Math.sin(i * Math.PI / 7));
            double minTempBase = baseTempMin + (baseTempMax - baseTempMin) * (0.2 + 0.3 * Math.sin(i * Math.PI / 7));
            
            double precip = 0;
            if (condition.contains("暴雨")) {
                precip = 50 + Math.random() * 50;
            } else if (condition.contains("大雨")) {
                precip = 25 + Math.random() * 25;
            } else if (condition.contains("中雨")) {
                precip = 10 + Math.random() * 15;
            } else if (condition.contains("雨") || condition.contains("雷阵雨")) {
                precip = 2 + Math.random() * 8;
            }
            precip *= precipMultiplier;
            
            dailyData.add(WeeklyForecastResponse.DailyData.builder()
                .date(date)
                .weekday(weekdays[date.getDayOfWeek().getValue() % 7])
                .maxTemp(BigDecimal.valueOf(Math.round((maxTempBase + Math.random() * 5) * 10) / 10.0))
                .minTemp(BigDecimal.valueOf(Math.round((minTempBase + Math.random() * 3) * 10) / 10.0))
                .weatherCondition(condition)
                .weatherIcon(getWeatherIcon(condition))
                .precipitation(BigDecimal.valueOf(Math.round(precip * 10) / 10.0))
                .windSpeed(BigDecimal.valueOf(2 + Math.random() * 5))
                .windDirection(new String[]{"东南风", "南风", "东风", "东北风"}[(int)(Math.random() * 4)])
                .uvIndex(3 + (int)(Math.random() * 6))
                .humidity(50 + (int)(Math.random() * 30))
                .build());
        }
        
        WeeklyForecastResponse response = WeeklyForecastResponse.builder()
            .location(location)
            .dailyData(dailyData)
            .build();
        
        redisUtil.set(cacheKey, response, CacheConstants.WEATHER_CACHE_TTL);
        return response;
    }
    
    private String getWeatherIcon(String condition) {
        if (condition.contains("雨")) return "rainy";
        if (condition.contains("云")) return "cloudy";
        if (condition.contains("阴")) return "overcast";
        return "sunny";
    }
    
    private WeatherAnalysisResponse generateAnalysisFromDailyData(String location, String timeRange, WeeklyForecastResponse dailyData) {
        List<WeeklyForecastResponse.DailyData> data = dailyData.getDailyData();
        
        if (data == null || data.isEmpty()) {
            return WeatherAnalysisResponse.builder()
                .location(location)
                .timeRange(timeRange)
                .temperatureAnalysis(WeatherAnalysisResponse.TemperatureAnalysis.builder()
                    .avgTemp(BigDecimal.valueOf(20))
                    .maxTemp(BigDecimal.valueOf(25))
                    .minTemp(BigDecimal.valueOf(15))
                    .trend("稳定")
                    .description("暂无温度数据")
                    .trendData(new ArrayList<>())
                    .build())
                .precipitationAnalysis(WeatherAnalysisResponse.PrecipitationAnalysis.builder()
                    .avgPrecipitationProb(0)
                    .totalPrecipitation(BigDecimal.ZERO)
                    .precipitationPeriod("无")
                    .description("暂无降水数据")
                    .trendData(new ArrayList<>())
                    .build())
                .build();
        }
        
        List<BigDecimal> maxTemps = data.stream()
            .map(WeeklyForecastResponse.DailyData::getMaxTemp)
            .filter(t -> t != null)
            .collect(Collectors.toList());
        
        List<BigDecimal> minTemps = data.stream()
            .map(WeeklyForecastResponse.DailyData::getMinTemp)
            .filter(t -> t != null)
            .collect(Collectors.toList());
        
        List<BigDecimal> avgTemps = new ArrayList<>();
        for (int i = 0; i < maxTemps.size() && i < minTemps.size(); i++) {
            BigDecimal avg = maxTemps.get(i).add(minTemps.get(i)).divide(BigDecimal.valueOf(2), 1, BigDecimal.ROUND_HALF_UP);
            avgTemps.add(avg);
        }
        
        BigDecimal overallAvgTemp = avgTemps.isEmpty() ? BigDecimal.ZERO :
            avgTemps.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(avgTemps.size()), 1, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal overallMaxTemp = maxTemps.isEmpty() ? BigDecimal.ZERO :
            maxTemps.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        
        BigDecimal overallMinTemp = minTemps.isEmpty() ? BigDecimal.ZERO :
            minTemps.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        
        String trend = determineTemperatureTrend(avgTemps);
        String description = generateTemperatureDescription(overallAvgTemp, overallMaxTemp, overallMinTemp, trend);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        List<WeatherAnalysisResponse.TrendPoint> tempTrendData = new ArrayList<>();
        
        int step = data.size() <= 7 ? 1 : 3;
        for (int i = 0; i < data.size(); i += step) {
            WeeklyForecastResponse.DailyData item = data.get(i);
            BigDecimal avgT = item.getMaxTemp().add(item.getMinTemp()).divide(BigDecimal.valueOf(2), 1, BigDecimal.ROUND_HALF_UP);
            tempTrendData.add(WeatherAnalysisResponse.TrendPoint.builder()
                .time(item.getDate().format(formatter))
                .value(avgT)
                .build());
        }
        
        WeatherAnalysisResponse.TemperatureAnalysis tempAnalysis = WeatherAnalysisResponse.TemperatureAnalysis.builder()
            .trendData(tempTrendData)
            .avgTemp(overallAvgTemp)
            .maxTemp(overallMaxTemp)
            .minTemp(overallMinTemp)
            .trend(trend)
            .description(description)
            .build();
        
        List<BigDecimal> precipitations = data.stream()
            .map(WeeklyForecastResponse.DailyData::getPrecipitation)
            .filter(p -> p != null)
            .collect(Collectors.toList());
        
        BigDecimal totalPrecipitation = precipitations.isEmpty() ? BigDecimal.ZERO :
            precipitations.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int rainyDays = (int) precipitations.stream()
            .filter(p -> p.compareTo(BigDecimal.ZERO) > 0)
            .count();
        int avgPrecipProb = data.size() > 0 ? (rainyDays * 100 / data.size()) : 0;
        
        String precipPeriod = determinePrecipitationPeriod(data);
        String precipDesc = generatePrecipitationDescription(totalPrecipitation, avgPrecipProb, precipPeriod);
        
        List<WeatherAnalysisResponse.TrendPoint> precipTrendData = new ArrayList<>();
        for (int i = 0; i < data.size(); i += step) {
            WeeklyForecastResponse.DailyData item = data.get(i);
            precipTrendData.add(WeatherAnalysisResponse.TrendPoint.builder()
                .time(item.getDate().format(formatter))
                .value(item.getPrecipitation() != null ? item.getPrecipitation() : BigDecimal.ZERO)
                .build());
        }
        
        WeatherAnalysisResponse.PrecipitationAnalysis precipAnalysis = WeatherAnalysisResponse.PrecipitationAnalysis.builder()
            .trendData(precipTrendData)
            .avgPrecipitationProb(avgPrecipProb)
            .totalPrecipitation(totalPrecipitation)
            .precipitationPeriod(precipPeriod)
            .description(precipDesc)
            .build();
        
        return WeatherAnalysisResponse.builder()
            .location(location)
            .timeRange(timeRange)
            .temperatureAnalysis(tempAnalysis)
            .precipitationAnalysis(precipAnalysis)
            .build();
    }
    
    private String determinePrecipitationPeriod(List<WeeklyForecastResponse.DailyData> data) {
        if (data == null || data.isEmpty()) return "无";
        
        int firstRainDay = -1;
        int lastRainDay = -1;
        
        for (int i = 0; i < data.size(); i++) {
            BigDecimal precip = data.get(i).getPrecipitation();
            if (precip != null && precip.compareTo(BigDecimal.ZERO) > 0) {
                if (firstRainDay == -1) firstRainDay = i;
                lastRainDay = i;
            }
        }
        
        if (firstRainDay == -1) return "无";
        
        if (firstRainDay == 0) {
            return lastRainDay <= 2 ? "未来1-2天" : "未来一周内";
        } else if (firstRainDay <= 3) {
            return "未来3天内";
        } else if (firstRainDay <= 7) {
            return "一周后";
        } else {
            return "两周后";
        }
    }
    
    private String generatePrecipitationDescription(BigDecimal total, int avgProb, String period) {
        StringBuilder desc = new StringBuilder();
        
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            desc.append("预计无降水，天气晴好");
        } else if (total.compareTo(BigDecimal.valueOf(10)) < 0) {
            desc.append("有少量降水，总量").append(total.setScale(1, BigDecimal.ROUND_HALF_UP)).append("mm");
        } else if (total.compareTo(BigDecimal.valueOf(50)) < 0) {
            desc.append("有中等降水，总量").append(total.setScale(1, BigDecimal.ROUND_HALF_UP)).append("mm");
        } else {
            desc.append("降水较多，总量").append(total.setScale(1, BigDecimal.ROUND_HALF_UP)).append("mm，注意防雨");
        }
        
        if (!period.equals("无")) {
            desc.append("，降水时段：").append(period);
        }
        
        return desc.toString();
    }
    
    @Override
    public LifestyleResponse getLifestyleIndices(String location) {
        String cacheKey = CacheConstants.LIFESTYLE + location;
        LifestyleResponse cached = redisUtil.get(cacheKey, LifestyleResponse.class);
        if (cached != null) {
            return cached;
        }
        
        LifestyleResponse response = LifestyleResponse.builder()
            .location(location)
            .comfort(LifestyleResponse.LifestyleIndex.builder()
                .name("舒适度")
                .level("较舒适")
                .description("白天天气晴好，您在这种天气条件下，会感觉早晚凉爽、舒适")
                .icon("comfort")
                .advice("建议穿着轻薄透气的衣物")
                .build())
            .dressing(LifestyleResponse.LifestyleIndex.builder()
                .name("穿衣")
                .level("薄款夏装")
                .description("天气较热，建议穿着轻薄透气的夏装")
                .icon("clothing")
                .advice("建议穿短袖、短裤等夏季服装")
                .build())
            .uv(LifestyleResponse.LifestyleIndex.builder()
                .name("紫外线")
                .level("中等")
                .description("紫外线强度中等，建议涂抹防晒霜")
                .icon("uv")
                .advice("外出时建议涂抹SPF15以上的防晒霜")
                .build())
            .carWashing(LifestyleResponse.LifestyleIndex.builder()
                .name("洗车")
                .level("适宜")
                .description("未来两天无雨，适合洗车")
                .icon("car")
                .advice("今天是个洗车的好日子")
                .build())
            .travel(LifestyleResponse.LifestyleIndex.builder()
                .name("旅游")
                .level("很适宜")
                .description("天气较好，非常适合出游")
                .icon("travel")
                .advice("建议安排户外活动")
                .build())
            .sport(LifestyleResponse.LifestyleIndex.builder()
                .name("运动")
                .level("较适宜")
                .description("天气较好，适合户外运动")
                .icon("sport")
                .advice("建议进行慢跑、骑行等户外运动")
                .build())
            .airQuality(LifestyleResponse.LifestyleIndex.builder()
                .name("空气质量")
                .level("良")
                .description("空气质量良好，适合户外活动")
                .icon("air")
                .advice("空气质量可接受，但敏感人群应减少户外活动")
                .build())
            .build();
        
        redisUtil.set(cacheKey, response, CacheConstants.WEATHER_CACHE_TTL);
        return response;
    }
    
    @Override
    public WeatherResponse getAirQuality(String location) {
        return getCurrentWeather(location);
    }
    
    @Override
    public WeatherResponse refreshWeatherData(String location) {
        String cacheKey = CacheConstants.WEATHER_CURRENT + location;
        redisUtil.delete(cacheKey);
        redisUtil.delete(CacheConstants.WEATHER_HOURLY + location);
        redisUtil.delete(CacheConstants.WEATHER_WEEKLY + location);
        
        WeatherData weatherData = fetchWeatherFromAPI(location);
        return convertToResponse(weatherData);
    }
    
    private WeatherData fetchWeatherFromAPI(String location) {
        // ========== 原和风天气API调用代码（已注释保留） ==========
        // try {
        //     log.info("开始获取天气数据，位置: {}, API密钥: {}", location, apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null");
        //     
        //     if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_qweather_api_key_here")) {
        //         log.warn("和风天气API密钥未配置，使用模拟数据");
        //         return generateMockWeatherData(location);
        //     }
        //     
        //     String locationId = getLocationId(location);
        //     log.info("调用和风天气API，位置ID: {}, URL: {}", locationId, baseUrl);
        //     
        //     String weatherUrl = baseUrl + "/weather/now?location=" + locationId + "&key=" + apiKey;
        //     String weatherResponseStr = webClient.get()
        //         .uri(weatherUrl)
        //         .retrieve()
        //         .bodyToMono(String.class)
        //         .block();
        //     
        //     log.info("和风天气API响应: {}", weatherResponseStr != null ? weatherResponseStr.substring(0, Math.min(200, weatherResponseStr.length())) : "null");
        //     
        //     Map<String, Object> weatherResponse = objectMapper.readValue(weatherResponseStr, Map.class);
        //     
        //     if (weatherResponse == null || !"200".equals(String.valueOf(weatherResponse.get("code")))) {
        //         log.warn("和风天气API调用失败，使用模拟数据。响应: {}", weatherResponse);
        //         return generateMockWeatherData(location);
        //     }
        //     
        //     Map<String, Object> nowData = (Map<String, Object>) weatherResponse.get("now");
        //     
        //     AirQualityData airQualityData = fetchAirQualityData(location);
        //     
        //     WeatherData weatherData = WeatherData.builder()
        //         .location(location)
        //         .temperature(new BigDecimal(String.valueOf(nowData.get("temp"))))
        //         .feelsLike(new BigDecimal(String.valueOf(nowData.get("feelsLike"))))
        //         .humidity(new BigDecimal(String.valueOf(nowData.get("humidity"))))
        //         .windSpeed(new BigDecimal(String.valueOf(nowData.get("windSpeed"))))
        //         .windDirection(String.valueOf(nowData.get("windDir")))
        //         .windScale(String.valueOf(nowData.get("windScale")))
        //         .weatherCondition(String.valueOf(nowData.get("text")))
        //         .weatherDescription(getWeatherDescription(String.valueOf(nowData.get("text"))))
        //         .airQualityIndex(airQualityData.aqi)
        //         .airQualityLevel(airQualityData.category)
        //         .pm25(airQualityData.pm25)
        //         .pm10(airQualityData.pm10)
        //         .uvIndex(5)
        //         .visibility(new BigDecimal(String.valueOf(nowData.get("vis"))))
        //         .pressure(Integer.parseInt(String.valueOf(nowData.get("pressure"))))
        //         .precipitation(new BigDecimal(String.valueOf(nowData.get("precip"))))
        //         .recordTime(LocalDateTime.now())
        //         .dataSource("qweather")
        //         .build();
        //     
        //     return weatherDataRepository.save(weatherData);
        // } catch (Exception e) {
        //     log.error("获取天气数据失败: {}", e.getMessage(), e);
        //     return generateMockWeatherData(location);
        // }
        // ========== 原和风天气API调用代码结束 ==========
        
        // 替代实现：从数据库获取数据，若数据库无数据则生成模拟数据
        log.info("从数据库获取天气数据，位置: {}", location);
        
        Optional<WeatherData> dbData = weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc(location);
        if (dbData.isPresent()) {
            log.info("从数据库成功获取天气数据: {}", location);
            return dbData.get();
        }
        
        log.info("数据库中无 {} 的天气数据，生成模拟数据", location);
        return generateMockWeatherData(location);
    }
    
    private AirQualityData fetchAirQualityData(String location) {
        // ========== 原和风天气API调用代码（已注释保留） ==========
        // try {
        //     CityCoordinate cityCoord = CityCoordinate.getCoordinate(location);
        //     
        //     if (cityCoord == null) {
        //         log.warn("未找到城市 {} 的坐标信息，使用默认空气质量数据", location);
        //         return getDefaultAirQualityData();
        //     }
        //     
        //     String airBaseUrl = baseUrl.replace("/v7", "");
        //     String airUrl = String.format("%s/airquality/v1/current/%.2f/%.2f?key=%s", 
        //         airBaseUrl, cityCoord.getLatitude(), cityCoord.getLongitude(), apiKey);
        //     
        //     log.info("调用空气质量API: {}", airUrl);
        //     
        //     String airResponseStr = webClient.get()
        //         .uri(airUrl)
        //         .retrieve()
        //         .bodyToMono(String.class)
        //         .block();
        //     
        //     log.info("空气质量API响应: {}", airResponseStr != null ? airResponseStr.substring(0, Math.min(300, airResponseStr.length())) : "null");
        //     
        //     AirQualityResponse airResponse = objectMapper.readValue(airResponseStr, AirQualityResponse.class);
        //     
        //     if (airResponse == null || airResponse.getIndexes() == null || airResponse.getIndexes().isEmpty()) {
        //         log.warn("空气质量API返回数据为空，使用默认值");
        //         return getDefaultAirQualityData();
        //     }
        //     
        //     Optional<AirQualityIndex> cnMeeIndex = airResponse.getIndexes().stream()
        //         .filter(idx -> "cn-mee".equals(idx.getCode()))
        //         .findFirst();
        //     
        //     Optional<AirQualityIndex> usEpaIndex = airResponse.getIndexes().stream()
        //         .filter(idx -> "us-epa".equals(idx.getCode()))
        //         .findFirst();
        //     
        //     AirQualityIndex selectedIndex = cnMeeIndex.orElse(usEpaIndex.orElse(null));
        //     
        //     Integer aqi = selectedIndex != null ? selectedIndex.getAqi() : 50;
        //     String category = selectedIndex != null ? selectedIndex.getCategory() : "良";
        //     
        //     Integer pm25 = null;
        //     Integer pm10 = null;
        //     
        //     if (airResponse.getPollutants() != null) {
        //         for (Pollutant pollutant : airResponse.getPollutants()) {
        //             if ("pm2p5".equals(pollutant.getCode()) && pollutant.getConcentration() != null) {
        //                 pm25 = pollutant.getConcentration().getValue().intValue();
        //             } else if ("pm10".equals(pollutant.getCode()) && pollutant.getConcentration() != null) {
        //                 pm10 = pollutant.getConcentration().getValue().intValue();
        //             }
        //         }
        //     }
        //     
        //     log.info("成功获取空气质量数据 - AQI: {}, 等级: {}, PM2.5: {}, PM10: {}", aqi, category, pm25, pm10);
        //     
        //     return new AirQualityData(
        //         aqi != null ? aqi : 50,
        //         category != null ? category : "良",
        //         pm25 != null ? pm25 : 20,
        //         pm10 != null ? pm10 : 30
        //     );
        //     
        // } catch (Exception e) {
        //     log.warn("获取空气质量数据失败: {}, 使用默认值", e.getMessage());
        //     return getDefaultAirQualityData();
        // }
        // ========== 原和风天气API调用代码结束 ==========
        
        // 替代实现：从数据库获取空气质量数据，若无数据则返回默认值
        log.info("从数据库获取空气质量数据，位置: {}", location);
        
        Optional<WeatherData> dbData = weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc(location);
        if (dbData.isPresent()) {
            WeatherData data = dbData.get();
            if (data.getAirQualityIndex() != null) {
                log.info("从数据库获取到空气质量数据 - AQI: {}", data.getAirQualityIndex());
                return new AirQualityData(
                    data.getAirQualityIndex(),
                    data.getAirQualityLevel() != null ? data.getAirQualityLevel() : "良",
                    data.getPm25() != null ? data.getPm25() : 20,
                    data.getPm10() != null ? data.getPm10() : 30
                );
            }
        }
        
        log.info("数据库中无 {} 的空气质量数据，使用默认值", location);
        return getDefaultAirQualityData();
    }
    
    private AirQualityData getDefaultAirQualityData() {
        return new AirQualityData(50, "良", 20, 30);
    }
    
    private static class AirQualityData {
        final Integer aqi;
        final String category;
        final Integer pm25;
        final Integer pm10;
        
        AirQualityData(Integer aqi, String category, Integer pm25, Integer pm10) {
            this.aqi = aqi;
            this.category = category;
            this.pm25 = pm25;
            this.pm10 = pm10;
        }
    }
    
    private WeatherData generateMockWeatherData(String location) {
        // 统一气象数据类型为整数型
        int temperature = (int) Math.round(25 + Math.random() * 10);
        int feelsLike = (int) Math.round(26 + Math.random() * 10);
        int humidity = (int) Math.round(50 + Math.random() * 30);
        int windSpeed = (int) Math.round(1 + Math.random() * 5);
        int visibility = (int) Math.round(10 + Math.random() * 10);
        int precipitation = 0;
        int pressure = 1013 + (int)(Math.random() * 10);
        
        WeatherData weatherData = WeatherData.builder()
            .location(location)
            .temperature(BigDecimal.valueOf(temperature))
            .feelsLike(BigDecimal.valueOf(feelsLike))
            .humidity(BigDecimal.valueOf(humidity))
            .windSpeed(BigDecimal.valueOf(windSpeed))
            .windDirection("东南风")
            .windScale("3级")
            .weatherCondition("晴")
            .weatherDescription("天气晴朗，适合户外活动")
            .airQualityIndex(50 + (int)(Math.random() * 50))
            .airQualityLevel("良")
            .pm25(20 + (int)(Math.random() * 30))
            .pm10(30 + (int)(Math.random() * 40))
            .uvIndex(5 + (int)(Math.random() * 5))
            .visibility(BigDecimal.valueOf(visibility))
            .pressure(pressure)
            .precipitation(BigDecimal.valueOf(precipitation))
            .recordTime(LocalDateTime.now())
            .dataSource("mock")
            .build();
        
        return weatherDataRepository.save(weatherData);
    }
    
    private String getLocationId(String location) {
        return switch (location) {
            case "北京" -> "101010100";
            case "上海" -> "101020100";
            case "广州" -> "101280101";
            case "深圳" -> "101280601";
            case "杭州" -> "101210101";
            case "成都" -> "101270101";
            case "武汉" -> "101200101";
            case "西安" -> "101110101";
            case "南京" -> "101190101";
            case "重庆" -> "101040100";
            default -> location;
        };
    }
    
    private String getWeatherDescription(String condition) {
        return switch (condition) {
            case "晴" -> "天气晴朗，适合户外活动";
            case "多云" -> "云层较多，温度适宜";
            case "阴" -> "天空阴沉，可能有降雨";
            case "小雨" -> "小雨绵绵，记得带伞";
            case "中雨" -> "中雨，建议减少外出";
            case "大雨" -> "大雨倾盆，请注意安全";
            case "暴雨" -> "暴雨天气，请勿外出";
            case "雷阵雨" -> "雷雨交加，注意防雷";
            case "小雪" -> "小雪纷飞，注意保暖";
            case "中雪" -> "中雪，路面湿滑";
            case "大雪" -> "大雪，请减少外出";
            case "雾" -> "雾气弥漫，能见度低";
            case "霾" -> "霾天气，空气质量差";
            default -> "天气状况：" + condition;
        };
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
    
    @Override
    public WeatherAnalysisResponse getWeatherAnalysis(String location, String timeRange) {
        String cacheKey = CacheConstants.WEATHER_ANALYSIS + location + ":" + timeRange;
        WeatherAnalysisResponse cached = redisUtil.get(cacheKey, WeatherAnalysisResponse.class);
        if (cached != null) {
            return cached;
        }
        
        WeatherAnalysisResponse response;
        
        if ("24h".equals(timeRange)) {
            HourlyForecastResponse hourlyData = getHourlyForecast(location);
            response = generateAnalysisFromHourlyData(location, timeRange, hourlyData);
        } else {
            WeeklyForecastResponse dailyData = getExtendedDailyForecast(location, timeRange);
            response = generateAnalysisFromDailyData(location, timeRange, dailyData);
        }
        
        redisUtil.set(cacheKey, response, CacheConstants.WEATHER_CACHE_TTL);
        return response;
    }
    
    private WeatherAnalysisResponse generateAnalysisFromHourlyData(String location, String timeRange, HourlyForecastResponse hourlyData) {
        int hours = getTimeRangeHours(timeRange);
        
        List<HourlyForecastResponse.HourlyData> relevantData = hourlyData.getHourlyData()
            .stream()
            .limit(hours)
            .collect(Collectors.toList());
        
        WeatherAnalysisResponse.TemperatureAnalysis tempAnalysis = analyzeTemperature(relevantData, hours);
        WeatherAnalysisResponse.PrecipitationAnalysis precipAnalysis = analyzePrecipitation(relevantData, hours);
        
        return WeatherAnalysisResponse.builder()
            .location(location)
            .timeRange(timeRange)
            .temperatureAnalysis(tempAnalysis)
            .precipitationAnalysis(precipAnalysis)
            .build();
    }
    
    private int getTimeRangeHours(String timeRange) {
        return switch (timeRange) {
            case "24h" -> 24;
            case "7d" -> 168;
            case "30d" -> 720;
            default -> 24;
        };
    }
    
    private WeatherAnalysisResponse.TemperatureAnalysis analyzeTemperature(List<HourlyForecastResponse.HourlyData> data, int hours) {
        if (data == null || data.isEmpty()) {
            return WeatherAnalysisResponse.TemperatureAnalysis.builder()
                .avgTemp(BigDecimal.valueOf(20))
                .maxTemp(BigDecimal.valueOf(25))
                .minTemp(BigDecimal.valueOf(15))
                .trend("稳定")
                .description("暂无温度数据")
                .trendData(new ArrayList<>())
                .build();
        }
        
        List<BigDecimal> temps = data.stream()
            .map(HourlyForecastResponse.HourlyData::getTemperature)
            .filter(t -> t != null)
            .collect(Collectors.toList());
        
        BigDecimal avgTemp = temps.isEmpty() ? BigDecimal.ZERO :
            temps.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(temps.size()), 1, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal maxTemp = temps.isEmpty() ? BigDecimal.ZERO :
            temps.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        
        BigDecimal minTemp = temps.isEmpty() ? BigDecimal.ZERO :
            temps.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        
        String trend = determineTemperatureTrend(temps);
        String description = generateTemperatureDescription(avgTemp, maxTemp, minTemp, trend);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<WeatherAnalysisResponse.TrendPoint> trendData = new ArrayList<>();
        
        int step = hours <= 24 ? 4 : (hours <= 168 ? 24 : 72);
        for (int i = 0; i < data.size(); i += step) {
            HourlyForecastResponse.HourlyData item = data.get(i);
            trendData.add(WeatherAnalysisResponse.TrendPoint.builder()
                .time(item.getTime().format(formatter))
                .value(item.getTemperature())
                .build());
        }
        
        return WeatherAnalysisResponse.TemperatureAnalysis.builder()
            .trendData(trendData)
            .avgTemp(avgTemp)
            .maxTemp(maxTemp)
            .minTemp(minTemp)
            .trend(trend)
            .description(description)
            .build();
    }
    
    private String determineTemperatureTrend(List<BigDecimal> temps) {
        if (temps.size() < 2) return "稳定";
        
        int risingCount = 0;
        int fallingCount = 0;
        
        for (int i = 1; i < temps.size(); i++) {
            int cmp = temps.get(i).compareTo(temps.get(i - 1));
            if (cmp > 0) risingCount++;
            else if (cmp < 0) fallingCount++;
        }
        
        if (risingCount > fallingCount * 1.5) return "上升";
        if (fallingCount > risingCount * 1.5) return "下降";
        return "稳定";
    }
    
    private String generateTemperatureDescription(BigDecimal avgTemp, BigDecimal maxTemp, BigDecimal minTemp, String trend) {
        StringBuilder desc = new StringBuilder();
        
        if (avgTemp.compareTo(BigDecimal.valueOf(30)) > 0) {
            desc.append("气温较高，注意防暑降温");
        } else if (avgTemp.compareTo(BigDecimal.valueOf(20)) > 0) {
            desc.append("气温适宜，适合户外活动");
        } else if (avgTemp.compareTo(BigDecimal.valueOf(10)) > 0) {
            desc.append("气温偏低，注意保暖");
        } else {
            desc.append("气温较低，请做好防寒措施");
        }
        
        BigDecimal diff = maxTemp.subtract(minTemp);
        if (diff.compareTo(BigDecimal.valueOf(10)) > 0) {
            desc.append("，昼夜温差较大");
        }
        
        desc.append("，趋势").append(trend);
        
        return desc.toString();
    }
    
    private WeatherAnalysisResponse.PrecipitationAnalysis analyzePrecipitation(List<HourlyForecastResponse.HourlyData> data, int hours) {
        if (data == null || data.isEmpty()) {
            return WeatherAnalysisResponse.PrecipitationAnalysis.builder()
                .avgPrecipitationProb(0)
                .totalPrecipitation(BigDecimal.ZERO)
                .precipitationPeriod("无")
                .description("暂无降水数据")
                .trendData(new ArrayList<>())
                .build();
        }
        
        List<BigDecimal> precips = data.stream()
            .map(HourlyForecastResponse.HourlyData::getPrecipitation)
            .filter(p -> p != null)
            .collect(Collectors.toList());
        
        BigDecimal totalPrecip = precips.isEmpty() ? BigDecimal.ZERO :
            precips.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int avgPrecipProb = calculatePrecipitationProbability(data);
        
        String precipPeriod = determineHourlyPrecipitationPeriod(data);
        String description = generateHourlyPrecipitationDescription(totalPrecip, avgPrecipProb, precipPeriod);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<WeatherAnalysisResponse.TrendPoint> trendData = new ArrayList<>();
        
        int step = hours <= 24 ? 4 : (hours <= 168 ? 24 : 72);
        for (int i = 0; i < data.size(); i += step) {
            HourlyForecastResponse.HourlyData item = data.get(i);
            trendData.add(WeatherAnalysisResponse.TrendPoint.builder()
                .time(item.getTime().format(formatter))
                .value(item.getPrecipitation())
                .build());
        }
        
        return WeatherAnalysisResponse.PrecipitationAnalysis.builder()
            .trendData(trendData)
            .avgPrecipitationProb(avgPrecipProb)
            .totalPrecipitation(totalPrecip.setScale(1, BigDecimal.ROUND_HALF_UP))
            .precipitationPeriod(precipPeriod)
            .description(description)
            .build();
    }
    
    private int calculatePrecipitationProbability(List<HourlyForecastResponse.HourlyData> data) {
        if (data.isEmpty()) return 0;
        
        long rainyHours = data.stream()
            .filter(d -> d.getPrecipitation() != null && d.getPrecipitation().compareTo(BigDecimal.ZERO) > 0)
            .count();
        
        return (int) ((rainyHours * 100) / data.size());
    }
    
    private String determineHourlyPrecipitationPeriod(List<HourlyForecastResponse.HourlyData> data) {
        List<HourlyForecastResponse.HourlyData> rainyHours = data.stream()
            .filter(d -> d.getPrecipitation() != null && d.getPrecipitation().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());
        
        if (rainyHours.isEmpty()) {
            return "无";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        LocalDateTime startTime = rainyHours.get(0).getTime();
        LocalDateTime endTime = rainyHours.get(rainyHours.size() - 1).getTime();
        
        return startTime.format(formatter) + " - " + endTime.format(formatter);
    }
    
    private String generateHourlyPrecipitationDescription(BigDecimal totalPrecip, int avgProb, String period) {
        StringBuilder desc = new StringBuilder();
        
        if (totalPrecip.compareTo(BigDecimal.ZERO) == 0) {
            desc.append("预计无降水");
        } else if (totalPrecip.compareTo(BigDecimal.valueOf(10)) < 0) {
            desc.append("预计有小雨");
        } else if (totalPrecip.compareTo(BigDecimal.valueOf(25)) < 0) {
            desc.append("预计有中雨");
        } else {
            desc.append("预计有大雨");
        }
        
        if (avgProb > 50) {
            desc.append("，降水概率较高");
        } else if (avgProb > 20) {
            desc.append("，降水概率中等");
        } else {
            desc.append("，降水概率较低");
        }
        
        return desc.toString();
    }
    
    @Override
    public BatchWeatherResponse getBatchWeatherData(List<String> locations, Boolean forceRefresh) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始批量查询天气数据，位置数量: {}, 强制刷新: {}", locations.size(), forceRefresh);
        
        Map<String, WeatherResponse> successResults = new HashMap<>();
        Map<String, String> failedResults = new HashMap<>();
        List<String> cachedLocations = new ArrayList<>();
        
        List<CompletableFuture<Void>> futures = locations.stream()
            .distinct()
            .map(location -> CompletableFuture.runAsync(() -> {
                try {
                    String cacheKey = CacheConstants.WEATHER_CURRENT + location;
                    
                    if (!forceRefresh) {
                        WeatherResponse cached = redisUtil.get(cacheKey, WeatherResponse.class);
                        if (cached != null) {
                            synchronized (successResults) {
                                successResults.put(location, cached);
                                cachedLocations.add(location);
                            }
                            log.debug("从缓存获取天气数据: {}", location);
                            return;
                        }
                    }
                    
                    WeatherData weatherData = weatherDataRepository
                        .findFirstByLocationOrderByRecordTimeDesc(location)
                        .orElseGet(() -> fetchWeatherFromAPI(location));
                    
                    WeatherResponse response = convertToResponse(weatherData);
                    
                    if (!forceRefresh) {
                        redisUtil.set(cacheKey, response, CacheConstants.WEATHER_CACHE_TTL);
                    }
                    
                    synchronized (successResults) {
                        successResults.put(location, response);
                    }
                    
                    log.debug("成功获取天气数据: {}", location);
                    
                } catch (Exception e) {
                    log.error("获取天气数据失败: {}, 错误: {}", location, e.getMessage());
                    synchronized (failedResults) {
                        failedResults.put(location, "获取失败: " + e.getMessage());
                    }
                }
            }))
            .collect(Collectors.toList());
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long queryTimeMs = System.currentTimeMillis() - startTime;
        
        BatchWeatherResponse response = BatchWeatherResponse.builder()
            .totalCount(locations.size())
            .successCount(successResults.size())
            .failedCount(failedResults.size())
            .successResults(successResults)
            .failedResults(failedResults)
            .cachedLocations(cachedLocations)
            .queryTimeMs(queryTimeMs)
            .build();
        
        log.info("批量查询完成，总数: {}, 成功: {}, 失败: {}, 缓存命中: {}, 耗时: {}ms", 
            response.getTotalCount(), response.getSuccessCount(), 
            response.getFailedCount(), response.getCachedLocations().size(), 
            response.getQueryTimeMs());
        
        return response;
    }
}
