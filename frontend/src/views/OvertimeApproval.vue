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
        <a-select
          v-model:value="filterDept"
          placeholder="部门"
          style="width: 150px"
          @change="handleSearch"
          show-search
          :filter-option="filterDeptOption"
        >
          <a-select-option :value="null">全部部门</a-select-option>
          <a-select-option v-for="dept in departments" :key="dept.id" :value="dept.id">
            {{ dept.deptName }}
          </a-select-option>
        </a-select>
      </a-space>
    </div>

    <a-table
      :columns="columns"
      :data-source="filteredRequests"
      :loading="loading"
      rowKey="id"
      :scroll="{ x: 1600 }"
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
            {{ record.overtimeTypeName }}
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
            <a-button
              v-if="record.status === 'PENDING'"
              type="link"
              size="small"
              @click="showApprovalModal(record, 'approve')"
            >
              批准
            </a-button>
            <a-button
              v-if="record.status === 'PENDING'"
              type="link"
              size="small"
              danger
              @click="showApprovalModal(record, 'reject')"
            >
              拒绝
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 审批模态框 -->
    <a-modal
      v-model:open="approvalModalVisible"
      :title="approvalAction === 'approve' ? '批准加班申请' : '拒绝加班申请'"
      @ok="handleApproval"
      @cancel="resetApprovalForm"
      :confirm-loading="approvalLoading"
    >
      <a-form layout="vertical">
        <a-form-item label="审批意见" required>
          <a-textarea
            v-model:value="approvalComment"
            placeholder="请输入审批意见"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情模态框 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="加班详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions v-if="currentRequest" :column="2" bordered>
        <a-descriptions-item label="员工姓名">
          {{ currentRequest.empName }}
        </a-descriptions-item>
        <a-descriptions-item label="工号">
          {{ currentRequest.empNo }}
        </a-descriptions-item>
        <a-descriptions-item label="部门">
          {{ currentRequest.deptName }}
        </a-descriptions-item>
        <a-descriptions-item label="职位">
          {{ currentRequest.position }}
        </a-descriptions-item>
        <a-descriptions-item label="加班类型">
          <a-tag :color="getOvertimeTypeColor(currentRequest.overtimeType)">
            {{ currentRequest.overtimeTypeName }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="加班日期">
          {{ currentRequest.overtimeDate }}
        </a-descriptions-item>
        <a-descriptions-item label="开始时间">
          {{ formatTime(currentRequest.startTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="结束时间">
          {{ formatTime(currentRequest.endTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="加班时长">
          {{ currentRequest.hours }} 小时
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentRequest.status)">
            {{ currentRequest.statusName }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="加班原因" :span="2">
          {{ currentRequest.reason }}
        </a-descriptions-item>
        <a-descriptions-item label="审批人" v-if="currentRequest.approverName">
          {{ currentRequest.approverName }}
        </a-descriptions-item>
        <a-descriptions-item label="审批时间" v-if="currentRequest.approvalTime">
          {{ formatDateTime(currentRequest.approvalTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="审批意见" v-if="currentRequest.approvalComment" :span="2">
          {{ currentRequest.approvalComment }}
        </a-descriptions-item>
        <a-descriptions-item label="申请时间" :span="2">
          {{ formatDateTime(currentRequest.createdTime) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { overtimeApi } from '../api/overtime'
import { departmentApi } from '../api/department'
import dayjs from 'dayjs'

const loading = ref(false)
const approvalLoading = ref(false)
const approvalModalVisible = ref(false)
const detailModalVisible = ref(false)

const requests = ref([])
const departments = ref([])
const currentRequest = ref(null)
const approvalAction = ref('approve')
const approvalComment = ref('')

const searchKeyword = ref('')
const filterStatus = ref(null)
const filterType = ref(null)
const filterDept = ref(null)

const user = ref(null)

const columns = [
  { title: '申请ID', dataIndex: 'id', key: 'id', width: 60 },
  { title: '员工信息', key: 'empInfo', width: 160 },
  { title: '加班类型', key: 'overtimeType', width: 90 },
  { title: '加班日期', dataIndex: 'overtimeDate', key: 'overtimeDate', width: 90 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 90 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 90 },
  { title: '时长(h)', dataIndex: 'hours', key: 'hours', width: 70 },
  { title: '原因', dataIndex: 'reason', key: 'reason', ellipsis: true ,width: 100},
  { title: '状态', key: 'status', width: 100 },
  { title: '申请时间', key: 'createdTime', width: 150 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
]

const filteredRequests = computed(() => {
  let result = requests.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(req =>
      req.empName?.toLowerCase().includes(keyword) ||
      req.empNo?.toLowerCase().includes(keyword)
    )
  }

  // 状态过滤
  if (filterStatus.value) {
    result = result.filter(req => req.status === filterStatus.value)
  }

  // 类型过滤
  if (filterType.value) {
    result = result.filter(req => req.overtimeType === filterType.value)
  }

  // 部门过滤
  if (filterDept.value) {
    result = result.filter(req => req.deptId === filterDept.value)
  }

  return result
})

const pendingCount = computed(() => {
  return requests.value.filter(r => r.status === 'PENDING').length
})

const loadRequests = async () => {
  loading.value = true
  try {
    let res
    if (user.value?.role === 'ADMIN') {
      // 管理员获取所有加班申请
      res = await overtimeApi.getAll()
    } else if (user.value?.role === 'MANAGER') {
      // 经理获取待审批的加班申请
      res = await overtimeApi.getPending(user.value.userId)
    }
    requests.value = res?.data || []
  } catch (error) {
    message.error('加载加班申请失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadDepartments = async () => {
  try {
    const res = await departmentApi.getAll()
    departments.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  // 搜索逻辑由计算属性 filteredRequests 处理
}

const filterDeptOption = (input, option) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase())
}

const showDetail = (record) => {
  currentRequest.value = record
  detailModalVisible.value = true
}

const showApprovalModal = (record, action) => {
  currentRequest.value = record
  approvalAction.value = action
  approvalModalVisible.value = true
}

const handleApproval = async () => {
  if (!approvalComment.value || approvalComment.value.trim() === '') {
    message.warning('请输入审批意见')
    return
  }

  approvalLoading.value = true
  try {
    const approvalData = {
      requestId: currentRequest.value.id,
      approverId: user.value.userId,
      action: approvalAction.value === 'approve' ? 'APPROVE' : 'REJECT',
      comment: approvalComment.value
    }

    await overtimeApi.approve(approvalData)
    message.success(approvalAction.value === 'approve' ? '已批准加班申请' : '已拒绝加班申请')
    loadRequests()
    resetApprovalForm()
  } catch (error) {
    message.error(error.response?.data?.message || '审批失败')
  } finally {
    approvalLoading.value = false
  }
}

const resetApprovalForm = () => {
  approvalModalVisible.value = false
  approvalComment.value = ''
  currentRequest.value = null
}

const getOvertimeTypeColor = (type) => {
  const colorMap = {
    'WEEKDAY': 'blue',
    'WEEKEND': 'green',
    'HOLIDAY': 'orange'
  }
  return colorMap[type] || 'default'
}

const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'orange',
    'APPROVED': 'green',
    'REJECTED': 'red',
    'CANCELLED': 'default'
  }
  return colorMap[status] || 'default'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string' && time.includes(':')) {
    return time
  }
  return dayjs(time).format('HH:mm:ss')
}

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
  }
  loadDepartments()
  loadRequests()
})
</script>
