<template>
  <header class="navbar" :class="{ scrolled }">
    <div class="nav-inner">
      <router-link to="/" class="brand">
        <span class="brand-icon">✦</span>
        <span class="brand-text">COSMOS<span class="brand-accent">科普</span></span>
      </router-link>

      <button class="menu-toggle" @click="menuOpen = !menuOpen" aria-label="菜单">
        <span></span><span></span><span></span>
      </button>

      <nav class="nav-links" :class="{ open: menuOpen }">
        <router-link to="/" @click="menuOpen = false">首页</router-link>
        <router-link to="/planets" @click="menuOpen = false">太阳系</router-link>
        <router-link to="/articles" @click="menuOpen = false">科普文库</router-link>
        <router-link to="/explore" @click="menuOpen = false">探索历程</router-link>
        <router-link to="/quizzes" @click="menuOpen = false">趣味测验</router-link>
        <router-link v-if="loggedIn" to="/achievements" @click="menuOpen = false">我的成就</router-link>
        <span class="nav-divider"></span>
        <router-link v-if="loggedIn" to="/profile" class="nav-profile" @click="menuOpen = false">
          {{ displayName }}
        </router-link>
        <router-link v-else to="/login" class="nav-cta" @click="menuOpen = false">登录</router-link>
        <button v-if="loggedIn" class="nav-logout" @click="logout">退出</button>
      </nav>
    </div>
  </header>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getToken, clearToken } from '../services/auth'

export default {
  name: 'AppNavbar',
  setup() {
    const router = useRouter()
    const menuOpen = ref(false)
    const scrolled = ref(false)

    const loggedIn = computed(() => !!getToken())
    const displayName = computed(() =>
      localStorage.getItem('auth_nickname') || localStorage.getItem('auth_username') || '我的账户'
    )

    const onScroll = () => {
      scrolled.value = window.scrollY > 24
    }

    const logout = () => {
      clearToken()
      menuOpen.value = false
      router.push('/')
      window.location.reload()
    }

    onMounted(() => window.addEventListener('scroll', onScroll))
    onUnmounted(() => window.removeEventListener('scroll', onScroll))

    return { menuOpen, scrolled, loggedIn, displayName, logout }
  }
}
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid transparent;
  transition: background 0.3s, border-color 0.3s, backdrop-filter 0.3s;
}

.navbar.scrolled {
  background: rgba(3, 0, 20, 0.85);
  border-bottom-color: var(--glass-border);
  backdrop-filter: blur(14px);
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  font-weight: 800;
  letter-spacing: 0.06em;
  font-size: 1.1rem;
}

.brand-icon {
  color: var(--accent-cyan);
  font-size: 1.3rem;
}

.brand-accent {
  color: var(--accent-violet);
  font-weight: 600;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.nav-links a {
  text-decoration: none;
  color: var(--text-muted);
  font-size: 0.95rem;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: #fff;
}

.nav-divider {
  width: 1px;
  height: 20px;
  background: var(--glass-border);
}

.nav-cta {
  padding: 0.45rem 1rem;
  border-radius: 6px;
  background: linear-gradient(135deg, var(--accent-indigo), var(--accent-violet));
  color: #fff !important;
}

.nav-profile {
  color: var(--accent-cyan) !important;
}

.nav-logout {
  background: none;
  border: 1px solid var(--glass-border);
  color: var(--text-muted);
  padding: 0.4rem 0.9rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
}

.nav-logout:hover {
  color: #fff;
  border-color: var(--accent-violet);
}

.menu-toggle {
  display: none;
  flex-direction: column;
  gap: 5px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
}

.menu-toggle span {
  display: block;
  width: 22px;
  height: 2px;
  background: #fff;
}

@media (max-width: 768px) {
  .menu-toggle {
    display: flex;
  }

  .nav-links {
    position: absolute;
    top: 72px;
    left: 0;
    right: 0;
    flex-direction: column;
    background: rgba(3, 0, 20, 0.96);
    padding: 1.5rem;
    border-bottom: 1px solid var(--glass-border);
    display: none;
  }

  .nav-links.open {
    display: flex;
  }

  .nav-divider {
    display: none;
  }
}
</style>
