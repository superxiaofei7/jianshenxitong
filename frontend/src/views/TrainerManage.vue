<template>
  <div class="trainer-page">
    <div class="page-header">
      <div>
        <h2>教练管理</h2>
        <p class="page-desc">管理所有教练信息</p>
      </div>
      <el-button type="primary" @click="handleAdd" class="add-btn">
        <el-icon :size="16"><Plus /></el-icon>
        新增教练
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
      <el-table :data="trainers" stripe v-loading="loading" :header-cell-style="headerStyle">
        <el-table-column prop="realName" label="姓名" width="100">
          <template #default="{ row }">
            <div class="trainer-name">
              <el-avatar :size="32" :src="row.avatar" class="trainer-avatar">{{ row.realName?.charAt(0) }}</el-avatar>
              <span>{{ row.realName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="specialties" label="擅长项目" />
        <el-table-column prop="pricePerHour" label="课时费(元/时)" width="120">
          <template #default="{ row }">
            <span class="money-text">¥{{ row.pricePerHour }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="experienceYears" label="从业年限" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round size="small">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="handleEdit(row)" round>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑教练' : '新增教练'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select>
        </el-form-item>
        <el-form-item label="擅长项目">
          <el-input v-model="form.specialties" placeholder="逗号分隔" />
        </el-form-item>
        <el-form-item label="课时费(元/时)"><el-input-number v-model="form.pricePerHour" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="从业年限"><el-input-number v-model="form.experienceYears" :min="0" /></el-form-item>
        <el-form-item label="个人简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
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
import { ref, onMounted } from 'vue'
import { getTrainerList, saveTrainer, updateTrainer, deleteTrainer } from '@/api/trainer'
import { ElMessage, ElMessageBox } from 'element-plus'

const trainers = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

const headerStyle = {
  background: '#f8fafc',
  color: '#64748b',
  fontWeight: 600,
  fontSize: '13px',
  borderBottom: '2px solid #e2e8f0'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTrainerList({ page: page.value, size: size.value, keyword: keyword.value })
    if (res.data.code === 200) {
      trainers.value = res.data.data.records
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { gender: '男', status: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (isEdit.value) {
    await updateTrainer(form.value)
  } else {
    await saveTrainer(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该教练吗？', '提示', { type: 'warning' })
  await deleteTrainer(id)
  ElMessage.success('删除成功')
  loadData()
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

/* Trainer name cell */
.trainer-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.trainer-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: white;
  font-size: 14px;
  font-weight: 600;
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

</style>