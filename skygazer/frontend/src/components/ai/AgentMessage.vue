<template>
  <div class="message" :class="message.isUser ? 'user-message' : 'ai-message'">
    <div class="message-avatar" :class="message.isUser ? 'user-avatar' : 'ai-avatar'">
      <svg v-if="message.isUser" width="18" height="18" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
        <path d="M4 20C4 16.6863 7.13401 14 12 14C16.866 14 20 16.6863 20 20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none">
        <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
      </svg>
    </div>

    <div class="message-body">
      <div class="message-header" v-if="!message.isUser">
        <span class="agent-name">{{ message.agentName || '天象智囊' }}</span>
        <span class="message-time">{{ formatTime(message.timestamp) }}</span>
        <button class="copy-btn" v-if="message.content" @click="copy" :title="copied ? '已复制' : '复制'">
          <svg v-if="!copied" width="14" height="14" viewBox="0 0 24 24" fill="none">
            <rect x="9" y="9" width="11" height="11" rx="2" stroke="currentColor" stroke-width="2"/>
            <path d="M5 15V5a2 2 0 012-2h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>

      <div class="message-content" :class="{ 'user-content': message.isUser }">
        <div v-if="showSkeleton" class="skeleton" aria-label="正在生成回答" role="status">
          <span class="sk-line"></span>
          <span class="sk-line"></span>
          <span class="sk-line sk-line-short"></span>
        </div>
        <div
          v-else-if="!message.isUser && message.content"
          class="md-content"
          v-html="renderedContent"
        ></div>
        <template v-else-if="message.isUser && message.imageUrl">
          <div class="user-image-wrap">
            <img :src="message.imageUrl" class="user-image" alt="上传的天气图片" />
          </div>
          <div v-if="message.content" class="plain-content user-caption">{{ message.content }}</div>
        </template>
        <div v-else-if="message.isUser" class="plain-content">{{ message.content }}</div>
        <span v-if="isStreaming && isLast && message.content" class="stream-cursor" aria-hidden="true"></span>

        <div v-if="!message.isUser && message.error" class="error-banner" role="alert">
          <svg class="error-ico" width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span class="error-text">回答生成失败，请检查网络或稍后再试。</span>
          <button class="retry-btn" @click="$emit('retry')" aria-label="重试">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M21 12a9 9 0 11-3-6.7L21 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M21 3v5h-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            重试
          </button>
        </div>

        <AgentSteps v-if="!message.isUser && message.steps && message.steps.length" :steps="message.steps" />
        <WeatherImageCard v-if="!message.isUser && message.weatherImage" :result="message.weatherImage" />
        <CitationChips v-if="!message.isUser && message.references && message.references.length" :references="message.references" />

        <div class="weather-context" v-if="!message.isUser && message.weatherContext">
          <div class="context-title">当前天气</div>
          <div class="context-grid">
            <div class="context-item" v-if="message.weatherContext.location"><span class="context-label">地点</span><span class="context-value">{{ message.weatherContext.location }}</span></div>
            <div class="context-item" v-if="message.weatherContext.weatherCondition"><span class="context-label">天气</span><span class="context-value">{{ message.weatherContext.weatherCondition }}</span></div>
            <div class="context-item" v-if="message.weatherContext.temperature !== undefined"><span class="context-label">温度</span><span class="context-value">{{ message.weatherContext.temperature }}℃</span></div>
            <div class="context-item" v-if="message.weatherContext.airQualityLevel"><span class="context-label">空气</span><span class="context-value">{{ message.weatherContext.airQualityLevel }}</span></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { renderMarkdown } from '@/utils/markdown'
import AgentSteps from './AgentSteps.vue'
import CitationChips from './CitationChips.vue'
import WeatherImageCard from './WeatherImageCard.vue'

const props = defineProps({
  message: { type: Object, required: true },
  isStreaming: { type: Boolean, default: false },
  isLast: { type: Boolean, default: false }
})

defineEmits(['retry'])

const copied = ref(false)

const renderedContent = computed(() => renderMarkdown(props.message.content || ''))

// 流式刚开始、AI 尚未产出任何字符前，展示骨架占位（非错误态）
const showSkeleton = computed(
  () => !props.message.isUser && props.isStreaming && props.isLast && !props.message.content && !props.message.error
)

function formatTime(ts) {
  if (!ts) return ''
  return new Date(ts).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function copy() {
  try {
    await navigator.clipboard.writeText(props.message.content || '')
    copied.value = true
    setTimeout(() => { copied.value = false }, 1500)
  } catch (e) {
    // 忽略复制失败
  }
}
</script>

<style scoped>
.message {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
  animation: fadeIn 0.25s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.user-message { flex-direction: row-reverse; }
.ai-message { flex-direction: row; }

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: var(--shadow-sm);
}

.user-avatar { background: linear-gradient(135deg, var(--blue-500) 0%, var(--blue-600) 100%); }
.ai-avatar { background: linear-gradient(135deg, var(--blue-900) 0%, #1e3a5f 100%); }

.message-body { max-width: 72%; min-width: 0; }

.message-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.agent-name {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--blue-700, #1d4ed8);
}

.message-time {
  font-size: 0.625rem;
  color: var(--text-muted);
}

.copy-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 0.125rem;
  border-radius: 4px;
  display: inline-flex;
  transition: color 0.2s ease, background 0.2s ease;
}

.copy-btn:hover {
  color: var(--blue-600);
  background: rgba(59, 130, 246, 0.08);
}

.message-content {
  background: var(--card-bg, #fff);
  padding: 0.75rem 1rem;
  border-radius: 14px;
  font-size: 0.9375rem;
  color: var(--text-primary);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color, rgba(59, 130, 246, 0.12));
  line-height: 1.65;
}

.user-content {
  border-top-right-radius: 4px;
  background: linear-gradient(135deg, var(--blue-50, #eff6ff) 0%, var(--blue-100, #dbeafe) 100%);
  border-color: var(--blue-200, #bfdbfe);
  color: #111827;
}

.ai-message .message-content { border-top-left-radius: 4px; }

.plain-content { white-space: pre-wrap; word-break: break-word; }

.user-image-wrap {
  display: flex;
  justify-content: flex-end;
}

.user-image {
  max-width: 220px;
  max-height: 220px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid var(--blue-200, #bfdbfe);
  box-shadow: var(--shadow-sm);
}

.user-caption {
  margin-top: 0.35rem;
  font-size: 0.8125rem;
  color: #374151;
  text-align: right;
}

/* 骨架占位（思考中） */
.skeleton {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.15rem 0;
}

.sk-line {
  height: 0.75rem;
  border-radius: 6px;
  background: linear-gradient(
    100deg,
    rgba(59, 130, 246, 0.08) 30%,
    rgba(59, 130, 246, 0.18) 50%,
    rgba(59, 130, 246, 0.08) 70%
  );
  background-size: 200% 100%;
  animation: shimmer 1.3s ease-in-out infinite;
}

.sk-line:nth-child(2) { width: 88%; }
.sk-line-short { width: 56%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 错误重试条 */
.error-banner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.625rem;
  padding: 0.5rem 0.75rem;
  background: rgba(239, 68, 68, 0.08);
  border: 1px solid rgba(239, 68, 68, 0.24);
  border-radius: 10px;
}

.error-ico { color: #ef4444; flex-shrink: 0; }

.error-text {
  flex: 1;
  min-width: 0;
  font-size: 0.75rem;
  color: #b91c1c;
}

.retry-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.3rem 0.65rem;
  background: #ef4444;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.15s ease;
}

.retry-btn:hover { background: #dc2626; transform: translateY(-1px); }
.retry-btn:active { transform: translateY(0) scale(0.98); }

.stream-cursor {
  display: inline-block;
  width: 2px;
  height: 1.1em;
  margin-left: 1px;
  background: var(--blue-500);
  vertical-align: text-bottom;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* Markdown 排版 */
.md-content :deep(.md-p) { margin: 0 0 0.5rem; }
.md-content :deep(.md-p:last-child) { margin-bottom: 0; }
.md-content :deep(.md-h) { margin: 0.5rem 0 0.375rem; font-weight: 700; color: var(--text-primary); }
.md-content :deep(.md-h1) { font-size: 1.125rem; }
.md-content :deep(.md-h2) { font-size: 1.0625rem; }
.md-content :deep(.md-h3), .md-content :deep(.md-h4) { font-size: 1rem; }
.md-content :deep(.md-ul), .md-content :deep(.md-ol) { margin: 0.25rem 0 0.5rem; padding-left: 1.25rem; }
.md-content :deep(.md-li) { margin-bottom: 0.25rem; }
.md-content :deep(.md-code) {
  background: rgba(59, 130, 246, 0.1);
  padding: 0.0625rem 0.3125rem;
  border-radius: 4px;
  font-size: 0.875em;
}
.md-content :deep(.md-pre) {
  background: rgba(30, 41, 59, 0.05);
  padding: 0.625rem 0.75rem;
  border-radius: 8px;
  overflow-x: auto;
  margin: 0.375rem 0 0.5rem;
  font-size: 0.8125rem;
  line-height: 1.5;
}
.md-content :deep(.md-quote) {
  border-left: 3px solid var(--blue-300, #93c5fd);
  margin: 0.375rem 0;
  padding: 0.25rem 0.75rem;
  color: var(--text-secondary);
}
.md-content :deep(.md-link) { color: var(--blue-600); text-decoration: underline; }
.md-content :deep(strong) { font-weight: 700; color: var(--text-primary); }

.weather-context {
  margin-top: 0.625rem;
  padding: 0.625rem 0.75rem;
  background: rgba(59, 130, 246, 0.04);
  border-radius: 10px;
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

.context-item { display: flex; gap: 0.5rem; }

.context-label { font-size: 0.6875rem; color: var(--text-muted); }
.context-value { font-size: 0.6875rem; color: var(--text-primary); font-weight: 500; }

@media (max-width: 768px) {
  .message-body { max-width: 85%; }
}

@media (prefers-reduced-motion: reduce) {
  .message { animation: none; }
  .stream-cursor { animation: none; }
  .sk-line { animation: none; }
}
</style>
