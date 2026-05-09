  <template>
  <div>
    <!-- 视频背景区域 -->
    <div class="video-container">
      <video 
        autoplay 
        muted 
        loop 
        playsinline
        webkit-playsinline
        preload="auto"
        :src="videoSrc"
        class="bg-video"
      >
        <source :src="videoSrc" type="video/mp4">
      </video>
      
      <!-- Logo -->
      <div class="logo">🌌</div>
      
      <!-- 导航栏 -->
      <nav class="navbar">
        <ul>
          <li><a href="#">首页</a></li>
          <li>
            <a href="#">天体展示</a>
            <div class="dropdown">
              <ul>
                <li><a href="#planets">行星</a></li>
                <li><a href="#">恒星</a></li>
                <li><a href="#">星系</a></li>
              </ul>
            </div>
          </li>
          <li>
            <a href="#">论坛</a>
            <div class="dropdown">
              <ul>
                <li><a href="#">讨论区</a></li>
                <li><a href="#">问答</a></li>
                <li><a href="#">精华帖</a></li>
              </ul>
            </div>
          </li>
          <li>
            <a href="#">测一测</a>
            <div class="dropdown">
              <ul>
                <li><a href="#">天文知识测验</a></li>
                <li><a href="#">星座配对</a></li>
                <li><a href="#">宇宙探索</a></li>
              </ul>
            </div>
          </li>
          <li>
            <a href="#">订阅</a>
            <div class="dropdown">
              <ul>
                <li><a href="#">天文资讯</a></li>
                <li><a href="#">观测提醒</a></li>
                <li><a href="#">会员服务</a></li>
              </ul>
            </div>
          </li>
          <li>
            <a href="#">个人中心</a>
            <div class="dropdown">
              <ul>
                <li><a href="#">我的收藏</a></li>
                <li><a href="#">浏览记录</a></li>
                <li><a href="#">设置</a></li>
              </ul>
            </div>
          </li>
          <li>
            <el-button v-if="!username" class="nav-login-btn" @click="goLogin">登录</el-button>
            <div v-else class="nav-user">
              <span class="nav-username">{{ username }}</span>
              <el-button class="nav-logout-btn" @click="logout">退出</el-button>
            </div>
          </li>
        </ul>
      </nav>
      
      <!-- 主标题内容 -->
      <div class="header-content">
        <h1>Solar System</h1>
        <p>{{ welcome }}</p>
      </div>
      
      <!-- 滚动提示 -->
      <div class="scroll-down"><i class="el-icon-arrow-down"></i></div>
    </div>

    <!-- 太阳系介绍区域 -->
    <div class="profile">
      <section id="planets" class="content-section">
        <h2>太阳系简介</h2>
        <div class="profile-grid">
          <div class="planet-card">
            <h3>形成与演变</h3>
            <p>约46亿年前，原始星云在引力作用下坍缩，中心形成原恒星（太阳），周围物质形成原行星盘并持续演化。</p>
          </div>
          <div class="planet-card">
            <h3>组成结构</h3>
            <p>太阳系由太阳、8个行星、近500个卫星和大量小行星，以及一些矮行星和彗星组成。</p>
          </div>
          <div class="planet-card">
            <h3>位置</h3>
            <p>太阳系位于银河系的猎户旋臂边缘，距离银河系中心大约2.4-2.7万光年。相对稳定的星际环境有利于生命的诞生和演化。</p>
          </div>
        </div>

        <h2>太阳系行星</h2>
        <p>我们的太阳系由八大行星组成，它们围绕太阳运行。</p>

        <div class="planet-grid">
          <div class="planet-card" v-for="item in planetCards" :key="item.name">
            <h3>{{ item.name }}</h3>
            <p>{{ item.text }}</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import api from '../services/api'
import { clearToken, getToken } from '../services/auth'
import { ref, onMounted } from 'vue'

export default {
  name: 'HomeView',
  setup() {
    const welcome = ref('一个关于太阳系的科普网站')
    const username = ref('')
    const videoSrc = ref(require('../assets/index/VID_20250526_203310.mp4'))
    
    // 行星数据
    const planetCards = ref([
      {
        name: '水星',
        text: '太阳系中最小的行星，也是离太阳最近的行星。表面温度变化极大，白天可达430°C，夜晚降至-180°C。'
      },
      {
        name: '金星',
        text: '太阳系中最热的行星，表面温度高达470°C。拥有浓厚的大气层，主要由二氧化碳组成。'
      },
      {
        name: '地球',
        text: '我们居住的星球，目前已知唯一有生命存在的行星。约71%的表面被水覆盖。'
      },
      {
        name: '火星',
        text: '被称为"红色星球"，表面富含氧化铁。拥有太阳系最高的火山和最大的峡谷。'
      },
      {
        name: '木星',
        text: '太阳系中最大的行星，质量是其他所有行星总和的2.5倍。拥有著名的大红斑风暴。'
      },
      {
        name: '土星',
        text: '以其壮观的环系统闻名，主要由冰和岩石颗粒组成。密度比水还低。'
      },
      {
        name: '天王星',
        text: '独特的侧向自转，看起来像是"躺着"绕太阳运行。大气中含有甲烷，呈现蓝色。'
      },
      {
        name: '海王星',
        text: '太阳系中风速最快的行星，可达2100km/h。1846年通过数学预测被发现。'
      }
    ])

    const load = async () => {
      try {
        const token = getToken()
        if (!token) {
          welcome.value = '一个关于太阳系的科普网站'
          return
        }
        const res = await api.get('/api/home')
        username.value = res.data.username || ''
        welcome.value = res.data.message || `欢迎回来，${username.value}！`
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
      welcome.value = '一个关于太阳系的科普网站'
    }

    onMounted(() => {
      load()
    })

    return { welcome, username, videoSrc, planetCards, goLogin, logout }
  }
}
</script>

<style scoped>
.video-container {
  position: relative;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.video-container video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: -1;
  min-width: 100%;
  min-height: 100%;
}

.bg-video {
  display: block;
  background: #000;
}

.logo {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 100;
  font-size: 32px;
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 10px 15px;
  border-radius: 50%;
}

.navbar {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 100;
  font-size: 40px;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 10px;
  padding-top: 2px;
  padding-right: 20px;
  border-radius: 30px;
}

.navbar ul {
  list-style-type: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
}

.navbar li {
  margin-left: 20px;
  position: relative;
}

.navbar a {
  color: white;
  text-decoration: none;
  font-size: 20px;
  transition: background-color 0.3s;
  display: inline-block;
  font-weight: bold;
  padding: 5px 20px;
}

.navbar a:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.dropdown {
  display: none;
  position: absolute;
  top: 100%;
  left: 0;
  background-color: rgba(255, 255, 255, 0.2);
  width: 120px;
  padding: 10px;
  margin-top: 10px;
  z-index: 1;
}

.dropdown ul {
  display: flex;
  flex-direction: column;
  margin: 0;
  padding: 0;
  list-style: none;
}

.dropdown li {
  margin: 0;
  padding: 0;
  width: 100%;
}

.dropdown a {
  padding: 10px 10px;
  display: block;
  color: white;
  text-decoration: none;
  font-size: 19px;
  transition: background-color 0.3s;
}

.dropdown a:hover {
  background-color: rgba(255, 255, 255, 0.3);
  text-decoration: none;
}

.navbar > ul > li::after {
  content: "";
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 100%;
  height: 15px;
  background: transparent;
}

.navbar > ul > li:hover > .dropdown {
  display: block;
}

/* 导航栏按钮样式 */
.nav-login-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-size: 16px;
  padding: 5px 20px;
  border-radius: 20px;
  font-weight: bold;
}

.nav-login-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-username {
  color: white;
  font-size: 16px;
  font-weight: bold;
}

.nav-logout-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-size: 14px;
  padding: 5px 15px;
  border-radius: 20px;
}

.nav-logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.header-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  width: 100%;
}

.header-content h1 {
  font-size: 7rem;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.header-content p {
  font-size: 1.8rem;
  margin-top: 20px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.profile {
  background: linear-gradient(to bottom, #000300, #004e92);
  padding: 50px 20px;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(370px, 1fr));
  gap: 40px;
  margin-top: 50px;
}

.content-section {
  max-width: 1700px;
  margin: 0 auto;
  padding: 40px 0;
}

.content-section h2 {
  font-size: 3rem;
  line-height: 200px;
  margin-bottom: 30px;
  text-align: center;
}

.content-section p {
  font-size: 1.6rem;
  line-height: 1.6;
  margin-bottom: 20px;
}

.planet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(370px, 1fr));
  gap: 30px;
  margin-top: 50px;
}

.planet-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  padding: 20px;
  backdrop-filter: blur(5px);
  transition: transform 0.3s;
}

.planet-card:hover {
  transform: translateY(-10px);
}

.planet-card h3 {
  font-size: 2.2rem;
  margin-top: 0;
  color: #fff;
}

.scroll-down {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 2rem;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateX(-50%) translateY(0);
  }
  50% {
    transform: translateX(-50%) translateY(10px);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar {
    right: 10px;
    padding: 5px 10px;
  }
  
  .navbar a {
    font-size: 16px;
    padding: 5px 10px;
  }
  
  .logo {
    font-size: 24px;
    padding: 8px 12px;
  }
  
  .header-content h1 {
    font-size: 4rem;
  }
  
  .header-content p {
    font-size: 1.2rem;
  }
  
  .content-section h2 {
    font-size: 2rem;
    line-height: 1.5;
  }
  
  .content-section p {
    font-size: 1.2rem;
  }
  
  .planet-card h3 {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .navbar {
    position: relative;
    top: auto;
    right: auto;
    margin-top: 70px;
    border-radius: 15px;
  }
  
  .navbar ul {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .navbar li {
    margin-left: 10px;
    margin-bottom: 10px;
  }
  
  .header-content h1 {
    font-size: 3rem;
  }
  
  .header-content p {
    font-size: 1rem;
  }
  
  .profile-grid,
  .planet-grid {
    grid-template-columns: 1fr;
  }
}
</style>