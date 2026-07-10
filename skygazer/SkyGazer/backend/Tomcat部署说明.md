# SkyGazer 后端 Tomcat 部署说明

> 本后端已改造为 **Spring Boot 3 + MyBatis（替代 JPA）+ WAR 包**，可部署到外部 Tomcat。
> AI 相关模块（Spring AI / 通义千问对话 / 向量知识库）已暂时移除。

## 1. 环境要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17 或 21 | Spring Boot 3 要求 Java 17+ |
| Tomcat | **10.1+（或 11）** | ⚠️ Spring Boot 3 使用 Jakarta EE 9+，**不支持 Tomcat 9（javax 命名空间）** |
| MySQL | 8.x | 数据源已配置 `skygazer_weather` 库 |
| Redis | 可选 | 仅天气缓存使用，未配置时不影响启动（需确认缓存配置） |

## 2. 数据库初始化

项目不再使用 JPA 自动建表，请手动执行建表脚本：

```bash
mysql -u root -p skygazer_weather < src/main/resources/db/migration/V1__Init_Schema.sql
```

（如仍需 AI 相关表，可继续执行 `V2__Add_Vector_Knowledge.sql`，当前已无用。）

## 3. 打包

```bash
cd backend
mvn clean package
# 产物：target/skygazer.war
```

> 如需离线构建请先确保本地 Maven 仓库已缓存依赖。

## 4. 部署到 Tomcat

将 `target/skygazer.war` 复制到 Tomcat 的 `webapps/` 目录，启动 Tomcat 后会自动解压。

### 访问路径说明

- 应用 `server.servlet.context-path` 为 `/api`。
- WAR 文件名决定上下文根。默认 `skygazer.war` → 访问基址为 `http://<host>:8080/skygazer/api`。
- 若希望保持与原开发环境一致（基址 `/api`），可重命名 war 为 `ROOT.war`（`webapps/ROOT.war`），则基址为 `http://<host>:8080/api`；或直接将 war 命名为 `api.war` 并把 `context-path` 改为 `/`。

## 5. 配置项（环境变量）

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_USERNAME` / `DB_PASSWORD` | MySQL 账号密码 | root / root |
| `DB_HOST` | MySQL 主机（prod profile） | localhost |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 连接 | localhost / 6379 / 空 |
| `JWT_SECRET` | JWT 签名密钥（建议至少 256 bit） | 内置默认值 |
| `QWEATHER_API_KEY` | 和风天气 API Key（可选） | 空 |

切换 profile：修改 `application.yml` 中 `spring.profiles.active`，或用启动参数
`-Dspring.profiles.active=prod`。

## 6. 验证

部署后访问：`http://<host>:<port>/<context>/api/health`（HealthController 健康检查）。
默认管理员账号：`admin / admin123`，测试账号：`test / test123`（首次启动由 DataInitializer 自动创建）。
