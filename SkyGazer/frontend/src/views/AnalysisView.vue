﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="analysis-view">
    <Breadcrumb current="智能分析" />
    
    <section class="analysis-section">
      <div class="section-header">
        <div class="section-header-left">
          <h2 class="section-main-title">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
            </svg>
            智能气象分析
          </h2>
          <p class="section-desc">AI驱动的气象趋势预测与异常预警系统</p>
        </div>
        <div class="section-header-right">
          <div class="export-dropdown">
            <button class="export-btn" @click="toggleExportMenu">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              导出视图
              <svg class="dropdown-arrow" :class="{ rotated: showExportMenu }" width="12" height="12" viewBox="0 0 24 24" fill="none">
                <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
            <transition name="dropdown">
              <div v-if="showExportMenu" class="export-menu">
                <div class="menu-item" @click="openExportViewModal">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  1. 导出视图
                </div>
                <div class="menu-item" @click="exportAllToPdf">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  2. 导出为PDF
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
      
      <div class="analysis-grid">
        <div class="analysis-card glass-card" ref="tempCardRef">
          <div class="analysis-header">
            <h3 class="analysis-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M3 3V21H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M7 16L11 12L15 14L21 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              气温趋势分析
            </h3>
            <div class="time-selector">
              <button 
                v-for="range in timeRanges" 
                :key="range.id"
                class="time-btn"
                :class="{ active: activeTimeRange === range.id }"
                @click="changeTimeRange(range.id)"
              >
                {{ range.label }}
              </button>
            </div>
          </div>
          <div class="analysis-chart" ref="tempChartRef">
            <div v-show="tempLoading" class="chart-loading">
              <div class="loading-spinner"></div>
            </div>
          </div>
          <div class="analysis-summary">
            <div class="summary-item">
              <span class="summary-label">平均温度</span>
              <span class="summary-value">{{ tempSummary.avgTemp }}°C</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">最高温度</span>
              <span class="summary-value high">{{ tempSummary.maxTemp }}°C</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">最低温度</span>
              <span class="summary-value low">{{ tempSummary.minTemp }}°C</span>
            </div>
          </div>
          <div v-if="tempSummary.description" class="analysis-desc">
            {{ tempSummary.description }}
          </div>
        </div>
        
        <div class="analysis-card glass-card" ref="precipCardRef">
          <div class="analysis-header">
            <h3 class="analysis-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M12 2.69L17.66 8.35C19.55 10.24 20.5 12.67 20.5 15.15C20.5 18.5 17.85 21.15 14.5 21.15C12.87 21.15 11.37 20.5 10.25 19.4L12 17.65L13.75 19.4C12.63 20.5 11.13 21.15 9.5 21.15C6.15 21.15 3.5 18.5 3.5 15.15C3.5 12.67 4.45 10.24 6.34 8.35L12 2.69Z" fill="currentColor"/>
              </svg>
              降水概率预测
            </h3>
            <div class="time-selector">
              <button 
                v-for="range in timeRanges" 
                :key="range.id"
                class="time-btn"
                :class="{ active: activePrecipRange === range.id }"
                @click="changePrecipRange(range.id)"
              >
                {{ range.label }}
              </button>
            </div>
          </div>
          <div class="analysis-chart" ref="precipChartRef">
            <div v-show="precipLoading" class="chart-loading">
              <div class="loading-spinner"></div>
            </div>
          </div>
          <div class="analysis-summary">
            <div class="summary-item">
              <span class="summary-label">降水概率</span>
              <span class="summary-value">{{ precipSummary.avgProb }}%</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">预计降水量</span>
              <span class="summary-value">{{ precipSummary.totalPrecip }}mm</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">降水时段</span>
              <span class="summary-value">{{ precipSummary.period }}</span>
            </div>
          </div>
          <div v-if="precipSummary.description" class="analysis-desc">
            {{ precipSummary.description }}
          </div>
        </div>
        
        <div class="analysis-card glass-card alert-card">
          <div class="analysis-header">
            <h3 class="analysis-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L2 22H22L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 9V13M12 17H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              气象预警中心
            </h3>
            <div class="alert-header-right">
              <span class="alert-count-badge" :class="alertLevelClass">
                {{ alertStats.total }}条预警
              </span>
              <button class="refresh-btn" @click="refreshAlerts" :disabled="alertLoading">
                <svg :class="{ 'spinning': alertLoading }" width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <path d="M21 12a9 9 0 11-6.219-8.56" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
          </div>
          
          <div class="alert-summary-bar">
            <div class="alert-stat" v-for="stat in alertStatsByLevel" :key="stat.level">
              <span class="stat-dot" :class="stat.level"></span>
              <span class="stat-label">{{ stat.label }}</span>
              <span class="stat-count">{{ stat.count }}</span>
            </div>
          </div>
          
          <div class="alert-filter">
            <button 
              v-for="filter in alertFilters" 
              :key="filter.id"
              class="filter-btn"
              :class="{ active: activeAlertFilter === filter.id }"
              @click="activeAlertFilter = filter.id"
            >
              {{ filter.label }}
            </button>
          </div>
          
          <div class="alert-list" v-if="filteredAlerts.length > 0">
            <div 
              v-for="(alert, index) in filteredAlerts" 
              :key="index"
              class="alert-item"
              :class="[alert.level]"
            >
              <div class="alert-main">
                <div class="alert-icon-wrapper" :class="alert.level">
                  <svg v-if="alert.type === 'heat'" class="alert-icon-svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="5" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  <svg v-else-if="alert.type === 'cold'" class="alert-icon-svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M12 2v20M2 12h20M12 6l-2-2M12 6l2-2M12 18l-2 2M12 18l2 2M6 12l-2-2M6 12l-2 2M18 12l2-2M18 12l2 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  <svg v-else-if="alert.type === 'rain'" class="alert-icon-svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M12 2.69L17.66 8.35C19.55 10.24 20.5 12.67 20.5 15.15C20.5 18.5 17.85 21.15 14.5 21.15C12.87 21.15 11.37 20.5 10.25 19.4L12 17.65L13.75 19.4C12.63 20.5 11.13 21.15 9.5 21.15C6.15 21.15 3.5 18.5 3.5 15.15C3.5 12.67 4.45 10.24 6.34 8.35L12 2.69Z" fill="currentColor"/>
                  </svg>
                  <svg v-else-if="alert.type === 'precipitation'" class="alert-icon-svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M8 19v2M8 13v2M16 19v2M16 13v2M12 21v2M12 15v2M20 16.58A5 5 0 0018 7h-1.26A8 8 0 104 15.25" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <svg v-else class="alert-icon-svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M12 9V2M12 9a4 4 0 100 8 4 4 0 000-8zM12 2a2 2 0 012 2v5.535a4 4 0 11-4 0V4a2 2 0 012-2z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="alert-content">
                  <div class="alert-title-row">
                    <span class="alert-level-tag" :class="alert.level">{{ getLevelText(alert.level) }}</span>
                    <h4 class="alert-title">{{ alert.title }}</h4>
                  </div>
                  <p class="alert-desc">{{ alert.desc }}</p>
                  <div class="alert-meta">
                    <span class="alert-time">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      </svg>
                      {{ alert.time }}
                    </span>
                    <span class="alert-source">{{ alert.source || '气象台' }}</span>
                  </div>
                </div>
                <button class="alert-view-btn" @click="openAlertDetail(alert)">
                  查看详情
                </button>
              </div>
            </div>
          </div>
          
          <div class="alert-empty" v-else>
            <div class="empty-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <p class="empty-text">暂无{{ activeAlertFilter !== 'all' ? getFilterLabel(activeAlertFilter) : '' }}预警</p>
            <p class="empty-subtext">当前天气状况良好，适合日常活动</p>
          </div>
        </div>
        
      </div>
      
      <div v-if="error" class="error-toast">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>{{ error }}</span>
        <button class="toast-retry" @click="fetchAnalysisData">重试</button>
      </div>
    </section>
    
    <transition name="fade">
      <div v-if="showQRCode" class="qrcode-modal-overlay" @click.self="showQRCode = false">
        <div class="qrcode-modal">
          <div class="qrcode-header">
            <h3>微信扫码分享</h3>
            <button class="close-btn" @click="showQRCode = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <div class="qrcode-body">
            <div class="qrcode-placeholder">
              <svg width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
                <rect x="3" y="3" width="7" height="7"/>
                <rect x="14" y="3" width="7" height="7"/>
                <rect x="3" y="14" width="7" height="7"/>
                <rect x="14" y="14" width="7" height="7"/>
                <rect x="5" y="5" width="3" height="3" fill="currentColor"/>
                <rect x="16" y="5" width="3" height="3" fill="currentColor"/>
                <rect x="5" y="16" width="3" height="3" fill="currentColor"/>
              </svg>
            </div>
            <p class="qrcode-tip">使用微信扫描二维码查看并分享</p>
          </div>
        </div>
      </div>
    </transition>
    
    <transition name="fade">
      <div v-if="showQQConfig" class="qrcode-modal-overlay" @click.self="showQQConfig = false">
        <div class="qq-config-modal">
          <div class="qq-config-header">
            <h3>QQ分享配置</h3>
            <button class="close-btn" @click="showQQConfig = false">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <div class="qq-config-body">
            <div class="config-warning">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                <path d="M12 9V13M12 17H12.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke="#f59e0b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <h4>需要配置QQ互联AppID</h4>
            <p class="config-desc">使用QQ分享功能需要先申请QQ互联平台的AppID</p>
            <div class="config-steps">
              <div class="step">
                <span class="step-num">1</span>
                <span class="step-text">访问 <a href="https://connect.qq.com" target="_blank">QQ互联平台</a></span>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <span class="step-text">创建应用并获取AppID</span>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <span class="step-text">配置回调域名</span>
              </div>
              <div class="step">
                <span class="step-num">4</span>
                <span class="step-text">在 .env 文件中设置 VITE_QQ_APP_ID</span>
              </div>
            </div>
            <div class="config-example">
              <code>.env 文件示例:</code>
              <pre>VITE_QQ_APP_ID=你的AppID
VITE_QQ_REDIRECT_URI=http://localhost/auth/callback</pre>
            </div>
          </div>
          <div class="qq-config-footer">
            <a href="https://connect.qq.com" target="_blank" class="config-link">
              前往QQ互联平台
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6M15 3h6v6M10 14L21 3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </a>
          </div>
        </div>
      </div>
    </transition>
    
    <transition name="fade">
      <div v-if="showAlertDetail" class="alert-modal-overlay" @click.self="closeAlertDetail">
        <div class="alert-modal">
          <div class="alert-modal-header" :class="selectedAlert?.level">
            <div class="header-content">
              <div class="alert-modal-icon">
                <svg v-if="selectedAlert?.type === 'heat'" width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="5" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <svg v-else-if="selectedAlert?.type === 'cold'" width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2v20M2 12h20M12 6l-2-2M12 6l2-2M12 18l-2 2M12 18l2 2M6 12l-2-2M6 12l-2 2M18 12l2-2M18 12l2 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <svg v-else-if="selectedAlert?.type === 'rain'" width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2.69L17.66 8.35C19.55 10.24 20.5 12.67 20.5 15.15C20.5 18.5 17.85 21.15 14.5 21.15C12.87 21.15 11.37 20.5 10.25 19.4L12 17.65L13.75 19.4C12.63 20.5 11.13 21.15 9.5 21.15C6.15 21.15 3.5 18.5 3.5 15.15C3.5 12.67 4.45 10.24 6.34 8.35L12 2.69Z" fill="currentColor"/>
                </svg>
                <svg v-else width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M12 9V2M12 9a4 4 0 100 8 4 4 0 000-8zM12 2a2 2 0 012 2v5.535a4 4 0 11-4 0V4a2 2 0 012-2z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
              </div>
              <div class="header-text">
                <span class="alert-modal-level-tag" :class="selectedAlert?.level">{{ getLevelText(selectedAlert?.level) }}</span>
                <h3 class="alert-modal-title">{{ selectedAlert?.title }}</h3>
              </div>
            </div>
            <button class="alert-modal-close" @click="closeAlertDetail">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          
          <div class="alert-modal-body">
            <div class="alert-modal-meta">
              <div class="meta-item">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <span>{{ selectedAlert?.time }}</span>
              </div>
              <div class="meta-item">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/>
                </svg>
                <span>{{ selectedAlert?.source || '气象台' }}</span>
              </div>
            </div>
            
            <div class="alert-modal-section">
              <h4 class="section-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
                </svg>
                预警详情
              </h4>
              <p class="section-content">{{ selectedAlert?.detail || selectedAlert?.desc }}</p>
            </div>
            
            <div class="alert-modal-section" v-if="selectedAlert?.suggestions && selectedAlert.suggestions.length">
              <h4 class="section-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M9 12L11 14L15 10M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                防护建议
              </h4>
              <ul class="suggestion-list">
                <li v-for="(suggestion, i) in selectedAlert.suggestions" :key="i">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  {{ suggestion }}
                </li>
              </ul>
            </div>
            
            <div class="alert-modal-section" v-if="selectedAlert?.affectedAreas">
              <h4 class="section-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                影响区域
              </h4>
              <p class="section-content">{{ selectedAlert.affectedAreas }}</p>
            </div>
          </div>
          
          <div class="alert-modal-footer">
            <button class="modal-btn secondary" @click="closeAlertDetail">关闭</button>
            <button class="modal-btn primary" @click="handleAlertAction(selectedAlert, 'export')">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              导出视图
            </button>
          </div>
        </div>
      </div>
    </transition>
    
    <transition name="fade">
      <div v-if="showExportViewDialog" class="export-view-modal-overlay" @click.self="closeExportViewModal">
        <div class="export-view-modal">
          <div class="export-view-modal-header">
            <h3 class="export-view-modal-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              选择导出视图类型
            </h3>
            <button class="close-btn" @click="closeExportViewModal">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <div class="export-view-modal-body">
            <div class="export-option" @click="exportChart('temperature')">
              <div class="option-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M3 3V21H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M7 16L11 12L15 14L21 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <div class="option-content">
                <h4>导出气温趋势图</h4>
                <p>导出完整的气温趋势分析图表（包含标题、时间选择、图表和统计数据）</p>
              </div>
            </div>
            <div class="export-option" @click="exportChart('precipitation')">
              <div class="option-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2.69l5.66 5.66a8 8 0 11-11.31 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <div class="option-content">
                <h4>导出降水概率预测图</h4>
                <p>导出完整的降水概率预测图表（包含标题、时间选择、图表和统计数据）</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import Breadcrumb from '@/components/common/Breadcrumb.vue'
import { weatherApi } from '@/api'
import { useWeatherStore } from '@/stores/weather'

const weatherStore = useWeatherStore()
const tempChartRef = ref(null)
const precipChartRef = ref(null)
const tempCardRef = ref(null)
const precipCardRef = ref(null)
const activeTimeRange = ref('24h')
const activePrecipRange = ref('24h')

const tempLoading = ref(false)
const precipLoading = ref(false)
const error = ref(null)
const tempData = ref(null)
const precipData = ref(null)
const expandedAlert = ref(null)
const activeAlertFilter = ref('all')
const alertLoading = ref(false)

const showQRCode = ref(false)
const showQQConfig = ref(false)
const showAlertDetail = ref(false)
const selectedAlert = ref(null)
const showExportMenu = ref(false)
const showExportViewDialog = ref(false)
const exportLoading = ref(false)

const timeRanges = [
  { id: '24h', label: '24小时' },
  { id: '7d', label: '7天' },
  { id: '30d', label: '30天' }
]

const alertFilters = [
  { id: 'all', label: '全部' },
  { id: 'red', label: '红色' },
  { id: 'orange', label: '橙色' },
  { id: 'yellow', label: '黄色' },
  { id: 'blue', label: '蓝色' }
]

const alerts = computed(() => {
  const alertList = []
  const location = weatherStore.currentCity?.name || '上海'
  
  if (tempData.value?.temperatureAnalysis) {
    const tempAnalysis = tempData.value.temperatureAnalysis
    const maxTemp = tempAnalysis.maxTemp
    const minTemp = tempAnalysis.minTemp
    
    if (maxTemp >= 40) {
      alertList.push({
        type: 'heat',
        level: 'red',
        title: '高温红色预警',
        desc: `预计最高温度将达到${maxTemp}°C，极易发生中暑`,
        detail: `${location}地区预计最高气温将达到${maxTemp}°C，高温天气持续，极易引发中暑或热射病，请做好防暑降温工作。`,
        suggestions: [
          '尽量避免在10:00-16:00时段进行户外活动',
          '外出时请做好防晒措施，佩戴遮阳帽、太阳镜',
          '及时补充水分，避免脱水',
          '关注老人、儿童等敏感人群的健康状况',
          '车内请勿放置易燃易爆物品'
        ],
        affectedAreas: `${location}及周边地区`,
        time: new Date().toLocaleString('zh-CN'),
        source: '中央气象台'
      })
    } else if (maxTemp >= 37) {
      alertList.push({
        type: 'heat',
        level: 'orange',
        title: '高温橙色预警',
        desc: `预计最高温度将达到${maxTemp}°C，请注意防暑`,
        detail: `${location}地区预计最高气温将达到${maxTemp}°C，天气炎热，请注意防暑降温。`,
        suggestions: [
          '减少户外活动，避免长时间暴晒',
          '多喝水，适量补充盐分',
          '穿着轻薄透气的衣物',
          '注意室内通风降温'
        ],
        affectedAreas: `${location}地区`,
        time: new Date().toLocaleString('zh-CN'),
        source: '中央气象台'
      })
    } else if (maxTemp >= 35) {
      alertList.push({
        type: 'heat',
        level: 'yellow',
        title: '高温黄色预警',
        desc: `预计最高温度将达到${maxTemp}°C，天气炎热`,
        detail: `${location}地区预计最高气温将达到${maxTemp}°C，请注意防暑。`,
        suggestions: [
          '午后尽量减少户外活动',
          '多饮水，保持身体水分'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '地方气象台'
      })
    }
    
    if (minTemp <= -10) {
      alertList.push({
        type: 'cold',
        level: 'red',
        title: '寒潮红色预警',
        desc: `预计最低温度将降至${minTemp}°C，极寒天气`,
        detail: `${location}地区预计最低气温将降至${minTemp}°C，极寒天气，请做好防寒保暖工作。`,
        suggestions: [
          '尽量减少外出，如需外出请做好保暖措施',
          '注意添衣保暖，尤其注意头部、手脚保暖',
          '检查供暖设备，确保正常运转',
          '关注老人、儿童等敏感人群',
          '注意防范一氧化碳中毒'
        ],
        affectedAreas: `${location}及周边地区`,
        time: new Date().toLocaleString('zh-CN'),
        source: '中央气象台'
      })
    } else if (minTemp <= -5) {
      alertList.push({
        type: 'cold',
        level: 'orange',
        title: '寒潮橙色预警',
        desc: `预计最低温度将降至${minTemp}°C，请注意保暖`,
        suggestions: [
          '注意添衣保暖',
          '外出时穿戴厚实衣物',
          '注意室内保暖'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '地方气象台'
      })
    } else if (minTemp <= 0) {
      alertList.push({
        type: 'cold',
        level: 'blue',
        title: '寒潮蓝色预警',
        desc: `预计最低温度将降至${minTemp}°C，气温较低`,
        suggestions: [
          '适当增加衣物',
          '早晚注意保暖'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '地方气象台'
      })
    }
    
    const tempDiff = maxTemp - minTemp
    if (tempDiff >= 10) {
      const level = tempDiff >= 15 ? 'yellow' : 'blue'
      alertList.push({
        type: 'temperature',
        level: level,
        title: '温差提醒',
        desc: `昼夜温差达${tempDiff.toFixed(0)}°C，请注意适时增减衣物`,
        detail: `${location}地区昼夜温差较大，达到${tempDiff.toFixed(0)}°C，早晚温差明显，请注意适时增减衣物，预防感冒。`,
        suggestions: [
          '早晚外出请携带外套',
          '注意预防感冒',
          '老人儿童请特别注意保暖',
          '根据气温变化及时调整着装'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '气象服务'
      })
    }
    
    if (maxTemp >= 30 && maxTemp < 35) {
      alertList.push({
        type: 'heat',
        level: 'blue',
        title: '高温提示',
        desc: `预计最高温度${maxTemp}°C，天气较热`,
        suggestions: [
          '注意防晒',
          '适量饮水'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '气象服务'
      })
    }
    
    if (minTemp > 0 && minTemp <= 5) {
      alertList.push({
        type: 'cold',
        level: 'blue',
        title: '低温提示',
        desc: `预计最低温度${minTemp}°C，早晚较凉`,
        suggestions: [
          '早晚注意添加衣物',
          '注意保暖'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '气象服务'
      })
    }
  }
  
  if (precipData.value?.precipitationAnalysis) {
    const precipAnalysis = precipData.value.precipitationAnalysis
    
    if (precipAnalysis.totalPrecipitation >= 50) {
      alertList.push({
        type: 'rain',
        level: 'red',
        title: '暴雨红色预警',
        desc: `预计降水量${precipAnalysis.totalPrecipitation}mm，暴雨天气`,
        detail: `${location}地区预计降水量将达到${precipAnalysis.totalPrecipitation}mm，可能引发城市内涝、山洪等灾害。`,
        suggestions: [
          '请勿外出，远离低洼地带',
          '注意防范城市内涝',
          '远离河道、山体等危险区域',
          '检查排水设施是否畅通',
          '准备应急物资和通讯设备'
        ],
        affectedAreas: `${location}及周边地区`,
        time: new Date().toLocaleString('zh-CN'),
        source: '中央气象台'
      })
    } else if (precipAnalysis.totalPrecipitation >= 25) {
      alertList.push({
        type: 'rain',
        level: 'orange',
        title: '暴雨橙色预警',
        desc: `预计降水量${precipAnalysis.totalPrecipitation}mm，大到暴雨`,
        suggestions: [
          '减少外出，携带雨具',
          '注意防范积水',
          '驾车请减速慢行'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '地方气象台'
      })
    } else if (precipAnalysis.totalPrecipitation >= 15) {
      alertList.push({
        type: 'rain',
        level: 'yellow',
        title: '暴雨黄色预警',
        desc: `预计降水量${precipAnalysis.totalPrecipitation}mm，中到大雨`,
        suggestions: [
          '外出请携带雨具',
          '注意道路湿滑'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '地方气象台'
      })
    } else if (precipAnalysis.totalPrecipitation >= 5) {
      alertList.push({
        type: 'rain',
        level: 'blue',
        title: '降水提醒',
        desc: `预计降水量${precipAnalysis.totalPrecipitation}mm，请携带雨具`,
        suggestions: [
          '外出请携带雨具',
          '注意出行安全'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '气象服务'
      })
    }
    
    if (precipAnalysis.avgPrecipitationProb >= 60) {
      alertList.push({
        type: 'precipitation',
        level: 'yellow',
        title: '高降水概率提醒',
        desc: `降水概率${precipAnalysis.avgPrecipitationProb}%，降水可能性较高`,
        suggestions: [
          '外出请携带雨具',
          '关注最新天气预报'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '气象服务'
      })
    } else if (precipAnalysis.avgPrecipitationProb >= 30) {
      alertList.push({
        type: 'precipitation',
        level: 'blue',
        title: '降水概率提示',
        desc: `降水概率${precipAnalysis.avgPrecipitationProb}%，可能有降水`,
        suggestions: [
          '建议携带雨具备用'
        ],
        time: new Date().toLocaleString('zh-CN'),
        source: '气象服务'
      })
    }
  }
  
  return alertList
})

const alertStats = computed(() => {
  const total = alerts.value.length
  const red = alerts.value.filter(a => a.level === 'red').length
  const orange = alerts.value.filter(a => a.level === 'orange').length
  const yellow = alerts.value.filter(a => a.level === 'yellow').length
  const blue = alerts.value.filter(a => a.level === 'blue').length
  return { total, red, orange, yellow, blue }
})

const alertStatsByLevel = computed(() => [
  { level: 'red', label: '红色', count: alertStats.value.red },
  { level: 'orange', label: '橙色', count: alertStats.value.orange },
  { level: 'yellow', label: '黄色', count: alertStats.value.yellow },
  { level: 'blue', label: '蓝色', count: alertStats.value.blue }
])

const alertLevelClass = computed(() => {
  if (alertStats.value.red > 0) return 'level-red'
  if (alertStats.value.orange > 0) return 'level-orange'
  if (alertStats.value.yellow > 0) return 'level-yellow'
  return 'level-blue'
})

const filteredAlerts = computed(() => {
  if (activeAlertFilter.value === 'all') return alerts.value
  return alerts.value.filter(a => a.level === activeAlertFilter.value)
})

function getLevelText(level) {
  const levelMap = {
    red: '红色预警',
    orange: '橙色预警',
    yellow: '黄色预警',
    blue: '蓝色预警'
  }
  return levelMap[level] || '预警'
}

function getFilterLabel(filterId) {
  const filter = alertFilters.find(f => f.id === filterId)
  return filter ? filter.label : ''
}

function openAlertDetail(alert) {
  selectedAlert.value = alert
  showAlertDetail.value = true
}

function closeAlertDetail() {
  showAlertDetail.value = false
  selectedAlert.value = null
}

function refreshAlerts() {
  alertLoading.value = true
  Promise.all([fetchTempData(), fetchPrecipData()]).finally(() => {
    setTimeout(() => {
      alertLoading.value = false
    }, 500)
  })
}

function handleAlertAction(alertItem, action) {
  if (action === 'export') {
    exportAlertAsImage(alertItem)
  }
}

async function exportAlertAsImage(alertItem) {
  try {
    const modalContent = document.querySelector('.alert-modal')
    if (!modalContent) {
      console.error('未找到预警详情模态框')
      return
    }
    
    const canvas = await html2canvas(modalContent, {
      backgroundColor: '#ffffff',
      scale: 2,
      useCORS: true,
      logging: false
    })
    
    const link = document.createElement('a')
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-')
    const alertTitle = alertItem?.title || '预警信息'
    link.download = `${alertTitle}-${timestamp}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
  } catch (error) {
    console.error('导出图片失败:', error)
    window.alert('导出图片失败，请稍后重试')
  }
}

function toggleExportMenu() {
  showExportMenu.value = !showExportMenu.value
}

function openExportViewModal() {
  showExportMenu.value = false
  showExportViewDialog.value = true
}

function closeExportViewModal() {
  showExportViewDialog.value = false
}

async function exportChart(chartType) {
  try {
    exportLoading.value = true
    closeExportViewModal()
    
    const cardRef = chartType === 'temperature' ? tempCardRef.value : precipCardRef.value
    const chartName = chartType === 'temperature' ? '气温趋势图' : '降水概率预测图'
    
    if (!cardRef) {
      window.alert('图表未加载，请稍后重试')
      return
    }
    
    const canvas = await html2canvas(cardRef, {
      backgroundColor: '#ffffff',
      scale: 3,
      useCORS: true,
      logging: false,
      width: cardRef.scrollWidth,
      height: cardRef.scrollHeight,
      windowWidth: cardRef.scrollWidth,
      windowHeight: cardRef.scrollHeight
    })
    
    const link = document.createElement('a')
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-')
    link.download = `${chartName}-${timestamp}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    
    window.alert('导出成功！')
  } catch (error) {
    console.error('导出图表失败:', error)
    window.alert('导出图表失败，请稍后重试')
  } finally {
    exportLoading.value = false
  }
}

async function exportAllToPdf() {
  try {
    showExportMenu.value = false
    exportLoading.value = true
    
    const pdf = new jsPDF('l', 'mm', 'a4')
    const pageWidth = pdf.internal.pageSize.getWidth()
    const pageHeight = pdf.internal.pageSize.getHeight()
    const margin = 10
    const imgWidth = pageWidth - 2 * margin
    let currentY = margin
    
    const chartConfigs = [
      { cardRef: tempCardRef.value, range: '24h', type: 'temp' },
      { cardRef: tempCardRef.value, range: '7d', type: 'temp' },
      { cardRef: tempCardRef.value, range: '30d', type: 'temp' },
      { cardRef: precipCardRef.value, range: '24h', type: 'precip' },
      { cardRef: precipCardRef.value, range: '7d', type: 'precip' },
      { cardRef: precipCardRef.value, range: '30d', type: 'precip' }
    ]
    
    for (let i = 0; i < chartConfigs.length; i++) {
      const config = chartConfigs[i]
      
      if (config.cardRef) {
        if (config.type === 'temp') {
          activeTimeRange.value = config.range
        } else {
          activePrecipRange.value = config.range
        }
        
        await new Promise(resolve => setTimeout(resolve, 600))
        
        const canvas = await html2canvas(config.cardRef, {
          backgroundColor: '#ffffff',
          scale: 2.5,
          useCORS: true,
          logging: false,
          width: config.cardRef.scrollWidth,
          height: config.cardRef.scrollHeight,
          windowWidth: config.cardRef.scrollWidth,
          windowHeight: config.cardRef.scrollHeight
        })
        
        if (i > 0) {
          pdf.addPage()
        }
        
        const imgHeight = (canvas.height * imgWidth) / canvas.width
        
        if (currentY + imgHeight > pageHeight - margin) {
          pdf.addPage()
          currentY = margin
        }
        
        const imgData = canvas.toDataURL('image/png')
        pdf.addImage(imgData, 'PNG', margin, currentY, imgWidth, imgHeight)
        
        currentY += imgHeight + 5
      }
    }
    
    const timestamp = new Date().toISOString().split('T')[0].replace(/-/g, '')
    pdf.save(`weather-data-export-${timestamp}.pdf`)
    
    window.alert('PDF exported successfully!')
  } catch (error) {
    console.error('Export PDF failed:', error)
    window.alert('Export PDF failed, please try again later')
  } finally {
    exportLoading.value = false
  }
}




const tempSummary = computed(() => {
  if (!tempData.value?.temperatureAnalysis) {
    return { avgTemp: '--', maxTemp: '--', minTemp: '--', description: '' }
  }
  const temp = tempData.value.temperatureAnalysis
  return {
    avgTemp: temp.avgTemp?.toFixed(1) || '--',
    maxTemp: temp.maxTemp?.toFixed(0) || '--',
    minTemp: temp.minTemp?.toFixed(0) || '--',
    description: temp.description || ''
  }
})

const precipSummary = computed(() => {
  if (!precipData.value?.precipitationAnalysis) {
    return { avgProb: '--', totalPrecip: '--', period: '--', description: '' }
  }
  const precip = precipData.value.precipitationAnalysis
  return {
    avgProb: precip.avgPrecipitationProb || 0,
    totalPrecip: precip.totalPrecipitation?.toFixed(1) || '0',
    period: precip.precipitationPeriod || '无',
    description: precip.description || ''
  }
})

let tempChart = null
let precipChart = null

async function fetchTempData() {
  tempLoading.value = true
  error.value = null
  
  try {
    const location = weatherStore.currentCity?.name || '上海'
    const response = await weatherApi.getAnalysis(location, activeTimeRange.value)
    
    if (response && response.data) {
      tempData.value = response.data
    } else {
      throw new Error('获取气温数据失败')
    }
  } catch (e) {
    console.error('Failed to fetch temp data:', e)
    error.value = e.message || '获取气温数据失败，请稍后重试'
  } finally {
    tempLoading.value = false
  }
}

async function fetchPrecipData() {
  precipLoading.value = true
  
  try {
    const location = weatherStore.currentCity?.name || '上海'
    const response = await weatherApi.getAnalysis(location, activePrecipRange.value)
    
    if (response && response.data) {
      precipData.value = response.data
    } else {
      throw new Error('获取降水数据失败')
    }
  } catch (e) {
    console.error('Failed to fetch precip data:', e)
  } finally {
    precipLoading.value = false
  }
}

function changeTimeRange(rangeId) {
  activeTimeRange.value = rangeId
  fetchTempData()
}

function changePrecipRange(rangeId) {
  activePrecipRange.value = rangeId
  fetchPrecipData()
}

function updateTempChart() {
  if (!tempChart || !tempData.value?.temperatureAnalysis) return
  
  const tempAnalysis = tempData.value.temperatureAnalysis
  const trendData = tempAnalysis.trendData || []
  
  const times = trendData.map(d => d.time)
  const values = trendData.map(d => d.value)
  
  tempChart.setOption({
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: times,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#64748b', formatter: '{value}°' },
      splitLine: { lineStyle: { color: '#e2e8f0', type: 'dashed' } }
    },
    series: [{
      type: 'line',
      smooth: true,
      data: values,
      lineStyle: { color: '#fb923c', width: 3 },
      itemStyle: { color: '#fb923c' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(251, 146, 60, 0.3)' },
            { offset: 1, color: 'rgba(251, 146, 60, 0)' }
          ]
        }
      }
    }]
  })
}

function updatePrecipChart() {
  if (!precipChart || !precipData.value?.precipitationAnalysis) return
  
  const precipAnalysis = precipData.value.precipitationAnalysis
  const trendData = precipAnalysis.trendData || []
  
  const times = trendData.map(d => d.time)
  const values = trendData.map(d => d.value)
  
  precipChart.setOption({
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: times,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#64748b', formatter: '{value}mm' },
      splitLine: { lineStyle: { color: '#e2e8f0', type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#60a5fa' },
            { offset: 1, color: '#3b82f6' }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      }
    }]
  })
}

const initTempChart = () => {
  if (tempChart) {
    tempChart.dispose()
    tempChart = null
  }
  if (tempChartRef.value) {
    tempChart = echarts.init(tempChartRef.value)
  }
}

const initPrecipChart = () => {
  if (precipChart) {
    precipChart.dispose()
    precipChart = null
  }
  if (precipChartRef.value) {
    precipChart = echarts.init(precipChartRef.value)
  }
}

const handleResize = () => {
  tempChart?.resize()
  precipChart?.resize()
}

watch(tempData, () => {
  nextTick(() => {
    if (!tempChart && tempChartRef.value) {
      initTempChart()
    }
    updateTempChart()
  })
}, { deep: true })

watch(precipData, () => {
  nextTick(() => {
    if (!precipChart && precipChartRef.value) {
      initPrecipChart()
    }
    updatePrecipChart()
  })
}, { deep: true })

onMounted(() => {
  window.addEventListener('resize', handleResize)
  document.addEventListener('click', handleClickOutside)
  nextTick(() => {
    initTempChart()
    initPrecipChart()
  })
  fetchTempData()
  fetchPrecipData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('click', handleClickOutside)
  tempChart?.dispose()
  precipChart?.dispose()
})

function handleClickOutside(event) {
  const dropdown = document.querySelector('.export-dropdown')
  if (dropdown && !dropdown.contains(event.target)) {
    showExportMenu.value = false
  }
}
</script>

<style scoped>
.analysis-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.section-header {
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.section-header-left {
  flex: 1;
}

.section-header-right {
  position: relative;
}

.export-dropdown {
  position: relative;
}

.export-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1rem;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border: none;
  border-radius: 0.5rem;
  color: white;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.export-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.dropdown-arrow {
  transition: transform 0.3s ease;
}

.dropdown-arrow.rotated {
  transform: rotate(180deg);
}

.export-menu {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 180px;
  overflow: hidden;
  z-index: 1000;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  font-size: 0.875rem;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background: rgba(59, 130, 246, 0.05);
  color: var(--blue-600);
}

.menu-item svg {
  color: var(--blue-500);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.section-main-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--blue-900);
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.section-desc {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: 0.5rem;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

.analysis-card {
  padding: 1.5rem;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.analysis-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--blue-900);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.time-selector {
  display: flex;
  gap: 0.5rem;
}

.time-btn {
  background: rgba(255, 255, 255, 0.8);
  border: none;
  padding: 0.375rem 0.75rem;
  border-radius: 0.375rem;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-secondary);
}

.time-btn:hover {
  background: white;
}

.time-btn.active {
  background: var(--blue-500);
  color: white;
}

.analysis-chart {
  height: 200px;
  margin: 1rem 0;
  position: relative;
}

.chart-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.5);
  z-index: 10;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(59, 130, 246, 0.2);
  border-top-color: var(--blue-500);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.analysis-summary {
  display: flex;
  justify-content: space-between;
  padding-top: 1rem;
  border-top: 1px solid rgba(147, 197, 253, 0.3);
}

.summary-item {
  text-align: center;
}

.summary-label {
  display: block;
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-bottom: 0.25rem;
}

.summary-value {
  font-size: 1rem;
  font-weight: 700;
  color: var(--blue-900);
}

.summary-value.high {
  color: #ef4444;
}

.summary-value.low {
  color: #3b82f6;
}

.analysis-desc {
  margin-top: 1rem;
  padding: 0.75rem;
  background: rgba(59, 130, 246, 0.05);
  border-radius: 0.5rem;
  font-size: 0.75rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.alert-card {
  grid-column: span 2;
}

.alert-header-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.refresh-btn {
  background: rgba(255, 255, 255, 0.8);
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-secondary);
}

.refresh-btn:hover:not(:disabled) {
  background: white;
  color: var(--blue-500);
}

.refresh-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.refresh-btn .spinning {
  animation: spin 1s linear infinite;
}

.alert-count-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.alert-count-badge.level-red {
  background: rgba(239, 68, 68, 0.15);
  color: #dc2626;
}

.alert-count-badge.level-orange {
  background: rgba(249, 115, 22, 0.15);
  color: #ea580c;
}

.alert-count-badge.level-yellow {
  background: rgba(234, 179, 8, 0.15);
  color: #ca8a04;
}

.alert-count-badge.level-blue {
  background: rgba(59, 130, 246, 0.15);
  color: #2563eb;
}

.alert-summary-bar {
  display: flex;
  gap: 1rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.alert-stat {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
}

.stat-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.stat-dot.red { background: #dc2626; }
.stat-dot.orange { background: #ea580c; }
.stat-dot.yellow { background: #eab308; }
.stat-dot.blue { background: #3b82f6; }

.stat-label {
  color: var(--text-secondary);
}

.stat-count {
  font-weight: 600;
  color: var(--text-primary);
}

.alert-filter {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.filter-btn {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(147, 197, 253, 0.3);
  padding: 0.375rem 0.875rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-secondary);
}

.filter-btn:hover {
  background: rgba(255, 255, 255, 0.8);
}

.filter-btn.active {
  background: var(--blue-500);
  color: white;
  border-color: var(--blue-500);
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.alert-item {
  border-radius: 0.75rem;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.alert-item:hover {
  background: rgba(255, 255, 255, 0.8);
}

.alert-item.red {
  border-left: 4px solid #dc2626;
  background: linear-gradient(90deg, rgba(239, 68, 68, 0.08) 0%, rgba(255, 255, 255, 0.5) 30%);
}

.alert-item.orange {
  border-left: 4px solid #ea580c;
  background: linear-gradient(90deg, rgba(249, 115, 22, 0.08) 0%, rgba(255, 255, 255, 0.5) 30%);
}

.alert-item.yellow {
  border-left: 4px solid #eab308;
  background: linear-gradient(90deg, rgba(234, 179, 8, 0.08) 0%, rgba(255, 255, 255, 0.5) 30%);
}

.alert-item.blue {
  border-left: 4px solid #3b82f6;
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.08) 0%, rgba(255, 255, 255, 0.5) 30%);
}

.alert-main {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  align-items: flex-start;
}

.alert-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.alert-icon-wrapper.red {
  background: rgba(239, 68, 68, 0.15);
  color: #dc2626;
}

.alert-icon-wrapper.orange {
  background: rgba(249, 115, 22, 0.15);
  color: #ea580c;
}

.alert-icon-wrapper.yellow {
  background: rgba(234, 179, 8, 0.15);
  color: #ca8a04;
}

.alert-icon-wrapper.blue {
  background: rgba(59, 130, 246, 0.15);
  color: #2563eb;
}

.alert-icon-svg {
  width: 24px;
  height: 24px;
}

.alert-content {
  flex: 1;
  min-width: 0;
}

.alert-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.375rem;
  flex-wrap: wrap;
}

.alert-level-tag {
  padding: 0.125rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
}

.alert-level-tag.red {
  background: #dc2626;
  color: white;
}

.alert-level-tag.orange {
  background: #ea580c;
  color: white;
}

.alert-level-tag.yellow {
  background: #eab308;
  color: #1f2937;
}

.alert-level-tag.blue {
  background: #3b82f6;
  color: white;
}

.alert-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-primary);
}

.alert-desc {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.alert-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.6875rem;
  color: var(--text-muted);
}

.alert-time {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.alert-source {
  opacity: 0.8;
}

.alert-view-btn {
  padding: 0.5rem 1rem;
  background: white;
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 8px;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--blue-600);
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  white-space: nowrap;
}

.alert-view-btn:hover {
  background: var(--blue-500);
  border-color: var(--blue-500);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.alert-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.alert-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.alert-modal-header {
  padding: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.alert-modal-header.red {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), rgba(239, 68, 68, 0.05));
}

.alert-modal-header.orange {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.1), rgba(249, 115, 22, 0.05));
}

.alert-modal-header.yellow {
  background: linear-gradient(135deg, rgba(234, 179, 8, 0.1), rgba(234, 179, 8, 0.05));
}

.alert-modal-header.blue {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05));
}

.header-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.alert-modal-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.alert-modal-header.red .alert-modal-icon {
  color: #dc2626;
}

.alert-modal-header.orange .alert-modal-icon {
  color: #ea580c;
}

.alert-modal-header.yellow .alert-modal-icon {
  color: #eab308;
}

.alert-modal-header.blue .alert-modal-icon {
  color: #3b82f6;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.alert-modal-level-tag {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  width: fit-content;
}

.alert-modal-level-tag.red {
  background: rgba(239, 68, 68, 0.15);
  color: #dc2626;
}

.alert-modal-level-tag.orange {
  background: rgba(249, 115, 22, 0.15);
  color: #ea580c;
}

.alert-modal-level-tag.yellow {
  background: rgba(234, 179, 8, 0.15);
  color: #eab308;
}

.alert-modal-level-tag.blue {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.alert-modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.alert-modal-close {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.alert-modal-close:hover {
  background: rgba(0, 0, 0, 0.1);
  color: var(--text-primary);
}

.alert-modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
}

.alert-modal-meta {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.meta-item svg {
  color: var(--blue-500);
}

.alert-modal-section {
  margin-bottom: 1.5rem;
}

.alert-modal-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.75rem 0;
}

.section-title svg {
  color: var(--blue-500);
}

.section-content {
  font-size: 0.9375rem;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0;
  padding-left: 1.5rem;
}

.alert-modal-section .suggestion-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.alert-modal-section .suggestion-list li {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  font-size: 0.9375rem;
  color: var(--text-secondary);
  line-height: 1.7;
  padding: 0.5rem 0;
}

.alert-modal-section .suggestion-list li svg {
  color: var(--blue-500);
  flex-shrink: 0;
  margin-top: 0.25rem;
}

.alert-modal-footer {
  padding: 1.5rem;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.modal-btn {
  padding: 0.75rem 1.5rem;
  border-radius: 10px;
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.modal-btn.secondary {
  background: rgba(0, 0, 0, 0.05);
  border: none;
  color: var(--text-secondary);
}

.modal-btn.secondary:hover {
  background: rgba(0, 0, 0, 0.1);
  color: var(--text-primary);
}

.modal-btn.primary {
  background: var(--blue-500);
  border: none;
  color: white;
}

.modal-btn.primary:hover {
  background: var(--blue-600);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.detail-section {
  margin-top: 1rem;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.detail-text {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.6;
  padding-left: 1.25rem;
}

.suggestion-list {
  list-style: none;
  padding: 0;
  margin: 0;
  padding-left: 1.25rem;
}

.suggestion-list li {
  position: relative;
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 0.25rem 0;
  padding-left: 1rem;
}

.suggestion-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--blue-500);
}

.detail-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(147, 197, 253, 0.2);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(147, 197, 253, 0.3);
  color: var(--text-secondary);
}

.action-btn:hover {
  background: white;
  color: var(--blue-500);
  border-color: var(--blue-300);
}

.action-btn.primary {
  background: var(--blue-500);
  color: white;
  border-color: var(--blue-500);
}

.action-btn.primary:hover {
  background: var(--blue-600);
}

.alert-empty {
  text-align: center;
  padding: 2rem;
}

.empty-icon {
  color: #10b981;
  margin-bottom: 1rem;
}

.empty-text {
  font-size: 0.9375rem;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.empty-subtext {
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.error-toast {
  position: fixed;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(239, 68, 68, 0.95);
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.toast-retry {
  background: white;
  color: #ef4444;
  border: none;
  padding: 0.25rem 0.75rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toast-retry:hover {
  background: rgba(255, 255, 255, 0.9);
}

@media (max-width: 1024px) {
  .analysis-grid {
    grid-template-columns: 1fr;
  }
  
  .alert-card {
    grid-column: span 1;
  }
  
  .analysis-summary {
    flex-wrap: wrap;
    gap: 0.75rem;
  }
  
  .summary-item {
    flex: 1;
    min-width: 80px;
  }
}

@media (max-width: 768px) {
  .section-main-title {
    font-size: 1.25rem;
  }
  
  .analysis-card {
    padding: 1rem;
  }
  
  .analysis-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .time-selector {
    width: 100%;
    justify-content: flex-start;
  }
  
  .alert-summary-bar {
    justify-content: center;
  }
  
  .alert-filter {
    width: 100%;
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 0.5rem;
    -webkit-overflow-scrolling: touch;
  }
  
  .filter-btn {
    flex-shrink: 0;
  }
  
  .alert-main {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .alert-expand-icon {
    position: absolute;
    right: 1rem;
    top: 1rem;
  }
  
  .alert-item {
    position: relative;
  }
  
  .detail-actions {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .section-main-title {
    font-size: 1.125rem;
  }
  
  .section-desc {
    font-size: 0.75rem;
  }
  
  .analysis-title {
    font-size: 0.875rem;
  }
  
  .time-btn {
    padding: 0.25rem 0.5rem;
    font-size: 0.6875rem;
  }
  
  .alert-summary-bar {
    gap: 0.5rem;
    padding: 0.5rem;
  }
  
  .alert-stat {
    font-size: 0.6875rem;
  }
  
  .alert-icon-wrapper {
    width: 36px;
    height: 36px;
  }
  
  .alert-icon-svg {
    width: 20px;
    height: 20px;
  }
  
  .alert-title {
    font-size: 0.875rem;
  }
  
  .alert-desc {
    font-size: 0.75rem;
  }
  
  .alert-meta {
    flex-direction: column;
    gap: 0.25rem;
  }
}

.qrcode-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
  padding: 1rem;
}

.qrcode-modal {
  background: white;
  border-radius: 1rem;
  width: 100%;
  max-width: 300px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.qrcode-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e5e7eb;
}

.qrcode-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.qrcode-body {
  padding: 1.5rem;
  text-align: center;
}

.qrcode-placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  border-radius: 0.75rem;
  padding: 1rem;
  margin-bottom: 1rem;
  color: #9ca3af;
}

.qrcode-tip {
  font-size: 0.8125rem;
  color: #6b7280;
  margin: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.qq-config-modal {
  background: white;
  border-radius: 1rem;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.qq-config-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e5e7eb;
}

.qq-config-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.qq-config-body {
  padding: 1.5rem;
  text-align: center;
}

.config-warning {
  margin-bottom: 1rem;
}

.qq-config-body h4 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.5rem;
}

.config-desc {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 1.5rem;
}

.config-steps {
  text-align: left;
  background: #f9fafb;
  border-radius: 0.75rem;
  padding: 1rem;
  margin-bottom: 1rem;
}

.step {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
}

.step:not(:last-child) {
  border-bottom: 1px dashed #e5e7eb;
}

.step-num {
  width: 24px;
  height: 24px;
  background: #3b82f6;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  flex-shrink: 0;
}

.step-text {
  font-size: 0.875rem;
  color: #4b5563;
}

.step-text a {
  color: #3b82f6;
  text-decoration: none;
}

.step-text a:hover {
  text-decoration: underline;
}

.config-example {
  background: #1f2937;
  border-radius: 0.5rem;
  padding: 1rem;
  text-align: left;
}

.config-example code {
  display: block;
  color: #9ca3af;
  font-size: 0.75rem;
  margin-bottom: 0.5rem;
}

.config-example pre {
  margin: 0;
  color: #10b981;
  font-size: 0.8125rem;
  font-family: 'Monaco', 'Menlo', monospace;
  white-space: pre-wrap;
}

.qq-config-footer {
  padding: 1rem 1.5rem 1.5rem;
  text-align: center;
}

.config-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #12B7F5 0%, #0099FF 100%);
  color: white;
  text-decoration: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  transition: transform 0.2s, box-shadow 0.2s;
}

.config-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(18, 183, 245, 0.4);
}

@media (max-width: 480px) {
  .qq-config-modal {
    max-width: 100%;
    margin: 0 0.5rem;
  }
  
  .config-steps {
    padding: 0.75rem;
  }
  
  .step-text {
    font-size: 0.8125rem;
  }
}

.export-view-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1002;
  padding: 1rem;
}

.export-view-modal {
  background: white;
  border-radius: 1rem;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.export-view-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  color: white;
}

.export-view-modal-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 0.5rem;
  padding: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  color: white;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.export-view-modal-body {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.export-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(59, 130, 246, 0.05);
  border: 2px solid rgba(59, 130, 246, 0.2);
  border-radius: 0.75rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.export-option:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: var(--blue-500);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.option-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, var(--blue-500), var(--blue-600));
  border-radius: 0.75rem;
  color: white;
}

.option-content h4 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.25rem 0;
}

.option-content p {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin: 0;
}
</style>
