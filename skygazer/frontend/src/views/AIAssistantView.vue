<template>
  <div class="ai-assistant-view">
    <aside class="sidebar" :class="{ 'sidebar-open': sidebarOpen }">
      <div class="sidebar-header">
        <div class="sidebar-brand">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
          </svg>
          <span>天象智囊</span>
        </div>
        <button class="new-chat-btn" @click="newChat" title="新建对话">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          新建
        </button>
      </div>

      <div class="sidebar-search">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
          <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
          <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <input v-model="searchQuery" type="text" placeholder="搜索对话" aria-label="搜索对话" />
      </div>

      <div class="chat-history">
        <template v-if="filteredHistory.length">
          <div class="history-group" v-for="group in groupedHistory" :key="group.key">
            <span class="group-title">{{ group.label }}</span>
            <div
              v-for="item in group.items"
              :key="item.id"
              class="history-item"
              :class="{ active: currentChatId === item.id }"
              @click="loadChat(item)"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M21 11.5C21 16.1944 16.9706 20 12 20C10.4607 20 9.01133 19.6565 7.74467 19.0511L3 20L4.39499 16.28C3.51156 14.9923 3 13.5104 3 11.5C3 6.80558 7.02944 3 12 3C16.9706 3 21 6.80558 21 11.5Z" stroke="currentColor" stroke-width="2"/>
              </svg>
              <span class="item-title">{{ item.title }}</span>
              <button class="delete-btn" @click.stop="deleteChat(item.id)" title="删除" aria-label="删除对话">✕</button>
            </div>
          </div>
        </template>

        <div class="empty-history" v-else>
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M21 11.5C21 16.1944 16.9706 20 12 20C10.4607 20 9.01133 19.6565 7.74467 19.0511L3 20L4.39499 16.28C3.51156 14.9923 3 13.5104 3 11.5C3 6.80558 7.02944 3 12 3C16.9706 3 21 6.80558 21 11.5Z" stroke="currentColor" stroke-width="1.5"/>
          </svg>
          <span>{{ searchQuery ? '未找到匹配的对话' : '还没有对话，开始提问吧' }}</span>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="status-dot" :class="{ live: chat.isGenerating }"></div>
        <span>{{ chat.isGenerating ? '智能体生成中…' : '智能体已就绪' }}</span>
      </div>
    </aside>

    <main class="main-content">
      <header class="main-header">
        <button class="toggle-sidebar-btn" @click="sidebarOpen = !sidebarOpen" title="切换侧边栏" aria-label="切换侧边栏">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M3 12H21M3 12L9 6M3 12L9 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <h2 class="page-title">天象智囊</h2>

        <div class="header-tools">
          <button class="tools-hint" @click="showTools = !showTools" @mouseenter="showTools = true" @mouseleave="showTools = false" aria-label="可用工具">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M14.7 6.3a4 4 0 00-5.4 5.4L3 18v3h3l6.3-6.3a4 4 0 005.4-5.4l-2.5 2.5-2-2 2.5-2.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            工具
          </button>
          <div class="tools-pop" v-if="showTools" @mouseenter="showTools = true" @mouseleave="showTools = false">
            <div class="tools-pop-title">智能体可用工具</div>
            <div class="tools-pop-hint">点击工具可直接发起对应查询</div>
            <div class="tool-row" v-for="t in tools" :key="t.name" @click="runTool(t.key)" role="button" tabindex="0" @keydown.enter.prevent="runTool(t.key)">
              <span class="tool-ico" :class="'tool-' + t.key">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <path d="M4 19.5A2.5 2.5 0 016.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
              <div class="tool-text">
                <div class="tool-name">{{ t.name }}</div>
                <div class="tool-desc">{{ t.desc }}</div>
              </div>
              <span class="tool-go" aria-hidden="true">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </div>
          </div>
        </div>
      </header>

      <div class="welcome-section" v-if="chat.messages.filter((m) => m.isUser).length === 0 && !pendingImage">
        <div class="welcome-content">
          <div class="welcome-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="var(--blue-500)"/>
            </svg>
          </div>
          <h3 class="welcome-text">你好，我是天象智囊</h3>
          <p class="welcome-desc">你的专业气象智能体，可查询天气、分析气象数据、提供活动建议，并标注知识来源。</p>

          <div
            class="featured-card"
            role="button"
            tabindex="0"
            @click="triggerImage"
            @keydown.enter.prevent="triggerImage"
          >
            <div class="featured-icon">
              <svg width="30" height="30" viewBox="0 0 24 24" fill="none">
                <path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 14m2-2l1.586-1.586a2 2 0 012.828 0L22 10" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <rect x="3" y="4" width="18" height="16" rx="3" stroke="#fff" stroke-width="2"/>
                <circle cx="9" cy="9" r="1.6" stroke="#fff" stroke-width="2"/>
              </svg>
            </div>
            <div class="featured-body">
              <span class="featured-badge">亮点功能</span>
              <h4 class="featured-title">拍天问雨 · 上传照片秒识天气</h4>
              <p class="featured-desc">拍一张天空或风景照片，AI 立即判断晴、阴、雨、雪，并给出四分类概率分布。</p>
            </div>
            <button class="featured-btn" @click.stop="triggerImage" aria-label="立即体验拍天问雨">
              立即体验
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>

          <div class="quick-cards">
            <button class="quick-card" v-for="q in quickActions" :key="q.q" @click="quickAsk(q)">
              <span class="quick-ico" v-html="q.icon"></span>
              <span>{{ q.q }}</span>
            </button>
          </div>
        </div>
      </div>

        <div class="chat-container" v-else @dragover.prevent="onDragOver" @dragleave="onDragLeave" @drop.prevent="onDrop">
          <div class="chat-messages" ref="messagesContainer">
            <AgentMessage
              v-for="(message, idx) in chat.messages"
              :key="message.id"
              :message="message"
              :is-streaming="chat.isStreaming"
              :is-last="idx === chat.messages.length - 1"
              @retry="handleRetry"
            />

            <div v-if="chat.isTyping && !chat.isStreaming" class="message ai-message">
              <div class="message-avatar ai-avatar">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
                </svg>
              </div>
              <div class="message-body">
                <div class="typing-bubble">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>

          <div class="drop-overlay" v-if="dragActive">
            <div class="drop-inner">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                <path d="M21 15V19A2 2 0 0119 21H5A2 2 0 013 19V15M12 3V15M7 8l5-5 5 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>松开以识别天气图片</span>
            </div>
          </div>
        </div>

        <div class="chat-input-container">
          <div class="pending-image" v-if="pendingImage">
            <img :src="pendingImage.url" class="pending-thumb" alt="待分析图片" />
            <div class="pending-meta">
              <span class="pending-name">已选择天气图片</span>
              <span class="pending-tip">将结合下方文字一起分析，点击发送即可得到天气识别结果</span>
            </div>
            <button class="pending-remove" @click="removePendingImage" title="移除图片" aria-label="移除图片">✕</button>
          </div>

          <div class="input-wrapper">
              <button
                class="img-btn"
                @click="triggerImage"
                :disabled="chat.isGenerating"
                title="上传天气图片"
                aria-label="上传天气图片"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 14m2-2l1.586-1.586a2 2 0 012.828 0L22 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <rect x="3" y="4" width="18" height="16" rx="3" stroke="currentColor" stroke-width="2"/>
                  <circle cx="9" cy="9" r="1.6" stroke="currentColor" stroke-width="2"/>
                </svg>
              </button>
              <textarea
                v-model="inputMessage"
                class="chat-input"
                rows="1"
                placeholder="询问天气、穿衣或行程建议…（Enter 发送，Shift+Enter 换行）"
                @keydown.enter.exact.prevent="sendMessage"
                @input="autoGrow"
                ref="inputEl"
              ></textarea>
              <button
                v-if="chat.isGenerating"
                class="stop-btn"
                @click="chat.stopGeneration()"
                title="停止生成"
                aria-label="停止生成"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <rect x="6" y="6" width="12" height="12" rx="2"/>
                </svg>
              </button>
              <button v-else class="send-btn" @click="sendMessage" :disabled="(!inputMessage.trim() && !pendingImage)" aria-label="发送消息">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
            <div class="input-hint">
              <span>天象智囊可能会出错，重要决策请结合官方气象信息。</span>
              <span v-if="chat.isGenerating" class="hint-stop" @click="chat.stopGeneration()">停止生成</span>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              class="hidden-file-input"
              @change="onFileSelected"
            />
          </div>
    </main>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, onMounted, computed } from 'vue'
import { useChatStore } from '@/stores/chat'
import AgentMessage from '@/components/ai/AgentMessage.vue'

const chat = useChatStore()
const inputMessage = ref('')
const messagesContainer = ref(null)
const inputEl = ref(null)
const fileInput = ref(null)
const dragActive = ref(false)
const pendingImage = ref(null)
const sidebarOpen = ref(true)
const currentChatId = ref(null)
const searchQuery = ref('')
const showTools = ref(false)

const chatHistory = ref([])

const tools = [
  { key: 'weather', name: '实时天气', desc: '查询指定城市最新实况气象', action: 'weather' },
  { key: 'alert', name: '气象预警', desc: '获取当前气象灾害预警信息', action: 'alert' },
  { key: 'rag', name: '知识库检索', desc: '从气象知识库检索相关来源', action: 'rag' },
  { key: 'life', name: '生活指数', desc: '出行、穿衣与活动决策建议', action: 'life' }
]

const quickActions = [
  { q: '今天天气怎么样？', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="5" stroke="currentColor" stroke-width="2"/><path d="M12 2V4M12 20V22M2 12H4M20 12H22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>' },
  { q: '明天会下雨吗？', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M19 16.9A5 5 0 0018 7h-1.26A8 8 0 104 15.25" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M13 11l-4 6h6l-4 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>' },
  { q: '适合户外运动吗？', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M9 12L11 14L15 10M12 3L13.91 4.87L16.5 4.21L17.22 6.78L19.79 7.5L19.13 10.09L21 12L19.13 13.91L19.79 16.5L17.22 17.22L16.5 19.79L13.91 19.13L12 21L10.09 19.13L7.5 19.79L6.78 17.22L4.21 16.5L4.87 13.91L3 12L4.87 10.09L4.21 7.5L6.78 6.78L7.5 4.21L10.09 4.87L12 3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>' },
  { q: '上传图片识别天气', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M4 16l4.586-4.586a2 2 0 012.828 0L16 14m2-2l1.586-1.586a2 2 0 012.828 0L22 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><rect x="3" y="4" width="18" height="16" rx="3" stroke="currentColor" stroke-width="2"/><circle cx="9" cy="9" r="1.6" stroke="currentColor" stroke-width="2"/></svg>', image: true }
]

const loadChatHistory = () => {
  try {
    const saved = localStorage.getItem('skygazer_chat_history')
    if (saved) chatHistory.value = JSON.parse(saved)
  } catch (e) {
    console.error('加载聊天记录失败:', e)
  }
}

const saveChatHistory = () => {
  try {
    localStorage.setItem('skygazer_chat_history', JSON.stringify(chatHistory.value))
  } catch (e) {
    console.error('保存聊天记录失败:', e)
  }
}

const saveCurrentChat = () => {
  if (chat.messages.length === 0) return
  const firstUserMsg = chat.messages.find((m) => m.isUser)
  if (!firstUserMsg) return
  const title = firstUserMsg.content.substring(0, 30) + (firstUserMsg.content.length > 30 ? '…' : '')

  if (currentChatId.value) {
    const idx = chatHistory.value.findIndex((c) => c.id === currentChatId.value)
    if (idx !== -1) {
      chatHistory.value[idx].messages = [...chat.messages]
      chatHistory.value[idx].title = title
      chatHistory.value[idx].updatedAt = new Date().toISOString()
    }
  } else {
    currentChatId.value = 'chat_' + Date.now()
    chatHistory.value.unshift({
      id: currentChatId.value,
      title,
      messages: [...chat.messages],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    })
  }
  saveChatHistory()
}

const loadChat = (item) => {
  saveCurrentChat()
  currentChatId.value = item.id
  chat.setMessages(item.messages || [])
}

const deleteChat = (id) => {
  chatHistory.value = chatHistory.value.filter((c) => c.id !== id)
  saveChatHistory()
  if (currentChatId.value === id) {
    currentChatId.value = null
    chat.clearMessages()
  }
}

const newChat = () => {
  saveCurrentChat()
  currentChatId.value = null
  chat.clearMessages()
}

const sendMessage = async () => {
  if (chat.isGenerating) return

  // 有待发送图片时：图片 + 引导语一起提交，走天气识别分析流程
  if (pendingImage.value) {
    const file = pendingImage.value.file
    const text = inputMessage.value.trim() || '分析此图片天气'
    inputMessage.value = ''
    autoGrow()
    pendingImage.value = null
    if (!currentChatId.value) currentChatId.value = 'chat_' + Date.now()
    await chat.sendImageWithText(file, text)
    setTimeout(() => saveCurrentChat(), 500)
    await nextTick()
    scrollToBottom()
    return
  }

  const message = inputMessage.value.trim()
  if (!message) return
  inputMessage.value = ''
  autoGrow()
  if (!currentChatId.value) currentChatId.value = 'chat_' + Date.now()
  await chat.sendMessage(message)
  setTimeout(() => saveCurrentChat(), 500)
  await nextTick()
  scrollToBottom()
}

const handleRetry = async () => {
  await chat.retryLast()
  setTimeout(() => saveCurrentChat(), 500)
  await nextTick()
  scrollToBottom()
}

const quickAsk = (item) => {
  if (item.image) {
    triggerImage()
    return
  }
  inputMessage.value = item.q
  sendMessage()
}

const runTool = async (key) => {
  showTools.value = false
  const prompts = {
    weather: '今天天气怎么样？',
    alert: '当前有哪些气象预警？',
    rag: '介绍一下气象知识库能查到哪些内容？',
    life: '适合户外运动吗？'
  }
  const q = prompts[key]
  if (!q) return
  if (!currentChatId.value) currentChatId.value = 'chat_' + Date.now()
  await chat.sendMessage(q)
  setTimeout(() => saveCurrentChat(), 500)
  await nextTick()
  scrollToBottom()
}

const triggerImage = () => {
  if (chat.isGenerating) return
  fileInput.value && fileInput.value.click()
}

const onFileSelected = (e) => {
  const file = e.target.files && e.target.files[0]
  if (file) handleImageFile(file)
  e.target.value = ''
}

const onDragOver = () => {
  dragActive.value = true
}

const onDragLeave = (e) => {
  if (e.currentTarget && e.currentTarget.contains(e.relatedTarget)) return
  dragActive.value = false
}

const onDrop = (e) => {
  dragActive.value = false
  const file = e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files[0]
  if (file) handleImageFile(file)
}

// 选中/拖入图片后：预填引导语并展示缩略图，等用户点发送再分析
const handleImageFile = async (file) => {
  if (chat.isGenerating) return
  await attachImage(file)
}

const attachImage = async (file) => {
  if (!file) return
  if (!file.type || !file.type.startsWith('image/')) {
    alert('请选择图片文件（jpg/png/bmp/webp）')
    return
  }
  const url = URL.createObjectURL(file)
  if (pendingImage.value && pendingImage.value.url) {
    URL.revokeObjectURL(pendingImage.value.url)
  }
  pendingImage.value = { file, url }
  if (!inputMessage.value.trim()) {
    inputMessage.value = '分析此图片天气'
  }
  await nextTick()
  autoGrow()
  if (inputEl.value) inputEl.value.focus()
}

const removePendingImage = () => {
  if (pendingImage.value && pendingImage.value.url) {
    URL.revokeObjectURL(pendingImage.value.url)
  }
  pendingImage.value = null
  if (inputEl.value) inputEl.value.focus()
}

const autoGrow = async () => {
  await nextTick()
  if (inputEl.value) {
    inputEl.value.style.height = 'auto'
    inputEl.value.style.height = Math.min(inputEl.value.scrollHeight, 140) + 'px'
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const filteredHistory = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return chatHistory.value
  return chatHistory.value.filter((c) => c.title.toLowerCase().includes(q))
})

const groupedHistory = computed(() => {
  const today = new Date().toDateString()
  const yesterday = new Date(Date.now() - 86400000).toDateString()
  const groups = [
    { key: 'today', label: '今天', items: [] },
    { key: 'yesterday', label: '昨天', items: [] },
    { key: 'older', label: '更早', items: [] }
  ]
  filteredHistory.value.forEach((c) => {
    const date = new Date(c.createdAt).toDateString()
    if (date === today) groups[0].items.push(c)
    else if (date === yesterday) groups[1].items.push(c)
    else groups[2].items.push(c)
  })
  return groups.filter((g) => g.items.length)
})

watch(
  () => chat.messages.length,
  () => nextTick(scrollToBottom)
)

onMounted(() => {
  if (window.innerWidth <= 768) sidebarOpen.value = false
  loadChatHistory()
  scrollToBottom()
})
</script>

<style scoped>
.ai-assistant-view {
  position: relative;
  display: flex;
  height: calc(100vh - 120px);
  background: var(--bg-primary, var(--surface-1));
  border-radius: 1rem;
  overflow: hidden;
}

.sidebar {
  width: 268px;
  min-width: 268px;
  background: var(--ai-sidebar-bg, rgba(255, 255, 255, 0.72));
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease, min-width 0.3s ease;
  border-right: 1px solid var(--glass-border, rgba(255, 255, 255, 0.4));
}

.sidebar:not(.sidebar-open) {
  width: 0;
  min-width: 0;
  overflow: hidden;
}

.sidebar-header {
  padding: 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--glass-border, rgba(255, 255, 255, 0.35));
}

.sidebar-brand {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.sidebar-brand svg { color: var(--blue-500); }

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.4rem 0.7rem;
  background: var(--blue-500);
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.15s ease;
}

.new-chat-btn:hover { background: var(--blue-600); }
.new-chat-btn:active { transform: scale(0.97); }

.sidebar-search {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0.75rem 0.75rem 0.25rem;
  padding: 0.5rem 0.7rem;
  background: var(--surface-2);
  border: 1px solid var(--border-color, rgba(59, 130, 246, 0.15));
  border-radius: 10px;
  color: var(--text-muted);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.sidebar-search:focus-within {
  border-color: var(--blue-400);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
}

.sidebar-search input {
  border: none;
  background: none;
  outline: none;
  font-size: 0.8125rem;
  color: var(--text-primary);
  width: 100%;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem 0.75rem;
}

.chat-history::-webkit-scrollbar { width: 4px; }
.chat-history::-webkit-scrollbar-thumb { background: rgba(59, 130, 246, 0.25); border-radius: 2px; }

.history-group { margin-bottom: 0.75rem; }

.group-title {
  font-size: 0.6875rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 0.25rem 0.5rem;
  display: block;
  margin-bottom: 0.25rem;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.55rem 0.7rem;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
  color: var(--text-secondary);
  position: relative;
}

.history-item:hover { background: rgba(59, 130, 246, 0.08); color: var(--text-primary); }
.history-item.active { background: rgba(59, 130, 246, 0.14); color: var(--text-primary); }
.history-item svg { flex-shrink: 0; opacity: 0.55; }

.item-title {
  font-size: 0.8125rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.delete-btn {
  opacity: 0;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 0.7rem;
  padding: 0.1rem 0.3rem;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.history-item:hover .delete-btn { opacity: 1; }
.delete-btn:hover { background: rgba(239, 68, 68, 0.18); color: #ef4444; }

.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 2rem 1rem;
  color: var(--text-muted);
}
.empty-history span { font-size: 0.8125rem; text-align: center; }

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-top: 1px solid var(--glass-border, rgba(255, 255, 255, 0.35));
  font-size: 0.75rem;
  color: var(--text-muted);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--green-500, #22c55e);
  flex-shrink: 0;
}
.status-dot.live { background: var(--blue-500); animation: pulse 1.4s ease-in-out infinite; }

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.25); }
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--bg-primary, var(--surface-1));
}

.main-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1.5rem;
  border-bottom: 1px solid var(--glass-border, rgba(255, 255, 255, 0.4));
}

.toggle-sidebar-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.4rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease, color 0.2s ease;
}
.toggle-sidebar-btn:hover { background: rgba(59, 130, 246, 0.1); color: var(--text-primary); }

.page-title { font-size: 1.0625rem; font-weight: 600; color: var(--text-primary); flex: 1; }

.header-tools { position: relative; }

.tools-hint {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.4rem 0.7rem;
  background: rgba(59, 130, 246, 0.08);
  border: 1px solid rgba(59, 130, 246, 0.16);
  border-radius: 8px;
  color: var(--blue-600);
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}
.tools-hint:hover { background: rgba(59, 130, 246, 0.16); }

.tools-pop {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 256px;
  padding: 0.75rem;
  background: var(--card-bg, #fff);
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.4));
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.14);
  z-index: 50;
  animation: popIn 0.18s ease-out;
}

.tools-pop-title {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.tool-row {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.4rem 0;
}
.tool-ico {
  width: 26px;
  height: 26px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(59, 130, 246, 0.12);
  color: var(--blue-600);
}
.tool-name { font-size: 0.8125rem; font-weight: 600; color: var(--text-primary); }
.tool-desc { font-size: 0.6875rem; color: var(--text-muted); }

.tools-pop-hint {
  font-size: 0.6875rem;
  color: var(--text-muted);
  margin-bottom: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--glass-border, rgba(255, 255, 255, 0.35));
}

.tool-row {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.45rem 0.4rem;
  margin: 0 -0.4rem;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.12s ease;
}

.tool-row:hover { background: rgba(59, 130, 246, 0.1); }
.tool-row:active { transform: scale(0.98); }
.tool-row:focus-visible { outline: 2px solid var(--blue-400, #60a5fa); outline-offset: 1px; }

.tool-text { flex: 1; min-width: 0; }

.tool-go {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  color: var(--blue-600);
  opacity: 0;
  transform: translateX(-4px);
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.tool-row:hover .tool-go,
.tool-row:focus-visible .tool-go {
  opacity: 1;
  transform: translateX(0);
}

.welcome-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.welcome-content {
  max-width: 680px;
  width: 100%;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.25rem;
}

.welcome-icon { animation: float 3s ease-in-out infinite; }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.welcome-text { font-size: 1.625rem; font-weight: 700; color: var(--text-primary); }
.welcome-desc { font-size: 0.9375rem; color: var(--text-muted); max-width: 460px; }

.featured-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  width: 100%;
  margin-top: 0.25rem;
  padding: 1.1rem 1.35rem;
  border: none;
  border-radius: 16px;
  background: linear-gradient(120deg, #34E3E0 0%, #06b6d4 100%);
  color: #fff;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 10px 28px rgba(8, 145, 178, 0.28);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  animation: featIn 0.4s ease-out both;
}

.featured-card:hover,
.featured-card:focus-visible {
  transform: translateY(-3px);
  box-shadow: 0 14px 34px rgba(8, 145, 178, 0.36);
  outline: none;
}

.featured-card:active { transform: translateY(-1px) scale(0.99); }

@keyframes featIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.featured-icon {
  width: 54px;
  height: 54px;
  flex-shrink: 0;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.25);
}

.featured-body { flex: 1; min-width: 0; }

.featured-badge {
  display: inline-block;
  font-size: 0.625rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  padding: 0.15rem 0.5rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  margin-bottom: 0.35rem;
}

.featured-title {
  font-size: 1.0625rem;
  font-weight: 700;
  margin: 0 0 0.2rem;
  line-height: 1.25;
  color: #fff;
}

.featured-desc {
  font-size: 0.8125rem;
  margin: 0;
  color: rgba(255, 255, 255, 0.88);
  line-height: 1.5;
}

.featured-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.6rem 1.1rem;
  border: none;
  border-radius: 11px;
  background: var(--surface-2);
  color: var(--signal);
  font-size: 0.875rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.15s ease;
}

.featured-btn:hover { background: var(--surface-3); transform: translateY(-1px); }
.featured-btn:active { transform: translateY(0) scale(0.98); }

.quick-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
  width: 100%;
  margin-top: 0.5rem;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.9rem 1.1rem;
  background: var(--card-bg, #fff);
  border: 1px solid var(--border-color, rgba(59, 130, 246, 0.14));
  border-radius: 12px;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
  text-align: left;
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 500;
}

.quick-card:hover {
  border-color: var(--blue-400);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.14);
  transform: translateY(-2px);
}

.quick-ico { color: var(--blue-500); display: flex; flex-shrink: 0; }

.chat-container {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 1.25rem 1.5rem 0;
  min-height: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding-right: 0.5rem;
  scrollbar-width: thin;
  scrollbar-color: var(--blue-200, var(--text-muted)) transparent;
}
.chat-messages::-webkit-scrollbar { width: 6px; }
.chat-messages::-webkit-scrollbar-thumb { background: var(--blue-200, var(--text-muted)); border-radius: 3px; }

.typing-bubble {
  display: inline-flex;
  gap: 5px;
  padding: 0.85rem 1.1rem;
  background: var(--card-bg, #fff);
  border: 1px solid var(--border-color, rgba(59, 130, 246, 0.12));
  border-radius: 14px;
  border-top-left-radius: 4px;
  box-shadow: var(--shadow-sm);
}
.typing-bubble span {
  width: 7px;
  height: 7px;
  background: var(--text-muted);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}
.typing-bubble span:nth-child(2) { animation-delay: 0.2s; }
.typing-bubble span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-5px); opacity: 1; }
}

.chat-input-container {
  margin-top: 1rem;
  padding: 0.875rem 0 1.25rem;
  border-top: 1px solid var(--glass-border, rgba(255, 255, 255, 0.4));
}

.pending-image {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  max-width: 820px;
  margin: 0 auto 0.75rem;
  padding: 0.6rem 0.75rem;
  background: var(--card-bg, #fff);
  border: 1px solid var(--border-color, rgba(59, 130, 246, 0.18));
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.pending-thumb {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid var(--blue-200, #bfdbfe);
}

.pending-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.pending-name { font-size: 0.8125rem; font-weight: 600; color: var(--text-primary); }
.pending-tip { font-size: 0.6875rem; color: var(--text-muted); }

.pending-remove {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  cursor: pointer;
  font-size: 0.8rem;
  transition: background 0.2s ease;
}
.pending-remove:hover { background: rgba(239, 68, 68, 0.18); }

.input-wrapper {
  display: flex;
  gap: 0.6rem;
  max-width: 820px;
  margin: 0 auto;
  align-items: flex-end;
}

.chat-input {
  flex: 1;
  background: var(--card-bg, #fff);
  border: 2px solid var(--border-color, rgba(59, 130, 246, 0.18));
  border-radius: 16px;
  padding: 0.75rem 1.1rem;
  font-size: 0.9375rem;
  font-family: inherit;
  color: var(--text-primary);
  line-height: 1.5;
  resize: none;
  max-height: 140px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.chat-input:focus {
  outline: none;
  border-color: var(--blue-400);
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.12);
}
.chat-input::placeholder { color: var(--text-muted); }

.send-btn {
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--blue-500) 0%, var(--blue-600) 100%);
  border: none;
  width: 46px;
  height: 46px;
  border-radius: 14px;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.15s ease, box-shadow 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
}
.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}
.send-btn:disabled { opacity: 0.45; cursor: not-allowed; }

.stop-btn {
  flex-shrink: 0;
  background: #ef4444;
  border: none;
  width: 46px;
  height: 46px;
  border-radius: 14px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease, transform 0.15s ease;
  box-shadow: var(--shadow-md);
}
.stop-btn:hover { background: #dc2626; transform: translateY(-2px); }

.img-btn {
  flex-shrink: 0;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.22);
  width: 46px;
  height: 46px;
  border-radius: 14px;
  color: var(--blue-600);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease, transform 0.15s ease, border-color 0.2s ease;
}

.img-btn:hover:not(:disabled) {
  background: rgba(59, 130, 246, 0.18);
  border-color: rgba(59, 130, 246, 0.35);
  transform: translateY(-2px);
}

.img-btn:disabled { opacity: 0.45; cursor: not-allowed; }

.hidden-file-input {
  display: none;
}

.drop-overlay {
  position: absolute;
  inset: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.12);
  backdrop-filter: blur(2px);
  border: 2px dashed var(--blue-400, #60a5fa);
  border-radius: 1rem;
  animation: fadeIn 0.15s ease-out;
  pointer-events: none;
}

.drop-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  color: var(--blue-600);
  font-weight: 600;
  font-size: 0.9375rem;
}

.input-hint {
  display: flex;
  justify-content: space-between;
  max-width: 820px;
  margin: 0.5rem auto 0;
  font-size: 0.6875rem;
  color: var(--text-muted);
}
.hint-stop {
  color: #ef4444;
  cursor: pointer;
  font-weight: 600;
}
.hint-stop:hover { text-decoration: underline; }

@media (max-width: 768px) {
  .ai-assistant-view { height: calc(100vh - 100px); }
  .sidebar {
    position: absolute;
    z-index: 100;
    height: calc(100vh - 100px);
    box-shadow: 0 0 24px rgba(15, 23, 42, 0.18);
  }
  .sidebar:not(.sidebar-open) { transform: translateX(-100%); }
  .quick-cards { grid-template-columns: 1fr; }

  .featured-card {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
    padding: 1.25rem 1.1rem;
  }
  .featured-icon { align-self: center; }
  .featured-btn { width: 100%; justify-content: center; }
}

@media (prefers-reduced-motion: reduce) {
  .welcome-icon, .typing-bubble span, .status-dot.live, .featured-card { animation: none; }
  .featured-card:hover, .featured-card:focus-visible, .featured-btn:hover { transform: none; }
}
</style>
