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
    path: '/tai-khoan',
    name: 'profile',
    component: () => import('../pages/account/ProfilePage.vue'),
    meta: { requiresAuth: true },
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
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { path: '/' }
  }
})

export default router
