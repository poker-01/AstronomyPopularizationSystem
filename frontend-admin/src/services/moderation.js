import api from './api'

function unwrap(res) {
  const body = res.data
  if (body && typeof body.code === 'number') {
    if (body.code !== 0) throw new Error(body.message || '请求失败')
    return body.data
  }
  return body
}

export async function fetchModerationPosts(params = {}) {
  return unwrap(await api.get('/api/admin/moderation/posts', { params }))
}

export async function fetchModerationComments(params = {}) {
  return unwrap(await api.get('/api/admin/moderation/comments', { params }))
}

export async function approvePost(id) {
  return unwrap(await api.put(`/api/admin/moderation/posts/${id}/approve`))
}

export async function rejectPost(id, reason) {
  return unwrap(await api.put(`/api/admin/moderation/posts/${id}/reject`, { rejectReason: reason || '' }))
}

export async function approveComment(id) {
  return unwrap(await api.put(`/api/admin/moderation/comments/${id}/approve`))
}

export async function rejectComment(id, reason) {
  return unwrap(await api.put(`/api/admin/moderation/comments/${id}/reject`, { rejectReason: reason || '' }))
}
