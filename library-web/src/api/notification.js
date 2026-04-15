import request from '@/utils/request'

export function getUnreadNotifications() {
  return request({
    url: '/notification/unread',
    method: 'get'
  })
}

export function getNotificationPage(params) {
  return request({
    url: '/notification/page',
    method: 'get',
    params
  })
}

export function getUnreadCount() {
  return request({
    url: '/notification/unreadCount',
    method: 'get'
  })
}

export function getNotificationStats() {
  return request({
    url: '/notification/stats',
    method: 'get'
  })
}

export function markAsRead(id) {
  return request({
    url: `/notification/${id}/read`,
    method: 'put'
  })
}

export function markAllAsRead() {
  return request({
    url: '/notification/readAll',
    method: 'put'
  })
}

export function deleteNotification(id) {
  return request({
    url: `/notification/${id}`,
    method: 'delete'
  })
}
