<script setup>
// Khu tài khoản: tab điều hướng (Hồ sơ · Vé của tôi · Điểm thưởng · Đổi mật khẩu) + nội dung từng trang.
// Tab ví voucher sẽ thêm ở P7.
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { tierLabel, tierVariant } from '../utils/tier'
import BaseBadge from '../components/ui/BaseBadge.vue'

const auth = useAuthStore()

// Admin không đặt vé/tích điểm nên ẩn các tab dành cho thành viên.
const tabs = computed(() =>
  [
    { name: 'account-profile', label: 'Hồ sơ' },
    { name: 'account-bookings', label: 'Vé của tôi', userOnly: true },
    { name: 'account-points', label: 'Điểm thưởng', userOnly: true },
    { name: 'account-password', label: 'Đổi mật khẩu' },
  ].filter((t) => !t.userOnly || !auth.isAdmin),
)
</script>

<template>
  <main class="mx-auto max-w-4xl px-4 py-10">
    <div class="flex items-center gap-3">
      <h1 class="text-2xl font-semibold text-ink-100">Tài khoản của tôi</h1>
      <BaseBadge v-if="auth.user && !auth.isAdmin" :variant="tierVariant(auth.user.tier)">
        {{ tierLabel(auth.user.tier) }}
      </BaseBadge>
    </div>

    <nav class="mt-6 flex gap-1 border-b border-white/5">
      <RouterLink
        v-for="tab in tabs"
        :key="tab.name"
        :to="{ name: tab.name }"
        class="tab-link -mb-px border-b-2 border-transparent px-4 py-2.5 text-sm text-ink-400 transition-colors hover:text-ink-100"
      >
        {{ tab.label }}
      </RouterLink>
    </nav>

    <div class="mt-6">
      <RouterView />
    </div>
  </main>
</template>

<style scoped>
.tab-link.router-link-active {
  color: var(--color-brand-500, #e11d48);
  border-bottom-color: var(--color-brand-500, #e11d48);
  font-weight: 600;
}
</style>
