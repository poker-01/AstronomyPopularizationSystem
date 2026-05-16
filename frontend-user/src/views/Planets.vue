<template>
  <div class="page-container">
    <header class="page-header">
      <h1 class="section-title">太阳系行星</h1>
      <p class="section-subtitle">
        探索八大行星与太阳系的结构。行星图片来自 NASA Image and Video Library，由后端代理获取并缓存。
      </p>
    </header>

    <div v-loading="loading" class="planet-layout">
      <aside class="planet-list glass-card">
        <button
          v-for="p in planets"
          :key="p.id"
          :id="p.id"
          class="planet-tab"
          :class="{ active: selected?.id === p.id }"
          @click="selected = p"
        >
          <span class="tab-order">{{ p.order }}</span>
          <span>{{ p.name }}</span>
          <small>{{ p.nameEn }}</small>
        </button>
      </aside>

      <article v-if="selected" class="planet-detail glass-card">
        <div class="detail-visual">
          <img v-if="selected.imageUrl" :src="selected.imageUrl" :alt="selected.name" />
          <div v-else class="visual-fallback">{{ selected.name }}</div>
        </div>
        <div class="detail-content">
          <p class="detail-type">{{ selected.type }}</p>
          <h2>{{ selected.name }} <span>{{ selected.nameEn }}</span></h2>
          <p class="detail-desc">{{ selected.description }}</p>
          <dl class="stats">
            <div><dt>距太阳</dt><dd>{{ selected.distanceFromSun }}</dd></div>
            <div><dt>公转周期</dt><dd>{{ selected.orbitalPeriod }}</dd></div>
            <div><dt>直径</dt><dd>{{ selected.diameter }}</dd></div>
            <div><dt>已知卫星</dt><dd>{{ selected.moons }}</dd></div>
          </dl>
          <p v-if="selected.imageCredit" class="credit">{{ selected.imageCredit }}</p>
        </div>
      </article>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchPlanets } from '../services/nasa'

export default {
  name: 'PlanetsView',
  setup() {
    const route = useRoute()
    const loading = ref(true)
    const planets = ref([])
    const selected = ref(null)

    const load = async () => {
      loading.value = true
      try {
        planets.value = await fetchPlanets()
        const hash = route.hash?.replace('#', '')
        selected.value = planets.value.find(p => p.id === hash) || planets.value[0]
      } catch {
        planets.value = []
        selected.value = null
      } finally {
        loading.value = false
      }
    }

    watch(() => route.hash, (hash) => {
      const id = hash?.replace('#', '')
      if (id && planets.value.length) {
        const found = planets.value.find(p => p.id === id)
        if (found) selected.value = found
      }
    })

    onMounted(load)

    return { loading, planets, selected }
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 2rem;
}

.planet-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 1.5rem;
  align-items: start;
}

.planet-list {
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  position: sticky;
  top: 88px;
}

.planet-tab {
  display: grid;
  grid-template-columns: 28px 1fr;
  grid-template-rows: auto auto;
  gap: 0 0.5rem;
  text-align: left;
  padding: 0.65rem 0.75rem;
  border: 1px solid transparent;
  border-radius: 10px;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.planet-tab small {
  grid-column: 2;
  font-size: 0.7rem;
  opacity: 0.7;
}

.tab-order {
  grid-row: span 2;
  align-self: center;
  font-weight: 700;
  color: var(--accent-violet);
}

.planet-tab.active,
.planet-tab:hover {
  background: rgba(79, 70, 229, 0.2);
  border-color: var(--glass-border);
  color: #fff;
}

.planet-detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  overflow: hidden;
}

.detail-visual img {
  width: 100%;
  height: 100%;
  min-height: 320px;
  object-fit: cover;
}

.visual-fallback {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  background: linear-gradient(160deg, #1a0a2e, #0d1b3d);
}

.detail-content {
  padding: 2rem;
}

.detail-type {
  color: var(--accent-cyan);
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.detail-content h2 {
  margin: 0.25rem 0 1rem;
  font-size: 2rem;
}

.detail-content h2 span {
  font-size: 1rem;
  color: var(--text-muted);
  font-weight: 400;
}

.detail-desc {
  line-height: 1.75;
  color: #c7d2fe;
}

.stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin: 1.5rem 0;
}

.stats dt {
  font-size: 0.75rem;
  color: var(--text-muted);
  text-transform: uppercase;
}

.stats dd {
  margin: 0.2rem 0 0;
  font-weight: 600;
}

.credit {
  font-size: 0.75rem;
  color: var(--text-muted);
}

@media (max-width: 900px) {
  .planet-layout {
    grid-template-columns: 1fr;
  }

  .planet-list {
    position: static;
    flex-direction: row;
    flex-wrap: wrap;
  }

  .planet-tab {
    flex: 1 1 140px;
  }

  .planet-detail {
    grid-template-columns: 1fr;
  }
}
</style>
