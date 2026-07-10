package com.skygazer.weather.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_AUTH_REQUESTS_PER_MINUTE = 5;
    private static final long CLEANUP_INTERVAL_MS = 60000;
    
    private final ConcurrentMap<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private volatile long lastCleanupTime = System.currentTimeMillis();
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();
        
        cleanupIfNeeded();
        
        boolean isAuthRequest = isAuthRequest(requestPath);
        int maxRequests = isAuthRequest ? MAX_AUTH_REQUESTS_PER_MINUTE : MAX_REQUESTS_PER_MINUTE;
        String key = clientIp + ":" + (isAuthRequest ? "auth" : "general");
        
        RequestCounter counter = requestCounts.computeIfAbsent(key, k -> new RequestCounter());
        
        if (counter.incrementAndGet() > maxRequests) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, requestPath);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private boolean isAuthRequest(String path) {
        return path.contains("/login") || path.contains("/register");
    }
    
    private void cleanupIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            synchronized (this) {
                if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
                    requestCounts.clear();
                    lastCleanupTime = currentTime;
                    log.debug("Rate limit counters cleaned up");
                }
            }
        }
    }
    
    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private final long timestamp = System.currentTimeMillis();
        
        public int incrementAndGet() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - timestamp > 60000) {
                count.set(0);
            }
            return count.incrementAndGet();
        }
    }
}
