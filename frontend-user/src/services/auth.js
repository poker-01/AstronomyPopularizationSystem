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

export async function register(username, password) {
  const res = await api.post('/api/auth/register', { username, password })
  return unwrap(res)
}

export async function login(username, password) {
  const res = await api.post('/api/auth/login', { username, password })
  return unwrap(res)
}

export function saveToken(token) {
  localStorage.setItem('auth_token', token)
}

export function saveUserInfo(info) {
  if (info.username) localStorage.setItem('auth_username', info.username)
  if (info.role) localStorage.setItem('auth_role', info.role)
  if (info.nickname) localStorage.setItem('auth_nickname', info.nickname)
}

export function clearToken() {
  localStorage.removeItem('auth_token')
  localStorage.removeItem('auth_username')
  localStorage.removeItem('auth_role')
  localStorage.removeItem('auth_nickname')
}

export function getToken() {
  return localStorage.getItem('auth_token')
}

export function isLoggedIn() {
  return !!getToken()
}
