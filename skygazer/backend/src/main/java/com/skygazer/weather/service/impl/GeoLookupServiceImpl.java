package com.skygazer.weather.service.impl;

import com.skygazer.weather.dto.geo.CityLocation;
import com.skygazer.weather.service.GeoLookupService;
import com.skygazer.weather.util.RedisUtil;
import com.skygazer.weather.util.StaticCityProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 城市检索 / 地理解析服务。
 * <p>
 * 原实现调用和风天气 Geo API；现改为基于内置 {@link StaticCityProvider} 离线城市库，
 * 不再依赖任何外部地理 API。
 * </p>
 */
@Service
@Slf4j
public class GeoLookupServiceImpl implements GeoLookupService {

    private final RedisUtil redisUtil;

    @Autowired
    public GeoLookupServiceImpl(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    private static final String GEO_CACHE_KEY = "weather:geo:cities";
    private static final long GEO_CACHE_TTL = 86400;

    private static final Map<String, String> PROVINCE_KEYWORDS = new LinkedHashMap<>();

    static {
        PROVINCE_KEYWORDS.put("北京", "北京");
        PROVINCE_KEYWORDS.put("天津", "天津");
        PROVINCE_KEYWORDS.put("河北", "河北");
        PROVINCE_KEYWORDS.put("山西", "山西");
        PROVINCE_KEYWORDS.put("内蒙古", "内蒙古");
        PROVINCE_KEYWORDS.put("辽宁", "辽宁");
        PROVINCE_KEYWORDS.put("吉林", "吉林");
        PROVINCE_KEYWORDS.put("黑龙江", "黑龙江");
        PROVINCE_KEYWORDS.put("上海", "上海");
        PROVINCE_KEYWORDS.put("江苏", "江苏");
        PROVINCE_KEYWORDS.put("浙江", "浙江");
        PROVINCE_KEYWORDS.put("安徽", "安徽");
        PROVINCE_KEYWORDS.put("福建", "福建");
        PROVINCE_KEYWORDS.put("江西", "江西");
        PROVINCE_KEYWORDS.put("山东", "山东");
        PROVINCE_KEYWORDS.put("河南", "河南");
        PROVINCE_KEYWORDS.put("湖北", "湖北");
        PROVINCE_KEYWORDS.put("湖南", "湖南");
        PROVINCE_KEYWORDS.put("广东", "广东");
        PROVINCE_KEYWORDS.put("广西", "广西");
        PROVINCE_KEYWORDS.put("海南", "海南");
        PROVINCE_KEYWORDS.put("重庆", "重庆");
        PROVINCE_KEYWORDS.put("四川", "四川");
        PROVINCE_KEYWORDS.put("贵州", "贵州");
        PROVINCE_KEYWORDS.put("云南", "云南");
        PROVINCE_KEYWORDS.put("西藏", "西藏");
        PROVINCE_KEYWORDS.put("陕西", "陕西");
        PROVINCE_KEYWORDS.put("甘肃", "甘肃");
        PROVINCE_KEYWORDS.put("青海", "青海");
        PROVINCE_KEYWORDS.put("宁夏", "宁夏");
        PROVINCE_KEYWORDS.put("新疆", "新疆");
        PROVINCE_KEYWORDS.put("台湾", "台湾");
        PROVINCE_KEYWORDS.put("香港", "香港");
        PROVINCE_KEYWORDS.put("澳门", "澳门");
    }

    private final Map<String, List<CityLocation>> provinceCitiesCache = new ConcurrentHashMap<>();
    private final Map<String, CityLocation> cityByNameCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("初始化城市数据缓存（离线城市库）...");
        loadCachedCities();
    }

    @Override
    public List<CityLocation> searchCities(String location, String adm, String range, int number) {
        try {
            String cacheKey = String.format("%s:%s:%s:%d", GEO_CACHE_KEY, location, adm != null ? adm : "", number);
            @SuppressWarnings("unchecked")
            List<CityLocation> cached = redisUtil.get(cacheKey, List.class);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }

            List<CityLocation> cities = StaticCityProvider.searchByName(location);
            if (adm != null && !adm.isEmpty()) {
                cities = cities.stream()
                        .filter(c -> adm.equals(c.getAdm1()))
                        .collect(Collectors.toList());
            }
            if (number > 0 && cities.size() > number) {
                cities = cities.subList(0, number);
            }

            if (!cities.isEmpty()) {
                redisUtil.set(cacheKey, cities, GEO_CACHE_TTL);
                for (CityLocation city : cities) {
                    cityByNameCache.put(city.getName(), city);
                }
            }
            return cities;
        } catch (Exception e) {
            log.error("搜索城市失败: location={}, error={}", location, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<CityLocation> searchCitiesByProvince(String provinceName) {
        if (provinceCitiesCache.containsKey(provinceName)) {
            return provinceCitiesCache.get(provinceName);
        }

        String keyword = PROVINCE_KEYWORDS.get(provinceName);
        if (keyword == null) {
            keyword = provinceName;
        }

        List<CityLocation> cities = StaticCityProvider.searchByProvince(keyword).stream()
                .filter(city -> {
                    String adm1 = city.getAdm1();
                    return adm1 != null && adm1.contains(provinceName);
                })
                .collect(Collectors.toList());

        if (!cities.isEmpty()) {
            provinceCitiesCache.put(provinceName, cities);
        }
        return cities;
    }

    @Override
    public List<CityLocation> searchCitiesByAdcode(String adcode) {
        return searchCities(adcode, null, "cn", 20);
    }

    @Override
    public CityLocation getCityByName(String cityName) {
        if (cityByNameCache.containsKey(cityName)) {
            return cityByNameCache.get(cityName);
        }
        CityLocation city = StaticCityProvider.getByName(cityName);
        if (city != null) {
            cityByNameCache.put(cityName, city);
        }
        return city;
    }

    @Override
    public Map<String, List<CityLocation>> getAllProvinceCities() {
        if (!provinceCitiesCache.isEmpty()) {
            return new HashMap<>(provinceCitiesCache);
        }

        Map<String, List<CityLocation>> result = new HashMap<>();
        for (String province : PROVINCE_KEYWORDS.keySet()) {
            try {
                List<CityLocation> cities = searchCitiesByProvince(province);
                if (!cities.isEmpty()) {
                    result.put(province, cities);
                }
            } catch (Exception e) {
                log.warn("获取省份 {} 城市失败: {}", province, e.getMessage());
            }
        }
        return result;
    }

    @Override
    public void refreshCityCache() {
        log.info("刷新城市数据缓存（离线城市库）...");
        provinceCitiesCache.clear();
        cityByNameCache.clear();
        try {
            for (String province : PROVINCE_KEYWORDS.keySet()) {
                List<CityLocation> cities = searchCitiesByProvince(province);
                if (!cities.isEmpty()) {
                    provinceCitiesCache.put(province, cities);
                    for (CityLocation city : cities) {
                        cityByNameCache.put(city.getName(), city);
                    }
                }
            }
            redisUtil.set(GEO_CACHE_KEY + ":all", new HashMap<>(provinceCitiesCache), GEO_CACHE_TTL);
            log.info("城市数据缓存刷新完成，共缓存 {} 个省份", provinceCitiesCache.size());
        } catch (Exception e) {
            log.error("刷新城市数据缓存失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadCachedCities() {
        try {
            Map<String, List<CityLocation>> cached = redisUtil.get(GEO_CACHE_KEY + ":all", Map.class);
            if (cached != null && !cached.isEmpty()) {
                provinceCitiesCache.putAll(cached);
                cached.forEach((province, cities) -> {
                    for (CityLocation city : cities) {
                        cityByNameCache.put(city.getName(), city);
                    }
                });
                log.info("从缓存加载了 {} 个省份的城市数据", cached.size());
            }
        } catch (Exception e) {
            log.debug("未找到城市缓存数据");
        }
    }
}
