<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import BaseButton from './ui/BaseButton.vue'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <header class="border-b border-white/5 bg-surface-900">
    <div class="mx-auto flex h-14 max-w-6xl items-center justify-between px-4">
      <RouterLink to="/" class="text-lg font-semibold text-brand-500">Linh Vé Các</RouterLink>

      <nav class="flex items-center gap-1">
        <RouterLink
          to="/"
          class="rounded-lg px-3 py-2 text-sm text-ink-300 transition-colors duration-150 hover:text-ink-100"
          active-class="text-ink-100"
          exact-active-class="text-ink-100"
        >
          Phim
        </RouterLink>
        <RouterLink
          to="/the-loai"
          class="rounded-lg px-3 py-2 text-sm text-ink-300 transition-colors duration-150 hover:text-ink-100"
          active-class="text-ink-100"
        >
          Thể loại
        </RouterLink>
        <RouterLink
          to="/rap"
          class="rounded-lg px-3 py-2 text-sm text-ink-300 transition-colors duration-150 hover:text-ink-100"
          active-class="text-ink-100"
        >
          Rạp
        </RouterLink>
      </nav>

      <nav class="flex items-center gap-2">
        <template v-if="auth.isAuthenticated">
          <RouterLink
            v-if="auth.isAdmin"
            to="/admin"
            class="rounded-lg px-3 py-2 text-sm text-brand-500 transition-colors duration-150 hover:text-brand-400"
          >
            Quản trị
          </RouterLink>
          <RouterLink
            to="/tai-khoan"
            class="rounded-lg px-3 py-2 text-sm text-ink-300 transition-colors duration-150 hover:text-ink-100"
          >
            {{ auth.user?.fullName ?? 'Tài khoản' }}
          </RouterLink>
          <BaseButton variant="ghost" @click="logout">Đăng xuất</BaseButton>
        </template>
        <template v-else>
          <RouterLink
            to="/dang-nhap"
            class="rounded-lg px-3 py-2 text-sm text-ink-300 transition-colors duration-150 hover:text-ink-100"
          >
            Đăng nhập
          </RouterLink>
          <RouterLink to="/dang-ky">
            <BaseButton variant="secondary">Đăng ký</BaseButton>
          </RouterLink>
        </template>
      </nav>
    </div>
  </header>
</template>
