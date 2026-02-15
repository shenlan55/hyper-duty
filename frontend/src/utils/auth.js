// 认证相关工具函数

/**
 * JWT解码工具函数
 * @param {string} token - JWT令牌
 * @returns {Object|null} 解码后的JWT载荷
 */
const decodeJWT = (token) => {
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
 * 获取当前用户信息
 * @returns {Object|null} 用户信息对象
 */
export function getUserInfo() {
  try {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    const username = localStorage.getItem('username')
    const employeeId = localStorage.getItem('employeeId')
    
    if (token) {
      // 解码JWT令牌
      const decoded = decodeJWT(token)
      if (decoded) {
        return {
          employeeId: employeeId ? parseInt(employeeId) : decoded.employeeId,
          employeeName: decoded.name || '当前用户',
          username: username,
          roles: ['值班长'] // 这里可以根据实际情况从后端获取角色信息
        }
      }
    }
    
    // 如果没有token或解码失败，返回默认值
    return {
      employeeId: employeeId ? parseInt(employeeId) : 1,
      employeeName: '当前用户',
      username: username,
      roles: ['值班长']
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}
