<template>
  <div class="hourly-forecast glass-card" ref="containerRef">
    <h3 class="section-title">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
        <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      72小时预测流
      <span v-if="weather.isLoading" class="loading-indicator">
        <svg class="spinner" width="16" height="16" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" opacity="0.3"/>
          <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </span>
      <span class="scroll-hint">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path d="M8 5L3 12L8 19M16 5L21 12L16 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        滚动查看更多
      </span>
    </h3>
    
    <div v-if="weather.error && !hasValidData" class="error-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
        <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <p class="error-text">{{ weather.error }}</p>
      <button class="retry-btn" @click="handleRetry">重试</button>
    </div>
    
    <div v-else-if="weather.isLoading && !hasValidData" class="loading-state">
      <div class="skeleton-scroll">
        <div v-for="i in 8" :key="i" class="skeleton-item">
          <div class="skeleton-time"></div>
          <div class="skeleton-icon"></div>
          <div class="skeleton-temp"></div>
        </div>
      </div>
    </div>
    
    <div 
      v-else-if="hasValidData" 
      ref="scrollContainer"
      class="forecast-scroll"
      @wheel="handleWheel"
    >
      <div 
        v-for="(item, index) in forecastData" 
        :key="index"
        class="forecast-item"
        :class="{ highlight: item.highlight }"
        @mouseenter="showPopup($event, item)"
        @mouseleave="hidePopup"
      >
        <div v-if="item.precipitation > 0" class="precipitation-badge">
          {{ item.precipitation }}mm
        </div>
        <span class="forecast-time">{{ item.time }}</span>
        <div class="forecast-icon" v-html="getWeatherIcon(item.icon)"></div>
        <span class="forecast-temp">{{ formatTemp(item.temp) }}</span>
      </div>
    </div>
    
    <div v-else class="empty-state">
      <p>暂无小时预报数据</p>
    </div>
    
    <transition name="popup-fade">
      <div 
        v-if="popupVisible && popupItem"
        class="detail-popup"
        :style="popupStyle"
        @mouseenter="cancelHide"
        @mouseleave="hidePopup"
      >
        <div class="popup-header">
          <span class="popup-time">{{ popupItem.time }}</span>
          <span class="popup-temp">{{ formatTemp(popupItem.temp) }}</span>
        </div>
        <div class="popup-body">
          <div class="popup-row">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M12 2C12 2 5 9 5 14C5 17.87 8.13 21 12 21C15.87 21 19 17.87 19 14C19 9 12 2 12 2Z" fill="currentColor" opacity="0.5"/>
            </svg>
            <span class="popup-label">湿度</span>
            <span class="popup-value">{{ popupItem.humidity !== null && popupItem.humidity !== undefined ? popupItem.humidity + '%' : '--' }}</span>
          </div>
          <div class="popup-row">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L12 22M12 2L8 6M12 2L16 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="popup-label">风速</span>
            <span class="popup-value">{{ popupItem.windSpeed ? popupItem.windSpeed + ' m/s' : '--' }}</span>
          </div>
          <div class="popup-row">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M12 2.69L17.66 8.35C19.55 10.24 20.5 12.67 20.5 15.15C20.5 18.5 17.85 21.15 14.5 21.15C12.87 21.15 11.37 20.5 10.25 19.4L12 17.65L13.75 19.4C12.63 20.5 11.13 21.15 9.5 21.15C6.15 21.15 3.5 18.5 3.5 15.15C3.5 12.67 4.45 10.24 6.34 8.35L12 2.69Z" fill="currentColor" opacity="0.5"/>
            </svg>
            <span class="popup-label">降水</span>
            <span class="popup-value">{{ popupItem.precipitation ? popupItem.precipitation + ' mm' : '0 mm' }}</span>
          </div>
          <div class="popup-row">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
              <path d="M12 5V3M12 21V19M5 12H3M21 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span class="popup-label">风向</span>
            <span class="popup-value">{{ popupItem.windDirection || '--' }}</span>
          </div>
        </div>
        <div class="popup-arrow"></div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useWeatherStore } from '@/stores/weather'
import { getWeatherIcon } from '@/utils/icons'

const weather = useWeatherStore()
const scrollContainer = ref(null)
const containerRef = ref(null)

const popupVisible = ref(false)
const popupItem = ref(null)
const popupStyle = ref({})
let hideTimeout = null

const hasValidData = computed(() => {
  const data = weather.hourlyForecast
  return Array.isArray(data) && data.length > 0
})

const forecastData = computed(() => {
  if (!hasValidData.value) return []
  return weather.hourlyForecast.slice(0, 72)
})

function formatTemp(temp) {
  if (temp === null || temp === undefined || temp === '--' || isNaN(temp)) {
    return '--°'
  }
  return `${Math.round(temp)}°`
}

function handleWheel(event) {
  if (!scrollContainer.value) return
  
  event.preventDefault()
  
  const scrollAmount = event.deltaY || event.deltaX
  const scrollSpeed = 2
  
  scrollContainer.value.scrollLeft += scrollAmount * scrollSpeed
}

function showPopup(event, item) {
  if (hideTimeout) {
    clearTimeout(hideTimeout)
    hideTimeout = null
  }
  
  popupItem.value = item
  popupVisible.value = true
  
  const target = event.currentTarget
  const rect = target.getBoundingClientRect()
  const containerRect = containerRef.value.getBoundingClientRect()
  
  let left = rect.left - containerRect.left + rect.width / 2
  const popupWidth = 180
  
  if (left < popupWidth / 2) {
    left = popupWidth / 2
  } else if (left > containerRect.width - popupWidth / 2) {
    left = containerRect.width - popupWidth / 2
  }
  
  const top = rect.top - containerRect.top - 10
  
  popupStyle.value = {
    left: `${left}px`,
    top: `${top}px`,
    transform: 'translate(-50%, -100%)'
  }
}

function hidePopup() {
  hideTimeout = setTimeout(() => {
    popupVisible.value = false
    popupItem.value = null
  }, 100)
}

function cancelHide() {
  if (hideTimeout) {
    clearTimeout(hideTimeout)
    hideTimeout = null
  }
}

function handleRetry() {
  weather.refreshWeather()
}
</script>

<style scoped>
.hourly-forecast {
  padding: 1.5rem;
  min-height: 180px;
  position: relative;
}

.section-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--blue-900);
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-title svg {
  color: var(--blue-500);
}

.loading-indicator {
  margin-left: auto;
}

.scroll-hint {
  margin-left: auto;
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 0.25rem;
  opacity: 0.7;
}

.spinner {
  animation: spin 1s linear infinite;
  color: var(--blue-500);
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.forecast-scroll {
  display: flex;
  gap: 1.5rem;
  overflow-x: auto;
  padding-bottom: 1rem;
  scrollbar-width: none;
  -ms-overflow-style: none;
  cursor: grab;
}

.forecast-scroll::-webkit-scrollbar {
  display: none;
}

.forecast-scroll:active {
  cursor: grabbing;
}

.forecast-item {
  flex-shrink: 0;
  width: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 0;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.forecast-item:hover {
  transform: translateY(-4px);
}

.forecast-item.highlight {
  background: rgba(59, 130, 246, 0.1);
  border-radius: 1rem;
  padding: 1rem;
}

.forecast-time {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-muted);
}

.forecast-item.highlight .forecast-time {
  color: var(--blue-600);
}

.forecast-icon {
  font-size: 2rem;
}

.forecast-temp {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--blue-900);
}

.precipitation-badge {
  font-size: 0.625rem;
  background: rgba(59, 130, 246, 0.15);
  color: var(--blue-600);
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
  font-weight: 600;
  white-space: nowrap;
}

.detail-popup {
  position: absolute;
  width: 180px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  padding: 1rem;
  z-index: 1000;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.1);
  pointer-events: auto;
}

.popup-arrow {
  position: absolute;
  bottom: -6px;
  left: 50%;
  width: 12px;
  height: 12px;
  background: rgba(255, 255, 255, 0.98);
  transform: translateX(-50%) rotate(45deg);
  border-right: 1px solid rgba(59, 130, 246, 0.1);
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  margin-bottom: 0.75rem;
}

.popup-time {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--blue-600);
}

.popup-temp {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--blue-900);
}

.popup-body {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.popup-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.popup-row svg {
  color: var(--blue-500);
  flex-shrink: 0;
}

.popup-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  flex: 1;
}

.popup-value {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-primary);
}

.popup-fade-enter-active,
.popup-fade-leave-active {
  transition: all 0.2s ease;
}

.popup-fade-enter-from,
.popup-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -90%);
}

.loading-state {
  display: flex;
  justify-content: center;
}

.skeleton-scroll {
  display: flex;
  gap: 1.5rem;
  width: 100%;
}

.skeleton-item {
  flex-shrink: 0;
  width: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 0;
}

.skeleton-time {
  width: 40px;
  height: 12px;
  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 50%, #e0e0e0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

.skeleton-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 50%, #e0e0e0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 50%;
}

.skeleton-temp {
  width: 30px;
  height: 18px;
  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 50%, #e0e0e0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: var(--text-secondary);
}

.error-state svg {
  color: #ef4444;
  margin-bottom: 1rem;
}

.error-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 1rem;
  text-align: center;
}

.retry-btn {
  padding: 0.5rem 1.5rem;
  background: var(--blue-500);
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  background: var(--blue-600);
  transform: translateY(-2px);
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: var(--text-secondary);
  font-size: 0.875rem;
}
</style>
