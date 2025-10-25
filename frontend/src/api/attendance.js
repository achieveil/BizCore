import request from './request'

export const attendanceApi = {
  // 签到
  checkIn(data) {
    return request.post('/attendances/check-in', data)
  },

  // 签退
  checkOut(data) {
    return request.post('/attendances/check-out', data)
  },

  // 获取员工考勤记录
  getEmployeeRecords(empId, startDate, endDate) {
    return request.get(`/attendances/employee/${empId}/records`, {
      params: { startDate, endDate }
    })
  },

  // 获取员工月度统计
  getMonthlyStats(empId, year, month) {
    return request.get(`/attendances/employee/${empId}/stats`, {
      params: { year, month }
    })
  },

  // 获取部门考勤记录（经理查看）
  getDepartmentRecords(deptId, startDate, endDate) {
    return request.get(`/attendances/department/${deptId}/records`, {
      params: { startDate, endDate }
    })
  },

  getAll(params) {
    return request.get('/attendances', { params })
  },

  getByEmpId(empId) {
    return request.get(`/attendances/employee/${empId}`)
  },

  getByDate(date) {
    return request.get(`/attendances/date/${date}`)
  },

  getByRange(empId, startDate, endDate) {
    return request.get(`/attendances/employee/${empId}/range`, {
      params: { startDate, endDate }
    })
  },

  create(data) {
    return request.post('/attendances', data)
  },

  update(id, data) {
    return request.put(`/attendances/${id}`, data)
  },

  delete(id) {
    return request.delete(`/attendances/${id}`)
  }
}
