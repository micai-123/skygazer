package com.skygazer.weather.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageAnalysisRequest {
    
    private String imageBase64;
    
    private String location;
    
    private String analysisType;
}
