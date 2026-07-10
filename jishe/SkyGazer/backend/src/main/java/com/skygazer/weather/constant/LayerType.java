package com.skygazer.weather.constant;

import lombok.Getter;

@Getter
public enum LayerType {
    
    TEMPERATURE("temperature", "温度", "温度分布图", "°C",
        new String[]{"#60a5fa", "#34d399", "#fbbf24", "#f97316", "#ef4444"},
        new String[]{"寒冷", "凉爽", "舒适", "温暖", "炎热"},
        -20.0, 40.0),
    PRECIPITATION("precipitation", "降水", "降水量分布图", "mm",
        new String[]{"#e0f2fe", "#7dd3fc", "#38bdf8", "#0284c7", "#075985"},
        new String[]{"无雨", "小雨", "中雨", "大雨", "暴雨"},
        0.0, 100.0),
    WIND("wind", "风力", "风力分布图", "级",
        new String[]{"#d1fae5", "#6ee7b7", "#34d399", "#10b981", "#059669"},
        new String[]{"微风", "轻风", "和风", "强风", "大风"},
        0.0, 12.0),
    PRESSURE("pressure", "气压", "气压分布图", "hPa",
        new String[]{"#fef3c7", "#fcd34d", "#f59e0b", "#d97706", "#b45309"},
        new String[]{"低压", "较低", "正常", "较高", "高压"},
        980.0, 1040.0),
    CLOUD("cloud", "云量", "云量分布图", "%",
        new String[]{"#f8fafc", "#cbd5e1", "#94a3b8", "#64748b", "#334155"},
        new String[]{"晴朗", "少云", "多云", "阴天", "密云"},
        0.0, 100.0),
    AIR_QUALITY("air_quality", "空气质量", "AQI分布图", "AQI",
        new String[]{"#4ade80", "#facc15", "#fb923c", "#f87171", "#9f1239"},
        new String[]{"优", "良", "轻度", "中度", "重度"},
        0.0, 300.0),
    VISIBILITY("visibility", "能见度", "能见度分布图", "km",
        new String[]{"#a5f3fc", "#67e8f9", "#22d3ee", "#06b6d4", "#0891b2"},
        new String[]{"极好", "良好", "一般", "较差", "极差"},
        0.0, 30.0);
    
    private final String code;
    private final String name;
    private final String description;
    private final String unit;
    private final String[] colorScale;
    private final String[] levelLabels;
    private final Double minValue;
    private final Double maxValue;
    
    LayerType(String code, String name, String description, String unit, 
              String[] colorScale, String[] levelLabels, Double minValue, Double maxValue) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.colorScale = colorScale;
        this.levelLabels = levelLabels;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public static LayerType fromCode(String code) {
        for (LayerType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return TEMPERATURE;
    }
    
    public String getColorForValue(Double value) {
        if (value == null) return "#94a3b8";
        
        double ratio = (value - minValue) / (maxValue - minValue);
        ratio = Math.max(0, Math.min(1, ratio));
        
        int index = (int) (ratio * (colorScale.length - 1));
        index = Math.max(0, Math.min(colorScale.length - 1, index));
        
        return colorScale[index];
    }
    
    public String getLevelLabel(Double value) {
        if (value == null) return "未知";
        
        double ratio = (value - minValue) / (maxValue - minValue);
        ratio = Math.max(0, Math.min(1, ratio));
        
        int index = (int) (ratio * (levelLabels.length - 1));
        index = Math.max(0, Math.min(levelLabels.length - 1, index));
        
        return levelLabels[index];
    }
}
