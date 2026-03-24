import request from './index'

export function getRoleList(params) {
  return request({
    url: '/sys/roles',
    method: 'get',
    params
  })
}

export function getAllRoles() {
  return request({
    url: '/sys/roles/all',
    method: 'get'
  })
}

export function getRole(id) {
  return request({
    url: `/sys/roles/${id}`,
    method: 'get'
  })
}

export function createRole(data) {
  return request({
    url: '/sys/roles',
    method: 'post',
    data
  })
}

export function updateRole(id, data) {
  return request({
    url: `/sys/roles/${id}`,
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: `/sys/roles/${id}`,
    method: 'delete'
  })
}

export function getRolePermissions(id) {
  return request({
    url: `/sys/roles/${id}/permissions`,
    method: 'get'
  })
}

export function assignPermissions(id, permissionIds) {
  return request({
    url: `/sys/roles/${id}/permissions`,
    method: 'put',
    data: { permissionIds }
  })
}

export function getAllPermissions() {
  return request({
    url: '/sys/roles/permissions',
    method: 'get'
  })
}
