import api from './api'

export async function register(username, password) {
  const res = await api.post('/api/auth/register', { username, password })
  return res.data
}

export async function login(username, password) {
  const res = await api.post('/api/auth/login', { username, password })
  return res.data
}

export function saveToken(token) {
  localStorage.setItem('auth_token', token)
}

export function clearToken() {
  localStorage.removeItem('auth_token')
}

export function getToken() {
  return localStorage.getItem('auth_token')
}
