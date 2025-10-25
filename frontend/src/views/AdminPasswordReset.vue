<template>
  <div class="admin-password-reset">


    <!-- 筛选区域 -->
    <div style="margin-bottom: 16px; display: flex; justify-content: space-between;">
      <a-space>
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索用户名或真实姓名"
          style="width: 300px"
          @search="loadUsers"
        />
        <a-select
          v-model:value="filterRole"
          placeholder="用户角色"
          style="width: 150px"
          allowClear
          @change="loadUsers"
        >
          <a-select-option :value="null">全部角色</a-select-option>
          <a-select-option value="ADMIN">管理员</a-select-option>
          <a-select-option value="MANAGER">经理</a-select-option>
          <a-select-option value="EMPLOYEE">员工</a-select-option>
        </a-select>
        <a-select
          v-model:value="filterStatus"
          placeholder="账户状态"
          style="width: 120px"
          @change="loadUsers"
        >
          <a-select-option :value="null">全部</a-select-option>
          <a-select-option :value="1">正常</a-select-option>
          <a-select-option :value="0">禁用</a-select-option>
        </a-select>
      </a-space>
    </div>
    <a-alert
        message="安全提醒"
        description="您当前拥有管理员权限，可以重置任何用户的密码。请谨慎使用此功能。"
        type="warning"
        show-icon
        style="margin-bottom: 16px;"
    />
    <!-- 用户列表 -->
    <a-table
      :columns="columns"
      :data-source="filteredUsers"
      :loading="loading"
      rowKey="id"
      :pagination="{ pageSize: 10 }"
      :scroll="{ x: 1100 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'role'">
          <a-tag :color="getRoleColor(record.role)">
            {{ getRoleText(record.role) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'success' : 'default'">
            {{ record.status === 1 ? '正常' : '禁用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'createdTime'">
          {{ formatDateTime(record.createdTime) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-button
            type="link"
            size="small"
            @click="showResetModal(record)"
          >
            重置密码
          </a-button>
        </template>
      </template>
    </a-table>

    <!-- 重置密码模态框 -->
    <a-modal
      v-model:open="resetModalVisible"
      title="重置用户密码"
      @ok="handleReset"
      @cancel="resetForm"
      :confirmLoading="resetting"
    >
      <a-descriptions v-if="currentUser" :column="1" bordered style="margin-bottom: 16px;">
        <a-descriptions-item label="用户名">
          {{ currentUser.username }}
        </a-descriptions-item>
        <a-descriptions-item label="真实姓名">
          {{ currentUser.realName }}
        </a-descriptions-item>
        <a-descriptions-item label="角色">
          <a-tag :color="getRoleColor(currentUser.role)">
            {{ getRoleText(currentUser.role) }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="新密码" required>
          <a-input-password
            v-model:value="newPassword"
            placeholder="请输入新密码（至少8位）"
            autocomplete="new-password"
          />
        </a-form-item>
        <a-form-item label="确认密码" required>
          <a-input-password
            v-model:value="confirmPassword"
            placeholder="请再次输入新密码"
            autocomplete="new-password"
          />
        </a-form-item>
      </a-form>

      <a-alert
        message="密码要求：至少8个字符，包含大小写字母和数字"
        type="info"
        show-icon
      />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { userApi } from '../api/user'
import dayjs from 'dayjs'

const loading = ref(false)
const resetting = ref(false)
const resetModalVisible = ref(false)
const users = ref([])
const currentUser = ref(null)
const searchKeyword = ref('')
const filterRole = ref(null)
const filterStatus = ref(null)
const newPassword = ref('')
const confirmPassword = ref('')

const user = ref(null)

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
  { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
  { title: '真实姓名', dataIndex: 'realName', key: 'realName', width: 100 },
  { title: '角色', key: 'role', width: 90 },
  { title: '邮箱', dataIndex: 'email', key: 'email', ellipsis: true, width: 180 },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', key: 'createdTime', width: 180 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' }
]

// 筛选后的用户列表
const filteredUsers = computed(() => {
  let result = users.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(u =>
      u.username?.toLowerCase().includes(keyword) ||
      u.realName?.toLowerCase().includes(keyword)
    )
  }

  // 角色过滤
  if (filterRole.value) {
    result = result.filter(u => u.role === filterRole.value)
  }

  // 状态过滤
  if (filterStatus.value !== null) {
    result = result.filter(u => u.status === filterStatus.value)
  }

  return result
})

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
    if (user.value.role !== 'ADMIN') {
      message.error('您没有管理员权限')
      return
    }
  }
  loadUsers()
})

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await userApi.getAll()
    users.value = res.data
  } catch (error) {
    message.error('加载用户列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const showResetModal = (userRecord) => {
  currentUser.value = userRecord
  resetModalVisible.value = true
}

const handleReset = async () => {
  if (!newPassword.value || newPassword.value.length < 8) {
    message.warning('密码至少需要8个字符')
    return
  }

  if (newPassword.value !== confirmPassword.value) {
    message.warning('两次输入的密码不一致')
    return
  }

  // 验证密码强度：大小写字母和数字
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d!@#$%^&*()_+\-={}\[\]:";'<>?,.\\/]{8,}$/
  if (!passwordRegex.test(newPassword.value)) {
    message.warning('密码必须包含大小写字母、数字，可包含特殊符号')
    return
  }

  resetting.value = true
  try {
    await userApi.adminResetPassword({
      userId: currentUser.value.id,
      newPassword: newPassword.value,
      adminId: user.value.userId
    })
    message.success('密码重置成功！')
    resetModalVisible.value = false
    resetForm()
  } catch (error) {
    message.error(error.response?.data?.message || '重置失败')
  } finally {
    resetting.value = false
  }
}

const resetForm = () => {
  currentUser.value = null
  newPassword.value = ''
  confirmPassword.value = ''
}

const getRoleColor = (role) => {
  const colorMap = {
    'ADMIN': 'red',
    'MANAGER': 'orange',
    'EMPLOYEE': 'blue'
  }
  return colorMap[role] || 'default'
}

const getRoleText = (role) => {
  const textMap = {
    'ADMIN': '管理员',
    'MANAGER': '经理',
    'EMPLOYEE': '员工'
  }
  return textMap[role] || role
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
.admin-password-reset {
  padding: 0;
}
</style>
