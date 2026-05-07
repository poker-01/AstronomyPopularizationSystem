<template>
  <el-row type="flex" justify="center" align="middle" style="height:80vh">
    <el-card style="width: 420px">
      <h2 style="text-align:center">登录</h2>
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" placeholder="密码" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit" style="width:100%">登录</el-button>
        </el-form-item>
        <el-form-item style="text-align:center">
          <router-link to="/register">还没有账号？注册</router-link>
        </el-form-item>
      </el-form>
    </el-card>
  </el-row>
</template>

<script>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login, saveToken } from '../services/auth'
import { ElMessage } from 'element-plus'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const form = reactive({ username: '', password: '' })

    const onSubmit = async () => {
      try {
        const data = await login(form.username, form.password)
        saveToken(data.token)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (err) {
        ElMessage.error(err.response?.data?.error || '登录失败')
      }
    }

    return { form, onSubmit }
  }
}
</script>

<style scoped>
</style>
