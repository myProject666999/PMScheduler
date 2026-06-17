<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { useReminderStore } from '@/stores/reminder'
import { getDashboardStats, refreshReminders } from '@/api/dashboard'
import StatCard from '@/components/StatCard.vue'
import ReminderCard from '@/components/ReminderCard.vue'
import { ClipboardList, Wrench, AlertCircle, RefreshCw } from 'lucide-vue-next'

const router = useRouter()
const reminderStore = useReminderStore()

const stats = ref<any>({
  pendingOrders: 0,
  dispatchedOrders: 0,
  executingOrders: 0,
  reviewingOrders: 0,
  totalEquipment: 0,
  normalCount: 0,
  upcomingCount: 0,
  overdueCount: 0,
  maintenanceCount: 0,
  monthlyCompletionRate: 0,
})

const overdueReminders = computed(() => reminderStore.reminders.filter((r: any) => r.overdueDays > 0))
const upcomingReminders = computed(() => reminderStore.reminders.filter((r: any) => r.overdueDays === 0 || r.overdueDays == null))

const refreshing = ref(false)
const equipmentChartRef = ref<HTMLDivElement>()
const completionChartRef = ref<HTMLDivElement>()

async function loadStats() {
  try {
    const res: any = await getDashboardStats()
    stats.value = res.data || stats.value
  } catch {}
}

async function handleRefresh() {
  refreshing.value = true
  try {
    await refreshReminders()
    await reminderStore.fetchReminders()
    await loadStats()
  } finally {
    refreshing.value = false
  }
}

function initEquipmentChart() {
  if (!equipmentChartRef.value) return
  const chart = echarts.init(equipmentChartRef.value)
  const s = stats.value
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: {
      bottom: 0,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 12, color: '#627d98' },
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: {
        show: true,
        position: 'center',
        formatter: `{a|${s.totalEquipment}}\n{b|设备总数}`,
        rich: {
          a: { fontSize: 28, fontWeight: 'bold', color: '#1B3A5C', lineHeight: 36 },
          b: { fontSize: 12, color: '#627d98', lineHeight: 20 },
        },
      },
      data: [
        { value: s.normalCount, name: '正常', itemStyle: { color: '#10b981' } },
        { value: s.upcomingCount, name: '即将到期', itemStyle: { color: '#f59e0b' } },
        { value: s.overdueCount, name: '逾期', itemStyle: { color: '#ef4444' } },
        { value: s.maintenanceCount, name: '保养中', itemStyle: { color: '#3b82f6' } },
      ],
    }],
  })
  window.addEventListener('resize', () => chart.resize())
}

function initCompletionChart() {
  if (!completionChartRef.value) return
  const chart = echarts.init(completionChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 20, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月'],
      axisLine: { lineStyle: { color: '#d9e2ec' } },
      axisLabel: { color: '#627d98' },
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { color: '#627d98', formatter: '{value}%' },
      splitLine: { lineStyle: { color: '#f0f4f8' } },
    },
    series: [{
      type: 'line',
      data: [85, 92, 88, 95, 91, s.monthlyCompletionRate || 0],
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#1B3A5C', width: 3 },
      itemStyle: { color: '#1B3A5C' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(27,58,92,0.3)' },
          { offset: 1, color: 'rgba(27,58,92,0.02)' },
        ]),
      },
    }],
  })
  window.addEventListener('resize', () => chart.resize())
}

onMounted(async () => {
  await Promise.all([reminderStore.fetchReminders(), loadStats()])
  setTimeout(() => {
    initEquipmentChart()
    initCompletionChart()
  }, 100)
})
</script>

<template>
  <div class="space-y-6">
    <div class="bg-white rounded-lg p-6 shadow-sm">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-industrial-900">到期提醒看板</h2>
        <button
          @click="handleRefresh"
          :disabled="refreshing"
          class="flex items-center space-x-1 text-sm text-industrial-600 hover:text-industrial-900 transition-colors"
        >
          <RefreshCw :class="['w-4 h-4', refreshing ? 'animate-spin' : '']" />
          <span>刷新</span>
        </button>
      </div>

      <div v-if="overdueReminders.length > 0" class="mb-6">
        <h3 class="text-sm font-medium text-red-600 mb-3 flex items-center">
          <AlertCircle class="w-4 h-4 mr-1" />
          逾期提醒
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
          <ReminderCard v-for="r in overdueReminders" :key="r.id" :reminder="r" />
        </div>
      </div>

      <div v-if="upcomingReminders.length > 0">
        <h3 class="text-sm font-medium text-orange-600 mb-3">即将到期</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
          <ReminderCard v-for="r in upcomingReminders" :key="r.id" :reminder="r" />
        </div>
      </div>

      <div v-if="reminderStore.reminders.length === 0" class="text-center py-8 text-gray-400">
        暂无到期提醒
      </div>
    </div>

    <div>
      <h2 class="text-lg font-semibold text-industrial-900 mb-4">待办工单概览</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <StatCard title="待派工" :value="stats.pendingOrders" :icon="ClipboardList" color="bg-gray-500" to="/workorder?status=PENDING" />
        <StatCard title="执行中" :value="stats.executingOrders" :icon="Wrench" color="bg-blue-500" to="/workorder?status=EXECUTING" />
        <StatCard title="待审核" :value="stats.reviewingOrders" :icon="AlertCircle" color="bg-orange-500" to="/workorder?status=REVIEWING" />
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-lg p-6 shadow-sm">
        <h2 class="text-lg font-semibold text-industrial-900 mb-4">设备状态统计</h2>
        <div ref="equipmentChartRef" class="h-64" />
      </div>
      <div class="bg-white rounded-lg p-6 shadow-sm">
        <h2 class="text-lg font-semibold text-industrial-900 mb-4">本月保养完成率</h2>
        <div ref="completionChartRef" class="h-64" />
      </div>
    </div>
  </div>
</template>
