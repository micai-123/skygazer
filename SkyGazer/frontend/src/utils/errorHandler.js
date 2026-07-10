export const ErrorCodes = {
  SUCCESS: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_ERROR: 500,
  SERVICE_UNAVAILABLE: 503,
  
  INVALID_PARAMETER: 400001,
  MISSING_PARAMETER: 400002,
  INVALID_CITY_NAME: 400003,
  INVALID_LAYER_TYPE: 400004,
  INVALID_TIME_RANGE: 400005,
  
  TOKEN_EXPIRED: 401001,
  INVALID_TOKEN: 401002,
  
  RATE_LIMIT_EXCEEDED: 403001,
  
  CITY_NOT_FOUND: 404001,
  WEATHER_DATA_NOT_FOUND: 404002,
  
  DATABASE_ERROR: 500001,
  CACHE_ERROR: 500002,
  CONFIG_ERROR: 500003,
  
  WEATHER_API_ERROR: 503001,
  AI_SERVICE_ERROR: 503002,
  EXTERNAL_API_TIMEOUT: 503003,
  EXTERNAL_API_ERROR: 503004,
  DATA_PARSE_ERROR: 503005,
  NETWORK_ERROR: 503006
}

export const ErrorMessages = {
  [ErrorCodes.BAD_REQUEST]: '请求参数错误',
  [ErrorCodes.UNAUTHORIZED]: '未授权访问',
  [ErrorCodes.FORBIDDEN]: '禁止访问',
  [ErrorCodes.NOT_FOUND]: '资源不存在',
  [ErrorCodes.INTERNAL_ERROR]: '系统内部错误',
  [ErrorCodes.SERVICE_UNAVAILABLE]: '服务暂时不可用',
  
  [ErrorCodes.INVALID_PARAMETER]: '参数验证失败',
  [ErrorCodes.MISSING_PARAMETER]: '缺少必要参数',
  [ErrorCodes.INVALID_CITY_NAME]: '无效的城市名称',
  [ErrorCodes.INVALID_LAYER_TYPE]: '无效的图层类型',
  [ErrorCodes.INVALID_TIME_RANGE]: '无效的时间范围',
  
  [ErrorCodes.TOKEN_EXPIRED]: '登录已过期，请重新登录',
  [ErrorCodes.INVALID_TOKEN]: '无效的令牌',
  
  [ErrorCodes.RATE_LIMIT_EXCEEDED]: '请求过于频繁，请稍后再试',
  
  [ErrorCodes.CITY_NOT_FOUND]: '城市不存在',
  [ErrorCodes.WEATHER_DATA_NOT_FOUND]: '天气数据不存在',
  
  [ErrorCodes.DATABASE_ERROR]: '数据库操作失败',
  [ErrorCodes.CACHE_ERROR]: '缓存操作失败',
  [ErrorCodes.CONFIG_ERROR]: '配置错误',
  
  [ErrorCodes.WEATHER_API_ERROR]: '天气服务暂时不可用',
  [ErrorCodes.AI_SERVICE_ERROR]: 'AI服务暂时不可用',
  [ErrorCodes.EXTERNAL_API_TIMEOUT]: '请求超时，请稍后重试',
  [ErrorCodes.EXTERNAL_API_ERROR]: '外部服务调用失败',
  [ErrorCodes.DATA_PARSE_ERROR]: '数据解析失败',
  [ErrorCodes.NETWORK_ERROR]: '网络连接失败'
}

export class AppError extends Error {
  constructor(code, message, details = null) {
    super(message || ErrorMessages[code] || '未知错误')
    this.code = code
    this.details = details
    this.timestamp = Date.now()
    this.name = 'AppError'
  }
  
  static fromResponse(response) {
    const code = response?.code || ErrorCodes.INTERNAL_ERROR
    const message = response?.message || ErrorMessages[code] || '未知错误'
    const details = response?.details || null
    return new AppError(code, message, details)
  }
  
  isNetworkError() {
    return this.code === ErrorCodes.NETWORK_ERROR || this.code === 0
  }
  
  isAuthError() {
    return this.code === ErrorCodes.UNAUTHORIZED || 
           this.code === ErrorCodes.TOKEN_EXPIRED ||
           this.code === ErrorCodes.INVALID_TOKEN
  }
  
  isRetryable() {
    return [
      ErrorCodes.SERVICE_UNAVAILABLE,
      ErrorCodes.WEATHER_API_ERROR,
      ErrorCodes.AI_SERVICE_ERROR,
      ErrorCodes.EXTERNAL_API_TIMEOUT,
      ErrorCodes.EXTERNAL_API_ERROR,
      ErrorCodes.NETWORK_ERROR
    ].includes(this.code)
  }
  
  getUserFriendlyMessage() {
    if (this.isNetworkError()) {
      return '网络连接失败，请检查网络设置后重试'
    }
    
    if (this.isAuthError()) {
      return '登录已过期，请重新登录'
    }
    
    if (this.code === ErrorCodes.RATE_LIMIT_EXCEEDED) {
      return '请求过于频繁，请稍后再试'
    }
    
    if (this.isRetryable()) {
      return '服务暂时不可用，请稍后重试'
    }
    
    return this.message
  }
}

export const ErrorHandler = {
  handle(error, options = {}) {
    const appError = error instanceof AppError 
      ? error 
      : AppError.fromResponse(error)
    
    if (options.log !== false) {
      console.error('[ErrorHandler]', {
        code: appError.code,
        message: appError.message,
        details: appError.details,
        timestamp: new Date(appError.timestamp).toISOString()
      })
    }
    
    if (appError.isAuthError() && options.redirectOnAuth !== false) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      
      if (window.location.pathname !== '/') {
        window.location.href = '/'
      }
    }
    
    return appError
  },
  
  getErrorMessage(error) {
    if (error instanceof AppError) {
      return error.getUserFriendlyMessage()
    }
    
    if (error?.response?.data?.message) {
      return error.response.data.message
    }
    
    if (error?.message) {
      if (error.message.includes('timeout')) {
        return '请求超时，请稍后重试'
      }
      if (error.message.includes('Network Error')) {
        return '网络连接失败，请检查网络设置'
      }
      return error.message
    }
    
    return '操作失败，请稍后重试'
  }
}

export const RetryHelper = {
  async executeWithRetry(fn, options = {}) {
    const {
      maxRetries = 3,
      initialDelay = 1000,
      backoffMultiplier = 2,
      shouldRetry = (error) => {
        if (error instanceof AppError) {
          return error.isRetryable()
        }
        return true
      },
      onRetry = () => {}
    } = options
    
    let lastError = null
    let delay = initialDelay
    
    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        return await fn()
      } catch (error) {
        lastError = error
        
        if (attempt === maxRetries || !shouldRetry(error)) {
          throw error
        }
        
        onRetry(attempt + 1, error)
        
        await new Promise(resolve => setTimeout(resolve, delay))
        delay *= backoffMultiplier
      }
    }
    
    throw lastError
  }
}

export default {
  ErrorCodes,
  ErrorMessages,
  AppError,
  ErrorHandler,
  RetryHelper
}
