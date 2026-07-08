<script setup>
// Trang kết quả thanh toán (P5) — đích redirect từ cổng VNPay/mock: /thanh-toan/ket-qua?code=&status=
// success → hiện vé (mã + QR); failed → cho thử lại; invalid → báo lỗi. Chỉ dựa query + API,
// không phụ thuộc sessionStorage (đơn đã rời wizard). Xem lại "Vé của tôi" thuộc phạm vi P8.
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBooking } from '../api/booking'
import { createPayment } from '../api/payment'
import { getApiMessage } from '../api/http'
import { useBookingStore } from '../stores/booking'
import { useAuthStore } from '../stores/auth'
import { formatDate, formatTime, formatVnd } from '../utils/format'
import BaseBadge from '../components/ui/BaseBadge.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import ErrorState from '../components/ui/ErrorState.vue'
import TicketQr from '../components/TicketQr.vue'

const route = useRoute()
const router = useRouter()
const booking = useBookingStore()
const auth = useAuthStore()

const state = ref('loading') // loading | success | failed | invalid | error
const errorMsg = ref('')
const order = ref(null)
const paying = ref(false)

const code = route.query.code || ''

async function load() {
  const status = route.query.status
  if (status === 'success') {
    try {
      order.value = await getBooking(code)
      booking.reset() // đơn đã thanh toán xong → dọn trạng thái wizard
      auth.fetchMe().catch(() => {}) // làm mới điểm/hạng vừa tích (badge hạng, tab điểm thưởng)
      state.value = 'success'
    } catch (e) {
      errorMsg.value = getApiMessage(e, 'Không tải được thông tin vé.')
      state.value = 'error'
    }
  } else if (status === 'failed') {
    state.value = 'failed'
  } else {
    state.value = 'invalid'
  }
}

async function retry() {
  paying.value = true
  try {
    const { payUrl } = await createPayment(code)
    window.location.href = payUrl
  } catch (e) {
    paying.value = false
    if (e.response?.status === 409) {
      // Đơn đã hết hạn → không thử lại được nữa
      state.value = 'invalid'
    } else {
      errorMsg.value = getApiMessage(e, 'Không mở được cổng thanh toán, vui lòng thử lại.')
      state.value = 'error'
    }
  }
}

function goHome() {
  booking.reset()
  router.push('/')
}

onMounted(load)
</script>

<template>
  <section class="mx-auto max-w-xl px-4 py-8">
    <LoadingState v-if="state === 'loading'" message="Đang xác nhận thanh toán…" />
    <ErrorState v-else-if="state === 'error'" :message="errorMsg" />

    <!-- ===== Thành công: hiện vé ===== -->
    <div v-else-if="state === 'success' && order" class="rounded-lg border border-white/5 bg-surface-800 p-6">
      <div class="text-center">
        <span class="text-4xl" aria-hidden="true">✅</span>
        <h1 class="mt-3 text-2xl font-semibold text-ink-100">Thanh toán thành công</h1>
        <p class="mt-1 text-sm text-ink-500">Mã đơn</p>
        <p class="font-mono text-lg tracking-widest text-brand-500">{{ order.code }}</p>
        <div class="mt-2">
          <BaseBadge variant="success">Đã thanh toán</BaseBadge>
        </div>
      </div>

      <dl class="mt-6 space-y-2 border-t border-white/5 pt-4 text-sm">
        <div class="flex justify-between">
          <dt class="text-ink-500">Phim</dt>
          <dd class="text-ink-100">{{ order.movieTitle }}</dd>
        </div>
        <div class="flex justify-between">
          <dt class="text-ink-500">Rạp · Phòng</dt>
          <dd class="text-ink-100">{{ order.cinemaName }} · {{ order.roomName }}</dd>
        </div>
        <div class="flex justify-between">
          <dt class="text-ink-500">Suất chiếu</dt>
          <dd class="text-ink-100">{{ formatDate(order.startsAt) }} {{ formatTime(order.startsAt) }}</dd>
        </div>
        <div class="flex justify-between border-t border-white/5 pt-2">
          <dt class="text-ink-300">Tổng đã thanh toán</dt>
          <dd class="text-lg font-semibold text-brand-500 tabular-nums">{{ formatVnd(order.total) }}</dd>
        </div>
      </dl>

      <p
        v-if="order.earnedPoints > 0"
        class="mt-4 flex items-center justify-center gap-1.5 rounded-md border border-brand-500/30 bg-brand-500/10 px-3 py-2 text-sm"
      >
        <span aria-hidden="true">⭐</span>
        <span class="text-ink-300">Bạn nhận được</span>
        <span class="font-semibold text-brand-500 tabular-nums">+{{ order.earnedPoints.toLocaleString('vi-VN') }} điểm</span>
        <span class="text-ink-300">Linh Vé</span>
      </p>

      <h2 class="mt-6 text-sm font-medium text-ink-300">Vé điện tử</h2>
      <div class="mt-3 flex flex-col items-center rounded-md border border-white/5 bg-surface-900 p-5">
        <TicketQr :value="order.code" :size="180" />
        <p class="mt-3 font-mono tracking-widest text-brand-500">{{ order.code }}</p>
        <p class="mt-1 text-xs text-ink-500">Đưa mã này cho nhân viên soát vé</p>

        <div class="mt-4 w-full border-t border-white/5 pt-4">
          <p class="text-sm text-ink-500">Ghế ({{ order.seats.length }})</p>
          <div class="mt-2 flex flex-wrap gap-2">
            <span
              v-for="seat in order.seats"
              :key="seat.seatId"
              class="rounded-md border border-white/10 bg-surface-800 px-3 py-1.5 text-sm font-semibold text-ink-100"
            >
              {{ seat.label }}
            </span>
          </div>
        </div>
      </div>

      <div class="mt-6">
        <BaseButton variant="ghost" block @click="goHome">Về trang chủ</BaseButton>
      </div>
    </div>

    <!-- ===== Thất bại: cho thử lại ===== -->
    <div v-else-if="state === 'failed'" class="rounded-lg border border-white/5 bg-surface-800 p-6 text-center">
      <span class="text-4xl" aria-hidden="true">⚠️</span>
      <h1 class="mt-3 text-2xl font-semibold text-ink-100">Thanh toán chưa hoàn tất</h1>
      <p class="mt-2 text-sm text-ink-500">
        Giao dịch bị hủy hoặc không thành công. Ghế vẫn được giữ trong thời gian còn lại — bạn có thể thử lại.
      </p>
      <div class="mt-6 space-y-3">
        <BaseButton size="lg" block :loading="paying" @click="retry">Thử lại thanh toán</BaseButton>
        <BaseButton variant="ghost" block :disabled="paying" @click="goHome">Đặt vé mới</BaseButton>
      </div>
    </div>

    <!-- ===== Không hợp lệ / hết hạn ===== -->
    <div v-else class="rounded-lg border border-white/5 bg-surface-800 p-6 text-center">
      <span class="text-4xl" aria-hidden="true">🚫</span>
      <h1 class="mt-3 text-2xl font-semibold text-ink-100">Không xác nhận được thanh toán</h1>
      <p class="mt-2 text-sm text-ink-500">
        Đường dẫn không hợp lệ hoặc đơn đã hết hạn. Vui lòng đặt vé lại.
      </p>
      <div class="mt-6">
        <BaseButton size="lg" block @click="goHome">Về trang chủ</BaseButton>
      </div>
    </div>
  </section>
</template>
