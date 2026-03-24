import Cookies from 'js-cookie'

const state = {
  sidebarOpened: Cookies.get('sidebarStatus') !== '0',
  device: 'desktop'
}

const mutations = {
  TOGGLE_SIDEBAR(state) {
    state.sidebarOpened = !state.sidebarOpened
    Cookies.set('sidebarStatus', state.sidebarOpened ? '1' : '0')
  },
  CLOSE_SIDEBAR(state) {
    state.sidebarOpened = false
    Cookies.set('sidebarStatus', '0')
  },
  SET_DEVICE(state, device) {
    state.device = device
  }
}

const actions = {
  toggleSidebar({ commit }) {
    commit('TOGGLE_SIDEBAR')
  },
  closeSidebar({ commit }) {
    commit('CLOSE_SIDEBAR')
  },
  setDevice({ commit }, device) {
    commit('SET_DEVICE', device)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
