<template>
  <div class="schedule-page">
    <div class="page-header">
      <div>
        <h2>排班管理</h2>
        <p class="page-desc">管理教练排班时间表</p>
      </div>
      <el-button type="primary" @click="showSetSchedule" class="add-btn" :disabled="!selectedTrainerId || !selectedDate">
        <el-icon :size="16"><Plus /></el-icon>
        设置排班
      </el-button>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <div class="filter-item">
        <label class="filter-label">
          <el-icon :size="14"><UserFilled /></el-icon>
          选择教练
        </label>
        <el-select v-model="selectedTrainerId" placeholder="选择教练" @change="loadSchedule" class="filter-select">
          <el-option v-for="t in trainers" :key="t.id" :label="t.realName" :value="t.id" />
        </el-select>
      </div>
      <div class="filter-item">
        <label class="filter-label">
          <el-icon :size="14"><Calendar /></el-icon>
          选择日期
        </label>
        <el-date-picker v-model="selectedDate" type="date" value-format="YYYY-MM-DD"
                        placeholder="选择日期" @change="loadSchedule" class="filter-date" />
      </div>
    </div>

    <!-- Schedule Timeline -->
    <div class="schedule-content" v-if="schedules.length > 0">
      <div class="schedule-grid">
        <div v-for="s in schedules" :key="s.id" class="schedule-slot"
             :class="{ full: s.bookedCount >= s.maxBookings }">
          <div class="slot-time">
            <div class="slot-start">{{ s.startTime }}</div>
            <div class="slot-divider"></div>
            <div class="slot-end">{{ s.endTime }}</div>
          </div>
          <div class="slot-info">
            <el-tag :type="s.bookedCount >= s.maxBookings ? 'danger' : 'success'" effect="light" round size="small">
              {{ s.bookedCount >= s.maxBookings ? '已约满' : '可预约' }}
            </el-tag>
            <div class="slot-progress">
              <div class="slot-progress-bar" :style="{ width: (s.bookedCount / s.maxBookings * 100) + '%' }"></div>
            </div>
            <span class="slot-count">{{ s.bookedCount }} / {{ s.maxBookings }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-else>
      <el-empty description="请选择教练和日期查看排班">
        <template #image>
          <div class="empty-icon">
            <el-icon :size="48" color="#cbd5e1"><Calendar /></el-icon>
          </div>
        </template>
      </el-empty>
    </div>

    <!-- 设置排班对话框 -->
    <el-dialog v-model="dialogVisible" title="设置排班" width="620px">
      <div class="dialog-info">
        <div class="dialog-info-item">
          <el-icon :size="16"><UserFilled /></el-icon>
          <span>教练：</span>
          <strong>{{ selectedTrainerName }}</strong>
        </div>
        <div class="dialog-info-item">
          <el-icon :size="16"><Calendar /></el-icon>
          <span>日期：</span>
          <strong>{{ selectedDate }}</strong>
        </div>
      </div>
      <el-divider />
      <div class="time-slots">
        <div v-for="(slot, index) in tempSchedules" :key="index" class="time-slot-row">
          <div class="slot-number">{{ index + 1 }}</div>
          <el-time-picker v-model="slot.startTime" format="HH:mm" value-format="HH:mm"
                          placeholder="开始时间" style="width:150px" />
          <span class="slot-sep">至</span>
          <el-time-picker v-model="slot.endTime" format="HH:mm" value-format="HH:mm"
                          placeholder="结束时间" style="width:150px" />
          <el-button type="danger" :icon="'Delete'" circle size="small" @click="tempSchedules.splice(index, 1)" />
        </div>
      </div>
      <el-button type="primary" plain @click="addSlot" class="add-slot-btn">
        <el-icon :size="14"><Plus /></el-icon> 添加时段
      </el-button>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSchedule">保存排班</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTrainerList } from '@/api/trainer'
import { getTrainerSchedule, setSchedule } from '@/api/schedule'
import { ElMessage } from 'element-plus'

const trainers = ref([])
const selectedTrainerId = ref(null)
const selectedTrainerName = ref('')
const selectedDate = ref('')
const schedules = ref([])
const dialogVisible = ref(false)
const tempSchedules = ref([])

onMounted(async () => {
  const res = await getTrainerList({ page: 1, size: 100 })
  if (res.data.code === 200) trainers.value = res.data.data.records
})

const loadSchedule = async () => {
  if (!selectedTrainerId.value || !selectedDate.value) return
  const res = await getTrainerSchedule(selectedTrainerId.value, selectedDate.value)
  if (res.data.code === 200) schedules.value = res.data.data
}

const showSetSchedule = () => {
  if (!selectedTrainerId.value || !selectedDate.value) {
    ElMessage.warning('请先选择教练和日期')
    return
  }
  const t = trainers.value.find(t => t.id === selectedTrainerId.value)
  selectedTrainerName.value = t ? t.realName : ''
  tempSchedules.value = [{ startTime: '09:00', endTime: '10:00' }]
  dialogVisible.value = true
}

const addSlot = () => {
  tempSchedules.value.push({ startTime: '', endTime: '' })
}

const handleSaveSchedule = async () => {
  for (const s of tempSchedules.value) {
    if (!s.startTime || !s.endTime) {
      ElMessage.warning('请填写完整时间段')
      return
    }
  }
  try {
    await setSchedule(selectedTrainerId.value, selectedDate.value, tempSchedules.value)
    ElMessage.success('排班设置成功')
    dialogVisible.value = false
    loadSchedule()
  } catch (e) {
    ElMessage.error('排班设置失败')
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.add-btn {
  height: 40px;
  border-radius: 12px !important;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  background: #fff;
  padding: 20px 24px;
  border-radius: 16px;
  border: 1px solid #f1f5f9;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.filter-select, .filter-date {
  width: 220px;
}

.filter-select :deep(.el-input__wrapper),
.filter-date :deep(.el-input__wrapper) {
  border-radius: 10px;
}

/* Schedule Grid */
.schedule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.schedule-slot {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #f1f5f9;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.schedule-slot::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #10b981, #34d399);
}

.schedule-slot.full::before {
  background: linear-gradient(90deg, #ef4444, #f87171);
}

.schedule-slot:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
}

.slot-time {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.slot-start, .slot-end {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.slot-divider {
  width: 20px;
  height: 2px;
  background: #e2e8f0;
}

.slot-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.slot-progress {
  flex: 1;
  height: 4px;
  background: #f1f5f9;
  border-radius: 2px;
  overflow: hidden;
}

.slot-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #34d399);
  border-radius: 2px;
  transition: width 0.3s;
}

.schedule-slot.full .slot-progress-bar {
  background: linear-gradient(90deg, #ef4444, #f87171);
}

.slot-count {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
}

/* Empty State */
.empty-state {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  border: 1px solid #f1f5f9;
}

.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Dialog */
.dialog-info {
  display: flex;
  gap: 24px;
  padding: 0 4px;
}

.dialog-info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 14px;
}

.dialog-info-item strong {
  color: #1e293b;
}

.time-slots {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.time-slot-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.slot-number {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #eef2ff;
  color: #6366f1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.slot-sep {
  color: #94a3b8;
  font-size: 13px;
}

.add-slot-btn {
  margin-top: 12px;
  width: 100%;
  border-radius: 10px !important;
}
</style>
