<script setup>
// Tab "Đổi mật khẩu": xác thực mật khẩu hiện tại rồi đặt mật khẩu mới (ràng buộc 8–72 ký tự).
import { reactive, ref } from 'vue'
import { getApiMessage, getFieldErrors } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseInput from '../../components/ui/BaseInput.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const auth = useAuthStore()

const form = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })
const fieldErrors = ref({})
const apiError = ref('')
const saved = ref(false)
const loading = ref(false)

async function submit() {
  apiError.value = ''
  fieldErrors.value = {}
  saved.value = false

  if (form.newPassword !== form.confirmPassword) {
    fieldErrors.value = { confirmPassword: 'Mật khẩu xác nhận không khớp' }
    return
  }

  loading.value = true
  try {
    await auth.changePassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
    })
    saved.value = true
    form.currentPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
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
    <p class="text-sm text-ink-500">Đặt mật khẩu mới cho tài khoản của bạn</p>

    <form class="mt-4 space-y-4 rounded-lg border border-white/5 bg-surface-800 p-6" novalidate @submit.prevent="submit">
      <ErrorState v-if="apiError" :message="apiError" />
      <p v-if="saved" class="rounded-md border border-success/40 bg-success/10 px-3 py-2.5 text-sm text-ink-100">
        ✓ Đã đổi mật khẩu
      </p>

      <BaseInput
        v-model="form.currentPassword"
        label="Mật khẩu hiện tại"
        type="password"
        required
        :error="fieldErrors.currentPassword"
      />
      <BaseInput
        v-model="form.newPassword"
        label="Mật khẩu mới"
        type="password"
        required
        hint="Từ 8 đến 72 ký tự"
        :error="fieldErrors.newPassword"
      />
      <BaseInput
        v-model="form.confirmPassword"
        label="Xác nhận mật khẩu mới"
        type="password"
        required
        :error="fieldErrors.confirmPassword"
      />

      <div class="flex justify-end pt-2">
        <BaseButton type="submit" :loading="loading">Đổi mật khẩu</BaseButton>
      </div>
    </form>
  </div>
</template>
