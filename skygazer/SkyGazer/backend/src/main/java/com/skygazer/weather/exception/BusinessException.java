package com.skygazer.weather.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    
    private final int code;
    private final ErrorCode errorCode;
    private final String detail;
    private final long timestamp;
    
    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.errorCode = ErrorCode.BAD_REQUEST;
        this.detail = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorCode = ErrorCode.getByCode(code);
        this.detail = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
        this.detail = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
        this.detail = detail;
        this.timestamp = System.currentTimeMillis();
    }
    
    public BusinessException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
        this.detail = detail;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static BusinessException invalidParameter(String fieldName, String reason) {
        return new BusinessException(ErrorCode.INVALID_PARAMETER, 
            String.format("字段 '%s' 验证失败: %s", fieldName, reason));
    }
    
    public static BusinessException missingParameter(String fieldName) {
        return new BusinessException(ErrorCode.MISSING_PARAMETER, 
            String.format("缺少必要参数: %s", fieldName));
    }
    
    public static BusinessException invalidCityName(String cityName) {
        return new BusinessException(ErrorCode.INVALID_CITY_NAME, 
            String.format("无效的城市名称: %s", cityName));
    }
    
    public static BusinessException cityNotFound(String cityName) {
        return new BusinessException(ErrorCode.CITY_NOT_FOUND, 
            String.format("城市不存在: %s", cityName));
    }
    
    public static BusinessException weatherDataNotFound(String cityName) {
        return new BusinessException(ErrorCode.WEATHER_DATA_NOT_FOUND, 
            String.format("未找到城市 %s 的天气数据", cityName));
    }
    
    public static BusinessException notFound(String resourceType, String resourceId) {
        return new BusinessException(ErrorCode.NOT_FOUND, 
            String.format("%s不存在: %s", resourceType, resourceId));
    }
}
