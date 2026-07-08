<script setup>
// Footer khu công khai — cùng tông màu với AppHeader (khu admin có layout riêng, không dùng).
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const year = new Date().getFullYear()
const isAuthenticated = computed(() => auth.isAuthenticated)
</script>

<template>
  <footer class="border-t border-white/5 bg-surface-900">
    <div class="mx-auto grid max-w-6xl gap-8 px-4 py-10 sm:grid-cols-3">
      <div>
        <p class="text-lg font-semibold text-brand-500">Linh Vé Các</p>
        <p class="mt-2 text-sm text-ink-500">
          Đặt vé xem phim trực tuyến, tích điểm và nhận ưu đãi thành viên.
        </p>
      </div>

      <div>
        <p class="text-sm font-medium text-ink-100">Khám phá</p>
        <nav class="mt-3 flex flex-col gap-2 text-sm">
          <RouterLink to="/" class="text-ink-300 transition-colors duration-150 hover:text-ink-100">
            Phim
          </RouterLink>
          <RouterLink to="/the-loai" class="text-ink-300 transition-colors duration-150 hover:text-ink-100">
            Thể loại
          </RouterLink>
          <RouterLink to="/rap" class="text-ink-300 transition-colors duration-150 hover:text-ink-100">
            Rạp
          </RouterLink>
        </nav>
      </div>

      <div>
        <p class="text-sm font-medium text-ink-100">Tài khoản</p>
        <nav class="mt-3 flex flex-col gap-2 text-sm">
          <template v-if="isAuthenticated">
            <RouterLink
              to="/tai-khoan"
              class="text-ink-300 transition-colors duration-150 hover:text-ink-100"
            >
              Tài khoản của tôi
            </RouterLink>
          </template>
          <template v-else>
            <RouterLink
              to="/dang-nhap"
              class="text-ink-300 transition-colors duration-150 hover:text-ink-100"
            >
              Đăng nhập
            </RouterLink>
            <RouterLink
              to="/dang-ky"
              class="text-ink-300 transition-colors duration-150 hover:text-ink-100"
            >
              Đăng ký thành viên
            </RouterLink>
          </template>
        </nav>
      </div>
    </div>

    <div class="border-t border-white/5">
      <div class="mx-auto max-w-6xl px-4 py-4 text-sm text-ink-500">
        © {{ year }} Linh Vé Các — Đồ án tốt nghiệp.
      </div>
    </div>
  </footer>
</template>
