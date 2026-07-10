export class AnimationOptimizer {
  constructor() {
    this.isLowEndDevice = this.detectLowEndDevice()
    this.preferredReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    this.performanceMetrics = {
      frameCount: 0,
      lastTime: performance.now(),
      fps: 60
    }
    
    if (!this.preferredReducedMotion) {
      this.startPerformanceMonitoring()
    }
  }

  detectLowEndDevice() {
    const canvas = document.createElement('canvas')
    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl')
    
    if (!gl) {
      return true
    }

    const debugInfo = gl.getExtension('WEBGL_debug_renderer_info')
    if (!debugInfo) {
      return false
    }

    const renderer = gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL)
    
    const lowEndIndicators = [
      'Intel(R) HD Graphics',
      'Intel(R) UHD Graphics',
      'Mali-4',
      'Adreno 3',
      'PowerVR'
    ]

    return lowEndIndicators.some(indicator => 
      renderer.toLowerCase().includes(indicator.toLowerCase())
    )
  }

  startPerformanceMonitoring() {
    let lastTime = performance.now()
    let frameCount = 0

    const measureFPS = () => {
      frameCount++
      const currentTime = performance.now()
      
      if (currentTime - lastTime >= 1000) {
        this.performanceMetrics.fps = frameCount
        frameCount = 0
        lastTime = currentTime
        
        if (this.performanceMetrics.fps < 30) {
          this.handleLowPerformance()
        }
      }
      
      requestAnimationFrame(measureFPS)
    }

    requestAnimationFrame(measureFPS)
  }

  handleLowPerformance() {
    console.warn('Low performance detected, optimizing animations')
    document.body.classList.add('reduce-animations')
  }

  getOptimalDuration(baseDuration) {
    if (this.preferredReducedMotion) {
      return 0
    }

    if (this.isLowEndDevice || this.performanceMetrics.fps < 30) {
      return Math.max(200, baseDuration * 0.5)
    }

    return baseDuration
  }

  shouldEnableAnimation() {
    return !this.preferredReducedMotion && this.performanceMetrics.fps >= 30
  }

  optimizeElement(element) {
    element.style.willChange = 'transform, opacity'
    element.style.transform = 'translateZ(0)'
    element.style.backfaceVisibility = 'hidden'
  }

  cleanupElement(element) {
    element.style.willChange = 'auto'
  }
}

export const animationOptimizer = new AnimationOptimizer()
