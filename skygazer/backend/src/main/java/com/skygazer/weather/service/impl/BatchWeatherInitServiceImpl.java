package com.skygazer.weather.service.impl;

import com.skygazer.weather.client.MetaWeatherClient;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.entity.WeatherData;
import com.skygazer.weather.mapper.WeatherDataMapper;
import com.skygazer.weather.service.BatchWeatherInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchWeatherInitServiceImpl implements BatchWeatherInitService {

    private final WeatherDataMapper weatherDataMapper;
    private final MetaWeatherClient metaWeatherClient;

    private static final long API_CALL_DELAY_MS = 500;

    /**
     * 仅批量初始化热门城市，避免对全部城市发起外部天气请求，减轻数据获取压力。
     * 与前端 src/data/cities.js 的 hotCities 保持一致。
     */
    private static final List<CityInfo> MAJOR_CITIES = Arrays.asList(
            new CityInfo("北京", 39.90, 116.40),
            new CityInfo("上海", 31.23, 121.47),
            new CityInfo("广州", 23.16, 113.23),
            new CityInfo("深圳", 22.54, 114.06),
            new CityInfo("杭州", 30.25, 120.17),
            new CityInfo("成都", 30.67, 104.07),
            new CityInfo("武汉", 30.58, 114.30),
            new CityInfo("西安", 34.26, 108.95),
            new CityInfo("南京", 32.06, 118.80),
            new CityInfo("重庆", 29.56, 106.55)
    );

    @Override
    public void initializeAllCitiesWeather() {
        log.info("========== 开始批量初始化城市天气数据（MetaWeather）==========");
        log.info("总共需要初始化 {} 个城市", MAJOR_CITIES.size());

        int successCount = 0;
        int failureCount = 0;

        for (int i = 0; i < MAJOR_CITIES.size(); i++) {
            CityInfo city = MAJOR_CITIES.get(i);
            log.info("正在处理 [{}/{}]: {}", i + 1, MAJOR_CITIES.size(), city.name);

            try {
                WeatherResponse response = fetchWeatherForCity(city.name, null, city.lat, city.lng);
                if (response != null) {
                    successCount++;
                    log.info("✓ {} 天气数据获取成功 - 温度: {}°C, 天气: {}",
                            city.name, response.getTemperature(), response.getWeatherCondition());
                } else {
                    failureCount++;
                    log.warn("✗ {} 天气数据获取失败，使用默认数据", city.name);
                }

                if (i < MAJOR_CITIES.size() - 1) {
                    Thread.sleep(API_CALL_DELAY_MS);
                }
            } catch (Exception e) {
                failureCount++;
                log.error("✗ {} 天气数据获取异常: {}", city.name, e.getMessage());
                saveDefaultWeatherData(city.name);
            }
        }

        log.info("========== 批量初始化完成 ==========");
        log.info("成功: {} 个城市", successCount);
        log.info("失败(已回退默认): {} 个城市", failureCount);
        log.info("总计: {} 个城市", MAJOR_CITIES.size());
    }

    @Override
    public WeatherResponse fetchWeatherForCity(String cityName, String cityCode, Double lat, Double lng) {
        log.info("获取城市天气数据: {}", cityName);
        List<MetaWeatherClient.ConsolidatedWeather> forecast = metaWeatherClient.fetchForecast(cityName);
        if (forecast != null && !forecast.isEmpty()) {
            MetaWeatherClient.ConsolidatedWeather c = forecast.get(0);
            double kmh = MetaWeatherClient.toKmh(c.getWindSpeed());
            WeatherData weatherData = WeatherData.builder()
                    .location(cityName)
                    .temperature(BigDecimal.valueOf(Math.round(c.getTheTemp() != null ? c.getTheTemp() : 20)))
                    .feelsLike(BigDecimal.valueOf(Math.round(c.getTheTemp() != null ? c.getTheTemp() : 20)))
                    .humidity(BigDecimal.valueOf(c.getHumidity() != null ? c.getHumidity() : 60))
                    .windSpeed(BigDecimal.valueOf(Math.round(kmh)))
                    .windDirection(MetaWeatherClient.toCompass(c.getWindDirection()))
                    .windScale(MetaWeatherClient.toWindScale(kmh))
                    .weatherCondition(MetaWeatherClient.toCondition(c))
                    .weatherDescription(MetaWeatherClient.toDescription(c))
                    .airQualityIndex(50)
                    .airQualityLevel("良")
                    .pm25(20)
                    .pm10(30)
                    .uvIndex(5)
                    .visibility(BigDecimal.valueOf(Math.round(MetaWeatherClient.toKm(c.getVisibility()))))
                    .pressure(c.getAirPressure() != null ? c.getAirPressure().intValue() : 1013)
                    .precipitation(MetaWeatherClient.toPrecipitation(c))
                    .recordTime(LocalDateTime.now())
                    .dataSource("metaweather")
                    .build();

            weatherDataMapper.insert(weatherData);
            return convertToResponse(weatherData);
        }

        // MetaWeather 不可用，使用默认数据
        log.info("MetaWeather 未获取到 {} 的天气数据，使用默认数据", cityName);
        saveDefaultWeatherData(cityName);

        Map<String, Object> defaultData = getDefaultWeatherData(cityName);
        return WeatherResponse.builder()
                .location(cityName)
                .temperature(new BigDecimal(String.valueOf(defaultData.get("temperature"))))
                .feelsLike(new BigDecimal(String.valueOf(defaultData.get("feelsLike"))))
                .humidity(new BigDecimal(String.valueOf(defaultData.get("humidity"))))
                .windSpeed(new BigDecimal(String.valueOf(defaultData.get("windSpeed"))))
                .windDirection(String.valueOf(defaultData.get("windDirection")))
                .windScale(String.valueOf(defaultData.get("windScale")))
                .weatherCondition(String.valueOf(defaultData.get("weatherCondition")))
                .weatherDescription(String.valueOf(defaultData.get("weatherDescription")))
                .airQualityIndex((Integer) defaultData.get("airQualityIndex"))
                .airQualityLevel(String.valueOf(defaultData.get("airQualityLevel")))
                .pm25((Integer) defaultData.get("pm25"))
                .pm10((Integer) defaultData.get("pm10"))
                .uvIndex((Integer) defaultData.get("uvIndex"))
                .visibility(new BigDecimal(String.valueOf(defaultData.get("visibility"))))
                .pressure((Integer) defaultData.get("pressure"))
                .precipitation(new BigDecimal(String.valueOf(defaultData.get("precipitation"))))
                .build();
    }

    @Override
    public Map<String, Object> getDefaultWeatherData(String cityName) {
        Map<String, Object> defaultData = new HashMap<>();
        defaultData.put("location", cityName);
        defaultData.put("temperature", 22);
        defaultData.put("feelsLike", 21);
        defaultData.put("humidity", 60);
        defaultData.put("windSpeed", 3);
        defaultData.put("windDirection", "微风");
        defaultData.put("windScale", "1");
        defaultData.put("weatherCondition", "晴");
        defaultData.put("weatherDescription", "天气状况：晴");
        defaultData.put("airQualityIndex", 50);
        defaultData.put("airQualityLevel", "良");
        defaultData.put("pm25", 25);
        defaultData.put("pm10", 50);
        defaultData.put("uvIndex", 5);
        defaultData.put("visibility", 10);
        defaultData.put("pressure", 1013);
        defaultData.put("precipitation", 0);
        defaultData.put("dataSource", "default");
        return defaultData;
    }

    private void saveDefaultWeatherData(String cityName) {
        try {
            Map<String, Object> defaultData = getDefaultWeatherData(cityName);

            WeatherData weatherData = WeatherData.builder()
                    .location(cityName)
                    .temperature(new BigDecimal(String.valueOf(defaultData.get("temperature"))))
                    .feelsLike(new BigDecimal(String.valueOf(defaultData.get("feelsLike"))))
                    .humidity(new BigDecimal(String.valueOf(defaultData.get("humidity"))))
                    .windSpeed(new BigDecimal(String.valueOf(defaultData.get("windSpeed"))))
                    .windDirection(String.valueOf(defaultData.get("windDirection")))
                    .windScale(String.valueOf(defaultData.get("windScale")))
                    .weatherCondition(String.valueOf(defaultData.get("weatherCondition")))
                    .weatherDescription(String.valueOf(defaultData.get("weatherDescription")))
                    .airQualityIndex((Integer) defaultData.get("airQualityIndex"))
                    .airQualityLevel(String.valueOf(defaultData.get("airQualityLevel")))
                    .pm25((Integer) defaultData.get("pm25"))
                    .pm10((Integer) defaultData.get("pm10"))
                    .uvIndex((Integer) defaultData.get("uvIndex"))
                    .visibility(new BigDecimal(String.valueOf(defaultData.get("visibility"))))
                    .pressure((Integer) defaultData.get("pressure"))
                    .precipitation(new BigDecimal(String.valueOf(defaultData.get("precipitation"))))
                    .recordTime(LocalDateTime.now())
                    .dataSource(String.valueOf(defaultData.get("dataSource")))
                    .build();

            weatherDataMapper.insert(weatherData);
            log.debug("已为 {} 保存默认天气数据", cityName);

        } catch (Exception e) {
            log.error("保存 {} 默认天气数据失败: {}", cityName, e.getMessage());
        }
    }

    private AirQualityData getDefaultAirQualityData() {
        return new AirQualityData(50, "良", 25, 50);
    }

    private WeatherResponse convertToResponse(WeatherData data) {
        return WeatherResponse.builder()
                .location(data.getLocation())
                .temperature(data.getTemperature())
                .feelsLike(data.getFeelsLike())
                .humidity(data.getHumidity())
                .windSpeed(data.getWindSpeed())
                .windDirection(data.getWindDirection())
                .windScale(data.getWindScale())
                .weatherCondition(data.getWeatherCondition())
                .weatherDescription(data.getWeatherDescription())
                .airQualityIndex(data.getAirQualityIndex())
                .airQualityLevel(data.getAirQualityLevel())
                .pm25(data.getPm25())
                .pm10(data.getPm10())
                .uvIndex(data.getUvIndex())
                .visibility(data.getVisibility())
                .pressure(data.getPressure())
                .precipitation(data.getPrecipitation())
                .recordTime(data.getRecordTime())
                .build();
    }

    private static class CityInfo {
        String name;
        Double lat;
        Double lng;

        CityInfo(String name, Double lat, Double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private static class AirQualityData {
        Integer aqi;
        String category;
        Integer pm25;
        Integer pm10;

        AirQualityData(Integer aqi, String category, Integer pm25, Integer pm10) {
            this.aqi = aqi;
            this.category = category;
            this.pm25 = pm25;
            this.pm10 = pm10;
        }
    }
}
