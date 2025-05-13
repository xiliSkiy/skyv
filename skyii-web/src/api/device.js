import request from '@/utils/request'

/**
 * 获取设备列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getDeviceList(params) {
  return request({
    url: '/api/devices',
    method: 'get',
    params
  })
}

/**
 * 条件查询设备
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function searchDevices(params) {
  return request({
    url: '/api/devices/search',
    method: 'get',
    params
  })
}

/**
 * 获取设备详情
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function getDeviceDetail(id) {
  return request({
    url: `/api/devices/${id}`,
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
    url: '/api/devices',
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
    url: `/api/devices/${id}`,
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
    url: `/api/devices/${id}`,
    method: 'delete'
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
    url: `/api/devices/${id}/status`,
    method: 'put',
    params: { status }
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
    url: '/devices/batch/status',
    method: 'put',
    data: ids,
    params: { status }
  })
}

/**
 * 检查设备连接
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function checkDeviceConnection(id) {
  return request({
    url: `/api/devices/${id}/check-connection`,
    method: 'get'
  })
}

/**
 * 获取设备状态统计
 * @returns {Promise}
 */
export function getDeviceStatusStats() {
  return request({
    url: '/api/devices/stats/status',
    method: 'get'
  })
} 