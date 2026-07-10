<template>
  <div class="profile-view">
    <div class="profile-header glass-card">
      <div class="header-content">
        <div class="avatar-section">
          <div class="avatar-large">
            {{ authStore.userName.charAt(0).toUpperCase() }}
          </div>
          <button class="change-avatar-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
              <circle cx="12" cy="13" r="4"/>
            </svg>
            更换头像
          </button>
        </div>
        <div class="user-info">
          <h2 class="username">{{ authStore.userName }}</h2>
          <p class="user-email">{{ authStore.user?.email || '未设置邮箱' }}</p>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">128</span>
              <span class="stat-label">查询次数</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">15</span>
              <span class="stat-label">收藏城市</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">32</span>
              <span class="stat-label">AI对话</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="profile-content">
      <div class="info-card glass-card">
        <h3 class="card-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          基本信息
        </h3>
        <form class="info-form" @submit.prevent="handleUpdateProfile">
          <div class="form-group">
            <label>用户名</label>
            <input 
              type="text" 
              v-model="profileForm.username" 
              disabled
              class="form-input disabled"
            >
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input 
              type="text" 
              v-model="profileForm.nickname"
              class="form-input"
              placeholder="请输入昵称"
            >
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input 
              type="email" 
              v-model="profileForm.email"
              class="form-input"
              placeholder="请输入邮箱"
            >
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input 
              type="tel" 
              v-model="profileForm.phone"
              class="form-input"
              placeholder="请输入手机号"
            >
          </div>
          <button type="submit" class="submit-btn" :disabled="saving">
            <span v-if="saving" class="btn-loading">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10" stroke-opacity="0.3"/>
                <path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round">
                  <animateTransform attributeName="transform" type="rotate" from="0 12 12" to="360 12 12" dur="1s" repeatCount="indefinite"/>
                </path>
              </svg>
              保存中...
            </span>
            <span v-else>保存修改</span>
          </button>
          
          <transition name="fade">
            <div v-if="saveMessage" class="save-message" :class="saveMessageType">
              <svg v-if="saveMessageType === 'success'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 6L9 17l-5-5"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
              {{ saveMessage }}
            </div>
          </transition>
        </form>
      </div>
      
      <div class="info-card glass-card">
        <h3 class="card-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
          默认位置
        </h3>
        <div class="location-setting">
          <div class="current-location">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="currentColor"/>
            </svg>
            <span>{{ profileForm.location || '未设置默认位置' }}</span>
          </div>
          <button class="change-location-btn">
            修改位置
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api'

const authStore = useAuthStore()

const profileForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  location: ''
})

const saving = ref(false)
const saveMessage = ref('')
const saveMessageType = ref('success')

onMounted(() => {
  if (authStore.user) {
    profileForm.username = authStore.user.username || ''
    profileForm.nickname = authStore.user.nickname || ''
    profileForm.email = authStore.user.email || ''
    profileForm.phone = authStore.user.phone || ''
    profileForm.location = authStore.user.defaultLocation || ''
  }
})

async function handleUpdateProfile() {
  if (saving.value) return
  
  saving.value = true
  saveMessage.value = ''
  
  try {
    const updateData = {
      nickname: profileForm.nickname,
      email: profileForm.email,
      phone: profileForm.phone
    }
    
    const response = await authApi.updateProfile(updateData)
    
    if (response.code === 200 || response.success) {
      saveMessage.value = '保存成功！'
      saveMessageType.value = 'success'
      
      if (authStore.user) {
        authStore.user.nickname = profileForm.nickname
        authStore.user.email = profileForm.email
        authStore.user.phone = profileForm.phone
        localStorage.setItem('user', JSON.stringify(authStore.user))
      }
      
      setTimeout(() => {
        saveMessage.value = ''
      }, 3000)
    } else {
      throw new Error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    saveMessage.value = error.message || '保存失败，请稍后重试'
    saveMessageType.value = 'error'
    
    setTimeout(() => {
      saveMessage.value = ''
    }, 3000)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-view {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.profile-header {
  padding: 2rem;
}

.header-content {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 2.5rem;
  font-weight: 600;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
}

.change-avatar-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba(59, 130, 246, 0.1);
  border: none;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--blue-600);
  cursor: pointer;
  transition: all 0.3s ease;
}

.change-avatar-btn:hover {
  background: rgba(59, 130, 246, 0.2);
}

.user-info {
  flex: 1;
}

.username {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--blue-900);
  margin-bottom: 0.25rem;
}

.user-email {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
}

.user-stats {
  display: flex;
  gap: 2rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--blue-600);
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.info-card {
  padding: 1.5rem;
}

.card-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--blue-900);
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.card-title svg {
  color: var(--blue-500);
}

.info-form {
  display: grid;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.form-input {
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 10px;
  font-size: 0.875rem;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--blue-500);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.disabled {
  background: rgba(0, 0, 0, 0.05);
  cursor: not-allowed;
}

.submit-btn {
  padding: 0.875rem 1.5rem;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border: none;
  border-radius: 10px;
  font-size: 0.9375rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  min-width: 120px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.btn-loading {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-loading svg {
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

.save-message {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  margin-top: 0.75rem;
  animation: slideIn 0.3s ease;
}

.save-message.success {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.save-message.error {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.save-message svg {
  flex-shrink: 0;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.location-setting {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current-location {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: var(--text-primary);
  font-size: 0.9375rem;
}

.current-location svg {
  color: var(--blue-500);
}

.change-location-btn {
  padding: 0.5rem 1rem;
  background: rgba(59, 130, 246, 0.1);
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--blue-600);
  cursor: pointer;
  transition: all 0.3s ease;
}

.change-location-btn:hover {
  background: rgba(59, 130, 246, 0.2);
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .user-stats {
    justify-content: center;
  }
}
</style>
