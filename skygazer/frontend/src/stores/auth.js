import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isLoading = ref(false)
  const error = ref(null)
  const showAuthModal = ref(false)
  const authModalMode = ref('login')
  const pendingAction = ref(null)

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  
  const userName = computed(() => user.value?.nickname || user.value?.username || '用户')
  
  const userAvatar = computed(() => user.value?.avatar || null)

  async function login(credentials) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await authApi.login(credentials)
      
      if (response.code === 200) {
        const { token: authToken, user: userData } = response.data
        
        token.value = authToken
        user.value = userData
        
        localStorage.setItem('token', authToken)
        localStorage.setItem('user', JSON.stringify(userData))
        
        showAuthModal.value = false
        
        if (pendingAction.value) {
          pendingAction.value()
          pendingAction.value = null
        }
        
        return { success: true }
      } else {
        error.value = response.message || '登录失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      const errorMessage = err.message || err.response?.data?.message || '网络错误，请稍后重试'
      error.value = errorMessage
      return { success: false, message: errorMessage }
    } finally {
      isLoading.value = false
    }
  }

  async function register(userData) {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await authApi.register(userData)
      
      if (response.code === 200) {
        const { token: authToken, user: userInfo } = response.data
        
        token.value = authToken
        user.value = userInfo
        
        localStorage.setItem('token', authToken)
        localStorage.setItem('user', JSON.stringify(userInfo))
        
        showAuthModal.value = false
        
        return { success: true }
      } else {
        error.value = response.message || '注册失败'
        return { success: false, message: error.value }
      }
    } catch (err) {
      const errorMessage = err.message || err.response?.data?.message || '网络错误，请稍后重试'
      error.value = errorMessage
      return { success: false, message: errorMessage }
    } finally {
      isLoading.value = false
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/')
  }

  function openAuthModal(mode = 'login', action = null) {
    authModalMode.value = mode
    pendingAction.value = action
    showAuthModal.value = true
  }

  function closeAuthModal() {
    showAuthModal.value = false
    pendingAction.value = null
    error.value = null
  }

  function requireAuth(action) {
    if (isAuthenticated.value) {
      action()
    } else {
      pendingAction.value = action
      openAuthModal('login')
    }
  }

  function checkAuth() {
    if (token.value && !user.value) {
      const storedUser = localStorage.getItem('user')
      if (storedUser) {
        user.value = JSON.parse(storedUser)
      }
    }
    return isAuthenticated.value
  }

  return {
    token,
    user,
    isLoading,
    error,
    showAuthModal,
    authModalMode,
    pendingAction,
    isAuthenticated,
    userName,
    userAvatar,
    login,
    register,
    logout,
    openAuthModal,
    closeAuthModal,
    requireAuth,
    checkAuth
  }
})
