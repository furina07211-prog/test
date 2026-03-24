import request from './index'

export function getOutboundList(params) {
  return request({
    url: '/outbound-orders',
    method: 'get',
    params
  })
}

export function getOutboundOrder(id) {
  return request({
    url: `/outbound-orders/${id}`,
    method: 'get'
  })
}

export function createOutboundOrder(data) {
  return request({
    url: '/outbound-orders',
    method: 'post',
    data
  })
}

export function submitOutboundOrder(id) {
  return request({
    url: `/outbound-orders/${id}/submit`,
    method: 'put'
  })
}

export function reviewOutboundOrder(id, approved, remark) {
  return request({
    url: `/outbound-orders/${id}/review`,
    method: 'put',
    data: { approved, remark }
  })
}

export function pickOutboundOrder(id) {
  return request({
    url: `/outbound-orders/${id}/pick`,
    method: 'post'
  })
}

export function completeOutboundOrder(id) {
  return request({
    url: `/outbound-orders/${id}/complete`,
    method: 'put'
  })
}

export function cancelOutboundOrder(id) {
  return request({
    url: `/outbound-orders/${id}/cancel`,
    method: 'put'
  })
}
