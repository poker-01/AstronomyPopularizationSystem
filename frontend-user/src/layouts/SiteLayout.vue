<template>
  <div class="site-layout">
    <StarfieldBg />
    <AppNavbar />
    <main class="site-main">
      <router-view />
    </main>
    <footer class="site-footer">
      <div class="footer-inner">
        <p>数据来源：NASA Open APIs · 仅供天文科普学习</p>
        <p class="footer-links">
          <a href="https://www.nasa.gov" target="_blank" rel="noopener">NASA.gov</a>
          <span>·</span>
          <a href="https://api.nasa.gov" target="_blank" rel="noopener">API.NASA.gov</a>
        </p>
      </div>
    </footer>
  </div>
</template>

<script>
import StarfieldBg from '../components/StarfieldBg.vue'
import AppNavbar from '../components/AppNavbar.vue'

export default {
  name: 'SiteLayout',
  components: { StarfieldBg, AppNavbar }
}
</script>

<style scoped>
.site-layout {
  min-height: 100vh;
  position: relative;
}

.site-footer {
  position: relative;
  z-index: 1;
  border-top: 1px solid var(--glass-border);
  background: rgba(3, 0, 20, 0.6);
  padding: 1.5rem;
  text-align: center;
}

.footer-inner p {
  margin: 0.25rem 0;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.footer-links a {
  color: var(--accent-cyan);
  text-decoration: none;
}

.footer-links a:hover {
  text-decoration: underline;
}
</style>
