<script setup>
// Badge chuẩn theo DESIGN.md §1.5 — hạng thành viên, trạng thái booking, nhãn thể loại/độ tuổi.
import { computed } from 'vue'

const props = defineProps({
  // neutral | brand | success | warning | danger | info | gray
  variant: { type: String, default: 'neutral' },
})

const variantClass = {
  neutral: 'bg-surface-700 text-ink-300',
  brand: 'bg-brand-500/15 text-brand-500 border border-brand-500/40',
  success: 'bg-success/15 text-success border border-success/40',
  warning: 'bg-warning/15 text-warning border border-warning/40',
  danger: 'bg-danger/15 text-danger border border-danger/40',
  info: 'bg-info/15 text-info border border-info/40',
  gray: 'bg-white/5 text-ink-500 border border-white/10',
}

const classes = computed(() => [
  'inline-flex items-center rounded-full px-2.5 py-0.5 text-sm font-medium',
  variantClass[props.variant] ?? variantClass.neutral,
])
</script>

<template>
  <span :class="classes"><slot /></span>
</template>
