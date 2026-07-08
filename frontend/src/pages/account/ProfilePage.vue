<script setup>
import { reactive, ref, watchEffect } from 'vue'
import { getApiMessage, getFieldErrors } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseInput from '../../components/ui/BaseInput.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const auth = useAuthStore()

const form = reactive({ fullName: '', phone: '', birthDate: '' })
const fieldErrors = ref({})
const apiError = ref('')
const saved = ref(false)
const loading = ref(false)

// Đổ dữ liệu user vào form (user đã được guard nạp trước khi vào trang)
watchEffect(() => {
  if (auth.user) {
    form.fullName = auth.user.fullName
    form.phone = auth.user.phone ?? ''
    form.birthDate = auth.user.birthDate ?? ''
  }
})

async function save() {
  apiError.value = ''
  fieldErrors.value = {}
  saved.value = false
  loading.value = true
  try {
    await auth.updateProfile({
      fullName: form.fullName,
      phone: form.phone || null,
      birthDate: form.birthDate || null,
    })
    saved.value = true
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
  <div class="w-full max-w-md">
    <p class="text-sm text-ink-500">Cập nhật thông tin để nhận ưu đãi phù hợp với bạn</p>

    <form class="mt-4 space-y-4 rounded-lg border border-white/5 bg-surface-800 p-6" novalidate @submit.prevent="save">
        <ErrorState v-if="apiError" :message="apiError" />
        <p v-if="saved" class="rounded-md border border-success/40 bg-success/10 px-3 py-2.5 text-sm text-ink-100">
          ✓ Đã lưu thay đổi
        </p>

        <BaseInput :model-value="auth.user?.email" label="Email" type="email" disabled hint="Email dùng để đăng nhập, không thể thay đổi" />
        <BaseInput v-model="form.fullName" label="Họ tên" required :error="fieldErrors.fullName" />
        <BaseInput v-model="form.phone" label="Số điện thoại" type="tel" placeholder="0912345678" :error="fieldErrors.phone" />
        <BaseInput v-model="form.birthDate" label="Ngày sinh" type="date" hint="Nhận ưu đãi sinh nhật 🎂" :error="fieldErrors.birthDate" />

        <div class="flex items-center justify-between pt-2">
          <span
            v-if="auth.user"
            class="rounded-full border border-surface-700 px-3 py-1 text-sm text-ink-300"
          >
            {{ auth.user.role === 'ADMIN' ? 'Quản trị viên' : 'Thành viên' }}
          </span>
          <BaseButton type="submit" :loading="loading">Lưu thay đổi</BaseButton>
        </div>
      </form>
  </div>
</template>
