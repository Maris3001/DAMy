<script setup>
// Admin: chạy OfferEngine sinh ưu đãi cá nhân hóa (demo) + danh sách thành viên theo hạng.
import { computed, onMounted, ref } from 'vue'
import { adminRunOffers, adminListMembers } from '../../api/admin'
import { getApiMessage } from '../../api/http'
import { formatDate } from '../../utils/format'
import { tierLabel, tierVariant } from '../../utils/tier'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'

const TIER_FILTERS = [
  { value: '', label: 'Tất cả' },
  { value: 'SILVER', label: 'Bạc' },
  { value: 'GOLD', label: 'Vàng' },
  { value: 'PLATINUM', label: 'Bạch kim' },
]

// ----- Chạy OfferEngine -----
const running = ref(false)
const runResult = ref(null) // { birthday, winback, genreFavorite, tierUp }
const runError = ref('')

const runTotal = computed(() =>
  runResult.value
    ? runResult.value.birthday + runResult.value.winback + runResult.value.genreFavorite + runResult.value.tierUp
    : 0,
)

async function runOffers() {
  running.value = true
  runError.value = ''
  try {
    runResult.value = await adminRunOffers()
    await loadMembers() // hạng/điểm có thể đổi sau khi cấp voucher — làm mới bảng
  } catch (e) {
    runError.value = getApiMessage(e, 'Chạy sinh ưu đãi thất bại')
  } finally {
    running.value = false
  }
}

// ----- Thành viên theo hạng -----
const tier = ref('')
const page = ref(0)
const members = ref(null) // PageResponse
const membersLoading = ref(false)

async function loadMembers() {
  membersLoading.value = true
  try {
    members.value = await adminListMembers({ tier: tier.value || undefined, page: page.value, size: 10 })
  } catch (e) {
    runError.value = getApiMessage(e, 'Không tải được danh sách thành viên')
  } finally {
    membersLoading.value = false
  }
}

function changeTier(value) {
  tier.value = value
  page.value = 0
  loadMembers()
}
function goPage(delta) {
  page.value += delta
  loadMembers()
}

onMounted(loadMembers)
</script>

<template>
  <div class="space-y-6">
    <h1 class="text-xl font-semibold text-gray-900">Ưu đãi & Thành viên</h1>

    <!-- Chạy OfferEngine -->
    <section class="rounded-lg border border-gray-200 bg-white p-5">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 class="font-medium text-gray-900">Sinh ưu đãi cá nhân hóa</h2>
          <p class="mt-1 text-sm text-gray-500">
            Chạy các quy tắc: sinh nhật · win-back · thể loại yêu thích · quà lên hạng. An toàn khi chạy lại (idempotent).
          </p>
        </div>
        <BaseButton :loading="running" @click="runOffers">Chạy sinh ưu đãi</BaseButton>
      </div>

      <p v-if="runError" class="mt-3 rounded-md border border-danger/40 bg-danger/10 px-3 py-2 text-sm text-gray-700">
        {{ runError }}
      </p>

      <div v-if="runResult" class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-5">
        <div v-for="stat in [
          { label: 'Sinh nhật', value: runResult.birthday },
          { label: 'Win-back', value: runResult.winback },
          { label: 'Thể loại', value: runResult.genreFavorite },
          { label: 'Lên hạng', value: runResult.tierUp },
          { label: 'Tổng cộng', value: runTotal, highlight: true },
        ]" :key="stat.label" class="rounded-lg border border-gray-200 p-3 text-center"
          :class="stat.highlight ? 'bg-brand-500/5' : ''">
          <p class="text-2xl font-semibold tabular-nums" :class="stat.highlight ? 'text-brand-500' : 'text-gray-900'">
            {{ stat.value }}
          </p>
          <p class="mt-0.5 text-xs text-gray-500">{{ stat.label }}</p>
        </div>
      </div>
    </section>

    <!-- Thành viên theo hạng -->
    <section class="rounded-lg border border-gray-200 bg-white p-5">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <h2 class="font-medium text-gray-900">Thành viên</h2>
        <div class="flex gap-1">
          <button
            v-for="f in TIER_FILTERS"
            :key="f.value"
            type="button"
            class="rounded-md px-3 py-1.5 text-sm transition-colors"
            :class="tier === f.value ? 'bg-brand-500 text-white' : 'text-gray-600 hover:bg-gray-100'"
            @click="changeTier(f.value)"
          >
            {{ f.label }}
          </button>
        </div>
      </div>

      <div class="mt-4 overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-200 text-left text-gray-500">
              <th class="py-2 pr-4 font-medium">Thành viên</th>
              <th class="py-2 pr-4 font-medium">Hạng</th>
              <th class="py-2 pr-4 font-medium text-right">Điểm khả dụng</th>
              <th class="py-2 pr-4 font-medium text-right">Tích lũy</th>
              <th class="py-2 font-medium">Tham gia</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in members?.content" :key="m.id" class="border-b border-gray-100">
              <td class="py-2.5 pr-4">
                <p class="font-medium text-gray-900">{{ m.fullName }}</p>
                <p class="text-xs text-gray-500">{{ m.email }}</p>
              </td>
              <td class="py-2.5 pr-4">
                <BaseBadge :variant="tierVariant(m.tier)">{{ tierLabel(m.tier) }}</BaseBadge>
              </td>
              <td class="py-2.5 pr-4 text-right tabular-nums text-gray-900">{{ m.pointsBalance.toLocaleString('vi-VN') }}</td>
              <td class="py-2.5 pr-4 text-right tabular-nums text-gray-500">{{ m.lifetimePoints.toLocaleString('vi-VN') }}</td>
              <td class="py-2.5 text-gray-500">{{ formatDate(m.createdAt) }}</td>
            </tr>
            <tr v-if="members && !members.content.length">
              <td colspan="5" class="py-8 text-center text-gray-400">Chưa có thành viên nào ở hạng này.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="members && members.totalPages > 1" class="mt-4 flex items-center justify-between text-sm text-gray-500">
        <span>Trang {{ members.page + 1 }} / {{ members.totalPages }}</span>
        <div class="flex gap-2">
          <BaseButton variant="ghost" size="md" :disabled="members.page === 0 || membersLoading" @click="goPage(-1)">
            Trước
          </BaseButton>
          <BaseButton
            variant="ghost"
            size="md"
            :disabled="members.page >= members.totalPages - 1 || membersLoading"
            @click="goPage(1)"
          >
            Sau
          </BaseButton>
        </div>
      </div>
    </section>
  </div>
</template>
