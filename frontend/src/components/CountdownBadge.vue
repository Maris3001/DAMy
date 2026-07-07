<script setup>
// Đồng hồ đếm ngược giữ ghế (DESIGN.md §1.7): hiển thị từ bước chọn ghế trở đi,
// chuyển màu danger khi còn < 2 phút; hết giờ emit 'expired' đúng 1 lần.
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { formatCountdown } from '../utils/format'

const props = defineProps({
  expiresAt: { type: String, default: null }, // ISO LocalDateTime từ backend
})

const emit = defineEmits(['expired'])

const remaining = ref(0)
let timer = null
let expiredEmitted = false

function tick() {
  if (!props.expiresAt) {
    remaining.value = 0
    return
  }
  remaining.value = Math.floor((new Date(props.expiresAt).getTime() - Date.now()) / 1000)
  if (remaining.value <= 0 && !expiredEmitted) {
    expiredEmitted = true
    emit('expired')
  }
}

watch(
  () => props.expiresAt,
  () => {
    expiredEmitted = false
    tick()
  },
)

onMounted(() => {
  tick()
  timer = setInterval(tick, 1000)
})
onUnmounted(() => clearInterval(timer))

const danger = computed(() => remaining.value < 120)
</script>

<template>
  <span
    v-if="expiresAt"
    class="inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-sm font-medium tabular-nums"
    :class="danger ? 'border-danger/40 bg-danger/15 text-danger' : 'border-brand-500/40 bg-brand-500/15 text-brand-500'"
  >
    <span aria-hidden="true">⏱</span>
    Giữ ghế {{ formatCountdown(remaining) }}
  </span>
</template>
