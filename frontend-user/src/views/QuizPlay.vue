<template>
  <div class="page-container">
    <router-link to="/quizzes" class="back-link">← 返回测验列表</router-link>

    <div v-loading="loading">
      <header v-if="quiz" class="page-header">
        <h1 class="section-title">{{ quiz.name }}</h1>
        <p class="section-subtitle">{{ quiz.description }}</p>
      </header>

      <div v-if="!submitted && quiz" class="questions">
        <section
          v-for="(q, idx) in quiz.questions"
          :key="q.id"
          class="question-block glass-card"
        >
          <div class="q-head">
            <span class="q-num">第 {{ idx + 1 }} 题</span>
            <el-tag size="small" type="info">{{ q.difficulty }}</el-tag>
            <el-tag size="small">{{ q.type === 'MULTIPLE' ? '多选' : '单选' }}</el-tag>
          </div>
          <p class="q-stem">{{ q.stem }}</p>
          <el-checkbox-group
            v-if="q.type === 'MULTIPLE'"
            v-model="answers[q.id]"
          >
            <el-checkbox
              v-for="opt in q.options"
              :key="opt.key"
              :label="opt.key"
              class="option-item"
            >
              {{ opt.key }}. {{ opt.text }}
            </el-checkbox>
          </el-checkbox-group>
          <el-radio-group v-else v-model="singleAnswers[q.id]">
            <el-radio
              v-for="opt in q.options"
              :key="opt.key"
              :label="opt.key"
              class="option-item"
            >
              {{ opt.key }}. {{ opt.text }}
            </el-radio>
          </el-radio-group>
        </section>

        <div class="submit-bar">
          <el-button type="primary" size="large" :loading="submitting" @click="onSubmit">
            提交答卷
          </el-button>
        </div>
      </div>

      <div v-else-if="submitted && result" class="result-panel glass-card">
        <h2>测验结果</h2>
        <p class="score-line">
          答对 <strong>{{ result.score }}</strong> / {{ result.total }} 题
          <span class="percent">（{{ result.percent }} 分）</span>
        </p>
        <div class="result-list">
          <div
            v-for="q in quiz.questions"
            :key="q.id"
            class="result-item"
            :class="{ correct: result.results[q.id]?.correct }"
          >
            <span class="ri-icon">{{ result.results[q.id]?.correct ? '✓' : '✗' }}</span>
            <div>
              <p class="ri-stem">{{ q.stem }}</p>
              <p v-if="!result.results[q.id]?.correct" class="ri-answer">
                正确答案：{{ (result.results[q.id]?.correctAnswer || []).join('、') }}
              </p>
              <p v-if="result.results[q.id]?.explanation" class="ri-exp">
                {{ result.results[q.id].explanation }}
              </p>
            </div>
          </div>
        </div>
        <div class="result-actions">
          <el-button @click="retry">再测一次</el-button>
          <router-link to="/achievements">
            <el-button type="primary">我的成就</el-button>
          </router-link>
        </div>
      </div>
    </div>

    <BadgeUnlock :badges="newBadges" @done="newBadges = []" />
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchQuiz, submitQuiz } from '../services/quiz'
import { getToken } from '../services/auth'
import BadgeUnlock from '../components/BadgeUnlock.vue'

export default {
  name: 'QuizPlayView',
  components: { BadgeUnlock },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(true)
    const submitting = ref(false)
    const quiz = ref(null)
    const answers = reactive({})
    const singleAnswers = reactive({})
    const submitted = ref(false)
    const result = ref(null)
    const newBadges = ref([])

    const initAnswers = (data) => {
      for (const q of data.questions || []) {
        if (q.type === 'MULTIPLE') {
          answers[q.id] = []
        } else {
          singleAnswers[q.id] = ''
        }
      }
    }

    const load = async () => {
      loading.value = true
      submitted.value = false
      result.value = null
      try {
        const data = await fetchQuiz(route.params.id)
        quiz.value = data
        initAnswers(data)
      } catch (e) {
        ElMessage.error(e.message || '加载失败')
        router.push('/quizzes')
      } finally {
        loading.value = false
      }
    }

    const buildPayload = () => {
      const payload = {}
      for (const q of quiz.value.questions) {
        if (q.type === 'MULTIPLE') {
          payload[String(q.id)] = answers[q.id] || []
        } else {
          const key = singleAnswers[q.id]
          payload[String(q.id)] = key ? [key] : []
        }
      }
      return payload
    }

    const onSubmit = async () => {
      if (!getToken()) {
        ElMessage.warning('请先登录后再提交')
        router.push({ name: 'Login', query: { redirect: route.fullPath } })
        return
      }
      submitting.value = true
      try {
        const res = await submitQuiz(route.params.id, buildPayload())
        result.value = res
        submitted.value = true
        if (res.newBadges && res.newBadges.length) {
          newBadges.value = res.newBadges
        }
        window.scrollTo({ top: 0, behavior: 'smooth' })
      } catch (e) {
        ElMessage.error(e.message || '提交失败')
      } finally {
        submitting.value = false
      }
    }

    const retry = () => {
      submitted.value = false
      result.value = null
      initAnswers(quiz.value)
    }

    onMounted(load)
    watch(() => route.params.id, load)

    return {
      loading,
      quiz,
      answers,
      singleAnswers,
      submitting,
      submitted,
      result,
      newBadges,
      onSubmit,
      retry
    }
  }
}
</script>

<style scoped>
.back-link {
  display: inline-block;
  margin-bottom: 1.5rem;
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.9rem;
}

.back-link:hover {
  color: var(--accent-cyan);
}

.page-header {
  margin-bottom: 2rem;
  text-align: center;
}

.questions {
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.question-block {
  padding: 1.25rem 1.5rem;
}

.q-head {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.q-num {
  font-weight: 600;
  color: var(--accent-cyan);
}

.q-stem {
  margin: 0 0 1rem;
  line-height: 1.6;
}

.option-item {
  display: flex;
  margin: 0.35rem 0;
}

.submit-bar {
  text-align: center;
  padding: 1rem 0 2rem;
}

.result-panel {
  max-width: 720px;
  margin: 0 auto;
  padding: 1.5rem 2rem;
}

.score-line {
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
}

.score-line strong {
  color: var(--accent-cyan);
  font-size: 1.4rem;
}

.percent {
  color: var(--text-muted);
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.result-item {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.03);
  border-left: 3px solid #ef4444;
}

.result-item.correct {
  border-left-color: #22c55e;
}

.ri-icon {
  font-weight: bold;
  font-size: 1.1rem;
}

.ri-stem {
  margin: 0 0 0.35rem;
  font-weight: 500;
}

.ri-answer,
.ri-exp {
  margin: 0.25rem 0 0;
  font-size: 0.9rem;
  color: var(--text-muted);
}

.result-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 2rem;
}
</style>
