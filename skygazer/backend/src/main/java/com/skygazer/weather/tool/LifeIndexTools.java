package com.skygazer.weather.tool;

import com.skygazer.weather.dto.geo.CityLocation;
import com.skygazer.weather.service.GeoLookupService;
import com.skygazer.weather.service.LifeIndexService;
import com.skygazer.weather.service.WarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 生活指数与气象预警工具：委托现有 {@link LifeIndexService} / {@link WarningService}，
 * 通过 {@link GeoLookupService} 将城市名解析为城市编码（adcode）。
 */
@Slf4j
@Component
public class LifeIndexTools {

    private final LifeIndexService lifeIndexService;
    private final WarningService warningService;
    private final GeoLookupService geoLookupService;

    public LifeIndexTools(LifeIndexService lifeIndexService,
                          WarningService warningService,
                          GeoLookupService geoLookupService) {
        this.lifeIndexService = lifeIndexService;
        this.warningService = warningService;
        this.geoLookupService = geoLookupService;
    }

    @Tool(description = "根据城市名计算生活指数，包括穿衣、紫外线、舒适度、洗车、运动、旅游等建议")
    public String getLifeIndex(@ToolParam(description = "城市名，例如 北京") String location) {
        log.info("[Tool] getLifeIndex -> {}", location);
        CityLocation city = geoLookupService.getCityByName(location);
        if (city == null || city.getId() == null) {
            return "无法解析「" + location + "」对应的城市编码，请提供更标准的城市名";
        }
        try {
            return lifeIndexService.calculateLifeIndices(city.getId()).toString();
        } catch (Exception e) {
            return "生活指数计算失败：" + e.getMessage();
        }
    }

    @Tool(description = "查询指定城市当前生效的气象预警信息（暴雨、高温、大风、雷电等）")
    public String getWeatherAlert(@ToolParam(description = "城市名，例如 北京") String location) {
        log.info("[Tool] getWeatherAlert -> {}", location);
        CityLocation city = geoLookupService.getCityByName(location);
        if (city == null || city.getId() == null) {
            return "无法解析「" + location + "」对应的城市编码，请提供更标准的城市名";
        }
        try {
            return warningService.getActiveWarnings(city.getId()).toString();
        } catch (Exception e) {
            return "预警查询失败：" + e.getMessage();
        }
    }
}
