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

export async function login(username, password) {
  const res = await api.post('/api/auth/login', { username, password })
  return unwrap(res)
}

export function saveToken(token) {
  localStorage.setItem('admin_auth_token', token)
}

export function saveUserInfo(info) {
  if (info.username) localStorage.setItem('admin_auth_username', info.username)
  if (info.role) localStorage.setItem('admin_auth_role', info.role)
}

export function clearToken() {
  localStorage.removeItem('admin_auth_token')
  localStorage.removeItem('admin_auth_username')
  localStorage.removeItem('admin_auth_role')
}

export function getToken() {
  return localStorage.getItem('admin_auth_token')
}

export function isLoggedIn() {
  return !!getToken()
}
