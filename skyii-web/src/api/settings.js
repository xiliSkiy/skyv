import request from '@/utils/request'

// 获取系统设置列表
export function getSettingsList(params) {
  return request({
    url: '/api/settings',
    method: 'get',
    params
  })
}

// 获取单个系统设置
export function getSetting(key) {
  return request({
    url: `/api/settings/${key}`,
    method: 'get'
  })
}

// 获取系统设置分组
export function getSettingsByGroup(group) {
  return request({
    url: `/api/settings/group/${group}`,
    method: 'get'
  })
}

// 更新系统设置
export function updateSetting(data) {
  return request({
    url: '/api/settings',
    method: 'put',
    data
  })
}

// 批量更新系统设置
export function batchUpdateSettings(data) {
  return request({
    url: '/api/settings/batch',
    method: 'put',
    data
  })
}

// 获取系统信息
export function getSystemInfo() {
  return request({
    url: '/api/settings/system-info',
    method: 'get'
  })
}

// 上传系统Logo
export function uploadLogo(data) {
  return request({
    url: '/api/settings/upload/logo',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传系统Favicon
export function uploadFavicon(data) {
  return request({
    url: '/api/settings/upload/favicon',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 重置为默认Logo
export function resetDefaultLogo() {
  return request({
    url: '/api/settings/reset-logo',
    method: 'post'
  })
}

// 获取操作日志
export function getOperationLogs(params) {
  return request({
    url: '/api/settings/operation-logs',
    method: 'get',
    params
  })
}

// 检查系统更新
export function checkSystemUpdate() {
  return request({
    url: '/api/settings/check-update',
    method: 'get'
  })
}

// 获取许可证信息
export function getLicenseInfo() {
  return request({
    url: '/api/settings/license-info',
    method: 'get'
  })
}

// 上传许可证文件
export function uploadLicense(data) {
  return request({
    url: '/api/settings/upload/license',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 激活许可证
export function activateLicense(data) {
  return request({
    url: '/api/settings/activate-license',
    method: 'post',
    data
  })
}

// 获取可用语言列表
export function getAvailableLanguages() {
  return request({
    url: '/api/settings/languages',
    method: 'get'
  })
}

// 获取当前语言设置
export function getCurrentLanguage() {
  return request({
    url: '/api/settings/current-language',
    method: 'get'
  })
}

// 设置系统语言
export function setSystemLanguage(data) {
  return request({
    url: '/api/settings/set-language',
    method: 'post',
    data
  })
} 