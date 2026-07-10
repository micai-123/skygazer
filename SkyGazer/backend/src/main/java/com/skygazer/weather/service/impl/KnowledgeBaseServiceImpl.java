package com.skygazer.weather.service.impl;

import com.skygazer.weather.entity.VectorKnowledge;
import com.skygazer.weather.repository.VectorKnowledgeRepository;
import com.skygazer.weather.service.KnowledgeBaseService;
import com.skygazer.weather.service.VectorStoreService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
    
    private final VectorStoreService vectorStoreService;
    private final VectorKnowledgeRepository vectorKnowledgeRepository;
    
    private static final String CATEGORY_WEATHER_WARNING = "weather_warning";
    private static final String CATEGORY_LIFESTYLE_RULE = "lifestyle_rule";
    private static final String CATEGORY_ACTIVITY_ADVICE = "activity_advice";
    private static final String CATEGORY_WEATHER_PHENOMENON = "weather_phenomenon";
    
    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void initializeKnowledgeBase() {
        long count = vectorStoreService.count();
        if (count == 0) {
            log.info("知识库为空，开始初始化...");
            addWeatherWarningKnowledge();
            addLifestyleRuleKnowledge();
            addActivityAdviceKnowledge();
            addWeatherPhenomenonKnowledge();
            log.info("知识库初始化完成，共 {} 条记录", vectorStoreService.count());
        } else {
            log.info("知识库已存在 {} 条记录", count);
        }
    }
    
    @Override
    public void addWeatherWarningKnowledge() {
        List<VectorKnowledge> warnings = new ArrayList<>();
        
        warnings.add(createKnowledge(
            "暴雨预警信号分为蓝色、黄色、橙色、红色四个等级。蓝色预警表示12小时内降雨量将达50毫米以上；黄色预警表示6小时内降雨量将达50毫米以上；橙色预警表示3小时内降雨量将达50毫米以上；红色预警表示降雨量可能达到或超过100毫米，需立即采取避险措施。遇到暴雨预警时应避免外出，远离低洼地带和河道。",
            CATEGORY_WEATHER_WARNING, "暴雨预警等级与应对"
        ));
        
        warnings.add(createKnowledge(
            "台风预警信号分为蓝色、黄色、橙色、红色四个等级。蓝色预警表示24小时内可能受热带气旋影响；黄色预警表示12小时内可能受热带气旋影响；橙色预警表示6小时内可能受热带气旋影响；红色预警表示台风可能正面袭击，需做好防台准备。台风来临前应关好门窗，收好阳台物品，储备食物和水。",
            CATEGORY_WEATHER_WARNING, "台风预警等级与应对"
        ));
        
        warnings.add(createKnowledge(
            "高温预警信号分为黄色、橙色、红色三个等级。黄色预警表示连续三天日最高气温将在35℃以上；橙色预警表示24小时内最高气温将升至37℃以上；红色预警表示气温将达到40℃以上，需注意防暑降温。高温天气应避免在中午时分进行户外活动，多喝水，注意防暑。",
            CATEGORY_WEATHER_WARNING, "高温预警等级与应对"
        ));
        
        warnings.add(createKnowledge(
            "大风预警信号分为蓝色、黄色、橙色、红色四个等级。蓝色预警表示24小时内可能受大风影响，平均风力达6级以上；黄色预警表示12小时内可能受大风影响；橙色预警表示6小时内可能受大风影响；红色预警表示阵风可达12级以上，需停止户外活动。大风天气应远离广告牌、临时搭建物等。",
            CATEGORY_WEATHER_WARNING, "大风预警等级与应对"
        ));
        
        warnings.add(createKnowledge(
            "雷电预警信号分为黄色、橙色、红色三个等级。黄色预警表示6小时内可能发生雷电活动；橙色预警表示2小时内发生雷电活动的可能性很大；红色预警表示2小时内发生雷电活动的可能性非常大。雷电天气应避免在空旷地带停留，不要在大树下避雨，关闭电器设备。",
            CATEGORY_WEATHER_WARNING, "雷电预警等级与应对"
        ));
        
        warnings.add(createKnowledge(
            "寒潮预警信号分为蓝色、黄色、橙色、红色四个等级。蓝色预警表示48小时内最低气温将下降8℃以上；黄色预警表示24小时内最低气温将下降10℃以上；橙色预警表示24小时内最低气温将下降12℃以上；红色预警表示24小时内最低气温将下降16℃以上。寒潮来袭时应注意保暖，预防感冒。",
            CATEGORY_WEATHER_WARNING, "寒潮预警等级与应对"
        ));
        
        vectorStoreService.addDocuments(warnings);
        log.info("添加天气预警知识: {} 条", warnings.size());
    }
    
    @Override
    public void addLifestyleRuleKnowledge() {
        List<VectorKnowledge> rules = new ArrayList<>();
        
        rules.add(createKnowledge(
            "紫外线指数分为5个等级：1-2为最弱，不需要防护；3-4为弱，适当防护；5-6为中等，外出需戴帽子、太阳镜；7-9为强，需涂抹SPF30以上防晒霜，避免在中午外出；10+为极强，尽量避免外出，必须外出时应全副武装。紫外线强时容易晒伤，长期暴露可能增加皮肤癌风险。",
            CATEGORY_LIFESTYLE_RULE, "紫外线指数解读"
        ));
        
        rules.add(createKnowledge(
            "空气质量指数AQI分为6个等级：0-50为优，空气质量令人满意；51-100为良，空气质量可接受；101-150为轻度污染，敏感人群症状加剧；151-200为中度污染，进一步加剧敏感人群症状；201-300为重度污染，健康人群出现症状；300+为严重污染，健康警报。轻度污染以上敏感人群应减少户外活动，重度污染时所有人应减少户外活动。",
            CATEGORY_LIFESTYLE_RULE, "空气质量指数解读"
        ));
        
        rules.add(createKnowledge(
            "穿衣指数根据温度划分：温度>28℃时穿夏装，如短袖、短裤、裙子；温度21-27℃时穿春秋装，如长袖衬衫、薄外套；温度11-20℃时穿初冬装，如毛衣、夹克；温度<10℃时穿冬装，如棉衣、羽绒服。同时还要考虑湿度、风速等因素，体感温度与实际温度可能有差异。",
            CATEGORY_LIFESTYLE_RULE, "穿衣指数建议"
        ));
        
        rules.add(createKnowledge(
            "洗车指数建议：未来24-48小时无雨、无雪、无沙尘天气适宜洗车；有雨、雪、沙尘天气不宜洗车。空气质量差时也不建议洗车，因为空气中的污染物会附着在车身上。洗车后最好能停在车库或阴凉处，避免阳光直射导致水渍残留。",
            CATEGORY_LIFESTYLE_RULE, "洗车指数建议"
        ));
        
        rules.add(createKnowledge(
            "运动指数建议：温度15-25℃、湿度40-70%、风速<4级、空气质量优或良时最适宜户外运动。温度过高或过低、湿度太大、空气质量差时不宜户外运动。夏季应避开中午时分，选择清晨或傍晚；冬季应选择气温较高的时段，注意保暖。",
            CATEGORY_LIFESTYLE_RULE, "运动指数建议"
        ));
        
        rules.add(createKnowledge(
            "感冒指数根据温度变化、湿度、风力等因素综合评估。指数1级为少发，感冒发生较少；2级为较易发，体质较弱者应注意防护；3级为易发，应注意保暖；4级为极易发，需特别注意防护。季节交替、气温骤降时感冒指数较高，应加强锻炼，注意饮食，保证睡眠。",
            CATEGORY_LIFESTYLE_RULE, "感冒指数解读"
        ));
        
        vectorStoreService.addDocuments(rules);
        log.info("添加生活指数知识: {} 条", rules.size());
    }
    
    @Override
    public void addActivityAdviceKnowledge() {
        List<VectorKnowledge> advices = new ArrayList<>();
        
        advices.add(createKnowledge(
            "露营活动建议选择晴朗无风的天气，温度在15-25℃为宜，湿度40-70%，空气质量优或良。避免在雷雨、大风天气露营，山区要注意防范山洪和滑坡。出发前查看天气预报，准备防雨、保暖装备。选择正规营地，注意用火安全，离开时带走垃圾。",
            CATEGORY_ACTIVITY_ADVICE, "露营活动建议"
        ));
        
        advices.add(createKnowledge(
            "跑步活动建议选择温度15-20℃、湿度40-60%的天气，空气质量指数应低于100，紫外线不宜过强。夏季应避开中午时分，选择清晨或傍晚；冬季应选择气温较高的时段，注意保暖。跑步前做好热身，跑步后做好拉伸，及时补充水分。雾霾天应选择室内运动。",
            CATEGORY_ACTIVITY_ADVICE, "跑步活动建议"
        ));
        
        advices.add(createKnowledge(
            "登山活动建议选择晴朗天气，避免雨天或大风天，温度10-25℃为宜。出发前需查看山区天气预报，注意防范山洪、滑坡、落石等自然灾害。携带足够的水和食物，穿着合适的登山鞋，准备防晒、防雨装备。不要单独登山，告知家人行程，保持通讯畅通。",
            CATEGORY_ACTIVITY_ADVICE, "登山活动建议"
        ));
        
        advices.add(createKnowledge(
            "户外婚礼建议选择温度18-25℃、无雨无风的天气，湿度40-70%为宜。夏季应避开中午时分，选择傍晚举行；春秋季要注意温差，准备外套；冬季户外婚礼需特别注意保暖。提前关注天气预报，准备备用方案。选择有遮阳或遮雨设施的场地更保险。",
            CATEGORY_ACTIVITY_ADVICE, "户外婚礼建议"
        ));
        
        advices.add(createKnowledge(
            "骑行活动建议选择温度15-25℃、无雨、微风或和风的天气，空气质量指数应低于100。夏季应避开中午时分，注意防晒；冬季要注意保暖，穿戴防风装备。出发前检查车辆状况，携带修车工具、水和食物。遵守交通规则，注意行车安全。",
            CATEGORY_ACTIVITY_ADVICE, "骑行活动建议"
        ));
        
        advices.add(createKnowledge(
            "钓鱼活动建议选择阴天或多云天气，温度15-25℃为宜，气压稳定时鱼儿活跃。雨前气压低，鱼儿会浮头，是钓鱼的好时机；雨后水质变浑，鱼儿不易上钩。夏季早晚是钓鱼的黄金时段，冬季应选择中午时分。注意安全，不要在危险水域钓鱼。",
            CATEGORY_ACTIVITY_ADVICE, "钓鱼活动建议"
        ));
        
        advices.add(createKnowledge(
            "野餐活动建议选择晴朗或多云天气，温度20-28℃为宜，无雨无大风。选择有树荫的地方，避免阳光直射。准备防虫用品，食物要新鲜，注意食品卫生。离开时带走垃圾，保护环境。夏季要注意食物保鲜，防止变质。",
            CATEGORY_ACTIVITY_ADVICE, "野餐活动建议"
        ));
        
        advices.add(createKnowledge(
            "滑雪活动建议选择温度-10℃到-5℃的天气，雪质要好，无大风。注意保暖，穿戴专业的滑雪装备，做好防护措施。初学者应在教练指导下学习，选择初级雪道。注意滑雪安全，遵守雪场规定，不要到未开放的雪道滑雪。",
            CATEGORY_ACTIVITY_ADVICE, "滑雪活动建议"
        ));
        
        vectorStoreService.addDocuments(advices);
        log.info("添加活动建议知识: {} 条", advices.size());
    }
    
    @Override
    public void addWeatherPhenomenonKnowledge() {
        List<VectorKnowledge> phenomena = new ArrayList<>();
        
        phenomena.add(createKnowledge(
            "云的类型主要分为高云、中云、低云三大类。高云包括卷云、卷积云、卷层云，高度在6000米以上，由冰晶组成，预示天气变化；中云包括高积云、高层云，高度在2000-6000米，可能带来降水；低云包括层云、层积云、雨层云、积云、积雨云，高度在2000米以下，与降水关系密切。",
            CATEGORY_WEATHER_PHENOMENON, "云的类型与天气"
        ));
        
        phenomena.add(createKnowledge(
            "积雨云是雷雨天气的主要标志，云体高大，呈塔状，顶部常呈砧状。积雨云出现时常伴有雷电、暴雨、大风、冰雹等强对流天气。看到积雨云发展时，应尽快寻找安全场所躲避，不要在空旷地带、大树下、水边停留。",
            CATEGORY_WEATHER_PHENOMENON, "积雨云与雷雨"
        ));
        
        phenomena.add(createKnowledge(
            "彩虹是阳光照射到空气中的水滴，发生折射和反射形成的。彩虹通常出现在雨后，太阳在观察者背后时。彩虹的出现说明空气中水汽较多，天气可能转晴。早晨出现彩虹预示可能有雨，傍晚出现彩虹预示天气可能转晴。",
            CATEGORY_WEATHER_PHENOMENON, "彩虹的形成与天气"
        ));
        
        phenomena.add(createKnowledge(
            "雾是近地面空气中水汽凝结形成的，能见度低于1公里。雾的形成需要充足的水汽、较低的温度和稳定的大气。大雾天气能见度低，对交通影响大，应减速慢行，开启雾灯。雾天空气质量通常较差，敏感人群应减少户外活动。",
            CATEGORY_WEATHER_PHENOMENON, "雾的形成与影响"
        ));
        
        phenomena.add(createKnowledge(
            "霾是空气中悬浮的大量灰尘、硫酸盐、硝酸盐等颗粒物形成的，能见度低于10公里。霾与雾的区别在于湿度，湿度大于90%为雾，小于80%为霾，80-90%之间为雾霾混合物。霾天气空气质量差，应减少户外活动，外出佩戴N95口罩。",
            CATEGORY_WEATHER_PHENOMENON, "霾的形成与防护"
        ));
        
        phenomena.add(createKnowledge(
            "霜是地面附近的水汽直接凝华在地面或地面物体上的白色冰晶。霜的形成需要晴朗无风的夜晚，地面温度降到0℃以下。有霜的早晨通常天气晴朗，但气温较低，要注意保暖。霜对农作物有影响，农业上要注意防霜冻。",
            CATEGORY_WEATHER_PHENOMENON, "霜的形成与天气"
        ));
        
        phenomena.add(createKnowledge(
            "台风是热带气旋的一种，中心附近最大风力达12级以上。台风路径受副热带高压影响，通常向西北方向移动。台风带来的主要灾害包括大风、暴雨、风暴潮。台风来临前要做好防台准备，关好门窗，收好阳台物品，储备食物和水。",
            CATEGORY_WEATHER_PHENOMENON, "台风的形成与影响"
        ));
        
        phenomena.add(createKnowledge(
            "梅雨是东亚地区特有的天气现象，每年6-7月份，长江中下游地区会出现持续阴雨天气。梅雨期间湿度大、日照少、气温高，容易导致物品发霉。梅雨季节要注意防潮防霉，保持室内通风，衣物要及时晾干。",
            CATEGORY_WEATHER_PHENOMENON, "梅雨天气特点"
        ));
        
        vectorStoreService.addDocuments(phenomena);
        log.info("添加天气现象知识: {} 条", phenomena.size());
    }
    
    @Override
    public List<VectorKnowledge> searchKnowledge(String query, int limit) {
        return vectorStoreService.similaritySearch(query, limit);
    }
    
    @Override
    public List<VectorKnowledge> searchKnowledgeByCategory(String query, String category, int limit) {
        return vectorStoreService.similaritySearchByCategory(query, category, limit);
    }
    
    @Override
    public void refreshKnowledgeBase() {
        log.info("开始刷新知识库...");
        vectorStoreService.deleteAll();
        addWeatherWarningKnowledge();
        addLifestyleRuleKnowledge();
        addActivityAdviceKnowledge();
        addWeatherPhenomenonKnowledge();
        log.info("知识库刷新完成，共 {} 条记录", vectorStoreService.count());
    }
    
    private VectorKnowledge createKnowledge(String content, String category, String title) {
        return VectorKnowledge.builder()
            .content(content)
            .category(category)
            .title(title)
            .build();
    }
}
