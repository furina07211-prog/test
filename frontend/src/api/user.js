import request from './index'

export function getUserList(params) {
  return request({
    url: '/sys/users',
    method: 'get',
    params
  })
}

export function getUser(id) {
  return request({
    url: `/sys/users/${id}`,
    method: 'get'
  })
}

export function createUser(data) {
  return request({
    url: '/sys/users',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: `/sys/users/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/sys/users/${id}`,
    method: 'delete'
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/sys/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function resetPassword(id, password = '123456') {
  return request({
    url: `/sys/users/${id}/reset-password`,
    method: 'put',
    data: { password }
  })
}
