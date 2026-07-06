<script setup>
// Nút chuẩn theo DESIGN.md §1.5 — dùng lại ở mọi nơi, không chế kiểu mới.
import { computed } from 'vue'

const props = defineProps({
  variant: { type: String, default: 'primary' }, // primary | secondary | ghost | danger
  size: { type: String, default: 'md' },         // md | lg
  type: { type: String, default: 'button' },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  block: { type: Boolean, default: false },      // full-width (CTA form)
})

const variantClass = {
  primary: 'bg-brand-500 text-black hover:bg-brand-400 active:bg-brand-600',
  secondary: 'border border-brand-500 text-brand-500 bg-transparent hover:bg-brand-500/10',
  ghost: 'text-ink-300 bg-transparent hover:text-ink-100 hover:bg-surface-700',
  danger: 'bg-danger text-white hover:bg-danger/90',
}

const sizeClass = {
  md: 'h-10 px-4 text-base',
  lg: 'h-12 px-6 text-base',
}

const classes = computed(() => [
  'inline-flex items-center justify-center gap-2 rounded-lg font-medium',
  'transition-colors duration-150 select-none',
  'disabled:opacity-50 disabled:cursor-not-allowed',
  variantClass[props.variant],
  sizeClass[props.size],
  props.block ? 'w-full' : '',
])
</script>

<template>
  <button :type="type" :class="classes" :disabled="disabled || loading">
    <span
      v-if="loading"
      class="size-5 animate-spin rounded-full border-2 border-current border-t-transparent"
      aria-hidden="true"
    />
    <slot v-if="!loading" />
  </button>
</template>
