<script setup>
// Tab "Vé của tôi": danh sách đơn của user (mới nhất trước). Đơn đã thanh toán → xem lại vé + QR.
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listMyBookings } from '../../api/booking'
import { getApiMessage } from '../../api/http'
import { formatDate, formatTime, formatVnd } from '../../utils/format'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import LoadingState from '../../components/ui/LoadingState.vue'
import EmptyState from '../../components/ui/EmptyState.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const router = useRouter()
const state = ref('loading') // loading | ok | error
const errorMsg = ref('')
const bookings = ref([])

const STATUS = {
  PAID: { variant: 'success', label: 'Đã thanh toán' },
  PENDING_PAYMENT: { variant: 'warning', label: 'Chờ thanh toán' },
  EXPIRED: { variant: 'gray', label: 'Hết hạn' },
  CANCELLED: { variant: 'danger', label: 'Đã hủy' },
}

async function load() {
  state.value = 'loading'
  try {
    bookings.value = await listMyBookings()
    state.value = 'ok'
  } catch (e) {
    errorMsg.value = getApiMessage(e, 'Không tải được danh sách vé.')
    state.value = 'error'
  }
}

function viewTickets(code) {
  router.push({ name: 'payment-result', query: { code, status: 'success' } })
}

onMounted(load)
</script>

<template>
  <LoadingState v-if="state === 'loading'" message="Đang tải vé của bạn…" />
  <ErrorState v-else-if="state === 'error'" :message="errorMsg" />
  <EmptyState
    v-else-if="!bookings.length"
    title="Chưa có đơn đặt vé"
    message="Bạn chưa đặt vé nào. Khám phá phim đang chiếu và đặt vé ngay nhé!"
  />

  <ul v-else class="space-y-3">
    <li
      v-for="b in bookings"
      :key="b.code"
      class="flex gap-4 rounded-lg border border-white/5 bg-surface-800 p-4"
    >
      <div class="hidden w-16 shrink-0 overflow-hidden rounded-md bg-surface-900 sm:block">
        <img
          v-if="b.posterUrl"
          :src="b.posterUrl"
          :alt="b.movieTitle"
          class="aspect-[2/3] w-full object-cover"
        />
        <div v-else class="flex aspect-[2/3] items-center justify-center p-1 text-center text-xs text-ink-500">
          {{ b.movieTitle }}
        </div>
      </div>

      <div class="min-w-0 flex-1">
        <div class="flex items-start justify-between gap-2">
          <p class="truncate font-medium text-ink-100">{{ b.movieTitle }}</p>
          <BaseBadge :variant="(STATUS[b.status] || {}).variant || 'neutral'">
            {{ (STATUS[b.status] || {}).label || b.status }}
          </BaseBadge>
        </div>
        <p class="mt-1 text-sm text-ink-300">{{ b.cinemaName }} · {{ b.roomName }}</p>
        <p class="mt-0.5 text-sm text-ink-500">
          {{ formatDate(b.startsAt) }} {{ formatTime(b.startsAt) }} · {{ b.seatCount }} ghế
        </p>
        <div class="mt-2 flex items-center justify-between">
          <span class="font-mono text-xs tracking-wider text-ink-500">{{ b.code }}</span>
          <span class="font-semibold text-brand-500 tabular-nums">{{ formatVnd(b.total) }}</span>
        </div>
        <div v-if="b.status === 'PAID'" class="mt-3">
          <BaseButton variant="secondary" @click="viewTickets(b.code)">Xem vé</BaseButton>
        </div>
      </div>
    </li>
  </ul>
</template>
