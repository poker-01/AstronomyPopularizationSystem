<template>
  <div class="page-container">
    <header class="page-header">
      <h1 class="section-title">人类探索宇宙历程</h1>
      <p class="section-subtitle">
        从第一颗人造卫星到詹姆斯·韦布空间望远镜——记录改变人类宇宙观的里程碑事件。
        风格灵感来自 NASA 任务时间线。
      </p>
    </header>

    <div v-loading="loading" class="timeline">
      <article
        v-for="(event, index) in events"
        :key="event.id"
        class="timeline-item glass-card"
        :class="{ reverse: index % 2 === 1 }"
      >
        <div class="timeline-year">
          <span class="year">{{ event.year }}</span>
          <span class="month">{{ event.month }}</span>
        </div>
        <div class="timeline-media">
          <img :src="event.imageUrl" :alt="event.title" loading="lazy" />
        </div>
        <div class="timeline-body">
          <span class="event-category">{{ event.category }}</span>
          <h3>{{ event.title }}</h3>
          <p>{{ event.description }}</p>
        </div>
      </article>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { fetchExplorationEvents } from '../services/exploration'

export default {
  name: 'ExploreView',
  setup() {
    const loading = ref(true)
    const events = ref([])

    onMounted(async () => {
      try {
        events.value = await fetchExplorationEvents()
      } finally {
        loading.value = false
      }
    })

    return { loading, events }
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 3rem;
  text-align: center;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  max-width: 900px;
  margin: 0 auto;
}

.timeline-item {
  display: grid;
  grid-template-columns: 100px 200px 1fr;
  gap: 1.5rem;
  padding: 1.5rem;
  align-items: center;
}

.timeline-item.reverse {
  grid-template-columns: 1fr 200px 100px;
}

.timeline-item.reverse .timeline-year {
  order: 3;
  text-align: right;
}

.timeline-item.reverse .timeline-media {
  order: 2;
}

.timeline-item.reverse .timeline-body {
  order: 1;
  text-align: right;
}

.timeline-year .year {
  display: block;
  font-size: 2rem;
  font-weight: 800;
  color: var(--accent-violet);
  line-height: 1;
}

.timeline-year .month {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.timeline-media img {
  width: 100%;
  height: 140px;
  object-fit: cover;
  border-radius: 10px;
}

.event-category {
  font-size: 0.75rem;
  color: var(--accent-cyan);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.timeline-body h3 {
  margin: 0.35rem 0 0.5rem;
  font-size: 1.25rem;
}

.timeline-body p {
  margin: 0;
  color: #c7d2fe;
  line-height: 1.65;
}

@media (max-width: 768px) {
  .timeline-item,
  .timeline-item.reverse {
    grid-template-columns: 1fr;
    text-align: left !important;
  }

  .timeline-item.reverse .timeline-year,
  .timeline-item.reverse .timeline-media,
  .timeline-item.reverse .timeline-body {
    order: unset;
    text-align: left;
  }
}
</style>
