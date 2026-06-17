import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refresh_token'

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(REFRESH_TOKEN_KEY)
      localStorage.removeItem('user')
      window.location.href = '/login'
      return Promise.reject(error)
    }

    const message =
      error.response?.data?.message ?? error.message ?? 'Request failed'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return instance.get<unknown, T>(url, config)
}

export function post<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<T> {
  return instance.post<unknown, T>(url, data, config)
}

export function put<T>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<T> {
  return instance.put<unknown, T>(url, data, config)
}

export function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return instance.delete<unknown, T>(url, config)
}
