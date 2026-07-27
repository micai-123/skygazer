package com.skygazer.weather.controller;

import com.skygazer.weather.dto.response.LifeIndexResponse;
import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.service.LifeIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/life-index")
@RequiredArgsConstructor
@Tag(name = "生活指数", description = "AI生活指数计算相关接口")
public class LifeIndexController {
    
    private final LifeIndexService lifeIndexService;
    
    @GetMapping("/{cityCode}")
    @Operation(summary = "获取城市生活指数", description = "获取指定城市的默认生活指数列表")
    public ApiResponse<LifeIndexResponse> getLifeIndices(
            @Parameter(description = "城市代码") @PathVariable String cityCode) {
        LifeIndexResponse response = lifeIndexService.calculateLifeIndices(cityCode);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{cityCode}/custom")
    @Operation(summary = "获取自定义生活指数", description = "获取指定城市的自定义生活指数列表")
    public ApiResponse<LifeIndexResponse> getCustomLifeIndices(
            @Parameter(description = "城市代码") @PathVariable String cityCode,
            @Parameter(description = "指数类型列表，逗号分隔") @RequestParam List<String> types) {
        LifeIndexResponse response = lifeIndexService.calculateLifeIndices(cityCode, types);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{cityCode}/{indexType}")
    @Operation(summary = "获取单个生活指数", description = "获取指定城市的单个生活指数详情")
    public ApiResponse<LifeIndexResponse.LifeIndex> getSingleLifeIndex(
            @Parameter(description = "城市代码") @PathVariable String cityCode,
            @Parameter(description = "指数类型") @PathVariable String indexType) {
        LifeIndexResponse weatherResponse = lifeIndexService.calculateLifeIndices(cityCode, List.of(indexType));
        if (weatherResponse.getIndices() != null && !weatherResponse.getIndices().isEmpty()) {
            return ApiResponse.success(weatherResponse.getIndices().get(0));
        }
        return ApiResponse.error(404, "指数类型不存在: " + indexType);
    }
}
