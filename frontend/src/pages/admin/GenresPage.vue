<script setup>
// Admin thể loại phim: CrudTable 1 cột + modal form 1 field.
import { onMounted, reactive, ref } from 'vue'
import {
  adminListGenres,
  adminCreateGenre,
  adminUpdateGenre,
  adminDeleteGenre,
} from '../../api/admin'
import { getApiMessage, getFieldErrors } from '../../api/http'
import CrudTable from '../../components/admin/CrudTable.vue'
import BaseModal from '../../components/ui/BaseModal.vue'
import BaseInput from '../../components/ui/BaseInput.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const columns = [{ key: 'name', label: 'Tên thể loại' }]

const rows = ref([])
const loading = ref(true)
const loadError = ref('')

const modalOpen = ref(false)
const editingId = ref(null)
const saving = ref(false)
const apiError = ref('')
const fieldErrors = ref({})
const form = reactive({ name: '' })

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    rows.value = await adminListGenres()
  } catch (e) {
    loadError.value = getApiMessage(e, 'Không tải được danh sách thể loại')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.name = ''
  apiError.value = ''
  fieldErrors.value = {}
  modalOpen.value = true
}
function openEdit(row) {
  editingId.value = row.id
  form.name = row.name
  apiError.value = ''
  fieldErrors.value = {}
  modalOpen.value = true
}
async function save() {
  saving.value = true
  apiError.value = ''
  fieldErrors.value = {}
  const payload = { name: form.name }
  try {
    if (editingId.value) {
      await adminUpdateGenre(editingId.value, payload)
    } else {
      await adminCreateGenre(payload)
    }
    modalOpen.value = false
    await load()
  } catch (e) {
    fieldErrors.value = getFieldErrors(e)
    if (Object.keys(fieldErrors.value).length === 0) {
      apiError.value = getApiMessage(e, 'Lưu thể loại thất bại')
    }
  } finally {
    saving.value = false
  }
}
async function remove(row) {
  try {
    await adminDeleteGenre(row.id)
    await load()
  } catch (e) {
    loadError.value = getApiMessage(e, 'Xóa thể loại thất bại')
  }
}

onMounted(load)
</script>

<template>
  <CrudTable
    title="Thể loại"
    :columns="columns"
    :rows="rows"
    :loading="loading"
    :error="loadError"
    add-label="+ Thêm thể loại"
    @add="openCreate"
    @edit="openEdit"
    @delete="remove"
  >
    <template #delete-message="{ row }">Xóa thể loại “{{ row?.name }}”?</template>
  </CrudTable>

  <BaseModal
    :open="modalOpen"
    :title="editingId ? 'Sửa thể loại' : 'Thêm thể loại'"
    size="sm"
    @close="modalOpen = false"
  >
    <form class="space-y-4" @submit.prevent="save">
      <ErrorState v-if="apiError" :message="apiError" />
      <BaseInput v-model="form.name" label="Tên thể loại" required :error="fieldErrors.name" />
    </form>
    <template #footer>
      <BaseButton variant="ghost" @click="modalOpen = false">Hủy</BaseButton>
      <BaseButton :loading="saving" @click="save">Lưu</BaseButton>
    </template>
  </BaseModal>
</template>
