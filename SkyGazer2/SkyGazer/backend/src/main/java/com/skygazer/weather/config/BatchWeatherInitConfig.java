package com.skygazer.weather.config;

import com.skygazer.weather.service.BatchWeatherInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchWeatherInitConfig {
    
    private final BatchWeatherInitService batchWeatherInitService;
    
    @Value("${app.weather.init-on-startup:false}")
    private boolean initOnStartup;
    
    @Bean
    public CommandLineRunner batchWeatherInitRunner() {
        return args -> {
            if (initOnStartup) {
                log.info("检测到批量初始化标志，开始执行天气数据初始化...");
                try {
                    batchWeatherInitService.initializeAllCitiesWeather();
                    log.info("天气数据批量初始化完成");
                } catch (Exception e) {
                    log.error("天气数据批量初始化失败: {}", e.getMessage(), e);
                }
            } else {
                log.info("批量初始化标志未启用，跳过天气数据初始化");
                log.info("如需启用，请在配置文件中设置 app.weather.init-on-startup=true");
            }
        };
    }
}
