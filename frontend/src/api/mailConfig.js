import request from '../utils/request'

// 获取当前邮件配置
export function getCurrentMailConfig() {
  return request({
    url: '/mail-config/current',
    method: 'get'
  })
}

// 保存或更新邮件配置
export function saveMailConfig(data) {
  return request({
    url: '/mail-config/save',
    method: 'post',
    data
  })
}

// 发送登录验证码
export function sendVerificationCode(data) {
  return request({
    url: '/mail-config/send-code',
    method: 'post',
    data
  })
}

// 测试邮件连接
export function testMailConnection(data) {
  return request({
    url: '/mail-config/test-connection',
    method: 'post',
    data
  })
}
