# 智观天象 AI - Vue项目运行指南

## 项目概述
本项目已完成Vue 3框架迁移，采用现代化的组件化架构。

## 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0 或 pnpm >= 7.0.0

## 安装步骤

### 1. 安装Node.js
如果您的系统尚未安装Node.js，请访问：
- 官网：https://nodejs.org/
- 下载LTS版本（推荐）
- 按照安装向导完成安装

### 2. 验证安装
```bash
node --version
npm --version
```

### 3. 安装项目依赖
```bash
cd d:\jishe\jishe02\frontend
npm install
```

### 4. 启动开发服务器
```bash
npm run dev
```

服务器将在 http://localhost:3000 启动

## 项目结构

```
jishe02/frontend/
├── src/
│   ├── components/          # Vue组件
│   │   ├── ai/             # AI交互组件
│   │   ├── charts/         # 图表组件
│   │   ├── common/         # 通用组件
│   │   └── home/           # 首页组件
│   ├── views/              # 页面视图
│   ├── stores/             # Pinia状态管理
│   ├── utils/              # 工具函数
│   ├── router/             # 路由配置
│   ├── styles/             # 全局样式
│   ├── App.vue             # 根组件
│   └── main.js             # 入口文件
├── index.html              # HTML入口
├── vite.config.js          # Vite配置
└── package.json            # 项目配置
```

## 可用脚本

```bash
# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 代码检查（如果配置了）
npm run lint
```

## 功能特性

### ✅ 已实现功能
- [x] 实时天气展示
- [x] 24小时预报
- [x] 7日气温趋势图
- [x] 气象地图可视化
- [x] AI智能助手
- [x] 多模态识图上传
- [x] 生活指数推荐
- [x] 主题切换（浅色/深色/自动）
- [x] 响应式布局
- [x] 智能分析页面
- [x] 异常预警系统

### 🎨 设计特点
- Glassmorphism（玻璃拟态）设计风格
- 流畅的动画过渡
- 响应式布局适配多设备
- 无障碍访问支持

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite 5
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **图表**: ECharts 5
- **样式**: CSS Variables + Scoped CSS

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 开发建议

### 推荐IDE设置
- VSCode + Volar插件
- 启用TypeScript支持（可选）

### 代码规范
- 使用Composition API
- 组件使用`<script setup>`语法
- 样式使用scoped CSS
- 遵循Vue风格指南

## 故障排除

### 端口被占用
```bash
# 修改vite.config.js中的端口
server: {
  port: 3001  # 改为其他端口
}
```

### 依赖安装失败
```bash
# 清除缓存重新安装
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### 构建错误
```bash
# 检查Node版本
node --version  # 确保 >= 16

# 更新依赖
npm update
```

## 性能优化

项目已进行以下优化：
- 路由懒加载
- 组件按需导入
- 图表实例正确销毁
- CSS变量主题系统
- 响应式数据缓存

## 联系支持

如遇到问题，请检查：
1. Node.js版本是否符合要求
2. 依赖是否完整安装
3. 端口是否被占用
4. 浏览器控制台是否有错误信息

---

**项目版本**: 1.0.0  
**最后更新**: 2026-03-26  
**维护团队**: SkyGazer Team
