import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 120000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('user_token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

function handleUnauthorized() {
  localStorage.removeItem('user_token')
  localStorage.removeItem('user_info')
  // 避免重复跳转
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
