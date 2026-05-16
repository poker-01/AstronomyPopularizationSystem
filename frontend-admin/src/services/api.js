import axios from 'axios'

const api = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080',
  timeout: 5000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_auth_token')
  if (token) config.headers['Authorization'] = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status
    const code = error.response?.data?.code
    if (status === 401 || code === 401) {
      localStorage.removeItem('admin_auth_token')
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api
