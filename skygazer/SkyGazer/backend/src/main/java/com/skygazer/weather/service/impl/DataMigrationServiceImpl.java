package com.skygazer.weather.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.constant.CacheConstants;
import com.skygazer.weather.dto.response.HourlyForecastResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.dto.response.WeeklyForecastResponse;
import com.skygazer.weather.entity.WeatherData;
import com.skygazer.weather.mapper.WeatherDataMapper;
import com.skygazer.weather.service.DataMigrationService;
import com.skygazer.weather.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationServiceImpl implements DataMigrationService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisUtil redisUtil;
    private final WeatherDataMapper weatherDataMapper;
    private final ObjectMapper objectMapper;
    
    private final AtomicInteger totalRecords = new AtomicInteger(0);
    private final AtomicInteger migratedRecords = new AtomicInteger(0);
    private final AtomicInteger failedRecords = new AtomicInteger(0);
    private volatile boolean migrationCompleted = false;
    
    @Override
    @Transactional
    public void migrateRedisToDatabase() {
        log.info("========== 开始Redis数据迁移到数据库 ==========");
        
        totalRecords.set(0);
        migratedRecords.set(0);
        failedRecords.set(0);
        migrationCompleted = false;
        
        int weatherCount = migrateWeatherData();
        int hourlyCount = migrateHourlyForecastData();
        int weeklyCount = migrateWeeklyForecastData();
        
        migrationCompleted = true;
        
        log.info("========== 数据迁移完成 ==========");
        log.info("当前天气数据迁移: {} 条", weatherCount);
        log.info("小时预报数据迁移: {} 条", hourlyCount);
        log.info("周预报数据迁移: {} 条", weeklyCount);
        log.info("总计迁移: {} 条", migratedRecords.get());
        if (failedRecords.get() > 0) {
            log.warn("迁移失败: {} 条", failedRecords.get());
        }
    }
    
    @Override
    @Transactional
    public int migrateWeatherData() {
        log.info("开始迁移当前天气数据...");
        
        Set<String> keys = redisTemplate.keys(CacheConstants.WEATHER_CURRENT + "*");
        if (keys == null || keys.isEmpty()) {
            log.info("Redis中没有当前天气数据需要迁移");
            return 0;
        }
        
        totalRecords.addAndGet(keys.size());
        int successCount = 0;
        
        for (String key : keys) {
            try {
                WeatherResponse response = redisUtil.get(key, WeatherResponse.class);
                if (response != null) {
                    WeatherData weatherData = convertToWeatherData(response);
                    weatherDataMapper.insert(weatherData);
                    migratedRecords.incrementAndGet();
                    successCount++;
                    log.debug("成功迁移天气数据: {}", response.getLocation());
                }
            } catch (Exception e) {
                failedRecords.incrementAndGet();
                log.error("迁移天气数据失败: key={}, error={}", key, e.getMessage());
            }
        }
        
        log.info("当前天气数据迁移完成，成功: {} 条", successCount);
        return successCount;
    }
    
    @Override
    @Transactional
    public int migrateHourlyForecastData() {
        log.info("开始迁移小时预报数据...");
        
        Set<String> keys = redisTemplate.keys(CacheConstants.WEATHER_HOURLY + "*");
        if (keys == null || keys.isEmpty()) {
            log.info("Redis中没有小时预报数据需要迁移");
            return 0;
        }
        
        totalRecords.addAndGet(keys.size());
        int successCount = 0;
        
        for (String key : keys) {
            try {
                HourlyForecastResponse response = redisUtil.get(key, HourlyForecastResponse.class);
                if (response != null && response.getHourlyData() != null && !response.getHourlyData().isEmpty()) {
                    String location = response.getLocation();
                    HourlyForecastResponse.HourlyData firstHour = response.getHourlyData().get(0);
                    
                    WeatherData weatherData = WeatherData.builder()
                        .location(location)
                        .temperature(firstHour.getTemperature())
                        .humidity(BigDecimal.valueOf(firstHour.getHumidity()))
                        .windSpeed(firstHour.getWindSpeed())
                        .windDirection(firstHour.getWindDirection())
                        .weatherCondition(firstHour.getWeatherCondition())
                        .precipitation(firstHour.getPrecipitation())
                        .recordTime(LocalDateTime.now())
                        .dataSource("redis_migration_hourly")
                        .build();
                    
                    weatherDataMapper.insert(weatherData);
                    migratedRecords.incrementAndGet();
                    successCount++;
                    log.debug("成功迁移小时预报数据: {}", location);
                }
            } catch (Exception e) {
                failedRecords.incrementAndGet();
                log.error("迁移小时预报数据失败: key={}, error={}", key, e.getMessage());
            }
        }
        
        log.info("小时预报数据迁移完成，成功: {} 条", successCount);
        return successCount;
    }
    
    @Override
    @Transactional
    public int migrateWeeklyForecastData() {
        log.info("开始迁移周预报数据...");
        
        Set<String> keys = redisTemplate.keys(CacheConstants.WEATHER_WEEKLY + "*");
        if (keys == null || keys.isEmpty()) {
            log.info("Redis中没有周预报数据需要迁移");
            return 0;
        }
        
        totalRecords.addAndGet(keys.size());
        int successCount = 0;
        
        for (String key : keys) {
            try {
                WeeklyForecastResponse response = redisUtil.get(key, WeeklyForecastResponse.class);
                if (response != null && response.getDailyData() != null && !response.getDailyData().isEmpty()) {
                    String location = response.getLocation();
                    WeeklyForecastResponse.DailyData firstDay = response.getDailyData().get(0);
                    
                    BigDecimal avgTemp = firstDay.getMaxTemp() != null && firstDay.getMinTemp() != null
                        ? firstDay.getMaxTemp().add(firstDay.getMinTemp()).divide(BigDecimal.valueOf(2), 0, BigDecimal.ROUND_HALF_UP)
                        : BigDecimal.ZERO;
                    
                    WeatherData weatherData = WeatherData.builder()
                        .location(location)
                        .temperature(avgTemp)
                        .humidity(BigDecimal.valueOf(firstDay.getHumidity() != null ? firstDay.getHumidity() : 60))
                        .windSpeed(firstDay.getWindSpeed())
                        .windDirection(firstDay.getWindDirection())
                        .weatherCondition(firstDay.getWeatherCondition())
                        .precipitation(firstDay.getPrecipitation())
                        .uvIndex(firstDay.getUvIndex())
                        .recordTime(LocalDateTime.now())
                        .dataSource("redis_migration_weekly")
                        .build();
                    
                    weatherDataMapper.insert(weatherData);
                    migratedRecords.incrementAndGet();
                    successCount++;
                    log.debug("成功迁移周预报数据: {}", location);
                }
            } catch (Exception e) {
                failedRecords.incrementAndGet();
                log.error("迁移周预报数据失败: key={}, error={}", key, e.getMessage());
            }
        }
        
        log.info("周预报数据迁移完成，成功: {} 条", successCount);
        return successCount;
    }
    
    @Override
    public MigrationStatus getMigrationStatus() {
        String message = migrationCompleted 
            ? "迁移已完成" 
            : "迁移进行中或未开始";
        
        return new MigrationStatus(
            totalRecords.get(),
            migratedRecords.get(),
            failedRecords.get(),
            migrationCompleted,
            message
        );
    }
    
    private WeatherData convertToWeatherData(WeatherResponse response) {
        return WeatherData.builder()
            .location(response.getLocation())
            .temperature(response.getTemperature())
            .feelsLike(response.getFeelsLike())
            .humidity(response.getHumidity())
            .windSpeed(response.getWindSpeed())
            .windDirection(response.getWindDirection())
            .windScale(response.getWindScale())
            .weatherCondition(response.getWeatherCondition())
            .weatherDescription(response.getWeatherDescription())
            .airQualityIndex(response.getAirQualityIndex())
            .airQualityLevel(response.getAirQualityLevel())
            .pm25(response.getPm25())
            .pm10(response.getPm10())
            .uvIndex(response.getUvIndex())
            .visibility(response.getVisibility())
            .pressure(response.getPressure())
            .precipitation(response.getPrecipitation())
            .recordTime(response.getRecordTime() != null ? response.getRecordTime() : LocalDateTime.now())
            .dataSource("redis_migration")
            .build();
    }
}
