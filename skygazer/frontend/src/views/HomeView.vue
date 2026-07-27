<template>
  <div class="home-view">
    <section class="hero-section">
      <WeatherHero />
    </section>

    <section class="forecast-section">
      <HourlyForecast />
    </section>
    
    <LifestyleCards />

    <section class="assistant-section">
      <AIChat />
    </section>
  </div>
</template>

<script setup>
import WeatherHero from '@/components/home/WeatherHero.vue'
import HourlyForecast from '@/components/home/HourlyForecast.vue'
import LifestyleCards from '@/components/home/LifestyleCards.vue'
import AIChat from '@/components/ai/AIChat.vue'
</script>

<style scoped>
.home-view {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.hero-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.forecast-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.assistant-section {
  max-width: 720px;
  width: 100%;
  margin: 0 auto;
}
</style>
