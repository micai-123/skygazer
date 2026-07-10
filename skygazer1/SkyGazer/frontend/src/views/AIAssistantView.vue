<template>
  <div class="ai-assistant-view">
    <div class="sidebar" :class="{ 'sidebar-open': sidebarOpen }">
      <div class="sidebar-header">
        <h3 class="sidebar-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
          </svg>
          天象智囊
        </h3>
        <button class="new-chat-btn" @click="newChat" title="新建对话">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          新建对话
        </button>
      </div>
      
      <div class="chat-history">
        <div class="history-group">
          <span class="group-title">今天</span>
          <div 
            v-for="item in todayChats" 
            :key="item.id"
            class="history-item"
            :class="{ active: currentChatId === item.id }"
            @click="loadChat(item)"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M21 11.5C21 16.1944 16.9706 20 12 20C10.4607 20 9.01133 19.6565 7.74467 19.0511L3 20L4.39499 16.28C3.51156 14.9923 3 13.5104 3 11.5C3 6.80558 7.02944 3 12 3C16.9706 3 21 6.80558 21 11.5Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <span class="item-title">{{ item.title }}</span>
            <button class="delete-btn" @click.stop="deleteChat(item.id)" title="删除">✕</button>
          </div>
        </div>
        
        <div class="history-group" v-if="yesterdayChats.length > 0">
          <span class="group-title">昨天</span>
          <div 
            v-for="item in yesterdayChats" 
            :key="item.id"
            class="history-item"
            :class="{ active: currentChatId === item.id }"
            @click="loadChat(item)"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M21 11.5C21 16.1944 16.9706 20 12 20C10.4607 20 9.01133 19.6565 7.74467 19.0511L3 20L4.39499 16.28C3.51156 14.9923 3 13.5104 3 11.5C3 6.80558 7.02944 3 12 3C16.9706 3 21 6.80558 21 11.5Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <span class="item-title">{{ item.title }}</span>
            <button class="delete-btn" @click.stop="deleteChat(item.id)" title="删除">✕</button>
          </div>
        </div>
        
        <div class="history-group" v-if="olderChats.length > 0">
          <span class="group-title">更早</span>
          <div 
            v-for="item in olderChats" 
            :key="item.id"
            class="history-item"
            :class="{ active: currentChatId === item.id }"
            @click="loadChat(item)"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M21 11.5C21 16.1944 16.9706 20 12 20C10.4607 20 9.01133 19.6565 7.74467 19.0511L3 20L4.39499 16.28C3.51156 14.9923 3 13.5104 3 11.5C3 6.80558 7.02944 3 12 3C16.9706 3 21 6.80558 21 11.5Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <span class="item-title">{{ item.title }}</span>
            <button class="delete-btn" @click.stop="deleteChat(item.id)" title="删除">✕</button>
          </div>
        </div>
        
        <div class="empty-history" v-if="chatHistory.length === 0">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M21 11.5C21 16.1944 16.9706 20 12 20C10.4607 20 9.01133 19.6565 7.74467 19.0511L3 20L4.39499 16.28C3.51156 14.9923 3 13.5104 3 11.5C3 6.80558 7.02944 3 12 3C16.9706 3 21 6.80558 21 11.5Z" stroke="currentColor" stroke-width="1.5"/>
          </svg>
          <span>暂无历史对话</span>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="main-header">
        <button class="toggle-sidebar-btn" @click="sidebarOpen = !sidebarOpen" title="切换侧边栏">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M3 12H21M3 12L9 6M3 12L9 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <h2 class="page-title">天象智囊</h2>
      </div>
      
      <div class="welcome-section" v-if="chat.messages.length === 0 && !chat.isTyping">
        <div class="welcome-content">
          <div class="welcome-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="var(--blue-500)"/>
            </svg>
          </div>
          <h3 class="welcome-text">你好，我是天象智囊</h3>
          <p class="welcome-desc">您的专业气象智能助手，可以帮您查询天气、分析气象数据、提供活动建议</p>
          
          <div class="quick-cards">
            <div class="quick-card" @click="quickAsk('今天天气怎么样？')">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="5" stroke="currentColor" stroke-width="2"/>
                <path d="M12 2V4M12 20V22M2 12H4M20 12H22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <span>今天天气怎么样？</span>
            </div>
            <div class="quick-card" @click="quickAsk('明天会下雨吗？')">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M19 16.9A5 5 0 0018 7h-1.26A8 8 0 104 15.25" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M13 11l-4 6h6l-4 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>明天会下雨吗？</span>
            </div>
            <div class="quick-card" @click="quickAsk('适合户外运动吗？')">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M9 12L11 14L15 10M12 3L13.91 4.87L16.5 4.21L17.22 6.78L19.79 7.5L19.13 10.09L21 12L19.13 13.91L19.79 16.5L17.22 17.22L16.5 19.79L13.91 19.13L12 21L10.09 19.13L7.5 19.79L6.78 17.22L4.21 16.5L4.87 13.91L3 12L4.87 10.09L4.21 7.5L6.78 6.78L7.5 4.21L10.09 4.87L12 3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>适合户外运动吗？</span>
            </div>
            <div class="quick-card" @click="quickAsk('今天穿什么衣服合适？')">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M20.38 3.4L16 2a4 4 0 01-8 0L3.62 3.4a2 2 0 00-1.34 2.23l.58 3.47a1 1 0 00.99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 002-2V10h2.15a1 1 0 00.99-.84l.58-3.47a2 2 0 00-1.34-2.23z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span>今天穿什么衣服合适？</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="chat-container glass-card" v-else>
        <div class="chat-messages" ref="messagesContainer">
          <div 
            v-for="message in chat.messages" 
            :key="message.id"
            class="message"
            :class="message.isUser ? 'user-message' : 'ai-message'"
          >
            <div class="message-avatar" :class="message.isUser ? 'user-avatar' : 'ai-avatar'">
              <svg v-if="message.isUser" width="18" height="18" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
                <path d="M4 20C4 16.6863 7.13401 14 12 14C16.866 14 20 16.6863 20 20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
              </svg>
            </div>
            <div class="message-body">
              <div class="message-header" v-if="!message.isUser">
                <span class="agent-name">{{ message.agentName || '天象智囊' }}</span>
                <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              </div>
              <div class="message-content">{{ message.content }}
              
              <div class="message-references" v-if="message.references && message.references.length > 0">
                <div class="references-title">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path d="M12 6.25278V19.2528M12 6.25278C10.8321 5.47686 9.24649 5 7.5 5C5.75351 5 4.16789 5.47686 3 6.25278V19.2528C4.16789 18.4769 5.75351 18 7.5 18C9.24649 18 10.8321 18.4769 12 19.2528M12 6.25278C13.1679 5.47686 14.7535 5 16.5 5C18.2465 5 19.8321 5.47686 21 6.25278V19.2528C19.8321 18.4769 18.2465 18 16.5 18C14.7535 18 13.1678 18.4769 12 19.2528" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  知识来源
                </div>
                <div class="reference-tags">
                  <span class="reference-tag" v-for="(ref, index) in message.references" :key="index">{{ ref }}</span>
                </div>
              </div>
              
              <div class="weather-context" v-if="message.weatherContext">
                <div class="context-title">当前天气</div>
                <div class="context-grid">
                  <div class="context-item"><span class="context-label">地点</span><span class="context-value">{{ message.weatherContext.location }}</span></div>
                  <div class="context-item"><span class="context-label">天气</span><span class="context-value">{{ message.weatherContext.weatherCondition }}</span></div>
                  <div class="context-item"><span class="context-label">温度</span><span class="context-value">{{ message.weatherContext.temperature }}℃</span></div>
                  <div class="context-item"><span class="context-label">空气质量</span><span class="context-value">{{ message.weatherContext.airQualityLevel }}</span></div>
                </div>
              </div>
              </div>
            </div>
          </div>
          
          <div v-if="chat.isTyping" class="message ai-message">
            <div class="message-avatar ai-avatar">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
              </svg>
            </div>
            <div class="message-body">
              <div class="message-content typing">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
          
          <div v-if="chat.isStreaming && !chat.isTyping" class="streaming-indicator">
            <div class="streaming-dots">
              <span></span><span></span><span></span>
            </div>
            <span class="streaming-text">正在生成回答...</span>
          </div>
        </div>
        
        <div class="chat-input-container">
          <div class="input-wrapper">
            <input 
              type="text" 
              id="chat-message-input"
              name="chat-message"
              class="chat-input" 
              v-model="inputMessage" 
              placeholder="输入您的问题..." 
              autocomplete="off"
              @keypress.enter="sendMessage"
            >
            <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim() || chat.isTyping || chat.isStreaming" aria-label="发送消息">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, onMounted, computed } from 'vue'
import { useChatStore } from '@/stores/chat'

const chat = useChatStore()
const inputMessage = ref('')
const messagesContainer = ref(null)
const sidebarOpen = ref(true)
const currentChatId = ref(null)

const chatHistory = ref([])

const todayChats = computed(() => {
  const today = new Date().toDateString()
  return chatHistory.value.filter(c => new Date(c.createdAt).toDateString() === today)
})

const yesterdayChats = computed(() => {
  const yesterday = new Date(Date.now() - 86400000).toDateString()
  const today = new Date().toDateString()
  return chatHistory.value.filter(c => {
    const date = new Date(c.createdAt).toDateString()
    return date === yesterday && date !== today
  })
})

const olderChats = computed(() => {
  const twoDaysAgo = new Date(Date.now() - 172800000).toDateString()
  return chatHistory.value.filter(c => new Date(c.createdAt).toDateString() < twoDaysAgo)
})

const loadChatHistory = () => {
  try {
    const saved = localStorage.getItem('skygazer_chat_history')
    if (saved) {
      chatHistory.value = JSON.parse(saved)
    }
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
  
  const firstUserMsg = chat.messages.find(m => m.isUser)
  if (!firstUserMsg) return
  
  const title = firstUserMsg.content.substring(0, 30) + (firstUserMsg.content.length > 30 ? '...' : '')
  
  if (currentChatId.value) {
    const idx = chatHistory.value.findIndex(c => c.id === currentChatId.value)
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
  chatHistory.value = chatHistory.value.filter(c => c.id !== id)
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
  const message = inputMessage.value.trim()
  if (!message || chat.isTyping) return
  
  inputMessage.value = ''
  
  if (!currentChatId.value) {
    currentChatId.value = 'chat_' + Date.now()
  }
  
  await chat.sendMessage(message)
  
  setTimeout(() => {
    saveCurrentChat()
  }, 500)
  
  await nextTick()
  scrollToBottom()
}

const quickAsk = (question) => {
  inputMessage.value = question
  sendMessage()
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

watch(() => chat.messages.length, () => {
  nextTick(scrollToBottom)
})

onMounted(() => {
  loadChatHistory()
  scrollToBottom()
})
</script>

<style scoped>
.ai-assistant-view {
  display: flex;
  height: calc(100vh - 120px);
  background: var(--bg-primary);
  border-radius: 1rem;
  overflow: hidden;
}

.sidebar {
  width: 260px;
  min-width: 260px;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
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
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-title {
  font-size: 1rem;
  font-weight: 700;
  color: white;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.sidebar-title svg {
  color: var(--blue-400);
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.45rem 0.85rem;
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 0.5rem;
  color: var(--blue-300);
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.new-chat-btn:hover {
  background: rgba(59, 130, 246, 0.3);
  transform: translateY(-1px);
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 0.75rem;
}

.chat-history::-webkit-scrollbar {
  width: 4px;
}

.chat-history::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 2px;
}

.history-group {
  margin-bottom: 1rem;
}

.group-title {
  font-size: 0.6875rem;
  color: rgba(255, 255, 255, 0.4);
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
  padding: 0.6rem 0.75rem;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  color: rgba(255, 255, 255, 0.7);
  position: relative;
}

.history-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.95);
}

.history-item.active {
  background: rgba(59, 130, 246, 0.2);
  color: white;
}

.history-item svg {
  flex-shrink: 0;
  opacity: 0.6;
}

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
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  font-size: 0.75rem;
  padding: 0.125rem 0.25rem;
  border-radius: 0.25rem;
  transition: all 0.2s ease;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
}

.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 2rem 1rem;
  color: rgba(255, 255, 255, 0.3);
}

.empty-history span {
  font-size: 0.8125rem;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--bg-primary);
}

.main-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.toggle-sidebar-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 0.5rem;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-sidebar-btn:hover {
  background: var(--hover-bg);
  color: var(--text-primary);
}

.page-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
}

.welcome-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.welcome-content {
  max-width: 720px;
  width: 100%;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
}

.welcome-icon {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.welcome-text {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
}

.welcome-desc {
  font-size: 1rem;
  color: var(--text-muted);
  max-width: 480px;
}

.quick-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
  width: 100%;
  margin-top: 1rem;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 0.875rem;
  cursor: pointer;
  transition: all 0.25s ease;
  text-align: left;
}

.quick-card:hover {
  border-color: var(--blue-400);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  transform: translateY(-2px);
}

.quick-card svg {
  color: var(--blue-500);
  flex-shrink: 0;
}

.quick-card span {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 500;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 1.5rem;
  min-height: 0;
  background: transparent;
  box-shadow: none;
  border: none;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding-right: 0.5rem;
  scrollbar-width: thin;
  scrollbar-color: var(--blue-200) transparent;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: var(--blue-200);
  border-radius: 3px;
}

.message {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.user-message {
  flex-direction: row-reverse;
}

.ai-message {
  flex-direction: row;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 0.75rem;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: var(--shadow-sm);
}

.user-avatar {
  background: linear-gradient(135deg, var(--blue-500) 0%, var(--blue-600) 100%);
}

.ai-avatar {
  background: linear-gradient(135deg, var(--blue-900) 0%, #1e3a5f 100%);
}

.message-body {
  max-width: 70%;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.agent-name {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--blue-700);
}

.message-time {
  font-size: 0.625rem;
  color: var(--text-muted);
}

.message-content {
  background: var(--card-bg);
  padding: 0.875rem 1.125rem;
  border-radius: 1rem;
  font-size: 0.9375rem;
  color: var(--text-primary);
  box-shadow: var(--shadow-sm);
  line-height: 1.6;
  border: 1px solid var(--border-color);
}

.user-message .message-content {
  border-top-right-radius: 0.25rem;
  background: linear-gradient(135deg, var(--blue-50) 0%, var(--blue-100) 100%);
  border-color: var(--blue-200);
  color: #111827;
}

.ai-message .message-content {
  border-top-left-radius: 0.25rem;
  background: var(--card-bg);
}

.message-content.typing {
  display: flex;
  gap: 5px;
  padding: 1rem 1.25rem;
  align-items: center;
}

.message-content.typing span {
  width: 8px;
  height: 8px;
  background: var(--text-muted);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.message-content.typing span:nth-child(1) { animation-delay: 0s; }
.message-content.typing span:nth-child(2) { animation-delay: 0.2s; }
.message-content.typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

.streaming-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  margin-top: 0.5rem;
  background: rgba(59, 130, 246, 0.05);
  border-radius: 0.5rem;
}

.streaming-dots {
  display: flex;
  gap: 4px;
}

.streaming-dots span {
  width: 6px;
  height: 6px;
  background: var(--blue-500);
  border-radius: 50%;
  animation: streaming 1.4s infinite ease-in-out;
}

.streaming-dots span:nth-child(1) { animation-delay: 0s; }
.streaming-dots span:nth-child(2) { animation-delay: 0.2s; }
.streaming-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes streaming {
  0%, 60%, 100% { transform: scale(1); opacity: 0.5; }
  30% { transform: scale(1.2); opacity: 1; }
}

.streaming-text {
  font-size: 0.75rem;
  color: var(--blue-600);
  font-weight: 500;
}

.message-references {
  margin-top: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: rgba(59, 130, 246, 0.06);
  border-radius: 0.5rem;
  border: 1px solid rgba(59, 130, 246, 0.12);
}

.references-title {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: var(--blue-600);
  margin-bottom: 0.375rem;
}

.reference-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
}

.reference-tag {
  font-size: 0.6875rem;
  padding: 0.125rem 0.5rem;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 0.25rem;
  color: var(--blue-700);
}

.weather-context {
  margin-top: 0.5rem;
  padding: 0.625rem 0.75rem;
  background: rgba(59, 130, 246, 0.04);
  border-radius: 0.5rem;
  border: 1px solid rgba(59, 130, 246, 0.1);
}

.context-title {
  font-size: 0.75rem;
  color: var(--blue-600);
  margin-bottom: 0.375rem;
  font-weight: 600;
}

.context-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.25rem 0.75rem;
}

.context-item {
  display: flex;
  gap: 0.5rem;
}

.context-label {
  font-size: 0.6875rem;
  color: var(--text-muted);
}

.context-value {
  font-size: 0.6875rem;
  color: var(--text-primary);
  font-weight: 500;
}

.chat-input-container {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.input-wrapper {
  display: flex;
  gap: 0.75rem;
  max-width: 800px;
  margin: 0 auto;
}

.chat-input {
  flex: 1;
  background: var(--card-bg);
  border: 2px solid var(--border-color);
  border-radius: 1.25rem;
  padding: 0.875rem 1.25rem;
  font-size: 0.9375rem;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.chat-input:focus {
  outline: none;
  border-color: var(--blue-400);
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.chat-input::placeholder {
  color: var(--text-muted);
}

.send-btn {
  background: linear-gradient(135deg, var(--blue-500) 0%, var(--blue-600) 100%);
  border: none;
  padding: 0.875rem 1.25rem;
  border-radius: 1.25rem;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--blue-600) 0%, var(--blue-700) 100%);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .ai-assistant-view {
    height: calc(100vh - 100px);
  }
  
  .sidebar {
    position: absolute;
    z-index: 100;
    height: calc(100vh - 100px);
  }
  
  .sidebar:not(.sidebar-open) {
    transform: translateX(-100%);
  }
  
  .quick-cards {
    grid-template-columns: 1fr;
  }
  
  .message-body {
    max-width: 85%;
  }
}
</style>
