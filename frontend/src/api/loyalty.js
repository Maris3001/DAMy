import http from './http'

// ===== Điểm thưởng / hạng thành viên (cần đăng nhập, chỉ USER) =====

/** Tổng quan điểm + hạng hiện tại + tiến độ lên hạng kế. */
export function getLoyaltySummary() {
  return http.get('/loyalty/summary').then((res) => res.data)
}

/** Lịch sử cộng/trừ điểm (mới nhất trước). */
export function getPointHistory() {
  return http.get('/loyalty/points-history').then((res) => res.data)
}
