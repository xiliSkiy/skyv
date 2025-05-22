import request from '@/utils/request'

/**
 * 获取采集器列表（分页）
 * @param {Object} params - 查询参数
 * @returns {Promise<any>}
 */
export function getCollectors(params) {
  return request({
    url: '/api/collectors',
    method: 'get',
    params
  })
}

/**
 * 获取所有采集器（不分页）
 * @returns {Promise<any>}
 */
export function getAllCollectors() {
  return request({
    url: '/api/collectors/all',
    method: 'get'
  })
}

/**
 * 根据ID获取采集器
 * @param {number} id - 采集器ID
 * @returns {Promise<any>}
 */
export function getCollectorById(id) {
  return request({
    url: `/api/collectors/${id}`,
    method: 'get'
  })
}

/**
 * 创建采集器
 * @param {Object} data - 采集器数据
 * @returns {Promise<any>}
 */
export function createCollector(data) {
  return request({
    url: '/api/collectors',
    method: 'post',
    data
  })
}

/**
 * 更新采集器
 * @param {number} id - 采集器ID
 * @param {Object} data - 采集器数据
 * @returns {Promise<any>}
 */
export function updateCollector(id, data) {
  return request({
    url: `/api/collectors/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除采集器
 * @param {number} id - 采集器ID
 * @returns {Promise<any>}
 */
export function deleteCollector(id) {
  return request({
    url: `/api/collectors/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除采集器
 * @param {Array<number>} ids - 采集器ID数组
 * @returns {Promise<any>}
 */
export function batchDeleteCollectors(ids) {
  return request({
    url: '/api/collectors/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 设置主采集器
 * @param {number} id - 采集器ID
 * @returns {Promise<any>}
 */
export function setPrimaryCollector(id) {
  return request({
    url: `/api/collectors/${id}/primary`,
    method: 'put'
  })
}

/**
 * 测试采集器连接
 * @param {Object} params - 采集器参数
 * @returns {Promise<any>}
 */
export function testCollectorConnection(params) {
  return request({
    url: '/api/collectors/test',
    method: 'post',
    data: params
  })
}

/**
 * 批量测试采集器连接
 * @param {Array<number>} ids - 采集器ID数组
 * @returns {Promise<any>}
 */
export function batchTestCollectorConnections(ids) {
  return request({
    url: '/api/collectors/batch-test',
    method: 'post',
    data: ids
  })
}

/**
 * 获取采集器状态统计
 * @returns {Promise<any>}
 */
export function getCollectorStatusStatistics() {
  return request({
    url: '/api/collectors/stats',
    method: 'get'
  })
}

/**
 * 获取采集器网络区域列表
 * @returns {Promise<any>}
 */
export function getCollectorNetworkZones() {
  return request({
    url: '/api/collectors/network-zones',
    method: 'get'
  })
}

/**
 * 重启采集器
 * @param {number} id - 采集器ID
 * @returns {Promise<any>}
 */
export function restartCollector(id) {
  return request({
    url: `/api/collectors/${id}/restart`,
    method: 'post'
  })
}

/**
 * 生成采集器注册令牌
 * @param {number} id - 采集器ID
 * @param {number} expireHours - 过期小时数
 * @returns {Promise<any>}
 */
export function generateRegistrationToken(id, expireHours = 24) {
  return request({
    url: `/api/collectors/${id}/token`,
    method: 'post',
    params: { expireHours }
  })
}

/**
 * 根据注册令牌激活采集器
 * @param {string} token - 注册令牌
 * @param {Object} collectorInfo - 采集器连接信息
 * @returns {Promise<any>}
 */
export function activateCollector(token, collectorInfo) {
  return request({
    url: '/api/collectors/activate',
    method: 'post',
    params: { token },
    data: collectorInfo
  })
}

/**
 * 获取采集器监控指标
 * @param {number} id - 采集器ID
 * @returns {Promise<any>}
 */
export function getCollectorMetrics(id) {
  return request({
    url: `/api/collectors/${id}/metrics`,
    method: 'get'
  })
}

/**
 * 获取采集器关联的设备
 * @param {number} collectorId - 采集器ID
 * @param {Object} params - 查询参数
 * @returns {Promise<any>}
 */
export function getDevicesByCollectorId(collectorId, params) {
  return request({
    url: `/api/collector-devices/collector/${collectorId}/devices`,
    method: 'get',
    params
  })
}

/**
 * 获取未关联采集器的设备列表
 * @param {Object} params - 查询参数
 * @returns {Promise<any>}
 */
export function getUnassociatedDevices(params) {
  return request({
    url: '/api/collector-devices/unassociated-devices',
    method: 'get',
    params
  })
}

/**
 * 设备关联到采集器
 * @param {Object} data - 关联信息
 * @returns {Promise<any>}
 */
export function associateDevice(data) {
  return request({
    url: '/api/collector-devices',
    method: 'post',
    data
  })
}

/**
 * 批量关联设备到采集器
 * @param {Object} data - 批量关联请求
 * @returns {Promise<any>}
 */
export function batchAssociateDevices(data) {
  return request({
    url: '/api/collector-devices/batch',
    method: 'post',
    data
  })
}

/**
 * 分配设备到采集器
 * @param {Object} data - 包含collectorId和deviceIds的对象
 * @returns {Promise<any>}
 */
export function assignDevicesToCollector(data) {
  return request({
    url: '/api/collector-devices/batch',
    method: 'post',
    data
  })
}

/**
 * 取消设备与采集器的关联
 * @param {number} collectorId - 采集器ID
 * @param {number} deviceId - 设备ID
 * @returns {Promise<any>}
 */
export function disassociateDevice(collectorId, deviceId) {
  return request({
    url: `/api/collector-devices/collector/${collectorId}/device/${deviceId}`,
    method: 'delete'
  })
}

/**
 * 根据ID取消关联
 * @param {number} id - 关联关系ID
 * @returns {Promise<any>}
 */
export function disassociateDeviceById(id) {
  return request({
    url: `/api/collector-devices/${id}`,
    method: 'delete'
  })
} 