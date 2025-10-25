<template>
  <div class="login-container">
    <!-- 背景粒子动画 -->
    <canvas ref="canvasRef" class="particle-canvas"></canvas>

    <!-- 登录卡片 -->
    <div class="login-box">

      <!-- 标题 -->
      <h1 class="welcome-title">欢迎回来！</h1>
      <!-- 登录表单 -->
      <a-form :model="formState" @finish="handleLogin" class="login-form">
        <a-form-item name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input
            v-model:value="formState.username"
            placeholder="用户名"
            size="large"
            class="custom-input"
          />
        </a-form-item>

        <a-form-item name="password" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password
            v-model:value="formState.password"
            placeholder="密码"
            size="large"
            class="custom-input"
          />
        </a-form-item>

        <a-form-item>
          <a-checkbox v-model:checked="rememberMe">保持登录状态</a-checkbox>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            block
            size="large"
            :loading="loading"
            class="login-button"
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>

      <!-- 底部链接 -->
      <div class="footer-links">
        <a @click="handleForgotPassword" class="footer-link">忘记密码？</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { authApi } from '../api/auth'

const router = useRouter()
const loading = ref(false)
const rememberMe = ref(true)
const canvasRef = ref(null)
const formState = reactive({
  username: '',
  password: ''
})

// 粒子动画
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

const ensureDeviceId = () => {
  const key = 'bizcore_device_id'
  let deviceId = localStorage.getItem(key)
  if (!deviceId) {
    if (window.crypto && typeof window.crypto.randomUUID === 'function') {
      deviceId = window.crypto.randomUUID()
    } else {
      deviceId = Math.random().toString(36).substring(2) + Date.now().toString(36)
    }
    localStorage.setItem(key, deviceId)
  }
  return deviceId
}

const handleLogin = async () => {
  loading.value = true
  try {
    const payload = {
      ...formState,
      deviceId: ensureDeviceId()
    }
    const res = await authApi.login(payload)
    localStorage.setItem('token', res.data.token)
    if (res.data.refreshToken) {
      localStorage.setItem('refreshToken', res.data.refreshToken)
    }
    if (res.data.sessionId) {
      localStorage.setItem('sessionId', res.data.sessionId)
    }
    localStorage.setItem('user', JSON.stringify(res.data))
    message.success('登录成功')

    const user = res.data
    if (user.role === 'EMPLOYEE') {
      router.push('/dashboard/my-attendance')
    } else if (user.role === 'MANAGER') {
      router.push('/dashboard')
    } else if (user.role === 'ADMIN') {
      router.push('/dashboard')
    } else {
      router.push('/dashboard')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleForgotPassword = () => {
  router.push('/forgot-password')
}
</script>

<style scoped>
.login-container {
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

/* 登录框 */
.login-box {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}


/* 标题 */
.welcome-title {
  font-size: 28px;
  font-weight: 400;
  text-align: center;
  color: #333;
  margin: 0 0 32px 0;
}

/* 表单 */
.login-form {
  margin-bottom: 0;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

/* 自定义输入框 */
.custom-input {
  border-radius: 8px;
  background: #f5f5f5;
  border: 1px solid transparent;
  transition: all 0.3s;
}



.login-form :deep(.ant-input),
.login-form :deep(.ant-input-password),
.login-form :deep(.ant-input-affix-wrapper) {
  background: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s;
}

.login-form :deep(.ant-input-affix-wrapper .ant-input) {
  background: transparent;
  padding: 0;
  border: none;
}

.login-form :deep(.ant-input:hover),
.login-form :deep(.ant-input:focus),
.login-form :deep(.ant-input-affix-wrapper:hover),
.login-form :deep(.ant-input-affix-wrapper:focus),
.login-form :deep(.ant-input-affix-wrapper-focused) {
  background: #fff;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

/* 复选框 */
.login-form :deep(.ant-checkbox-wrapper) {
  color: #666;
  font-size: 14px;
}

/* 登录按钮 */
.login-button {
  height: 48px;
  font-size: 14px;

  font-weight: 400;
  border-radius: 24px;
  background: #009ac7;
  border: none;
  transition: all 0.3s;
}

.login-button:hover {
  box-shadow: 0 6px 16px rgba(3, 169, 244, 0.4);
}

.login-button:active {
  transform: translateY(0);
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
  .login-box {
    max-width: 70%;
    padding: 40px 32px;
  }
  .welcome-title {
    font-size: 24px;
  }

}

@media (max-width: 480px) {
  .login-box {
    max-width: 90%;
    padding: 32px 24px;
  }
}
</style>
