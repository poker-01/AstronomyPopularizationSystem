import { createRouter, createWebHistory } from 'vue-router'
import SiteLayout from '../layouts/SiteLayout.vue'
import Home from '../views/Home.vue'
import Planets from '../views/Planets.vue'
import Explore from '../views/Explore.vue'
import Articles from '../views/Articles.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Profile from '../views/Profile.vue'
import Quizzes from '../views/Quizzes.vue'
import QuizPlay from '../views/QuizPlay.vue'
import Achievements from '../views/Achievements.vue'
import { getToken } from '../services/auth'

const routes = [
  {
    path: '/',
    component: SiteLayout,
    children: [
      { path: '', name: 'Home', component: Home },
      { path: 'planets', name: 'Planets', component: Planets },
      { path: 'explore', name: 'Explore', component: Explore },
      { path: 'articles', name: 'Articles', component: Articles },
      { path: 'articles/:slug', name: 'ArticleDetail', component: ArticleDetail },
      { path: 'quizzes', name: 'Quizzes', component: Quizzes },
      { path: 'quizzes/:id', name: 'QuizPlay', component: QuizPlay },
      { path: 'achievements', name: 'Achievements', component: Achievements, meta: { requiresAuth: true } }
    ]
  },
  { path: '/login', name: 'Login', component: Login, meta: { guest: true } },
  { path: '/register', name: 'Register', component: Register, meta: { guest: true } },
  { path: '/profile', name: 'Profile', component: Profile, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
  scrollBehavior(to) {
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const loggedIn = !!getToken()
  if (to.meta.requiresAuth && !loggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.guest && loggedIn && (to.name === 'Login' || to.name === 'Register')) {
    next({ name: 'Home' })
    return
  }
  next()
})

export default router
