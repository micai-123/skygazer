---
name: weather-image-module-integration
overview: 将已有的 Python 天气图片分类模型 API（weather-model-api）与后端 Java 服务和前端 Vue 界面集成，实现"上传天气图片 → 返回四类天气识别结果"的完整功能链路，并融入天象智囊（AIAssistantView）交互体验中。
todos:
  - id: backend-config-properties
    content: 后端：新增 weather-image 配置属性类和 application.yml 配置，更新 WebClientConfig 和 ErrorCode
    status: pending
  - id: backend-service
    content: 后端：创建 WeatherImageService 和 WeatherImageController，实现图片校验、Python API 调用和中文映射
    status: pending
    dependencies:
      - backend-config-properties
  - id: backend-cleanup
    content: 后端：更新 AiController.analyzeImage() 占位实现，重定向到新端点
    status: pending
    dependencies:
      - backend-service
  - id: frontend-api-store
    content: 前端：新增 weatherImageApi 方法和 chatStore.sendImageMessage() 图片消息流程
    status: pending
  - id: frontend-image-card
    content: 前端：使用 [skill:impeccable] 创建 WeatherImageCard.vue 识别结果卡片组件（含概率条形图和天气图标）
    status: pending
  - id: frontend-upload-ui
    content: 前端：改造 AIAssistantView.vue 增加图片上传按钮、拖拽区域和快捷卡片，更新 AgentMessage.vue 支持图片消息渲染
    status: pending
    dependencies:
      - frontend-api-store
      - frontend-image-card
---

## 用户需求

用户希望创建一个适配本项目的"天气智囊"图片识别模块，允许在"天象智囊"页面上传天气图片，系统调用已有的 Python 天气分类模型（ResNet-18，四分类：晴天/阴天/雨天/雪天），并将识别结果（天气类型、置信度、四类概率分布）以智能体消息形式展示在聊天界面中。

## 产品概述

在现有"天象智囊"（`/ai-assistant`）聊天页面中增加天气图片上传识别能力，形成"图片上传 → 模型推理 → 结果展示"的完整交互闭环。用户通过点击或拖拽上传天气图片后，图片以缩略图形式展示在聊天消息流中，系统自动调用 Python 模型 API 进行分析，最终以卡片形式展示预测的天气类型、置信度和概率分布，并可选由 AI 生成进一步天气建议。

## 核心功能

- **图片上传入口**：在聊天输入区添加图片上传按钮，支持点击选择文件和拖拽上传，格式校验（jpg/png/bmp/webp）
- **图片消息展示**：上传的图片在聊天流中作为用户消息展示，显示缩略图预览
- **模型推理**：后端接收图片文件，转发至 Python 模型 API 进行分类推理
- **结果卡片展示**：以智能体消息卡片展示识别结果，包含预测天气类型（带中文映射和图标）、置信度百分比、四类概率分布条形图
- **工作步骤可视化**：展示"图片上传 → 模型推理 → 结果分析"三步工作过程（复用 AgentSteps 组件）
- **AI 天气建议**：基于识别结果，可选由 AI 智能体生成对应天气类型的出行/活动建议

## 技术栈选择

- **后端**：Spring Boot 3.2.5（现有）+ WebClient（与现有 WebClientConfig 模式一致）
- **前端**：Vue 3 + Pinia（现有）+ FormData multipart 上传
- **图片传输**：FormData multipart/form-data（而非 base64），减少编码开销
- **跨服务通信**：WebClient 调用 Python Flask API（HTTP，JSON 响应）

## 实施方案

### 整体策略

在后端新建独立的 `WeatherImageController` 和 `WeatherImageService`，负责接收前端上传的图片并转发至 Python 模型 API。前端在 `AIAssistantView.vue` 聊天输入区增加图片上传按钮，通过 `chatStore.sendImageMessage()` 新方法驱动上传+展示流程。识别结果通过新增的 `WeatherImageCard.vue` 组件以卡片形式渲染在 AgentMessage 中，复用现有 AgentSteps 组件展示工作步骤。

### 后端设计

#### 新建文件

| 文件 | 说明 |
| --- | --- |
| `controller/WeatherImageController.java` | `POST /weather-image/predict` 端点，接收 multipart 图片 |
| `service/WeatherImageService.java` + `impl/WeatherImageServiceImpl.java` | 校验图片 → 调用 Python API → 解析结果 |
| `dto/response/WeatherImagePredictResponse.java` | 响应 DTO：label/confidence/probabilities + 中文映射 |
| `config/WeatherImageProperties.java` | `@ConfigurationProperties("weather-image.model")` 配置类 |


#### 修改文件

| 文件 | 改动 |
| --- | --- |
| `config/WebClientConfig.java` | 新增 `weatherImageWebClient` Bean（Python API 基址） |
| `application.yml` | 新增 `weather-image.model.base-url` 配置（默认 `http://localhost:5000`） |
| `exception/ErrorCode.java` | 新增 `IMAGE_FORMAT_ERROR`(400006)、`IMAGE_TOO_LARGE`(400007)、`MODEL_INFERENCE_ERROR`(503007) |
| `controller/AiController.java` | 将 `analyzeImage()` 占位实现改为重定向到新端点 |


#### 数据流

```
前端 FormData → POST /api/weather-image/predict
  → WeatherImageService:
    1. 校验文件类型/大小
    2. WebClient 转发至 Python POST /predict
    3. 解析 JSON 响应为 WeatherImagePredictResponse
    4. 中文映射（sunny→晴、cloudy→阴、rainy→雨、snowy→雪）
  → 返回 ApiResponse<WeatherImagePredictResponse>
```

### 前端设计

#### 新建文件

| 文件 | 说明 |
| --- | --- |
| `components/ai/WeatherImageCard.vue` | 识别结果卡片：天气图标、类型标签、置信度、概率条形图 |


#### 修改文件

| 文件 | 改动 |
| --- | --- |
| `api/index.js` | 新增 `weatherImageApi.predict(formData)` 和 `weatherImageApi.predictWithAdvice(formData, location)` |
| `stores/chat.js` | 新增 `sendImageMessage(file)` 方法：上传图片→显示预览→添加结果消息（含 weatherImage 字段） |
| `views/AIAssistantView.vue` | 输入区增加图片上传按钮+拖拽支持；增加"上传图片识别天气"快捷卡片；图片消息渲染 |
| `components/ai/AgentMessage.vue` | 支持消息类型 image（用户侧显示缩略图）和 weatherImage 字段渲染 WeatherImageCard |


### 关键设计决策

1. **图片传输用 FormData 而非 base64**：减少 33% 传输体积，且 Python API 已原生支持 multipart
2. **复用 AgentSteps 展示工作步骤**：定义 `type: 'model'` 新步骤类型，与现有 rag/tool/reason 模式一致
3. **中文映射在后端完成**：保持前端国际化友好，后端返回 `labelEn` 和 `labelCn` 双字段
4. **WebClient 独立 Bean**：遵循 `WebClientConfig` 现有模式，使用 `@Value` 注入配置，可独立设置超时和缓冲区大小

## Agent Extensions

### Skill

- **impeccable**
- **用途**：设计和优化 `WeatherImageCard.vue` 识别结果卡片组件与 `AIAssistantView.vue` 图片上传交互的 UI/UX
- **预期成果**：产出精美的概率分布条形图动画、四类天气图标设计、图片拖拽上传视觉反馈、卡片渐变配色等前端视觉方案