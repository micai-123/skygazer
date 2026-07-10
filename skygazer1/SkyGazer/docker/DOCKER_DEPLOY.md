# 智观天象 AI - Docker 部署文档

## 环境要求

- Docker Desktop 4.0+ (Windows/macOS) 或 Docker Engine 20.10+ (Linux)
- Docker Compose 2.0+
- 至少 4GB 可用内存
- 至少 10GB 可用磁盘空间

## 快速部署

### Windows 用户

进入 deploy 目录，双击运行 `deploy.bat` 或在 PowerShell 中执行：

```powershell
cd deploy
.\deploy.bat
```

### Linux/macOS 用户

```bash
cd deploy
chmod +x deploy.sh
./deploy.sh
```

### 手动部署

1. **配置环境变量**

```bash
# 复制环境变量模板
cp config/.env.example config/.env

# 编辑 config/.env 文件，配置必要的环境变量
# 必须配置: OPENAI_API_KEY, QWEATHER_API_KEY
```

2. **构建并启动服务**

```bash
# 构建镜像
docker-compose -f docker/docker-compose.yml build

# 启动所有服务
docker-compose -f docker/docker-compose.yml up -d
```

3. **查看服务状态**

```bash
docker-compose -f docker/docker-compose.yml ps
```

## 服务访问地址

| 服务 | 地址 | 说明 |
|-----|------|------|
| 前端 | http://localhost | Vue 3 应用 |
| 后端 API | http://localhost:8080/api | Spring Boot 服务 |
| MySQL | localhost:3306 | 数据库 |
| Redis | localhost:6379 | 缓存服务 |

## 常用命令

### 服务管理

```bash
# 启动所有服务
docker-compose -f docker/docker-compose.yml up -d

# 停止所有服务
docker-compose -f docker/docker-compose.yml down

# 重启所有服务
docker-compose -f docker/docker-compose.yml restart

# 停止并删除数据卷（清除所有数据）
docker-compose -f docker/docker-compose.yml down -v
```

### 日志查看

```bash
# 查看所有服务日志
docker-compose -f docker/docker-compose.yml logs -f

# 查看特定服务日志
docker-compose -f docker/docker-compose.yml logs -f backend
docker-compose -f docker/docker-compose.yml logs -f frontend
docker-compose -f docker/docker-compose.yml logs -f mysql
```

### 服务状态

```bash
# 查看服务状态
docker-compose -f docker/docker-compose.yml ps

# 查看资源使用情况
docker stats
```

### 进入容器

```bash
# 进入后端容器
docker-compose -f docker/docker-compose.yml exec backend sh

# 进入前端容器
docker-compose -f docker/docker-compose.yml exec frontend sh

# 进入 MySQL 容器
docker-compose -f docker/docker-compose.yml exec mysql mysql -u root -p
```

## 数据持久化

数据存储在 Docker 数据卷中：

- `mysql-data`: MySQL 数据文件
- `redis-data`: Redis 数据文件

### 备份数据

```bash
# 备份 MySQL 数据
docker-compose -f docker/docker-compose.yml exec mysql mysqldump -u root -p skygazer_weather > backup.sql

# 备份 Redis 数据
docker-compose -f docker/docker-compose.yml exec redis redis-cli BGSAVE
docker cp skygazer-redis:/data/dump.rdb ./redis-backup.rdb
```

### 恢复数据

```bash
# 恢复 MySQL 数据
cat backup.sql | docker-compose -f docker/docker-compose.yml exec -T mysql mysql -u root -p skygazer_weather

# 恢复 Redis 数据
docker cp ./redis-backup.rdb skygazer-redis:/data/dump.rdb
docker-compose -f docker/docker-compose.yml restart redis
```

## 环境变量说明

| 变量名 | 说明 | 默认值 |
|-------|------|--------|
| DB_USERNAME | 数据库用户名 | root |
| DB_PASSWORD | 数据库密码 | root |
| REDIS_HOST | Redis 主机地址 | redis |
| REDIS_PORT | Redis 端口 | 6379 |
| OPENAI_API_KEY | OpenAI API 密钥 | - |
| OPENAI_BASE_URL | OpenAI API 地址 | https://api.openai.com |
| QWEATHER_API_KEY | 和风天气 API 密钥 | - |
| JWT_SECRET | JWT 签名密钥 | - |

## 健康检查

所有服务都配置了健康检查：

```bash
# 检查后端健康状态
curl http://localhost:8080/api/health

# 检查前端
curl http://localhost:80

# 检查 MySQL
docker-compose -f docker/docker-compose.yml exec mysql mysqladmin ping -h localhost

# 检查 Redis
docker-compose -f docker/docker-compose.yml exec redis redis-cli ping
```

## 故障排查

### 服务无法启动

1. 检查端口是否被占用：
```bash
# Windows
netstat -ano | findstr :80
netstat -ano | findstr :8080
netstat -ano | findstr :3306

# Linux/macOS
lsof -i :80
lsof -i :8080
lsof -i :3306
```

2. 查看服务日志：
```bash
docker-compose -f docker/docker-compose.yml logs backend
docker-compose -f docker/docker-compose.yml logs frontend
```

### 数据库连接失败

1. 确认 MySQL 服务已启动：
```bash
docker-compose ps mysql
```

2. 检查数据库连接配置：
```bash
docker-compose exec mysql mysql -u root -p
```

### 前端无法访问后端

1. 确认后端服务已启动：
```bash
curl http://localhost:8080/api/health
```

2. 检查网络连接：
```bash
docker network ls
docker network inspect jishe02_skygazer-network
```

## 生产环境建议

1. **修改默认密码**：修改 `.env` 中的数据库密码和 JWT 密钥
2. **配置 HTTPS**：使用 Nginx 反向代理配置 SSL 证书
3. **限制资源**：在 `docker-compose.yml` 中添加资源限制
4. **日志管理**：配置日志驱动和日志轮转
5. **监控告警**：集成 Prometheus + Grafana 监控

## 文件结构

```
jishe02/
├── docker-compose.yml      # Docker Compose 配置
├── .env                    # 环境变量（不提交到 Git）
├── .env.example            # 环境变量模板
├── deploy.sh               # Linux/macOS 部署脚本
├── deploy.bat              # Windows 部署脚本
├── frontend/
│   ├── Dockerfile          # 前端 Dockerfile
│   └── nginx.conf          # Nginx 配置
└── backend/
    ├── Dockerfile          # 后端 Dockerfile
    └── ...
```
