@echo off
chcp 65001 >nul
echo ========================================
echo   SkyGazeer 一键打包脚本
echo   版本：1.0.0
echo ========================================
echo.

:: 检查 Java 环境
echo [1/6] 检查 Java 环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java 环境，请先安装 JDK 21+
    pause
    exit /b 1
)
echo [✓] Java 环境检查通过
echo.

:: 检查 Node.js 环境
echo [2/6] 检查 Node.js 环境...
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Node.js 环境，请先安装 Node.js 18+
    pause
    exit /b 1
)
echo [✓] Node.js 环境检查通过
echo.

:: 检查 Docker 环境
echo [3/6] 检查 Docker 环境...
docker -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] 未检测到 Docker 环境，将跳过 Docker 镜像构建
    set SKIP_DOCKER=true
) else (
    echo [✓] Docker 环境检查通过
    set SKIP_DOCKER=false
)
echo.

:: 构建后端
echo [4/6] 构建后端 Spring Boot 项目...
cd backend
if exist "mvnw.cmd" (
    echo 使用 Maven Wrapper 构建...
    call mvnw.cmd clean package -DskipTests
) else (
    echo 使用系统 Maven 构建...
    call mvn clean package -DskipTests
)
if %errorlevel% neq 0 (
    echo [错误] 后端构建失败
    cd ..
    pause
    exit /b 1
)
cd ..
echo [✓] 后端构建完成
echo.

:: 构建前端
echo [5/6] 构建前端 Vue 项目...
cd frontend
if not exist "node_modules" (
    echo 首次构建，安装依赖...
    call npm install
)
call npm run build
if %errorlevel% neq 0 (
    echo [错误] 前端构建失败
    cd ..
    pause
    exit /b 1
)
cd ..
echo [✓] 前端构建完成
echo.

:: 创建发布包
echo [6/6] 创建发布包...
set PACKAGE_NAME=SkyGazeer-Package-%date:~0,4%%date:~5,2%%date:~8,2%
set PACKAGE_DIR=%PACKAGE_NAME%

if exist "%PACKAGE_DIR%" rmdir /s /q "%PACKAGE_DIR%"
mkdir "%PACKAGE_DIR%"
mkdir "%PACKAGE_DIR%\backend"
mkdir "%PACKAGE_DIR%\frontend"
mkdir "%PACKAGE_DIR%\config"
mkdir "%PACKAGE_DIR%\database"
mkdir "%PACKAGE_DIR%\docs"
mkdir "%PACKAGE_DIR%\scripts"

:: 复制后端文件
echo 复制后端文件...
copy "backend\target\weather-backend-1.0.0.jar" "%PACKAGE_DIR%\backend\"
copy "backend\src\main\resources\application-prod.yml" "%PACKAGE_DIR%\backend\" 2>nul

:: 复制前端文件
echo 复制前端文件...
xcopy /E /I /Y "frontend\dist" "%PACKAGE_DIR%\frontend\dist"
copy "frontend\nginx.conf" "%PACKAGE_DIR%\frontend\" 2>nul

:: 复制配置文件
echo 复制配置文件...
copy "config\.env.production" "%PACKAGE_DIR%\config\" 2>nul
copy "config\.env.example" "%PACKAGE_DIR%\config\" 2>nul

:: 复制数据库脚本
echo 复制数据库脚本...
copy "backend\src\main\resources\db\migration\*.sql" "%PACKAGE_DIR%\database\"

:: 复制文档
echo 复制文档...
copy "打包部署指南.md" "%PACKAGE_DIR%\docs\"
copy "backend\README.md" "%PACKAGE_DIR%\docs\" 2>nul
copy "frontend\README.md" "%PACKAGE_DIR%\docs\" 2>nul

:: 创建启动脚本
echo 创建启动脚本...
(
echo @echo off
echo chcp 65001 ^>nul
echo echo ========================================
echo echo   SkyGazeer 启动脚本
echo echo ========================================
echo echo.
echo echo [提示] 请确保已配置以下环境：
echo echo   - Java 21+
echo echo   - MySQL 8.0+
echo echo   - Redis 7.0+
echo echo.
echo echo [1/3] 启动后端服务...
echo start "" "java" -Xms512m -Xmx1024m -jar "backend\weather-backend-1.0.0.jar"
echo timeout /t 10 /nobreak ^>nul
echo echo [✓] 后端服务已启动 ^(http://localhost:8080^)
echo echo.
echo echo [2/3] 启动前端服务...
echo if exist "nginx\nginx.exe" ^(
echo     start "" "nginx\nginx.exe" -p "frontend"
echo     echo [✓] Nginx 已启动 ^(http://localhost:80^)
echo ^) else ^(
echo     echo [警告] 未找到 Nginx，请手动部署前端到 Web 服务器
echo     echo 或使用：cd frontend ^&^& npm run preview
echo ^)
echo echo.
echo echo [3/3] 打开浏览器...
echo timeout /t 3 /nobreak ^>nul
echo start http://localhost
echo echo.
echo echo ========================================
echo echo   SkyGazeer 已成功启动!
echo echo ========================================
echo echo.
echo echo 按 Ctrl+C 可停止所有服务
echo pause
) > "%PACKAGE_DIR%\start.bat"

:: 创建 Docker 启动脚本
if "%SKIP_DOCKER%"=="false" (
    echo 创建 Docker 启动脚本...
    (
    echo @echo off
    echo chcp 65001 ^>nul
    echo echo ========================================
    echo echo   SkyGazeer Docker 启动脚本
    echo echo ========================================
    echo echo.
    echo cd docker
    echo docker compose -f docker-compose.prod.yml up -d --build
    echo echo.
    echo echo [✓] SkyGazeer 已通过 Docker 启动
    echo echo   - 前端：http://localhost
    echo echo   - 后端：http://localhost:8080
    echo echo   - 数据库：localhost:3306
    echo echo.
    echo echo 停止服务：docker compose -f docker-compose.prod.yml down
    echo echo 查看日志：docker compose -f docker-compose.prod.yml logs -f
    echo echo.
    echo pause
    ) > "%PACKAGE_DIR%\start-docker.bat"
)

:: 创建配置说明
echo 创建配置说明...
(
echo ========================================
echo SkyGazeer 配置说明
echo ========================================
echo.
echo 1. 复制配置文件
echo    copy config\.env.production.local.example config\.env.production.local
echo.
echo 2. 编辑 config\.env.production.local，填写必要配置：
echo    - DB_PASSWORD: 数据库密码
echo    - REDIS_PASSWORD: Redis 密码
echo    - OPENAI_API_KEY: OpenAI API 密钥
echo    - QWEATHER_API_KEY: 和风天气 API 密钥
echo    - JWT_SECRET: JWT 密钥
echo.
echo 3. 初始化数据库
echo    mysql -u root -p ^< database\init.sql
echo.
echo 4. 启动服务
echo    Windows: 双击 start.bat
echo    Docker: 双击 start-docker.bat
echo.
echo 详细文档请参考：docs\打包部署指南.md
) > "%PACKAGE_DIR%\配置说明.txt"

echo [✓] 发布包创建完成：%PACKAGE_DIR%
echo.
echo ========================================
echo   打包完成!
echo ========================================
echo.
echo 发布包位置：%cd%\%PACKAGE_DIR%
echo.
echo 下一步操作:
echo   1. 查看 配置说明.txt 了解配置步骤
echo   2. 编辑 config\.env.production.local 配置文件
echo   3. 运行 start.bat 或 start-docker.bat 启动服务
echo.
echo ========================================
pause
