<script setup>
// Trang chủ (P-01): hero phim nổi bật + "Phim đang chiếu" + "Sắp chiếu".
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listMovies } from '../api/movies'
import { getApiMessage } from '../api/http'
import MovieCard from '../components/MovieCard.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import ErrorState from '../components/ui/ErrorState.vue'

const router = useRouter()
const state = ref('loading') // loading | ok | error
const errorMsg = ref('')
const movies = ref([])

const nowShowing = computed(() => movies.value.filter((m) => m.status === 'NOW_SHOWING'))
const comingSoon = computed(() => movies.value.filter((m) => m.status === 'COMING_SOON'))
const featured = computed(() => nowShowing.value[0] ?? null)

onMounted(async () => {
  try {
    movies.value = await listMovies()
    state.value = 'ok'
  } catch (e) {
    errorMsg.value = getApiMessage(e, 'Không tải được danh sách phim')
    state.value = 'error'
  }
})

function goDetail(id) {
  router.push({ name: 'movie-detail', params: { id } })
}
</script>

<template>
  <main class="pb-16">
    <!-- Hero -->
    <section v-if="featured" class="relative overflow-hidden border-b border-white/5">
      <div class="absolute inset-0">
        <img
          v-if="featured.backdropUrl || featured.posterUrl"
          :src="featured.backdropUrl || featured.posterUrl"
          :alt="featured.title"
          class="h-full w-full object-cover object-center opacity-40"
        />
        <div class="absolute inset-0 bg-gradient-to-t from-surface-950 via-surface-950/80 to-surface-950/30" />
      </div>
      <div class="relative mx-auto max-w-6xl px-4 py-16 sm:py-24">
        <p class="text-sm font-medium uppercase tracking-wide text-brand-500">Đang chiếu</p>
        <h1 class="mt-2 max-w-2xl text-4xl font-semibold text-ink-100">{{ featured.title }}</h1>
        <p class="mt-3 max-w-xl text-ink-300 line-clamp-3">{{ featured.description }}</p>
        <div class="mt-6">
          <BaseButton size="lg" @click="goDetail(featured.id)">Đặt vé ngay</BaseButton>
        </div>
      </div>
    </section>

    <div class="mx-auto max-w-6xl px-4">
      <LoadingState v-if="state === 'loading'" message="Đang tải phim…" />
      <ErrorState v-else-if="state === 'error'" :message="errorMsg" class="mt-8" />

      <template v-else>
        <!-- Phim đang chiếu -->
        <section class="py-12">
          <h2 class="mb-6 text-2xl font-semibold text-ink-100">Phim đang chiếu</h2>
          <EmptyState
            v-if="nowShowing.length === 0"
            title="Chưa có phim đang chiếu"
            message="Vui lòng quay lại sau nhé."
          />
          <div v-else class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
            <MovieCard v-for="m in nowShowing" :key="m.id" :movie="m" mode="now" />
          </div>
        </section>

        <!-- Sắp chiếu -->
        <section v-if="comingSoon.length" class="py-4">
          <h2 class="mb-6 text-2xl font-semibold text-ink-100">Sắp chiếu</h2>
          <div class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
            <MovieCard v-for="m in comingSoon" :key="m.id" :movie="m" mode="coming" />
          </div>
        </section>
      </template>
    </div>
  </main>
</template>
