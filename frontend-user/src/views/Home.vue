<template>
  <div class="home-container">
    <!-- 星空背景 -->
    <div class="stars-bg" ref="starsBg"></div>
    
    <!-- 主要内容 -->
    <div class="home-content">
      <!-- 头部导航 -->
      <header class="header">
        <div class="logo">
          <span class="logo-text">🌌 天文科普系统</span>
        </div>
        <div class="user-actions">
          <el-button v-if="!username" class="login-btn" @click="goLogin">
            <i class="el-icon-user"></i> 登录
          </el-button>
          <div v-else class="user-info">
            <span class="username">{{ username }}</span>
            <el-button class="logout-btn" @click="logout">退出</el-button>
          </div>
        </div>
      </header>

      <!-- 主内容区域 -->
      <main class="main-content">
        <div class="hero-section">
          <div class="hero-content">
            <h1 class="hero-title">探索宇宙奥秘</h1>
            <p class="hero-subtitle">{{ welcome }}</p>
            <div class="hero-cards">
              <div class="feature-card" v-for="feature in features" :key="feature.id">
                <div class="card-icon">{{ feature.icon }}</div>
                <h3>{{ feature.title }}</h3>
                <p>{{ feature.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script>
import api from '../services/api'
import { clearToken, getToken } from '../services/auth'
import { ref, onMounted, nextTick } from 'vue'

export default {
  name: 'HomeView',
  setup() {
    const welcome = ref('欢迎来到天文科普世界！')
    const username = ref('')
    const starsBg = ref(null)
    
    const features = ref([
      { id: 1, icon: '🌠', title: '行星探索', description: '深入了解太阳系各大行星的奥秘' },
      { id: 2, icon: '⭐', title: '星座知识', description: '探索古老星座背后的神话与科学' },
      { id: 3, icon: '🌌', title: '宇宙奥秘', description: '揭开黑洞、暗物质等宇宙未解之谜' },
      { id: 4, icon: '🔭', title: '天文观测', description: '学习天文望远镜使用和观测技巧' }
    ])

    const load = async () => {
      try {
        const token = getToken()
        if (!token) {
          welcome.value = '探索宇宙奥秘，开启天文之旅'
          return
        }
        const res = await api.get('/api/home')
        username.value = res.data.username || ''
        welcome.value = res.data.message || `欢迎回来，${username.value}！`
      } catch (err) {
        console.warn(err)
      }
    }

    const createStars = () => {
      if (!starsBg.value) return
      
      const starsCount = 100
      const container = starsBg.value
      
      for (let i = 0; i < starsCount; i++) {
        const star = document.createElement('div')
        star.className = 'star'
        
        // 随机位置和大小
        const size = Math.random() * 3 + 1
        const left = Math.random() * 100
        const top = Math.random() * 100
        const delay = Math.random() * 5
        
        star.style.width = `${size}px`
        star.style.height = `${size}px`
        star.style.left = `${left}%`
        star.style.top = `${top}%`
        star.style.animationDelay = `${delay}s`
        
        container.appendChild(star)
      }
    }

    const goLogin = () => {
      window.location.href = '/login'
    }

    const logout = () => {
      clearToken()
      username.value = ''
      welcome.value = '探索宇宙奥秘，开启天文之旅'
    }

    onMounted(() => {
      load()
      nextTick(() => {
        createStars()
      })
    })

    return { welcome, username, starsBg, features, goLogin, logout }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.stars-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  pointer-events: none;
}

.star {
  position: absolute;
  background: #ffffff;
  border-radius: 50%;
  animation: twinkle 3s infinite ease-in-out;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.2); }
}

.home-content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: rgba(10, 10, 42, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(128, 90, 213, 0.3);
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(45deg, #7b68ee, #9370db, #ba55d3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.login-btn {
  background: linear-gradient(45deg, #4a4a8a, #6a5acd);
  border: none;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(45deg, #6a5acd, #7b68ee);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(123, 104, 238, 0.4);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  color: #e6e6fa;
  font-weight: 500;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #e6e6fa;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
}

.main-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.hero-section {
  text-align: center;
  max-width: 1200px;
  width: 100%;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: bold;
  margin-bottom: 20px;
  background: linear-gradient(45deg, #ffffff, #e6e6fa, #d8bfd8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 30px rgba(123, 104, 238, 0.5);
}

.hero-subtitle {
  font-size: 1.3rem;
  color: #c0c0ff;
  margin-bottom: 50px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
  margin-top: 50px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(128, 90, 213, 0.3);
  border-radius: 15px;
  padding: 30px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(123, 104, 238, 0.2), transparent);
  transition: left 0.5s ease;
}

.feature-card:hover::before {
  left: 100%;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(123, 104, 238, 0.3);
  border-color: rgba(123, 104, 238, 0.6);
}

.card-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 10px;
  color: #ffffff;
}

.feature-card p {
  color: #b0b0ff;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 15px 20px;
    flex-direction: column;
    gap: 15px;
  }
  
  .logo-text {
    font-size: 20px;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-subtitle {
    font-size: 1.1rem;
  }
  
  .hero-cards {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .feature-card {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-subtitle {
    font-size: 1rem;
  }
  
  .main-content {
    padding: 20px 15px;
  }
}
</style>