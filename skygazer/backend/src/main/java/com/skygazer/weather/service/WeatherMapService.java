package com.skygazer.weather.service;

import com.skygazer.weather.controller.WeatherMapController;
import com.skygazer.weather.dto.response.WeatherMapResponse;
import com.skygazer.weather.dto.response.WeatherTimelineResponse;

import java.util.List;

public interface WeatherMapService {
    
    WeatherMapResponse getWeatherLayer(String layerType, List<String> cities);
    
    WeatherTimelineResponse getWeatherTimeline(String layerType, Integer hours, List<String> cities);
    
    List<WeatherMapResponse> getMultiLayerData(List<String> layerTypes, List<String> cities);
    
    WeatherMapResponse refreshMapData(String layerType, List<String> cities);
    
    WeatherMapResponse getDistrictWeather(String adcode, String layerType);
    
    WeatherMapResponse refreshDistrictWeather(String adcode, String layerType);
    
    WeatherMapController.RefreshStatus getRefreshStatus(String adcode);
    
    String getGeoJson(String adcode, String type);
}
