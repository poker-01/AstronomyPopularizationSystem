<template>
  <div class="starfield" ref="container" aria-hidden="true"></div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'

export default {
  name: 'StarfieldBg',
  setup() {
    const container = ref(null)
    let stars = []

    onMounted(() => {
      if (!container.value) return
      for (let i = 0; i < 120; i++) {
        const el = document.createElement('div')
        el.className = 'star'
        const size = Math.random() * 2 + 0.5
        el.style.cssText = `
          width:${size}px;height:${size}px;
          left:${Math.random() * 100}%;
          top:${Math.random() * 100}%;
          animation-delay:${Math.random() * 5}s;
          opacity:${0.2 + Math.random() * 0.6};
        `
        container.value.appendChild(el)
        stars.push(el)
      }
    })

    onUnmounted(() => {
      stars.forEach(el => el.remove())
      stars = []
    })

    return { container }
  }
}
</script>

<style scoped>
.starfield {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background: var(--gradient-hero);
  background-attachment: fixed;
}

.star {
  position: absolute;
  background: #fff;
  border-radius: 50%;
  animation: twinkle 4s ease-in-out infinite;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.2; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.15); }
}
</style>
