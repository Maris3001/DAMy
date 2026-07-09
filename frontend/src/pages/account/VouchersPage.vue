<script setup>
// Tab "Ví voucher": danh sách phiếu của user + đổi điểm lấy voucher.
import { onMounted, ref } from 'vue'
import { getMyVouchers, getRedeemableCampaigns, redeemVoucher } from '../../api/voucher'
import { getApiMessage } from '../../api/http'
import { useAuthStore } from '../../stores/auth'
import { formatVnd } from '../../utils/format'
import { voucherDiscountLabel } from '../../utils/voucher'
import { tierLabel } from '../../utils/tier'
import BaseButton from '../../components/ui/BaseButton.vue'
import LoadingState from '../../components/ui/LoadingState.vue'
import EmptyState from '../../components/ui/EmptyState.vue'
import ErrorState from '../../components/ui/ErrorState.vue'
import VoucherCard from '../../components/VoucherCard.vue'

const auth = useAuthStore()

const state = ref('loading') // loading | ok | error
const errorMsg = ref('')
const vouchers = ref([])
const redeemables = ref([])
const redeemingId = ref(null)
const notice = ref(null) // { type: 'success' | 'error', text }

async function load() {
  state.value = 'loading'
  try {
    const [v, r] = await Promise.all([getMyVouchers(), getRedeemableCampaigns()])
    vouchers.value = v
    redeemables.value = r
    state.value = 'ok'
  } catch (e) {
    errorMsg.value = getApiMessage(e, 'Không tải được ví voucher.')
    state.value = 'error'
  }
}

async function redeem(campaign) {
  redeemingId.value = campaign.id
  notice.value = null
  try {
    await redeemVoucher(campaign.id)
    notice.value = { type: 'success', text: `Đã đổi thành công voucher "${campaign.name}".` }
    await Promise.all([load(), auth.fetchMe()]) // làm mới ví + số dư điểm
  } catch (e) {
    notice.value = { type: 'error', text: getApiMessage(e, 'Không đổi được voucher, vui lòng thử lại.') }
  } finally {
    redeemingId.value = null
  }
}

onMounted(load)
</script>

<template>
  <LoadingState v-if="state === 'loading'" message="Đang tải ví voucher…" />
  <ErrorState v-else-if="state === 'error'" :message="errorMsg" />

  <div v-else class="space-y-8">
    <p
      v-if="notice"
      class="rounded-md border px-3 py-2.5 text-sm"
      :class="notice.type === 'success'
        ? 'border-success/40 bg-success/10 text-ink-100'
        : 'border-danger/40 bg-danger/10 text-ink-100'"
    >
      {{ notice.text }}
    </p>

    <!-- ===== Voucher của tôi ===== -->
    <section>
      <h2 class="text-sm font-medium text-ink-300">Voucher của tôi</h2>
      <EmptyState
        v-if="!vouchers.length"
        class="mt-3"
        icon="🎫"
        title="Chưa có voucher nào"
        message="Đổi điểm lấy voucher bên dưới, hoặc nhận ưu đãi cá nhân hóa từ hệ thống."
      />
      <div v-else class="mt-3 grid gap-3 sm:grid-cols-2">
        <VoucherCard v-for="v in vouchers" :key="v.id" :voucher="v" />
      </div>
    </section>

    <!-- ===== Đổi điểm lấy voucher ===== -->
    <section>
      <div class="flex items-center justify-between">
        <h2 class="text-sm font-medium text-ink-300">Đổi điểm lấy voucher</h2>
        <p class="text-sm text-ink-500">
          Điểm khả dụng:
          <span class="font-semibold text-brand-500 tabular-nums">
            {{ (auth.user?.pointsBalance ?? 0).toLocaleString('vi-VN') }}
          </span>
        </p>
      </div>

      <EmptyState
        v-if="!redeemables.length"
        class="mt-3"
        icon="✨"
        title="Chưa có ưu đãi đổi điểm"
        message="Hãy quay lại sau, hệ thống sẽ mở thêm ưu đãi đổi điểm."
      />
      <div v-else class="mt-3 grid gap-3 sm:grid-cols-2">
        <div
          v-for="c in redeemables"
          :key="c.id"
          class="flex flex-col rounded-lg border border-white/5 bg-surface-800 p-4"
        >
          <p class="font-medium text-ink-100">{{ c.name }}</p>
          <p class="mt-0.5 text-sm font-semibold text-brand-500">{{ voucherDiscountLabel(c) }}</p>
          <p v-if="c.description" class="mt-2 text-sm text-ink-500">{{ c.description }}</p>

          <dl class="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-ink-500">
            <div v-if="c.minOrderAmount > 0">
              <dt class="inline">Đơn tối thiểu:</dt>
              <dd class="inline text-ink-300">{{ formatVnd(c.minOrderAmount) }}</dd>
            </div>
            <div v-if="c.minTier">
              <dt class="inline">Hạng:</dt>
              <dd class="inline text-ink-300">{{ tierLabel(c.minTier) }}+</dd>
            </div>
          </dl>

          <div class="mt-4 flex items-center justify-between gap-2 border-t border-white/5 pt-3">
            <span class="text-sm text-ink-300">
              <span class="font-semibold text-ink-100 tabular-nums">{{ c.pointsCost.toLocaleString('vi-VN') }}</span> điểm
            </span>
            <BaseButton
              size="md"
              :disabled="!c.eligible"
              :loading="redeemingId === c.id"
              @click="redeem(c)"
            >
              {{ c.eligible ? 'Đổi ngay' : c.reason || 'Không đủ điều kiện' }}
            </BaseButton>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
