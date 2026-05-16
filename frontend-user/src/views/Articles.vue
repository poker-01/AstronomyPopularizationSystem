<template>
  <div class="page-container">
    <header class="page-header">
      <h1 class="section-title">科普文库</h1>
      <p class="section-subtitle">精选天文科普文章，涵盖太阳系、深空天体与观测实践</p>
    </header>

    <div class="filters">
      <el-radio-group v-model="categoryId" @change="reload">
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button v-for="c in categories" :key="c.id" :label="c.id">{{ c.name }}</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading" class="article-grid">
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
          <span class="card-meta">{{ formatDate(item.publishedAt) }} · {{ item.viewCount }} 阅读</span>
        </div>
      </router-link>
    </div>

    <div class="pager" v-if="total > size">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page + 1"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { fetchCategories, fetchArticles } from '../services/content'

export default {
  name: 'ArticlesView',
  setup() {
    const categories = ref([])
    const articles = ref([])
    const loading = ref(true)
    const categoryId = ref(null)
    const page = ref(0)
    const size = ref(9)
    const total = ref(0)

    const coverStyle = (url) => ({
      backgroundImage: url ? `url(${url})` : 'linear-gradient(135deg, #1a0a2e, #0d1b3d)'
    })

    const formatDate = (v) => (v ? new Date(v).toLocaleDateString('zh-CN') : '')

    const load = async () => {
      loading.value = true
      try {
        const data = await fetchArticles({
          page: page.value,
          size: size.value,
          categoryId: categoryId.value || undefined
        })
        articles.value = data.content || []
        total.value = data.totalElements || 0
      } finally {
        loading.value = false
      }
    }

    const reload = () => {
      page.value = 0
      load()
    }

    const onPage = (p) => {
      page.value = p - 1
      load()
    }

    onMounted(async () => {
      categories.value = await fetchCategories()
      load()
    })

    return { categories, articles, loading, categoryId, page, size, total, coverStyle, formatDate, reload, onPage }
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 2rem;
}

.filters {
  margin-bottom: 2rem;
}

.filters :deep(.el-radio-button__inner) {
  background: var(--glass-bg);
  border-color: var(--glass-border) !important;
  color: var(--text-muted);
}

.filters :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, var(--accent-indigo), var(--accent-violet));
  border-color: transparent !important;
  color: #fff;
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
}

.card-cover {
  height: 180px;
  background-size: cover;
  background-position: center;
}

.card-body {
  padding: 1.25rem;
}

.card-tag {
  font-size: 0.75rem;
  color: var(--accent-cyan);
}

.card-body h3 {
  margin: 0.5rem 0;
}

.card-body p {
  color: var(--text-muted);
  font-size: 0.9rem;
  line-height: 1.5;
}

.card-meta {
  display: block;
  margin-top: 0.75rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.pager {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
}
</style>
