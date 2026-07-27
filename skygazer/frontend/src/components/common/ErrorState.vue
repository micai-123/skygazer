<template>
  <div class="error-state" role="alert" aria-live="assertive">
    <div class="error-icon">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <circle cx="12" cy="12" r="10" stroke="#EF4444" stroke-width="1.5" />
        <path d="M12 8V12M12 16H12.01" stroke="#EF4444" stroke-width="2" stroke-linecap="round" />
      </svg>
    </div>
    <h3 class="error-title">{{ title }}</h3>
    <p class="error-description">{{ message }}</p>
    <div class="error-actions">
      <button
        class="error-action primary"
        @click="$emit('retry')"
        :aria-label="retryLabel"
      >
        {{ retryLabel }}
      </button>
      <button
        v-if="showDismiss"
        class="error-action secondary"
        @click="$emit('dismiss')"
        aria-label="关闭错误提示"
      >
        关闭
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '加载失败'
  },
  message: {
    type: String,
    default: '数据加载失败，请检查网络连接后重试'
  },
  retryLabel: {
    type: String,
    default: '重新加载'
  },
  showDismiss: {
    type: Boolean,
    default: false
  }
})

defineEmits(['retry', 'dismiss'])
</script>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  text-align: center;
}

.error-icon {
  margin-bottom: 1.5rem;
}

.error-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.error-description {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
  max-width: 24rem;
}

.error-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  justify-content: center;
}

.error-action {
  padding: 0.625rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.error-action.primary {
  background: var(--blue-500);
  color: white;
}

.error-action.primary:hover {
  background: var(--blue-600);
  transform: translateY(-1px);
}

.error-action.secondary {
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--text-muted);
}

.error-action.secondary:hover {
  background: rgba(0, 0, 0, 0.05);
}

.error-action:focus-visible {
  outline: 2px solid var(--blue-500);
  outline-offset: 2px;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.2);
}

@media (prefers-reduced-motion: reduce) {
  .error-action:hover {
    transform: none;
  }
}
</style>