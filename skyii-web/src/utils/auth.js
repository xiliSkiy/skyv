import Cookies from 'js-cookie'

const TokenKey = 'skyeye_token'

// 获取token
export function getToken() {
  return Cookies.get(TokenKey)
}

// 设置token
export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

// 移除token
export function removeToken() {
  return Cookies.remove(TokenKey)
}

// 判断是否有权限
export function hasPermission(permission, userPermissions) {
  if (!permission || !userPermissions) return false
  return userPermissions.includes(permission)
}

// 判断是否有角色
export function hasRole(role, userRoles) {
  if (!role || !userRoles) return false
  return userRoles.includes(role)
} 