<template>
  <div
    class="ai-widget"
    :style="widgetStyle"
    @mousedown.stop
  >
    <!-- 对话面板 -->
    <transition name="panel-fade">
      <div v-if="open" class="chat-panel glass-card" @mousedown.stop>
        <header class="panel-header">
          <div class="panel-title">
            <span class="dot"></span>
            <span>天文 AI 助手</span>
            <small>DeepSeek</small>
          </div>
          <div class="panel-actions">
            <el-button link @click="startNewChat" title="新对话">＋</el-button>
            <el-button link @click="open = false">✕</el-button>
          </div>
        </header>

        <div v-if="!loggedIn" class="login-hint">
          <p>登录后即可向 AI 提问天文问题</p>
          <router-link to="/login" class="btn-primary" @click="open = false">去登录</router-link>
        </div>

        <template v-else>
          <div ref="messageBox" class="messages">
            <div v-if="messages.length === 0" class="welcome-msg">
              <p>你好，我是 COSMOS 天文助手 🌌</p>
              <p>可以问我关于行星、恒星、黑洞、观测技巧等问题。</p>
            </div>
            <div
              v-for="msg in messages"
              :key="msg.id || msg._key"
              class="msg-row"
              :class="msg.role"
            >
              <div class="bubble">{{ msg.content }}</div>
            </div>
            <div v-if="loading && !streaming" class="msg-row assistant">
              <div class="bubble typing">思考中…</div>
            </div>
          </div>

          <footer class="input-area">
            <el-input
              v-model="input"
              type="textarea"
              :rows="2"
              placeholder="输入天文问题…"
              resize="none"
              @keydown.enter.exact.prevent="send"
            />
            <el-button
              type="primary"
              class="send-btn"
              :loading="loading"
              :disabled="!input.trim()"
              @click="send"
            >
              发送
            </el-button>
          </footer>
        </template>
      </div>
    </transition>

    <!-- 圆形拖动按钮 -->
    <button
      class="fab"
      :class="{ open, dragging }"
      @pointerdown="onFabPointerDown"
      @click="onFabClick"
      title="天文 AI 问答（可拖动）"
    >
      <span v-if="!open" class="fab-icon">✦</span>
      <span v-else class="fab-icon">▼</span>
    </button>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { getToken } from '../services/auth'
import { sendChatStream, createSession } from '../services/ai'
import { ElMessage } from 'element-plus'

const POS_KEY = 'ai_widget_pos'

export default {
  name: 'AiChatWidget',
  setup() {
    const open = ref(false)
    const loading = ref(false)
    const streaming = ref(false)
    const input = ref('')
    const messages = ref([])
    const sessionId = ref(null)
    const messageBox = ref(null)
    let activeStream = null

    const posX = ref(null)
    const posY = ref(null)
    const dragging = ref(false)
    const dragMoved = ref(false)

    let dragStart = null

    const loggedIn = computed(() => !!getToken())

    const widgetStyle = computed(() => {
      if (posX.value != null && posY.value != null) {
        return {
          left: `${posX.value}px`,
          top: `${posY.value}px`,
          right: 'auto',
          bottom: 'auto'
        }
      }
      return {}
    })

    const loadPosition = () => {
      try {
        const raw = localStorage.getItem(POS_KEY)
        if (!raw) return
        const { x, y } = JSON.parse(raw)
        const maxX = window.innerWidth - 72
        const maxY = window.innerHeight - 72
        posX.value = Math.min(Math.max(0, x), maxX)
        posY.value = Math.min(Math.max(0, y), maxY)
      } catch {
        /* ignore */
      }
    }

    const savePosition = () => {
      if (posX.value == null || posY.value == null) return
      localStorage.setItem(POS_KEY, JSON.stringify({ x: posX.value, y: posY.value }))
    }

    const clampPosition = () => {
      if (posX.value == null || posY.value == null) return
      const maxX = window.innerWidth - 72
      const maxY = window.innerHeight - 72
      posX.value = Math.min(Math.max(8, posX.value), maxX)
      posY.value = Math.min(Math.max(8, posY.value), maxY)
    }

    const onPointerMove = (e) => {
      if (!dragStart) return
      const dx = e.clientX - dragStart.x
      const dy = e.clientY - dragStart.y
      if (Math.abs(dx) > 4 || Math.abs(dy) > 4) {
        dragMoved.value = true
        dragging.value = true
      }
      if (!dragging.value) return
      posX.value = dragStart.posX + dx
      posY.value = dragStart.posY + dy
      clampPosition()
    }

    const onPointerUp = () => {
      if (dragging.value) savePosition()
      dragStart = null
      dragging.value = false
    }

    const onFabPointerDown = (e) => {
      if (e.button !== 0 && e.pointerType === 'mouse') return
      e.currentTarget.setPointerCapture?.(e.pointerId)
      const rect = e.currentTarget.getBoundingClientRect()
      const parent = e.currentTarget.closest('.ai-widget')
      const parentRect = parent?.getBoundingClientRect()
      if (posX.value == null && parentRect) {
        posX.value = parentRect.left
        posY.value = parentRect.top
      }
      dragStart = {
        x: e.clientX,
        y: e.clientY,
        posX: posX.value ?? rect.left,
        posY: posY.value ?? rect.top
      }
      dragMoved.value = false
    }

    const onFabClick = () => {
      if (dragMoved.value) return
      open.value = !open.value
    }

    const scrollToBottom = () => {
      nextTick(() => {
        if (messageBox.value) {
          messageBox.value.scrollTop = messageBox.value.scrollHeight
        }
      })
    }

    watch(open, (v) => {
      if (v) scrollToBottom()
    })

    const startNewChat = async () => {
      if (!loggedIn.value) return
      try {
        const session = await createSession()
        sessionId.value = session.id
        messages.value = []
        input.value = ''
      } catch (err) {
        ElMessage.error(err.message || '创建会话失败')
      }
    }

    const send = async () => {
      if (!loggedIn.value) {
        ElMessage.warning('请先登录')
        return
      }
      const text = input.value.trim()
      if (!text || loading.value) return

      messages.value.push({ role: 'user', content: text, _key: Date.now() })
      const assistantKey = Date.now() + 1
      messages.value.push({ role: 'assistant', content: '', _key: assistantKey })
      input.value = ''
      loading.value = true
      streaming.value = false
      scrollToBottom()

      activeStream?.abort()
      activeStream = sendChatStream(text, sessionId.value, {
        onMeta: (meta) => {
          if (meta.sessionId != null) sessionId.value = meta.sessionId
        },
        onDelta: (chunk) => {
          streaming.value = true
          const idx = messages.value.findIndex(m => m._key === assistantKey)
          if (idx >= 0) {
            messages.value[idx] = {
              ...messages.value[idx],
              content: messages.value[idx].content + chunk
            }
          }
          scrollToBottom()
        },
        onDone: (data) => {
          sessionId.value = data.sessionId
          messages.value = (data.messages || []).map(m => ({
            id: m.id,
            role: m.role,
            content: m.content
          }))
        },
        onError: (err) => {
          ElMessage.error(err.message || '发送失败')
          messages.value = messages.value.filter(m => m._key !== assistantKey)
          const lastUser = messages.value[messages.value.length - 1]
          if (lastUser?.role === 'user' && lastUser.content === text) {
            messages.value.pop()
          }
          input.value = text
        }
      })

      try {
        await activeStream.done
      } catch {
        /* onError 已处理 */
      } finally {
        loading.value = false
        streaming.value = false
        activeStream = null
        scrollToBottom()
      }
    }

    onMounted(() => {
      loadPosition()
      window.addEventListener('pointermove', onPointerMove)
      window.addEventListener('pointerup', onPointerUp)
      window.addEventListener('resize', clampPosition)
    })

    onUnmounted(() => {
      activeStream?.abort()
      window.removeEventListener('pointermove', onPointerMove)
      window.removeEventListener('pointerup', onPointerUp)
      window.removeEventListener('resize', clampPosition)
    })

    return {
      open,
      loading,
      streaming,
      input,
      messages,
      messageBox,
      loggedIn,
      widgetStyle,
      dragging,
      onFabPointerDown,
      onFabClick,
      startNewChat,
      send
    }
  }
}
</script>

<style scoped>
.ai-widget {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  user-select: none;
}

.fab {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 2px solid rgba(123, 104, 238, 0.6);
  background: linear-gradient(145deg, #1a0a2e 0%, #312e81 50%, #4f46e5 100%);
  color: #fff;
  cursor: grab;
  touch-action: none;
  box-shadow: 0 8px 32px rgba(79, 70, 229, 0.45), 0 0 0 4px rgba(79, 70, 229, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s, box-shadow 0.2s;
  flex-shrink: 0;
}

.fab:hover {
  transform: scale(1.06);
  box-shadow: 0 12px 40px rgba(123, 104, 238, 0.55);
}

.fab.dragging {
  cursor: grabbing;
  transform: scale(1.08);
}

.fab.open {
  background: linear-gradient(145deg, #312e81, #6d28d9);
}

.fab-icon {
  font-size: 1.5rem;
  line-height: 1;
  pointer-events: none;
}

.chat-panel {
  width: min(380px, calc(100vw - 48px));
  height: 480px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid var(--glass-border);
  flex-shrink: 0;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 0.95rem;
}

.panel-title small {
  font-weight: 400;
  color: var(--text-muted);
  font-size: 0.75rem;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-cyan);
  box-shadow: 0 0 8px var(--accent-cyan);
}

.panel-actions :deep(.el-button) {
  color: var(--text-muted);
  padding: 4px 8px;
}

.login-hint {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 2rem;
  text-align: center;
  color: var(--text-muted);
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.welcome-msg {
  text-align: center;
  color: var(--text-muted);
  font-size: 0.9rem;
  line-height: 1.6;
  padding: 1rem 0.5rem;
}

.msg-row {
  display: flex;
}

.msg-row.user {
  justify-content: flex-end;
}

.msg-row.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: 88%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 0.9rem;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-row.user .bubble {
  background: linear-gradient(135deg, var(--accent-indigo), var(--accent-violet));
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-row.assistant .bubble {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid var(--glass-border);
  color: #e0e7ff;
  border-bottom-left-radius: 4px;
}

.bubble.typing {
  color: var(--text-muted);
  font-style: italic;
}

.input-area {
  padding: 10px 12px 12px;
  border-top: 1px solid var(--glass-border);
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

.input-area :deep(.el-textarea__inner) {
  background: rgba(0, 0, 0, 0.25);
  border-color: var(--glass-border);
  color: #fff;
}

.send-btn {
  align-self: flex-end;
  background: linear-gradient(135deg, var(--accent-indigo), var(--accent-violet)) !important;
  border: none !important;
}

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.96);
}
</style>
