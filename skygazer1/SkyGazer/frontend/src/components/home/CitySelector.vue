<template>
  <div class="city-selector-wrapper">
    <div class="city-selector">
      <div class="city-trigger" @click="toggleDropdown">
        <svg class="location-icon" width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="currentColor"/>
        </svg>
        <span class="current-city">{{ currentCity?.name || '选择城市' }}</span>
        <svg class="arrow-icon" :class="{ rotated: isOpen }" width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    </div>

    <button 
      class="refresh-btn" 
      :class="{ loading: isRefreshing }" 
      :disabled="isRefreshing"
      @click="handleRefresh"
      :title="isRefreshing ? '正在获取数据...' : '获取实时天气'"
    >
      <svg 
        class="refresh-icon" 
        :class="{ spinning: isRefreshing }" 
        width="18" 
        height="18" 
        viewBox="0 0 24 24" 
        fill="none"
      >
        <path d="M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C15.3019 3 18.1885 4.77814 19.7545 7.42909" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        <path d="M21 3V7H17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>

    <Teleport to="body">
      <Transition name="fade">
        <div v-if="isOpen" class="city-dropdown-overlay" @click="closeDropdown"></div>
      </Transition>
      
      <Transition name="slide">
        <div v-if="isOpen" class="city-dropdown" :style="dropdownStyle">
          <div class="search-section">
            <div class="search-input-wrapper">
              <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none">
                <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
                <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <input
                ref="searchInput"
                v-model="searchKeyword"
                type="text"
                class="search-input"
                placeholder="搜索城市（拼音/汉字）"
                @input="handleSearch"
              />
              <button v-if="searchKeyword" class="clear-btn" @click="clearSearch">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </div>

          <div v-if="!searchKeyword" class="hot-cities-section">
            <div class="section-title">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
              </svg>
              热门城市
            </div>
            <div class="hot-cities-grid">
              <button
                v-for="city in hotCities"
                :key="city.code"
                class="hot-city-btn"
                :class="{ active: currentCity?.code === city.code }"
                @click="selectCity(city)"
              >
                {{ city.name }}
              </button>
            </div>
          </div>

          <div v-if="!searchKeyword" class="alphabet-section">
            <div class="alphabet-nav">
              <button
                v-for="letter in alphabet"
                :key="letter"
                class="alphabet-btn"
                :class="{ active: selectedLetter === letter }"
                @click="scrollToLetter(letter)"
              >
                {{ letter }}
              </button>
            </div>
          </div>

          <div class="city-list-section" ref="cityListRef">
            <div v-if="searchKeyword && searchResults.length > 0" class="search-results">
              <button
                v-for="city in searchResults"
                :key="city.code"
                class="city-item"
                :class="{ active: currentCity?.code === city.code }"
                @click="selectCity(city)"
              >
                <span class="city-name">{{ city.name }}</span>
                <span class="city-province">{{ city.province }}</span>
              </button>
            </div>

            <div v-else-if="searchKeyword && searchResults.length === 0" class="no-results">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="1.5" opacity="0.5"/>
                <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.5"/>
                <path d="M8 11H14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.5"/>
              </svg>
              <p>未找到匹配的城市</p>
            </div>

            <div v-else class="alphabet-city-list">
              <div
                v-for="letter in alphabet"
                :key="letter"
                :ref="el => letterRefs[letter] = el"
                class="letter-group"
              >
                <div class="letter-header">{{ letter }}</div>
                <button
                  v-for="city in getCitiesByLetter(letter)"
                  :key="city.code"
                  class="city-item"
                  :class="{ active: currentCity?.code === city.code }"
                  @click="selectCity(city)"
                >
                  <span class="city-name">{{ city.name }}</span>
                  <span class="city-province">{{ city.province }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>

      <Transition name="toast">
        <div v-if="showToast" class="toast-message" :class="toastType">
          <svg v-if="toastType === 'success'" width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M8 12L11 15L16 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span>{{ toastMessage }}</span>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { cities, hotCities, alphabet, searchCities, getCitiesByInitial } from '@/data/cities'
import { useWeatherStore } from '@/stores/weather'

const props = defineProps({
  modelValue: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'city-change'])

const weather = useWeatherStore()

const isOpen = ref(false)
const searchKeyword = ref('')
const selectedLetter = ref('')
const searchInput = ref(null)
const cityListRef = ref(null)
const letterRefs = ref({})
const dropdownStyle = ref({})
const isRefreshing = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

const currentCity = computed(() => props.modelValue)

const searchResults = computed(() => {
  if (!searchKeyword.value) return []
  return searchCities(searchKeyword.value).slice(0, 50)
})

const getCitiesByLetter = (letter) => {
  return getCitiesByInitial(letter)
}

const showtoastMessage = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

const handleRefresh = async () => {
  if (isRefreshing.value) return
  
  isRefreshing.value = true
  
  try {
    showtoastMessage('正在获取当前城市数据...', 'info')
    
    await weather.refreshWeather()
    
    showtoastMessage('天气数据获取成功', 'success')
  } catch (error) {
    console.error('Failed to refresh weather:', error)
    showtoastMessage(error.message || '获取天气数据失败，请重试', 'error')
  } finally {
    isRefreshing.value = false
  }
}

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    updateDropdownPosition()
    nextTick(() => {
      searchInput.value?.focus()
    })
  }
}

const closeDropdown = () => {
  isOpen.value = false
  searchKeyword.value = ''
  selectedLetter.value = ''
}

const updateDropdownPosition = () => {
  const trigger = document.querySelector('.city-trigger')
  if (trigger) {
    const rect = trigger.getBoundingClientRect()
    const viewportHeight = window.innerHeight
    const viewportWidth = window.innerWidth
    const spaceBelow = viewportHeight - rect.bottom
    const spaceAbove = rect.top
    
    const dropdownWidth = Math.min(Math.max(rect.width, 320), Math.min(480, viewportWidth - 32))
    
    if (spaceBelow < 450 && spaceAbove > spaceBelow) {
      dropdownStyle.value = {
        position: 'fixed',
        top: 'auto',
        bottom: `${viewportHeight - rect.top + 8}px`,
        left: `${Math.max(16, rect.left)}px`,
        width: `${dropdownWidth}px`,
        maxHeight: '450px'
      }
    } else {
      dropdownStyle.value = {
        position: 'fixed',
        top: `${rect.bottom + 8}px`,
        left: `${Math.max(16, rect.left)}px`,
        width: `${dropdownWidth}px`,
        maxHeight: `${Math.min(spaceBelow - 20, 450)}px`
      }
    }
  }
}

const handleSearch = () => {
  selectedLetter.value = ''
}

const clearSearch = () => {
  searchKeyword.value = ''
  searchInput.value?.focus()
}

const scrollToLetter = (letter) => {
  selectedLetter.value = letter
  const element = letterRefs.value[letter]
  if (element && cityListRef.value) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const selectCity = (city) => {
  emit('update:modelValue', city)
  emit('city-change', city)
  closeDropdown()
}

const handleKeydown = (e) => {
  if (e.key === 'Escape' && isOpen.value) {
    closeDropdown()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('resize', updateDropdownPosition)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('resize', updateDropdownPosition)
})
</script>

<style scoped>
.city-selector-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.city-selector {
  position: relative;
}

.city-trigger {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(147, 197, 253, 0.3);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.city-trigger:hover {
  background: rgba(255, 255, 255, 0.7);
  border-color: var(--blue-400);
}

.location-icon {
  color: var(--blue-500);
}

.current-city {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--blue-900);
}

.arrow-icon {
  color: var(--blue-500);
  transition: transform 0.2s ease;
}

.arrow-icon.rotated {
  transform: rotate(180deg);
}

.refresh-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(147, 197, 253, 0.3);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--blue-500);
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.7);
  border-color: var(--blue-400);
  color: var(--blue-600);
  transform: scale(1.05);
}

.refresh-btn:active:not(:disabled) {
  transform: scale(0.95);
}

.refresh-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.refresh-btn.loading {
  background: rgba(59, 130, 246, 0.1);
  border-color: var(--blue-400);
}

.refresh-icon {
  transition: transform 0.3s ease;
}

.refresh-icon.spinning {
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

.toast-message {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1.25rem;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  z-index: 2000;
  font-size: 0.9375rem;
  font-weight: 500;
}

.toast-message.success {
  color: var(--green-500);
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.toast-message.error {
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.toast-message.info {
  color: var(--blue-500);
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.toast-message svg {
  flex-shrink: 0;
}

.city-dropdown-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: transparent;
  z-index: 999;
}

.city-dropdown {
  position: fixed;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(147, 197, 253, 0.3);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.search-section {
  padding: 1rem;
  border-bottom: 1px solid rgba(147, 197, 253, 0.2);
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: rgba(241, 245, 249, 0.8);
  border: 1px solid rgba(147, 197, 253, 0.3);
  border-radius: 12px;
  padding: 0.75rem 1rem;
  transition: all 0.2s ease;
}

.search-input-wrapper:focus-within {
  border-color: var(--blue-400);
  background: rgba(255, 255, 255, 0.9);
}

.search-icon {
  color: var(--text-muted);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.9375rem;
  color: var(--text-primary);
  outline: none;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.clear-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  color: var(--text-secondary);
}

.hot-cities-section {
  padding: 1rem;
  border-bottom: 1px solid rgba(147, 197, 253, 0.2);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 0.75rem;
}

.section-title svg {
  color: var(--orange-400);
}

.hot-cities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
  gap: 0.5rem;
}

.hot-city-btn {
  padding: 0.5rem 0.75rem;
  background: rgba(241, 245, 249, 0.6);
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.hot-city-btn:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: var(--blue-400);
  color: var(--blue-600);
}

.hot-city-btn.active {
  background: var(--blue-500);
  color: white;
  border-color: var(--blue-500);
}

.alphabet-section {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid rgba(147, 197, 253, 0.2);
}

.alphabet-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.alphabet-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.alphabet-btn:hover {
  background: rgba(59, 130, 246, 0.1);
  color: var(--blue-600);
}

.alphabet-btn.active {
  background: var(--blue-500);
  color: white;
}

.city-list-section {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem 0;
}

.city-list-section::-webkit-scrollbar {
  width: 6px;
}

.city-list-section::-webkit-scrollbar-track {
  background: transparent;
}

.city-list-section::-webkit-scrollbar-thumb {
  background: rgba(147, 197, 253, 0.5);
  border-radius: 3px;
}

.city-list-section::-webkit-scrollbar-thumb:hover {
  background: rgba(147, 197, 253, 0.8);
}

.letter-group {
  padding: 0 1rem;
}

.letter-header {
  position: sticky;
  top: 0;
  background: rgba(241, 245, 249, 0.9);
  padding: 0.5rem 0;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--blue-500);
  letter-spacing: 0.05em;
}

.city-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0.75rem 0.5rem;
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.city-item:hover {
  background: rgba(59, 130, 246, 0.08);
}

.city-item.active {
  background: rgba(59, 130, 246, 0.15);
}

.city-name {
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--text-primary);
}

.city-province {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  color: var(--text-muted);
}

.no-results svg {
  margin-bottom: 1rem;
}

.no-results p {
  font-size: 0.9375rem;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}

@media (max-width: 640px) {
  .city-dropdown {
    left: 16px !important;
    right: 16px !important;
    width: auto !important;
    max-width: none !important;
  }

  .hot-cities-grid {
    grid-template-columns: repeat(5, 1fr);
  }

  .alphabet-btn {
    width: 28px;
    height: 28px;
    font-size: 0.6875rem;
  }
  
  .toast-message {
    left: 16px;
    right: 16px;
    transform: none;
  }
}

@media (prefers-color-scheme: dark) {
  .city-trigger {
    background: rgba(30, 41, 59, 0.5);
    border-color: rgba(71, 85, 105, 0.3);
  }

  .city-trigger:hover {
    background: rgba(30, 41, 59, 0.7);
  }

  .current-city {
    color: var(--text-primary);
  }

  .refresh-btn {
    background: rgba(30, 41, 59, 0.5);
    border-color: rgba(71, 85, 105, 0.3);
  }

  .refresh-btn:hover:not(:disabled) {
    background: rgba(30, 41, 59, 0.7);
  }

  .city-dropdown {
    background: rgba(30, 41, 59, 0.95);
    border-color: rgba(71, 85, 105, 0.3);
  }

  .search-input-wrapper {
    background: rgba(15, 23, 42, 0.6);
    border-color: rgba(71, 85, 105, 0.3);
  }

  .search-input-wrapper:focus-within {
    background: rgba(30, 41, 59, 0.8);
  }

  .hot-city-btn {
    background: rgba(51, 65, 85, 0.6);
  }

  .hot-city-btn:hover {
    background: rgba(59, 130, 246, 0.2);
  }

  .letter-header {
    background: rgba(30, 41, 59, 0.9);
  }

  .city-item:hover {
    background: rgba(59, 130, 246, 0.15);
  }

  .toast-message {
    background: rgba(30, 41, 59, 0.95);
  }
}
</style>
