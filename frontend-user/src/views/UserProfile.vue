<template>
  <div class="page-container" v-loading="loading">
    <template v-if="profile">
      <router-link to="/forum" class="back">← 返回论坛</router-link>

      <div class="glass-card profile-card">
        <div class="profile-head">
          <span class="avatar-lg" :style="avatarStyle(profile.avatar)">{{ initials(displayName) }}</span>
          <div class="profile-info">
            <h1>{{ displayName }}</h1>
            <p class="username">@{{ profile.username }}</p>
            <p class="joined">加入于 {{ formatDate(profile.createdAt) }}</p>
          </div>
          <el-button
            v-if="loggedIn && !isSelf"
            :type="profile.following ? 'default' : 'primary'"
            :loading="followLoading"
            @click="toggleFollow"
          >
            {{ profile.following ? '已关注' : '关注' }}
          </el-button>
        </div>
        <div class="stats">
          <span><strong>{{ profile.postCount }}</strong> 帖子</span>
          <span><strong>{{ profile.followerCount }}</strong> 粉丝</span>
          <span><strong>{{ profile.followingCount }}</strong> 关注</span>
        </div>
      </div>

      <h2 class="section-subtitle posts-title">TA 的帖子</h2>
      <div v-loading="postsLoading" class="post-list">
        <router-link
          v-for="item in posts"
          :key="item.id"
          :to="`/forum/${item.id}`"
          class="glass-card post-card"
        >
          <h3>{{ item.title }}</h3>
          <p class="preview">{{ item.contentPreview }}</p>
          <span class="meta">
            {{ formatDate(item.createdAt) }} · {{ item.likeCount || 0 }} 赞 · {{ item.commentCount || 0 }} 评论
          </span>
        </router-link>
        <el-empty v-if="!postsLoading && posts.length === 0" description="暂无已发布帖子" />
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
    </template>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getToken } from '../services/auth'
import { fetchPublicProfile, followUser, unfollowUser, fetchProfile } from '../services/user'
import { fetchPosts } from '../services/posts'

export default {
  name: 'UserProfileView',
  setup() {
    const route = useRoute()
    const loading = ref(true)
    const postsLoading = ref(true)
    const profile = ref(null)
    const posts = ref([])
    const page = ref(0)
    const size = ref(10)
    const total = ref(0)
    const followLoading = ref(false)
    const myId = ref(null)

    const loggedIn = computed(() => !!getToken())
    const userId = computed(() => Number(route.params.id))
    const isSelf = computed(() => myId.value != null && myId.value === userId.value)
    const displayName = computed(() =>
      profile.value?.nickname || profile.value?.username || ''
    )

    const formatDate = (v) => (v ? new Date(v).toLocaleDateString('zh-CN') : '')
    const initials = (name) => (name ? name.charAt(0).toUpperCase() : '?')
    const avatarStyle = (url) => (url ? { backgroundImage: `url(${url})`, backgroundSize: 'cover' } : {})

    const loadProfile = async () => {
      loading.value = true
      try {
        profile.value = await fetchPublicProfile(userId.value)
      } catch (e) {
        profile.value = null
        ElMessage.error(e.message || '用户不存在')
      } finally {
        loading.value = false
      }
    }

    const loadPosts = async () => {
      postsLoading.value = true
      try {
        const data = await fetchPosts({
          page: page.value,
          size: size.value,
          userId: userId.value
        })
        posts.value = data.content || []
        total.value = data.totalElements || 0
      } finally {
        postsLoading.value = false
      }
    }

    const toggleFollow = async () => {
      followLoading.value = true
      try {
        if (profile.value.following) {
          await unfollowUser(userId.value)
          profile.value.following = false
          profile.value.followerCount = Math.max(0, profile.value.followerCount - 1)
          ElMessage.success('已取消关注')
        } else {
          await followUser(userId.value)
          profile.value.following = true
          profile.value.followerCount += 1
          ElMessage.success('已关注')
        }
      } catch (e) {
        ElMessage.error(e.message || '操作失败')
      } finally {
        followLoading.value = false
      }
    }

    const onPage = (p) => {
      page.value = p - 1
      loadPosts()
    }

    onMounted(async () => {
      if (loggedIn.value) {
        try {
          const me = await fetchProfile()
          myId.value = me.id
        } catch {
          myId.value = null
        }
      }
      await loadProfile()
      loadPosts()
    })

    watch(() => route.params.id, async () => {
      page.value = 0
      await loadProfile()
      loadPosts()
    })

    return {
      loading,
      postsLoading,
      profile,
      posts,
      page,
      size,
      total,
      followLoading,
      loggedIn,
      isSelf,
      displayName,
      formatDate,
      initials,
      avatarStyle,
      toggleFollow,
      onPage
    }
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

.profile-card {
  padding: 2rem;
  margin-bottom: 2rem;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  flex-wrap: wrap;
}

.avatar-lg {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-indigo), var(--accent-violet));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 800;
  color: #fff;
  flex-shrink: 0;
}

.profile-info {
  flex: 1;
  min-width: 160px;
}

.profile-info h1 {
  margin: 0;
}

.username {
  color: var(--text-muted);
  margin: 0.25rem 0;
}

.joined {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin: 0;
}

.stats {
  display: flex;
  gap: 2rem;
  margin-top: 1.5rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--glass-border);
  color: var(--text-muted);
}

.stats strong {
  color: #fff;
  margin-right: 0.25rem;
}

.posts-title {
  margin-bottom: 1rem;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.post-card {
  display: block;
  padding: 1.25rem 1.5rem;
  text-decoration: none;
  color: inherit;
}

.post-card h3 {
  margin: 0 0 0.5rem;
}

.preview {
  color: var(--text-muted);
  margin: 0;
  font-size: 0.9rem;
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
