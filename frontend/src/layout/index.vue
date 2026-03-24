<template>
  <div class="app-wrapper" :class="{ 'sidebar-collapsed': !sidebarOpened }">
    <sidebar class="sidebar-container" />
    <div class="main-container">
      <navbar />
      <app-main />
    </div>
  </div>
</template>

<script>
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'
import { mapGetters } from 'vuex'

export default {
  name: 'Layout',
  components: {
    Sidebar,
    Navbar,
    AppMain
  },
  computed: {
    ...mapGetters(['sidebarOpened'])
  }
}
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  height: 100%;
  width: 100%;
}

.sidebar-container {
  width: $sidebar-width;
  height: 100%;
  background-color: #304156;
  transition: width 0.3s;
  flex-shrink: 0;
  overflow: hidden;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.sidebar-collapsed {
  .sidebar-container {
    width: $sidebar-collapsed-width;
  }
}
</style>
