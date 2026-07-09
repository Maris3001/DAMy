<script setup>
// Thẻ voucher tái dùng: ví voucher (hiện trạng thái) + chọn phiếu ở bước thanh toán (selectable).
import { computed } from 'vue'
import { formatVnd, formatDate } from '../utils/format'
import { voucherDiscountLabel, VOUCHER_STATUS } from '../utils/voucher'
import BaseBadge from './ui/BaseBadge.vue'

const props = defineProps({
  voucher: { type: Object, required: true }, // UserVoucherResponse
  selectable: { type: Boolean, default: false },
  selected: { type: Boolean, default: false },
})

const emit = defineEmits(['select'])

const discountLabel = computed(() => voucherDiscountLabel(props.voucher))
const statusMeta = computed(() => VOUCHER_STATUS[props.voucher.status] ?? null)
</script>

<template>
  <component
    :is="selectable ? 'button' : 'div'"
    :type="selectable ? 'button' : undefined"
    class="block w-full rounded-lg border bg-surface-800 p-4 text-left transition"
    :class="[
      selected ? 'border-brand-500 ring-1 ring-brand-500' : 'border-white/5',
      selectable ? 'hover:border-brand-500/60 cursor-pointer' : '',
    ]"
    @click="selectable && emit('select', voucher)"
  >
    <div class="flex items-start justify-between gap-3">
      <div class="min-w-0">
        <p class="truncate font-medium text-ink-100">{{ voucher.name }}</p>
        <p class="mt-0.5 text-sm font-semibold text-brand-500">{{ discountLabel }}</p>
      </div>
      <BaseBadge v-if="statusMeta && !selectable" :variant="statusMeta.variant">
        {{ statusMeta.label }}
      </BaseBadge>
      <span v-else-if="selected" class="shrink-0 text-brand-500" aria-hidden="true">✓</span>
    </div>

    <p v-if="voucher.description" class="mt-2 line-clamp-2 text-sm text-ink-500">
      {{ voucher.description }}
    </p>

    <dl class="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-ink-500">
      <div v-if="voucher.minOrderAmount > 0">
        <dt class="inline">Đơn tối thiểu:</dt>
        <dd class="inline text-ink-300">{{ formatVnd(voucher.minOrderAmount) }}</dd>
      </div>
      <div v-if="voucher.validTo">
        <dt class="inline">HSD:</dt>
        <dd class="inline text-ink-300">{{ formatDate(voucher.validTo) }}</dd>
      </div>
      <div class="font-mono text-ink-500">{{ voucher.code }}</div>
    </dl>
  </component>
</template>
