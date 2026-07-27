// 前端路由表：采用基于 history 模式的懒加载路由（按页面分包，首屏更快）。
// 各路由 meta.title 用于动态设置页面标题；meta.requiresAuth 标记需登录才能访问的页面。
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

// 全局前置守卫：每次路由跳转前执行
router.beforeEach((to, from, next) => {
  // 1) 根据目标路由的 meta.title 设置浏览器标签页标题
  document.title = to.meta.title || '智观天象 AI'
  
  // 2) 登录保护：标记了 requiresAuth 且当前未登录的页面，
  //    弹出登录弹窗并把「登录后跳转回原页面」的动作暂存为 pendingAction，随后重定向到首页
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
