import { login, logout, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

const state = {
  token: getToken(),
  userInfo: null,
  roles: [],
  permissions: []
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
  },
  SET_ROLES(state, roles) {
    state.roles = roles
  },
  SET_PERMISSIONS(state, permissions) {
    state.permissions = permissions
  }
}

const actions = {
  async login({ commit }, loginForm) {
    const data = await login(loginForm)
    commit('SET_TOKEN', data.token)
    setToken(data.token)
    commit('SET_USER_INFO', data.userInfo)
    commit('SET_ROLES', data.userInfo.roles.map(r => r.roleCode))
    commit('SET_PERMISSIONS', data.permissions)
    return data
  },

  async getUserInfo({ commit, state }) {
    const data = await getUserInfo()
    commit('SET_USER_INFO', data)
    const roles = data.roles.map(r => r.roleCode)
    commit('SET_ROLES', roles)
    return { roles }
  },

  async logout({ commit }) {
    try {
      await logout()
    } catch (e) {
      // Ignore logout error
    }
    commit('SET_TOKEN', '')
    commit('SET_USER_INFO', null)
    commit('SET_ROLES', [])
    commit('SET_PERMISSIONS', [])
    removeToken()
  },

  resetToken({ commit }) {
    commit('SET_TOKEN', '')
    commit('SET_USER_INFO', null)
    commit('SET_ROLES', [])
    commit('SET_PERMISSIONS', [])
    removeToken()
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
