<template>
  <div class="page">
    <div class="page-content">
      <header class="header">
        <div class="left">
          <router-link to="/" class="nav-link">← 控制台</router-link>
          <h1>成就徽章</h1>
        </div>
        <el-button type="primary" @click="openCreate">新建徽章</el-button>
      </header>

      <el-card class="panel" shadow="never">
        <el-table :data="badges" v-loading="loading">
          <el-table-column label="图标" width="70">
            <template #default="{ row }">
              <span class="icon-cell">{{ row.iconUrl || '🏅' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" width="120" />
          <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
          <el-table-column prop="ruleType" label="规则类型" width="120" />
          <el-table-column prop="ruleValue" label="阈值" width="80" />
          <el-table-column prop="enabled" label="启用" width="80">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="visible" :title="editingId ? '编辑徽章' : '新建徽章'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.iconUrl" placeholder="Emoji 或图片 URL，如 🌟" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="form.ruleType" style="width: 100%">
            <el-option label="测验得分 (QUIZ_SCORE)" value="QUIZ_SCORE" />
            <el-option label="完成次数 (QUIZ_COUNT)" value="QUIZ_COUNT" />
            <el-option label="连续天数 (STREAK)" value="STREAK" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则阈值">
          <el-input-number v-model="form.ruleValue" :min="0" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
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
import { fetchBadges, createBadge, updateBadge, deleteBadge } from '../services/quiz'

export default {
  name: 'BadgeListView',
  setup() {
    const loading = ref(false)
    const saving = ref(false)
    const badges = ref([])
    const visible = ref(false)
    const editingId = ref(null)
    const form = reactive({
      name: '',
      iconUrl: '🏅',
      description: '',
      ruleType: 'QUIZ_COUNT',
      ruleValue: 1,
      enabled: true
    })

    const load = async () => {
      loading.value = true
      try {
        badges.value = await fetchBadges()
      } finally {
        loading.value = false
      }
    }

    const openCreate = () => {
      editingId.value = null
      form.name = ''
      form.iconUrl = '🏅'
      form.description = ''
      form.ruleType = 'QUIZ_COUNT'
      form.ruleValue = 1
      form.enabled = true
      visible.value = true
    }

    const openEdit = (row) => {
      editingId.value = row.id
      form.name = row.name
      form.iconUrl = row.iconUrl || '🏅'
      form.description = row.description || ''
      form.ruleType = row.ruleType
      form.ruleValue = row.ruleValue
      form.enabled = row.enabled !== false
      visible.value = true
    }

    const save = async () => {
      saving.value = true
      try {
        const payload = { ...form }
        if (editingId.value) {
          await updateBadge(editingId.value, payload)
        } else {
          await createBadge(payload)
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
      await ElMessageBox.confirm('确定删除该徽章？', '提示', { type: 'warning' })
      await deleteBadge(row.id)
      ElMessage.success('已删除')
      load()
    }

    onMounted(load)

    return {
      loading, saving, badges, visible, editingId, form,
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
.icon-cell { font-size: 1.5rem; }
</style>
