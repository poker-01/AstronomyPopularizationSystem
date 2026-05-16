<template>
  <div class="register-container">
    <!-- 星空背景 -->
    <div class="stars-bg" ref="starsBg"></div>
    
    <!-- 注册表单 -->
    <div class="register-form-container">
      <div class="register-card">
        <div class="card-header">
          <div class="logo">🚀</div>
          <h2 class="title">加入天文之旅</h2>
          <p class="subtitle">创建您的天文科普账户</p>
        </div>
        
        <el-form :model="form" @submit.prevent="onSubmit" class="register-form">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="用户名"
              size="large"
              class="custom-input"
            >
              <template #prefix>
                <i class="el-icon-user"></i>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="form.password" 
              placeholder="密码" 
              show-password
              size="large"
              class="custom-input"
              @keyup.enter="onSubmit"
            >
              <template #prefix>
                <i class="el-icon-lock"></i>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click="onSubmit" 
              class="register-btn"
              :loading="loading"
            >
              {{ loading ? '注册中...' : '注册' }}
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <router-link to="/login" class="link">
              已有账号？立即登录
            </router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { register, login, saveToken, saveUserInfo } from '../services/auth'
import { ElMessage } from 'element-plus'

export default {
  name: 'RegisterView',
  setup() {
    const router = useRouter()
    const form = reactive({ username: '', password: '' })
    const loading = ref(false)
    const starsBg = ref(null)

    const createStars = () => {
      if (!starsBg.value) return
      
      const starsCount = 80
      const container = starsBg.value
      
      for (let i = 0; i < starsCount; i++) {
        const star = document.createElement('div')
        star.className = 'star'
        
        const size = Math.random() * 2 + 1
        const left = Math.random() * 100
        const top = Math.random() * 100
        const delay = Math.random() * 4
        
        star.style.width = `${size}px`
        star.style.height = `${size}px`
        star.style.left = `${left}%`
        star.style.top = `${top}%`
        star.style.animationDelay = `${delay}s`
        
        container.appendChild(star)
      }
    }

    const onSubmit = async () => {
      if (!form.username || !form.password) {
        ElMessage.warning('请输入用户名和密码')
        return
      }
      
      if (form.password.length < 4) {
        ElMessage.warning('密码长度至少4位')
        return
      }
      
      loading.value = true
      try {
        await register(form.username, form.password)
        // 自动登录
        const data = await login(form.username, form.password)
        saveToken(data.token)
        saveUserInfo(data)
        ElMessage.success('注册并登录成功')
        router.push('/')
      } catch (err) {
        ElMessage.error(err.response?.data?.message || err.message || '注册失败')
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      nextTick(() => {
        createStars()
      })
    })

    return { form, loading, starsBg, onSubmit }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a0a2a 0%, #1a1a4a 50%, #2d1a4a 100%);
  padding: 20px;
}

.stars-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.star {
  position: absolute;
  background: #ffffff;
  border-radius: 50%;
  animation: twinkle 3s infinite ease-in-out;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.2; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

.register-form-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 420px;
}

.register-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(128, 90, 213, 0.3);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
}

.register-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(123, 104, 238, 0.1), transparent);
  transition: left 0.6s ease;
}

.register-card:hover::before {
  left: 100%;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 3rem;
  margin-bottom: 15px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 10px;
  background: linear-gradient(45deg, #ffffff, #e6e6fa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  color: #b0b0ff;
  font-size: 0.9rem;
  margin: 0;
}

.register-form {
  width: 100%;
}

.custom-input {
  width: 100%;
}

  .custom-input :deep(.el-input__inner) {
  background: rgba(255, 255, 255, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: #000000;
  padding: 12px 15px;
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
}

.custom-input :deep(.el-input__inner:focus) {
  border-color: #7b68ee;
  box-shadow: 0 0 0 2px rgba(123, 104, 238, 0.2);
}

.custom-input :deep(.el-input__prefix) {
  color: #7b68ee;
  margin-right: 10px;
}

.register-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(45deg, #6a5acd, #7b68ee);
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.register-btn:hover {
  background: linear-gradient(45deg, #7b68ee, #9370db);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(123, 104, 238, 0.4);
}

.register-btn:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
  margin-top: 20px;
}

.link {
  color: #b0b0ff;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.link:hover {
  color: #7b68ee;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    padding: 15px;
  }
  
  .register-card {
    padding: 30px 25px;
  }
  
  .title {
    font-size: 1.7rem;
  }
  
  .logo {
    font-size: 2.5rem;
  }
}

@media (max-width: 480px) {
  .register-card {
    padding: 25px 20px;
    border-radius: 15px;
  }
  
  .title {
    font-size: 1.5rem;
  }
  
  .logo {
    font-size: 2rem;
  }
}
</style>
