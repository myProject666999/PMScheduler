<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getWorkOrderDetail, dispatchWorkOrder, startExecuteWorkOrder, executeWorkOrder, reviewWorkOrder } from '@/api/workorder'
import StatusBadge from '@/components/StatusBadge.vue'
import { ArrowLeft, Plus, Trash2, CheckCircle, XCircle } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)

const order = ref<any>({})
const loading = ref(false)

const dispatchForm = ref({ executeUserId: '', planDate: '' })
const executeForm = ref({ actualHours: 0, maintenanceContent: '', remark: '', parts: [] as any[] })
const reviewRemark = ref('')

const showDispatch = computed(() => order.value.status === 'PENDING')
const showStartExecute = computed(() => order.value.status === 'DISPATCHED')
const showExecute = computed(() => order.value.status === 'EXECUTING')
const showReview = computed(() => order.value.status === 'REVIEWING')

function addPart() {
  executeForm.value.parts.push({ partName: '', quantity: 1, remark: '' })
}

function removePart(index: number) {
  executeForm.value.parts.splice(index, 1)
}

async function loadDetail() {
  loading.value = true
  try {
    const res: any = await getWorkOrderDetail(id)
    order.value = res.data || {}
  } catch {} finally {
    loading.value = false
  }
}

async function handleDispatch() {
  try {
    await dispatchWorkOrder(id, dispatchForm.value)
    loadDetail()
  } catch {}
}

async function handleStartExecute() {
  try {
    await startExecuteWorkOrder(id)
    loadDetail()
  } catch {}
}

async function handleExecute() {
  try {
    await executeWorkOrder(id, executeForm.value)
    loadDetail()
  } catch {}
}

async function handleReview(passed: boolean) {
  try {
    await reviewWorkOrder(id, { approved: passed, remark: reviewRemark.value })
    loadDetail()
  } catch {}
}

onMounted(loadDetail)
</script>

<template>
  <div class="space-y-6">
    <button @click="router.push('/workorder')" class="flex items-center text-sm text-industrial-600 hover:text-industrial-900 transition-colors">
      <ArrowLeft class="w-4 h-4 mr-1" />
      返回工单列表
    </button>

    <div class="bg-white rounded-lg shadow-sm p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-industrial-900">工单信息</h2>
        <StatusBadge :status="order.status" />
      </div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div>
          <div class="text-sm text-gray-500">工单编号</div>
          <div class="font-mono font-medium text-industrial-900">{{ order.orderNo }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">设备名称</div>
          <div class="font-medium text-industrial-900">{{ order.equipmentName }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">保养项目</div>
          <div class="font-medium text-industrial-900">{{ order.standardItemName }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">执行人</div>
          <div class="font-medium text-industrial-900">{{ order.executeUserName || '-' }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">计划日期</div>
          <div class="font-medium text-industrial-900">{{ order.planDate || '-' }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">触发类型</div>
          <StatusBadge :status="order.triggerType || 'CALENDAR'" />
        </div>
        <div>
          <div class="text-sm text-gray-500">实际工时</div>
          <div class="font-medium text-industrial-900">{{ order.actualHours ? order.actualHours + ' h' : '-' }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">完成时间</div>
          <div class="font-medium text-industrial-900">{{ order.completeAt || '-' }}</div>
        </div>
      </div>
      <div v-if="order.maintenanceContent" class="mt-4 pt-4 border-t">
        <div class="text-sm text-gray-500 mb-1">保养内容</div>
        <div class="text-gray-700 whitespace-pre-wrap">{{ order.maintenanceContent }}</div>
      </div>
    </div>

    <div v-if="showDispatch" class="bg-white rounded-lg shadow-sm p-6">
      <h2 class="text-lg font-semibold text-industrial-900 mb-4">派工操作</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">执行人</label>
          <input v-model="dispatchForm.executeUserId" placeholder="输入执行人ID" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">计划日期</label>
          <input v-model="dispatchForm.planDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
        </div>
      </div>
      <button @click="handleDispatch" class="px-6 py-2 bg-industrial-900 hover:bg-industrial-800 text-white text-sm rounded-lg transition-colors">确认派工</button>
    </div>

    <div v-if="showStartExecute" class="bg-white rounded-lg shadow-sm p-6">
      <h2 class="text-lg font-semibold text-industrial-900 mb-4">开始执行</h2>
      <p class="text-sm text-gray-500 mb-4">工单已派工，点击开始执行进入保养作业状态。</p>
      <button @click="handleStartExecute" class="px-6 py-2 bg-safety hover:bg-safety-dark text-white text-sm rounded-lg transition-colors">开始执行</button>
    </div>

    <div v-if="showExecute" class="bg-white rounded-lg shadow-sm p-6">
      <h2 class="text-lg font-semibold text-industrial-900 mb-4">执行回填</h2>
      <div class="space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">实际工时(h)</label>
            <input v-model.number="executeForm.actualHours" type="number" min="0" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">保养内容</label>
          <textarea v-model="executeForm.maintenanceContent" rows="3" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
        </div>

        <div>
          <div class="flex items-center justify-between mb-2">
            <label class="text-sm font-medium text-gray-700">零件更换明细</label>
            <button @click="addPart" class="flex items-center text-sm text-safety hover:text-safety-dark transition-colors">
              <Plus class="w-4 h-4 mr-1" />添加零件
            </button>
          </div>
          <div v-if="executeForm.parts.length > 0" class="space-y-2">
            <div v-for="(part, idx) in executeForm.parts" :key="idx" class="flex items-center space-x-2">
              <input v-model="part.partName" placeholder="零件名称" class="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
              <input v-model.number="part.quantity" type="number" min="1" placeholder="数量" class="w-20 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
              <input v-model="part.remark" placeholder="备注" class="w-32 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
              <button @click="removePart(idx)" class="text-red-400 hover:text-red-600"><Trash2 class="w-4 h-4" /></button>
            </div>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">备注</label>
          <textarea v-model="executeForm.remark" rows="2" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
        </div>

        <button @click="handleExecute" class="px-6 py-2 bg-industrial-900 hover:bg-industrial-800 text-white text-sm rounded-lg transition-colors">提交执行</button>
      </div>
    </div>

    <div v-if="showReview" class="bg-white rounded-lg shadow-sm p-6">
      <h2 class="text-lg font-semibold text-industrial-900 mb-4">审核操作</h2>
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-1">审核意见</label>
        <textarea v-model="reviewRemark" rows="2" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-industrial-500" />
      </div>
      <div class="flex space-x-3">
        <button @click="handleReview(true)" class="flex items-center px-5 py-2 bg-green-600 hover:bg-green-700 text-white text-sm rounded-lg transition-colors">
          <CheckCircle class="w-4 h-4 mr-1" />审核通过
        </button>
        <button @click="handleReview(false)" class="flex items-center px-5 py-2 bg-red-500 hover:bg-red-600 text-white text-sm rounded-lg transition-colors">
          <XCircle class="w-4 h-4 mr-1" />退回
        </button>
      </div>
    </div>

    <div v-if="order.parts && order.parts.length > 0" class="bg-white rounded-lg shadow-sm p-6">
      <h2 class="text-lg font-semibold text-industrial-900 mb-4">零件更换明细</h2>
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b">
          <tr>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">零件名称</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">数量</th>
            <th class="text-left px-4 py-2 text-gray-600 font-medium">备注</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(p, idx) in order.parts" :key="idx" class="border-b">
            <td class="px-4 py-2 text-industrial-900">{{ p.partName }}</td>
            <td class="px-4 py-2 text-gray-600">{{ p.quantity }}</td>
            <td class="px-4 py-2 text-gray-600">{{ p.remark || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
