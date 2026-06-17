<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = defineProps<{
  title: string
  value: string | number
  icon: any
  color: string
  to?: string
}>()

const router = useRouter()

function handleClick() {
  if (props.to) {
    router.push(props.to)
  }
}
</script>

<template>
  <div
    :class="[
      'bg-white rounded-lg p-5 shadow-sm border border-gray-100 flex items-center space-x-4 transition-shadow',
      to ? 'cursor-pointer hover:shadow-md' : ''
    ]"
    @click="handleClick"
  >
    <div :class="['w-12 h-12 rounded-lg flex items-center justify-center', color]">
      <component :is="icon" class="w-6 h-6 text-white" />
    </div>
    <div>
      <div class="text-2xl font-bold text-industrial-900">{{ value }}</div>
      <div class="text-sm text-gray-500 mt-0.5">{{ title }}</div>
    </div>
  </div>
</template>
