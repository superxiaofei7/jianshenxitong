<template>
  <div class="dashboard">
    <!-- Welcome Banner -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h2>欢迎回来，{{ userStore.user?.realName }} 👋</h2>
        <p>{{ greetingText }} · {{ roleName }}</p>
      </div>
      <div class="welcome-date">
        <div class="date-day">{{ currentDate.day }}</div>
        <div class="date-info">
          <span>{{ currentDate.month }}</span>
          <span>{{ currentDate.weekday }}</span>
        </div>
      </div>
    </div>

    <!-- Stats Cards (Admin & Finance) -->
    <el-row :gutter="20" class="stats-row" v-if="showStats">
      <el-col :span="6" v-for="(stat, index) in statCards" :key="index">
        <div class="stat-card" :class="'stat-card--' + stat.color">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-decoration"></div>
        </div>
      </el-col>
    </el-row>

    <!-- Quick Actions & Info -->
    <el-row :gutter="20" style="margin-top:24px">
      <el-col :span="14">
        <el-card class="actions-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">快捷操作</span>
              <span class="card-desc">常用功能入口</span>
            </div>
          </template>
          <el-row :gutter="16">
            <el-col :span="8" v-for="item in quickActions" :key="item.label">
              <div class="action-btn" :class="'action-btn--' + item.color" @click="$router.push(item.path)">
                <div class="action-icon">
                  <el-icon :size="28"><component :is="item.icon" /></el-icon>
                </div>
                <span class="action-label">{{ item.label }}</span>
                <el-icon class="action-arrow"><ArrowRight /></el-icon>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">系统信息</span>
              <el-tag size="small" effect="plain" round>v2.0</el-tag>
            </div>
          </template>
          <div class="info-list">
            <div class="info-item">
              <div class="info-label">
                <el-icon :size="16"><User /></el-icon>
                <span>用户名</span>
              </div>
              <span class="info-value">{{ userStore.user?.username }}</span>
            </div>
            <div class="info-item">
              <div class="info-label">
                <el-icon :size="16"><Postcard /></el-icon>
                <span>姓名</span>
              </div>
              <span class="info-value">{{ userStore.user?.realName }}</span>
            </div>
            <div class="info-item">
              <div class="info-label">
                <el-icon :size="16"><Stamp /></el-icon>
                <span>角色</span>
              </div>
              <el-tag :type="roleTagType" effect="dark" round size="small">{{ roleName }}</el-tag>
            </div>
            <div class="info-item">
              <div class="info-label">
                <el-icon :size="16"><Monitor /></el-icon>
                <span>系统版本</span>
              </div>
              <span class="info-value">FitPro v2.0</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getDashboard } from '@/api/statistics'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const stats = ref({})

const isAdmin = computed(() => userStore.isAdmin())
const isFinance = computed(() => userStore.isFinance())
const showStats = computed(() => userStore.isAdmin() || userStore.isFinance())

const greetingText = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const d = new Date()
  const months = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return {
    day: d.getDate(),
    month: months[d.getMonth()],
    weekday: weekdays[d.getDay()]
  }
})

const roleName = computed(() => {
  const map = { MEMBER: '会员', TRAINER: '教练', FRONT_DESK: '前台管理员', FINANCE: '财务管理员' }
  return map[userStore.user?.role] || '用户'
})

const roleTagType = computed(() => {
  const map = { MEMBER: 'success', TRAINER: 'warning', FRONT_DESK: '', FINANCE: 'danger' }
  return map[userStore.user?.role] || 'info'
})

const statCards = computed(() => [
  { label: '会员总数', value: stats.value.totalMembers || 0, icon: 'Avatar', color: 'blue' },
  { label: '活跃教练', value: stats.value.activeTrainers || 0, icon: 'UserFilled', color: 'green' },
  { label: '今日预约', value: stats.value.todayBookings || 0, icon: 'Calendar', color: 'orange' },
  { label: '今日签到', value: stats.value.todayCheckIns || 0, icon: 'Checked', color: 'purple' }
])

const quickActions = computed(() => {
  if (userStore.user?.role === 'MEMBER') {
    return [
      { label: '预约课程', path: '/booking', color: 'blue', icon: 'Calendar' },
      { label: '我的预约', path: '/my-bookings', color: 'green', icon: 'Tickets' },
      { label: '教练展示', path: '/trainers', color: 'orange', icon: 'UserFilled' }
    ]
  } else if (userStore.user?.role === 'TRAINER') {
    return [
      { label: '课程安排', path: '/trainer-schedule', color: 'blue', icon: 'Calendar' },
      { label: '我的信息', path: '/trainers', color: 'green', icon: 'UserFilled' },
      { label: '个人资料', path: '/profile', color: 'orange', icon: 'Edit' }
    ]
  } else if (userStore.user?.role === 'FINANCE') {
    return [
      { label: '数据统计', path: '/statistics', color: 'blue', icon: 'DataAnalysis' },
      { label: '系统首页', path: '/dashboard', color: 'orange', icon: 'HomeFilled' }
    ]
  } else {
    return [
      { label: '会员管理', path: '/members', color: 'blue', icon: 'Avatar' },
      { label: '教练管理', path: '/trainer-manage', color: 'green', icon: 'UserFilled' },
      { label: '数据统计', path: '/statistics', color: 'orange', icon: 'DataAnalysis' }
    ]
  }
})

onMounted(async () => {
  if (showStats.value) {
    try {
      const res = await getDashboard()
      if (res.data.code === 200) stats.value = res.data.data
    } catch (e) {
      ElMessage.error('获取统计数据失败')
    }
  }
})
</script>

<style scoped>
.dashboard {
  animation: fadeInUp 0.4s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Welcome Banner */
.welcome-banner {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 50%, #7c3aed 100%);
  border-radius: 20px;
  padding: 32px 36px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  right: -50px;
  top: -100px;
}

.welcome-banner::after {
  content: '';
  position: absolute;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  left: 30%;
  bottom: -80px;
}

.welcome-content h2 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px 0;
  position: relative;
  z-index: 1;
}

.welcome-content p {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
  position: relative;
  z-index: 1;
}

.welcome-date {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.date-day {
  font-size: 42px;
  font-weight: 700;
  line-height: 1;
}

.date-info {
  display: flex;
  flex-direction: column;
  font-size: 13px;
  opacity: 0.8;
  gap: 2px;
}

/* Stats Cards */
.stats-row {
  margin-top: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  border: 1px solid #f1f5f9;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-card--blue .stat-icon-wrap { background: linear-gradient(135deg, #6366f1, #818cf8); }
.stat-card--green .stat-icon-wrap { background: linear-gradient(135deg, #10b981, #34d399); }
.stat-card--orange .stat-icon-wrap { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.stat-card--purple .stat-icon-wrap { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 4px;
}

.stat-decoration {
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  right: -30px;
  bottom: -40px;
  opacity: 0.04;
}

.stat-card--blue .stat-decoration { background: #6366f1; }
.stat-card--green .stat-decoration { background: #10b981; }
.stat-card--orange .stat-decoration { background: #f59e0b; }
.stat-card--purple .stat-decoration { background: #8b5cf6; }

/* Card Headers */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.card-desc {
  font-size: 12px;
  color: #94a3b8;
}

/* Quick Actions */
.actions-card :deep(.el-card__body) {
  padding: 20px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f1f5f9;
  margin-bottom: 12px;
}

.action-btn:hover {
  transform: translateX(4px);
}

.action-btn--blue { background: linear-gradient(135deg, #eef2ff, #e0e7ff); }
.action-btn--blue:hover { border-color: #818cf8; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15); }
.action-btn--green { background: linear-gradient(135deg, #ecfdf5, #d1fae5); }
.action-btn--green:hover { border-color: #34d399; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.15); }
.action-btn--orange { background: linear-gradient(135deg, #fffbeb, #fef3c7); }
.action-btn--orange:hover { border-color: #fbbf24; box-shadow: 0 4px 12px rgba(245, 158, 11, 0.15); }

.action-icon {
  color: #6366f1;
}

.action-btn--blue .action-icon { color: #6366f1; }
.action-btn--green .action-icon { color: #10b981; }
.action-btn--orange .action-icon { color: #f59e0b; }

.action-label {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.action-arrow {
  color: #cbd5e1;
  font-size: 14px;
  transition: all 0.3s;
}

.action-btn:hover .action-arrow {
  color: #6366f1;
  transform: translateX(4px);
}

/* Info Card */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.info-item {
  padding: 14px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f8fafc;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 14px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}
</style>
