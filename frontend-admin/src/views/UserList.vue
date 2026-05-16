<template>
  <div class="users-page">
    <div class="stars-bg" ref="starsBg"></div>

    <div class="page-content">
      <header class="header">
        <div class="left">
          <router-link to="/" class="nav-link">← 控制台</router-link>
          <h1>用户管理</h1>
        </div>
        <el-button type="primary" @click="openCreate">新建用户</el-button>
      </header>

      <el-card class="panel" shadow="never">
        <div class="filters">
          <el-input v-model="filters.keyword" placeholder="搜索用户名/昵称/邮箱" clearable style="width: 220px" />
          <el-select v-model="filters.role" placeholder="角色" clearable style="width: 120px">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
          <el-button type="primary" @click="loadUsers">查询</el-button>
        </div>

        <el-table :data="users" v-loading="loading" class="user-table">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="用户名" min-width="110" />
          <el-table-column prop="nickname" label="昵称" min-width="110" />
          <el-table-column prop="email" label="邮箱" min-width="140" />
          <el-table-column prop="role" label="角色" width="90">
            <template #default="{ row }">
              <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'info'" size="small">{{ row.role }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
                {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" min-width="160">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="removeUser(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="page + 1"
            @current-change="onPageChange"
          />
        </div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名" v-if="!editingId">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item :label="editingId ? '新密码' : '密码'">
          <el-input v-model="form.password" type="password" show-password :placeholder="editingId ? '留空则不修改' : ''" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="form.avatar" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { reactive, ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchUsers, createUser, updateUser, deleteUser } from '../services/users'

export default {
  name: 'UserListView',
  setup() {
    const starsBg = ref(null)
    const loading = ref(false)
    const submitting = ref(false)
    const users = ref([])
    const total = ref(0)
    const page = ref(0)
    const pageSize = ref(10)
    const dialogVisible = ref(false)
    const editingId = ref(null)

    const filters = reactive({
      keyword: '',
      role: '',
      status: ''
    })

    const form = reactive({
      username: '',
      password: '',
      role: 'USER',
      nickname: '',
      email: '',
      avatar: '',
      status: 'ACTIVE'
    })

    const dialogTitle = computed(() => (editingId.value ? '编辑用户' : '新建用户'))

    const createStars = () => {
      if (!starsBg.value) return
      const container = starsBg.value
      for (let i = 0; i < 60; i++) {
        const star = document.createElement('div')
        star.className = 'star'
        const size = Math.random() * 2 + 1
        star.style.width = `${size}px`
        star.style.height = `${size}px`
        star.style.left = `${Math.random() * 100}%`
        star.style.top = `${Math.random() * 100}%`
        container.appendChild(star)
      }
    }

    const formatDate = (value) => {
      if (!value) return '-'
      return new Date(value).toLocaleString('zh-CN')
    }

    const resetForm = () => {
      form.username = ''
      form.password = ''
      form.role = 'USER'
      form.nickname = ''
      form.email = ''
      form.avatar = ''
      form.status = 'ACTIVE'
    }

    const loadUsers = async () => {
      loading.value = true
      try {
        const data = await fetchUsers({
          page: page.value,
          size: pageSize.value,
          keyword: filters.keyword || undefined,
          role: filters.role || undefined,
          status: filters.status || undefined
        })
        users.value = data.content || []
        total.value = data.totalElements || 0
      } catch (err) {
        ElMessage.error(err.response?.data?.message || err.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const onPageChange = (p) => {
      page.value = p - 1
      loadUsers()
    }

    const openCreate = () => {
      editingId.value = null
      resetForm()
      dialogVisible.value = true
    }

    const openEdit = (row) => {
      editingId.value = row.id
      form.password = ''
      form.role = row.role
      form.nickname = row.nickname || ''
      form.email = row.email || ''
      form.avatar = row.avatar || ''
      form.status = row.status
      dialogVisible.value = true
    }

    const submitForm = async () => {
      if (!editingId.value && (!form.username || !form.password)) {
        ElMessage.warning('请填写用户名和密码')
        return
      }
      submitting.value = true
      try {
        if (editingId.value) {
          const payload = {
            role: form.role,
            nickname: form.nickname,
            email: form.email,
            avatar: form.avatar,
            status: form.status
          }
          if (form.password) payload.password = form.password
          await updateUser(editingId.value, payload)
          ElMessage.success('用户已更新')
        } else {
          await createUser({
            username: form.username,
            password: form.password,
            role: form.role,
            nickname: form.nickname,
            email: form.email,
            avatar: form.avatar,
            status: form.status
          })
          ElMessage.success('用户已创建')
        }
        dialogVisible.value = false
        loadUsers()
      } catch (err) {
        ElMessage.error(err.response?.data?.message || err.message || '保存失败')
      } finally {
        submitting.value = false
      }
    }

    const removeUser = async (row) => {
      try {
        await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '确认删除', { type: 'warning' })
        await deleteUser(row.id)
        ElMessage.success('已删除')
        loadUsers()
      } catch (err) {
        if (err !== 'cancel' && err !== 'close') {
          ElMessage.error(err.response?.data?.message || err.message || '删除失败')
        }
      }
    }

    onMounted(() => {
      loadUsers()
      nextTick(createStars)
    })

    return {
      starsBg,
      loading,
      submitting,
      users,
      total,
      page,
      pageSize,
      filters,
      form,
      dialogVisible,
      editingId,
      dialogTitle,
      formatDate,
      loadUsers,
      onPageChange,
      openCreate,
      openEdit,
      submitForm,
      removeUser
    }
  }
}
</script>

<style scoped>
.users-page {
  min-height: 100vh;
  position: relative;
  padding: 24px;
}

.stars-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.star {
  position: absolute;
  background: #fff;
  border-radius: 50%;
  animation: twinkle 3s infinite ease-in-out;
}

.page-content {
  position: relative;
  z-index: 1;
  max-width: 1100px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 8px 0 0;
  font-size: 1.5rem;
}

.nav-link {
  color: #b0b0ff;
  text-decoration: none;
}

.panel {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(128, 90, 213, 0.35);
  color: #fff;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
