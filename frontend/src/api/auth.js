import request from './request'

export const authApi = {
  login(data) {
    return request.post('/auth/login', data)
  },

  refresh(data) {
    return request.post('/auth/refresh', data)
  },

  logout(data) {
    return request.post('/auth/logout', data)
  }
}
