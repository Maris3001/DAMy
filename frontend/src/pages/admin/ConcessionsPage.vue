<script setup>
// Admin bắp nước (P-11): CrudTable + form kèm upload ảnh.
import { onMounted, reactive, ref } from 'vue'
import {
  adminListConcessions,
  adminCreateConcession,
  adminUpdateConcession,
  adminDeleteConcession,
} from '../../api/admin'
import { getApiMessage, getFieldErrors } from '../../api/http'
import { formatVnd } from '../../utils/format'
import CrudTable from '../../components/admin/CrudTable.vue'
import ImageUploader from '../../components/admin/ImageUploader.vue'
import BaseModal from '../../components/ui/BaseModal.vue'
import BaseInput from '../../components/ui/BaseInput.vue'
import BaseSelect from '../../components/ui/BaseSelect.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const CATEGORY_OPTIONS = [
  { value: 'COMBO', label: 'Combo' },
  { value: 'SINGLE', label: 'Món lẻ' },
]
const columns = [
  { key: 'image', label: '', class: 'w-16' },
  { key: 'name', label: 'Tên món' },
  { key: 'category', label: 'Nhóm' },
  { key: 'price', label: 'Giá' },
  { key: 'active', label: 'Trạng thái' },
]

const rows = ref([])
const loading = ref(true)
const loadError = ref('')

const modalOpen = ref(false)
const editingId = ref(null)
const saving = ref(false)
const apiError = ref('')
const fieldErrors = ref({})
const form = reactive(emptyForm())

function emptyForm() {
  return { name: '', description: '', price: 0, category: 'SINGLE', imageUrl: '', active: true }
}

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    rows.value = await adminListConcessions()
  } catch (e) {
    loadError.value = getApiMessage(e, 'Không tải được danh sách bắp nước')
  } finally {
    loading.value = false
  }
}

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
    name: row.name,
    description: row.description ?? '',
    price: row.price,
    category: row.category,
    imageUrl: row.imageUrl ?? '',
    active: row.active,
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
    name: form.name,
    description: form.description || null,
    price: Number(form.price),
    category: form.category,
    imageUrl: form.imageUrl || null,
    active: form.active,
  }
  try {
    if (editingId.value) {
      await adminUpdateConcession(editingId.value, payload)
    } else {
      await adminCreateConcession(payload)
    }
    modalOpen.value = false
    await load()
  } catch (e) {
    fieldErrors.value = getFieldErrors(e)
    if (Object.keys(fieldErrors.value).length === 0) {
      apiError.value = getApiMessage(e, 'Lưu món thất bại')
    }
  } finally {
    saving.value = false
  }
}
async function remove(row) {
  try {
    await adminDeleteConcession(row.id)
    await load()
  } catch (e) {
    loadError.value = getApiMessage(e, 'Xóa món thất bại')
  }
}

onMounted(load)
</script>

<template>
  <CrudTable
    title="Bắp nước"
    :columns="columns"
    :rows="rows"
    :loading="loading"
    :error="loadError"
    add-label="+ Thêm món"
    @add="openCreate"
    @edit="openEdit"
    @delete="remove"
  >
    <template #cell-image="{ row }">
      <div class="h-10 w-10 overflow-hidden rounded bg-gray-100">
        <img v-if="row.imageUrl" :src="row.imageUrl" :alt="row.name" class="h-full w-full object-cover" />
      </div>
    </template>
    <template #cell-category="{ row }">
      {{ row.category === 'COMBO' ? 'Combo' : 'Món lẻ' }}
    </template>
    <template #cell-price="{ row }">{{ formatVnd(row.price) }}</template>
    <template #cell-active="{ row }">
      <BaseBadge :variant="row.active ? 'success' : 'gray'">
        {{ row.active ? 'Đang bán' : 'Ẩn' }}
      </BaseBadge>
    </template>
    <template #delete-message="{ row }">Xóa món “{{ row?.name }}”?</template>
  </CrudTable>

  <BaseModal
    :open="modalOpen"
    :title="editingId ? 'Sửa món' : 'Thêm món'"
    size="md"
    @close="modalOpen = false"
  >
    <form class="space-y-4" @submit.prevent="save">
      <ErrorState v-if="apiError" :message="apiError" />
      <BaseInput v-model="form.name" label="Tên món" required :error="fieldErrors.name" />
      <BaseInput v-model="form.description" label="Mô tả" :error="fieldErrors.description" />
      <div class="grid gap-4 sm:grid-cols-2">
        <BaseInput v-model="form.price" type="number" label="Giá (₫)" required :error="fieldErrors.price" />
        <BaseSelect v-model="form.category" label="Nhóm" :options="CATEGORY_OPTIONS" />
      </div>
      <ImageUploader v-model="form.imageUrl" label="Ảnh món" />
      <label class="flex items-center gap-2 text-sm text-ink-300">
        <input v-model="form.active" type="checkbox" class="size-4" />
        Đang bán
      </label>
    </form>
    <template #footer>
      <BaseButton variant="ghost" @click="modalOpen = false">Hủy</BaseButton>
      <BaseButton :loading="saving" @click="save">Lưu</BaseButton>
    </template>
  </BaseModal>
</template>
