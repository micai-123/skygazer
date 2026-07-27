package com.skygazer.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 天气图片识别响应。
 * 后端将 Python 模型返回的英文标签映射为中文，便于前端展示。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherImagePredictResponse {

    /** 英文类别标签，例如 sunny */
    private String labelEn;

    /** 中文类别名称，例如 晴天 */
    private String labelCn;

    /** 置信度（0~1，最高类概率） */
    private double confidence;

    /** 四类天气概率分布（英文标签 -> 概率） */
    private Map<String, Double> probabilities;
}
