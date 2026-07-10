<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="auth-modal-overlay" @click.self="handleClose">
        <div class="auth-modal glass-modal">
          <button class="close-btn" @click="handleClose" aria-label="关闭">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
          </button>
          
          <div class="modal-header">
            <div class="header-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
                <path d="M4 20C4 16.6863 7.13401 14 12 14C16.866 14 20 16.6863 20 20" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <h2 class="modal-title">{{ isLogin ? '欢迎回来' : '创建账户' }}</h2>
            <p class="modal-subtitle">{{ isLogin ? '登录以使用完整服务' : '注册以获取更多功能' }}</p>
          </div>
          
          <div class="modal-tabs">
            <button 
              class="tab-btn" 
              :class="{ active: isLogin }"
              @click="switchMode('login')"
            >
              登录
            </button>
            <button 
              class="tab-btn" 
              :class="{ active: !isLogin }"
              @click="switchMode('register')"
            >
              注册
            </button>
          </div>
          
          <form class="auth-form" @submit.prevent="handleSubmit">
            <Transition name="fade" mode="out-in">
              <div v-if="!isLogin" class="form-group">
                <label class="form-label">昵称</label>
                <div class="input-wrapper">
                  <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  <input 
                    v-model="form.nickname"
                    type="text" 
                    class="form-input"
                    placeholder="请输入昵称"
                    maxlength="20"
                  >
                </div>
              </div>
            </Transition>
            
            <div class="form-group">
              <label class="form-label">用户名 <span class="required">*</span></label>
              <div class="input-wrapper">
                <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                <input 
                  v-model="form.username"
                  type="text" 
                  class="form-input"
                  :class="{ error: errors.username }"
                  placeholder="请输入用户名"
                  @blur="validateUsername"
                  @input="errors.username = ''"
                >
              </div>
              <Transition name="fade">
                <span v-if="errors.username" class="error-text">{{ errors.username }}</span>
              </Transition>
            </div>
            
            <div class="form-group">
              <label class="form-label">密码 <span class="required">*</span></label>
              <div class="input-wrapper">
                <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                <input 
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'" 
                  class="form-input"
                  :class="{ error: errors.password }"
                  placeholder="请输入密码"
                  @blur="validatePassword"
                  @input="errors.password = ''"
                >
                <button 
                  type="button" 
                  class="password-toggle"
                  @click="showPassword = !showPassword"
                >
                  <svg v-if="showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                    <line x1="1" y1="1" x2="23" y2="23"/>
                  </svg>
                  <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                </button>
              </div>
              <Transition name="fade">
                <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
              </Transition>
            </div>
            
            <Transition name="fade" mode="out-in">
              <div v-if="!isLogin" class="form-group">
                <label class="form-label">确认密码 <span class="required">*</span></label>
                <div class="input-wrapper">
                  <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                  </svg>
                  <input 
                    v-model="form.confirmPassword"
                    :type="showConfirmPassword ? 'text' : 'password'" 
                    class="form-input"
                    :class="{ error: errors.confirmPassword }"
                    placeholder="请再次输入密码"
                    @blur="validateConfirmPassword"
                    @input="errors.confirmPassword = ''"
                  >
                  <button 
                    type="button" 
                    class="password-toggle"
                    @click="showConfirmPassword = !showConfirmPassword"
                  >
                    <svg v-if="showConfirmPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                    <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                </div>
                <Transition name="fade">
                  <span v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</span>
                </Transition>
              </div>
            </Transition>
            
            <Transition name="fade" mode="out-in">
              <div v-if="!isLogin" class="form-group">
                <label class="form-label">邮箱</label>
                <div class="input-wrapper">
                  <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                  <input 
                    v-model="form.email"
                    type="email" 
                    class="form-input"
                    :class="{ error: errors.email }"
                    placeholder="请输入邮箱（选填）"
                    @blur="validateEmail"
                    @input="errors.email = ''"
                  >
                </div>
                <Transition name="fade">
                  <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
                </Transition>
              </div>
            </Transition>
            
            <Transition name="fade">
              <div v-if="submitError" class="error-banner">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12"/>
                  <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                {{ submitError }}
              </div>
            </Transition>
            
            <button 
              type="submit" 
              class="submit-btn"
              :disabled="authStore.isLoading || !isFormValid"
            >
              <Transition name="fade" mode="out-in">
                <span v-if="authStore.isLoading" class="loading-content">
                  <svg class="spinner" width="20" height="20" viewBox="0 0 24 24">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" fill="none" stroke-dasharray="31.416" stroke-dashoffset="10"/>
                  </svg>
                  处理中...
                </span>
                <span v-else>{{ isLogin ? '登 录' : '注 册' }}</span>
              </Transition>
            </button>
          </form>
          
          <div class="modal-footer">
            <p v-if="isLogin" class="footer-text">
              还没有账户？
              <button type="button" class="link-btn" @click="switchMode('register')">立即注册</button>
            </p>
            <p v-else class="footer-text">
              已有账户？
              <button type="button" class="link-btn" @click="switchMode('login')">立即登录</button>
            </p>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'login'
  }
})

const emit = defineEmits(['close', 'update:mode'])

const authStore = useAuthStore()

const isLogin = computed(() => props.mode === 'login')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: ''
})

const errors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const submitError = ref('')

const isFormValid = computed(() => {
  if (isLogin.value) {
    return form.username.trim().length >= 3 && form.password.length >= 6
  }
  return (
    form.username.trim().length >= 3 &&
    form.password.length >= 6 &&
    form.confirmPassword === form.password &&
    !errors.username &&
    !errors.password &&
    !errors.confirmPassword &&
    !errors.email
  )
})

watch(() => props.show, (newVal) => {
  if (newVal) {
    resetForm()
  }
})

function resetForm() {
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.nickname = ''
  form.email = ''
  errors.username = ''
  errors.password = ''
  errors.confirmPassword = ''
  errors.email = ''
  showPassword.value = false
  showConfirmPassword.value = false
  submitError.value = ''
}

function switchMode(mode) {
  emit('update:mode', mode)
  resetForm()
}

function handleClose() {
  submitError.value = ''
  emit('close')
}

function validateUsername() {
  if (!form.username.trim()) {
    errors.username = '请输入用户名'
  } else if (form.username.trim().length < 3) {
    errors.username = '用户名至少3个字符'
  } else if (!/^[a-zA-Z0-9_\u4e00-\u9fa5]+$/.test(form.username)) {
    errors.username = '用户名只能包含字母、数字、下划线和中文'
  } else {
    errors.username = ''
  }
}

function validatePassword() {
  if (!form.password) {
    errors.password = '请输入密码'
  } else if (form.password.length < 6) {
    errors.password = '密码至少6个字符'
  } else {
    errors.password = ''
  }
}

function validateConfirmPassword() {
  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
  } else if (form.confirmPassword !== form.password) {
    errors.confirmPassword = '两次密码不一致'
  } else {
    errors.confirmPassword = ''
  }
}

function validateEmail() {
  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '请输入有效的邮箱地址'
  } else {
    errors.email = ''
  }
}

async function handleSubmit() {
  submitError.value = ''
  validateUsername()
  validatePassword()
  
  if (!isLogin.value) {
    validateConfirmPassword()
    validateEmail()
  }
  
  if (!isFormValid.value) return
  
  let result
  if (isLogin.value) {
    result = await authStore.login({
      username: form.username.trim(),
      password: form.password
    })
  } else {
    result = await authStore.register({
      username: form.username.trim(),
      password: form.password,
      nickname: form.nickname.trim() || form.username.trim(),
      email: form.email || undefined
    })
  }
  
  if (result.success) {
    handleClose()
  } else {
    submitError.value = result.message || (isLogin.value ? '登录失败，请重试' : '注册失败，请重试')
  }
}
</script>

<style scoped>
.auth-modal-overlay {
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
  z-index: 1000;
  padding: 1rem;
}

.glass-modal {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-width: 420px;
  position: relative;
  overflow: hidden;
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 36px;
  height: 36px;
  background: rgba(0, 0, 0, 0.05);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: all 0.3s ease;
  z-index: 10;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  color: var(--text-primary);
  transform: rotate(90deg);
}

.modal-header {
  text-align: center;
  padding: 2rem 2rem 1rem;
}

.header-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  color: white;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
}

.modal-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--blue-900);
  margin: 0 0 0.5rem;
}

.modal-subtitle {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0;
}

.modal-tabs {
  display: flex;
  gap: 0.5rem;
  padding: 0 2rem;
  margin-bottom: 1.5rem;
}

.tab-btn {
  flex: 1;
  padding: 0.75rem;
  background: rgba(0, 0, 0, 0.03);
  border: none;
  border-radius: 10px;
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: rgba(59, 130, 246, 0.1);
  color: var(--blue-600);
}

.tab-btn.active {
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.auth-form {
  padding: 0 2rem;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.required {
  color: #ef4444;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-secondary);
  pointer-events: none;
}

.form-input {
  width: 100%;
  padding: 0.875rem 1rem 0.875rem 2.75rem;
  background: rgba(0, 0, 0, 0.03);
  border: 2px solid transparent;
  border-radius: 12px;
  font-size: 0.9375rem;
  color: #1e293b;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  background: white;
  border-color: var(--blue-500);
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.form-input.error {
  border-color: #ef4444;
}

.form-input.error:focus {
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.1);
}

.form-input::placeholder {
  color: var(--text-secondary);
}

.password-toggle {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.3s ease;
}

.password-toggle:hover {
  color: var(--blue-600);
}

.error-text {
  display: block;
  font-size: 0.75rem;
  color: #ef4444;
  margin-top: 0.375rem;
  padding-left: 0.25rem;
}

.error-banner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 12px;
  font-size: 0.875rem;
  color: #ef4444;
  margin-bottom: 1rem;
}

.submit-btn {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  margin-bottom: 1.5rem;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.modal-footer {
  text-align: center;
  padding: 1rem 2rem 2rem;
}

.footer-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0;
}

.link-btn {
  background: none;
  border: none;
  color: var(--blue-600);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  transition: color 0.3s ease;
}

.link-btn:hover {
  color: var(--blue-700);
  text-decoration: underline;
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 480px) {
  .glass-modal {
    border-radius: 20px;
    margin: 0.5rem;
  }
  
  .modal-header {
    padding: 1.5rem 1.5rem 0.75rem;
  }
  
  .modal-tabs {
    padding: 0 1.5rem;
  }
  
  .auth-form {
    padding: 0 1.5rem;
  }
  
  .modal-footer {
    padding: 1rem 1.5rem 1.5rem;
  }
}
</style>
