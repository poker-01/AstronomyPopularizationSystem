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
  return unwrap(await api.get('/api/content/categories'))
}

export async function fetchArticles(params = {}) {
  return unwrap(await api.get('/api/content/articles', { params }))
}

export async function fetchArticle(slug) {
  return unwrap(await api.get(`/api/content/articles/${slug}`))
}
