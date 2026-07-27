package com.skygazer.weather.tool;

import com.skygazer.weather.client.MetaWeatherClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 外部天气预报工具：委托现有 {@link MetaWeatherClient} 获取公开源的多日预报。
 */
@Slf4j
@Component
public class ExternalWeatherTools {

    private final MetaWeatherClient metaWeatherClient;

    public ExternalWeatherTools(MetaWeatherClient metaWeatherClient) {
        this.metaWeatherClient = metaWeatherClient;
    }

    @Tool(description = "通过外部公开天气源(MetaWeather)获取指定城市未来几天的天气预报（含天气、均温、风速、描述，无小时级数据）")
    public String getExternalForecast(@ToolParam(description = "城市名，例如 北京、London") String location) {
        log.info("[Tool] getExternalForecast -> {}", location);
        List<MetaWeatherClient.ConsolidatedWeather> list = metaWeatherClient.fetchForecast(location);
        if (list == null || list.isEmpty()) {
            return "外部预报源未提供「" + location + "」的数据（可能不支持该城市名）";
        }
        StringBuilder sb = new StringBuilder();
        for (MetaWeatherClient.ConsolidatedWeather c : list) {
            sb.append(String.format("日期:%s 天气:%s 均温:%.1f℃ 风:%s %.0fkm/h | %s%n",
                    c.getApplicableDate(),
                    MetaWeatherClient.toCondition(c),
                    c.getTheTemp() == null ? 0 : c.getTheTemp(),
                    MetaWeatherClient.toCompass(c.getWindDirection()),
                    MetaWeatherClient.toKmh(c.getWindSpeed()),
                    MetaWeatherClient.toDescription(c)));
        }
        return sb.toString().trim();
    }
}
