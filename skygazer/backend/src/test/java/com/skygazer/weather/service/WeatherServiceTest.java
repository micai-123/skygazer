package com.skygazer.weather.service;

import com.skygazer.weather.client.MetaWeatherClient;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.mapper.WeatherDataMapper;
import com.skygazer.weather.service.impl.WeatherServiceImpl;
import com.skygazer.weather.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {
    
    @Mock
    private WeatherDataMapper weatherDataMapper;
    
    @Mock
    private RedisUtil redisUtil;
    
    @Mock
    private MetaWeatherClient metaWeatherClient;
    
    private WeatherService weatherService;
    
    @BeforeEach
    void setUp() {
        weatherService = new WeatherServiceImpl(
            weatherDataMapper,
            redisUtil,
            metaWeatherClient
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
        verify(weatherDataMapper, never()).findFirstByLocationOrderByRecordTimeDesc(anyString());
    }
}
