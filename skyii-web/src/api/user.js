import request from '@/utils/request'

// 获取用户列表
export function getUserList(params) {
  const apiParams = { ...params };
  if (apiParams.size !== undefined) {
    apiParams.limit = apiParams.size;
    delete apiParams.size;
  }
  
  return request({
    url: '/api/user/list',
    method: 'get',
    params: apiParams
  })
}

// 获取用户详情
export function getUserDetail(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'get'
  })
}

// 创建用户
export function createUser(data) {
  return request({
    url: '/api/user',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id, data) {
  return request({
    url: `/api/user/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'delete'
  })
}

// 批量删除用户
export function batchDeleteUsers(ids) {
  return request({
    url: '/api/user/batch',
    method: 'delete',
    data: ids
  })
}

// 重置用户密码
export function resetPassword(id, newPassword) {
  return request({
    url: `/api/user/${id}/reset-password`,
    method: 'put',
    params: { newPassword }
  })
}

// 获取所有角色
export function getAllRoles() {
  return request({
    url: '/api/user/roles',
    method: 'get'
  })
}

// 获取用户统计信息
export function getUserStatistics() {
  return request({
    url: '/api/user/statistics',
    method: 'get'
  })
}

// 获取在线用户列表
export function getOnlineUsers() {
  return request({
    url: '/api/user/online',
    method: 'get'
  })
}

// 导出用户数据
export function exportUserData(params) {
  return request({
    url: '/api/user/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
} 