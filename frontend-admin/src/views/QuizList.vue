<template>
  <div class="page">
    <div class="page-content">
      <header class="header">
        <div class="left">
          <router-link to="/" class="nav-link">← 控制台</router-link>
          <h1>测验套卷</h1>
        </div>
        <el-button type="primary" @click="openCreate">新建套卷</el-button>
      </header>

      <el-card class="panel" shadow="never">
        <el-table :data="quizzes" v-loading="loading">
          <el-table-column prop="name" label="名称" min-width="160" />
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="questionCount" label="题数" width="80" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="visible" :title="editingId ? '编辑套卷' : '新建套卷'" width="640px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="题目">
          <el-select v-model="form.questionIds" multiple filterable style="width: 100%" placeholder="选择题目">
            <el-option
              v-for="q in allQuestions"
              :key="q.id"
              :label="`${q.id}. ${q.stem.slice(0, 40)}...`"
              :value="q.id"
            />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchQuizzes, fetchQuiz, createQuiz, updateQuiz, deleteQuiz, fetchQuestions
} from '../services/quiz'

export default {
  name: 'QuizListView',
  setup() {
    const loading = ref(false)
    const saving = ref(false)
    const quizzes = ref([])
    const allQuestions = ref([])
    const visible = ref(false)
    const editingId = ref(null)
    const form = reactive({
      name: '',
      description: '',
      enabled: true,
      questionIds: []
    })

    const load = async () => {
      loading.value = true
      try {
        quizzes.value = await fetchQuizzes()
      } finally {
        loading.value = false
      }
    }

    const loadQuestions = async () => {
      const data = await fetchQuestions({ page: 0, size: 200 })
      allQuestions.value = data.content || []
    }

    const openCreate = () => {
      editingId.value = null
      form.name = ''
      form.description = ''
      form.enabled = true
      form.questionIds = []
      visible.value = true
    }

    const openEdit = async (row) => {
      editingId.value = row.id
      const q = await fetchQuiz(row.id)
      form.name = q.name
      form.description = q.description || ''
      form.enabled = q.enabled !== false
      form.questionIds = [...(q.questionIds || [])]
      visible.value = true
    }

    const save = async () => {
      saving.value = true
      try {
        const payload = {
          name: form.name,
          description: form.description,
          enabled: form.enabled,
          questionIds: form.questionIds
        }
        if (editingId.value) {
          await updateQuiz(editingId.value, payload)
        } else {
          await createQuiz(payload)
        }
        ElMessage.success('已保存')
        visible.value = false
        load()
      } catch (e) {
        ElMessage.error(e.message)
      } finally {
        saving.value = false
      }
    }

    const remove = async (row) => {
      await ElMessageBox.confirm('确定删除该套卷？', '提示', { type: 'warning' })
      await deleteQuiz(row.id)
      ElMessage.success('已删除')
      load()
    }

    onMounted(() => {
      load()
      loadQuestions()
    })

    return {
      loading, saving, quizzes, allQuestions, visible, editingId, form,
      openCreate, openEdit, save, remove
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 24px 32px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.left { display: flex; align-items: center; gap: 16px; }
.left h1 { margin: 0; color: #fff; }
.nav-link { color: #b0b0ff; text-decoration: none; }
.panel { background: rgba(255,255,255,0.05); border: 1px solid rgba(128,90,213,0.3); }
</style>
