<template>
  <div class="breadcrumb">
    <router-link to="/">首页</router-link>
    <span class="breadcrumb-separator">/</span>
    <span class="breadcrumb-current">{{ current }}</span>
  </div>
</template>

<script setup>
defineProps({
  current: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  font-size: 0.875rem;
}

.breadcrumb a {
  color: var(--blue-500);
  text-decoration: none;
  transition: color 0.3s ease;
}

.breadcrumb a:hover {
  color: var(--blue-600);
}

.breadcrumb-separator {
  color: var(--text-muted);
}

.breadcrumb-current {
  color: var(--text-secondary);
  font-weight: 500;
}
</style>
