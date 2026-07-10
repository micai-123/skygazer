# SkyGazer 文件夹结构说明

## 📁 文件夹组织

### 根目录
```
SkyGazer/
├── .vscode/          # VS Code 配置
├── backend/          # 后端 Spring Boot 项目
├── tests/            # 测试文件（临时）
├── logs/             # 日志文件
├── .env.example      # 环境变量示例
└── .gitignore        # Git 忽略规则
```

### 后端项目结构
```
backend/
├── src/
│   ├── main/
│   │   ├── java/           # Java 源代码
│   │   │   └── com/skygazer/weather/
│   │   │       ├── config/        # 配置类
│   │   │       ├── constant/      # 常量定义
│   │   │       ├── controller/    # 控制器
│   │   │       ├── dto/           # 数据传输对象
│   │   │       ├── entity/        # 实体类
│   │   │       ├── exception/     # 异常处理
│   │   │       ├── repository/    # 数据访问层
│   │   │       ├── scheduler/     # 定时任务
│   │   │       ├── security/      # 安全相关
│   │   │       ├── service/       # 服务层
│   │   │       │   └── impl/      # 服务实现
│   │   │       └── util/          # 工具类
│   │   └── resources/      # 资源文件
│   │       ├── data/              # 数据文件
│   │       ├── db/                # 数据库迁移脚本
│   │       └── application*.yml   # 配置文件
│   └── test/               # 测试代码
├── target/             # 编译输出（已忽略）
├── Dockerfile          # Docker 构建文件
├── pom.xml             # Maven 配置
└── README.md           # 项目说明
```

## 📝 新增文件夹

### tests/
用于存放临时测试文件：
- `test-login.html` - 登录 API 测试工具
- `test_response.json` - 测试响应数据

### logs/
用于存放日志文件：
- `error_detail.log` - 错误详情日志
- `backend_error.log` - 后端错误日志

## 🔒 忽略规则

以下文件已被 `.gitignore` 忽略：

### 测试文件
- `tests/test-*.html` - 临时 HTML 测试文件
- `tests/test_*.json` - 临时 JSON 测试文件

### 日志文件
- `logs/` - 所有日志文件夹
- `*.log` - 所有日志文件

### 其他
- `target/` - 编译输出
- `.env` - 环境变量文件
- `*.jar` - 打包文件

## 📌 注意事项

1. **测试文件**：`tests/` 文件夹中的文件是临时测试文件，不应提交到版本控制
2. **日志文件**：`logs/` 文件夹中的日志文件会自动被忽略
3. **环境变量**：请复制 `.env.example` 并重命名为 `.env`，填入实际配置

## 🚀 快速开始

1. 配置环境变量：
   ```bash
   cp .env.example .env
   # 编辑 .env 文件，填入实际配置
   ```

2. 启动后端服务：
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. 访问应用：
   - 后端 API: http://localhost:8080/api
   - 健康检查: http://localhost:8080/api/health

## 📖 相关文档

- [后端开发文档](backend/README.md)
- [API 配置验证指南](../SkyGazer2/SkyGazer/config/API配置验证指南.md)
- [和风天气 API 配置指南](../SkyGazer2/SkyGazer/config/和风天气API配置指南.md)
