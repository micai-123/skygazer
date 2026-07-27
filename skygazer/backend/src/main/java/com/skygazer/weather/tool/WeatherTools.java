package com.skygazer.weather.tool;

import com.skygazer.weather.entity.WeatherData;
import com.skygazer.weather.mapper.WeatherDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实时天气数据工具：委托现有 {@link WeatherDataMapper} 查询数据库天气记录。
 */
@Slf4j
@Component
public class WeatherTools {

    private final WeatherDataMapper weatherDataMapper;

    public WeatherTools(WeatherDataMapper weatherDataMapper) {
        this.weatherDataMapper = weatherDataMapper;
    }

    @Tool(description = "查询指定城市在数据库中的最新实况天气记录，包含温度、湿度、风速、风向、天气状况、空气质量等")
    public String getCurrentWeather(@ToolParam(description = "城市名，例如 北京、上海、广州") String location) {
        log.info("[Tool] getCurrentWeather -> {}", location);
        return weatherDataMapper.findFirstByLocationOrderByRecordTimeDesc(location)
                .map(WeatherData::toString)
                .orElse("未找到「" + location + "」的天气记录，可尝试周边城市或稍后再试");
    }

    @Tool(description = "查询指定城市最近若干天的历史天气记录，用于趋势分析与对比")
    public String getRecentWeather(
            @ToolParam(description = "城市名，例如 北京") String location,
            @ToolParam(description = "最近天数，默认 3 天") int days) {
        int d = days <= 0 ? 3 : days;
        log.info("[Tool] getRecentWeather -> {}, {}天", location, d);
        LocalDateTime start = LocalDateTime.now().minusDays(d);
        List<WeatherData> list = weatherDataMapper.findRecentByLocation(location, start);
        if (list == null || list.isEmpty()) {
            return "未找到「" + location + "」近 " + d + " 天的天气记录";
        }
        return list.stream().map(WeatherData::toString).collect(Collectors.joining("\n"));
    }
}
