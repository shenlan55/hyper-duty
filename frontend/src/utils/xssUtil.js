/**
 * XSS防护工具函数
 */

/**
 * 转义HTML特殊字符
 * @param {string} str - 需要转义的字符串
 * @returns {string} - 转义后的字符串
 */
export const escapeHtml = (str) => {
  if (!str || typeof str !== 'string') return str
  
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

/**
 * 过滤HTML标签
 * @param {string} str - 需要过滤的字符串
 * @returns {string} - 过滤后的字符串
 */
export const filterHtml = (str) => {
  if (!str || typeof str !== 'string') return str
  
  return str.replace(/<[^>]*>/g, '')
}

/**
 * 安全处理用户输入
 * @param {string} str - 用户输入
 * @param {object} options - 配置选项
 * @param {boolean} options.escape - 是否转义
 * @param {boolean} options.filter - 是否过滤标签
 * @returns {string} - 处理后的字符串
 */
export const safeInput = (str, options = {}) => {
  const { escape = true, filter = false } = options
  
  let result = str
  
  if (filter) {
    result = filterHtml(result)
  }
  
  if (escape) {
    result = escapeHtml(result)
  }
  
  return result
}

/**
 * 验证输入是否包含XSS攻击代码
 * @param {string} str - 需要验证的字符串
 * @returns {boolean} - 是否包含XSS攻击代码
 */
export const hasXss = (str) => {
  if (!str || typeof str !== 'string') return false
  
  const xssPatterns = [
    /<script[^>]*>.*?<\/script>/gi,
    /<iframe[^>]*>.*?<\/iframe>/gi,
    /<object[^>]*>.*?<\/object>/gi,
    /<embed[^>]*>.*?<\/embed>/gi,
    /<link[^>]*>.*?<\/link>/gi,
    /<style[^>]*>.*?<\/style>/gi,
    /javascript:/gi,
    /on\w+\s*=/gi,
    /expression\s*\(/gi
  ]
  
  return xssPatterns.some(pattern => pattern.test(str))
}

/**
 * 清理XSS攻击代码
 * @param {string} str - 需要清理的字符串
 * @returns {string} - 清理后的字符串
 */
export const cleanXss = (str) => {
  if (!str || typeof str !== 'string') return str
  
  let result = str
  
  // 移除script标签
  result = result.replace(/<script[^>]*>.*?<\/script>/gi, '')
  
  // 移除iframe标签
  result = result.replace(/<iframe[^>]*>.*?<\/iframe>/gi, '')
  
  // 移除object标签
  result = result.replace(/<object[^>]*>.*?<\/object>/gi, '')
  
  // 移除embed标签
  result = result.replace(/<embed[^>]*>.*?<\/embed>/gi, '')
  
  // 移除link标签
  result = result.replace(/<link[^>]*>.*?<\/link>/gi, '')
  
  // 移除style标签
  result = result.replace(/<style[^>]*>.*?<\/style>/gi, '')
  
  // 移除javascript:伪协议
  result = result.replace(/javascript:/gi, '')
  
  // 移除事件属性
  result = result.replace(/on\w+\s*=/gi, '')
  
  // 移除expression
  result = result.replace(/expression\s*\(/gi, '')
  
  return result
}
