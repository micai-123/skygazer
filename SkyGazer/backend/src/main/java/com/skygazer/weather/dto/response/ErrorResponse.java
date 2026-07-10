package com.skygazer.weather.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private int code;
    private String message;
    private String detail;
    private String errorType;
    private String path;
    private long timestamp;
    private String traceId;
    private RetryInfo retry;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RetryInfo {
        private int currentRetry;
        private int maxRetry;
        private long nextRetryDelayMs;
        private boolean canRetry;
    }
    
    public static ErrorResponse of(int code, String message) {
        return ErrorResponse.builder()
            .code(code)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static ErrorResponse of(int code, String message, String detail) {
        return ErrorResponse.builder()
            .code(code)
            .message(message)
            .detail(detail)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static ErrorResponse of(int code, String message, String detail, String errorType) {
        return ErrorResponse.builder()
            .code(code)
            .message(message)
            .detail(detail)
            .errorType(errorType)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static ErrorResponse withRetry(int code, String message, String detail, 
                                          int currentRetry, int maxRetry, long nextRetryDelayMs) {
        return ErrorResponse.builder()
            .code(code)
            .message(message)
            .detail(detail)
            .timestamp(System.currentTimeMillis())
            .retry(RetryInfo.builder()
                .currentRetry(currentRetry)
                .maxRetry(maxRetry)
                .nextRetryDelayMs(nextRetryDelayMs)
                .canRetry(currentRetry < maxRetry)
                .build())
            .build();
    }
}
