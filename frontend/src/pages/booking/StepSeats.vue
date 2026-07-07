<script setup>
// Bước 4 (P-04): sơ đồ ghế + giữ ghế server-side. Poll 10s; bấm ghế trống → hold,
// bấm ghế của mình → nhả; 409 → toast "Ghế X vừa có người chọn".
import { computed, inject, onMounted, onUnmounted, ref } from 'vue'
import { getSeatMap, holdSeats, releaseSeats } from '../../api/booking'
import { getApiMessage } from '../../api/http'
import { useBookingStore } from '../../stores/booking'
import { formatDate, formatTime, formatVnd } from '../../utils/format'
import SeatMap from '../../components/SeatMap.vue'
import LoadingState from '../../components/ui/LoadingState.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const MAX_SEATS = 8
const POLL_MS = 10_000

const booking = useBookingStore()
const showToast = inject('bookingToast', () => {})

const state = ref('loading') // loading | ok | error
const seats = ref([])
let pollTimer = null

/** Đồng bộ store theo sơ đồ mới nhất (ghế MINE trên server là nguồn sự thật). */
function syncFromMap(map) {
  seats.value = map.seats
  const mine = map.seats
    .filter((s) => s.state === 'MINE')
    .map((s) => ({
      seatId: s.seatId,
      label: `${s.rowLabel}${s.colNumber}`,
      seatType: s.seatType,
      price: s.price,
    }))
  booking.syncHolds({ seats: mine, holdExpiresAt: map.holdExpiresAt })
}

async function load() {
  try {
    syncFromMap(await getSeatMap(booking.showtime.id))
    state.value = 'ok'
  } catch {
    if (state.value === 'loading') state.value = 'error'
  }
}

function applyHoldResponse(res) {
  booking.syncHolds({
    seats: res.seats.map((s) => ({
      seatId: s.seatId,
      label: s.label,
      seatType: s.seatType,
      price: s.price,
    })),
    holdExpiresAt: res.holdExpiresAt,
  })
  // Cập nhật trạng thái trên lưới theo ghế mình đang giữ
  const mineIds = new Set(res.seats.map((s) => s.seatId))
  seats.value = seats.value.map((s) => {
    if (mineIds.has(s.seatId)) return { ...s, state: 'MINE' }
    if (s.state === 'MINE') return { ...s, state: 'AVAILABLE' }
    return s
  })
}

async function toggle(seat) {
  if (seat.state === 'MINE') {
    try {
      applyHoldResponse(await releaseSeats(booking.showtime.id, [seat.seatId]))
    } catch (e) {
      showToast(getApiMessage(e, 'Không nhả được ghế, vui lòng thử lại.'))
    }
    return
  }
  if (seat.state !== 'AVAILABLE') return
  if (booking.seats.length >= MAX_SEATS) {
    showToast(`Chỉ được chọn tối đa ${MAX_SEATS} ghế.`)
    return
  }
  try {
    applyHoldResponse(await holdSeats(booking.showtime.id, [seat.seatId]))
  } catch (e) {
    if (e.response?.status === 409) {
      showToast(`Ghế ${seat.rowLabel}${seat.colNumber} vừa có người chọn. Vui lòng chọn ghế khác.`)
      load() // làm mới sơ đồ để hiện đúng ghế vừa bị giữ
    } else {
      showToast(getApiMessage(e, 'Không giữ được ghế, vui lòng thử lại.'))
    }
  }
}

async function removeSeat(item) {
  const seat = seats.value.find((s) => s.seatId === item.seatId)
  if (seat) await toggle({ ...seat, state: 'MINE' })
}

// Giá theo loại ghế có mặt trong phòng (server đã tính sẵn trên từng ghế)
const TYPE_LABELS = { STANDARD: 'Ghế thường', VIP: 'Ghế VIP', COUPLE: 'Ghế đôi' }
const priceByType = computed(() => {
  const map = new Map()
  for (const s of seats.value) {
    if (!map.has(s.seatType)) map.set(s.seatType, s.price)
  }
  return [...map.entries()].map(([type, price]) => ({ type, label: TYPE_LABELS[type] ?? type, price }))
})

onMounted(() => {
  load()
  pollTimer = setInterval(load, POLL_MS)
})
onUnmounted(() => clearInterval(pollTimer))
</script>

<template>
  <section>
    <h1 class="text-2xl font-semibold text-ink-100">Chọn ghế</h1>
    <p class="mt-1 text-sm text-ink-500">
      {{ booking.showtime?.movieTitle }} · {{ booking.cinema?.name }} · {{ booking.showtime?.roomName }} ·
      {{ formatDate(booking.showtime?.startsAt) }} {{ formatTime(booking.showtime?.startsAt) }}
    </p>

    <LoadingState v-if="state === 'loading'" message="Đang tải sơ đồ ghế…" />
    <ErrorState v-else-if="state === 'error'" message="Không tải được sơ đồ ghế, vui lòng thử lại." />

    <div v-else class="mt-6 grid gap-6 lg:grid-cols-[1fr_300px]">
      <div class="rounded-lg border border-white/5 bg-surface-800 p-4 md:p-6">
        <SeatMap :seats="seats" @toggle="toggle" />
      </div>

      <!-- Panel ghế đã chọn -->
      <aside class="space-y-4">
        <div class="rounded-lg border border-white/5 bg-surface-800 p-4">
          <p class="font-medium text-ink-100">Ghế đã chọn ({{ booking.seats.length }}/8)</p>
          <p v-if="booking.seats.length === 0" class="mt-2 text-sm text-ink-500">
            Bấm vào ghế trống trên sơ đồ để chọn.
          </p>
          <div v-else class="mt-3 flex flex-wrap gap-2">
            <span
              v-for="s in booking.seats"
              :key="s.seatId"
              class="inline-flex items-center gap-1.5 rounded-full border border-brand-500/40 bg-brand-500/10 py-1 pl-3 pr-1.5 text-sm text-brand-500"
            >
              {{ s.label }}
              <button
                type="button"
                class="flex size-5 items-center justify-center rounded-full text-ink-500 transition-colors duration-150 hover:bg-surface-700 hover:text-ink-100"
                :aria-label="`Bỏ ghế ${s.label}`"
                @click="removeSeat(s)"
              >
                ✕
              </button>
            </span>
          </div>
        </div>

        <div class="rounded-lg border border-white/5 bg-surface-800 p-4">
          <p class="font-medium text-ink-100">Giá vé</p>
          <ul class="mt-2 space-y-1.5 text-sm">
            <li v-for="p in priceByType" :key="p.type" class="flex justify-between text-ink-300">
              <span>{{ p.label }}</span>
              <span class="tabular-nums">{{ formatVnd(p.price) }}</span>
            </li>
          </ul>
          <div class="mt-3 flex justify-between border-t border-white/5 pt-3">
            <span class="text-ink-300">Tiền vé</span>
            <span class="font-semibold text-brand-500 tabular-nums">{{ formatVnd(booking.ticketTotal) }}</span>
          </div>
        </div>
      </aside>
    </div>
  </section>
</template>
