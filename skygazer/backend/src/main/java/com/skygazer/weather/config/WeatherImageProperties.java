package com.skygazer.weather.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 天气图片识别模型（Python Flask 服务）配置属性。
 * 对应 application.yml 中 weather-image.model.* 配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "weather-image.model")
public class WeatherImageProperties {

    /** Python 模型 API 基址（默认 http://localhost:5000） */
    private String baseUrl = "http://localhost:5000";

    /** 单次上传最大字节数（默认 10MB） */
    private long maxFileSize = 10L * 1024 * 1024;

    /** 允许的图片扩展名（小写，含点号） */
    private List<String> allowedExtensions =
            Arrays.asList(".jpg", ".jpeg", ".png", ".bmp", ".webp");

    /** 连接超时（秒） */
    private int connectTimeout = 5;

    /** 响应超时（秒） */
    private int responseTimeout = 30;
}
