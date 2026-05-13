import { defineStore } from 'pinia'
import { getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('user_token') || '',
    userInfo: JSON.parse(localStorage.getItem('user_info') || 'null'),
  }),
  actions: {
    setLogin(data) {
      this.token = data.token
      this.userInfo = data
      localStorage.setItem('user_token', data.token)
      localStorage.setItem('user_info', JSON.stringify(data))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_info')
    },
    async refreshBalance() {
      if (!this.token) return
      try {
        const info = await getUserInfo()
        this.userInfo = { ...this.userInfo, ...info }
        localStorage.setItem('user_info', JSON.stringify(this.userInfo))
      } catch { /* token expired, ignore */ }
    },
  },
})
