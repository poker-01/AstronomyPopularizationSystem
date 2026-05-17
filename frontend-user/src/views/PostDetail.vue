<template>
  <div class="page-container" v-loading="loading">
    <template v-if="post">
      <router-link to="/forum" class="back">← 返回论坛</router-link>

      <article class="glass-card post-article">
        <div class="post-top">
          <router-link :to="`/users/${post.userId}`" class="author">
            <span class="avatar" :style="avatarStyle(post.authorAvatar)">{{ initials(post.authorName) }}</span>
            <span>{{ post.authorName }}</span>
          </router-link>
          <el-tag v-if="post.status !== 'APPROVED'" :type="statusTagType(post.status)" size="small">
            {{ statusLabel(post.status) }}
          </el-tag>
        </div>
        <h1>{{ post.title }}</h1>
        <p class="meta">{{ formatDate(post.createdAt) }} · {{ post.likeCount || 0 }} 赞 · {{ post.commentCount || 0 }} 评论</p>
        <p v-if="post.status === 'REJECTED' && post.rejectReason" class="reject-hint">驳回原因：{{ post.rejectReason }}</p>
        <div class="post-body markdown-body" v-html="htmlContent"></div>
        <div v-if="post.status === 'APPROVED'" class="actions">
          <el-button v-if="loggedIn" :type="post.likedByMe ? 'default' : 'primary'" @click="toggleLike">
            {{ post.likedByMe ? '已赞' : '点赞' }}
          </el-button>
        </div>
      </article>

      <section v-if="post.status === 'APPROVED'" class="comments-section glass-card">
        <h2>评论</h2>
        <div v-if="loggedIn" class="comment-form">
          <el-input
            v-model="commentText"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论…"
          />
          <el-button type="primary" :loading="commenting" class="submit-comment" @click="submitComment">
            发表评论
          </el-button>
          <p class="hint">评论需审核后展示</p>
        </div>
        <p v-else class="login-hint">
          <router-link to="/login">登录</router-link> 后参与讨论
        </p>

        <div v-if="comments.length" class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-head">
              <router-link :to="`/users/${c.userId}`" class="author-sm">{{ c.authorName }}</router-link>
              <span class="time">{{ formatDate(c.createdAt) }}</span>
            </div>
            <p class="comment-content">{{ c.content }}</p>
            <button v-if="loggedIn" class="reply-btn" type="button" @click="setReply(c)">回复</button>
            <div v-if="replyTo?.id === c.id" class="reply-form">
              <el-input v-model="replyText" type="textarea" :rows="2" :placeholder="`回复 ${c.authorName}`" />
              <el-button size="small" type="primary" :loading="commenting" @click="submitReply(c.id)">发送</el-button>
              <el-button size="small" @click="replyTo = null">取消</el-button>
            </div>
            <div v-if="c.replies?.length" class="replies">
              <div v-for="r in c.replies" :key="r.id" class="comment-item reply">
                <div class="comment-head">
                  <router-link :to="`/users/${r.userId}`" class="author-sm">{{ r.authorName }}</router-link>
                  <span class="time">{{ formatDate(r.createdAt) }}</span>
                </div>
                <p class="comment-content">{{ r.content }}</p>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评论，来抢沙发吧" />
      </section>
    </template>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getToken } from '../services/auth'
import { fetchPost, fetchComments, createComment, likePost, unlikePost } from '../services/posts'
import { renderSimpleMarkdown } from '../utils/markdown'

export default {
  name: 'PostDetailView',
  setup() {
    const route = useRoute()
    const loading = ref(true)
    const post = ref(null)
    const comments = ref([])
    const commentText = ref('')
    const replyText = ref('')
    const replyTo = ref(null)
    const commenting = ref(false)
    const loggedIn = computed(() => !!getToken())

    const htmlContent = computed(() =>
      post.value ? renderSimpleMarkdown(post.value.content) : ''
    )

    const formatDate = (v) => (v ? new Date(v).toLocaleString('zh-CN') : '')
    const initials = (name) => (name ? name.charAt(0).toUpperCase() : '?')
    const avatarStyle = (url) => (url ? { backgroundImage: `url(${url})`, backgroundSize: 'cover' } : {})
    const statusLabel = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }[s] || s)
    const statusTagType = (s) => ({ PENDING: 'warning', REJECTED: 'danger', APPROVED: 'success' }[s] || 'info')

    const load = async () => {
      loading.value = true
      try {
        const id = route.params.id
        post.value = await fetchPost(id)
        if (post.value?.status === 'APPROVED') {
          comments.value = await fetchComments(id)
        } else {
          comments.value = []
        }
      } catch (e) {
        post.value = null
        ElMessage.error(e.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const toggleLike = async () => {
      if (!post.value) return
      try {
        if (post.value.likedByMe) {
          await unlikePost(post.value.id)
          post.value.likedByMe = false
          post.value.likeCount = Math.max(0, (post.value.likeCount || 1) - 1)
        } else {
          await likePost(post.value.id)
          post.value.likedByMe = true
          post.value.likeCount = (post.value.likeCount || 0) + 1
        }
      } catch (e) {
        ElMessage.error(e.message || '操作失败')
      }
    }

    const submitComment = async () => {
      if (!commentText.value.trim()) {
        ElMessage.warning('请输入评论内容')
        return
      }
      commenting.value = true
      try {
        await createComment(route.params.id, { content: commentText.value.trim() })
        commentText.value = ''
        ElMessage.success('评论已提交，等待审核')
      } catch (e) {
        ElMessage.error(e.message || '评论失败')
      } finally {
        commenting.value = false
      }
    }

    const setReply = (c) => {
      replyTo.value = c
      replyText.value = ''
    }

    const submitReply = async (parentId) => {
      if (!replyText.value.trim()) {
        ElMessage.warning('请输入回复内容')
        return
      }
      commenting.value = true
      try {
        await createComment(route.params.id, { content: replyText.value.trim(), parentId })
        replyTo.value = null
        replyText.value = ''
        ElMessage.success('回复已提交，等待审核')
      } catch (e) {
        ElMessage.error(e.message || '回复失败')
      } finally {
        commenting.value = false
      }
    }

    onMounted(load)
    watch(() => route.params.id, load)

    return {
      loading,
      post,
      comments,
      commentText,
      replyText,
      replyTo,
      commenting,
      loggedIn,
      htmlContent,
      formatDate,
      initials,
      avatarStyle,
      statusLabel,
      statusTagType,
      toggleLike,
      submitComment,
      setReply,
      submitReply
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

.post-article {
  padding: 2rem;
  margin-bottom: 1.5rem;
}

.post-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.author {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  color: var(--accent-cyan);
  text-decoration: none;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-indigo), var(--accent-violet));
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #fff;
}

.post-article h1 {
  margin: 0.5rem 0;
  font-size: clamp(1.5rem, 3vw, 2rem);
}

.meta {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.reject-hint {
  color: #f56c6c;
  font-size: 0.9rem;
}

.post-body {
  margin: 1.5rem 0;
  line-height: 1.7;
}

.actions {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--glass-border);
}

.comments-section {
  padding: 1.5rem 2rem;
}

.comments-section h2 {
  margin: 0 0 1rem;
  font-size: 1.2rem;
}

.comment-form {
  margin-bottom: 1.5rem;
}

.submit-comment {
  margin-top: 0.75rem;
}

.hint,
.login-hint {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-top: 0.5rem;
}

.login-hint a {
  color: var(--accent-cyan);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.comment-item {
  padding: 1rem 0;
  border-bottom: 1px solid var(--glass-border);
}

.comment-item.reply {
  margin-left: 1.5rem;
  padding: 0.75rem 0 0.75rem 1rem;
  border-left: 2px solid var(--accent-violet);
  border-bottom: none;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.35rem;
}

.author-sm {
  color: var(--accent-cyan);
  text-decoration: none;
  font-weight: 600;
}

.time {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.comment-content {
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.reply-btn {
  margin-top: 0.5rem;
  background: none;
  border: none;
  color: var(--accent-violet);
  cursor: pointer;
  font-size: 0.85rem;
  padding: 0;
}

.reply-form {
  margin-top: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-start;
}
</style>
