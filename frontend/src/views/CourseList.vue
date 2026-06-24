<template>
  <div class="course-page">
    <div class="page-header">
      <div>
        <h2>课程管理</h2>
        <p class="page-desc">管理所有课程类型和配置</p>
      </div>
      <el-button v-if="userStore.isAdmin()" type="primary" @click="handleAdd" class="add-btn">
        <el-icon :size="16"><Plus /></el-icon>
        新增课程
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索课程名称" clearable @clear="loadData"
                class="search-input" @keyup.enter="loadData">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="courseType" placeholder="全部类型" clearable @change="loadData" class="type-filter">
        <el-option label="私教课" value="PRIVATE" />
        <el-option label="团体课" value="GROUP" />
      </el-select>
    </div>

    <div class="table-card">
      <el-table :data="courses" stripe v-loading="loading" :header-cell-style="headerStyle">
        <el-table-column prop="courseName" label="课程名称" width="180">
          <template #default="{ row }">
            <div class="course-name-cell">
              <div class="course-icon" :class="row.courseType === 'PRIVATE' ? 'private' : 'group'">
                <el-icon :size="16">
                  <UserFilled v-if="row.courseType === 'PRIVATE'" />
                  <User v-else />
                </el-icon>
              </div>
              <span class="course-name-text">{{ row.courseName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="courseType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.courseType === 'PRIVATE' ? 'primary' : 'success'" effect="light" round size="small">
              {{ row.courseType === 'PRIVATE' ? '🧑 私教课' : '👥 团体课' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMinutes" label="时长" width="100">
          <template #default="{ row }">
            <span>{{ row.durationMinutes }} 分钟</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxStudents" label="最多人数" width="90" align="center" />
        <el-table-column prop="pricePerSession" label="价格" width="110">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.pricePerSession }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">
            <span class="desc-text">{{ row.description || '暂无描述' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0"
                       @change="(v) => updateCourseStatus(row)" v-if="userStore.isAdmin()" />
            <el-tag v-else :type="row.status === 1 ? 'success' : 'danger'" effect="light" round size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="userStore.isAdmin()" label="操作" width="160" align="center">
          <template #default="{ row }">
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
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                     layout="prev, pager, next, total" @current-change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '新增课程'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程名称"><el-input v-model="form.courseName" /></el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="form.courseType">
            <el-option label="私教课" value="PRIVATE" />
            <el-option label="团体课" value="GROUP" />
          </el-select>
        </el-form-item>
        <el-form-item label="教练ID"><el-input-number v-model="form.trainerId" :min="1" /></el-form-item>
        <el-form-item label="时长(分钟)"><el-input-number v-model="form.durationMinutes" :min="30" :step="30" /></el-form-item>
        <el-form-item label="最多人数"><el-input-number v-model="form.maxStudents" :min="1" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.pricePerSession" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
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
import { useUserStore } from '@/store/user'
import { getTrainerList } from '@/api/trainer'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const userStore = useUserStore()
const courses = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const courseType = ref('')
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
    const res = await request.get('/course/list', { params: { page: page.value, size: size.value, keyword: keyword.value, courseType: courseType.value } })
    if (res.data.code === 200) {
      courses.value = res.data.data.records
      total.value = res.data.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { courseType: 'PRIVATE', durationMinutes: 60, maxStudents: 1, pricePerSession: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (isEdit.value) {
    await request.put('/course', form.value)
  } else {
    await request.post('/course', form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该课程吗？', '提示', { type: 'warning' })
  await request.delete(`/course/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

const updateCourseStatus = async (row) => {
  await request.put('/course', row)
  ElMessage.success('状态已更新')
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
  max-width: 260px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.type-filter {
  width: 140px;
}

.type-filter :deep(.el-input__wrapper) {
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

/* Course name cell */
.course-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.course-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.course-icon.private {
  background: linear-gradient(135deg, #6366f1, #818cf8);
}

.course-icon.group {
  background: linear-gradient(135deg, #10b981, #34d399);
}

.course-name-text {
  font-weight: 500;
  color: #1e293b;
}

.price-text {
  font-weight: 600;
  color: #f59e0b;
}

.desc-text {
  color: #94a3b8;
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
