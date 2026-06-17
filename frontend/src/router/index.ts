import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Dashboard', component: () => import('@/pages/Dashboard.vue') },
  { path: '/equipment', name: 'EquipmentList', component: () => import('@/pages/EquipmentList.vue') },
  { path: '/equipment/:id', name: 'EquipmentDetail', component: () => import('@/pages/EquipmentDetail.vue') },
  { path: '/workorder', name: 'WorkOrderList', component: () => import('@/pages/WorkOrderList.vue') },
  { path: '/workorder/:id', name: 'WorkOrderDetail', component: () => import('@/pages/WorkOrderDetail.vue') },
  { path: '/statistics', name: 'Statistics', component: () => import('@/pages/Statistics.vue') },
  { path: '/login', name: 'Login', component: () => import('@/pages/Login.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.name !== 'Login' && !token) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && token) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
