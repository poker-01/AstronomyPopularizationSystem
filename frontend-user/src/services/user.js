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

export async function fetchProfile() {
  const res = await api.get('/api/users/me')
  return unwrap(res)
}

export async function updateProfile(payload) {
  const res = await api.put('/api/users/me', payload)
  return unwrap(res)
}

export async function changePassword(oldPassword, newPassword) {
  const res = await api.put('/api/users/me/password', { oldPassword, newPassword })
  return unwrap(res)
}
