import request from './request'

export const overtimeApi = {
  // 创建加班申请
  create(data) {
    return request.post('/overtime-requests', data)
  },

  // 获取员工的加班记录
  getByEmployee(empId) {
    return request.get(`/overtime-requests/employee/${empId}`)
  },

  // 获取待审批的加班申请（部门经理）
  getPending(managerId) {
    return request.get(`/overtime-requests/pending/${managerId}`)
  },

  // 获取所有加班申请（管理员）
  getAll() {
    return request.get('/overtime-requests/all')
  },

  // 获取部门员工的加班记录（经理）
  getByDepartment(deptId) {
    return request.get(`/overtime-requests/department/${deptId}`)
  },

  // 审批加班申请
  approve(data) {
    return request.post('/overtime-requests/approve', data)
  },

  // 撤销加班申请
  cancel(requestId, empId) {
    return request.delete(`/overtime-requests/${requestId}/employee/${empId}`)
  }
}
