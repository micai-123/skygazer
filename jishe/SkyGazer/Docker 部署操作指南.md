# 🚀 SkyGazeer Docker 一键部署操作指南

> ⚠️ **重要提示**：执行前请确保 Docker Desktop 已启动！

---

## 📋 前置检查清单

### ✅ 必须满足的条件

1. **Docker Desktop 已安装并正在运行**
   - Windows 用户：在开始菜单搜索"Docker Desktop"并启动
   - 等待 Docker Desktop 左下角显示绿色"Engine running"
   
2. **后端 JAR 包已构建**
   - 文件位置：`SkyGazer\backend\target\weather-backend-1.0.0.jar`
   - ✅ 已存在

3. **配置文件已创建**
   - ✅ `config\.env.production` 已创建
   - ✅ `config\.env.production.local` 已创建
   - ✅ `docker\docker-compose.prod.yml` 已创建

---

## 🎯 手动部署步骤

### 步骤 1：启动 Docker Desktop

```
1. 按 Win 键，搜索 "Docker Desktop"
2. 点击启动
3. 等待左下角显示绿色 "Engine running"
4. 保持 Docker Desktop 运行，不要关闭
```

### 步骤 2：打开 PowerShell

```
1. 按 Win+X，选择 "Windows PowerShell" 或 "终端"
2. 进入项目目录：
   cd d:\jishe\SkyGazer\docker
```

### 步骤 3：执行 Docker 构建和启动

```powershell
# 构建并启动所有服务
docker compose -f docker-compose.prod.yml up -d --build
```

**预期输出**：
```
[+] Building 120.5s (45/45) FINISHED
[+] Running 5/5
 ✔ Network skygazer-network Created
 ✔ Container skygazer-mysql Started
 ✔ Container skygazer-redis Started
 ✔ Container skygazer-backend Started
 ✔ Container skygazer-frontend Started
```

### 步骤 4：等待服务就绪

```powershell
# 等待约 2-3 分钟，让服务完成初始化
# 可以查看日志：
docker compose -f docker-compose.prod.yml logs -f
```

### 步骤 5：验证部署

```powershell
# 查看所有容器状态
docker compose -f docker-compose.prod.yml ps

# 预期输出：所有服务状态为 "Up (healthy)"
```

### 步骤 6：访问应用

打开浏览器访问：
- **前端界面**: http://localhost
- **后端 API**: http://localhost:8080/api/health

---

## 🔧 常用运维命令

### 查看服务状态

```powershell
docker compose -f docker-compose.prod.yml ps
```

### 查看实时日志

```powershell
# 查看所有服务日志
docker compose -f docker-compose.prod.yml logs -f

# 查看单个服务日志
docker compose -f docker-compose.prod.yml logs backend
docker compose -f docker-compose.prod.yml logs frontend
docker compose -f docker-compose.prod.yml logs mysql
docker compose -f docker-compose.prod.yml logs redis
```

### 重启服务

```powershell
# 重启所有服务
docker compose -f docker-compose.prod.yml restart

# 重启单个服务
docker compose -f docker-compose.prod.yml restart backend
```

### 停止服务

```powershell
# 停止所有服务（保留数据）
docker compose -f docker-compose.prod.yml down

# 停止并删除数据（危险！会删除数据库数据）
docker compose -f docker-compose.prod.yml down -v
```

### 重新构建

```powershell
# 停止并重新构建所有服务
docker compose -f docker-compose.prod.yml up -d --build --force-recreate
```

---

## ⚠️ 常见问题排查

### 问题 1：Docker Desktop 未启动

**错误信息**：
```
failed to connect to the docker API...
```

**解决方案**：
1. 启动 Docker Desktop
2. 等待 Engine 完全启动
3. 重新执行命令

### 问题 2：端口被占用

**错误信息**：
```
Error starting userland proxy: listen tcp4 0.0.0.0:80: bind: Only one usage of each socket address...
```

**解决方案**：
```powershell
# 查找占用端口的进程
netstat -ano | findstr :80
netstat -ano | findstr :8080
netstat -ano | findstr :3306

# 停止占用进程或修改 docker-compose.prod.yml 中的端口映射
```

### 问题 3：后端启动失败

**查看日志**：
```powershell
docker compose -f docker-compose.prod.yml logs backend
```

**常见原因**：
- 数据库连接失败：检查 MySQL 是否已启动
- 配置错误：检查环境变量配置

### 问题 4：MySQL 初始化失败

**解决方案**：
```powershell
# 删除 MySQL 数据卷（会删除所有数据！）
docker volume rm docker_mysql-data

# 重新构建
docker compose -f docker-compose.prod.yml up -d
```

---

## 📊 服务健康检查

### 等待所有服务健康

```powershell
# 持续监控直到所有服务健康
while ($true) {
  $status = docker compose -f docker-compose.prod.yml ps
  Write-Host $status
  if ($status -match "healthy") {
    Write-Host "所有服务已就绪！" -ForegroundColor Green
    break
  }
  Start-Sleep -Seconds 10
}
```

### 手动测试 API

```powershell
# 测试后端健康检查
curl http://localhost:8080/api/health

# 预期输出：
# {"status":"UP","timestamp":"2026-04-20T..."}
```

---

## 🎯 快速部署脚本

### 创建一键启动脚本

在 `docker` 目录创建 `start.bat`：

```batch
@echo off
chcp 65001 >nul
echo ========================================
echo   SkyGazeer Docker 一键启动
echo ========================================
echo.
echo [1/3] 检查 Docker...
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker 未启动，请先启动 Docker Desktop
    pause
    exit /b 1
)
echo [✓] Docker 运行正常
echo.

echo [2/3] 构建并启动服务...
docker compose -f docker-compose.prod.yml up -d --build
if %errorlevel% neq 0 (
    echo [错误] 启动失败
    pause
    exit /b 1
)
echo [✓] 服务启动成功
echo.

echo [3/3] 等待服务就绪...
timeout /t 30 /nobreak >nul
echo.
echo ========================================
echo   SkyGazeer 已成功部署!
echo ========================================
echo.
echo 访问地址:
echo   前端：http://localhost
echo   后端：http://localhost:8080/api/health
echo.
echo 查看日志：docker compose -f docker-compose.prod.yml logs -f
echo 停止服务：docker compose -f docker-compose.prod.yml down
echo.
pause
```

---

## 📈 性能优化建议

### 调整资源限制

编辑 `docker-compose.prod.yml`，添加资源限制：

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '1.0'
          memory: 1G
  
  mysql:
    deploy:
      resources:
        limits:
          cpus: '1.5'
          memory: 1.5G
```

---

## 🆘 获取帮助

如遇到问题：
1. 查看详细日志：`docker compose -f docker-compose.prod.yml logs -f`
2. 检查 Docker Desktop 日志
3. 参考完整文档：`打包部署指南.md`

---

**祝你部署成功！** 🎉
