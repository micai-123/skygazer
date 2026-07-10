package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherTimelineResponse {
    
    private String layerType;
    private Integer totalFrames;
    private List<TimelineFrame> frames;
    private Integer currentFrameIndex;
    private Boolean isPlaying;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineFrame {
        private String time;
        private String formattedTime;
        private String imageUrl;
        private WeatherMapResponse mapData;
    }
}
