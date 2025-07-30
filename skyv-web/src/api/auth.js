import request from '@/utils/request'

// 登录
export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

// 注册
export function register(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/api/auth/info',
    method: 'get'
  })
}

// 登出
export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

// 忘记密码
export function forgotPassword(data) {
  return request({
    url: '/api/auth/forgot-password',
    method: 'post',
    data
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/api/auth/reset-password',
    method: 'post',
    data
  })
} 