import { get, post, put, del } from './index'

export function getEquipmentList(params: any) {
  return get('/equipment/page', params)
}

export function getEquipmentDetail(id: number) {
  return get(`/equipment/${id}`)
}

export function createEquipment(data: any) {
  return post('/equipment', data)
}

export function updateEquipment(id: number, data: any) {
  return put(`/equipment/${id}`, data)
}

export function deleteEquipment(id: number) {
  return del(`/equipment/${id}`)
}

export function registerRuntime(id: number, data: any) {
  return post(`/equipment/${id}/runtime`, data)
}
