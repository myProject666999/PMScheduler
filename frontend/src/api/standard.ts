import { get, post, put, del } from './index'

export function getStandards(equipmentId: number) {
  return get('/maintenance-standard', { equipmentId })
}

export function createStandard(data: any) {
  return post('/maintenance-standard', data)
}

export function updateStandard(id: number, data: any) {
  return put(`/maintenance-standard/${id}`, data)
}

export function deleteStandard(id: number) {
  return del(`/maintenance-standard/${id}`)
}
