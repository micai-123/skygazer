package com.skygazer.weather.controller;

import com.skygazer.weather.dto.response.WarningResponse;
import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.dto.response.WeatherResponse;
import com.skygazer.weather.service.WarningService;
import com.skygazer.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warnings")
@RequiredArgsConstructor
@Tag(name = "智能预警", description = "天气预警相关接口")
public class WarningController {
    
    private final WarningService warningService;
    private final WeatherService weatherService;
    
    @GetMapping("/{cityCode}")
    @Operation(summary = "获取城市预警列表", description = "获取指定城市的所有预警信息")
    public ApiResponse<WarningResponse.WarningList> getWarnings(
            @Parameter(description = "城市代码") @PathVariable String cityCode) {
        WarningResponse.WarningList warnings = warningService.getWarnings(cityCode);
        return ApiResponse.success(warnings);
    }
    
    @GetMapping("/{cityCode}/active")
    @Operation(summary = "获取活跃预警", description = "获取指定城市当前生效的预警信息")
    public ApiResponse<WarningResponse.WarningList> getActiveWarnings(
            @Parameter(description = "城市代码") @PathVariable String cityCode) {
        WarningResponse.WarningList warnings = warningService.getActiveWarnings(cityCode);
        return ApiResponse.success(warnings);
    }
    
    @GetMapping("/detail/{warningId}")
    @Operation(summary = "获取预警详情", description = "获取指定预警的详细信息")
    public ApiResponse<WarningResponse> getWarningDetail(
            @Parameter(description = "预警ID") @PathVariable String warningId) {
        WarningResponse warning = warningService.getWarningDetail(warningId);
        return ApiResponse.success(warning);
    }
    
    @GetMapping("/{cityCode}/analyze")
    @Operation(summary = "智能分析预警", description = "基于当前天气数据智能分析可能的预警")
    public ApiResponse<List<WarningResponse>> analyzeWarnings(
            @Parameter(description = "城市代码") @PathVariable String cityCode) {
        WeatherResponse weather = weatherService.getCurrentWeather(cityCode);
        List<WarningResponse> warnings = warningService.analyzeWarnings(cityCode, weather);
        return ApiResponse.success(warnings);
    }
    
    @PostMapping("/{cityCode}")
    @Operation(summary = "创建预警", description = "手动创建预警信息")
    public ApiResponse<WarningResponse> createWarning(
            @Parameter(description = "城市代码") @PathVariable String cityCode,
            @Parameter(description = "预警类型") @RequestParam String warningType,
            @Parameter(description = "预警等级(0开始)") @RequestParam int level,
            @Parameter(description = "预警内容") @RequestParam String content) {
        WarningResponse warning = warningService.createWarning(cityCode, warningType, level, content);
        return ApiResponse.success(warning);
    }
    
    @PutMapping("/{warningId}/dismiss")
    @Operation(summary = "解除预警", description = "解除指定的预警信息")
    public ApiResponse<Void> dismissWarning(
            @Parameter(description = "预警ID") @PathVariable String warningId) {
        warningService.dismissWarning(warningId);
        return ApiResponse.success(null);
    }
    
    @GetMapping("/{cityCode}/type/{warningType}")
    @Operation(summary = "按类型获取预警", description = "获取指定城市的特定类型预警")
    public ApiResponse<List<WarningResponse>> getWarningsByType(
            @Parameter(description = "城市代码") @PathVariable String cityCode,
            @Parameter(description = "预警类型") @PathVariable String warningType) {
        List<WarningResponse> warnings = warningService.getWarningsByType(cityCode, warningType);
        return ApiResponse.success(warnings);
    }
}
