import { defineStore } from 'pinia'
import * as authApi from '../api/auth'
import { tokenStorage } from '../api/http'

/**
 * Phiên đăng nhập: token JWT persist trong localStorage, user nạp lại
 * qua fetchMe() khi mở app (gọi ở router guard lần điều hướng đầu).
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: tokenStorage.get(),
    user: null,
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 'ADMIN',
  },

  actions: {
    setSession({ token, user }) {
      this.token = token
      this.user = user
      tokenStorage.set(token)
    },

    clearSession() {
      this.token = null
      this.user = null
      tokenStorage.clear()
    },

    async register(payload) {
      this.setSession(await authApi.register(payload))
    },

    async login(payload) {
      this.setSession(await authApi.login(payload))
    },

    /** Khôi phục thông tin user từ token đã lưu; token hỏng → xóa phiên. */
    async fetchMe() {
      try {
        this.user = await authApi.getMe()
      } catch (error) {
        this.clearSession()
        throw error
      }
    },

    async updateProfile(payload) {
      this.user = await authApi.updateMe(payload)
    },

    logout() {
      this.clearSession()
    },
  },
})
