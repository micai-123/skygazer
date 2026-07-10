import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '实时预览 | 智观天象 AI' }
  },
  {
    path: '/analysis',
    name: 'Analysis',
    component: () => import('@/views/AnalysisView.vue'),
    meta: { title: '智能分析 | 智观天象 AI' }
  },
  {
    path: '/map',
    name: 'Map',
    component: () => import('@/views/MapView.vue'),
    meta: { title: '气象地图 | 智观天象 AI' }
  },
  {
    path: '/lifestyle',
    name: 'Lifestyle',
    component: () => import('@/views/LifestyleView.vue'),
    meta: { title: '生活指数 | 智观天象 AI' }
  },
  {
    path: '/ai-assistant',
    name: 'AIAssistant',
    component: () => import('@/views/AIAssistantView.vue'),
    meta: { title: '天象智囊 | 智观天象 AI' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { 
      title: '个人中心 | 智观天象 AI',
      requiresAuth: true
    }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SettingsView.vue'),
    meta: { 
      title: '设置 | 智观天象 AI',
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '智观天象 AI'
  
  if (to.meta.requiresAuth) {
    const authStore = useAuthStore()
    
    if (!authStore.isAuthenticated) {
      authStore.openAuthModal('login')
      authStore.pendingAction = () => {
        next(to.fullPath)
      }
      next({ name: 'Home' })
      return
    }
  }
  
  next()
})

export default router
