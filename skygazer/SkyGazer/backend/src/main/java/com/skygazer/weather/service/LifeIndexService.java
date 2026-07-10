package com.skygazer.weather.service;

import com.skygazer.weather.dto.response.LifeIndexResponse;
import com.skygazer.weather.dto.response.WeatherResponse;

import java.util.List;

public interface LifeIndexService {
    
    LifeIndexResponse calculateLifeIndices(String cityCode);
    
    LifeIndexResponse calculateLifeIndices(String cityCode, List<String> indexTypes);
    
    LifeIndexResponse.LifeIndex calculateSingleIndex(String indexType, WeatherResponse weather);
    
    List<LifeIndexResponse.LifeIndex> calculateAllIndices(WeatherResponse weather);
}
