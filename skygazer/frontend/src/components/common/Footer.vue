<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-brand">
        <div class="brand-icon-small">
          <svg width="24" height="24" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="16" cy="16" r="15" fill="url(#sg-bg-footer)" stroke="url(#sg-stroke-footer)" stroke-width="1"/>
            <circle cx="16" cy="16" r="8.5" fill="none" stroke="url(#sg-moon-footer)" stroke-width="1.5" opacity="0.35"/>
            <circle cx="16" cy="16" r="10.5" fill="none" stroke="url(#sg-moon-footer)" stroke-width="1" opacity="0.2"/>
            <circle cx="16" cy="16" r="7" fill="url(#sg-moon-fill-footer)"/>
            <circle cx="7" cy="7" r="1" fill="#FFD54F" opacity="0.85"/>
            <circle cx="25" cy="9" r="0.9" fill="#FFD54F" opacity="0.7"/>
            <circle cx="23" cy="25" r="1.1" fill="#FFD54F" opacity="0.75"/>
            <circle cx="9" cy="23" r="0.7" fill="#FFD54F" opacity="0.6"/>
            <circle cx="12" cy="11" r="0.5" fill="#FFD54F" opacity="0.5"/>
            <circle cx="20" cy="12" r="0.6" fill="#FFD54F" opacity="0.55"/>
            <circle cx="11" cy="20" r="0.4" fill="#FFD54F" opacity="0.45"/>
            <circle cx="21" cy="21" r="0.8" fill="#FFD54F" opacity="0.65"/>
            <defs>
              <linearGradient id="sg-bg-footer" x1="0" y1="0" x2="32" y2="32">
                <stop offset="0%" stop-color="#16C7C4"/>
                <stop offset="50%" stop-color="#34E3E0"/>
                <stop offset="100%" stop-color="#5FD9D6"/>
              </linearGradient>
              <linearGradient id="sg-stroke-footer" x1="0" y1="0" x2="32" y2="32">
                <stop offset="0%" stop-color="#34E3E0"/>
                <stop offset="100%" stop-color="#16C7C4"/>
              </linearGradient>
              <radialGradient id="sg-moon-fill-footer" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stop-color="#FFF9C4"/>
                <stop offset="35%" stop-color="#FFEB3B"/>
                <stop offset="65%" stop-color="#FFC107"/>
                <stop offset="100%" stop-color="#FFB300"/>
              </radialGradient>
              <radialGradient id="sg-moon-footer" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stop-color="#FFD54F" stop-opacity="0.7"/>
                <stop offset="100%" stop-color="#FFD54F" stop-opacity="0"/>
              </radialGradient>
            </defs>
          </svg>
        </div>
        <span class="footer-brand-text">SkyGazer AI</span>
        <span class="status-pill">
          <span class="status-dot"></span>
          系统运行正常
        </span>
      </div>
      <p class="footer-desc">基于 2026 年最新量子气象模型与边缘 AI 识别技术，为您提供毫秒级的气象决策支持。</p>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2026 SkyGazer AI. 智观天象 · 保留所有权利。</p>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.footer {
  margin-top: 2rem;
  border-top: 1px solid var(--border-subtle);
  background: var(--space-800);
  padding: 2.5rem clamp(1rem, 3vw, 2rem) 1.5rem;
}

.footer-content {
  max-width: 1320px;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1.25rem;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.brand-icon-small {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, var(--signal), var(--signal-strong));
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-brand-text {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.3rem 0.7rem;
  border-radius: var(--radius-pill);
  background: rgba(52, 211, 153, 0.1);
  border: 1px solid rgba(52, 211, 153, 0.28);
  color: var(--ok);
  font-size: 0.75rem;
  font-weight: 600;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--ok);
  box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.6);
  animation: sg-pulse 2.4s ease-out infinite;
}

@keyframes sg-pulse {
  0% { box-shadow: 0 0 0 0 rgba(52, 211, 153, 0.55); }
  70% { box-shadow: 0 0 0 7px rgba(52, 211, 153, 0); }
  100% { box-shadow: 0 0 0 0 rgba(52, 211, 153, 0); }
}

.footer-desc {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.6;
  max-width: 420px;
}

.footer-bottom {
  max-width: 1320px;
  margin: 1.75rem auto 0;
  padding-top: 1.25rem;
  border-top: 1px solid var(--border-subtle);
  text-align: center;
}

.footer-bottom p {
  font-size: 0.75rem;
  color: var(--text-muted);
}

@media (max-width: 640px) {
  .footer-content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
