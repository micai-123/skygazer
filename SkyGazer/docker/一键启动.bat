@echo off
chcp 65001 >nul
echo ========================================
echo   SkyGazeer Docker 一键启动脚本
echo ========================================
echo.

:: 检查 Docker
echo [1/4] 检查 Docker 是否运行...
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [错误] Docker 未运行！
    echo.
    echo 请先启动 Docker Desktop:
    echo   1. 按 Win 键
    echo   2. 搜索 "Docker Desktop"
    echo   3. 启动并等待 "Engine running"
    echo.
    pause
    exit /b 1
)
echo [✓] Docker 运行正常
echo.

:: 检查配置文件
echo [2/4] 检查配置文件...
if not exist "docker-compose.prod.yml" (
    echo [错误] 找不到 docker-compose.prod.yml
    pause
    exit /b 1
)
echo [✓] 配置文件存在
echo.

:: 构建并启动
echo [3/4] 构建并启动 Docker 服务...
echo [提示] 首次构建可能需要 5-10 分钟，请耐心等待...
echo.
docker compose -f docker-compose.prod.yml up -d --build
if %errorlevel% neq 0 (
    echo.
    echo [错误] Docker 启动失败
    echo 请查看上面的错误信息
    pause
    exit /b 1
)
echo.
echo [✓] Docker 服务启动成功
echo.

:: 等待服务就绪
echo [4/4] 等待服务健康检查...
echo [提示] 等待约 60 秒...
timeout /t 60 /nobreak >nul
echo.

:: 查看状态
echo ========================================
echo   服务状态
echo ========================================
docker compose -f docker-compose.prod.yml ps
echo.

:: 访问信息
echo ========================================
echo   SkyGazeer 已成功部署!
echo ========================================
echo.
echo 访问地址:
echo   🌐 前端界面：http://localhost
echo   🔧 后端 API: http://localhost:8080/api/health
echo   🗄️ 数据库：localhost:3306
echo   💾 Redis: localhost:6379
echo.
echo 常用命令:
echo   查看日志：docker compose -f docker-compose.prod.yml logs -f
echo   重启服务：docker compose -f docker-compose.prod.yml restart
echo   停止服务：docker compose -f docker-compose.prod.yml down
echo.
echo ========================================
pause
