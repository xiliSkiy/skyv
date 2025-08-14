const ACCESS_TOKEN_KEY = 'skyeye_access_token'
const REFRESH_TOKEN_KEY = 'skyeye_refresh_token'
const USER_INFO_KEY = 'skyeye_user_info'

// ==================== Token 管理 ====================

// 获取访问Token
export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

// 设置访问Token
export function setAccessToken(token) {
  return localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

// 移除访问Token
export function removeAccessToken() {
  return localStorage.removeItem(ACCESS_TOKEN_KEY)
}

// 获取刷新Token
export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

// 设置刷新Token
export function setRefreshToken(token) {
  return localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

// 移除刷新Token
export function removeRefreshToken() {
  return localStorage.removeItem(REFRESH_TOKEN_KEY)
}

// ==================== 兼容性方法 ====================

// 获取token（兼容旧代码）
export function getToken() {
  return getAccessToken()
}

// 设置token（兼容旧代码）
export function setToken(token) {
  return setAccessToken(token)
}

// 移除token（兼容旧代码）
export function removeToken() {
  removeAccessToken()
  removeRefreshToken()
  removeUserInfo()
}

// ==================== 用户信息管理 ====================

// 获取用户信息
export function getUserInfo() {
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

// 设置用户信息
export function setUserInfo(userInfo) {
  return localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

// 移除用户信息
export function removeUserInfo() {
  return localStorage.removeItem(USER_INFO_KEY)
}

// ==================== 权限检查 ====================

// 判断是否有权限
export function hasPermission(permission, userPermissions) {
  if (!permission || !userPermissions) return false
  if (Array.isArray(permission)) {
    return permission.some(p => userPermissions.includes(p))
  }
  return userPermissions.includes(permission)
}

// 判断是否有角色
export function hasRole(role, userRoles) {
  if (!role || !userRoles) return false
  if (Array.isArray(role)) {
    return role.some(r => userRoles.includes(r))
  }
  return userRoles.includes(role)
}

// 判断是否为管理员
export function isAdmin(userRoles) {
  return hasRole('ADMIN', userRoles)
}

// ==================== 登录状态检查 ====================

// 检查是否已登录
export function isLoggedIn() {
  return !!getAccessToken()
}

// 检查Token是否即将过期（需要后端支持）
export function isTokenExpiringSoon() {
  // 这里可以解析JWT Token的过期时间
  // 目前简单返回false，后续可以完善
  return false
}

// ==================== 清除所有认证信息 ====================

// 清除所有认证相关的本地存储
export function clearAuth() {
  removeAccessToken()
  removeRefreshToken()
  removeUserInfo()
}

// ==================== Token 解析工具 ====================

// 解析JWT Token（简单实现）
export function parseJwtToken(token) {
  if (!token) return null
  
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    
    const payload = parts[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decoded)
  } catch (error) {
    console.error('Parse JWT token failed:', error)
    return null
  }
}

// 获取Token过期时间
export function getTokenExpiration(token) {
  const payload = parseJwtToken(token)
  return payload ? payload.exp * 1000 : null
}

// 检查Token是否过期
export function isTokenExpired(token) {
  const expiration = getTokenExpiration(token)
  return expiration ? Date.now() >= expiration : true
} 