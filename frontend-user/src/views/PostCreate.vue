<template>
  <div class="page-container">
    <router-link to="/forum" class="back">← 返回论坛</router-link>
    <div class="glass-card form-card">
      <h1 class="section-title">发布帖子</h1>
      <p class="hint">提交后将进入待审核，审核通过后会在论坛展示</p>
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="正文" required>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="分享你的天文见闻、观测记录或问题…"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">提交审核</el-button>
          <router-link to="/forum">
            <el-button>取消</el-button>
          </router-link>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPost } from '../services/posts'

export default {
  name: 'PostCreateView',
  setup() {
    const router = useRouter()
    const saving = ref(false)
    const form = reactive({ title: '', content: '' })

    const submit = async () => {
      if (!form.title.trim() || !form.content.trim()) {
        ElMessage.warning('请填写标题和正文')
        return
      }
      saving.value = true
      try {
        const post = await createPost({
          title: form.title.trim(),
          content: form.content.trim()
        })
        ElMessage.success('已提交，等待审核')
        router.push(post?.id ? `/forum/${post.id}` : '/forum')
      } catch (e) {
        ElMessage.error(e.message || '发布失败')
      } finally {
        saving.value = false
      }
    }

    return { form, saving, submit }
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

.form-card {
  padding: 2rem;
  max-width: 720px;
}

.hint {
  color: var(--text-muted);
  margin: -0.5rem 0 1.5rem;
  font-size: 0.9rem;
}
</style>
