<template>
  <div class="layout-shell" :class="{ 'is-collapsed': isCollapsed }">
    <aside class="sidebar" v-show="!isMobile">
      <div class="brand-wrap">
        <div class="brand-text" v-show="!isCollapsed">
          <strong>水果仓库管理系统</strong>
          <small>智能批发业务管理</small>
        </div>
        <el-button class="collapse-btn" link @click="toggleCollapse">
          <el-icon><component :is="isCollapsed ? Expand : Fold" /></el-icon>
        </el-button>
      </div>

      <el-menu
        class="menu"
        :default-active="active"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </aside>

    <el-drawer v-model="mobileDrawerVisible" direction="ltr" :with-header="false" size="240px" v-if="isMobile">
      <div class="mobile-brand">水果仓库管理系统</div>
      <el-menu :default-active="active" router @select="mobileDrawerVisible = false">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <section class="main-area">
      <header class="topbar">
        <div class="top-left">
          <el-button class="mobile-trigger" link @click="mobileDrawerVisible = true" v-if="isMobile">
            <el-icon><Menu /></el-icon>
          </el-button>
          <div>
            <p class="sub">智能批发业务管理</p>
            <h3 class="title">{{ pageTitle }}</h3>
          </div>
        </div>

        <div class="user-area">
          <el-dropdown>
            <span class="el-dropdown-link">
              <el-avatar size="small">{{ initials }}</el-avatar>
              <span class="username">{{ userStore.username || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="page-padding">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Box, ChatLineSquare, Expand, Fold, Menu, Monitor, ShoppingCart, Tickets } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const menus = [
  { path: '/dashboard', title: '数据看板', icon: Monitor },
  { path: '/inventory', title: '库存管理', icon: Box },
  { path: '/purchase', title: '采购管理', icon: ShoppingCart },
  { path: '/sales', title: '销售管理', icon: Tickets },
  { path: '/ai', title: '智能助手', icon: ChatLineSquare }
]

const pageTitleMap = {
  '/dashboard': '数据看板',
  '/inventory': '库存管理',
  '/purchase': '采购管理',
  '/sales': '销售管理',
  '/ai': '智能助手'
}

const active = computed(() => route.path)
const initials = computed(() => (userStore.username || '用户').slice(0, 1).toUpperCase())
const pageTitle = computed(() => pageTitleMap[route.path] || '工作台')

const isCollapsed = ref(false)
const isMobile = ref(false)
const mobileDrawerVisible = ref(false)

const handleResize = () => {
  const width = window.innerWidth
  isMobile.value = width <= 900
  if (isMobile.value) {
    mobileDrawerVisible.value = false
  }
  if (width <= 1200 && width > 900) {
    isCollapsed.value = true
  }
  if (width > 1200) {
    isCollapsed.value = false
  }
}

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleLogout = () => {
  userStore.logout()
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.layout-shell {
  display: grid;
  grid-template-columns: var(--sidebar-width) 1fr;
  min-height: 100vh;
}

.layout-shell.is-collapsed {
  grid-template-columns: var(--sidebar-collapsed-width) 1fr;
}

.sidebar {
  background: #ffffff;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease;
  overflow: hidden;
}

.brand-wrap {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10px 0 14px;
  border-bottom: 1px solid var(--border);
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-text strong {
  font-size: 14px;
  color: var(--text-main);
}

.brand-text small {
  color: var(--text-secondary);
  font-size: 12px;
}

.collapse-btn {
  color: var(--text-secondary);
}

.menu {
  border-right: none;
  flex: 1;
  padding-top: 8px;
}

.menu :deep(.el-menu-item) {
  margin: 4px 8px;
  border-radius: 8px;
  position: relative;
}

.menu :deep(.el-menu-item.is-active) {
  background: var(--primary-soft);
  color: var(--primary);
  font-weight: 600;
}

.menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 3px;
  background: var(--primary);
}

.mobile-brand {
  font-weight: 600;
  margin-bottom: 10px;
}

.main-area {
  min-height: 100vh;
  background: var(--bg-page);
}

.topbar {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mobile-trigger {
  font-size: 18px;
}

.sub {
  margin: 0;
  color: var(--text-secondary);
  font-size: 12px;
}

.title {
  margin: 2px 0 0;
  font-size: 18px;
}

.username {
  margin-left: 8px;
  color: var(--text-main);
}

@media (max-width: 900px) {
  .layout-shell,
  .layout-shell.is-collapsed {
    grid-template-columns: 1fr;
  }

  .topbar {
    padding: 0 12px;
  }

  .title {
    font-size: 16px;
  }
}
</style>
