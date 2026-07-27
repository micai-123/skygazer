<template>
  <div class="weather-hero glass-card" :class="{ loading: weather.isLoading }">
    <div class="hero-main">
      <div class="hero-left">
        <div class="location-header">
          <CitySelector 
            v-model="weather.currentCity" 
            @city-change="handleCityChange"
          />
          <div class="air-quality-badge">
            <span class="quality-dot"></span>
            <span class="quality-text">{{ weather.currentWeather.airQualityLevel }} {{ weather.currentWeather.airQuality }}</span>
          </div>
        </div>
        
        <div class="weather-main">
          <div class="temperature-display">
            <span class="temp-value mono">{{ weather.currentWeather.temperature }}</span>
            <span class="temp-unit">°</span>
          </div>
          <div class="weather-info">
            <p class="weather-description">{{ weather.currentWeather.description }}</p>
            <div class="temp-range mono">{{ weather.currentWeather.lowTemp }}° / {{ weather.currentWeather.highTemp }}°</div>
          </div>
        </div>
        
        <div class="weather-metrics">
          <div class="metric-item">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L12 22M12 2L8 6M12 2L16 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M17.5 10C19 11 20 12.5 20 14.5C20 17.5 17.5 20 14 20H12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span class="mono">{{ weather.currentWeather.wind }}</span>
          </div>
          <div class="metric-item">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M12 2.69L17.66 8.35C19.55 10.24 20.5 12.67 20.5 15.15C20.5 18.5 17.85 21.15 14.5 21.15C12.87 21.15 11.37 20.5 10.25 19.4L12 17.65L13.75 19.4C12.63 20.5 11.13 21.15 9.5 21.15C6.15 21.15 3.5 18.5 3.5 15.15C3.5 12.67 4.45 10.24 6.34 8.35L12 2.69Z" fill="currentColor" opacity="0.3"/>
            </svg>
            <span class="mono">湿度 {{ weather.currentWeather.humidity }}%</span>
          </div>
        </div>
      </div>
      
      <div class="hero-right">
        <div class="weather-icon-large">
          <div class="icon-container">
            <svg width="100" height="100" viewBox="0 0 24 24" fill="none" class="main-weather-icon">
              <circle cx="12" cy="12" r="5" fill="#FCD34D"/>
              <line x1="12" y1="1" x2="12" y2="3" stroke="#FCD34D" stroke-width="2" stroke-linecap="round"/>
              <line x1="12" y1="21" x2="12" y2="23" stroke="#FCD34D" stroke-width="2" stroke-linecap="round"/>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" stroke="#FCD34D" stroke-width="2" stroke-linecap="round"/>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" stroke="#FCD34D" stroke-width="2" stroke-linecap="round"/>
              <line x1="1" y1="12" x2="3" y2="12" stroke="#FCD34D" stroke-width="2" stroke-linecap="round"/>
              <line x1="21" y1="12" x2="23" y2="12" stroke="#FCD34D" stroke-width="2" stroke-linecap="round"/>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" stroke="#FCD34D" stroke-width="2" stroke-linecap="round"/>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" stroke="#FCD34D" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <div class="icon-glow"></div>
          </div>
        </div>
        
        <div class="extra-metrics">
          <div class="extra-metric">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="4" stroke="currentColor" stroke-width="2"/>
              <path d="M12 2V4M12 20V22M2 12H4M20 12H22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span>紫外线</span>
            <span class="metric-value">中等</span>
          </div>
          <div class="extra-metric">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M2 12H22M12 2V22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <circle cx="12" cy="12" r="3" fill="currentColor" opacity="0.3"/>
            </svg>
            <span>能见度</span>
            <span class="metric-value">10km</span>
          </div>
          <div class="extra-metric">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <span>气压</span>
            <span class="metric-value mono">1013hPa</span>
          </div>
        </div>
      </div>
    </div>

    <Transition name="fade">
      <div v-if="weather.isLoading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <p>正在获取天气数据...</p>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useWeatherStore } from '@/stores/weather'
import CitySelector from './CitySelector.vue'

const weather = useWeatherStore()

const handleCityChange = async (city) => {
  await weather.changeCity(city)
}

onMounted(() => {
  weather.initializeWeather()
})
</script>

<style scoped>
.weather-hero {
  padding: 1.5rem clamp(1.25rem, 3vw, 2rem);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 200px;
}

.weather-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(600px 300px at 88% 0%, var(--weather-glow), transparent 70%);
  pointer-events: none;
}

.weather-hero.loading {
  pointer-events: none;
}

.hero-main {
  display: flex;
  flex-direction: row;
  gap: 2rem;
  position: relative;
  z-index: 2;
  align-items: center;
}

.hero-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.hero-right {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  align-items: flex-end;
  flex-shrink: 0;
}

.location-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.air-quality-badge {
  background: rgba(52, 211, 153, 0.12);
  padding: 0.4rem 0.875rem;
  border-radius: 1rem;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--ok);
  display: flex;
  align-items: center;
  gap: 0.375rem;
  border: 1px solid rgba(52, 211, 153, 0.28);
}

.quality-dot {
  width: 7px;
  height: 7px;
  background: var(--ok);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

.weather-main {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.temperature-display {
  display: flex;
  align-items: flex-start;
  gap: 0.125rem;
}

.temp-value {
  font-size: clamp(3rem, 8vw, 4.5rem);
  font-weight: 500;
  line-height: 1;
  letter-spacing: -0.03em;
  color: var(--text-primary);
  text-shadow: 0 0 36px var(--weather-glow);
}

.temp-unit {
  font-size: 1.75rem;
  font-weight: 300;
  color: var(--signal);
  margin-top: 0.25rem;
}

.weather-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.weather-description {
  font-size: 1.125rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.temp-range {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.weather-metrics {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--surface-2);
  padding: 0.5rem 0.875rem;
  border-radius: 0.75rem;
  border: 1px solid var(--border-subtle);
  color: var(--text-secondary);
  font-size: 0.8rem;
  font-weight: 500;
}

.metric-item svg {
  color: var(--signal);
}

.weather-icon-large {
  display: flex;
  justify-content: center;
  align-items: center;
}

.icon-container {
  position: relative;
  width: 100px;
  height: 100px;
}

.main-weather-icon {
  animation: rotate-slow 30s linear infinite;
}

@keyframes rotate-slow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(252, 211, 77, 0.25) 0%, transparent 70%);
  border-radius: 50%;
  animation: glow-pulse 3s ease-in-out infinite;
}

@keyframes glow-pulse {
  0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.15); }
}

.extra-metrics {
  display: flex;
  flex-direction: row;
  gap: 0.75rem;
}

.extra-metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.375rem;
  background: var(--surface-2);
  padding: 0.75rem 0.875rem;
  border-radius: 0.625rem;
  border: 1px solid var(--border-subtle);
  transition: border-color 0.2s var(--ease-out), transform 0.2s var(--ease-out);
  text-align: center;
  min-width: 70px;
}

.extra-metric:hover {
  border-color: var(--signal-line);
  transform: translateY(-2px);
}

.extra-metric svg {
  color: var(--signal);
  flex-shrink: 0;
}

.extra-metric span:first-of-type {
  font-size: 0.7rem;
  color: var(--text-muted);
  font-weight: 500;
}

.metric-value {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--text-primary);
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(11, 15, 26, 0.82);
  backdrop-filter: blur(8px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  z-index: 10;
  border-radius: var(--radius-md);
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--signal-line);
  border-top-color: var(--signal);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-overlay p {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .weather-hero {
    padding: 1.25rem;
    min-height: auto;
  }
  
  .hero-main {
    flex-direction: column;
    gap: 1.25rem;
    align-items: flex-start;
  }
  
  .hero-right {
    width: 100%;
    align-items: center;
    flex-direction: row;
    justify-content: space-between;
  }
  
  .weather-main {
    flex-direction: row;
  }
  
  .location-header {
    flex-direction: row;
    flex-wrap: wrap;
  }
  
  .extra-metrics {
    gap: 0.5rem;
  }
  
  .extra-metric {
    padding: 0.625rem 0.75rem;
    min-width: auto;
  }
  
  .icon-container {
    width: 80px;
    height: 80px;
  }
  
  .main-weather-icon {
    width: 80px;
    height: 80px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .main-weather-icon,
  .icon-glow,
  .quality-dot {
    animation: none;
  }
}
</style>
