package com.skygazer.weather.dto.airquality;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pollutant {
    
    private String code;
    private String name;
    
    @JsonProperty("fullName")
    private String fullName;
    
    private Concentration concentration;
    private List<SubIndex> subIndexes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Concentration {
        private Double value;
        private String unit;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubIndex {
        private String code;
        private Integer aqi;
        
        @JsonProperty("aqiDisplay")
        private String aqiDisplay;
    }
}
