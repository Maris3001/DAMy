<script setup>
// Trang "Thể loại": chip thể loại rút từ danh sách phim công khai + lưới phim lọc theo chip chọn.
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listMovies } from '../api/movies'
import { getApiMessage } from '../api/http'
import MovieCard from '../components/MovieCard.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import ErrorState from '../components/ui/ErrorState.vue'

const route = useRoute()
const router = useRouter()

const state = ref('loading') // loading | ok | error
const errorMsg = ref('')
const movies = ref([])
const selectedGenreId = ref(route.query.id ? Number(route.query.id) : null)

const genres = computed(() => {
  const seen = new Map()
  for (const m of movies.value) {
    for (const g of m.genres ?? []) {
      if (!seen.has(g.id)) seen.set(g.id, g)
    }
  }
  return [...seen.values()].sort((a, b) => a.name.localeCompare(b.name))
})

const filtered = computed(() =>
  selectedGenreId.value == null
    ? movies.value
    : movies.value.filter((m) => m.genres?.some((g) => g.id === selectedGenreId.value)),
)

function selectGenre(id) {
  selectedGenreId.value = id
  router.replace({ query: id == null ? {} : { id } })
}

onMounted(async () => {
  try {
    movies.value = await listMovies()
    state.value = 'ok'
  } catch (e) {
    errorMsg.value = getApiMessage(e, 'Không tải được danh sách phim')
    state.value = 'error'
  }
})
</script>

<template>
  <main class="mx-auto max-w-6xl px-4 py-8">
    <h1 class="text-2xl font-semibold text-ink-100">Thể loại phim</h1>

    <LoadingState v-if="state === 'loading'" message="Đang tải phim…" class="mt-8" />
    <ErrorState v-else-if="state === 'error'" :message="errorMsg" class="mt-8" />

    <template v-else>
      <div class="mt-4 flex flex-wrap gap-2">
        <button
          type="button"
          class="rounded-full border px-4 py-1.5 text-sm transition-colors duration-150"
          :class="
            selectedGenreId == null
              ? 'border-brand-500 bg-brand-500/10 text-brand-500'
              : 'border-surface-700 text-ink-300 hover:border-brand-500/40'
          "
          @click="selectGenre(null)"
        >
          Tất cả
        </button>
        <button
          v-for="g in genres"
          :key="g.id"
          type="button"
          class="rounded-full border px-4 py-1.5 text-sm transition-colors duration-150"
          :class="
            selectedGenreId === g.id
              ? 'border-brand-500 bg-brand-500/10 text-brand-500'
              : 'border-surface-700 text-ink-300 hover:border-brand-500/40'
          "
          @click="selectGenre(g.id)"
        >
          {{ g.name }}
        </button>
      </div>

      <EmptyState
        v-if="filtered.length === 0"
        class="mt-8"
        title="Không có phim thuộc thể loại này"
        message="Vui lòng chọn thể loại khác."
      />
      <div v-else class="mt-6 grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
        <MovieCard
          v-for="m in filtered"
          :key="m.id"
          :movie="m"
          :mode="m.status === 'COMING_SOON' ? 'coming' : 'now'"
        />
      </div>
    </template>
  </main>
</template>
