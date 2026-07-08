<script setup>
// Thanh tiến độ tới hạng kế tiếp. summary: { tier, nextTier, pointsToNextTier, lifetimePoints }.
import { computed } from 'vue'
import { TIER_ORDER, tierLabel } from '../utils/tier'

const props = defineProps({
  summary: { type: Object, required: true },
})

// Ngưỡng lên hạng (khớp TierPolicy phía backend) chỉ để vẽ % tiến độ trong cấp hiện tại.
const THRESHOLDS = { SILVER: 0, GOLD: 100, PLATINUM: 300 }

const isMax = computed(() => !props.summary.nextTier)

const percent = computed(() => {
  if (isMax.value) return 100
  const start = THRESHOLDS[props.summary.tier] ?? 0
  const end = THRESHOLDS[props.summary.nextTier] ?? start + 1
  const span = end - start
  if (span <= 0) return 100
  const done = (props.summary.lifetimePoints ?? 0) - start
  return Math.min(100, Math.max(0, Math.round((done / span) * 100)))
})

// Vị trí hạng hiện tại trong dãy để tô các mốc đã đạt.
const currentIndex = computed(() => TIER_ORDER.indexOf(props.summary.tier))
</script>

<template>
  <div>
    <p v-if="isMax" class="text-sm text-ink-300">
      🎉 Bạn đang ở hạng cao nhất — <span class="font-medium text-indigo-300">Platinum</span>.
    </p>
    <p v-else class="text-sm text-ink-300">
      Còn
      <span class="font-semibold text-brand-500 tabular-nums">{{ summary.pointsToNextTier.toLocaleString('vi-VN') }}</span>
      điểm nữa để lên hạng
      <span class="font-medium text-ink-100">{{ tierLabel(summary.nextTier) }}</span>.
    </p>

    <div class="mt-3 h-2 overflow-hidden rounded-full bg-surface-700">
      <div
        class="h-full rounded-full bg-brand-500 transition-[width] duration-300"
        :style="{ width: percent + '%' }"
      />
    </div>

    <div class="mt-2 flex justify-between text-xs">
      <span
        v-for="(t, i) in TIER_ORDER"
        :key="t"
        :class="i <= currentIndex ? 'font-medium text-ink-100' : 'text-ink-500'"
      >
        {{ tierLabel(t) }}
      </span>
    </div>
  </div>
</template>
