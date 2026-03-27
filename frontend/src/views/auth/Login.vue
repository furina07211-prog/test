<template>
  <div class="login-wrapper login-bg">
    <div class="decor-fruit fruit-a">🍎</div>
    <div class="decor-fruit fruit-b">🍊</div>
    <div class="decor-fruit fruit-c">🍐</div>

    <div class="login-card surface">
      <header class="login-header">
        <p class="eyebrow">水果仓库管理系统</p>
        <h1>水果批发仓库管理系统</h1>
        <p class="muted">采购、销售、库存与智能分析一体化后台</p>
      </header>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        :hide-required-asterisk="false"
        require-asterisk-position="left"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" @keyup.enter="handleSubmit" />
        </el-form-item>
        <el-button type="primary" class="full" :loading="loading" :disabled="loading" @click="handleSubmit">
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </el-form>

      <p class="tips">演示账号：admin / 123456</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()
const form = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(form)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  position: relative;
}

.login-card {
  position: relative;
  width: 430px;
  padding: 30px 30px 24px;
  display: grid;
  gap: 16px;
  z-index: 2;
}

.login-header h1 {
  margin: 8px 0 6px;
  font-size: 26px;
  line-height: 1.2;
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.15em;
  color: var(--primary);
  text-transform: uppercase;
}

.muted {
  margin: 0;
  color: var(--text-secondary);
}

.full {
  width: 100%;
  height: 40px;
  margin-top: 6px;
}

.tips {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.decor-fruit {
  position: absolute;
  font-size: 34px;
  opacity: 0.18;
  user-select: none;
  z-index: 1;
}

.fruit-a {
  left: 18%;
  top: 20%;
}

.fruit-b {
  right: 20%;
  top: 26%;
}

.fruit-c {
  right: 24%;
  bottom: 18%;
}

@media (max-width: 768px) {
  .login-card {
    width: min(100%, 420px);
    padding: 24px 20px 20px;
  }

  .login-header h1 {
    font-size: 22px;
  }
}
</style>
