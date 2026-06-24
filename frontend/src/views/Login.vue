<template>
  <div class="login-container">
    <!-- Animated background shapes -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">
          <el-icon :size="72"><Fitness /></el-icon>
        </div>
        <h2 class="login-title">FitPro</h2>
        <p class="login-subtitle">健身房私教预约管理系统</p>
      </div>

      <el-form :model="form" :rules="rules" ref="loginFormRef" size="large" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                    @keyup.enter="handleLogin" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-links">
        <el-link @click="$router.push('/register')" class="link">注册账号</el-link>
        <span class="link-divider">|</span>
        <el-link @click="$router.push('/forgot-password')" class="link">忘记密码</el-link>
      </div>

<!--
      <div class="login-tips">
        <div class="tips-title">
          <el-icon :size="14"><InfoFilled /></el-icon>
          <span>演示账号</span>
        </div>
        <div class="tips-grid">
          <div class="tip-item">
            <el-tag size="small" effect="dark" round>前台</el-tag>
            <span>admin / admin123</span>
          </div>
          <div class="tip-item">
            <el-tag size="small" effect="dark" round type="danger">财务</el-tag>
            <span>finance / admin123</span>
          </div>
          <div class="tip-item">
            <el-tag size="small" effect="dark" round type="success">会员</el-tag>
            <span>member01 / admin123</span>
          </div>
          <div class="tip-item">
            <el-tag size="small" effect="dark" round type="warning">教练</el-tag>
            <span>trainer01 / admin123</span>
          </div>
        </div>
      </div>
-->
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await userStore.login(form.username, form.password)
      if (res.code === 200) {
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } else {
        ElMessage.error(res.msg || '登录失败')
      }
    } catch (e) {
      ElMessage.error(e.response?.data?.msg || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 30%, #eef1f5 60%, #f0f4f8 100%);
  position: relative;
  overflow: hidden;
}

/* Animated floating shapes */
.bg-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  top: -200px;
  right: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #10b981, #34d399);
  bottom: -150px;
  left: -100px;
  animation-delay: -5s;
}

.shape-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  top: 50%;
  left: 10%;
  animation-delay: -10s;
}

.shape-4 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #6366f1, #a5b4fc);
  top: 20%;
  right: 20%;
  animation-delay: -15s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -40px) scale(1.05); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(40px, 30px) scale(1.02); }
}

/* Login card */
.login-card {
  width: 440px;
  padding: 48px 40px 36px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 1;
  animation: cardEnter 0.6s ease-out;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Header */
.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-logo {
  width: 144px;
  height: 144px;
  background: url('../images/logo.png') center/cover no-repeat;
  border-radius: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin: 0 auto 20px;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  letter-spacing: -0.02em;
}

.login-subtitle {
  color: #94a3b8;
  font-size: 14px;
  margin-top: 6px;
}

/* Form */
.login-form {
  margin-top: 8px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #6366f1 inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #6366f1 inset, 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px !important;
  letter-spacing: 0.1em;
  margin-top: 4px;
}

/* Links */
.login-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.link-divider {
  color: #cbd5e1;
  font-size: 14px;
}

.login-links .link {
  color: #6366f1;
  font-size: 14px;
  text-decoration: none;
}

.login-links .link:hover {
  color: #4f46e5;
  text-decoration: underline;
}

/* Tips */
.login-tips {
  margin-top: 24px;
  padding: 16px;
  background: linear-gradient(135deg, #f8fafc, #eef2ff);
  border-radius: 14px;
  border: 1px solid #e2e8f0;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 10px;
}

.tips-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}
</style>
