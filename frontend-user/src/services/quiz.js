import api from './api'

function unwrap(res) {
  const body = res.data
  if (body && typeof body.code === 'number') {
    if (body.code !== 0) throw new Error(body.message || '请求失败')
    return body.data
  }
  return body
}

export async function fetchQuizzes() {
  return unwrap(await api.get('/api/quizzes'))
}

export async function fetchQuiz(id) {
  return unwrap(await api.get(`/api/quizzes/${id}`))
}

export async function submitQuiz(id, answers) {
  return unwrap(await api.post(`/api/quizzes/${id}/submit`, { answers }))
}

export async function fetchMyBadges() {
  return unwrap(await api.get('/api/badges/mine'))
}
