@echo off
chcp 65001 >nul
echo ========================================
echo   SkyGazeer Docker 构建脚本
echo ========================================
echo.

:: 检查 Docker
echo [检查] 验证 Docker 环境...
docker -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Docker，请先安装 Docker Desktop
    echo 下载地址：https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)
echo [✓] Docker 版本信息:
docker --version
echo.

:: 检查 Docker Compose
echo [检查] 验证 Docker Compose...
docker compose version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Docker Compose
    pause
    exit /b 1
)
docker compose version
echo.

:: 进入 docker 目录
cd docker

:: 检查配置文件
echo [检查] 验证配置文件...
if not exist "..\config\.env.production.local" (
    echo [警告] 未找到 config\.env.production.local
    echo 正在从模板创建...
    if exist "..\config\.env.production" (
        copy "..\config\.env.production" "..\config\.env.production.local"
        echo [✓] 配置文件已创建，请编辑后再运行
        echo 文件位置：config\.env.production.local
        pause
        exit /b 0
    ) else (
        echo [错误] 也未找到配置文件模板
        pause
        exit /b 1
    )
)
echo [✓] 配置文件检查通过
echo.

:: 构建 Docker 镜像
echo ========================================
echo   开始构建 Docker 镜像
echo ========================================
echo.

echo [1/4] 构建 MySQL 镜像...
docker compose -f docker-compose.prod.yml pull mysql
echo.

echo [2/4] 构建 Redis 镜像...
docker compose -f docker-compose.prod.yml pull redis
echo.

echo [3/4] 构建后端镜像...
docker compose -f docker-compose.prod.yml build backend
if %errorlevel% neq 0 (
    echo [错误] 后端镜像构建失败，请检查后端 target 目录是否有 JAR 文件
    pause
    exit /b 1
)
echo.

echo [4/4] 构建前端镜像...
docker compose -f docker-compose.prod.yml build frontend
if %errorlevel% neq 0 (
    echo [错误] 前端镜像构建失败
    pause
    exit /b 1
)
echo.

:: 查看镜像
echo ========================================
echo   Docker 镜像列表
echo ========================================
docker images | findstr skygazer
echo.

:: 询问是否启动
set /p START_NOW="是否立即启动服务？(Y/N): "
if /i "%START_NOW%"=="Y" (
    echo.
    echo 正在启动服务...
    docker compose -f docker-compose.prod.yml up -d
    echo.
    echo [✓] 服务启动完成!
    echo.
    echo 访问地址:
    echo   - 前端：http://localhost
    echo   - 后端 API: http://localhost:8080/api/health
    echo.
    echo 查看日志：docker compose -f docker-compose.prod.yml logs -f
    echo 停止服务：docker compose -f docker-compose.prod.yml down
) else (
    echo.
    echo 提示：稍后可手动启动
    echo   cd docker
    echo   docker compose -f docker-compose.prod.yml up -d
)

echo.
echo ========================================
echo   Docker 构建完成!
echo ========================================
pause
