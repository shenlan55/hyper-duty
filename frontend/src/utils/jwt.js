/**
 * JWT解码工具函数
 * @param {string} token - JWT令牌
 * @returns {Object|null} 解码后的JWT载荷
 */
export const decodeJWT = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('JWT解码失败:', error)
    return null
  }
}

/**
 * 检查JWT是否过期
 * @param {string} token - JWT令牌
 * @returns {boolean} 是否过期
 */
export const isJWTExpired = (token) => {
  if (!token) return true
  
  const decoded = decodeJWT(token)
  if (!decoded || !decoded.exp) return true
  
  const currentTime = Math.floor(Date.now() / 1000)
  return currentTime > decoded.exp
}

/**
 * 获取当前用户ID
 * @returns {number|null} 用户ID
 */
export const getCurrentUserId = () => {
  const token = localStorage.getItem('token')
  if (!token) return null
  
  const decoded = decodeJWT(token)
  return decoded?.employeeId || decoded?.userId || decoded?.sub
}
