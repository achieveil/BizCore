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
          v-model:value="filterDept"
          placeholder="选择部门"
          style="width: 200px"
          allowClear
          @change="handleSearch"
        >
          <a-select-option :value="null">全部部门</a-select-option>
          <a-select-option v-for="dept in departments" :key="dept.id" :value="dept.id">
            {{ dept.deptName }}
          </a-select-option>
        </a-select>
        <a-select
          v-model:value="filterStatus"
          placeholder="员工状态"
          style="width: 120px"
          @change="handleSearch"
        >
          <a-select-option :value="null">全部</a-select-option>
          <a-select-option :value="1">在职</a-select-option>
          <a-select-option :value="0">离职</a-select-option>
        </a-select>
      </a-space>
      <a-button type="primary" @click="showModal(null)">
        <PlusOutlined /> 新增员工
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="employees"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
      rowKey="id"
      :scroll="{ x: 1150 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'gender'">
          {{ record.gender || '-' }}
        </template>
        <template v-if="column.key === 'deptName'">
          {{ getDeptName(record.deptId) }}
        </template>
        <template v-if="column.key === 'hireDate'">
          {{ formatDate(record.hireDate) }}
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '在职' : '离职' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'role'">
          <a-tag :color="getRoleColor(record.userRole, record.manager)">
            {{ getRoleLabel(record.userRole, record.manager) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'accountStatus'">
          <a-tag :color="record.userId ? 'blue' : 'default'">
            {{ record.userId ? '已注册' : '未注册' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="showModal(record)">编辑</a-button>
            <a-button
              v-if="!record.userId"
              type="link"
              size="small"
              @click="openAccountModal(record)"
            >
              注册账号
            </a-button>
            <a-button
              v-else
              type="link"
              size="small"
              @click="openResetModal(record)"
            >
              重置密码
            </a-button>
            <a-popconfirm
              title="确定删除该员工吗？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑员工模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="editingEmployee ? '编辑员工' : '新增员工'"
      @ok="handleSave"
      @cancel="handleCancel"
      width="800px"
    >
      <a-form :model="formState" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="工号" required>
              <a-input v-model:value="formState.empNo" placeholder="请输入工号" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="姓名" required>
              <a-input v-model:value="formState.name" placeholder="请输入姓名" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="性别">
              <a-select v-model:value="formState.gender" placeholder="请选择性别">
                <a-select-option value="男">男</a-select-option>
                <a-select-option value="女">女</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="出生日期">
              <a-date-picker v-model:value="formState.birthDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="手机号" required>
              <a-input v-model:value="formState.phone" placeholder="请输入手机号" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="邮箱">
              <a-input v-model:value="formState.email" placeholder="请输入邮箱" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="身份证号">
              <a-input v-model:value="formState.idCard" placeholder="请输入身份证号" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="所属部门">
              <a-select v-model:value="formState.deptId" placeholder="请选择部门">
                <a-select-option v-for="dept in departments" :key="dept.id" :value="dept.id">
                  {{ dept.deptName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="职位">
              <a-input v-model:value="formState.position" placeholder="请输入职位" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="职级">
              <a-input v-model:value="formState.level" placeholder="请输入职级" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="入职日期">
              <a-date-picker v-model:value="formState.hireDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="薪资">
              <a-input-number v-model:value="formState.salary" :min="0" :precision="2" style="width: 100%" placeholder="请输入薪资" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="地址">
          <a-textarea v-model:value="formState.address" placeholder="请输入地址" :rows="2" />
        </a-form-item>

        <a-form-item label="状态">
          <a-radio-group v-model:value="formState.status">
            <a-radio :value="1">在职</a-radio>
            <a-radio :value="0">离职</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 注册账号模态框 -->
    <a-modal
      v-model:open="accountModalVisible"
      title="注册员工账号"
      @ok="handleRegisterAccount"
      @cancel="handleAccountModalCancel"
      :confirm-loading="accountSubmitting"
    >
      <div v-if="targetEmployee" style="margin-bottom: 16px;">
        <p>将为员工 <strong>{{ targetEmployee.name }}</strong> 创建登录账号。</p>
        <p>用户名默认为员工工号：<strong>{{ targetEmployee.empNo }}</strong></p>
      </div>
      <a-form :model="accountForm" layout="vertical">
        <a-form-item label="初始密码">
          <a-input-password
            v-model:value="accountForm.password"
            placeholder="留空则使用默认密码 PASSword123"
          />
        </a-form-item>
        <a-form-item label="安全问题" required>
          <a-input
            v-model:value="accountForm.securityQuestion"
            placeholder="请输入安全问题"
          />
        </a-form-item>
        <a-form-item label="安全答案" required>
          <a-input
            v-model:value="accountForm.securityAnswer"
            placeholder="请输入安全答案"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 重置密码模态框 -->
    <a-modal
      v-model:open="resetModalVisible"
      title="重置员工密码"
      @ok="handleResetPassword"
      @cancel="handleResetModalCancel"
      :confirm-loading="resetSubmitting"
    >
      <div v-if="targetEmployee" style="margin-bottom: 16px;">
        <p>将重置员工 <strong>{{ targetEmployee.name }}</strong> 的登录密码。</p>
        <p>用户名：<strong>{{ targetEmployee.empNo }}</strong></p>
      </div>
      <a-form :model="resetPasswordForm" layout="vertical">
        <a-form-item label="新密码">
          <a-input-password
            v-model:value="resetPasswordForm.password"
            placeholder="留空则重置为默认密码 PASSword123"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { employeeApi } from '../api/employee'
import { departmentApi } from '../api/department'
import dayjs from 'dayjs'

const loading = ref(false)
const employees = ref([])
const departments = ref([])
const searchKeyword = ref('')
const filterDept = ref(null)
const filterStatus = ref(null)
const modalVisible = ref(false)
const editingEmployee = ref(null)
const accountModalVisible = ref(false)
const resetModalVisible = ref(false)
const accountSubmitting = ref(false)
const resetSubmitting = ref(false)
const targetEmployee = ref(null)

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const formState = reactive({
  empNo: '',
  name: '',
  gender: '',
  birthDate: null,
  idCard: '',
  phone: '',
  email: '',
  deptId: null,
  position: '',
  level: '',
  hireDate: null,
  salary: null,
  address: '',
  status: 1
})

const accountForm = reactive({
  password: '',
  securityQuestion: '',
  securityAnswer: ''
})

const resetPasswordForm = reactive({
  password: ''
})

const columns = [
  { title: '工号', dataIndex: 'empNo', key: 'empNo', width: 120 },
  { title: '姓名', dataIndex: 'name', key: 'name', width: 100 },
  { title: '性别', key: 'gender', width: 70 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '部门', key: 'deptName', width: 120 },
  { title: '职位', dataIndex: 'position', key: 'position', width: 120 },
  { title: '入职日期', key: 'hireDate', width: 120 },
  { title: '状态', key: 'status', width: 80 },
  { title: '身份', key: 'role', width: 90 },
  { title: '账号状态', key: 'accountStatus', width: 110 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' }
]

const loadEmployees = async () => {
  loading.value = true
  try {
    const res = await employeeApi.search({
      keyword: searchKeyword.value,
      deptId: filterDept.value,
      status: filterStatus.value,
      page: pagination.current - 1,
      size: pagination.pageSize
    })
    employees.value = res.data.content
    pagination.total = res.data.totalElements
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadDepartments = async () => {
  try {
    const res = await departmentApi.getActive()
    departments.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const getDeptName = (deptId) => {
  const dept = departments.value.find(d => d.id === deptId)
  return dept ? dept.deptName : '-'
}

const resetAccountForm = () => {
  accountForm.password = ''
  accountForm.securityQuestion = ''
  accountForm.securityAnswer = ''
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

const handleSearch = () => {
  pagination.current = 1
  loadEmployees()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadEmployees()
}

const showModal = (record) => {
  editingEmployee.value = record
  if (record) {
    Object.assign(formState, {
      ...record,
      birthDate: record.birthDate ? dayjs(record.birthDate) : null,
      hireDate: record.hireDate ? dayjs(record.hireDate) : null
    })
  } else {
    resetForm()
  }
  modalVisible.value = true
}

const resetForm = () => {
  Object.assign(formState, {
    empNo: '',
    name: '',
    gender: '',
    birthDate: null,
    idCard: '',
    phone: '',
    email: '',
    deptId: null,
    position: '',
    level: '',
    hireDate: null,
    salary: null,
    address: '',
    status: 1
  })
}

const handleSave = async () => {
  try {
    const data = {
      ...formState,
      birthDate: formState.birthDate ? formState.birthDate.format('YYYY-MM-DD') : null,
      hireDate: formState.hireDate ? formState.hireDate.format('YYYY-MM-DD') : null
    }

    if (editingEmployee.value) {
      await employeeApi.update(editingEmployee.value.id, data)
      message.success('员工信息更新成功')
    } else {
      await employeeApi.create(data)
      message.success('员工创建成功')
    }

    modalVisible.value = false
    loadEmployees()
  } catch (error) {
    console.error(error)
  }
}

const handleCancel = () => {
  modalVisible.value = false
  resetForm()
}

const openAccountModal = (record) => {
  targetEmployee.value = record
  resetAccountForm()
  accountModalVisible.value = true
}

const handleAccountModalCancel = () => {
  accountModalVisible.value = false
  resetAccountForm()
  targetEmployee.value = null
}

const handleRegisterAccount = async () => {
  if (!targetEmployee.value) return
  if (!accountForm.securityQuestion.trim() || !accountForm.securityAnswer.trim()) {
    message.error('请填写安全问题和答案')
    return
  }
  accountSubmitting.value = true
  try {
    const payload = {
      securityQuestion: accountForm.securityQuestion.trim(),
      securityAnswer: accountForm.securityAnswer.trim()
    }
    if (accountForm.password) {
      payload.password = accountForm.password
    }
    await employeeApi.registerAccount(targetEmployee.value.id, payload)
    message.success('账号注册成功')
    handleAccountModalCancel()
    loadEmployees()
  } catch (error) {
    console.error(error)
  } finally {
    accountSubmitting.value = false
  }
}

const openResetModal = (record) => {
  targetEmployee.value = record
  resetPasswordForm.password = ''
  resetModalVisible.value = true
}

const handleResetModalCancel = () => {
  resetModalVisible.value = false
  resetPasswordForm.password = ''
  targetEmployee.value = null
}

const handleResetPassword = async () => {
  if (!targetEmployee.value) return
  resetSubmitting.value = true
  try {
    const payload = resetPasswordForm.password ? { password: resetPasswordForm.password } : {}
    await employeeApi.resetPassword(targetEmployee.value.id, payload)
    message.success('密码重置成功')
    handleResetModalCancel()
    loadEmployees()
  } catch (error) {
    console.error(error)
  } finally {
    resetSubmitting.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await employeeApi.delete(id)
    message.success('员工删除成功')
    loadEmployees()
  } catch (error) {
    console.error(error)
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(() => {
  loadEmployees()
  loadDepartments()
})
</script>
