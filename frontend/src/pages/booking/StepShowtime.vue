<script setup>
// Bước 3: chọn ngày (tab 7 ngày) & suất chiếu — gộp theo phim tại rạp đã chọn.
import { computed, onMounted, ref, watch } from 'vue'
import { listCinemaShowtimes } from '../../api/catalog'
import { useBookingStore } from '../../stores/booking'
import { formatDayTab, formatTime, toDateParam } from '../../utils/format'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import LoadingState from '../../components/ui/LoadingState.vue'
import ErrorState from '../../components/ui/ErrorState.vue'
import EmptyState from '../../components/ui/EmptyState.vue'

const booking = useBookingStore()

const days = computed(() => {
  const base = new Date()
  base.setHours(0, 0, 0, 0)
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(base)
    d.setDate(base.getDate() + i)
    return d
  })
})
const selectedDate = ref(toDateParam(new Date()))

const state = ref('loading') // loading | ok | error
const movies = ref([])

async function load() {
  state.value = 'loading'
  try {
    movies.value = await listCinemaShowtimes(booking.cinema.id, selectedDate.value)
    state.value = 'ok'
  } catch {
    state.value = 'error'
  }
}

function pick(movie, slot) {
  if (booking.showtime?.id !== slot.id) {
    booking.setShowtime({
      id: slot.id,
      startsAt: slot.startsAt,
      basePrice: slot.basePrice,
      roomName: slot.roomName,
      roomType: slot.roomType,
      movieId: movie.movieId,
      movieTitle: movie.title,
      moviePosterUrl: movie.posterUrl,
    })
  }
}

watch(selectedDate, load)
onMounted(load)
</script>

<template>
  <section>
    <h1 class="text-2xl font-semibold text-ink-100">Chọn suất chiếu</h1>
    <p class="mt-1 text-sm text-ink-500">{{ booking.cinema?.name }} · {{ booking.cinema?.address }}</p>

    <!-- Tab 7 ngày -->
    <div class="mt-4 flex gap-2 overflow-x-auto pb-2">
      <button
        v-for="d in days"
        :key="toDateParam(d)"
        type="button"
        class="shrink-0 rounded-lg border px-4 py-2 text-sm transition-colors duration-150"
        :class="
          selectedDate === toDateParam(d)
            ? 'border-brand-500 bg-brand-500/10 text-brand-500'
            : 'border-surface-700 text-ink-300 hover:border-brand-500/40'
        "
        @click="selectedDate = toDateParam(d)"
      >
        {{ formatDayTab(d) }}
      </button>
    </div>

    <div class="mt-4">
      <LoadingState v-if="state === 'loading'" message="Đang tải suất chiếu…" />
      <ErrorState v-else-if="state === 'error'" message="Không tải được suất chiếu, vui lòng thử lại." />
      <EmptyState
        v-else-if="movies.length === 0"
        icon="📅"
        title="Chưa có suất chiếu cho ngày này"
        message="Vui lòng chọn ngày khác."
      />

      <div v-else class="space-y-4">
        <div
          v-for="m in movies"
          :key="m.movieId"
          class="flex gap-4 rounded-lg border border-white/5 bg-surface-800 p-4"
        >
          <div class="hidden w-20 shrink-0 overflow-hidden rounded-md bg-surface-900 sm:block">
            <img
              v-if="m.posterUrl"
              :src="m.posterUrl"
              :alt="m.title"
              class="aspect-[2/3] w-full object-cover"
            />
            <div v-else class="flex aspect-[2/3] items-center justify-center p-1 text-center text-sm text-ink-500">
              {{ m.title }}
            </div>
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-center gap-2">
              <p class="text-lg font-medium text-ink-100">{{ m.title }}</p>
              <BaseBadge variant="brand">{{ m.ageRating }}</BaseBadge>
              <span class="text-sm text-ink-500">{{ m.durationMin }} phút</span>
            </div>
            <div class="mt-3 flex flex-wrap gap-2">
              <button
                v-for="s in m.slots"
                :key="s.id"
                type="button"
                class="rounded-md border px-4 py-2 text-sm transition-colors duration-150"
                :class="
                  booking.showtime?.id === s.id
                    ? 'border-brand-500 bg-brand-500/10 text-brand-500'
                    : 'border-surface-700 text-ink-100 hover:border-brand-500 hover:text-brand-500'
                "
                @click="pick(m, s)"
              >
                {{ formatTime(s.startsAt) }}
                <span class="ml-1 text-ink-500">· {{ s.roomName }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
