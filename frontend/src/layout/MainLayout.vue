<template>
  <div class="layout-root">
    <aside class="layout-nav">
      <div class="brand-zone">
        <span class="brand-mark">果仓</span>
        <div>
          <h1>水果仓库管理系统</h1>
          <p>批发仓储与智能决策工作台</p>
        </div>
      </div>
      <nav class="nav-list">
        <button
          v-for="item in visibleNav"
          :key="item.path"
          :class="['nav-item', { active: route.path === item.path }]"
          @click="router.push(item.path)"
        >
          <span>{{ item.title }}</span>
          <small>{{ item.desc }}</small>
        </button>
      </nav>
      <div class="nav-footer">
        <p>角色：{{ authStore.roles.join(' / ') || '未登录' }}</p>
        <el-button text type="warning" @click="logout">退出</el-button>
      </div>
    </aside>

    <main class="layout-main">
      <header class="layout-header">
        <div>
          <p>水果批发仓库管理系统</p>
          <h2>{{ currentTitle }}</h2>
        </div>
        <div class="user-badge">
          <span>{{ authStore.displayName }}</span>
        </div>
      </header>
      <section class="layout-content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const navItems = [
  { path: '/dashboard', title: '数据看板', desc: '实时看板与图表', roles: ['ADMIN', 'WAREHOUSE', 'SALES'] },
  { path: '/system', title: '系统管理', desc: '用户、角色、菜单', roles: ['ADMIN'] },
  { path: '/basic', title: '基础资料', desc: '水果、供应商、客户', roles: ['ADMIN', 'WAREHOUSE', 'SALES'] },
  { path: '/purchase', title: '采购管理', desc: '采购单与批次入库', roles: ['ADMIN', 'WAREHOUSE'] },
  { path: '/sales', title: '销售管理', desc: '销售开单与先进先出', roles: ['ADMIN', 'SALES'] },
  { path: '/inventory', title: '库存管理', desc: '批次、流水、预警', roles: ['ADMIN', 'WAREHOUSE'] },
  { path: '/ai', title: '智能助手', desc: '预测、建议、对话', roles: ['ADMIN', 'WAREHOUSE', 'SALES'] }
]

const visibleNav = computed(() => navItems.filter((item) => item.roles.some((role) => authStore.roles.includes(role))))
const currentTitle = computed(() => route.meta?.title || '工作台')

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
}

.layout-nav {
  background: linear-gradient(180deg, #17211c 0%, #121712 100%);
  color: white;
  padding: 28px 22px;
  display: flex;
  flex-direction: column;
  gap: 26px;
}

.brand-zone {
  display: flex;
  gap: 14px;
  align-items: center;
}

.brand-mark {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f08c2b 0%, #f5c35f 100%);
  color: #1d1f1b;
  font-family: 'Poppins', sans-serif;
  font-weight: 700;
}

.brand-zone p,
.nav-footer p {
  margin: 0;
  color: var(--nav-soft);
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.nav-item {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  color: white;
  border-radius: 18px;
  padding: 14px 16px;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 4px;
  cursor: pointer;
  transition: transform 0.24s ease, background 0.24s ease;
}

.nav-item small {
  color: var(--nav-soft);
}

.nav-item:hover,
.nav-item.active {
  transform: translateX(4px);
  background: rgba(240, 140, 43, 0.18);
}

.nav-footer {
  margin-top: auto;
  padding-top: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.layout-main {
  padding: 22px 24px;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 1px solid var(--line);
  padding-bottom: 16px;
}

.layout-header p {
  margin: 0 0 6px;
  color: var(--text-soft);
}

.layout-content {
  padding-top: 24px;
}

.user-badge {
  background: white;
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 10px 16px;
}

@media (max-width: 980px) {
  .layout-root {
    grid-template-columns: 1fr;
  }

  .layout-nav {
    padding-bottom: 18px;
  }
}
</style>
