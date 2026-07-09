import { formatVnd } from './format'

/** Nhãn mức giảm của voucher/campaign: "Giảm 15% (tối đa 50.000 ₫)" hoặc "Giảm 20.000 ₫". */
export function voucherDiscountLabel(v) {
  if (v.voucherType === 'PERCENT') {
    const cap = v.maxDiscountAmount ? ` (tối đa ${formatVnd(v.maxDiscountAmount)})` : ''
    return `Giảm ${v.discountValue}%${cap}`
  }
  return `Giảm ${formatVnd(v.discountValue)}`
}

/** Nhãn + màu badge theo trạng thái phiếu. */
export const VOUCHER_STATUS = {
  AVAILABLE: { label: 'Có thể dùng', variant: 'success' },
  RESERVED: { label: 'Đang giữ', variant: 'warning' },
  USED: { label: 'Đã dùng', variant: 'gray' },
  EXPIRED: { label: 'Hết hạn', variant: 'danger' },
}
