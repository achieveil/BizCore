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
          placeholder="请假类型"
          style="width: 150px"
          @change="handleSearch"
        >
          <a-select-option :value="null">全部类型</a-select-option>
          <a-select-option value="SICK">病假</a-select-option>
          <a-select-option value="ANNUAL">年假</a-select-option>
          <a-select-option value="PERSONAL">事假</a-select-option>
          <a-select-option value="MARRIAGE">婚假</a-select-option>
          <a-select-option value="MATERNITY">产假</a-select-option>
          <a-select-option value="PATERNITY">陪产假</a-select-option>
          <a-select-option value="BEREAVEMENT">丧假</a-select-option>
        </a-select>
      </a-space>
      <a-button type="primary" @click="showCreateModal">
        <PlusOutlined /> 新增请假申请
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
        <template v-if="column.key === 'leaveType'">
          <a-tag :color="getLeaveTypeColor(record.leaveType)">
            {{ record.leaveTypeName }}
          </a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ record.statusName }}
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

    <!-- 申请请假模态框 -->
    <a-modal
      v-model:open="applyModalVisible"
      title="申请请假"
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
        <a-form-item label="请假类型" required>
          <a-select v-model:value="applyForm.leaveType" placeholder="请选择请假类型">
            <a-select-option value="SICK">病假</a-select-option>
            <a-select-option value="ANNUAL">年假</a-select-option>
            <a-select-option value="PERSONAL">事假</a-select-option>
            <a-select-option value="MARRIAGE">婚假</a-select-option>
            <a-select-option value="MATERNITY">产假</a-select-option>
            <a-select-option value="PATERNITY">陪产假</a-select-option>
            <a-select-option value="BEREAVEMENT">丧假</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="开始日期" required>
          <a-date-picker
            v-model:value="applyForm.startDate"
            style="width: 100%"
            :disabled-date="disabledStartDate"
          />
        </a-form-item>

        <a-form-item label="结束日期" required>
          <a-date-picker
            v-model:value="applyForm.endDate"
            style="width: 100%"
            :disabled-date="disabledEndDate"
          />
        </a-form-item>

        <a-form-item label="请假天数">
          <a-input :value="calculateDays" disabled />
        </a-form-item>

        <a-form-item label="请假原因" required>
          <a-textarea
            v-model:value="applyForm.reason"
            placeholder="请输入请假原因"
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>

        <a-form-item label="附件URL">
          <a-input
            v-model:value="applyForm.attachmentUrl"
            placeholder="如病假条图片URL（可选）"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情模态框 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="请假详情"
      width="600px"
      :footer="null"
    >
      <a-descriptions v-if="currentLeave" :column="1" bordered>
        <a-descriptions-item label="请假类型">
          <a-tag :color="getLeaveTypeColor(currentLeave.leaveType)">
            {{ getLeaveTypeText(currentLeave.leaveType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="开始日期">
          {{ currentLeave.startDate }}
        </a-descriptions-item>
        <a-descriptions-item label="结束日期">
          {{ currentLeave.endDate }}
        </a-descriptions-item>
        <a-descriptions-item label="请假天数">
          {{ currentLeave.days }} 天
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentLeave.status)">
            {{ getStatusText(currentLeave.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="请假原因">
          {{ currentLeave.reason }}
        </a-descriptions-item>
        <a-descriptions-item label="附件" v-if="currentLeave.attachmentUrl">
          <a :href="currentLeave.attachmentUrl" target="_blank">查看附件</a>
        </a-descriptions-item>
        <a-descriptions-item label="审批人" v-if="currentLeave.approverId">
          审批人ID: {{ currentLeave.approverId }}
        </a-descriptions-item>
        <a-descriptions-item label="审批意见" v-if="currentLeave.approvalComment">
          {{ currentLeave.approvalComment }}
        </a-descriptions-item>
        <a-descriptions-item label="审批时间" v-if="currentLeave.approvalTime">
          {{ currentLeave.approvalTime }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ currentLeave.createdTime }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { leaveApi } from '../api/leave'
import dayjs from 'dayjs'

// 状态变量
const loading = ref(false)
const applyLoading = ref(false)
const applyModalVisible = ref(false)
const detailModalVisible = ref(false)
const leaveRecords = ref([])
const currentLeave = ref(null)
const searchKeyword = ref('')
const filterStatus = ref(null)
const filterType = ref(null)

// 用户信息
const user = ref(null)
const empId = ref(null)
const deptId = ref(null)

// 申请表单
const applyForm = reactive({
  leaveType: undefined,
  startDate: null,
  endDate: null,
  reason: '',
  attachmentUrl: ''
})

// 表格列定义
const columns = [
  { title: '员工信息', key: 'empInfo', width: 200 },
  { title: '请假类型', key: 'leaveType', width: 100 },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 120 },
  { title: '结束日期', dataIndex: 'endDate', key: 'endDate', width: 120 },
  { title: '天数', dataIndex: 'days', key: 'days', width: 80 },
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
  loadLeaveRecords()
})

// 加载请假记录
const loadLeaveRecords = async () => {
  loading.value = true
  try {
    const res = await leaveApi.getByDepartment(deptId.value)
    leaveRecords.value = res.data
  } catch (error) {
    message.error('加载请假记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 过滤后的请假记录
const filteredRequests = computed(() => {
  let filtered = leaveRecords.value

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
    filtered = filtered.filter(record => record.leaveType === filterType.value)
  }

  return filtered
})

// 搜索处理
const handleSearch = () => {
  // 搜索由computed自动触发
}

// 计算请假天数
const calculateDays = computed(() => {
  if (applyForm.startDate && applyForm.endDate) {
    const start = dayjs(applyForm.startDate)
    const end = dayjs(applyForm.endDate)
    const days = end.diff(start, 'day') + 1
    return days > 0 ? `${days} 天` : '请选择正确的日期'
  }
  return '0 天'
})

// 显示申请模态框
const showCreateModal = () => {
  applyModalVisible.value = true
}

// 显示详情模态框
const showDetail = (record) => {
  currentLeave.value = record
  detailModalVisible.value = true
}

// 处理申请
const handleApply = async () => {
  // 表单验证
  if (!applyForm.leaveType) {
    message.warning('请选择请假类型')
    return
  }
  if (!applyForm.startDate || !applyForm.endDate) {
    message.warning('请选择请假日期')
    return
  }
  if (!applyForm.reason || applyForm.reason.trim() === '') {
    message.warning('请输入请假原因')
    return
  }

  const start = dayjs(applyForm.startDate)
  const end = dayjs(applyForm.endDate)
  if (end.isBefore(start)) {
    message.warning('结束日期不能早于开始日期')
    return
  }

  applyLoading.value = true
  try {
    await leaveApi.create({
      empId: empId.value,
      leaveType: applyForm.leaveType,
      startDate: applyForm.startDate.format('YYYY-MM-DD'),
      endDate: applyForm.endDate.format('YYYY-MM-DD'),
      reason: applyForm.reason,
      attachmentUrl: applyForm.attachmentUrl || null
    })
    message.success('请假申请提交成功！')
    applyModalVisible.value = false
    resetApplyForm()
    loadLeaveRecords()
  } catch (error) {
    message.error(error.response?.data?.message || '申请失败')
  } finally {
    applyLoading.value = false
  }
}

// 撤销申请
const handleCancel = async (requestId) => {
  try {
    await leaveApi.cancel(requestId, empId.value)
    message.success('已成功撤销请假申请')
    loadLeaveRecords()
  } catch (error) {
    message.error(error.response?.data?.message || '撤销失败')
  }
}

// 重置申请表单
const resetApplyForm = () => {
  Object.assign(applyForm, {
    leaveType: undefined,
    startDate: null,
    endDate: null,
    reason: '',
    attachmentUrl: ''
  })
}

// 禁用开始日期（不能选择过去的日期）
const disabledStartDate = (current) => {
  return current && current < dayjs().startOf('day')
}

// 禁用结束日期（不能早于开始日期）
const disabledEndDate = (current) => {
  if (!applyForm.startDate) {
    return current && current < dayjs().startOf('day')
  }
  return current && current < dayjs(applyForm.startDate).startOf('day')
}

// 请假类型颜色
const getLeaveTypeColor = (type) => {
  const colorMap = {
    'SICK': 'orange',
    'ANNUAL': 'green',
    'PERSONAL': 'blue',
    'MARRIAGE': 'pink',
    'MATERNITY': 'purple',
    'PATERNITY': 'cyan',
    'BEREAVEMENT': 'red'
  }
  return colorMap[type] || 'default'
}

// 请假类型文本
const getLeaveTypeText = (type) => {
  const textMap = {
    'SICK': '病假',
    'ANNUAL': '年假',
    'PERSONAL': '事假',
    'MARRIAGE': '婚假',
    'MATERNITY': '产假',
    'PATERNITY': '陪产假',
    'BEREAVEMENT': '丧假'
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
.leave-management {
  padding: 0;
}
</style>
