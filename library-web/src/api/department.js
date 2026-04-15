import request from '@/utils/request'

export function getDepartmentList(params) {
  return request({
    url: '/department/page',
    method: 'get',
    params
  })
}

export function getDepartmentTree() {
  return request({
    url: '/department/tree',
    method: 'get'
  })
}

export function getAllDepartments() {
  return request({
    url: '/department/list',
    method: 'get'
  })
}

export function getDepartmentDetail(id) {
  return request({
    url: `/department/${id}`,
    method: 'get'
  })
}

export function addDepartment(data) {
  return request({
    url: '/department',
    method: 'post',
    data
  })
}

export function updateDepartment(id, data) {
  return request({
    url: `/department/${id}`,
    method: 'put',
    data
  })
}

export function deleteDepartment(id) {
  return request({
    url: `/department/${id}`,
    method: 'delete'
  })
}

export function updateDepartmentStatus(id, status) {
  return request({
    url: `/department/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function generateDepartmentCode() {
  return request({
    url: '/department/generate-code',
    method: 'get'
  })
}
