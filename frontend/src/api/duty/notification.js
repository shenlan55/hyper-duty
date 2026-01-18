import request from '../../utils/request'

export function getNotifications(receiverId) {
  return request({
    url: `/duty/notification/list/${receiverId}`,
    method: 'get'
  })
}

export function getUnreadNotifications(receiverId) {
  return request({
    url: `/duty/notification/unread/${receiverId}`,
    method: 'get'
  })
}

export function markAsRead(notificationId) {
  return request({
    url: `/duty/notification/read/${notificationId}`,
    method: 'put'
  })
}

export function markAllAsRead(receiverId) {
  return request({
    url: `/duty/notification/read-all/${receiverId}`,
    method: 'put'
  })
}

export function deleteNotification(notificationId) {
  return request({
    url: `/duty/notification/${notificationId}`,
    method: 'delete'
  })
}
