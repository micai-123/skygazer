<template>
  <div class="theme-settings glass-card">
    <h2 class="settings-title">主题定制</h2>

    <div class="settings-section">
      <h3 class="section-title">主题色选择</h3>
      <div class="color-grid">
        <button
          v-for="color in themeStore.presetColors"
          :key="color.value"
          class="color-option"
          :class="{ active: themeStore.primaryColor === color.value }"
          :style="{ backgroundColor: color.value }"
          @click="themeStore.setPrimaryColor(color.value)"
          :aria-label="color.name"
        >
          <span class="color-label">{{ color.name }}</span>
        </button>
      </div>
    </div>

    <div class="settings-section">
      <h3 class="section-title">显示模式</h3>
      <div class="mode-options">
        <button
          class="mode-option"
          :class="{ active: themeStore.themeMode === 'light' }"
          @click="themeStore.setThemeMode('light')"
          aria-label="浅色模式"
        >
          浅色
        </button>
        <button
          class="mode-option"
          :class="{ active: themeStore.themeMode === 'dark' }"
          @click="themeStore.setThemeMode('dark')"
          aria-label="深色模式"
        >
          深色
        </button>
        <button
          class="mode-option"
          :class="{ active: themeStore.themeMode === 'auto' }"
          @click="themeStore.setThemeMode('auto')"
          aria-label="自动模式"
        >
          自动
        </button>
      </div>
    </div>

    <div class="settings-section">
      <button
        class="reset-button"
        @click="themeStore.resetTheme()"
        aria-label="重置为默认主题"
      >
        重置为默认
      </button>
    </div>
  </div>
</template>

<script setup>
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
</script>

<style scoped>
.theme-settings {
  padding: 2rem;
  max-width: 600px;
  margin: 0 auto;
}

.settings-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
  text-align: center;
}

.settings-section {
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 1rem;
}

.color-option {
  aspect-ratio: 1;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
}

.color-option:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.color-option.active {
  border-color: var(--text-primary);
  transform: scale(1.05);
}

.color-label {
  position: absolute;
  bottom: -1.5rem;
  left: 50%;
  transform: translateX(-50%);
  font-size: 0.75rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.mode-options {
  display: flex;
  gap: 0.75rem;
}

.mode-option {
  flex: 1;
  padding: 0.75rem 1.5rem;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mode-option:hover {
  background: rgba(var(--user-primary), 0.1);
  border-color: var(--user-primary);
}

.mode-option.active {
  background: var(--user-primary);
  color: white;
  border-color: var(--user-primary);
}

.reset-button {
  width: 100%;
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: 1px solid var(--text-muted);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.reset-button:hover {
  background: rgba(0, 0, 0, 0.05);
}

@media (prefers-reduced-motion: reduce) {
  .color-option:hover,
  .mode-option:hover {
    transform: none;
  }
}
</style>