package com.skygazer.weather.service;

import com.skygazer.weather.dto.geo.CityLocation;

import java.util.List;
import java.util.Map;

public interface GeoLookupService {
    
    List<CityLocation> searchCities(String location, String adm, String range, int number);
    
    List<CityLocation> searchCitiesByProvince(String provinceName);
    
    List<CityLocation> searchCitiesByAdcode(String adcode);
    
    CityLocation getCityByName(String cityName);
    
    Map<String, List<CityLocation>> getAllProvinceCities();
    
    void refreshCityCache();
}
