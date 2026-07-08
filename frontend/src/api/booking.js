import http from './http'

// ===== Đặt vé (cần đăng nhập) =====

/** Sơ đồ ghế của suất: mỗi ghế có state AVAILABLE | MINE | HELD | BOOKED + giá. */
export function getSeatMap(showtimeId) {
  return http.get(`/showtimes/${showtimeId}/seats`).then((res) => res.data)
}

export function holdSeats(showtimeId, seatIds) {
  return http.post('/bookings/hold', { showtimeId, seatIds }).then((res) => res.data)
}

export function releaseSeats(showtimeId, seatIds) {
  return http.delete('/bookings/hold', { data: { showtimeId, seatIds } }).then((res) => res.data)
}

/** concessions: map { id: qty } → chuỗi "id:qty,id:qty" cho query param. */
export function getQuote(showtimeId, concessions = {}) {
  const param = Object.entries(concessions)
    .filter(([, qty]) => qty > 0)
    .map(([id, qty]) => `${id}:${qty}`)
    .join(',')
  return http
    .get('/bookings/quote', { params: { showtimeId, concessions: param || undefined } })
    .then((res) => res.data)
}

/** Tạo đơn PENDING_PAYMENT từ ghế đang giữ; concessions: [{concessionId, quantity}]. */
export function createBooking(showtimeId, concessions = []) {
  return http.post('/bookings', { showtimeId, concessions }).then((res) => res.data)
}

/** Chi tiết đơn theo mã (kèm vé nếu đã thanh toán) — chỉ chủ đơn xem được. */
export function getBooking(code) {
  return http.get(`/bookings/${code}`).then((res) => res.data)
}

/** Danh sách đơn của user hiện tại (mới nhất trước) — trang "Vé của tôi". */
export function listMyBookings() {
  return http.get('/bookings').then((res) => res.data)
}
