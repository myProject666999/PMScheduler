<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEquipmentDetail, registerRuntime } from '@/api/equipment'
import { getStandards, createStandard, updateStandard, deleteStandard } from '@/api/standard'
import { getWorkOrderList } from '@/api/workorder'
import StatusBadge from '@/components/StatusBadge.vue'
import { ArrowLeft, Plus, Pencil, Trash2, X } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)

const equipment = ref<any>({})
const standards = ref<any[]>([])
const workOrders = ref<any[]>([])
const recentRuntimes = ref<any[]>([])

const runtimeForm = ref({ hours: 0 })
const runtimeLoading = ref(false)

const showStdDialog = ref(false)
const stdForm = ref<any>({ id: 0, itemName: '', triggerType: 'CALENDAR', cycleDays: 30, cycleRuntime: 0, description: '' })
const stdEditing = ref(false)

async function loadDetail() {
  try {
    const res: any = await getEquipmentDetail(id)
    equipment.value = res.data || {}
    recentRuntimes.value = res.data?.recentRuntimes || []
  } catch {}
}

async function loadStandards() {
  try {
    const res: any = await getStandards(id)
    standards.value = res.data || []
  } catch {
    standards.value = []
  }
}

async function loadWorkOrders() {
  try {
    const res: any = await getWorkOrderList({ equipmentId: id, page: 1, size: 10 })
    workOrders.value = res.data?.records || []
  } catch {
    workOrders.value = []
  }
}

async function handleRegisterRuntime() {
  if (runtimeForm.value.hours <= 0) return
  runtimeLoading.value = true
  try {
    await registerRuntime(id, { hours: runtimeForm.value.hours })
    runtimeForm.value.hours = 0
    await loadDetail()
  } catch {} finally {
    runtimeLoading.value = false
  }
}

function openStdDialog(item?: any) {
  if (item) {
    stdEditing.value = true
    stdForm.value = { ...item }
  } else {
    stdEditing.value = false
    stdForm.value = { id: 0, itemName: '', triggerType: 'CALENDAR', cycleDays: 30, cycleRuntime: 0, description: '' }
  }
  showStdDialog.value = true
}

async function handleStdSubmit() {
  try {
    const payload = { ...stdForm.value, equipmentId: id }
    if (stdEditing.value) {
      await updateStandard(payload.id, payload)
    } else {
      await createStandard(payload)
    }
    showStdDialog.value = false
    loadStandards()
  } catch {}
}

async function handleDeleteStd(stdId: number) {
  if (!confirm('确认删除该保养标准？')) return
  try {
    await deleteStandard(stdId)
    loadStandards()
  } catch {}
}

onMounted(() => {
  loadDetail()
  loadStandards()
  loadWorkOrders()
})
</script>

<template>
  <div class="space-y-6">
    <button @click="router.push('/equipment')" class="flex items-center text-sm text-industrial-600 hover:text-industrial-900 transition-colors">
      <ArrowLeft class="w-4 h-4 mr-1" />
      返回设备列表
    </button>

    <div class="bg-white rounded-lg shadow-sm p-6">
      <h2 class="text-lg font-semibold text-industrial-900 mb-4">设备基本信息</h2>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div>
          <div class="text-sm text-gray-500">设备编码</div>
          <div class="font-medium text-industrial-900">{{ equipment.code }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">设备名称</div>
          <div class="font-medium text-industrial-900">{{ equipment.name }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">设备型号</div>
          <div class="font-medium text-industrial-900">{{ equipment.model }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">所属产线</div>
          <div class="font-medium text-industrial-900">{{ equipment.productionLine }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">运行时数</div>
          <div class="font-medium text-industrial-900">{{ equipment.currentRuntime ?? 0 }} h</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">保养状态</div>
          <StatusBadge :status="equipment.maintenanceStatus || 'NORMAL'" />
        </div>
        <div>
          <div class="text-sm text-gray-500">上次保养</div>
          <div class="font-medium text-industrial-900">{{ equipment.lastMaintenanceDate || '-' }}</div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-industrial-900">保养标准</h2>
        <button @click="openStdDialog()" class="flex items-center px-3 py-1.5 bg-safety hover:bg-safety-dark text-white rounded-lg text-sm transition-colors">
          <Plus class="w-4 h-4 mr-1" />
          新增
        </button>
      </div>
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b">
          <tr>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">保养项目</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">触发类型</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">周期(天)</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">运行时数</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">说明</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="standards.length === 0">
            <td colspan="6" class="text-center py-6 text-gray-400">暂无保养标准</td>
          </tr>
          <tr v-for="s in standards" :key="s.id" class="border-b hover:bg-gray-50">
            <td class="px-4 py-2 font-medium text-industrial-900">{{ s.itemName }}</td>
            <td class="px-4 py-2"><StatusBadge :status="s.triggerType" /></td>
            <td class="px-4 py-2 text-gray-600">{{ s.triggerType === 'CALENDAR' ? s.cycleDays + ' 天' : '-' }}</td>
            <td class="px-4 py-2 text-gray-600">{{ s.triggerType === 'RUNTIME' ? s.cycleRuntime + ' h' : '-' }}</td>
            <td class="px-4 py-2 text-gray-600">{{ s.description || '-' }}</td>
            <td class="px-4 py-2">
              <div class="flex items-center space-x-2">
                <button @click="openStdDialog(s)" class="text-industrial-600 hover:text-industrial-900"><Pencil class="w-4 h-4" /></button>
                <button @click="handleDeleteStd(s.id)" class="text-red-400 hover:text-red-600"><Trash2 class="w-4 h-4" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="bg-white rounded-lg shadow-sm p-6">
      <h2 class="text-lg font-semibold text-industrial-900 mb-4">运行时数登记</h2>
      <div class="flex items-center space-x-3 mb-4">
        <input
          v-model.number="runtimeForm.hours"
          type="number"
          min="1"
          placeholder="输入运行时数"
          class="w-48 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500"
        />
        <button
          @click="handleRegisterRuntime"
          :disabled="runtimeLoading"
          class="px-4 py-2 bg-industrial-900 hover:bg-industrial-800 text-white text-sm rounded-lg transition-colors disabled:opacity-50"
        >
          {{ runtimeLoading ? '登记中...' : '登记' }}
        </button>
      </div>
      <div v-if="recentRuntimes.length > 0">
        <h3 class="text-sm text-gray-500 mb-2">最近登记记录</h3>
        <div class="space-y-2">
          <div v-for="r in recentRuntimes" :key="r.id" class="flex items-center justify-between text-sm border-b border-gray-100 pb-2">
            <span class="text-gray-600">{{ r.registerDate }}</span>
            <span class="font-medium text-industrial-900">+{{ r.hours }} h</span>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow-sm p-6">
      <h2 class="text-lg font-semibold text-industrial-900 mb-4">历史保养工单</h2>
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b">
          <tr>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">工单编号</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">保养项目</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">状态</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">执行人</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">计划日期</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="workOrders.length === 0">
            <td colspan="5" class="text-center py-6 text-gray-400">暂无工单</td>
          </tr>
          <tr v-for="wo in workOrders" :key="wo.id" class="border-b hover:bg-gray-50 cursor-pointer" @click="router.push(`/workorder/${wo.id}`)">
            <td class="px-4 py-2 font-mono text-industrial-700">{{ wo.orderNo }}</td>
            <td class="px-4 py-2 text-gray-700">{{ wo.itemName }}</td>
            <td class="px-4 py-2"><StatusBadge :status="wo.status" /></td>
            <td class="px-4 py-2 text-gray-600">{{ wo.executorName || '-' }}</td>
            <td class="px-4 py-2 text-gray-600">{{ wo.plannedDate || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showStdDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showStdDialog = false">
      <div class="bg-white rounded-xl w-full max-w-lg p-6 shadow-2xl">
        <div class="flex items-center justify-between mb-5">
          <h3 class="text-lg font-semibold text-industrial-900">{{ stdEditing ? '编辑' : '新增' }}保养标准</h3>
          <button @click="showStdDialog = false" class="text-gray-400 hover:text-gray-600"><X class="w-5 h-5" /></button>
        </div>
        <form @submit.prevent="handleStdSubmit" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">保养项目</label>
            <input v-model="stdForm.itemName" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">触发类型</label>
            <select v-model="stdForm.triggerType" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500">
              <option value="CALENDAR">日历周期</option>
              <option value="RUNTIME">运行时数</option>
            </select>
          </div>
          <div v-if="stdForm.triggerType === 'CALENDAR'">
            <label class="block text-sm font-medium text-gray-700 mb-1">周期天数</label>
            <input v-model.number="stdForm.cycleDays" type="number" min="1" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
          </div>
          <div v-if="stdForm.triggerType === 'RUNTIME'">
            <label class="block text-sm font-medium text-gray-700 mb-1">运行时数阈值</label>
            <input v-model.number="stdForm.cycleRuntime" type="number" min="1" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">说明</label>
            <textarea v-model="stdForm.description" rows="2" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
          </div>
          <div class="flex justify-end space-x-3 pt-2">
            <button type="button" @click="showStdDialog = false" class="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50">取消</button>
            <button type="submit" class="px-4 py-2 text-sm bg-industrial-900 text-white rounded-lg hover:bg-industrial-800">确定</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
