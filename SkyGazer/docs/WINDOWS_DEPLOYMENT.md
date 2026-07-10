# SkyGazer Windows Server 部署指南

## 📋 适用场景

本指南适用于：**Windows Server 2019/2022** + **2核2G** 配置

---

## ⚠️ 重要提示

### 2核2G Windows 系统资源分析：
```
系统基础占用：约 1.2-1.5GB
剩余可用内存：约 0.5-0.8GB
```

**结论**：
- ✅ 可以运行，但会比较紧张
- ⚠️ 建议关闭不必要的 Windows 服务
- 💡 如果预算允许，建议升级到 **2核4G**（约增加￥20-30/月）

---

## 🚀 部署步骤

### 步骤1：远程连接服务器

1. 在云服务器控制台获取 **公网IP** 和 **管理员密码**
2. 使用 Windows 远程桌面连接：
   - 按 `Win + R`，输入 `mstsc`
   - 输入服务器IP地址
   - 用户名：`Administrator`
   - 密码：控制台获取的密码

---

### 步骤2：安装必要软件

#### 2.1 安装 Docker Desktop

1. 下载 Docker Desktop for Windows：
   ```
   https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe
   ```

2. 双击安装，选择 **"Use WSL 2 instead of Hyper-V"**

3. 安装完成后重启服务器

4. 验证安装：
   ```powershell
   docker --version
   docker-compose --version
   ```

#### 2.2 安装 Git（可选，用于拉取代码）

下载地址：https://git-scm.com/download/win

或直接使用 GitHub Desktop 图形界面

#### 2.3 安装 VS Code（可选，用于编辑配置文件）

下载地址：https://code.visualstudio.com/

---

### 步骤3：准备项目文件

#### 方法A：使用 Git 克隆（推荐）

```powershell
# 打开 PowerShell（以管理员身份运行）
cd C:\
git clone https://github.com/你的用户名/SkyGazer.git
cd SkyGazer
```

#### 方法B：本地打包上传

1. 在本地电脑压缩整个 SkyGazer 项目
2. 使用远程桌面复制粘贴到服务器
3. 或使用 FTP 工具（如 WinSCP）上传

---

### 步骤4：配置环境变量

1. 打开项目目录 `C:\SkyGazer`
2. 复制 `.env.example` 为 `.env`
3. 使用记事本或 VS Code 编辑 `.env`：

```env
# 数据库配置
DB_PASSWORD=SkyGazer@2026Secure

# Redis配置
REDIS_PASSWORD=SkyGazer@Redis2026Secure

# JWT密钥
JWT_SECRET=your-very-long-and-random-jwt-secret-key-at-least-256-bits

# API密钥（必须填写）
OPENAI_API_KEY=sk-你的通义千问API密钥
QWEATHER_API_KEY=你的和风天气API密钥
```

---

### 步骤5：修改 Docker Compose 配置（Windows优化版）

由于 Windows 内存有限，需要调整资源配置：

编辑 `docker-compose.yml`，修改后端 JVM 内存参数：

```yaml
backend:
  # ... 其他配置 ...
  environment:
    # ... 其他环境变量 ...
    JAVA_OPTS: "-Xms256m -Xmx512m -XX:+UseG1GC"  # 降低内存占用
```

---

### 步骤6：一键部署

#### 使用 PowerShell 执行部署脚本：

```powershell
# 打开 PowerShell（以管理员身份运行）
cd C:\SkyGazer

# 执行部署脚本
.\deploy.bat
```

或者手动执行：

```powershell
# 进入后端目录构建
cd backend
.\mvnw.cmd clean package -DskipTests
cd ..

# 使用 Docker Compose 启动
docker-compose up -d --build

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

---

### 步骤7：配置防火墙

Windows 防火墙需要开放端口：

```powershell
# 以管理员身份运行 PowerShell

# 开放 80 端口（HTTP）
netsh advfirewall firewall add rule name="SkyGazer HTTP" dir=in action=allow protocol=TCP localport=80

# 开放 443 端口（HTTPS）
netsh advfirewall firewall add rule name="SkyGazer HTTPS" dir=in action=allow protocol=TCP localport=443

# 开放 8080 端口（API）
netsh advfirewall firewall add rule name="SkyGazer API" dir=in action=allow protocol=TCP localport=8080
```

---

### 步骤8：访问测试

打开服务器浏览器访问：
```
http://localhost          # 前端
http://localhost:8080/api/health  # 后端健康检查
```

从外网访问：
```
http://你的服务器公网IP
```

---

## 💾 Windows 系统优化建议

### 关闭不必要的服务（释放内存）

按 `Win + R`，输入 `services.msc`，关闭以下服务：

1. **Windows Search** - 搜索服务（占用大量内存）
2. **Windows Update** - 自动更新（建议手动更新）
3. **Print Spooler** - 打印服务（服务器不需要）
4. **Windows Audio** - 音频服务（不需要）

**操作方法**：
- 右键服务 → 属性
- 启动类型改为"禁用"
- 点击"停止"
- 确定

### 设置虚拟内存（页面文件）

1. 右键"此电脑" → 属性
2. 高级系统设置 → 高级 → 性能设置
3. 高级 → 虚拟内存 → 更改
4. 取消"自动管理"
5. 选择系统盘 → 自定义大小
6. 初始大小：2048 MB
7. 最大值：4096 MB
8. 设置 → 确定 → 重启

### 禁用视觉效果

1. 右键"此电脑" → 属性
2. 高级系统设置 → 高级 → 性能设置
3. 选择"调整为最佳性能"
4. 确定

---

## 🛠️ 常用命令（PowerShell）

```powershell
# 查看所有容器状态
docker-compose ps

# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启服务
docker-compose restart

# 查看实时日志
docker-compose logs -f

# 查看后端日志
docker-compose logs -f backend

# 查看资源占用
docker stats

# 进入容器
docker exec -it skygazer-backend powershell

# 备份数据库
docker exec skygazer-mysql mysqldump -u root -p skygazer_weather > C:\backups\backup_$(Get-Date -Format "yyyyMMdd").sql
```

---

## 📊 Windows vs Linux 资源对比

| 项目 | Windows Server 2019 | Ubuntu 22.04 |
|------|---------------------|--------------|
| 系统空闲内存 | 1.2-1.5GB | 200-300MB |
| 可用内存 | 0.5-0.8GB | 1.7-1.8GB |
| Docker性能 | 90%（WSL2） | 100%（原生） |
| 磁盘占用 | 约15GB | 约5GB |
| 启动时间 | 2-3分钟 | 30秒 |
| 管理方式 | 图形界面 | 命令行 |

---

## ⚠️ 常见问题

### Q1: Docker Desktop 启动失败
**解决方法**：
1. 确保已启用 WSL2
2. 在 BIOS 中启用虚拟化技术（VT-x/AMD-V）
3. 以管理员身份运行 Docker Desktop

### Q2: 内存不足，容器频繁重启
**解决方法**：
1. 降低 JVM 内存参数（见步骤5）
2. 关闭不必要的 Windows 服务
3. 增加服务器内存到 4G

### Q3: 无法从外网访问
**解决方法**：
1. 检查云服务器安全组是否开放 80/443 端口
2. 检查 Windows 防火墙设置
3. 确认容器已正常启动：`docker-compose ps`

### Q4: 部署后访问很慢
**解决方法**：
1. 检查带宽使用率（4M带宽较小）
2. 查看服务器资源占用（任务管理器）
3. 考虑升级到更高带宽

---

## 💰 成本优化建议

### 如果预算紧张：
1. **使用新用户优惠**：约￥96/年（2核2G）
2. **选择按量付费**：用多少付多少（适合测试）
3. **升级时机**：等业务稳定后再升级到 2核4G

### 如果追求更好体验：
直接选择 **2核4G** 配置（约￥68-98/月），长期稳定运行更省心

---

## 🎯 下一步建议

1. **测试基本功能**：注册、登录、天气查询
2. **配置域名**（可选）：购买域名并解析到服务器IP
3. **设置自动备份**：定期备份数据库
4. **监控系统状态**：使用任务管理器查看资源使用

---

## 📞 技术支持

如果遇到 Windows 特有问题：
- Docker Desktop 文档：https://docs.docker.com/desktop/
- Windows Server 论坛：https://techcommunity.microsoft.com/
- 云服务器厂商工单支持

---

**最后更新**: 2026-04-10  
**适用系统**: Windows Server 2019/2022  
**推荐配置**: 2核4G（2核2G需优化）
