<template>
  <div class="page-container">
    <header class="page-header">
      <h1 class="section-title">趣味测验</h1>
      <p class="section-subtitle">
        挑战天文知识题库，完成测验即可解锁成就徽章。登录后提交答案并计入个人成就。
      </p>
      <router-link v-if="loggedIn" to="/achievements" class="achievements-link">查看我的成就 →</router-link>
    </header>

    <div v-loading="loading" class="quiz-grid">
      <article
        v-for="quiz in quizzes"
        :key="quiz.id"
        class="quiz-card glass-card"
        @click="goPlay(quiz.id)"
      >
        <div class="quiz-icon">🧪</div>
        <h3>{{ quiz.name }}</h3>
        <p>{{ quiz.description || '暂无描述' }}</p>
        <span class="quiz-meta">{{ quiz.questionCount }} 道题</span>
        <el-button type="primary" plain size="small" class="quiz-btn">开始测验</el-button>
      </article>
      <p v-if="!loading && quizzes.length === 0" class="empty">暂无可用测验，请稍后再来。</p>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchQuizzes } from '../services/quiz'
import { getToken } from '../services/auth'

export default {
  name: 'QuizzesView',
  setup() {
    const router = useRouter()
    const loading = ref(true)
    const quizzes = ref([])
    const loggedIn = computed(() => !!getToken())

    onMounted(async () => {
      try {
        quizzes.value = await fetchQuizzes()
      } finally {
        loading.value = false
      }
    })

    const goPlay = (id) => {
      router.push({ name: 'QuizPlay', params: { id } })
    }

    return { loading, quizzes, loggedIn, goPlay }
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 2.5rem;
  text-align: center;
}

.achievements-link {
  display: inline-block;
  margin-top: 1rem;
  color: var(--accent-cyan);
  text-decoration: none;
  font-size: 0.95rem;
}

.achievements-link:hover {
  text-decoration: underline;
}

.quiz-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
  max-width: 1000px;
  margin: 0 auto;
}

.quiz-card {
  padding: 1.5rem;
  cursor: pointer;
  transition: transform 0.2s, border-color 0.2s;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.quiz-card:hover {
  transform: translateY(-4px);
  border-color: var(--accent-violet);
}

.quiz-icon {
  font-size: 2rem;
}

.quiz-card h3 {
  margin: 0;
  font-size: 1.2rem;
}

.quiz-card p {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.9rem;
  line-height: 1.5;
  flex: 1;
}

.quiz-meta {
  font-size: 0.85rem;
  color: var(--accent-cyan);
}

.quiz-btn {
  align-self: flex-start;
  margin-top: 0.5rem;
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  color: var(--text-muted);
}
</style>
