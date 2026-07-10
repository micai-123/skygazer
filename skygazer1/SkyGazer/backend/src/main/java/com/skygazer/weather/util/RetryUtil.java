package com.skygazer.weather.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@Slf4j
public class RetryUtil {
    
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_INITIAL_DELAY_MS = 1000;
    private static final double DEFAULT_MULTIPLIER = 2.0;
    
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        return executeWithRetry(operation, operationName, DEFAULT_MAX_RETRIES);
    }
    
    public <T> T executeWithRetry(Supplier<T> operation, String operationName, int maxRetries) {
        return executeWithRetry(operation, operationName, maxRetries, DEFAULT_INITIAL_DELAY_MS, DEFAULT_MULTIPLIER);
    }
    
    public <T> T executeWithRetry(
            Supplier<T> operation, 
            String operationName, 
            int maxRetries,
            long initialDelayMs,
            double multiplier) {
        
        Exception lastException = null;
        long delay = initialDelayMs;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                T result = operation.get();
                if (attempt > 1) {
                    log.info("{} 重试成功，第 {} 次尝试", operationName, attempt);
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                log.warn("{} 第 {} 次尝试失败: {}", operationName, attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        log.info("{} 等待 {} 毫秒后重试...", operationName, delay);
                        Thread.sleep(delay);
                        delay = (long) (delay * multiplier);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("{} 重试被中断", operationName);
                        throw new RuntimeException("操作被中断", ie);
                    }
                }
            }
        }
        
        log.error("{} 重试 {} 次后仍然失败", operationName, maxRetries);
        throw new RuntimeException(operationName + " 失败", lastException);
    }
    
    public <T> T executeWithRetryAndFallback(
            Supplier<T> operation, 
            String operationName, 
            Supplier<T> fallback) {
        
        try {
            return executeWithRetry(operation, operationName);
        } catch (Exception e) {
            log.warn("{} 执行失败，使用降级方案: {}", operationName, e.getMessage());
            return fallback.get();
        }
    }
    
    public boolean executeWithRetryBoolean(
            Runnable operation, 
            String operationName, 
            int maxRetries) {
        
        Exception lastException = null;
        long delay = DEFAULT_INITIAL_DELAY_MS;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                operation.run();
                if (attempt > 1) {
                    log.info("{} 重试成功，第 {} 次尝试", operationName, attempt);
                }
                return true;
            } catch (Exception e) {
                lastException = e;
                log.warn("{} 第 {} 次尝试失败: {}", operationName, attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delay);
                        delay = (long) (delay * DEFAULT_MULTIPLIER);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("{} 重试被中断", operationName);
                        return false;
                    }
                }
            }
        }
        
        log.error("{} 重试 {} 次后仍然失败", operationName, maxRetries);
        return false;
    }
}
