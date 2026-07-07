<script setup>
// Bảng CRUD tái sử dụng cho khu admin (P-11) — light theme.
// Cột: [{ key, label, class? }]. Ô tùy biến qua slot #cell-<key>="{ row }".
import { ref } from 'vue'
import BaseModal from '../ui/BaseModal.vue'
import BaseButton from '../ui/BaseButton.vue'

const props = defineProps({
  title: { type: String, default: '' },
  columns: { type: Array, required: true },
  rows: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  addLabel: { type: String, default: '+ Thêm mới' },
  searchable: { type: Boolean, default: false },
  searchPlaceholder: { type: String, default: 'Tìm kiếm…' },
  rowKey: { type: String, default: 'id' },
  deletable: { type: Boolean, default: true },
  editable: { type: Boolean, default: true },
})
const emit = defineEmits(['add', 'edit', 'delete'])

const search = defineModel('search', { type: String, default: '' })

const confirmRow = ref(null)

function askDelete(row) {
  confirmRow.value = row
}
function confirmDelete() {
  emit('delete', confirmRow.value)
  confirmRow.value = null
}
</script>

<template>
  <div>
    <!-- Thanh công cụ -->
    <div class="mb-4 flex flex-wrap items-center gap-3">
      <h1 v-if="title" class="mr-auto text-xl font-semibold text-gray-900">{{ title }}</h1>
      <input
        v-if="searchable"
        v-model="search"
        type="search"
        :placeholder="searchPlaceholder"
        class="h-9 w-56 rounded-md border border-gray-300 bg-white px-3 text-sm text-gray-900 outline-none focus:border-brand-600"
      />
      <slot name="filters" />
      <button
        type="button"
        class="h-9 rounded-lg bg-brand-600 px-4 text-sm font-medium text-white transition-colors duration-150 hover:bg-brand-500"
        @click="emit('add')"
      >
        {{ addLabel }}
      </button>
    </div>

    <!-- Bảng -->
    <div class="overflow-x-auto rounded-lg border border-gray-200 bg-white shadow-sm">
      <table class="w-full text-left text-sm">
        <thead>
          <tr class="border-b border-gray-200 text-xs uppercase tracking-wide text-gray-500">
            <th v-for="col in columns" :key="col.key" class="px-4 py-3" :class="col.class">
              {{ col.label }}
            </th>
            <th v-if="editable || deletable" class="px-4 py-3 text-right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td :colspan="columns.length + 1" class="px-4 py-10 text-center text-gray-400">
              Đang tải…
            </td>
          </tr>
          <tr v-else-if="error">
            <td :colspan="columns.length + 1" class="px-4 py-10 text-center text-danger">
              {{ error }}
            </td>
          </tr>
          <tr v-else-if="rows.length === 0">
            <td :colspan="columns.length + 1" class="px-4 py-10 text-center text-gray-400">
              <slot name="empty">Chưa có dữ liệu.</slot>
            </td>
          </tr>
          <tr
            v-for="row in rows"
            v-else
            :key="row[rowKey]"
            class="border-b border-gray-100 last:border-0 hover:bg-gray-50"
          >
            <td v-for="col in columns" :key="col.key" class="px-4 py-3 align-middle" :class="col.class">
              <slot :name="`cell-${col.key}`" :row="row">{{ row[col.key] }}</slot>
            </td>
            <td v-if="editable || deletable" class="px-4 py-3 text-right whitespace-nowrap">
              <button
                v-if="editable"
                type="button"
                class="rounded px-2 py-1 text-brand-600 hover:bg-brand-600/10"
                @click="emit('edit', row)"
              >
                Sửa
              </button>
              <button
                v-if="deletable"
                type="button"
                class="rounded px-2 py-1 text-danger hover:bg-danger/10"
                @click="askDelete(row)"
              >
                Xóa
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Phân trang (tùy chọn) -->
    <div v-if="$slots.pagination" class="mt-4 flex justify-end">
      <slot name="pagination" />
    </div>

    <!-- Xác nhận xóa -->
    <BaseModal :open="Boolean(confirmRow)" title="Xác nhận xóa" size="sm" @close="confirmRow = null">
      <p class="text-ink-300">
        <slot name="delete-message" :row="confirmRow">Bạn có chắc muốn xóa mục này?</slot>
      </p>
      <p class="mt-2 text-sm text-danger">Hành động này không thể hoàn tác.</p>
      <template #footer>
        <BaseButton variant="ghost" @click="confirmRow = null">Hủy</BaseButton>
        <BaseButton variant="danger" @click="confirmDelete">Xóa</BaseButton>
      </template>
    </BaseModal>
  </div>
</template>
