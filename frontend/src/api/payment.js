import http from './http'

// ===== Thanh toán (cần đăng nhập) =====

/**
 * Khởi tạo thanh toán cho đơn → trả { payUrl, txnRef }. FE điều hướng cả trang tới payUrl
 * (VNPay thật hoặc cổng giả lập); cổng xử lý xong redirect về /thanh-toan/ket-qua.
 */
export function createPayment(bookingCode) {
  return http.post('/payments', { bookingCode }).then((res) => res.data)
}
