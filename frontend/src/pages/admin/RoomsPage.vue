<script setup>
// Admin rạp & phòng: chọn rạp → CRUD phòng; thêm rạp mới; link sang sơ đồ ghế.
import { onMounted, reactive, ref, watch } from 'vue'
import {
  adminListCinemas,
  adminCreateCinema,
  adminListRegions,
  adminListChains,
  adminListRooms,
  adminCreateRoom,
  adminUpdateRoom,
  adminDeleteRoom,
} from '../../api/admin'
import { getApiMessage, getFieldErrors } from '../../api/http'
import CrudTable from '../../components/admin/CrudTable.vue'
import BaseModal from '../../components/ui/BaseModal.vue'
import BaseInput from '../../components/ui/BaseInput.vue'
import BaseSelect from '../../components/ui/BaseSelect.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const ROOM_TYPES = ['2D', '3D', 'IMAX'].map((v) => ({ value: v, label: v }))

const cinemas = ref([])
const regions = ref([])
const chains = ref([])
const selectedCinemaId = ref('')

const rows = ref([])
const loading = ref(false)
const loadError = ref('')

const columns = [
  { key: 'name', label: 'Phòng' },
  { key: 'roomType', label: 'Loại' },
  { key: 'seatCount', label: 'Số ghế' },
  { key: 'seats', label: 'Sơ đồ ghế' },
]

// Room modal
const roomModal = ref(false)
const editingRoomId = ref(null)
const savingRoom = ref(false)
const roomError = ref('')
const roomFieldErrors = ref({})
const roomForm = reactive({ name: '', roomType: '2D' })

// Cinema modal
const cinemaModal = ref(false)
const savingCinema = ref(false)
const cinemaError = ref('')
const cinemaFieldErrors = ref({})
const cinemaForm = reactive({ regionId: '', chainId: '', name: '', address: '' })

async function loadRooms() {
  if (!selectedCinemaId.value) {
    rows.value = []
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    rows.value = await adminListRooms(selectedCinemaId.value)
  } catch (e) {
    loadError.value = getApiMessage(e, 'Không tải được phòng chiếu')
  } finally {
    loading.value = false
  }
}

watch(selectedCinemaId, loadRooms)

// --- Room ---
function openCreateRoom() {
  editingRoomId.value = null
  Object.assign(roomForm, { name: '', roomType: '2D' })
  roomError.value = ''
  roomFieldErrors.value = {}
  roomModal.value = true
}
function openEditRoom(row) {
  editingRoomId.value = row.id
  Object.assign(roomForm, { name: row.name, roomType: row.roomType })
  roomError.value = ''
  roomFieldErrors.value = {}
  roomModal.value = true
}
async function saveRoom() {
  savingRoom.value = true
  roomError.value = ''
  roomFieldErrors.value = {}
  const payload = { cinemaId: Number(selectedCinemaId.value), name: roomForm.name, roomType: roomForm.roomType }
  try {
    if (editingRoomId.value) {
      await adminUpdateRoom(editingRoomId.value, payload)
    } else {
      await adminCreateRoom(payload)
    }
    roomModal.value = false
    await loadRooms()
  } catch (e) {
    roomFieldErrors.value = getFieldErrors(e)
    if (Object.keys(roomFieldErrors.value).length === 0) {
      roomError.value = getApiMessage(e, 'Lưu phòng thất bại')
    }
  } finally {
    savingRoom.value = false
  }
}
async function removeRoom(row) {
  try {
    await adminDeleteRoom(row.id)
    await loadRooms()
  } catch (e) {
    loadError.value = getApiMessage(e, 'Xóa phòng thất bại')
  }
}

// --- Cinema ---
function openCreateCinema() {
  Object.assign(cinemaForm, { regionId: '', chainId: '', name: '', address: '' })
  cinemaError.value = ''
  cinemaFieldErrors.value = {}
  cinemaModal.value = true
}
async function saveCinema() {
  savingCinema.value = true
  cinemaError.value = ''
  cinemaFieldErrors.value = {}
  try {
    const created = await adminCreateCinema({
      regionId: Number(cinemaForm.regionId),
      chainId: Number(cinemaForm.chainId),
      name: cinemaForm.name,
      address: cinemaForm.address,
    })
    cinemaModal.value = false
    cinemas.value = await adminListCinemas()
    selectedCinemaId.value = created.id
  } catch (e) {
    cinemaFieldErrors.value = getFieldErrors(e)
    if (Object.keys(cinemaFieldErrors.value).length === 0) {
      cinemaError.value = getApiMessage(e, 'Lưu rạp thất bại')
    }
  } finally {
    savingCinema.value = false
  }
}

onMounted(async () => {
  cinemas.value = await adminListCinemas()
  regions.value = await adminListRegions()
  chains.value = await adminListChains()
  if (cinemas.value.length) selectedCinemaId.value = cinemas.value[0].id
})
</script>

<template>
  <div class="mb-4 flex flex-wrap items-center gap-3">
    <h1 class="mr-auto text-xl font-semibold text-gray-900">Rạp &amp; phòng</h1>
    <select
      v-model="selectedCinemaId"
      class="h-9 rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-900 outline-none focus:border-brand-600"
    >
      <option value="" disabled>Chọn rạp</option>
      <option v-for="c in cinemas" :key="c.id" :value="c.id">{{ c.name }}</option>
    </select>
    <button
      type="button"
      class="h-9 rounded-lg border border-brand-600 px-4 text-sm font-medium text-brand-600 transition-colors duration-150 hover:bg-brand-600/10"
      @click="openCreateCinema"
    >
      + Thêm rạp
    </button>
  </div>

  <CrudTable
    :columns="columns"
    :rows="rows"
    :loading="loading"
    :error="loadError"
    add-label="+ Thêm phòng"
    @add="openCreateRoom"
    @edit="openEditRoom"
    @delete="removeRoom"
  >
    <template #empty>Rạp này chưa có phòng chiếu nào.</template>
    <template #cell-seats="{ row }">
      <RouterLink
        :to="{ name: 'admin-room-seats', params: { id: row.id } }"
        class="text-brand-600 hover:underline"
      >
        Sơ đồ ghế →
      </RouterLink>
    </template>
  </CrudTable>

  <!-- Room form -->
  <BaseModal
    :open="roomModal"
    :title="editingRoomId ? 'Sửa phòng' : 'Thêm phòng'"
    size="sm"
    @close="roomModal = false"
  >
    <form class="space-y-4" @submit.prevent="saveRoom">
      <ErrorState v-if="roomError" :message="roomError" />
      <BaseInput v-model="roomForm.name" label="Tên phòng" required :error="roomFieldErrors.name" />
      <BaseSelect v-model="roomForm.roomType" label="Loại phòng" :options="ROOM_TYPES" />
    </form>
    <template #footer>
      <BaseButton variant="ghost" @click="roomModal = false">Hủy</BaseButton>
      <BaseButton :loading="savingRoom" @click="saveRoom">Lưu</BaseButton>
    </template>
  </BaseModal>

  <!-- Cinema form -->
  <BaseModal :open="cinemaModal" title="Thêm rạp" size="md" @close="cinemaModal = false">
    <form class="space-y-4" @submit.prevent="saveCinema">
      <ErrorState v-if="cinemaError" :message="cinemaError" />
      <div class="grid gap-4 sm:grid-cols-2">
        <BaseSelect
          v-model="cinemaForm.regionId"
          label="Khu vực"
          required
          placeholder="Chọn khu vực"
          :options="regions.map((r) => ({ value: r.id, label: r.name }))"
          :error="cinemaFieldErrors.regionId"
        />
        <BaseSelect
          v-model="cinemaForm.chainId"
          label="Hãng rạp"
          required
          placeholder="Chọn hãng"
          :options="chains.map((c) => ({ value: c.id, label: c.name }))"
          :error="cinemaFieldErrors.chainId"
        />
      </div>
      <BaseInput v-model="cinemaForm.name" label="Tên rạp" required :error="cinemaFieldErrors.name" />
      <BaseInput v-model="cinemaForm.address" label="Địa chỉ" required :error="cinemaFieldErrors.address" />
    </form>
    <template #footer>
      <BaseButton variant="ghost" @click="cinemaModal = false">Hủy</BaseButton>
      <BaseButton :loading="savingCinema" @click="saveCinema">Lưu</BaseButton>
    </template>
  </BaseModal>
</template>
