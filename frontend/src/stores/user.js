import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    username: localStorage.getItem('username') || '',
    token: localStorage.getItem('token') || '',
    employeeId: localStorage.getItem('employeeId') ? parseInt(localStorage.getItem('employeeId')) : null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    login(userInfo) {
      this.username = userInfo.username
      this.token = userInfo.token
      this.employeeId = userInfo.employeeId
      localStorage.setItem('username', userInfo.username)
      localStorage.setItem('token', userInfo.token)
      localStorage.setItem('employeeId', userInfo.employeeId)
    },
    logout() {
      this.username = ''
      this.token = ''
      this.employeeId = null
      localStorage.removeItem('username')
      localStorage.removeItem('token')
      localStorage.removeItem('employeeId')
    }
  }
})