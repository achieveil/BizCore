<template>
  <div class="forgot-container">
    <!-- 背景粒子动画 -->
    <canvas ref="canvasRef" class="particle-canvas"></canvas>

    <!-- 忘记密码卡片 -->
    <div class="forgot-box">
      <!-- 标题 -->
      <h1 class="page-title">找回密码</h1>

      <!-- 步骤条 -->
      <a-steps :current="current" class="steps-bar" size="small">
        <a-step title="输入用户名" />
        <a-step title="回答密保问题" />
        <a-step title="验证手机" />
        <a-step title="设置密码" />
        <a-step title="完成" />
      </a-steps>

      <!-- 步骤1: 输入用户名 -->
      <a-form v-if="current === 0" :model="step1Form" @finish="handleStep1" class="forgot-form">
        <a-form-item name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input
            v-model:value="step1Form.username"
            placeholder="用户名"
            size="large"
            class="custom-input"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" block size="large" class="submit-button" :loading="loading">
            下一步
          </a-button>
        </a-form-item>
      </a-form>

      <!-- 步骤2: 回答密保问题 -->
      <a-form v-if="current === 1" :model="step2Form" @finish="handleStep2" class="forgot-form">
        <a-alert :message="securityQuestion" type="info" show-icon class="security-alert" />
        <a-form-item name="securityAnswer" :rules="[{ required: true, message: '请输入密保答案' }]">
          <a-input
            v-model:value="step2Form.securityAnswer"
            placeholder="密保答案"
            size="large"
            class="custom-input"
          />
        </a-form-item>
        <a-form-item>
          <div class="button-group">
            <a-button @click="current = 0" size="large" class="back-button">上一步</a-button>
            <a-button type="primary" html-type="submit" size="large" class="submit-button" :loading="loading">
              下一步
            </a-button>
          </div>
        </a-form-item>
      </a-form>

      <!-- 步骤3: 验证手机号中间四位 -->
      <a-form v-if="current === 2" :model="step3Form" @finish="handleStep3" class="forgot-form">
        <a-alert
          :message="`请输入手机号 ${maskedPhone} 中间四位数字`"
          type="info"
          show-icon
          class="security-alert"
        />
        <a-form-item
          name="phoneMiddle"
          :rules="[
            { required: true, message: '请输入手机号中间四位' },
            { pattern: /^\d{4}$/, message: '请输入4位数字' }
          ]"
        >
          <a-input
            v-model:value="step3Form.phoneMiddle"
            placeholder="手机号中间四位"
            size="large"
            class="custom-input"
            maxlength="4"
          />
        </a-form-item>
        <a-form-item>
          <div class="button-group">
            <a-button @click="current = 1" size="large" class="back-button">上一步</a-button>
            <a-button type="primary" html-type="submit" size="large" class="submit-button" :loading="loading">
              下一步
            </a-button>
          </div>
        </a-form-item>
      </a-form>

      <!-- 步骤4: 设置新密码 -->
      <a-form v-if="current === 3" :model="step4Form" @finish="handleStep4" class="forgot-form">
        <a-alert message="验证成功！请设置新密码" type="success" show-icon class="security-alert" />
        <a-form-item
          name="newPassword"
          :rules="[
            { required: true, message: '请输入新密码' },
            { pattern: passwordPattern, message: '密码必须包含大小写字母、数字，可包含特殊符号，长度6-20位' }
          ]"
        >
          <a-input-password
            v-model:value="step4Form.newPassword"
            placeholder="新密码（包含大小写字母和数字，可含特殊符号，6-20位）"
            size="large"
            class="custom-input"
          />
        </a-form-item>
        <a-form-item
          name="confirmPassword"
          :rules="[
            { required: true, message: '请确认新密码' },
            { validator: validateConfirmPassword }
          ]"
        >
          <a-input-password
            v-model:value="step4Form.confirmPassword"
            placeholder="确认新密码"
            size="large"
            class="custom-input"
          />
        </a-form-item>
        <a-form-item>
          <div class="button-group">
            <a-button @click="current = 2" size="large" class="back-button">上一步</a-button>
            <a-button type="primary" html-type="submit" size="large" class="submit-button" :loading="loading">
              提交
            </a-button>
          </div>
        </a-form-item>
      </a-form>

      <!-- 步骤5: 完成 -->
      <div v-if="current === 4" class="success-content">
        <div class="success-icon">
          <svg viewBox="0 0 24 24" >
            <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
          </svg>
        </div>
        <h2 class="success-title">密码重置成功</h2>
        <p class="success-subtitle">您可以使用新密码登录系统了</p>
        <a-button type="primary" size="large" @click="$router.push('/login')" class="submit-button">
          返回登录
        </a-button>
      </div>

      <!-- 返回登录链接 -->
      <div v-if="current < 4" class="footer-links">
        <a @click="$router.push('/login')" class="footer-link">返回登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { userApi } from '../api/user'

const router = useRouter()
const current = ref(0)
const securityQuestion = ref('')
const maskedPhone = ref('')
const canvasRef = ref(null)
const loading = ref(false)

// Password validation regex - moved from template to avoid parsing issues
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d!@#$%^&*()_+\-={}[\]:";'<>?,.\/\\]{6,20}$/

const step1Form = reactive({
  username: ''
})

const step2Form = reactive({
  securityAnswer: ''
})

const step3Form = reactive({
  phoneMiddle: ''
})

const step4Form = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 粒子动画（与登录页面相同）
class Particle {
  constructor(canvas) {
    this.canvas = canvas
    this.x = Math.random() * canvas.width
    this.y = Math.random() * canvas.height
    this.vx = (Math.random() - 0.5) * 0.5
    this.vy = (Math.random() - 0.5) * 0.5
    this.radius = Math.random() * 2 + 1
  }

  update() {
    this.x += this.vx
    this.y += this.vy

    if (this.x < 0 || this.x > this.canvas.width) this.vx = -this.vx
    if (this.y < 0 || this.y > this.canvas.height) this.vy = -this.vy
  }

  draw(ctx) {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2)
    // 降低粒子透明度从0.5到0.3
    ctx.fillStyle = 'rgba(24, 144, 255, 0.3)'
    ctx.fill()
  }
}

let particles = []
let animationId = null

const initParticles = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  particles = []
  // 减少粒子数量：从 /10000 改为 /20000
  const particleCount = Math.floor((canvas.width * canvas.height) / 20000)
  for (let i = 0; i < particleCount; i++) {
    particles.push(new Particle(canvas))
  }

  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    particles.forEach((particle, i) => {
      particle.update()
      particle.draw(ctx)

      // 绘制连线 - 减少连线距离从120到100
      particles.slice(i + 1).forEach(otherParticle => {
        const dx = particle.x - otherParticle.x
        const dy = particle.y - otherParticle.y
        const distance = Math.sqrt(dx * dx + dy * dy)

        if (distance < 100) {
          ctx.beginPath()
          // 降低透明度从0.2到0.15
          ctx.strokeStyle = `rgba(24, 144, 255, ${0.15 * (1 - distance / 100)})`
          ctx.lineWidth = 1
          ctx.moveTo(particle.x, particle.y)
          ctx.lineTo(otherParticle.x, otherParticle.y)
          ctx.stroke()
        }
      })
    })

    animationId = requestAnimationFrame(animate)
  }

  animate()
}

const handleResize = () => {
  if (canvasRef.value) {
    canvasRef.value.width = window.innerWidth
    canvasRef.value.height = window.innerHeight
  }
}

onMounted(() => {
  initParticles()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
})

// 验证确认密码
const validateConfirmPassword = (rule, value) => {
  if (value && value !== step4Form.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const handleStep1 = async () => {
  loading.value = true
  try {
    // 获取密保问题和脱敏的手机号
    const res = await userApi.getSecurityQuestion(step1Form.username)
    console.log('Step1 response:', res)

    // API返回的数据在 res.data 中
    if (res.data) {
      securityQuestion.value = res.data.securityQuestion || '未设置密保问题'
      maskedPhone.value = res.data.maskedPhone || '***-****-****'
    }

    current.value = 1
    message.success('请回答密保问题')
  } catch (error) {
    console.error('Step1 error:', error)
    message.error(error.response?.data?.message || '获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const handleStep2 = async () => {
  loading.value = true
  try {
    // 验证密保答案
    await userApi.verifySecurityAnswer({
      username: step1Form.username,
      securityAnswer: step2Form.securityAnswer
    })
    current.value = 2
    message.success('密保答案验证成功，请输入手机号中间四位')
  } catch (error) {
    console.error('Step2 error:', error)
    message.error(error.response?.data?.message || '密保答案验证失败')
  } finally {
    loading.value = false
  }
}

const handleStep3 = async () => {
  loading.value = true
  try {
    // 验证手机号中间四位
    await userApi.verifyPhone({
      username: step1Form.username,
      phoneMiddle: step3Form.phoneMiddle
    })
    current.value = 3
    message.success('手机号验证成功！请设置新密码')
  } catch (error) {
    console.error('Step3 error:', error)
    message.error(error.response?.data?.message || '手机号验证失败')
  } finally {
    loading.value = false
  }
}

const handleStep4 = async () => {
  loading.value = true
  try {
    // 验证手机号中间四位并重置密码
    console.log('Step4 data:', {
      username: step1Form.username,
      securityAnswer: step2Form.securityAnswer,
      phoneMiddle: step3Form.phoneMiddle,
      newPassword: step4Form.newPassword
    })

    await userApi.resetPassword({
      username: step1Form.username,
      securityAnswer: step2Form.securityAnswer,
      newPassword: step4Form.newPassword,
      phoneMiddle: step3Form.phoneMiddle
    })

    current.value = 4
    message.success('密码重置成功！')
  } catch (error) {
    console.error('Step4 error:', error)
    message.error(error.response?.data?.message || '密码重置失败，请检查输入信息')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #ffffff;
  overflow: hidden;
}

/* 背景粒子画布 */
.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

/* 忘记密码框 */
.forgot-box {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 标题 */
.page-title {
  font-size: 28px;
  font-weight: 400;
  text-align: center;
  color: #333;
  margin: 0 0 24px 0;
}

/* 步骤条 */
.steps-bar {
  margin-bottom: 32px;
}

/* 步骤项容器 - 动态分配空间 */
.steps-bar :deep(.ant-steps-item) {
  flex: 0 0 auto;
  min-width: 32px;
  transition: all 0.3s ease;
}

/* 当前步骤占据更多空间 */
.steps-bar :deep(.ant-steps-item-process) {
  flex: 1 1 auto;
}

/* 隐藏非当前步骤的标题 */
.steps-bar :deep(.ant-steps-item-wait .ant-steps-item-title),
.steps-bar :deep(.ant-steps-item-finish .ant-steps-item-title) {
  display: none;
}

/* 显示当前步骤的标题，确保完整显示 */
.steps-bar :deep(.ant-steps-item-process .ant-steps-item-title) {
  display: block;
  white-space: nowrap;
  overflow: visible;
}

/* 表单 */
.forgot-form {
  margin-bottom: 0;
}

.forgot-form :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.forgot-form :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

/* 安全提示 */
.security-alert {
  margin-bottom: 20px;
  border-radius: 8px;
}

/* 自定义输入框 */
.custom-input {
  border-radius: 8px;
  background: #f5f5f5;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.forgot-form :deep(.ant-input),
.forgot-form :deep(.ant-input-password),
.forgot-form :deep(.ant-input-affix-wrapper) {
  background: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s;
}

.forgot-form :deep(.ant-input-affix-wrapper .ant-input) {
  background: transparent;
  padding: 0;
  border: none;
}

.forgot-form :deep(.ant-input:hover),
.forgot-form :deep(.ant-input:focus),
.forgot-form :deep(.ant-input-affix-wrapper:hover),
.forgot-form :deep(.ant-input-affix-wrapper:focus),
.forgot-form :deep(.ant-input-affix-wrapper-focused) {
  background: #fff;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 12px;
  width: 100%;
}

.button-group .back-button,
.button-group .submit-button {
  flex: 1;
  height: 48px;
  font-size: 14px;
  font-weight: 400;
  border-radius: 24px;
  transition: all 0.3s;
}

/* 上一步按钮 */
.back-button {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  color: rgba(0, 0, 0, 0.65);
}

.back-button:hover {
  border-color: #1890ff;
  color: #1890ff;
}

/* 提交按钮 */
.submit-button {
  background: #009ac7;
  border: none;
  color: white;
}

.submit-button:hover {
  background: #007ea8;
  box-shadow: 0 6px 16px rgba(3, 169, 244, 0.4);
}

.submit-button:active {
  transform: translateY(0);
}

/* 成功页面 */
.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin: 0 auto 24px;
  box-shadow: 0 4px 20px rgba(82, 196, 26, 0.4);
}

.success-icon svg {
  width: 48px;
  height: 48px;
}

.success-title {
  font-size: 24px;
  font-weight: 500;
  color: #333;
  margin: 0 0 12px 0;
}

.success-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0 0 32px 0;
}

/* 底部链接 */
.footer-links {
  text-align: center;
  margin-top: 24px;
}

.footer-link {
  color: #1890ff;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.3s;
  cursor: pointer;
}

.footer-link:hover {
  color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .forgot-box {
    max-width: 70%;
    padding: 40px 32px;
  }

  .page-title {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .forgot-box {
    max-width: 90%;
    padding: 32px 24px;
  }

  .page-title {
    font-size: 22px;
  }
}
</style>
