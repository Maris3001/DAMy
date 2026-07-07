<script setup>
// Thanh 6 bước wizard đặt vé (DESIGN.md §1.7): xong = check brand-500, hiện tại = ink-100,
// chưa tới = ink-500; mobile chỉ hiện "Bước 2/6". Luôn cho quay lại bước đã qua.
import { useRouter } from 'vue-router'

const props = defineProps({
  currentStep: { type: Number, required: true },
  // Bước xa nhất được phép vào (firstIncompleteStep) — bấm được các bước <= mốc này
  maxStep: { type: Number, required: true },
})

const STEPS = [
  { name: 'booking-region', label: 'Khu vực' },
  { name: 'booking-cinema', label: 'Rạp' },
  { name: 'booking-showtime', label: 'Suất chiếu' },
  { name: 'booking-seats', label: 'Ghế' },
  { name: 'booking-concessions', label: 'Bắp nước' },
  { name: 'booking-checkout', label: 'Thanh toán' },
]

const router = useRouter()

function goTo(index) {
  const step = index + 1
  if (step !== props.currentStep && step <= props.maxStep) {
    router.push({ name: STEPS[index].name })
  }
}
</script>

<template>
  <nav aria-label="Các bước đặt vé">
    <!-- Mobile: chỉ số bước -->
    <p class="text-sm text-ink-300 md:hidden">
      Bước <span class="text-brand-500">{{ currentStep }}/6</span> ·
      <span class="text-ink-100">{{ STEPS[currentStep - 1].label }}</span>
    </p>

    <!-- Desktop: đầy đủ 6 bước -->
    <ol class="hidden items-center gap-1 md:flex">
      <li v-for="(s, i) in STEPS" :key="s.name" class="flex items-center">
        <button
          type="button"
          class="flex items-center gap-1.5 rounded-lg px-2 py-1 text-sm transition-colors duration-150"
          :class="[
            i + 1 < currentStep ? 'text-brand-500' : i + 1 === currentStep ? 'text-ink-100' : 'text-ink-500',
            i + 1 <= maxStep && i + 1 !== currentStep ? 'hover:bg-surface-700 cursor-pointer' : 'cursor-default',
          ]"
          :disabled="i + 1 > maxStep"
          @click="goTo(i)"
        >
          <span
            v-if="i + 1 < currentStep"
            class="flex size-5 items-center justify-center rounded-full bg-brand-500/15 text-brand-500"
            aria-hidden="true"
            >✓</span
          >
          <span
            v-else
            class="flex size-5 items-center justify-center rounded-full border text-sm"
            :class="i + 1 === currentStep ? 'border-brand-500 text-brand-500' : 'border-surface-700'"
            aria-hidden="true"
            >{{ i + 1 }}</span
          >
          {{ s.label }}
        </button>
        <span v-if="i < STEPS.length - 1" class="mx-1 text-ink-500" aria-hidden="true">›</span>
      </li>
    </ol>
  </nav>
</template>
