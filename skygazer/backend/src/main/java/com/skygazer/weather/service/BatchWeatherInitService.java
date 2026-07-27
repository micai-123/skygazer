package com.skygazer.weather.service;

import com.skygazer.weather.dto.response.WeatherResponse;

import java.util.List;
import java.util.Map;

public interface BatchWeatherInitService {
    
    void initializeAllCitiesWeather();
    
    WeatherResponse fetchWeatherForCity(String cityName, String cityCode, Double lat, Double lng);
    
    Map<String, Object> getDefaultWeatherData(String cityName);
}
