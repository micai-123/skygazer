<template>
  <div class="lifestyle-cards">
    <h3 class="section-main-title">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
      </svg>
      生活决策
    </h3>
    <div class="cards-grid">
      <div 
        v-for="(item, index) in weather.lifestyleIndices" 
        :key="index"
        class="lifestyle-card glass-card"
        @click="handleCardClick(item)"
      >
        <div class="card-icon" :class="item.color" v-html="getLifestyleIcon(item.icon)"></div>
        <h4 class="card-title">{{ item.title }}</h4>
        <p class="card-desc">{{ item.desc }}</p>
        <div class="card-footer">
          <span>{{ item.level }}</span>
          <button class="detail-btn" @click.stop="handleViewDetails(item)">
            查看详情
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject } from 'vue'
import { useRouter } from 'vue-router'
import { useWeatherStore } from '@/stores/weather'
import { useAuthStore } from '@/stores/auth'
import { getLifestyleIcon } from '@/utils/icons'

const router = useRouter()
const weather = useWeatherStore()
const authStore = useAuthStore()
const requireAuth = inject('requireAuth')

function handleCardClick(item) {
  handleViewDetails(item)
}

function handleViewDetails(item) {
  requireAuth(() => {
    router.push('/lifestyle')
  })
}
</script>

<style scoped>
.lifestyle-cards {
  width: 100%;
}

.section-main-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-main-title svg {
  color: var(--signal);
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
}

.lifestyle-card {
  padding: 1.25rem;
  cursor: pointer;
  transition: transform 0.25s var(--ease-out), border-color 0.25s var(--ease-out), box-shadow 0.25s var(--ease-out);
}

.lifestyle-card:hover {
  transform: translateY(-4px);
  border-color: var(--signal-line);
  box-shadow: var(--shadow-md);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
}

.card-icon.orange {
  background: rgba(255, 159, 69, 0.15);
  color: var(--warn);
}

.card-icon.blue {
  background: var(--signal-soft);
  color: var(--signal);
}

.card-icon.green {
  background: rgba(52, 211, 153, 0.15);
  color: var(--ok);
}

.card-icon.purple {
  background: rgba(168, 85, 247, 0.15);
  color: #C084FC;
}

.card-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.card-desc {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 1rem;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--signal);
}

.detail-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.375rem 0.75rem;
  background: var(--signal);
  border: none;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--on-signal);
  cursor: pointer;
  transition: transform 0.15s var(--ease-out), box-shadow 0.2s var(--ease-out);
}

.detail-btn:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 14px rgba(52, 227, 224, 0.3);
}

.detail-btn svg {
  transition: transform 0.3s var(--ease-out);
}

.detail-btn:hover svg {
  transform: translateX(2px);
}
</style>
