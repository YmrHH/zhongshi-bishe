import axios from 'axios'
import type { ApiResult } from '@/types/api'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    if (res.config.responseType === 'blob' || res.data instanceof Blob) {
      if (res.status >= 400) {
        return Promise.reject(new Error('文件请求失败'))
      }
      return res
    }
    const body = res.data as ApiResult<unknown>
    if (body.code === 401) {
      localStorage.removeItem('token')
      useUserStore().clear()
      router.push('/login')
      return Promise.reject(new Error(body.message || '登录已失效'))
    }
    if (body.code !== 200) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return res
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      useUserStore().clear()
      router.push('/login')
    }
    return Promise.reject(err)
  }
)

export default http
