<template>
  <div class="checkin-page">
    <div class="page-header">
      <div>
        <h2>签到管理</h2>
        <p class="page-desc">管理课程签到记录和快速签到操作</p>
      </div>
    </div>

    <!-- 管理员：全部签到记录 -->
    <div v-if="isAdmin">
      <!-- Quick CheckIn Panel -->
      <div class="quick-checkin-card">
        <div class="qc-left">
          <div class="qc-icon-wrap">
            <el-icon :size="28"><Checked /></el-icon>
          </div>
          <div>
            <h3 class="qc-title">快速签到</h3>
            <p class="qc-desc">输入预约订单ID进行签到</p>
          </div>
        </div>
        <div class="qc-right">
          <el-input-number v-model="bookingIdForCheckIn" :min="1" placeholder="预约订单ID"
                           style="width:180px" controls-position="right" />
          <el-button type="primary" @click="handleCheckIn" class="checkin-btn" :disabled="!bookingIdForCheckIn">
            <el-icon :size="16"><CircleCheck /></el-icon>
            签 到
          </el-button>
        </div>
      </div>

      <div class="table-card">
        <el-table :data="checkIns" stripe v-loading="loading" :header-cell-style="headerStyle">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="bookingId" label="预约ID" width="90">
            <template #default="{ row }">
              <span class="booking-id">#{{ row.bookingId }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="memberId" label="会员ID" width="90" />
          <el-table-column prop="trainerId" label="教练ID" width="90" />
          <el-table-column prop="checkInTime" label="签到时间" width="170" />
          <el-table-column prop="hoursConsumed" label="消耗课时" width="110">
            <template #default="{ row }">
              <span class="hours-consumed">{{ row.hoursConsumed }}h</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <div :class="['checkin-status', row.status === 1 ? 'success' : 'cancelled']">
                <span class="checkin-status-dot"></span>
                {{ row.status === 1 ? '已签到' : '已取消' }}
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination">
        <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                       layout="prev, pager, next, total" @current-change="loadData" />
      </div>
    </div>

    <!-- 教练/会员个人签到记录 -->
    <div v-else>
      <div class="table-card">
        <el-table :data="checkIns" stripe v-loading="loading" :header-cell-style="headerStyle">
          <el-table-column prop="bookingId" label="预约ID" width="120">
            <template #default="{ row }">
              <span class="booking-id">#{{ row.bookingId }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="checkInTime" label="签到时间" width="200" />
          <el-table-column prop="hoursConsumed" label="消耗课时" width="120">
            <template #default="{ row }">
              <span class="hours-consumed">{{ row.hoursConsumed }}h</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="记录时间" width="200" />
        </el-table>
      </div>
      <div class="pagination" v-if="total > size">
        <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                       layout="prev, pager, next" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getAllCheckIns, getMemberCheckIns, getTrainerCheckIns, doCheckIn } from '@/api/checkin'
import { getMemberByUserId } from '@/api/member'
import { getTrainerByUserId } from '@/api/trainer'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const checkIns = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const bookingIdForCheckIn = ref(null)
const bizId = ref(null)

const isAdmin = computed(() => userStore.isAdmin())

const headerStyle = {
  background: '#f8fafc',
  color: '#64748b',
  fontWeight: 600,
  fontSize: '13px',
  borderBottom: '2px solid #e2e8f0'
}

const resolveBizId = async () => {
  const userId = userStore.user?.id
  if (userStore.user?.role === 'TRAINER') {
    const res = await getTrainerByUserId(userId)
    if (res.data.code === 200) bizId.value = res.data.data.id
  } else if (userStore.user?.role === 'MEMBER') {
    const res = await getMemberByUserId(userId)
    if (res.data.code === 200) bizId.value = res.data.data.id
  }
}

const loadData = async () => {
  loading.value = true
  try {
    let res
    if (isAdmin.value) {
      res = await getAllCheckIns({ page: page.value, size: size.value })
    } else if (userStore.user?.role === 'TRAINER') {
      res = await getTrainerCheckIns(bizId.value, { page: page.value, size: size.value })
    } else {
      res = await getMemberCheckIns(bizId.value, { page: page.value, size: size.value })
    }
    if (res.data.code === 200) {
      checkIns.value = res.data.data.records
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleCheckIn = async () => {
  if (!bookingIdForCheckIn.value) {
    ElMessage.warning('请输入预约ID')
    return
  }
  try {
    await doCheckIn(bookingIdForCheckIn.value)
    ElMessage.success('签到成功，已扣减对应课时')
    bookingIdForCheckIn.value = null
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '签到失败')
  }
}

onMounted(async () => {
  await resolveBizId()
  loadData()
})
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

/* Quick CheckIn Card */
.quick-checkin-card {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border-radius: 18px;
  padding: 24px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.quick-checkin-card::before {
  content: '';
  position: absolute;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  right: -40px;
  top: -60px;
}

.qc-left {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.qc-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.qc-title {
  color: white;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.qc-desc {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  margin: 4px 0 0;
}

.qc-right {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.checkin-btn {
  height: 40px;
  border-radius: 10px !important;
  background: white !important;
  color: #4f46e5 !important;
  border: none !important;
  font-weight: 600;
}

.checkin-btn:hover {
  background: #f0f0ff !important;
}

/* Table Card */
.table-card {
  background: #fff;
  border-radius: 16px;
  padding: 4px;
  border: 1px solid #f1f5f9;
  overflow: hidden;
}

.booking-id {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 13px;
  color: #6366f1;
  font-weight: 500;
}

.hours-consumed {
  font-weight: 600;
  color: #f59e0b;
}

/* CheckIn Status */
.checkin-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.checkin-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.checkin-status.success {
  background: #dcfce7;
  color: #16a34a;
}

.checkin-status.success .checkin-status-dot {
  background: #16a34a;
}

.checkin-status.cancelled {
  background: #fef2f2;
  color: #dc2626;
}

.checkin-status.cancelled .checkin-status-dot {
  background: #dc2626;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
