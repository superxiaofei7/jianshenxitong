<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>个人信息</h2>
      <p class="page-desc">查看和编辑您的个人信息</p>
    </div>

    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="80" :src="userStore.user?.avatar" class="profile-avatar">
            <el-icon :size="40"><UserFilled /></el-icon>
          </el-avatar>
          <div class="avatar-info">
            <div class="upload-section">
              <el-upload
                :show-file-list="false"
                :http-request="customUpload"
                :before-upload="beforeAvatarUpload"
                class="avatar-uploader"
              >
                <el-button type="primary" size="small" class="upload-btn">
                  <el-icon :size="14"><Upload /></el-icon> 上传头像
                </el-button>
              </el-upload>
              <p class="upload-tip">支持JPG、PNG格式，大小不超过5MB</p>
            </div>
          </div>
        </div>

        <div class="user-info">
          <div class="info-item">
            <span class="label">姓名：</span>
            <span class="value">{{ userStore.user?.realName }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号：</span>
            <span class="value">{{ userStore.user?.phone }}</span>
          </div>
          <div class="info-item">
            <span class="label">用户名：</span>
            <span class="value">{{ userStore.user?.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">角色：</span>
            <span class="value">{{ roleName }}</span>
          </div>
        </div>
      </div>

      <div class="profile-body">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="基本资料" name="basic">
            <div class="form-section">
              <el-form :model="profileForm" label-width="100px" ref="profileFormRef">
                <el-form-item label="真实姓名">
                  <el-input v-model="profileForm.realName" />
                </el-form-item>
                <el-form-item label="手机号码">
                  <el-input v-model="profileForm.phone" />
                </el-form-item>
                <el-form-item label="角色">
                  <el-input :model-value="roleName" disabled />
                </el-form-item>
                <el-form-item label="用户名">
                  <el-input :model-value="userStore.user?.username" disabled />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveBasicInfo">保存基本信息</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane label="账户安全" name="security">
            <div class="form-section">
              <el-form :model="passwordForm" label-width="100px" ref="passwordFormRef">
                <el-form-item label="旧密码">
                  <el-input v-model="passwordForm.oldPassword" type="password" />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="passwordForm.newPassword" type="password" />
                </el-form-item>
                <el-form-item label="确认新密码">
                  <el-input v-model="passwordForm.confirmPassword" type="password" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updatePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane label="账户余额" name="balance" v-if="userStore.isMember()">
            <div class="balance-section">
              <div class="balance-card">
                <div class="balance-title">账户余额</div>
                <div class="balance-amount">¥{{ memberInfo?.totalRecharge || 0 }}</div>
                <div class="balance-note">如需充值请联系前台管理员</div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { uploadAvatar, updateProfile, updatePassword as updatePasswordApi } from '@/api/auth'
import { getMemberByUserId } from '@/api/member'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const activeTab = ref('basic')
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const memberInfo = ref(null)

const profileForm = ref({
  realName: '',
  phone: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleName = computed(() => {
  const map = { MEMBER: '会员', TRAINER: '教练', FRONT_DESK: '前台', FINANCE: '财务' }
  return map[userStore.user?.role] || '用户'
})

const customUpload = async (options) => {
  try {
    const res = await uploadAvatar(options.file)
    if (res.data.code === 200) {
      const avatarUrl = res.data.data?.avatar
      if (avatarUrl) {
        await updateProfile({ avatar: avatarUrl })
        const updatedUser = { ...userStore.user, avatar: avatarUrl }
        userStore.updateUser(updatedUser)
        ElMessage.success('头像上传成功')
      }
    } else {
      ElMessage.error(res.data.msg || '头像上传失败')
    }
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isJPGorPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPGorPNG) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
  }
  if (!isLt5M) {
    ElMessage.error('头像图片大小不能超过 5MB!')
  }
  return isJPGorPNG && isLt5M
}

const saveBasicInfo = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()

    const formData = {
      ...profileForm.value
    }

    const res = await updateProfile(formData)
    if (res.data.code === 200) {
      // 更新本地用户信息
      const updatedUser = { ...userStore.user, ...formData }
      userStore.updateUser(updatedUser)
      ElMessage.success('基本信息更新成功')
    } else {
      ElMessage.error(res.data.msg || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const updatePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()

    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
      ElMessage.error('两次输入的新密码不一致')
      return
    }

    const res = await updatePasswordApi(passwordForm.value.oldPassword, passwordForm.value.newPassword)
    if (res.data.code === 200) {
      ElMessage.success('密码修改成功')
      passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    } else {
      ElMessage.error(res.data.msg || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败')
  }
}

// 初始化表单数据
onMounted(async () => {
  if (userStore.user) {
    profileForm.value = {
      realName: userStore.user.realName || '',
      phone: userStore.user.phone || ''
    }
  }

  // 如果是会员，加载会员信息以显示余额
  if (userStore.isMember() && userStore.user?.id) {
    try {
      const res = await getMemberByUserId(userStore.user.id)
      if (res.data.code === 200) {
        memberInfo.value = res.data.data
      }
    } catch (e) {
      // ignore
    }
  }
})
</script>

<style scoped>
.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}

.page-desc {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 14px;
}

.profile-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #f1f5f9;
}

.profile-header {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.profile-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: white;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

.avatar-info {
  text-align: center;
}

.upload-btn {
  border-radius: 8px !important;
}

.upload-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.label {
  font-weight: 600;
  color: #1e293b;
  min-width: 60px;
}

.value {
  color: #64748b;
}

.profile-body {
  padding-top: 16px;
}

.profile-tabs :deep(.el-tabs__content) {
  padding: 0;
}

.form-section {
  padding: 20px 0;
}

.balance-section {
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.balance-card {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  color: white;
  width: 100%;
  max-width: 300px;
  margin-bottom: 24px;
}

.balance-title {
  font-size: 14px;
  margin-bottom: 8px;
}

.balance-amount {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.balance-note {
  font-size: 12px;
  opacity: 0.8;
}

.balance-actions {
  width: 100%;
  max-width: 300px;
}

.recharge-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 4px;
}

.recharge-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: white;
  font-weight: 600;
}

.recharge-details {
  flex: 1;
}

.recharge-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.recharge-balance {
  font-size: 14px;
  color: #94a3b8;
}
</style>