<template>
  <a-layout class="dashboard-layout">
    <!-- 桌面端侧边栏 -->
    <a-layout-sider
      v-if="!isMobile"
      v-model:collapsed="collapsed"
      collapsible
      class="dashboard-sider"
      :width="200"
      :collapsed-width="80"
    >
      <div class="logo">
        <h3 v-if="!collapsed" class="logo-text">BizCore</h3>
        <h3 v-else class="logo-text-collapsed">BC</h3>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
      >
        <template v-for="item in menuItems" :key="item.key">
          <a-sub-menu v-if="item.children" :key="item.key">
            <template #title>
              <component :is="item.icon" />
              <span>{{ item.label }}</span>
            </template>
            <a-menu-item v-for="child in item.children" :key="child.key">
              <component :is="child.icon" />
              <span>{{ child.label }}</span>
            </a-menu-item>
          </a-sub-menu>
          <a-menu-item v-else :key="item.key">
            <component :is="item.icon" />
            <span>{{ item.label }}</span>
          </a-menu-item>
        </template>
      </a-menu>
    </a-layout-sider>

    <!-- 移动端抽屉式侧边栏 -->
    <a-drawer
      v-model:open="drawerVisible"
      placement="left"
      :closable="false"
      :width="250"
      :body-style="{ padding: 0, background: '#001529' }"
      class="mobile-drawer"
    >
      <div class="logo mobile-logo">
        <h3 class="logo-text">BizCore</h3>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        theme="dark"
        mode="inline"
        @click="handleMobileMenuClick"
        class="mobile-menu"
      >
        <template v-for="item in menuItems" :key="item.key">
          <a-sub-menu v-if="item.children" :key="item.key">
            <template #title>
              <component :is="item.icon" />
              <span>{{ item.label }}</span>
            </template>
            <a-menu-item v-for="child in item.children" :key="child.key">
              <component :is="child.icon" />
              <span>{{ child.label }}</span>
            </a-menu-item>
          </a-sub-menu>
          <a-menu-item v-else :key="item.key">
            <component :is="item.icon" />
            <span>{{ item.label }}</span>
          </a-menu-item>
        </template>
      </a-menu>
    </a-drawer>

    <a-layout class="dashboard-main" :class="{ 'collapsed': collapsed && !isMobile, 'mobile': isMobile }">
      <a-layout-header class="dashboard-header">
        <div class="header-left">
          <MenuUnfoldOutlined
            v-if="!isMobile && collapsed"
            @click="collapsed = !collapsed"
            class="menu-icon"
          />
          <MenuFoldOutlined
            v-else-if="!isMobile && !collapsed"
            @click="collapsed = !collapsed"
            class="menu-icon"
          />
          <MenuOutlined
            v-if="isMobile"
            @click="drawerVisible = true"
            class="menu-icon"
          />
          <span v-if="isMobile" class="mobile-title">BizCore</span>
        </div>
        <a-dropdown>
          <a class="ant-dropdown-link" @click.prevent>
            <span class="user-name">{{ user?.realName || user?.username }}</span>
            <DownOutlined />
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="$router.push('/dashboard/profile')">
                <UserOutlined />
                个人中心
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item @click="handleLogout">
                <LogoutOutlined />
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-layout-header>

      <a-layout-content class="dashboard-content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </a-layout-content>

      <a-layout-footer class="dashboard-footer">
        achieveil ©2025
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Modal } from 'ant-design-vue'
import {
  DashboardOutlined,
  TeamOutlined,
  ApartmentOutlined,
  UserOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  FileTextOutlined,
  FieldTimeOutlined,
  AuditOutlined,
  SettingOutlined,
  LogoutOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  MenuOutlined,
  DownOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)
const drawerVisible = ref(false)
const selectedKeys = ref(['/dashboard'])
const openKeys = ref([])
const user = ref(null)
const isMobile = ref(false)

// 检测屏幕尺寸
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  // 在移动端自动折叠侧边栏
  if (isMobile.value) {
    collapsed.value = true
    drawerVisible.value = false
  }
}

// 处理窗口大小变化
const handleResize = () => {
  checkMobile()
}

// 根据角色动态生成菜单项
const menuItems = computed(() => {
  const role = user.value?.role || 'EMPLOYEE'

  const allMenus = {
    // 管理员和经理可见的首页
    common: [
      { key: '/dashboard', icon: DashboardOutlined, label: '首页', roles: ['ADMIN', 'MANAGER'] }
    ],
    // 员工功能
    employee: [
      { key: '/dashboard/my-attendance', icon: ClockCircleOutlined, label: '我的考勤', roles: ['EMPLOYEE', 'MANAGER'] },
      { key: '/dashboard/leave-management', icon: FileTextOutlined, label: '请假管理', roles: ['EMPLOYEE', 'MANAGER'] },
      { key: '/dashboard/overtime-management', icon: FieldTimeOutlined, label: '加班管理', roles: ['EMPLOYEE', 'MANAGER'] }
    ],
    // 经理功能
    manager: [
      { key: '/dashboard/leave-approval', icon: FileTextOutlined, label: '请假审批', roles: ['MANAGER', 'ADMIN'] },
      { key: '/dashboard/overtime-approval', icon: FieldTimeOutlined, label: '加班审批', roles: ['MANAGER', 'ADMIN'] },
      { key: '/dashboard/manager-attendance', icon: CalendarOutlined, label: '部门考勤', roles: ['MANAGER'] },
      { key: '/dashboard/attendance-rule-settings', icon: SettingOutlined, label: '考勤规则', roles: ['MANAGER'] }
    ],
    // 管理员功能
    admin: [
      { key: '/dashboard/employees', icon: TeamOutlined, label: '员工管理', roles: ['ADMIN'] },
      { key: '/dashboard/departments', icon: ApartmentOutlined, label: '部门管理', roles: ['ADMIN'] },
      { key: '/dashboard/attendances', icon: CalendarOutlined, label: '考勤管理', roles: ['ADMIN'] }
    ],
    // 个人中心（所有角色）
    profile: [
      { key: '/dashboard/profile', icon: UserOutlined, label: '个人中心', roles: ['ADMIN', 'MANAGER', 'EMPLOYEE'] }
    ]
  }

  // 合并所有菜单并根据角色过滤
  const menus = [
    ...allMenus.common,
    ...allMenus.employee,
    ...allMenus.manager,
    ...allMenus.admin,
    ...allMenus.profile
  ]

  return menus.filter(menu => menu.roles.includes(role))
})

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) {
    user.value = JSON.parse(userData)
  }
  selectedKeys.value = [route.path]

  // 初始化检测屏幕尺寸
  checkMobile()
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 清理监听器
  window.removeEventListener('resize', handleResize)
})

watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath]
})

const handleMenuClick = ({ key }) => {
  router.push(key)
}

const handleMobileMenuClick = ({ key }) => {
  router.push(key)
  // 移动端点击菜单后关闭抽屉
  drawerVisible.value = false
}

const handleLogout = () => {
  Modal.confirm({
    title: '确认退出',
    content: '您确定要退出登录吗？',
    onOk() {
      localStorage.removeItem('user')
      router.push('/login')
    }
  })
}
</script>

<style scoped>
/* 主布局 */
.dashboard-layout {
  min-height: 100vh;
}

/* 侧边栏 */
.dashboard-sider {
  overflow: auto;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
}

/* Logo区域 */
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
}

.logo-text {
  color: white;
  text-align: center;
  padding: 16px 0;
  margin: 0;
  font-size: 20px;
}

.logo-text-collapsed {
  color: white;
  text-align: center;
  margin: 0;
  font-size: 18px;
}

/* 移动端抽屉 Logo */
.mobile-logo {
  background: #001529;
}

/* 主内容区 */
.dashboard-main {
  margin-left: 200px;
  transition: margin-left 0.2s;
  min-height: 100vh;
}

.dashboard-main.collapsed {
  margin-left: 80px;
}

.dashboard-main.mobile {
  margin-left: 0;
}

/* 顶部导航栏 */
.dashboard-header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 菜单折叠图标 */
.menu-icon {
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
  padding: 8px;
  display: flex;
  align-items: center;
}

.menu-icon:hover {
  color: #1890ff;
}

.mobile-title {
  font-size: 18px;
  font-weight: 600;
  color: #001529;
}

/* 用户名 */
.user-name {
  margin-right: 8px;
}

/* 内容区域 */
.dashboard-content {
  margin: 0;
}

.content-wrapper {
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 64px - 70px);
}

/* 底部 */
.dashboard-footer {
  text-align: center;
  padding: 24px 50px;
}

/* 下拉菜单链接 */
.ant-dropdown-link {
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 移动端抽屉样式 */
.mobile-drawer :deep(.ant-drawer-body) {
  background: #001529 !important;
  padding: 0;
}

.mobile-drawer :deep(.ant-drawer-content-wrapper) {
  background: #001529;
}

.mobile-drawer :deep(.ant-drawer-content) {
  background: #001529;
}

.mobile-menu {
  background: #001529 !important;
  border-right: none;
}

.mobile-menu :deep(.ant-menu-item) {
  background: #001529 !important;
}

.mobile-menu :deep(.ant-menu-item-selected) {
  background: #1890ff !important;
}

/* 响应式样式 - 平板 */
@media (max-width: 992px) {
  .dashboard-main {
    margin-left: 0;
  }

  .content-wrapper {
    padding: 16px;
  }

  .dashboard-header {
    padding: 0 16px;
  }
}

/* 响应式样式 - 移动端 */
@media (max-width: 768px) {
  .dashboard-header {
    padding: 0 12px;
  }

  .content-wrapper {
    padding: 12px;
    min-height: calc(100vh - 64px - 62px);
  }

  .dashboard-footer {
    padding: 16px 12px;
    font-size: 12px;
  }

  .user-name {
    display: none;
  }

  .ant-dropdown-link {
    font-size: 14px;
  }

  .mobile-title {
    font-size: 16px;
  }
}

/* 响应式样式 - 小屏手机 */
@media (max-width: 480px) {
  .dashboard-header {
    padding: 0 8px;
  }

  .content-wrapper {
    padding: 8px;
  }

  .menu-icon {
    font-size: 16px;
    padding: 6px;
  }

  .mobile-title {
    font-size: 14px;
  }
}
</style>
