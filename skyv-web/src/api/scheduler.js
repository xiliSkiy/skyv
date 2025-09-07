import request from '@/utils/request'

/**
 * 获取调度器状态
 * @returns {Promise} 请求Promise
 */
export function getSchedulerStatus() {
  return request({
    url: '/api/collector/scheduler/status',
    method: 'get'
  })
}

/**
 * 启动调度器
 * @returns {Promise} 请求Promise
 */
export function startScheduler() {
  return request({
    url: '/api/collector/scheduler/start',
    method: 'post'
  })
}

/**
 * 停止调度器
 * @returns {Promise} 请求Promise
 */
export function stopScheduler() {
  return request({
    url: '/api/collector/scheduler/stop',
    method: 'post'
  })
}

/**
 * 获取调度器统计信息
 * @returns {Promise} 请求Promise
 */
export function getSchedulerStatistics() {
  return request({
    url: '/api/collector/scheduler/statistics',
    method: 'get'
  })
}

/**
 * 获取已调度的任务ID列表
 * @returns {Promise} 请求Promise
 */
export function getScheduledTaskIds() {
  return request({
    url: '/api/collector/scheduler/scheduled-tasks',
    method: 'get'
  })
}

/**
 * 检查任务是否已调度
 * @param {number} taskId 任务ID
 * @returns {Promise} 请求Promise
 */
export function isTaskScheduled(taskId) {
  return request({
    url: `/api/collector/scheduler/tasks/${taskId}/scheduled`,
    method: 'get'
  })
}

/**
 * 重新加载所有任务
 * @returns {Promise} 请求Promise
 */
export function reloadAllTasks() {
  return request({
    url: '/api/collector/scheduler/reload',
    method: 'post'
  })
}

/**
 * 清理过期任务
 * @returns {Promise} 请求Promise
 */
export function cleanupExpiredTasks() {
  return request({
    url: '/api/collector/scheduler/cleanup',
    method: 'post'
  })
}

/**
 * 获取调度器健康状态
 * @returns {Promise} 请求Promise
 */
export function getSchedulerHealth() {
  return request({
    url: '/api/collector/scheduler/health',
    method: 'get'
  })
}

/**
 * 获取调度器指标
 * @returns {Promise} 请求Promise
 */
export function getSchedulerMetrics() {
  return request({
    url: '/api/collector/scheduler/metrics',
    method: 'get'
  })
}

/**
 * 获取调度器配置
 * @returns {Promise} 请求Promise
 */
export function getSchedulerConfig() {
  return request({
    url: '/api/collector/scheduler/config',
    method: 'get'
  })
}

/**
 * 获取任务下次执行时间
 * @param {number} taskId 任务ID
 * @returns {Promise} 请求Promise
 */
export function getTaskNextExecutionTime(taskId) {
  return request({
    url: `/api/collector/scheduler/tasks/${taskId}/next-execution-time`,
    method: 'get'
  })
}

/**
 * 暂停任务调度
 * @param {number} taskId 任务ID
 * @returns {Promise} 请求Promise
 */
export function pauseTaskScheduling(taskId) {
  return request({
    url: `/api/collector/scheduler/tasks/${taskId}/pause`,
    method: 'post'
  })
}

/**
 * 恢复任务调度
 * @param {number} taskId 任务ID
 * @returns {Promise} 请求Promise
 */
export function resumeTaskScheduling(taskId) {
  return request({
    url: `/api/collector/scheduler/tasks/${taskId}/resume`,
    method: 'post'
  })
}

/**
 * 取消任务调度
 * @param {number} taskId 任务ID
 * @returns {Promise} 请求Promise
 */
export function cancelTaskScheduling(taskId) {
  return request({
    url: `/api/collector/scheduler/tasks/${taskId}/cancel`,
    method: 'post'
  })
}

/**
 * 重新调度任务
 * @param {number} taskId 任务ID
 * @returns {Promise} 请求Promise
 */
export function rescheduleTask(taskId) {
  return request({
    url: `/api/collector/scheduler/tasks/${taskId}/reschedule`,
    method: 'post'
  })
}

/**
 * 获取调度器性能报告
 * @param {Object} params 查询参数
 * @returns {Promise} 请求Promise
 */
export function getSchedulerPerformanceReport(params) {
  return request({
    url: '/api/collector/scheduler/performance-report',
    method: 'get',
    params
  })
}

/**
 * 导出调度器日志
 * @param {Object} params 导出参数
 * @returns {Promise} 请求Promise
 */
export function exportSchedulerLogs(params) {
  return request({
    url: '/api/collector/scheduler/export-logs',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 获取调度器事件历史
 * @param {Object} params 查询参数
 * @returns {Promise} 请求Promise
 */
export function getSchedulerEventHistory(params) {
  return request({
    url: '/api/collector/scheduler/event-history',
    method: 'get',
    params
  })
}

/**
 * 设置调度器配置
 * @param {Object} data 配置数据
 * @returns {Promise} 请求Promise
 */
export function setSchedulerConfig(data) {
  return request({
    url: '/api/collector/scheduler/config',
    method: 'put',
    data
  })
}

/**
 * 重置调度器配置
 * @returns {Promise} 请求Promise
 */
export function resetSchedulerConfig() {
  return request({
    url: '/api/collector/scheduler/config/reset',
    method: 'post'
  })
}

/**
 * 备份调度器配置
 * @returns {Promise} 请求Promise
 */
export function backupSchedulerConfig() {
  return request({
    url: '/api/collector/scheduler/config/backup',
    method: 'post'
  })
}

/**
 * 恢复调度器配置
 * @param {Object} data 备份数据
 * @returns {Promise} 请求Promise
 */
export function restoreSchedulerConfig(data) {
  return request({
    url: '/api/collector/scheduler/config/restore',
    method: 'post',
    data
  })
}
