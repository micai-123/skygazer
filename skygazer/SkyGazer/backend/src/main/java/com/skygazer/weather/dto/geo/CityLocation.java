package com.skygazer.weather.dto.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityLocation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String name;
    
    private String id;
    
    private String lat;
    
    private String lon;
    
    @JsonProperty("adm2")
    private String adm2;
    
    @JsonProperty("adm1")
    private String adm1;
    
    private String country;
    
    private String tz;
    
    @JsonProperty("utcOffset")
    private String utcOffset;
    
    @JsonProperty("isDst")
    private String isDst;
    
    private String type;
    
    private String rank;
    
    @JsonProperty("fxLink")
    private String fxLink;
    
    public Double getLatitude() {
        try {
            return lat != null ? Double.parseDouble(lat) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Double getLongitude() {
        try {
            return lon != null ? Double.parseDouble(lon) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
