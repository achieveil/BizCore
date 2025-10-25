import request from './request'

export const attendanceRuleApi = {
  // 创建或更新部门考勤规则
  save(data, operatorId) {
    return request.post('/attendance-rules', data, {
      params: { operatorId }
    })
  },

  // 获取部门考勤规则
  getByDepartment(deptId) {
    return request.get(`/attendance-rules/department/${deptId}`)
  },

  // 获取所有部门考勤规则
  getAll() {
    return request.get('/attendance-rules')
  },

  // 删除部门考勤规则
  delete(deptId) {
    return request.delete(`/attendance-rules/department/${deptId}`)
  }
}
