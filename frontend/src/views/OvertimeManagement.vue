<template>
  <div>
    <div style="margin-bottom: 16px; display: flex; justify-content: space-between;">
      <a-space>
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索员工姓名或工号"
          style="width: 300px"
          @search="handleSearch"
        />
        <a-select
          v-model:value="filterStatus"
          placeholder="审批状态"
          style="width: 150px"
          @change="handleSearch"
        >
          <a-select-option :value="null">全部状态</a-select-option>
          <a-select-option value="PENDING">待审批</a-select-option>
          <a-select-option value="APPROVED">已批准</a-select-option>
          <a-select-option value="REJECTED">已拒绝</a-select-option>
          <a-select-option value="CANCELLED">已撤销</a-select-option>
        </a-select>
        <a-select
          v-model:value="filterType"
          placeholder="加班类型"
          style="width: 150px"
          @change="handleSearch"
        >
          <a-select-option :value="null">全部类型</a-select-option>
          <a-select-option value="WEEKDAY">工作日加班</a-select-option>
          <a-select-option value="WEEKEND">周末加班</a-select-option>
          <a-select-option value="HOLIDAY">节假日加班</a-select-option>
        </a-select>
      </a-space>
      <a-button type="primary" @click="showCreateModal">
        <PlusOutlined /> 新增加班申请
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="filteredRequests"
      :loading="loading"
      rowKey="id"
      :scroll="{ x: 1500 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'empInfo'">
          <div>
            <div>{{ record.empName }} ({{ record.empNo }})</div>
            <div style="font-size: 12px; color: #999;">{{ record.deptName }} - {{ record.position }}</div>
          </div>
        </template>
        <template v-if="column.key === 'overtimeType'">
          <a-tag :color="getOvertimeTypeColor(record.overtimeType)">
            {{ getOvertimeTypeText(record.overtimeType) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'createdTime'">
          {{ formatDateTime(record.createdTime) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="showDetail(record)">查看</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 申请加班模态框 -->
    <a-modal
      v-model:open="applyModalVisible"
      title="申请加班"
      width="600px"
      @ok="handleApply"
      @cancel="resetApplyForm"
      :confirmLoading="applyLoading"
    >
      <a-form
        :model="applyForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="加班类型" required>
          <a-select v-model:value="applyForm.overtimeType" placeholder="请选择加班类型">
            <a-select-option value="WEEKDAY">工作日加班</a-select-option>
            <a-select-option value="WEEKEND">周末加班</a-select-option>
            <a-select-option value="HOLIDAY">节假日加班</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="加班日期" required>
          <a-date-picker
            v-model:value="applyForm.overtimeDate"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </a-form-item>

        <a-form-item label="开始时间" required>
          <a-time-picker
            v-model:value="applyForm.startTime"
            style="width: 100%"
            format="HH:mm"
            :minute-step="15"
          />
        </a-form-item>

        <a-form-item label="结束时间" required>
          <a-time-picker
            v-model:value="applyForm.endTime"
            style="width: 100%"
            format="HH:mm"
            :minute-step="15"
          />
        </a-form-item>

        <a-form-item label="加班时长">
          <a-input :value="calculateHours" disabled />
        </a-form-item>

        <a-form-item label="加班原因" required>
          <a-textarea
            v-model:value="applyForm.reason"
            placeholder="请输入加班原因"
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情模态框 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="加班详情"
      width="600px"
      :footer="null"
    >
      <a-descriptions v-if="currentOvertime" :column="1" bordered>
        <a-descriptions-item label="加班类型">
          <a-tag :color="getOvertimeTypeColor(currentOvertime.overtimeType)">
            {{ getOvertimeTypeText(currentOvertime.overtimeType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="加班日期">
          {{ currentOvertime.overtimeDate }}
        </a-descriptions-item>
        <a-descriptions-item label="开始时间">
          {{ currentOvertime.startTime }}
        </a-descriptions-item>
        <a-descriptions-item label="结束时间">
          {{ currentOvertime.endTime }}
        </a-descriptions-item>
        <a-descriptions-item label="加班时长">
          {{ currentOvertime.hours }} 小时
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentOvertime.status)">
            {{ getStatusText(currentOvertime.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="加班原因">
          {{ currentOvertime.reason }}
        </a-descriptions-item>
        <a-descriptions-item label="审批人" v-if="currentOvertime.approverId">
          审批人ID: {{ currentOvertime.approverId }}
        </a-descriptions-item>
        <a-descriptions-item label="审批意见" v-if="currentOvertime.approvalComment">
          {{ currentOvertime.approvalComment }}
        </a-descriptions-item>
        <a-descriptions-item label="审批时间" v-if="currentOvertime.approvalTime">
          {{ currentOvertime.approvalTime }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ currentOvertime.createdTime }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { overtimeApi } from '../api/overtime'
import dayjs from 'dayjs'

// 状态变量
const loading = ref(false)
const applyLoading = ref(false)
const applyModalVisible = ref(false)
const detailModalVisible = ref(false)
const overtimeRecords = ref([])
const currentOvertime = ref(null)
const searchKeyword = ref('')
const filterStatus = ref(null)
const filterType = ref(null)

// 用户信息
const user = ref(null)
const empId = ref(null)
const deptId = ref(null)

// 申请表单
const applyForm = reactive({
  overtimeType: undefined,
  overtimeDate: null,
  startTime: null,
  endTime: null,
  reason: ''
})

// 表格列定义
const columns = [
  { title: '员工信息', key: 'empInfo', width: 200 },
  { title: '加班类型', key: 'overtimeType', width: 120 },
  { title: '加班日期', dataIndex: 'overtimeDate', key: 'overtimeDate', width: 120 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 100 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 100 },
  { title: '时长', dataIndex: 'hours', key: 'hours', width: 80,
    customRender: ({ text }) => `${text} 小时`
  },
  { title: '状态', key: 'status', width: 100 },
  { title: '原因', dataIndex: 'reason', key: 'reason', ellipsis: true },
  { title: '审批意见', dataIndex: 'approvalComment', key: 'approvalComment', ellipsis: true },
  { title: '申请时间', key: 'createdTime', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

// 初始化
onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
    empId.value = user.value.empId
    deptId.value = user.value.deptId
  }
  loadOvertimeRecords()
})

// 加载加班记录
const loadOvertimeRecords = async () => {
  loading.value = true
  try {
    const res = await overtimeApi.getByDepartment(deptId.value)
    overtimeRecords.value = res.data
  } catch (error) {
    message.error('加载加班记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 过滤后的加班记录
const filteredRequests = computed(() => {
  let filtered = overtimeRecords.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(record =>
      record.empName?.toLowerCase().includes(keyword) ||
      record.empNo?.toLowerCase().includes(keyword)
    )
  }

  // 状态过滤
  if (filterStatus.value) {
    filtered = filtered.filter(record => record.status === filterStatus.value)
  }

  // 类型过滤
  if (filterType.value) {
    filtered = filtered.filter(record => record.overtimeType === filterType.value)
  }

  return filtered
})

// 搜索处理
const handleSearch = () => {
  // 搜索由computed自动触发
}

// 计算加班时长
const calculateHours = computed(() => {
  if (applyForm.startTime && applyForm.endTime) {
    const start = dayjs(applyForm.startTime)
    const end = dayjs(applyForm.endTime)
    const hours = end.diff(start, 'hour', true)
    return hours > 0 ? `${hours.toFixed(1)} 小时` : '请选择正确的时间'
  }
  return '0 小时'
})

// 显示申请模态框
const showCreateModal = () => {
  applyModalVisible.value = true
}

// 显示详情模态框
const showDetail = (record) => {
  currentOvertime.value = record
  detailModalVisible.value = true
}

// 处理申请
const handleApply = async () => {
  // 表单验证
  if (!applyForm.overtimeType) {
    message.warning('请选择加班类型')
    return
  }
  if (!applyForm.overtimeDate) {
    message.warning('请选择加班日期')
    return
  }
  if (!applyForm.startTime || !applyForm.endTime) {
    message.warning('请选择加班时间')
    return
  }
  if (!applyForm.reason || applyForm.reason.trim() === '') {
    message.warning('请输入加班原因')
    return
  }

  const start = dayjs(applyForm.startTime)
  const end = dayjs(applyForm.endTime)
  if (end.isBefore(start) || end.isSame(start)) {
    message.warning('结束时间必须晚于开始时间')
    return
  }

  applyLoading.value = true
  try {
    await overtimeApi.create({
      empId: empId.value,
      overtimeType: applyForm.overtimeType,
      overtimeDate: applyForm.overtimeDate.format('YYYY-MM-DD'),
      startTime: applyForm.startTime.format('HH:mm:ss'),
      endTime: applyForm.endTime.format('HH:mm:ss'),
      reason: applyForm.reason
    })
    message.success('加班申请提交成功！')
    applyModalVisible.value = false
    resetApplyForm()
    loadOvertimeRecords()
  } catch (error) {
    message.error(error.response?.data?.message || '申请失败')
  } finally {
    applyLoading.value = false
  }
}

// 重置申请表单
const resetApplyForm = () => {
  Object.assign(applyForm, {
    overtimeType: undefined,
    overtimeDate: null,
    startTime: null,
    endTime: null,
    reason: ''
  })
}

// 禁用未来的日期
const disabledDate = (current) => {
  return current && current > dayjs().endOf('day')
}

// 加班类型颜色
const getOvertimeTypeColor = (type) => {
  const colorMap = {
    'WEEKDAY': 'blue',
    'WEEKEND': 'orange',
    'HOLIDAY': 'red'
  }
  return colorMap[type] || 'default'
}

// 加班类型文本
const getOvertimeTypeText = (type) => {
  const textMap = {
    'WEEKDAY': '工作日加班',
    'WEEKEND': '周末加班',
    'HOLIDAY': '节假日加班'
  }
  return textMap[type] || type
}

// 状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'processing',
    'APPROVED': 'success',
    'REJECTED': 'error'
  }
  return colorMap[status] || 'default'
}

// 状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审批',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'CANCELLED': '已撤销'
  }
  return textMap[status] || status
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
.overtime-management {
  padding: 0;
}
</style>
