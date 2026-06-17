import { post, get } from './index'

export function login(data: { username: string; password: string }) {
  return post('/auth/login', data)
}

export function getCurrentUser() {
  return get('/auth/current')
}
