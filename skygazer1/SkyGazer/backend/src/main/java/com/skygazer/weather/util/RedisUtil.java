package com.skygazer.weather.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public void set(String key, Object value, long timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis set error: {}", e.getMessage());
        }
    }
    
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis set error: {}", e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            if (clazz.isInstance(value)) {
                return (T) value;
            }
            return objectMapper.convertValue(value, clazz);
        } catch (Exception e) {
            log.error("Redis get error: {}", e.getMessage());
            return null;
        }
    }
    
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete error: {}", e.getMessage());
        }
    }
    
    public void delete(Collection<String> keys) {
        try {
            redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("Redis delete collection error: {}", e.getMessage());
        }
    }
    
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Deleted {} keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("Redis deleteByPattern error: {}", e.getMessage());
        }
    }
    
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis hasKey error: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean expire(String key, long timeout) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, TimeUnit.SECONDS));
        } catch (Exception e) {
            log.error("Redis expire error: {}", e.getMessage());
            return false;
        }
    }
    
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -1;
        } catch (Exception e) {
            log.error("Redis getExpire error: {}", e.getMessage());
            return -1;
        }
    }
}
