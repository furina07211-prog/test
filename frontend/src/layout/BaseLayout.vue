<template>
  <div class="layout-shell">
    <aside class="sidebar">
      <div class="brand">Fruit Warehouse</div>
      <el-menu :default-active="active" router class="menu" background-color="#fff" text-color="#1f2937" active-text-color="#5c7cfa">
        <el-menu-item index="/dashboard"><el-icon><Monitor /></el-icon><span>Dashboard</span></el-menu-item>
        <el-menu-item index="/inventory"><el-icon><Box /></el-icon><span>Inventory</span></el-menu-item>
        <el-menu-item index="/purchase"><el-icon><ShoppingCart /></el-icon><span>Purchase</span></el-menu-item>
        <el-menu-item index="/sales"><el-icon><Tickets /></el-icon><span>Sales</span></el-menu-item>
        <el-menu-item index="/ai"><el-icon><ChatLineSquare /></el-icon><span>AI 助手</span></el-menu-item>
      </el-menu>
    </aside>

    <section class="main-area">
      <header class="topbar">
        <div class="breadcrumbs">Smart wholesale ops</div>
        <div class="user-area">
          <el-dropdown>
            <span class="el-dropdown-link">
              <el-avatar size="small">{{ initials }}</el-avatar>
              <span class="username">{{ userStore.username || 'User' }}</span>
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
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Box, ChatLineSquare, Monitor, ShoppingCart, Tickets } from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()

const active = computed(() => route.path)
const initials = computed(() => (userStore.username || 'U').slice(0, 1).toUpperCase())

function handleLogout() {
  userStore.logout()
}
</script>

<style scoped>
.layout-shell {
  display: grid;
  grid-template-columns: 240px 1fr;
  min-height: 100vh;
}

.sidebar {
  background: #ffffff;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
}

.brand {
  font-weight: 700;
  letter-spacing: 0.08em;
  padding: 18px;
  border-bottom: 1px solid var(--border);
  color: var(--primary);
}

.menu {
  border-right: none;
  flex: 1;
}

.main-area {
  background: var(--bg-soft);
  min-height: 100vh;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 18px;
  background: #fff;
  border-bottom: 1px solid var(--border);
}

.username {
  margin-left: 8px;
}

.page-padding {
  padding: 18px 20px;
}
</style>
