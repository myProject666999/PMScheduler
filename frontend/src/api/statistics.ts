import { get } from './index'

export function completionByPerson(year: number, month: number) {
  return get('/statistics/completion/person', { year, month })
}

export function completionByEquipment(year: number, month: number) {
  return get('/statistics/completion/equipment', { year, month })
}

export function monthlyTrend() {
  return get('/statistics/monthly-trend')
}
