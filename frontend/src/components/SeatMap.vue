<script setup>
// Sơ đồ ghế (DESIGN.md §1.2 — bắt buộc kèm chú giải): trống viền surface-700, MINE nền brand-500
// chữ đen, người khác giữ = vân sọc chéo + icon (dễ phân biệt với ghế trống), đã bán mờ 50% + ✕,
// VIP viền brand-500, ghế đôi rộng gấp đôi viền crimson-500. Bấm ghế emit 'toggle'.
import { computed } from 'vue'

const props = defineProps({
  // [{ seatId, rowLabel, colNumber, seatType, state: AVAILABLE|MINE|HELD|BOOKED, price }]
  seats: { type: Array, required: true },
})

const emit = defineEmits(['toggle'])

// Vân sọc chéo cho ghế người khác đang giữ — mẫu "đang bận" dễ đọc, khác hẳn ghế trống.
const HELD_STRIPES =
  'repeating-linear-gradient(45deg, rgba(255,255,255,0.10) 0 2px, transparent 2px 5px)'

const rows = computed(() => {
  const byRow = new Map()
  for (const s of props.seats) {
    if (!byRow.has(s.rowLabel)) byRow.set(s.rowLabel, [])
    byRow.get(s.rowLabel).push(s)
  }
  return [...byRow.entries()].map(([label, seats]) => ({ label, seats }))
})

function seatClass(seat) {
  const base = [
    'relative flex h-8 items-center justify-center rounded-md border text-[11px] md:h-9 md:text-xs',
    'transition-colors duration-150 tabular-nums',
    seat.seatType === 'COUPLE' ? 'w-11 md:w-12' : 'w-8 md:w-9',
  ]
  switch (seat.state) {
    case 'MINE':
      return [...base, 'border-brand-500 bg-brand-500 font-semibold text-black cursor-pointer']
    case 'HELD':
      return [...base, 'border-ink-500/50 bg-surface-950 text-ink-500 cursor-not-allowed']
    case 'BOOKED':
      return [...base, 'border-surface-800 bg-surface-800 text-ink-500 opacity-50 cursor-not-allowed']
    default: // AVAILABLE
      return [
        ...base,
        seat.seatType === 'COUPLE'
          ? 'border-crimson-500 text-ink-300 hover:bg-crimson-500/15 cursor-pointer'
          : seat.seatType === 'VIP'
            ? 'border-brand-500 text-ink-300 hover:bg-brand-500/15 cursor-pointer'
            : 'border-surface-700 text-ink-300 hover:border-brand-500/60 cursor-pointer',
      ]
  }
}

function seatStyle(seat) {
  return seat.state === 'HELD' ? { backgroundImage: HELD_STRIPES } : {}
}
</script>

<template>
  <div>
    <!-- Màn hình -->
    <div class="mx-auto max-w-md">
      <div class="h-3 rounded-t-[50%] border-t-2 border-brand-500" aria-hidden="true" />
      <p class="pt-1 text-center text-sm tracking-[0.4em] text-ink-500">MÀN HÌNH</p>
    </div>

    <!-- Lưới ghế: các hàng căn giữa; cuộn ngang trong container riêng trên mobile -->
    <div class="mt-6 overflow-x-auto pb-2">
      <div class="mx-auto w-max space-y-1.5">
        <div v-for="row in rows" :key="row.label" class="flex items-center justify-center gap-1.5">
          <span class="w-5 shrink-0 text-center text-sm text-ink-500">{{ row.label }}</span>
          <button
            v-for="seat in row.seats"
            :key="seat.seatId"
            type="button"
            :class="seatClass(seat)"
            :style="seatStyle(seat)"
            :disabled="seat.state === 'HELD' || seat.state === 'BOOKED'"
            :aria-label="`Ghế ${seat.rowLabel}${seat.colNumber}`"
            @click="emit('toggle', seat)"
          >
            <span v-if="seat.state === 'BOOKED'" aria-hidden="true">✕</span>
            <span v-else-if="seat.state === 'HELD'" aria-hidden="true">🔒</span>
            <span v-else>{{ seat.colNumber }}</span>
          </button>
          <span class="w-5 shrink-0 text-center text-sm text-ink-500">{{ row.label }}</span>
        </div>
      </div>
    </div>

    <!-- Chú giải -->
    <div class="mt-5 flex flex-wrap items-center justify-center gap-x-4 gap-y-2 text-sm text-ink-300">
      <span class="flex items-center gap-1.5">
        <span class="size-4 rounded border border-surface-700" aria-hidden="true" /> Trống
      </span>
      <span class="flex items-center gap-1.5">
        <span class="size-4 rounded bg-brand-500" aria-hidden="true" /> Đang chọn
      </span>
      <span class="flex items-center gap-1.5">
        <span
          class="size-4 rounded border border-ink-500/50 bg-surface-950"
          :style="{ backgroundImage: HELD_STRIPES }"
          aria-hidden="true"
        />
        Có người giữ
      </span>
      <span class="flex items-center gap-1.5">
        <span class="flex size-4 items-center justify-center rounded bg-surface-800 text-[9px] text-ink-500 opacity-70" aria-hidden="true">✕</span>
        Đã bán
      </span>
      <span class="flex items-center gap-1.5">
        <span class="size-4 rounded border border-brand-500" aria-hidden="true" /> VIP
      </span>
      <span class="flex items-center gap-1.5">
        <span class="h-4 w-6 rounded border border-crimson-500" aria-hidden="true" /> Ghế đôi
      </span>
    </div>
  </div>
</template>
