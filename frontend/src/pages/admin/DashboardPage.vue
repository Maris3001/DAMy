<script setup>
// Dashboard admin (P3 bản gọn): vài số đếm + lối tắt. Số liệu doanh thu đầy đủ ở P8.
import { onMounted, ref } from 'vue'
import {
  adminListMovies,
  adminListShowtimes,
  adminListCinemas,
  adminListConcessions,
} from '../../api/admin'

const stats = ref({ movies: 0, showtimesToday: 0, cinemas: 0, concessions: 0 })
const loading = ref(true)

const links = [
  { name: 'admin-movies', label: 'Quản lý phim', icon: '🎬' },
  { name: 'admin-showtimes', label: 'Xếp lịch chiếu', icon: '🕒' },
  { name: 'admin-rooms', label: 'Rạp & phòng', icon: '🏢' },
  { name: 'admin-concessions', label: 'Bắp nước', icon: '🍿' },
]

onMounted(async () => {
  try {
    const [movies, showtimes, cinemas, concessions] = await Promise.all([
      adminListMovies({ size: 1 }),
      adminListShowtimes({}),
      adminListCinemas(),
      adminListConcessions(),
    ])
    stats.value = {
      movies: movies.totalElements ?? 0,
      showtimesToday: showtimes.length ?? 0,
      cinemas: cinemas.length ?? 0,
      concessions: concessions.length ?? 0,
    }
  } finally {
    loading.value = false
  }
})

const cards = [
  { key: 'movies', label: 'Phim trong hệ thống' },
  { key: 'showtimesToday', label: 'Suất chiếu hôm nay' },
  { key: 'cinemas', label: 'Rạp' },
  { key: 'concessions', label: 'Món bắp nước' },
]
</script>

<template>
  <div>
    <h1 class="mb-1 text-xl font-semibold text-gray-900">Xin chào 👋</h1>
    <p class="mb-6 text-sm text-gray-500">Tổng quan nhanh hệ thống Linh Vé Các.</p>

    <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div
        v-for="c in cards"
        :key="c.key"
        class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm"
      >
        <p class="text-sm text-gray-500">{{ c.label }}</p>
        <p class="mt-1 text-3xl font-semibold tabular-nums text-gray-900">
          {{ loading ? '…' : stats[c.key] }}
        </p>
      </div>
    </div>

    <h2 class="mt-8 mb-3 text-sm font-semibold uppercase tracking-wide text-gray-500">Lối tắt</h2>
    <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
      <RouterLink
        v-for="l in links"
        :key="l.name"
        :to="{ name: l.name }"
        class="flex items-center gap-3 rounded-lg border border-gray-200 bg-white p-4 text-gray-800 shadow-sm transition-colors duration-150 hover:border-brand-600"
      >
        <span class="text-2xl" aria-hidden="true">{{ l.icon }}</span>
        <span class="text-sm font-medium">{{ l.label }}</span>
      </RouterLink>
    </div>
  </div>
</template>
