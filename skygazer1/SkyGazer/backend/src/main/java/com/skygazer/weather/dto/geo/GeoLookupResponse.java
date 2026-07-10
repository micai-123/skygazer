package com.skygazer.weather.dto.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLookupResponse {
    
    private String code;
    
    private List<CityLocation> location;
    
    @JsonProperty("refer")
    private Refer refer;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Refer {
        private List<String> sources;
        private List<String> license;
    }
    
    public boolean isSuccess() {
        return "200".equals(code);
    }
}
