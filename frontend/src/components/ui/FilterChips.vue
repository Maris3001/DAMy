<script setup>
// Hàng nút lọc dạng chip (dark theme khu người dùng). Dùng cho lọc lịch sử vé/điểm/voucher.
// options: [{ value, label, count? }]. v-model là value đang chọn.
const model = defineModel({ type: [String, null], default: null })

defineProps({
  options: { type: Array, required: true },
})
</script>

<template>
  <div class="flex flex-wrap gap-2">
    <button
      v-for="opt in options"
      :key="String(opt.value)"
      type="button"
      class="rounded-full border px-3 py-1.5 text-sm transition-colors duration-150"
      :class="model === opt.value
        ? 'border-brand-500 bg-brand-500 font-medium text-black'
        : 'border-surface-700 text-ink-300 hover:border-brand-500'"
      @click="model = opt.value"
    >
      {{ opt.label }}
      <span v-if="opt.count != null" class="tabular-nums opacity-70">({{ opt.count }})</span>
    </button>
  </div>
</template>
