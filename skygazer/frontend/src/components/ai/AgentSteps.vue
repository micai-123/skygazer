<template>
  <div class="agent-steps" :class="{ 'is-expanded': expanded }">
    <button class="steps-summary" @click="expanded = !expanded" :aria-expanded="expanded">
      <span class="summary-icon" aria-hidden="true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
        </svg>
      </span>
      <span class="summary-text">
        <span class="summary-title">工作过程</span>
        <span class="summary-trail">
          <template v-for="(s, idx) in steps" :key="idx">
            <span class="trail-sep" v-if="idx > 0">·</span>
            <span class="trail-item" :class="'trail-' + s.status">{{ s.label }}</span>
          </template>
        </span>
      </span>
      <span class="summary-toggle" aria-hidden="true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" :class="{ rotated: expanded }">
          <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </span>
    </button>

    <div class="steps-detail" v-show="expanded">
      <div
        v-for="(step, idx) in steps"
        :key="idx"
        class="step-item"
        :class="'step-' + step.status"
      >
        <div class="step-rail">
          <span class="step-dot" :class="'dot-' + step.status">
            <svg v-if="step.status === 'done'" width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg v-else-if="step.status === 'error'" width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            </svg>
            <span v-else class="dot-pulse"></span>
          </span>
          <span class="step-line" v-if="idx < steps.length - 1"></span>
        </div>

        <div class="step-body">
          <div class="step-head">
            <span class="step-type-icon" :class="'type-' + step.type" aria-hidden="true">
              <svg v-if="step.type === 'rag'" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M4 19.5A2.5 2.5 0 016.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else-if="step.type === 'tool'" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M14.7 6.3a4 4 0 00-5.4 5.4L3 18v3h3l6.3-6.3a4 4 0 005.4-5.4l-2.5 2.5-2-2 2.5-2.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else-if="step.type === 'model'" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <rect x="6" y="6" width="12" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M9 9h.01M15 9h.01M9 15h.01M15 15h.01M12 12h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M9 18h6M10 22h4M12 2a7 7 0 00-4 12.7c.6.5 1 1.3 1 2.1h6c0-.8.4-1.6 1-2.1A7 7 0 0012 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <span class="step-label">{{ step.label }}</span>
            <span class="step-status-tag" :class="'tag-' + step.status">
              {{ step.status === 'running' ? '进行中' : step.status === 'done' ? '完成' : '失败' }}
            </span>
          </div>
          <div class="step-detail md-content" v-if="step.detail" v-html="rendered(step.detail)"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps({
  steps: { type: Array, default: () => [] }
})

const expanded = ref(props.steps.some(s => s.status === 'running'))

const rendered = (text) => renderMarkdown(text)
</script>

<style scoped>
.agent-steps {
  margin-top: 0.625rem;
  border: 1px solid var(--ai-step-border, rgba(59, 130, 246, 0.16));
  border-radius: 12px;
  background: var(--ai-step-bg, rgba(59, 130, 246, 0.04));
  overflow: hidden;
}

.steps-summary {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  width: 100%;
  padding: 0.625rem 0.75rem;
  background: none;
  border: none;
  cursor: pointer;
  text-align: left;
  color: var(--text-primary);
  transition: background 0.2s ease;
}

.steps-summary:hover {
  background: var(--ai-step-hover, rgba(59, 130, 246, 0.08));
}

.summary-icon {
  color: var(--blue-500);
  display: flex;
  flex-shrink: 0;
}

.summary-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.summary-title {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.summary-trail {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
  font-size: 0.75rem;
  color: var(--text-primary);
}

.trail-sep {
  color: var(--text-muted);
}

.trail-done { color: var(--text-secondary); }
.trail-running { color: var(--blue-600); font-weight: 600; }
.trail-error { color: #ef4444; }

.summary-toggle {
  color: var(--text-muted);
  display: flex;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.summary-toggle svg.rotated {
  transform: rotate(180deg);
}

.steps-detail {
  padding: 0.25rem 0.75rem 0.625rem;
  border-top: 1px solid var(--ai-step-border, rgba(59, 130, 246, 0.12));
}

.step-item {
  display: flex;
  gap: 0.625rem;
  padding-top: 0.625rem;
}

.step-rail {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.step-dot {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dot-done {
  background: rgba(34, 197, 94, 0.15);
  color: #16a34a;
}

.dot-error {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.dot-running {
  background: rgba(59, 130, 246, 0.15);
  color: var(--blue-600);
}

.dot-pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--blue-500);
  animation: stepPulse 1.2s ease-in-out infinite;
}

@keyframes stepPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.4); opacity: 0.5; }
}

.step-line {
  flex: 1;
  width: 2px;
  margin-top: 2px;
  background: var(--ai-step-border, rgba(59, 130, 246, 0.16));
  min-height: 12px;
}

.step-body {
  flex: 1;
  min-width: 0;
  padding-bottom: 0.25rem;
}

.step-head {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.step-type-icon {
  display: flex;
  align-items: center;
}

.type-rag { color: var(--blue-600); }
.type-tool { color: #0ea5e9; }
.type-model { color: #f59e0b; }
.type-reason { color: #8b5cf6; }

.step-label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--text-primary);
}

.step-status-tag {
  font-size: 0.625rem;
  padding: 0.0625rem 0.375rem;
  border-radius: 999px;
  font-weight: 600;
}

.tag-done { background: rgba(34, 197, 94, 0.12); color: #16a34a; }
.tag-running { background: rgba(59, 130, 246, 0.12); color: var(--blue-600); }
.tag-error { background: rgba(239, 68, 68, 0.12); color: #ef4444; }

.step-detail {
  margin-top: 0.375rem;
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.55;
}

.step-detail :deep(.md-p) { margin: 0 0 0.375rem; }
.step-detail :deep(.md-p:last-child) { margin-bottom: 0; }
.step-detail :deep(.md-code) {
  background: rgba(59, 130, 246, 0.1);
  padding: 0.0625rem 0.25rem;
  border-radius: 4px;
  font-size: 0.75rem;
}
.step-detail :deep(.md-pre) {
  background: rgba(30, 41, 59, 0.06);
  padding: 0.5rem 0.625rem;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 0.75rem;
}

@media (prefers-reduced-motion: reduce) {
  .dot-pulse { animation: none; }
  .summary-toggle { transition: none; }
}
</style>
