package com.skygazer.weather.service;

import com.skygazer.weather.dto.response.BatchWeatherResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.entity.WeatherData;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchWeatherServiceTest {
    
    @Mock
    private WeatherDataRepository weatherDataRepository;
    
    @Mock
    private RedisUtil redisUtil;
    
    @Mock
    private WebClient webClient;
    
    private WeatherServiceImpl weatherService;
    
    @BeforeEach
    void setUp() {
        weatherService = new WeatherServiceImpl(
            weatherDataRepository,
            redisUtil,
            webClient,
            new ObjectMapper()
        );
        ReflectionTestUtils.setField(weatherService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(weatherService, "baseUrl", "https://test.api.com");
    }
    
    @Test
    @DisplayName("批量查询 - 成功查询多个城市")
    void testBatchWeatherData_Success() {
        List<String> locations = Arrays.asList("北京", "上海", "广州");
        
        WeatherData beijingData = createMockWeatherData("北京");
        WeatherData shanghaiData = createMockWeatherData("上海");
        WeatherData guangzhouData = createMockWeatherData("广州");
        
        when(redisUtil.get(anyString(), eq(WeatherResponse.class))).thenReturn(null);
        when(weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc("北京"))
            .thenReturn(Optional.of(beijingData));
        when(weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc("上海"))
            .thenReturn(Optional.of(shanghaiData));
        when(weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc("广州"))
            .thenReturn(Optional.of(guangzhouData));
        
        BatchWeatherResponse response = weatherService.getBatchWeatherData(locations, false);
        
        assertNotNull(response);
        assertEquals(3, response.getTotalCount());
        assertEquals(3, response.getSuccessCount());
        assertEquals(0, response.getFailedCount());
        assertEquals(3, response.getSuccessResults().size());
        assertTrue(response.getQueryTimeMs() > 0);
        
        verify(weatherDataRepository, times(3)).findFirstByLocationOrderByRecordTimeDesc(anyString());
        verify(redisUtil, times(3)).set(anyString(), any(), anyLong());
    }
    
    @Test
    @DisplayName("批量查询 - 部分从缓存获取")
    void testBatchWeatherData_PartialCache() {
        List<String> locations = Arrays.asList("北京", "上海");
        
        WeatherResponse cachedResponse = WeatherResponse.builder()
            .location("北京")
            .temperature(BigDecimal.valueOf(25))
            .build();
        
        WeatherData shanghaiData = createMockWeatherData("上海");
        
        when(redisUtil.get(contains("北京"), eq(WeatherResponse.class)))
            .thenReturn(cachedResponse);
        when(redisUtil.get(contains("上海"), eq(WeatherResponse.class)))
            .thenReturn(null);
        when(weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc("上海"))
            .thenReturn(Optional.of(shanghaiData));
        
        BatchWeatherResponse response = weatherService.getBatchWeatherData(locations, false);
        
        assertNotNull(response);
        assertEquals(2, response.getTotalCount());
        assertEquals(2, response.getSuccessCount());
        assertEquals(0, response.getFailedCount());
        assertEquals(1, response.getCachedLocations().size());
        assertTrue(response.getCachedLocations().contains("北京"));
        
        verify(weatherDataRepository, times(1)).findFirstByLocationOrderByRecordTimeDesc("上海");
        verify(weatherDataRepository, never()).findFirstByLocationOrderByRecordTimeDesc("北京");
    }
    
    @Test
    @DisplayName("批量查询 - 强制刷新")
    void testBatchWeatherData_ForceRefresh() {
        List<String> locations = Arrays.asList("北京");
        
        WeatherData beijingData = createMockWeatherData("北京");
        
        when(weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc("北京"))
            .thenReturn(Optional.of(beijingData));
        
        BatchWeatherResponse response = weatherService.getBatchWeatherData(locations, true);
        
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getCachedLocations().size());
        
        verify(weatherDataRepository, times(1)).findFirstByLocationOrderByRecordTimeDesc("北京");
        verify(redisUtil, never()).get(anyString(), eq(WeatherResponse.class));
        verify(redisUtil, never()).set(anyString(), any(), anyLong());
    }
    
    @Test
    @DisplayName("批量查询 - 空列表")
    void testBatchWeatherData_EmptyList() {
        List<String> locations = Collections.emptyList();
        
        BatchWeatherResponse response = weatherService.getBatchWeatherData(locations, false);
        
        assertNotNull(response);
        assertEquals(0, response.getTotalCount());
        assertEquals(0, response.getSuccessCount());
        assertEquals(0, response.getFailedCount());
    }
    
    @Test
    @DisplayName("批量查询 - 去重测试")
    void testBatchWeatherData_Deduplication() {
        List<String> locations = Arrays.asList("北京", "北京", "上海");
        
        WeatherData beijingData = createMockWeatherData("北京");
        WeatherData shanghaiData = createMockWeatherData("上海");
        
        when(redisUtil.get(anyString(), eq(WeatherResponse.class))).thenReturn(null);
        when(weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc("北京"))
            .thenReturn(Optional.of(beijingData));
        when(weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc("上海"))
            .thenReturn(Optional.of(shanghaiData));
        
        BatchWeatherResponse response = weatherService.getBatchWeatherData(locations, false);
        
        assertNotNull(response);
        assertEquals(3, response.getTotalCount());
        assertEquals(2, response.getSuccessCount());
        assertEquals(2, response.getSuccessResults().size());
        
        verify(weatherDataRepository, times(1)).findFirstByLocationOrderByRecordTimeDesc("北京");
        verify(weatherDataRepository, times(1)).findFirstByLocationOrderByRecordTimeDesc("上海");
    }
    
    @Test
    @DisplayName("批量查询 - 部分失败")
    void testBatchWeatherData_PartialFailure() {
        List<String> locations = Arrays.asList("北京", "无效城市");
        
        WeatherData beijingData = createMockWeatherData("北京");
        
        when(redisUtil.get(anyString(), eq(WeatherResponse.class))).thenReturn(null);
        when(weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc("北京"))
            .thenReturn(Optional.of(beijingData));
        when(weatherDataRepository.findFirstByLocationOrderByRecordTimeDesc("无效城市"))
            .thenThrow(new RuntimeException("数据库查询失败"));
        
        BatchWeatherResponse response = weatherService.getBatchWeatherData(locations, false);
        
        assertNotNull(response);
        assertEquals(2, response.getTotalCount());
        assertEquals(1, response.getSuccessCount());
        assertEquals(1, response.getFailedCount());
        assertTrue(response.getSuccessResults().containsKey("北京"));
        assertTrue(response.getFailedResults().containsKey("无效城市"));
    }
    
    private WeatherData createMockWeatherData(String location) {
        return WeatherData.builder()
            .location(location)
            .temperature(BigDecimal.valueOf(25))
            .feelsLike(BigDecimal.valueOf(26))
            .humidity(BigDecimal.valueOf(60))
            .windSpeed(BigDecimal.valueOf(3))
            .windDirection("东南风")
            .windScale("3级")
            .weatherCondition("晴")
            .weatherDescription("天气晴朗")
            .airQualityIndex(50)
            .airQualityLevel("良")
            .pm25(20)
            .pm10(30)
            .uvIndex(5)
            .visibility(BigDecimal.valueOf(10))
            .pressure(1013)
            .precipitation(BigDecimal.ZERO)
            .recordTime(LocalDateTime.now())
            .dataSource("test")
            .build();
    }
}
