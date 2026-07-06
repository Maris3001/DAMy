<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getApiMessage, getFieldErrors } from '../api/http'
import { useAuthStore } from '../stores/auth'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import ErrorState from '../components/ui/ErrorState.vue'

const auth = useAuthStore()
const router = useRouter()

const form = reactive({ fullName: '', email: '', phone: '', birthDate: '', password: '' })
const fieldErrors = ref({})
const apiError = ref('')
const loading = ref(false)

async function submit() {
  apiError.value = ''
  fieldErrors.value = {}
  loading.value = true
  try {
    // SĐT/ngày sinh bỏ trống thì gửi null — backend coi là không bắt buộc
    await auth.register({
      ...form,
      phone: form.phone || null,
      birthDate: form.birthDate || null,
    })
    router.push('/')
  } catch (error) {
    fieldErrors.value = getFieldErrors(error)
    if (Object.keys(fieldErrors.value).length === 0) {
      apiError.value = getApiMessage(error)
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
      <p class="mt-1 text-center text-sm text-ink-500">Tạo tài khoản thành viên — tích điểm mỗi lần đặt vé</p>

      <form class="mt-6 space-y-4" novalidate @submit.prevent="submit">
        <ErrorState v-if="apiError" :message="apiError" />

        <BaseInput
          v-model="form.fullName"
          label="Họ tên"
          placeholder="Nguyễn Văn A"
          autocomplete="name"
          required
          :error="fieldErrors.fullName"
        />
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
          v-model="form.phone"
          label="Số điện thoại"
          type="tel"
          placeholder="0912345678"
          autocomplete="tel"
          :error="fieldErrors.phone"
        />
        <BaseInput
          v-model="form.birthDate"
          label="Ngày sinh"
          type="date"
          hint="Nhận ưu đãi sinh nhật 🎂"
          :error="fieldErrors.birthDate"
        />
        <BaseInput
          v-model="form.password"
          label="Mật khẩu"
          type="password"
          placeholder="Tối thiểu 8 ký tự"
          autocomplete="new-password"
          required
          :error="fieldErrors.password"
        />

        <BaseButton type="submit" size="lg" block :loading="loading">Đăng ký</BaseButton>
      </form>

      <p class="mt-6 text-center text-sm text-ink-500">
        Đã có tài khoản?
        <RouterLink to="/dang-nhap" class="text-brand-500 transition-colors duration-150 hover:text-brand-400">
          Đăng nhập
        </RouterLink>
      </p>
    </div>
  </main>
</template>
