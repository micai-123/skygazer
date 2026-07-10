package com.skygazer.weather.service;

import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.repository.WeatherDataRepository;
import com.skygazer.weather.service.impl.WeatherServiceImpl;
import com.skygazer.weather.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {
    
    @Mock
    private WeatherDataRepository weatherDataRepository;
    
    @Mock
    private RedisUtil redisUtil;
    
    @Mock
    private WebClient webClient;
    
    private WeatherService weatherService;
    
    @BeforeEach
    void setUp() {
        weatherService = new WeatherServiceImpl(
            weatherDataRepository,
            redisUtil,
            webClient,
            new ObjectMapper()
        );
    }
    
    @Test
    @DisplayName("获取当前天气 - 从缓存获取")
    void testGetCurrentWeatherFromCache() {
        WeatherResponse cachedResponse = WeatherResponse.builder()
            .location("北京")
            .temperature(java.math.BigDecimal.valueOf(25))
            .build();
        
        when(redisUtil.get(anyString(), eq(WeatherResponse.class)))
            .thenReturn(cachedResponse);
        
        WeatherResponse response = weatherService.getCurrentWeather("北京");
        
        assertNotNull(response);
        assertEquals("北京", response.getLocation());
        assertEquals(java.math.BigDecimal.valueOf(25), response.getTemperature());
        
        verify(redisUtil).get(anyString(), eq(WeatherResponse.class));
        verify(weatherDataRepository, never()).findFirstByLocationOrderByRecordTimeDesc(anyString());
    }
}
