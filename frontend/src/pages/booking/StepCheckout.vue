<script setup>
// Bước 6 (P-06): báo giá server-side → "Xác nhận đặt vé" tạo đơn PENDING_PAYMENT →
// "Thanh toán qua VNPay" khởi tạo payment và redirect sang cổng (P5).
import { inject, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createBooking, getQuote } from '../../api/booking'
import { createPayment } from '../../api/payment'
import { getApiMessage } from '../../api/http'
import { useBookingStore } from '../../stores/booking'
import { formatDate, formatTime, formatVnd } from '../../utils/format'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import LoadingState from '../../components/ui/LoadingState.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const booking = useBookingStore()
const router = useRouter()
const showToast = inject('bookingToast', () => {})

const state = ref('loading') // loading | ok | error
const errorMsg = ref('')
const quote = ref(null)
const creating = ref(false)
const paying = ref(false)

async function loadQuote() {
  if (booking.booking) {
    state.value = 'ok' // đã có đơn → hiện màn kết quả, không cần báo giá
    return
  }
  state.value = 'loading'
  try {
    quote.value = await getQuote(booking.showtime.id, booking.concessionQtyMap)
    state.value = 'ok'
  } catch (e) {
    if (e.response?.status === 409) {
      // Hold hết hạn → quay về chọn ghế
      booking.clearSeats()
      showToast(getApiMessage(e, 'Ghế giữ đã hết hạn, vui lòng chọn lại ghế.'))
      router.push({ name: 'booking-seats' })
      return
    }
    errorMsg.value = getApiMessage(e, 'Không tải được báo giá, vui lòng thử lại.')
    state.value = 'error'
  }
}

async function confirm() {
  creating.value = true
  try {
    booking.setBooking(await createBooking(booking.showtime.id, booking.concessionLines))
  } catch (e) {
    if (e.response?.status === 409) {
      booking.clearSeats()
      showToast(getApiMessage(e, 'Ghế giữ đã hết hạn, vui lòng chọn lại ghế.'))
      router.push({ name: 'booking-seats' })
    } else {
      showToast(getApiMessage(e, 'Không tạo được đơn, vui lòng thử lại.'))
    }
  } finally {
    creating.value = false
  }
}

async function pay() {
  paying.value = true
  try {
    const { payUrl } = await createPayment(booking.booking.code)
    // Điều hướng cả trang sang cổng thanh toán; cổng sẽ redirect về /thanh-toan/ket-qua.
    window.location.href = payUrl
  } catch (e) {
    paying.value = false
    if (e.response?.status === 409) {
      // Đơn hết hạn thanh toán → dọn và về trang chủ
      showToast(getApiMessage(e, 'Đơn đã hết hạn thanh toán, vui lòng đặt lại.'))
      booking.reset()
      router.push('/')
    } else {
      showToast(getApiMessage(e, 'Không mở được cổng thanh toán, vui lòng thử lại.'))
    }
  }
}

function startOver() {
  booking.reset()
  router.push('/')
}

onMounted(loadQuote)
</script>

<template>
  <section>
    <LoadingState v-if="state === 'loading'" message="Đang tính tiền…" />
    <ErrorState v-else-if="state === 'error'" :message="errorMsg" />

    <!-- ===== Màn kết quả sau khi tạo đơn ===== -->
    <div v-else-if="booking.booking" class="mx-auto max-w-xl">
      <div class="rounded-lg border border-white/5 bg-surface-800 p-6 text-center">
        <span class="text-4xl" aria-hidden="true">🎟️</span>
        <h1 class="mt-3 text-2xl font-semibold text-ink-100">Đã tạo đơn đặt vé</h1>
        <p class="mt-1 text-sm text-ink-500">Mã đơn của bạn</p>
        <p class="mt-1 font-mono text-2xl tracking-widest text-brand-500">{{ booking.booking.code }}</p>
        <div class="mt-3">
          <BaseBadge variant="warning">Chờ thanh toán</BaseBadge>
        </div>

        <dl class="mt-6 space-y-2 border-t border-white/5 pt-4 text-left text-sm">
          <div class="flex justify-between">
            <dt class="text-ink-500">Phim</dt>
            <dd class="text-ink-100">{{ booking.booking.movieTitle }}</dd>
          </div>
          <div class="flex justify-between">
            <dt class="text-ink-500">Rạp · Phòng</dt>
            <dd class="text-ink-100">{{ booking.booking.cinemaName }} · {{ booking.booking.roomName }}</dd>
          </div>
          <div class="flex justify-between">
            <dt class="text-ink-500">Suất chiếu</dt>
            <dd class="text-ink-100">
              {{ formatDate(booking.booking.startsAt) }} {{ formatTime(booking.booking.startsAt) }}
            </dd>
          </div>
          <div class="flex justify-between">
            <dt class="text-ink-500">Ghế</dt>
            <dd class="text-ink-100">{{ booking.booking.seats.map((s) => s.label).join(', ') }}</dd>
          </div>
          <div v-if="booking.booking.concessions.length" class="flex justify-between">
            <dt class="text-ink-500">Bắp nước</dt>
            <dd class="text-ink-100">
              {{ booking.booking.concessions.map((c) => `${c.name} ×${c.quantity}`).join(', ') }}
            </dd>
          </div>
          <div class="flex justify-between border-t border-white/5 pt-2">
            <dt class="text-ink-300">Tổng cộng</dt>
            <dd class="text-lg font-semibold text-brand-500 tabular-nums">
              {{ formatVnd(booking.booking.total) }}
            </dd>
          </div>
        </dl>

        <div class="mt-6 space-y-3">
          <BaseButton size="lg" block :loading="paying" @click="pay">Thanh toán qua VNPay</BaseButton>
          <p class="text-sm text-ink-500">Bạn sẽ được chuyển sang cổng thanh toán an toàn.</p>
          <BaseButton variant="ghost" block :disabled="paying" @click="startOver">Đặt vé mới</BaseButton>
        </div>
      </div>
    </div>

    <!-- ===== Xác nhận đơn (trước khi tạo) ===== -->
    <div v-else-if="quote" class="mx-auto grid max-w-4xl gap-6 lg:grid-cols-[1fr_320px]">
      <div class="rounded-lg border border-white/5 bg-surface-800 p-5">
        <h1 class="text-2xl font-semibold text-ink-100">Xác nhận đơn</h1>

        <div class="mt-4 flex gap-4">
          <div class="w-20 shrink-0 overflow-hidden rounded-md bg-surface-900">
            <img
              v-if="booking.showtime?.moviePosterUrl"
              :src="booking.showtime.moviePosterUrl"
              :alt="booking.showtime.movieTitle"
              class="aspect-[2/3] w-full object-cover"
            />
            <div v-else class="flex aspect-[2/3] items-center justify-center p-1 text-center text-sm text-ink-500">
              {{ booking.showtime?.movieTitle }}
            </div>
          </div>
          <div class="text-sm">
            <p class="text-lg font-medium text-ink-100">{{ booking.showtime?.movieTitle }}</p>
            <p class="mt-1 text-ink-300">{{ booking.cinema?.name }} · {{ booking.showtime?.roomName }}</p>
            <p class="mt-1 text-ink-500">
              {{ formatDate(booking.showtime?.startsAt) }} {{ formatTime(booking.showtime?.startsAt) }}
            </p>
          </div>
        </div>

        <ul class="mt-5 space-y-2 border-t border-white/5 pt-4 text-sm">
          <li v-for="s in quote.seats" :key="s.seatId" class="flex justify-between text-ink-300">
            <span>Ghế {{ s.label }}</span>
            <span class="tabular-nums">{{ formatVnd(s.price) }}</span>
          </li>
          <li
            v-for="c in quote.concessions"
            :key="c.concessionId"
            class="flex justify-between text-ink-300"
          >
            <span>{{ c.name }} ×{{ c.quantity }}</span>
            <span class="tabular-nums">{{ formatVnd(c.lineTotal) }}</span>
          </li>
        </ul>
      </div>

      <aside class="h-fit rounded-lg border border-white/5 bg-surface-800 p-5">
        <dl class="space-y-2 text-sm">
          <div class="flex justify-between">
            <dt class="text-ink-300">Tạm tính</dt>
            <dd class="tabular-nums text-ink-100">{{ formatVnd(quote.subtotal) }}</dd>
          </div>
          <div class="flex justify-between">
            <dt class="text-ink-300">Giảm giá</dt>
            <dd class="tabular-nums text-success">−{{ formatVnd(quote.discount) }}</dd>
          </div>
          <div class="flex justify-between border-t border-white/5 pt-3">
            <dt class="text-ink-100">Tổng cộng</dt>
            <dd class="text-2xl font-semibold text-brand-500 tabular-nums">{{ formatVnd(quote.total) }}</dd>
          </div>
        </dl>
        <p class="mt-3 text-sm text-ink-500">
          Ưu đãi thành viên và voucher sẽ được áp dụng ở giai đoạn tiếp theo.
        </p>
        <div class="mt-5">
          <BaseButton size="lg" block :loading="creating" @click="confirm">Xác nhận đặt vé</BaseButton>
        </div>
      </aside>
    </div>
  </section>
</template>
