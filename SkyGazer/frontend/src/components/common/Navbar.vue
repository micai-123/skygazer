<template>
  <nav class="navbar glass-nav">
    <div class="nav-brand">
      <div class="brand-icon">
        <svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="16" cy="16" r="15" fill="url(#blue-bg-nav)" stroke="url(#blue-stroke-nav)" stroke-width="1"/>
          <circle cx="16" cy="16" r="8.5" fill="none" stroke="url(#moon-glow-nav)" stroke-width="1.5" opacity="0.35"/>
          <circle cx="16" cy="16" r="10.5" fill="none" stroke="url(#moon-glow-nav)" stroke-width="1" opacity="0.2"/>
          <circle cx="16" cy="16" r="7" fill="url(#moon-gradient-nav)"/>
          <circle cx="7" cy="7" r="1" fill="#FFD54F" opacity="0.85"/>
          <circle cx="25" cy="9" r="0.9" fill="#FFD54F" opacity="0.7"/>
          <circle cx="23" cy="25" r="1.1" fill="#FFD54F" opacity="0.75"/>
          <circle cx="9" cy="23" r="0.7" fill="#FFD54F" opacity="0.6"/>
          <circle cx="12" cy="11" r="0.5" fill="#FFD54F" opacity="0.5"/>
          <circle cx="20" cy="12" r="0.6" fill="#FFD54F" opacity="0.55"/>
          <circle cx="11" cy="20" r="0.4" fill="#FFD54F" opacity="0.45"/>
          <circle cx="21" cy="21" r="0.8" fill="#FFD54F" opacity="0.65"/>
          <defs>
            <linearGradient id="blue-bg-nav" x1="0" y1="0" x2="32" y2="32">
              <stop offset="0%" stop-color="#1E3A8A"/>
              <stop offset="50%" stop-color="#1E40AF"/>
              <stop offset="100%" stop-color="#1D4ED8"/>
            </linearGradient>
            <linearGradient id="blue-stroke-nav" x1="0" y1="0" x2="32" y2="32">
              <stop offset="0%" stop-color="#3B82F6"/>
              <stop offset="100%" stop-color="#60A5FA"/>
            </linearGradient>
            <radialGradient id="moon-gradient-nav" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stop-color="#FFF9C4"/>
              <stop offset="35%" stop-color="#FFEB3B"/>
              <stop offset="65%" stop-color="#FFC107"/>
              <stop offset="100%" stop-color="#FFB300"/>
            </radialGradient>
            <radialGradient id="moon-glow-nav" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stop-color="#FFD54F" stop-opacity="0.7"/>
              <stop offset="100%" stop-color="#FFD54F" stop-opacity="0"/>
            </radialGradient>
          </defs>
        </svg>
      </div>
      <span class="brand-text">智观天象 <span class="brand-ai">AI</span></span>
    </div>
    
    <div class="nav-links">
      <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">实时预览</router-link>
      <router-link to="/map" class="nav-link" :class="{ active: $route.path === '/map' }">气象地图</router-link>
      <router-link to="/analysis" class="nav-link" :class="{ active: $route.path === '/analysis' }">智能分析</router-link>
      <router-link to="/lifestyle" class="nav-link" :class="{ active: $route.path === '/lifestyle' }">生活指数</router-link>
      <router-link to="/ai-assistant" class="nav-link" :class="{ active: $route.path === '/ai-assistant' }">AI智慧助理</router-link>
    </div>
    
    <div class="nav-info">
      <div class="date-info">
        <p class="date-year">{{ currentDate }}</p>
        <p class="date-lunar">{{ weekDay }} · {{ lunarDate }}</p>
      </div>
      
      <div class="user-section">
        <template v-if="authStore.isAuthenticated">
          <div class="user-dropdown-wrapper">
            <button class="user-btn logged-in" aria-label="用户中心" @click="toggleUserMenu">
              <div v-if="authStore.userAvatar" class="user-avatar">
                <img :src="authStore.userAvatar" alt="用户头像">
              </div>
              <div v-else class="user-avatar-placeholder">
                {{ authStore.userName.charAt(0).toUpperCase() }}
              </div>
              <span class="user-name">{{ authStore.userName }}</span>
              <svg class="dropdown-arrow" :class="{ rotated: userMenuOpen }" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </button>
            
            <Transition name="dropdown">
              <div v-if="userMenuOpen" class="user-dropdown glass-dropdown">
                <div class="dropdown-header">
                  <div class="dropdown-avatar">
                    {{ authStore.userName.charAt(0).toUpperCase() }}
                  </div>
                  <div class="dropdown-user-info">
                    <div class="dropdown-username">{{ authStore.userName }}</div>
                    <div class="dropdown-email">{{ authStore.user?.email || '未设置邮箱' }}</div>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
                <div class="dropdown-menu">
                  <button class="dropdown-item" @click="goToProfile">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                    个人中心
                  </button>
                  <button class="dropdown-item" @click="goToSettings">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="3"/>
                      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
                    </svg>
                    设置
                  </button>
                </div>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item logout" @click="handleLogout">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                  </svg>
                  退出登录
                </button>
              </div>
            </Transition>
          </div>
        </template>
        <template v-else>
          <button class="login-btn" @click="authStore.openAuthModal('login')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
              <polyline points="10 17 15 12 10 7"/>
              <line x1="15" y1="12" x2="3" y2="12"/>
            </svg>
            登录
          </button>
        </template>
      </div>
      
      <div class="theme-toggle-wrapper">
        <button 
          class="theme-toggle" 
          aria-label="主题切换" 
          aria-haspopup="true" 
          :aria-expanded="themeStore.themeMenuOpen"
          @click="themeStore.toggleThemeMenu"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="5"></circle>
            <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"></path>
          </svg>
        </button>
        
        <div 
          class="theme-menu" 
          :class="{ show: themeStore.themeMenuOpen }"
          role="menu" 
          aria-label="主题选择菜单"
        >
          <div 
            v-for="theme in themeStore.themes" 
            :key="theme.id"
            class="theme-menu-item"
            :class="{ active: themeStore.currentTheme === theme.id }"
            :data-theme="theme.id"
            role="menuitemradio"
            :aria-checked="themeStore.currentTheme === theme.id"
            @click="themeStore.setTheme(theme.id)"
          >
            <div class="theme-indicator" :class="theme.id"></div>
            <div class="item-info">
              <div class="item-label">{{ theme.name }}</div>
              <div class="item-desc">{{ theme.desc }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const themeStore = useThemeStore()
const authStore = useAuthStore()

const userMenuOpen = ref(false)

const currentDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})

const weekDay = computed(() => {
  const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return days[new Date().getDay()]
})

const lunarDate = computed(() => {
  return '农历二月初六'
})

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value
}

function goToProfile() {
  userMenuOpen.value = false
  router.push('/profile')
}

function goToSettings() {
  userMenuOpen.value = false
  router.push('/settings')
}

function handleLogout() {
  userMenuOpen.value = false
  authStore.logout()
}

const handleClickOutside = (event) => {
  if (!event.target.closest('.theme-toggle-wrapper')) {
    themeStore.closeThemeMenu()
  }
  if (!event.target.closest('.user-dropdown-wrapper')) {
    userMenuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  authStore.checkAuth()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 2rem;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.brand-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
}

.brand-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--blue-900);
  letter-spacing: -0.02em;
}

.brand-ai {
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  display: flex;
  gap: 0.5rem;
}

.nav-link {
  padding: 0.625rem 1.25rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
  background: rgba(59, 130, 246, 0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.1);
}

.nav-link:hover {
  color: var(--blue-600);
  background: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.2);
  transform: translateY(-2px);
}

.nav-link.active {
  color: var(--blue-600);
  background: rgba(59, 130, 246, 0.12);
  border-color: rgba(59, 130, 246, 0.25);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.15);
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: 0.25rem;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: var(--blue-500);
  border-radius: 2px;
}

.nav-info {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.date-info {
  text-align: right;
}

.date-year {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.date-lunar {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-top: 0.125rem;
}

.user-section {
  display: flex;
  align-items: center;
}

.login-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.25rem;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border: none;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.user-dropdown-wrapper {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.75rem 0.375rem 0.375rem;
  background: rgba(255, 255, 255, 0.8);
  border: none;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.user-btn:hover {
  background: white;
  box-shadow: var(--shadow-md);
}

.user-avatar,
.user-avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.875rem;
  font-weight: 600;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  color: var(--text-secondary);
  transition: transform 0.3s ease;
}

.dropdown-arrow.rotated {
  transform: rotate(180deg);
}

.glass-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  padding: 0.5rem;
  z-index: 1001;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
}

.dropdown-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1rem;
  font-weight: 600;
}

.dropdown-user-info {
  flex: 1;
  min-width: 0;
}

.dropdown-username {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-email {
  font-size: 0.75rem;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.05);
  margin: 0.5rem 0;
}

.dropdown-menu {
  padding: 0.25rem 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.75rem;
  background: none;
  border: none;
  border-radius: 10px;
  font-size: 0.875rem;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.dropdown-item:hover {
  background: rgba(59, 130, 246, 0.1);
  color: var(--blue-600);
}

.dropdown-item.logout {
  color: #ef4444;
}

.dropdown-item.logout:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.theme-toggle-wrapper {
  position: relative;
}

.theme-toggle {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.8);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--blue-600);
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.theme-toggle:hover {
  background: white;
  box-shadow: var(--shadow-md);
  transform: scale(1.05);
}

.theme-toggle svg {
  width: 20px;
  height: 20px;
  transition: transform 0.3s ease;
}

.theme-menu {
  position: absolute;
  top: 50px;
  right: 0;
  width: 220px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  box-shadow: var(--shadow-xl);
  padding: 0.5rem 0;
  display: none;
  opacity: 0;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  z-index: 1001;
}

.theme-menu.show {
  display: block;
  opacity: 1;
  transform: translateY(0);
}

.theme-menu-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-primary);
}

.theme-menu-item:hover {
  background: rgba(59, 130, 246, 0.1);
  padding-left: 1.25rem;
}

.theme-menu-item.active {
  background: rgba(59, 130, 246, 0.15);
  color: var(--blue-600);
}

.theme-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--blue-500);
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.5);
}

.theme-indicator.light {
  background: #fbbf24;
  box-shadow: 0 0 8px rgba(251, 191, 36, 0.6);
}

.theme-indicator.dark {
  background: #3b82f6;
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.6);
}

.theme-indicator.auto {
  background: linear-gradient(135deg, #fbbf24, #3b82f6);
  box-shadow: 0 0 8px rgba(251, 191, 36, 0.5), 0 0 8px rgba(59, 130, 246, 0.5);
}

.item-label {
  font-size: 0.875rem;
  font-weight: 500;
}

.item-desc {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-top: 0.125rem;
}

@media (max-width: 1024px) {
  .nav-links {
    display: none;
  }
  
  .date-info {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar {
    padding: 1rem;
  }
  
  .brand-text {
    font-size: 1rem;
  }
  
  .user-name {
    display: none;
  }
  
  .login-btn span {
    display: none;
  }
  
  .login-btn {
    padding: 0.625rem;
  }
}
</style>
