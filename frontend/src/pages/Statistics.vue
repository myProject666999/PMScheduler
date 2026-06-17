<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { completionByPerson, completionByEquipment, monthlyTrend } from '@/api/statistics'

const now = new Date()
const selectedYear = ref(now.getFullYear())
const selectedMonth = ref(now.getMonth() + 1)

const activeTab = ref<'person' | 'equipment' | 'trend'>('person')
const personData = ref<any[]>([])
const equipmentData = ref<any[]>([])
const trendData = ref<any[]>([])

const personChartRef = ref<HTMLDivElement>()
const equipmentChartRef = ref<HTMLDivElement>()
const trendChartRef = ref<HTMLDivElement>()

let personChart: echarts.ECharts | null = null
let equipmentChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

const months = Array.from({ length: 12 }, (_, i) => i + 1)
const years = Array.from({ length: 3 }, (_, i) => now.getFullYear() - i)

async function loadPersonData() {
  try {
    const res: any = await completionByPerson(selectedYear.value, selectedMonth.value)
    personData.value = res.data || []
  } catch { personData.value = [] }
}

async function loadEquipmentData() {
  try {
    const res: any = await completionByEquipment(selectedYear.value, selectedMonth.value)
    equipmentData.value = res.data || []
  } catch { equipmentData.value = [] }
}

async function loadTrendData() {
  try {
    const res: any = await monthlyTrend()
    trendData.value = res.data || []
  } catch { trendData.value = [] }
}

function renderPersonChart() {
  if (!personChartRef.value) return
  if (!personChart) personChart = echarts.init(personChartRef.value)
  const names = personData.value.map((d: any) => d.name)
  const rates = personData.value.map((d: any) => d.completionRate)
  const totals = personData.value.map((d: any) => d.total)
  const completed = personData.value.map((d: any) => d.completed)

  personChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        let str = params[0].name + '<br/>'
        params.forEach((p: any) => { str += `${p.marker} ${p.seriesName}: ${p.value}${p.seriesName === '完成率' ? '%' : ''}<br/>` })
        return str
      }
    },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    grid: { top: 20, right: 60, bottom: 40, left: 60 },
    xAxis: { type: 'category', data: names, axisLabel: { color: '#627d98' } },
    yAxis: [
      { type: 'value', name: '工单数', axisLabel: { color: '#627d98' }, splitLine: { lineStyle: { color: '#f0f4f8' } } },
      { type: 'value', name: '完成率', max: 100, axisLabel: { color: '#627d98', formatter: '{value}%' }, splitLine: { show: false } },
    ],
    series: [
      { name: '总工单', type: 'bar', data: totals, itemStyle: { color: '#bcccdc' }, barWidth: 20 },
      { name: '已完成', type: 'bar', data: completed, itemStyle: { color: '#1B3A5C' }, barWidth: 20 },
      { name: '完成率', type: 'line', yAxisIndex: 1, data: rates, smooth: true, lineStyle: { color: '#E8630A', width: 2 }, itemStyle: { color: '#E8630A' } },
    ],
  })
}

function renderEquipmentChart() {
  if (!equipmentChartRef.value) return
  if (!equipmentChart) equipmentChart = echarts.init(equipmentChartRef.value)
  const names = equipmentData.value.map((d: any) => d.name)
  const rates = equipmentData.value.map((d: any) => d.completionRate)
  const totals = equipmentData.value.map((d: any) => d.total)
  const completed = equipmentData.value.map((d: any) => d.completed)

  equipmentChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        let str = params[0].name + '<br/>'
        params.forEach((p: any) => { str += `${p.marker} ${p.seriesName}: ${p.value}${p.seriesName === '完成率' ? '%' : ''}<br/>` })
        return str
      }
    },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    grid: { top: 20, right: 60, bottom: 40, left: 60 },
    xAxis: { type: 'category', data: names, axisLabel: { color: '#627d98', rotate: names.length > 6 ? 30 : 0 } },
    yAxis: [
      { type: 'value', name: '工单数', axisLabel: { color: '#627d98' }, splitLine: { lineStyle: { color: '#f0f4f8' } } },
      { type: 'value', name: '完成率', max: 100, axisLabel: { color: '#627d98', formatter: '{value}%' }, splitLine: { show: false } },
    ],
    series: [
      { name: '总工单', type: 'bar', data: totals, itemStyle: { color: '#bcccdc' }, barWidth: 20 },
      { name: '已完成', type: 'bar', data: completed, itemStyle: { color: '#1B3A5C' }, barWidth: 20 },
      { name: '完成率', type: 'line', yAxisIndex: 1, data: rates, smooth: true, lineStyle: { color: '#E8630A', width: 2 }, itemStyle: { color: '#E8630A' } },
    ],
  })
}

function renderTrendChart() {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  const months = trendData.value.map((d: any) => d.month)
  const rates = trendData.value.map((d: any) => d.completionRate)
  const totals = trendData.value.map((d: any) => d.total)
  const completed = trendData.value.map((d: any) => d.completed)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    grid: { top: 20, right: 60, bottom: 40, left: 60 },
    xAxis: { type: 'category', data: months, axisLabel: { color: '#627d98' } },
    yAxis: [
      { type: 'value', name: '工单数', axisLabel: { color: '#627d98' }, splitLine: { lineStyle: { color: '#f0f4f8' } } },
      { type: 'value', name: '完成率', max: 100, axisLabel: { color: '#627d98', formatter: '{value}%' }, splitLine: { show: false } },
    ],
    series: [
      { name: '总工单', type: 'bar', data: totals, itemStyle: { color: '#bcccdc' }, barWidth: 16 },
      { name: '已完成', type: 'bar', data: completed, itemStyle: { color: '#1B3A5C' }, barWidth: 16 },
      {
        name: '完成率', type: 'line', yAxisIndex: 1, data: rates, smooth: true,
        lineStyle: { color: '#E8630A', width: 3 },
        itemStyle: { color: '#E8630A' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(232,99,10,0.2)' },
            { offset: 1, color: 'rgba(232,99,10,0.02)' },
          ]),
        },
      },
    ],
  })
}

async function loadAll() {
  await Promise.all([loadPersonData(), loadEquipmentData(), loadTrendData()])
  await nextTick()
  renderPersonChart()
  renderEquipmentChart()
  renderTrendChart()
}

watch([selectedYear, selectedMonth], async () => {
  await Promise.all([loadPersonData(), loadEquipmentData()])
  await nextTick()
  renderPersonChart()
  renderEquipmentChart()
})

onMounted(loadAll)
</script>

<template>
  <div class="space-y-6">
    <div class="bg-white rounded-lg shadow-sm p-4 flex items-center space-x-4">
      <label class="text-sm font-medium text-gray-700">统计月份</label>
      <select v-model="selectedYear" class="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500">
        <option v-for="y in years" :key="y" :value="y">{{ y }}年</option>
      </select>
      <select v-model="selectedMonth" class="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500">
        <option v-for="m in months" :key="m" :value="m">{{ m }}月</option>
      </select>
    </div>

    <div class="bg-white rounded-lg shadow-sm">
      <div class="border-b px-6 pt-4">
        <div class="flex space-x-6">
          <button
            :class="['pb-3 text-sm font-medium border-b-2 transition-colors', activeTab === 'person' ? 'text-industrial-900 border-industrial-900' : 'text-gray-500 border-transparent hover:text-gray-700']"
            @click="activeTab = 'person'"
          >按人完成率</button>
          <button
            :class="['pb-3 text-sm font-medium border-b-2 transition-colors', activeTab === 'equipment' ? 'text-industrial-900 border-industrial-900' : 'text-gray-500 border-transparent hover:text-gray-700']"
            @click="activeTab = 'equipment'"
          >按设备完成率</button>
          <button
            :class="['pb-3 text-sm font-medium border-b-2 transition-colors', activeTab === 'trend' ? 'text-industrial-900 border-industrial-900' : 'text-gray-500 border-transparent hover:text-gray-700']"
            @click="activeTab = 'trend'"
          >月度趋势</button>
        </div>
      </div>

      <div class="p-6">
        <div v-show="activeTab === 'person'" class="space-y-4">
          <div ref="personChartRef" class="h-80" />
          <table class="w-full text-sm">
            <thead class="bg-gray-50 border-b">
              <tr>
                <th class="text-left px-4 py-2 text-gray-600 font-medium">人员</th>
                <th class="text-left px-4 py-2 text-gray-600 font-medium">总工单</th>
                <th class="text-left px-4 py-2 text-gray-600 font-medium">已完成</th>
                <th class="text-left px-4 py-2 text-gray-600 font-medium">完成率</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="personData.length === 0"><td colspan="4" class="text-center py-6 text-gray-400">暂无数据</td></tr>
              <tr v-for="d in personData" :key="d.name" class="border-b hover:bg-gray-50">
                <td class="px-4 py-2 font-medium text-industrial-900">{{ d.name }}</td>
                <td class="px-4 py-2 text-gray-600">{{ d.total }}</td>
                <td class="px-4 py-2 text-gray-600">{{ d.completed }}</td>
                <td class="px-4 py-2">
                  <span :class="['font-medium', d.completionRate >= 80 ? 'text-green-600' : d.completionRate >= 50 ? 'text-yellow-600' : 'text-red-600']">
                    {{ d.completionRate }}%
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-show="activeTab === 'equipment'" class="space-y-4">
          <div ref="equipmentChartRef" class="h-80" />
          <table class="w-full text-sm">
            <thead class="bg-gray-50 border-b">
              <tr>
                <th class="text-left px-4 py-2 text-gray-600 font-medium">设备</th>
                <th class="text-left px-4 py-2 text-gray-600 font-medium">总工单</th>
                <th class="text-left px-4 py-2 text-gray-600 font-medium">已完成</th>
                <th class="text-left px-4 py-2 text-gray-600 font-medium">完成率</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="equipmentData.length === 0"><td colspan="4" class="text-center py-6 text-gray-400">暂无数据</td></tr>
              <tr v-for="d in equipmentData" :key="d.name" class="border-b hover:bg-gray-50">
                <td class="px-4 py-2 font-medium text-industrial-900">{{ d.name }}</td>
                <td class="px-4 py-2 text-gray-600">{{ d.total }}</td>
                <td class="px-4 py-2 text-gray-600">{{ d.completed }}</td>
                <td class="px-4 py-2">
                  <span :class="['font-medium', d.completionRate >= 80 ? 'text-green-600' : d.completionRate >= 50 ? 'text-yellow-600' : 'text-red-600']">
                    {{ d.completionRate }}%
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-show="activeTab === 'trend'">
          <div ref="trendChartRef" class="h-80" />
        </div>
      </div>
    </div>
  </div>
</template>
