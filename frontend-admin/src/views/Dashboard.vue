<template>
  <div class="dashboard-container">
    <!-- 星空背景 -->
    <div class="stars-bg" ref="starsBg"></div>
    
    <!-- 主要内容 -->
    <div class="dashboard-content">
      <!-- 头部导航 -->
      <header class="header">
        <div class="logo">
          <span class="logo-text">🔧 管理员后台</span>
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
        <div class="dashboard-section">
          <h1 class="dashboard-title">系统管理面板</h1>
          <p class="dashboard-subtitle">欢迎使用天文科普系统管理后台</p>
          
          <div class="stats-grid">
            <div class="stat-card" v-for="stat in stats" :key="stat.id">
              <div class="stat-icon">{{ stat.icon }}</div>
              <div class="stat-content">
                <h3>{{ stat.title }}</h3>
                <p class="stat-value">{{ stat.value }}</p>
                <span class="stat-label">{{ stat.label }}</span>
              </div>
            </div>
          </div>
          
          <div class="quick-actions">
            <h3>快速操作</h3>
            <div class="actions-grid">
              <div class="action-card" v-for="action in quickActions" :key="action.id">
                <div class="action-icon">{{ action.icon }}</div>
                <h4>{{ action.title }}</h4>
                <p>{{ action.description }}</p>
                <el-button class="action-btn" @click="handleAction(action.id)">
                  {{ action.buttonText }}
                </el-button>
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
import { fetchStats } from '../services/users'
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'AdminDashboardView',
  setup() {
    const router = useRouter()
    const username = ref(localStorage.getItem('admin_auth_username') || '')
    const starsBg = ref(null)
    
    const stats = ref([
      { id: 1, icon: '👥', title: '用户总数', value: '-', label: '正常用户' },
      { id: 2, icon: '📊', title: '今日访问', value: '567', label: '页面浏览量' },
      { id: 3, icon: '⭐', title: '活跃用户', value: '89', label: '在线用户' },
      { id: 4, icon: '📝', title: '内容数量', value: '456', label: '文章/视频' }
    ])
    
    const quickActions = ref([
      { id: 1, icon: '👤', title: '用户管理', description: '管理用户账户和权限', buttonText: '管理用户' },
      { id: 2, icon: '📚', title: '内容管理', description: '编辑和发布天文科普文章', buttonText: '管理内容' },
      { id: 5, icon: '❓', title: '题库管理', description: '题目 CRUD 与 JSON 批量导入', buttonText: '管理题库' },
      { id: 6, icon: '🧪', title: '测验套卷', description: '组卷并绑定题目', buttonText: '管理套卷' },
      { id: 7, icon: '🏅', title: '成就徽章', description: '配置徽章规则与图标', buttonText: '管理徽章' },
      { id: 3, icon: '📈', title: '数据统计', description: '查看系统使用数据', buttonText: '查看统计' },
      { id: 4, icon: '⚙️', title: '系统设置', description: '配置系统参数', buttonText: '系统设置' }
    ])

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

    const load = async () => {
      try {
        const token = getToken()
        if (!token) return
        const res = await api.get('/api/home')
        const payload = res.data?.data ?? res.data
        if (payload.role === 'ADMIN') username.value = payload.username
        const statData = await fetchStats()
        stats.value[0].value = String(statData.userCount ?? 0)
      } catch (err) {
        console.warn(err)
      }
    }

    const handleAction = (actionId) => {
      if (actionId === 1) {
        router.push('/users')
        return
      }
      if (actionId === 2) {
        router.push('/content')
        return
      }
      if (actionId === 5) {
        router.push('/questions')
        return
      }
      if (actionId === 6) {
        router.push('/quizzes')
        return
      }
      if (actionId === 7) {
        router.push('/badges')
        return
      }
      ElMessage.info('功能开发中')
    }

    const goLogin = () => { router.push('/login') }
    const logout = () => { clearToken(); username.value = '' }

    onMounted(() => {
      load()
      nextTick(() => {
        createStars()
      })
    })

    return { username, starsBg, stats, quickActions, handleAction, goLogin, logout }
  }
}
</script>

<style scoped>
.dashboard-container {
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

.dashboard-content {
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
  padding: 40px;
}

.dashboard-section {
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 10px;
  background: linear-gradient(45deg, #ffffff, #e6e6fa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.dashboard-subtitle {
  color: #b0b0ff;
  font-size: 1.1rem;
  margin-bottom: 40px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 50px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(128, 90, 213, 0.3);
  border-radius: 15px;
  padding: 25px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(123, 104, 238, 0.2);
  border-color: rgba(123, 104, 238, 0.6);
}

.stat-icon {
  font-size: 2.5rem;
  opacity: 0.8;
}

.stat-content h3 {
  margin: 0 0 5px 0;
  font-size: 1rem;
  color: #b0b0ff;
  font-weight: normal;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: bold;
  margin: 0;
  color: #ffffff;
}

.stat-label {
  font-size: 0.8rem;
  color: #8888cc;
}

.quick-actions h3 {
  font-size: 1.5rem;
  margin-bottom: 25px;
  color: #ffffff;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 25px;
}

.action-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(128, 90, 213, 0.3);
  border-radius: 15px;
  padding: 25px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(123, 104, 238, 0.1), transparent);
  transition: left 0.5s ease;
}

.action-card:hover::before {
  left: 100%;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(123, 104, 238, 0.3);
  border-color: rgba(123, 104, 238, 0.6);
}

.action-icon {
  font-size: 2.5rem;
  margin-bottom: 15px;
}

.action-card h4 {
  font-size: 1.2rem;
  margin-bottom: 10px;
  color: #ffffff;
}

.action-card p {
  color: #b0b0ff;
  margin-bottom: 20px;
  line-height: 1.5;
}

.action-btn {
  background: linear-gradient(45deg, #6a5acd, #7b68ee);
  border: none;
  color: white;
  padding: 8px 20px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: linear-gradient(45deg, #7b68ee, #9370db);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(123, 104, 238, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 15px 20px;
    flex-direction: column;
    gap: 15px;
  }
  
  .main-content {
    padding: 20px;
  }
  
  .dashboard-title {
    font-size: 2rem;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .action-card {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .dashboard-title {
    font-size: 1.7rem;
  }
  
  .main-content {
    padding: 15px;
  }
  
  .stat-card {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
}
</style>
