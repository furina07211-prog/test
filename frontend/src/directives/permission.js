import store from '@/store'

function checkPermission(el, binding) {
  const { value } = binding
  const permissions = store.getters && store.getters.permissions
  const roles = store.getters && store.getters.roles

  if (value && value instanceof Array) {
    if (value.length > 0) {
      const permissionCodes = value
      
      // Admin has all permissions
      const hasAdminRole = roles && roles.some(role => role.roleCode === 'ADMIN')
      if (hasAdminRole) {
        return true
      }

      const hasPermission = permissions && permissions.some(permission => {
        return permissionCodes.includes(permission)
      })

      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  } else {
    throw new Error('need permissions! Like v-permission="[\'user:add\']"')
  }
}

export default {
  inserted(el, binding) {
    checkPermission(el, binding)
  },
  update(el, binding) {
    checkPermission(el, binding)
  }
}
