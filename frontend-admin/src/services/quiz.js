import api from './api'

function unwrap(res) {
  const body = res.data
  if (body && typeof body.code === 'number') {
    if (body.code !== 0) throw new Error(body.message || '请求失败')
    return body.data
  }
  return body
}

export async function fetchQuestions(params = {}) {
  return unwrap(await api.get('/api/admin/questions', { params }))
}

export async function fetchQuestion(id) {
  return unwrap(await api.get(`/api/admin/questions/${id}`))
}

export async function createQuestion(payload) {
  return unwrap(await api.post('/api/admin/questions', payload))
}

export async function updateQuestion(id, payload) {
  return unwrap(await api.put(`/api/admin/questions/${id}`, payload))
}

export async function deleteQuestion(id) {
  return unwrap(await api.delete(`/api/admin/questions/${id}`))
}

export async function importQuestions(questions) {
  return unwrap(await api.post('/api/admin/questions/import', { questions }))
}

export async function fetchQuizzes() {
  return unwrap(await api.get('/api/admin/quizzes'))
}

export async function fetchQuiz(id) {
  return unwrap(await api.get(`/api/admin/quizzes/${id}`))
}

export async function createQuiz(payload) {
  return unwrap(await api.post('/api/admin/quizzes', payload))
}

export async function updateQuiz(id, payload) {
  return unwrap(await api.put(`/api/admin/quizzes/${id}`, payload))
}

export async function deleteQuiz(id) {
  return unwrap(await api.delete(`/api/admin/quizzes/${id}`))
}

export async function fetchBadges() {
  return unwrap(await api.get('/api/admin/badges'))
}

export async function createBadge(payload) {
  return unwrap(await api.post('/api/admin/badges', payload))
}

export async function updateBadge(id, payload) {
  return unwrap(await api.put(`/api/admin/badges/${id}`, payload))
}

export async function deleteBadge(id) {
  return unwrap(await api.delete(`/api/admin/badges/${id}`))
}
