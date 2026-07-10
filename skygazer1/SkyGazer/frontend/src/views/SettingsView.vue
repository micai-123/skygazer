<template>
  <div class="settings-view">
    <div class="settings-card glass-card">
      <h3 class="card-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3"/>
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
        </svg>
        主题设置
      </h3>
      <div class="setting-item">
        <div class="setting-info">
          <span class="setting-label">界面主题</span>
          <span class="setting-desc">选择您喜欢的界面主题风格</span>
        </div>
        <div class="theme-options">
          <button 
            v-for="theme in themeStore.themes" 
            :key="theme.id"
            class="theme-option"
            :class="{ active: themeStore.currentTheme === theme.id }"
            @click="themeStore.setTheme(theme.id)"
          >
            {{ theme.name }}
          </button>
        </div>
      </div>
    </div>
    
    <div class="settings-card glass-card">
      <h3 class="card-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        通知设置
      </h3>
      <div class="setting-item">
        <div class="setting-info">
          <span class="setting-label">天气预警通知</span>
          <span class="setting-desc">接收恶劣天气预警推送</span>
        </div>
        <label class="toggle-switch">
          <input type="checkbox" id="weather-alert" name="weather-alert" v-model="settings.weatherAlert">
          <span class="toggle-slider"></span>
        </label>
      </div>
      <div class="setting-item">
        <div class="setting-info">
          <span class="setting-label">每日天气提醒</span>
          <span class="setting-desc">每天早上推送天气概况</span>
        </div>
        <label class="toggle-switch">
          <input type="checkbox" id="daily-reminder" name="daily-reminder" v-model="settings.dailyReminder">
          <span class="toggle-slider"></span>
        </label>
      </div>
    </div>
    
    <div class="settings-card glass-card">
      <h3 class="card-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
          <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
        </svg>
        安全设置
      </h3>
      <div class="setting-item">
        <div class="setting-info">
          <span class="setting-label">修改密码</span>
          <span class="setting-desc">定期修改密码可以提高账户安全性</span>
        </div>
        <button class="action-btn" @click="handleChangePassword">
          修改
        </button>
      </div>
      <div class="setting-item">
        <div class="setting-info">
          <span class="setting-label">两步验证</span>
          <span class="setting-desc">启用两步验证增强账户安全</span>
        </div>
        <label class="toggle-switch">
          <input type="checkbox" id="two-factor-auth" name="two-factor-auth" v-model="settings.twoFactorAuth">
          <span class="toggle-slider"></span>
        </label>
      </div>
    </div>
    
    <div class="settings-card glass-card">
      <h3 class="card-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="currentColor"/>
        </svg>
        数据与隐私
      </h3>
      <div class="setting-item">
        <div class="setting-info">
          <span class="setting-label">位置信息</span>
          <span class="setting-desc">允许应用获取您的位置信息</span>
        </div>
        <label class="toggle-switch">
          <input type="checkbox" id="location-access" name="location-access" v-model="settings.locationAccess">
          <span class="toggle-slider"></span>
        </label>
      </div>
      <div class="setting-item">
        <div class="setting-info">
          <span class="setting-label">数据统计</span>
          <span class="setting-desc">允许收集匿名使用数据以改进服务</span>
        </div>
        <label class="toggle-switch">
          <input type="checkbox" id="analytics" name="analytics" v-model="settings.analytics">
          <span class="toggle-slider"></span>
        </label>
      </div>
    </div>
    
    <div class="settings-card glass-card danger-zone">
      <h3 class="card-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
          <line x1="12" y1="9" x2="12" y2="13"/>
          <line x1="12" y1="17" x2="12.01" y2="17"/>
        </svg>
        危险操作
      </h3>
      <div class="setting-item">
        <div class="setting-info">
          <span class="setting-label">退出登录</span>
          <span class="setting-desc">退出当前登录的账户</span>
        </div>
        <button class="action-btn danger" @click="handleLogout">
          退出
        </button>
      </div>
      <div class="setting-item">
        <div class="setting-info">
          <span class="setting-label">删除账户</span>
          <span class="setting-desc">永久删除您的账户和所有数据</span>
        </div>
        <button class="action-btn danger" @click="handleDeleteAccount">
          删除
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useAuthStore } from '@/stores/auth'

const themeStore = useThemeStore()
const authStore = useAuthStore()

const settings = reactive({
  weatherAlert: true,
  dailyReminder: false,
  twoFactorAuth: false,
  locationAccess: true,
  analytics: true
})

function handleChangePassword() {
  console.log('修改密码')
}

function handleLogout() {
  authStore.logout()
}

function handleDeleteAccount() {
  if (confirm('确定要删除账户吗？此操作不可撤销。')) {
    console.log('删除账户')
  }
}
</script>

<style scoped>
.settings-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 800px;
  margin: 0 auto;
}

.settings-card {
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

.danger-zone .card-title svg {
  color: #ef4444;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.setting-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.setting-item:first-of-type {
  padding-top: 0;
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.setting-label {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-primary);
}

.setting-desc {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.theme-options {
  display: flex;
  gap: 0.5rem;
}

.theme-option {
  padding: 0.5rem 1rem;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--blue-600);
  cursor: pointer;
  transition: all 0.3s ease;
}

.theme-option:hover {
  background: rgba(59, 130, 246, 0.2);
}

.theme-option.active {
  background: var(--blue-500);
  color: white;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 26px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.1);
  transition: 0.3s;
  border-radius: 26px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-switch input:checked + .toggle-slider {
  background-color: var(--blue-500);
}

.toggle-switch input:checked + .toggle-slider:before {
  transform: translateX(22px);
}

.action-btn {
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

.action-btn:hover {
  background: rgba(59, 130, 246, 0.2);
}

.action-btn.danger {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.action-btn.danger:hover {
  background: rgba(239, 68, 68, 0.2);
}

@media (max-width: 768px) {
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .theme-options {
    width: 100%;
  }
  
  .theme-option {
    flex: 1;
    text-align: center;
  }
}
</style>
