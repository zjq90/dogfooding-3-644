import request from '@/utils/request'

export function getEmployeeList(params) {
  return request({
    url: '/employee/page',
    method: 'get',
    params
  })
}

export function getAllEmployees() {
  return request({
    url: '/employee/list',
    method: 'get'
  })
}

export function getEmployeesByDepartment(departmentId) {
  return request({
    url: `/employee/department/${departmentId}`,
    method: 'get'
  })
}

export function getEmployeeDetail(id) {
  return request({
    url: `/employee/${id}`,
    method: 'get'
  })
}

export function addEmployee(data) {
  return request({
    url: '/employee',
    method: 'post',
    data
  })
}

export function updateEmployee(id, data) {
  return request({
    url: `/employee/${id}`,
    method: 'put',
    data
  })
}

export function deleteEmployee(id) {
  return request({
    url: `/employee/${id}`,
    method: 'delete'
  })
}

export function updateEmployeeStatus(id, status) {
  return request({
    url: `/employee/${id}/status`,
    method: 'put',
    params: { status }
  })
}
