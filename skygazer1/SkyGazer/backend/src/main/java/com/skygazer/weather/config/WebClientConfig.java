package com.skygazer.weather.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;

@Configuration
public class WebClientConfig {
    
    @Value("${weather.api.base-url:https://devapi.qweather.com/v7}")
    private String qweatherBaseUrl;
    
    @Value("${weather.api.geo-url:https://mv33jqaeug.re.qweatherapi.com/geo/v2}")
    private String qweatherGeoUrl;
    
    @Bean
    @Primary
    public WebClient webClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(configurer -> {
                configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                configurer.defaultCodecs().enableLoggingRequestDetails(true);
            })
            .build();
        
        return WebClient.builder()
            .baseUrl(qweatherBaseUrl)
            .exchangeStrategies(strategies)
            .defaultHeader("Accept-Charset", StandardCharsets.UTF_8.name())
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
    
    @Bean("geoWebClient")
    public WebClient geoWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(configurer -> {
                configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                configurer.defaultCodecs().enableLoggingRequestDetails(true);
            })
            .build();
        
        return WebClient.builder()
            .baseUrl(qweatherGeoUrl)
            .exchangeStrategies(strategies)
            .defaultHeader("Accept-Charset", StandardCharsets.UTF_8.name())
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
