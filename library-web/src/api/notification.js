import request from '@/utils/request'

// 获取通知列表
export function getNotificationPage(params) {
  return request({
    url: '/notification/page',
    method: 'get',
    params
  })
}

// 获取未读通知列表
export function getUnreadNotifications() {
  return request({
    url: '/notification/unread',
    method: 'get'
  })
}

// 获取未读归还提醒列表
export function getUnreadReturnReminders() {
  return request({
    url: '/notification/unread/return-reminders',
    method: 'get'
  })
}

// 获取未读通知数量
export function countUnread() {
  return request({
    url: '/notification/count/unread',
    method: 'get'
  })
}

// 标记通知已读
export function markAsRead(id) {
  return request({
    url: `/notification/read/${id}`,
    method: 'put'
  })
}

// 标记所有通知已读
export function markAllAsRead() {
  return request({
    url: '/notification/read/all',
    method: 'put'
  })
}

// 删除通知
export function deleteNotification(id) {
  return request({
    url: `/notification/${id}`,
    method: 'delete'
  })
}
