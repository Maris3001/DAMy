import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'

const routes = [
  { path: '/', name: 'home', component: HomePage },
  {
    path: '/dang-nhap',
    name: 'login',
    component: () => import('../pages/LoginPage.vue'),
    meta: { guestOnly: true },
  },
  {
    path: '/dang-ky',
    name: 'register',
    component: () => import('../pages/RegisterPage.vue'),
    meta: { guestOnly: true },
  },
  {
    path: '/phim/:id',
    name: 'movie-detail',
    component: () => import('../pages/MovieDetailPage.vue'),
  },
  {
    path: '/the-loai',
    name: 'genres',
    component: () => import('../pages/GenresPage.vue'),
  },
  {
    path: '/rap',
    name: 'cinemas',
    component: () => import('../pages/CinemasPage.vue'),
  },
  {
    path: '/tai-khoan',
    component: () => import('../layouts/AccountLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: { name: 'account-profile' } },
      {
        path: 'ho-so',
        name: 'account-profile',
        component: () => import('../pages/account/ProfilePage.vue'),
      },
      {
        path: 've-cua-toi',
        name: 'account-bookings',
        component: () => import('../pages/account/MyBookingsPage.vue'),
      },
      {
        path: 'diem-thuong',
        name: 'account-points',
        component: () => import('../pages/account/PointsPage.vue'),
      },
      {
        path: 'vi-voucher',
        name: 'account-vouchers',
        component: () => import('../pages/account/VouchersPage.vue'),
      },
      {
        path: 'doi-mat-khau',
        name: 'account-password',
        component: () => import('../pages/account/ChangePasswordPage.vue'),
      },
    ],
  },
  {
    // Đích redirect từ cổng thanh toán (VNPay/mock) — ngoài wizard /dat-ve
    path: '/thanh-toan/ket-qua',
    name: 'payment-result',
    component: () => import('../pages/PaymentResultPage.vue'),
    meta: { requiresAuth: true, userOnly: true },
  },
  // ===== Wizard đặt vé 6 bước (P4) =====
  {
    path: '/dat-ve',
    component: () => import('../pages/booking/BookingLayout.vue'),
    meta: { requiresAuth: true, userOnly: true },
    children: [
      { path: '', redirect: { name: 'booking-region' } },
      {
        path: 'khu-vuc',
        name: 'booking-region',
        component: () => import('../pages/booking/StepRegion.vue'),
        meta: { step: 1 },
      },
      {
        path: 'rap',
        name: 'booking-cinema',
        component: () => import('../pages/booking/StepCinema.vue'),
        meta: { step: 2 },
      },
      {
        path: 'suat-chieu',
        name: 'booking-showtime',
        component: () => import('../pages/booking/StepShowtime.vue'),
        meta: { step: 3 },
      },
      {
        path: 'ghe',
        name: 'booking-seats',
        component: () => import('../pages/booking/StepSeats.vue'),
        meta: { step: 4 },
      },
      {
        path: 'bap-nuoc',
        name: 'booking-concessions',
        component: () => import('../pages/booking/StepConcessions.vue'),
        meta: { step: 5 },
      },
      {
        path: 'thanh-toan',
        name: 'booking-checkout',
        component: () => import('../pages/booking/StepCheckout.vue'),
        meta: { step: 6 },
      },
    ],
  },
  // ===== Khu admin (light theme, layout riêng) =====
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAdmin: true, admin: true },
    children: [
      { path: '', redirect: { name: 'admin-dashboard' } },
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: () => import('../pages/admin/DashboardPage.vue'),
      },
      {
        path: 'movies',
        name: 'admin-movies',
        component: () => import('../pages/admin/MoviesPage.vue'),
      },
      {
        path: 'genres',
        name: 'admin-genres',
        component: () => import('../pages/admin/GenresPage.vue'),
      },
      {
        path: 'showtimes',
        name: 'admin-showtimes',
        component: () => import('../pages/admin/ShowtimesPage.vue'),
      },
      {
        path: 'rooms',
        name: 'admin-rooms',
        component: () => import('../pages/admin/RoomsPage.vue'),
      },
      {
        path: 'rooms/:id/seats',
        name: 'admin-room-seats',
        component: () => import('../pages/admin/RoomSeatsPage.vue'),
      },
      {
        path: 'concessions',
        name: 'admin-concessions',
        component: () => import('../pages/admin/ConcessionsPage.vue'),
      },
      {
        path: 'campaigns',
        name: 'admin-campaigns',
        component: () => import('../pages/admin/CampaignsPage.vue'),
      },
      {
        path: 'offers',
        name: 'admin-offers',
        component: () => import('../pages/admin/OffersPage.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  // Import trong guard để tránh vòng lặp import router ↔ store
  const { useAuthStore } = await import('../stores/auth')
  const auth = useAuthStore()

  // Có token nhưng chưa có user (vừa mở app / F5) → khôi phục phiên
  if (auth.token && !auth.user) {
    await auth.fetchMe().catch(() => {})
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin) {
    if (!auth.isAuthenticated) {
      return { name: 'login', query: { redirect: to.fullPath } }
    }
    if (!auth.isAdmin) {
      return { path: '/' }
    }
  }
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { path: '/' }
  }

  // Luồng đặt vé/thanh toán chỉ dành cho thành viên; admin có khu quản trị riêng.
  if (to.meta.userOnly && auth.isAdmin) {
    return { path: '/admin' }
  }

  // Wizard đặt vé: chặn nhảy cóc quá bước đầu tiên chưa hoàn thành
  if (to.meta.step) {
    const { useBookingStore } = await import('../stores/booking')
    const booking = useBookingStore()
    if (to.meta.step > booking.firstIncompleteStep) {
      const stepRoutes = [
        'booking-region',
        'booking-cinema',
        'booking-showtime',
        'booking-seats',
        'booking-concessions',
        'booking-checkout',
      ]
      return { name: stepRoutes[booking.firstIncompleteStep - 1] }
    }
  }
})

export default router
