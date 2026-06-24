<template>
  <div class="member-page">
    <div class="page-header">
      <div>
        <h2>会员管理</h2>
        <p class="page-desc">管理所有会员信息和充值记录</p>
      </div>
      <el-button type="primary" @click="handleAdd" class="add-btn">
        <el-icon :size="16"><Plus /></el-icon>
        新增会员
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索姓名 / 手机号" clearable @clear="loadData"
                class="search-input" @keyup.enter="loadData">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button @click="loadData">
        <el-icon :size="16"><Search /></el-icon>
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="members" stripe v-loading="loading" :header-cell-style="headerStyle">
        <el-table-column prop="realName" label="姓名" width="100">
          <template #default="{ row }">
            <div class="member-name">
              <el-avatar :size="32" :src="row.avatar" class="member-avatar">{{ row.realName?.charAt(0) }}</el-avatar>
              <span>{{ row.realName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="memberLevel" label="会员等级" width="120">
          <template #default="{ row }">
            <el-tag :type="levelType(row.memberLevel)" effect="light" round size="small">
              {{ levelIcon(row.memberLevel) }} {{ row.memberLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remainingHours" label="剩余课时" width="100">
          <template #default="{ row }">
            <span :class="['hours-badge', row.remainingHours <= 5 ? 'low' : 'ok']">
              {{ row.remainingHours }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalRecharge" label="累计充值" width="110">
          <template #default="{ row }">
            <span class="money-text">¥{{ row.totalRecharge }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="memberExpireDate" label="到期日期" width="120" />
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="handleRecharge(row)" round>
              <el-icon :size="12"><Wallet /></el-icon> 充值
            </el-button>
            <el-button size="small" plain @click="handleEdit(row)" round>
              <el-icon :size="12"><Edit /></el-icon> 编辑
            </el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row.id)" round>
              <el-icon :size="12"><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page" :page-size="size" :total="total"
        layout="prev, pager, next, total" @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑会员' : '新增会员'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select>
        </el-form-item>
        <el-form-item label="生日"><el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="form.memberLevel">
            <el-option label="普通会员" value="普通会员" />
            <el-option label="银卡会员" value="银卡会员" />
            <el-option label="金卡会员" value="金卡会员" />
            <el-option label="钻石会员" value="钻石会员" />
          </el-select>
        </el-form-item>
        <el-form-item label="剩余课时"><el-input-number v-model="form.remainingHours" :min="0" /></el-form-item>
        <el-form-item label="到期日期"><el-date-picker v-model="form.memberExpireDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog v-model="rechargeVisible" title="会员充值" width="440px">
      <div class="recharge-member-info">
        <el-avatar :size="40" class="recharge-avatar">{{ currentMember?.realName?.charAt(0) }}</el-avatar>
        <div>
          <div class="recharge-name">{{ currentMember?.realName }}</div>
          <div class="recharge-level">
            <el-tag size="small" :type="levelType(currentMember?.memberLevel)" round>
              {{ currentMember?.memberLevel }}
            </el-tag>
            <span class="recharge-hours">剩余 {{ currentMember?.remainingHours }} 课时</span>
          </div>
        </div>
      </div>
      <el-divider />
      <el-form :model="rechargeForm" label-width="100px">
        <el-form-item label="充值课时">
          <el-input-number v-model="rechargeForm.hours" :min="1" :max="999" />
        </el-form-item>
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeForm.amount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="rechargeForm.payMethod">
            <el-radio-button value="微信">微信</el-radio-button>
            <el-radio-button value="支付宝">支付宝</el-radio-button>
            <el-radio-button value="现金">现金</el-radio-button>
            <el-radio-button value="银行卡">银行卡</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="rechargeForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" @click="doRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMemberList, saveMember, updateMember, deleteMember, rechargeMember } from '@/api/member'
import { ElMessage, ElMessageBox } from 'element-plus'

const members = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})
const rechargeVisible = ref(false)
const currentMember = ref(null)
const rechargeForm = ref({ hours: 10, amount: 0, payMethod: '微信', remark: '' })

const headerStyle = {
  background: '#f8fafc',
  color: '#64748b',
  fontWeight: 600,
  fontSize: '13px',
  borderBottom: '2px solid #e2e8f0'
}

const levelType = (level) => {
  const map = { '普通会员': 'info', '银卡会员': '', '金卡会员': 'warning', '钻石会员': 'danger' }
  return map[level] || 'info'
}

const levelIcon = (level) => {
  const map = { '普通会员': '○', '银卡会员': '◉', '金卡会员': '★', '钻石会员': '◆' }
  return map[level] || '○'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMemberList({ page: page.value, size: size.value, keyword: keyword.value })
    if (res.data.code === 200) {
      members.value = res.data.data.records
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { gender: '男', memberLevel: '普通会员', remainingHours: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (isEdit.value) {
    await updateMember(form.value)
  } else {
    await saveMember(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该会员吗？', '提示', { type: 'warning' })
  await deleteMember(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleRecharge = (row) => {
  currentMember.value = row
  rechargeForm.value = { hours: 10, amount: 0, payMethod: '微信', remark: '' }
  rechargeVisible.value = true
}

const doRecharge = async () => {
  try {
    const res = await rechargeMember(currentMember.value.id, rechargeForm.value)
    if (res.data.code === 200) {
      ElMessage.success(`成功为${currentMember.value.realName}充值${rechargeForm.value.hours}课时`)
      rechargeVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.msg || '充值失败')
    }
  } catch (error) {
    ElMessage.error('充值失败')
  }
}

onMounted(loadData)
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

/* Search */
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.search-input {
  max-width: 300px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px;
}

/* Table Card */
.table-card {
  background: #fff;
  border-radius: 16px;
  padding: 4px;
  border: 1px solid #f1f5f9;
  overflow: hidden;
}

.table-card :deep(.el-table) {
  border-radius: 12px;
}

/* Member name cell */
.member-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: white;
  font-size: 14px;
  font-weight: 600;
}

/* Hours badge */
.hours-badge {
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 13px;
}

.hours-badge.ok {
  background: #ecfdf5;
  color: #10b981;
}

.hours-badge.low {
  background: #fef2f2;
  color: #ef4444;
}

.money-text {
  font-weight: 600;
  color: #f59e0b;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* Recharge dialog */
.recharge-member-info {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 4px;
}

.recharge-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: white;
  font-weight: 600;
}

.recharge-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.recharge-level {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.recharge-hours {
  font-size: 12px;
  color: #94a3b8;
}
</style>
