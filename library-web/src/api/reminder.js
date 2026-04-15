import request from '@/utils/request'

export function getReminderPage(params) {
  return request({
    url: '/reminder/page',
    method: 'get',
    params
  })
}

export function getPendingReminders() {
  return request({
    url: '/reminder/pending',
    method: 'get'
  })
}

export function getRemindersByOrderId(orderId) {
  return request({
    url: `/reminder/order/${orderId}`,
    method: 'get'
  })
}

export function completeReminder(id) {
  return request({
    url: `/reminder/${id}/complete`,
    method: 'put'
  })
}

export function cancelReminder(orderId) {
  return request({
    url: `/reminder/cancel/${orderId}`,
    method: 'put'
  })
}
