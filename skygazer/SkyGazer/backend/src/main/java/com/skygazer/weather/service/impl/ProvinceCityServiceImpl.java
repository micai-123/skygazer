package com.skygazer.weather.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.dto.geo.CityLocation;
import com.skygazer.weather.service.ProvinceCityService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProvinceCityServiceImpl implements ProvinceCityService {
    
    private final ObjectMapper objectMapper;
    
    private final Map<String, ProvinceData> provinceDataMap = new ConcurrentHashMap<>();
    private final Map<String, String> adcodeToProvinceName = new ConcurrentHashMap<>();
    
    private static final String DATA_FILE = "data/province_cities.json";
    
    public ProvinceCityServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void init() {
        log.info("开始加载省份城市数据...");
        loadData();
        log.info("省份城市数据加载完成，共加载 {} 个省份", provinceDataMap.size());
    }
    
    private void loadData() {
        try {
            ClassPathResource resource = new ClassPathResource(DATA_FILE);
            if (!resource.exists()) {
                log.error("省份城市数据文件不存在: {}", DATA_FILE);
                return;
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                JsonNode root = objectMapper.readTree(inputStream);
                
                Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String adcode = entry.getKey();
                    JsonNode provinceNode = entry.getValue();
                    
                    String provinceName = provinceNode.path("name").asText();
                    String shortName = provinceNode.path("shortName").asText();
                    
                    List<CityData> cities = new ArrayList<>();
                    JsonNode citiesNode = provinceNode.path("cities");
                    if (citiesNode.isArray()) {
                        for (JsonNode cityNode : citiesNode) {
                            String cityName = cityNode.path("name").asText();
                            String cityCode = cityNode.path("code").asText();
                            double lat = cityNode.path("lat").asDouble();
                            double lon = cityNode.path("lon").asDouble();
                            
                            cities.add(new CityData(cityName, cityCode, lat, lon));
                        }
                    }
                    
                    ProvinceData provinceData = new ProvinceData(provinceName, shortName, cities);
                    provinceDataMap.put(adcode, provinceData);
                    adcodeToProvinceName.put(adcode, shortName);
                }
                
                log.info("成功加载 {} 个省级行政区的城市数据", provinceDataMap.size());
            }
        } catch (IOException e) {
            log.error("加载省份城市数据失败", e);
        }
    }
    
    @Override
    public List<String> getCitiesByAdcode(String adcode) {
        if (adcode == null || adcode.trim().isEmpty()) {
            log.warn("行政区划代码为空");
            return Collections.emptyList();
        }
        
        if ("100000".equals(adcode)) {
            return provinceDataMap.values().stream()
                .filter(p -> !p.getCities().isEmpty())
                .map(p -> p.getCities().get(0).getName())
                .collect(Collectors.toList());
        }
        
        ProvinceData provinceData = provinceDataMap.get(adcode);
        if (provinceData == null) {
            log.warn("未找到行政区划代码对应的省份: {}", adcode);
            return Collections.emptyList();
        }
        
        return provinceData.getCities().stream()
            .map(CityData::getName)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<CityLocation> getCityLocationsByAdcode(String adcode) {
        if (adcode == null || adcode.trim().isEmpty()) {
            log.warn("行政区划代码为空");
            return Collections.emptyList();
        }
        
        if ("100000".equals(adcode)) {
            return provinceDataMap.values().stream()
                .filter(p -> !p.getCities().isEmpty())
                .map(p -> {
                    CityData city = p.getCities().get(0);
                    return CityLocation.builder()
                        .name(city.getName())
                        .id(city.getCode())
                        .lat(String.valueOf(city.getLat()))
                        .lon(String.valueOf(city.getLon()))
                        .adm1(p.getShortName())
                        .build();
                })
                .collect(Collectors.toList());
        }
        
        ProvinceData provinceData = provinceDataMap.get(adcode);
        if (provinceData == null) {
            log.warn("未找到行政区划代码对应的省份: {}", adcode);
            return Collections.emptyList();
        }
        
        return provinceData.getCities().stream()
            .map(city -> CityLocation.builder()
                .name(city.getName())
                .id(city.getCode())
                .lat(String.valueOf(city.getLat()))
                .lon(String.valueOf(city.getLon()))
                .adm1(provinceData.getShortName())
                .build())
            .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, String> getAllProvinces() {
        return new HashMap<>(adcodeToProvinceName);
    }
    
    @Override
    public String getProvinceNameByAdcode(String adcode) {
        if (adcode == null || adcode.trim().isEmpty()) {
            return null;
        }
        
        if ("100000".equals(adcode)) {
            return "全国";
        }
        
        return adcodeToProvinceName.get(adcode);
    }
    
    @Override
    public boolean isValidAdcode(String adcode) {
        if (adcode == null || adcode.trim().isEmpty()) {
            return false;
        }
        
        if ("100000".equals(adcode)) {
            return true;
        }
        
        return provinceDataMap.containsKey(adcode);
    }
    
    @Override
    public void reloadData() {
        log.info("重新加载省份城市数据...");
        provinceDataMap.clear();
        adcodeToProvinceName.clear();
        loadData();
        log.info("省份城市数据重新加载完成");
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ProvinceData {
        private String name;
        private String shortName;
        private List<CityData> cities;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class CityData {
        private String name;
        private String code;
        private double lat;
        private double lon;
    }
}
