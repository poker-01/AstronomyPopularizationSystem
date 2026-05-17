import { createRouter, createWebHistory } from 'vue-router'
import AdminLogin from '../views/AdminLogin.vue'
import Dashboard from '../views/Dashboard.vue'
import UserList from '../views/UserList.vue'
import ArticleList from '../views/ArticleList.vue'
import QuestionList from '../views/QuestionList.vue'
import QuizList from '../views/QuizList.vue'
import BadgeList from '../views/BadgeList.vue'
import ModerationList from '../views/ModerationList.vue'
import CalendarEventList from '../views/CalendarEventList.vue'
import { getToken } from '../services/auth'

const routes = [
  { path: '/login', name: 'AdminLogin', component: AdminLogin, meta: { guest: true } },
  { path: '/', name: 'Dashboard', component: Dashboard, meta: { requiresAuth: true } },
  { path: '/users', name: 'UserList', component: UserList, meta: { requiresAuth: true } },
  { path: '/content', name: 'ArticleList', component: ArticleList, meta: { requiresAuth: true } },
  { path: '/questions', name: 'QuestionList', component: QuestionList, meta: { requiresAuth: true } },
  { path: '/quizzes', name: 'QuizList', component: QuizList, meta: { requiresAuth: true } },
  { path: '/badges', name: 'BadgeList', component: BadgeList, meta: { requiresAuth: true } },
  { path: '/moderation', name: 'ModerationList', component: ModerationList, meta: { requiresAuth: true } },
  { path: '/calendar', name: 'CalendarEventList', component: CalendarEventList, meta: { requiresAuth: true } }
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
