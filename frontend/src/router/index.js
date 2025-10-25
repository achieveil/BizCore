import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPassword.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue')
      },
      {
        path: 'employees',
        name: 'EmployeeList',
        component: () => import('../views/EmployeeList.vue')
      },
      {
        path: 'departments',
        name: 'DepartmentList',
        component: () => import('../views/DepartmentList.vue')
      },
      {
        path: 'attendances',
        name: 'AttendanceList',
        component: () => import('../views/AttendanceList.vue')
      },
      {
        path: 'my-attendance',
        name: 'EmployeeAttendance',
        component: () => import('../views/EmployeeAttendance.vue')
      },
      {
        path: 'leave-management',
        name: 'LeaveManagement',
        component: () => import('../views/LeaveManagement.vue')
      },
      {
        path: 'overtime-management',
        name: 'OvertimeManagement',
        component: () => import('../views/OvertimeManagement.vue')
      },
      {
        path: 'manager-attendance',
        name: 'ManagerAttendance',
        component: () => import('../views/ManagerAttendance.vue')
      },
      {
        path: 'attendance-rule-settings',
        name: 'AttendanceRuleSettings',
        component: () => import('../views/AttendanceRuleSettings.vue')
      },
      {
        path: 'leave-approval',
        name: 'LeaveApproval',
        component: () => import('../views/LeaveApproval.vue')
      },
      {
        path: 'overtime-approval',
        name: 'OvertimeApproval',
        component: () => import('../views/OvertimeApproval.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('user')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
