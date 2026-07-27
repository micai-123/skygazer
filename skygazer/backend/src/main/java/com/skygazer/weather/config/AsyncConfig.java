package com.skygazer.weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务线程池配置。
 *
 * <p>提供名为 {@code taskExecutor} 的线程池（核心 5 / 最大 20 / 队列 100），
 * 供 {@code @Async} 方法（典型如 AI 智能体的流式问答）在独立线程执行，
 * 避免阻塞 Web 请求线程；线程名前缀 {@code weather-async-} 便于日志排查。</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("weather-async-");
        executor.initialize();
        return executor;
    }
}
