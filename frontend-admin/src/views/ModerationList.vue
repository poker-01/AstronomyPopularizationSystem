<template>
  <div class="moderation-page">
    <div class="stars-bg" ref="starsBg"></div>
    <div class="page-content">
      <header class="header">
        <div class="left">
          <router-link to="/" class="nav-link">← 控制台</router-link>
          <h1>内容审核</h1>
        </div>
      </header>

      <el-card class="panel" shadow="never">
        <el-tabs v-model="activeTab" @tab-change="onTabChange">
          <el-tab-pane label="帖子" name="posts" />
          <el-tab-pane label="评论" name="comments" />
        </el-tabs>

        <div class="filters">
          <el-select v-model="status" placeholder="审核状态" clearable style="width: 140px" @change="reload">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
          <el-button type="primary" @click="load">刷新</el-button>
        </div>

        <el-table :data="items" v-loading="loading">
          <el-table-column prop="type" label="类型" width="80">
            <template #default="{ row }">{{ row.type === 'POST' ? '帖子' : '评论' }}</template>
          </el-table-column>
          <el-table-column prop="authorName" label="作者" width="100" />
          <el-table-column prop="title" label="标题/所属帖" min-width="140" show-overflow-tooltip />
          <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="时间" width="160">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button link type="success" @click="approve(row)">通过</el-button>
                <el-button link type="danger" @click="reject(row)">驳回</el-button>
              </template>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :total="total"
            :page-size="size"
            :current-page="page + 1"
            @current-change="p => { page = p - 1; load() }"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchModerationPosts,
  fetchModerationComments,
  approvePost,
  rejectPost,
  approveComment,
  rejectComment
} from '../services/moderation'

export default {
  name: 'ModerationListView',
  setup() {
    const starsBg = ref(null)
    const activeTab = ref('posts')
    const status = ref('PENDING')
    const items = ref([])
    const loading = ref(false)
    const page = ref(0)
    const size = ref(10)
    const total = ref(0)

    const statusLabel = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }[s] || s)
    const statusTag = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info')
    const formatDate = (v) => (v ? new Date(v).toLocaleString('zh-CN') : '')

    const load = async () => {
      loading.value = true
      try {
        const params = { page: page.value, size: size.value }
        if (status.value) params.status = status.value
        const data =
          activeTab.value === 'posts'
            ? await fetchModerationPosts(params)
            : await fetchModerationComments(params)
        items.value = data.content || []
        total.value = data.totalElements || 0
      } catch (e) {
        ElMessage.error(e.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const reload = () => {
      page.value = 0
      load()
    }

    const onTabChange = () => {
      page.value = 0
      load()
    }

    const approve = async (row) => {
      try {
        if (row.type === 'POST') await approvePost(row.id)
        else await approveComment(row.id)
        ElMessage.success('已通过')
        load()
      } catch (e) {
        ElMessage.error(e.message || '操作失败')
      }
    }

    const reject = async (row) => {
      try {
        const { value } = await ElMessageBox.prompt('请输入驳回原因（可选）', '驳回', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '不符合社区规范等'
        })
        if (row.type === 'POST') await rejectPost(row.id, value)
        else await rejectComment(row.id, value)
        ElMessage.success('已驳回')
        load()
      } catch (e) {
        if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
      }
    }

    onMounted(() => {
      load()
      nextTick(() => {
        if (!starsBg.value) return
        for (let i = 0; i < 60; i++) {
          const star = document.createElement('div')
          star.className = 'star'
          star.style.width = `${Math.random() * 2 + 1}px`
          star.style.height = star.style.width
          star.style.left = `${Math.random() * 100}%`
          star.style.top = `${Math.random() * 100}%`
          star.style.animationDelay = `${Math.random() * 4}s`
          starsBg.value.appendChild(star)
        }
      })
    })

    return {
      starsBg,
      activeTab,
      status,
      items,
      loading,
      page,
      size,
      total,
      statusLabel,
      statusTag,
      formatDate,
      load,
      reload,
      onTabChange,
      approve,
      reject
    }
  }
}
</script>

<style scoped>
.moderation-page {
  min-height: 100vh;
  position: relative;
}

.stars-bg {
  position: fixed;
  inset: 0;
  z-index: -1;
  pointer-events: none;
}

.star {
  position: absolute;
  background: #fff;
  border-radius: 50%;
  animation: twinkle 3s infinite ease-in-out;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.8; }
}

.page-content {
  padding: 24px 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 24px;
}

.header h1 {
  margin: 8px 0 0;
  color: #fff;
}

.nav-link {
  color: #b0b0ff;
  text-decoration: none;
}

.panel {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(128, 90, 213, 0.3);
}

.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.muted {
  color: #888;
}
</style>
