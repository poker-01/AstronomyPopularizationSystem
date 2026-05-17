<template>
  <teleport to="body">
    <transition name="badge-fade">
      <div v-if="visible" class="badge-overlay" @click.self="dismiss">
        <div class="badge-card glass-card">
          <div class="badge-glow"></div>
          <p class="badge-label">🎉 获得新徽章</p>
          <span class="badge-icon">{{ current?.iconUrl || '🏅' }}</span>
          <h3>{{ current?.name }}</h3>
          <p class="badge-desc">{{ current?.description }}</p>
          <div class="badge-actions">
            <el-button v-if="queue.length > 0" @click="next">下一个 ({{ queue.length }})</el-button>
            <el-button type="primary" @click="dismiss">太棒了</el-button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script>
import { ref, watch } from 'vue'

export default {
  name: 'BadgeUnlock',
  props: {
    badges: { type: Array, default: () => [] }
  },
  emits: ['done'],
  setup(props, { emit }) {
    const visible = ref(false)
    const current = ref(null)
    const queue = ref([])

    const showQueue = (list) => {
      if (!list || list.length === 0) return
      queue.value = [...list.slice(1)]
      current.value = list[0]
      visible.value = true
    }

    const next = () => {
      if (queue.value.length === 0) {
        dismiss()
        return
      }
      current.value = queue.value[0]
      queue.value = queue.value.slice(1)
    }

    const dismiss = () => {
      visible.value = false
      current.value = null
      queue.value = []
      emit('done')
    }

    watch(() => props.badges, (val) => {
      if (val && val.length) showQueue(val)
    }, { deep: true })

    return { visible, current, queue, next, dismiss }
  }
}
</script>

<style scoped>
.badge-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(3, 0, 20, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
}

.badge-card {
  text-align: center;
  padding: 2.5rem 2rem;
  max-width: 360px;
  width: 100%;
  position: relative;
  overflow: hidden;
  animation: badge-pop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.badge-glow {
  position: absolute;
  inset: -50%;
  background: radial-gradient(circle, rgba(123, 104, 238, 0.35) 0%, transparent 70%);
  animation: badge-spin 8s linear infinite;
  pointer-events: none;
}

.badge-label {
  color: var(--accent-cyan);
  font-size: 0.9rem;
  letter-spacing: 0.1em;
  margin: 0 0 1rem;
  position: relative;
}

.badge-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 0.75rem;
  position: relative;
  animation: badge-bounce 1.2s ease-in-out infinite;
}

.badge-card h3 {
  margin: 0 0 0.5rem;
  font-size: 1.5rem;
  position: relative;
}

.badge-desc {
  color: var(--text-muted);
  font-size: 0.95rem;
  line-height: 1.5;
  margin: 0 0 1.5rem;
  position: relative;
}

.badge-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
  position: relative;
}

@keyframes badge-pop {
  from { opacity: 0; transform: scale(0.6); }
  to { opacity: 1; transform: scale(1); }
}

@keyframes badge-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes badge-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.badge-fade-enter-active,
.badge-fade-leave-active {
  transition: opacity 0.3s;
}
.badge-fade-enter-from,
.badge-fade-leave-to {
  opacity: 0;
}
</style>
