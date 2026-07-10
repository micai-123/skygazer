package com.skygazer.weather.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_data", indexes = {
    @Index(name = "idx_location_time", columnList = "location, recordTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String location;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal feelsLike;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal humidity;
    
    @Column(name = "wind_speed", precision = 5, scale = 2)
    private BigDecimal windSpeed;
    
    @Column(name = "wind_direction", length = 20)
    private String windDirection;
    
    @Column(name = "wind_scale", length = 10)
    private String windScale;
    
    @Column(name = "weather_condition", length = 50)
    private String weatherCondition;
    
    @Column(name = "weather_description", length = 200)
    private String weatherDescription;
    
    @Column(name = "air_quality_index")
    private Integer airQualityIndex;
    
    @Column(name = "air_quality_level", length = 20)
    private String airQualityLevel;
    
    @Column(name = "pm25")
    private Integer pm25;
    
    @Column(name = "pm10")
    private Integer pm10;
    
    @Column(name = "uv_index")
    private Integer uvIndex;
    
    @Column(name = "visibility", precision = 5, scale = 2)
    private BigDecimal visibility;
    
    @Column(name = "pressure")
    private Integer pressure;
    
    @Column(name = "precipitation", precision = 5, scale = 2)
    private BigDecimal precipitation;
    
    @Column(name = "record_time")
    private LocalDateTime recordTime;
    
    @Column(name = "data_source", length = 50)
    private String dataSource;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
