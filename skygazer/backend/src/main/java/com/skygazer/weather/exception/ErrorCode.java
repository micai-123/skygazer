package com.skygazer.weather.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    
    SUCCESS(200, "操作成功"),
    
    BAD_REQUEST(400, "请求参数错误"),
    INVALID_PARAMETER(400001, "参数验证失败"),
    MISSING_PARAMETER(400002, "缺少必要参数"),
    INVALID_CITY_NAME(400003, "无效的城市名称"),
    INVALID_TIME_RANGE(400005, "无效的时间范围"),
    IMAGE_FORMAT_ERROR(400006, "图片格式不支持"),
    IMAGE_TOO_LARGE(400007, "图片文件过大"),
    
    UNAUTHORIZED(401, "未授权访问"),
    TOKEN_EXPIRED(401001, "令牌已过期"),
    INVALID_TOKEN(401002, "无效的令牌"),
    
    FORBIDDEN(403, "禁止访问"),
    RATE_LIMIT_EXCEEDED(403001, "请求频率超限"),
    
    NOT_FOUND(404, "资源不存在"),
    CITY_NOT_FOUND(404001, "城市不存在"),
    WEATHER_DATA_NOT_FOUND(404002, "天气数据不存在"),
    
    INTERNAL_ERROR(500, "系统内部错误"),
    DATABASE_ERROR(500001, "数据库操作失败"),
    CACHE_ERROR(500002, "缓存操作失败"),
    CONFIG_ERROR(500003, "配置错误"),
    
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    WEATHER_API_ERROR(503001, "天气API服务异常"),
    AI_SERVICE_ERROR(503002, "AI服务异常"),
    EXTERNAL_API_TIMEOUT(503003, "外部API请求超时"),
    EXTERNAL_API_ERROR(503004, "外部API调用失败"),
    
    DATA_PARSE_ERROR(503005, "数据解析失败"),
    NETWORK_ERROR(503006, "网络连接失败"),
    MODEL_INFERENCE_ERROR(503007, "天气图片模型推理失败");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return INTERNAL_ERROR;
    }
}
