<script setup>
// Thẻ poster phim tái sử dụng (trang chủ, danh sách). Poster null → nền thay thế.
import BaseBadge from './ui/BaseBadge.vue'
import { formatDate } from '../utils/format'

const props = defineProps({
  movie: { type: Object, required: true },
  // 'now' hiện nút Đặt vé; 'coming' hiện ngày khởi chiếu
  mode: { type: String, default: 'now' },
})
</script>

<template>
  <RouterLink
    :to="{ name: 'movie-detail', params: { id: movie.id } }"
    class="group block overflow-hidden rounded-lg border border-white/5 bg-surface-800 transition-colors duration-150 hover:border-brand-500/40"
  >
    <div class="relative aspect-[2/3] overflow-hidden bg-surface-900">
      <img
        v-if="movie.posterUrl"
        :src="movie.posterUrl"
        :alt="movie.title"
        class="h-full w-full object-cover"
      />
      <div v-else class="flex h-full w-full items-center justify-center p-4 text-center">
        <span class="text-lg text-ink-500">{{ movie.title }}</span>
      </div>
      <span
        v-if="movie.ageRating"
        class="absolute left-2 top-2 rounded-md bg-black/70 px-1.5 py-0.5 text-sm font-semibold text-brand-500"
      >
        {{ movie.ageRating }}
      </span>
    </div>

    <div class="space-y-1 p-3">
      <h3 class="line-clamp-2 text-lg font-medium text-ink-100">{{ movie.title }}</h3>
      <p class="text-sm text-ink-500">
        {{ movie.genres?.map((g) => g.name).join(', ') || 'Đang cập nhật' }} · {{ movie.durationMin }} phút
      </p>
      <div class="pt-1">
        <BaseBadge v-if="mode === 'coming'" variant="info">
          Khởi chiếu {{ formatDate(movie.releaseDate) || 'sắp tới' }}
        </BaseBadge>
        <span
          v-else
          class="inline-block text-sm font-medium text-brand-500 opacity-0 transition-opacity duration-150 group-hover:opacity-100"
        >
          Đặt vé →
        </span>
      </div>
    </div>
  </RouterLink>
</template>
