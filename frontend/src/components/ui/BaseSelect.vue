<script setup>
// Select chuẩn — khớp phong cách BaseInput (nền surface-800, focus brand-500).
import { useId } from 'vue'

defineProps({
  label: { type: String, default: '' },
  // options: mảng { value, label }
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: '' },
  error: { type: String, default: '' },
  hint: { type: String, default: '' },
  required: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
})

const model = defineModel({ type: [String, Number, null], default: '' })
const id = useId()
</script>

<template>
  <div>
    <label v-if="label" :for="id" class="mb-1.5 block text-sm text-ink-300">
      {{ label }}<span v-if="required" class="text-danger"> *</span>
    </label>
    <select
      :id="id"
      v-model="model"
      :disabled="disabled"
      class="h-10 w-full rounded-md border bg-surface-800 px-3 text-base text-ink-100
             outline-none transition-colors duration-150
             disabled:cursor-not-allowed disabled:opacity-60 [color-scheme:dark]"
      :class="error ? 'border-danger' : 'border-surface-700 focus:border-brand-500'"
    >
      <option v-if="placeholder" value="" disabled>{{ placeholder }}</option>
      <slot>
        <option v-for="opt in options" :key="opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </slot>
    </select>
    <p v-if="error" class="mt-1 text-sm text-danger">{{ error }}</p>
    <p v-else-if="hint" class="mt-1 text-sm text-ink-500">{{ hint }}</p>
  </div>
</template>
