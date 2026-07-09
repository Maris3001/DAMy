<script setup>
// Biểu đồ cột đơn giản bằng div (không cần thư viện) — light theme admin.
// data: [{ label, value, hint? }]. formatValue: hàm định dạng giá trị hiển thị ở tooltip.
import { computed } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] },
  formatValue: { type: Function, default: (v) => String(v) },
  // Số nhãn trục X hiển thị (thưa bớt khi nhiều cột để không rối).
  labelEvery: { type: Number, default: 5 },
})

const max = computed(() => Math.max(1, ...props.data.map((d) => Number(d.value) || 0)))

function heightPct(value) {
  return Math.round(((Number(value) || 0) / max.value) * 100)
}
</script>

<template>
  <div class="overflow-x-auto">
    <div class="flex h-48 min-w-full items-end gap-1" :style="{ minWidth: data.length * 14 + 'px' }">
      <div
        v-for="(d, i) in data"
        :key="i"
        class="group relative flex h-full flex-1 items-end"
        :title="`${d.label}: ${formatValue(d.value)}`"
      >
        <div
          class="w-full rounded-t bg-brand-600/80 transition-colors duration-150 group-hover:bg-brand-600"
          :style="{ height: Math.max(heightPct(d.value), d.value > 0 ? 2 : 0) + '%' }"
        />
      </div>
    </div>

    <!-- Trục X: nhãn thưa để tránh chồng chữ -->
    <div class="mt-2 flex min-w-full gap-1" :style="{ minWidth: data.length * 14 + 'px' }">
      <div v-for="(d, i) in data" :key="i" class="flex-1 text-center">
        <span v-if="i % labelEvery === 0 || i === data.length - 1" class="text-[10px] text-gray-400">
          {{ d.label }}
        </span>
      </div>
    </div>
  </div>
</template>
