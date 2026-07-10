package com.skygazer.weather.service;

import com.skygazer.weather.entity.WeatherData;
import com.skygazer.weather.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockDataInitializationService implements CommandLineRunner {

    private final MockWeatherDataGenerator mockWeatherDataGenerator;
    private final WeatherDataRepository weatherDataRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("检查是否需要初始化模拟数据...");
        
        long count = weatherDataRepository.count();
        if (count == 0) {
            log.info("开始生成模拟天气数据...");
            generateMockData();
            log.info("模拟数据生成完成！");
        } else {
            log.info("数据库中已有 {} 条天气数据，跳过初始化", count);
        }
    }

    @Transactional
    public void generateMockData() {
        log.info("为测试用户生成完整的模拟天气数据");
        
        String[] cities = {
            "北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "武汉",
            "西安", "重庆", "天津", "苏州", "长沙", "郑州", "青岛", "济南"
        };
        
        for (String city : cities) {
            generateCityData(city);
        }
        
        log.info("成功为 {} 个城市生成模拟数据", cities.length);
    }

    private void generateCityData(String city) {
        log.info("生成城市 {} 的模拟数据", city);
        
        WeatherData currentWeather = mockWeatherDataGenerator.generateRealTimeWeather(city);
        weatherDataRepository.save(currentWeather);
        
        List<WeatherData> hourlyForecasts = mockWeatherDataGenerator.generateHourlyForecast(city, 24);
        weatherDataRepository.saveAll(hourlyForecasts);
        
        List<WeatherData> weeklyForecasts = mockWeatherDataGenerator.generateWeeklyForecast(city);
        weatherDataRepository.saveAll(weeklyForecasts);
        
        log.info("城市 {} 数据生成完成: 实时数据1条, 小时预报24条, 周预报7条", city);
    }

    public void refreshMockData() {
        log.info("刷新模拟数据...");
        
        weatherDataRepository.deleteAll();
        
        generateMockData();
        
        log.info("模拟数据刷新完成！");
    }
}
