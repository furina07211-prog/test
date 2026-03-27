import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/BaseLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard/Home.vue'),
        meta: { title: 'Dashboard' }
      },
      {
        path: 'inventory',
        name: 'inventory',
        component: () => import('@/views/inventory/InventoryView.vue'),
        meta: { title: 'Inventory', roles: ['ADMIN', 'WAREHOUSE'] }
      },
      {
        path: 'purchase',
        name: 'purchase',
        component: () => import('@/views/purchase/PurchaseView.vue'),
        meta: { title: 'Purchase', roles: ['ADMIN', 'WAREHOUSE'] }
      },
      {
        path: 'sales',
        name: 'sales',
        component: () => import('@/views/sales/SalesView.vue'),
        meta: { title: 'Sales', roles: ['ADMIN', 'SALES'] }
      },
      {
        path: 'ai',
        name: 'ai',
        component: () => import('@/views/ai/AiWorkbenchView.vue'),
        meta: { title: 'AI 助手', roles: ['ADMIN', 'WAREHOUSE', 'SALES'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (!userStore.token && to.path !== '/login') {
    return '/login'
  }
  if (to.meta?.roles?.length) {
    const allowed = to.meta.roles.includes(userStore.roleCode)
    if (!allowed) return '/dashboard'
  }
  if (userStore.token && to.path === '/login') {
    return '/dashboard'
  }
  return true
})

export default router
