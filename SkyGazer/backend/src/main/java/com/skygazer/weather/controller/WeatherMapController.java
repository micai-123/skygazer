package com.skygazer.weather.controller;

import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.dto.response.WeatherMapResponse;
import com.skygazer.weather.dto.response.WeatherTimelineResponse;
import com.skygazer.weather.service.WeatherMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/weather-map")
@RequiredArgsConstructor
@Tag(name = "气象地图", description = "气象地图数据可视化接口")
public class WeatherMapController {
    
    private final WeatherMapService weatherMapService;
    
    @GetMapping("/layer/{layerType}")
    @Operation(summary = "获取天气图层数据", description = "获取指定类型的天气图层数据，支持温度、降水、风力、气压、云量、空气质量、能见度等图层")
    public ApiResponse<WeatherMapResponse> getWeatherLayer(
            @Parameter(description = "图层类型: temperature, precipitation, wind, pressure, cloud, air_quality, visibility")
            @PathVariable String layerType,
            @Parameter(description = "城市列表，多个城市用逗号分隔")
            @RequestParam(required = false) String cities) {
        
        List<String> cityList = parseCityList(cities);
        WeatherMapResponse response = weatherMapService.getWeatherLayer(layerType, cityList);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/timeline/{layerType}")
    @Operation(summary = "获取天气时间轴数据", description = "获取指定类型天气图层的时间轴数据，用于动画播放")
    public ApiResponse<WeatherTimelineResponse> getWeatherTimeline(
            @Parameter(description = "图层类型")
            @PathVariable String layerType,
            @Parameter(description = "预测小时数，默认24小时，最大72小时")
            @RequestParam(required = false, defaultValue = "24") Integer hours,
            @Parameter(description = "城市列表")
            @RequestParam(required = false) String cities) {
        
        List<String> cityList = parseCityList(cities);
        WeatherTimelineResponse response = weatherMapService.getWeatherTimeline(layerType, hours, cityList);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/multi-layer")
    @Operation(summary = "获取多图层数据", description = "同时获取多个图层的数据，用于图层叠加显示")
    public ApiResponse<List<WeatherMapResponse>> getMultiLayerData(
            @Parameter(description = "图层类型列表，多个类型用逗号分隔")
            @RequestParam String layerTypes,
            @Parameter(description = "城市列表")
            @RequestParam(required = false) String cities) {
        
        List<String> layerTypeList = Arrays.stream(layerTypes.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
        
        List<String> cityList = parseCityList(cities);
        List<WeatherMapResponse> response = weatherMapService.getMultiLayerData(layerTypeList, cityList);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "刷新天气地图数据", description = "强制刷新指定图层的天气数据")
    public ApiResponse<WeatherMapResponse> refreshMapData(
            @Parameter(description = "图层类型")
            @RequestParam String layerType,
            @Parameter(description = "城市列表")
            @RequestParam(required = false) String cities) {
        
        List<String> cityList = parseCityList(cities);
        WeatherMapResponse response = weatherMapService.refreshMapData(layerType, cityList);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/district-weather/{adcode}")
    @Operation(summary = "获取地级市天气数据", description = "根据行政区划代码获取地级市的实时天气数据")
    public ApiResponse<WeatherMapResponse> getDistrictWeather(
            @Parameter(description = "行政区划代码，如110000代表北京市")
            @PathVariable String adcode,
            @Parameter(description = "图层类型")
            @RequestParam(required = false, defaultValue = "temperature") String layerType) {
        
        WeatherMapResponse response = weatherMapService.getDistrictWeather(adcode, layerType);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/district-refresh/{adcode}")
    @Operation(summary = "刷新地级市天气数据", description = "强制刷新指定地级市的天气数据，返回缓存数据")
    public ApiResponse<WeatherMapResponse> refreshDistrictWeather(
            @Parameter(description = "行政区划代码")
            @PathVariable String adcode,
            @Parameter(description = "图层类型")
            @RequestParam(required = false, defaultValue = "temperature") String layerType) {
        
        WeatherMapResponse response = weatherMapService.refreshDistrictWeather(adcode, layerType);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/refresh-status/{adcode}")
    @Operation(summary = "获取刷新状态", description = "检查指定地级市的刷新限制状态")
    public ApiResponse<RefreshStatus> getRefreshStatus(
            @Parameter(description = "行政区划代码")
            @PathVariable String adcode) {
        
        RefreshStatus status = weatherMapService.getRefreshStatus(adcode);
        return ApiResponse.success(status);
    }
    
    @GetMapping("/geojson/{adcode}")
    @Operation(summary = "获取行政区划GeoJSON数据", description = "获取指定行政区划的地图边界数据，用于地图渲染")
    public ApiResponse<String> getGeoJson(
            @Parameter(description = "行政区划代码，如110000代表北京市")
            @PathVariable String adcode,
            @Parameter(description = "数据类型: full-省级地图, city-地级市地图")
            @RequestParam(required = false, defaultValue = "full") String type) {
        
        String geoJson = weatherMapService.getGeoJson(adcode, type);
        return ApiResponse.success(geoJson);
    }
    
    @GetMapping("/layers")
    @Operation(summary = "获取可用图层列表", description = "获取所有可用的天气图层类型")
    public ApiResponse<List<LayerInfo>> getAvailableLayers() {
        List<LayerInfo> layers = Arrays.asList(
            new LayerInfo("temperature", "温度分布图", "显示各地区实时温度分布情况", "°C"),
            new LayerInfo("precipitation", "降水量分布图", "显示各地区降水量分布情况", "mm"),
            new LayerInfo("wind", "风力分布图", "显示各地区风力等级分布情况", "级"),
            new LayerInfo("pressure", "气压分布图", "显示各地区气压分布情况", "hPa"),
            new LayerInfo("cloud", "云量分布图", "显示各地区云量分布情况", "%"),
            new LayerInfo("air_quality", "空气质量分布图", "显示各地区AQI指数分布情况", "AQI"),
            new LayerInfo("visibility", "能见度分布图", "显示各地区能见度分布情况", "km")
        );
        return ApiResponse.success(layers);
    }
    
    private List<String> parseCityList(String cities) {
        if (cities == null || cities.trim().isEmpty()) {
            return null;
        }
        return Arrays.stream(cities.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class LayerInfo {
        private String code;
        private String name;
        private String description;
        private String unit;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class RefreshStatus {
        private boolean canRefresh;
        private Long remainingSeconds;
        private String message;
    }
}
