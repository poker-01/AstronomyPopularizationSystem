import api from './api'

function unwrap(res) {
  const body = res.data
  if (body && typeof body.code === 'number') {
    if (body.code !== 0) throw new Error(body.message || '请求失败')
    return body.data
  }
  return body
}

const API_BASE = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

function parseSseBlock(block, handlers) {
  let eventName = 'message'
  const dataLines = []
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim())
    }
  }
  if (!dataLines.length) return
  let payload
  try {
    payload = JSON.parse(dataLines.join('\n'))
  } catch {
    return
  }
  switch (eventName) {
    case 'meta':
      handlers.onMeta?.(payload)
      break
    case 'delta':
      handlers.onDelta?.(payload.content ?? '')
      break
    case 'done':
      handlers.onDone?.(payload)
      break
    case 'error':
      handlers.onError?.(new Error(payload.message || 'AI 回复失败'))
      break
    default:
      break
  }
}

/**
 * 流式发送问题，通过 SSE 逐段返回 AI 回复。
 * @returns {{ abort: () => void, done: Promise<void> }}
 */
export function sendChatStream(message, sessionId, handlers = {}) {
  const token = localStorage.getItem('auth_token')
  const controller = new AbortController()

  const done = fetch(`${API_BASE}/api/ai/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify({ message, sessionId }),
    signal: controller.signal
  })
    .then(async (res) => {
      if (!res.ok) {
        const err = await res.json().catch(() => ({}))
        throw new Error(err.message || `请求失败 (${res.status})`)
      }
      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let readResult = await reader.read()
      while (!readResult.done) {
        buffer += decoder.decode(readResult.value, { stream: true })
        const parts = buffer.split('\n\n')
        buffer = parts.pop() || ''
        for (const part of parts) {
          if (part.trim()) parseSseBlock(part, handlers)
        }
        readResult = await reader.read()
      }
      if (buffer.trim()) parseSseBlock(buffer, handlers)
    })
    .catch((err) => {
      if (err.name === 'AbortError') return
      handlers.onError?.(err)
      throw err
    })

  return {
    abort: () => controller.abort(),
    done
  }
}

export async function sendChat(message, sessionId) {
  const res = await api.post('/api/ai/chat', { message, sessionId }, { timeout: 120000 })
  return unwrap(res)
}

export async function fetchSessions() {
  return unwrap(await api.get('/api/ai/sessions'))
}

export async function fetchSession(id) {
  return unwrap(await api.get(`/api/ai/sessions/${id}`))
}

export async function createSession() {
  return unwrap(await api.post('/api/ai/sessions'))
}

export async function deleteSession(id) {
  return unwrap(await api.delete(`/api/ai/sessions/${id}`))
}
