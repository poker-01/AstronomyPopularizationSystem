import api from './api'

function unwrap(res) {
  const body = res.data
  if (body && typeof body.code === 'number') {
    if (body.code !== 0) throw new Error(body.message || '请求失败')
    return body.data
  }
  return body
}

export async function fetchPosts(params = {}) {
  return unwrap(await api.get('/api/posts', { params }))
}

export async function fetchFollowingPosts(params = {}) {
  return unwrap(await api.get('/api/posts', { params: { ...params, following: true } }))
}

export async function fetchMyPendingPosts(params = {}) {
  return unwrap(await api.get('/api/posts/mine/pending', { params }))
}

export async function fetchPost(id) {
  return unwrap(await api.get(`/api/posts/${id}`))
}

export async function createPost(payload) {
  return unwrap(await api.post('/api/posts', payload))
}

export async function fetchComments(postId) {
  return unwrap(await api.get(`/api/posts/${postId}/comments`))
}

export async function createComment(postId, payload) {
  return unwrap(await api.post(`/api/posts/${postId}/comments`, payload))
}

export async function likePost(postId) {
  return unwrap(await api.post(`/api/posts/${postId}/like`))
}

export async function unlikePost(postId) {
  return unwrap(await api.delete(`/api/posts/${postId}/like`))
}
