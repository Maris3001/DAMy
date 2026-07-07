import { defineStore } from 'pinia'

/**
 * State wizard đặt vé 6 bước, persist sessionStorage (mất khi đóng tab — đúng ý:
 * hold ghế cũng chỉ sống 10 phút). Mỗi action ghi đều gọi persist() — không dùng plugin.
 */
const STORAGE_KEY = 'lvc_booking'

function emptyState() {
  return {
    region: null, // { id, name }
    cinema: null, // { id, name, address, chainName }
    showtime: null, // { id, startsAt, basePrice, roomName, roomType, movieId, movieTitle, moviePosterUrl }
    seats: [], // [{ seatId, label, seatType, price }] — bản sao hold trên server
    holdExpiresAt: null, // ISO string
    concessions: [], // [{ id, name, price, qty }]
    booking: null, // BookingResponse sau khi tạo đơn
  }
}

function loadPersisted() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? { ...emptyState(), ...JSON.parse(raw) } : emptyState()
  } catch {
    return emptyState()
  }
}

export const useBookingStore = defineStore('booking', {
  state: () => loadPersisted(),

  getters: {
    holdAlive: (state) =>
      state.seats.length > 0 &&
      Boolean(state.holdExpiresAt) &&
      new Date(state.holdExpiresAt).getTime() > Date.now(),

    /**
     * Bước đầu tiên chưa hoàn thành — guard chặn nhảy cóc quá bước này.
     * Chọn suất từ trang chi tiết phim điền sẵn 1–3 nên vào thẳng bước 4 hợp lệ.
     */
    firstIncompleteStep() {
      if (this.booking) return 6 // đã có đơn → luôn xem được màn kết quả
      if (!this.region) return 1
      if (!this.cinema) return 2
      if (!this.showtime) return 3
      if (!this.holdAlive) return 4
      return 6 // bước 5 (bắp nước) bỏ qua được → còn hold là đi được tới thanh toán
    },

    ticketTotal: (state) => state.seats.reduce((sum, s) => sum + s.price, 0),

    concessionTotal: (state) => state.concessions.reduce((sum, c) => sum + c.price * c.qty, 0),

    grandTotal() {
      return this.ticketTotal + this.concessionTotal
    },

    /** [{concessionId, quantity}] cho POST /bookings. */
    concessionLines: (state) =>
      state.concessions
        .filter((c) => c.qty > 0)
        .map((c) => ({ concessionId: c.id, quantity: c.qty })),

    /** { id: qty } cho GET /bookings/quote. */
    concessionQtyMap: (state) =>
      Object.fromEntries(state.concessions.filter((c) => c.qty > 0).map((c) => [c.id, c.qty])),
  },

  actions: {
    persist() {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(this.$state))
    },

    setRegion(region) {
      this.region = region
      // Đổi khu vực → mọi lựa chọn phía sau vô nghĩa
      this.cinema = null
      this.setShowtime(null)
    },

    setCinema(cinema) {
      this.cinema = cinema
      this.setShowtime(null)
    },

    setShowtime(showtime) {
      this.showtime = showtime
      this.seats = []
      this.holdExpiresAt = null
      this.concessions = []
      this.booking = null
      this.persist()
    },

    /** Vào wizard từ trang chi tiết phim: điền sẵn bước 1–3 rồi nhảy tới chọn ghế. */
    enterFromMovie({ region, cinema, showtime }) {
      this.$state = emptyState()
      this.region = region
      this.cinema = cinema
      this.showtime = showtime
      this.persist()
    },

    /** Đồng bộ ghế đang giữ theo phản hồi server (nguồn sự thật là server). */
    syncHolds({ seats, holdExpiresAt }) {
      this.seats = seats
      this.holdExpiresAt = holdExpiresAt
      this.persist()
    },

    clearSeats() {
      this.seats = []
      this.holdExpiresAt = null
      this.persist()
    },

    setConcessionQty(item, qty) {
      const existing = this.concessions.find((c) => c.id === item.id)
      if (existing) {
        existing.qty = qty
      } else if (qty > 0) {
        this.concessions.push({ id: item.id, name: item.name, price: item.price, qty })
      }
      this.concessions = this.concessions.filter((c) => c.qty > 0)
      this.persist()
    },

    setBooking(booking) {
      this.booking = booking
      this.persist()
    },

    reset() {
      this.$state = emptyState()
      sessionStorage.removeItem(STORAGE_KEY)
    },
  },
})
