<script setup>
// Input chuẩn theo DESIGN.md §1.5 — label trên, lỗi màu danger dưới input.
import { useId } from 'vue'

defineProps({
  label: { type: String, default: '' },
  type: { type: String, default: 'text' },
  placeholder: { type: String, default: '' },
  error: { type: String, default: '' },
  hint: { type: String, default: '' },
  required: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  autocomplete: { type: String, default: 'off' },
})

const model = defineModel({ type: [String, Number], default: '' })
const id = useId()
</script>

<template>
  <div>
    <label v-if="label" :for="id" class="mb-1.5 block text-sm text-ink-300">
      {{ label }}<span v-if="required" class="text-danger"> *</span>
    </label>
    <input
      :id="id"
      v-model="model"
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      :autocomplete="autocomplete"
      class="h-10 w-full rounded-md border bg-surface-800 px-3 text-base text-ink-100
             placeholder:text-ink-500 outline-none transition-colors duration-150
             disabled:cursor-not-allowed disabled:opacity-60
             [color-scheme:dark]"
      :class="error ? 'border-danger' : 'border-surface-700 focus:border-brand-500'"
    />
    <p v-if="error" class="mt-1 text-sm text-danger">{{ error }}</p>
    <p v-else-if="hint" class="mt-1 text-sm text-ink-500">{{ hint }}</p>
  </div>
</template>
