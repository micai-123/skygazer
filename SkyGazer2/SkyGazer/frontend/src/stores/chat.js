import { defineStore } from 'pinia'
import { ref } from 'vue'
import { agentApi } from '@/api'
import { useWeatherStore } from './weather'

export const useChatStore = defineStore('chat', () => {
  const messages = ref([
    {
      id: 1,
      content: '你好！我是天象智囊，您的专业气象助手。我可以帮您查询天气、分析气象数据、提供活动建议。请问有什么可以帮您的？',
      isUser: false,
      timestamp: new Date(),
      agentName: '天象智囊',
      references: []
    }
  ])

  const isTyping = ref(false)
  const isStreaming = ref(false)
  const sessionId = ref(null)
  const currentLocation = ref('北京')

  function addMessage(content, isUser = true, options = {}) {
    const message = {
      id: Date.now(),
      content,
      isUser,
      timestamp: new Date(),
      agentName: options.agentName || '天象智囊',
      references: options.references || [],
      confidence: options.confidence,
      weatherContext: options.weatherContext
    }
    messages.value.push(message)
    return message
  }

  function updateLastMessage(content, append = false) {
    if (messages.value.length > 0) {
      const lastMessage = messages.value[messages.value.length - 1]
      if (append) {
        lastMessage.content += content
      } else {
        lastMessage.content = content
      }
    }
  }

  async function sendMessage(content) {
    addMessage(content, true)
    
    isTyping.value = true
    isStreaming.value = false
    
    const maxRetries = 2
    let retryCount = 0
    
    while (retryCount <= maxRetries) {
      try {
        const weatherStore = useWeatherStore()
        const location = weatherStore.currentLocation || currentLocation.value
        
        if (retryCount === 0) {
          addMessage('', false, { agentName: '天象智囊' })
        }
        isStreaming.value = true
        isTyping.value = false
        
        const controller = new AbortController()
        const timeoutId = setTimeout(() => controller.abort(), 60000)
        
        const response = await fetch('/api/agent/query/stream', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'text/event-stream',
          },
          body: JSON.stringify({
            question: content,
            location: location,
            sessionId: sessionId.value
          }),
          signal: controller.signal
        })
        
        clearTimeout(timeoutId)
        
        if (!response.ok) {
          const errorText = await response.text()
          console.error('API错误响应:', response.status, errorText)
          
          if (response.status >= 500 && retryCount < maxRetries) {
            retryCount++
            updateLastMessage(`请求失败，正在重试 (${retryCount}/${maxRetries})...`)
            await new Promise(resolve => setTimeout(resolve, 1000 * retryCount))
            continue
          }
          
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const reader = response.body.getReader()
        const decoder = new TextDecoder('utf-8')
        let buffer = ''
        let hasReceivedData = false
        
        while (true) {
          const { done, value } = await reader.read()
          
          if (done) {
            if (!hasReceivedData && retryCount < maxRetries) {
              retryCount++
              updateLastMessage(`未收到响应，正在重试 (${retryCount}/${maxRetries})...`)
              await new Promise(resolve => setTimeout(resolve, 1000 * retryCount))
              break
            }
            break
          }
          
          hasReceivedData = true
          buffer += decoder.decode(value, { stream: true })
          
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''
          
          for (const line of lines) {
            const trimmedLine = line.trim()
            if (trimmedLine.startsWith('data:')) {
              const data = trimmedLine.slice(5).trim()
              if (data && data !== '[DONE]') {
                updateLastMessage(data, true)
              }
            }
          }
        }
        
        if (hasReceivedData) {
          retryCount = maxRetries + 1
        }
        
      } catch (error) {
        console.error('智能体查询失败:', error)
        
        if (error.name === 'AbortError') {
          updateLastMessage('请求超时，请稍后再试。')
        } else if (retryCount < maxRetries && !error.message.includes('401') && !error.message.includes('403')) {
          retryCount++
          updateLastMessage(`发生错误，正在重试 (${retryCount}/${maxRetries})...`)
          await new Promise(resolve => setTimeout(resolve, 1000 * retryCount))
          continue
        } else {
          updateLastMessage('抱歉，天象智囊暂时无法回答您的问题，请稍后再试。')
        }
        
        break
      } finally {
        isTyping.value = false
        isStreaming.value = false
      }
    }
  }

  async function sendMessageNonStream(content) {
    addMessage(content, true)
    
    isTyping.value = true
    
    try {
      const weatherStore = useWeatherStore()
      const location = weatherStore.currentLocation || currentLocation.value
      
      const response = await agentApi.query({
        question: content,
        location: location,
        sessionId: sessionId.value,
        context: null
      })
      
      if (response.data) {
        sessionId.value = response.data.sessionId
        
        addMessage(response.data.answer, false, {
          agentName: response.data.agentName,
          references: response.data.references,
          confidence: response.data.confidence,
          weatherContext: response.data.weatherContext
        })
        
        return response.data.answer
      }
    } catch (error) {
      console.error('智能体查询失败:', error)
      addMessage('抱歉，天象智囊暂时无法回答您的问题，请稍后再试。', false)
    } finally {
      isTyping.value = false
    }
  }

  async function analyzeWeather(location) {
    isTyping.value = true
    
    try {
      const response = await agentApi.analyzeWeather(location)
      
      if (response.data) {
        addMessage(response.data.answer, false, {
          agentName: response.data.agentName,
          weatherContext: response.data.weatherContext
        })
        return response.data.answer
      }
    } catch (error) {
      console.error('天气分析失败:', error)
      addMessage('抱歉，无法获取天气分析结果。', false)
    } finally {
      isTyping.value = false
    }
  }

  async function getActivityAdvice(location, activity) {
    isTyping.value = true
    
    try {
      const response = await agentApi.getActivityAdvice(location, activity)
      
      if (response.data) {
        addMessage(response.data.answer, false, {
          agentName: response.data.agentName,
          references: response.data.references,
          weatherContext: response.data.weatherContext
        })
        return response.data.answer
      }
    } catch (error) {
      console.error('获取活动建议失败:', error)
      addMessage('抱歉，无法获取活动建议。', false)
    } finally {
      isTyping.value = false
    }
  }

  function clearMessages() {
    messages.value = []
    sessionId.value = null
  }

  function setMessages(newMessages) {
    messages.value = newMessages || []
  }

  function setLocation(location) {
    currentLocation.value = location
  }

  return {
    messages,
    isTyping,
    isStreaming,
    sessionId,
    currentLocation,
    addMessage,
    updateLastMessage,
    sendMessage,
    sendMessageNonStream,
    analyzeWeather,
    getActivityAdvice,
    clearMessages,
    setMessages,
    setLocation
  }
})
