package com.skygazer.weather.controller;

import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private WeatherService weatherService;
    
    @Test
    @DisplayName("获取当前天气接口测试")
    void testGetCurrentWeather() throws Exception {
        WeatherResponse response = WeatherResponse.builder()
            .location("北京")
            .temperature(BigDecimal.valueOf(25))
            .weatherCondition("晴")
            .build();
        
        when(weatherService.getCurrentWeather("北京")).thenReturn(response);
        
        mockMvc.perform(get("/weather/current").param("location", "北京"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.location").value("北京"))
            .andExpect(jsonPath("$.data.temperature").value(25));
    }
}
