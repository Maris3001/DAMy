<script setup>
// Admin quản lý phim (P-11): CrudTable + modal form kèm upload poster/backdrop.
import { onMounted, reactive, ref, watch } from 'vue'
import {
  adminListMovies,
  adminCreateMovie,
  adminUpdateMovie,
  adminDeleteMovie,
  adminListGenres,
} from '../../api/admin'
import { getApiMessage, getFieldErrors } from '../../api/http'
import { formatDate } from '../../utils/format'
import CrudTable from '../../components/admin/CrudTable.vue'
import ImageUploader from '../../components/admin/ImageUploader.vue'
import BaseModal from '../../components/ui/BaseModal.vue'
import BaseInput from '../../components/ui/BaseInput.vue'
import BaseSelect from '../../components/ui/BaseSelect.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const AGE_OPTIONS = ['P', 'K', 'T13', 'T16', 'T18'].map((v) => ({ value: v, label: v }))
const STATUS_OPTIONS = [
  { value: 'NOW_SHOWING', label: 'Đang chiếu' },
  { value: 'COMING_SOON', label: 'Sắp chiếu' },
  { value: 'ARCHIVED', label: 'Lưu trữ' },
]
const STATUS_META = {
  NOW_SHOWING: { label: 'Đang chiếu', variant: 'success' },
  COMING_SOON: { label: 'Sắp chiếu', variant: 'info' },
  ARCHIVED: { label: 'Lưu trữ', variant: 'gray' },
}
const columns = [
  { key: 'poster', label: '', class: 'w-16' },
  { key: 'title', label: 'Tên phim' },
  { key: 'genres', label: 'Thể loại' },
  { key: 'durationMin', label: 'Thời lượng' },
  { key: 'ageRating', label: 'Độ tuổi' },
  { key: 'status', label: 'Trạng thái' },
  { key: 'releaseDate', label: 'Khởi chiếu' },
]

const rows = ref([])
const genres = ref([])
const loading = ref(true)
const loadError = ref('')
const search = ref('')
const page = ref(0)
const totalPages = ref(1)

const modalOpen = ref(false)
const editingId = ref(null)
const saving = ref(false)
const apiError = ref('')
const fieldErrors = ref({})
const form = reactive(emptyForm())

function emptyForm() {
  return {
    title: '',
    description: '',
    durationMin: 90,
    genreIds: [],
    ageRating: 'P',
    status: 'COMING_SOON',
    releaseDate: '',
    posterUrl: '',
    backdropUrl: '',
    trailerUrl: '',
  }
}

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await adminListMovies({ search: search.value || undefined, page: page.value, size: 10 })
    rows.value = res.content
    totalPages.value = res.totalPages || 1
  } catch (e) {
    loadError.value = getApiMessage(e, 'Không tải được danh sách phim')
  } finally {
    loading.value = false
  }
}

let searchTimer
watch(search, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 0
    load()
  }, 300)
})

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  apiError.value = ''
  fieldErrors.value = {}
  modalOpen.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    description: row.description ?? '',
    durationMin: row.durationMin,
    genreIds: (row.genres ?? []).map((g) => g.id),
    ageRating: row.ageRating,
    status: row.status,
    releaseDate: row.releaseDate ?? '',
    posterUrl: row.posterUrl ?? '',
    backdropUrl: row.backdropUrl ?? '',
    trailerUrl: row.trailerUrl ?? '',
  })
  apiError.value = ''
  fieldErrors.value = {}
  modalOpen.value = true
}

async function save() {
  saving.value = true
  apiError.value = ''
  fieldErrors.value = {}
  const payload = {
    ...form,
    durationMin: Number(form.durationMin),
    releaseDate: form.releaseDate || null,
    genreIds: form.genreIds,
    description: form.description || null,
    posterUrl: form.posterUrl || null,
    backdropUrl: form.backdropUrl || null,
    trailerUrl: form.trailerUrl || null,
  }
  try {
    if (editingId.value) {
      await adminUpdateMovie(editingId.value, payload)
    } else {
      await adminCreateMovie(payload)
    }
    modalOpen.value = false
    await load()
  } catch (e) {
    fieldErrors.value = getFieldErrors(e)
    if (Object.keys(fieldErrors.value).length === 0) {
      apiError.value = getApiMessage(e, 'Lưu phim thất bại')
    }
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await adminDeleteMovie(row.id)
    await load()
  } catch (e) {
    loadError.value = getApiMessage(e, 'Xóa phim thất bại')
  }
}

function toggleGenre(id) {
  const i = form.genreIds.indexOf(id)
  if (i === -1) form.genreIds.push(id)
  else form.genreIds.splice(i, 1)
}

async function loadGenres() {
  try {
    genres.value = await adminListGenres()
  } catch {
    genres.value = []
  }
}

onMounted(() => {
  load()
  loadGenres()
})
</script>

<template>
  <CrudTable
    v-model:search="search"
    title="Phim"
    :columns="columns"
    :rows="rows"
    :loading="loading"
    :error="loadError"
    searchable
    search-placeholder="Tìm theo tên phim…"
    add-label="+ Thêm phim"
    @add="openCreate"
    @edit="openEdit"
    @delete="remove"
  >
    <template #cell-poster="{ row }">
      <div class="h-14 w-10 overflow-hidden rounded bg-gray-100">
        <img v-if="row.posterUrl" :src="row.posterUrl" :alt="row.title" class="h-full w-full object-cover" />
      </div>
    </template>
    <template #cell-genres="{ row }">
      {{ row.genres?.map((g) => g.name).join(', ') || '—' }}
    </template>
    <template #cell-durationMin="{ row }">{{ row.durationMin }} phút</template>
    <template #cell-status="{ row }">
      <BaseBadge :variant="STATUS_META[row.status]?.variant">
        {{ STATUS_META[row.status]?.label ?? row.status }}
      </BaseBadge>
    </template>
    <template #cell-releaseDate="{ row }">{{ formatDate(row.releaseDate) || '—' }}</template>
    <template #delete-message="{ row }">Xóa phim “{{ row?.title }}”?</template>
    <template #pagination>
      <div class="flex items-center gap-2 text-sm text-gray-600">
        <button
          class="rounded border border-gray-300 px-3 py-1 disabled:opacity-40"
          :disabled="page === 0"
          @click="page--; load()"
        >
          Trước
        </button>
        <span>Trang {{ page + 1 }} / {{ totalPages }}</span>
        <button
          class="rounded border border-gray-300 px-3 py-1 disabled:opacity-40"
          :disabled="page + 1 >= totalPages"
          @click="page++; load()"
        >
          Sau
        </button>
      </div>
    </template>
  </CrudTable>

  <!-- Form thêm/sửa -->
  <BaseModal
    :open="modalOpen"
    :title="editingId ? 'Sửa phim' : 'Thêm phim'"
    size="lg"
    @close="modalOpen = false"
  >
    <form class="space-y-4" @submit.prevent="save">
      <ErrorState v-if="apiError" :message="apiError" />
      <BaseInput v-model="form.title" label="Tên phim" required :error="fieldErrors.title" />

      <div>
        <label class="mb-1.5 block text-sm text-ink-300">Mô tả</label>
        <textarea
          v-model="form.description"
          rows="3"
          class="w-full rounded-md border border-surface-700 bg-surface-800 px-3 py-2 text-base text-ink-100 outline-none focus:border-brand-500 [color-scheme:dark]"
        />
      </div>

      <div class="grid gap-4 sm:grid-cols-3">
        <BaseInput
          v-model="form.durationMin"
          type="number"
          label="Thời lượng (phút)"
          required
          :error="fieldErrors.durationMin"
        />
        <BaseSelect v-model="form.ageRating" label="Độ tuổi" :options="AGE_OPTIONS" />
        <BaseSelect v-model="form.status" label="Trạng thái" :options="STATUS_OPTIONS" />
      </div>

      <div>
        <div class="mb-1.5 flex items-center justify-between">
          <label class="block text-sm text-ink-300">Thể loại</label>
          <RouterLink :to="{ name: 'admin-genres' }" class="text-xs text-brand-400 hover:underline">
            Quản lý thể loại →
          </RouterLink>
        </div>
        <div v-if="genres.length" class="flex flex-wrap gap-2">
          <button
            v-for="g in genres"
            :key="g.id"
            type="button"
            class="rounded-full border px-3 py-1 text-sm transition-colors duration-150"
            :class="
              form.genreIds.includes(g.id)
                ? 'border-brand-500 bg-brand-500 text-white'
                : 'border-surface-700 text-ink-300 hover:border-brand-500'
            "
            @click="toggleGenre(g.id)"
          >
            {{ g.name }}
          </button>
        </div>
        <p v-else class="text-sm text-ink-500">
          Chưa có thể loại nào. Thêm ở
          <RouterLink :to="{ name: 'admin-genres' }" class="text-brand-400 hover:underline">Quản lý thể loại</RouterLink>.
        </p>
      </div>

      <BaseInput v-model="form.releaseDate" type="date" label="Ngày khởi chiếu" />

      <BaseInput v-model="form.trailerUrl" label="Link trailer (YouTube)" placeholder="https://youtu.be/..." />

      <div class="grid gap-4 sm:grid-cols-2">
        <ImageUploader v-model="form.posterUrl" label="Poster" />
        <ImageUploader v-model="form.backdropUrl" label="Ảnh nền (backdrop)" />
      </div>
    </form>

    <template #footer>
      <BaseButton variant="ghost" @click="modalOpen = false">Hủy</BaseButton>
      <BaseButton :loading="saving" @click="save">Lưu</BaseButton>
    </template>
  </BaseModal>
</template>
