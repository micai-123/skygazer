<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="prompt-overlay" @click.self="handleClose">
        <div class="prompt-modal glass-modal">
          <div class="prompt-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" fill="currentColor" opacity="0.1"/>
              <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          
          <h3 class="prompt-title">{{ title }}</h3>
          <p class="prompt-message">{{ message }}</p>
          
          <div class="prompt-actions">
            <button class="btn-cancel" @click="handleClose">
              取消
            </button>
            <button class="btn-confirm" @click="handleConfirm">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                <polyline points="10 17 15 12 10 7"/>
                <line x1="15" y1="12" x2="3" y2="12"/>
              </svg>
              去登录
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '提示'
  },
  message: {
    type: String,
    default: '请登录后使用完整服务'
  }
})

const emit = defineEmits(['close', 'confirm'])

function handleClose() {
  emit('close')
}

function handleConfirm() {
  emit('confirm')
  handleClose()
}
</script>

<style scoped>
.prompt-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
  padding: 1rem;
}

.glass-modal {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-width: 360px;
  padding: 2rem;
  text-align: center;
}

.prompt-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  color: white;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
}

.prompt-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 0.5rem;
}

.prompt-message {
  font-size: 0.9375rem;
  color: var(--text-secondary);
  margin: 0 0 1.5rem;
  line-height: 1.6;
}

.prompt-actions {
  display: flex;
  gap: 0.75rem;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  padding: 0.875rem 1rem;
  border-radius: 12px;
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: rgba(0, 0, 0, 0.05);
  border: none;
  color: var(--text-secondary);
}

.btn-cancel:hover {
  background: rgba(0, 0, 0, 0.1);
  color: var(--text-primary);
}

.btn-confirm {
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border: none;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .glass-modal,
.modal-leave-to .glass-modal {
  transform: scale(0.95) translateY(20px);
}

@media (max-width: 480px) {
  .glass-modal {
    padding: 1.5rem;
  }
  
  .prompt-icon {
    width: 64px;
    height: 64px;
  }
  
  .prompt-icon svg {
    width: 32px;
    height: 32px;
  }
}
</style>
