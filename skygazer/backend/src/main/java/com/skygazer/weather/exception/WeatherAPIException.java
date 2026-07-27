package com.skygazer.weather.exception;

import lombok.Getter;

@Getter
public class WeatherAPIException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String apiName;
    private final String cityName;
    private final int retryCount;
    private final long timestamp;
    
    public WeatherAPIException(String message) {
        super(message);
        this.errorCode = ErrorCode.WEATHER_API_ERROR;
        this.apiName = null;
        this.cityName = null;
        this.retryCount = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    public WeatherAPIException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.WEATHER_API_ERROR;
        this.apiName = null;
        this.cityName = null;
        this.retryCount = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    public WeatherAPIException(ErrorCode errorCode, String apiName, String message) {
        super(message);
        this.errorCode = errorCode;
        this.apiName = apiName;
        this.cityName = null;
        this.retryCount = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    public WeatherAPIException(ErrorCode errorCode, String apiName, String cityName, String message) {
        super(message);
        this.errorCode = errorCode;
        this.apiName = apiName;
        this.cityName = cityName;
        this.retryCount = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    public WeatherAPIException(ErrorCode errorCode, String apiName, String cityName, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.apiName = apiName;
        this.cityName = cityName;
        this.retryCount = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    public WeatherAPIException(ErrorCode errorCode, String apiName, String cityName, String message, int retryCount) {
        super(message);
        this.errorCode = errorCode;
        this.apiName = apiName;
        this.cityName = cityName;
        this.retryCount = retryCount;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static WeatherAPIException timeout(String apiName, String cityName) {
        return new WeatherAPIException(ErrorCode.EXTERNAL_API_TIMEOUT, apiName, cityName,
            String.format("API请求超时: %s, 城市: %s", apiName, cityName));
    }
    
    public static WeatherAPIException networkError(String apiName, String cityName, Throwable cause) {
        return new WeatherAPIException(ErrorCode.NETWORK_ERROR, apiName, cityName,
            String.format("网络连接失败: %s, 城市: %s", apiName, cityName), cause);
    }
    
    public static WeatherAPIException parseError(String apiName, String cityName, String rawData) {
        return new WeatherAPIException(ErrorCode.DATA_PARSE_ERROR, apiName, cityName,
            String.format("数据解析失败: %s, 城市: %s", apiName, cityName));
    }
    
    public static WeatherAPIException apiError(String apiName, String cityName, int retryCount) {
        return new WeatherAPIException(ErrorCode.WEATHER_API_ERROR, apiName, cityName,
            String.format("API调用失败: %s, 城市: %s, 重试次数: %d", apiName, cityName, retryCount), retryCount);
    }
}
