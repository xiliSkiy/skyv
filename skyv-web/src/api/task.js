import request from '@/utils/request'

/**
 * 获取任务列表
 * @param {Object} params 查询参数
 * @returns {Promise} 请求Promise
 */
export function getTaskList(params) {
  return request({
    url: '/api/collector/tasks',
    method: 'get',
    params
  })
}

/**
 * 获取任务详情
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function getTaskDetail(id) {
  return request({
    url: `/api/collector/tasks/${id}`,
    method: 'get'
  })
}

/**
 * 根据名称获取任务
 * @param {string} name 任务名称
 * @returns {Promise} 请求Promise
 */
export function getTaskByName(name) {
  return request({
    url: `/api/collector/tasks/name/${name}`,
    method: 'get'
  })
}

/**
 * 检查任务名称是否存在
 * @param {string} name 任务名称
 * @param {number} excludeId 排除的任务ID（编辑时使用）
 * @returns {Promise} 请求Promise
 */
export function checkTaskNameExists(name, excludeId = null) {
  const params = { name }
  if (excludeId) {
    params.excludeId = excludeId
  }
  return request({
    url: '/api/collector/tasks/check-name',
    method: 'get',
    params
  })
}

/**
 * 创建任务
 * @param {Object} data 任务数据
 * @returns {Promise} 请求Promise
 */
export function createTask(data) {
  return request({
    url: '/api/collector/tasks',
    method: 'post',
    data
  })
}

/**
 * 更新任务
 * @param {number} id 任务ID
 * @param {Object} data 任务数据
 * @returns {Promise} 请求Promise
 */
export function updateTask(id, data) {
  return request({
    url: `/api/collector/tasks/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除任务
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function deleteTask(id) {
  return request({
    url: `/api/collector/tasks/${id}`,
    method: 'delete'
  })
}

/**
 * 执行任务
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function executeTask(id) {
  return request({
    url: `/api/collector/tasks/${id}/execute`,
    method: 'post'
  })
}

/**
 * 手动执行任务
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function manualExecuteTask(id) {
  return request({
    url: `/api/collector/tasks/${id}/manual-execute`,
    method: 'post'
  })
}

/**
 * 暂停任务
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function pauseTask(id) {
  return request({
    url: `/api/collector/tasks/${id}/pause`,
    method: 'post'
  })
}

/**
 * 恢复任务
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function resumeTask(id) {
  return request({
    url: `/api/collector/tasks/${id}/resume`,
    method: 'post'
  })
}

/**
 * 启用任务
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function enableTask(id) {
  return request({
    url: `/api/collector/tasks/${id}/enable`,
    method: 'post'
  })
}

/**
 * 禁用任务
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function disableTask(id) {
  return request({
    url: `/api/collector/tasks/${id}/disable`,
    method: 'post'
  })
}

/**
 * 更新任务状态
 * @param {number} id 任务ID
 * @param {Object} data 状态更新数据
 * @returns {Promise} 请求Promise
 */
export function updateTaskStatus(id, data) {
  return request({
    url: `/api/collector/tasks/${id}/status`,
    method: 'put',
    data
  })
}

/**
 * 获取任务统计信息
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function getTaskStatistics(id) {
  return request({
    url: `/api/collector/tasks/${id}/statistics`,
    method: 'get'
  })
}

/**
 * 获取任务执行历史
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function getTaskExecutionHistory(id) {
  return request({
    url: `/api/collector/tasks/${id}/execution-history`,
    method: 'get'
  })
}

/**
 * 获取任务执行日志
 * @param {number} id 任务ID
 * @param {Object} params 查询参数
 * @returns {Promise} 请求Promise
 */
export function getTaskExecutionLogs(id, params) {
  return request({
    url: `/api/collector/tasks/${id}/execution-logs`,
    method: 'get',
    params
  })
}

/**
 * 获取任务概览统计
 * @returns {Promise} 请求Promise
 */
export function getTaskOverviewStatistics() {
  return request({
    url: '/api/collector/tasks/overview-statistics',
    method: 'get'
  })
}

/**
 * 获取任务性能排名
 * @returns {Promise} 请求Promise
 */
export function getTaskPerformanceRanking() {
  return request({
    url: '/api/collector/tasks/performance-ranking',
    method: 'get'
  })
}

/**
 * 批量更新任务状态
 * @param {Object} data 批量操作数据
 * @returns {Promise} 请求Promise
 */
export function batchUpdateTaskStatus(data) {
  return request({
    url: '/api/collector/tasks/batch-update-status',
    method: 'post',
    data
  })
}

/**
 * 批量启用任务
 * @param {Array} taskIds 任务ID数组
 * @returns {Promise} 请求Promise
 */
export function batchEnableTasks(taskIds) {
  return request({
    url: '/api/collector/tasks/batch-enable',
    method: 'post',
    data: { taskIds }
  })
}

/**
 * 批量禁用任务
 * @param {Array} taskIds 任务ID数组
 * @returns {Promise} 请求Promise
 */
export function batchDisableTasks(taskIds) {
  return request({
    url: '/api/collector/tasks/batch-disable',
    method: 'post',
    data: { taskIds }
  })
}

/**
 * 验证任务配置
 * @param {Object} data 任务配置数据
 * @returns {Promise} 请求Promise
 */
export function validateTaskConfig(data) {
  return request({
    url: '/api/collector/tasks/validate',
    method: 'post',
    data
  })
}

/**
 * 测试任务执行
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function testTaskExecution(id) {
  return request({
    url: `/api/collector/tasks/${id}/test`,
    method: 'post'
  })
}

/**
 * 获取任务调度信息
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function getTaskScheduleInfo(id) {
  return request({
    url: `/api/collector/tasks/${id}/schedule-info`,
    method: 'get'
  })
}

/**
 * 清理过期任务数据
 * @returns {Promise} 请求Promise
 */
export function cleanupExpiredTaskData() {
  return request({
    url: '/api/collector/tasks/cleanup',
    method: 'post'
  })
}

/**
 * 获取任务执行状态
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function getTaskExecutionStatus(id) {
  return request({
    url: `/api/collector/tasks/${id}/execution-status`,
    method: 'get'
  })
}

/**
 * 获取可用设备列表
 * @param {Object} params 查询参数
 * @returns {Promise} 请求Promise
 */
export function getAvailableDevices(params) {
  return request({
    url: '/api/devices',
    method: 'get',
    params
  })
}

/**
 * 获取可用指标模板
 * @returns {Promise} 请求Promise
 */
export function getMetricTemplates() {
  return request({
    url: '/api/collector/metrics/templates',
    method: 'get'
  })
}

/**
 * 保存任务草稿
 * @param {Object} data 任务草稿数据
 * @returns {Promise} 请求Promise
 */
export function saveTaskDraft(data) {
  return request({
    url: '/api/collector/tasks/draft',
    method: 'post',
    data
  })
}

/**
 * 获取任务草稿
 * @param {string} draftId 草稿ID
 * @returns {Promise} 请求Promise
 */
export function getTaskDraft(draftId) {
  return request({
    url: `/api/collector/tasks/draft/${draftId}`,
    method: 'get'
  })
}

/**
 * 批量操作任务（兼容旧接口）
 * @param {string} action 操作类型：start, pause, stop, delete
 * @param {Array} ids 任务ID数组
 * @returns {Promise} 请求Promise
 */
export function batchTaskAction(action, ids) {
  switch (action) {
    case 'start':
      return batchEnableTasks(ids)
    case 'pause':
      return batchUpdateTaskStatus({ taskIds: ids, status: 0, reason: '批量暂停' })
    case 'stop':
      return batchDisableTasks(ids)
    case 'delete':
      // 批量删除需要逐个调用删除接口
      return Promise.all(ids.map(id => deleteTask(id)))
    default:
      return Promise.reject(new Error(`不支持的操作类型: ${action}`))
  }
}

// 兼容旧接口名称
export const startTask = enableTask
export const stopTask = disableTask
export const getTaskStats = getTaskOverviewStatistics 