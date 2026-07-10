import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useTransitionStore = defineStore('transition', () => {
  const transitionType = ref(localStorage.getItem('pageTransitionType') || 'fade')
  const transitionDuration = ref(parseInt(localStorage.getItem('pageTransitionDuration')) || 400)
  const enableTransition = ref(localStorage.getItem('enablePageTransition') !== 'false')

  const transitionTypes = [
    {
      id: 'fade',
      name: '淡入淡出',
      description: '优雅的透明度过渡效果',
      icon: '🌫️'
    },
    {
      id: 'slide',
      name: '滑动切换',
      description: '流畅的左右滑动效果',
      icon: '➡️'
    },
    {
      id: 'zoom',
      name: '缩放过渡',
      description: '现代感的缩放动画',
      icon: '🔍'
    },
    {
      id: 'slideUp',
      name: '上滑切换',
      description: '从下往上的滑动效果',
      icon: '⬆️'
    },
    {
      id: 'flip',
      name: '翻转切换',
      description: '3D翻转效果',
      icon: '🔄'
    }
  ]

  watch(transitionType, (newType) => {
    localStorage.setItem('pageTransitionType', newType)
  })

  watch(transitionDuration, (newDuration) => {
    localStorage.setItem('pageTransitionDuration', newDuration.toString())
  })

  watch(enableTransition, (newEnabled) => {
    localStorage.setItem('enablePageTransition', newEnabled.toString())
  })

  function setTransitionType(type) {
    transitionType.value = type
  }

  function setTransitionDuration(duration) {
    transitionDuration.value = Math.max(200, Math.min(800, duration))
  }

  function toggleTransition() {
    enableTransition.value = !enableTransition.value
  }

  return {
    transitionType,
    transitionDuration,
    enableTransition,
    transitionTypes,
    setTransitionType,
    setTransitionDuration,
    toggleTransition
  }
})
