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
public class AirQualityResponse {
    
    private Metadata metadata;
    private List<AirQualityIndex> indexes;
    private List<Pollutant> pollutants;
    private List<Station> stations;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Metadata {
        private String tag;
    }
}
