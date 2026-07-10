# 和风天气API配置指南

## 📋 配置概述

和风天气API域名已成功配置到项目中，配置详情如下：

### 配置文件位置
- **环境变量文件**: `d:\jishe\jishe02\config\.env`
- **Spring配置文件**: `d:\jishe\jishe02\backend\src\main\resources\application.yml`
- **Docker配置文件**: `d:\jishe\jishe02\docker\docker-compose.yml`

### API配置信息
- **API域名**: `https://mv33jqaeug.re.qweatherapi.com/v7`
- **API密钥**: 需要用户自行申请并配置
- **支持接口**: 
  - 实时天气: `/weather/now`
  - 空气质量: `/air/now`
  - 24小时预报: `/weather/24h`
  - 7天预报: `/weather/7d`

## 🔧 配置步骤

### 步骤1：获取API密钥

1. 访问和风天气开发者平台：https://dev.qweather.com/
2. 注册并登录账号
3. 创建应用（选择免费订阅或付费订阅）
4. 获取API Key（KEY）

### 步骤2：配置环境变量

编辑 `d:\jishe\jishe02\config\.env` 文件：

```bash
# QWeather API Configuration (和风天气API)
QWEATHER_API_KEY=你的API密钥
QWEATHER_BASE_URL=https://mv33jqaeug.re.qweatherapi.com/v7
```

**重要提示**：
- 将 `你的API密钥` 替换为实际的API Key
- API域名已配置为：`https://mv33jqaeug.re.qweatherapi.com/v7`
- 不要修改 `QWEATHER_BASE_URL` 的值

### 步骤3：验证配置

运行验证脚本确认配置正确：

```powershell
cd d:\jishe\jishe02
python verify_qweather_api.py
```

## ✅ 验证测试

### 方法一：Python验证脚本（推荐）

```powershell
python d:\jishe\jishe02\verify_qweather_api.py
```

**预期输出**（成功）：
```
🚀 开始验证和风天气API配置...
📁 配置文件路径: d:\jishe\jishe02\config\.env

📋 配置信息:
   API密钥: 已配置
   API域名: https://mv33jqaeug.re.qweatherapi.com/v7

============================================================
🌤️  和风天气API连接测试
============================================================
✓ API密钥已配置: xxxxxxxx...xxxxxxxx
✓ API基础URL: https://mv33jqaeug.re.qweatherapi.com/v7

📡 发送测试请求到: https://mv33jqaeug.re.qweatherapi.com/v7/weather/now
📍 测试城市: 北京 (Location ID: 101010100)

📊 响应状态码: 200

✅ API连接成功!

🌤️  当前天气数据:
   城市: 北京
   天气: 晴
   温度: 25°C
   体感温度: 26°C
   湿度: 45%
   风向: 东南风
   风力: 3级
   风速: 15 km/h
   能见度: 10 km
   气压: 1015 hPa
   降水量: 0.0 mm
   数据更新时间: 2026-03-29T14:00+08:00

============================================================
🎉 和风天气API配置验证成功!
============================================================

✅ 所有验证通过,API配置正确!
```

### 方法二：启动后端服务测试

```powershell
cd d:\jishe\jishe02\backend
mvn spring-boot:run
```

测试API端点：
```bash
curl http://localhost:8080/api/weather/current?location=北京
```

## 📊 支持的城市

系统已内置以下城市的Location ID映射：

| 城市 | Location ID |
|------|-------------|
| 北京 | 101010100 |
| 上海 | 101020100 |
| 广州 | 101280101 |
| 深圳 | 101280601 |
| 杭州 | 101210101 |
| 成都 | 101270101 |
| 武汉 | 101200101 |
| 西安 | 101110101 |
| 南京 | 101190101 |
| 重庆 | 101040100 |

其他城市可直接使用城市名称或Location ID。

## 🔍 故障排查

### 问题1：API密钥无效

**症状**：验证脚本返回 `code: 401`

**解决方案**：
1. 检查API密钥是否正确复制
2. 确认API密钥未过期
3. 验证API密钥权限是否包含所需服务

### 问题2：访问次数超限

**症状**：验证脚本返回 `code: 402`

**解决方案**：
1. 检查API订阅计划的访问次数限制
2. 升级订阅计划或等待配额重置
3. 实现缓存机制减少API调用

### 问题3：请求频率过高

**症状**：验证脚本返回 `code: 429`

**解决方案**：
1. 降低API调用频率
2. 实现请求限流机制
3. 使用Redis缓存天气数据

### 问题4：网络连接失败

**症状**：连接超时或无法访问API

**解决方案**：
1. 检查网络连接
2. 确认防火墙未阻止访问
3. 验证API域名配置正确

## 🔒 安全建议

1. **密钥保护**：
   - 不要将API密钥提交到代码仓库
   - 定期更换API密钥
   - 为不同环境使用不同的密钥

2. **访问控制**：
   - 设置IP白名单（在和风天气控制台）
   - 监控API使用情况
   - 及时发现异常访问

3. **数据缓存**：
   - 使用Redis缓存天气数据
   - 设置合理的缓存过期时间
   - 减少不必要的API调用

## 📝 API调用示例

### 实时天气查询

```java
// Java代码示例
String url = baseUrl + "/weather/now?location=101010100&key=" + apiKey;
Map<String, Object> response = webClient.get()
    .uri(url)
    .retrieve()
    .bodyToMono(Map.class)
    .block();
```

### 空气质量查询

```java
// Java代码示例
String url = baseUrl + "/air/now?location=101010100&key=" + apiKey;
Map<String, Object> response = webClient.get()
    .uri(url)
    .retrieve()
    .bodyToMono(Map.class)
    .block();
```

## 📚 相关文档

- [和风天气API官方文档](https://dev.qweather.com/docs/api/)
- [API响应码说明](https://dev.qweather.com/docs/resource/status-code/)
- [城市Location ID查询](https://github.com/qwd/LocationList)

## 🎯 下一步操作

1. **配置API密钥**：在 `.env` 文件中设置 `QWEATHER_API_KEY`
2. **运行验证脚本**：确认API连接正常
3. **启动后端服务**：测试完整功能
4. **前端集成测试**：验证天气数据显示

## 📞 技术支持

如遇到问题，请检查：
1. 本文档的故障排查章节
2. 后端服务日志：`d:\jishe\jishe02\backend\logs\`
3. 和风天气API官方文档：https://dev.qweather.com/docs/

---

**配置完成日期**: 2026年3月29日  
**API域名**: https://mv33jqaeug.re.qweatherapi.com/v7  
**配置状态**: ✅ 域名已配置，等待API密钥
