import http from './http'

export const authApi = {
  login: (data) => http.post('/api/auth/login', data),
  me: () => http.get('/api/auth/me')
}

export const basicApi = {
  categories: () => http.get('/api/basic/categories'),
  createCategory: (data) => http.post('/api/basic/categories', data),
  updateCategory: (id, data) => http.put(`/api/basic/categories/${id}`, data),
  deleteCategory: (id) => http.delete(`/api/basic/categories/${id}`),
  fruits: (params) => http.get('/api/basic/fruits', { params }),
  createFruit: (data) => http.post('/api/basic/fruits', data),
  updateFruit: (id, data) => http.put(`/api/basic/fruits/${id}`, data),
  deleteFruit: (id) => http.delete(`/api/basic/fruits/${id}`),
  suppliers: (params) => http.get('/api/basic/suppliers', { params }),
  createSupplier: (data) => http.post('/api/basic/suppliers', data),
  updateSupplier: (id, data) => http.put(`/api/basic/suppliers/${id}`, data),
  deleteSupplier: (id) => http.delete(`/api/basic/suppliers/${id}`),
  customers: (params) => http.get('/api/basic/customers', { params }),
  createCustomer: (data) => http.post('/api/basic/customers', data),
  updateCustomer: (id, data) => http.put(`/api/basic/customers/${id}`, data),
  deleteCustomer: (id) => http.delete(`/api/basic/customers/${id}`),
  warehouses: () => http.get('/api/basic/warehouses'),
  createWarehouse: (data) => http.post('/api/basic/warehouses', data),
  updateWarehouse: (id, data) => http.put(`/api/basic/warehouses/${id}`, data),
  deleteWarehouse: (id) => http.delete(`/api/basic/warehouses/${id}`)
}

export const systemApi = {
  users: (params) => http.get('/api/system/users', { params }),
  createUser: (data) => http.post('/api/system/users', data),
  updateUser: (id, data) => http.put(`/api/system/users/${id}`, data),
  deleteUser: (id) => http.delete(`/api/system/users/${id}`)
}

export const inventoryApi = {
  batches: (params) => http.get('/api/inventory/batches', { params }),
  createBatch: (data) => http.post('/api/inventory/batches', data),
  updateBatch: (id, data) => http.put(`/api/inventory/batches/${id}`, data),
  deleteBatch: (id) => http.delete(`/api/inventory/batches/${id}`),
  nearExpire: (warningDays) => http.get('/api/inventory/batches/near-expire', { params: { warningDays } }),
  lowSafety: () => http.get('/api/inventory/batches/low-safety')
}

export const purchaseApi = {
  orders: (params) => http.get('/api/purchase/orders', { params }),
  items: (id) => http.get(`/api/purchase/orders/${id}/items`),
  createOrder: (data) => http.post('/api/purchase/orders', data),
  submit: (id) => http.post(`/api/purchase/orders/${id}/submit`),
  approve: (id) => http.post(`/api/purchase/orders/${id}/approve`),
  receive: (id, data) => http.post(`/api/purchase/orders/${id}/receive`, data)
}

export const salesApi = {
  orders: (params) => http.get('/api/sales/orders', { params }),
  items: (id) => http.get(`/api/sales/orders/${id}/items`),
  createOrder: (data) => http.post('/api/sales/orders', data),
  submit: (id) => http.post(`/api/sales/orders/${id}/submit`),
  approve: (id) => http.post(`/api/sales/orders/${id}/approve`),
  ship: (id, data) => http.post(`/api/sales/orders/${id}/ship`, data)
}

function parseSseEvent(block) {
  const lines = block.split('\n')
  let event = 'message'
  const dataLines = []
  for (const raw of lines) {
    const line = raw.trim()
    if (!line) continue
    if (line.startsWith('event:')) event = line.slice(6).trim()
    if (line.startsWith('data:')) dataLines.push(line.slice(5).trim())
  }
  return { event, data: dataLines.join('\n') }
}

export const aiApi = {
  chat: (data) => http.post('/api/ai/chat', data),
  intentQuery: (data) => http.post('/api/ai/intent/query', data),
  assistantDispatch: (data) => http.post('/api/ai/assistant/dispatch', data),
  assistantConfirm: (data) => http.post('/api/ai/assistant/confirm', data),
  assistantHistory: (params) => http.get('/api/ai/assistant/history', { params }),
  async streamChat(payload, { onChunk, onDone, onError, signal } = {}) {
    const token = localStorage.getItem('fruit-token')
    const response = await fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify(payload),
      signal
    })

    if (!response.ok) {
      const text = await response.text()
      throw new Error(text || `流式请求失败：${response.status}`)
    }

    if (!response.body) {
      throw new Error('流式响应体为空')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      let splitIndex = buffer.indexOf('\n\n')
      while (splitIndex >= 0) {
        const block = buffer.slice(0, splitIndex)
        buffer = buffer.slice(splitIndex + 2)
        const { event, data } = parseSseEvent(block)

        if (event === 'chunk' && data) onChunk?.(data)
        if (event === 'done') {
          onDone?.()
          return
        }
        if (event === 'error') {
          onError?.(data)
          throw new Error(data || '流式响应异常')
        }
        splitIndex = buffer.indexOf('\n\n')
      }
    }

    onDone?.()
  }
}

export const dashboardApi = {
  overview: () => http.get('/api/dashboard/overview'),
  amountTrend: (params) => http.get('/api/dashboard/amount-trend', { params }),
  categoryRatio: () => http.get('/api/dashboard/inventory/category-ratio'),
  salesTop: (params) => http.get('/api/dashboard/sales-top', { params }),
  warnings: (params) => http.get('/api/dashboard/warnings', { params }),
  runForecast: (data) => http.post('/api/dashboard/forecast/run', data),
  forecastTrend: (params) => http.get('/api/dashboard/forecast/trend', { params }),
  runOptimize: (data) => http.post('/api/dashboard/inventory/optimize/run', data),
  optimizeList: (params) => http.get('/api/dashboard/inventory/optimize/list', { params }),
  alertHeatmap: () => http.get('/api/dashboard/alert-heatmap')
}
