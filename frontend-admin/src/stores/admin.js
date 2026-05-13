import { defineStore } from 'pinia'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    adminInfo: JSON.parse(localStorage.getItem('admin_info') || 'null'),
  }),
  actions: {
    setLogin(data) {
      this.token = data.token
      this.adminInfo = data
      localStorage.setItem('admin_token', data.token)
      localStorage.setItem('admin_info', JSON.stringify(data))
    },
    logout() {
      this.token = ''
      this.adminInfo = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
    },
  },
})
