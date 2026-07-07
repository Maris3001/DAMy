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
    path: '/tai-khoan',
    name: 'profile',
    component: () => import('../pages/account/ProfilePage.vue'),
    meta: { requiresAuth: true },
  },
  // ===== Wizard đặt vé 6 bước (P4) =====
  {
    path: '/dat-ve',
    component: () => import('../pages/booking/BookingLayout.vue'),
    meta: { requiresAuth: true },
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
