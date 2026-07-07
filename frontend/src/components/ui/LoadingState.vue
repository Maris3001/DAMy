<script setup>
// Trạng thái đang tải dùng chung cho danh sách gọi API (DESIGN.md §1.5).
defineProps({
  message: { type: String, default: 'Đang tải…' },
})
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-3 py-12 text-ink-500">
    <span
      class="size-8 animate-spin rounded-full border-2 border-surface-700 border-t-brand-500"
      aria-hidden="true"
    />
    <p class="text-sm">{{ message }}</p>
  </div>
</template>
