import { get, post } from './index'

export function getReminders() {
  return get('/dashboard/reminders')
}

export function getDashboardStats() {
  return get('/dashboard/stats')
}

export function refreshReminders() {
  return post('/dashboard/refresh-reminders')
}
