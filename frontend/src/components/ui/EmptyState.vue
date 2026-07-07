<script setup>
// Trạng thái rỗng dùng chung — thông điệp tiếng Việt tự nhiên (DESIGN.md §1.5).
defineProps({
  icon: { type: String, default: '🎬' },
  title: { type: String, default: 'Chưa có dữ liệu' },
  message: { type: String, default: '' },
})
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-2 py-12 text-center">
    <span class="text-4xl" aria-hidden="true">{{ icon }}</span>
    <p class="text-lg text-ink-300">{{ title }}</p>
    <p v-if="message" class="max-w-sm text-sm text-ink-500">{{ message }}</p>
    <slot />
  </div>
</template>
