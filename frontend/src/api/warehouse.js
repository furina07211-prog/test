import request from './index'

export function getWarehouseList(params) {
  return request({
    url: '/warehouses',
    method: 'get',
    params
  })
}

export function getAllWarehouses() {
  return request({
    url: '/warehouses/all',
    method: 'get'
  })
}

export function getWarehouse(id) {
  return request({
    url: `/warehouses/${id}`,
    method: 'get'
  })
}

export function createWarehouse(data) {
  return request({
    url: '/warehouses',
    method: 'post',
    data
  })
}

export function updateWarehouse(id, data) {
  return request({
    url: `/warehouses/${id}`,
    method: 'put',
    data
  })
}

export function deleteWarehouse(id) {
  return request({
    url: `/warehouses/${id}`,
    method: 'delete'
  })
}
