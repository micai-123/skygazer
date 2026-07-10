package com.skygazer.weather.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum LifeIndexType {
    
    DRESSING(
        "dressing",
        "穿衣指数",
        "根据气温、湿度、风力综合评估穿衣建议",
        Arrays.asList("寒冷", "冷", "较冷", "舒适", "温暖", "热", "炎热"),
        Arrays.asList("#3b82f6", "#60a5fa", "#93c5fd", "#22c55e", "#fbbf24", "#f97316", "#ef4444"),
        true
    ),
    
    SPORT(
        "sport",
        "运动指数",
        "根据天气条件评估户外运动适宜度",
        Arrays.asList("不宜", "较不宜", "一般", "适宜", "非常适宜"),
        Arrays.asList("#ef4444", "#f97316", "#fbbf24", "#22c55e", "#10b981"),
        true
    ),
    
    UV(
        "uv",
        "紫外线指数",
        "评估紫外线强度及防护建议",
        Arrays.asList("最弱", "弱", "中等", "强", "很强", "极强"),
        Arrays.asList("#22c55e", "#84cc16", "#fbbf24", "#f97316", "#ef4444", "#7c2d12"),
        true
    ),
    
    CAR_WASH(
        "car_wash",
        "洗车指数",
        "根据天气条件评估洗车适宜度",
        Arrays.asList("不宜", "较不宜", "较适宜", "适宜"),
        Arrays.asList("#ef4444", "#f97316", "#22c55e", "#10b981"),
        true
    ),
    
    TRAVEL(
        "travel",
        "旅游指数",
        "根据天气条件评估旅游适宜度",
        Arrays.asList("一般", "较适宜", "适宜", "很适宜"),
        Arrays.asList("#fbbf24", "#84cc16", "#22c55e", "#10b981"),
        true
    ),
    
    ALLERGY(
        "allergy",
        "过敏指数",
        "评估过敏风险等级",
        Arrays.asList("极低", "低", "中等", "高", "极高"),
        Arrays.asList("#22c55e", "#84cc16", "#fbbf24", "#f97316", "#ef4444"),
        true
    ),
    
    AIR_QUALITY(
        "air_quality",
        "空气污染扩散指数",
        "评估空气污染物扩散条件",
        Arrays.asList("差", "较差", "一般", "较好", "好"),
        Arrays.asList("#ef4444", "#f97316", "#fbbf24", "#84cc16", "#22c55e"),
        true
    ),
    
    COMFORT(
        "comfort",
        "舒适度指数",
        "综合评估人体舒适度",
        Arrays.asList("极不舒适", "不舒适", "较不舒适", "一般", "较舒适", "舒适", "非常舒适"),
        Arrays.asList("#7c2d12", "#ef4444", "#f97316", "#fbbf24", "#84cc16", "#22c55e", "#10b981"),
        true
    ),
    
    FISHING(
        "fishing",
        "钓鱼指数",
        "评估钓鱼适宜度",
        Arrays.asList("不宜", "较不宜", "一般", "适宜", "很适宜"),
        Arrays.asList("#ef4444", "#f97316", "#fbbf24", "#22c55e", "#10b981"),
        false
    ),
    
    SUNGLASSES(
        "sunglasses",
        "太阳镜指数",
        "评估是否需要佩戴太阳镜",
        Arrays.asList("不需要", "建议佩戴", "需要佩戴"),
        Arrays.asList("#94a3b8", "#fbbf24", "#f97316"),
        false
    ),
    
    UMBRELLA(
        "umbrella",
        "雨伞指数",
        "评估是否需要携带雨伞",
        Arrays.asList("不需要", "建议携带", "需要携带"),
        Arrays.asList("#94a3b8", "#60a5fa", "#3b82f6"),
        true
    ),
    
    DRYING(
        "drying",
        "晾晒指数",
        "评估晾晒衣物适宜度",
        Arrays.asList("不宜", "较不宜", "一般", "适宜", "极适宜"),
        Arrays.asList("#ef4444", "#f97316", "#fbbf24", "#22c55e", "#10b981"),
        true
    );
    
    private final String code;
    private final String name;
    private final String description;
    private final java.util.List<String> levels;
    private final java.util.List<String> colors;
    private final boolean isDefault;
    
    private static final Map<String, LifeIndexType> CODE_MAP = new HashMap<>();
    
    static {
        for (LifeIndexType type : values()) {
            CODE_MAP.put(type.code, type);
        }
    }
    
    LifeIndexType(String code, String name, String description, 
                  java.util.List<String> levels, java.util.List<String> colors, boolean isDefault) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.levels = levels;
        this.colors = colors;
        this.isDefault = isDefault;
    }
    
    public static LifeIndexType fromCode(String code) {
        return CODE_MAP.get(code.toLowerCase());
    }
    
    public static boolean isValidCode(String code) {
        return CODE_MAP.containsKey(code.toLowerCase());
    }
    
    public static java.util.List<LifeIndexType> getDefaultIndices() {
        return Arrays.asList(DRESSING, SPORT, UV, CAR_WASH, TRAVEL, COMFORT, UMBRELLA);
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
    
    public int calculateLevelIndex(double normalizedValue) {
        int index = (int) Math.floor(normalizedValue * levels.size());
        return Math.max(0, Math.min(levels.size() - 1, index));
    }
    
    public Map<String, Object> toMetadataMap() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("code", code);
        metadata.put("name", name);
        metadata.put("description", description);
        metadata.put("levels", levels);
        metadata.put("colors", colors);
        return metadata;
    }
}
