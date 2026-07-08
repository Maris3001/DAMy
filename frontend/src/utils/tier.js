// Hiển thị hạng thành viên nhất quán (DESIGN.md §1.2): Silver bạc, Gold vàng brand, Platinum indigo.
// Dùng chung cho badge hạng, thanh tiến độ và bảng quyền lợi.

export const TIER_ORDER = ['SILVER', 'GOLD', 'PLATINUM']

export const TIERS = {
  SILVER: {
    label: 'Silver',
    variant: 'gray',
    benefits: ['Tích 1 điểm cho mỗi 10.000 ₫', 'Nhận ưu đãi sinh nhật'],
  },
  GOLD: {
    label: 'Gold',
    variant: 'brand',
    benefits: ['Mọi quyền lợi hạng Silver', 'Ưu đãi theo phim & rạp yêu thích', 'Voucher đổi điểm giá tốt hơn'],
  },
  PLATINUM: {
    label: 'Platinum',
    variant: 'platinum',
    benefits: ['Mọi quyền lợi hạng Gold', 'Ưu tiên suất chiếu đặc biệt', 'Quà tặng khi lên hạng'],
  },
}

export function tierLabel(tier) {
  return TIERS[tier]?.label ?? tier ?? '—'
}

export function tierVariant(tier) {
  return TIERS[tier]?.variant ?? 'neutral'
}
