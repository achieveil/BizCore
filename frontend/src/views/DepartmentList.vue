<template>
  <div>
    <div style="margin-bottom: 16px; display: flex; justify-content: space-between;">
      <a-space>
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索部门名称或编码"
          style="width: 300px"
          @search="handleSearch"
        />
        <a-select
          v-model:value="filterStatus"
          placeholder="部门状态"
          style="width: 120px"
          @change="handleSearch"
        >
          <a-select-option :value="null">全部</a-select-option>
          <a-select-option :value="1">启用</a-select-option>
          <a-select-option :value="0">禁用</a-select-option>
        </a-select>
      </a-space>
      <a-button type="primary" @click="showModal(null)">
        <PlusOutlined /> 新增部门
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="filteredDepartments"
      :loading="loading"
      rowKey="id"
      :scroll="{ x: 1150 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'managers'">
          <template v-if="record.managers && record.managers.length > 0">
            <a-tag v-for="manager in record.managers" :key="manager.managerId" color="blue" style="margin: 2px;">
              {{ manager.managerName }}
            </a-tag>
          </template>
          <template v-else>
            <span style="color: #999;">未设置</span>
          </template>
        </template>
        <template v-if="column.key === 'employeeCount'">
          <a-badge :count="record.employeeCount" :number-style="{ backgroundColor: '#52c41a' }" />
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'createdTime'">
          {{ formatDateTime(record.createdTime) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="showModal(record)">编辑</a-button>
            <a-popconfirm
              title="确定删除该部门吗？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑部门模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="editingDept ? '编辑部门' : '新增部门'"
      @ok="handleSave"
      @cancel="handleCancel"
    >
      <a-form :model="formState" layout="vertical">
        <a-form-item label="部门名称" required>
          <a-input v-model:value="formState.deptName" placeholder="请输入部门名称" />
        </a-form-item>

        <a-form-item label="部门编码" required>
          <a-input v-model:value="formState.deptCode" placeholder="请输入部门编码" />
        </a-form-item>

        <a-form-item label="上级部门">
          <a-tree-select
            v-model:value="formState.parentId"
            :tree-data="deptTreeData"
            placeholder="请选择上级部门（可选）"
            tree-default-expand-all
            allowClear
          />
        </a-form-item>

        <a-form-item label="部门描述">
          <a-textarea v-model:value="formState.description" placeholder="请输入部门描述" :rows="3" />
        </a-form-item>

        <a-form-item label="部门经理">
          <a-select
            v-model:value="selectedManagerId"
            placeholder="请选择部门经理（可为空）"
            allowClear
            show-search
            option-filter-prop="children"
          >
            <a-select-option v-for="emp in employees" :key="emp.id" :value="emp.id">
              {{ emp.name }} ({{ emp.empNo }})
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="状态">
          <a-radio-group v-model:value="formState.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { departmentApi } from '../api/department'
import { departmentManagerApi } from '../api/departmentManager'
import { employeeApi } from '../api/employee'
import dayjs from 'dayjs'

const loading = ref(false)
const departments = ref([])
const employees = ref([])
const searchKeyword = ref('')
const filterStatus = ref(null)
const modalVisible = ref(false)
const editingDept = ref(null)
const selectedManagerId = ref(null)
const currentUser = ref(null)

const formState = reactive({
  deptName: '',
  deptCode: '',
  description: '',
  parentId: 0,
  status: 1
})

const columns = [
  { title: '部门名称', dataIndex: 'deptName', key: 'deptName', width: 150 },
  { title: '部门编码', dataIndex: 'deptCode', key: 'deptCode', width: 120 },
  { title: '部门经理', key: 'managers', width: 150 },
  { title: '员工数量', key: 'employeeCount', width: 100 },
  { title: '部门描述', dataIndex: 'description', key: 'description', ellipsis: true },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', key: 'createdTime', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

// 筛选后的部门列表
const filteredDepartments = computed(() => {
  let result = departments.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(dept =>
      dept.deptName?.toLowerCase().includes(keyword) ||
      dept.deptCode?.toLowerCase().includes(keyword)
    )
  }

  // 状态过滤
  if (filterStatus.value !== null) {
    result = result.filter(dept => dept.status === filterStatus.value)
  }

  return result
})

const deptTreeData = computed(() => {
  const buildTree = (items, parentId = 0) => {
    return items
      .filter(item => item.parentId === parentId)
      .map(item => ({
        title: item.deptName,
        value: item.id,
        key: item.id,
        children: buildTree(items, item.id)
      }))
  }
  return [
    { title: '顶级部门', value: 0, key: 0 },
    ...buildTree(departments.value)
  ]
})

const loadDepartments = async () => {
  loading.value = true
  try {
    const res = await departmentApi.getAll()
    departments.value = res.data
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

const handleSearch = () => {
  // 搜索逻辑由计算属性 filteredDepartments 处理
}

const showModal = (record) => {
  editingDept.value = record
  if (record) {
    Object.assign(formState, record)
    selectedManagerId.value = record.managers?.[0]?.managerId || null
  } else {
    resetForm()
  }
  modalVisible.value = true
}

const resetForm = () => {
  Object.assign(formState, {
    deptName: '',
    deptCode: '',
    description: '',
    parentId: 0,
    status: 1
  })
  selectedManagerId.value = null
}

const handleSave = async () => {
  try {
    let deptId
    if (editingDept.value) {
      const res = await departmentApi.update(editingDept.value.id, formState)
      deptId = res.data?.id || editingDept.value.id
      message.success('部门信息更新成功')
    } else {
      const res = await departmentApi.create(formState)
      deptId = res.data?.id
      message.success('部门创建成功')
    }

    if (deptId) {
      const payload = {
        managerId: selectedManagerId.value ?? null
      }
      if (currentUser.value?.userId) {
        payload.assignedBy = currentUser.value.userId
      }
      await departmentManagerApi.setManager(deptId, payload)
    }

    modalVisible.value = false
    loadDepartments()
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
    await departmentApi.delete(id)
    message.success('部门删除成功')
    loadDepartments()
  } catch (error) {
    console.error(error)
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    currentUser.value = JSON.parse(userData)
  }
  loadDepartments()
  loadEmployees()
})
</script>
