<template>
  <div>
    <div style="margin-bottom: 16px; display: flex; justify-content: space-between;">
      <a-space>
        <a-range-picker v-model:value="dateRange" @change="handleDateChange" />
        <a-select
          v-model:value="managerFilter"
          placeholder="身份筛选"
          style="width: 140px"
          allowClear
          @change="loadAttendances"
        >
          <a-select-option :value="true">仅经理</a-select-option>
          <a-select-option :value="false">仅员工</a-select-option>
        </a-select>
        <a-button type="primary" @click="loadAttendances">
          <SearchOutlined /> 查询
        </a-button>
      </a-space>
      <a-button type="primary" @click="showModal(null)">
        <PlusOutlined /> 新增考勤
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="attendances"
      :loading="loading"
      rowKey="id"
      :scroll="{ x: 1200 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'attendanceDate'">
          {{ formatDate(record.attendanceDate) }}
        </template>
        <template v-if="column.key === 'checkInTime'">
          {{ formatTime(record.checkInTime) }}
        </template>
        <template v-if="column.key === 'checkOutTime'">
          {{ formatTime(record.checkOutTime) }}
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'role'">
          <a-tag :color="getRoleColor(record.role, record.manager)">
            {{ getRoleLabel(record.role, record.manager) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="showModal(record)">编辑</a-button>
            <a-popconfirm
              title="确定删除该考勤记录吗？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑考勤模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="editingAttendance ? '编辑考勤' : '新增考勤'"
      @ok="handleSave"
      @cancel="handleCancel"
    >
      <a-form :model="formState" layout="vertical">
        <a-form-item label="员工" required>
          <a-select v-model:value="formState.empId" placeholder="请选择员工">
            <a-select-option v-for="emp in employees" :key="emp.id" :value="emp.id">
              {{ emp.name }} ({{ emp.empNo }})
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="考勤日期" required>
          <a-date-picker v-model:value="formState.attendanceDate" style="width: 100%" />
        </a-form-item>

        <a-form-item label="签到时间">
          <a-time-picker v-model:value="formState.checkInTime" style="width: 100%" format="HH:mm:ss" />
        </a-form-item>

        <a-form-item label="签退时间">
          <a-time-picker v-model:value="formState.checkOutTime" style="width: 100%" format="HH:mm:ss" />
        </a-form-item>

        <a-form-item label="状态" required>
          <a-select v-model:value="formState.status" placeholder="请选择状态">
            <a-select-option value="NORMAL">正常</a-select-option>
            <a-select-option value="LATE">迟到</a-select-option>
            <a-select-option value="EARLY">早退</a-select-option>
            <a-select-option value="ABSENT">缺勤</a-select-option>
            <a-select-option value="LEAVE">请假</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="备注">
          <a-textarea v-model:value="formState.remark" placeholder="请输入备注" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { attendanceApi } from '../api/attendance'
import { employeeApi } from '../api/employee'
import dayjs from 'dayjs'

const loading = ref(false)
const attendances = ref([])
const employees = ref([])
const dateRange = ref([dayjs().subtract(7, 'day'), dayjs()])
const managerFilter = ref()
const modalVisible = ref(false)
const editingAttendance = ref(null)

const formState = reactive({
  empId: null,
  attendanceDate: null,
  checkInTime: null,
  checkOutTime: null,
  status: 'NORMAL',
  remark: ''
})

const columns = [
  { title: '员工姓名', dataIndex: 'empName', key: 'empName', width: 100 },
  { title: '工号', dataIndex: 'empNo', key: 'empNo', width: 120 },
  { title: '部门', dataIndex: 'deptName', key: 'deptName', width: 120 },
  { title: '职位', dataIndex: 'position', key: 'position', width: 120 },
  { title: '考勤日期', key: 'attendanceDate', width: 120 },
  { title: '签到时间', key: 'checkInTime', width: 100 },
  { title: '签退时间', key: 'checkOutTime', width: 100 },
  { title: '状态', key: 'status', width: 80 },
  { title: '身份', key: 'role', width: 80 },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const getStatusColor = (status) => {
  const colorMap = {
    'NORMAL': 'green',
    'LATE': 'orange',
    'EARLY': 'orange',
    'ABSENT': 'red',
    'LEAVE': 'blue'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    'NORMAL': '正常',
    'LATE': '迟到',
    'EARLY': '早退',
    'ABSENT': '缺勤',
    'LEAVE': '请假'
  }
  return textMap[status] || status
}

const getRoleLabel = (role, managerFlag) => {
  if (role === 'ADMIN') return '管理员'
  if (role === 'MANAGER' || managerFlag) return '经理'
  return '员工'
}

const getRoleColor = (role, managerFlag) => {
  if (role === 'ADMIN') return 'red'
  if (role === 'MANAGER' || managerFlag) return 'gold'
  return 'default'
}

const loadAttendances = async () => {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      const [start, end] = dateRange.value
      if (start) {
        params.startDate = start.format('YYYY-MM-DD')
      }
      if (end) {
        params.endDate = end.format('YYYY-MM-DD')
      }
    }
    if (managerFilter.value !== undefined) {
      params.isManager = managerFilter.value
    }
    const res = await attendanceApi.getAll(params)
    attendances.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadEmployees = async () => {
  try {
    const res = await employeeApi.getAll()
    employees.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const handleDateChange = () => {
  loadAttendances()
}

const showModal = (record) => {
  editingAttendance.value = record
  if (record) {
    Object.assign(formState, {
      ...record,
      attendanceDate: record.attendanceDate ? dayjs(record.attendanceDate) : null,
      checkInTime: record.checkInTime ? dayjs(record.checkInTime, 'HH:mm:ss') : null,
      checkOutTime: record.checkOutTime ? dayjs(record.checkOutTime, 'HH:mm:ss') : null
    })
  } else {
    resetForm()
  }
  modalVisible.value = true
}

const resetForm = () => {
  Object.assign(formState, {
    empId: null,
    attendanceDate: dayjs(),
    checkInTime: null,
    checkOutTime: null,
    status: 'NORMAL',
    remark: ''
  })
}

const handleSave = async () => {
  try {
    const data = {
      ...formState,
      attendanceDate: formState.attendanceDate ? formState.attendanceDate.format('YYYY-MM-DD') : null,
      checkInTime: formState.checkInTime ? formState.checkInTime.format('HH:mm:ss') : null,
      checkOutTime: formState.checkOutTime ? formState.checkOutTime.format('HH:mm:ss') : null
    }

    if (editingAttendance.value) {
      await attendanceApi.update(editingAttendance.value.id, data)
      message.success('考勤记录更新成功')
    } else {
      await attendanceApi.create(data)
      message.success('考勤记录创建成功')
    }

    modalVisible.value = false
    loadAttendances()
  } catch (error) {
    console.error(error)
  }
}

const handleCancel = () => {
  modalVisible.value = false
  resetForm()
}

const handleDelete = async (id) => {
  try {
    await attendanceApi.delete(id)
    message.success('考勤记录删除成功')
    loadAttendances()
  } catch (error) {
    console.error(error)
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

const formatTime = (time) => {
  if (!time) return '-'
  // 如果time是时间字符串(HH:mm:ss)
  if (typeof time === 'string' && time.includes(':')) {
    return time
  }
  // 如果是完整日期时间
  return dayjs(time).format('HH:mm:ss')
}

onMounted(() => {
  loadEmployees()
  loadAttendances()
})
</script>
