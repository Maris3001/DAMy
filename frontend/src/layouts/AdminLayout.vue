<script setup>
// Khung khu admin (P-10): sidebar tối cố định + header trắng; nội dung nền sáng gray-50.
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const menuOpen = ref(false)

const menu = [
  { name: 'admin-dashboard', label: 'Dashboard', icon: '📊' },
  { name: 'admin-movies', label: 'Phim', icon: '🎬' },
  { name: 'admin-genres', label: 'Thể loại', icon: '🏷️' },
  { name: 'admin-showtimes', label: 'Lịch chiếu', icon: '🕒' },
  { name: 'admin-rooms', label: 'Rạp & phòng', icon: '🏢' },
  { name: 'admin-concessions', label: 'Bắp nước', icon: '🍿' },
  { name: 'admin-campaigns', label: 'Voucher', icon: '🎫' },
  { name: 'admin-offers', label: 'Ưu đãi & Thành viên', icon: '🎁' },
]

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 text-gray-900 md:flex">
    <!-- Sidebar -->
    <aside
      class="bg-surface-900 text-ink-300 md:fixed md:inset-y-0 md:w-60 md:flex md:flex-col"
      :class="menuOpen ? 'block' : 'hidden md:block'"
    >
      <div class="flex h-14 items-center px-5">
        <RouterLink to="/" class="text-lg font-semibold text-brand-500">Linh Vé Các</RouterLink>
      </div>
      <nav class="space-y-1 px-3 py-2">
        <RouterLink
          v-for="item in menu"
          :key="item.name"
          :to="{ name: item.name }"
          class="flex items-center gap-3 rounded-lg border-l-2 border-transparent px-3 py-2 text-sm transition-colors duration-150 hover:bg-white/5"
          active-class="border-brand-500 bg-brand-500/10 text-ink-100"
          @click="menuOpen = false"
        >
          <span aria-hidden="true">{{ item.icon }}</span>
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <!-- Vùng nội dung -->
    <div class="flex min-h-screen flex-1 flex-col md:ml-60">
      <header class="flex h-14 items-center justify-between border-b border-gray-200 bg-white px-4">
        <button
          type="button"
          class="rounded-md p-2 text-gray-600 hover:bg-gray-100 md:hidden"
          aria-label="Menu"
          @click="menuOpen = !menuOpen"
        >
          ☰
        </button>
        <span class="hidden text-sm text-gray-500 md:block">Khu quản trị</span>
        <div class="flex items-center gap-3">
          <span class="text-sm text-gray-600">{{ auth.user?.fullName ?? 'Admin' }}</span>
          <button
            type="button"
            class="rounded-lg px-3 py-1.5 text-sm text-gray-600 transition-colors duration-150 hover:bg-gray-100"
            @click="logout"
          >
            Đăng xuất
          </button>
        </div>
      </header>

      <main class="flex-1 p-4 md:p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>
