import request from '@/utils/request'

export function getBorrowOrderList(params) {
  return request({
    url: '/borrowOrder/page',
    method: 'get',
    params
  })
}

export function getBorrowOrderDetail(id) {
  return request({
    url: `/borrowOrder/${id}`,
    method: 'get'
  })
}

export function createBorrowOrder(data) {
  return request({
    url: '/borrowOrder',
    method: 'post',
    data
  })
}

export function returnBook(id) {
  return request({
    url: `/borrowOrder/${id}/return`,
    method: 'put'
  })
}

export function refundOrderDeposit(id) {
  return request({
    url: `/borrowOrder/${id}/refundDeposit`,
    method: 'put'
  })
}

export function deleteBorrowOrder(id) {
  return request({
    url: `/borrowOrder/${id}`,
    method: 'delete'
  })
}
