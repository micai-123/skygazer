@echo off
chcp 65001 >nul
echo ========================================
echo   SkyGazeer Docker 启动脚本
echo ========================================
echo.

cd docker

:: 检查是否已运行
echo [检查] 查看容器状态...
docker compose -f docker-compose.prod.yml ps
echo.

:: 启动服务
echo [启动] 启动所有服务...
docker compose -f docker-compose.prod.yml up -d
if %errorlevel% neq 0 (
    echo [错误] 启动失败
    pause
    exit /b 1
)
echo [✓] 服务启动成功
echo.

:: 等待服务就绪
echo [等待] 等待服务健康检查通过...
timeout /t 10 /nobreak >nul

:: 查看状态
echo [状态] 当前运行状态:
docker compose -f docker-compose.prod.yml ps
echo.

:: 访问信息
echo ========================================
echo   SkyGazeer 已启动
echo ========================================
echo.
echo 访问地址:
echo   🌐 前端界面：http://localhost
echo   🔧 后端 API: http://localhost:8080/api/health
echo   🗄️ 数据库：localhost:3306
echo   💾 缓存服务：localhost:6379
echo.
echo 常用命令:
echo   查看日志：docker compose -f docker-compose.prod.yml logs -f
echo   重启服务：docker compose -f docker-compose.prod.yml restart
echo   停止服务：docker compose -f docker-compose.prod.yml down
echo   查看资源：docker stats
echo.
echo ========================================
pause
