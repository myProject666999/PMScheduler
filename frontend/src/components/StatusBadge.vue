<script setup lang="ts">
defineProps<{ status: string }>()

const statusMap: Record<string, { label: string; class: string }> = {
  PENDING: { label: '待派工', class: 'bg-gray-100 text-gray-700' },
  DISPATCHED: { label: '已派工', class: 'bg-blue-100 text-blue-700' },
  EXECUTING: { label: '执行中', class: 'bg-indigo-100 text-indigo-700' },
  REVIEWING: { label: '待审核', class: 'bg-orange-100 text-orange-700' },
  COMPLETED: { label: '已完成', class: 'bg-green-100 text-green-700' },
  REJECTED: { label: '已退回', class: 'bg-red-100 text-red-700' },
  NORMAL: { label: '正常', class: 'bg-green-100 text-green-700' },
  MAINTENANCE: { label: '保养中', class: 'bg-blue-100 text-blue-700' },
  STOPPED: { label: '停机', class: 'bg-gray-100 text-gray-700' },
  UPCOMING: { label: '即将到期', class: 'bg-yellow-100 text-yellow-700' },
  OVERDUE: { label: '逾期', class: 'bg-red-100 text-red-700' },
  CALENDAR: { label: '日历周期', class: 'bg-blue-100 text-blue-700' },
  RUNTIME: { label: '运行时数', class: 'bg-purple-100 text-purple-700' },
}
</script>

<template>
  <span :class="['px-2 py-0.5 rounded text-xs font-medium', statusMap[status]?.class || 'bg-gray-100 text-gray-700']">
    {{ statusMap[status]?.label || status }}
  </span>
</template>
