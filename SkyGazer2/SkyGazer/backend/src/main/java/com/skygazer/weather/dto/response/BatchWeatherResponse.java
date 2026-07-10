package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchWeatherResponse {
    
    private int totalCount;
    private int successCount;
    private int failedCount;
    private Map<String, WeatherResponse> successResults;
    private Map<String, String> failedResults;
    private List<String> cachedLocations;
    private long queryTimeMs;
}
