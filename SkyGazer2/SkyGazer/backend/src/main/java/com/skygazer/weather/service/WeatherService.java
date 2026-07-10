package com.skygazer.weather.service;

import com.skygazer.weather.dto.request.BatchWeatherRequest;
import com.skygazer.weather.dto.response.BatchWeatherResponse;
import com.skygazer.weather.dto.response.HourlyForecastResponse;
import com.skygazer.weather.dto.response.LifestyleResponse;
import com.skygazer.weather.dto.response.WeatherAnalysisResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.dto.response.WeeklyForecastResponse;

import java.util.List;

public interface WeatherService {
    
    WeatherResponse getCurrentWeather(String location);
    
    HourlyForecastResponse getHourlyForecast(String location);
    
    WeeklyForecastResponse getWeeklyForecast(String location);
    
    LifestyleResponse getLifestyleIndices(String location);
    
    WeatherResponse getAirQuality(String location);
    
    WeatherResponse refreshWeatherData(String location);
    
    BatchWeatherResponse getBatchWeatherData(List<String> locations, Boolean forceRefresh);
    
    WeatherAnalysisResponse getWeatherAnalysis(String location, String timeRange);
}
