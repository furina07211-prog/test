import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '@/layout/index.vue'

Vue.use(VueRouter)

export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/404.vue'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'el-icon-s-home' }
      }
    ]
  }
]

export const asyncRoutes = [
  {
    path: '/system',
    component: Layout,
    redirect: '/system/user',
    name: 'System',
    meta: { title: '系统管理', icon: 'el-icon-setting', roles: ['ADMIN'] },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'el-icon-user' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'el-icon-s-custom' }
      }
    ]
  },
  {
    path: '/base',
    component: Layout,
    redirect: '/base/product',
    name: 'Base',
    meta: { title: '基础数据', icon: 'el-icon-s-management', roles: ['ADMIN', 'WAREHOUSE_MANAGER'] },
    children: [
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/base/product/index.vue'),
        meta: { title: '商品管理', icon: 'el-icon-goods' }
      },
      {
        path: 'supplier',
        name: 'Supplier',
        component: () => import('@/views/base/supplier/index.vue'),
        meta: { title: '供应商管理', icon: 'el-icon-truck' }
      },
      {
        path: 'warehouse',
        name: 'Warehouse',
        component: () => import('@/views/base/warehouse/index.vue'),
        meta: { title: '仓库管理', icon: 'el-icon-office-building' }
      }
    ]
  },
  {
    path: '/inbound',
    component: Layout,
    redirect: '/inbound/list',
    name: 'Inbound',
    meta: { title: '入库管理', icon: 'el-icon-download', roles: ['ADMIN', 'WAREHOUSE_MANAGER', 'WAREHOUSE_WORKER', 'PURCHASER'] },
    children: [
      {
        path: 'list',
        name: 'InboundList',
        component: () => import('@/views/inbound/index.vue'),
        meta: { title: '入库单列表', icon: 'el-icon-document' }
      },
      {
        path: 'create',
        name: 'InboundCreate',
        component: () => import('@/views/inbound/create.vue'),
        meta: { title: '新建入库单', icon: 'el-icon-plus' },
        hidden: true
      }
    ]
  },
  {
    path: '/outbound',
    component: Layout,
    redirect: '/outbound/list',
    name: 'Outbound',
    meta: { title: '出库管理', icon: 'el-icon-upload2', roles: ['ADMIN', 'WAREHOUSE_MANAGER', 'WAREHOUSE_WORKER'] },
    children: [
      {
        path: 'list',
        name: 'OutboundList',
        component: () => import('@/views/outbound/index.vue'),
        meta: { title: '出库单列表', icon: 'el-icon-document' }
      },
      {
        path: 'create',
        name: 'OutboundCreate',
        component: () => import('@/views/outbound/create.vue'),
        meta: { title: '新建出库单', icon: 'el-icon-plus' },
        hidden: true
      }
    ]
  },
  {
    path: '/inventory',
    component: Layout,
    redirect: '/inventory/query',
    name: 'Inventory',
    meta: { title: '库存管理', icon: 'el-icon-box', roles: ['ADMIN', 'WAREHOUSE_MANAGER', 'PURCHASER'] },
    children: [
      {
        path: 'query',
        name: 'InventoryQuery',
        component: () => import('@/views/inventory/index.vue'),
        meta: { title: '库存查询', icon: 'el-icon-search' }
      },
      {
        path: 'batch',
        name: 'BatchManagement',
        component: () => import('@/views/inventory/batch.vue'),
        meta: { title: '批次管理', icon: 'el-icon-tickets' }
      }
    ]
  },
  {
    path: '/alert',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Alert',
        component: () => import('@/views/alert/index.vue'),
        meta: { title: '预警中心', icon: 'el-icon-warning', roles: ['ADMIN', 'WAREHOUSE_MANAGER'] }
      }
    ]
  },
  {
    path: '/statistics',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '统计分析', icon: 'el-icon-s-data', roles: ['ADMIN', 'WAREHOUSE_MANAGER', 'PURCHASER'] }
      }
    ]
  },
  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () => new VueRouter({
  mode: 'history',
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher
}

export default router
