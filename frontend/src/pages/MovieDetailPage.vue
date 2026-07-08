<script setup>
// Chi tiết phim (P-02): thông tin phim + tab 7 ngày + suất chiếu gộp theo rạp.
// Bấm suất chiếu → điền sẵn bước 1–3 của wizard đặt vé rồi vào thẳng bước chọn ghế (P4).
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMovie, getMovieShowtimes } from '../api/movies'
import { listRegions } from '../api/catalog'
import { getApiMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'
import { useBookingStore } from '../stores/booking'
import { formatDayTab, formatTime, toDateParam } from '../utils/format'
import BaseBadge from '../components/ui/BaseBadge.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseModal from '../components/ui/BaseModal.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import ErrorState from '../components/ui/ErrorState.vue'

const route = useRoute()
const router = useRouter()
const bookingStore = useBookingStore()
const auth = useAuthStore()
const isAdmin = computed(() => auth.isAdmin)
const movieId = computed(() => route.params.id)

const state = ref('loading') // loading | ok | error
const errorMsg = ref('')
const movie = ref(null)

// 7 ngày kể từ hôm nay
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

const showtimesState = ref('loading') // loading | ok | error
const cinemas = ref([])

const trailerOpen = ref(false)

const trailerEmbed = computed(() => {
  const url = movie.value?.trailerUrl
  if (!url) return ''
  const m = url.match(/(?:youtu\.be\/|v=)([\w-]{11})/)
  return m ? `https://www.youtube.com/embed/${m[1]}` : ''
})

async function loadShowtimes() {
  showtimesState.value = 'loading'
  try {
    cinemas.value = await getMovieShowtimes(movieId.value, selectedDate.value)
    showtimesState.value = 'ok'
  } catch {
    showtimesState.value = 'error'
  }
}

async function loadMovie() {
  state.value = 'loading'
  try {
    movie.value = await getMovie(movieId.value)
    state.value = 'ok'
    await loadShowtimes()
  } catch (e) {
    errorMsg.value = getApiMessage(e, 'Không tải được thông tin phim')
    state.value = 'error'
  }
}

/** Điền sẵn khu vực/rạp/suất vào bookingStore rồi vào thẳng bước chọn ghế. */
async function pickShowtime(cinema, slot) {
  if (isAdmin.value) return // Tài khoản quản trị không đặt vé
  let regionName = ''
  try {
    const regions = await listRegions()
    regionName = regions.find((r) => r.id === cinema.regionId)?.name ?? ''
  } catch {
    // Không lấy được tên khu vực cũng không chặn luồng đặt vé
  }
  bookingStore.enterFromMovie({
    region: { id: cinema.regionId, name: regionName },
    cinema: {
      id: cinema.cinemaId,
      name: cinema.cinemaName,
      address: cinema.cinemaAddress,
      chainName: cinema.chainName,
    },
    showtime: {
      id: slot.id,
      startsAt: slot.startsAt,
      basePrice: slot.basePrice,
      roomName: slot.roomName,
      roomType: slot.roomType,
      movieId: movie.value.id,
      movieTitle: movie.value.title,
      moviePosterUrl: movie.value.posterUrl,
    },
  })
  router.push({ name: 'booking-seats' })
}

watch(selectedDate, loadShowtimes)
onMounted(loadMovie)
</script>

<template>
  <main class="mx-auto max-w-6xl px-4 py-8">
    <LoadingState v-if="state === 'loading'" message="Đang tải phim…" />
    <ErrorState v-else-if="state === 'error'" :message="errorMsg" />

    <template v-else>
      <!-- Thông tin phim (ảnh nền mờ phía sau) -->
      <section class="relative">
        <div
          v-if="movie.backdropUrl || movie.posterUrl"
          class="pointer-events-none absolute inset-x-0 -top-8 h-64 overflow-hidden rounded-b-2xl"
          aria-hidden="true"
        >
          <img
            :src="movie.backdropUrl || movie.posterUrl"
            :alt="movie.title"
            class="h-full w-full object-cover object-center opacity-25"
          />
          <div class="absolute inset-0 bg-gradient-to-b from-surface-950/30 via-surface-950/70 to-surface-950" />
        </div>

        <div class="relative grid gap-6 md:grid-cols-[280px_1fr]">
          <div class="overflow-hidden rounded-lg border border-white/5 bg-surface-800">
          <div class="aspect-[2/3] bg-surface-900">
            <img
              v-if="movie.posterUrl"
              :src="movie.posterUrl"
              :alt="movie.title"
              class="h-full w-full object-cover"
            />
            <div v-else class="flex h-full items-center justify-center p-4 text-center text-ink-500">
              {{ movie.title }}
            </div>
          </div>
        </div>

        <div>
          <h1 class="text-2xl font-semibold text-ink-100">{{ movie.title }}</h1>
          <div class="mt-3 flex flex-wrap gap-2">
            <BaseBadge variant="brand">{{ movie.ageRating }}</BaseBadge>
            <BaseBadge v-for="g in movie.genres" :key="g.id">{{ g.name }}</BaseBadge>
            <BaseBadge>{{ movie.durationMin }} phút</BaseBadge>
          </div>
          <p class="mt-4 whitespace-pre-line text-ink-300">
            {{ movie.description || 'Nội dung đang được cập nhật.' }}
          </p>
          <div class="mt-6 flex gap-2">
            <BaseButton v-if="trailerEmbed" variant="secondary" @click="trailerOpen = true">
              Xem trailer
            </BaseButton>
          </div>
          </div>
        </div>
      </section>

      <!-- Chọn suất chiếu -->
      <section class="mt-10">
        <h2 class="mb-4 text-2xl font-semibold text-ink-100">Lịch chiếu</h2>

        <p
          v-if="isAdmin"
          class="mb-4 rounded-md border border-warning/40 bg-warning/10 px-3 py-2.5 text-sm text-ink-100"
        >
          Bạn đang đăng nhập bằng tài khoản quản trị nên không thể đặt vé.
        </p>

        <!-- Tab 7 ngày -->
        <div class="flex gap-2 overflow-x-auto pb-2">
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
          <LoadingState v-if="showtimesState === 'loading'" message="Đang tải suất chiếu…" />
          <ErrorState
            v-else-if="showtimesState === 'error'"
            message="Không tải được suất chiếu, vui lòng thử lại."
          />
          <EmptyState
            v-else-if="cinemas.length === 0"
            icon="📅"
            title="Chưa có suất chiếu cho ngày này"
            message="Vui lòng chọn ngày khác."
          />
          <div v-else class="space-y-4">
            <div
              v-for="c in cinemas"
              :key="c.cinemaId"
              class="rounded-lg border border-white/5 bg-surface-800 p-4"
            >
              <p class="font-medium text-ink-100">{{ c.cinemaName }}</p>
              <p class="text-sm text-ink-500">{{ c.cinemaAddress }}</p>
              <div class="mt-3 flex flex-wrap gap-2">
                <button
                  v-for="s in c.slots"
                  :key="s.id"
                  type="button"
                  :disabled="isAdmin"
                  class="rounded-md border border-surface-700 px-4 py-2 text-sm text-ink-100 transition-colors duration-150 enabled:hover:border-brand-500 enabled:hover:text-brand-500 disabled:cursor-not-allowed disabled:opacity-50"
                  @click="pickShowtime(c, s)"
                >
                  {{ formatTime(s.startsAt) }}
                  <span class="ml-1 text-ink-500">· {{ s.roomName }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </template>

    <!-- Trailer -->
    <BaseModal :open="trailerOpen" title="Trailer" size="lg" @close="trailerOpen = false">
      <div class="aspect-video w-full overflow-hidden rounded-md bg-black">
        <iframe
          v-if="trailerEmbed"
          :src="trailerEmbed"
          class="h-full w-full"
          allowfullscreen
          title="Trailer"
        />
      </div>
    </BaseModal>

  </main>
</template>
