import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { agentApi, weatherImageApi } from '@/api'
import { useWeatherStore } from './weather'

// 将图片文件转为 data URL，用于上传前的本地预览
function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

// 将后端可能返回的引用（字符串数组或对象数组）归一化为统一结构
function normalizeCitations(refs) {
  if (!Array.isArray(refs)) return []
  return refs.map((r) => {
    if (typeof r === 'string') return { title: r }
    return {
      title: r.title || r.name || '气象知识来源',
      source: r.source || r.docName || '',
      snippet: r.snippet || r.content || r.text || '',
      url: r.url || ''
    }
  })
}

// 由 references / weatherContext 派生「工作过程」步骤（向后兼容当前数据）
function deriveSteps(references, weatherContext) {
  const steps = []
  const base = Date.now()
  if (references && references.length) {
    steps.push({
      type: 'rag',
      label: '知识检索',
      detail: `从气象知识库检索到 ${references.length} 条相关来源，用于增强回答的可靠性。`,
      status: 'done',
      ts: base
    })
  }
  if (weatherContext) {
    const loc = weatherContext.location || ''
    steps.push({
      type: 'tool',
      label: '实时天气',
      detail:
        `${loc ? loc + ' ' : ''}实时气象数据已获取` +
        (weatherContext.weatherCondition ? `（${weatherContext.weatherCondition}）` : '') +
        '。',
      status: 'done',
      ts: base + 1
    })
  }
  return steps
}

export const useChatStore = defineStore('chat', () => {
  const messages = ref([
    {
      id: 1,
      content:
        '你好！我是天象智囊，您的专业气象助手。我可以帮您查询天气、分析气象数据、提供活动建议。请问有什么可以帮您的？',
      isUser: false,
      timestamp: new Date(),
      agentName: '天象智囊',
      references: [],
      weatherContext: null,
      steps: []
    }
  ])

  const isTyping = ref(false)
  const isStreaming = ref(false)
  const imageProcessing = ref(false)
  const sessionId = ref(null)
  const currentLocation = ref('北京')
  const currentController = ref(null)

  const isGenerating = computed(() => isTyping.value || isStreaming.value || imageProcessing.value)

  function addMessage(content, isUser = true, options = {}) {
    const message = {
      id: Date.now() + Math.floor(Math.random() * 1000),
      content,
      isUser,
      timestamp: new Date(),
      agentName: options.agentName || '天象智囊',
      references: normalizeCitations(options.references),
      confidence: options.confidence,
      weatherContext: options.weatherContext || null,
      steps: options.steps || []
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

  // 向最后一条 AI 消息追加/更新工作步骤（兼容未来后端下发的 step 事件）
  function appendStep(step) {
    const aiMsgs = messages.value.filter((m) => !m.isUser)
    if (!aiMsgs.length) return
    const target = aiMsgs[aiMsgs.length - 1]
    if (!Array.isArray(target.steps)) target.steps = []
    const normalized = {
      type: step.type || 'reason',
      label: step.label || '智能体推理',
      detail: step.detail || '',
      status: step.status || 'done',
      ts: step.ts || Date.now()
    }
    // 同类型标签去重：若已存在同 label 步骤则更新
    const existing = target.steps.find((s) => s.label === normalized.label && s.type === normalized.type)
    if (existing) {
      Object.assign(existing, normalized)
    } else {
      target.steps.push(normalized)
    }
  }

  function setLastStepStatus(label, status) {
    const aiMsgs = messages.value.filter((m) => !m.isUser)
    if (!aiMsgs.length) return
    const target = aiMsgs[aiMsgs.length - 1]
    const step = target.steps && target.steps.find((s) => s.label === label)
    if (step) step.status = status
  }

  function stopGeneration() {
    if (currentController.value) {
      currentController.value.abort()
      currentController.value = null
    }
    isTyping.value = false
    isStreaming.value = false
  }

  // 标记/清除最后一条 AI 消息的错误态，供 UI 渲染重试入口
  function setLastMessageError(isError) {
    const aiMsgs = messages.value.filter((m) => !m.isUser)
    if (!aiMsgs.length) return
    aiMsgs[aiMsgs.length - 1].error = isError
  }

  async function sendMessage(content) {
    addMessage(content, true)
    await runAgentQuery(content)
  }

  // 重试最后一次提问：移除失败的 AI 回复后，用最后一条用户问题重新发起
  async function retryLast() {
    if (isGenerating.value) return
    let lastUserIndex = -1
    for (let i = messages.value.length - 1; i >= 0; i--) {
      if (messages.value[i].isUser) {
        lastUserIndex = i
        break
      }
    }
    if (lastUserIndex === -1) return
    const content = messages.value[lastUserIndex].content
    // 丢弃该用户消息之后的所有（失败的）AI 消息
    messages.value = messages.value.slice(0, lastUserIndex + 1)
    await runAgentQuery(content)
  }

  async function runAgentQuery(content) {
    isTyping.value = true
    isStreaming.value = false

    const maxRetries = 2
    let retryCount = 0

    while (retryCount <= maxRetries) {
      try {
        const weatherStore = useWeatherStore()
        const location = weatherStore.currentLocation || currentLocation.value

        if (retryCount === 0) {
          // 流式占位消息，附带一个实时的「智能体推理」步骤，使工作过程可视化立即可见
          addMessage('', false, {
            agentName: '天象智囊',
            steps: [{ type: 'reason', label: '智能体推理', detail: '', status: 'running', ts: Date.now() }]
          })
        }
        isStreaming.value = true
        isTyping.value = false

        const controller = new AbortController()
        currentController.value = controller
        const timeoutId = setTimeout(() => controller.abort(), 60000)

        const response = await fetch('/api/agent/query/stream', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'text/event-stream'
          },
          body: JSON.stringify({
            question: content,
            location: location,
            sessionId: sessionId.value
          }),
          signal: controller.signal
        })

        clearTimeout(timeoutId)
        currentController.value = null

        if (!response.ok) {
          const errorText = await response.text()
          console.error('API错误响应:', response.status, errorText)

          if (response.status >= 500 && retryCount < maxRetries) {
            retryCount++
            updateLastMessage(`请求失败，正在重试 (${retryCount}/${maxRetries})...`)
            await new Promise((resolve) => setTimeout(resolve, 1000 * retryCount))
            continue
          }

          throw new Error(`HTTP error! status: ${response.status}`)
        }

        const reader = response.body.getReader()
        const decoder = new TextDecoder('utf-8')
        let buffer = ''
        let hasReceivedData = false
        let pendingEventType = ''

        while (true) {
          const { done, value } = await reader.read()

          if (done) {
            if (!hasReceivedData && retryCount < maxRetries) {
              retryCount++
              updateLastMessage(`未收到响应，正在重试 (${retryCount}/${maxRetries})...`)
              await new Promise((resolve) => setTimeout(resolve, 1000 * retryCount))
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
            if (!trimmedLine) continue

            // 事件类型标记（向前兼容后端下发的结构化 step 事件）
            if (trimmedLine.startsWith('event:')) {
              pendingEventType = trimmedLine.slice(6).trim()
              continue
            }

            if (trimmedLine.startsWith('data:')) {
              const data = trimmedLine.slice(5).trim()
              if (!data || data === '[DONE]') {
                pendingEventType = ''
                continue
              }

              // 结构化步骤事件：data 形如 [STEP]{...} 或 event: step + data JSON
              const isStepEvent =
                pendingEventType === 'step' || data.startsWith('[STEP]')
              if (isStepEvent) {
                try {
                  const json = data.startsWith('[STEP]') ? data.slice(6) : data
                  const step = JSON.parse(json)
                  appendStep(step)
                } catch (e) {
                  // 解析失败则忽略该事件，不影响正文
                }
                pendingEventType = ''
                continue
              }

              updateLastMessage(data, true)
            }
          }
        }

        if (hasReceivedData) {
          retryCount = maxRetries + 1
          setLastStepStatus('智能体推理', 'done')
          setLastMessageError(false)
        }
      } catch (error) {
        console.error('智能体查询失败:', error)

        if (error.name === 'AbortError') {
          setLastStepStatus('智能体推理', 'done')
          updateLastMessage('已停止生成。')
        } else if (retryCount < maxRetries && !error.message.includes('401') && !error.message.includes('403')) {
          retryCount++
          updateLastMessage(`发生错误，正在重试 (${retryCount}/${maxRetries})...`)
          await new Promise((resolve) => setTimeout(resolve, 1000 * retryCount))
          continue
        } else {
          setLastStepStatus('智能体推理', 'error')
          updateLastMessage('抱歉，天象智囊暂时无法回答您的问题，请稍后再试。')
          setLastMessageError(true)
        }

        break
      } finally {
        isTyping.value = false
        isStreaming.value = false
        currentController.value = null
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
          references: normalizeCitations(response.data.references),
          confidence: response.data.confidence,
          weatherContext: response.data.weatherContext,
          steps: deriveSteps(response.data.references, response.data.weatherContext)
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
          references: normalizeCitations(response.data.references),
          weatherContext: response.data.weatherContext,
          steps: deriveSteps(response.data.references, response.data.weatherContext)
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
          references: normalizeCitations(response.data.references),
          weatherContext: response.data.weatherContext,
          steps: deriveSteps(response.data.references, response.data.weatherContext)
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

  // 根据四分类结果生成一段中文文字分析
  function buildWeatherImageAnalysis(result) {
    const conf = (result.confidence * 100).toFixed(1)
    const probs = result.probabilities || {}
    const order = ['sunny', 'cloudy', 'rainy', 'snowy']
    const cn = { sunny: '晴天', cloudy: '多云', rainy: '雨天', snowy: '雪天' }
    const ranked = order
      .map((k) => `${cn[k]} ${((probs[k] || 0) * 100).toFixed(1)}%`)
      .join('、')
    return `根据图片识别结果，当前场景最可能属于「${result.labelCn}」（置信度 ${conf}%）。\n\n四分类概率分布：${ranked}。\n\n小提示：拍照时尽量包含大面积天空与地平线、避免逆光或夜间低光，可显著提升识别准确率。`
  }

  // 上传图片 + 文字，进行天气识别并给出文字分析。text 为用户侧引导语。
  async function sendImageWithText(file, text) {
    if (!file || isTyping.value) return

    const imageUrl = await fileToDataUrl(file)
    addMessage(text || '分析此图片天气', true, { imageUrl })

    imageProcessing.value = true

    // AI 占位消息：先展示「图片上传」完成、「模型推理」进行中
    addMessage('', false, {
      agentName: '天象智囊',
      steps: [
        { type: 'model', label: '图片上传', detail: '已接收天气图片，准备提交模型推理。', status: 'done', ts: Date.now() },
        { type: 'model', label: '模型推理', detail: '正在调用天气识别模型进行四分类推理…', status: 'running', ts: Date.now() + 1 }
      ]
    })

    try {
      const form = new FormData()
      form.append('image', file)
      const resp = await weatherImageApi.predict(form)
      const result = resp.data
      if (!result || !result.labelEn) throw new Error('返回结果无效')
      setLastWeatherResult(result, buildWeatherImageAnalysis(result))
    } catch (error) {
      console.error('天气图片识别失败:', error)
      setLastWeatherError('图片识别失败，请确认模型服务（:5000）已启动后重试。')
    } finally {
      imageProcessing.value = false
    }
  }

  // 向后兼容：无文字时自动使用引导语
  async function sendImageMessage(file) {
    return sendImageWithText(file, '分析此图片天气')
  }

  // 用识别结果更新最后一条 AI 消息（含工作步骤、文字分析与天气卡片数据）
  function setLastWeatherResult(result, analysisText) {
    const aiMsgs = messages.value.filter((m) => !m.isUser)
    if (!aiMsgs.length) return
    const target = aiMsgs[aiMsgs.length - 1]
    target.weatherImage = result
    target.content = analysisText || ''
    const conf = (result.confidence * 100).toFixed(1)
    target.steps = [
      { type: 'model', label: '图片上传', detail: '已接收天气图片，提交模型推理。', status: 'done', ts: Date.now() },
      { type: 'model', label: '模型推理', detail: `已完成 ${result.labelCn} 分类推理。`, status: 'done', ts: Date.now() + 1 },
      { type: 'model', label: '结果分析', detail: `识别为「${result.labelCn}」，置信度 ${conf}%。`, status: 'done', ts: Date.now() + 2 }
    ]
  }

  function setLastWeatherError(text) {
    const aiMsgs = messages.value.filter((m) => !m.isUser)
    if (!aiMsgs.length) return
    const target = aiMsgs[aiMsgs.length - 1]
    target.content = text
    target.steps = (Array.isArray(target.steps) ? target.steps : []).map((s) =>
      s.status === 'running' ? { ...s, status: 'error' } : s
    )
  }

  function clearMessages() {
    messages.value = []
    sessionId.value = null
  }

  function setMessages(newMessages) {
    // 历史消息载入时，旧字符串引用归一化；确保 steps 字段存在
    messages.value = (newMessages || []).map((m) => ({
      ...m,
      references: normalizeCitations(m.references),
      steps: Array.isArray(m.steps) ? m.steps : []
    }))
  }

  function setLocation(location) {
    currentLocation.value = location
  }

  return {
    messages,
    isTyping,
    isStreaming,
    isGenerating,
    sessionId,
    currentLocation,
    addMessage,
    updateLastMessage,
    appendStep,
    sendMessage,
    retryLast,
    sendMessageNonStream,
    sendImageMessage,
    sendImageWithText,
    stopGeneration,
    analyzeWeather,
    getActivityAdvice,
    clearMessages,
    setMessages,
    setLocation
  }
})
