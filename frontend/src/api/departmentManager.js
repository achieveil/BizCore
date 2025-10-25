import request from './request'

export const departmentManagerApi = {
  setManager(deptId, data) {
    return request.post(`/department-managers/departments/${deptId}/manager`, data)
  },

  getByManager(managerId) {
    return request.get(`/department-managers/managers/${managerId}/departments`)
  }
}
