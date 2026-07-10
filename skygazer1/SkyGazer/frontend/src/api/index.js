import axios from 'axios'
import { ErrorCodes, ErrorMessages, AppError, ErrorHandler, RetryHelper } from '@/utils/errorHandler'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    config.metadata = { startTime: Date.now() }
    
    return config
  },
  (error) => {
    return Promise.reject(ErrorHandler.handle(error))
  }
)

api.interceptors.response.use(
  (response) => {
    const duration = Date.now() - response.config.metadata?.startTime
    
    if (response.data.code && response.data.code !== 200) {
      const error = AppError.fromResponse(response.data)
      console.warn(`API请求返回业务错误 [${response.config.url}]:`, {
        code: error.code,
        message: error.message,
        duration: `${duration}ms`
      })
      return Promise.reject(error)
    }
    
    return response.data
  },
  (error) => {
    const duration = error.config?.metadata ? Date.now() - error.config.metadata.startTime : 0
    
    let appError
    
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      appError = new AppError(
        ErrorCodes.EXTERNAL_API_TIMEOUT,
        '请求超时，请稍后重试'
      )
    } else if (error.message === 'Network Error' || !error.response) {
      appError = new AppError(
        ErrorCodes.NETWORK_ERROR,
        '网络连接失败，请检查网络设置'
      )
    } else {
      const status = error.response.status
      const data = error.response.data
      
      let code = status
      let message = data?.message || ErrorMessages[status] || '请求失败'
      
      if (data?.code) {
        code = data.code
      }
      
      if (status === 401) {
        code = ErrorCodes.TOKEN_EXPIRED
        message = '登录已过期，请重新登录'
      } else if (status === 403) {
        code = ErrorCodes.FORBIDDEN
        message = '没有权限访问该资源'
        // 403 通常也是因为 token 过期或无效，需要重新登录
        status = 401 // 将 403 视为认证错误处理
      } else if (status === 404) {
        code = ErrorCodes.NOT_FOUND
        message = '请求的资源不存在'
      } else if (status === 429) {
        code = ErrorCodes.RATE_LIMIT_EXCEEDED
        message = '请求过于频繁，请稍后再试'
      } else if (status >= 500) {
        code = ErrorCodes.SERVICE_UNAVAILABLE
        message = '服务暂时不可用，请稍后重试'
      }
      
      appError = new AppError(code, message, data?.details)
    }
    
    console.error(`API请求失败 [${error.config?.url || 'unknown'}]:`, {
      code: appError.code,
      message: appError.message,
      duration: `${duration}ms`
    })
    
    if (appError.isAuthError()) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      
      if (window.location.pathname !== '/') {
        window.location.href = '/'
      }
    }
    
    return Promise.reject(appError)
  }
)

export const authApi = {
  login(credentials) {
    return api.post('/user/login', credentials)
  },
  
  register(userData) {
    return api.post('/user/register', userData)
  },
  
  getProfile() {
    return api.get('/user/profile')
  },
  
  updateProfile(data) {
    return api.put('/user/profile', data)
  },
  
  updateLocation(location) {
    return api.put('/user/location', null, { params: { location } })
  }
}

export const weatherApi = {
  getCurrent(location) {
    return RetryHelper.executeWithRetry(
      () => api.get('/weather/current', { params: { location } }),
      { maxRetries: 2 }
    )
  },
  
  getHourly(location) {
    return api.get('/weather/hourly', { params: { location } })
  },
  
  getWeekly(location) {
    return api.get('/weather/weekly', { params: { location } })
  },
  
  getLifestyle(location) {
    return api.get('/weather/lifestyle', { params: { location } })
  },
  
  getAirQuality(location) {
    return api.get('/weather/air-quality', { params: { location } })
  },
  
  refresh(location) {
    return api.post('/weather/refresh', null, { params: { location } })
  },
  
  getAnalysis(location, timeRange = '24h') {
    return api.get('/weather/analysis', { params: { location, timeRange } })
  }
}

export const aiApi = {
  chat(data) {
    return RetryHelper.executeWithRetry(
      () => api.post('/ai/chat', data),
      { maxRetries: 2, initialDelay: 2000 }
    )
  },
  
  streamChat(data) {
    return api.post('/ai/chat/stream', data, {
      responseType: 'stream'
    })
  },
  
  analyzeImage(data) {
    return api.post('/ai/analyze-image', data)
  },
  
  generateWeatherStory(location, style) {
    return RetryHelper.executeWithRetry(
      () => api.post('/ai/weather-story', null, { params: { location, style } }),
      { maxRetries: 1, initialDelay: 3000 }
    )
  },
  
  getDecisionAdvice(location, scenario) {
    return api.post('/ai/decision-advice', null, { params: { location, scenario } })
  }
}

export const agentApi = {
  query(data) {
    return RetryHelper.executeWithRetry(
      () => api.post('/agent/query', data),
      { maxRetries: 2, initialDelay: 2000 }
    )
  },
  
  streamQuery(data) {
    return api.post('/agent/query/stream', data, {
      responseType: 'stream'
    })
  },
  
  analyzeWeather(location) {
    return api.get('/agent/analyze', { params: { location } })
  },
  
  getActivityAdvice(location, activity) {
    return api.get('/agent/activity-advice', { params: { location, activity } })
  },
  
  getWeatherAlert(location) {
    return api.get('/agent/alert', { params: { location } })
  },
  
  getStatus() {
    return api.get('/agent/status')
  },
  
  refreshKnowledge() {
    return api.post('/agent/knowledge/refresh')
  }
}

export const mapApi = {
  getLayer(layerType, cities) {
    const params = {}
    if (cities && cities.length > 0) {
      params.cities = cities.join(',')
    }
    return api.get(`/weather-map/layer/${layerType}`, { params })
  },
  
  getTimeline(layerType, hours = 24, cities) {
    const params = { hours }
    if (cities && cities.length > 0) {
      params.cities = cities.join(',')
    }
    return api.get(`/weather-map/timeline/${layerType}`, { params })
  },
  
  getMultiLayer(layerTypes, cities) {
    const params = { layerTypes: layerTypes.join(',') }
    if (cities && cities.length > 0) {
      params.cities = cities.join(',')
    }
    return api.get('/weather-map/multi-layer', { params })
  },
  
  refresh(layerType, cities) {
    const params = { layerType }
    if (cities && cities.length > 0) {
      params.cities = cities.join(',')
    }
    return api.post('/weather-map/refresh', null, { params })
  },
  
  getAvailableLayers() {
    return api.get('/weather-map/layers')
  },
  
  getDistrictWeather(adcode, layerType = 'temperature') {
    return api.get(`/weather-map/district-weather/${adcode}`, { 
      params: { layerType } 
    })
  },
  
  refreshDistrictWeather(adcode, layerType = 'temperature') {
    return api.post(`/weather-map/district-refresh/${adcode}`, null, { 
      params: { layerType } 
    })
  },
  
  getRefreshStatus(adcode) {
    return api.get(`/weather-map/refresh-status/${adcode}`)
  },
  
  getGeoJson(adcode, type = 'full') {
    return api.get(`/weather-map/geojson/${adcode}`, {
      params: { type }
    })
  }
}

api.getMapLayer = mapApi.getLayer
api.getMapTimeline = mapApi.getTimeline
api.getMultiLayerData = mapApi.getMultiLayer
api.refreshMapData = mapApi.refresh
api.getAvailableLayers = mapApi.getAvailableLayers

export { ErrorCodes, ErrorMessages, AppError, ErrorHandler, RetryHelper }
export default api
