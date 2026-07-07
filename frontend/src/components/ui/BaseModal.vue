<script setup>
// Modal chuẩn theo DESIGN.md §1.5 — overlay black/70, panel surface-800, đóng bằng ESC + click overlay.
import { onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '' },
  // sm | md | lg
  size: { type: String, default: 'md' },
})
const emit = defineEmits(['close'])

const sizeClass = {
  sm: 'max-w-md',
  md: 'max-w-lg',
  lg: 'max-w-3xl',
}

function onKey(e) {
  if (e.key === 'Escape' && props.open) emit('close')
}

// Khóa scroll nền khi mở modal
watch(
  () => props.open,
  (v) => {
    document.body.style.overflow = v ? 'hidden' : ''
  },
)

onMounted(() => document.addEventListener('keydown', onKey))
onUnmounted(() => {
  document.removeEventListener('keydown', onKey)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      leave-active-class="transition-opacity duration-150"
      leave-to-class="opacity-0"
    >
      <div
        v-if="open"
        class="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-black/70 p-4 py-10"
        @click.self="emit('close')"
      >
        <div
          class="w-full rounded-lg border border-white/5 bg-surface-800 p-6 shadow-xl"
          :class="sizeClass[size]"
        >
          <div v-if="title || $slots.header" class="mb-4 flex items-center justify-between gap-4">
            <slot name="header">
              <h2 class="text-2xl font-semibold text-ink-100">{{ title }}</h2>
            </slot>
            <button
              type="button"
              class="text-ink-500 transition-colors duration-150 hover:text-ink-100"
              aria-label="Đóng"
              @click="emit('close')"
            >
              ✕
            </button>
          </div>

          <slot />

          <div v-if="$slots.footer" class="mt-6 flex justify-end gap-2">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
