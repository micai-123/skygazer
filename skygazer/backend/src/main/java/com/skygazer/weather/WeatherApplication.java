package com.skygazer.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智观天象后端服务启动类（Spring Boot 应用入口）。
 *
 * <p>关键点说明：
 * <ul>
 *   <li>继承 {@link SpringBootServletInitializer} 并覆写 {@code configure}，
 *       以支持打成 <b>WAR 包部署到外部 Tomcat</b>（而非内嵌容器）；开发期仍可用 {@code mvn spring-boot:run} 直接运行。</li>
 *   <li>{@code @EnableAsync} 开启异步方法支持，供智能体流式问答等耗时任务在独立线程池中执行。</li>
 *   <li>{@code @EnableScheduling} 开启定时任务支持（如批量天气初始化等调度任务）。</li>
 * </ul>
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class WeatherApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WeatherApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(WeatherApplication.class, args);
    }
}
