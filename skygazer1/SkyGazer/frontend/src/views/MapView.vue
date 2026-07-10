<template>
  <div class="weather-map-page">
    <div class="map-header glass-card">
      <div class="header-left">
        <h1 class="page-title">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
          </svg>
          气象地图
          <span class="current-level" v-if="currentLevel !== 'country'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
            {{ currentRegionName }}
          </span>
        </h1>
        <p class="update-time" v-if="lastUpdate">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          {{ formatTime(lastUpdate) }}
        </p>
      </div>
      <div class="header-actions">
        <button class="btn-back" v-if="currentLevel !== 'country'" @click="goBack">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          返回上级
        </button>
        <div class="refresh-wrapper">
          <button class="btn-refresh" @click="refreshData" :disabled="loading || !canRefresh">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :class="{ 'spinning': loading }">
              <path d="M21 2v6h-6M3 12a9 9 0 0 1 15-6.7L21 8M3 22v-6h6M21 12a9 9 0 0 1-15 6.7L3 16"/>
            </svg>
            刷新
          </button>
          <div class="refresh-status" v-if="!canRefresh">
            <span class="cooldown-text">{{ refreshMessage }}</span>
            <span class="cooldown-timer">{{ refreshCooldown }}秒</span>
          </div>
        </div>
      </div>
    </div>

    <div class="map-container">
      <div class="layer-panel glass-card">
        <div class="panel-header">
          <h3>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="12 2 2 7 12 12 22 7 12 2"/>
              <polyline points="2 17 12 22 22 17"/>
              <polyline points="2 12 12 17 22 12"/>
            </svg>
            图层选择
          </h3>
        </div>
        <div class="layer-list">
          <button
            v-for="layer in layers"
            :key="layer.code"
            :class="['layer-btn', { active: currentLayer === layer.code }]"
            @click="switchLayer(layer.code)"
          >
            <span class="layer-icon" :style="{ background: layer.gradient }"></span>
            <span class="layer-name">{{ layer.name }}</span>
            <span class="layer-unit">{{ layer.unit }}</span>
          </button>
        </div>
      </div>

      <div class="map-main">
        <div ref="chartRef" class="chart-container"></div>
        
        <div class="map-overlay" v-if="loading">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>

        <div class="map-overlay error-overlay" v-if="error">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <p>{{ error }}</p>
          <button class="btn-retry" @click="refreshData">重试</button>
        </div>
      </div>

      <div class="info-panel glass-card" v-if="hoveredCity">
        <div class="info-header">
          <h4>{{ hoveredCity.name }}</h4>
          <span class="info-value" :style="{ color: getValueColor(hoveredCity.value) }">
            {{ hoveredCity.value }}{{ currentLayerConfig?.unit }}
          </span>
        </div>
        <div class="info-details">
          <div class="info-row">
            <span class="info-label">等级</span>
            <span class="info-data">{{ getLevelLabel(hoveredCity.value) }}</span>
          </div>
          <div class="info-row" v-if="hoveredCity.weather">
            <span class="info-label">天气</span>
            <span class="info-data">{{ hoveredCity.weather }}</span>
          </div>
          <div class="info-row" v-if="hoveredCity.humidity">
            <span class="info-label">湿度</span>
            <span class="info-data">{{ hoveredCity.humidity }}%</span>
          </div>
          <div class="info-row" v-if="hoveredCity.wind">
            <span class="info-label">风力</span>
            <span class="info-data">{{ hoveredCity.wind }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { mapApi } from '@/api'

const chartRef = ref(null)
let chartInstance = null
let chinaGeoJson = null
let districtGeoJson = null

const loading = ref(false)
const error = ref(null)
const lastUpdate = ref(null)
const currentLayer = ref('temperature')
const currentLevel = ref('country')
const currentRegionCode = ref('100000')
const currentRegionName = ref('')
const hoveredCity = ref(null)
const weatherData = ref([])
const useDistrictLevel = ref(true)

const canRefresh = ref(true)
const refreshCooldown = ref(0)
const refreshMessage = ref('')
let refreshTimer = null

const layers = [
  { code: 'temperature', name: '温度', unit: '°C', gradient: 'linear-gradient(90deg, #60a5fa, #34d399, #fbbf24, #f97316, #ef4444)' },
  { code: 'precipitation', name: '降水', unit: 'mm', gradient: 'linear-gradient(90deg, #e0f2fe, #7dd3fc, #38bdf8, #0284c7, #075985)' },
  { code: 'wind', name: '风力', unit: '级', gradient: 'linear-gradient(90deg, #dcfce7, #86efac, #4ade80, #22c55e, #15803d)' },
  { code: 'pressure', name: '气压', unit: 'hPa', gradient: 'linear-gradient(90deg, #fef3c7, #fcd34d, #f59e0b, #d97706, #92400e)' },
  { code: 'cloud', name: '云量', unit: '%', gradient: 'linear-gradient(90deg, #f8fafc, #e2e8f0, #94a3b8, #64748b, #1e293b)' },
  { code: 'air_quality', name: '空气质量', unit: 'AQI', gradient: 'linear-gradient(90deg, #22c55e, #eab308, #f97316, #ef4444, #7c2d12)' },
  { code: 'visibility', name: '能见度', unit: 'km', gradient: 'linear-gradient(90deg, #a5f3fc, #67e8f9, #22d3ee, #06b6d4, #0891b2)' }
]

const layerConfig = {
  temperature: { name: '温度', unit: '°C', min: -20, max: 40, colors: ['#60a5fa', '#34d399', '#fbbf24', '#f97316', '#ef4444'], levels: ['寒冷', '凉爽', '舒适', '温暖', '炎热'] },
  precipitation: { name: '降水', unit: 'mm', min: 0, max: 100, colors: ['#e0f2fe', '#7dd3fc', '#38bdf8', '#0284c7', '#075985'], levels: ['无雨', '小雨', '中雨', '大雨', '暴雨'] },
  wind: { name: '风力', unit: '级', min: 0, max: 12, colors: ['#dcfce7', '#86efac', '#4ade80', '#22c55e', '#15803d'], levels: ['微风', '轻风', '和风', '强风', '大风'] },
  pressure: { name: '气压', unit: 'hPa', min: 980, max: 1040, colors: ['#fef3c7', '#fcd34d', '#f59e0b', '#d97706', '#92400e'], levels: ['低压', '较低', '正常', '较高', '高压'] },
  cloud: { name: '云量', unit: '%', min: 0, max: 100, colors: ['#f8fafc', '#e2e8f0', '#94a3b8', '#64748b', '#1e293b'], levels: ['晴朗', '少云', '多云', '阴天', '密云'] },
  air_quality: { name: '空气质量', unit: 'AQI', min: 0, max: 300, colors: ['#22c55e', '#eab308', '#f97316', '#ef4444', '#7c2d12'], levels: ['优', '良', '轻度污染', '中度污染', '重度污染'] },
  visibility: { name: '能见度', unit: 'km', min: 0, max: 30, colors: ['#a5f3fc', '#67e8f9', '#22d3ee', '#06b6d4', '#0891b2'], levels: ['极好', '良好', '一般', '较差', '极差'] }
}

const currentLayerConfig = computed(() => layerConfig[currentLayer.value])

const provinceAdcodeMap = {
  '北京市': '110000', '天津市': '120000', '河北省': '130000', '山西省': '140000',
  '内蒙古自治区': '150000', '辽宁省': '210000', '吉林省': '220000', '黑龙江省': '230000',
  '上海市': '310000', '江苏省': '320000', '浙江省': '330000', '安徽省': '340000',
  '福建省': '350000', '江西省': '360000', '山东省': '370000', '河南省': '410000',
  '湖北省': '420000', '湖南省': '430000', '广东省': '440000', '广西壮族自治区': '450000',
  '海南省': '460000', '重庆市': '500000', '四川省': '510000', '贵州省': '520000',
  '云南省': '530000', '西藏自治区': '540000', '陕西省': '610000', '甘肃省': '620000',
  '青海省': '630000', '宁夏回族自治区': '640000', '新疆维吾尔自治区': '650000',
  '台湾省': '710000', '香港特别行政区': '810000', '澳门特别行政区': '820000'
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getValueColor = (value) => {
  const config = currentLayerConfig.value
  if (!config || value === null || value === undefined) return '#94a3b8'
  
  const ratio = Math.max(0, Math.min(1, (value - config.min) / (config.max - config.min)))
  const index = Math.floor(ratio * (config.colors.length - 1))
  return config.colors[Math.min(index, config.colors.length - 1)]
}

const getLevelLabel = (value) => {
  const config = currentLayerConfig.value
  if (!config || value === null || value === undefined) return '未知'
  
  const ratio = Math.max(0, Math.min(1, (value - config.min) / (config.max - config.min)))
  const index = Math.floor(ratio * (config.levels.length - 1))
  return config.levels[Math.min(index, config.levels.length - 1)]
}

const loadChinaGeoJson = async () => {
  if (chinaGeoJson) return chinaGeoJson
  
  try {
    const response = await mapApi.getGeoJson('100000', 'full')
    if (response && response.data) {
      chinaGeoJson = typeof response.data === 'string' 
        ? JSON.parse(response.data) 
        : response.data
      return chinaGeoJson
    }
    throw new Error('GeoJSON 数据格式无效')
  } catch (err) {
    console.error('加载地图数据失败:', err)
    throw err
  }
}

const loadDistrictGeoJson = async () => {
  if (districtGeoJson) return districtGeoJson
  
  try {
    const response = await fetch('/map/china-district.json')
    districtGeoJson = await response.json()
    return districtGeoJson
  } catch (err) {
    console.error('加载区县地图数据失败:', err)
    throw err
  }
}

const filterDistrictsByProvince = (geoJson, provinceAdcode) => {
  const filteredFeatures = geoJson.features.filter(feature => {
    return feature.properties.parent && feature.properties.parent.adcode === provinceAdcode
  })
  
  return {
    type: 'FeatureCollection',
    features: filteredFeatures
  }
}

const loadProvinceGeoJson = async (adcode) => {
  try {
    const response = await mapApi.getGeoJson(adcode, 'full')
    
    if (!response || !response.data) {
      throw new Error('获取地图数据失败：响应为空')
    }
    
    const geoJson = typeof response.data === 'string' 
      ? JSON.parse(response.data) 
      : response.data
    
    if (!geoJson || !geoJson.features || !Array.isArray(geoJson.features)) {
      throw new Error('地图数据格式无效')
    }
    
    console.log(`加载省份地图成功: ${adcode}, features: ${geoJson.features.length}`)
    return geoJson
    
  } catch (err) {
    console.error('加载省份地图数据失败:', err)
    
    if (err.message && err.message.includes('网络')) {
      throw new Error('网络连接失败，请检查网络设置')
    } else if (err.message && err.message.includes('格式')) {
      throw new Error('地图数据服务暂时不可用，请稍后重试')
    }
    
    throw err
  }
}

const calculateGeoCenter = (geoJson) => {
  if (!geoJson || !geoJson.features || geoJson.features.length === 0) {
    return { center: [104.5, 36], zoom: 1.5 }
  }
  
  let allCoords = []
  
  geoJson.features.forEach(feature => {
    if (feature.geometry && feature.geometry.coordinates) {
      const extractCoords = (coords, depth = 0) => {
        if (depth > 3) return
        
        if (Array.isArray(coords[0])) {
          coords.forEach(coord => extractCoords(coord, depth + 1))
        } else if (Array.isArray(coords) && coords.length >= 2) {
          allCoords.push([coords[0], coords[1]])
        }
      }
      
      extractCoords(feature.geometry.coordinates)
    }
  })
  
  if (allCoords.length === 0) {
    return { center: [104.5, 36], zoom: 1.5 }
  }
  
  let minLng = Infinity
  let maxLng = -Infinity
  let minLat = Infinity
  let maxLat = -Infinity
  
  allCoords.forEach(([lng, lat]) => {
    minLng = Math.min(minLng, lng)
    maxLng = Math.max(maxLng, lng)
    minLat = Math.min(minLat, lat)
    maxLat = Math.max(maxLat, lat)
  })
  
  const centerLng = (minLng + maxLng) / 2
  const centerLat = (minLat + maxLat) / 2
  
  const lngDiff = maxLng - minLng
  const latDiff = maxLat - minLat
  const maxDiff = Math.max(lngDiff, latDiff)
  
  let zoom
  if (maxDiff > 15) {
    zoom = 0.8
  } else if (maxDiff > 10) {
    zoom = 1.0
  } else if (maxDiff > 5) {
    zoom = 1.3
  } else if (maxDiff > 2) {
    zoom = 1.6
  } else {
    zoom = 2.0
  }
  
  console.log(`计算中心点: [${centerLng.toFixed(4)}, ${centerLat.toFixed(4)}], 缩放: ${zoom}`)
  
  return {
    center: [centerLng, centerLat],
    zoom: zoom
  }
}

const fetchWeatherData = async () => {
  try {
    const response = await mapApi.getDistrictWeather('100000', currentLayer.value)
    if (response.data && response.data.cities) {
      weatherData.value = response.data.cities
      lastUpdate.value = response.data.updateTime || Date.now()
    }
    return weatherData.value
  } catch (err) {
    console.error('获取天气数据失败:', err)
    throw err
  }
}

const aggregateDataByProvince = (cityData) => {
  const provinceMap = new Map()
  
  cityData.forEach(city => {
    const provinceName = city.province || city.name
    
    if (provinceName === '北京') {
      const fullName = '北京市'
      if (!provinceMap.has(fullName)) {
        provinceMap.set(fullName, { name: fullName, value: city.value, cities: [city] })
      } else {
        const existing = provinceMap.get(fullName)
        existing.value = (existing.value + city.value) / 2
        existing.cities.push(city)
      }
    } else if (provinceName === '天津') {
      const fullName = '天津市'
      if (!provinceMap.has(fullName)) {
        provinceMap.set(fullName, { name: fullName, value: city.value, cities: [city] })
      } else {
        const existing = provinceMap.get(fullName)
        existing.value = (existing.value + city.value) / 2
        existing.cities.push(city)
      }
    } else if (provinceName === '上海') {
      const fullName = '上海市'
      if (!provinceMap.has(fullName)) {
        provinceMap.set(fullName, { name: fullName, value: city.value, cities: [city] })
      } else {
        const existing = provinceMap.get(fullName)
        existing.value = (existing.value + city.value) / 2
        existing.cities.push(city)
      }
    } else if (provinceName === '重庆') {
      const fullName = '重庆市'
      if (!provinceMap.has(fullName)) {
        provinceMap.set(fullName, { name: fullName, value: city.value, cities: [city] })
      } else {
        const existing = provinceMap.get(fullName)
        existing.value = (existing.value + city.value) / 2
        existing.cities.push(city)
      }
    } else if (provinceName === '香港') {
      const fullName = '香港特别行政区'
      if (!provinceMap.has(fullName)) {
        provinceMap.set(fullName, { name: fullName, value: city.value, cities: [city] })
      } else {
        const existing = provinceMap.get(fullName)
        existing.value = (existing.value + city.value) / 2
        existing.cities.push(city)
      }
    } else if (provinceName === '澳门') {
      const fullName = '澳门特别行政区'
      if (!provinceMap.has(fullName)) {
        provinceMap.set(fullName, { name: fullName, value: city.value, cities: [city] })
      } else {
        const existing = provinceMap.get(fullName)
        existing.value = (existing.value + city.value) / 2
        existing.cities.push(city)
      }
    } else if (provinceName === '台北' || provinceName === '台湾') {
      const fullName = '台湾省'
      if (!provinceMap.has(fullName)) {
        provinceMap.set(fullName, { name: fullName, value: city.value, cities: [city] })
      } else {
        const existing = provinceMap.get(fullName)
        existing.value = (existing.value + city.value) / 2
        existing.cities.push(city)
      }
    } else {
      const provinceFullName = provinceName + '省'
      if (!provinceMap.has(provinceFullName)) {
        provinceMap.set(provinceFullName, { name: provinceFullName, value: city.value, cities: [city] })
      } else {
        const existing = provinceMap.get(provinceFullName)
        existing.value = (existing.value + city.value) / 2
        existing.cities.push(city)
      }
    }
  })
  
  return Array.from(provinceMap.values())
}

const getChartOption = (geoJson, data) => {
  const config = currentLayerConfig.value
  
  const isProvinceView = currentLevel.value === 'province'
  
  const mapData = isProvinceView 
    ? data.map(city => ({
        name: city.name,
        value: city.value
      }))
    : aggregateDataByProvince(data).map(province => ({
        name: province.name,
        value: province.value
      }))

  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'transparent',
      borderColor: 'transparent',
      borderWidth: 0,
      padding: 0,
      extraCssText: 'box-shadow: 0 10px 40px rgba(0,0,0,0.2); border-radius: 12px; overflow: hidden;',
      formatter: (params) => {
        if (params.componentType === 'series') {
          if (isProvinceView) {
            const city = data.find(c => c.name === params.name)
            if (city) {
              const config = currentLayerConfig.value
              const levelLabel = getLevelLabel(city.value)
              const valueColor = getValueColor(city.value)
              const now = new Date()
              const updateTime = now.toLocaleString('zh-CN', { 
                year: 'numeric', 
                month: '2-digit', 
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
              })
            
            return `
              <div style="width: 280px; background: linear-gradient(135deg, rgba(255,255,255,0.99) 0%, rgba(248,250,252,0.99) 100%); border-radius: 16px; overflow: hidden; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; box-shadow: 0 12px 40px rgba(0,0,0,0.18); border: 1px solid rgba(226,232,240,0.8);">
                <div style="background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%); padding: 16px 20px; color: white; position: relative; overflow: hidden;">
                  <div style="position: absolute; top: -20px; right: -20px; width: 80px; height: 80px; background: rgba(255,255,255,0.1); border-radius: 50%;"></div>
                  <div style="position: absolute; bottom: -30px; left: -10px; width: 60px; height: 60px; background: rgba(255,255,255,0.08); border-radius: 50%;"></div>
                  <div style="font-weight: 700; font-size: 19px; display: flex; align-items: center; gap: 10px; position: relative; z-index: 1;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                      <circle cx="12" cy="10" r="3"/>
                    </svg>
                    ${city.name}
                  </div>
                  ${city.province ? `<div style="font-size: 14px; opacity: 0.9; margin-top: 6px; position: relative; z-index: 1;">📍 ${city.province}</div>` : ''}
                </div>
                
                <div style="padding: 20px;">
                  <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;">
                    <div>
                      <div style="font-size: 38px; font-weight: 800; color: ${valueColor}; line-height: 1; letter-spacing: -1px;">
                        ${city.value !== null && city.value !== undefined ? city.value : '--'}<span style="font-size: 18px; font-weight: 500; color: #64748b; margin-left: 3px;">${config.unit}</span>
                      </div>
                      <div style="font-size: 14px; color: #64748b; margin-top: 6px; font-weight: 500;">${config.name}</div>
                    </div>
                    <div style="text-align: center; padding: 10px 16px; background: linear-gradient(135deg, ${valueColor}15 0%, ${valueColor}25 100%); border-radius: 12px; border: 1px solid ${valueColor}30;">
                      <div style="font-size: 13px; font-weight: 600; color: ${valueColor};">等级</div>
                      <div style="font-size: 16px; font-weight: 700; color: ${valueColor}; margin-top: 4px;">${levelLabel}</div>
                    </div>
                  </div>
                  
                  ${city.weather ? `
                  <div style="display: flex; align-items: center; gap: 10px; padding: 12px; background: linear-gradient(90deg, #f8fafc 0%, #f1f5f9 100%); border-radius: 10px; margin-bottom: 12px; border-left: 3px solid #3b82f6;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2">
                      <path d="M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z"/>
                    </svg>
                    <div style="flex: 1;">
                      <div style="color: #475569; font-size: 13px; font-weight: 600;">天气状况</div>
                      <div style="color: #1e293b; font-size: 15px; font-weight: 600; margin-top: 2px;">${city.weather}</div>
                    </div>
                  </div>
                  ` : ''}
                  
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; padding: 12px; background: #fafbfc; border-radius: 10px;">
                    ${city.humidity !== null && city.humidity !== undefined ? `
                    <div style="display: flex; flex-direction: column; align-items: center; padding: 8px; background: white; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#0ea5e9" stroke-width="2">
                        <path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/>
                      </svg>
                      <span style="color: #0ea5e9; font-size: 16px; font-weight: 700; margin: 4px 0 2px;">${city.humidity}%</span>
                      <span style="color: #64748b; font-size: 11px; font-weight: 500;">湿度</span>
                    </div>
                    ` : ''}
                    ${city.wind ? `
                    <div style="display: flex; flex-direction: column; align-items: center; padding: 8px; background: white; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2">
                        <path d="M17.7 7.7a2.5 2.5 0 1 1 1.8 4.3H2"/>
                        <path d="M9.6 4.6A2 2 0 1 1 11 8H2"/>
                        <path d="M12.6 19.4A2 2 0 1 0 14 16H2"/>
                      </svg>
                      <span style="color: #22c55e; font-size: 14px; font-weight: 700; margin: 4px 0 2px;">${city.wind}</span>
                      <span style="color: #64748b; font-size: 11px; font-weight: 500;">风力</span>
                    </div>
                    ` : ''}
                    ${city.latitude && city.longitude ? `
                    <div style="display: flex; flex-direction: column; align-items: center; padding: 8px; background: white; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); grid-column: span 2;">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2">
                        <circle cx="12" cy="12" r="10"/>
                        <path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                      </svg>
                      <span style="color: #f59e0b; font-size: 13px; font-weight: 600; margin: 4px 0 2px;">${city.latitude.toFixed(2)}°N, ${city.longitude.toFixed(2)}°E</span>
                      <span style="color: #64748b; font-size: 11px; font-weight: 500;">坐标位置</span>
                    </div>
                    ` : ''}
                  </div>
                  
                  <div style="margin-top: 12px; padding-top: 12px; border-top: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: space-between;">
                    <div style="display: flex; align-items: center; gap: 6px; color: #94a3b8; font-size: 12px;">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"/>
                        <polyline points="12 6 12 12 16 14"/>
                      </svg>
                      更新时间
                    </div>
                    <div style="color: #64748b; font-size: 12px; font-weight: 500;">${updateTime}</div>
                  </div>
                </div>
              </div>
            `}
          } else {
            const provinceData = aggregateDataByProvince(data).find(p => p.name === params.name)
            if (provinceData) {
              const config = currentLayerConfig.value
              const levelLabel = getLevelLabel(provinceData.value)
              const valueColor = getValueColor(provinceData.value)
              
              return `
                <div style="width: 240px; background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(248,250,252,0.95) 100%); border-radius: 12px; overflow: hidden; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;">
                  <div style="background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%); padding: 12px 16px; color: white;">
                    <div style="font-weight: 600; font-size: 16px; display: flex; align-items: center; gap: 8px;">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                        <circle cx="12" cy="10" r="3"/>
                      </svg>
                      ${provinceData.name}
                    </div>
                  </div>
                  <div style="padding: 16px;">
                    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;">
                      <div>
                        <div style="font-size: 28px; font-weight: 700; color: ${valueColor};">
                          ${provinceData.value !== null && provinceData.value !== undefined ? provinceData.value.toFixed(1) : '--'}<span style="font-size: 14px; font-weight: 400; color: #64748b;">${config.unit}</span>
                        </div>
                        <div style="font-size: 12px; color: #64748b; margin-top: 2px;">${config.name} (平均)</div>
                      </div>
                      <div style="background: ${valueColor}20; color: ${valueColor}; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500;">
                        ${levelLabel}
                      </div>
                    </div>
                    <div style="padding: 8px 0; border-top: 1px solid #e2e8f0;">
                      <div style="font-size: 12px; color: #64748b; margin-bottom: 4px;">包含城市: ${provinceData.cities.length}个</div>
                      <div style="font-size: 11px; color: #94a3b8;">${provinceData.cities.map(c => c.name).join('、')}</div>
                    </div>
                  </div>
                </div>
              `
            }
          }
        }
        return params.name
      }
    },
    visualMap: {
      type: 'continuous',
      min: config.min,
      max: config.max,
      seriesIndex: 0,
      text: ['高', '低'],
      textStyle: {
        color: '#94a3b8'
      },
      inRange: {
        color: config.colors
      },
      calculable: true,
      orient: 'vertical',
      left: 20,
      bottom: 20,
      itemWidth: 12,
      itemHeight: 140,
      formatter: (value) => `${value}${config.unit}`
    },
    series: [
      {
        name: currentLayerConfig.value?.name || '数据',
        type: 'map',
        map: 'china',
        roam: true,
        zoom: isProvinceView ? 1.5 : 1.2,
        center: isProvinceView ? undefined : [104.5, 36],
        scaleLimit: {
          min: 0.8,
          max: 8
        },
        data: mapData,
        selectedMode: 'single',
        label: {
          show: true,
          color: '#475569',
          fontSize: isProvinceView ? 11 : 10,
          formatter: (params) => {
            if (isProvinceView) {
              if (params.name.length > 3) {
                return params.name.slice(0, 2)
              }
              return params.name
            } else {
              const name = params.name
              if (name.includes('特别行政区')) {
                return name.replace('特别行政区', '')
              } else if (name.includes('自治区')) {
                const match = name.match(/(.+?)自治区/)
                return match ? match[1].slice(0, 2) : name.slice(0, 2)
              } else if (name.includes('省')) {
                return name.replace('省', '')
              } else if (name.includes('市')) {
                return name.replace('市', '')
              }
              return name
            }
          }
        },
        emphasis: {
          label: {
            show: true,
            color: '#0f172a',
            fontSize: 12,
            fontWeight: 'bold'
          },
          itemStyle: {
            areaColor: '#dbeafe',
            shadowBlur: 20,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        itemStyle: {
          borderColor: '#cbd5e1',
          borderWidth: 1
        },
        select: {
          label: {
            show: true,
            color: '#0f172a'
          },
          itemStyle: {
            areaColor: '#bfdbfe'
          }
        }
      }
    ]
  }
}

const initChart = async () => {
  if (!chartRef.value) {
    console.error('初始化地图失败: chartRef未绑定到DOM元素')
    return
  }
  
  loading.value = true
  error.value = null
  
  try {
    const geoJson = await loadChinaGeoJson()
    echarts.registerMap('china', geoJson)
    
    if (useDistrictLevel.value) {
      loadDistrictGeoJson().catch(err => {
        console.warn('预加载区县数据失败:', err)
      })
    }
    
    const data = await fetchWeatherData()
    
    if (!chartRef.value) {
      console.error('初始化地图失败: DOM元素已卸载')
      return
    }
    
    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value, null, {
        renderer: 'canvas'
      })
    }
    
    const option = getChartOption(geoJson, data)
    chartInstance.setOption(option, true)
    
    chartInstance.on('click', async (params) => {
      if (params.componentType === 'series' && currentLevel.value === 'country') {
        const provinceName = params.name
        const adcode = provinceAdcodeMap[provinceName]
        
        if (adcode) {
          await drillDown(provinceName, adcode)
        }
      }
    })
    
    chartInstance.on('mouseover', (params) => {
      if (params.componentType === 'series') {
        const city = weatherData.value.find(c => c.name === params.name)
        if (city) {
          hoveredCity.value = city
        }
      }
    })
    
    chartInstance.on('mouseout', () => {
      hoveredCity.value = null
    })
    
  } catch (err) {
    console.error('初始化地图失败:', err)
    error.value = '加载地图数据失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const drillDown = async (provinceName, adcode) => {
  if (adcode === '710000') {
    error.value = '台湾省地图数据暂不可用'
    setTimeout(() => {
      error.value = null
    }, 3000)
    return
  }
  
  loading.value = true
  error.value = null
  
  try {
    console.log(`开始下钻到省份: ${provinceName}, adcode: ${adcode}`)
    
    const geoJson = await loadProvinceGeoJson(adcode)
    echarts.registerMap('china', geoJson)
    
    const { center, zoom } = calculateGeoCenter(geoJson)
    
    currentLevel.value = 'province'
    currentRegionCode.value = adcode
    currentRegionName.value = provinceName
    
    chartInstance.setOption({
      series: [{
        data: [],
        animation: false
      }]
    }, {
      lazyUpdate: true,
      silent: true
    })
    
    chartInstance.setOption({
      series: [{
        center: center,
        zoom: zoom
      }]
    }, {
      lazyUpdate: false,
      silent: false
    })
    
    const response = await mapApi.getDistrictWeather(adcode, currentLayer.value)
    if (response.data && response.data.cities) {
      const provinceData = response.data.cities

      console.log(`📍 省份: ${provinceName}, adcode: ${adcode}, 城市数: ${provinceData.length}`)
      console.log('📊 城市数据示例:', provinceData.slice(0, 3))

      weatherData.value = provinceData

      const geoJson = await loadProvinceGeoJson(adcode)
      console.log('🗺️ GeoJSON加载成功, 特征数:', geoJson.features?.length)

      const option = getChartOption(geoJson, provinceData)

      console.log('🎨 visualMap配置:', {
        min: option.visualMap?.min,
        max: option.visualMap?.max,
        colors: option.visualMap?.inRange?.color,
        seriesIndex: option.visualMap?.seriesIndex
      })

      console.log('📈 地图数据 (前5个):', option.series?.[0]?.data?.slice(0, 5))

      chartInstance.setOption(option, {
        animation: true,
        animationDuration: 500,
        animationEasing: 'cubicOut'
      })

      console.log('✅ 地图配置已应用')
    }
    
  } catch (err) {
    console.error('下钻失败:', err)
    error.value = '加载省级数据失败'
    
    currentLevel.value = 'country'
    currentRegionCode.value = '100000'
    currentRegionName.value = ''
  } finally {
    loading.value = false
  }
}

const goBack = async () => {
  currentLevel.value = 'country'
  currentRegionCode.value = '100000'
  currentRegionName.value = ''
  
  await initChart()
}

const switchLayer = async (layerCode) => {
  if (currentLayer.value === layerCode) return
  
  currentLayer.value = layerCode
  await initChart()
}

const refreshData = async () => {
  if (!canRefresh.value) {
    return
  }
  
  try {
    const statusResponse = await mapApi.getRefreshStatus(currentRegionCode.value)
    if (statusResponse.data && !statusResponse.data.canRefresh) {
      canRefresh.value = false
      refreshMessage.value = statusResponse.data.message
      refreshCooldown.value = statusResponse.data.remainingSeconds
      
      startRefreshTimer()
      return
    }
    
    canRefresh.value = false
    loading.value = true
    
    const response = await mapApi.refreshDistrictWeather(currentRegionCode.value, currentLayer.value)
    
    if (response.data && response.data.cities) {
      weatherData.value = response.data.cities
      lastUpdate.value = response.data.updateTime || Date.now()
      
      const mapData = weatherData.value.map(city => ({
        name: city.name,
        value: city.value
      }))
      
      const scatterData = weatherData.value
        .filter(city => city.longitude && city.latitude)
        .map(city => ({
          name: city.name,
          value: [city.longitude, city.latitude, city.value]
        }))
      
      if (chartInstance) {
        chartInstance.setOption({
          series: [
            { data: mapData },
            { data: scatterData }
          ]
        })
      }
    }
    
    refreshMessage.value = '数据已刷新'
    startRefreshTimer()
    
  } catch (err) {
    console.error('刷新数据失败:', err)
    error.value = '刷新数据失败，请稍后重试'
    canRefresh.value = true
  } finally {
    loading.value = false
  }
}

const startRefreshTimer = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  
  refreshCooldown.value = 180
  
  refreshTimer = setInterval(() => {
    refreshCooldown.value--
    
    if (refreshCooldown.value <= 0) {
      clearInterval(refreshTimer)
      refreshTimer = null
      canRefresh.value = true
      refreshMessage.value = ''
      refreshCooldown.value = 0
    }
  }, 1000)
}

const checkRefreshStatus = async () => {
  try {
    const response = await mapApi.getRefreshStatus(currentRegionCode.value)
    if (response.data) {
      canRefresh.value = response.data.canRefresh
      refreshCooldown.value = response.data.remainingSeconds || 0
      refreshMessage.value = response.data.message || ''
      
      if (!canRefresh.value && refreshCooldown.value > 0) {
        startRefreshTimer()
      }
    }
  } catch (err) {
    console.error('检查刷新状态失败:', err)
  }
}

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(async () => {
  await nextTick()
  initChart()
  checkRefreshStatus()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', handleResize)
})

watch(currentLayer, () => {
  nextTick(() => {
    if (chartInstance) {
      initChart()
    }
  })
})
</script>

<style scoped>
.weather-map-page {
  min-height: 100vh;
  padding: 1.5rem;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  margin-bottom: 1.5rem;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.page-title svg {
  color: var(--blue-500);
}

.current-level {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 1rem;
  font-weight: 400;
  color: var(--text-secondary);
}

.update-time {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.btn-back, .btn-refresh {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-back {
  background: rgba(148, 163, 184, 0.2);
  color: var(--text-primary);
}

.btn-back:hover {
  background: rgba(148, 163, 184, 0.3);
}

.btn-refresh {
  background: var(--blue-500);
  color: white;
}

.btn-refresh:hover:not(:disabled) {
  background: var(--blue-600);
}

.btn-refresh:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.refresh-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.25rem;
}

.refresh-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.5rem;
  background: rgba(251, 191, 36, 0.1);
  border-radius: 0.375rem;
  font-size: 0.75rem;
}

.cooldown-text {
  color: #d97706;
}

.cooldown-timer {
  font-weight: 600;
  color: #b45309;
  background: rgba(251, 191, 36, 0.2);
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.map-container {
  display: grid;
  grid-template-columns: 240px 1fr 280px;
  gap: 1.5rem;
  height: calc(100vh - 180px);
}

.layer-panel {
  padding: 1rem;
  height: fit-content;
}

.panel-header h3 {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 1rem 0;
}

.panel-header svg {
  color: var(--blue-500);
}

.layer-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.layer-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.layer-btn:hover {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(59, 130, 246, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.layer-btn.active {
  background: rgba(255, 255, 255, 1);
  border-color: var(--blue-500);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.layer-icon {
  width: 24px;
  height: 24px;
  border-radius: 0.375rem;
  flex-shrink: 0;
}

.layer-name {
  flex: 1;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
}

.layer-unit {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.map-main {
  position: relative;
  border-radius: 1rem;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.chart-container {
  width: 100%;
  height: 100%;
}

.map-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  z-index: 10;
}

.map-overlay svg {
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

.map-overlay p {
  color: var(--text-secondary);
  margin: 0;
}

.error-overlay svg {
  color: #ef4444;
}

.btn-retry {
  margin-top: 1rem;
  padding: 0.5rem 1.5rem;
  background: var(--blue-500);
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  cursor: pointer;
  transition: background 0.2s ease;
}

.btn-retry:hover {
  background: var(--blue-600);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(59, 130, 246, 0.2);
  border-top-color: var(--blue-500);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

.info-panel {
  padding: 1rem;
  height: fit-content;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.info-header h4 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.info-value {
  font-size: 1.25rem;
  font-weight: 700;
}

.info-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.info-data {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-primary);
}

@media (max-width: 1200px) {
  .map-container {
    grid-template-columns: 200px 1fr;
  }
  
  .info-panel {
    display: none;
  }
}

@media (max-width: 768px) {
  .weather-map-page {
    padding: 1rem;
  }
  
  .map-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .map-container {
    grid-template-columns: 1fr;
    height: auto;
  }
  
  .layer-panel {
    order: 1;
  }
  
  .map-main {
    order: 2;
    height: 400px;
  }
  
  .layer-list {
    flex-direction: row;
    flex-wrap: wrap;
  }
  
  .layer-btn {
    flex: 1;
    min-width: calc(50% - 0.25rem);
  }
}

@media (prefers-color-scheme: dark) {
  .layer-btn {
    background: rgba(30, 41, 59, 0.75);
    border-color: rgba(71, 85, 105, 0.3);
  }
  
  .layer-btn:hover {
    background: rgba(30, 41, 59, 0.85);
    border-color: rgba(59, 130, 246, 0.5);
  }
  
  .layer-btn.active {
    background: rgba(59, 130, 246, 0.2);
  }
  
  .map-main {
    background: rgba(30, 41, 59, 0.75);
    border-color: rgba(71, 85, 105, 0.3);
  }
  
  .map-overlay {
    background: rgba(15, 23, 42, 0.9);
  }
}
</style>
