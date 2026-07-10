import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { weatherApi } from '@/api'
import { getCityByCode } from '@/data/cities'

const WEATHER_ICON_MAP = {
  '晴': 'sun',
  '多云': 'cloud-sun',
  '阴': 'cloud',
  '小雨': 'rain',
  '中雨': 'rain',
  '大雨': 'rain',
  '暴雨': 'rain',
  '雷阵雨': 'thunder',
  '小雪': 'snow',
  '中雪': 'snow',
  '大雪': 'snow',
  '雾': 'fog',
  '霾': 'fog',
  '沙尘': 'wind',
  '大风': 'wind',
  'clear': 'sun',
  'cloudy': 'cloud',
  'overcast': 'cloud',
  'rain': 'rain',
  'snow': 'snow',
  'thunderstorm': 'thunder',
  'fog': 'fog',
  'wind': 'wind'
}

function formatHourlyTime(dateTimeStr, index) {
  try {
    let date
    if (typeof dateTimeStr === 'string') {
      date = new Date(dateTimeStr.replace('T', ' '))
    } else if (dateTimeStr instanceof Date) {
      date = dateTimeStr
    } else {
      date = new Date()
      date.setHours(date.getHours() + index)
    }
    
    if (isNaN(date.getTime())) {
      date = new Date()
      date.setHours(date.getHours() + index)
    }
    
    const now = new Date()
    const hourDiff = Math.round((date.getTime() - now.getTime()) / (1000 * 60 * 60))
    
    if (hourDiff <= 0) return '现在'
    if (hourDiff <= 24) return `${date.getHours().toString().padStart(2, '0')}:00`
    
    const month = date.getMonth() + 1
    const day = date.getDate()
    const hour = date.getHours().toString().padStart(2, '0')
    return `${month}/${day} ${hour}:00`
  } catch (e) {
    const fallbackDate = new Date()
    fallbackDate.setHours(fallbackDate.getHours() + index)
    return `${fallbackDate.getHours().toString().padStart(2, '0')}:00`
  }
}

function mapWeatherIcon(condition) {
  if (!condition) return 'sun'
  const normalized = condition.toLowerCase().trim()
  return WEATHER_ICON_MAP[normalized] || WEATHER_ICON_MAP[condition] || 'sun'
}

function validateHourlyData(data) {
  if (!data) return false
  if (!Array.isArray(data)) return false
  if (data.length === 0) return false
  return true
}

function transformHourlyData(rawData) {
  if (!validateHourlyData(rawData)) {
    console.warn('Invalid hourly data format, using fallback data')
    return null
  }
  
  const now = new Date()
  const currentHour = now.getHours()
  
  return rawData.map((item, index) => {
    const temp = item.temperature !== undefined && item.temperature !== null
      ? (typeof item.temperature === 'object' ? item.temperature : Number(item.temperature))
      : null
    
    const timeStr = item.time || item.recordTime || item.dateTime || null
    const formattedTime = formatHourlyTime(timeStr, index)
    
    const isCurrentHour = formattedTime === '现在' || 
      (timeStr && new Date(timeStr).getHours() === currentHour)
    
    return {
      time: formattedTime,
      temp: temp !== null && !isNaN(temp) ? Math.round(temp) : '--',
      icon: mapWeatherIcon(item.weatherCondition || item.condition || item.icon),
      highlight: isCurrentHour,
      precipitation: item.precipitation !== undefined ? Number(item.precipitation) : 0,
      windSpeed: item.windSpeed !== undefined ? Number(item.windSpeed) : 0,
      windDirection: item.windDirection || '',
      humidity: item.humidity !== undefined ? Number(item.humidity) : null
    }
  }).filter(item => item !== null)
}

function validateWeeklyData(data) {
  if (!data) return false
  if (!Array.isArray(data)) return false
  return true
}

function transformWeeklyData(rawData) {
  if (!validateWeeklyData(rawData)) {
    console.warn('Invalid weekly data format')
    return null
  }
  
  const weekdays = ['今天', '明天', '周三', '周四', '周五', '周六', '周日']
  
  return rawData.map((item, index) => {
    const maxTemp = item.maxTemp !== undefined ? Number(item.maxTemp) : 
                    item.high !== undefined ? Number(item.high) : null
    const minTemp = item.minTemp !== undefined ? Number(item.minTemp) : 
                    item.low !== undefined ? Number(item.low) : null
    
    let dayLabel = item.day || item.weekday || item.date
    if (!dayLabel || dayLabel.length > 3) {
      dayLabel = weekdays[index] || `第${index + 1}天`
    }
    
    return {
      day: dayLabel,
      high: maxTemp !== null && !isNaN(maxTemp) ? Math.round(maxTemp) : '--',
      low: minTemp !== null && !isNaN(minTemp) ? Math.round(minTemp) : '--',
      icon: mapWeatherIcon(item.weatherCondition || item.condition || item.icon),
      date: item.date || null
    }
  })
}

export const useWeatherStore = defineStore('weather', () => {
  const currentCity = ref({
    name: '上海',
    pinyin: 'shanghai',
    initial: 'S',
    province: '上海市',
    code: '101020100',
    lat: 31.23,
    lng: 121.47
  })

  const currentWeather = ref({
    location: '上海,浦东新区',
    temperature: 22,
    description: '清心晴朗',
    humidity: 42,
    wind: '东北风 3级',
    airQuality: 45,
    airQualityLevel: '良',
    highTemp: 24,
    lowTemp: 14
  })

  const hourlyForecast = ref([
    { time: '现在', temp: 22, icon: 'sun', highlight: false },
    { time: '16:00', temp: 23, icon: 'sun', highlight: true },
    { time: '18:00', temp: 20, icon: 'cloud-sun', highlight: false },
    { time: '20:00', temp: 18, icon: 'cloud', highlight: false },
    { time: '22:00', temp: 16, icon: 'moon', highlight: false },
    { time: '00:00', temp: 15, icon: 'moon-fog', highlight: false },
    { time: '02:00', temp: 14, icon: 'rain', highlight: false },
    { time: '04:00', temp: 14, icon: 'wind', highlight: false },
    { time: '06:00', temp: 15, icon: 'sun', highlight: false },
    { time: '08:00', temp: 18, icon: 'sun', highlight: false }
  ])

  const weeklyForecast = ref([
    { day: '今天', high: 24, low: 14, icon: 'sun' },
    { day: '明天', high: 25, low: 15, icon: 'cloud-sun' },
    { day: '周三', high: 23, low: 16, icon: 'cloud' },
    { day: '周四', high: 21, low: 14, icon: 'rain' },
    { day: '周五', high: 22, low: 13, icon: 'cloud-sun' },
    { day: '周六', high: 26, low: 15, icon: 'sun' },
    { day: '周日', high: 27, low: 16, icon: 'sun' }
  ])

  const lifestyleIndices = ref([
    {
      title: '穿衣决策',
      desc: '建议棉麻质地长袖衫，早晚温差10度，需备薄风衣。',
      level: '适中',
      icon: 't-shirt',
      color: 'orange'
    },
    {
      title: '户外运动',
      desc: '空气质量优，紫外线中等，适合户外慢跑或飞盘。',
      level: '极高',
      icon: 'dumbbells',
      color: 'blue'
    },
    {
      title: '洗车指数',
      desc: '未来3天无雨，放心洗车。当前尘埃指数低。',
      level: '五星',
      icon: 'wash',
      color: 'green'
    },
    {
      title: '呼吸健康',
      desc: '花粉浓度较高，建议过敏人群佩戴口罩出门。',
      level: '警惕',
      icon: 'mask',
      color: 'purple'
    }
  ])

  const metrics = ref({
    uv: { value: '中等', desc: '适合户外，建议涂抹防晒霜' },
    visibility: { value: '15.4 km', desc: '空气通透感良好' },
    pressure: { value: '1012 hPa', desc: '稳定范围' },
    allergy: { value: '高发', desc: '正值花粉季节，易敏体质避让' }
  })

  const updateTime = ref(new Date())
  const isLoading = ref(false)
  const error = ref(null)

  const weatherCondition = computed(() => {
    const temp = currentWeather.value.temperature
    if (temp >= 30) return 'hot'
    if (temp <= 10) return 'cold'
    return 'clear'
  })

  function updateTemperature(temp) {
    currentWeather.value.temperature = temp
  }

  function updateCurrentTime() {
    updateTime.value = new Date()
  }

  async function setCurrentCity(city) {
    currentCity.value = city
    
    const savedCity = localStorage.getItem('selectedCity')
    if (savedCity) {
      try {
        const parsed = JSON.parse(savedCity)
        currentCity.value = parsed
      } catch (e) {
        console.error('Failed to parse saved city:', e)
      }
    }
  }

  function saveCityToStorage(city) {
    localStorage.setItem('selectedCity', JSON.stringify(city))
  }

  async function changeCity(city) {
    currentCity.value = city
    saveCityToStorage(city)
    
    await fetchWeatherData(city.name)
  }

  async function fetchWeatherData(location) {
    isLoading.value = true
    error.value = null
    
    let hasAnySuccess = false
    const errors = []
    
    try {
      const [currentRes, hourlyRes, weeklyRes, lifestyleRes, airRes] = await Promise.all([
        weatherApi.getCurrent(location).catch(e => {
          console.warn('Failed to fetch current weather:', e)
          errors.push({ type: 'current', message: e.message || '获取当前天气失败' })
          return null
        }),
        weatherApi.getHourly(location).catch(e => {
          console.warn('Failed to fetch hourly forecast:', e)
          errors.push({ type: 'hourly', message: e.message || '获取小时预报失败' })
          return null
        }),
        weatherApi.getWeekly(location).catch(e => {
          console.warn('Failed to fetch weekly forecast:', e)
          errors.push({ type: 'weekly', message: e.message || '获取周预报失败' })
          return null
        }),
        weatherApi.getLifestyle(location).catch(e => {
          console.warn('Failed to fetch lifestyle:', e)
          errors.push({ type: 'lifestyle', message: e.message || '获取生活指数失败' })
          return null
        }),
        weatherApi.getAirQuality(location).catch(e => {
          console.warn('Failed to fetch air quality:', e)
          errors.push({ type: 'airQuality', message: e.message || '获取空气质量失败' })
          return null
        })
      ])

      if (currentRes && currentRes.data) {
        const data = currentRes.data
        currentWeather.value = {
          location: data.location || location,
          temperature: data.temperature || 22,
          description: data.description || '晴朗',
          humidity: data.humidity || 50,
          wind: data.wind || '微风',
          airQuality: data.airQuality || 50,
          airQualityLevel: data.airQualityLevel || '良',
          highTemp: data.highTemp || 25,
          lowTemp: data.lowTemp || 15
        }
        hasAnySuccess = true
      }

      if (hourlyRes && hourlyRes.data) {
        const rawHourlyData = hourlyRes.data.hourlyData || hourlyRes.data
        const transformed = transformHourlyData(rawHourlyData)
        if (transformed && transformed.length > 0) {
          hourlyForecast.value = transformed
          hasAnySuccess = true
        } else {
          console.warn('Hourly data transformation failed, keeping existing data')
        }
      }

      if (weeklyRes && weeklyRes.data) {
        const rawWeeklyData = weeklyRes.data.dailyData || weeklyRes.data
        const transformed = transformWeeklyData(rawWeeklyData)
        if (transformed && transformed.length > 0) {
          weeklyForecast.value = transformed
          hasAnySuccess = true
        } else {
          console.warn('Weekly data transformation failed, keeping existing data')
        }
      }

      if (lifestyleRes && lifestyleRes.data) {
        const lifestyleData = lifestyleRes.data
        if (lifestyleData.comfort || lifestyleData.dressing) {
          lifestyleIndices.value = [
            {
              title: lifestyleData.dressing?.name || '穿衣指数',
              desc: lifestyleData.dressing?.description || lifestyleData.dressing?.advice || '',
              level: lifestyleData.dressing?.level || '适中',
              icon: 't-shirt',
              color: 'orange'
            },
            {
              title: lifestyleData.sport?.name || '运动指数',
              desc: lifestyleData.sport?.description || lifestyleData.sport?.advice || '',
              level: lifestyleData.sport?.level || '适宜',
              icon: 'dumbbells',
              color: 'blue'
            },
            {
              title: lifestyleData.carWashing?.name || '洗车指数',
              desc: lifestyleData.carWashing?.description || lifestyleData.carWashing?.advice || '',
              level: lifestyleData.carWashing?.level || '适宜',
              icon: 'wash',
              color: 'green'
            },
            {
              title: lifestyleData.uv?.name || '紫外线指数',
              desc: lifestyleData.uv?.description || lifestyleData.uv?.advice || '',
              level: lifestyleData.uv?.level || '中等',
              icon: 'mask',
              color: 'purple'
            }
          ]
        } else {
          lifestyleIndices.value = lifestyleRes.data
        }
        hasAnySuccess = true
      }

      if (airRes && airRes.data) {
        currentWeather.value.airQuality = airRes.data.aqi || 50
        currentWeather.value.airQualityLevel = airRes.data.category || '良'
        hasAnySuccess = true
      }

      updateTime.value = new Date()
      
      if (!hasAnySuccess) {
        const errorMessage = errors.length > 0 
          ? errors.map(e => e.message).join('；')
          : '获取天气数据失败'
        throw new Error(errorMessage)
      }
      
    } catch (e) {
      console.error('Failed to fetch weather data:', e)
      error.value = e.message || '获取天气数据失败'
      throw e
    } finally {
      isLoading.value = false
    }
  }

  async function initializeWeather() {
    const savedCity = localStorage.getItem('selectedCity')
    if (savedCity) {
      try {
        const city = JSON.parse(savedCity)
        currentCity.value = city
        await fetchWeatherData(city.name)
      } catch (e) {
        console.error('Failed to initialize weather:', e)
        await fetchWeatherData(currentCity.value.name)
      }
    } else {
      await fetchWeatherData(currentCity.value.name)
    }
  }

  async function refreshWeather() {
    if (currentCity.value) {
      await fetchWeatherData(currentCity.value.name)
    }
  }

  return {
    currentCity,
    currentWeather,
    hourlyForecast,
    weeklyForecast,
    lifestyleIndices,
    metrics,
    updateTime,
    isLoading,
    error,
    weatherCondition,
    updateTemperature,
    updateCurrentTime,
    setCurrentCity,
    changeCity,
    fetchWeatherData,
    initializeWeather,
    refreshWeather
  }
})
