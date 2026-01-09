import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    username: localStorage.getItem('username') || '',
    token: localStorage.getItem('token') || ''
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    // 登录
    login(userInfo) {
      this.username = userInfo.username
      this.token = userInfo.token
      localStorage.setItem('username', userInfo.username)
      localStorage.setItem('token', userInfo.token)
    },
    // 登出
    logout() {
      this.username = ''
      this.token = ''
      localStorage.removeItem('username')
      localStorage.removeItem('token')
    }
  }
})