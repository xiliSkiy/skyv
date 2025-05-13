import request from '@/utils/request'

/**
 * 获取监控设备列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getMonitoringDevices(params) {
  return request({
    url: '/api/devices/search',
    method: 'get',
    params
  })
}

/**
 * 获取设备流地址
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function getDeviceStreamUrl(id) {
  return request({
    url: `/api/devices/${id}/stream`,
    method: 'get'
  })
}

/**
 * 启动设备监控
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function startDeviceMonitoring(id) {
  return request({
    url: `/api/devices/${id}/monitoring/start`,
    method: 'post'
  })
}

/**
 * 停止设备监控
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function stopDeviceMonitoring(id) {
  return request({
    url: `/api/devices/${id}/monitoring/stop`,
    method: 'post'
  })
}

/**
 * 获取设备监控状态
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function getDeviceMonitoringStatus(id) {
  return request({
    url: `/api/devices/${id}/monitoring/status`,
    method: 'get'
  })
}

/**
 * 获取设备分组列表
 * @returns {Promise}
 */
export function getDeviceGroups() {
  return request({
    url: '/api/device-groups',
    method: 'get'
  })
}

/**
 * 截取监控画面
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function captureSnapshot(id) {
  return request({
    url: `/api/devices/${id}/snapshot`,
    method: 'post'
  })
}

/**
 * 开始录制
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function startRecording(id) {
  return request({
    url: `/api/devices/${id}/recording/start`,
    method: 'post'
  })
}

/**
 * 停止录制
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function stopRecording(id) {
  return request({
    url: `/api/devices/${id}/recording/stop`,
    method: 'post'
  })
} 