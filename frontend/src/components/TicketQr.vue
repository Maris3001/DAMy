<script setup>
// Sinh QR chứa mã vé (canvas) — dùng cho vé sau khi thanh toán thành công.
import { onMounted, ref, watch } from 'vue'
import QRCode from 'qrcode'

const props = defineProps({
  value: { type: String, required: true },
  size: { type: Number, default: 112 },
})

const canvas = ref(null)

function render() {
  if (!canvas.value || !props.value) return
  QRCode.toCanvas(canvas.value, props.value, {
    width: props.size,
    margin: 1,
    color: { dark: '#0f1115', light: '#ffffff' },
  })
}

onMounted(render)
watch(() => props.value, render)
</script>

<template>
  <canvas ref="canvas" class="rounded-md bg-white p-1" :aria-label="`Mã QR vé ${value}`" />
</template>
