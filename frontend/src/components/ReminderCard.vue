<script setup lang="ts">
import StatusBadge from './StatusBadge.vue'

defineProps<{ reminder: any }>()
</script>

<template>
  <div
    :class="[
      'bg-white rounded-lg p-4 shadow-sm border-l-4 transition-shadow hover:shadow-md',
      reminder.status === 'OVERDUE'
        ? 'border-l-red-500 animate-pulse'
        : reminder.status === 'UPCOMING'
          ? 'border-l-orange-400'
          : 'border-l-yellow-400'
    ]"
  >
    <div class="flex items-start justify-between">
      <div class="flex-1">
        <div class="flex items-center space-x-2 mb-1">
          <span class="font-semibold text-industrial-900">{{ reminder.equipmentName }}</span>
          <StatusBadge :status="reminder.triggerType || 'CALENDAR'" />
        </div>
        <div class="text-sm text-gray-600">{{ reminder.itemName }}</div>
      </div>
      <div :class="[
        'text-right text-sm font-medium',
        reminder.status === 'OVERDUE' ? 'text-red-600' : reminder.status === 'UPCOMING' ? 'text-orange-600' : 'text-yellow-600'
      ]">
        <div v-if="reminder.status === 'OVERDUE'">
          逾期 {{ reminder.overdueDays || 0 }} 天
        </div>
        <div v-else>
          剩余 {{ reminder.remainingDays || 0 }} 天
        </div>
      </div>
    </div>
  </div>
</template>
