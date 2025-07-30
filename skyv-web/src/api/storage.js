import request from './request'

// 存储设备管理
export const getStorageDevices = () => {
  return request.get('/storage/devices')
}

export const addStorageDevice = (data) => {
  return request.post('/storage/devices', data)
}

export const updateStorageDevice = (id, data) => {
  return request.put(`/storage/devices/${id}`, data)
}

export const deleteStorageDevice = (id) => {
  return request.delete(`/storage/devices/${id}`)
}

export const toggleStorageDevice = (id, enabled) => {
  return request.put(`/storage/devices/${id}/toggle`, { enabled })
}

export const checkStorageDevice = (id) => {
  return request.post(`/storage/devices/${id}/check`)
}

export const getStorageDeviceDetails = (id) => {
  return request.get(`/storage/devices/${id}/details`)
}

export const formatStorageDevice = (id) => {
  return request.post(`/storage/devices/${id}/format`)
}

export const mountStorageDevice = (id) => {
  return request.post(`/storage/devices/${id}/mount`)
}

export const unmountStorageDevice = (id) => {
  return request.post(`/storage/devices/${id}/unmount`)
}

// 存储概览
export const getStorageOverview = () => {
  return request.get('/storage/overview')
}

export const refreshStorageStatus = () => {
  return request.post('/storage/refresh')
}

// 备份管理
export const getBackupSettings = () => {
  return request.get('/storage/backup/settings')
}

export const updateBackupSettings = (data) => {
  return request.put('/storage/backup/settings', data)
}

export const getBackupList = (params) => {
  return request.get('/storage/backup/list', { params })
}

export const createBackup = (data) => {
  return request.post('/storage/backup/create', data)
}

export const restoreBackup = (id) => {
  return request.post(`/storage/backup/${id}/restore`)
}

export const deleteBackup = (id) => {
  return request.delete(`/storage/backup/${id}`)
}

export const downloadBackup = (id) => {
  return request.get(`/storage/backup/${id}/download`, { responseType: 'blob' })
}

export const getBackupProgress = (id) => {
  return request.get(`/storage/backup/${id}/progress`)
}

export const cancelBackup = (id) => {
  return request.post(`/storage/backup/${id}/cancel`)
}

// 数据清理
export const getCleanupSettings = () => {
  return request.get('/storage/cleanup/settings')
}

export const updateCleanupSettings = (data) => {
  return request.put('/storage/cleanup/settings', data)
}

export const getCleanupAnalysis = (params) => {
  return request.get('/storage/cleanup/analysis', { params })
}

export const startCleanup = (data) => {
  return request.post('/storage/cleanup/start', data)
}

export const getCleanupProgress = (id) => {
  return request.get(`/storage/cleanup/${id}/progress`)
}

export const cancelCleanup = (id) => {
  return request.post(`/storage/cleanup/${id}/cancel`)
}

export const getCleanupHistory = (params) => {
  return request.get('/storage/cleanup/history', { params })
}

// 配额管理
export const getQuotaSettings = () => {
  return request.get('/storage/quota/settings')
}

export const updateQuotaSettings = (data) => {
  return request.put('/storage/quota/settings', data)
}

export const getQuotaUsage = () => {
  return request.get('/storage/quota/usage')
}

export const getUserQuotas = (params) => {
  return request.get('/storage/quota/users', { params })
}

export const updateUserQuota = (userId, data) => {
  return request.put(`/storage/quota/users/${userId}`, data)
}

export const getDeviceQuotas = (params) => {
  return request.get('/storage/quota/devices', { params })
}

export const updateDeviceQuota = (deviceId, data) => {
  return request.put(`/storage/quota/devices/${deviceId}`, data)
}

// 存储告警
export const getStorageAlerts = () => {
  return request.get('/storage/alerts')
}

export const updateStorageAlerts = (data) => {
  return request.put('/storage/alerts', data)
}

export const getStorageAlertHistory = (params) => {
  return request.get('/storage/alerts/history', { params })
}

// 存储性能
export const getStoragePerformance = (params) => {
  return request.get('/storage/performance', { params })
}

export const getStorageIOStats = (deviceId, params) => {
  return request.get(`/storage/devices/${deviceId}/iostats`, { params })
}

// 存储监控
export const getStorageMonitoring = () => {
  return request.get('/storage/monitoring')
}

export const getStorageHealthCheck = () => {
  return request.post('/storage/health-check')
}

// 存储报告
export const generateStorageReport = (data) => {
  return request.post('/storage/report/generate', data)
}

export const getStorageReports = (params) => {
  return request.get('/storage/report/list', { params })
}

export const downloadStorageReport = (id) => {
  return request.get(`/storage/report/${id}/download`, { responseType: 'blob' })
}

// 存储策略
export const getStoragePolicies = () => {
  return request.get('/storage/policies')
}

export const updateStoragePolicy = (id, data) => {
  return request.put(`/storage/policies/${id}`, data)
}

export const createStoragePolicy = (data) => {
  return request.post('/storage/policies', data)
}

export const deleteStoragePolicy = (id) => {
  return request.delete(`/storage/policies/${id}`)
} 