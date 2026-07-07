<script setup>
// Bước 5 (P-05): bắp nước — nhóm Combo/Món lẻ, stepper số lượng 0..10, bỏ qua được.
import { computed, onMounted, ref } from 'vue'
import { listConcessions } from '../../api/catalog'
import { useBookingStore } from '../../stores/booking'
import { formatVnd } from '../../utils/format'
import LoadingState from '../../components/ui/LoadingState.vue'
import ErrorState from '../../components/ui/ErrorState.vue'
import EmptyState from '../../components/ui/EmptyState.vue'

const MAX_QTY = 10

const booking = useBookingStore()

const state = ref('loading') // loading | ok | error
const items = ref([])

const groups = computed(() => {
  const active = items.value.filter((c) => c.active)
  return [
    { label: 'Combo', items: active.filter((c) => c.category === 'COMBO') },
    { label: 'Món lẻ', items: active.filter((c) => c.category === 'SINGLE') },
  ].filter((g) => g.items.length > 0)
})

function qtyOf(item) {
  return booking.concessions.find((c) => c.id === item.id)?.qty ?? 0
}

function changeQty(item, delta) {
  const next = Math.min(MAX_QTY, Math.max(0, qtyOf(item) + delta))
  booking.setConcessionQty(item, next)
}

onMounted(async () => {
  try {
    items.value = await listConcessions()
    state.value = 'ok'
  } catch {
    state.value = 'error'
  }
})
</script>

<template>
  <section>
    <h1 class="text-2xl font-semibold text-ink-100">Thêm bắp nước</h1>
    <p class="mt-1 text-sm text-ink-500">Không bắt buộc — bạn có thể bấm Tiếp tục để bỏ qua.</p>

    <LoadingState v-if="state === 'loading'" message="Đang tải thực đơn…" />
    <ErrorState v-else-if="state === 'error'" message="Không tải được thực đơn, vui lòng thử lại." />
    <EmptyState v-else-if="groups.length === 0" icon="🍿" title="Chưa có món nào" />

    <div v-else class="mt-6 grid gap-6 lg:grid-cols-[1fr_300px]">
      <div class="space-y-6">
        <div v-for="group in groups" :key="group.label">
          <h2 class="mb-3 text-lg font-medium text-ink-100">{{ group.label }}</h2>
          <div class="space-y-3">
            <div
              v-for="item in group.items"
              :key="item.id"
              class="flex items-center gap-4 rounded-lg border border-white/5 bg-surface-800 p-3"
            >
              <div class="size-20 shrink-0 overflow-hidden rounded-md bg-surface-900">
                <img
                  v-if="item.imageUrl"
                  :src="item.imageUrl"
                  :alt="item.name"
                  class="h-full w-full object-cover"
                />
                <div v-else class="flex h-full items-center justify-center text-2xl" aria-hidden="true">🍿</div>
              </div>
              <div class="min-w-0 flex-1">
                <p class="font-medium text-ink-100">{{ item.name }}</p>
                <p v-if="item.description" class="mt-0.5 line-clamp-2 text-sm text-ink-500">
                  {{ item.description }}
                </p>
                <p class="mt-1 text-sm font-medium text-brand-500 tabular-nums">{{ formatVnd(item.price) }}</p>
              </div>
              <!-- Stepper số lượng -->
              <div class="flex items-center gap-2">
                <button
                  type="button"
                  class="flex size-8 items-center justify-center rounded-md border border-surface-700 text-ink-300 transition-colors duration-150 hover:border-brand-500/60 disabled:cursor-not-allowed disabled:opacity-40"
                  :disabled="qtyOf(item) === 0"
                  :aria-label="`Bớt ${item.name}`"
                  @click="changeQty(item, -1)"
                >
                  −
                </button>
                <span class="w-6 text-center tabular-nums text-ink-100">{{ qtyOf(item) }}</span>
                <button
                  type="button"
                  class="flex size-8 items-center justify-center rounded-md border border-surface-700 text-ink-300 transition-colors duration-150 hover:border-brand-500/60 disabled:cursor-not-allowed disabled:opacity-40"
                  :disabled="qtyOf(item) >= MAX_QTY"
                  :aria-label="`Thêm ${item.name}`"
                  @click="changeQty(item, 1)"
                >
                  +
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Panel tóm tắt -->
      <aside class="h-fit rounded-lg border border-white/5 bg-surface-800 p-4">
        <p class="font-medium text-ink-100">Tóm tắt đơn</p>
        <ul class="mt-3 space-y-1.5 text-sm">
          <li class="flex justify-between text-ink-300">
            <span>Tiền vé ({{ booking.seats.length }} ghế)</span>
            <span class="tabular-nums">{{ formatVnd(booking.ticketTotal) }}</span>
          </li>
          <li
            v-for="c in booking.concessions"
            :key="c.id"
            class="flex justify-between text-ink-300"
          >
            <span>{{ c.name }} ×{{ c.qty }}</span>
            <span class="tabular-nums">{{ formatVnd(c.price * c.qty) }}</span>
          </li>
        </ul>
        <div class="mt-3 flex justify-between border-t border-white/5 pt-3">
          <span class="text-ink-300">Tạm tính</span>
          <span class="font-semibold text-brand-500 tabular-nums">{{ formatVnd(booking.grandTotal) }}</span>
        </div>
      </aside>
    </div>
  </section>
</template>
