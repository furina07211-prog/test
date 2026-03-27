import { defineStore } from 'pinia'
import { authApi } from '@/api/modules'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('fruit-token') || '',
    user: JSON.parse(localStorage.getItem('fruit-user') || 'null'),
    roles: JSON.parse(localStorage.getItem('fruit-roles') || '[]'),
    menus: JSON.parse(localStorage.getItem('fruit-menus') || '[]')
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    displayName: (state) => state.user?.realName || state.user?.username || '演示用户'
  },
  actions: {
    async login(payload) {
      const data = await authApi.login(payload)
      this.setSession(data)
      return data
    },
    async restore() {
      if (!this.token) return null
      try {
        const data = await authApi.me()
        this.setSession({ ...data, token: this.token })
        return data
      } catch (error) {
        this.logout()
        return null
      }
    },
    setSession(data) {
      this.token = data.token || this.token
      this.user = data.userInfo
      this.roles = data.roleCodes || []
      this.menus = data.menus || []
      localStorage.setItem('fruit-token', this.token)
      localStorage.setItem('fruit-user', JSON.stringify(this.user))
      localStorage.setItem('fruit-roles', JSON.stringify(this.roles))
      localStorage.setItem('fruit-menus', JSON.stringify(this.menus))
    },
    logout() {
      this.token = ''
      this.user = null
      this.roles = []
      this.menus = []
      localStorage.removeItem('fruit-token')
      localStorage.removeItem('fruit-user')
      localStorage.removeItem('fruit-roles')
      localStorage.removeItem('fruit-menus')
    }
  }
})
