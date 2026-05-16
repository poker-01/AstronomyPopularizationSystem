import { createRouter, createWebHistory } from 'vue-router'
import AdminLogin from '../views/AdminLogin.vue'
import Dashboard from '../views/Dashboard.vue'
import UserList from '../views/UserList.vue'
import ArticleList from '../views/ArticleList.vue'
import { getToken } from '../services/auth'

const routes = [
  { path: '/login', name: 'AdminLogin', component: AdminLogin, meta: { guest: true } },
  { path: '/', name: 'Dashboard', component: Dashboard, meta: { requiresAuth: true } },
  { path: '/users', name: 'UserList', component: UserList, meta: { requiresAuth: true } },
  { path: '/content', name: 'ArticleList', component: ArticleList, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const loggedIn = !!getToken()
  if (to.meta.requiresAuth && !loggedIn) {
    next({ name: 'AdminLogin', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.guest && loggedIn && to.name === 'AdminLogin') {
    next({ name: 'Dashboard' })
    return
  }
  next()
})

export default router
