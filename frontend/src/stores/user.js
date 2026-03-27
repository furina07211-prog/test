import { defineStore } from 'pinia'
import request from '@/utils/request'
import router from '@/router'

const TOKEN_KEY = 'fruit-token'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    username: localStorage.getItem('fruit-username') || '',
    roleCode: localStorage.getItem('fruit-role') || ''
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    async login(payload) {
      const res = await request.post('/auth/login', payload)
      const data = res?.code === 200 ? res.data : res
      if (!data?.token) {
        throw new Error(res?.message || '登录失败')
      }
      this.setSession(data)
      return data
    },
    setSession(data) {
      const user = data?.user || data?.userInfo || {}
      const role = data?.roleCode || data?.roles?.[0] || user?.roleCode || ''

      this.token = data?.token || this.token || ''
      this.username = data?.username || user?.username || user?.realName || ''
      this.roleCode = role

      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem('fruit-username', this.username)
      localStorage.setItem('fruit-role', this.roleCode)
      localStorage.setItem('fruit-user', JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.username = ''
      this.roleCode = ''
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('fruit-username')
      localStorage.removeItem('fruit-role')
      localStorage.removeItem('fruit-user')
      router.push('/login')
    }
  }
})
