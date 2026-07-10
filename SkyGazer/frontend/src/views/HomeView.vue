<template>
  <div class="home-view">
    <section class="hero-section">
      <WeatherHero />
    </section>

    <section class="forecast-section">
      <HourlyForecast />
    </section>
    
    <LifestyleCards />
  </div>
</template>

<script setup>
import WeatherHero from '@/components/home/WeatherHero.vue'
import HourlyForecast from '@/components/home/HourlyForecast.vue'
import LifestyleCards from '@/components/home/LifestyleCards.vue'
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
</style>
