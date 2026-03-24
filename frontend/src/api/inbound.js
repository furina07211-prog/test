import request from './index'

export function getInboundList(params) {
  return request({
    url: '/inbound-orders',
    method: 'get',
    params
  })
}

export function getInboundOrder(id) {
  return request({
    url: `/inbound-orders/${id}`,
    method: 'get'
  })
}

export function createInboundOrder(data) {
  return request({
    url: '/inbound-orders',
    method: 'post',
    data
  })
}

export function submitInboundOrder(id) {
  return request({
    url: `/inbound-orders/${id}/submit`,
    method: 'put'
  })
}

export function reviewInboundOrder(id, approved, remark) {
  return request({
    url: `/inbound-orders/${id}/review`,
    method: 'put',
    data: { approved, remark }
  })
}

export function receiveInboundOrder(id, items) {
  return request({
    url: `/inbound-orders/${id}/receive`,
    method: 'post',
    data: { orderId: id, items }
  })
}

export function completeInboundOrder(id) {
  return request({
    url: `/inbound-orders/${id}/complete`,
    method: 'put'
  })
}

export function cancelInboundOrder(id) {
  return request({
    url: `/inbound-orders/${id}/cancel`,
    method: 'put'
  })
}
