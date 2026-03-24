import request from './index'

export function getAlertList(params) {
  return request({
    url: '/alerts',
    method: 'get',
    params
  })
}

export function getAlertCount() {
  return request({
    url: '/alerts/count',
    method: 'get'
  })
}

export function markAlertRead(id) {
  return request({
    url: `/alerts/${id}/read`,
    method: 'put'
  })
}

export function handleAlert(id, remark) {
  return request({
    url: `/alerts/${id}/handle`,
    method: 'put',
    data: { remark }
  })
}
