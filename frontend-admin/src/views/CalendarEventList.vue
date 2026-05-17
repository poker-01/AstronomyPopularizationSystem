<template>
  <div class="page">
    <div class="page-content">
      <header class="header">
        <div class="left">
          <router-link to="/" class="nav-link">← 控制台</router-link>
          <h1>天文日历事件</h1>
        </div>
        <div class="header-actions">
          <el-button @click="runImport" :loading="importing">批量导入</el-button>
          <el-button type="primary" @click="openCreate">新建事件</el-button>
        </div>
      </header>

      <el-card class="panel" shadow="never">
        <el-table :data="events" v-loading="loading" row-key="id">
          <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
          <el-table-column prop="eventTypeLabel" label="类型" width="100" />
          <el-table-column label="开始时间" width="170">
            <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="结束时间" width="170">
            <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
          </el-table-column>
          <el-table-column prop="reminderOffsetMinutes" label="提前(分)" width="90" />
          <el-table-column prop="source" label="来源" width="100" show-overflow-tooltip />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="visible" :title="editingId ? '编辑事件' : '新建事件'" width="560px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.eventType" style="width: 100%">
            <el-option label="流星雨" value="METEOR_SHOWER" />
            <el-option label="月食" value="LUNAR_ECLIPSE" />
            <el-option label="日食" value="SOLAR_ECLIPSE" />
            <el-option label="行星合" value="PLANETARY_CONJUNCTION" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="提前提醒">
          <el-input-number v-model="form.reminderOffsetMinutes" :min="0" :step="30" />
          <span class="hint">分钟</span>
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
  fetchCalendarEvents,
  createCalendarEvent,
  updateCalendarEvent,
  deleteCalendarEvent,
  importCalendarEvents
} from '../services/calendar'

export default {
  name: 'CalendarEventListView',
  setup() {
    const loading = ref(false)
    const saving = ref(false)
    const importing = ref(false)
    const events = ref([])
    const visible = ref(false)
    const editingId = ref(null)
    const form = reactive({
      title: '',
      eventType: 'OTHER',
      startTime: '',
      endTime: '',
      description: '',
      reminderOffsetMinutes: 60
    })

    const formatTime = (iso) => {
      if (!iso) return ''
      const d = new Date(iso)
      return d.toLocaleString('zh-CN', { hour12: false })
    }

    const load = async () => {
      loading.value = true
      try {
        events.value = await fetchCalendarEvents()
      } finally {
        loading.value = false
      }
    }

    const openCreate = () => {
      editingId.value = null
      form.title = ''
      form.eventType = 'OTHER'
      form.startTime = ''
      form.endTime = ''
      form.description = ''
      form.reminderOffsetMinutes = 60
      visible.value = true
    }

    const openEdit = (row) => {
      editingId.value = row.id
      form.title = row.title
      form.eventType = row.eventType
      form.startTime = normalizeIso(row.startTime)
      form.endTime = normalizeIso(row.endTime)
      form.description = row.description || ''
      form.reminderOffsetMinutes = row.reminderOffsetMinutes ?? 60
      visible.value = true
    }

    const normalizeIso = (iso) => {
      if (!iso) return ''
      return iso.length >= 19 ? iso.slice(0, 19) : iso
    }

    const save = async () => {
      if (!form.title || !form.startTime || !form.endTime) {
        ElMessage.warning('请填写标题与时间')
        return
      }
      saving.value = true
      try {
        const payload = { ...form }
        if (editingId.value) {
          await updateCalendarEvent(editingId.value, payload)
        } else {
          await createCalendarEvent(payload)
        }
        ElMessage.success('已保存')
        visible.value = false
        await load()
      } catch (e) {
        ElMessage.error(e.message || '保存失败')
      } finally {
        saving.value = false
      }
    }

    const remove = async (row) => {
      await ElMessageBox.confirm(`确定删除「${row.title}」？`, '确认')
      await deleteCalendarEvent(row.id)
      ElMessage.success('已删除')
      await load()
    }

    const runImport = async () => {
      const year = new Date().getFullYear()
      importing.value = true
      try {
        const result = await importCalendarEvents(year)
        ElMessage.success(result.message || `导入 ${result.imported} 条`)
        await load()
      } catch (e) {
        ElMessage.error(e.message || '导入失败')
      } finally {
        importing.value = false
      }
    }

    onMounted(load)

    return {
      loading,
      saving,
      importing,
      events,
      visible,
      editingId,
      form,
      formatTime,
      openCreate,
      openEdit,
      save,
      remove,
      runImport
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #0a0a1a;
  color: #e2e8f0;
}

.page-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.nav-link {
  color: #94a3b8;
  text-decoration: none;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.panel {
  background: rgba(15, 15, 35, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.hint {
  margin-left: 0.5rem;
  color: #94a3b8;
  font-size: 0.85rem;
}
</style>
