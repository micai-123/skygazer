# 智观天象 AI - 后端服务

基于 Spring Boot 3 + Spring AI 的智能气象决策系统后端服务。

## 技术栈

- **框架**: Spring Boot 3.2.5
- **AI集成**: Spring AI 1.0.0-M4
- **数据库**: MySQL 8.0+
- **缓存**: Redis 6.0+
- **安全**: Spring Security + JWT
- **构建工具**: Maven 3.8+

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/skygazer/weather/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器层
│   │   │   ├── service/         # 服务层
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── entity/          # 实体类
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── exception/       # 异常处理
│   │   │   ├── util/            # 工具类
│   │   │   ├── constant/        # 常量定义
│   │   │   └── security/        # 安全相关
│   │   └── resources/
│   │       ├── application.yml  # 主配置文件
│   │       └── db/migration/    # 数据库迁移脚本
│   └── test/                    # 测试代码
├── pom.xml                      # Maven配置
├── Dockerfile                   # Docker镜像构建
└── docker-compose.yml           # Docker编排配置
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 本地开发

1. **克隆项目并进入目录**
```bash
cd backend
```

2. **配置环境变量**
```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export REDIS_HOST=localhost
export OPENAI_API_KEY=your_api_key
export QWEATHER_API_KEY=your_qweather_key
```

3. **启动MySQL和Redis**
```bash
# 使用Docker启动
docker-compose up -d mysql redis
```

4. **构建并运行**
```bash
mvn clean package -DskipTests
java -jar target/weather-backend-1.0.0.jar
```

### Docker部署

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down
```

## API接口

### 天气数据接口

| 接口 | 方法 | 说明 |
|-----|------|------|
| /api/weather/current | GET | 获取当前天气 |
| /api/weather/hourly | GET | 获取24小时预报 |
| /api/weather/weekly | GET | 获取7天预报 |
| /api/weather/lifestyle | GET | 获取生活指数 |
| /api/weather/air-quality | GET | 获取空气质量 |

### AI交互接口

| 接口 | 方法 | 说明 |
|-----|------|------|
| /api/ai/chat | POST | AI对话 |
| /api/ai/chat/stream | POST | AI流式对话 |
| /api/ai/analyze-image | POST | 图片分析 |
| /api/ai/weather-story | POST | 天气叙事 |
| /api/ai/decision-advice | POST | 决策建议 |

### 用户管理接口

| 接口 | 方法 | 说明 |
|-----|------|------|
| /api/user/register | POST | 用户注册 |
| /api/user/login | POST | 用户登录 |
| /api/user/profile | GET | 获取用户信息 |
| /api/user/profile | PUT | 更新用户信息 |

## 配置说明

### application.yml 主要配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/skygazer_weather
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL}

weather:
  api:
    key: ${QWEATHER_API_KEY}
    base-url: https://devapi.qweather.com/v7
```

## 开发规范

1. **代码规范**: 遵循阿里巴巴Java开发手册
2. **命名规范**: 类名使用大驼峰，方法名使用小驼峰
3. **注释规范**: 公共方法必须添加JavaDoc注释
4. **异常处理**: 使用统一异常处理器
5. **日志规范**: 使用SLF4J，禁止使用System.out

## 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=WeatherServiceTest
```

## 许可证

MIT License
