<script setup>
// Admin voucher/chiến dịch KM: CrudTable + form modal.
import { onMounted, reactive, ref } from 'vue'
import {
  adminListCampaigns,
  adminCreateCampaign,
  adminUpdateCampaign,
  adminDeleteCampaign,
} from '../../api/admin'
import { getApiMessage, getFieldErrors } from '../../api/http'
import { voucherDiscountLabel } from '../../utils/voucher'
import CrudTable from '../../components/admin/CrudTable.vue'
import BaseModal from '../../components/ui/BaseModal.vue'
import BaseInput from '../../components/ui/BaseInput.vue'
import BaseSelect from '../../components/ui/BaseSelect.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import ErrorState from '../../components/ui/ErrorState.vue'

const TYPE_OPTIONS = [
  { value: 'PERCENT', label: 'Phần trăm (%)' },
  { value: 'FIXED', label: 'Số tiền cố định (₫)' },
]
const TRIGGER_OPTIONS = [
  { value: 'MANUAL', label: 'Thủ công / công khai' },
  { value: 'REDEEM', label: 'Đổi điểm' },
  { value: 'BIRTHDAY', label: 'Sinh nhật (OfferEngine)' },
  { value: 'WINBACK', label: 'Win-back (OfferEngine)' },
  { value: 'GENRE_FAVORITE', label: 'Thể loại yêu thích (OfferEngine)' },
  { value: 'TIER_UP', label: 'Quà lên hạng (OfferEngine)' },
]
const TIER_OPTIONS = [
  { value: '', label: 'Mọi hạng' },
  { value: 'SILVER', label: 'Bạc' },
  { value: 'GOLD', label: 'Vàng' },
  { value: 'PLATINUM', label: 'Bạch kim' },
]
const TRIGGER_LABEL = Object.fromEntries(TRIGGER_OPTIONS.map((o) => [o.value, o.label]))

const columns = [
  { key: 'code', label: 'Mã' },
  { key: 'name', label: 'Tên chương trình' },
  { key: 'discount', label: 'Ưu đãi' },
  { key: 'trigger', label: 'Nguồn' },
  { key: 'points', label: 'Đổi điểm' },
  { key: 'issued', label: 'Đã phát' },
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
  return {
    code: '',
    name: '',
    description: '',
    voucherType: 'PERCENT',
    discountValue: 10,
    maxDiscountAmount: '',
    minOrderAmount: 0,
    minTier: '',
    pointsCost: '',
    triggerType: 'MANUAL',
    validDays: 30,
    quantity: '',
    perUserLimit: 1,
    active: true,
  }
}

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    rows.value = await adminListCampaigns()
  } catch (e) {
    loadError.value = getApiMessage(e, 'Không tải được danh sách chương trình')
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
    code: row.code,
    name: row.name,
    description: row.description ?? '',
    voucherType: row.voucherType,
    discountValue: row.discountValue,
    maxDiscountAmount: row.maxDiscountAmount ?? '',
    minOrderAmount: row.minOrderAmount,
    minTier: row.minTier ?? '',
    pointsCost: row.pointsCost ?? '',
    triggerType: row.triggerType,
    validDays: row.validDays,
    quantity: row.quantity ?? '',
    perUserLimit: row.perUserLimit,
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
    code: form.code,
    name: form.name,
    description: form.description || null,
    voucherType: form.voucherType,
    discountValue: Number(form.discountValue),
    maxDiscountAmount: form.maxDiscountAmount === '' ? null : Number(form.maxDiscountAmount),
    minOrderAmount: Number(form.minOrderAmount),
    minTier: form.minTier || null,
    pointsCost: form.pointsCost === '' ? null : Number(form.pointsCost),
    triggerType: form.triggerType,
    validDays: Number(form.validDays),
    quantity: form.quantity === '' ? null : Number(form.quantity),
    perUserLimit: Number(form.perUserLimit),
    active: form.active,
  }
  try {
    if (editingId.value) {
      await adminUpdateCampaign(editingId.value, payload)
    } else {
      await adminCreateCampaign(payload)
    }
    modalOpen.value = false
    await load()
  } catch (e) {
    fieldErrors.value = getFieldErrors(e)
    if (Object.keys(fieldErrors.value).length === 0) {
      apiError.value = getApiMessage(e, 'Lưu chương trình thất bại')
    }
  } finally {
    saving.value = false
  }
}
async function remove(row) {
  try {
    await adminDeleteCampaign(row.id)
    await load()
  } catch (e) {
    loadError.value = getApiMessage(e, 'Xóa chương trình thất bại')
  }
}

onMounted(load)
</script>

<template>
  <CrudTable
    title="Voucher & chiến dịch"
    :columns="columns"
    :rows="rows"
    :loading="loading"
    :error="loadError"
    add-label="+ Thêm chương trình"
    @add="openCreate"
    @edit="openEdit"
    @delete="remove"
  >
    <template #cell-code="{ row }"><span class="font-mono text-sm">{{ row.code }}</span></template>
    <template #cell-discount="{ row }">{{ voucherDiscountLabel(row) }}</template>
    <template #cell-trigger="{ row }">{{ TRIGGER_LABEL[row.triggerType] ?? row.triggerType }}</template>
    <template #cell-points="{ row }">
      <span v-if="row.pointsCost != null">{{ row.pointsCost.toLocaleString('vi-VN') }} điểm</span>
      <span v-else class="text-gray-400">—</span>
    </template>
    <template #cell-issued="{ row }">{{ row.issuedCount }}</template>
    <template #cell-active="{ row }">
      <BaseBadge :variant="row.active ? 'success' : 'gray'">
        {{ row.active ? 'Đang chạy' : 'Tắt' }}
      </BaseBadge>
    </template>
    <template #delete-message="{ row }">Xóa chương trình “{{ row?.name }}”?</template>
  </CrudTable>

  <BaseModal
    :open="modalOpen"
    :title="editingId ? 'Sửa chương trình' : 'Thêm chương trình'"
    size="lg"
    @close="modalOpen = false"
  >
    <form class="space-y-4" @submit.prevent="save">
      <ErrorState v-if="apiError" :message="apiError" />
      <div class="grid gap-4 sm:grid-cols-2">
        <BaseInput v-model="form.code" label="Mã campaign" required :error="fieldErrors.code" />
        <BaseInput v-model="form.name" label="Tên chương trình" required :error="fieldErrors.name" />
      </div>
      <BaseInput v-model="form.description" label="Mô tả" :error="fieldErrors.description" />

      <div class="grid gap-4 sm:grid-cols-2">
        <BaseSelect v-model="form.voucherType" label="Kiểu giảm" :options="TYPE_OPTIONS" />
        <BaseInput
          v-model="form.discountValue"
          type="number"
          :label="form.voucherType === 'PERCENT' ? 'Phần trăm giảm (%)' : 'Số tiền giảm (₫)'"
          required
          :error="fieldErrors.discountValue"
        />
      </div>

      <div class="grid gap-4 sm:grid-cols-2">
        <BaseInput
          v-model="form.maxDiscountAmount"
          type="number"
          label="Trần giảm (₫, để trống nếu không chặn)"
          :error="fieldErrors.maxDiscountAmount"
        />
        <BaseInput
          v-model="form.minOrderAmount"
          type="number"
          label="Đơn tối thiểu (₫)"
          required
          :error="fieldErrors.minOrderAmount"
        />
      </div>

      <div class="grid gap-4 sm:grid-cols-2">
        <BaseSelect v-model="form.triggerType" label="Nguồn phát sinh" :options="TRIGGER_OPTIONS" />
        <BaseSelect v-model="form.minTier" label="Hạng tối thiểu" :options="TIER_OPTIONS" />
      </div>

      <div class="grid gap-4 sm:grid-cols-3">
        <BaseInput
          v-model="form.pointsCost"
          type="number"
          label="Điểm đổi (để trống nếu không đổi điểm)"
          :error="fieldErrors.pointsCost"
        />
        <BaseInput v-model="form.validDays" type="number" label="Số ngày hiệu lực" required :error="fieldErrors.validDays" />
        <BaseInput v-model="form.perUserLimit" type="number" label="Giới hạn / người" required :error="fieldErrors.perUserLimit" />
      </div>

      <BaseInput
        v-model="form.quantity"
        type="number"
        label="Tổng số lượng (để trống nếu không giới hạn)"
        :error="fieldErrors.quantity"
      />

      <label class="flex items-center gap-2 text-sm text-ink-300">
        <input v-model="form.active" type="checkbox" class="size-4" />
        Đang chạy
      </label>
    </form>
    <template #footer>
      <BaseButton variant="ghost" @click="modalOpen = false">Hủy</BaseButton>
      <BaseButton :loading="saving" @click="save">Lưu</BaseButton>
    </template>
  </BaseModal>
</template>
