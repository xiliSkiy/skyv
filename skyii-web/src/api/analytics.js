import request from '@/utils/request'

/**
 * 获取数据分析概览数据
 * @returns {Promise} 返回数据分析概览数据
 */
export function getAnalyticsOverview() {
  return request({
    url: '/api/analytics/overview',
    method: 'get'
  })
}

/**
 * 获取人流量趋势与预测数据
 * @param {Object} params 查询参数
 * @returns {Promise} 返回人流量趋势与预测数据
 */
export function getTrafficTrends(params) {
  return request({
    url: '/api/analytics/traffic/trends',
    method: 'get',
    params
  })
}

/**
 * 获取人员行为模式分析数据
 * @param {Object} params 查询参数
 * @returns {Promise} 返回人员行为模式分析数据
 */
export function getBehaviorPatterns(params) {
  return request({
    url: '/api/analytics/behavior/patterns',
    method: 'get',
    params
  })
}

/**
 * 获取异常事件智能分析数据
 * @param {Object} params 查询参数
 * @returns {Promise} 返回异常事件智能分析数据
 */
export function getAbnormalEvents(params) {
  return request({
    url: '/api/analytics/abnormal/events',
    method: 'get',
    params
  })
}

/**
 * 获取设备故障预测数据
 * @param {Object} params 查询参数
 * @returns {Promise} 返回设备故障预测数据
 */
export function getDeviceFailurePredictions(params) {
  return request({
    url: '/api/analytics/device/failure/predictions',
    method: 'get',
    params
  })
}

/**
 * 获取热力图分析数据
 * @param {Object} params 查询参数
 * @returns {Promise} 返回热力图分析数据
 */
export function getHeatmapAnalysis(params) {
  return request({
    url: '/api/analytics/heatmap',
    method: 'get',
    params
  })
}

/**
 * 获取智能数据探索结果
 * @param {Object} params 查询参数
 * @returns {Promise} 返回智能数据探索结果
 */
export function getDataDiscoveries(params) {
  return request({
    url: '/api/analytics/data/discoveries',
    method: 'get',
    params
  })
}

/**
 * 获取设备分析明细数据
 * @param {Object} params 查询参数
 * @returns {Promise} 返回设备分析明细数据
 */
export function getDeviceAnalytics(params) {
  return request({
    url: '/api/analytics/devices',
    method: 'get',
    params
  })
}

/**
 * 提交AI分析问题
 * @param {Object} data 问题数据
 * @returns {Promise} 返回AI分析结果
 */
export function submitAIQuery(data) {
  return request({
    url: '/api/analytics/ai/query',
    method: 'post',
    data
  })
}

/**
 * 创建自定义分析
 * @param {Object} data 自定义分析配置
 * @returns {Promise} 返回创建结果
 */
export function createCustomAnalysis(data) {
  return request({
    url: '/api/analytics/custom',
    method: 'post',
    data
  })
}

/**
 * 导出分析报告
 * @param {Object} params 报告参数
 * @returns {Promise} 返回导出结果
 */
export function exportAnalyticsReport(params) {
  return request({
    url: '/api/analytics/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 设置异常预警规则
 * @param {Object} data 预警规则配置
 * @returns {Promise} 返回设置结果
 */
export function setAbnormalAlert(data) {
  return request({
    url: '/api/analytics/abnormal/alert',
    method: 'post',
    data
  })
}

/**
 * 创建设备维护工单
 * @param {Object} data 工单数据
 * @returns {Promise} 返回创建结果
 */
export function createMaintenanceOrder(data) {
  return request({
    url: '/api/maintenance/orders',
    method: 'post',
    data
  })
} 