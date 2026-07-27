package com.skygazer.weather.util;

import com.skygazer.weather.dto.geo.CityLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线静态城市库（替换原和风天气 Geo API）。
 * <p>
 * 提供一份内置的中外主要城市坐标数据集，供城市检索 / 经纬度解析使用，
 * 不再依赖任何外部地理 API。如需扩充城市，在此列表追加即可。
 * </p>
 */
public final class StaticCityProvider {

    private StaticCityProvider() {
    }

    private static final List<CityLocation> CITIES = new ArrayList<>();

    static {
        // 国内主要城市：name, 省/直辖市, 纬度, 经度
        add("北京", "北京", "中国", 39.9042, 116.4074);
        add("上海", "上海", "中国", 31.2304, 121.4737);
        add("天津", "天津", "中国", 39.3434, 117.3616);
        add("重庆", "重庆", "中国", 29.4316, 106.9123);
        add("广州", "广东", "中国", 23.1291, 113.2644);
        add("深圳", "广东", "中国", 22.5431, 114.0579);
        add("东莞", "广东", "中国", 23.0207, 113.7518);
        add("杭州", "浙江", "中国", 30.2741, 120.1551);
        add("宁波", "浙江", "中国", 29.8683, 121.5440);
        add("南京", "江苏", "中国", 32.0603, 118.7969);
        add("苏州", "江苏", "中国", 31.2989, 120.5853);
        add("无锡", "江苏", "中国", 31.4900, 120.3100);
        add("成都", "四川", "中国", 30.5728, 104.0668);
        add("武汉", "湖北", "中国", 30.5928, 114.3055);
        add("西安", "陕西", "中国", 34.3416, 108.9398);
        add("长沙", "湖南", "中国", 28.2282, 112.9388);
        add("郑州", "河南", "中国", 34.7466, 113.6254);
        add("济南", "山东", "中国", 36.6512, 116.9972);
        add("青岛", "山东", "中国", 36.0671, 120.3826);
        add("沈阳", "辽宁", "中国", 41.8057, 123.4315);
        add("大连", "辽宁", "中国", 38.9140, 121.6147);
        add("哈尔滨", "黑龙江", "中国", 45.8038, 126.5350);
        add("长春", "吉林", "中国", 43.8171, 125.3235);
        add("石家庄", "河北", "中国", 38.0428, 114.5149);
        add("太原", "山西", "中国", 37.8706, 112.5489);
        add("合肥", "安徽", "中国", 31.8206, 117.2272);
        add("福州", "福建", "中国", 26.0745, 119.2965);
        add("厦门", "福建", "中国", 24.4798, 118.0894);
        add("南昌", "江西", "中国", 28.6820, 115.8579);
        add("昆明", "云南", "中国", 25.0389, 102.7183);
        add("贵阳", "贵州", "中国", 26.6470, 106.6302);
        add("南宁", "广西", "中国", 22.8170, 108.3665);
        add("海口", "海南", "中国", 20.0440, 110.1999);
        add("兰州", "甘肃", "中国", 36.0611, 103.8343);
        add("西宁", "青海", "中国", 36.6171, 101.7782);
        add("银川", "宁夏", "中国", 38.4872, 106.2309);
        add("乌鲁木齐", "新疆", "中国", 43.8256, 87.6168);
        add("呼和浩特", "内蒙古", "中国", 40.8414, 111.7519);
        add("拉萨", "西藏", "中国", 29.6500, 91.1000);
        add("香港", "香港", "中国", 22.3193, 114.1694);
        add("澳门", "澳门", "中国", 22.1987, 113.5439);
        add("台北", "台湾", "中国", 25.0330, 121.5654);
        // 国际城市
        add("纽约", "美国", "美国", 40.7128, -74.0060);
        add("伦敦", "英国", "英国", 51.5074, -0.1278);
        add("东京", "日本", "日本", 35.6762, 139.6503);
        add("巴黎", "法国", "法国", 48.8566, 2.3522);
        add("新加坡", "新加坡", "新加坡", 1.3521, 103.8198);
        add("悉尼", "澳大利亚", "澳大利亚", -33.8688, 151.2093);
        add("迪拜", "阿联酋", "阿联酋", 25.2048, 55.2708);
        add("首尔", "韩国", "韩国", 37.5665, 126.9780);
        add("曼谷", "泰国", "泰国", 13.7563, 100.5018);
        add("莫斯科", "俄罗斯", "俄罗斯", 55.7558, 37.6173);
        add("洛杉矶", "美国", "美国", 34.0522, -118.2437);
        add("旧金山", "美国", "美国", 37.7749, -122.4194);
        add("多伦多", "加拿大", "加拿大", 43.6532, -79.3832);
        add("柏林", "德国", "德国", 52.5200, 13.4050);
        add("罗马", "意大利", "意大利", 41.9028, 12.4964);
    }

    private static void add(String name, String adm1, String country, double lat, double lon) {
        CITIES.add(CityLocation.builder()
                .id(name)
                .name(name)
                .lat(String.valueOf(lat))
                .lon(String.valueOf(lon))
                .adm1(adm1)
                .country(country)
                .type("city")
                .build());
    }

    public static List<CityLocation> getAllCities() {
        return new ArrayList<>(CITIES);
    }

    /** 按名称子串匹配（不区分大小写） */
    public static List<CityLocation> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }
        String k = keyword.toLowerCase();
        List<CityLocation> result = new ArrayList<>();
        for (CityLocation c : CITIES) {
            if (c.getName() != null && c.getName().toLowerCase().contains(k)) {
                result.add(c);
            }
        }
        return result;
    }

    /** 按省份/直辖市筛选 */
    public static List<CityLocation> searchByProvince(String province) {
        if (province == null || province.isBlank()) {
            return new ArrayList<>();
        }
        List<CityLocation> result = new ArrayList<>();
        for (CityLocation c : CITIES) {
            if (province.equals(c.getAdm1()) || (c.getName() != null && c.getName().contains(province))) {
                result.add(c);
            }
        }
        return result;
    }

    public static CityLocation getByName(String name) {
        if (name == null) {
            return null;
        }
        for (CityLocation c : CITIES) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }
}
