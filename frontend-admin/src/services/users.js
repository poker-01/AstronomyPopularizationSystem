import api from './api'

function unwrap(res) {
  const body = res.data
  if (body && typeof body.code === 'number') {
    if (body.code !== 0) {
      const err = new Error(body.message || '请求失败')
      err.code = body.code
      throw err
    }
    return body.data
  }
  return body
}

export async function fetchUsers(params = {}) {
  const res = await api.get('/api/admin/users', { params })
  return unwrap(res)
}

export async function fetchUser(id) {
  const res = await api.get(`/api/admin/users/${id}`)
  return unwrap(res)
}

export async function createUser(payload) {
  const res = await api.post('/api/admin/users', payload)
  return unwrap(res)
}

export async function updateUser(id, payload) {
  const res = await api.put(`/api/admin/users/${id}`, payload)
  return unwrap(res)
}

export async function deleteUser(id) {
  const res = await api.delete(`/api/admin/users/${id}`)
  return unwrap(res)
}

export async function fetchStats() {
  const res = await api.get('/api/admin/stats')
  return unwrap(res)
}
