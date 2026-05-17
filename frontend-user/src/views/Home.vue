<template>
  <div>
    <section class="hero">
      <div class="hero-bg" v-if="apodImage" :style="{ backgroundImage: `url(${apodImage})` }"></div>
      <div class="hero-overlay"></div>
      <div class="hero-content page-container">
        <p class="hero-tag">NASA Astronomy Picture of the Day</p>
        <h1 class="hero-title">{{ apodTitle }}</h1>
        <p class="hero-desc">{{ apodExcerpt }}</p>
        <div class="hero-actions">
          <router-link to="/planets" class="btn-primary">探索太阳系</router-link>
          <router-link to="/explore" class="btn-ghost">人类航天史</router-link>
        </div>
        <p v-if="apodDate" class="hero-meta">{{ apodDate }} · {{ apodCopyright }}</p>
      </div>
    </section>

    <section v-if="upcomingToday.length || upcomingWeek.length" class="page-container section upcoming-section">
      <h2 class="section-title">天象速览</h2>
      <p class="section-subtitle">今日与本周值得关注的天文事件</p>
      <div v-if="upcomingToday.length" class="upcoming-block">
        <h3 class="upcoming-label today-label">今日</h3>
        <div class="upcoming-list">
          <router-link
            v-for="ev in upcomingToday"
            :key="'t-' + ev.id"
            to="/calendar"
            class="upcoming-chip glass-card"
          >
            <span class="chip-type">{{ ev.eventTypeLabel }}</span>
            <span class="chip-title">{{ ev.title }}</span>
          </router-link>
        </div>
      </div>
      <div v-if="upcomingWeek.length" class="upcoming-block">
        <h3 class="upcoming-label">本周</h3>
        <div class="upcoming-list">
          <router-link
            v-for="ev in upcomingWeekFiltered"
            :key="'w-' + ev.id"
            to="/calendar"
            class="upcoming-chip glass-card"
          >
            <span class="chip-type">{{ ev.eventTypeLabel }}</span>
            <span class="chip-title">{{ ev.title }}</span>
          </router-link>
        </div>
      </div>
      <div class="section-cta">
        <router-link to="/calendar" class="btn-ghost">打开天文日历</router-link>
      </div>
    </section>

    <section class="page-container section">
      <h2 class="section-title">科普精选</h2>
      <p class="section-subtitle">深入理解宇宙——从太阳系形成到黑洞与观测实践</p>
      <div v-loading="articlesLoading" class="article-grid">
        <router-link
          v-for="item in articles"
          :key="item.id"
          :to="`/articles/${item.slug}`"
          class="glass-card article-card"
        >
          <div class="card-cover" :style="coverStyle(item.coverUrl)"></div>
          <div class="card-body">
            <span class="card-tag">{{ item.categoryName }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.summary }}</p>
          </div>
        </router-link>
      </div>
      <div class="section-cta">
        <router-link to="/articles" class="btn-primary">浏览全部文章</router-link>
      </div>
    </section>

    <section class="page-container section planets-preview">
      <h2 class="section-title">八大行星</h2>
      <p class="section-subtitle">行星影像来自 NASA Image and Video Library，科普数据由本站整理</p>
      <div v-loading="planetsLoading" class="planet-orbit">
        <router-link
          v-for="p in planetsPreview"
          :key="p.id"
          :to="`/planets#${p.id}`"
          class="planet-chip glass-card"
        >
          <img v-if="p.imageUrl" :src="p.imageUrl" :alt="p.name" loading="lazy" />
          <span v-else class="planet-placeholder">{{ p.name[0] }}</span>
          <span class="planet-name">{{ p.name }}</span>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { fetchApod, fetchPlanets } from '../services/nasa'
import { fetchArticles } from '../services/content'
import { fetchUpcoming } from '../services/calendar'

export default {
  name: 'HomeView',
  setup() {
    const apodTitle = ref('探索宇宙')
    const apodExcerpt = ref('加载 NASA 每日天文图…')
    const apodImage = ref('')
    const apodDate = ref('')
    const apodCopyright = ref('')
    const articles = ref([])
    const articlesLoading = ref(true)
    const planetsPreview = ref([])
    const planetsLoading = ref(true)
    const upcomingToday = ref([])
    const upcomingWeek = ref([])

    const upcomingWeekFiltered = computed(() => {
      const todayIds = new Set(upcomingToday.value.map(e => e.id))
      return upcomingWeek.value.filter(e => !todayIds.has(e.id))
    })

    const coverStyle = (url) => ({
      backgroundImage: url ? `url(${url})` : 'linear-gradient(135deg, #1a0a2e, #0d1b3d)'
    })

    const loadApod = async () => {
      try {
        const data = await fetchApod()
        apodTitle.value = data.title || '每日天文图'
        apodDate.value = data.date || ''
        apodCopyright.value = data.copyright || 'Image: NASA'
        const explanation = data.explanation || ''
        apodExcerpt.value = explanation.length > 180 ? explanation.slice(0, 180) + '…' : explanation
        apodImage.value = data.url || data.hdurl || ''
        if (data.media_type === 'video' && data.url) {
          apodImage.value = 'https://www.nasa.gov/wp-content/uploads/2023/03/main_pillars_of_creation-m16-4029.jpg'
        }
      } catch {
        apodTitle.value = '仰望星空'
        apodExcerpt.value = '连接 NASA 数据源失败，您仍可浏览太阳系与科普文库。'
        apodImage.value = 'https://www.nasa.gov/wp-content/uploads/2023/03/main_pillars_of_creation-m16-4029.jpg'
      }
    }

    const loadArticles = async () => {
      articlesLoading.value = true
      try {
        const data = await fetchArticles({ page: 0, size: 3 })
        articles.value = data.content || []
      } finally {
        articlesLoading.value = false
      }
    }

    const loadPlanets = async () => {
      planetsLoading.value = true
      try {
        const list = await fetchPlanets()
        planetsPreview.value = (list || []).slice(0, 8)
      } catch {
        planetsPreview.value = []
      } finally {
        planetsLoading.value = false
      }
    }

    const loadUpcoming = async () => {
      try {
        const data = await fetchUpcoming()
        upcomingToday.value = data.today || []
        upcomingWeek.value = data.thisWeek || []
      } catch {
        upcomingToday.value = []
        upcomingWeek.value = []
      }
    }

    onMounted(() => {
      loadApod()
      loadArticles()
      loadPlanets()
      loadUpcoming()
    })

    return {
      apodTitle,
      apodExcerpt,
      apodImage,
      apodDate,
      apodCopyright,
      articles,
      articlesLoading,
      planetsPreview,
      planetsLoading,
      upcomingToday,
      upcomingWeek,
      upcomingWeekFiltered,
      coverStyle
    }
  }
}
</script>

<style scoped>
.hero {
  position: relative;
  min-height: 72vh;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  filter: brightness(0.45);
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, #030014 10%, transparent 55%),
    linear-gradient(to right, rgba(26, 10, 46, 0.7), transparent 60%);
}

.hero-content {
  position: relative;
  z-index: 1;
  padding-top: 4rem;
  padding-bottom: 3rem;
}

.hero-tag {
  text-transform: uppercase;
  letter-spacing: 0.15em;
  font-size: 0.75rem;
  color: var(--accent-cyan);
  margin-bottom: 0.75rem;
}

.hero-title {
  font-size: clamp(2rem, 6vw, 3.5rem);
  font-weight: 800;
  margin: 0 0 1rem;
  max-width: 800px;
  line-height: 1.15;
}

.hero-desc {
  font-size: 1.05rem;
  color: #c7d2fe;
  max-width: 640px;
  line-height: 1.7;
  margin-bottom: 1.5rem;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1rem;
}

.hero-meta {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.section {
  padding-top: 3rem;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.article-card {
  text-decoration: none;
  color: inherit;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.card-cover {
  height: 160px;
  background-size: cover;
  background-position: center;
}

.card-body {
  padding: 1.25rem;
}

.card-tag {
  font-size: 0.75rem;
  color: var(--accent-cyan);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.card-body h3 {
  margin: 0.5rem 0;
  font-size: 1.15rem;
}

.card-body p {
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-muted);
  line-height: 1.5;
}

.section-cta {
  margin-top: 2rem;
  text-align: center;
}

.planet-orbit {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 1rem;
}

.planet-chip {
  padding: 1rem;
  text-align: center;
  text-decoration: none;
  color: inherit;
}

.planet-chip img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 0.5rem;
}

.planet-placeholder {
  display: inline-flex;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-indigo), var(--accent-violet));
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.planet-name {
  display: block;
  font-weight: 600;
}

.upcoming-section {
  padding-top: 2rem;
}

.upcoming-block {
  margin-bottom: 1.5rem;
}

.upcoming-label {
  font-size: 0.95rem;
  color: var(--text-muted);
  margin: 0 0 0.75rem;
  font-weight: 600;
}

.today-label {
  color: var(--accent-cyan);
}

.upcoming-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.upcoming-chip {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.75rem 1rem;
  text-decoration: none;
  color: inherit;
  min-width: 180px;
}

.chip-type {
  font-size: 0.75rem;
  color: var(--accent-violet);
}

.chip-title {
  font-weight: 600;
  font-size: 0.95rem;
}
</style>
