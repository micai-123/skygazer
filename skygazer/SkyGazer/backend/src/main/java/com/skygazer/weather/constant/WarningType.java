package com.skygazer.weather.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum WarningType {
    
    TYPHOON(
        "typhoon",
        "台风预警",
        "台风灾害预警",
        Arrays.asList("蓝色", "黄色", "橙色", "红色"),
        Arrays.asList("#3b82f6", "#fbbf24", "#f97316", "#ef4444"),
        1
    ),
    
    RAINSTORM(
        "rainstorm",
        "暴雨预警",
        "暴雨灾害预警",
        Arrays.asList("蓝色", "黄色", "橙色", "红色"),
        Arrays.asList("#3b82f6", "#fbbf24", "#f97316", "#ef4444"),
        1
    ),
    
    HIGH_TEMP(
        "high_temp",
        "高温预警",
        "高温灾害预警",
        Arrays.asList("黄色", "橙色", "红色"),
        Arrays.asList("#fbbf24", "#f97316", "#ef4444"),
        2
    ),
    
    COLD_WAVE(
        "cold_wave",
        "寒潮预警",
        "寒潮灾害预警",
        Arrays.asList("蓝色", "黄色", "橙色", "红色"),
        Arrays.asList("#3b82f6", "#fbbf24", "#f97316", "#ef4444"),
        2
    ),
    
    STRONG_WIND(
        "strong_wind",
        "大风预警",
        "大风灾害预警",
        Arrays.asList("蓝色", "黄色", "橙色", "红色"),
        Arrays.asList("#3b82f6", "#fbbf24", "#f97316", "#ef4444"),
        3
    ),
    
    FOG(
        "fog",
        "大雾预警",
        "大雾灾害预警",
        Arrays.asList("黄色", "橙色", "红色"),
        Arrays.asList("#fbbf24", "#f97316", "#ef4444"),
        3
    ),
    
    HAZE(
        "haze",
        "霾预警",
        "霾灾害预警",
        Arrays.asList("黄色", "橙色", "红色"),
        Arrays.asList("#fbbf24", "#f97316", "#ef4444"),
        3
    ),
    
    SNOW(
        "snow",
        "暴雪预警",
        "暴雪灾害预警",
        Arrays.asList("蓝色", "黄色", "橙色", "红色"),
        Arrays.asList("#3b82f6", "#fbbf24", "#f97316", "#ef4444"),
        2
    ),
    
    THUNDER(
        "thunder",
        "雷电预警",
        "雷电灾害预警",
        Arrays.asList("黄色", "橙色", "红色"),
        Arrays.asList("#fbbf24", "#f97316", "#ef4444"),
        3
    ),
    
    AIR_QUALITY(
        "air_quality",
        "空气污染预警",
        "空气质量预警",
        Arrays.asList("轻度", "中度", "重度", "严重"),
        Arrays.asList("#fbbf24", "#f97316", "#ef4444", "#7c2d12"),
        4
    ),
    
    UV(
        "uv",
        "紫外线预警",
        "紫外线强度预警",
        Arrays.asList("中等", "强", "很强", "极强"),
        Arrays.asList("#fbbf24", "#f97316", "#ef4444", "#7c2d12"),
        4
    ),
    
    FLOOD(
        "flood",
        "洪水预警",
        "洪水灾害预警",
        Arrays.asList("蓝色", "黄色", "橙色", "红色"),
        Arrays.asList("#3b82f6", "#fbbf24", "#f97316", "#ef4444"),
        1
    );
    
    private final String code;
    private final String name;
    private final String description;
    private final java.util.List<String> levels;
    private final java.util.List<String> colors;
    private final int priority;
    
    private static final Map<String, WarningType> CODE_MAP = new HashMap<>();
    
    static {
        for (WarningType type : values()) {
            CODE_MAP.put(type.code, type);
        }
    }
    
    WarningType(String code, String name, String description, 
                java.util.List<String> levels, java.util.List<String> colors, int priority) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.levels = levels;
        this.colors = colors;
        this.priority = priority;
    }
    
    public static WarningType fromCode(String code) {
        return CODE_MAP.get(code.toLowerCase());
    }
    
    public static boolean isValidCode(String code) {
        return CODE_MAP.containsKey(code.toLowerCase());
    }
    
    public String getLevel(int index) {
        if (index >= 0 && index < levels.size()) {
            return levels.get(index);
        }
        return levels.get(0);
    }
    
    public String getColor(int index) {
        if (index >= 0 && index < colors.size()) {
            return colors.get(index);
        }
        return colors.get(0);
    }
    
    public int getLevelIndex(String levelName) {
        return levels.indexOf(levelName);
    }
    
    public Map<String, Object> toMetadataMap() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("code", code);
        metadata.put("name", name);
        metadata.put("description", description);
        metadata.put("levels", levels);
        metadata.put("colors", colors);
        metadata.put("priority", priority);
        return metadata;
    }
}
