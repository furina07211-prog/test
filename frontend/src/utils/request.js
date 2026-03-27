import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

const TOKEN_KEY = 'fruit-token'

service.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('fruit-user')
      localStorage.removeItem('fruit-role')
      const { default: router } = await import('@/router')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default service
