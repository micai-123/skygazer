<template>
  <div class="app-container" :data-theme="themeStore.themeMode">
    <Navbar />
    <main class="main-content">
      <transition name="view" mode="out-in">
        <router-view />
      </transition>
    </main>
    <Footer />
    
    <AuthModal 
      :show="authStore.showAuthModal" 
      :mode="authStore.authModalMode"
      @close="authStore.closeAuthModal"
      @update:mode="(mode) => authStore.authModalMode = mode"
    />
    
    <LoginPrompt 
      :show="showLoginPrompt"
      @close="showLoginPrompt = false"
      @confirm="handleLoginPromptConfirm"
    />
  </div>
</template>

<script setup>
import { ref, provide } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useAuthStore } from '@/stores/auth'
import Navbar from '@/components/common/Navbar.vue'
import Footer from '@/components/common/Footer.vue'
import AuthModal from '@/components/auth/AuthModal.vue'
import LoginPrompt from '@/components/auth/LoginPrompt.vue'

// 根组件：组合全局布局（导航栏 / 主体 / 页脚）与全局弹层（登录弹窗、登录提示）
const themeStore = useThemeStore()
const authStore = useAuthStore()

// 当未登录用户触发需鉴权操作时，弹出轻量级「请登录」提示
const showLoginPrompt = ref(false)

function showAuthRequired() {
  showLoginPrompt.value = true
}

function handleLoginPromptConfirm() {
  authStore.openAuthModal('login')
}

// 统一鉴权入口：已登录直接执行回调；未登录则提示登录并把回调暂存，
// 供登录成功后通过 authStore.pendingAction 继续执行
function requireAuth(callback) {
  if (authStore.isAuthenticated) {
    callback()
  } else {
    showLoginPrompt.value = true
    authStore.pendingAction = callback
  }
}

// 以依赖注入方式向任意后代组件暴露两个鉴权辅助方法，避免层层透传 props
provide('showAuthRequired', showAuthRequired)
provide('requireAuth', requireAuth)
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: clamp(1.25rem, 3vw, 2.5rem);
  max-width: 1320px;
  width: 100%;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }
}
</style>
