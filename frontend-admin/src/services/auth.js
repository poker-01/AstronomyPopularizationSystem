import api from './api'

export async function login(username, password) {
  const res = await api.post('/api/auth/login', { username, password })
  return res.data
}

export function saveToken(token) {
  localStorage.setItem('admin_auth_token', token)
}

export function clearToken() {
  localStorage.removeItem('admin_auth_token')
}

export function getToken() {
  return localStorage.getItem('admin_auth_token')
}
