import api from './api'

function unwrap(res) {
  const body = res.data
  if (body && typeof body.code === 'number') {
    if (body.code !== 0) throw new Error(body.message || '请求失败')
    return body.data
  }
  return body
}

export async function fetchMonthEvents(year, month) {
  return unwrap(await api.get('/api/calendar/events', { params: { year, month } }))
}

export async function fetchEvent(id) {
  return unwrap(await api.get(`/api/calendar/events/${id}`))
}

export async function fetchUpcoming() {
  return unwrap(await api.get('/api/calendar/upcoming'))
}

export async function fetchMyReminders() {
  return unwrap(await api.get('/api/calendar/reminders/mine'))
}

export async function subscribeEvent(id, notifyChannel = 'IN_APP') {
  return unwrap(await api.post(`/api/calendar/events/${id}/subscribe`, { notifyChannel }))
}

export async function unsubscribeEvent(id) {
  return unwrap(await api.delete(`/api/calendar/events/${id}/subscribe`))
}
