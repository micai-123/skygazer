package com.skygazer.weather.exception;

import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(
            BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [{}]: {} - 详情: {}", e.getErrorCode(), e.getMessage(), e.getDetail());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(e.getCode())
            .message(e.getMessage())
            .detail(e.getDetail())
            .errorType(e.getErrorCode().name())
            .path(request.getRequestURI())
            .timestamp(e.getTimestamp())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(WeatherAPIException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleWeatherAPIException(
            WeatherAPIException e, HttpServletRequest request) {
        log.error("天气API异常 [{}]: API={}, 城市={}, 重试次数={}", 
            e.getErrorCode(), e.getApiName(), e.getCityName(), e.getRetryCount(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(e.getErrorCode().getCode())
            .message(e.getMessage())
            .detail(String.format("API: %s, 城市: %s", e.getApiName(), e.getCityName()))
            .errorType(e.getErrorCode().name())
            .path(request.getRequestURI())
            .timestamp(e.getTimestamp())
            .retry(ErrorResponse.RetryInfo.builder()
                .currentRetry(e.getRetryCount())
                .maxRetry(3)
                .nextRetryDelayMs(1000)
                .canRetry(e.getRetryCount() < 3)
                .build())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.error(e.getErrorCode().getCode(), "天气服务暂时不可用，请稍后重试"));
    }
    
    @ExceptionHandler(AIModelException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAIModelException(
            AIModelException e, HttpServletRequest request) {
        log.error("AI模型异常: {}", e.getMessage(), e);
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.error(ErrorCode.AI_SERVICE_ERROR.getCode(), "AI服务暂时不可用: " + e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("参数验证失败 [{}]: {}", request.getRequestURI(), errors);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ErrorCode.INVALID_PARAMETER.getCode(), "参数验证失败: " + errors));
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        
        log.warn("约束违反 [{}]: {}", request.getRequestURI(), message);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ErrorCode.INVALID_PARAMETER.getCode(), message));
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<String>> handleMissingParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少必要参数 [{}]: {}", request.getRequestURI(), e.getParameterName());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ErrorCode.MISSING_PARAMETER.getCode(), 
                "缺少必要参数: " + e.getParameterName()));
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = String.format("参数 '%s' 类型错误，期望类型: %s", 
            e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        
        log.warn("参数类型错误 [{}]: {}", request.getRequestURI(), message);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ErrorCode.INVALID_PARAMETER.getCode(), message));
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体解析失败 [{}]: {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ErrorCode.INVALID_PARAMETER.getCode(), "请求体格式错误"));
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("不支持的请求方法 [{}]: {}", request.getRequestURI(), e.getMethod());
        
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ApiResponse.error(405, "不支持的请求方法: " + e.getMethod()));
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNoHandlerFoundException(
            NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("资源不存在 [{}]: {}", request.getRequestURI(), e.getRequestURL());
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(404, "资源不存在: " + e.getRequestURL()));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数 [{}]: {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(
            Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}]: ", request.getRequestURI(), e);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "系统内部错误，请稍后重试"));
    }
}
