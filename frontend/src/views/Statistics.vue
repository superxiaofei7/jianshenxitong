<template>
  <div class="statistics-page">
    <div class="page-header">
      <div>
        <h2>数据统计</h2>
        <p class="page-desc">系统运营数据概览与分析</p>
      </div>
      <el-button @click="refreshAll" plain round>
        <el-icon :size="14"><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <!-- Overview Stats -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(stat, index) in overviewCards" :key="index">
        <div class="stat-card" :class="'stat-' + stat.color">
          <div class="stat-icon-wrap">
            <el-icon :size="26"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.prefix }}{{ stat.value }}{{ stat.suffix }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-bg-icon">
            <el-icon :size="80"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Revenue Section -->
    <el-card class="section-card" style="margin-top:24px">
      <template #header>
        <div class="section-header">
          <div class="section-title">
            <div class="section-icon revenue">
              <el-icon :size="18"><Coin /></el-icon>
            </div>
            <span>月度营收统计</span>
          </div>
        </div>
      </template>
      <div class="query-bar">
        <div class="query-group">
          <label class="query-label">年份</label>
          <el-input-number v-model="queryYear" :min="2020" :max="2030" />
        </div>
        <div class="query-group">
          <label class="query-label">月份</label>
          <el-input-number v-model="queryMonth" :min="1" :max="12" />
        </div>
        <el-button type="primary" @click="loadMonthlyRevenue" class="query-btn">
          <el-icon :size="14"><Search /></el-icon>
          查询
        </el-button>
      </div>

      <div v-if="revenueData" class="revenue-grid">
        <div class="revenue-item">
          <div class="revenue-label">总营收</div>
          <div class="revenue-value">¥{{ revenueData.totalRevenue || 0 }}</div>
        </div>
        <div class="revenue-item">
          <div class="revenue-label">预约数</div>
          <div class="revenue-value">{{ revenueData.totalBookings || 0 }}</div>
        </div>
        <div class="revenue-item">
          <div class="revenue-label">充值额</div>
          <div class="revenue-value">¥{{ revenueData.totalRecharge || 0 }}</div>
        </div>
      </div>
      <el-empty v-else description="请选择年月查询营收数据" :image-size="60" />
    </el-card>

    <!-- Activity & Trainer Stats -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="10">
        <el-card class="section-card">
          <template #header>
            <div class="section-header">
              <div class="section-title">
                <div class="section-icon activity">
                  <el-icon :size="18"><TrendCharts /></el-icon>
                </div>
                <span>会员活跃度</span>
              </div>
              <el-button size="small" text type="primary" @click="loadMemberActivity">
                <el-icon :size="14"><Refresh /></el-icon> 刷新
              </el-button>
            </div>
          </template>
          <div class="activity-center">
            <div class="activity-number">{{ memberActivity.activeMemberCount || 0 }}</div>
            <div class="activity-label">本月活跃会员</div>
            <div class="activity-bar">
              <div class="activity-bar-fill" :style="{ width: Math.min((memberActivity.activeMemberCount || 0) * 2, 100) + '%' }"></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card class="section-card">
          <template #header>
            <div class="section-header">
              <div class="section-title">
                <div class="section-icon trainer">
                  <el-icon :size="18"><UserFilled /></el-icon>
                </div>
                <span>教练授课统计</span>
              </div>
            </div>
          </template>
          <div class="trainer-select">
            <label class="query-label">选择教练</label>
            <el-select v-model="statsTrainerId" placeholder="选择教练" @change="loadTrainerStats" style="width:200px">
              <el-option v-for="t in trainers" :key="t.id" :label="t.realName" :value="t.id" />
            </el-select>
          </div>

          <div v-if="trainerStats" class="trainer-stats-grid">
            <div class="tstat-item">
              <div class="tstat-icon">
                <el-icon :size="20"><Notebook /></el-icon>
              </div>
              <div class="tstat-value">{{ trainerStats.totalClasses || 0 }}</div>
              <div class="tstat-label">总授课数</div>
            </div>
            <div class="tstat-item">
              <div class="tstat-icon">
                <el-icon :size="20"><Calendar /></el-icon>
              </div>
              <div class="tstat-value">{{ trainerStats.monthClasses || 0 }}</div>
              <div class="tstat-label">本月授课</div>
            </div>
            <div class="tstat-item">
              <div class="tstat-icon">
                <el-icon :size="20"><Tickets /></el-icon>
              </div>
              <div class="tstat-value">{{ trainerStats.totalBookings || 0 }}</div>
              <div class="tstat-label">总预约数</div>
            </div>
          </div>
          <el-empty v-else description="请选择教练查看授课数据" :image-size="50" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboard, getMonthlyRevenue, getTrainerStats, getMemberActivity } from '@/api/statistics'
import { getTrainerList } from '@/api/trainer'

const dashboard = ref({})
const queryYear = ref(new Date().getFullYear())
const queryMonth = ref(new Date().getMonth() + 1)
const revenueData = ref(null)
const memberActivity = ref({})
const trainers = ref([])
const statsTrainerId = ref(null)
const trainerStats = ref(null)

const overviewCards = computed(() => [
  { label: '会员总数', value: dashboard.value.totalMembers || 0, icon: 'Avatar', color: 'blue', prefix: '', suffix: '' },
  { label: '活跃教练', value: dashboard.value.activeTrainers || 0, icon: 'UserFilled', color: 'green', prefix: '', suffix: '' },
  { label: '今日预约', value: dashboard.value.todayBookings || 0, icon: 'Calendar', color: 'orange', prefix: '', suffix: '' },
  { label: '本月营收', value: dashboard.value.monthlyRevenue || 0, icon: 'Coin', color: 'purple', prefix: '¥', suffix: '' }
])

const refreshAll = async () => {
  const dashRes = await getDashboard()
  if (dashRes.data.code === 200) dashboard.value = dashRes.data.data
  loadMemberActivity()
}

onMounted(async () => {
  const dashRes = await getDashboard()
  if (dashRes.data.code === 200) dashboard.value = dashRes.data.data

  const trainerRes = await getTrainerList({ page: 1, size: 100 })
  if (trainerRes.data.code === 200) trainers.value = trainerRes.data.data.records

  loadMemberActivity()
})

const loadMonthlyRevenue = async () => {
  const res = await getMonthlyRevenue(queryYear.value, queryMonth.value)
  if (res.data.code === 200) revenueData.value = res.data.data
}

const loadTrainerStats = async () => {
  if (!statsTrainerId.value) return
  const res = await getTrainerStats(statsTrainerId.value)
  if (res.data.code === 200) trainerStats.value = res.data.data
}

const loadMemberActivity = async () => {
  const res = await getMemberActivity()
  if (res.data.code === 200) memberActivity.value = res.data.data
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

/* Stat Cards */
.stat-card {
  border-radius: 18px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  cursor: default;
  color: white;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.12);
}

.stat-blue { background: linear-gradient(135deg, #6366f1, #4f46e5); }
.stat-green { background: linear-gradient(135deg, #10b981, #059669); }
.stat-orange { background: linear-gradient(135deg, #f59e0b, #d97706); }
.stat-purple { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }

.stat-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  opacity: 0.8;
  margin-top: 4px;
}

.stat-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -15px;
  opacity: 0.06;
  transform: rotate(-15deg);
}

/* Section Card */
.section-card {
  border-radius: 16px !important;
  border: 1px solid #f1f5f9;
}

.section-card :deep(.el-card__header) {
  padding: 18px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.section-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.section-icon.revenue { background: linear-gradient(135deg, #f59e0b, #d97706); }
.section-icon.activity { background: linear-gradient(135deg, #10b981, #059669); }
.section-icon.trainer { background: linear-gradient(135deg, #6366f1, #4f46e5); }

/* Query Bar */
.query-bar {
  display: flex;
  align-items: flex-end;
  gap: 16px;
}

.query-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.query-label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
}

.query-btn {
  height: 40px;
  border-radius: 10px !important;
}

/* Revenue Grid */
.revenue-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.revenue-item {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border-radius: 14px;
  padding: 20px;
  text-align: center;
}

.revenue-label {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.revenue-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

/* Activity */
.activity-center {
  text-align: center;
  padding: 20px 0;
}

.activity-number {
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(135deg, #10b981, #059669);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.activity-label {
  font-size: 14px;
  color: #94a3b8;
  margin-top: 8px;
}

.activity-bar {
  margin-top: 16px;
  height: 6px;
  background: #f1f5f9;
  border-radius: 3px;
  overflow: hidden;
}

.activity-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #34d399);
  border-radius: 3px;
  transition: width 0.5s ease;
}

/* Trainer select */
.trainer-select {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

/* Trainer stats grid */
.trainer-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.tstat-item {
  text-align: center;
  padding: 16px 8px;
  background: #f8fafc;
  border-radius: 12px;
}

.tstat-icon {
  color: #6366f1;
  margin-bottom: 8px;
}

.tstat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.tstat-label {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}
</style>
