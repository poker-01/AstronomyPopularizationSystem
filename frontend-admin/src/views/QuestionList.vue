<template>
  <div class="page">
    <div class="stars-bg" ref="starsBg"></div>
    <div class="page-content">
      <header class="header">
        <div class="left">
          <router-link to="/" class="nav-link">← 控制台</router-link>
          <h1>题库管理</h1>
        </div>
        <div class="header-actions">
          <el-button @click="openImport">JSON 批量导入</el-button>
          <el-button type="primary" @click="openCreate">新建题目</el-button>
        </div>
      </header>

      <el-card class="panel" shadow="never">
        <div class="filters">
          <el-input v-model="keyword" placeholder="搜索题干" clearable style="width: 200px" @keyup.enter="load" />
          <el-select v-model="difficulty" placeholder="难度" clearable style="width: 120px">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
          <el-button type="primary" @click="load">查询</el-button>
        </div>

        <el-table :data="questions" v-loading="loading">
          <el-table-column prop="stem" label="题干" min-width="220" show-overflow-tooltip />
          <el-table-column prop="type" label="题型" width="80">
            <template #default="{ row }">{{ row.type === 'MULTIPLE' ? '多选' : '单选' }}</template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="80" />
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

    <el-dialog v-model="visible" :title="editingId ? '编辑题目' : '新建题目'" width="640px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="题干">
          <el-input v-model="form.stem" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="form.type">
            <el-option label="单选" value="SINGLE" />
            <el-option label="多选" value="MULTIPLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="选项">
          <div v-for="(opt, i) in form.options" :key="i" class="opt-row">
            <el-input v-model="opt.key" placeholder="键" style="width: 60px" />
            <el-input v-model="opt.text" placeholder="选项文字" />
            <el-button link type="danger" @click="form.options.splice(i, 1)">删</el-button>
          </div>
          <el-button size="small" @click="addOption">添加选项</el-button>
        </el-form-item>
        <el-form-item label="正确答案">
          <el-select v-model="form.correctAnswer" :multiple="form.type === 'MULTIPLE'" style="width: 100%">
            <el-option v-for="opt in form.options" :key="opt.key" :label="opt.key" :value="opt.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="form.explanation" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="JSON 批量导入" width="560px">
      <p class="hint">粘贴 JSON 数组，每项含 stem、type、options、correctAnswer、difficulty、explanation</p>
      <el-input v-model="importJson" type="textarea" :rows="12" placeholder='[{"stem":"...","type":"SINGLE",...}]' />
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="doImport">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchQuestions, fetchQuestion, createQuestion, updateQuestion, deleteQuestion, importQuestions
} from '../services/quiz'

export default {
  name: 'QuestionListView',
  setup() {
    const starsBg = ref(null)
    const loading = ref(false)
    const saving = ref(false)
    const importing = ref(false)
    const questions = ref([])
    const keyword = ref('')
    const difficulty = ref(null)
    const page = ref(0)
    const size = ref(10)
    const total = ref(0)
    const visible = ref(false)
    const importVisible = ref(false)
    const importJson = ref('')
    const editingId = ref(null)
    const form = reactive({
      stem: '',
      type: 'SINGLE',
      difficulty: 'MEDIUM',
      options: [{ key: 'A', text: '' }, { key: 'B', text: '' }],
      correctAnswer: [],
      explanation: ''
    })

    const load = async () => {
      loading.value = true
      try {
        const data = await fetchQuestions({
          page: page.value,
          size: size.value,
          keyword: keyword.value || undefined,
          difficulty: difficulty.value || undefined
        })
        questions.value = data.content || []
        total.value = data.totalElements || 0
      } finally {
        loading.value = false
      }
    }

    const resetForm = () => {
      form.stem = ''
      form.type = 'SINGLE'
      form.difficulty = 'MEDIUM'
      form.options = [{ key: 'A', text: '' }, { key: 'B', text: '' }, { key: 'C', text: '' }, { key: 'D', text: '' }]
      form.correctAnswer = form.type === 'MULTIPLE' ? [] : ''
      form.explanation = ''
    }

    const openCreate = () => {
      editingId.value = null
      resetForm()
      visible.value = true
    }

    const openEdit = async (row) => {
      editingId.value = row.id
      const q = await fetchQuestion(row.id)
      form.stem = q.stem
      form.type = q.type
      form.difficulty = q.difficulty
      form.options = [...(q.options || [])]
      form.correctAnswer = q.type === 'MULTIPLE' ? [...(q.correctAnswer || [])] : (q.correctAnswer?.[0] || '')
      form.explanation = q.explanation || ''
      visible.value = true
    }

    const addOption = () => {
      const keys = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
      const next = keys[form.options.length] || String(form.options.length + 1)
      form.options.push({ key: next, text: '' })
    }

    const buildPayload = () => ({
      stem: form.stem,
      type: form.type,
      difficulty: form.difficulty,
      options: form.options.filter(o => o.key && o.text),
      correctAnswer: form.type === 'MULTIPLE'
        ? (Array.isArray(form.correctAnswer) ? form.correctAnswer : [])
        : [form.correctAnswer].filter(Boolean),
      explanation: form.explanation
    })

    const save = async () => {
      saving.value = true
      try {
        const payload = buildPayload()
        if (editingId.value) {
          await updateQuestion(editingId.value, payload)
        } else {
          await createQuestion(payload)
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
      await ElMessageBox.confirm('确定删除该题目？', '提示', { type: 'warning' })
      await deleteQuestion(row.id)
      ElMessage.success('已删除')
      load()
    }

    const openImport = () => {
      importJson.value = ''
      importVisible.value = true
    }

    const doImport = async () => {
      importing.value = true
      try {
        const list = JSON.parse(importJson.value)
        if (!Array.isArray(list)) throw new Error('须为 JSON 数组')
        const count = await importQuestions(list)
        ElMessage.success(`成功导入 ${count} 题`)
        importVisible.value = false
        load()
      } catch (e) {
        ElMessage.error(e.message || '导入失败')
      } finally {
        importing.value = false
      }
    }

    onMounted(() => {
      load()
      nextTick(() => {
        if (!starsBg.value) return
        for (let i = 0; i < 40; i++) {
          const star = document.createElement('div')
          star.className = 'star'
          star.style.cssText = `width:${Math.random() * 2 + 1}px;height:${Math.random() * 2 + 1}px;left:${Math.random() * 100}%;top:${Math.random() * 100}%`
          starsBg.value.appendChild(star)
        }
      })
    })

    return {
      starsBg, loading, saving, importing, questions, keyword, difficulty,
      page, size, total, visible, importVisible, importJson, editingId, form,
      load, openCreate, openEdit, addOption, save, remove, openImport, doImport
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; position: relative; }
.stars-bg { position: fixed; inset: 0; z-index: -1; pointer-events: none; }
.page-content { position: relative; z-index: 1; padding: 24px 32px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.left { display: flex; align-items: center; gap: 16px; }
.left h1 { margin: 0; color: #fff; font-size: 1.5rem; }
.nav-link { color: #b0b0ff; text-decoration: none; }
.header-actions { display: flex; gap: 8px; }
.panel { background: rgba(255,255,255,0.05); border: 1px solid rgba(128,90,213,0.3); }
.filters { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
.opt-row { display: flex; gap: 8px; margin-bottom: 8px; width: 100%; }
.hint { color: #8888cc; font-size: 0.85rem; margin-bottom: 8px; }
</style>
