<script setup>
// Khối báo lỗi dùng chung (lỗi API trên form, lỗi tải danh sách...).
defineProps({
  message: { type: String, default: 'Có lỗi xảy ra, vui lòng thử lại.' },
})
</script>

<template>
  <div class="flex items-start gap-2 rounded-md border border-danger/40 bg-danger/10 px-3 py-2.5">
    <span class="text-danger" aria-hidden="true">✕</span>
    <p class="text-sm text-ink-100">{{ message }}</p>
  </div>
</template>
