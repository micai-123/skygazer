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
  color: var(--blue-900);
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-main-title svg {
  color: var(--blue-500);
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
}

.lifestyle-card {
  padding: 1.25rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.lifestyle-card:hover {
  transform: translateY(-0.5rem);
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
  background: rgba(251, 146, 60, 0.15);
  color: #fb923c;
}

.card-icon.blue {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.card-icon.green {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.card-icon.purple {
  background: rgba(168, 85, 247, 0.15);
  color: #a855f7;
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
  color: var(--blue-600);
}

.detail-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.375rem 0.75rem;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border: none;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.detail-btn:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.detail-btn svg {
  transition: transform 0.3s ease;
}

.detail-btn:hover svg {
  transform: translateX(2px);
}
</style>
