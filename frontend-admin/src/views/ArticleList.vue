<template>
  <div class="articles-page">
    <div class="stars-bg" ref="starsBg"></div>
    <div class="page-content">
      <header class="header">
        <div class="left">
          <router-link to="/" class="nav-link">← 控制台</router-link>
          <h1>内容管理</h1>
        </div>
        <el-button type="primary" @click="openCreate">发布文章</el-button>
      </header>

      <el-card class="panel" shadow="never">
        <div class="filters">
          <el-input v-model="keyword" placeholder="搜索标题" clearable style="width: 200px" @keyup.enter="load" />
          <el-select v-model="status" placeholder="状态" clearable style="width: 120px">
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
          </el-select>
          <el-select v-model="categoryId" placeholder="分类" clearable style="width: 140px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-button type="primary" @click="load">查询</el-button>
        </div>

        <el-table :data="articles" v-loading="loading">
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
                {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="阅读" width="70" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="visible" :title="editingId ? '编辑文章' : '新建文章'" width="720px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="Slug">
          <el-input v-model="form.slug" placeholder="留空自动生成" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="封面 URL">
          <el-input v-model="form.coverUrl" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
          </el-select>
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="支持 Markdown：## 标题、**粗体**、- 列表" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchCategories, fetchArticles, fetchArticle, createArticle, updateArticle, deleteArticle } from '../services/content'

export default {
  name: 'ArticleListView',
  setup() {
    const starsBg = ref(null)
    const loading = ref(false)
    const saving = ref(false)
    const articles = ref([])
    const categories = ref([])
    const keyword = ref('')
    const status = ref(null)
    const categoryId = ref(null)
    const page = ref(0)
    const size = ref(10)
    const total = ref(0)
    const visible = ref(false)
    const editingId = ref(null)
    const form = reactive({
      categoryId: null,
      title: '',
      slug: '',
      summary: '',
      coverUrl: '',
      content: '',
      status: 'DRAFT'
    })

    const load = async () => {
      loading.value = true
      try {
        const data = await fetchArticles({
          page: page.value,
          size: size.value,
          keyword: keyword.value || undefined,
          status: status.value || undefined,
          categoryId: categoryId.value || undefined
        })
        articles.value = data.content || []
        total.value = data.totalElements || 0
      } finally {
        loading.value = false
      }
    }

    const resetForm = () => {
      form.categoryId = categories.value[0]?.id || null
      form.title = ''
      form.slug = ''
      form.summary = ''
      form.coverUrl = ''
      form.content = ''
      form.status = 'DRAFT'
    }

    const openCreate = () => {
      editingId.value = null
      resetForm()
      visible.value = true
    }

    const openEdit = async (row) => {
      editingId.value = row.id
      const detail = await fetchArticle(row.id)
      form.categoryId = detail.categoryId
      form.title = detail.title
      form.slug = detail.slug
      form.summary = detail.summary
      form.coverUrl = detail.coverUrl
      form.content = detail.content
      form.status = detail.status
      visible.value = true
    }

    const save = async () => {
      saving.value = true
      try {
        const payload = { ...form }
        if (editingId.value) await updateArticle(editingId.value, payload)
        else await createArticle(payload)
        ElMessage.success('已保存')
        visible.value = false
        load()
      } catch (e) {
        ElMessage.error(e.message || '保存失败')
      } finally {
        saving.value = false
      }
    }

    const remove = async (row) => {
      try {
        await ElMessageBox.confirm(`删除「${row.title}」？`, '确认', { type: 'warning' })
        await deleteArticle(row.id)
        ElMessage.success('已删除')
        load()
      } catch (e) {
        if (e !== 'cancel' && e !== 'close') ElMessage.error(e.message || '删除失败')
      }
    }

    onMounted(async () => {
      categories.value = await fetchCategories()
      load()
      nextTick(() => {
        if (!starsBg.value) return
        for (let i = 0; i < 50; i++) {
          const s = document.createElement('div')
          s.className = 'star'
          s.style.cssText = `position:absolute;width:2px;height:2px;background:#fff;border-radius:50%;left:${Math.random()*100}%;top:${Math.random()*100}%`
          starsBg.value.appendChild(s)
        }
      })
    })

    return {
      starsBg, loading, saving, articles, categories, keyword, status, categoryId,
      page, size, total, visible, editingId, form, load, openCreate, openEdit, save, remove
    }
  }
}
</script>

<style scoped>
.articles-page { min-height: 100vh; position: relative; padding: 24px; }
.stars-bg { position: fixed; inset: 0; z-index: 0; pointer-events: none; }
.page-content { position: relative; z-index: 1; max-width: 1100px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.nav-link { color: #b0b0ff; text-decoration: none; }
.panel { background: rgba(255,255,255,0.06); border: 1px solid rgba(128,90,213,0.35); color: #fff; }
.filters { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
