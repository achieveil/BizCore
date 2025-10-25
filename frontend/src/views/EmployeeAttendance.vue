<template>
  <div class="employee-attendance">
    <!-- 签到/签退区域 -->
    <a-row :gutter="16" style="margin-bottom: 24px;">
      <a-col :span="24">
        <a-card title="考勤打卡" :bordered="false">
          <div class="check-in-area">
            <div class="time-display">
              <h2>{{ currentTime }}</h2>
              <p>{{ currentDate }}</p>
            </div>
            <div class="action-buttons">
              <a-space size="large">
                <a-button
                  type="primary"
                  size="large"
                  @click="handleCheckIn"
                  :disabled="todayAttendance && todayAttendance.checkInTime"
                  :loading="checkInLoading"
                >
                  <template #icon><LoginOutlined /></template>
                  签到
                </a-button>
                <a-button
                  size="large"
                  @click="handleCheckOut"
                  :disabled="!todayAttendance || !todayAttendance.checkInTime || todayAttendance.checkOutTime"
                  :loading="checkOutLoading"
                >
                  <template #icon><LogoutOutlined /></template>
                  签退
                </a-button>
              </a-space>
              <a-input
                v-model:value="remark"
                placeholder="备注（可选）"
                style="margin-top: 16px; max-width: 400px;"
              />
            </div>
            <div v-if="todayAttendance" class="today-status">
              <a-descriptions :column="2" bordered size="small">
                <a-descriptions-item label="签到时间">
                  {{ todayAttendance.checkInTime || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="签退时间">
                  {{ todayAttendance.checkOutTime || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="考勤状态">
                  <a-tag :color="getStatusColor(todayAttendance.status)">
                    {{ getStatusText(todayAttendance.status) }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="工作时长">
                  {{ todayAttendance.workHours ? todayAttendance.workHours + ' 小时' : '-' }}
                </a-descriptions-item>
              </a-descriptions>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 月度统计 -->
    <a-row :gutter="16" style="margin-bottom: 24px;">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="本月出勤天数"
            :value="monthlyStats.attendanceDays || 0"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="迟到次数"
            :value="monthlyStats.lateDays || 0"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="早退次数"
            :value="monthlyStats.earlyLeaveDays || 0"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="本月签到天数"
            :value="calendarAttendances.length"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <CalendarOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 日历视图 -->
    <a-row :gutter="16" style="margin-bottom: 24px;">
      <a-col :span="24">
        <a-card title="考勤日历" :bordered="false">
          <a-calendar
            v-model:value="selectedDate"
            @select="onDateSelect"
            :fullscreen="false"
          >
            <template #dateCellRender="{ current }">
              <div class="calendar-cell">
                <div
                  v-if="getAttendanceByDate(current)"
                  class="attendance-indicator"
                  :class="getAttendanceStatusClass(current)"
                >
                  <CheckOutlined v-if="getAttendanceByDate(current)" />
                </div>
              </div>
            </template>
          </a-calendar>
        </a-card>
      </a-col>
    </a-row>

    <!-- 考勤记录表格 -->
    <a-row :gutter="16">
      <a-col :span="24">
        <a-card title="考勤记录" :bordered="false">
          <div style="margin-bottom: 16px;">
            <a-range-picker
              v-model:value="dateRange"
              @change="handleDateChange"
              :disabled-date="disabledDate"
            />
          </div>
          <a-table
            :columns="columns"
            :data-source="attendanceRecords"
            :loading="tableLoading"
            rowKey="id"
            :pagination="{ pageSize: 10 }"
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
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  LoginOutlined,
  LogoutOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CalendarOutlined,
  CheckOutlined
} from '@ant-design/icons-vue'
import { attendanceApi } from '../api/attendance'
import dayjs from 'dayjs'

// 状态变量
const currentTime = ref(dayjs().format('HH:mm:ss'))
const currentDate = ref(dayjs().format('YYYY年MM月DD日 dddd'))
const remark = ref('')
const checkInLoading = ref(false)
const checkOutLoading = ref(false)
const tableLoading = ref(false)
const todayAttendance = ref(null)
const attendanceRecords = ref([])
const calendarAttendances = ref([])
const selectedDate = ref(dayjs())
const dateRange = ref([dayjs().startOf('month'), dayjs().endOf('month')])
const monthlyStats = reactive({
  attendanceDays: 0,
  lateDays: 0,
  earlyLeaveDays: 0
})

// 用户信息
const user = ref(null)
const empId = ref(null)

// 定时器
let timer = null

// 表格列定义
const columns = [
  { title: '日期', dataIndex: 'attendanceDate', key: 'attendanceDate', width: 120 },
  { title: '签到时间', dataIndex: 'checkInTime', key: 'checkInTime', width: 100 },
  { title: '签退时间', dataIndex: 'checkOutTime', key: 'checkOutTime', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '迟到情况', key: 'isLate', width: 120 },
  { title: '早退情况', key: 'isEarlyLeave', width: 120 },
  { title: '工作时长', dataIndex: 'workHours', key: 'workHours', width: 100,
    customRender: ({ text }) => text ? `${text} 小时` : '-'
  },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true }
]

// 初始化
onMounted(() => {
  // 获取用户信息
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
    empId.value = user.value.empId // 使用登录响应返回的员工ID
  }

  // 启动时钟
  timer = setInterval(() => {
    currentTime.value = dayjs().format('HH:mm:ss')
    currentDate.value = dayjs().format('YYYY年MM月DD日 dddd')
  }, 1000)

  // 加载数据
  loadTodayAttendance()
  loadMonthlyStats()
  loadAttendanceRecords()
  loadCalendarAttendances()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

// 加载今日考勤
const loadTodayAttendance = async () => {
  try {
    const today = dayjs().format('YYYY-MM-DD')
    const res = await attendanceApi.getByRange(empId.value, today, today)
    if (res.data && res.data.length > 0) {
      todayAttendance.value = res.data[0]
    }
  } catch (error) {
    console.error('加载今日考勤失败:', error)
  }
}

// 加载月度统计
const loadMonthlyStats = async () => {
  try {
    const year = dayjs().year()
    const month = dayjs().month() + 1
    const res = await attendanceApi.getMonthlyStats(empId.value, year, month)
    Object.assign(monthlyStats, res.data)
  } catch (error) {
    console.error('加载月度统计失败:', error)
  }
}

// 加载考勤记录
const loadAttendanceRecords = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return

  tableLoading.value = true
  try {
    const startDate = dateRange.value[0].format('YYYY-MM-DD')
    const endDate = dateRange.value[1].format('YYYY-MM-DD')
    const res = await attendanceApi.getEmployeeRecords(empId.value, startDate, endDate)
    attendanceRecords.value = res.data
  } catch (error) {
    console.error('加载考勤记录失败:', error)
  } finally {
    tableLoading.value = false
  }
}

// 加载日历考勤数据
const loadCalendarAttendances = async () => {
  try {
    const startDate = selectedDate.value.startOf('month').format('YYYY-MM-DD')
    const endDate = selectedDate.value.endOf('month').format('YYYY-MM-DD')
    const res = await attendanceApi.getByRange(empId.value, startDate, endDate)
    calendarAttendances.value = res.data
  } catch (error) {
    console.error('加载日历数据失败:', error)
  }
}

// 签到
const handleCheckIn = async () => {
  checkInLoading.value = true
  try {
    await attendanceApi.checkIn({
      empId: empId.value,
      remark: remark.value
    })
    message.success('签到成功！')
    remark.value = ''
    loadTodayAttendance()
    loadMonthlyStats()
    loadAttendanceRecords()
    loadCalendarAttendances()
  } catch (error) {
    message.error(error.response?.data?.message || '签到失败')
  } finally {
    checkInLoading.value = false
  }
}

// 签退
const handleCheckOut = async () => {
  checkOutLoading.value = true
  try {
    await attendanceApi.checkOut({
      empId: empId.value,
      remark: remark.value
    })
    message.success('签退成功！')
    remark.value = ''
    loadTodayAttendance()
    loadAttendanceRecords()
    loadCalendarAttendances()
  } catch (error) {
    message.error(error.response?.data?.message || '签退失败')
  } finally {
    checkOutLoading.value = false
  }
}

// 日期范围变化
const handleDateChange = () => {
  loadAttendanceRecords()
}

// 日历日期选择
const onDateSelect = (date) => {
  selectedDate.value = date
  if (date.month() !== selectedDate.value.month()) {
    loadCalendarAttendances()
  }
}

// 获取指定日期的考勤记录
const getAttendanceByDate = (date) => {
  const dateStr = date.format('YYYY-MM-DD')
  return calendarAttendances.value.find(att => att.attendanceDate === dateStr)
}

// 获取考勤状态样式类
const getAttendanceStatusClass = (date) => {
  const att = getAttendanceByDate(date)
  if (!att) return ''

  if (att.status === 'NORMAL') return 'status-normal'
  if (att.status === 'LATE' || att.isLate) return 'status-late'
  if (att.status === 'EARLY_LEAVE' || att.isEarlyLeave) return 'status-early'
  if (att.status === 'LATE_AND_EARLY') return 'status-both'
  return ''
}

// 状态颜色
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

// 状态文本
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

// 禁用未来日期
const disabledDate = (current) => {
  return current && current > dayjs().endOf('day')
}
</script>

<style scoped>
.employee-attendance {
  padding: 0;
}

.check-in-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.time-display {
  text-align: center;
}

.time-display h2 {
  font-size: 48px;
  margin: 0;
  color: #1890ff;
}

.time-display p {
  font-size: 18px;
  margin: 8px 0 0 0;
  color: #666;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.today-status {
  width: 100%;
  max-width: 600px;
}

.calendar-cell {
  position: relative;
  height: 100%;
}

.attendance-indicator {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: white;
}

.status-normal {
  background-color: #52c41a;
}

.status-late {
  background-color: #faad14;
}

.status-early {
  background-color: #faad14;
}

.status-both {
  background-color: #f5222d;
}
</style>
