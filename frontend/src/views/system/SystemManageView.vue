<template>
  <div class="page-shell">
    <header class="page-header">
      <div class="page-title">
        <h2>系统管理</h2>
        <p>维护账号、角色和菜单，支撑管理员、仓库管理员、销售员三个角色的差异化访问。</p>
      </div>
    </header>

    <el-tabs v-model="activeTab" class="surface section-block">
      <el-tab-pane label="用户" name="users">
        <div class="toolbar">
          <el-button type="warning" @click="saveUser">新增用户</el-button>
          <el-input v-model="userKeyword" placeholder="用户名/姓名" @change="loadUsers" style="max-width: 240px" />
        </div>
        <el-form :model="userForm" inline class="mini-form">
          <el-form-item label="用户名"><el-input v-model="userForm.username" /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="userForm.realName" /></el-form-item>
          <el-form-item label="密码"><el-input v-model="userForm.password" /></el-form-item>
          <el-form-item label="角色ID"><el-input v-model="roleIdsText" placeholder="1,2" /></el-form-item>
        </el-form>
        <el-table :data="users.records || []" border>
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="realName" label="姓名" />
          <el-table-column prop="phone" label="手机" />
          <el-table-column prop="status" label="状态" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="角色" name="roles">
        <div class="toolbar">
          <el-button type="warning" @click="saveRole">新增角色</el-button>
        </div>
        <el-form :model="roleForm" inline class="mini-form">
          <el-form-item label="编码"><el-input v-model="roleForm.roleCode" /></el-form-item>
          <el-form-item label="名称"><el-input v-model="roleForm.roleName" /></el-form-item>
        </el-form>
        <el-table :data="roles" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="roleCode" label="角色编码" />
          <el-table-column prop="roleName" label="角色名称" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="菜单" name="menus">
        <div class="toolbar">
          <el-button type="warning" @click="saveMenu">新增菜单</el-button>
        </div>
        <el-form :model="menuForm" inline class="mini-form">
          <el-form-item label="上级ID"><el-input v-model="menuForm.parentId" /></el-form-item>
          <el-form-item label="名称"><el-input v-model="menuForm.menuName" /></el-form-item>
          <el-form-item label="路由"><el-input v-model="menuForm.routePath" /></el-form-item>
        </el-form>
        <el-tree :data="menus" node-key="id" default-expand-all :props="{ label: 'menuName', children: 'children' }" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { systemApi } from '@/api/modules'

const activeTab = ref('users')
const userKeyword = ref('')
const users = ref({ records: [] })
const roles = ref([])
const menus = ref([])
const roleIdsText = ref('1')
const userForm = reactive({ username: '', realName: '', password: '123456', roleIds: [1], status: 1 })
const roleForm = reactive({ roleCode: '', roleName: '', status: 1 })
const menuForm = reactive({ parentId: 0, menuName: '', menuType: 'MENU', routePath: '', componentPath: '', visible: 1, sortNo: 1 })

async function loadUsers() {
  users.value = await systemApi.users({ current: 1, size: 20, keyword: userKeyword.value })
}
async function loadRoles() {
  roles.value = await systemApi.roles()
}
async function loadMenus() {
  menus.value = await systemApi.menus()
}
async function saveUser() {
  userForm.roleIds = roleIdsText.value.split(',').filter(Boolean).map((item) => Number(item.trim()))
  await systemApi.createUser(userForm)
  ElMessage.success('用户已保存')
  await loadUsers()
}
async function saveRole() {
  await systemApi.createRole(roleForm)
  ElMessage.success('角色已保存')
  await loadRoles()
}
async function saveMenu() {
  await systemApi.createMenu(menuForm)
  ElMessage.success('菜单已保存')
  await loadMenus()
}

onMounted(async () => {
  await Promise.all([loadUsers(), loadRoles(), loadMenus()])
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.mini-form {
  margin-bottom: 14px;
}
</style>
