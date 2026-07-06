<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const state = ref('loading') // loading | ok | error
const health = ref(null)
const errorMsg = ref('')

onMounted(async () => {
  try {
    const { data } = await axios.get('/api/health')
    health.value = data
    state.value = data.db === 'UP' ? 'ok' : 'error'
    if (data.db !== 'UP') errorMsg.value = data.dbError || 'Không kết nối được cơ sở dữ liệu'
  } catch (e) {
    state.value = 'error'
    errorMsg.value = e.message || 'Không gọi được API backend'
  }
})
</script>

<template>
  <main class="min-h-screen flex items-center justify-center px-4">
    <div class="w-full max-w-md rounded-lg border border-white/5 bg-surface-800 p-6 text-center">
      <h1 class="text-2xl text-brand-500 font-semibold">Linh Vé Các</h1>
      <p class="mt-1 text-sm text-ink-500">Kiểm tra kết nối hệ thống (P1)</p>

      <!-- Loading -->
      <p v-if="state === 'loading'" class="mt-6 text-ink-300">Đang kiểm tra kết nối…</p>

      <!-- OK -->
      <div v-else-if="state === 'ok'" class="mt-6 space-y-1">
        <p class="text-success text-lg">✓ Backend OK · DB OK</p>
        <p class="text-ink-300">Ứng dụng: <span class="text-ink-100">{{ health.app }}</span></p>
        <p class="text-sm text-ink-500">Thời gian máy chủ: {{ health.time }}</p>
      </div>

      <!-- Error -->
      <div v-else class="mt-6 space-y-1">
        <p class="text-danger text-lg">✕ Kết nối thất bại</p>
        <p class="text-sm text-ink-500">{{ errorMsg }}</p>
        <p class="text-sm text-ink-500">Kiểm tra backend đã chạy ở cổng 8080 chưa.</p>
      </div>
    </div>
  </main>
</template>
