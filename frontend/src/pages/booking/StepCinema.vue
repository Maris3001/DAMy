<script setup>
// Bước 2: chọn hãng rạp (tab) & rạp (card).
import { computed, onMounted, ref } from 'vue'
import { listCinemas } from '../../api/catalog'
import { useBookingStore } from '../../stores/booking'
import LoadingState from '../../components/ui/LoadingState.vue'
import ErrorState from '../../components/ui/ErrorState.vue'
import EmptyState from '../../components/ui/EmptyState.vue'

const booking = useBookingStore()

const state = ref('loading') // loading | ok | error
const cinemas = ref([])
const selectedChain = ref(null) // null = tất cả

const chains = computed(() => {
  const seen = new Map()
  for (const c of cinemas.value) {
    if (!seen.has(c.chainId)) seen.set(c.chainId, { id: c.chainId, name: c.chainName })
  }
  return [...seen.values()]
})

const filtered = computed(() =>
  selectedChain.value == null
    ? cinemas.value
    : cinemas.value.filter((c) => c.chainId === selectedChain.value),
)

function pick(cinema) {
  if (booking.cinema?.id !== cinema.id) {
    booking.setCinema({
      id: cinema.id,
      name: cinema.name,
      address: cinema.address,
      chainName: cinema.chainName,
    })
  }
}

onMounted(async () => {
  try {
    cinemas.value = await listCinemas(booking.region?.id)
    state.value = 'ok'
  } catch {
    state.value = 'error'
  }
})
</script>

<template>
  <section>
    <h1 class="text-2xl font-semibold text-ink-100">Chọn rạp tại {{ booking.region?.name }}</h1>

    <LoadingState v-if="state === 'loading'" message="Đang tải danh sách rạp…" />
    <ErrorState v-else-if="state === 'error'" message="Không tải được danh sách rạp, vui lòng thử lại." />
    <EmptyState
      v-else-if="cinemas.length === 0"
      icon="🏢"
      title="Khu vực này chưa có rạp"
      message="Vui lòng quay lại chọn khu vực khác."
    />

    <template v-else>
      <!-- Tab hãng rạp -->
      <div class="mt-4 flex gap-2 overflow-x-auto pb-2">
        <button
          type="button"
          class="shrink-0 rounded-full border px-4 py-1.5 text-sm transition-colors duration-150"
          :class="
            selectedChain == null
              ? 'border-brand-500 bg-brand-500/10 text-brand-500'
              : 'border-surface-700 text-ink-300 hover:border-brand-500/40'
          "
          @click="selectedChain = null"
        >
          Tất cả
        </button>
        <button
          v-for="chain in chains"
          :key="chain.id"
          type="button"
          class="shrink-0 rounded-full border px-4 py-1.5 text-sm transition-colors duration-150"
          :class="
            selectedChain === chain.id
              ? 'border-brand-500 bg-brand-500/10 text-brand-500'
              : 'border-surface-700 text-ink-300 hover:border-brand-500/40'
          "
          @click="selectedChain = chain.id"
        >
          {{ chain.name }}
        </button>
      </div>

      <!-- Danh sách rạp -->
      <div class="mt-4 grid gap-3 md:grid-cols-2">
        <button
          v-for="c in filtered"
          :key="c.id"
          type="button"
          class="rounded-lg border p-4 text-left transition-colors duration-150"
          :class="
            booking.cinema?.id === c.id
              ? 'border-brand-500 bg-brand-500/10'
              : 'border-white/5 bg-surface-800 hover:border-brand-500/40'
          "
          @click="pick(c)"
        >
          <p class="font-medium" :class="booking.cinema?.id === c.id ? 'text-brand-500' : 'text-ink-100'">
            {{ c.name }}
          </p>
          <p class="mt-1 text-sm text-ink-500">{{ c.chainName }} · {{ c.address }}</p>
        </button>
      </div>
    </template>
  </section>
</template>
