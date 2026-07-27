import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const savedTheme = localStorage.getItem('user-theme')
  const primaryColor = ref(savedTheme?.primaryColor || '#34E3E0')
  const themeMode = ref(savedTheme?.mode || 'dark')

  const presetColors = [
    { name: '极光青', value: '#34E3E0' },
    { name: '森林绿', value: '#34D399' },
    { name: '日落橙', value: '#FF9F45' },
    { name: '葡萄紫', value: '#a855f7' },
    { name: '玫瑰红', value: '#F87171' },
    { name: '晴空蓝', value: '#38BDF8' },
    { name: '月光银', value: '#94A3B8' },
    { name: '深海墨', value: '#0B3B45' }
  ]

  const setPrimaryColor = (color) => {
    primaryColor.value = color
    applyTheme()
    saveTheme()
  }

  const setThemeMode = (mode) => {
    themeMode.value = mode
    applyTheme()
    saveTheme()
  }

  const applyTheme = () => {
    document.documentElement.style.setProperty('--user-primary', primaryColor.value)
    document.documentElement.setAttribute('data-theme', themeMode.value)
  }

  const saveTheme = () => {
    localStorage.setItem('user-theme', JSON.stringify({
      primaryColor: primaryColor.value,
      mode: themeMode.value
    }))
  }

  const resetTheme = () => {
    primaryColor.value = '#34E3E0'
    themeMode.value = 'dark'
    applyTheme()
    saveTheme()
  }

  const themes = [
    { id: 'light', name: '浅色', desc: '明亮清晰' },
    { id: 'dark', name: '深色', desc: '气象指挥中心' },
    { id: 'auto', name: '自动', desc: '跟随系统' }
  ]

  const themeMenuOpen = ref(false)
  const toggleThemeMenu = () => { themeMenuOpen.value = !themeMenuOpen.value }
  const closeThemeMenu = () => { themeMenuOpen.value = false }
  const setTheme = (id) => { setThemeMode(id); themeMenuOpen.value = false }

  const currentTheme = computed(() => themeMode.value)

  watch([primaryColor, themeMode], () => {
    applyTheme()
  })

  if (savedTheme) {
    try {
      const parsed = JSON.parse(savedTheme)
      primaryColor.value = parsed.primaryColor || '#34E3E0'
      themeMode.value = parsed.mode || 'dark'
      applyTheme()
    } catch (e) {
      console.error('Failed to parse saved theme:', e)
    }
  }

  return {
    primaryColor,
    themeMode,
    currentTheme,
    themes,
    themeMenuOpen,
    presetColors,
    setPrimaryColor,
    setThemeMode,
    setTheme,
    toggleThemeMenu,
    closeThemeMenu,
    resetTheme
  }
})