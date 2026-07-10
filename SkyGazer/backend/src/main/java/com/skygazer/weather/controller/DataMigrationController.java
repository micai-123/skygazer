package com.skygazer.weather.controller;

import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.service.DataMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/data-migration")
@RequiredArgsConstructor
public class DataMigrationController {
    
    private final DataMigrationService dataMigrationService;
    
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<String>> executeMigration() {
        log.info("收到数据迁移请求");
        
        try {
            dataMigrationService.migrateRedisToDatabase();
            
            return ResponseEntity.ok(ApiResponse.success("迁移成功", "数据迁移完成"));
        } catch (Exception e) {
            log.error("数据迁移失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "数据迁移失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<DataMigrationService.MigrationStatus>> getMigrationStatus() {
        DataMigrationService.MigrationStatus status = dataMigrationService.getMigrationStatus();
        return ResponseEntity.ok(ApiResponse.success("迁移状态", status));
    }
    
    @PostMapping("/weather")
    public ResponseEntity<ApiResponse<Integer>> migrateWeatherData() {
        log.info("收到当前天气数据迁移请求");
        
        try {
            int count = dataMigrationService.migrateWeatherData();
            return ResponseEntity.ok(ApiResponse.success("当前天气数据迁移完成", count));
        } catch (Exception e) {
            log.error("当前天气数据迁移失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "当前天气数据迁移失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/hourly")
    public ResponseEntity<ApiResponse<Integer>> migrateHourlyForecastData() {
        log.info("收到小时预报数据迁移请求");
        
        try {
            int count = dataMigrationService.migrateHourlyForecastData();
            return ResponseEntity.ok(ApiResponse.success("小时预报数据迁移完成", count));
        } catch (Exception e) {
            log.error("小时预报数据迁移失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "小时预报数据迁移失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/weekly")
    public ResponseEntity<ApiResponse<Integer>> migrateWeeklyForecastData() {
        log.info("收到周预报数据迁移请求");
        
        try {
            int count = dataMigrationService.migrateWeeklyForecastData();
            return ResponseEntity.ok(ApiResponse.success("周预报数据迁移完成", count));
        } catch (Exception e) {
            log.error("周预报数据迁移失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "周预报数据迁移失败: " + e.getMessage()));
        }
    }
}
