import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    username: localStorage.getItem('username') || '',
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    employeeId: localStorage.getItem('employeeId') ? parseInt(localStorage.getItem('employeeId')) : null,
    employeeName: localStorage.getItem('employeeName') || ''
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    login(userInfo) {
      this.username = userInfo.username
      this.token = userInfo.accessToken
      this.refreshToken = userInfo.refreshToken
      this.employeeId = userInfo.employeeId
      this.employeeName = userInfo.employeeName || ''
      localStorage.setItem('username', userInfo.username)
      localStorage.setItem('token', userInfo.accessToken)
      localStorage.setItem('refreshToken', userInfo.refreshToken)
      localStorage.setItem('employeeId', userInfo.employeeId)
      localStorage.setItem('employeeName', userInfo.employeeName || '')
    },
    logout() {
      this.username = ''
      this.token = ''
      this.refreshToken = ''
      this.employeeId = null
      this.employeeName = ''
      localStorage.removeItem('username')
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('employeeId')
      localStorage.removeItem('employeeName')
    }
  }
})