import request from '@/utils/request'

export function getRemindPage(params) {
  return request({
    url: '/api/remind/page',
    method: 'get',
    params
  })
}

export function getRemindOverview() {
  return request({
    url: '/api/remind/overview',
    method: 'get'
  })
}

export function markAsRead(id) {
  return request({
    url: `/api/remind/${id}/read`,
    method: 'post'
  })
}

export function markAllAsRead() {
  return request({
    url: '/api/remind/read-all',
    method: 'post'
  })
}

export function processOverdueOrders() {
  return request({
    url: '/api/remind/process',
    method: 'post'
  })
}

export function createTestRemind(orderId, remindDay) {
  return request({
    url: `/api/remind/test/${orderId}`,
    method: 'post',
    params: { remindDay }
  })
}

export function getSmsPage(params) {
  return request({
    url: '/api/sms/page',
    method: 'get',
    params
  })
}

export function sendSms(data) {
  return request({
    url: '/api/sms/send',
    method: 'post',
    params: data
  })
}