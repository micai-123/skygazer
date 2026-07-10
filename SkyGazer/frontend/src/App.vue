<template>
  <div class="app-container" :data-theme="themeStore.currentTheme">
    <Navbar />
    <main class="main-content">
      <PageTransition>
        <router-view />
      </PageTransition>
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
import PageTransition from '@/components/common/PageTransition.vue'

const themeStore = useThemeStore()
const authStore = useAuthStore()

const showLoginPrompt = ref(false)
let pendingNavigation = ref(null)

function showAuthRequired() {
  showLoginPrompt.value = true
}

function handleLoginPromptConfirm() {
  authStore.openAuthModal('login')
}

function requireAuth(callback) {
  if (authStore.isAuthenticated) {
    callback()
  } else {
    showLoginPrompt.value = true
    authStore.pendingAction = callback
  }
}

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
  padding: 2rem;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }
}
</style>
