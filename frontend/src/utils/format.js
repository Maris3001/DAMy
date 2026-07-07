// Định dạng dùng chung — theo DESIGN.md §1.3. Dùng ở mọi nơi, không tự viết lại.

const WEEKDAYS = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7']

function toDate(value) {
  if (value instanceof Date) return value
  if (value == null) return null
  // Chuỗi LocalDateTime từ backend không có timezone → parse theo giờ máy (giờ VN khi demo).
  return new Date(value)
}

/** 120000 → "120.000 ₫" (dấu chấm ngăn nghìn, ký hiệu ₫ sau số). */
export function formatVnd(amount) {
  const n = Number(amount ?? 0)
  return `${n.toLocaleString('vi-VN')} ₫`
}

/** Date/ISO → "Thứ 7, 12/07/2026". */
export function formatDate(value) {
  const d = toDate(value)
  if (!d || Number.isNaN(d.getTime())) return ''
  const dd = String(d.getDate()).padStart(2, '0')
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  return `${WEEKDAYS[d.getDay()]}, ${dd}/${mm}/${d.getFullYear()}`
}

/** Date/ISO → "19:30" (24h, không AM/PM). */
export function formatTime(value) {
  const d = toDate(value)
  if (!d || Number.isNaN(d.getTime())) return ''
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${hh}:${mi}`
}

/** Nhãn tab ngày ngắn: "T7 12/07" (dùng cho hàng tab 7 ngày). */
export function formatDayTab(value) {
  const d = toDate(value)
  if (!d || Number.isNaN(d.getTime())) return ''
  const label = d.getDay() === 0 ? 'CN' : `T${d.getDay() + 1}`
  const dd = String(d.getDate()).padStart(2, '0')
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  return `${label} ${dd}/${mm}`
}

/** Số giây → "mm:ss" (đồng hồ đếm ngược giữ ghế). */
export function formatCountdown(seconds) {
  const s = Math.max(0, Math.floor(Number(seconds) || 0))
  const mm = String(Math.floor(s / 60)).padStart(2, '0')
  const ss = String(s % 60).padStart(2, '0')
  return `${mm}:${ss}`
}

/** Date → "YYYY-MM-DD" (tham số ngày gửi lên API). */
export function toDateParam(value) {
  const d = toDate(value)
  if (!d || Number.isNaN(d.getTime())) return ''
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mm}-${dd}`
}
