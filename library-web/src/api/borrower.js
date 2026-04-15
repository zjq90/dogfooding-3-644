import request from '@/utils/request'

export function getBorrowerList(params) {
  return request({
    url: '/borrower/page',
    method: 'get',
    params
  })
}

export function getAllBorrowers() {
  return request({
    url: '/borrower/list',
    method: 'get'
  })
}

export function getBorrowerDetail(id) {
  return request({
    url: `/borrower/${id}`,
    method: 'get'
  })
}

export function addBorrower(data) {
  return request({
    url: '/borrower',
    method: 'post',
    data
  })
}

export function updateBorrower(id, data) {
  return request({
    url: `/borrower/${id}`,
    method: 'put',
    data
  })
}

export function deleteBorrower(id) {
  return request({
    url: `/borrower/${id}`,
    method: 'delete'
  })
}

export function updateBorrowerStatus(id, status) {
  return request({
    url: `/borrower/${id}/status`,
    method: 'put',
    params: { status }
  })
}
