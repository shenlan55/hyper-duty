import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'

// JWT解码工具函数
const decodeJWT = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))
    return JSON.parse(jsonPayload)
  } catch (error) {
    return null
  }
}

// 检查JWT是否过期
const isJWTExpired = (token) => {
  if (!token) return true
  
  const decoded = decodeJWT(token)
  if (!decoded || !decoded.exp) return true
  
  // 检查token是否过期（当前时间大于过期时间）
  const currentTime = Math.floor(Date.now() / 1000)
  return currentTime > decoded.exp
}

// 创建axios实例
const request = axios.create({
  baseURL: '/api', // 后端API基础URL，使用/api前缀，通过Vite代理转发
  timeout: 10000 // 请求超时时间
})

// 创建用于刷新令牌的axios实例（不使用拦截器，避免循环调用）
const refreshRequest = axios.create({
  baseURL: '/api',
  timeout: 5000
})

// 加载状态管理
let loadingInstance = null
let loadingCount = 0

// 显示加载状态
const showLoading = () => {
  if (loadingCount === 0) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: '加载中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }
  loadingCount++
}

// 隐藏加载状态
const hideLoading = () => {
  if (loadingCount > 0) {
    loadingCount--
    if (loadingCount === 0 && loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
  }
}

// 请求刷新令牌
const refreshToken = async () => {
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) {
    throw new Error('No refresh token available')
  }
  
  try {
    const response = await refreshRequest.post('/auth/refresh-token', {
      refreshToken: refreshToken,
      employeeId: localStorage.getItem('employeeId'),
      employeeName: localStorage.getItem('employeeName')
    })
    
    if (response.data.code === 200 && response.data.data) {
      const newAccessToken = response.data.data.accessToken
      localStorage.setItem('token', newAccessToken)
      return newAccessToken
    } else {
      throw new Error('Failed to refresh token')
    }
  } catch (error) {
    throw error
  }
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 显示加载状态（默认情况下所有请求都不会显示加载状态，只有在明确需要显示加载状态的请求中，通过设置 loading: true 来启用）
    if (config.loading === true) {
      showLoading()
    }
    
    // 添加token到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 设置POST和PUT请求的默认Content-Type为application/json
    if ((config.method === 'post' || config.method === 'put') && !config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json'
    }
    
    return config
  },
  error => {
    // 隐藏加载状态
    hideLoading()
    
    // 处理请求错误
    ElMessage.error('请求配置错误')
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 隐藏加载状态
    hideLoading()
    
    // 统一处理响应数据
    const data = response.data
    if (data.code === 200) {
      return data.data
    } else {
      // 后端返回错误
      ElMessage.error(data.message || '操作失败')
      return Promise.reject(new Error(data.message || '操作失败'))
    }
  },
  async error => {
    // 隐藏加载状态
    hideLoading()
    
    // 处理响应错误
    if (error.response) {
      // 服务器返回错误状态码
      switch (error.response.status) {
        case 401:
          // 未授权，尝试刷新令牌
          try {
            // 刷新令牌
            const newAccessToken = await refreshToken()
            
            // 使用新令牌重新发送请求
            const originalRequest = error.config
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
            return request(originalRequest)
          } catch (refreshError) {
            // 刷新令牌失败，跳转到登录页
            localStorage.removeItem('token')
            localStorage.removeItem('refreshToken')
            localStorage.removeItem('username')
            localStorage.removeItem('employeeId')
            localStorage.removeItem('employeeName')
            ElMessage.error('登录已过期，请重新登录')
            window.location.href = '/login'
            return Promise.reject(refreshError)
          }
        case 403:
          // 禁止访问
          ElMessage.error('权限不足，无法访问')
          break
        case 404:
          // 资源不存在
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          // 服务器内部错误
          ElMessage.error('服务器内部错误，请稍后重试')
          break
        default:
          // 其他错误
          ElMessage.error(`请求失败：${error.response.status}`)
          break
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      // 请求配置错误
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

// 导出request实例和工具函数
export default request
export { decodeJWT, isJWTExpired }