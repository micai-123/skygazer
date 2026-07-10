#!/bin/bash

echo "============================================"
echo "  SkyGazer 智观天象 - 部署脚本 (Linux)"
echo "============================================"
echo

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "[错误] 未检测到Docker，请先安装Docker"
    echo "安装命令: curl -fsSL https://get.docker.com | sh"
    exit 1
fi

# 检查Docker Compose
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "[错误] 未检测到Docker Compose"
    exit 1
fi

# 使用docker compose或docker-compose
COMPOSE_CMD="docker compose"
if command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
fi

echo "[步骤1/5] 检查环境变量配置..."
if [ ! -f .env ]; then
    echo "[提示] 未找到.env文件，从.env.example创建..."
    if [ -f .env.example ]; then
        cp .env.example .env
        echo "[警告] 请编辑.env文件并填入实际的API密钥！"
        read -p "按回车键继续..." 
    else
        echo "[错误] 未找到.env.example文件"
        exit 1
    fi
else
    echo "[✓] 环境变量配置已就绪"
fi

echo
echo "[步骤2/5] 构建后端项目..."
cd backend
./mvnw clean package -DskipTests -q || mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "[错误] 后端构建失败"
    exit 1
fi
echo "[✓] 后端构建完成"
cd ..

echo
echo "[步骤3/5] 构建前端镜像..."
$COMPOSE_CMD build frontend
if [ $? -ne 0 ]; then
    echo "[错误] 前端构建失败"
    exit 1
fi
echo "[✓] 前端构建完成"

echo
echo "[步骤4/5] 启动所有服务..."
$COMPOSE_CMD up -d
if [ $? -ne 0 ]; then
    echo "[错误] 服务启动失败"
    exit 1
fi

echo
echo "[步骤5/5] 等待服务就绪..."
sleep 15

echo
echo "============================================"
echo "  ✅ 部署成功！"
echo "============================================"
echo
echo "访问地址："
echo "  前端: http://$(hostname -I | awk '{print $1}')"
echo "  后端API: http://$(hostname -I | awk '{print $1}'):8080/api"
echo
echo "常用命令："
echo "  查看日志: $COMPOSE_CMD logs -f"
echo "  停止服务: $COMPOSE_CMD down"
echo "  重启服务: $COMPOSE_CMD restart"
echo "  查看状态: $COMPOSE_CMD ps"
echo
