// 轻量、零依赖的 Markdown 渲染器，仅用于 AI 智能体消息正文与步骤详情。
// 设计目标：安全（先转义）、克制（仅支持常见语法）、输出带语义 class 的 HTML 便于主题化。

function escapeHtml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function inline(text) {
  // 行内代码保护：先占位，最后还原
  const codes = []
  text = text.replace(/`([^`]+)`/g, (_, c) => {
    codes.push(c)
    return 'CODE' + (codes.length - 1) + ''
  })

  // 链接 [text](url)
  text = text.replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, (_, t, url) => {
    const safe = /^(https?:\/\/|mailto:|\/)/i.test(url) ? url : '#'
    return '<a class="md-link" href="' + safe + '" target="_blank" rel="noopener noreferrer">' + t + '</a>'
  })

  // 粗体 **x** / __x__
  text = text.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  text = text.replace(/__([^_]+)__/g, '<strong>$1</strong>')

  // 斜体 *x* / _x_
  text = text.replace(/(^|[^*])\*([^*\n]+)\*/g, '$1<em>$2</em>')
  text = text.replace(/(^|[^_])_([^_\n]+)_/g, '$1<em>$2</em>')

  // 还原行内代码
  text = text.replace(/CODE(\d+)/g, (_, i) => '<code class="md-code">' + escapeHtml(codes[+i]) + '</code>')

  return text
}

export function renderMarkdown(raw) {
  if (!raw) return ''
  const escaped = escapeHtml(raw)
  const lines = escaped.split('\n')
  const html = []
  let listType = null
  let para = []

  const flushPara = () => {
    if (para.length) {
      html.push('<p class="md-p">' + inline(para.join(' ')) + '</p>')
      para = []
    }
  }
  const closeList = () => {
    if (listType) {
      html.push('</' + listType + '>')
      listType = null
    }
  }

  let i = 0
  while (i < lines.length) {
    const line = lines[i]

    // 代码块 ```lang
    const fence = line.match(/^```(\w*)\s*$/)
    if (fence) {
      flushPara()
      closeList()
      const buf = []
      i++
      while (i < lines.length && !/^```\s*$/.test(lines[i])) {
        buf.push(lines[i])
        i++
      }
      i++
      html.push('<pre class="md-pre"><code>' + buf.join('\n') + '</code></pre>')
      continue
    }

    // 标题
    const h = line.match(/^(#{1,4})\s+(.*)$/)
    if (h) {
      flushPara()
      closeList()
      const level = h[1].length
      html.push('<h' + level + ' class="md-h md-h' + level + '">' + inline(h[2]) + '</h' + level + '>')
      i++
      continue
    }

    // 引用
    const quote = line.match(/^&gt;\s?(.*)$/)
    if (quote) {
      flushPara()
      closeList()
      html.push('<blockquote class="md-quote">' + inline(quote[1]) + '</blockquote>')
      i++
      continue
    }

    // 有序列表
    const ol = line.match(/^\s*\d+\.\s+(.*)$/)
    if (ol) {
      flushPara()
      if (listType !== 'ol') {
        closeList()
        html.push('<ol class="md-ol">')
        listType = 'ol'
      }
      html.push('<li>' + inline(ol[1]) + '</li>')
      i++
      continue
    }

    // 无序列表 - / *
    const ul = line.match(/^\s*[-*]\s+(.*)$/)
    if (ul) {
      flushPara()
      if (listType !== 'ul') {
        closeList()
        html.push('<ul class="md-ul">')
        listType = 'ul'
      }
      html.push('<li>' + inline(ul[1]) + '</li>')
      i++
      continue
    }

    // 空行
    if (!line.trim()) {
      flushPara()
      closeList()
      i++
      continue
    }

    // 普通段落行
    para.push(line.trim())
    i++
  }

  flushPara()
  closeList()
  return html.join('\n')
}
