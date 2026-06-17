<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getEquipmentList, createEquipment, deleteEquipment } from '@/api/equipment'
import StatusBadge from '@/components/StatusBadge.vue'
import { Search, Plus, Eye, Trash2, X } from 'lucide-vue-next'

const router = useRouter()

const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

const query = ref({
  keyword: '',
  productionLine: '',
  status: '',
  pageNum: 1,
  pageSize: 10,
})

const showDialog = ref(false)
const form = ref({
  code: '',
  name: '',
  model: '',
  productionLine: '',
})

async function loadList() {
  loading.value = true
  try {
    const res: any = await getEquipmentList(query.value)
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

function openDialog() {
  form.value = { code: '', name: '', model: '', productionLine: '' }
  showDialog.value = true
}

async function handleSubmit() {
  try {
    await createEquipment(form.value)
    showDialog.value = false
    loadList()
  } catch {}
}

async function handleDelete(id: number) {
  if (!confirm('确认删除该设备？')) return
  try {
    await deleteEquipment(id)
    loadList()
  } catch {}
}

function viewDetail(id: number) {
  router.push(`/equipment/${id}`)
}

onMounted(loadList)
</script>

<template>
  <div class="space-y-4">
    <div class="bg-white rounded-lg shadow-sm p-4">
      <div class="flex items-center space-x-4 flex-wrap gap-y-2">
        <div class="flex items-center flex-1 min-w-[200px] border border-gray-300 rounded-lg overflow-hidden">
          <Search class="w-4 h-4 text-gray-400 ml-3" />
          <input
            v-model="query.keyword"
            placeholder="搜索设备名称/编码"
            class="flex-1 px-3 py-2 text-sm outline-none"
            @keyup.enter="handleSearch"
          />
        </div>
        <select
          v-model="query.productionLine"
          class="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500"
          @change="handleSearch"
        >
          <option value="">全部产线</option>
          <option value="A线">A线</option>
          <option value="B线">B线</option>
          <option value="C线">C线</option>
        </select>
        <select
          v-model="query.status"
          class="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500"
          @change="handleSearch"
        >
          <option value="">全部状态</option>
          <option value="NORMAL">正常</option>
          <option value="UPCOMING">即将到期</option>
          <option value="OVERDUE">逾期</option>
          <option value="MAINTENANCE">保养中</option>
        </select>
        <button
          @click="openDialog"
          class="flex items-center px-4 py-2 bg-safety hover:bg-safety-dark text-white rounded-lg text-sm font-medium transition-colors"
        >
          <Plus class="w-4 h-4 mr-1" />
          新增设备
        </button>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b">
          <tr>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">设备编码</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">名称</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">型号</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">产线</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">运行时数</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">上次保养</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">状态</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="text-center py-8 text-gray-400">加载中...</td>
          </tr>
          <tr v-else-if="list.length === 0">
            <td colspan="8" class="text-center py-8 text-gray-400">暂无数据</td>
          </tr>
          <tr v-for="item in list" :key="item.id" class="border-b hover:bg-gray-50 transition-colors">
            <td class="px-4 py-3 text-industrial-700 font-mono">{{ item.code }}</td>
            <td class="px-4 py-3 text-industrial-900 font-medium">{{ item.name }}</td>
            <td class="px-4 py-3 text-gray-600">{{ item.model }}</td>
            <td class="px-4 py-3 text-gray-600">{{ item.productionLine }}</td>
            <td class="px-4 py-3 text-gray-600">{{ item.currentRuntime ?? '-' }} h</td>
            <td class="px-4 py-3 text-gray-600">{{ item.lastMaintenanceDate || '-' }}</td>
            <td class="px-4 py-3"><StatusBadge :status="item.maintenanceStatus || 'NORMAL'" /></td>
            <td class="px-4 py-3">
              <div class="flex items-center space-x-2">
                <button @click="viewDetail(item.id)" class="text-industrial-600 hover:text-industrial-900 transition-colors">
                  <Eye class="w-4 h-4" />
                </button>
                <button @click="handleDelete(item.id)" class="text-red-400 hover:text-red-600 transition-colors">
                  <Trash2 class="w-4 h-4" />
                </button>
              </div>
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

    <div v-if="showDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showDialog = false">
      <div class="bg-white rounded-xl w-full max-w-lg p-6 shadow-2xl">
        <div class="flex items-center justify-between mb-5">
          <h3 class="text-lg font-semibold text-industrial-900">新增设备</h3>
          <button @click="showDialog = false" class="text-gray-400 hover:text-gray-600">
            <X class="w-5 h-5" />
          </button>
        </div>
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">设备编码</label>
            <input v-model="form.code" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">设备名称</label>
            <input v-model="form.name" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">设备型号</label>
            <input v-model="form.model" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">所属产线</label>
            <input v-model="form.productionLine" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
          </div>
          <div class="flex justify-end space-x-3 pt-2">
            <button type="button" @click="showDialog = false" class="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50">取消</button>
            <button type="submit" class="px-4 py-2 text-sm bg-industrial-900 text-white rounded-lg hover:bg-industrial-800">确定</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
