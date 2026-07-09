import http from './http'

// ===== Voucher / ưu đãi thành viên (cần đăng nhập, chỉ USER) =====

/** Ví voucher: tất cả phiếu của user (mới nhất trước). */
export function getMyVouchers() {
  return http.get('/vouchers').then((res) => res.data)
}

/** Danh mục ưu đãi đổi điểm + trạng thái đủ điều kiện của user. */
export function getRedeemableCampaigns() {
  return http.get('/vouchers/redeemable').then((res) => res.data)
}

/** Đổi điểm lấy voucher theo campaign. */
export function redeemVoucher(campaignId) {
  return http.post('/vouchers/redeem', { campaignId }).then((res) => res.data)
}
