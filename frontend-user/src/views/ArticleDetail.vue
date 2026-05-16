<template>
  <div class="page-container" v-loading="loading">
    <article v-if="article" class="article-detail">
      <router-link to="/articles" class="back">← 返回文库</router-link>
      <header class="article-header glass-card">
        <span class="tag">{{ article.categoryName }}</span>
        <h1>{{ article.title }}</h1>
        <p class="summary">{{ article.summary }}</p>
        <p class="meta">{{ formatDate(article.publishedAt) }} · {{ article.viewCount }} 次阅读</p>
        <div v-if="article.coverUrl" class="cover" :style="{ backgroundImage: `url(${article.coverUrl})` }"></div>
      </header>
      <div class="article-content glass-card markdown-body" v-html="htmlContent"></div>
    </article>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchArticle } from '../services/content'
import { renderSimpleMarkdown } from '../utils/markdown'

export default {
  name: 'ArticleDetailView',
  setup() {
    const route = useRoute()
    const loading = ref(true)
    const article = ref(null)

    const htmlContent = computed(() =>
      article.value ? renderSimpleMarkdown(article.value.content) : ''
    )

    const formatDate = (v) => (v ? new Date(v).toLocaleDateString('zh-CN') : '')

    const load = async () => {
      loading.value = true
      try {
        article.value = await fetchArticle(route.params.slug)
      } finally {
        loading.value = false
      }
    }

    onMounted(load)
    watch(() => route.params.slug, load)

    return { loading, article, htmlContent, formatDate }
  }
}
</script>

<style scoped>
.back {
  display: inline-block;
  margin-bottom: 1.5rem;
  color: var(--accent-cyan);
  text-decoration: none;
}

.article-header {
  padding: 2rem;
  margin-bottom: 1.5rem;
}

.tag {
  color: var(--accent-cyan);
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.article-header h1 {
  margin: 0.5rem 0;
  font-size: clamp(1.75rem, 4vw, 2.5rem);
}

.summary {
  color: var(--text-muted);
  line-height: 1.6;
}

.meta {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.cover {
  margin-top: 1.5rem;
  height: 280px;
  border-radius: 12px;
  background-size: cover;
  background-position: center;
}

.article-content {
  padding: 2rem;
}
</style>
