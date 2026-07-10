# 千问同义API配置验证指南

## 📋 配置概述

千问同义API已成功配置到项目中，配置详情如下：

### 配置文件位置
- **环境变量文件**: `d:\jishe\jishe02\config\.env`
- **Spring配置文件**: `d:\jishe\jishe02\backend\src\main\resources\application.yml`
- **Docker配置文件**: `d:\jishe\jishe02\docker\docker-compose.yml`

### API配置信息
- **API密钥**: 已配置（安全存储在.env文件中）
- **API基础URL**: `https://dashscope.aliyuncs.com/compatible-mode/v1`
- **默认模型**: `qwen-plus`（文本对话）
- **视觉模型**: `qwen-vl-plus`（图像分析）

## 🔒 安全措施

### 已实施的安全措施

1. **环境变量隔离**
   - API密钥存储在独立的`.env`文件中
   - 不在源代码中硬编码敏感信息
   - 使用Spring的`${OPENAI_API_KEY}`占位符引用

2. **Git版本控制保护**
   - 创建了`.gitignore`文件
   - 明确排除`config/.env`文件不被提交
   - 提供`.env.example`模板文件供参考

3. **配置文件权限**
   - `.env`文件包含敏感信息，不应共享
   - `.env.example`可安全提交到代码仓库

## ✅ 验证步骤

### 方法一：使用Python验证脚本（推荐）

#### 前置要求
- Python 3.6+
- requests库（如未安装，执行：`pip install requests`）

#### 执行验证
```powershell
cd d:\jishe\jishe02
python verify_api.py
```

#### 预期输出
```
🚀 开始验证千问同义API配置...
📁 配置文件路径: d:\jishe\jishe02\config\.env

============================================================
🔍 千问同义API连接测试
============================================================
✓ API密钥已配置: sk-b91c0e...eb7363c6
✓ API基础URL: https://dashscope.aliyuncs.com/compatible-mode/v1

📡 发送测试请求到: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
📝 请求数据: {...}

📊 响应状态码: 200

✅ API连接成功!

🤖 AI响应:
   你好！我是通义千问，由阿里云开发的大规模语言模型...

============================================================
🎉 千问同义API配置验证成功!
============================================================

✅ 所有验证通过,API配置正确!
```

### 方法二：启动后端服务验证

#### 步骤1：启动后端服务
```powershell
cd d:\jishe\jishe02\backend
& "D:\JavaSoft\Nodejs\npm.cmd" run start:backend
```

或使用Docker：
```powershell
cd d:\jishe\jishe02\docker
docker-compose -f docker-compose.yml --env-file ../config/.env up -d backend
```

#### 步骤2：检查启动日志
查看后端启动日志，确认以下信息：
```
✓ Spring AI配置加载成功
✓ OpenAI API Key已注入
✓ Base URL: https://dashscope.aliyuncs.com/compatible-mode/v1
✓ Model: qwen-plus
```

#### 步骤3：测试API端点
使用Postman或curl测试AI对话接口：

**请求示例**：
```bash
POST http://localhost:8080/api/ai/chat
Content-Type: application/json
Authorization: Bearer {your_jwt_token}

{
  "message": "你好，请介绍一下自己",
  "location": "北京",
  "style": "professional"
}
```

**预期响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "message": "你好！我是通义千问...",
    "style": "professional",
    "responseTimeMs": 1234,
    "modelUsed": "qwen-plus"
  }
}
```

### 方法三：前端集成测试

#### 步骤1：启动前后端服务
```powershell
# 启动后端
cd d:\jishe\jishe02\backend
mvn spring-boot:run

# 启动前端（新终端）
cd d:\jishe\jishe02\frontend
& "D:\JavaSoft\Nodejs\npm.cmd" run dev
```

#### 步骤2：访问前端应用
打开浏览器访问：`http://localhost:3000`

#### 步骤3：测试AI功能
1. 登录系统（使用测试账号：test/test123）
2. 进入AI助手页面
3. 发送测试消息："今天天气怎么样？"
4. 验证AI是否正常响应

## 🔧 故障排查

### 问题1：API密钥无效
**症状**：验证脚本返回401错误
**解决方案**：
1. 检查`.env`文件中的`OPENAI_API_KEY`是否正确
2. 确认密钥是否有效且未过期
3. 访问 https://dashscope.console.aliyun.com/ 验证密钥状态

### 问题2：网络连接失败
**症状**：连接超时或无法访问API
**解决方案**：
1. 检查网络连接
2. 确认防火墙未阻止访问
3. 验证`OPENAI_BASE_URL`配置正确

### 问题3：后端服务无法读取环境变量
**症状**：后端启动时报API密钥为空
**解决方案**：
1. 确认`.env`文件位于`config`目录
2. 检查文件格式（无BOM，UTF-8编码）
3. 验证环境变量名称与配置文件一致

### 问题4：Docker部署时环境变量未生效
**症状**：容器内API密钥为空
**解决方案**：
1. 确认`docker-compose.yml`中`env_file`路径正确
2. 验证环境变量传递配置
3. 使用`docker exec`进入容器检查环境变量

## 📊 配置验证清单

- [x] API密钥已配置到`.env`文件
- [x] `application.yml`正确引用环境变量
- [x] `docker-compose.yml`正确传递环境变量
- [x] `.gitignore`排除敏感文件
- [x] 创建API验证脚本
- [x] 更新代码使用千问模型
- [ ] 执行验证脚本确认连接成功
- [ ] 启动后端服务测试功能
- [ ] 前端集成测试完成

## 🎯 下一步操作

1. **立即验证**：运行`python verify_api.py`确认API连接
2. **启动服务**：启动后端服务测试完整功能
3. **前端测试**：通过前端界面测试AI对话功能
4. **生产部署**：确认配置正确后进行生产环境部署

## 📝 注意事项

1. **密钥安全**：
   - 切勿将`.env`文件提交到代码仓库
   - 定期更换API密钥
   - 为不同环境使用不同的密钥

2. **API配额**：
   - 监控API使用量，避免超额
   - 设置合理的调用频率限制
   - 实现缓存机制减少重复调用

3. **错误处理**：
   - 实现API调用失败的重试机制
   - 提供友好的错误提示
   - 记录详细的错误日志

## 📞 技术支持

如遇到问题，请检查：
1. 本文档的故障排查章节
2. 后端服务日志：`d:\jishe\jishe02\backend\logs\`
3. 千问同义API官方文档：https://help.aliyun.com/zh/dashscope/

---

**配置完成日期**: 2026年3月29日
**配置状态**: ✅ 已完成，待验证
