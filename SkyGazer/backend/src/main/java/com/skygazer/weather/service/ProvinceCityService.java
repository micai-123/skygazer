package com.skygazer.weather.service;

import com.skygazer.weather.dto.geo.CityLocation;
import java.util.List;
import java.util.Map;

public interface ProvinceCityService {
    
    List<String> getCitiesByAdcode(String adcode);
    
    List<CityLocation> getCityLocationsByAdcode(String adcode);
    
    Map<String, String> getAllProvinces();
    
    String getProvinceNameByAdcode(String adcode);
    
    boolean isValidAdcode(String adcode);
    
    void reloadData();
}
