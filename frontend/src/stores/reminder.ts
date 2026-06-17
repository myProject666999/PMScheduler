import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getReminders } from '@/api/dashboard'

export const useReminderStore = defineStore('reminder', () => {
  const reminders = ref<any[]>([])

  async function fetchReminders() {
    try {
      const res: any = await getReminders()
      reminders.value = res.data || []
    } catch {
      reminders.value = []
    }
  }

  return { reminders, fetchReminders }
})
