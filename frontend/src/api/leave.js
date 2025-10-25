import request from './request'

export const leaveApi = {
  // 创建请假申请
  create(data) {
    return request.post('/leave-requests', data)
  },

  // 获取员工的请假记录
  getByEmployee(empId) {
    return request.get(`/leave-requests/employee/${empId}`)
  },

  // 获取待审批的请假申请（部门经理）
  getPending(managerId) {
    return request.get(`/leave-requests/pending/${managerId}`)
  },

  // 获取所有请假申请（管理员）
  getAll() {
    return request.get('/leave-requests/all')
  },

  // 获取部门员工的请假记录（经理）
  getByDepartment(deptId) {
    return request.get(`/leave-requests/department/${deptId}`)
  },

  // 审批请假申请
  approve(data) {
    return request.post('/leave-requests/approve', data)
  },

  // 撤销请假申请
  cancel(requestId, empId) {
    return request.delete(`/leave-requests/${requestId}/employee/${empId}`)
  }
}
