---
name: ai-assistant-ux-fixes
overview: 修复天气智囊(AI助手)页面的三个体验问题：① 首次打开无输入框（输入区需常驻）；② 右上角"工具"按钮点击无响应（工具行改为可点击执行）；③ "拍天问雨"上传识天气功能缺乏视觉重点（在欢迎区新增高亮主推卡片）。
design:
  styleKeywords:
    - Glassmorphism
    - 渐变高亮主推卡
    - 蓝青色调
    - 微动效
    - 可交互工具浮层
  fontSystem:
    fontFamily: PingFang SC
    heading:
      size: 20px
      weight: 700
    subheading:
      size: 14px
      weight: 600
    body:
      size: 14px
      weight: 400
  colorSystem:
    primary:
      - "#3b82f6"
      - "#2563eb"
      - "#06b6d4"
    background:
      - "#f8fafc"
      - "#ffffff"
      - "#eff6ff"
    text:
      - "#0f172a"
      - "#475569"
      - "#ffffff"
    functional:
      - "#22c55e"
      - "#ef4444"
      - "#f59e0b"
todos:
  - id: restructure-input-always-visible
    content: 重构 AIAssistantView 模板，将输入区移出 v-else 实现底部常驻
    status: completed
  - id: make-tools-clickable
    content: 为 tools 增加 action 与 runTool 方法，工具行可点击并加交互样式 [skill:impeccable]
    status: completed
    dependencies:
      - restructure-input-always-visible
  - id: featured-image-card
    content: 在欢迎区新增全宽渐变"拍天问雨"主推卡与立即体验按钮 [skill:impeccable]
    status: completed
    dependencies:
      - restructure-input-always-visible
  - id: verify-build-lint
    content: 运行前端 lint 与构建验证三处改动无回归
    status: completed
    dependencies:
      - make-tools-clickable
      - featured-image-card
---

## 用户需求

天气智囊（天象智囊 / AI 助手）页面存在三处体验问题，需要修复并优化：

## 核心问题

- **问题一：初始无输入框**：页面首次打开（无任何用户消息）时只显示欢迎区，底部输入框被 `v-else` 隐藏，必须发送首条消息后才出现，导致"不知如何开始"。
- **问题二：右上角"工具"按钮点击无响应**：该按钮仅弹出一个说明性浮层，列出"实时天气/气象预警/知识库检索/生活指数"四个工具，但工具行没有任何点击行为，用户点击后无任何动作，与"可点击使用"的直觉不符。
- **问题三："拍天问雨"功能不突出**：上传图片识别天气（后端链路、store `sendImageMessage`、API `weatherImageApi.predict`、`WeatherImageCard` 组件均已就绪且可用）目前仅作为一个普通快捷卡片存在，缺少视觉重点，用户难以发现。

## 产品概述

在现有"天象智囊"聊天页内完成三处前端体验修复：输入框常驻底部、工具按钮可点击直接触发对应智能体能力、在欢迎区以高亮主推卡片重点呈现"拍天问雨"功能。本次仅改动前端页面，不改动后端。

## 技术栈选择

- 前端：Vue 3 SFC + Pinia（`useChatStore`）+ 组件内 scoped CSS（沿用现有栈，无新增依赖）
- 后端：本次不改动（图片识别链路 `WeatherImageController`/`WeatherImageService`/`weatherImageApi` 已就绪）

## 实施方案

### 整体策略

仅修改 `skygazer/frontend/src/views/AIAssistantView.vue` 一个文件，复用既有能力（`chat.sendMessage`、`quickAsk`、`triggerImage`、`sendImageMessage`），不新增文件、不新增 API。

### 关键修改点

1. **输入区常驻（修复问题一）**

- 将 `.chat-input-container`（含 `img-btn`、textarea、`send-btn`、隐藏 `fileInput`）从 `.chat-container` 内部移出，作为 `.main-content` 下与 `.welcome-section` / `.chat-container` 平级的常驻节点。
- `.welcome-section` 与 `.chat-container` 仍通过 `v-if` / `v-else` 互斥（均 `flex:1`），输入区始终在底部可见。拖拽遮罩 `.drop-overlay` 仍留在 `.chat-container` 内（`position:absolute; inset:0`），仅聊天态生效，符合预期。

2. **工具按钮可点击（修复问题二）**

- 为 `tools` 数组每项增加 `action` 字段（如 `weather`/`alert`/`rag`/`life`）。
- 新增 `runTool(key)` 方法：根据 `key` 调用既有动作——实时天气/生活指数走 `quickAsk` 预置问句，气象预警/知识库检索走 `chat.sendMessage` 对应提示语；执行后关闭浮层（`showTools=false`）。
- `.tool-row` 增加 `@click="runTool(t.key)"`、pointer 光标与 `hover`/`:active` 反馈样式，浮层加"点击可直接使用"文案。

3. **拍天问雨重点呈现（修复问题三）**

- 在 `.welcome-section` 的 4 张普通快捷卡之上，新增一张全宽、渐变高亮、带相机图标的"拍天问雨"主推卡，含标题、一句说明与"立即体验"按钮，按钮调用 `triggerImage()`。
- 辅以轻微悬浮/呼吸微动效，与全局视觉风格一致。

### 实施注意

- 不破坏既有 `quickActions`（含 `image:true` 卡片）与拖拽上传逻辑。
- 欢迎态点击工具/拍天问雨会经由 `chat.sendMessage`/`sendImageMessage` 写入消息，触发 `v-else` 切到聊天态，行为自然。
- 性能无新增开销：所有改动为轻量 DOM 与事件绑定，无额外渲染负担。

## 架构与目录结构

本次只改一个文件，无新增模块：

```
skygazer/frontend/src/views/AIAssistantView.vue   # [MODIFY] 重构模板（输入区常驻）、tools 增加 action + runTool、欢迎区新增拍天问雨主推卡、补充相关样式
```

## 复用与依赖

- `stores/chat.js`：`sendMessage`、`sendImageMessage`（已就绪，无需改）
- `api/index.js`：`weatherImageApi.predict`（已就绪，无需改）
- `components/ai/WeatherImageCard.vue`：结果卡片（已就绪，无需改）

## 设计风格

在现有"天象智囊"蓝色玻璃拟态（Glassmorphism）基础上进行体验升级，保持蓝青色调与毛玻璃面板语言。重点通过"渐变高亮主推卡 + 可交互工具浮层"强化视觉层次与操作引导。

## 区块设计

### 顶部工具浮层（修复问题二）

- 浮层由纯展示升级为"可点击操作面板"：每项工具行（图标 + 名称 + 描述）整体可点击，鼠标悬停时背景加深、右侧出现"使用"箭头提示，按下有轻微缩放反馈。
- 浮层顶部增加一行小字说明："点击工具可直接发起对应查询"。
- 图标底色沿用蓝青渐变胶囊，保持与全局一致。

### 欢迎区"拍天问雨"主推卡（修复问题三）

- 位于 4 张普通快捷卡之上，全宽卡片，背景采用蓝→青对角渐变（#3b82f6 → #06b6d4），白色文字。
- 左侧大号相机/云朵图标（半透明白色），右侧为标题"拍天问雨"、副标题"上传一张天气照片，AI 秒辨晴/阴/雨/雪并给出概率"与"立即体验"按钮（白底蓝字、悬停上浮）。
- 卡片入场带轻微上浮微动效，提示这是本页亮点功能。

### 常驻输入区（修复问题一）

- 输入框从聊天态中解耦，作为页面底部常驻区域；欢迎态与聊天态均可见。
- 保留图片按钮（蓝青描边）、文本框、发送/停止按钮的现有风格，确保两态视觉连续。

## 响应式

- 桌面：主推卡横向布局（图标 + 文案 + 按钮）。
- 移动端（≤768px）：主推卡与快捷卡均改为纵向堆叠，按钮全宽。

## Agent Extensions

### Skill

- **impeccable**
- 用途：打磨"拍天问雨"主推卡与工具浮层的可视化设计（渐变配色、微动效、层级与可读性）及整体欢迎区/输入区视觉一致性
- 预期成果：产出高亮主推卡与可交互工具浮层的精致视觉方案，确保重点功能一眼可发现、工具可点可用，且风格与现有玻璃拟态一致