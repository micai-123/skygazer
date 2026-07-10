@echo off
chcp 65001 >nul
echo ============================================
echo   SkyGazer 智观天象 - 部署脚本
echo ============================================
echo.

:: 检查Docker是否安装
docker --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Docker，请先安装Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)

echo [步骤1/5] 检查环境变量配置...
if not exist .env (
    echo [提示] 未找到.env文件，从.env.example创建...
    if exist .env.example (
        copy .env.example .env >nul
        echo [警告] 请编辑.env文件并填入实际的API密钥！
        pause
    ) else (
        echo [错误] 未找到.env.example文件
        pause
        exit /b 1
    )
) else (
    echo [✓] 环境变量配置已就绪
)

echo.
echo [步骤2/5] 构建后端镜像...
cd backend
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo [错误] 后端构建失败
    pause
    exit /b 1
)
echo [✓] 后端构建完成
cd ..

echo.
echo [步骤3/5] 构建前端镜像...
docker-compose build frontend
if errorlevel 1 (
    echo [错误] 前端构建失败
    pause
    exit /b 1
)
echo [✓] 前端构建完成

echo.
echo [步骤4/5] 启动所有服务...
docker-compose up -d
if errorlevel 1 (
    echo [错误] 服务启动失败
    pause
    exit /b 1
)

echo.
echo [步骤5/5] 等待服务就绪...
timeout /t 10 /nobreak >nul

echo.
echo ============================================
echo   ✅ 部署成功！
echo ============================================
echo.
echo 访问地址：
echo   前端: http://localhost
echo   后端API: http://localhost:8080/api
echo   API文档: http://localhost:8080/api/swagger-ui.html (如果启用)
echo.
echo 常用命令：
echo   查看日志: docker-compose logs -f
echo   停止服务: docker-compose down
echo   重启服务: docker-compose restart
echo.
pause
