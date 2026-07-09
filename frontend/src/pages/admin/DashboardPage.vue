<script setup>
// Dashboard admin (P8): 4 stat card (±% so hôm qua), biểu đồ doanh thu 30 ngày,
// top 5 phim theo doanh thu, donut phân bố hạng thành viên. Light theme.
import { computed, onMounted, ref } from 'vue'
import { adminGetDashboard } from '../../api/admin'
import { getApiMessage } from '../../api/http'
import { formatVnd } from '../../utils/format'
import { tierLabel } from '../../utils/tier'
import LoadingState from '../../components/ui/LoadingState.vue'
import ErrorState from '../../components/ui/ErrorState.vue'
import EmptyState from '../../components/ui/EmptyState.vue'
import BarChart from '../../components/charts/BarChart.vue'
import DonutChart from '../../components/charts/DonutChart.vue'

const REVENUE_DAYS = 30
// Màu hạng theo DESIGN.md §1.2 — dùng cho donut.
const TIER_COLORS = { SILVER: '#9CA3AF', GOLD: '#D4AF37', PLATINUM: '#A5B4FC' }

const state = ref('loading') // loading | ok | error
const errorMsg = ref('')
const data = ref(null)

async function load() {
  state.value = 'loading'
  try {
    data.value = await adminGetDashboard({ days: REVENUE_DAYS })
    state.value = 'ok'
  } catch (e) {
    errorMsg.value = getApiMessage(e, 'Không tải được số liệu tổng quan.')
    state.value = 'error'
  }
}

// Chênh lệch so hôm qua → nhãn ±% + màu.
function delta(today, yesterday) {
  const t = Number(today) || 0
  const y = Number(yesterday) || 0
  if (y === 0) {
    if (t === 0) return { text: 'không đổi', cls: 'text-gray-400' }
    return { text: 'mới hôm nay', cls: 'text-success' }
  }
  const pct = Math.round(((t - y) / y) * 100)
  if (pct === 0) return { text: 'không đổi', cls: 'text-gray-400' }
  const up = pct > 0
  return { text: `${up ? '▲' : '▼'} ${Math.abs(pct)}% so với hôm qua`, cls: up ? 'text-success' : 'text-danger' }
}

const summary = computed(() => data.value?.summary ?? {})

const cards = computed(() => {
  const s = summary.value
  return [
    { label: 'Doanh thu hôm nay', value: formatVnd(s.revenueToday), delta: delta(s.revenueToday, s.revenueYesterday) },
    { label: 'Vé bán hôm nay', value: (s.ticketsToday ?? 0).toLocaleString('vi-VN'), delta: delta(s.ticketsToday, s.ticketsYesterday) },
    { label: 'Thành viên mới', value: (s.newMembersToday ?? 0).toLocaleString('vi-VN'), delta: delta(s.newMembersToday, s.newMembersYesterday) },
    { label: 'Đơn chờ thanh toán', value: (s.pendingBookings ?? 0).toLocaleString('vi-VN'), delta: null },
  ]
})

// Nhãn cột = ngày/tháng "12/07".
const revenueBars = computed(() =>
  (data.value?.revenueDaily ?? []).map((d) => {
    const [y, m, day] = d.day.split('-')
    return { label: `${day}/${m}`, value: d.revenue }
  }),
)

const donutSegments = computed(() =>
  (data.value?.tierDistribution ?? []).map((t) => ({
    label: tierLabel(t.tier),
    value: t.count,
    color: TIER_COLORS[t.tier] ?? '#9CA3AF',
  })),
)

const topMovies = computed(() => data.value?.topMovies ?? [])

onMounted(load)
</script>

<template>
  <div>
    <h1 class="mb-1 text-xl font-semibold text-gray-900">Tổng quan 👋</h1>
    <p class="mb-6 text-sm text-gray-500">Số liệu kinh doanh hệ thống Linh Vé Các.</p>

    <LoadingState v-if="state === 'loading'" message="Đang tải số liệu…" />
    <ErrorState v-else-if="state === 'error'" :message="errorMsg" />

    <div v-else class="space-y-6">
      <!-- ===== 4 stat card ===== -->
      <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <div v-for="c in cards" :key="c.label" class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
          <p class="text-sm text-gray-500">{{ c.label }}</p>
          <p class="mt-1 text-2xl font-semibold tabular-nums text-gray-900">{{ c.value }}</p>
          <p v-if="c.delta" class="mt-1 text-xs" :class="c.delta.cls">{{ c.delta.text }}</p>
        </div>
      </div>

      <!-- ===== Biểu đồ doanh thu 30 ngày ===== -->
      <div class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm sm:p-6">
        <h2 class="mb-4 text-sm font-semibold text-gray-700">Doanh thu {{ REVENUE_DAYS }} ngày gần đây</h2>
        <BarChart :data="revenueBars" :format-value="formatVnd" />
      </div>

      <div class="grid gap-6 lg:grid-cols-2">
        <!-- ===== Top phim theo doanh thu ===== -->
        <div class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm sm:p-6">
          <h2 class="mb-4 text-sm font-semibold text-gray-700">Top phim theo doanh thu</h2>
          <EmptyState
            v-if="!topMovies.length"
            icon="🎬"
            title="Chưa có doanh thu"
            message="Số liệu sẽ xuất hiện khi có đơn thanh toán thành công."
          />
          <div v-else class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b border-gray-200 text-left text-xs uppercase tracking-wide text-gray-500">
                  <th class="py-2 pr-2 font-medium">Phim</th>
                  <th class="py-2 px-2 text-right font-medium">Vé</th>
                  <th class="py-2 pl-2 text-right font-medium">Doanh thu</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="m in topMovies" :key="m.movieId" class="border-b border-gray-100 last:border-0">
                  <td class="py-2.5 pr-2">
                    <div class="flex items-center gap-2.5">
                      <img
                        v-if="m.posterUrl"
                        :src="m.posterUrl"
                        :alt="m.title"
                        class="h-10 w-7 shrink-0 rounded object-cover"
                      />
                      <div v-else class="h-10 w-7 shrink-0 rounded bg-gray-100" />
                      <span class="font-medium text-gray-800">{{ m.title }}</span>
                    </div>
                  </td>
                  <td class="py-2.5 px-2 text-right tabular-nums text-gray-600">{{ m.tickets }}</td>
                  <td class="py-2.5 pl-2 text-right font-semibold tabular-nums text-gray-900">{{ formatVnd(m.revenue) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- ===== Phân bố hạng thành viên ===== -->
        <div class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm sm:p-6">
          <h2 class="mb-4 text-sm font-semibold text-gray-700">Phân bố hạng thành viên</h2>
          <DonutChart :segments="donutSegments" />
        </div>
      </div>
    </div>
  </div>
</template>
