import api from './api'

const NASA_TIMEOUT_MS = 20000

function unwrap(res) {
  const body = res.data
  if (body && typeof body.code === 'number') {
    if (body.code !== 0) throw new Error(body.message || '请求失败')
    return body.data
  }
  return body
}

export async function fetchApod() {
  return unwrap(await api.get('/api/nasa/apod', { timeout: NASA_TIMEOUT_MS }))
}

export async function fetchPlanets() {
  return unwrap(await api.get('/api/nasa/planets', { timeout: NASA_TIMEOUT_MS }))
}

export async function fetchPlanet(id) {
  return unwrap(await api.get(`/api/nasa/planets/${id}`, { timeout: NASA_TIMEOUT_MS }))
}
