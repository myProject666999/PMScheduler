<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useReminderStore } from '@/stores/reminder'
import { LayoutDashboard, Cog, ClipboardList, BarChart3, Bell, LogOut } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const reminderStore = useReminderStore()

const menuItems = [
  { name: '工作台', icon: LayoutDashboard, path: '/' },
  { name: '设备档案', icon: Cog, path: '/equipment' },
  { name: '保养工单', icon: ClipboardList, path: '/workorder' },
  { name: '统计报表', icon: BarChart3, path: '/statistics' },
]

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const overdueCount = computed(() => reminderStore.reminders.filter((r: any) => r.status === 'OVERDUE').length)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <aside class="w-[220px] bg-industrial-900 text-white flex flex-col flex-shrink-0">
      <div class="h-16 flex items-center px-5 border-b border-industrial-700">
        <Cog class="w-6 h-6 text-safety mr-2" />
        <span class="text-lg font-bold tracking-wide">保养排程台</span>
      </div>
      <nav class="flex-1 py-4 space-y-1">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          :class="[
            'flex items-center px-5 py-3 text-sm transition-colors',
            isActive(item.path)
              ? 'bg-industrial-700 text-white border-r-3 border-safety'
              : 'text-industrial-200 hover:bg-industrial-800 hover:text-white'
          ]"
        >
          <component :is="item.icon" class="w-5 h-5 mr-3" />
          {{ item.name }}
        </router-link>
      </nav>
      <div class="p-4 border-t border-industrial-700">
        <button
          @click="handleLogout"
          class="flex items-center w-full px-3 py-2 text-sm text-industrial-300 hover:text-white hover:bg-industrial-800 rounded transition-colors"
        >
          <LogOut class="w-4 h-4 mr-2" />
          退出登录
        </button>
      </div>
    </aside>

    <div class="flex-1 flex flex-col overflow-hidden">
      <header class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 flex-shrink-0">
        <div class="text-lg font-semibold text-industrial-900">{{ menuItems.find(i => isActive(i.path))?.name || '工作台' }}</div>
        <div class="flex items-center space-x-4">
          <button class="relative p-2 text-gray-500 hover:text-industrial-900 transition-colors">
            <Bell class="w-5 h-5" />
            <span
              v-if="overdueCount > 0"
              class="absolute -top-0.5 -right-0.5 w-4 h-4 bg-red-500 text-white text-[10px] rounded-full flex items-center justify-center"
            >
              {{ overdueCount }}
            </span>
          </button>
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-industrial-600 rounded-full flex items-center justify-center text-white text-sm font-medium">
              {{ userStore.userInfo?.username?.[0]?.toUpperCase() || 'U' }}
            </div>
            <span class="text-sm text-gray-700">{{ userStore.userInfo?.username || '用户' }}</span>
          </div>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto bg-gray-50 p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>
