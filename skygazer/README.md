# 智观天象 SkyGazer · 智能天气决策系统

一个前后端结合、并集成 AI 能力的智能天气系统。系统由 **Vue 3 前端**、**Spring Boot 后端** 与 **Python 天气图像识别模型服务** 三部分组成，提供实时天气、生活指数、气象预警、天气地图、AI 气象助手（会话记忆 + RAG + 工具调用）以及基于深度学习的天气图片识别等功能。

---

## 目录

- [系统架构](#系统架构)
- [技术栈](#技术栈)
- [目录结构](#目录结构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
  - [1. 基础中间件（MySQL / Redis）](#1-基础中间件mysql--redis)
  - [2. 后端服务（Spring Boot）](#2-后端服务spring-boot)
  - [3. 前端服务（Vue 3）](#3-前端服务vue-3)
  - [4. 天气图像模型服务（Flask）](#4-天气图像模型服务flask)
- [核心功能](#核心功能)
- [API 概览](#api-概览)
- [配置说明](#配置说明)
- [常见问题](#常见问题)

---

## 系统架构

```
┌──────────────────┐        HTTP/JSON        ┌────────────────────────┐
│   前端 frontend    │  ───────────────────▶  │     后端 backend         │
│  Vue 3 + Vite     │  ◀───────────────────  │  Spring Boot 3.2.5      │
│  (端口 5173)       │                         │  (端口 8080, /api)       │
└──────────────────┘                         └───────────┬────────────┘
                                                          │
                          ┌───────────────┬──────────────┼───────────────┐
                          ▼               ▼              ▼               ▼
                     ┌─────────┐    ┌─────────┐   ┌────────────┐  ┌──────────────┐
                     │  MySQL  │    │  Redis  │   │ 阿里云百炼   │  │ 天气模型服务   │
                     │ (3306)  │    │ (6379)  │   │ Qwen (LLM)  │  │ Flask (5000) │
                     └─────────┘    └─────────┘   └────────────┘  └──────────────┘
                        持久化        会话记忆/          对话/          图像分类
                                     向量知识库          RAG          (PyTorch)
```

- **前端** 通过 REST API 与后端交互，负责界面展示与交互。
- **后端** 承担业务逻辑、数据持久化、鉴权，并集成 Spring AI 对接阿里云百炼 Qwen 实现智能体；使用 Redis 存储会话记忆与轻量向量知识库。
- **天气模型服务** 独立的 Python Flask 服务，封装 ResNet-18 天气图像分类模型，供后端调用。

---

## 技术栈

### 前端 `frontend/`
- Vue 3 + Vite 5
- Vue Router 4 · Pinia 2（状态管理）
- ECharts 5（数据可视化）
- Axios（HTTP 请求）
- Glassmorphism 玻璃拟态 UI 设计

### 后端 `backend/`
- Spring Boot 3.2.5（Java 17，**WAR 打包，部署至外部 Tomcat**）
- MyBatis 3（持久层）· MySQL 8
- Spring Data Redis（Lettuce）
- Spring Security + JWT（jjwt 0.12.5）鉴权
- Spring AI 1.0.0-M6（OpenAI 兼容方式接入阿里云百炼 Qwen）
- Spring WebFlux（WebClient 调用外部服务）
- MapStruct + Lombok

### 天气图像模型服务 `weather-model-api/`
- Python 3 + Flask
- PyTorch + torchvision（ResNet-18 + Dropout 分类头）
- OpenCV / NumPy（图像预处理）
- 四分类：`cloudy` / `rainy` / `snowy` / `sunny`

---

## 目录结构

```
skygazer/
├── frontend/                  # Vue 3 前端
│   ├── src/
│   │   ├── api/               # 接口封装
│   │   ├── components/        # 通用组件
│   │   ├── views/             # 页面（首页/分析/生活/地图/AI助手/我的/设置）
│   │   ├── stores/           # Pinia 状态
│   │   ├── router/           # 路由
│   │   ├── styles/ assets/   # 样式与静态资源
│   │   └── main.js App.vue
│   ├── vite.config.js
│   └── package.json
│
├── backend/                   # Spring Boot 后端（WAR）
│   ├── src/main/java/com/skygazer/weather/
│   │   ├── controller/        # REST 接口
│   │   ├── service/           # 业务逻辑
│   │   ├── mapper/ entity/    # MyBatis 映射与实体
│   │   ├── rag/               # AI 智能体、向量知识库、工具
│   │   ├── config/ security/  # 配置与安全
│   │   └── WeatherApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml            # 主配置
│   │   ├── application-dev.yml        # 开发环境
│   │   ├── db/migration/*.sql         # 数据库脚本
│   │   └── knowledge/                 # RAG 知识库文档
│   └── pom.xml
│
└── weather-model-api/         # 天气图像分类模型服务（Flask）
    ├── serve_api.py
    ├── requirements.txt
    └── results/               # 模型权重（*.pth）
```

---

## 环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 17 |
| Maven | 3.6+ |
| Node.js | 18+ |
| MySQL | 8.x（或 5.7+） |
| Redis | 5+（推荐用 Docker 运行） |
| Python | 3.8+ |

---

## 快速开始

### 1. 基础中间件（MySQL / Redis）

**MySQL**：创建数据库并执行初始化脚本。

```sql
-- 库与表结构见 backend/src/main/resources/db/migration/
--   V1__Init_Schema.sql          初始化库表
--   V2__Add_Vector_Knowledge.sql 向量知识库
```

默认库名 `skygazer_weather`，连接信息在 `application.yml` / `application-dev.yml` 中配置。

**Redis**（推荐 Docker）：

```powershell
docker run -d --name redis --restart unless-stopped -p 6379:6379 redis
```

> Redis 用于：AI 智能体的**会话记忆**（键前缀 `chat:mem:`）与**轻量向量知识库**（键前缀 `rag:doc:`）。

### 2. 后端服务（Spring Boot）

```powershell
cd backend
# 开发模式运行（首次会下载依赖）
mvn spring-boot:run -Dmaven.test.skip=true
```

- 启动后监听 `http://localhost:8080`，上下文路径为 `/api`（即接口前缀 `http://localhost:8080/api`）。
- 生产部署：`mvn clean package` 生成 `target/skygazer.war`，部署到外部 Tomcat 10.1+。

### 3. 前端服务（Vue 3）

```powershell
cd frontend
npm install
npm run dev        # 开发模式，默认 http://localhost:5173
npm run build      # 生产构建，产物在 dist/
```

### 4. 天气图像模型服务（Flask）

```powershell
cd weather-model-api
pip install -r requirements.txt
python serve_api.py    # 默认 http://0.0.0.0:5000
```

- 模型权重路径可通过环境变量 `WEATHER_MODEL_PATH` 指定，默认读取 `results/model_sample.pth`。
- 后端通过 `weather-image.model.base-url`（默认 `http://localhost:5000`）调用该服务。

---

## 核心功能

- **实时天气**：当前天气、逐小时、未来一周、空气质量、天气分析。
- **生活指数**：多类生活/健康指数计算与自定义指数。
- **气象预警**：预警查询、生成、分析、按类型筛选与解除。
- **天气地图**：多图层天气可视化、时间轴、行政区天气与 GeoJSON。
- **AI 气象助手**（`/agent`）：
  - 会话记忆（基于 Redis，按 `conversationId` 维持上下文）
  - 工具调用（如 `getWeatherByCity` 联动实时天气）
  - RAG 检索（基于向量知识库，可选）
- **天气图像识别**：上传天气图片，返回天气类别与置信度（对接 Flask 模型服务）。
- **用户系统**：注册、登录（JWT）、个人资料管理。

---

## API 概览

> 统一前缀：`http://localhost:8080/api`

| 模块 | 基础路径 | 说明 |
|------|----------|------|
| 健康检查 | `/health` | 服务健康状态 |
| 用户 | `/user` | 注册 / 登录 / 资料 |
| 天气 | `/weather` | 当前/逐时/一周/空气质量/分析/刷新 |
| 生活指数 | `/life-index` | 指数查询与自定义 |
| 气象预警 | `/warnings` | 预警查询/生成/分析/解除 |
| 天气地图 | `/weather-map` | 图层/时间轴/行政区/GeoJSON |
| AI 智能体 | `/agent` | `status` / `query`（会话记忆+RAG+工具） / `analyze` |
| 天气图像 | `/weather-image` | `predict` 图像识别 |
| 数据迁移 | `/data-migration` | 数据初始化与迁移 |

**AI 智能体调用示例**：

```bash
curl -X POST http://localhost:8080/api/agent/query \
  -H "Content-Type: application/json" \
  -d '{"question":"北京今天天气怎么样？","city":"北京","conversationId":"demo-001"}'
```

同一 `conversationId` 连续提问即可延续上下文（会话记忆）。

---

## 配置说明

后端主要配置位于 `backend/src/main/resources/application.yml`，关键项支持环境变量覆盖：

| 配置项 | 环境变量 | 默认值 / 说明 |
|--------|----------|---------------|
| MySQL 连接 | `DB_USERNAME` / `DB_PASSWORD` | 数据库账号密码 |
| Redis | `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | 默认 `localhost:6379` |
| 阿里云百炼 API Key | `ALIYUN_AI_API_KEY` | Qwen 访问密钥 |
| AI 对话端点 | `ALIYUN_AI_OPENAI_BASE` | OpenAI 兼容基础地址（不含 `/v1`，由 Spring AI 自动拼接） |
| AI 模型 | `ALIYUN_AI_MODEL` | 默认 `qwen-plus` |
| 向量库开关 | — | `ai.aliyun.vector-store-enabled`（`application-dev.yml`） |
| 天气模型服务 | `WEATHER_MODEL_BASE_URL` | 默认 `http://localhost:5000` |
| JWT | `JWT_SECRET` | 令牌签名密钥 |

> **注意**：`spring.ai.openai.base-url` 应配置到 `.../compatible-mode`（不带 `/v1`），Spring AI 会自动追加 `/v1/chat/completions` 与 `/v1/embeddings`。

---

## 常见问题

- **RAG 检索无结果 / 启动出现 embedding 相关 WARN**：说明所用 AI 端点未提供 embedding 接口或未开通对应模型。此时会话记忆、工具调用、天气问答仍正常，仅 RAG 文档检索降级为空。如需启用 RAG，请为 embedding 配置支持向量化的端点与模型（如阿里云百炼 `text-embedding-v2/v3`）。
- **`mvn spring-boot:run` 编译测试报错**：可加 `-Dmaven.test.skip=true` 跳过测试编译再启动。
- **端口冲突**：后端 8080、前端 5173、模型服务 5000、Redis 6379、MySQL 3306，按需调整。

---

## License

MIT © SkyGazer Team
