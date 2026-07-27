package com.skygazer.weather.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.dto.request.BatchWeatherRequest;
import com.skygazer.weather.dto.response.BatchWeatherResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
class BatchWeatherControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private WeatherService weatherService;
    
    @Test
    @DisplayName("批量查询天气 - 成功")
    void testBatchWeather_Success() throws Exception {
        BatchWeatherRequest request = BatchWeatherRequest.builder()
            .locations(Arrays.asList("北京", "上海"))
            .forceRefresh(false)
            .build();
        
        Map<String, WeatherResponse> successResults = new HashMap<>();
        successResults.put("北京", WeatherResponse.builder()
            .location("北京")
            .temperature(BigDecimal.valueOf(25))
            .weatherCondition("晴")
            .build());
        successResults.put("上海", WeatherResponse.builder()
            .location("上海")
            .temperature(BigDecimal.valueOf(28))
            .weatherCondition("多云")
            .build());
        
        BatchWeatherResponse response = BatchWeatherResponse.builder()
            .totalCount(2)
            .successCount(2)
            .failedCount(0)
            .successResults(successResults)
            .failedResults(new HashMap<>())
            .cachedLocations(Arrays.asList())
            .queryTimeMs(150L)
            .build();
        
        when(weatherService.getBatchWeatherData(anyList(), any())).thenReturn(response);
        
        mockMvc.perform(post("/weather/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("批量查询成功"))
            .andExpect(jsonPath("$.data.totalCount").value(2))
            .andExpect(jsonPath("$.data.successCount").value(2))
            .andExpect(jsonPath("$.data.failedCount").value(0))
            .andExpect(jsonPath("$.data.successResults['北京'].temperature").value(25))
            .andExpect(jsonPath("$.data.successResults['上海'].temperature").value(28));
    }
    
    @Test
    @DisplayName("批量查询天气 - 参数验证失败（空列表）")
    void testBatchWeather_EmptyList() throws Exception {
        BatchWeatherRequest request = BatchWeatherRequest.builder()
            .locations(Arrays.asList())
            .build();
        
        mockMvc.perform(post("/weather/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("批量查询天气 - 参数验证失败（超过上限）")
    void testBatchWeather_ExceedLimit() throws Exception {
        String[] locations = new String[21];
        for (int i = 0; i < 21; i++) {
            locations[i] = "城市" + i;
        }
        
        BatchWeatherRequest request = BatchWeatherRequest.builder()
            .locations(Arrays.asList(locations))
            .build();
        
        mockMvc.perform(post("/weather/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("批量查询天气 - 强制刷新")
    void testBatchWeather_ForceRefresh() throws Exception {
        BatchWeatherRequest request = BatchWeatherRequest.builder()
            .locations(Arrays.asList("北京"))
            .forceRefresh(true)
            .build();
        
        Map<String, WeatherResponse> successResults = new HashMap<>();
        successResults.put("北京", WeatherResponse.builder()
            .location("北京")
            .temperature(BigDecimal.valueOf(26))
            .build());
        
        BatchWeatherResponse response = BatchWeatherResponse.builder()
            .totalCount(1)
            .successCount(1)
            .failedCount(0)
            .successResults(successResults)
            .failedResults(new HashMap<>())
            .cachedLocations(Arrays.asList())
            .queryTimeMs(200L)
            .build();
        
        when(weatherService.getBatchWeatherData(anyList(), any())).thenReturn(response);
        
        mockMvc.perform(post("/weather/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.cachedLocations").isEmpty());
    }
    
    @Test
    @DisplayName("批量查询天气 - 部分失败")
    void testBatchWeather_PartialFailure() throws Exception {
        BatchWeatherRequest request = BatchWeatherRequest.builder()
            .locations(Arrays.asList("北京", "无效城市"))
            .build();
        
        Map<String, WeatherResponse> successResults = new HashMap<>();
        successResults.put("北京", WeatherResponse.builder()
            .location("北京")
            .temperature(BigDecimal.valueOf(25))
            .build());
        
        Map<String, String> failedResults = new HashMap<>();
        failedResults.put("无效城市", "获取失败: 城市不存在");
        
        BatchWeatherResponse response = BatchWeatherResponse.builder()
            .totalCount(2)
            .successCount(1)
            .failedCount(1)
            .successResults(successResults)
            .failedResults(failedResults)
            .cachedLocations(Arrays.asList())
            .queryTimeMs(180L)
            .build();
        
        when(weatherService.getBatchWeatherData(anyList(), any())).thenReturn(response);
        
        mockMvc.perform(post("/weather/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalCount").value(2))
            .andExpect(jsonPath("$.data.successCount").value(1))
            .andExpect(jsonPath("$.data.failedCount").value(1))
            .andExpect(jsonPath("$.data.failedResults['无效城市']").value("获取失败: 城市不存在"));
    }
}
