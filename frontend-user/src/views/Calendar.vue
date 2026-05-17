<template>
  <div class="page-container calendar-page">
    <header class="page-header">
      <h1 class="section-title">天文日历</h1>
      <p class="section-subtitle">
        流星雨、食相、行星合等天象一览；登录后可订阅并在「我的提醒」中查看即将发生的事件。
      </p>
    </header>

    <div class="tabs">
      <button :class="{ active: tab === 'calendar' }" @click="tab = 'calendar'">月历</button>
      <button :class="{ active: tab === 'reminders' }" @click="switchReminders">我的提醒</button>
    </div>

    <div v-if="tab === 'calendar'" v-loading="loading" class="calendar-layout">
      <section class="calendar-panel glass-card">
        <div class="month-nav">
          <button class="nav-btn" @click="prevMonth">‹</button>
          <span class="month-label">{{ year }} 年 {{ month }} 月</span>
          <button class="nav-btn" @click="nextMonth">›</button>
          <button class="nav-btn today-btn" @click="goToday">今天</button>
        </div>
        <div class="weekday-row">
          <span v-for="w in weekdays" :key="w">{{ w }}</span>
        </div>
        <div class="days-grid">
          <button
            v-for="cell in calendarCells"
            :key="cell.key"
            class="day-cell"
            :class="{
              muted: !cell.inMonth,
              today: cell.isToday,
              selected: selectedDate === cell.dateStr,
              'has-event': eventDays.has(cell.dateStr)
            }"
            :disabled="!cell.inMonth"
            @click="selectDay(cell)"
          >
            <span class="day-num">{{ cell.day }}</span>
            <span v-if="eventDays.has(cell.dateStr)" class="event-dot"></span>
          </button>
        </div>
      </section>

      <section class="events-panel glass-card">
        <h2 class="panel-title">
          {{ selectedDate ? formatDateLabel(selectedDate) : `${month} 月全部` }}
        </h2>
        <article v-for="ev in filteredEvents" :key="ev.id" class="event-card">
          <div class="event-meta">
            <span class="type-tag">{{ ev.eventTypeLabel }}</span>
            <time>{{ formatRange(ev.startTime, ev.endTime) }}</time>
          </div>
          <h3>{{ ev.title }}</h3>
          <p>{{ ev.description }}</p>
          <div class="event-actions">
            <el-button
              v-if="loggedIn"
              :type="ev.subscribed ? 'default' : 'primary'"
              size="small"
              :loading="actionId === ev.id"
              @click="toggleSubscribe(ev)"
            >
              {{ ev.subscribed ? '取消订阅' : '订阅提醒' }}
            </el-button>
            <router-link v-else to="/login" class="login-hint">登录后订阅</router-link>
          </div>
        </article>
        <p v-if="!loading && filteredEvents.length === 0" class="empty">该时段暂无天象事件</p>
      </section>
    </div>

    <div v-else v-loading="remindersLoading" class="reminders-panel">
      <article v-for="r in reminders" :key="r.subscriptionId" class="reminder-card glass-card">
        <span class="type-tag">{{ r.eventTypeLabel }}</span>
        <h3>{{ r.title }}</h3>
        <p>{{ r.description }}</p>
        <p class="countdown">
          <template v-if="r.minutesUntilStart > 0">
            约 {{ formatCountdown(r.minutesUntilStart) }} 后开始
          </template>
          <template v-else>进行中</template>
        </p>
        <time>{{ formatRange(r.startTime, r.endTime) }}</time>
      </article>
      <p v-if="!remindersLoading && reminders.length === 0" class="empty">
        暂无已订阅的 upcoming 事件，在月历中点击「订阅提醒」即可添加。
      </p>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getToken } from '../services/auth'
import {
  fetchMonthEvents,
  fetchMyReminders,
  subscribeEvent,
  unsubscribeEvent
} from '../services/calendar'

export default {
  name: 'CalendarView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const tab = ref('calendar')
    const loading = ref(true)
    const remindersLoading = ref(false)
    const events = ref([])
    const reminders = ref([])
    const actionId = ref(null)

    const formatDateStr = (d) =>
      `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

    const now = new Date()
    const year = ref(now.getFullYear())
    const month = ref(now.getMonth() + 1)
    const selectedDate = ref(formatDateStr(now))

    const weekdays = ['日', '一', '二', '三', '四', '五', '六']
    const loggedIn = computed(() => !!getToken())

    const eventDays = computed(() => {
      const set = new Set()
      for (const ev of events.value) {
        const start = new Date(ev.startTime)
        const end = new Date(ev.endTime)
        const cur = new Date(start)
        cur.setHours(0, 0, 0, 0)
        const endDay = new Date(end)
        endDay.setHours(0, 0, 0, 0)
        while (cur <= endDay) {
          set.add(formatDateStr(cur))
          cur.setDate(cur.getDate() + 1)
        }
      }
      return set
    })

    const calendarCells = computed(() => {
      const first = new Date(year.value, month.value - 1, 1)
      const startPad = first.getDay()
      const daysInMonth = new Date(year.value, month.value, 0).getDate()
      const cells = []
      const todayStr = formatDateStr(new Date())

      for (let i = 0; i < startPad; i++) {
        const d = new Date(year.value, month.value - 1, -startPad + i + 1)
        cells.push(makeCell(d, false, todayStr))
      }
      for (let d = 1; d <= daysInMonth; d++) {
        const date = new Date(year.value, month.value - 1, d)
        cells.push(makeCell(date, true, todayStr))
      }
      while (cells.length % 7 !== 0) {
        const last = cells[cells.length - 1]
        const d = new Date(last.year, last.month - 1, last.day + 1)
        cells.push(makeCell(d, false, todayStr))
      }
      return cells
    })

    const makeCell = (date, inMonth, todayStr) => ({
      key: `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`,
      day: date.getDate(),
      inMonth,
      isToday: formatDateStr(date) === todayStr,
      dateStr: formatDateStr(date),
      year: date.getFullYear(),
      month: date.getMonth() + 1
    })

    const filteredEvents = computed(() => {
      if (!selectedDate.value) return events.value
      return events.value.filter((ev) => {
        const start = formatDateStr(new Date(ev.startTime))
        const end = formatDateStr(new Date(ev.endTime))
        return selectedDate.value >= start && selectedDate.value <= end
      })
    })

    const loadMonth = async () => {
      loading.value = true
      try {
        events.value = await fetchMonthEvents(year.value, month.value)
      } catch (e) {
        ElMessage.error(e.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const loadReminders = async () => {
      remindersLoading.value = true
      try {
        reminders.value = await fetchMyReminders()
      } catch (e) {
        ElMessage.error(e.message || '加载失败')
      } finally {
        remindersLoading.value = false
      }
    }

    const prevMonth = () => {
      if (month.value === 1) {
        month.value = 12
        year.value--
      } else month.value--
    }

    const nextMonth = () => {
      if (month.value === 12) {
        month.value = 1
        year.value++
      } else month.value++
    }

    const goToday = () => {
      const t = new Date()
      year.value = t.getFullYear()
      month.value = t.getMonth() + 1
      selectedDate.value = formatDateStr(t)
    }

    const selectDay = (cell) => {
      if (!cell.inMonth) return
      selectedDate.value = cell.dateStr
    }

    const switchReminders = () => {
      tab.value = 'reminders'
      if (!loggedIn.value) {
        router.push({ name: 'Login', query: { redirect: '/calendar' } })
        return
      }
      loadReminders()
    }

    const toggleSubscribe = async (ev) => {
      actionId.value = ev.id
      try {
        if (ev.subscribed) {
          await unsubscribeEvent(ev.id)
          ev.subscribed = false
          ElMessage.success('已取消订阅')
        } else {
          await subscribeEvent(ev.id)
          ev.subscribed = true
          ElMessage.success('订阅成功')
        }
      } catch (e) {
        ElMessage.error(e.message || '操作失败')
      } finally {
        actionId.value = null
      }
    }

    const formatDateLabel = (str) => {
      const [y, m, d] = str.split('-')
      return `${y}年${Number(m)}月${Number(d)}日`
    }

    const formatRange = (start, end) => {
      const s = new Date(start)
      const e = new Date(end)
      const fmt = (dt) =>
        `${dt.getMonth() + 1}/${dt.getDate()} ${String(dt.getHours()).padStart(2, '0')}:${String(dt.getMinutes()).padStart(2, '0')}`
      return `${fmt(s)} — ${fmt(e)}`
    }

    const formatCountdown = (minutes) => {
      if (minutes < 60) return `${minutes} 分钟`
      if (minutes < 1440) return `${Math.floor(minutes / 60)} 小时`
      return `${Math.floor(minutes / 1440)} 天`
    }

    watch([year, month], loadMonth)

    onMounted(() => {
      loadMonth()
      if (route.name === 'CalendarReminders') {
        tab.value = 'reminders'
        if (loggedIn.value) loadReminders()
      }
    })

    return {
      tab,
      loading,
      remindersLoading,
      events,
      reminders,
      actionId,
      year,
      month,
      selectedDate,
      weekdays,
      loggedIn,
      eventDays,
      calendarCells,
      filteredEvents,
      prevMonth,
      nextMonth,
      goToday,
      selectDay,
      switchReminders,
      toggleSubscribe,
      formatDateLabel,
      formatRange,
      formatCountdown
    }
  }
}
</script>

<style scoped>
.calendar-page {
  max-width: 1100px;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.tabs button {
  background: transparent;
  border: 1px solid var(--glass-border);
  color: var(--text-muted);
  padding: 0.5rem 1.2rem;
  border-radius: 8px;
  cursor: pointer;
}

.tabs button.active {
  color: #fff;
  border-color: var(--accent-violet);
  background: rgba(139, 92, 246, 0.15);
}

.calendar-layout {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  gap: 1.5rem;
  align-items: start;
}

.month-nav {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.month-label {
  font-weight: 600;
  min-width: 120px;
  text-align: center;
}

.nav-btn {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--glass-border);
  color: #fff;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  cursor: pointer;
}

.today-btn {
  width: auto;
  padding: 0 0.75rem;
  margin-left: auto;
  font-size: 0.85rem;
}

.weekday-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  color: var(--text-muted);
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.day-cell {
  position: relative;
  aspect-ratio: 1;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #fff;
  cursor: pointer;
  font-size: 0.9rem;
}

.day-cell.muted {
  color: rgba(255, 255, 255, 0.25);
  cursor: default;
}

.day-cell.today {
  box-shadow: inset 0 0 0 2px var(--accent-cyan);
}

.day-cell.selected {
  background: rgba(139, 92, 246, 0.35);
}

.day-cell.has-event .event-dot {
  position: absolute;
  bottom: 6px;
  left: 50%;
  transform: translateX(-50%);
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--accent-cyan);
}

.panel-title {
  font-size: 1.1rem;
  margin: 0 0 1rem;
}

.event-card {
  padding: 1rem 0;
  border-bottom: 1px solid var(--glass-border);
}

.event-card:last-child {
  border-bottom: none;
}

.type-tag {
  display: inline-block;
  font-size: 0.75rem;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  background: rgba(6, 182, 212, 0.2);
  color: var(--accent-cyan);
  margin-right: 0.5rem;
}

.event-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.35rem;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.event-card h3 {
  margin: 0.25rem 0;
  font-size: 1.05rem;
}

.event-card p {
  color: var(--text-muted);
  font-size: 0.9rem;
  line-height: 1.5;
}

.event-actions {
  margin-top: 0.75rem;
}

.login-hint {
  color: var(--accent-cyan);
  font-size: 0.9rem;
}

.reminders-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.reminder-card {
  padding: 1.25rem;
}

.countdown {
  color: var(--accent-violet);
  font-weight: 600;
  margin: 0.5rem 0;
}

.empty {
  color: var(--text-muted);
  text-align: center;
  padding: 2rem;
}

@media (max-width: 900px) {
  .calendar-layout {
    grid-template-columns: 1fr;
  }
}
</style>
