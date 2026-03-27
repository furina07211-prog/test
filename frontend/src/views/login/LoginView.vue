<template>
  <div class="login-root">
    <section class="login-poster">
      <div class="poster-copy">
        <span>水果仓库管理系统</span>
        <h1>水果批发仓库管理系统</h1>
        <p>围绕采购入库、销售出库、库存盘点与智能采购建议，构建完整的仓储运营闭环。</p>
      </div>
      <div class="poster-band">
        <div>
          <strong>采购批次</strong>
          <small>保质期与成本同时追踪</small>
        </div>
        <div>
          <strong>销量预测</strong>
          <small>预测算法直接落地</small>
        </div>
        <div>
          <strong>预警热力图</strong>
          <small>答辩展示更直观</small>
        </div>
      </div>
    </section>

    <section class="login-panel surface">
      <div>
        <p class="eyebrow">系统登录</p>
        <h2>进入运营工作台</h2>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="warning" class="submit-btn" @click="handleLogin">登录</el-button>
      </el-form>
      <div class="login-tips">
        <p>默认演示账号建议：</p>
        <span>admin / 123456</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const form = reactive({
  username: 'admin',
  password: '123456'
})

async function handleLogin() {
  try {
    await authStore.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    // handled by interceptor
  }
}
</script>

<style scoped>
.login-root {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  background: radial-gradient(circle at top left, #f8d596 0%, #f6f2ea 42%, #efe7db 100%);
}

.login-poster {
  padding: 56px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.poster-copy {
  max-width: 640px;
  animation: slide-up 0.6s ease;
}

.poster-copy span,
.eyebrow {
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--accent-deep);
  font-size: 12px;
}

.poster-copy h1 {
  font-size: clamp(42px, 6vw, 76px);
  line-height: 1.02;
  margin: 18px 0;
}

.poster-copy p {
  font-size: 18px;
  line-height: 1.8;
  color: var(--text-soft);
}

.poster-band {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.poster-band div {
  padding: 18px;
  border-top: 1px solid rgba(29, 31, 27, 0.18);
}

.poster-band strong,
.poster-band small {
  display: block;
}

.poster-band small {
  margin-top: 6px;
  color: var(--text-soft);
}

.login-panel {
  margin: 36px;
  padding: 42px 34px;
  align-self: center;
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: slide-up 0.7s ease;
}

.submit-btn {
  width: 100%;
  height: 44px;
}

.login-tips {
  color: var(--text-soft);
}

@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 960px) {
  .login-root {
    grid-template-columns: 1fr;
  }

  .login-poster,
  .login-panel {
    margin: 0;
    padding: 28px;
  }

  .poster-band {
    grid-template-columns: 1fr;
  }
}
</style>
