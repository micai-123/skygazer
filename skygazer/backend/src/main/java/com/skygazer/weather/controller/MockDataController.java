package com.skygazer.weather.controller;

import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.entity.WeatherData;
import com.skygazer.weather.service.MockDataInitializationService;
import com.skygazer.weather.service.MockWeatherDataGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/mock")
@RequiredArgsConstructor
@Tag(name = "模拟数据接口", description = "提供本地生成的模拟天气数据")
public class MockDataController {

    private final MockWeatherDataGenerator mockWeatherDataGenerator;
    private final MockDataInitializationService mockDataInitializationService;

    @GetMapping("/realtime/{location}")
    @Operation(summary = "获取实时天气模拟数据", description = "生成指定位置的实时天气模拟数据")
    public ApiResponse<WeatherData> getRealTimeWeather(
            @Parameter(description = "城市名称") @PathVariable String location) {
        log.info("获取实时天气模拟数据: {}", location);
        WeatherData weather = mockWeatherDataGenerator.generateRealTimeWeather(location);
        return ApiResponse.success(weather);
    }

    @GetMapping("/hourly/{location}")
    @Operation(summary = "获取小时预报模拟数据", description = "生成指定位置的小时预报模拟数据")
    public ApiResponse<List<WeatherData>> getHourlyForecast(
            @Parameter(description = "城市名称") @PathVariable String location,
            @Parameter(description = "预报小时数") @RequestParam(defaultValue = "24") int hours) {
        log.info("获取小时预报模拟数据: {}, {}小时", location, hours);
        List<WeatherData> forecasts = mockWeatherDataGenerator.generateHourlyForecast(location, hours);
        return ApiResponse.success(forecasts);
    }

    @GetMapping("/weekly/{location}")
    @Operation(summary = "获取周预报模拟数据", description = "生成指定位置的周预报模拟数据")
    public ApiResponse<List<WeatherData>> getWeeklyForecast(
            @Parameter(description = "城市名称") @PathVariable String location) {
        log.info("获取周预报模拟数据: {}", location);
        List<WeatherData> forecasts = mockWeatherDataGenerator.generateWeeklyForecast(location);
        return ApiResponse.success(forecasts);
    }

    @GetMapping("/lifestyle/{location}")
    @Operation(summary = "获取生活指数模拟数据", description = "生成指定位置的生活指数模拟数据")
    public ApiResponse<Map<String, Object>> getLifestyleIndices(
            @Parameter(description = "城市名称") @PathVariable String location) {
        log.info("获取生活指数模拟数据: {}", location);
        Map<String, Object> indices = mockWeatherDataGenerator.generateLifestyleIndices(location);
        return ApiResponse.success(indices);
    }

    @GetMapping("/analysis/{location}")
    @Operation(summary = "获取天气分析模拟数据", description = "生成指定位置的天气分析模拟数据")
    public ApiResponse<Map<String, Object>> getWeatherAnalysis(
            @Parameter(description = "城市名称") @PathVariable String location) {
        log.info("获取天气分析模拟数据: {}", location);
        Map<String, Object> analysis = mockWeatherDataGenerator.generateWeatherAnalysis(location);
        return ApiResponse.success(analysis);
    }

    @GetMapping("/ai-assistant/{location}")
    @Operation(summary = "获取AI智慧助理模拟数据", description = "生成指定位置的AI智慧助理模拟数据")
    public ApiResponse<Map<String, Object>> getAIAssistantData(
            @Parameter(description = "城市名称") @PathVariable String location) {
        log.info("获取AI智慧助理模拟数据: {}", location);
        Map<String, Object> aiData = mockWeatherDataGenerator.generateAIAssistantData(location);
        return ApiResponse.success(aiData);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新模拟数据", description = "重新生成所有模拟数据")
    public ApiResponse<String> refreshMockData() {
        log.info("刷新模拟数据");
        mockDataInitializationService.refreshMockData();
        return ApiResponse.success("模拟数据刷新成功");
    }
}
