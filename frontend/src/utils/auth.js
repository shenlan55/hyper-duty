// 认证相关工具函数

/**
 * 获取当前用户信息
 * @returns {Object|null} 用户信息对象
 */
export function getUserInfo() {
  try {
    // 这里应该从localStorage或其他存储中获取用户信息
    // 暂时返回一个模拟的用户信息对象
    return {
      employeeId: 1,
      roles: ['值班长']
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}
