<template>
  <div class="manager-attendance">
    <a-card title="部门考勤管理" :bordered="false">
      <!-- 筛选区域 -->
      <div style="margin-bottom: 16px;">
        <a-space>
          <a-range-picker
            v-model:value="dateRange"
            @change="loadAttendanceRecords"
            :disabled-date="disabledDate"
          />
          <a-button type="primary" @click="loadAttendanceRecords" :loading="loading">
            <SearchOutlined /> 查询
          </a-button>
          <a-button @click="handleExport">
            <DownloadOutlined /> 导出报表
          </a-button>
        </a-space>
      </div>

      <!-- 考勤记录表格 -->
      <a-table
        :columns="columns"
        :data-source="attendanceRecords"
        :loading="loading"
        rowKey="id"
        :pagination="{ pageSize: 20 }"
        :scroll="{ x: 1500 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'isLate'">
            <a-tag v-if="record.isLate" color="orange">
              迟到 {{ record.lateMinutes }} 分钟
            </a-tag>
            <span v-else>-</span>
          </template>
          <template v-if="column.key === 'isEarlyLeave'">
            <a-tag v-if="record.isEarlyLeave" color="orange">
              早退 {{ record.earlyLeaveMinutes }} 分钟
            </a-tag>
            <span v-else>-</span>
          </template>
        </template>
      </a-table>

      <!-- 统计信息 -->
      <a-row :gutter="16" style="margin-top: 24px;">
        <a-col :span="6">
          <a-card>
            <a-statistic title="总考勤记录" :value="attendanceRecords.length" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="迟到人次"
              :value="lateCount"
              :value-style="{ color: '#cf1322' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="早退人次"
              :value="earlyLeaveCount"
              :value-style="{ color: '#cf1322' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic
              title="正常出勤"
              :value="normalCount"
              :value-style="{ color: '#3f8600' }"
            />
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { attendanceApi } from '../api/attendance'
import dayjs from 'dayjs'

const loading = ref(false)
const attendanceRecords = ref([])
const dateRange = ref([dayjs().subtract(7, 'day'), dayjs()])

const user = ref(null)
const deptId = ref(null)

const columns = [
  { title: '员工姓名', dataIndex: 'empName', key: 'empName', width: 120, fixed: 'left' },
  { title: '工号', dataIndex: 'empNo', key: 'empNo', width: 120 },
  { title: '日期', dataIndex: 'attendanceDate', key: 'attendanceDate', width: 120 },
  { title: '签到时间', dataIndex: 'checkInTime', key: 'checkInTime', width: 100 },
  { title: '签退时间', dataIndex: 'checkOutTime', key: 'checkOutTime', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '迟到', key: 'isLate', width: 120 },
  { title: '早退', key: 'isEarlyLeave', width: 120 },
  { title: '工作时长', dataIndex: 'workHours', key: 'workHours', width: 100,
    customRender: ({ text }) => text ? `${text} 小时` : '-'
  },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true }
]

const lateCount = computed(() => attendanceRecords.value.filter(r => r.isLate).length)
const earlyLeaveCount = computed(() => attendanceRecords.value.filter(r => r.isEarlyLeave).length)
const normalCount = computed(() => attendanceRecords.value.filter(r => r.status === 'NORMAL').length)

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
    // 假设user对象中有deptId，实际需要通过employee接口获取
    deptId.value = 1 // 临时硬编码，实际应该从员工信息中获取
  }
  loadAttendanceRecords()
})

const loadAttendanceRecords = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    message.warning('请选择日期范围')
    return
  }

  loading.value = true
  try {
    const startDate = dateRange.value[0].format('YYYY-MM-DD')
    const endDate = dateRange.value[1].format('YYYY-MM-DD')
    const res = await attendanceApi.getDepartmentRecords(deptId.value, startDate, endDate)
    attendanceRecords.value = res.data
  } catch (error) {
    message.error('加载部门考勤记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  message.info('导出功能开发中...')
  // 可以使用 xlsx 库来实现Excel导出
}

const disabledDate = (current) => {
  return current && current > dayjs().endOf('day')
}

const getStatusColor = (status) => {
  const colorMap = {
    'NORMAL': 'green',
    'LATE': 'orange',
    'EARLY_LEAVE': 'orange',
    'LATE_AND_EARLY': 'red',
    'ABSENT': 'red',
    'LEAVE': 'blue'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    'NORMAL': '正常',
    'LATE': '迟到',
    'EARLY_LEAVE': '早退',
    'LATE_AND_EARLY': '迟到且早退',
    'ABSENT': '缺勤',
    'LEAVE': '请假'
  }
  return textMap[status] || status
}
</script>

<style scoped>
.manager-attendance {
  padding: 0;
}
</style>
