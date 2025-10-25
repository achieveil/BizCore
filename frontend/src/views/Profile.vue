<template>
  <div class="profile-page">
    <div class="section-card">
      <div class="section-title">个人信息</div>
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="用户名">{{ userInfo.username }}</a-descriptions-item>
        <a-descriptions-item label="真实姓名">{{ userInfo.realName }}</a-descriptions-item>
        <a-descriptions-item label="邮箱">{{ userInfo.email }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="角色">
          <a-tag :color="getRoleColor(userInfo.role)">
            {{ getRoleText(userInfo.role) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="注册时间">{{ formatDateTime(userInfo.createdTime) }}</a-descriptions-item>
      </a-descriptions>
    </div>

    <div class="section-card">
      <div class="section-title">修改密码</div>
      <a-alert
        message="密码要求"
        description="密码必须包含大小写字母和数字，长度6-20位"
        type="info"
        show-icon
        style="margin-bottom: 24px;"
      />
      <a-form
        :model="passwordForm"
        @finish="handlePasswordChange"
        layout="vertical"
        class="password-form"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="原密码"
              name="oldPassword"
              :rules="[{ required: true, message: '请输入原密码' }]"
            >
              <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入原密码" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item
              label="新密码"
              name="newPassword"
              :rules="[
                { required: true, message: '请输入新密码' },
                { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,20}$/, message: '密码必须包含大小写字母和数字，长度6-20位' }
              ]"
            >
              <a-input-password v-model:value="passwordForm.newPassword" placeholder="包含大小写字母和数字，6-20位" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item
              label="确认密码"
              name="confirmPassword"
              :rules="[
                { required: true, message: '请确认密码' },
                { validator: validateConfirmPassword }
              ]"
            >
              <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="loading">
              修改密码
            </a-button>
            <a-button @click="resetPasswordForm">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { userApi } from '../api/user'
import dayjs from 'dayjs'

const loading = ref(false)
const userInfo = ref({})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value) => {
  if (value !== passwordForm.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
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
    'EMPLOYEE': '普通员工'
  }
  return textMap[role] || role
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
}

const handlePasswordChange = async () => {
  loading.value = true
  try {
    await userApi.updatePassword(userInfo.value.id, {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    message.success('密码修改成功')
    resetPasswordForm()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    userInfo.value = JSON.parse(userData)
  }
})
</script>

<style scoped>
.profile-page {
  padding: 0;
}

.section-card {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 16px;
}

.password-form {
  max-width: 100%;
}
</style>
