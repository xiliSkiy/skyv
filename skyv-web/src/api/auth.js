import request from '@/utils/request'

// 登录
export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

// 登出
export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

// 刷新Token
export function refreshToken(data) {
  return request({
    url: '/api/auth/refresh',
    method: 'post',
    data
  })
}

// 检查Token有效性
export function checkToken() {
  return request({
    url: '/api/auth/check',
    method: 'get'
  })
}

// 获取当前用户信息
export function getCurrentUser() {
  return request({
    url: '/api/auth/me',
    method: 'get'
  })
}

// 测试公开接口
export function testPublicApi() {
  return request({
    url: '/api/auth/test/public',
    method: 'get'
  })
}

// 测试受保护接口
export function testProtectedApi() {
  return request({
    url: '/api/auth/test/protected',
    method: 'get'
  })
}

// 测试管理员接口
export function testAdminApi() {
  return request({
    url: '/api/auth/test/admin',
    method: 'get'
  })
}

// 获取用户信息测试接口
export function getUserInfoTest() {
  return request({
    url: '/api/auth/test/userinfo',
    method: 'get'
  })
}

// 注册（暂时保留，后续可能需要）
export function register(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

// 忘记密码（暂时保留，后续可能需要）
export function forgotPassword(data) {
  return request({
    url: '/api/auth/forgot-password',
    method: 'post',
    data
  })
}

// 重置密码（暂时保留，后续可能需要）
export function resetPassword(data) {
  return request({
    url: '/api/auth/reset-password',
    method: 'post',
    data
  })
} 