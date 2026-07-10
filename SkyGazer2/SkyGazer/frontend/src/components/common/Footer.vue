<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-brand">
        <div class="brand-icon-small">
          <svg width="24" height="24" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="16" cy="16" r="15" fill="url(#blue-bg-footer)" stroke="url(#blue-stroke-footer)" stroke-width="1"/>
            <circle cx="16" cy="16" r="8.5" fill="none" stroke="url(#moon-glow-footer)" stroke-width="1.5" opacity="0.35"/>
            <circle cx="16" cy="16" r="10.5" fill="none" stroke="url(#moon-glow-footer)" stroke-width="1" opacity="0.2"/>
            <circle cx="16" cy="16" r="7" fill="url(#moon-gradient-footer)"/>
            <circle cx="7" cy="7" r="1" fill="#FFD54F" opacity="0.85"/>
            <circle cx="25" cy="9" r="0.9" fill="#FFD54F" opacity="0.7"/>
            <circle cx="23" cy="25" r="1.1" fill="#FFD54F" opacity="0.75"/>
            <circle cx="9" cy="23" r="0.7" fill="#FFD54F" opacity="0.6"/>
            <circle cx="12" cy="11" r="0.5" fill="#FFD54F" opacity="0.5"/>
            <circle cx="20" cy="12" r="0.6" fill="#FFD54F" opacity="0.55"/>
            <circle cx="11" cy="20" r="0.4" fill="#FFD54F" opacity="0.45"/>
            <circle cx="21" cy="21" r="0.8" fill="#FFD54F" opacity="0.65"/>
            <defs>
              <linearGradient id="blue-bg-footer" x1="0" y1="0" x2="32" y2="32">
                <stop offset="0%" stop-color="#1E3A8A"/>
                <stop offset="50%" stop-color="#1E40AF"/>
                <stop offset="100%" stop-color="#1D4ED8"/>
              </linearGradient>
              <linearGradient id="blue-stroke-footer" x1="0" y1="0" x2="32" y2="32">
                <stop offset="0%" stop-color="#3B82F6"/>
                <stop offset="100%" stop-color="#60A5FA"/>
              </linearGradient>
              <radialGradient id="moon-gradient-footer" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stop-color="#FFF9C4"/>
                <stop offset="35%" stop-color="#FFEB3B"/>
                <stop offset="65%" stop-color="#FFC107"/>
                <stop offset="100%" stop-color="#FFB300"/>
              </radialGradient>
              <radialGradient id="moon-glow-footer" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stop-color="#FFD54F" stop-opacity="0.7"/>
                <stop offset="100%" stop-color="#FFD54F" stop-opacity="0"/>
              </radialGradient>
            </defs>
          </svg>
        </div>
        <span class="footer-brand-text">SkyGazer AI</span>
      </div>
      <p class="footer-desc">基于 2026 年最新量子气象模型与边缘 AI 识别技术，为您提供毫秒级的气象决策支持。</p>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2026 SkyGazer AI. All rights reserved.</p>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.footer {
  margin-top: 2rem;
  border-top: 1px solid rgba(147, 197, 253, 0.3);
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  padding: 3rem 2rem 1.5rem;
}

.footer-content {
  max-width: 1440px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 2rem;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.brand-icon-small {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-brand-text {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--blue-900);
}

.footer-desc {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.6;
  max-width: 400px;
}

.footer-bottom {
  max-width: 1440px;
  margin: 2rem auto 0;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(147, 197, 253, 0.2);
  text-align: center;
}

.footer-bottom p {
  font-size: 0.75rem;
  color: var(--text-muted);
}

@media (min-width: 768px) {
  .footer-content {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
