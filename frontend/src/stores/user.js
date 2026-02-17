import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    username: localStorage.getItem('username') || '',
    token: localStorage.getItem('token') || '',
    employeeId: localStorage.getItem('employeeId') ? parseInt(localStorage.getItem('employeeId')) : null,
    employeeName: localStorage.getItem('employeeName') || ''
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    login(userInfo) {
      this.username = userInfo.username
      this.token = userInfo.token
      this.employeeId = userInfo.employeeId
      this.employeeName = userInfo.employeeName || ''
      localStorage.setItem('username', userInfo.username)
      localStorage.setItem('token', userInfo.token)
      localStorage.setItem('employeeId', userInfo.employeeId)
      localStorage.setItem('employeeName', userInfo.employeeName || '')
    },
    logout() {
      this.username = ''
      this.token = ''
      this.employeeId = null
      this.employeeName = ''
      localStorage.removeItem('username')
      localStorage.removeItem('token')
      localStorage.removeItem('employeeId')
      localStorage.removeItem('employeeName')
    }
  }
})