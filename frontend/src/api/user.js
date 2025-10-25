import request from './request'

export const userApi = {
  login(data) {
    return request.post('/users/login', data)
  },

  getUserById(id) {
    return request.get(`/users/${id}`)
  },

  updatePassword(id, data) {
    return request.put(`/users/${id}/password`, data)
  },

  getSecurityQuestion(username) {
    return request.get(`/users/security-question/${username}`)
  },

  verifySecurityAnswer(data) {
    return request.post('/users/verify-security-answer', data)
  },

  verifyPhone(data) {
    return request.post('/users/verify-phone', data)
  },

  resetPassword(data) {
    return request.post('/users/reset-password', data)
  },

  // 管理员功能
  getAll() {
    return request.get('/users')
  },

  adminResetPassword(data) {
    return request.post('/users/admin-reset-password', data)
  }
}
