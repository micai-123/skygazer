package com.skygazer.weather.service;

import com.skygazer.weather.dto.response.WarningResponse;
import com.skygazer.weather.dto.response.WeatherResponse;

import java.util.List;

public interface WarningService {
    
    WarningResponse.WarningList getWarnings(String cityCode);
    
    WarningResponse.WarningList getActiveWarnings(String cityCode);
    
    WarningResponse getWarningDetail(String warningId);
    
    List<WarningResponse> analyzeWarnings(String cityCode, WeatherResponse weather);
    
    WarningResponse createWarning(String cityCode, String warningType, int level, String content);
    
    void dismissWarning(String warningId);
    
    List<WarningResponse> getWarningsByType(String cityCode, String warningType);
}
