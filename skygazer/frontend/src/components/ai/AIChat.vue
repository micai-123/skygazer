<template>
  <div class="ai-chat glass-card">
    <div class="chat-head">
      <div class="head-title">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
          <path d="M21 11.5C21 16.1944 16.9706 20 12 20C10.4607 20 9.01133 19.6565 7.74467 19.0511L3 20L4.39499 16.28C3.51156 14.9923 3 13.5104 3 11.5C3 6.80558 7.02944 3 12 3C16.9706 3 21 6.80558 21 11.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>SkyGazer AI 智慧助理</span>
      </div>
      <div class="head-status">
        <span class="status-dot" :class="{ live: chat.isGenerating }"></span>
        {{ chat.isGenerating ? '生成中' : '在线' }}
      </div>
    </div>

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
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
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

    <div class="chat-input-container">
      <textarea
        v-model="inputMessage"
        class="chat-input"
        rows="1"
        placeholder="询问天气、穿衣或行程建议…"
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
        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
          <rect x="6" y="6" width="12" height="12" rx="2"/>
        </svg>
      </button>
      <button v-else class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim()" aria-label="发送消息">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
          <path d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { useChatStore } from '@/stores/chat'
import AgentMessage from './AgentMessage.vue'

const chat = useChatStore()
const inputMessage = ref('')
const messagesContainer = ref(null)
const inputEl = ref(null)

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || chat.isGenerating) return
  inputMessage.value = ''
  autoGrow()
  await chat.sendMessage(message)
  await nextTick()
  scrollToBottom()
}

const handleRetry = async () => {
  await chat.retryLast()
  await nextTick()
  scrollToBottom()
}

const autoGrow = async () => {
  await nextTick()
  if (inputEl.value) {
    inputEl.value.style.height = 'auto'
    inputEl.value.style.height = Math.min(inputEl.value.scrollHeight, 100) + 'px'
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

watch(
  () => chat.messages.length,
  () => nextTick(scrollToBottom)
)
</script>

<style scoped>
.ai-chat {
  display: flex;
  flex-direction: column;
  padding: 1.25rem;
  height: 440px;
}

.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.head-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--blue-900);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.head-status {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.6875rem;
  color: var(--text-muted);
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--green-500, #22c55e);
}
.status-dot.live { background: var(--blue-500); animation: dotPulse 1.4s ease-in-out infinite; }

@keyframes dotPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.3); }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding-right: 0.4rem;
  scrollbar-width: thin;
  scrollbar-color: var(--blue-200, #bfdbfe) transparent;
}
.chat-messages::-webkit-scrollbar { width: 5px; }
.chat-messages::-webkit-scrollbar-thumb { background: var(--blue-200, #bfdbfe); border-radius: 3px; }

.message-body { max-width: 82%; }

.typing-bubble {
  display: inline-flex;
  gap: 4px;
  padding: 0.7rem 0.95rem;
  background: var(--card-bg, #fff);
  border: 1px solid var(--border-color, rgba(59, 130, 246, 0.12));
  border-radius: 14px;
  border-top-left-radius: 4px;
  box-shadow: var(--shadow-sm);
}
.typing-bubble span {
  width: 6px;
  height: 6px;
  background: var(--text-muted);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}
.typing-bubble span:nth-child(2) { animation-delay: 0.2s; }
.typing-bubble span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-4px); opacity: 1; }
}

.chat-input-container {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
  align-items: flex-end;
}

.chat-input {
  flex: 1;
  background: var(--card-bg, #fff);
  border: 1px solid var(--blue-200, #bfdbfe);
  border-radius: 14px;
  padding: 0.7rem 0.95rem;
  font-size: 0.875rem;
  font-family: inherit;
  color: var(--text-primary);
  line-height: 1.5;
  resize: none;
  max-height: 100px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.chat-input:focus {
  outline: none;
  border-color: var(--blue-400);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
}
.chat-input::placeholder { color: var(--text-muted); }

.send-btn {
  flex-shrink: 0;
  background: var(--blue-600);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease, transform 0.15s ease;
  box-shadow: var(--shadow-md);
}
.send-btn:hover:not(:disabled) { background: var(--blue-700); transform: translateY(-1px); }
.send-btn:disabled { opacity: 0.45; cursor: not-allowed; }

.stop-btn {
  flex-shrink: 0;
  background: #ef4444;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease, transform 0.15s ease;
  box-shadow: var(--shadow-md);
}
.stop-btn:hover { background: #dc2626; transform: translateY(-1px); }

@media (prefers-reduced-motion: reduce) {
  .typing-bubble span, .status-dot.live { animation: none; }
}
</style>
