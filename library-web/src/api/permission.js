import request from '@/utils/request'

export function getAllPermissions() {
  return request({
    url: '/permission/list',
    method: 'get'
  })
}

export function getPermissionTree() {
  return request({
    url: '/permission/tree',
    method: 'get'
  })
}

export function getPermissionsByType(type) {
  return request({
    url: `/permission/type/${type}`,
    method: 'get'
  })
}

export function getPermissionsByDepartment(departmentId) {
  return request({
    url: `/permission/department/${departmentId}`,
    method: 'get'
  })
}

export function getPermissionDetail(id) {
  return request({
    url: `/permission/${id}`,
    method: 'get'
  })
}

export function addPermission(data) {
  return request({
    url: '/permission',
    method: 'post',
    data
  })
}

export function updatePermission(id, data) {
  return request({
    url: `/permission/${id}`,
    method: 'put',
    data
  })
}

export function deletePermission(id) {
  return request({
    url: `/permission/${id}`,
    method: 'delete'
  })
}

export function updatePermissionStatus(id, status) {
  return request({
    url: `/permission/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function getDepartmentPermissionIds(departmentId) {
  return request({
    url: `/permission/department/${departmentId}/ids`,
    method: 'get'
  })
}

export function assignDepartmentPermissions(departmentId, permissionIds) {
  return request({
    url: `/permission/department/${departmentId}`,
    method: 'post',
    data: permissionIds
  })
}
