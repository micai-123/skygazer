package com.skygazer.weather.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchWeatherRequest {
    
    @NotEmpty(message = "位置列表不能为空")
    @Size(max = 20, message = "单次批量查询最多支持20个位置")
    private List<@NotBlank(message = "位置不能为空") String> locations;
    
    @Builder.Default
    private Boolean forceRefresh = false;
}
