import request from './request'

export const employeeApi = {
  getAll() {
    return request.get('/employees')
  },

  getById(id) {
    return request.get(`/employees/${id}`)
  },

  getByUserId(userId) {
    return request.get(`/employees/by-user/${userId}`)
  },

  search(params) {
    return request.get('/employees/search', { params })
  },

  create(data) {
    return request.post('/employees', data)
  },

  update(id, data) {
    return request.put(`/employees/${id}`, data)
  },

  delete(id) {
    return request.delete(`/employees/${id}`)
  },

  registerAccount(id, data) {
    return request.post(`/employees/${id}/register-account`, data)
  },

  resetPassword(id, data) {
    return request.post(`/employees/${id}/reset-password`, data)
  },

  getStatistics() {
    return request.get('/employees/statistics')
  },

  getGenderStatistics() {
    return request.get('/employees/statistics/gender')
  },

  getDepartmentStatistics() {
    return request.get('/employees/statistics/department')
  },

  getSalaryStatistics() {
    return request.get('/employees/statistics/salary')
  },

  getHireTrendStatistics() {
    return request.get('/employees/statistics/hire-trend')
  }
}
