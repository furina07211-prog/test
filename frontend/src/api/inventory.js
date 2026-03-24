import request from './index'

export function getInventoryList(params) {
  return request({
    url: '/inventory',
    method: 'get',
    params
  })
}

export function getBatchList(params) {
  return request({
    url: '/inventory/batches',
    method: 'get',
    params
  })
}

export function getInventoryDetail(productId, warehouseId) {
  return request({
    url: `/inventory/${productId}/${warehouseId}`,
    method: 'get'
  })
}
