<template>
  <div class="trainer-page">
    <div class="page-header">
      <div>
        <h2>教练信息</h2>
        <p class="page-desc">查看和编辑您的教练信息</p>
      </div>
    </div>

    <div class="trainer-card">
      <div class="trainer-card-top">
        <div class="trainer-avatar-wrap">
          <el-avatar :size="72" :src="avatarSrc" class="trainer-avatar">
            <el-icon :size="36"><UserFilled /></el-icon>
          </el-avatar>
          <div class="trainer-status-dot" :class="currentUser?.status === 1 ? 'active' : 'inactive'"></div>
        </div>
        <h3 class="trainer-name">{{ currentUser?.realName }}</h3>
        <div class="trainer-tags">
          <el-tag :type="currentUser?.gender === '男' ? 'primary' : 'danger'" effect="plain" round size="small">
            {{ currentUser?.gender === '男' ? '♂ 男教练' : '♀ 女教练' }}
          </el-tag>
          <el-tag effect="plain" round size="small" type="success">{{ currentUser?.experienceYears }}年经验</el-tag>
        </div>
      </div>

      <div class="trainer-specialties" v-if="currentUser?.specialties">
        <el-tag v-for="s in (currentUser?.specialties || '').split(',')" :key="s" size="small"
                class="specialty-tag" effect="light" round>
          {{ s }}
        </el-tag>
      </div>

      <div class="trainer-stats">
        <div class="trainer-stat">
          <span class="trainer-stat-value">¥{{ currentUser?.pricePerHour }}</span>
          <span class="trainer-stat-label">元/课时</span>
        </div>
        <div class="trainer-stat-divider"></div>
        <div class="trainer-stat">
          <span class="trainer-stat-value">{{ currentUser?.experienceYears }}</span>
          <span class="trainer-stat-label">年从业</span>
        </div>
      </div>

      <p class="trainer-desc" v-if="currentUser?.description">{{ currentUser?.description }}</p>

      <!-- 编辑按钮（仅教练本人可见） -->
      <el-button v-if="userStore.isTrainer()" type="primary" class="edit-btn" @click="handleEdit">
        <el-icon :size="16"><Edit /></el-icon>
        编辑信息
      </el-button>
    </div>

    <!-- 非教练用户看到教练列表 -->
    <div v-if="!userStore.isTrainer()" class="trainers-grid">
      <div class="page-header" style="margin-top:24px">
        <h2>全部教练</h2>
        <p class="page-desc">浏览所有在职教练信息</p>
      </div>
      <el-row :gutter="20">
        <el-col :span="8" v-for="t in allTrainers" :key="t.id">
          <div class="trainer-card trainer-list-card">
            <div class="trainer-card-top">
              <div class="trainer-avatar-wrap">
                <el-avatar :size="56" :src="t.avatar" class="trainer-avatar">
                  <el-icon :size="28"><UserFilled /></el-icon>
                </el-avatar>
                <div class="trainer-status-dot" :class="t.status === 1 ? 'active' : 'inactive'"></div>
              </div>
              <h3 class="trainer-name">{{ t.realName }}</h3>
              <div class="trainer-tags">
                <el-tag :type="t.gender === '男' ? 'primary' : 'danger'" effect="plain" round size="small">
                  {{ t.gender === '男' ? '♂ 男教练' : '♀ 女教练' }}
                </el-tag>
                <el-tag effect="plain" round size="small" type="success">{{ t.experienceYears }}年经验</el-tag>
              </div>
            </div>
            <div class="trainer-specialties" v-if="t.specialties">
              <el-tag v-for="s in (t.specialties || '').split(',')" :key="s" size="small"
                      class="specialty-tag" effect="light" round>
                {{ s }}
              </el-tag>
            </div>
            <div class="trainer-stats">
              <div class="trainer-stat">
                <span class="trainer-stat-value">¥{{ t.pricePerHour }}</span>
                <span class="trainer-stat-label">元/课时</span>
              </div>
              <div class="trainer-stat-divider"></div>
              <div class="trainer-stat">
                <span class="trainer-stat-value">{{ t.experienceYears }}</span>
                <span class="trainer-stat-label">年从业</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-empty v-if="allTrainers.length === 0" description="暂无在职教练" :image-size="60" />
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑教练信息" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="擅长项目">
          <el-input v-model="form.specialties" placeholder="逗号分隔" />
        </el-form-item>
        <el-form-item label="课时费(元/时)">
          <el-input-number v-model="form.pricePerHour" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="从业年限">
          <el-input-number v-model="form.experienceYears" :min="0" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="在职" inactive-text="离职" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { getTrainerById, getTrainerByUserId, getAvailableTrainers, updateTrainer } from '@/api/trainer'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const dialogVisible = ref(false)
const form = ref({})
const currentUser = ref(null)
const allTrainers = ref([])

// 头像优先使用 userStore（上传后立即更新），兜底使用 API 返回的 trainer.avatar
const avatarSrc = computed(() => {
  return userStore.user?.avatar || currentUser.value?.avatar || ''
})

const loadData = async () => {
  // 如果是教练，则加载当前教练信息
  if (userStore.isTrainer() && userStore.user?.id) {
    const res = await getTrainerByUserId(userStore.user.id)
    if (res.data.code === 200) {
      currentUser.value = res.data.data
    }
  } else {
    // 其他角色加载在职教练列表
    const res = await getAvailableTrainers({ page: 1, size: 50 })
    if (res.data.code === 200) {
      allTrainers.value = res.data.data.records || []
    }
  }
}

const handleEdit = () => {
  if (currentUser.value) {
    form.value = { ...currentUser.value }
    dialogVisible.value = true
  }
}

const handleSave = async () => {
  if (currentUser.value) {
    await updateTrainer({ ...form.value, id: currentUser.value.id })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  }
}

onMounted(loadData)
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

/* Trainer Card */
.trainer-card {
  background: #fff;
  border-radius: 18px;
  padding: 28px 24px;
  text-align: center;
  border: 1px solid #f1f5f9;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  max-width: 600px;
  margin: 0 auto;
}

.trainer-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #6366f1, #818cf8, #a78bfa);
  opacity: 0;
  transition: opacity 0.3s;
}

.trainer-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  border-color: transparent;
}

.trainer-card:hover::before {
  opacity: 1;
}

/* Avatar */
.trainer-card-top {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.trainer-avatar-wrap {
  position: relative;
  margin-bottom: 12px;
}

.trainer-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: white;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.25);
}

.trainer-status-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 3px solid #fff;
}

.trainer-status-dot.active { background: #10b981; }
.trainer-status-dot.inactive { background: #94a3b8; }

.trainer-name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.trainer-tags {
  display: flex;
  gap: 6px;
  justify-content: center;
}

/* Specialties */
.trainer-specialties {
  margin: 14px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
}

.specialty-tag {
  border: none !important;
  background: #eef2ff !important;
  color: #6366f1 !important;
}

/* Stats */
.trainer-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 14px 0;
  margin: 14px 0;
  background: #f8fafc;
  border-radius: 12px;
}

.trainer-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.trainer-stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #6366f1;
}

.trainer-stat-label {
  font-size: 12px;
  color: #94a3b8;
}

.trainer-stat-divider {
  width: 1px;
  height: 32px;
  background: #e2e8f0;
}

.trainer-desc {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
  margin: 0 0 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.edit-btn {
  width: 100%;
  height: 42px;
  border-radius: 12px !important;
  font-weight: 500;
  margin-top: 16px;
}

/* Trainer list grid for non-trainer users */
.trainers-grid {
  margin-top: 8px;
}

.trainer-list-card {
  margin-bottom: 20px;
  cursor: default;
}

.trainer-list-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
}
</style>
