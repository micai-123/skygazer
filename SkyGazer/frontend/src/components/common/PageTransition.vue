<template>
  <div class="page-transition-wrapper">
    <transition
      :name="transitionName"
      :css="enableTransition"
      :duration="transitionDuration"
      mode="out-in"
      @before-enter="onBeforeEnter"
      @enter="onEnter"
      @after-enter="onAfterEnter"
      @before-leave="onBeforeLeave"
      @leave="onLeave"
      @after-leave="onAfterLeave"
    >
      <div
        :key="$route.fullPath"
        class="page-transition-container"
        :style="containerStyle"
      >
        <slot />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useTransitionStore } from '@/stores/transition'
import { useRoute } from 'vue-router'

const transitionStore = useTransitionStore()
const route = useRoute()

const transitionName = computed(() => {
  if (!transitionStore.enableTransition) return ''
  return `page-${transitionStore.transitionType}`
})

const transitionDuration = computed(() => {
  return transitionStore.transitionDuration
})

const enableTransition = computed(() => {
  return transitionStore.enableTransition
})

const containerStyle = computed(() => ({
  '--transition-duration': `${transitionDuration.value}ms`
}))

function onBeforeEnter(el) {
  el.style.willChange = 'transform, opacity'
  requestAnimationFrame(() => {
    el.style.transition = `all ${transitionDuration.value}ms cubic-bezier(0.4, 0, 0.2, 1)`
  })
}

function onEnter(el, done) {
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      el.style.willChange = 'auto'
      done()
    })
  })
}

function onAfterEnter(el) {
  el.style.willChange = 'auto'
}

function onBeforeLeave(el) {
  el.style.willChange = 'transform, opacity'
}

function onLeave(el, done) {
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      el.style.willChange = 'auto'
      done()
    })
  })
}

function onAfterLeave(el) {
  el.style.willChange = 'auto'
}
</script>

<style scoped>
.page-transition-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.page-transition-container {
  width: 100%;
  height: 100%;
  position: relative;
  backface-visibility: hidden;
  perspective: 1000px;
  transform-style: preserve-3d;
}

/* Fade Transition */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity var(--transition-duration, 400ms) cubic-bezier(0.4, 0, 0.2, 1);
}

.page-fade-enter-from {
  opacity: 0;
}

.page-fade-leave-to {
  opacity: 0;
}

/* Slide Transition */
.page-slide-enter-active,
.page-slide-leave-active {
  transition: transform var(--transition-duration, 400ms) cubic-bezier(0.4, 0, 0.2, 1),
              opacity var(--transition-duration, 400ms) cubic-bezier(0.4, 0, 0.2, 1);
}

.page-slide-enter-from {
  transform: translateX(30px);
  opacity: 0;
}

.page-slide-leave-to {
  transform: translateX(-30px);
  opacity: 0;
}

/* Slide Up Transition */
.page-slideUp-enter-active,
.page-slideUp-leave-active {
  transition: transform var(--transition-duration, 400ms) cubic-bezier(0.4, 0, 0.2, 1),
              opacity var(--transition-duration, 400ms) cubic-bezier(0.4, 0, 0.2, 1);
}

.page-slideUp-enter-from {
  transform: translateY(30px);
  opacity: 0;
}

.page-slideUp-leave-to {
  transform: translateY(-30px);
  opacity: 0;
}

/* Zoom Transition */
.page-zoom-enter-active,
.page-zoom-leave-active {
  transition: transform var(--transition-duration, 400ms) cubic-bezier(0.4, 0, 0.2, 1),
              opacity var(--transition-duration, 400ms) cubic-bezier(0.4, 0, 0.2, 1);
}

.page-zoom-enter-from {
  transform: scale(0.95);
  opacity: 0;
}

.page-zoom-leave-to {
  transform: scale(1.05);
  opacity: 0;
}

/* Flip Transition */
.page-flip-enter-active,
.page-flip-leave-active {
  transition: transform var(--transition-duration, 400ms) cubic-bezier(0.4, 0, 0.2, 1),
              opacity var(--transition-duration, 400ms) cubic-bezier(0.4, 0, 0.2, 1);
}

.page-flip-enter-from {
  transform: rotateY(90deg);
  opacity: 0;
}

.page-flip-leave-to {
  transform: rotateY(-90deg);
  opacity: 0;
}

/* Performance Optimizations */
@media (prefers-reduced-motion: reduce) {
  .page-fade-enter-active,
  .page-fade-leave-active,
  .page-slide-enter-active,
  .page-slide-leave-active,
  .page-slideUp-enter-active,
  .page-slideUp-leave-active,
  .page-zoom-enter-active,
  .page-zoom-leave-active,
  .page-flip-enter-active,
  .page-flip-leave-active {
    transition: none !important;
  }
  
  .page-fade-enter-from,
  .page-fade-leave-to,
  .page-slide-enter-from,
  .page-slide-leave-to,
  .page-slideUp-enter-from,
  .page-slideUp-leave-to,
  .page-zoom-enter-from,
  .page-zoom-leave-to,
  .page-flip-enter-from,
  .page-flip-leave-to {
    transform: none !important;
    opacity: 1 !important;
  }
}
</style>
