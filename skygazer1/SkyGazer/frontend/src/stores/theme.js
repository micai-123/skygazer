import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref(localStorage.getItem('theme') || 'auto')
  const themeMenuOpen = ref(false)

  const themes = [
    { id: 'light', name: '浅色模式', desc: '明亮背景与深色文字，适合白天使用' },
    { id: 'dark', name: '深色模式', desc: '深色背景与浅色文字，适合夜晚使用' },
    { id: 'auto', name: '自动模式', desc: '根据天气和时间自动切换主题' }
  ]

  function setTheme(theme) {
    currentTheme.value = theme
    localStorage.setItem('theme', theme)
    applyTheme(theme)
    themeMenuOpen.value = false
  }

  function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme)
    
    if (theme === 'auto') {
      const hour = new Date().getHours()
      const isDark = hour < 6 || hour >= 18
      document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light')
    }
  }

  function toggleThemeMenu() {
    themeMenuOpen.value = !themeMenuOpen.value
  }

  function closeThemeMenu() {
    themeMenuOpen.value = false
  }

  watch(currentTheme, (newTheme) => {
    applyTheme(newTheme)
  }, { immediate: true })

  return {
    currentTheme,
    themeMenuOpen,
    themes,
    setTheme,
    toggleThemeMenu,
    closeThemeMenu
  }
})
