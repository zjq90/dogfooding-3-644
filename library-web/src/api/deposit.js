import request from '@/utils/request'

export function getDepositList(params) {
  return request({
    url: '/deposit/page',
    method: 'get',
    params
  })
}

export function addDeposit(data) {
  return request({
    url: '/deposit/add',
    method: 'post',
    data
  })
}

export function refundDeposit(data) {
  return request({
    url: '/deposit/refund',
    method: 'post',
    data
  })
}

export function deductDeposit(data) {
  return request({
    url: '/deposit/deduct',
    method: 'post',
    data
  })
}
