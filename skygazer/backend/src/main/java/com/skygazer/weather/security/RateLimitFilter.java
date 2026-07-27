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

/**
 * 全局限流过滤器：基于客户端 IP 对请求频率进行简单计数限流，防止接口被恶意刷取。
 *
 * <p>策略要点：
 * <ul>
 *   <li>普通接口每分钟上限 {@code 120} 次；登录/注册等认证接口更严格，上限 {@code 10} 次/分钟。</li>
 *   <li>天气地图等高频只读接口（白名单）直接放行，不做限制。</li>
 *   <li>以 {@code IP:类型} 为键，使用内存 {@code ConcurrentHashMap} 计数；每 60 秒清理一次计数器。</li>
 *   <li>超限返回 {@code 429} 及统一 JSON 错误体。</li>
 * </ul>
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private static final int MAX_REQUESTS_PER_MINUTE = 120;
    private static final int MAX_AUTH_REQUESTS_PER_MINUTE = 10;
    private static final long CLEANUP_INTERVAL_MS = 60000;
    
    private final ConcurrentMap<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private volatile long lastCleanupTime = System.currentTimeMillis();
    
    private static final String[] WHITELIST_PATHS = {
        "/weather-map/",
        "/api/weather-map/",
        "/geojson",
        "/district-weather"
    };
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();
        
        if (isWhitelistedPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
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
    
    private boolean isWhitelistedPath(String path) {
        for (String whitelistPath : WHITELIST_PATHS) {
            if (path.contains(whitelistPath)) {
                return true;
            }
        }
        return false;
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
