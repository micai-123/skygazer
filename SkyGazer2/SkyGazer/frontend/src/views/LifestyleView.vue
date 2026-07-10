<template>
  <div class="lifestyle-view">
    <Breadcrumb current="生活指数" />
    
    <div class="lifestyle-header">
      <h2 class="page-title">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
        </svg>
        生活指数
      </h2>
      <p class="page-desc">基于实时气象数据的专业生活指导建议</p>
    </div>
    
    <div class="lifestyle-index-grid">
      <div 
        v-for="(index, idx) in lifestyleIndices" 
        :key="idx"
        class="index-card glass-card"
      >
        <div class="index-header">
          <div class="index-icon" :class="index.iconClass" v-html="index.icon"></div>
          <div class="index-info">
            <h3 class="index-name">{{ index.name }}</h3>
            <div class="index-level">
              <span class="level-badge" :class="index.levelClass">{{ index.level }}</span>
              <span class="level-value" v-if="index.value">{{ index.value }}</span>
            </div>
          </div>
        </div>
        
        <div class="index-bar">
          <div class="bar-fill" :class="index.levelClass" :style="{ width: index.percentage + '%' }"></div>
          <div class="bar-labels">
            <span>{{ index.rangeMin }}</span>
            <span>{{ index.rangeMax }}</span>
          </div>
        </div>
        
        <p class="index-suggestion">{{ index.suggestion }}</p>
        
        <button class="detail-btn" @click="openDetailModal(index)">
          查看详情
        </button>
      </div>
    </div>
    
    <div class="lifestyle-tips">
      <div class="tip-card glass-card">
        <div class="tip-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2Z" fill="currentColor" opacity="0.2"/>
            <path d="M12 16V12M12 8H12.01M22 12C22 17.52 17.52 22 12 22C6.48 22 2 17.52 2 12C2 6.48 6.48 2 12 2C17.52 2 22 6.48 22 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="tip-content">
          <h4>温馨提示</h4>
          <p>生活指数基于实时气象数据计算得出，仅供参考。实际感受可能因个人体质、活动强度等因素有所差异，请结合实际情况合理安排活动。</p>
        </div>
      </div>
    </div>
    
    <transition name="fade">
      <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
        <div class="modal-container">
          <div class="modal-header">
            <div class="modal-title-wrapper">
              <div class="modal-icon" :class="selectedIndex?.iconClass" v-html="selectedIndex?.icon"></div>
              <div>
                <h3 class="modal-title">{{ selectedIndex?.name }}</h3>
                <div class="modal-level">
                  <span class="level-badge" :class="selectedIndex?.levelClass">{{ selectedIndex?.level }}</span>
                  <span class="level-value" v-if="selectedIndex?.value">{{ selectedIndex?.value }}</span>
                </div>
              </div>
            </div>
            <button class="modal-close" @click="closeDetailModal">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          
          <div class="modal-body">
            <div class="modal-section">
              <div class="section-header">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 16V12M12 8H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <h4>指数说明</h4>
              </div>
              <p class="section-text">{{ selectedIndex?.description }}</p>
            </div>
            
            <div class="modal-section" v-if="selectedIndex?.levels && selectedIndex.levels.length">
              <div class="section-header">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <path d="M9 19V6L12 3L15 6V19C15 20.1 14.1 21 13 21H11C9.9 21 9 20.1 9 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <h4>等级划分</h4>
              </div>
              <div class="level-cards">
                <div 
                  v-for="(level, lidx) in selectedIndex.levels" 
                  :key="lidx"
                  class="level-card"
                  :class="{ 'active': level.name === selectedIndex.level }"
                >
                  <div class="level-card-header">
                    <span class="level-card-name">{{ level.name }}</span>
                    <span class="level-card-range">{{ level.range }}</span>
                  </div>
                  <div class="level-card-indicator" v-if="level.name === selectedIndex.level">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                      <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    当前等级
                  </div>
                </div>
              </div>
            </div>
            
            <div class="modal-section">
              <div class="section-header">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <path d="M9 12L11 14L15 10M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <h4>行动指南</h4>
              </div>
              <div class="action-cards">
                <div 
                  v-for="(action, aidx) in selectedIndex?.actions" 
                  :key="aidx"
                  class="action-card"
                >
                  <div class="action-number">{{ aidx + 1 }}</div>
                  <p>{{ action }}</p>
                </div>
              </div>
            </div>
            
            <div class="modal-section" v-if="selectedIndex?.scenarios && selectedIndex.scenarios.length">
              <div class="section-header">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <path d="M17 21V19C17 16.79 15.21 15 13 15H5C2.79 15 1 16.79 1 19V21M23 21V19C22.99 17.13 21.8 15.5 20 15M16 3.13C17.79 3.48 19.08 5.07 19.08 6.94C19.08 8.81 17.79 10.4 16 10.75M9 7C11.21 7 13 5.21 13 3C13 0.79 11.21 -1 9 -1C6.79 -1 5 0.79 5 3C5 5.21 6.79 7 9 7Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" transform="translate(0, 2)"/>
                </svg>
                <h4>适用场景</h4>
              </div>
              <div class="scenario-cards">
                <div 
                  v-for="(scenario, sidx) in selectedIndex.scenarios" 
                  :key="sidx"
                  class="scenario-card"
                >
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                    <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke="currentColor" stroke-width="2"/>
                    <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span>{{ scenario }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Breadcrumb from '@/components/common/Breadcrumb.vue'

const showDetailModal = ref(false)
const selectedIndex = ref(null)

function openDetailModal(index) {
  selectedIndex.value = index
  showDetailModal.value = true
}

function closeDetailModal() {
  showDetailModal.value = false
  selectedIndex.value = null
}

const lifestyleIndices = ref([
  {
    name: '紫外线指数',
    level: '中等',
    levelClass: 'warning',
    value: 'UV 5',
    iconClass: 'uv-icon',
    percentage: 60,
    rangeMin: '最弱',
    rangeMax: '极强',
    suggestion: '建议涂抹SPF30防晒霜，佩戴太阳镜',
    description: '紫外线指数是衡量太阳紫外线辐射强度的指标，分为5个等级。紫外线过强会对皮肤和眼睛造成伤害，增加皮肤癌风险，适当的防护措施可以有效降低紫外线对人体的危害。',
    levels: [
      { name: '最弱', range: '0-2' },
      { name: '弱', range: '3-4' },
      { name: '中等', range: '5-6' },
      { name: '强', range: '7-9' },
      { name: '极强', range: '10+' }
    ],
    actions: [
      '外出时涂抹SPF30以上防晒霜',
      '佩戴防紫外线太阳镜',
      '尽量避免10:00-16:00时段外出',
      '穿着长袖衣物或使用遮阳伞'
    ],
    scenarios: ['户外运动', '海边度假', '登山徒步', '日常通勤'],
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="5" fill="currentColor"/><path d="M12 1V3M12 21V23M4.22 4.22L5.64 5.64M18.36 18.36L19.78 19.78M1 12H3M21 12H23M4.22 19.78L5.64 18.36M18.36 5.64L19.78 4.22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>'
  },
  {
    name: '舒适度指数',
    level: '舒适',
    levelClass: 'good',
    value: '85分',
    iconClass: 'comfort-icon',
    percentage: 85,
    rangeMin: '极不舒适',
    rangeMax: '非常舒适',
    suggestion: '天气舒适，适合各类户外活动',
    description: '舒适度指数综合评估温度、湿度、风速等气象要素对人体舒适感的影响。指数越高表示人体感觉越舒适，适合进行各类户外活动；指数较低时可能会感到闷热或寒冷，需要注意调节。',
    levels: [
      { name: '极不舒适', range: '0-20' },
      { name: '不舒适', range: '21-40' },
      { name: '一般', range: '41-60' },
      { name: '舒适', range: '61-80' },
      { name: '非常舒适', range: '81-100' }
    ],
    actions: [
      '当前天气条件非常适宜户外活动',
      '可以安排郊游、野餐等户外活动',
      '注意适时补充水分',
      '早晚温差较大时注意增减衣物'
    ],
    scenarios: ['户外休闲', '运动健身', '旅游出行', '日常活动'],
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none"><path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2Z" fill="currentColor" opacity="0.3"/><path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 20C7.59 20 4 16.41 4 12C4 7.59 7.59 4 12 4C16.41 4 20 7.59 20 12C20 16.41 16.41 20 12 20Z" fill="currentColor"/><path d="M12 17C14.21 17 16 15.21 16 13H8C8 15.21 9.79 17 12 17Z" fill="currentColor"/></svg>'
  },
  {
    name: '穿衣指数',
    level: '薄款',
    levelClass: 'good',
    value: '18-25°C',
    iconClass: 'dress-icon',
    percentage: 70,
    rangeMin: '严寒',
    rangeMax: '炎热',
    suggestion: '建议穿着薄款长袖或短袖，早晚可添外套',
    description: '穿衣指数根据气温、湿度、风速等气象条件，为不同人群提供穿衣建议。选择合适的衣物不仅能保持舒适，还能有效预防感冒等疾病的发生。',
    levels: [
      { name: '严寒', range: '羽绒服' },
      { name: '寒冷', range: '棉衣+毛衣' },
      { name: '较冷', range: '厚外套' },
      { name: '适中', range: '薄款外套' },
      { name: '薄款', range: '长袖/短袖' },
      { name: '炎热', range: '短袖/薄裙' }
    ],
    actions: [
      '白天可穿短袖或薄长袖',
      '早晚外出建议携带薄外套',
      '选择透气性好的面料',
      '根据个人体质适当调整'
    ],
    scenarios: ['日常通勤', '商务出行', '休闲活动', '约会聚会'],
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none"><path d="M21 3H3V7L6 10V21H18V10L21 7V3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M6 3V10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M18 3V10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>'
  },
  {
    name: '运动指数',
    level: '适宜',
    levelClass: 'good',
    value: 'A级',
    iconClass: 'sport-icon',
    percentage: 90,
    rangeMin: '不宜',
    rangeMax: '极适宜',
    suggestion: '天气晴好，非常适合户外运动',
    description: '运动指数综合评估气象条件对户外运动的影响，包括温度、湿度、风速、空气质量等因素。适宜的运动天气有助于提高运动效果，降低运动风险。',
    levels: [
      { name: '不宜', range: 'E级' },
      { name: '较不宜', range: 'D级' },
      { name: '一般', range: 'C级' },
      { name: '适宜', range: 'B级' },
      { name: '极适宜', range: 'A级' }
    ],
    actions: [
      '非常适合进行各类户外运动',
      '建议选择跑步、骑行、球类运动',
      '运动前做好热身准备',
      '及时补充水分，避免中暑'
    ],
    scenarios: ['晨跑夜跑', '球类运动', '骑行健身', '户外瑜伽'],
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="5" r="3" fill="currentColor"/><path d="M12 8V14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M8 22L12 14L16 22" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M7 11H17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>'
  },
  {
    name: '洗车指数',
    level: '适宜',
    levelClass: 'good',
    value: '4天无雨',
    iconClass: 'carwash-icon',
    percentage: 95,
    rangeMin: '不宜',
    rangeMax: '极适宜',
    suggestion: '未来两天无雨，适合洗车',
    description: '洗车指数根据未来天气情况预测是否适合洗车。考虑因素包括降水概率、空气质量、风力等。选择合适的洗车时机可以保持车辆清洁更持久。',
    levels: [
      { name: '不宜', range: '未来1天有雨' },
      { name: '较不宜', range: '未来2天有雨' },
      { name: '较适宜', range: '未来3天无雨' },
      { name: '适宜', range: '未来4天无雨' },
      { name: '极适宜', range: '未来5天无雨' }
    ],
    actions: [
      '未来几天天气晴好，适合洗车',
      '建议选择正规洗车店',
      '洗车后可做打蜡保养',
      '注意检查雨刮器状态'
    ],
    scenarios: ['日常保养', '长途出行前', '车辆美容', '季节更替'],
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none"><path d="M5 11L7 5H17L19 11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M3 11H21V17H3V11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><circle cx="7" cy="17" r="2" stroke="currentColor" stroke-width="2"/><circle cx="17" cy="17" r="2" stroke="currentColor" stroke-width="2"/></svg>'
  },
  {
    name: '过敏指数',
    level: '中等',
    levelClass: 'warning',
    value: '花粉浓度中等',
    iconClass: 'allergy-icon',
    percentage: 50,
    rangeMin: '极低',
    rangeMax: '极高',
    suggestion: '花粉浓度中等，过敏体质请注意防护',
    description: '过敏指数综合评估空气中花粉、霉菌、尘螨等过敏原的浓度，为过敏体质人群提供防护建议。春季和秋季是过敏高发季节，需要特别注意防护。',
    levels: [
      { name: '极低', range: '无需防护' },
      { name: '低', range: '基本无影响' },
      { name: '中等', range: '需适当防护' },
      { name: '较高', range: '加强防护' },
      { name: '极高', range: '避免外出' }
    ],
    actions: [
      '外出佩戴口罩和眼镜',
      '回家后及时清洗面部和鼻腔',
      '关闭门窗，使用空气净化器',
      '过敏体质者随身携带抗过敏药物'
    ],
    scenarios: ['春季赏花', '秋季出行', '户外活动', '居家生活'],
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none"><path d="M12 2C13.1 2 14 2.9 14 4V12C15.21 12.81 16 14.17 16 15.71C16 18.1 14.21 20 12 20C9.79 20 8 18.1 8 15.71C8 14.17 8.79 12.81 10 12V4C10 2.9 10.9 2 12 2Z" fill="currentColor"/><circle cx="6" cy="6" r="2" fill="currentColor" opacity="0.5"/><circle cx="18" cy="8" r="1.5" fill="currentColor" opacity="0.5"/><circle cx="5" cy="12" r="1" fill="currentColor" opacity="0.5"/></svg>'
  },
  {
    name: '旅游指数',
    level: '极佳',
    levelClass: 'excellent',
    value: '晴天',
    iconClass: 'travel-icon',
    percentage: 92,
    rangeMin: '一般',
    rangeMax: '极佳',
    suggestion: '天气晴朗，非常适合外出旅游',
    description: '旅游指数综合评估天气条件对旅游活动的影响，包括温度、降水、能见度等因素。选择合适的旅游天气可以提升旅游体验，确保旅途安全。',
    levels: [
      { name: '一般', range: '天气欠佳' },
      { name: '较适宜', range: '天气尚可' },
      { name: '适宜', range: '天气较好' },
      { name: '良好', range: '天气良好' },
      { name: '极佳', range: '天气完美' }
    ],
    actions: [
      '非常适合各类旅游活动',
      '建议提前预订景点门票',
      '做好防晒和补水准备',
      '可安排户外景点游览'
    ],
    scenarios: ['周末出游', '假期旅行', '景点打卡', '户外探险'],
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none"><path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  },
  {
    name: '空气质量',
    level: '良',
    levelClass: 'good',
    value: 'AQI 78',
    iconClass: 'air-icon',
    percentage: 78,
    rangeMin: '优',
    rangeMax: '严重污染',
    suggestion: '空气质量良好，可正常户外活动',
    description: '空气质量指数(AQI)是定量描述空气质量状况的无量纲指数，主要评估PM2.5、PM10、O3、NO2、SO2、CO等污染物浓度。空气质量直接影响人体健康，特别是呼吸系统。',
    levels: [
      { name: '优', range: '0-50' },
      { name: '良', range: '51-100' },
      { name: '轻度污染', range: '101-150' },
      { name: '中度污染', range: '151-200' },
      { name: '重度污染', range: '201-300' },
      { name: '严重污染', range: '300+' }
    ],
    actions: [
      '空气质量良好，可正常户外活动',
      '敏感人群应适当减少户外运动',
      '可开窗通风换气',
      '关注空气质量变化'
    ],
    scenarios: ['日常活动', '户外运动', '开窗通风', '儿童老人外出'],
    icon: '<svg width="28" height="28" viewBox="0 0 24 24" fill="none"><path d="M8 12H18C19.66 12 21 10.66 21 9C21 7.34 19.66 6 18 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M4 6H10C11.66 6 13 7.34 13 9C13 10.66 11.66 12 10 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M4 18H16C17.66 18 19 16.66 19 15C19 13.34 17.66 12 16 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>'
  }
])
</script>

<style scoped>
.lifestyle-view {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.lifestyle-header {
  margin-bottom: 0.5rem;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--blue-900);
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.page-title svg {
  color: var(--blue-500);
}

.page-desc {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.lifestyle-index-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem;
}

.index-card {
  padding: 1.25rem;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.index-card:hover {
  transform: translateY(-2px);
}

.index-card.expanded {
  cursor: default;
  z-index: 10;
}

.index-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  align-items: flex-start;
}

.index-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.index-icon.uv-icon {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.2) 0%, rgba(245, 158, 11, 0.2) 100%);
  color: #f59e0b;
}

.index-icon.comfort-icon {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.2) 0%, rgba(22, 163, 74, 0.2) 100%);
  color: #22c55e;
}

.index-icon.dress-icon {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(37, 99, 235, 0.2) 100%);
  color: #3b82f6;
}

.index-icon.sport-icon {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.2) 0%, rgba(147, 51, 234, 0.2) 100%);
  color: #a855f7;
}

.index-icon.carwash-icon {
  background: linear-gradient(135deg, rgba(34, 211, 238, 0.2) 0%, rgba(6, 182, 212, 0.2) 100%);
  color: #06b6d4;
}

.index-icon.allergy-icon {
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.2) 0%, rgba(249, 115, 22, 0.2) 100%);
  color: #f97316;
}

.index-icon.travel-icon {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(5, 150, 105, 0.2) 100%);
  color: #10b981;
}

.index-icon.air-icon {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2) 0%, rgba(79, 70, 229, 0.2) 100%);
  color: #6366f1;
}

.index-info {
  flex: 1;
  min-width: 0;
}

.index-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.375rem;
}

.index-level {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.level-badge {
  padding: 0.25rem 0.625rem;
  border-radius: 1rem;
  font-size: 0.75rem;
  font-weight: 600;
}

.level-badge.good {
  background: rgba(34, 197, 94, 0.15);
  color: #16a34a;
}

.level-badge.warning {
  background: rgba(245, 158, 11, 0.15);
  color: #d97706;
}

.level-badge.excellent {
  background: rgba(16, 185, 129, 0.15);
  color: #059669;
}

.level-value {
  font-size: 0.75rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.expand-icon {
  color: var(--text-muted);
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.index-card.expanded .expand-icon {
  transform: rotate(180deg);
}

.index-bar {
  margin-bottom: 0.75rem;
}

.index-bar .bar-fill {
  height: 6px;
  background: rgba(147, 197, 253, 0.3);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 0.25rem;
}

.index-bar .bar-fill::after {
  content: '';
  display: block;
  height: 100%;
  background: var(--blue-500);
  border-radius: 3px;
  transition: width 0.5s ease;
  width: var(--width, 0);
}

.index-bar .bar-fill.good::after {
  background: var(--green-500);
}

.index-bar .bar-fill.warning::after {
  background: #f59e0b;
}

.index-bar .bar-fill.excellent::after {
  background: #10b981;
}

.bar-labels {
  display: flex;
  justify-content: space-between;
  font-size: 0.625rem;
  color: var(--text-muted);
}

.index-suggestion {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.index-detail {
  position: absolute;
  left: 0;
  right: 0;
  top: 100%;
  margin-top: 0.5rem;
  padding: 1.25rem;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 0 0 16px 16px;
  border-top: 1px solid rgba(147, 197, 253, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  z-index: 20;
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  transform: translateY(0);
}

.detail-section {
  margin-bottom: 1rem;
}

.detail-section:last-child {
  margin-bottom: 0;
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

.detail-title svg {
  color: var(--blue-500);
}

.detail-text {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.6;
  padding-left: 1.25rem;
}

.level-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 0.5rem;
  padding-left: 1.25rem;
}

.level-item {
  padding: 0.5rem;
  background: rgba(147, 197, 253, 0.1);
  border-radius: 0.5rem;
  text-align: center;
  transition: all 0.2s ease;
}

.level-item.active {
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid var(--blue-300);
}

.level-name {
  display: block;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.125rem;
}

.level-range {
  display: block;
  font-size: 0.625rem;
  color: var(--text-muted);
}

.action-list {
  list-style: none;
  padding: 0;
  margin: 0;
  padding-left: 1.25rem;
}

.action-list li {
  position: relative;
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 0.25rem 0;
  padding-left: 1rem;
}

.action-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--blue-500);
}

.scenario-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  padding-left: 1.25rem;
}

.scenario-tag {
  padding: 0.25rem 0.75rem;
  background: rgba(147, 197, 253, 0.15);
  border-radius: 1rem;
  font-size: 0.75rem;
  color: #1a1a1a;
}

.lifestyle-tips {
  margin-top: 0.5rem;
}

.tip-card {
  display: flex;
  gap: 1rem;
  padding: 1.25rem;
}

.tip-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--blue-500);
  flex-shrink: 0;
}

.tip-content h4 {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.375rem;
}

.tip-content p {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.detail-btn {
  position: absolute;
  bottom: 1rem;
  right: 1rem;
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, var(--blue-500) 0%, var(--blue-600) 100%);
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.8125rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.detail-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.detail-btn:active {
  transform: translateY(0);
}

.modal-overlay {
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

.modal-container {
  background: white;
  border-radius: 1.5rem;
  width: 100%;
  max-width: 640px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid rgba(147, 163, 184, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, rgba(37, 99, 235, 0.05) 100%);
}

.modal-title-wrapper {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.modal-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 0.375rem 0;
}

.modal-level {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.modal-close {
  background: none;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s ease;
  border-radius: 0.5rem;
}

.modal-close:hover {
  background: rgba(148, 163, 184, 0.2);
  color: var(--text-primary);
}

.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
}

.modal-section {
  margin-bottom: 1.5rem;
}

.modal-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.section-header svg {
  color: var(--blue-500);
}

.section-header h4 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.section-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0;
  padding-left: 1.5rem;
}

.level-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 0.75rem;
  padding-left: 1.5rem;
}

.level-card {
  background: rgba(248, 250, 252, 1);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 0.75rem;
  padding: 0.875rem;
  transition: all 0.2s ease;
}

.level-card.active {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(37, 99, 235, 0.1) 100%);
  border-color: var(--blue-500);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.level-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.level-card-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.level-card-range {
  font-size: 0.75rem;
  color: var(--text-secondary);
  background: rgba(148, 163, 184, 0.2);
  padding: 0.125rem 0.5rem;
  border-radius: 0.375rem;
}

.level-card.active .level-card-range {
  background: rgba(59, 130, 246, 0.2);
  color: var(--blue-600);
}

.level-card-indicator {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.75rem;
  color: var(--blue-600);
  font-weight: 500;
}

.level-card-indicator svg {
  color: var(--blue-600);
}

.action-cards {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-left: 1.5rem;
}

.action-card {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.875rem;
  background: rgba(248, 250, 252, 1);
  border-radius: 0.75rem;
  border: 1px solid rgba(148, 163, 184, 0.15);
  transition: all 0.2s ease;
}

.action-card:hover {
  background: rgba(241, 245, 249, 1);
  border-color: rgba(148, 163, 184, 0.3);
}

.action-number {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--blue-500) 0%, var(--blue-600) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8125rem;
  font-weight: 600;
  flex-shrink: 0;
}

.action-card p {
  font-size: 0.875rem;
  color: var(--text-primary);
  line-height: 1.6;
  margin: 0;
  flex: 1;
}

.scenario-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
  padding-left: 1.5rem;
}

.scenario-card {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: rgba(248, 250, 252, 1);
  border-radius: 0.75rem;
  border: 1px solid rgba(148, 163, 184, 0.15);
  transition: all 0.2s ease;
}

.scenario-card:hover {
  background: rgba(241, 245, 249, 1);
  border-color: rgba(148, 163, 184, 0.3);
}

.scenario-card svg {
  color: var(--blue-500);
  flex-shrink: 0;
}

.scenario-card span {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 500;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-active .modal-container,
.fade-leave-active .modal-container {
  transition: transform 0.3s ease;
}

.fade-enter-from .modal-container,
.fade-leave-to .modal-container {
  transform: scale(0.95);
}

@media (max-width: 1024px) {
  .lifestyle-index-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 1.25rem;
  }
  
  .index-icon {
    width: 44px;
    height: 44px;
  }
  
  .modal-container {
    max-width: 100%;
    max-height: 85vh;
  }
  
  .modal-header {
    padding: 1.25rem;
  }
  
  .modal-body {
    padding: 1.25rem;
  }
  
  .level-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .scenario-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .index-header {
    flex-wrap: wrap;
  }
  
  .index-card {
    position: relative;
    padding-bottom: 3.5rem;
  }
  
  .detail-btn {
    bottom: 0.75rem;
    right: 0.75rem;
  }
  
  .level-cards {
    grid-template-columns: 1fr;
    padding-left: 0;
  }
  
  .action-cards {
    padding-left: 0;
  }
  
  .scenario-cards {
    padding-left: 0;
  }
  
  .section-text {
    padding-left: 0;
  }
  
  .tip-card {
    flex-direction: column;
  }
}
</style>
