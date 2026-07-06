import axios from 'axios'

const TOKEN_KEY = 'lvc_token'

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

/** Client API tập trung — mọi module gọi backend qua instance này. */
const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// Gắn Bearer token cho mọi request khi đã đăng nhập
http.interceptors.request.use((config) => {
  const token = tokenStorage.get()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Token hết hạn/không hợp lệ → xóa phiên, đưa về trang đăng nhập kèm đường quay lại.
// Bỏ qua các request /auth/* (login sai mật khẩu cũng trả 401 — để form tự hiển thị lỗi).
http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error.response?.status
    const url = error.config?.url ?? ''
    if (status === 401 && !url.startsWith('/auth/')) {
      const { useAuthStore } = await import('../stores/auth')
      const { default: router } = await import('../router')
      useAuthStore().clearSession()
      router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
    }
    return Promise.reject(error)
  },
)

/** Lấy message tiếng Việt từ ErrorResponse của backend, có fallback. */
export function getApiMessage(error, fallback = 'Có lỗi xảy ra, vui lòng thử lại.') {
  return error.response?.data?.message ?? fallback
}

/** Lấy map lỗi theo field từ response 400 validation (hoặc object rỗng). */
export function getFieldErrors(error) {
  return error.response?.data?.errors ?? {}
}

export default http
