<template>
  <div class="booking-page">
    <div class="page-header">
      <div>
        <h2>在线预约</h2>
        <p class="page-desc">选择教练和时段，轻松预约私教课程</p>
      </div>
    </div>

    <div class="steps-wrap">
      <el-steps :active="step" align-center finish-status="success" class="booking-steps">
        <el-step title="选择教练" icon="UserFilled" />
        <el-step title="选择时段" icon="Clock" />
        <el-step title="确认预约" icon="CircleCheck" />
      </el-steps>
    </div>

    <!-- Step 1: 选择教练 -->
    <div v-if="step === 0" class="step-content">
      <el-row :gutter="20">
        <el-col :span="8" v-for="t in trainers" :key="t.id">
          <div :class="['trainer-select-card', { selected: selectedTrainer?.id === t.id }]"
               @click="selectTrainer(t)">
            <div class="tsc-header">
              <el-avatar :size="48" class="tsc-avatar">
                {{ t.realName?.charAt(0) }}
              </el-avatar>
              <div v-if="selectedTrainer?.id === t.id" class="tsc-check">
                <el-icon :size="16"><CircleCheck /></el-icon>
              </div>
            </div>
            <h3 class="tsc-name">{{ t.realName }}</h3>
            <div class="tsc-info">
              <span>💰 ¥{{ t.pricePerHour }}/时</span>
              <span>📅 {{ t.experienceYears }}年</span>
            </div>
            <p class="tsc-specialties">{{ t.specialties }}</p>
          </div>
        </el-col>
      </el-row>
      <div class="step-actions">
        <el-button type="primary" :disabled="!selectedTrainer" @click="step = 1" class="step-btn" round>
          下一步
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- Step 2: 选择时段 -->
    <div v-if="step === 1" class="step-content">
      <div class="date-picker-wrap">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
                        start-placeholder="开始日期" end-placeholder="结束日期"
                        value-format="YYYY-MM-DD" @change="loadSlots" style="width:100%" size="large" />
      </div>

      <div class="slots-section" v-if="availableSlots.length > 0 || dateRange">
        <div class="slots-header">
          <span class="slots-title">可选时段</span>
          <el-tag v-if="selectedTrainer" effect="plain" round>
            <el-icon :size="12"><UserFilled /></el-icon>
            {{ selectedTrainer.realName }}
          </el-tag>
        </div>
        <el-row :gutter="12">
          <el-col :span="6" v-for="slot in availableSlots" :key="slot.id">
            <div :class="['slot-card', { selected: selectedSlot?.id === slot.id, disabled: slot.status !== 1 }]"
                 @click="slot.status === 1 && selectSlot(slot)">
              <div class="sc-date">{{ slot.scheduleDate }}</div>
              <div class="sc-time">{{ slot.startTime }} - {{ slot.endTime }}</div>
              <el-tag :type="slot.status === 1 ? 'success' : 'danger'" effect="light" round size="small">
                {{ slot.status === 1 ? '可预约' : '已满' }}
              </el-tag>
            </div>
          </el-col>
        </el-row>
        <el-empty v-if="availableSlots.length === 0 && dateRange" description="该时段暂无可用排班" :image-size="60" />
      </div>

      <div class="step-actions">
        <el-button @click="step = 0" round>
          <el-icon class="el-icon--left"><ArrowLeft /></el-icon>
          上一步
        </el-button>
        <el-button type="primary" :disabled="!selectedSlot" @click="step = 2" round>
          下一步
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- Step 3: 确认预约 -->
    <div v-if="step === 2" class="step-content">
      <el-card class="confirm-card">
        <div class="confirm-title">
          <el-icon :size="24" color="#10b981"><CircleCheck /></el-icon>
          <span>预约确认</span>
        </div>
        <el-descriptions :column="2" border class="confirm-desc">
          <el-descriptions-item label="教练">{{ selectedTrainer?.realName }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ selectedSlot?.scheduleDate }}</el-descriptions-item>
          <el-descriptions-item label="时段">{{ selectedSlot?.startTime }} - {{ selectedSlot?.endTime }}</el-descriptions-item>
          <el-descriptions-item label="课时费">¥{{ selectedTrainer?.pricePerHour }}/时</el-descriptions-item>
        </el-descriptions>

        <el-form style="margin-top:24px" label-width="100px">
          <el-form-item label="选择课程">
            <el-select v-model="selectedCourseId" placeholder="选择课程" style="width:100%">
              <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="remark" type="textarea" :rows="2" placeholder="如有特殊要求请备注" />
          </el-form-item>
        </el-form>

        <div class="step-actions">
          <el-button @click="step = 1" round>
            <el-icon class="el-icon--left"><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button type="primary" :loading="submitting" @click="submitBooking" round class="submit-btn">
            <el-icon :size="16"><CircleCheck /></el-icon>
            确认预约
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getAvailableTrainers, getTrainerList } from '@/api/trainer'
import { getMemberByUserId } from '@/api/member'
import { getAvailableSlots } from '@/api/schedule'
import { createBooking } from '@/api/booking'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const step = ref(0)
const trainers = ref([])
const selectedTrainer = ref(null)
const dateRange = ref(null)
const availableSlots = ref([])
const selectedSlot = ref(null)
const courses = ref([])
const selectedCourseId = ref(null)
const remark = ref('')
const submitting = ref(false)
const memberId = ref(null)

onMounted(async () => {
  const res = await getAvailableTrainers({ page: 1, size: 50 })
  if (res.data.code === 200) trainers.value = res.data.data.records

  const courseRes = await request.get('/course/list', { params: { page: 1, size: 100 } })
  if (courseRes.data.code === 200) courses.value = courseRes.data.data.records

  const memberRes = await getMemberByUserId(userStore.user.id)
  if (memberRes.data.code === 200) memberId.value = memberRes.data.data.id
})

const selectTrainer = (t) => {
  selectedTrainer.value = t
  selectedSlot.value = null
  availableSlots.value = []
}

const loadSlots = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  const res = await getAvailableSlots(
    selectedTrainer.value.id,
    dateRange.value[0],
    dateRange.value[1]
  )
  if (res.data.code === 200) availableSlots.value = res.data.data
}

const selectSlot = (slot) => {
  selectedSlot.value = slot
}

const submitBooking = async () => {
  if (!selectedCourseId.value) {
    ElMessage.warning('请选择课程')
    return
  }
  if (!memberId.value) {
    ElMessage.warning('无法获取会员信息，请联系管理员')
    return
  }
  submitting.value = true
  try {
    const res = await createBooking({
      memberId: memberId.value,
      trainerId: selectedTrainer.value.id,
      courseId: selectedCourseId.value,
      scheduleId: selectedSlot.value.id,
      remark: remark.value
    })
    if (res.data.code === 200) {
      ElMessage.success('预约成功！')
      router.push('/my-bookings')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '预约失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
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

/* Steps */
.steps-wrap {
  background: #fff;
  border-radius: 16px;
  padding: 24px 32px;
  margin-bottom: 24px;
  border: 1px solid #f1f5f9;
}

/* Step Content */
.step-content {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Trainer Select Card */
.trainer-select-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
  border: 2px solid transparent;
  transition: all 0.3s;
  cursor: pointer;
  text-align: center;
}

.trainer-select-card:hover {
  border-color: #818cf8;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.1);
}

.trainer-select-card.selected {
  border-color: #6366f1;
  background: #fafafe;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.15);
}

.tsc-header {
  position: relative;
  display: inline-block;
}

.tsc-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.tsc-check {
  position: absolute;
  top: -4px;
  right: -4px;
  color: #10b981;
  background: #fff;
  border-radius: 50%;
  display: flex;
}

.tsc-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 12px 0 8px;
}

.tsc-info {
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
}

.tsc-specialties {
  font-size: 12px;
  color: #94a3b8;
  margin: 8px 0 0;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Step actions */
.step-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
}

.step-btn {
  min-width: 140px;
  height: 42px;
}

/* Date picker */
.date-picker-wrap {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 20px;
  border: 1px solid #f1f5f9;
  max-width: 500px;
}

/* Slots */
.slots-section {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  border: 1px solid #f1f5f9;
}

.slots-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.slots-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

/* Slot Card */
.slot-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 12px;
  border: 2px solid #f1f5f9;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.slot-card:hover:not(.disabled) {
  border-color: #818cf8;
}

.slot-card.selected {
  border-color: #6366f1;
  background: #fafafe;
}

.slot-card.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.sc-date {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.sc-time {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}

/* Confirm Card */
.confirm-card {
  border-radius: 18px !important;
  max-width: 700px;
  margin: 0 auto;
}

.confirm-card :deep(.el-card__body) {
  padding: 32px;
}

.confirm-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 24px;
}

.confirm-desc {
  border-radius: 12px;
  overflow: hidden;
}

.submit-btn {
  min-width: 160px;
  height: 44px;
  font-size: 15px;
}
</style>
