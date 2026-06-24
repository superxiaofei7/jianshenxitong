<template>
  <div class="forgot-password-container">
    <!-- Animated background shapes -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <div class="forgot-password-card">
      <div class="forgot-password-header">
        <div class="forgot-password-logo">
          <el-icon :size="72"><Fitness /></el-icon>
        </div>
        <h2 class="forgot-password-title">找回密码</h2>
        <p class="forgot-password-subtitle">请输入您的手机号码，我们将发送验证码</p>
      </div>

      <el-form :model="form" :rules="rules" ref="forgotFormRef" size="large" class="forgot-password-form">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="verificationCode">
          <el-input v-model="form.verificationCode" placeholder="请输入验证码" prefix-icon="Unlock">
            <template #append>
              <el-button @click="sendVerificationCode" :disabled="countdown > 0" class="send-code-btn">
                {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" prefix-icon="Lock"
                    show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请确认新密码" prefix-icon="Lock"
                    show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleResetPassword" :loading="loading" class="reset-btn">
            <span v-if="!loading">重置密码</span>
            <span v-else>重置中...</span>
          </el-button>
        </el-form-item>
      </el-form>

      <div class="forgot-password-links">
        <el-link @click="goToLogin" class="link">返回登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { forgotPassword as forgotPasswordApi } from '@/api/auth'

const router = useRouter()
const forgotFormRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
let timer = null

const form = reactive({
  phone: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  verificationCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ]
}

function validatePassword(rule, value, callback) {
  if (value === form.newPassword) {
    callback()
  } else {
    callback(new Error('两次输入的密码不一致'))
  }
}

const sendVerificationCode = () => {
  if (!form.phone) {
    ElMessage.error('请输入手机号码')
    return
  }

  // 模拟发送验证码逻辑
  ElMessage.success('验证码已发送，请注意查收')
  countdown.value = 60

  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleResetPassword = async () => {
  if (!forgotFormRef.value) return
  await forgotFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await forgotPasswordApi({
        phone: form.phone,
        verificationCode: form.verificationCode,
        newPassword: form.newPassword
      })
      if (res.data.code === 200) {
        ElMessage.success('密码重置成功')
        router.push('/login')
      } else {
        ElMessage.error(res.data.msg || '密码重置失败')
      }
    } catch (e) {
      ElMessage.error(e.response?.data?.msg || '密码重置失败')
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  // 清除可能存在的计时器
  if (timer) {
    clearInterval(timer)
  }
})

onUnmounted(() => {
  // 组件销毁时清除计时器
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.forgot-password-container {
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

/* Forgot password card */
.forgot-password-card {
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
.forgot-password-header {
  text-align: center;
  margin-bottom: 36px;
}

.forgot-password-logo {
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

.forgot-password-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  letter-spacing: -0.02em;
}

.forgot-password-subtitle {
  color: #94a3b8;
  font-size: 14px;
  margin-top: 6px;
}

/* Form */
.forgot-password-form {
  margin-top: 8px;
}

.forgot-password-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  transition: all 0.3s;
}

.forgot-password-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #6366f1 inset;
}

.forgot-password-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #6366f1 inset, 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.send-code-btn {
  border-radius: 0 12px 12px 0 !important;
  background: linear-gradient(135deg, #6366f1, #818cf8) !important;
  color: white !important;
  border: none !important;
  padding: 0 16px;
}

.reset-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px !important;
  letter-spacing: 0.1em;
  margin-top: 4px;
}

/* Links */
.forgot-password-links {
  margin-top: 24px;
  text-align: center;
}

.link {
  color: #6366f1;
  text-decoration: none;
  font-size: 14px;
}

.link:hover {
  color: #4f46e5;
  text-decoration: underline;
}
</style>