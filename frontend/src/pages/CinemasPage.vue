<script setup>
// Trang "Rạp": tab khu vực + card rạp. Bấm "Đặt vé" điền sẵn khu vực/rạp vào bookingStore
// rồi vào thẳng bước 3 (chọn suất chiếu) của wizard đặt vé.
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listRegions, listCinemas } from '../api/catalog'
import { getApiMessage } from '../api/http'
import { useAuthStore } from '../stores/auth'
import { useBookingStore } from '../stores/booking'
import BaseButton from '../components/ui/BaseButton.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import ErrorState from '../components/ui/ErrorState.vue'

const router = useRouter()
const auth = useAuthStore()
const bookingStore = useBookingStore()
const isAdmin = computed(() => auth.isAdmin)

const state = ref('loading') // loading | ok | error
const errorMsg = ref('')
const regions = ref([])
const cinemas = ref([])
const selectedRegionId = ref(null) // null = tất cả

const filtered = computed(() =>
  selectedRegionId.value == null
    ? cinemas.value
    : cinemas.value.filter((c) => c.regionId === selectedRegionId.value),
)

function pick(c) {
  if (isAdmin.value) return
  bookingStore.setRegion({ id: c.regionId, name: c.regionName })
  bookingStore.setCinema({ id: c.id, name: c.name, address: c.address, chainName: c.chainName })
  router.push({ name: 'booking-showtime' })
}

onMounted(async () => {
  try {
    ;[regions.value, cinemas.value] = await Promise.all([listRegions(), listCinemas()])
    state.value = 'ok'
  } catch (e) {
    errorMsg.value = getApiMessage(e, 'Không tải được danh sách rạp')
    state.value = 'error'
  }
})
</script>

<template>
  <main class="mx-auto max-w-6xl px-4 py-8">
    <h1 class="text-2xl font-semibold text-ink-100">Hệ thống rạp</h1>

    <LoadingState v-if="state === 'loading'" message="Đang tải danh sách rạp…" class="mt-8" />
    <ErrorState v-else-if="state === 'error'" :message="errorMsg" class="mt-8" />

    <template v-else>
      <p
        v-if="isAdmin"
        class="mt-4 rounded-md border border-warning/40 bg-warning/10 px-3 py-2.5 text-sm text-ink-100"
      >
        Bạn đang đăng nhập bằng tài khoản quản trị nên không thể đặt vé.
      </p>

      <div class="mt-4 flex flex-wrap gap-2">
        <button
          type="button"
          class="rounded-full border px-4 py-1.5 text-sm transition-colors duration-150"
          :class="
            selectedRegionId == null
              ? 'border-brand-500 bg-brand-500/10 text-brand-500'
              : 'border-surface-700 text-ink-300 hover:border-brand-500/40'
          "
          @click="selectedRegionId = null"
        >
          Tất cả
        </button>
        <button
          v-for="r in regions"
          :key="r.id"
          type="button"
          class="rounded-full border px-4 py-1.5 text-sm transition-colors duration-150"
          :class="
            selectedRegionId === r.id
              ? 'border-brand-500 bg-brand-500/10 text-brand-500'
              : 'border-surface-700 text-ink-300 hover:border-brand-500/40'
          "
          @click="selectedRegionId = r.id"
        >
          {{ r.name }}
        </button>
      </div>

      <EmptyState
        v-if="filtered.length === 0"
        class="mt-8"
        icon="🏢"
        title="Chưa có rạp"
        message="Vui lòng quay lại sau nhé."
      />
      <div v-else class="mt-6 grid gap-3 md:grid-cols-2">
        <div
          v-for="c in filtered"
          :key="c.id"
          class="flex items-start justify-between gap-4 rounded-lg border border-white/5 bg-surface-800 p-4"
        >
          <div>
            <p class="font-medium text-ink-100">{{ c.name }}</p>
            <p class="mt-1 text-sm text-ink-500">{{ c.chainName }} · {{ c.regionName }}</p>
            <p class="mt-1 text-sm text-ink-500">{{ c.address }}</p>
          </div>
          <BaseButton :disabled="isAdmin" @click="pick(c)">Đặt vé</BaseButton>
        </div>
      </div>
    </template>
  </main>
</template>
