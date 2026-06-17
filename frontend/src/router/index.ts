import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useAuthStore } from '@/stores/auth'
import type { UserRole } from '@/types'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    role?: UserRole
  }
}

NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/HomeView.vue'),
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/product/ProductListView.vue'),
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/product/ProductDetailView.vue'),
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/product/ProductListView.vue'),
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/cart/CartView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/order/CheckoutView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderListView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'orders/:orderNo',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetailView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'user/profile',
        name: 'Profile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'user/addresses',
        name: 'Addresses',
        component: () => import('@/views/user/AddressView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'user/notifications',
        name: 'Notifications',
        component: () => import('@/views/user/NotificationView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'seckill',
        name: 'SeckillList',
        component: () => import('@/views/seckill/SeckillListView.vue'),
      },
      {
        path: 'seckill/:id',
        name: 'SeckillPurchase',
        component: () => import('@/views/seckill/SeckillPurchaseView.vue'),
        meta: { requiresAuth: true },
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
  },
  {
    path: '/merchant',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true, role: 'merchant' },
    children: [
      {
        path: 'products',
        name: 'MerchantProductList',
        component: () => import('@/views/merchant/MerchantProductList.vue'),
      },
      {
        path: 'products/new',
        name: 'MerchantProductCreate',
        component: () => import('@/views/merchant/ProductForm.vue'),
      },
      {
        path: 'products/:id/edit',
        name: 'MerchantProductEdit',
        component: () => import('@/views/merchant/ProductForm.vue'),
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/MerchantOrders.vue'),
      },
      {
        path: 'refunds',
        name: 'MerchantRefunds',
        component: () => import('@/views/merchant/MerchantRefunds.vue'),
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
      },
      {
        path: 'merchants',
        name: 'MerchantApproval',
        component: () => import('@/views/admin/MerchantApproval.vue'),
      },
      {
        path: 'products',
        name: 'ProductAudit',
        component: () => import('@/views/admin/ProductAudit.vue'),
      },
      {
        path: 'disputes',
        name: 'DisputeView',
        component: () => import('@/views/admin/DisputeView.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

function requiresAuth(route: { matched: { meta: { requiresAuth?: boolean } }[] }) {
  return route.matched.some((record) => record.meta.requiresAuth)
}

function requiredRole(route: {
  matched: { meta: { role?: UserRole } }[]
}): UserRole | undefined {
  const record = [...route.matched].reverse().find((r) => r.meta.role)
  return record?.meta.role
}

router.beforeEach((to, _from, next) => {
  NProgress.start()

  const authStore = useAuthStore()

  if (requiresAuth(to) && !authStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  const role = requiredRole(to)
  if (role && authStore.userRole !== role) {
    next('/403')
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
