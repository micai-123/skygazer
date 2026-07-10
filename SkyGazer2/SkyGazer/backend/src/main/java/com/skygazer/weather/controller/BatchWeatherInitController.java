package com.skygazer.weather.controller;

import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.service.BatchWeatherInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class BatchWeatherInitController {
    
    private final BatchWeatherInitService batchWeatherInitService;
    
    @PostMapping("/batch-init")
    public ResponseEntity<ApiResponse<String>> batchInitializeWeather() {
        log.info("收到批量初始化天气数据请求");
        
        try {
            batchWeatherInitService.initializeAllCitiesWeather();
            
            return ResponseEntity.ok(ApiResponse.success("批量初始化天气数据成功"));
            
        } catch (Exception e) {
            log.error("批量初始化天气数据失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "批量初始化天气数据失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/init-city")
    public ResponseEntity<ApiResponse<String>> initializeSingleCity(
        @RequestParam String cityName,
        @RequestParam String cityCode,
        @RequestParam(required = false) Double lat,
        @RequestParam(required = false) Double lng
    ) {
        log.info("收到初始化单个城市天气数据请求: {}", cityName);
        
        try {
            batchWeatherInitService.fetchWeatherForCity(cityName, cityCode, lat, lng);
            
            return ResponseEntity.ok(ApiResponse.success(cityName + " 天气数据初始化成功"));
            
        } catch (Exception e) {
            log.error("初始化 {} 天气数据失败: {}", cityName, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, cityName + " 天气数据初始化失败: " + e.getMessage()));
        }
    }
}
