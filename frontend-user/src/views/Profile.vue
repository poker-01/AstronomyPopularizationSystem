<template>
  <div class="profile-page">
    <StarfieldBg />

    <div class="profile-shell">
      <header class="top-bar">
        <router-link to="/" class="back-link">← 返回首页</router-link>
        <h1>个人中心</h1>
      </header>

      <el-card class="profile-card" shadow="never" v-loading="loading">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本资料" name="profile">
            <el-form :model="form" label-width="90px" class="profile-form">
              <el-form-item label="用户名">
                <el-input v-model="form.username" disabled />
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" placeholder="显示名称" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="选填" />
              </el-form-item>
              <el-form-item label="头像 URL">
                <el-input v-model="form.avatar" placeholder="图片链接" />
              </el-form-item>
              <el-form-item label="角色">
                <el-tag>{{ form.role }}</el-tag>
              </el-form-item>
              <el-form-item label="状态">
                <el-tag :type="form.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ form.status === 'ACTIVE' ? '正常' : '已禁用' }}
                </el-tag>
              </el-form-item>
              <el-form-item label="注册时间">
                <span class="meta-text">{{ formatDate(form.createdAt) }}</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password">
            <el-form :model="pwdForm" label-width="90px" class="profile-form">
              <el-form-item label="原密码">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="pwdForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="pwdSaving" @click="savePassword">更新密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script>
import { reactive, ref, onMounted } from 'vue'
import StarfieldBg from '../components/StarfieldBg.vue'
import { fetchProfile, updateProfile, changePassword } from '../services/user'
import { clearToken, saveUserInfo } from '../services/auth'
import { ElMessage } from 'element-plus'

export default {
  name: 'ProfileView',
  components: { StarfieldBg },
  setup() {
    const loading = ref(true)
    const saving = ref(false)
    const pwdSaving = ref(false)
    const activeTab = ref('profile')

    const form = reactive({
      username: '',
      nickname: '',
      email: '',
      avatar: '',
      role: '',
      status: '',
      createdAt: null
    })

    const pwdForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const formatDate = (value) => {
      if (!value) return '-'
      return new Date(value).toLocaleString('zh-CN')
    }

    const loadProfile = async () => {
      loading.value = true
      try {
        const data = await fetchProfile()
        Object.assign(form, data)
        saveUserInfo({
          username: data.username,
          role: data.role,
          nickname: data.nickname || data.username
        })
      } catch (err) {
        ElMessage.error(err.response?.data?.message || err.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const saveProfile = async () => {
      saving.value = true
      try {
        const data = await updateProfile({
          nickname: form.nickname,
          email: form.email,
          avatar: form.avatar
        })
        Object.assign(form, data)
        saveUserInfo({
          username: data.username,
          role: data.role,
          nickname: data.nickname || data.username
        })
        ElMessage.success('资料已保存')
      } catch (err) {
        ElMessage.error(err.response?.data?.message || err.message || '保存失败')
      } finally {
        saving.value = false
      }
    }

    const savePassword = async () => {
      if (!pwdForm.oldPassword || !pwdForm.newPassword) {
        ElMessage.warning('请填写完整')
        return
      }
      if (pwdForm.newPassword.length < 4) {
        ElMessage.warning('新密码至少 4 位')
        return
      }
      if (pwdForm.newPassword !== pwdForm.confirmPassword) {
        ElMessage.warning('两次输入的新密码不一致')
        return
      }
      pwdSaving.value = true
      try {
        await changePassword(pwdForm.oldPassword, pwdForm.newPassword)
        ElMessage.success('密码已更新，请重新登录')
        clearToken()
        window.location.href = '/login'
      } catch (err) {
        ElMessage.error(err.response?.data?.message || err.message || '修改失败')
      } finally {
        pwdSaving.value = false
      }
    }

    onMounted(loadProfile)

    return {
      loading,
      saving,
      pwdSaving,
      activeTab,
      form,
      pwdForm,
      formatDate,
      saveProfile,
      savePassword
    }
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  position: relative;
  padding: 24px;
}

.profile-shell {
  position: relative;
  z-index: 1;
  max-width: 720px;
  margin: 0 auto;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.top-bar h1 {
  margin: 0;
  font-size: 1.6rem;
}

.back-link {
  color: #b0b0ff;
  text-decoration: none;
}

.back-link:hover {
  color: #7b68ee;
}

.profile-card {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(128, 90, 213, 0.35);
  border-radius: 16px;
  color: #fff;
}

.profile-card :deep(.el-card__body) {
  padding: 24px;
}

.profile-card :deep(.el-tabs__item) {
  color: #c8c8ff;
}

.profile-card :deep(.el-tabs__item.is-active) {
  color: #fff;
}

.profile-form {
  margin-top: 12px;
}

.meta-text {
  color: #c8c8ff;
}
</style>
