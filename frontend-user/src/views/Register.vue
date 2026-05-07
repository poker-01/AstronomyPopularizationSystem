<template>
  <el-row type="flex" justify="center" align="middle" style="height:80vh">
    <el-card style="width: 420px">
      <h2 style="text-align:center">注册</h2>
      <el-form :model="form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" placeholder="密码" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit" style="width:100%">注册</el-button>
        </el-form-item>
        <el-form-item style="text-align:center">
          <router-link to="/login">已有账号？登录</router-link>
        </el-form-item>
      </el-form>
    </el-card>
  </el-row>
</template>

<script>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register, login, saveToken } from '../services/auth'
import { ElMessage } from 'element-plus'

export default {
  name: 'RegisterView',
  setup() {
    const router = useRouter()
    const form = reactive({ username: '', password: '' })

    const onSubmit = async () => {
      try {
        await register(form.username, form.password)
        // auto-login
        const data = await login(form.username, form.password)
        saveToken(data.token)
        ElMessage.success('注册并登录成功')
        router.push('/')
      } catch (err) {
        ElMessage.error(err.response?.data?.error || '注册失败')
      }
    }

    return { form, onSubmit }
  }
}
</script>

<style scoped>
</style>
