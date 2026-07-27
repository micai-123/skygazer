package com.skygazer.weather.constant;

import java.util.Map;

/**
 * 中文城市名 → 英文名 映射表。
 * MetaWeather 的检索接口仅支持英文（拉丁）城市名，无法直接用中文检索，
 * 因此这里维护一份主要城市的中→英映射；命中则返回英文名，未命中且输入含中文则返回 null（上层回退模拟），
 * 输入本身为英文/拼音时原样返回。
 */
public final class CityNameMapping {

    private static final Map<String, String> MAP = Map.ofEntries(
            // 直辖市 / 省会
            Map.entry("北京", "Beijing"),
            Map.entry("上海", "Shanghai"),
            Map.entry("天津", "Tianjin"),
            Map.entry("重庆", "Chongqing"),
            Map.entry("广州", "Guangzhou"),
            Map.entry("深圳", "Shenzhen"),
            Map.entry("杭州", "Hangzhou"),
            Map.entry("南京", "Nanjing"),
            Map.entry("成都", "Chengdu"),
            Map.entry("武汉", "Wuhan"),
            Map.entry("西安", "Xian"),
            Map.entry("苏州", "Suzhou"),
            Map.entry("郑州", "Zhengzhou"),
            Map.entry("长沙", "Changsha"),
            Map.entry("青岛", "Qingdao"),
            Map.entry("大连", "Dalian"),
            Map.entry("宁波", "Ningbo"),
            Map.entry("厦门", "Xiamen"),
            Map.entry("无锡", "Wuxi"),
            Map.entry("合肥", "Hefei"),
            Map.entry("昆明", "Kunming"),
            Map.entry("哈尔滨", "Harbin"),
            Map.entry("济南", "Jinan"),
            Map.entry("福州", "Fuzhou"),
            Map.entry("沈阳", "Shenyang"),
            Map.entry("长春", "Changchun"),
            Map.entry("南昌", "Nanchang"),
            Map.entry("贵阳", "Guiyang"),
            Map.entry("太原", "Taiyuan"),
            Map.entry("石家庄", "Shijiazhuang"),
            Map.entry("兰州", "Lanzhou"),
            Map.entry("南宁", "Nanning"),
            Map.entry("海口", "Haikou"),
            Map.entry("银川", "Yinchuan"),
            Map.entry("西宁", "Xining"),
            Map.entry("乌鲁木齐", "Urumqi"),
            Map.entry("拉萨", "Lhasa"),
            Map.entry("呼和浩特", "Hohhot"),
            Map.entry("香港", "Hong Kong"),
            Map.entry("澳门", "Macau"),
            Map.entry("台北", "Taipei"),
            // 国际城市
            Map.entry("纽约", "New York"),
            Map.entry("伦敦", "London"),
            Map.entry("东京", "Tokyo"),
            Map.entry("巴黎", "Paris"),
            Map.entry("新加坡", "Singapore"),
            Map.entry("悉尼", "Sydney"),
            Map.entry("迪拜", "Dubai"),
            Map.entry("首尔", "Seoul"),
            Map.entry("曼谷", "Bangkok"),
            Map.entry("莫斯科", "Moscow"),
            Map.entry("洛杉矶", "Los Angeles"),
            Map.entry("旧金山", "San Francisco"),
            Map.entry("多伦多", "Toronto"),
            Map.entry("柏林", "Berlin"),
            Map.entry("罗马", "Rome")
    );

    private CityNameMapping() {
    }

    /**
     * 将用户输入的城市名转换为 MetaWeather 可检索的英文名。
     *
     * @return 英文名；若输入为中文且未配置映射则返回 null；英文/拼音原样返回
     */
    public static String toEnglish(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        String mapped = MAP.get(location.trim());
        if (mapped != null) {
            return mapped;
        }
        // 含中文字符且未命中映射 → MetaWeather 无法检索
        if (location.matches(".*[\\u4e00-\\u9fa5].*")) {
            return null;
        }
        // 已是英文/拼音，直接用作检索词
        return location.trim();
    }

    public static boolean isSupported(String location) {
        return toEnglish(location) != null;
    }
}
