<script setup>
// Khung wizard đặt vé (P-03): BookingStepper trên đầu + CountdownBadge từ bước ghế,
// nội dung theo router-view, thanh đáy cố định (tổng tạm tính + Quay lại/Tiếp tục).
import { computed, provide, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBookingStore } from '../../stores/booking'
import { formatVnd } from '../../utils/format'
import BookingStepper from '../../components/BookingStepper.vue'
import CountdownBadge from '../../components/CountdownBadge.vue'
import BaseButton from '../../components/ui/BaseButton.vue'

const STEP_ROUTES = [
  'booking-region',
  'booking-cinema',
  'booking-showtime',
  'booking-seats',
  'booking-concessions',
  'booking-checkout',
]

const route = useRoute()
const router = useRouter()
const booking = useBookingStore()

const currentStep = computed(() => route.meta.step ?? 1)

const canContinue = computed(() => {
  switch (currentStep.value) {
    case 1:
      return Boolean(booking.region)
    case 2:
      return Boolean(booking.cinema)
    case 3:
      return Boolean(booking.showtime)
    case 4:
      return booking.holdAlive
    case 5:
      return true // bắp nước bỏ qua được
    default:
      return false
  }
})

function goNext() {
  if (currentStep.value < 6 && canContinue.value) {
    router.push({ name: STEP_ROUTES[currentStep.value] })
  }
}

function goBack() {
  if (currentStep.value === 1) {
    router.push('/')
  } else {
    router.push({ name: STEP_ROUTES[currentStep.value - 2] })
  }
}

// Toast nhẹ dùng chung cho các bước (vd: "Ghế A5 vừa có người chọn")
const toast = ref(null)
let toastTimer = null
function showToast(message) {
  toast.value = message
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = null), 4000)
}
provide('bookingToast', showToast)

// Hết giờ giữ ghế → nhả ghế local + quay về bước chọn ghế (đơn đã tạo thì màn kết quả tự xử lý)
function onHoldExpired() {
  if (booking.booking) return
  booking.clearSeats()
  showToast('Hết thời gian giữ ghế. Vui lòng chọn lại ghế.')
  if (currentStep.value > 4) {
    router.push({ name: 'booking-seats' })
  }
}
</script>

<template>
  <div class="min-h-screen pb-24">
    <!-- Stepper + countdown -->
    <div class="sticky top-0 z-30 border-b border-white/5 bg-surface-950/95 backdrop-blur">
      <div class="mx-auto flex max-w-6xl items-center justify-between gap-3 px-4 py-3">
        <BookingStepper :current-step="currentStep" :max-step="booking.firstIncompleteStep" />
        <CountdownBadge
          v-if="currentStep >= 4 && !booking.booking"
          :expires-at="booking.holdExpiresAt"
          @expired="onHoldExpired"
        />
      </div>
    </div>

    <main class="mx-auto max-w-6xl px-4 py-6">
      <router-view />
    </main>

    <!-- Thanh đáy cố định -->
    <div class="fixed inset-x-0 bottom-0 z-30 border-t border-white/5 bg-surface-900/95 backdrop-blur">
      <div class="mx-auto flex max-w-6xl items-center justify-between gap-3 px-4 py-3">
        <div>
          <p class="text-sm text-ink-500">Tạm tính</p>
          <p class="text-lg font-semibold text-brand-500 tabular-nums">
            {{ formatVnd(booking.grandTotal) }}
          </p>
        </div>
        <div class="flex items-center gap-2">
          <BaseButton variant="ghost" @click="goBack">Quay lại</BaseButton>
          <BaseButton v-if="currentStep < 6" :disabled="!canContinue" @click="goNext">
            Tiếp tục
          </BaseButton>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <Transition
      enter-active-class="transition-opacity duration-150"
      leave-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      leave-to-class="opacity-0"
    >
      <div
        v-if="toast"
        class="fixed bottom-24 left-1/2 z-40 -translate-x-1/2 rounded-lg border border-warning/40 bg-surface-800 px-4 py-2.5 text-sm text-ink-100 shadow-none"
        role="status"
      >
        {{ toast }}
      </div>
    </Transition>
  </div>
</template>
