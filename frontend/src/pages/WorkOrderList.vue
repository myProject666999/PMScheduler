<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getWorkOrderList } from '@/api/workorder'
import StatusBadge from '@/components/StatusBadge.vue'
import { Search } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

const query = ref({
  status: (route.query.status as string) || '',
  keyword: '',
  startDate: '',
  endDate: '',
  pageNum: 1,
  pageSize: 10,
})

async function loadList() {
  loading.value = true
  try {
    const res: any = await getWorkOrderList(query.value)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.value.pageNum = 1
  loadList()
}

function handlePageChange(page: number) {
  query.value.pageNum = page
  loadList()
}

function viewDetail(id: number) {
  router.push(`/workorder/${id}`)
}

onMounted(loadList)
</script>

<template>
  <div class="space-y-4">
    <div class="bg-white rounded-lg shadow-sm p-4">
      <div class="flex items-center space-x-4 flex-wrap gap-y-2">
        <select
          v-model="query.status"
          class="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500"
          @change="handleSearch"
        >
          <option value="">全部状态</option>
          <option value="PENDING">待派工</option>
          <option value="DISPATCHED">已派工</option>
          <option value="EXECUTING">执行中</option>
          <option value="REVIEWING">待审核</option>
          <option value="COMPLETED">已完成</option>
          <option value="REJECTED">已退回</option>
        </select>
        <div class="flex items-center flex-1 min-w-[200px] border border-gray-300 rounded-lg overflow-hidden">
          <Search class="w-4 h-4 text-gray-400 ml-3" />
          <input
            v-model="query.keyword"
            placeholder="搜索设备名称"
            class="flex-1 px-3 py-2 text-sm outline-none"
            @keyup.enter="handleSearch"
          />
        </div>
        <input
          v-model="query.startDate"
          type="date"
          class="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500"
        />
        <span class="text-gray-400">至</span>
        <input
          v-model="query.endDate"
          type="date"
          class="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500"
        />
        <button @click="handleSearch" class="px-4 py-2 bg-industrial-900 text-white text-sm rounded-lg hover:bg-industrial-800 transition-colors">查询</button>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b">
          <tr>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">工单编号</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">设备名称</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">保养项目</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">状态</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">执行人</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">计划日期</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center py-8 text-gray-400">加载中...</td>
          </tr>
          <tr v-else-if="list.length === 0">
            <td colspan="7" class="text-center py-8 text-gray-400">暂无数据</td>
          </tr>
          <tr v-for="item in list" :key="item.id" class="border-b hover:bg-gray-50 transition-colors">
            <td class="px-4 py-3 font-mono text-industrial-700">{{ item.orderNo }}</td>
            <td class="px-4 py-3 text-industrial-900 font-medium">{{ item.equipmentName }}</td>
            <td class="px-4 py-3 text-gray-600">{{ item.standardItemName || '-' }}</td>
            <td class="px-4 py-3"><StatusBadge :status="item.status" /></td>
            <td class="px-4 py-3 text-gray-600">{{ item.executeUserName || '-' }}</td>
            <td class="px-4 py-3 text-gray-600">{{ item.planDate || '-' }}</td>
            <td class="px-4 py-3">
              <button @click="viewDetail(item.id)" class="text-industrial-600 hover:text-industrial-900 text-sm font-medium transition-colors">查看</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="total > query.pageSize" class="flex items-center justify-between px-4 py-3 border-t bg-gray-50">
        <span class="text-sm text-gray-500">共 {{ total }} 条</span>
        <div class="flex items-center space-x-1">
          <button
            v-for="p in Math.ceil(total / query.pageSize)"
            :key="p"
            :class="[
              'px-3 py-1 rounded text-sm',
              p === query.pageNum ? 'bg-industrial-900 text-white' : 'text-gray-600 hover:bg-gray-200'
            ]"
            @click="handlePageChange(p)"
          >
            {{ p }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
