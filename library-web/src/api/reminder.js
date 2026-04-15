import request from '@/utils/request'

// 获取提醒列表
export function getReminderPage(params) {
  return request({
    url: '/reminder/page',
    method: 'get',
    params
  })
}

// 获取今日提醒列表
export function getTodayReminders() {
  return request({
    url: '/reminder/today',
    method: 'get'
  })
}

// 获取活跃提醒列表
export function getActiveReminders() {
  return request({
    url: '/reminder/active',
    method: 'get'
  })
}

// 获取订单最新提醒
export function getLatestByOrderId(orderId) {
  return request({
    url: `/reminder/order/${orderId}`,
    method: 'get'
  })
}

// 创建提醒
export function createReminder(orderId) {
  return request({
    url: `/reminder/create/${orderId}`,
    method: 'post'
  })
}

// 处理提醒
export function processReminder(id) {
  return request({
    url: `/reminder/process/${id}`,
    method: 'put'
  })
}

// 取消提醒
export function cancelReminder(orderId) {
  return request({
    url: `/reminder/cancel/${orderId}`,
    method: 'put'
  })
}

// 删除提醒
export function deleteReminder(id) {
  return request({
    url: `/reminder/${id}`,
    method: 'delete'
  })
}
