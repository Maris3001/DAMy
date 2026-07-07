<script setup>
// Admin sơ đồ ghế (P-12): sinh lưới rows×cols; click ghế để sơn loại; lưu thay đổi.
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  adminListSeats,
  adminGenerateSeats,
  adminUpdateSeats,
} from '../../api/admin'
import { getApiMessage } from '../../api/http'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseModal from '../../components/ui/BaseModal.vue'
import BaseInput from '../../components/ui/BaseInput.vue'

const route = useRoute()
const roomId = computed(() => route.params.id)

const TYPES = [
  { value: 'STANDARD', label: 'Thường' },
  { value: 'VIP', label: 'VIP' },
  { value: 'COUPLE', label: 'Ghế đôi' },
]

const seats = ref([])
const loading = ref(true)
const loadError = ref('')
const activeType = ref('VIP')
const saving = ref(false)
const saveMsg = ref('')
const dirty = ref(false)

const genModal = ref(false)
const generating = ref(false)
const genForm = reactive({ rows: 8, cols: 10 })

// Nhóm ghế theo hàng (A, B, ...) để render lưới
const grid = computed(() => {
  const byRow = new Map()
  for (const s of seats.value) {
    if (!byRow.has(s.rowLabel)) byRow.set(s.rowLabel, [])
    byRow.get(s.rowLabel).push(s)
  }
  return [...byRow.entries()]
    .sort((a, b) => a[0].localeCompare(b[0]))
    .map(([label, list]) => ({ label, seats: list.sort((a, b) => a.colNumber - b.colNumber) }))
})

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    seats.value = await adminListSeats(roomId.value)
    dirty.value = false
  } catch (e) {
    loadError.value = getApiMessage(e, 'Không tải được sơ đồ ghế')
  } finally {
    loading.value = false
  }
}

function paint(seat) {
  if (seat.seatType !== activeType.value) {
    seat.seatType = activeType.value
    dirty.value = true
    saveMsg.value = ''
  }
}

function seatClass(seat) {
  const base = 'flex h-8 w-8 items-center justify-center rounded-md border text-xs font-medium cursor-pointer transition-colors duration-150 '
  if (seat.seatType === 'VIP') return base + 'border-brand-600 bg-brand-600/10 text-brand-600'
  if (seat.seatType === 'COUPLE') return base + 'border-crimson-500 bg-crimson-500/10 text-crimson-500'
  return base + 'border-gray-300 bg-white text-gray-500 hover:border-brand-600'
}

async function generate() {
  generating.value = true
  try {
    seats.value = await adminGenerateSeats(roomId.value, {
      rows: Number(genForm.rows),
      cols: Number(genForm.cols),
    })
    dirty.value = false
    genModal.value = false
    saveMsg.value = 'Đã sinh sơ đồ ghế mới.'
  } catch (e) {
    loadError.value = getApiMessage(e, 'Sinh sơ đồ thất bại')
  } finally {
    generating.value = false
  }
}

async function save() {
  saving.value = true
  saveMsg.value = ''
  try {
    const payload = { seats: seats.value.map((s) => ({ seatId: s.id, seatType: s.seatType })) }
    seats.value = await adminUpdateSeats(roomId.value, payload)
    dirty.value = false
    saveMsg.value = 'Đã lưu sơ đồ ghế.'
  } catch (e) {
    loadError.value = getApiMessage(e, 'Lưu sơ đồ thất bại')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="mb-4 flex flex-wrap items-center gap-3">
      <RouterLink :to="{ name: 'admin-rooms' }" class="text-sm text-brand-600 hover:underline">
        ← Rạp &amp; phòng
      </RouterLink>
      <h1 class="mr-auto text-xl font-semibold text-gray-900">Sơ đồ ghế</h1>
      <BaseButton variant="secondary" @click="genModal = true">Sinh lưới ghế</BaseButton>
      <BaseButton :loading="saving" :disabled="!dirty" @click="save">Lưu sơ đồ</BaseButton>
    </div>

    <p v-if="saveMsg" class="mb-3 text-sm text-success">✓ {{ saveMsg }}</p>
    <p v-if="loadError" class="mb-3 text-sm text-danger">{{ loadError }}</p>

    <div class="rounded-lg border border-gray-200 bg-white p-4 shadow-sm md:p-6">
      <p v-if="loading" class="py-10 text-center text-gray-400">Đang tải…</p>
      <div v-else-if="seats.length === 0" class="py-10 text-center text-gray-400">
        Phòng chưa có ghế. Bấm “Sinh lưới ghế” để tạo.
      </div>
      <template v-else>
        <!-- Công cụ sơn loại ghế -->
        <div class="mb-4 flex flex-wrap items-center gap-2">
          <span class="text-sm text-gray-500">Chọn loại rồi bấm vào ghế:</span>
          <button
            v-for="t in TYPES"
            :key="t.value"
            type="button"
            class="rounded-lg border px-3 py-1.5 text-sm transition-colors duration-150"
            :class="
              activeType === t.value
                ? 'border-brand-600 bg-brand-600 text-white'
                : 'border-gray-300 text-gray-700 hover:border-brand-600'
            "
            @click="activeType = t.value"
          >
            {{ t.label }}
          </button>
        </div>

        <!-- Màn hình -->
        <div class="mb-6 text-center">
          <div class="mx-auto max-w-md rounded-t-full border-t-2 border-brand-600 py-1 text-xs text-gray-400">
            MÀN HÌNH
          </div>
        </div>

        <!-- Lưới ghế (căn giữa theo màn hình; nhãn hàng 2 bên để lưới đối xứng) -->
        <div class="overflow-x-auto text-center">
          <div class="inline-flex flex-col gap-1">
            <div v-for="row in grid" :key="row.label" class="flex items-center gap-1">
              <span class="w-5 text-center text-xs text-gray-400">{{ row.label }}</span>
              <button
                v-for="seat in row.seats"
                :key="seat.id"
                type="button"
                :class="seatClass(seat)"
                :title="`${seat.rowLabel}${seat.colNumber}`"
                @click="paint(seat)"
              >
                {{ seat.colNumber }}
              </button>
              <span class="w-5 text-center text-xs text-gray-400">{{ row.label }}</span>
            </div>
          </div>
        </div>

        <!-- Chú giải -->
        <div class="mt-6 flex flex-wrap justify-center gap-4 text-sm text-gray-600">
          <span class="flex items-center gap-2">
            <span class="h-4 w-4 rounded border border-gray-300 bg-white" /> Thường
          </span>
          <span class="flex items-center gap-2">
            <span class="h-4 w-4 rounded border border-brand-600 bg-brand-600/10" /> VIP
          </span>
          <span class="flex items-center gap-2">
            <span class="h-4 w-4 rounded border border-crimson-500 bg-crimson-500/10" /> Ghế đôi
          </span>
        </div>
      </template>
    </div>

    <!-- Modal sinh lưới -->
    <BaseModal :open="genModal" title="Sinh lưới ghế" size="sm" @close="genModal = false">
      <p class="mb-3 text-sm text-ink-500">
        Thao tác này sẽ <span class="text-danger">xóa toàn bộ ghế hiện tại</span> và tạo lưới mới
        (mặc định loại Thường).
      </p>
      <div class="grid grid-cols-2 gap-4">
        <BaseInput v-model="genForm.rows" type="number" label="Số hàng (tối đa 26)" />
        <BaseInput v-model="genForm.cols" type="number" label="Số cột (tối đa 50)" />
      </div>
      <template #footer>
        <BaseButton variant="ghost" @click="genModal = false">Hủy</BaseButton>
        <BaseButton :loading="generating" @click="generate">Sinh sơ đồ</BaseButton>
      </template>
    </BaseModal>
  </div>
</template>
