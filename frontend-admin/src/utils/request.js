import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({ baseURL: '/api', timeout: 300000 })

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) config.headers.Authorization = token
  return config
})

function handleUnauthorized() {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_info')
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code === 'A0401') {
      handleUnauthorized()
      return Promise.reject(result)
    }
    if (result.code !== '00000') {
      ElMessage.error(result.msg || '请求失败')
      return Promise.reject(result)
    }
    return result.data
  },
  (error) => {
    const code = error.response?.data?.code
    if (code === 'A0401') {
      handleUnauthorized()
      return Promise.reject(error)
    }
    ElMessage.error(error.response?.data?.msg || '网络请求失败')
    return Promise.reject(error)
  },
)

export default request
