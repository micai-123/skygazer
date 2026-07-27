<template>
  <div class="weather-image-card" :style="{ '--accent': meta.color }">
    <div class="wic-header">
      <div class="wic-icon" :style="{ background: meta.gradient }">
        <span v-html="meta.icon"></span>
      </div>
      <div class="wic-title">
        <div class="wic-label">{{ result.labelCn }}</div>
        <div class="wic-sub">{{ result.labelEn }} · 天气识别</div>
      </div>
      <div class="wic-confidence">
        <div class="wic-conf-num">{{ (result.confidence * 100).toFixed(1) }}%</div>
        <div class="wic-conf-label">置信度</div>
      </div>
    </div>

    <div class="wic-probs">
      <div
        class="wic-prob-row"
        v-for="key in order"
        :key="key"
        :class="{ active: key === result.labelEn }"
      >
        <span class="wic-prob-name">{{ LABEL_CN[key] || key }}</span>
        <div class="wic-bar-track">
          <div
            class="wic-bar-fill"
            :style="{ width: (pct(key)) + '%', background: barColor(key) }"
          ></div>
        </div>
        <span class="wic-prob-val">{{ (pct(key)).toFixed(1) }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  result: { type: Object, required: true }
})

const order = ['sunny', 'cloudy', 'rainy', 'snowy']

const LABEL_CN = {
  sunny: '晴天',
  cloudy: '多云',
  rainy: '雨天',
  snowy: '雪天'
}

const META = {
  sunny: {
    color: '#f59e0b',
    gradient: 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)',
    icon: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="4.5" fill="#fff"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M19.1 4.9l-1.4 1.4M6.3 17.7l-1.4 1.4" stroke="#fff" stroke-width="2" stroke-linecap="round"/></svg>'
  },
  cloudy: {
    color: '#64748b',
    gradient: 'linear-gradient(135deg, #94a3b8 0%, #64748b 100%)',
    icon: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M7 18h9a3.5 3.5 0 000-7 4.5 4.5 0 00-8.6-1.5A3.5 3.5 0 007 18z" fill="#fff"/></svg>'
  },
  rainy: {
    color: '#3b82f6',
    gradient: 'linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%)',
    icon: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M7 15h9a3.5 3.5 0 000-7 4.5 4.5 0 00-8.6-1.5A3.5 3.5 0 007 15z" fill="#fff"/><path d="M9 18l-1 2M13 18l-1 2M17 18l-1 2" stroke="#fff" stroke-width="2" stroke-linecap="round"/></svg>'
  },
  snowy: {
    color: '#06b6d4',
    gradient: 'linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%)',
    icon: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M7 15h9a3.5 3.5 0 000-7 4.5 4.5 0 00-8.6-1.5A3.5 3.5 0 007 15z" fill="#fff"/><path d="M12 17v4M9.5 19l2.5-2 2.5 2" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  }
}

const meta = computed(() => META[props.result.labelEn] || META.cloudy)

function pct(key) {
  const v = props.result.probabilities?.[key]
  return typeof v === 'number' ? v * 100 : 0
}

function barColor(key) {
  return key === props.result.labelEn
    ? (META[key]?.color || '#3b82f6')
    : 'rgba(148, 163, 184, 0.55)'
}
</script>

<style scoped>
.weather-image-card {
  margin-top: 0.625rem;
  padding: 0.875rem 1rem;
  border-radius: 14px;
  background: var(--card-bg, #fff);
  border: 1px solid var(--border-color, rgba(59, 130, 246, 0.14));
  box-shadow: var(--shadow-sm);
}

.wic-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.wic-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
}

.wic-title { flex: 1; min-width: 0; }

.wic-label {
  font-size: 1.0625rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.wic-sub {
  font-size: 0.6875rem;
  color: var(--text-muted);
  margin-top: 0.125rem;
}

.wic-confidence {
  text-align: right;
  flex-shrink: 0;
}

.wic-conf-num {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--accent);
  line-height: 1.1;
}

.wic-conf-label {
  font-size: 0.625rem;
  color: var(--text-muted);
}

.wic-probs {
  margin-top: 0.875rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.wic-prob-row {
  display: grid;
  grid-template-columns: 48px 1fr 48px;
  align-items: center;
  gap: 0.5rem;
}

.wic-prob-name {
  font-size: 0.75rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.wic-prob-row.active .wic-prob-name {
  color: var(--text-primary);
  font-weight: 600;
}

.wic-bar-track {
  height: 8px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.18);
  overflow: hidden;
}

.wic-bar-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}

.wic-prob-val {
  font-size: 0.6875rem;
  color: var(--text-muted);
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.wic-prob-row.active .wic-prob-val {
  color: var(--accent);
  font-weight: 600;
}

@media (prefers-reduced-motion: reduce) {
  .wic-bar-fill { transition: none; }
}
</style>
