<template>
  <div>
    <!-- 管理员控制台 -->
    <div v-if="isAdmin">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-card>
            <a-statistic
              title="员工总数"
              :value="statistics.total"
              :value-style="{ color: '#3f8600' }"
            >
              <template #prefix>
                <TeamOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card>
            <a-statistic
              title="在职员工"
              :value="statistics.active"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card>
            <a-statistic
              title="部门总数"
              :value="departments.length"
              :value-style="{ color: '#cf1322' }"
            >
              <template #prefix>
                <ApartmentOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 24px">
        <a-col :span="12">
          <a-card>
            <PieChart title="员工性别分布" :data="genderData" />
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card>
            <BarChart title="部门员工数量分布" :data="departmentData" />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="12">
          <a-card>
            <PieChart title="薪资分布统计" :data="salaryData" />
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card>
            <LineChart title="员工入职趋势" :xData="hireTrendX" :yData="hireTrendY" />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="24">
          <a-card title="最近加入的员工">
            <a-table
              :columns="columns"
              :data-source="recentEmployees"
              :pagination="false"
              :loading="loading"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'deptName'">
                  {{ getDeptName(record.deptId) }}
                </template>
                <template v-if="column.key === 'status'">
                  <a-tag :color="record.status === 1 ? 'green' : 'red'">
                    {{ record.status === 1 ? '在职' : '离职' }}
                  </a-tag>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 经理视图 -->
    <div v-else-if="isManager">
      <a-spin :spinning="loading">
        <template v-if="managerDepartments.length">
          <a-row :gutter="16">
            <a-col v-for="dept in managerDepartments" :key="dept.deptId" :span="8">
              <a-card>
                <a-statistic :title="dept.deptName" :value="dept.employeeCount || 0">
                  <template #suffix>
                    <TeamOutlined />
                  </template>
                </a-statistic>
              </a-card>
            </a-col>
          </a-row>

          <a-row :gutter="16" style="margin-top: 24px">
            <a-col :span="12">
              <a-card title="部门成员">
                <a-table
                  :columns="managerEmployeeColumns"
                  :data-source="managerEmployees"
                  :pagination="{ pageSize: 5 }"
                  rowKey="id"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'deptName'">
                      {{ getDeptName(record.deptId) }}
                    </template>
                  </template>
                </a-table>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card title="最近考勤（7日内）">
                <a-table
                  :columns="managerAttendanceColumns"
                  :data-source="managerAttendance"
                  :pagination="{ pageSize: 5 }"
                  rowKey="id"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'attendanceDate'">
                      {{ formatDate(record.attendanceDate) }}
                    </template>
                    <template v-if="column.key === 'status'">
                      <a-tag :color="getAttendanceStatusColor(record.status)">
                        {{ getAttendanceStatusText(record.status) }}
                      </a-tag>
                    </template>
                    <template v-if="column.key === 'checkInTime'">
                      {{ formatTime(record.checkInTime) }}
                    </template>
                    <template v-if="column.key === 'checkOutTime'">
                      {{ formatTime(record.checkOutTime) }}
                    </template>
                  </template>
                </a-table>
              </a-card>
            </a-col>
          </a-row>
        </template>
        <a-empty v-else description="尚未分配管理的部门" />
      </a-spin>
    </div>

    <!-- 普通员工欢迎页 -->
    <div v-else>
      <a-card>
        <h3>欢迎回来，{{ currentUser?.realName || currentUser?.username || '用户' }}！</h3>
        <p>请通过左侧菜单访问相应功能。</p>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { TeamOutlined, UserOutlined, ApartmentOutlined } from '@ant-design/icons-vue'
import { employeeApi } from '../api/employee'
import { departmentApi } from '../api/department'
import { departmentManagerApi } from '../api/departmentManager'
import { attendanceApi } from '../api/attendance'
import dayjs from 'dayjs'
import PieChart from '../components/PieChart.vue'
import BarChart from '../components/BarChart.vue'
import LineChart from '../components/LineChart.vue'

const loading = ref(false)
const statistics = ref({ total: 0, active: 0 })
const departments = ref([])
const recentEmployees = ref([])
const genderStats = ref([])
const departmentStats = ref([])
const salaryStats = ref({})
const hireTrendStats = ref({})
const currentUser = ref(null)
const managerDepartments = ref([])
const managerEmployees = ref([])
const managerAttendance = ref([])

const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
const isManager = computed(() => currentUser.value?.role === 'MANAGER')

const columns = [
  { title: '工号', dataIndex: 'empNo', key: 'empNo' },
  { title: '姓名', dataIndex: 'name', key: 'name' },
  { title: '性别', dataIndex: 'gender', key: 'gender' },
  { title: '部门', key: 'deptName' },
  { title: '职位', dataIndex: 'position', key: 'position' },
  { title: '入职日期', dataIndex: 'hireDate', key: 'hireDate' },
  { title: '状态', key: 'status' }
]

const managerEmployeeColumns = [
  { title: '工号', dataIndex: 'empNo', key: 'empNo' },
  { title: '姓名', dataIndex: 'name', key: 'name' },
  { title: '部门', key: 'deptName' },
  { title: '职位', dataIndex: 'position', key: 'position' },
  { title: '联系电话', dataIndex: 'phone', key: 'phone' }
]

const managerAttendanceColumns = [
  { title: '员工', dataIndex: 'empName', key: 'empName' },
  { title: '部门', dataIndex: 'deptName', key: 'deptName' },
  { title: '日期', key: 'attendanceDate' },
  { title: '状态', key: 'status' },
  { title: '签到', key: 'checkInTime' },
  { title: '签退', key: 'checkOutTime' },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true }
]

const genderData = computed(() => {
  return genderStats.value.map(item => ({
    name: item.name,
    value: item.value
  }))
})

const departmentData = computed(() => {
  return departmentStats.value.map(item => ({
    name: item.name,
    value: item.value
  }))
})

const salaryData = computed(() => {
  return Object.entries(salaryStats.value).map(([key, value]) => ({
    name: key,
    value: value
  }))
})

const hireTrendX = computed(() => {
  return Object.keys(hireTrendStats.value).sort()
})

const hireTrendY = computed(() => {
  return hireTrendX.value.map(key => hireTrendStats.value[key])
})

const getDeptName = (deptId) => {
  const dept = departments.value.find(d => d.id === deptId)
  return dept ? dept.deptName : '-'
}

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string') {
    const parsed = dayjs(time, 'HH:mm:ss', true)
    return parsed.isValid() ? parsed.format('HH:mm') : time
  }
  return dayjs(time).format('HH:mm')
}

const getAttendanceStatusText = (status) => {
  const map = {
    NORMAL: '正常',
    LATE: '迟到',
    EARLY: '早退',
    LATE_AND_EARLY: '迟到且早退',
    EARLY_LEAVE: '早退',
    ABSENT: '缺勤',
    LEAVE: '请假'
  }
  return map[status] || status
}

const getAttendanceStatusColor = (status) => {
  const map = {
    NORMAL: 'green',
    LATE: 'orange',
    EARLY: 'orange',
    LATE_AND_EARLY: 'orange',
    EARLY_LEAVE: 'orange',
    ABSENT: 'red',
    LEAVE: 'blue'
  }
  return map[status] || 'default'
}

const loadStatistics = async () => {
  try {
    const [statsRes, genderRes, deptStatsRes, salaryRes, trendRes] = await Promise.all([
      employeeApi.getStatistics(),
      employeeApi.getGenderStatistics(),
      employeeApi.getDepartmentStatistics(),
      employeeApi.getSalaryStatistics(),
      employeeApi.getHireTrendStatistics()
    ])

    statistics.value = statsRes.data
    genderStats.value = genderRes.data
    departmentStats.value = deptStatsRes.data
    salaryStats.value = salaryRes.data
    hireTrendStats.value = trendRes.data
  } catch (error) {
    console.error(error)
  }
}

const loadManagerData = async () => {
  if (!currentUser.value?.userId) {
    managerDepartments.value = []
    managerEmployees.value = []
    managerAttendance.value = []
    return
  }

  loading.value = true
  try {
    const [deptRes, managerEmpRes] = await Promise.all([
      departmentApi.getAll(),
      employeeApi.getByUserId(currentUser.value.userId)
    ])

    departments.value = deptRes.data || []
    const managerEmp = managerEmpRes.data

    if (!managerEmp || !managerEmp.id) {
      managerDepartments.value = []
      managerEmployees.value = []
      managerAttendance.value = []
      return
    }

    const assignmentsRes = await departmentManagerApi.getByManager(managerEmp.id)
    managerDepartments.value = assignmentsRes.data || []

    if (!managerDepartments.value.length) {
      managerEmployees.value = []
      managerAttendance.value = []
      return
    }

    const start = dayjs().subtract(7, 'day').format('YYYY-MM-DD')
    const end = dayjs().format('YYYY-MM-DD')

    const employeeMap = new Map()
    const attendanceRecords = []

    const employeePromises = managerDepartments.value.map(dept =>
      employeeApi.search({ deptId: dept.deptId, page: 0, size: 100 })
    )

    const attendancePromises = managerDepartments.value.map(dept =>
      attendanceApi.getDepartmentRecords(dept.deptId, start, end)
    )

    const employeeResults = await Promise.allSettled(employeePromises)
    employeeResults.forEach(result => {
      if (result.status === 'fulfilled' && result.value.data?.content) {
        result.value.data.content.forEach(emp => {
          employeeMap.set(emp.id, emp)
        })
      }
    })

    const attendanceResults = await Promise.allSettled(attendancePromises)
    attendanceResults.forEach((result, index) => {
      if (result.status === 'fulfilled' && Array.isArray(result.value.data)) {
        const deptName = managerDepartments.value[index]?.deptName
        result.value.data.forEach(item => {
          attendanceRecords.push({
            ...item,
            deptName: item.deptName || deptName
          })
        })
      }
    })

    managerEmployees.value = Array.from(employeeMap.values())
    managerAttendance.value = attendanceRecords
      .sort((a, b) => dayjs(b.attendanceDate).diff(dayjs(a.attendanceDate)))
      .slice(0, 20)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const userData = localStorage.getItem('user')
  if (userData) {
    currentUser.value = JSON.parse(userData)
  }

  if (isAdmin.value) {
    loading.value = true
    try {
      await loadStatistics()
      const deptRes = await departmentApi.getAll()
      departments.value = deptRes.data
      const empRes = await employeeApi.getAll()
      recentEmployees.value = empRes.data.slice(0, 5)
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  } else if (isManager.value) {
    await loadManagerData()
  }
})
</script>
