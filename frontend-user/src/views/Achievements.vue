<template>
  <div class="page-container">
    <header class="page-header">
      <h1 class="section-title">我的成就</h1>
      <p class="section-subtitle">完成趣味测验解锁的徽章将展示在这里。</p>
    </header>

    <div v-loading="loading" class="badge-grid">
      <article
        v-for="badge in badges"
        :key="badge.badgeId"
        class="badge-item glass-card"
      >
        <span class="badge-emoji">{{ badge.iconUrl || '🏅' }}</span>
        <h3>{{ badge.name }}</h3>
        <p>{{ badge.description }}</p>
        <time class="earned">{{ formatDate(badge.earnedAt) }} 获得</time>
      </article>
      <p v-if="!loading && badges.length === 0" class="empty">
        还没有徽章，去
        <router-link to="/quizzes">趣味测验</router-link>
        挑战一下吧！
      </p>
    </div>

    <div class="footer-actions">
      <router-link to="/quizzes">
        <el-button type="primary">继续测验</el-button>
      </router-link>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { fetchMyBadges } from '../services/quiz'

export default {
  name: 'AchievementsView',
  setup() {
    const loading = ref(true)
    const badges = ref([])

    const formatDate = (iso) => {
      if (!iso) return ''
      const d = new Date(iso)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    }

    onMounted(async () => {
      try {
        badges.value = await fetchMyBadges()
      } finally {
        loading.value = false
      }
    })

    return { loading, badges, formatDate }
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 2.5rem;
  text-align: center;
}

.badge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1.25rem;
  max-width: 900px;
  margin: 0 auto;
}

.badge-item {
  padding: 1.5rem;
  text-align: center;
}

.badge-emoji {
  font-size: 3rem;
  display: block;
  margin-bottom: 0.75rem;
}

.badge-item h3 {
  margin: 0 0 0.5rem;
}

.badge-item p {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.9rem;
  line-height: 1.5;
}

.earned {
  display: block;
  margin-top: 0.75rem;
  font-size: 0.8rem;
  color: var(--accent-cyan);
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  color: var(--text-muted);
}

.empty a {
  color: var(--accent-cyan);
}

.footer-actions {
  text-align: center;
  margin-top: 2.5rem;
}
</style>
