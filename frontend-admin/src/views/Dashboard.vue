<template>
  <el-container style="padding:20px">
    <el-header style="display:flex;justify-content:space-between;align-items:center">
      <div style="font-size:20px">管理员后台</div>
      <div>
        <el-button v-if="!username" type="primary" @click="goLogin">登录</el-button>
        <el-button v-if="username" plain>{{ username }}</el-button>
        <el-button v-if="username" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <el-card>
        <h2>管理员仪表盘</h2>
        <p>这里是管理员功能入口占位。</p>
      </el-card>
    </el-main>
  </el-container>
</template>

<script>
import api from '../services/api'
import { clearToken, getToken } from '../services/auth'
import { ref, onMounted } from 'vue'

export default {
  name: 'AdminDashboardView',
  setup() {
    const username = ref('')

    const load = async () => {
      try {
        const token = getToken()
        if (!token) return
        const res = await api.get('/api/home')
        if (res.data.role === 'ADMIN') username.value = res.data.username
      } catch (err) {
        console.warn(err)
      }
    }

    const goLogin = () => { window.location.href = '/login' }
    const logout = () => { clearToken(); username.value = '' }

    onMounted(load)
    return { username, goLogin, logout }
  }
}
</script>

<style scoped>
</style>
