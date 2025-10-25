import request from './request'

export const departmentApi = {
  getAll() {
    return request.get('/departments')
  },

  getActive() {
    return request.get('/departments/active')
  },

  getById(id) {
    return request.get(`/departments/${id}`)
  },

  create(data) {
    return request.post('/departments', data)
  },

  update(id, data) {
    return request.put(`/departments/${id}`, data)
  },

  delete(id) {
    return request.delete(`/departments/${id}`)
  }
}
