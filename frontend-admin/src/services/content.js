import api from './api'

function unwrap(res) {
  const body = res.data
  if (body && typeof body.code === 'number') {
    if (body.code !== 0) throw new Error(body.message || '请求失败')
    return body.data
  }
  return body
}

export async function fetchCategories() {
  return unwrap(await api.get('/api/admin/content/categories'))
}

export async function fetchArticles(params = {}) {
  return unwrap(await api.get('/api/admin/content/articles', { params }))
}

export async function fetchArticle(id) {
  return unwrap(await api.get(`/api/admin/content/articles/${id}`))
}

export async function createArticle(payload) {
  return unwrap(await api.post('/api/admin/content/articles', payload))
}

export async function updateArticle(id, payload) {
  return unwrap(await api.put(`/api/admin/content/articles/${id}`, payload))
}

export async function deleteArticle(id) {
  return unwrap(await api.delete(`/api/admin/content/articles/${id}`))
}
