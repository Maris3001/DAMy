<script setup>
// Tab "Điểm thưởng": số điểm hiện có, tiến độ lên hạng, quyền lợi từng hạng, lịch sử điểm.
import { computed, onMounted, ref } from 'vue'
import { getLoyaltySummary, getPointHistory } from '../../api/loyalty'
import { getApiMessage } from '../../api/http'
import { formatDate, formatTime } from '../../utils/format'
import { TIER_ORDER, TIERS, tierLabel, tierVariant } from '../../utils/tier'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import FilterChips from '../../components/ui/FilterChips.vue'
import LoadingState from '../../components/ui/LoadingState.vue'
import EmptyState from '../../components/ui/EmptyState.vue'
import ErrorState from '../../components/ui/ErrorState.vue'
import TierProgressBar from '../../components/TierProgressBar.vue'

const state = ref('loading') // loading | ok | error
const errorMsg = ref('')
const summary = ref(null)
const history = ref([])
const txnFilter = ref('ALL') // lọc lịch sử điểm client-side: cộng (delta≥0) / trừ (delta<0)

const TXN = {
  EARN: { sign: '+', label: 'Tích điểm' },
  REDEEM: { sign: '−', label: 'Đổi điểm' },
  ADJUST: { sign: '', label: 'Điều chỉnh' },
}

const filterOptions = computed(() => {
  const earned = history.value.filter((t) => t.delta >= 0).length
  return [
    { value: 'ALL', label: 'Tất cả', count: history.value.length },
    { value: 'EARN', label: 'Cộng điểm', count: earned },
    { value: 'REDEEM', label: 'Trừ điểm', count: history.value.length - earned },
  ]
})

const filteredHistory = computed(() => {
  if (txnFilter.value === 'EARN') return history.value.filter((t) => t.delta >= 0)
  if (txnFilter.value === 'REDEEM') return history.value.filter((t) => t.delta < 0)
  return history.value
})

function signedDelta(tx) {
  const meta = TXN[tx.type] || {}
  return `${meta.sign || (tx.delta >= 0 ? '+' : '')}${Math.abs(tx.delta).toLocaleString('vi-VN')}`
}

async function load() {
  state.value = 'loading'
  try {
    const [s, h] = await Promise.all([getLoyaltySummary(), getPointHistory()])
    summary.value = s
    history.value = h
    state.value = 'ok'
  } catch (e) {
    errorMsg.value = getApiMessage(e, 'Không tải được điểm thưởng.')
    state.value = 'error'
  }
}

onMounted(load)
</script>

<template>
  <LoadingState v-if="state === 'loading'" message="Đang tải điểm thưởng…" />
  <ErrorState v-else-if="state === 'error'" :message="errorMsg" />

  <div v-else-if="summary" class="space-y-6">
    <!-- ===== Thẻ điểm + hạng ===== -->
    <div class="rounded-lg border border-white/5 bg-surface-800 p-6">
      <div class="flex items-start justify-between gap-4">
        <div>
          <p class="text-sm text-ink-500">Điểm khả dụng</p>
          <p class="mt-1 text-4xl font-semibold text-brand-500 tabular-nums">
            {{ summary.pointsBalance.toLocaleString('vi-VN') }}
          </p>
          <p class="mt-1 text-sm text-ink-500">
            Tích lũy trọn đời: <span class="tabular-nums text-ink-300">{{ summary.lifetimePoints.toLocaleString('vi-VN') }}</span>
          </p>
        </div>
        <div class="text-right">
          <p class="text-sm text-ink-500">Hạng của bạn</p>
          <div class="mt-1">
            <BaseBadge :variant="tierVariant(summary.tier)">{{ tierLabel(summary.tier) }}</BaseBadge>
          </div>
        </div>
      </div>

      <div class="mt-6 border-t border-white/5 pt-5">
        <TierProgressBar :summary="summary" />
      </div>
    </div>

    <!-- ===== Quyền lợi từng hạng ===== -->
    <div>
      <h2 class="text-sm font-medium text-ink-300">Quyền lợi thành viên</h2>
      <div class="mt-3 grid gap-3 sm:grid-cols-3">
        <div
          v-for="t in TIER_ORDER"
          :key="t"
          class="rounded-lg border p-4"
          :class="t === summary.tier ? 'border-brand-500/40 bg-surface-800' : 'border-white/5 bg-surface-900'"
        >
          <BaseBadge :variant="tierVariant(t)">{{ tierLabel(t) }}</BaseBadge>
          <ul class="mt-3 space-y-1.5 text-sm text-ink-300">
            <li v-for="b in TIERS[t].benefits" :key="b" class="flex gap-2">
              <span class="text-brand-500" aria-hidden="true">✓</span>
              <span>{{ b }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- ===== Lịch sử điểm ===== -->
    <div>
      <h2 class="text-sm font-medium text-ink-300">Lịch sử điểm</h2>
      <EmptyState
        v-if="!history.length"
        class="mt-3"
        title="Chưa có giao dịch điểm"
        message="Điểm sẽ được cộng sau mỗi lần thanh toán vé thành công."
      />
      <template v-else>
        <FilterChips v-model="txnFilter" :options="filterOptions" class="mt-3" />
        <EmptyState
          v-if="!filteredHistory.length"
          class="mt-3"
          title="Không có giao dịch phù hợp"
          message="Không có giao dịch nào ở nhóm này."
        />
        <ul v-else class="mt-3 space-y-2">
        <li
          v-for="tx in filteredHistory"
          :key="tx.id"
          class="flex items-center justify-between gap-4 rounded-lg border border-white/5 bg-surface-800 px-4 py-3"
        >
          <div class="min-w-0">
            <p class="truncate text-sm text-ink-100">{{ tx.description || (TXN[tx.type] || {}).label || tx.type }}</p>
            <p class="mt-0.5 text-xs text-ink-500">{{ formatDate(tx.createdAt) }} · {{ formatTime(tx.createdAt) }}</p>
          </div>
          <div class="text-right">
            <p
              class="font-semibold tabular-nums"
              :class="tx.delta >= 0 ? 'text-success' : 'text-danger'"
            >
              {{ signedDelta(tx) }}
            </p>
            <p class="text-xs text-ink-500 tabular-nums">Số dư: {{ tx.balanceAfter.toLocaleString('vi-VN') }}</p>
          </div>
        </li>
        </ul>
      </template>
    </div>
  </div>
</template>
