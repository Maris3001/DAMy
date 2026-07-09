<script setup>
// Biểu đồ donut bằng SVG thuần (stroke-dasharray) — không cần thư viện.
// segments: [{ label, value, color }]. Màu truyền vào (vd màu hạng thành viên).
import { computed } from 'vue'

const props = defineProps({
  segments: { type: Array, default: () => [] },
})

const R = 60 // bán kính vòng
const C = 2 * Math.PI * R // chu vi

const total = computed(() => props.segments.reduce((s, x) => s + (Number(x.value) || 0), 0))

// Mỗi cung: độ dài dash theo tỉ lệ + offset cộng dồn để xếp nối tiếp nhau.
const arcs = computed(() => {
  let acc = 0
  return props.segments.map((seg) => {
    const value = Number(seg.value) || 0
    const frac = total.value > 0 ? value / total.value : 0
    const dash = frac * C
    const arc = { color: seg.color, dash, gap: C - dash, offset: -acc, percent: Math.round(frac * 100) }
    acc += dash
    return arc
  })
})

function percentOf(value) {
  return total.value > 0 ? Math.round(((Number(value) || 0) / total.value) * 100) : 0
}
</script>

<template>
  <div class="flex flex-col items-center gap-5 sm:flex-row sm:items-center sm:gap-8">
    <div class="relative shrink-0">
      <svg viewBox="0 0 160 160" class="h-40 w-40 -rotate-90">
        <!-- Vòng nền khi chưa có dữ liệu -->
        <circle cx="80" cy="80" :r="R" fill="none" stroke="#E5E7EB" stroke-width="20" />
        <circle
          v-for="(a, i) in arcs"
          :key="i"
          cx="80"
          cy="80"
          :r="R"
          fill="none"
          :stroke="a.color"
          stroke-width="20"
          :stroke-dasharray="`${a.dash} ${a.gap}`"
          :stroke-dashoffset="a.offset"
        />
      </svg>
      <div class="absolute inset-0 flex flex-col items-center justify-center">
        <span class="text-2xl font-semibold tabular-nums text-gray-900">{{ total }}</span>
        <span class="text-xs text-gray-500">thành viên</span>
      </div>
    </div>

    <ul class="w-full space-y-2">
      <li v-for="(seg, i) in segments" :key="i" class="flex items-center gap-2 text-sm">
        <span class="h-3 w-3 shrink-0 rounded-full" :style="{ backgroundColor: seg.color }" />
        <span class="text-gray-700">{{ seg.label }}</span>
        <span class="ml-auto tabular-nums text-gray-900">{{ seg.value }}</span>
        <span class="w-10 text-right tabular-nums text-gray-400">{{ percentOf(seg.value) }}%</span>
      </li>
    </ul>
  </div>
</template>
