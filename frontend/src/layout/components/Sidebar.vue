<template>
  <div class="sidebar">
    <div class="logo-container">
      <img src="@/assets/logo.png" alt="logo" class="logo" v-if="sidebarOpened" />
      <h1 class="title" v-if="sidebarOpened">水果仓库管理</h1>
      <img src="@/assets/logo.png" alt="logo" class="logo-small" v-else />
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="!sidebarOpened"
      :collapse-transition="false"
      :unique-opened="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      mode="vertical"
    >
      <template v-for="route in routes">
        <template v-if="!route.hidden">
          <el-submenu
            v-if="route.children && route.children.filter(c => !c.hidden).length > 1"
            :index="route.path"
            :key="route.path"
          >
            <template slot="title">
              <i :class="route.meta && route.meta.icon"></i>
              <span>{{ route.meta && route.meta.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children.filter(c => !c.hidden)"
              :key="child.path"
              :index="resolvePath(route.path, child.path)"
            >
              <i :class="child.meta && child.meta.icon"></i>
              <span slot="title">{{ child.meta && child.meta.title }}</span>
            </el-menu-item>
          </el-submenu>
          <el-menu-item
            v-else
            :index="getFirstChildPath(route)"
            :key="route.path"
          >
            <i :class="getIcon(route)"></i>
            <span slot="title">{{ getTitle(route) }}</span>
          </el-menu-item>
        </template>
      </template>
    </el-menu>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Sidebar',
  computed: {
    ...mapGetters(['sidebarOpened', 'routes']),
    activeMenu() {
      const { path } = this.$route
      return path
    }
  },
  methods: {
    resolvePath(basePath, childPath) {
      if (childPath.startsWith('/')) {
        return childPath
      }
      return `${basePath}/${childPath}`.replace(/\/+/g, '/')
    },
    getFirstChildPath(route) {
      if (route.children && route.children.length > 0) {
        const firstChild = route.children.find(c => !c.hidden) || route.children[0]
        return this.resolvePath(route.path, firstChild.path)
      }
      return route.path
    },
    getIcon(route) {
      if (route.children && route.children.length === 1) {
        return route.children[0].meta && route.children[0].meta.icon
      }
      return route.meta && route.meta.icon
    },
    getTitle(route) {
      if (route.children && route.children.length === 1) {
        return route.children[0].meta && route.children[0].meta.title
      }
      return route.meta && route.meta.title
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background-color: #2b3649;

  .logo {
    width: 32px;
    height: 32px;
    margin-right: 8px;
  }

  .logo-small {
    width: 32px;
    height: 32px;
  }

  .title {
    color: #fff;
    font-size: 14px;
    font-weight: 600;
    white-space: nowrap;
  }
}

.el-menu {
  border: none;
  flex: 1;
  overflow-y: auto;
}

:deep(.el-submenu__title),
:deep(.el-menu-item) {
  &:hover {
    background-color: #263445 !important;
  }
}

:deep(.el-menu-item.is-active) {
  background-color: #409EFF !important;
}
</style>
