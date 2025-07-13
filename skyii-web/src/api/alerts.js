import request from '@/utils/request'

// 获取报警规则列表
export function getAlertRules(params) {
  return request({
    url: '/api/alerts/rules',
    method: 'get',
    params
  })
}

// 创建报警规则
export function createAlertRule(data) {
  return request({
    url: '/api/alerts/rules',
    method: 'post',
    data
  })
}

// 更新报警规则
export function updateAlertRule(id, data) {
  return request({
    url: `/api/alerts/rules/${id}`,
    method: 'put',
    data
  })
}

// 删除报警规则
export function deleteAlertRule(id) {
  return request({
    url: `/api/alerts/rules/${id}`,
    method: 'delete'
  })
}

// 启用/禁用报警规则
export function toggleAlertRule(id, enabled) {
  return request({
    url: `/api/alerts/rules/${id}/toggle`,
    method: 'post',
    data: { enabled }
  })
}

// 克隆报警规则
export function cloneAlertRule(id) {
  return request({
    url: `/api/alerts/rules/${id}/clone`,
    method: 'post'
  })
}

// 获取报警规则详情
export function getAlertRuleDetail(id) {
  return request({
    url: `/api/alerts/rules/${id}`,
    method: 'get'
  })
}

// 获取报警规则统计
export function getAlertRuleStats() {
  return request({
    url: '/api/alerts/rules/stats',
    method: 'get'
  })
}

// 获取AI推荐规则
export function getAIRecommendations() {
  return request({
    url: '/api/alerts/ai/recommendations',
    method: 'get'
  })
}

// 应用AI推荐规则
export function applyAIRecommendations(data) {
  return request({
    url: '/api/alerts/ai/recommendations/apply',
    method: 'post',
    data
  })
}

// 获取规则模板
export function getRuleTemplates() {
  return request({
    url: '/api/alerts/templates',
    method: 'get'
  })
}

// 应用规则模板
export function applyRuleTemplate(templateId, data) {
  return request({
    url: `/api/alerts/templates/${templateId}/apply`,
    method: 'post',
    data
  })
}

// 获取监控目标列表
export function getMonitoringTargets(type) {
  return request({
    url: '/api/alerts/targets',
    method: 'get',
    params: { type }
  })
}

// 获取监控指标列表
export function getMonitoringMetrics(type) {
  return request({
    url: '/api/alerts/metrics',
    method: 'get',
    params: { type }
  })
}

// 测试报警规则
export function testAlertRule(data) {
  return request({
    url: '/api/alerts/rules/test',
    method: 'post',
    data
  })
}

// 获取报警规则触发历史
export function getAlertRuleHistory(id, params) {
  return request({
    url: `/api/alerts/rules/${id}/history`,
    method: 'get',
    params
  })
}

// 获取报警规则趋势数据
export function getAlertRuleTrend(id, params) {
  return request({
    url: `/api/alerts/rules/${id}/trend`,
    method: 'get',
    params
  })
}

// 批量操作报警规则
export function batchOperateAlertRules(operation, ruleIds) {
  return request({
    url: '/api/alerts/rules/batch',
    method: 'post',
    data: { operation, ruleIds }
  })
}

// 导入报警规则
export function importAlertRules(data) {
  return request({
    url: '/api/alerts/rules/import',
    method: 'post',
    data
  })
}

// 导出报警规则
export function exportAlertRules(ruleIds) {
  return request({
    url: '/api/alerts/rules/export',
    method: 'post',
    data: { ruleIds },
    responseType: 'blob'
  })
}

// 获取系统状态
export function getSystemStatus() {
  return request({
    url: '/api/alerts/system/status',
    method: 'get'
  })
}

// 获取通知渠道配置
export function getNotificationChannels() {
  return request({
    url: '/api/alerts/notifications/channels',
    method: 'get'
  })
}

// 测试通知渠道
export function testNotificationChannel(channelId, data) {
  return request({
    url: `/api/alerts/notifications/channels/${channelId}/test`,
    method: 'post',
    data
  })
} 