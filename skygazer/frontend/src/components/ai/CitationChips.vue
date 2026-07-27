<template>
  <div class="citation-chips">
    <div class="chips-title">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
        <path d="M12 6.25278V19.2528M12 6.25278C10.8321 5.47686 9.24649 5 7.5 5C5.75351 5 4.16789 5.47686 3 6.25278V19.2528C4.16789 18.4769 5.75351 18 7.5 18C9.24649 18 10.8321 18.4769 12 19.2528M12 6.25278C13.1679 5.47686 14.7535 5 16.5 5C18.2465 5 19.8321 5.47686 21 6.25278V19.2528C19.8321 18.4769 18.2465 18 16.5 18C14.7535 18 13.1678 18.4769 12 19.2528" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      知识来源
    </div>
    <div class="chips-row">
      <button
        v-for="(ref, idx) in references"
        :key="idx"
        class="chip"
        :class="{ active: activeIndex === idx }"
        @mouseenter="open(idx, $event)"
        @mouseleave="scheduleClose"
        @focus="open(idx, $event)"
        @click="toggle(idx, $event)"
        @keydown.esc="close"
      >
        <span class="chip-index">{{ idx + 1 }}</span>
        <span class="chip-title">{{ ref.title }}</span>
      </button>
    </div>

    <Teleport to="body">
      <div
        v-if="activeIndex !== null && activeRef"
        class="ref-popover"
        :style="popoverStyle"
        @mouseenter="cancelClose"
        @mouseleave="scheduleClose"
        role="dialog"
        aria-label="引用详情"
      >
        <div class="pop-header">
          <span class="pop-index">{{ activeIndex + 1 }}</span>
          <span class="pop-title">{{ activeRef.title }}</span>
        </div>
        <div class="pop-source" v-if="activeRef.source">{{ activeRef.source }}</div>
        <div class="pop-snippet" v-if="activeRef.snippet">{{ activeRef.snippet }}</div>
        <a
          v-if="activeRef.url"
          class="pop-link"
          :href="activeRef.url"
          target="_blank"
          rel="noopener noreferrer"
        >查看原文 ↗</a>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  references: { type: Array, default: () => [] }
})

const activeIndex = ref(null)
const popoverPos = ref({ top: 0, left: 0 })
let closeTimer = null

const activeRef = computed(() => (activeIndex.value !== null ? props.references[activeIndex.value] : null))

const popoverStyle = computed(() => ({
  top: popoverPos.value.top + 'px',
  left: popoverPos.value.left + 'px'
}))

function positionFromEvent(e) {
  const rect = e.currentTarget.getBoundingClientRect()
  const popW = 280
  let left = rect.left + rect.width / 2 - popW / 2
  left = Math.max(12, Math.min(left, window.innerWidth - popW - 12))
  const top = rect.top - 12 // 浮层在 chip 上方，translateY(-100%) 实现
  popoverPos.value = { top, left }
}

function open(idx, e) {
  cancelClose()
  positionFromEvent(e)
  activeIndex.value = idx
}

function toggle(idx, e) {
  if (activeIndex.value === idx) {
    close()
  } else {
    open(idx, e)
  }
}

function scheduleClose() {
  cancelClose()
  closeTimer = setTimeout(() => { activeIndex.value = null }, 160)
}

function cancelClose() {
  if (closeTimer) {
    clearTimeout(closeTimer)
    closeTimer = null
  }
}

function close() {
  activeIndex.value = null
}
</script>

<style scoped>
.citation-chips {
  margin-top: 0.625rem;
}

.chips-title {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: var(--blue-600);
  margin-bottom: 0.375rem;
  font-weight: 600;
}

.chips-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  max-width: 220px;
  padding: 0.25rem 0.5rem 0.25rem 0.375rem;
  background: var(--ai-chip-bg, rgba(59, 130, 246, 0.1));
  border: 1px solid var(--ai-chip-border, rgba(59, 130, 246, 0.18));
  border-radius: 999px;
  cursor: pointer;
  color: var(--blue-700, #1d4ed8);
  font-size: 0.75rem;
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.15s ease;
}

.chip:hover,
.chip.active {
  background: rgba(59, 130, 246, 0.16);
  border-color: rgba(59, 130, 246, 0.32);
}

.chip:active {
  transform: scale(0.98);
}

.chip-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--blue-500);
  color: #fff;
  font-size: 0.625rem;
  font-weight: 700;
  flex-shrink: 0;
}

.chip-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.ref-popover {
  position: fixed;
  transform: translateY(-100%);
  width: 280px;
  padding: 0.75rem 0.875rem;
  background: var(--card-bg, #fff);
  border: 1px solid var(--ai-chip-border, rgba(59, 130, 246, 0.18));
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.16);
  z-index: 1000;
  animation: popIn 0.18s ease-out;
}

@keyframes popIn {
  from { opacity: 0; transform: translateY(-92%); }
  to { opacity: 1; transform: translateY(-100%); }
}

.pop-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.375rem;
}

.pop-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--blue-500);
  color: #fff;
  font-size: 0.6875rem;
  font-weight: 700;
  flex-shrink: 0;
}

.pop-title {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--text-primary);
}

.pop-source {
  font-size: 0.6875rem;
  color: var(--blue-600);
  margin-bottom: 0.375rem;
}

.pop-snippet {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.6;
  max-height: 140px;
  overflow-y: auto;
}

.pop-link {
  display: inline-block;
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: var(--blue-600);
  text-decoration: none;
  font-weight: 600;
}

.pop-link:hover {
  text-decoration: underline;
}

@media (prefers-reduced-motion: reduce) {
  .ref-popover { animation: none; }
}
</style>
