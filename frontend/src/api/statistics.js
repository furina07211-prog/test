import request from './index'

export function getDashboard() {
  return request({
    url: '/stats/dashboard',
    method: 'get'
  })
}

export function getInventoryTrend(params) {
  return request({
    url: '/stats/inventory-trend',
    method: 'get',
    params
  })
}

export function getInOutStats(params) {
  return request({
    url: '/stats/inout-stats',
    method: 'get',
    params
  })
}

export function getProductRanking(params) {
  return request({
    url: '/stats/product-ranking',
    method: 'get',
    params
  })
}
