import request from '@/utils/request'

/**
 * 获取设备列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getDeviceList(params) {
  return request({
    url: '/api/v1/devices',
    method: 'get',
    params
  })
}

/**
 * 获取所有设备（不分页）
 * @returns {Promise}
 */
export function getAllDevices() {
  return request({
    url: '/api/v1/devices/all',
    method: 'get'
  })
}

/**
 * 根据类型获取设备
 * @param {String} deviceType 设备类型
 * @returns {Promise}
 */
export function getDevicesByType(deviceType) {
  return request({
    url: `/api/v1/devices/type/${deviceType}`,
    method: 'get'
  })
}

/**
 * 获取设备详情
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function getDeviceById(id) {
  return request({
    url: `/api/v1/devices/${id}`,
    method: 'get'
  })
}

/**
 * 创建设备
 * @param {Object} data 设备数据
 * @returns {Promise}
 */
export function createDevice(data) {
  return request({
    url: '/api/v1/devices',
    method: 'post',
    data
  })
}

/**
 * 更新设备
 * @param {Number} id 设备ID
 * @param {Object} data 设备数据
 * @returns {Promise}
 */
export function updateDevice(id, data) {
  return request({
    url: `/api/v1/devices/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除设备
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function deleteDevice(id) {
  return request({
    url: `/api/v1/devices/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除设备
 * @param {Array} ids 设备ID列表
 * @returns {Promise}
 */
export function batchDeleteDevices(ids) {
  return request({
    url: '/api/v1/devices/batch',
    method: 'delete',
    data: { ids }
  })
}

/**
 * 更新设备状态
 * @param {Number} id 设备ID
 * @param {Number} status 设备状态
 * @returns {Promise}
 */
export function updateDeviceStatus(id, status) {
  return request({
    url: `/api/v1/devices/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 获取设备统计信息
 * @returns {Promise}
 */
export function getDeviceStatistics() {
  return request({
    url: '/api/v1/devices/statistics',
    method: 'get'
  })
}

/**
 * 条件查询设备
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function searchDevices(params) {
  return request({
    url: '/api/v1/devices/search',
    method: 'get',
    params
  })
}

/**
 * 获取设备状态统计
 * @returns {Promise}
 */
export function getDeviceStatusStats() {
  return request({
    url: '/api/v1/devices/stats/status',
    method: 'get'
  })
}

/**
 * 检查设备连接
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function checkDeviceConnection(id) {
  return request({
    url: `/api/v1/devices/${id}/check-connection`,
    method: 'get'
  })
}

/**
 * 批量更新设备状态
 * @param {Array} ids 设备ID列表
 * @param {Number} status 设备状态
 * @returns {Promise}
 */
export function batchUpdateDeviceStatus(ids, status) {
  return request({
    url: '/api/v1/devices/batch/status',
    method: 'put',
    data: ids,
    params: { status }
  })
} 