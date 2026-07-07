<script setup>
// Admin lịch chiếu (P-11): filter rạp+ngày, form tạo suất; cảnh báo 409 trùng giờ.
import { onMounted, reactive, ref, watch } from 'vue'
import {
  adminListShowtimes,
  adminCreateShowtime,
  adminUpdateShowtime,
  adminDeleteShowtime,
  adminListMovies,
  adminListCinemas,
  adminListRooms,
} from '../../api/admin'
import { getApiMessage, getFieldErrors } from '../../api/http'
import { formatDate, formatTime, formatVnd, toDateParam } from '../../utils/format'
import CrudTable from '../../components/admin/CrudTable.vue'
import BaseModal from '../../components/ui/BaseModal.vue'
import BaseInput from '../../components/ui/BaseInput.vue'
import BaseSelect from '../../components/ui/BaseSelect.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const columns = [
  { key: 'movieTitle', label: 'Phim' },
  { key: 'room', label: 'Rạp / Phòng' },
  { key: 'time', label: 'Giờ chiếu' },
  { key: 'basePrice', label: 'Giá vé' },
]

const rows = ref([])
const loading = ref(true)
const loadError = ref('')

const cinemas = ref([])
const movies = ref([])

// Filter
const filter = reactive({ cinemaId: '', roomId: '', date: toDateParam(new Date()) })
const filterRooms = ref([])

// Form
const modalOpen = ref(false)
const editingId = ref(null)
const saving = ref(false)
const apiError = ref('')
const fieldErrors = ref({})
const form = reactive({ movieId: '', cinemaId: '', roomId: '', startsAt: '', basePrice: 90000 })
const formRooms = ref([])

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    rows.value = await adminListShowtimes({
      cinemaId: filter.cinemaId || undefined,
      roomId: filter.roomId || undefined,
      date: filter.date || undefined,
    })
  } catch (e) {
    loadError.value = getApiMessage(e, 'Không tải được lịch chiếu')
  } finally {
    loading.value = false
  }
}

watch(() => filter.cinemaId, async (id) => {
  filter.roomId = ''
  filterRooms.value = id ? await adminListRooms(id) : []
  load()
})
watch(() => filter.roomId, load)
watch(() => filter.date, load)

watch(() => form.cinemaId, async (id) => {
  form.roomId = ''
  formRooms.value = id ? await adminListRooms(id) : []
})

function openCreate() {
  editingId.value = null
  Object.assign(form, { movieId: '', cinemaId: '', roomId: '', startsAt: '', basePrice: 90000 })
  formRooms.value = []
  apiError.value = ''
  fieldErrors.value = {}
  modalOpen.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  apiError.value = ''
  fieldErrors.value = {}
  form.movieId = row.movieId
  form.cinemaId = row.cinemaId
  formRooms.value = await adminListRooms(row.cinemaId)
  form.roomId = row.roomId
  form.startsAt = row.startsAt?.slice(0, 16) // "YYYY-MM-DDTHH:mm"
  form.basePrice = row.basePrice
  modalOpen.value = true
}

async function save() {
  saving.value = true
  apiError.value = ''
  fieldErrors.value = {}
  const payload = {
    movieId: Number(form.movieId),
    roomId: Number(form.roomId),
    startsAt: form.startsAt,
    basePrice: Number(form.basePrice),
    status: 'OPEN',
  }
  try {
    if (editingId.value) {
      await adminUpdateShowtime(editingId.value, payload)
    } else {
      await adminCreateShowtime(payload)
    }
    modalOpen.value = false
    await load()
  } catch (e) {
    fieldErrors.value = getFieldErrors(e)
    if (Object.keys(fieldErrors.value).length === 0) {
      apiError.value = getApiMessage(e, 'Lưu suất chiếu thất bại')
    }
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await adminDeleteShowtime(row.id)
    await load()
  } catch (e) {
    loadError.value = getApiMessage(e, 'Xóa suất chiếu thất bại')
  }
}

onMounted(async () => {
  cinemas.value = await adminListCinemas()
  const movieRes = await adminListMovies({ size: 100 })
  movies.value = movieRes.content
  await load()
})
</script>

<template>
  <CrudTable
    title="Lịch chiếu"
    :columns="columns"
    :rows="rows"
    :loading="loading"
    :error="loadError"
    add-label="+ Thêm suất chiếu"
    @add="openCreate"
    @edit="openEdit"
    @delete="remove"
  >
    <template #filters>
      <select
        v-model="filter.cinemaId"
        class="h-9 rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-900 outline-none focus:border-brand-600"
      >
        <option value="">Tất cả rạp</option>
        <option v-for="c in cinemas" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <select
        v-model="filter.roomId"
        :disabled="!filter.cinemaId"
        class="h-9 rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-900 outline-none focus:border-brand-600 disabled:opacity-50"
      >
        <option value="">Tất cả phòng</option>
        <option v-for="r in filterRooms" :key="r.id" :value="r.id">{{ r.name }}</option>
      </select>
      <input
        v-model="filter.date"
        type="date"
        class="h-9 rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-900 outline-none focus:border-brand-600"
      />
    </template>

    <template #cell-room="{ row }">{{ row.cinemaName }} · {{ row.roomName }}</template>
    <template #cell-time="{ row }">
      {{ formatDate(row.startsAt) }} · {{ formatTime(row.startsAt) }}–{{ formatTime(row.endsAt) }}
    </template>
    <template #cell-basePrice="{ row }">{{ formatVnd(row.basePrice) }}</template>
    <template #delete-message="{ row }">
      Xóa suất {{ row ? formatTime(row.startsAt) : '' }} — {{ row?.movieTitle }}?
    </template>
  </CrudTable>

  <BaseModal
    :open="modalOpen"
    :title="editingId ? 'Sửa suất chiếu' : 'Thêm suất chiếu'"
    size="md"
    @close="modalOpen = false"
  >
    <form class="space-y-4" @submit.prevent="save">
      <ErrorState v-if="apiError" :message="apiError" />
      <BaseSelect
        v-model="form.movieId"
        label="Phim"
        required
        placeholder="Chọn phim"
        :options="movies.map((m) => ({ value: m.id, label: m.title }))"
        :error="fieldErrors.movieId"
      />
      <div class="grid gap-4 sm:grid-cols-2">
        <BaseSelect
          v-model="form.cinemaId"
          label="Rạp"
          required
          placeholder="Chọn rạp"
          :options="cinemas.map((c) => ({ value: c.id, label: c.name }))"
        />
        <BaseSelect
          v-model="form.roomId"
          label="Phòng"
          required
          placeholder="Chọn phòng"
          :options="formRooms.map((r) => ({ value: r.id, label: r.name }))"
          :error="fieldErrors.roomId"
        />
      </div>
      <div class="grid gap-4 sm:grid-cols-2">
        <BaseInput
          v-model="form.startsAt"
          type="datetime-local"
          label="Bắt đầu"
          required
          :error="fieldErrors.startsAt"
        />
        <BaseInput
          v-model="form.basePrice"
          type="number"
          label="Giá vé (₫)"
          required
          :error="fieldErrors.basePrice"
        />
      </div>
    </form>

    <template #footer>
      <BaseButton variant="ghost" @click="modalOpen = false">Hủy</BaseButton>
      <BaseButton :loading="saving" @click="save">Lưu</BaseButton>
    </template>
  </BaseModal>
</template>
