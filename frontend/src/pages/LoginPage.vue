<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getApiMessage, getFieldErrors } from '../api/http'
import { useAuthStore } from '../stores/auth'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import ErrorState from '../components/ui/ErrorState.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const form = reactive({ email: '', password: '' })
const fieldErrors = ref({})
const apiError = ref('')
const loading = ref(false)

async function submit() {
  apiError.value = ''
  fieldErrors.value = {}
  loading.value = true
  try {
    await auth.login(form)
    router.push(typeof route.query.redirect === 'string' ? route.query.redirect : '/')
  } catch (error) {
    fieldErrors.value = getFieldErrors(error)
    if (Object.keys(fieldErrors.value).length === 0) {
      apiError.value = getApiMessage(error, 'Email hoặc mật khẩu không đúng')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="flex min-h-[calc(100vh-3.5rem)] items-center justify-center px-4 py-12">
    <div class="w-full max-w-md rounded-lg border border-white/5 bg-surface-800 p-6">
      <h1 class="text-center text-2xl font-semibold text-brand-500">Linh Vé Các</h1>
      <p class="mt-1 text-center text-sm text-ink-500">Đăng nhập để đặt vé và nhận ưu đãi thành viên</p>

      <form class="mt-6 space-y-4" novalidate @submit.prevent="submit">
        <ErrorState v-if="apiError" :message="apiError" />

        <BaseInput
          v-model="form.email"
          label="Email"
          type="email"
          placeholder="ban@email.com"
          autocomplete="email"
          required
          :error="fieldErrors.email"
        />
        <BaseInput
          v-model="form.password"
          label="Mật khẩu"
          type="password"
          placeholder="••••••••"
          autocomplete="current-password"
          required
          :error="fieldErrors.password"
        />

        <BaseButton type="submit" size="lg" block :loading="loading">Đăng nhập</BaseButton>
      </form>

      <p class="mt-6 text-center text-sm text-ink-500">
        Chưa có tài khoản?
        <RouterLink to="/dang-ky" class="text-brand-500 transition-colors duration-150 hover:text-brand-400">
          Đăng ký ngay
        </RouterLink>
      </p>
    </div>
  </main>
</template>
