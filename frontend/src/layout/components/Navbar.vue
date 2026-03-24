<template>
  <div class="navbar">
    <div class="left-menu">
      <i
        :class="sidebarOpened ? 'el-icon-s-fold' : 'el-icon-s-unfold'"
        class="hamburger"
        @click="toggleSidebar"
      ></i>
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.meta.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="right-menu">
      <el-badge :value="alertCount" :hidden="alertCount === 0" class="alert-badge">
        <i class="el-icon-bell" @click="$router.push('/alert/index')"></i>
      </el-badge>
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" icon="el-icon-user-solid"></el-avatar>
          <span class="username">{{ userInfo ? userInfo.realName : '' }}</span>
          <i class="el-icon-arrow-down"></i>
        </div>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="password">修改密码</el-dropdown-item>
          <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <el-dialog title="修改密码" :visible.sync="passwordDialogVisible" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPassword">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getAlertCount } from '@/api/alert'
import { changePassword } from '@/api/auth'

export default {
  name: 'Navbar',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    return {
      alertCount: 0,
      passwordDialogVisible: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['sidebarOpened', 'userInfo']),
    breadcrumbs() {
      return this.$route.matched.filter(item => item.meta && item.meta.title)
    }
  },
  created() {
    this.fetchAlertCount()
    this.timer = setInterval(this.fetchAlertCount, 60000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    toggleSidebar() {
      this.$store.dispatch('app/toggleSidebar')
    },
    async fetchAlertCount() {
      try {
        this.alertCount = await getAlertCount()
      } catch (e) {
        // Ignore
      }
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '提示', {
          type: 'warning'
        }).then(() => {
          this.$store.dispatch('user/logout').then(() => {
            this.$router.push('/login')
          })
        })
      } else if (command === 'password') {
        this.passwordDialogVisible = true
        this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
      }
    },
    submitPassword() {
      this.$refs.passwordForm.validate(async valid => {
        if (valid) {
          try {
            await changePassword(this.passwordForm)
            this.$message.success('密码修改成功，请重新登录')
            this.passwordDialogVisible = false
            this.$store.dispatch('user/logout').then(() => {
              this.$router.push('/login')
            })
          } catch (e) {
            // Error handled by interceptor
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: $header-height;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.left-menu {
  display: flex;
  align-items: center;

  .hamburger {
    font-size: 20px;
    cursor: pointer;
    margin-right: 15px;

    &:hover {
      color: $primary-color;
    }
  }
}

.right-menu {
  display: flex;
  align-items: center;

  .alert-badge {
    margin-right: 25px;
    cursor: pointer;

    .el-icon-bell {
      font-size: 20px;

      &:hover {
        color: $primary-color;
      }
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    cursor: pointer;

    .username {
      margin: 0 8px;
    }
  }
}
</style>
