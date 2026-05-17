<template>
  <div class="page-container">
    <header class="page-header">
      <div class="header-row">
        <div>
          <h1 class="section-title">社区论坛</h1>
          <p class="section-subtitle">分享天文见闻、观测心得与科普讨论</p>
        </div>
        <router-link v-if="loggedIn" to="/forum/new" class="btn-create">发帖</router-link>
        <router-link v-else to="/login" class="btn-create btn-muted">登录后发帖</router-link>
      </div>
    </header>

    <div class="tabs-row">
      <el-radio-group v-model="tab" @change="onTabChange">
        <el-radio-button label="all">全部帖子</el-radio-button>
        <el-radio-button v-if="loggedIn" label="following">关注动态</el-radio-button>
        <el-radio-button v-if="loggedIn" label="pending">我的待审</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading" class="post-list">
      <router-link
        v-for="item in posts"
        :key="item.id"
        :to="`/forum/${item.id}`"
        class="glass-card post-card"
      >
        <div class="post-head">
          <router-link :to="`/users/${item.userId}`" class="author" @click.stop>
            <span class="avatar" :style="avatarStyle(item.authorAvatar)">{{ initials(item.authorName) }}</span>
            <span>{{ item.authorName }}</span>
          </router-link>
          <el-tag v-if="item.status !== 'APPROVED'" :type="statusTagType(item.status)" size="small">
            {{ statusLabel(item.status) }}
          </el-tag>
        </div>
        <h3>{{ item.title }}</h3>
        <p class="preview">{{ item.contentPreview }}</p>
        <p v-if="item.status === 'REJECTED' && item.rejectReason" class="reject-hint">
          驳回原因：{{ item.rejectReason }}
        </p>
        <span class="meta">
          {{ formatDate(item.createdAt) }} · {{ item.likeCount || 0 }} 赞 · {{ item.commentCount || 0 }} 评论
        </span>
      </router-link>
      <el-empty v-if="!loading && posts.length === 0" description="暂无帖子" />
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
import { ref, computed, onMounted } from 'vue'
import { getToken } from '../services/auth'
import { fetchPosts, fetchFollowingPosts, fetchMyPendingPosts } from '../services/posts'

export default {
  name: 'ForumView',
  setup() {
    const loggedIn = computed(() => !!getToken())
    const tab = ref('all')
    const posts = ref([])
    const loading = ref(true)
    const page = ref(0)
    const size = ref(10)
    const total = ref(0)

    const formatDate = (v) => (v ? new Date(v).toLocaleString('zh-CN') : '')
    const initials = (name) => (name ? name.charAt(0).toUpperCase() : '?')
    const avatarStyle = (url) => (url ? { backgroundImage: `url(${url})`, backgroundSize: 'cover' } : {})

    const statusLabel = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }[s] || s)
    const statusTagType = (s) => ({ PENDING: 'warning', REJECTED: 'danger', APPROVED: 'success' }[s] || 'info')

    const load = async () => {
      loading.value = true
      try {
        const params = { page: page.value, size: size.value }
        let data
        if (tab.value === 'following') {
          data = await fetchFollowingPosts(params)
        } else if (tab.value === 'pending') {
          data = await fetchMyPendingPosts(params)
        } else {
          data = await fetchPosts(params)
        }
        posts.value = data.content || []
        total.value = data.totalElements || 0
      } catch {
        posts.value = []
        total.value = 0
      } finally {
        loading.value = false
      }
    }

    const onTabChange = () => {
      page.value = 0
      load()
    }

    const onPage = (p) => {
      page.value = p - 1
      load()
    }

    onMounted(load)

    return {
      loggedIn,
      tab,
      posts,
      loading,
      page,
      size,
      total,
      formatDate,
      initials,
      avatarStyle,
      statusLabel,
      statusTagType,
      onTabChange,
      onPage
    }
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 1.5rem;
}

.header-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.btn-create {
  padding: 0.55rem 1.25rem;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--accent-indigo), var(--accent-violet));
  color: #fff;
  text-decoration: none;
  font-weight: 600;
  white-space: nowrap;
}

.btn-muted {
  opacity: 0.85;
}

.tabs-row {
  margin-bottom: 1.5rem;
}

.tabs-row :deep(.el-radio-button__inner) {
  background: var(--glass-bg);
  border-color: var(--glass-border) !important;
  color: var(--text-muted);
}

.tabs-row :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, var(--accent-indigo), var(--accent-violet));
  border-color: transparent !important;
  color: #fff;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 120px;
}

.post-card {
  display: block;
  padding: 1.25rem 1.5rem;
  text-decoration: none;
  color: inherit;
  transition: border-color 0.2s, transform 0.2s;
}

.post-card:hover {
  border-color: var(--accent-violet);
  transform: translateY(-2px);
}

.post-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.author {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--accent-cyan);
  text-decoration: none;
  font-size: 0.9rem;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-indigo), var(--accent-violet));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  color: #fff;
}

.post-card h3 {
  margin: 0.25rem 0 0.5rem;
  font-size: 1.15rem;
}

.preview {
  color: var(--text-muted);
  font-size: 0.9rem;
  line-height: 1.5;
  margin: 0;
}

.reject-hint {
  margin: 0.5rem 0 0;
  font-size: 0.85rem;
  color: #f56c6c;
}

.meta {
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
