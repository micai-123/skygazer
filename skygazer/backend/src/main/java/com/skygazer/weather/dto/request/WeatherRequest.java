package com.skygazer.weather.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherRequest {
    
    @NotBlank(message = "位置不能为空")
    private String location;
    
    private String type;
}
