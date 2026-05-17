import api from './api'

function unwrap(res) {
  const body = res.data
  if (body && typeof body.code === 'number') {
    if (body.code !== 0) throw new Error(body.message || '请求失败')
    return body.data
  }
  return body
}

export async function fetchCalendarEvents() {
  return unwrap(await api.get('/api/admin/calendar/events'))
}

export async function createCalendarEvent(payload) {
  return unwrap(await api.post('/api/admin/calendar/events', payload))
}

export async function updateCalendarEvent(id, payload) {
  return unwrap(await api.put(`/api/admin/calendar/events/${id}`, payload))
}

export async function deleteCalendarEvent(id) {
  return unwrap(await api.delete(`/api/admin/calendar/events/${id}`))
}

export async function importCalendarEvents(year) {
  return unwrap(await api.post('/api/admin/calendar/import', null, { params: { year } }))
}
