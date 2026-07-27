package com.skygazer.weather.config;

import com.skygazer.weather.config.WeatherImageProperties;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
public class WebClientConfig {

    /** MetaWeather API 基址 */
    @Value("${metaweather.api.base-url:https://www.metaweather.com}")
    private String metaWeatherBaseUrl;

    /** 阿里云 DataV 地图边界 GeoJSON 基址（天气地图图层使用） */
    @Value("${geo.datav.base-url:https://geo.datav.aliyun.com}")
    private String geoDataVBaseUrl;

    /** MetaWeather 客户端 */
    @Bean("metaWeatherWebClient")
    public WebClient metaWeatherWebClient() {
        return buildClient(metaWeatherBaseUrl);
    }

    /** 主 WebClient（天气地图 GeoJSON 等），baseUrl 指向阿里云 DataV */
    @Bean
    @Primary
    public WebClient webClient() {
        return buildClient(geoDataVBaseUrl);
    }

    /** 天气图片识别模型（Python Flask 服务）专用 WebClient */
    @Bean("weatherImageWebClient")
    public WebClient weatherImageWebClient(WeatherImageProperties props) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                })
                .build();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) (props.getConnectTimeout() * 1000L))
                .responseTimeout(Duration.ofSeconds(props.getResponseTimeout()));

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Accept-Charset", StandardCharsets.UTF_8.name())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private WebClient buildClient(String baseUrl) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                })
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .defaultHeader("Accept-Charset", StandardCharsets.UTF_8.name())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
