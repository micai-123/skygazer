package com.skygazer.weather.scheduler;

import com.skygazer.weather.constant.CacheConstants;
import com.skygazer.weather.constant.LayerType;
import com.skygazer.weather.dto.response.WeatherMapResponse;
import com.skygazer.weather.service.WeatherMapService;
import com.skygazer.weather.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherMapScheduler {
    
    private final WeatherMapService weatherMapService;
    private final RedisUtil redisUtil;
    
    private static final List<String> PROVINCE_ADCODES = Arrays.asList(
        "110000", "120000", "130000", "140000", "150000",
        "210000", "220000", "230000", "310000", "320000",
        "330000", "340000", "350000", "360000", "370000",
        "410000", "420000", "430000", "440000", "450000",
        "460000", "500000", "510000", "520000", "530000",
        "540000", "610000", "620000", "630000", "640000", "650000"
    );
    
    private static final List<String> LAYER_TYPES = Arrays.asList(
        "temperature", "precipitation", "wind", "pressure", 
        "cloud", "air_quality", "visibility"
    );
    
    @Scheduled(cron = "0 0 * * * ?")
    public void refreshDistrictWeatherHourly() {
        log.info("开始执行每小时天气数据刷新任务");
        
        int successCount = 0;
        int failCount = 0;
        
        for (String adcode : PROVINCE_ADCODES) {
            for (String layerType : LAYER_TYPES) {
                try {
                    String cacheKey = CacheConstants.DISTRICT_WEATHER_PREFIX + adcode + ":" + layerType;
                    redisUtil.delete(cacheKey);
                    
                    WeatherMapResponse response = weatherMapService.getDistrictWeather(adcode, layerType);
                    
                    if (response != null && response.getCities() != null) {
                        successCount++;
                        log.debug("刷新成功: adcode={}, layerType={}, cityCount={}", 
                            adcode, layerType, response.getCities().size());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("刷新失败: adcode={}, layerType={}, error={}", 
                        adcode, layerType, e.getMessage());
                }
            }
        }
        
        log.info("天气数据刷新任务完成: 成功={}, 失败={}", successCount, failCount);
    }
    
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearRefreshLimits() {
        log.info("清理所有刷新限制");
        
        for (String adcode : PROVINCE_ADCODES) {
            String refreshKey = CacheConstants.DISTRICT_REFRESH_LIMIT + adcode;
            redisUtil.delete(refreshKey);
        }
    }
}
