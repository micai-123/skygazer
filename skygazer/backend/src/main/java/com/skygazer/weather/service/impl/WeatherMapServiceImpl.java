package com.skygazer.weather.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.constant.CacheConstants;
import com.skygazer.weather.constant.LayerType;
import com.skygazer.weather.dto.geo.CityLocation;
import com.skygazer.weather.dto.response.WeatherMapResponse;
import com.skygazer.weather.dto.response.WeatherTimelineResponse;
import com.skygazer.weather.exception.BusinessException;
import com.skygazer.weather.exception.ErrorCode;
import com.skygazer.weather.exception.WeatherAPIException;
import com.skygazer.weather.service.GeoLookupService;
import com.skygazer.weather.service.ProvinceCityService;
import com.skygazer.weather.service.WeatherMapService;
import com.skygazer.weather.util.RetryUtil;
import com.skygazer.weather.util.RedisUtil;
import com.skygazer.weather.util.WeatherDataValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeatherMapServiceImpl implements WeatherMapService {
    
    private final WebClient webClient;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    private final Executor taskExecutor;
    private final RetryUtil retryUtil;
    private final WeatherDataValidator dataValidator;
    private GeoLookupService geoLookupService;
    private ProvinceCityService provinceCityService;
    
    @Autowired
    public WeatherMapServiceImpl(
            WebClient webClient, 
            RedisUtil redisUtil, 
            ObjectMapper objectMapper, 
            Executor taskExecutor,
            RetryUtil retryUtil,
            WeatherDataValidator dataValidator) {
        this.webClient = webClient;
        this.redisUtil = redisUtil;
        this.objectMapper = objectMapper;
        this.taskExecutor = taskExecutor;
        this.retryUtil = retryUtil;
        this.dataValidator = dataValidator;
    }
    
    @Autowired
    public void setGeoLookupService(GeoLookupService geoLookupService) {
        this.geoLookupService = geoLookupService;
    }
    
    @Autowired
    public void setProvinceCityService(ProvinceCityService provinceCityService) {
        this.provinceCityService = provinceCityService;
    }
    
    private static final List<String> PROVINCE_CAPITALS = Arrays.asList(
        "北京", "天津", "石家庄", "太原", "呼和浩特",
        "沈阳", "长春", "哈尔滨", "上海", "南京",
        "杭州", "合肥", "福州", "南昌", "济南",
        "郑州", "武汉", "长沙", "广州", "南宁",
        "海口", "重庆", "成都", "贵阳", "昆明",
        "拉萨", "西安", "兰州", "西宁", "银川",
        "乌鲁木齐", "香港", "澳门", "台北"
    );
    
    private static final List<String> DEFAULT_CITIES = Arrays.asList(
        "北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "武汉", 
        "西安", "重庆", "天津", "苏州", "郑州", "长沙", "东莞", "青岛",
        "沈阳", "宁波", "昆明", "合肥", "福州", "厦门", "哈尔滨", "济南",
        "大连", "长春", "太原", "贵阳", "南宁", "南昌", "石家庄", "兰州",
        "纽约", "伦敦", "东京", "巴黎", "新加坡", "悉尼", "迪拜", "首尔",
        "曼谷", "香港", "莫斯科", "洛杉矶", "旧金山", "多伦多", "柏林", "罗马"
    );
    
    private static final Map<String, CityInfo> CITY_INFO_MAP = new HashMap<>();
    
    static {
        CITY_INFO_MAP.put("北京", new CityInfo("北京", "北京", "101010100", 39.9042, 116.4074));
        CITY_INFO_MAP.put("上海", new CityInfo("上海", "上海", "101020100", 31.2304, 121.4737));
        CITY_INFO_MAP.put("广州", new CityInfo("广州", "广东", "101280101", 23.1291, 113.2644));
        CITY_INFO_MAP.put("深圳", new CityInfo("深圳", "广东", "101280601", 22.5431, 114.0579));
        CITY_INFO_MAP.put("杭州", new CityInfo("杭州", "浙江", "101210101", 30.2741, 120.1551));
        CITY_INFO_MAP.put("南京", new CityInfo("南京", "江苏", "101190101", 32.0603, 118.7969));
        CITY_INFO_MAP.put("成都", new CityInfo("成都", "四川", "101270101", 30.5728, 104.0668));
        CITY_INFO_MAP.put("武汉", new CityInfo("武汉", "湖北", "101200101", 30.5928, 114.3055));
        CITY_INFO_MAP.put("西安", new CityInfo("西安", "陕西", "101110101", 34.3416, 108.9398));
        CITY_INFO_MAP.put("重庆", new CityInfo("重庆", "重庆", "101040100", 29.4316, 106.9123));
        CITY_INFO_MAP.put("天津", new CityInfo("天津", "天津", "101030100", 39.3434, 117.3616));
        CITY_INFO_MAP.put("苏州", new CityInfo("苏州", "江苏", "101190401", 31.2989, 120.5853));
        CITY_INFO_MAP.put("郑州", new CityInfo("郑州", "河南", "101180101", 34.7466, 113.6254));
        CITY_INFO_MAP.put("长沙", new CityInfo("长沙", "湖南", "101250101", 28.2282, 112.9388));
        CITY_INFO_MAP.put("东莞", new CityInfo("东莞", "广东", "101281601", 23.0207, 113.7518));
        CITY_INFO_MAP.put("青岛", new CityInfo("青岛", "山东", "101120201", 36.0671, 120.3826));
        CITY_INFO_MAP.put("沈阳", new CityInfo("沈阳", "辽宁", "101070101", 41.8057, 123.4315));
        CITY_INFO_MAP.put("宁波", new CityInfo("宁波", "浙江", "101210401", 29.8683, 121.5440));
        CITY_INFO_MAP.put("昆明", new CityInfo("昆明", "云南", "101290101", 25.0389, 102.7183));
        CITY_INFO_MAP.put("合肥", new CityInfo("合肥", "安徽", "101220101", 31.8206, 117.2272));
        CITY_INFO_MAP.put("福州", new CityInfo("福州", "福建", "101230101", 26.0745, 119.2965));
        CITY_INFO_MAP.put("厦门", new CityInfo("厦门", "福建", "101230201", 24.4798, 118.0894));
        CITY_INFO_MAP.put("哈尔滨", new CityInfo("哈尔滨", "黑龙江", "101050101", 45.8038, 126.5350));
        CITY_INFO_MAP.put("济南", new CityInfo("济南", "山东", "101120101", 36.6512, 116.9972));
        CITY_INFO_MAP.put("大连", new CityInfo("大连", "辽宁", "101070201", 38.9140, 121.6147));
        CITY_INFO_MAP.put("长春", new CityInfo("长春", "吉林", "101060101", 43.8171, 125.3235));
        CITY_INFO_MAP.put("太原", new CityInfo("太原", "山西", "101100101", 37.8706, 112.5489));
        CITY_INFO_MAP.put("贵阳", new CityInfo("贵阳", "贵州", "101260101", 26.6470, 106.6302));
        CITY_INFO_MAP.put("南宁", new CityInfo("南宁", "广西", "101300101", 22.8170, 108.3665));
        CITY_INFO_MAP.put("南昌", new CityInfo("南昌", "江西", "101240101", 28.6820, 115.8579));
        CITY_INFO_MAP.put("石家庄", new CityInfo("石家庄", "河北", "101090101", 38.0428, 114.5149));
        CITY_INFO_MAP.put("兰州", new CityInfo("兰州", "甘肃", "101160101", 36.0611, 103.8343));
        CITY_INFO_MAP.put("呼和浩特", new CityInfo("呼和浩特", "内蒙古", "101080101", 40.8414, 111.7519));
        CITY_INFO_MAP.put("海口", new CityInfo("海口", "海南", "101310101", 20.0440, 110.1999));
        CITY_INFO_MAP.put("拉萨", new CityInfo("拉萨", "西藏", "101140101", 29.6500, 91.1000));
        CITY_INFO_MAP.put("西宁", new CityInfo("西宁", "青海", "101150101", 36.6171, 101.7782));
        CITY_INFO_MAP.put("银川", new CityInfo("银川", "宁夏", "101170101", 38.4872, 106.2309));
        CITY_INFO_MAP.put("乌鲁木齐", new CityInfo("乌鲁木齐", "新疆", "101130101", 43.8256, 87.6168));
        CITY_INFO_MAP.put("澳门", new CityInfo("澳门", "澳门", "MAC", 22.1987, 113.5439));
        CITY_INFO_MAP.put("台北", new CityInfo("台北", "台湾", "TPE", 25.0330, 121.5654));
        CITY_INFO_MAP.put("纽约", new CityInfo("纽约", "美国", "NYC", 40.7128, -74.0060));
        CITY_INFO_MAP.put("伦敦", new CityInfo("伦敦", "英国", "LON", 51.5074, -0.1278));
        CITY_INFO_MAP.put("东京", new CityInfo("东京", "日本", "TYO", 35.6762, 139.6503));
        CITY_INFO_MAP.put("巴黎", new CityInfo("巴黎", "法国", "PAR", 48.8566, 2.3522));
        CITY_INFO_MAP.put("新加坡", new CityInfo("新加坡", "新加坡", "SIN", 1.3521, 103.8198));
        CITY_INFO_MAP.put("悉尼", new CityInfo("悉尼", "澳大利亚", "SYD", -33.8688, 151.2093));
        CITY_INFO_MAP.put("迪拜", new CityInfo("迪拜", "阿联酋", "DXB", 25.2048, 55.2708));
        CITY_INFO_MAP.put("首尔", new CityInfo("首尔", "韩国", "SEL", 37.5665, 126.9780));
        CITY_INFO_MAP.put("曼谷", new CityInfo("曼谷", "泰国", "BKK", 13.7563, 100.5018));
        CITY_INFO_MAP.put("香港", new CityInfo("香港", "香港", "HKG", 22.3193, 114.1694));
        CITY_INFO_MAP.put("莫斯科", new CityInfo("莫斯科", "俄罗斯", "MOW", 55.7558, 37.6173));
        CITY_INFO_MAP.put("洛杉矶", new CityInfo("洛杉矶", "美国", "LAX", 34.0522, -118.2437));
        CITY_INFO_MAP.put("旧金山", new CityInfo("旧金山", "美国", "SFO", 37.7749, -122.4194));
        CITY_INFO_MAP.put("多伦多", new CityInfo("多伦多", "加拿大", "YTO", 43.6532, -79.3832));
        CITY_INFO_MAP.put("柏林", new CityInfo("柏林", "德国", "BER", 52.5200, 13.4050));
        CITY_INFO_MAP.put("罗马", new CityInfo("罗马", "意大利", "ROM", 41.9028, 12.4964));
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class CityInfo {
        private String name;
        private String province;
        private String cityCode;
        private double latitude;
        private double longitude;
    }
    
    @Override
    public WeatherMapResponse getWeatherLayer(String layerType, List<String> cities) {
        LayerType type = LayerType.fromCode(layerType);
        
        final List<String> effectiveCities = (cities == null || cities.isEmpty()) 
            ? PROVINCE_CAPITALS : cities;
        
        validateCities(effectiveCities);
        
        String cacheKey = CacheConstants.WEATHER_MAP_LAYER + type.getCode() + ":" + effectiveCities.hashCode();
        WeatherMapResponse cached = redisUtil.get(cacheKey, WeatherMapResponse.class);
        if (cached != null) {
            return cached;
        }
        
        List<WeatherMapResponse.CityWeatherPoint> cityData = fetchCityWeatherData(effectiveCities, type);
        
        Double minValue = type.getMinValue();
        Double maxValue = type.getMaxValue();
        
        WeatherMapResponse response = WeatherMapResponse.builder()
            .layerType(type.getCode())
            .updateTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .cities(cityData)
            .metadata(WeatherMapResponse.MapMetadata.builder()
                .layerName(type.getName())
                .layerDescription(type.getDescription())
                .colorScale(String.join("-", type.getColorScale()))
                .minValue(BigDecimal.valueOf(minValue))
                .maxValue(BigDecimal.valueOf(maxValue))
                .unit(type.getUnit())
                .build())
            .build();
        
        long ttl = CacheConstants.HOT_CITIES.contains(effectiveCities.get(0)) 
            ? CacheConstants.HOT_CITY_CACHE_TTL 
            : CacheConstants.WEATHER_CACHE_TTL;
        redisUtil.set(cacheKey, response, ttl);
        
        return response;
    }
    
    @Override
    public WeatherTimelineResponse getWeatherTimeline(String layerType, Integer hours, List<String> cities) {
        LayerType type = LayerType.fromCode(layerType);
        
        if (hours == null || hours <= 0) {
            hours = 24;
        }
        
        if (hours > 72) {
            hours = 72;
        }
        
        final List<String> effectiveCities = (cities == null || cities.isEmpty()) 
            ? DEFAULT_CITIES : cities;
        
        List<WeatherTimelineResponse.TimelineFrame> frames = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < hours; i += 3) {
            LocalDateTime frameTime = now.plusHours(i);
            WeatherMapResponse mapData = getWeatherLayer(layerType, effectiveCities);
            
            frames.add(WeatherTimelineResponse.TimelineFrame.builder()
                .time(frameTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .formattedTime(frameTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")))
                .imageUrl("/api/weather-map/images/" + type.getCode() + "/" + frameTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")))
                .mapData(mapData)
                .build());
        }
        
        return WeatherTimelineResponse.builder()
            .layerType(type.getCode())
            .totalFrames(frames.size())
            .frames(frames)
            .currentFrameIndex(0)
            .isPlaying(false)
            .build();
    }
    
    @Override
    public List<WeatherMapResponse> getMultiLayerData(List<String> layerTypes, List<String> cities) {
        if (layerTypes == null || layerTypes.isEmpty()) {
            throw BusinessException.missingParameter("layerTypes");
        }
        
        for (String layerType : layerTypes) {
            LayerType.fromCode(layerType);
        }
        
        final List<String> effectiveCities = (cities == null || cities.isEmpty()) 
            ? DEFAULT_CITIES : cities;
        
        validateCities(effectiveCities);
        
        List<CompletableFuture<WeatherMapResponse>> futures = layerTypes.stream()
            .map(layerType -> CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return getWeatherLayer(layerType, effectiveCities);
                    } catch (Exception e) {
                        log.warn("获取图层 {} 数据失败: {}", layerType, e.getMessage());
                        return null;
                    }
                }, 
                taskExecutor))
            .collect(Collectors.toList());
        
        List<WeatherMapResponse> results = futures.stream()
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (results.isEmpty()) {
            throw new WeatherAPIException(ErrorCode.WEATHER_API_ERROR, "MultiLayerService", null,
                "无法获取任何图层数据");
        }
        
        return results;
    }
    
    @Override
    public WeatherMapResponse refreshMapData(String layerType, List<String> cities) {
        LayerType type = LayerType.fromCode(layerType);
        
        final List<String> effectiveCities = (cities == null || cities.isEmpty()) 
            ? DEFAULT_CITIES : cities;
        
        String cacheKey = CacheConstants.WEATHER_MAP_LAYER + type.getCode() + ":" + effectiveCities.hashCode();
        redisUtil.delete(cacheKey);
        
        return getWeatherLayer(layerType, effectiveCities);
    }
    
    private List<WeatherMapResponse.CityWeatherPoint> fetchCityWeatherData(List<String> cities, LayerType layerType) {
        List<CompletableFuture<WeatherMapResponse.CityWeatherPoint>> futures = cities.stream()
            .map(city -> CompletableFuture.supplyAsync(() -> fetchSingleCityWeather(city, layerType), taskExecutor))
            .collect(Collectors.toList());
        
        return futures.stream()
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    private WeatherMapResponse.CityWeatherPoint fetchSingleCityWeather(String cityName, LayerType layerType) {
        try {
            CityInfo cityInfo = CITY_INFO_MAP.get(cityName);
            
            if (cityInfo == null && geoLookupService != null) {
                log.info("尝试通过GeoAPI获取城市信息: {}", cityName);
                try {
                    CityLocation cityLocation = geoLookupService.getCityByName(cityName);
                    if (cityLocation != null) {
                        cityInfo = new CityInfo(
                            cityLocation.getName(),
                            cityLocation.getAdm1() != null ? cityLocation.getAdm1() : "",
                            cityLocation.getId(),
                            cityLocation.getLatitude() != null ? cityLocation.getLatitude() : 0,
                            cityLocation.getLongitude() != null ? cityLocation.getLongitude() : 0
                        );
                        CITY_INFO_MAP.put(cityName, cityInfo);
                        log.info("通过GeoAPI获取城市信息成功: {} -> {}", cityName, cityLocation.getId());
                    } else {
                        log.warn("GeoAPI未找到城市: {}", cityName);
                    }
                } catch (Exception e) {
                    log.warn("GeoAPI获取城市 {} 信息失败: {}", cityName, e.getMessage());
                }
            } else if (cityInfo == null && geoLookupService == null) {
                log.warn("GeoLookupService未注入，无法获取城市 {} 的信息", cityName);
            }
            
            if (cityInfo == null) {
                cityInfo = new CityInfo(cityName, "", "", 0, 0);
            }
            
            String cacheKey = CacheConstants.WEATHER_CACHE_PREFIX + cityName + ":" + layerType.getCode();
            WeatherMapResponse.CityWeatherPoint cached = redisUtil.get(cacheKey, WeatherMapResponse.CityWeatherPoint.class);
            if (cached != null) {
                return cached;
            }
            
            String location = cityInfo.getCityCode();
            if (location == null || location.isEmpty()) {
                location = cityName;
            }
            
            JsonNode nowResponse = fetchFromAPI("/weather/now", location);
            JsonNode airResponse = fetchFromAPI("/air/now", location);
            
            Double value = null;
            String weather = "";
            Integer humidity = null;
            String wind = "";
            
            if (nowResponse != null && nowResponse.has("now")) {
                JsonNode now = nowResponse.get("now");
                weather = now.has("text") ? now.get("text").asText() : "";
                humidity = parseInteger(now, "humidity");
                
                String windDir = now.has("windDir") ? now.get("windDir").asText() : "";
                String windScale = now.has("windScale") ? now.get("windScale").asText() : "";
                wind = windDir + " " + windScale + "级";
                
                switch (layerType) {
                    case TEMPERATURE:
                        value = parseDouble(now, "temp");
                        break;
                    case PRECIPITATION:
                        value = parseDouble(now, "precip");
                        if (value == null) value = 0.0;
                        break;
                    case WIND:
                        value = parseDouble(now, "windSpeed");
                        break;
                    case PRESSURE:
                        value = parseDouble(now, "pressure");
                        break;
                    case CLOUD:
                        value = parseDouble(now, "cloud");
                        if (value == null) value = Math.random() * 100;
                        break;
                    case VISIBILITY:
                        value = parseDouble(now, "vis");
                        break;
                    default:
                        value = parseDouble(now, "temp");
                }
            }
            
            if (layerType == LayerType.AIR_QUALITY && airResponse != null && airResponse.has("now")) {
                JsonNode airNow = airResponse.get("now");
                if (dataValidator.isValidAirQuality(airNow)) {
                    value = parseDouble(airNow, "aqi");
                }
            }
            
            WeatherMapResponse.CityWeatherPoint point = WeatherMapResponse.CityWeatherPoint.builder()
                .name(cityName)
                .cityCode(cityInfo.getCityCode())
                .latitude(cityInfo.getLatitude())
                .longitude(cityInfo.getLongitude())
                .value(value)
                .weather(weather)
                .humidity(humidity)
                .wind(wind)
                .province(cityInfo.getProvince())
                .build();
            
            point = dataValidator.fillMissingData(point, layerType, cityName);
            
            if (!dataValidator.isValidCityWeatherPoint(point, layerType)) {
                log.warn("城市 {} 的天气数据校验失败，使用默认数据", cityName);
                return generateMockData(cityName, layerType);
            }
            
            redisUtil.set(cacheKey, point, CacheConstants.WEATHER_CACHE_TTL);
            
            return point;
        } catch (Exception e) {
            log.error("获取城市 {} 天气数据失败: {}", cityName, e.getMessage());
            return generateMockData(cityName, layerType);
        }
    }
    
    private JsonNode fetchFromAPI(String endpoint, String location) {
        // 天气图层数据由地图服务按图层类型生成模拟/校验数据（已移除和风天气 API 依赖）
        return null;
    }
    
    private Double generateMockValue(String cityName, LayerType layerType) {
        Random random = new Random(cityName.hashCode() + layerType.getCode().hashCode());
        
        double min = layerType.getMinValue();
        double max = layerType.getMaxValue();
        
        // 统一气象数据类型为整数型
        return (double) Math.round(min + random.nextDouble() * (max - min));
    }
    
    private WeatherMapResponse.CityWeatherPoint generateMockData(String cityName, LayerType layerType) {
        CityInfo cityInfo = CITY_INFO_MAP.get(cityName);
        if (cityInfo == null) {
            cityInfo = new CityInfo(cityName, "", "", 0, 0);
        }
        
        Random random = new Random(cityName.hashCode());
        
        // 统一气象数据类型为整数型
        Double value = generateMockValue(cityName, layerType);
        Integer humidity = 20 + random.nextInt(80);
        String wind = getRandomWindDirection(random) + " " + (1 + random.nextInt(6)) + "级";
        String weather = getRandomWeatherCondition(random);
        
        return WeatherMapResponse.CityWeatherPoint.builder()
            .name(cityName)
            .cityCode(cityInfo.getCityCode())
            .latitude(cityInfo.getLatitude())
            .longitude(cityInfo.getLongitude())
            .value(value)
            .weather(weather)
            .humidity(humidity)
            .wind(wind)
            .province(cityInfo.getProvince())
            .build();
    }
    
    private String getRandomWindDirection(Random random) {
        String[] directions = {"东风", "南风", "西风", "北风", "东南风", "东北风", "西南风", "西北风"};
        return directions[random.nextInt(directions.length)];
    }
    
    private String getRandomWeatherCondition(Random random) {
        String[] conditions = {"晴", "多云", "阴", "小雨", "中雨", "雾", "霾"};
        return conditions[random.nextInt(conditions.length)];
    }
    
    private Double parseDouble(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            try {
                return Double.parseDouble(node.get(field).asText());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private Integer parseInteger(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            try {
                return Integer.parseInt(node.get(field).asText());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private void validateCities(List<String> cities) {
        if (cities == null || cities.isEmpty()) {
            return;
        }
        
        for (String city : cities) {
            if (city == null || city.trim().isEmpty()) {
                throw BusinessException.invalidParameter("city", "城市名称不能为空");
            }
        }
    }
    
    @Override
    public WeatherMapResponse getDistrictWeather(String adcode, String layerType) {
        LayerType type = LayerType.fromCode(layerType);
        
        String cacheKey = CacheConstants.DISTRICT_WEATHER_PREFIX + adcode + ":" + type.getCode();
        WeatherMapResponse cached = redisUtil.get(cacheKey, WeatherMapResponse.class);
        if (cached != null && dataValidator.validateWeatherMapResponse(cached, type)) {
            log.debug("从缓存获取区域天气数据: adcode={}, layerType={}", adcode, layerType);
            return cached;
        }
        
        List<String> cities = getCitiesByAdcode(adcode);
        if (cities.isEmpty()) {
            log.warn("区域 {} 没有找到城市列表，使用默认城市", adcode);
            cities = DEFAULT_CITIES;
        }
        
        log.info("获取区域 {} 的天气数据，共 {} 个城市", adcode, cities.size());
        List<WeatherMapResponse.CityWeatherPoint> cityData = fetchCityWeatherData(cities, type);
        
        WeatherMapResponse response = WeatherMapResponse.builder()
            .layerType(type.getCode())
            .updateTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .cities(cityData)
            .metadata(WeatherMapResponse.MapMetadata.builder()
                .layerName(type.getName())
                .layerDescription(type.getDescription())
                .colorScale(String.join("-", type.getColorScale()))
                .minValue(BigDecimal.valueOf(type.getMinValue()))
                .maxValue(BigDecimal.valueOf(type.getMaxValue()))
                .unit(type.getUnit())
                .build())
            .build();
        
        if (!dataValidator.validateWeatherMapResponse(response, type)) {
            log.warn("区域 {} 的天气数据完整性校验失败", adcode);
        }
        
        redisUtil.set(cacheKey, response, CacheConstants.DISTRICT_WEATHER_CACHE_TTL);
        
        return response;
    }
    
    @Override
    public WeatherMapResponse refreshDistrictWeather(String adcode, String layerType) {
        LayerType type = LayerType.fromCode(layerType);
        
        String refreshKey = CacheConstants.DISTRICT_REFRESH_LIMIT + adcode;
        Long lastRefreshTime = redisUtil.get(refreshKey, Long.class);
        
        if (lastRefreshTime != null) {
            long elapsedSeconds = (System.currentTimeMillis() - lastRefreshTime) / 1000;
            if (elapsedSeconds < CacheConstants.REFRESH_COOLDOWN_SECONDS) {
                String cacheKey = CacheConstants.DISTRICT_WEATHER_PREFIX + adcode + ":" + type.getCode();
                WeatherMapResponse cached = redisUtil.get(cacheKey, WeatherMapResponse.class);
                if (cached != null) {
                    return cached;
                }
            }
        }
        
        redisUtil.set(refreshKey, System.currentTimeMillis(), CacheConstants.REFRESH_COOLDOWN_SECONDS);
        
        String cacheKey = CacheConstants.DISTRICT_WEATHER_PREFIX + adcode + ":" + type.getCode();
        redisUtil.delete(cacheKey);
        
        return getDistrictWeather(adcode, layerType);
    }
    
    @Override
    public com.skygazer.weather.controller.WeatherMapController.RefreshStatus getRefreshStatus(String adcode) {
        String refreshKey = CacheConstants.DISTRICT_REFRESH_LIMIT + adcode;
        Long lastRefreshTime = redisUtil.get(refreshKey, Long.class);
        
        if (lastRefreshTime == null) {
            return new com.skygazer.weather.controller.WeatherMapController.RefreshStatus(
                true, 0L, "可以刷新"
            );
        }
        
        long elapsedSeconds = (System.currentTimeMillis() - lastRefreshTime) / 1000;
        long remainingSeconds = CacheConstants.REFRESH_COOLDOWN_SECONDS - elapsedSeconds;
        
        if (remainingSeconds <= 0) {
            return new com.skygazer.weather.controller.WeatherMapController.RefreshStatus(
                true, 0L, "可以刷新"
            );
        }
        
        return new com.skygazer.weather.controller.WeatherMapController.RefreshStatus(
            false, remainingSeconds, "请等待 " + remainingSeconds + " 秒后再刷新"
        );
    }
    
    @Override
    public String getGeoJson(String adcode, String type) {
        if (adcode == null || adcode.trim().isEmpty()) {
            throw BusinessException.missingParameter("adcode");
        }
        
        if (type == null || type.trim().isEmpty()) {
            type = "full";
        }
        
        final String finalType = type;
        final String finalAdcode = adcode;
        
        String cacheKey = CacheConstants.GEOJSON_PREFIX + adcode + ":" + type;
        String cached = redisUtil.get(cacheKey, String.class);
        if (cached != null) {
            log.debug("从缓存获取GeoJSON数据: adcode={}, type={}", adcode, type);
            return cached;
        }
        
        try {
            String url;
            if ("city".equals(type)) {
                url = String.format("https://geo.datav.aliyun.com/areas_v3/bound/%s_full_city.json", adcode);
            } else {
                url = String.format("https://geo.datav.aliyun.com/areas_v3/bound/%s_full.json", adcode);
            }
            
            log.info("从阿里云DataV获取GeoJSON数据: adcode={}, type={}, url={}", adcode, type, url);
            
            String geoJson = webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> {
                        log.error("GeoJSON API返回错误状态: adcode={}, type={}, status={}", 
                            finalAdcode, finalType, response.statusCode());
                        return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> new BusinessException(
                                ErrorCode.EXTERNAL_API_ERROR, 
                                "该地区地图数据暂不可用"
                            ));
                    }
                )
                .bodyToMono(String.class)
                .block();
            
            if (geoJson == null || geoJson.trim().isEmpty()) {
                log.error("获取GeoJSON失败, adcode: {}, type: {}, 响应为空", adcode, type);
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "地图数据为空");
            }
            
            if (!geoJson.trim().startsWith("{")) {
                log.error("获取GeoJSON失败, adcode: {}, type: {}, 响应格式错误, 响应前100字符: {}", 
                    adcode, type, geoJson.substring(0, Math.min(100, geoJson.length())));
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "地图数据格式错误，返回非JSON格式");
            }
            
            try {
                JsonNode jsonNode = objectMapper.readTree(geoJson);
                if (!jsonNode.has("features") || !jsonNode.get("features").isArray()) {
                    log.error("GeoJSON格式无效, adcode: {}, type: {}, 缺少features字段", adcode, type);
                    throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "地图数据格式无效");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("GeoJSON解析失败, adcode: {}, type: {}", adcode, type, e);
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "地图数据解析失败");
            }
            
            redisUtil.set(cacheKey, geoJson, CacheConstants.GEOJSON_CACHE_TTL);
            log.info("GeoJSON数据获取并缓存成功: adcode={}, type={}", adcode, type);
            
            return geoJson;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取GeoJSON异常, adcode: {}, type: {}", adcode, type, e);
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "该地区地图数据暂不可用");
            }
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "获取地图数据失败: " + e.getMessage());
        }
    }
    
    private List<String> getCitiesByAdcode(String adcode) {
        if (provinceCityService != null) {
            List<String> cities = provinceCityService.getCitiesByAdcode(adcode);
            if (cities != null && !cities.isEmpty()) {
                log.info("通过ProvinceCityService获取 {} 的城市列表，共 {} 个城市", adcode, cities.size());
                return cities;
            }
        }
        
        log.warn("ProvinceCityService未注入或返回空列表，使用默认城市列表");
        
        if ("100000".equals(adcode)) {
            return PROVINCE_CAPITALS;
        }
        
        return DEFAULT_CITIES;
    }
}
