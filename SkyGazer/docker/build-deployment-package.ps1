#!/usr/bin/env pwsh
# ========================================
# SkyGazeer Docker 部署包自动打包脚本
# ========================================

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SkyGazeer Docker 部署包打包工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 设置路径
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$packageName = "SkyGazer-Docker-Deployment-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
$packageDir = Join-Path $projectRoot $packageName
$zipPath = Join-Path $projectRoot "$packageName.zip"

Write-Host "[1/6] 检查必要文件..." -ForegroundColor Yellow

# 检查后端 JAR 包
$jarPath = Join-Path $projectRoot "backend\target\weather-backend-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "  [错误] 未找到后端 JAR 包：$jarPath" -ForegroundColor Red
    Write-Host "  请先执行：cd backend; .\mvnw clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}
Write-Host "  [✓] 后端 JAR 包存在" -ForegroundColor Green

# 检查 Docker Compose 文件
$dockerComposePath = Join-Path $projectRoot "docker\docker-compose.prod.yml"
if (-not (Test-Path $dockerComposePath)) {
    Write-Host "  [错误] 未找到 docker-compose.prod.yml" -ForegroundColor Red
    exit 1
}
Write-Host "  [✓] Docker Compose 配置存在" -ForegroundColor Green

# 检查前端文件
$frontendDockerfile = Join-Path $projectRoot "frontend\Dockerfile"
if (-not (Test-Path $frontendDockerfile)) {
    Write-Host "  [错误] 未找到 frontend\Dockerfile" -ForegroundColor Red
    exit 1
}
Write-Host "  [✓] 前端 Dockerfile 存在" -ForegroundColor Green

Write-Host ""
Write-Host "[2/6] 创建部署包目录..." -ForegroundColor Yellow

# 创建目录结构
$dirs = @(
    "docker",
    "backend\target",
    "frontend",
    "config",
    "docs"
)

foreach ($dir in $dirs) {
    $targetDir = Join-Path $packageDir $dir
    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
}

Write-Host "  [✓] 目录结构创建完成" -ForegroundColor Green

Write-Host ""
Write-Host "[3/6] 复制必要文件..." -ForegroundColor Yellow

# 复制 Docker 文件
Write-Host "  复制 Docker 配置文件..."
Copy-Item (Join-Path $projectRoot "docker\docker-compose.prod.yml") (Join-Path $packageDir "docker\") -Force
Copy-Item (Join-Path $projectRoot "docker\一键启动.bat") (Join-Path $packageDir "docker\") -ErrorAction SilentlyContinue

# 复制后端文件
Write-Host "  复制后端 JAR 包..."
Copy-Item $jarPath (Join-Path $packageDir "backend\target\") -Force

# 复制前端文件
Write-Host "  复制前端文件..."
Copy-Item (Join-Path $projectRoot "frontend\Dockerfile") (Join-Path $packageDir "frontend\") -Force
Copy-Item (Join-Path $projectRoot "frontend\package.json") (Join-Path $packageDir "frontend\") -Force
Copy-Item (Join-Path $projectRoot "frontend\package-lock.json") (Join-Path $packageDir "frontend\") -ErrorAction SilentlyContinue
Copy-Item (Join-Path $projectRoot "frontend\nginx.conf") (Join-Path $packageDir "frontend\") -Force

# 复制配置文件
Write-Host "  复制配置文件..."
Copy-Item (Join-Path $projectRoot "config\.env.production") (Join-Path $packageDir "config\") -Force
Copy-Item (Join-Path $projectRoot "config\.env.production.local") (Join-Path $packageDir "config\.env.production.local.example") -Force

Write-Host "  [✓] 文件复制完成" -ForegroundColor Green

Write-Host ""
Write-Host "[4/6] 复制文档..." -ForegroundColor Yellow

# 复制文档
$docs = @(
    "Docker 部署操作指南.md",
    "方案一执行总结.md",
    "快速开始.md",
    "打包部署指南.md"
)

foreach ($doc in $docs) {
    $sourcePath = Join-Path $projectRoot $doc
    if (Test-Path $sourcePath) {
        Copy-Item $sourcePath (Join-Path $packageDir "docs\") -Force
        Write-Host "  [✓] 复制：$doc" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "[5/6] 创建 README..." -ForegroundColor Yellow

# 创建 README
$readmeContent = @"
# SkyGazeer Docker 部署包

> 智观天象 - 基于 Spring AI 的多模态智能天气预测与决策系统

## 📦 版本信息

- **版本号**: v1.0.0
- **构建日期**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
- **后端版本**: weather-backend-1.0.0.jar
- **前端版本**: Vue 3 + Vite

## 🚀 快速开始

### 前置要求

- Docker Desktop 26.x+（已安装并运行）
- 至少 8GB 可用内存
- 至少 20GB 可用磁盘空间

### 部署步骤

#### 方式一：一键启动（推荐）

1. 启动 Docker Desktop
2. 进入 \`docker\` 目录
3. 双击运行 \`一键启动.bat\`
4. 等待构建完成（首次约 5-10 分钟）
5. 访问 http://localhost

#### 方式二：手动启动

\`\`\`bash
# 进入 docker 目录
cd docker

# 构建并启动
docker compose -f docker-compose.prod.yml up -d --build

# 查看状态
docker compose -f docker-compose.prod.yml ps

# 查看日志
docker compose -f docker-compose.prod.yml logs -f
\`\`\`

## ⚙️ 配置说明

### 编辑配置文件

进入 \`config\` 目录，复制并编辑配置文件：

\`\`\`bash
cd config
cp .env.production.local.example .env.production.local
\`\`\`

### 必须配置项

打开 \`.env.production.local\`，修改以下配置：

\`\`\`bash
# 数据库密码（必须修改）
DB_PASSWORD=your_secure_password

# Redis 密码（可选）
REDIS_PASSWORD=

# JWT 密钥（必须修改，至少 32 位）
JWT_SECRET=your_very_long_secret_key_here

# OpenAI API 密钥（AI 功能需要）
OPENAI_API_KEY=sk-xxx

# 和风天气 API 密钥（天气数据需要）
QWEATHER_API_KEY=your_qweather_key
\`\`\`

## 🌐 访问地址

部署成功后，可通过以下地址访问：

- **前端界面**: http://localhost
- **后端 API**: http://localhost:8080/api/health
- **数据库**: localhost:3306（如需外部访问）
- **Redis**: localhost:6379（如需外部访问）

## 🔧 运维命令

### 查看服务状态

\`\`\`bash
docker compose -f docker-compose.prod.yml ps
\`\`\`

### 查看实时日志

\`\`\`bash
# 查看所有服务日志
docker compose -f docker-compose.prod.yml logs -f

# 查看单个服务日志
docker compose -f docker-compose.prod.yml logs backend
docker compose -f docker-compose.prod.yml logs frontend
\`\`\`

### 重启服务

\`\`\`bash
# 重启所有服务
docker compose -f docker-compose.prod.yml restart

# 重启单个服务
docker compose -f docker-compose.prod.yml restart backend
\`\`\`

### 停止服务

\`\`\`bash
# 停止所有服务（保留数据）
docker compose -f docker-compose.prod.yml down

# 停止并删除数据（危险！）
docker compose -f docker-compose.prod.yml down -v
\`\`\`

## 📚 文档说明

- **Docker 部署操作指南.md**: 详细部署手册和故障排查
- **方案一执行总结.md**: 执行总结和配置说明
- **快速开始.md**: 5 分钟快速部署指南
- **打包部署指南.md**: 完整打包方案（包含其他部署方式）

## ⚠️ 常见问题

### Q: Docker 启动失败？

**A**: 确保 Docker Desktop 已启动并显示"Engine running"

### Q: 端口被占用？

**A**: 检查端口 80/8080/3306/6379 是否被占用：
\`\`\`bash
netstat -ano | findstr :80
netstat -ano | findstr :8080
\`\`\`

### Q: 数据库连接失败？

**A**: 
1. 等待 MySQL 完全启动（约 1-2 分钟）
2. 检查配置文件中的数据库密码
3. 查看日志：\`docker compose -f docker-compose.prod.yml logs mysql\`

## 🆘 获取帮助

如遇到其他问题，请查看详细文档：

1. Docker 部署操作指南.md
2. 打包部署指南.md

## 📞 技术支持

- 项目版本：v1.0.0
- 构建日期：$(Get-Date -Format 'yyyy-MM-dd')
- 文档版本：v1.0

---

**祝你部署成功！** 🎉
"@

$readmePath = Join-Path $packageDir "README.md"
$readmeContent | Out-File -FilePath $readmePath -Encoding UTF8

Write-Host "  [✓] README 创建完成" -ForegroundColor Green

Write-Host ""
Write-Host "[6/6] 创建压缩包..." -ForegroundColor Yellow

# 创建压缩包
try {
    # 使用 Compress-Archive 创建 zip 文件
    $sourcePath = Join-Path $packageDir "*"
    Compress-Archive -Path $sourcePath -DestinationPath $zipPath -Force -CompressionLevel Optimal
    Write-Host "  [✓] 压缩包创建完成" -ForegroundColor Green
} catch {
    Write-Host "  [警告] 创建压缩包失败：$_" -ForegroundColor Yellow
    Write-Host "  部署包目录已创建：$packageDir" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  部署包打包完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "部署包位置：" -ForegroundColor Cyan
if (Test-Path $zipPath) {
    Write-Host "  📦 $zipPath" -ForegroundColor Green
} else {
    Write-Host "  📂 $packageDir" -ForegroundColor Green
}
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Cyan
Write-Host "  1. 将部署包上传到服务器" -ForegroundColor White
Write-Host "  2. 解压部署包" -ForegroundColor White
Write-Host "  3. 编辑 config/.env.production.local 配置文件" -ForegroundColor White
Write-Host "  4. 运行 docker/一键启动.bat" -ForegroundColor White
Write-Host "  5. 等待构建完成，访问 http://localhost" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
