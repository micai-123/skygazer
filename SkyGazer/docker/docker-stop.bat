@echo off
chcp 65001 >nul
echo ========================================
echo   SkyGazeer Docker 停止脚本
echo ========================================
echo.

cd docker

:: 确认操作
set /p CONFIRM="确定要停止所有 SkyGazeer 服务吗？(Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo [已取消]
    pause
    exit /b 0
)

:: 停止服务
echo [停止] 正在停止所有服务...
docker compose -f docker-compose.prod.yml down
if %errorlevel% neq 0 (
    echo [错误] 停止失败
    pause
    exit /b 1
)
echo [✓] 所有服务已停止
echo.

:: 询问是否清理数据卷
set /p CLEAN_VOLUMES="是否同时清理数据卷？(警告：这将删除所有数据库数据) (Y/N): "
if /i "%CLEAN_VOLUMES%"=="Y" (
    echo [清理] 正在清理数据卷...
    docker volume prune -f
    echo [✓] 数据卷已清理
)

echo.
echo ========================================
echo   SkyGazeer 已完全停止
echo ========================================
pause
