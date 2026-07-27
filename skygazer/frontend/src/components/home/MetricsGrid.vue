<template>
  <div class="metrics-grid">
    <div class="metric-card glass-card">
      <div class="metric-header">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="5" fill="#FFA726"/>
          <path d="M12 2V4M12 20V22M2 12H4M20 12H22M4.93 4.93L6.34 6.34M17.66 17.66L19.07 19.07M4.93 19.07L6.34 17.66M17.66 6.34L19.07 4.93" stroke="#FFA726" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>紫外线强度</span>
      </div>
      <div class="metric-body">
        <p class="metric-value">{{ weather.metrics.uv.value }}</p>
        <p class="metric-desc">{{ weather.metrics.uv.desc }}</p>
      </div>
    </div>
    
    <div class="metric-card glass-card">
      <div class="metric-header">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
          <path d="M12 5V3M12 21V19M5 12H3M21 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>能见度</span>
      </div>
      <div class="metric-body">
        <p class="metric-value">{{ weather.metrics.visibility.value }}</p>
        <p class="metric-desc">{{ weather.metrics.visibility.desc }}</p>
      </div>
    </div>
    
    <div class="metric-card glass-card">
      <div class="metric-header">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <path d="M12 8V12L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>气压</span>
      </div>
      <div class="metric-body">
        <p class="metric-value">{{ weather.metrics.pressure.value }}</p>
        <p class="metric-desc">{{ weather.metrics.pressure.desc }}</p>
      </div>
    </div>
    
    <div class="metric-card glass-card">
      <div class="metric-header">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M13 6H20M20 6V13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>过敏风险</span>
      </div>
      <div class="metric-body">
        <p class="metric-value">{{ weather.metrics.allergy.value }}</p>
        <p class="metric-desc">{{ weather.metrics.allergy.desc }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useWeatherStore } from '@/stores/weather'

const weather = useWeatherStore()
</script>

<style scoped>
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.metric-card {
  position: relative;
  padding: 1.1rem 1.1rem 1rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 120px;
  overflow: hidden;
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, var(--signal), transparent);
  opacity: 0.7;
}

.metric-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-secondary);
}

.metric-header svg {
  color: var(--signal);
}

.metric-body {
  margin-top: 0.5rem;
}

.metric-value {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-variant-numeric: tabular-nums;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.metric-desc {
  font-size: 0.625rem;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
}
</style>
