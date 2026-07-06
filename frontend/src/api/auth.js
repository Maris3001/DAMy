import http from './http'

export function register(payload) {
  return http.post('/auth/register', payload).then((res) => res.data)
}

export function login(payload) {
  return http.post('/auth/login', payload).then((res) => res.data)
}

export function getMe() {
  return http.get('/users/me').then((res) => res.data)
}

export function updateMe(payload) {
  return http.put('/users/me', payload).then((res) => res.data)
}
