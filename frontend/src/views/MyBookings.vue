<template>
  <div class="bookings-page">
    <div class="page-header">
      <div>
        <h2>我的预约</h2>
        <p class="page-desc">查看和管理所有预约订单</p>
      </div>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="statusFilter" @change="loadData" class="status-filter">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="1">已确认</el-radio-button>
        <el-radio-button :value="2">已完成</el-radio-button>
        <el-radio-button :value="3">已取消</el-radio-button>
      </el-radio-group>
    </div>

    <div class="table-card">
      <el-table :data="bookings" stripe v-loading="loading" :header-cell-style="headerStyle">
        <el-table-column prop="orderNo" label="订单号" width="180">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bookingDate" label="预约日期" width="120" />
        <el-table-column label="时段" width="160">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon :size="14" color="#6366f1"><Clock /></el-icon>
              <span>{{ row.startTime }} - {{ row.endTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="hours" label="课时" width="80" align="center">
          <template #default="{ row }">
            <span class="hours-text">{{ row.hours }}h</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="110">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <div :class="['status-badge', statusClass(row.status)]">
              <span class="status-dot"></span>
              {{ statusText(row.status) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120">
          <template #default="{ row }">
            <span class="remark-text">{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" size="small" type="danger" plain @click="handleCancel(row.id)" round>
              取消
            </el-button>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                     layout="prev, pager, next, total" @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getMemberBookings, getTrainerBookings, cancelBooking } from '@/api/booking'
import { getMemberByUserId } from '@/api/member'
import { getTrainerByUserId } from '@/api/trainer'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const bookings = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusFilter = ref(undefined)
const loading = ref(false)
const bizId = ref(null)

const isTrainer = userStore.user?.role === 'TRAINER'

const headerStyle = {
  background: '#f8fafc',
  color: '#64748b',
  fontWeight: 600,
  fontSize: '13px',
  borderBottom: '2px solid #e2e8f0'
}

const statusText = (status) => {
  const map = { 0: '待确认', 1: '已确认', 2: '已完成', 3: '已取消' }
  return map[status] || '未知'
}

const statusClass = (status) => {
  const map = { 0: 'pending', 1: 'confirmed', 2: 'completed', 3: 'cancelled' }
  return map[status] || 'pending'
}

const resolveBizId = async () => {
  const userId = userStore.user?.id
  if (isTrainer) {
    const res = await getTrainerByUserId(userId)
    if (res.data.code === 200) bizId.value = res.data.data.id
  } else {
    const res = await getMemberByUserId(userId)
    if (res.data.code === 200) bizId.value = res.data.data.id
  }
}

const loadData = async () => {
  if (!bizId.value) return
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, status: statusFilter.value }
    const res = isTrainer
      ? await getTrainerBookings(bizId.value, params)
      : await getMemberBookings(bizId.value, params)
    if (res.data.code === 200) {
      bookings.value = res.data.data.records
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleCancel = async (id) => {
  await ElMessageBox.confirm('确定取消该预约吗？', '提示', { type: 'warning' })
  await cancelBooking(id)
  ElMessage.success('已取消')
  loadData()
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

/* Filter Bar */
.filter-bar {
  background: #fff;
  border-radius: 14px;
  padding: 16px 20px;
  margin-bottom: 16px;
  border: 1px solid #f1f5f9;
}

.status-filter :deep(.el-radio-button__inner) {
  border-radius: 8px !important;
  border: none !important;
  box-shadow: none !important;
  padding: 8px 20px;
}

.status-filter :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: white;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3) !important;
}

/* Table Card */
.table-card {
  background: #fff;
  border-radius: 16px;
  padding: 4px;
  border: 1px solid #f1f5f9;
  overflow: hidden;
}

.order-no {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 13px;
  color: #6366f1;
  font-weight: 500;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #1e293b;
  font-size: 13px;
}

.hours-text {
  font-weight: 600;
  color: #6366f1;
}

.amount-text {
  font-weight: 600;
  color: #f59e0b;
}

/* Status Badge */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}
.status-badge.pending .status-dot { background: #d97706; }

.status-badge.confirmed {
  background: #dcfce7;
  color: #16a34a;
}
.status-badge.confirmed .status-dot { background: #16a34a; }

.status-badge.completed {
  background: #f1f5f9;
  color: #64748b;
}
.status-badge.completed .status-dot { background: #64748b; }

.status-badge.cancelled {
  background: #fef2f2;
  color: #dc2626;
}
.status-badge.cancelled .status-dot { background: #dc2626; }

.remark-text {
  color: #94a3b8;
  font-size: 13px;
}

.no-action {
  color: #cbd5e1;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
