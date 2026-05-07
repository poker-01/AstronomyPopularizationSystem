<template>
  <el-container style="padding: 20px">
    <el-header style="display:flex;justify-content:space-between;align-items:center">
      <div style="font-size:20px">天文科普首页</div>
      <div>
        <el-button v-if="!username" type="primary" @click="goLogin">登录</el-button>
        <el-button v-if="username" plain>{{ username }}</el-button>
        <el-button v-if="username" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <el-card>
        <h2>{{ welcome }}</h2>
        <p>这是一个简单的首页示例。登录后会显示用户名与身份。</p>
      </el-card>
    </el-main>
  </el-container>
</template>

<script>
import api from '../services/api'
import { clearToken, getToken } from '../services/auth'
import { ref, onMounted } from 'vue'

export default {
  name: 'HomeView',
  setup() {
    const welcome = ref('欢迎访客！')
    const username = ref('')

    const load = async () => {
      try {
        const token = getToken()
        if (!token) return
        const res = await api.get('/api/home')
        username.value = res.data.username || ''
        welcome.value = res.data.message || welcome.value
      } catch (err) {
        console.warn(err)
      }
    }

    const goLogin = () => {
      window.location.href = '/login'
    }

    const logout = () => {
      clearToken()
      username.value = ''
      welcome.value = '已退出，欢迎访客！'
    }

    onMounted(load)
    return { welcome, username, goLogin, logout }
  }
}
</script>

<style scoped>
</style>
