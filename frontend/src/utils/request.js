import axios from 'axios'

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
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加token到请求头
    const token = localStorage.getItem('token')
    if (token) {
      // 检查token是否过期
      try {
        if (!isJWTExpired(token)) {
          config.headers.Authorization = `Bearer ${token}`
        } else {
          // token过期，清除token
          localStorage.removeItem('token')
          localStorage.removeItem('username')
          localStorage.removeItem('employeeId')
        }
      } catch (error) {
        // token解析失败，清除token
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('employeeId')
      }
    }
    
    // 设置POST请求的默认Content-Type为application/json
    if (config.method === 'post' && !config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json'
    }
    
    return config
  },
  error => {
    // 处理请求错误
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 直接返回响应数据
    return response.data
  },
  error => {
    // 处理响应错误
    if (error.response) {
      // 服务器返回错误状态码
      switch (error.response.status) {
        case 401:
          // 未授权，跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('username')
          localStorage.removeItem('employeeId')
          window.location.href = '/login'
          break
        case 403:
          // 禁止访问
          break
        case 404:
          // 资源不存在
          break
        case 500:
          // 服务器内部错误
          break
        default:
          // 其他错误
          break
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
    } else {
      // 请求配置错误
    }
    return Promise.reject(error)
  }
)

export default request