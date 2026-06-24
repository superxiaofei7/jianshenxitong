<template>
  <div class="finance-dashboard">
    <div class="dashboard-header">
      <h2>财务管理系统</h2>
      <p class="dashboard-subtitle">欢迎使用财务管理系统</p>
    </div>

    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="card-content">
              <div class="card-icon">
                <el-icon :size="24"><Money /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">¥{{ financeStats.totalRevenue }}</div>
                <div class="card-label">总营收</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="card-content">
              <div class="card-icon">
                <el-icon :size="24"><Wallet /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">¥{{ financeStats.totalBalance }}</div>
                <div class="card-label">账户余额</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="card-content">
              <div class="card-icon">
                <el-icon :size="24"><User /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ financeStats.memberCount }}</div>
                <div class="card-label">会员数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="card-content">
              <div class="card-icon">
                <el-icon :size="24"><UserFilled /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ financeStats.trainerCount }}</div>
                <div class="card-label">教练数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="chart-section">
      <el-card class="chart-card">
        <div class="chart-header">
          <h3>月度收入趋势</h3>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            size="small"
          />
        </div>
        <div class="chart-container">
          <div ref="chartRef" style="width: 100%; height: 400px;"></div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getFinanceStats } from '@/api/statistics'
import * as echarts from 'echarts'

const financeStats = ref({
  totalRevenue: 0,
  totalBalance: 0,
  memberCount: 0,
  trainerCount: 0
})

const dateRange = ref([])

const chartRef = ref(null)
let chartInstance = null

const loadFinanceStats = async () => {
  try {
    const res = await getFinanceStats()
    if (res.data.code === 200) {
      financeStats.value = res.data.data
    }
  } catch (error) {
    console.error('获取财务统计数据失败:', error)
  }
}

const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)

    const option = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '¥{value}'
        }
      },
      series: [{
        data: [120, 132, 101, 134, 90, 230, 210, 182, 191, 234, 290, 330],
        type: 'line',
        smooth: true,
        lineStyle: {
          color: '#6366f1'
        },
        areaStyle: {
          color: 'rgba(99, 102, 241, 0.1)'
        }
      }]
    }

    chartInstance.setOption(option)
  }
}

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  loadFinanceStats()
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.finance-dashboard {
  padding: 24px;
}

.dashboard-header {
  margin-bottom: 32px;
}

.dashboard-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.dashboard-subtitle {
  margin: 8px 0 0;
  color: #94a3b8;
  font-size: 14px;
}

.stats-cards {
  margin-bottom: 32px;
}

.stat-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #94a3b8;
}

.chart-section {
  margin-top: 24px;
}

.chart-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.chart-container {
  padding: 24px;
}
</style>