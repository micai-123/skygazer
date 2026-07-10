package com.skygazer.weather.controller;

import com.skygazer.weather.dto.request.BatchWeatherRequest;
import com.skygazer.weather.dto.request.WeatherRequest;
import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.dto.response.BatchWeatherResponse;
import com.skygazer.weather.dto.response.HourlyForecastResponse;
import com.skygazer.weather.dto.response.LifestyleResponse;
import com.skygazer.weather.dto.response.WeatherAnalysisResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.dto.response.WeeklyForecastResponse;
import com.skygazer.weather.service.WeatherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {
    
    private final WeatherService weatherService;
    
    @GetMapping("/current")
    public ApiResponse<WeatherResponse> getCurrentWeather(@RequestParam String location) {
        WeatherResponse response = weatherService.getCurrentWeather(location);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/hourly")
    public ApiResponse<HourlyForecastResponse> getHourlyForecast(@RequestParam String location) {
        HourlyForecastResponse response = weatherService.getHourlyForecast(location);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/weekly")
    public ApiResponse<WeeklyForecastResponse> getWeeklyForecast(@RequestParam String location) {
        WeeklyForecastResponse response = weatherService.getWeeklyForecast(location);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/lifestyle")
    public ApiResponse<LifestyleResponse> getLifestyle(@RequestParam String location) {
        LifestyleResponse response = weatherService.getLifestyleIndices(location);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/air-quality")
    public ApiResponse<WeatherResponse> getAirQuality(@RequestParam String location) {
        WeatherResponse response = weatherService.getAirQuality(location);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/refresh")
    public ApiResponse<WeatherResponse> refreshWeather(@Valid @RequestBody WeatherRequest request) {
        WeatherResponse response = weatherService.refreshWeatherData(request.getLocation());
        return ApiResponse.success("数据刷新成功", response);
    }
    
    @PostMapping("/batch")
    public ApiResponse<BatchWeatherResponse> getBatchWeather(@Valid @RequestBody BatchWeatherRequest request) {
        BatchWeatherResponse response = weatherService.getBatchWeatherData(
            request.getLocations(), 
            request.getForceRefresh()
        );
        return ApiResponse.success("批量查询成功", response);
    }
    
    @GetMapping("/analysis")
    public ApiResponse<WeatherAnalysisResponse> getWeatherAnalysis(
            @RequestParam String location,
            @RequestParam(defaultValue = "24h") String timeRange) {
        WeatherAnalysisResponse response = weatherService.getWeatherAnalysis(location, timeRange);
        return ApiResponse.success(response);
    }
}
