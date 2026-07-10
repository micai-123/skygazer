<template>
  <div class="ai-chat glass-card">
    <h3 class="section-title">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <path d="M21 11.5C21 16.1944 16.9706 20 12 20C10.4607 20 9.01133 19.6565 7.74467 19.0511L3 20L4.39499 16.28C3.51156 14.9923 3 13.5104 3 11.5C3 6.80558 7.02944 3 12 3C16.9706 3 21 6.80558 21 11.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      SkyGazer AI 智慧助理
    </h3>
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="message in chat.messages" 
        :key="message.id"
        class="message"
        :class="message.isUser ? 'user-message' : 'ai-message'"
      >
        <div class="message-avatar" :class="message.isUser ? 'user-avatar' : 'ai-avatar'">
          <svg v-if="message.isUser" width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
            <path d="M4 20C4 16.6863 7.13401 14 12 14C16.866 14 20 16.6863 20 20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
          </svg>
        </div>
        <div class="message-content">{{ message.content }}</div>
      </div>
      
      <div v-if="chat.isTyping" class="message ai-message">
        <div class="message-avatar ai-avatar">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
          </svg>
        </div>
        <div class="message-content typing">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>
    <div class="chat-input-container">
      <input 
        type="text" 
        class="chat-input" 
        v-model="inputMessage"
        placeholder="询问天气、穿衣或行程建议..."
        @keypress.enter="sendMessage"
      >
      <button class="send-btn" @click="sendMessage" aria-label="发送消息">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { useChatStore } from '@/stores/chat'

const chat = useChatStore()
const inputMessage = ref('')
const messagesContainer = ref(null)

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message) return
  
  inputMessage.value = ''
  await chat.sendMessage(message)
  
  await nextTick()
  scrollToBottom()
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

watch(() => chat.messages.length, () => {
  nextTick(scrollToBottom)
})
</script>

<style scoped>
.ai-chat {
  display: flex;
  flex-direction: column;
  padding: 1.5rem;
  height: 400px;
}

.section-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--blue-900);
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding-right: 0.5rem;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.chat-messages::-webkit-scrollbar {
  display: none;
}

.message {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.user-message {
  flex-direction: row;
}

.ai-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 0.5rem;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.user-avatar {
  background: var(--blue-500);
}

.ai-avatar {
  background: var(--blue-900);
}

.message-content {
  background: rgba(255, 255, 255, 0.8);
  padding: 0.75rem 1rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  color: var(--text-primary);
  max-width: 80%;
  box-shadow: var(--shadow-sm);
}

.user-message .message-content {
  border-top-left-radius: 0;
}

.ai-message .message-content {
  background: var(--blue-900);
  color: white;
  border-top-right-radius: 0;
}

.message-content.typing {
  display: flex;
  gap: 4px;
  padding: 0.75rem 1rem;
}

.message-content.typing span {
  width: 8px;
  height: 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.message-content.typing span:nth-child(1) {
  animation-delay: 0s;
}

.message-content.typing span:nth-child(2) {
  animation-delay: 0.2s;
}

.message-content.typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    background: rgba(255, 255, 255, 0.6);
  }
  30% {
    transform: translateY(-4px);
    background: rgba(255, 255, 255, 1);
  }
}

.chat-input-container {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.chat-input {
  flex: 1;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(59, 130, 246, 0.1);
  border-radius: 1rem;
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.chat-input:focus {
  outline: none;
  border-color: var(--blue-400);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.chat-input::placeholder {
  color: var(--text-muted);
}

.send-btn {
  background: var(--blue-600);
  border: none;
  padding: 0.75rem;
  border-radius: 1rem;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
}

.send-btn:hover {
  background: var(--blue-700);
  transform: scale(1.05);
}
</style>
