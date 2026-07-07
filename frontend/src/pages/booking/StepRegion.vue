<script setup>
// Bước 1: chọn khu vực (grid card tỉnh/thành).
import { onMounted, ref } from 'vue'
import { listRegions } from '../../api/catalog'
import { useBookingStore } from '../../stores/booking'
import LoadingState from '../../components/ui/LoadingState.vue'
import ErrorState from '../../components/ui/ErrorState.vue'
import EmptyState from '../../components/ui/EmptyState.vue'

const booking = useBookingStore()

const state = ref('loading') // loading | ok | error
const regions = ref([])

function pick(region) {
  if (booking.region?.id !== region.id) {
    booking.setRegion({ id: region.id, name: region.name })
  }
}

onMounted(async () => {
  try {
    regions.value = await listRegions()
    state.value = 'ok'
  } catch {
    state.value = 'error'
  }
})
</script>

<template>
  <section>
    <h1 class="text-2xl font-semibold text-ink-100">Bạn muốn xem phim ở đâu?</h1>
    <p class="mt-1 text-sm text-ink-500">Chọn tỉnh/thành để xem các rạp gần bạn.</p>

    <LoadingState v-if="state === 'loading'" message="Đang tải khu vực…" />
    <ErrorState v-else-if="state === 'error'" message="Không tải được danh sách khu vực, vui lòng thử lại." />
    <EmptyState v-else-if="regions.length === 0" icon="📍" title="Chưa có khu vực nào" />

    <div v-else class="mt-6 grid grid-cols-2 gap-3 md:grid-cols-4">
      <button
        v-for="r in regions"
        :key="r.id"
        type="button"
        class="rounded-lg border p-5 text-center transition-colors duration-150"
        :class="
          booking.region?.id === r.id
            ? 'border-brand-500 bg-brand-500/10 text-brand-500'
            : 'border-white/5 bg-surface-800 text-ink-100 hover:border-brand-500/40'
        "
        @click="pick(r)"
      >
        <span class="block text-2xl" aria-hidden="true">📍</span>
        <span class="mt-2 block font-medium">{{ r.name }}</span>
      </button>
    </div>
  </section>
</template>
