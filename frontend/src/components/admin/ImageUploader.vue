<script setup>
// Upload ảnh cho form admin: chọn file → upload → lưu URL trả về vào v-model.
import { ref } from 'vue'
import { adminUploadImage } from '../../api/admin'
import { getApiMessage } from '../../api/http'

defineProps({
  label: { type: String, default: 'Ảnh' },
})
const model = defineModel({ type: String, default: '' })

const uploading = ref(false)
const error = ref('')
const inputRef = ref(null)

async function onPick(e) {
  const file = e.target.files?.[0]
  if (!file) return
  error.value = ''
  uploading.value = true
  try {
    const { url } = await adminUploadImage(file)
    model.value = url
  } catch (err) {
    error.value = getApiMessage(err, 'Tải ảnh thất bại')
  } finally {
    uploading.value = false
    if (inputRef.value) inputRef.value.value = ''
  }
}

function clear() {
  model.value = ''
}
</script>

<template>
  <div>
    <label class="mb-1.5 block text-sm text-ink-300">{{ label }}</label>
    <div class="flex items-center gap-3">
      <div
        class="flex h-24 w-16 shrink-0 items-center justify-center overflow-hidden rounded-md border border-surface-700 bg-surface-900"
      >
        <img v-if="model" :src="model" alt="Xem trước" class="h-full w-full object-cover" />
        <span v-else class="text-xs text-ink-500">Chưa có</span>
      </div>
      <div class="space-y-2">
        <input
          ref="inputRef"
          type="file"
          accept="image/*"
          class="block w-full text-sm text-ink-300 file:mr-3 file:rounded-md file:border-0 file:bg-brand-500 file:px-3 file:py-1.5 file:text-sm file:font-medium file:text-black hover:file:bg-brand-400"
          @change="onPick"
        />
        <div class="flex items-center gap-3 text-sm">
          <span v-if="uploading" class="text-ink-500">Đang tải ảnh…</span>
          <button
            v-if="model && !uploading"
            type="button"
            class="text-danger hover:underline"
            @click="clear"
          >
            Xóa ảnh
          </button>
        </div>
        <p v-if="error" class="text-sm text-danger">{{ error }}</p>
      </div>
    </div>
  </div>
</template>
