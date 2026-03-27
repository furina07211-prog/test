<template>
  <div class="login-wrapper login-bg">
    <div class="login-card surface">
      <header>
        <p class="eyebrow">Fruit Warehouse</p>
        <h1>登录后台</h1>
        <p class="muted">输入账号密码获取访问权限。</p>
      </header>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="••••••" />
        </el-form-item>
        <el-button type="primary" class="full" :loading="loading" @click="handleSubmit">登录</el-button>
      </el-form>
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
    } catch (err) {
      // 错误由拦截器处理
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
}

.login-card {
  width: 420px;
  padding: 32px;
  display: grid;
  gap: 18px;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-size: 12px;
  color: var(--text-soft);
  margin: 0;
}

h1 {
  margin: 6px 0 4px;
}

.muted {
  margin: 0;
  color: var(--text-soft);
}

.full {
  width: 100%;
}
</style>
