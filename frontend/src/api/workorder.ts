import { get, post } from './index'

export function getWorkOrderList(params: any) {
  return get('/work-order/page', params)
}

export function getWorkOrderDetail(id: number) {
  return get(`/work-order/${id}`)
}

export function dispatchWorkOrder(id: number, data: any) {
  return post(`/work-order/dispatch/${id}`, data)
}

export function executeWorkOrder(id: number, data: any) {
  return post(`/work-order/execute/${id}`, data)
}

export function reviewWorkOrder(id: number, data: any) {
  return post(`/work-order/review/${id}`, data)
}
