import request from '@/utils/request'

// 获取指标列表（分页）
export function getMetrics(params) {
  return request({
    url: '/api/v1/metrics',
    method: 'get',
    params
  })
}

// 获取所有指标（不分页）
export function getAllMetrics() {
  return request({
    url: '/api/v1/metrics/all',
    method: 'get'
  })
}

// 根据ID获取指标
export function getMetricById(id) {
  return request({
    url: `/api/v1/metrics/${id}`,
    method: 'get'
  })
}

// 根据指标类型获取指标列表
export function getMetricsByType(type) {
  return request({
    url: `/api/v1/metrics/type/${type}`,
    method: 'get'
  })
}

// 根据设备类型获取指标列表
export function getMetricsByDeviceType(deviceType) {
  return request({
    url: `/api/v1/metrics/device-type/${deviceType}`,
    method: 'get'
  })
}

// 创建指标
export function createMetric(data) {
  return request({
    url: '/api/v1/metrics',
    method: 'post',
    data
  })
}

// 批量创建指标
export function batchCreateMetrics(data) {
  return request({
    url: '/api/v1/metrics/batch',
    method: 'post',
    data
  })
}

// 更新指标
export function updateMetric(id, data) {
  return request({
    url: `/api/v1/metrics/${id}`,
    method: 'put',
    data
  })
}

// 删除指标
export function deleteMetric(id) {
  return request({
    url: `/api/v1/metrics/${id}`,
    method: 'delete'
  })
}

// 批量删除指标
export function batchDeleteMetrics(ids) {
  return request({
    url: '/api/v1/metrics/batch',
    method: 'delete',
    data: ids
  })
}

// 更新指标状态
export function updateMetricStatus(id, status) {
  return request({
    url: `/api/v1/metrics/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 批量更新指标状态
export function batchUpdateMetricStatus(ids, status) {
  return request({
    url: '/api/v1/metrics/batch/status',
    method: 'put',
    data: ids,
    params: { status }
  })
}

// 导入指标
export function importMetrics(data) {
  return request({
    url: '/api/v1/metrics/import',
    method: 'post',
    data
  })
}

// 导出指标
export function exportMetrics(ids) {
  return request({
    url: '/api/v1/metrics/export',
    method: 'post',
    data: ids
  })
}

// 获取指标统计信息
export function getMetricStatistics() {
  return request({
    url: '/api/v1/metrics/statistics',
    method: 'get'
  })
}

// 检查指标Key是否存在
export function checkMetricKey(metricKey, id) {
  return request({
    url: '/api/v1/metrics/check-key',
    method: 'get',
    params: { metricKey, id }
  })
}

/**
 * 获取指标概览数据
 * @returns {Promise<any>}
 */
export function getMetricsOverview() {
  return request({
    url: '/api/v1/metrics/overview',
    method: 'get'
  })
}

 

/**
 * 获取指标列表
 * @param {Object} params - 查询参数
 * @returns {Promise<any>}
 */
export function getMetricList(params) {
  return request({
    url: '/api/v1/metrics',
    method: 'get',
    params
  })
}

// ====================== 指标模板相关接口 ======================

// 获取指标模板列表（分页）
export function getMetricTemplates(params) {
  return request({
    url: '/api/v1/metric-templates',
    method: 'get',
    params
  })
}

// 获取所有指标模板
export function getAllMetricTemplates() {
  return request({
    url: '/api/v1/metric-templates/all',
    method: 'get'
  })
}

// 根据ID获取指标模板
export function getMetricTemplateById(id) {
  return request({
    url: `/api/v1/metric-templates/${id}`,
    method: 'get'
  })
}

// 创建指标模板
export function createMetricTemplate(data) {
  return request({
    url: '/api/v1/metric-templates',
    method: 'post',
    data
  })
}

// 更新指标模板
export function updateMetricTemplate(id, data) {
  return request({
    url: `/api/v1/metric-templates/${id}`,
    method: 'put',
    data
  })
}

// 删除指标模板
export function deleteMetricTemplate(id) {
  return request({
    url: `/api/v1/metric-templates/${id}`,
    method: 'delete'
  })
}

// 批量删除指标模板
export function batchDeleteMetricTemplates(ids) {
  return request({
    url: '/api/v1/metric-templates/batch',
    method: 'delete',
    data: ids
  })
}

// 从模板创建指标
export function createMetricFromTemplate(id) {
  return request({
    url: `/api/v1/metric-templates/${id}/create-metric`,
    method: 'post'
  })
}

// 从指标创建模板
export function createTemplateFromMetric(metricId, templateName) {
  return request({
    url: '/api/v1/metric-templates/create-from-metric',
    method: 'post',
    params: { metricId, templateName }
  })
}

// 应用指标模板
export function applyMetricTemplate(id, data) {
  return request({
    url: `/api/v1/metric-templates/${id}/apply`,
    method: 'post',
    data
  })
}

// 获取模板应用历史
export function getTemplateApplicationHistory(templateId) {
  return request({
    url: `/api/v1/metric-templates/${templateId}/application-history`,
    method: 'get'
  })
}

 