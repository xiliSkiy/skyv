import request from '@/utils/request'

/**
 * 获取任务列表
 * @param {Object} params 查询参数
 * @returns {Promise} 请求Promise
 */
export function getTaskList(params) {
  return request({
    url: '/api/tasks',
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
    url: `/api/tasks/${id}`,
    method: 'get'
  })
}

/**
 * 创建任务
 * @param {Object} data 任务数据
 * @returns {Promise} 请求Promise
 */
export function createTask(data) {
  return request({
    url: '/api/tasks',
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
    url: `/api/tasks/${id}`,
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
    url: `/api/tasks/${id}`,
    method: 'delete'
  })
}

/**
 * 启动任务
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function startTask(id) {
  return request({
    url: `/api/tasks/${id}/start`,
    method: 'put'
  })
}

/**
 * 暂停任务
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function pauseTask(id) {
  return request({
    url: `/api/tasks/${id}/pause`,
    method: 'put'
  })
}

/**
 * 停止任务
 * @param {number} id 任务ID
 * @returns {Promise} 请求Promise
 */
export function stopTask(id) {
  return request({
    url: `/api/tasks/${id}/stop`,
    method: 'put'
  })
}

/**
 * 获取任务统计数据
 * @returns {Promise} 请求Promise
 */
export function getTaskStats() {
  return request({
    url: '/api/tasks/stats',
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
    url: '/api/tasks/devices',
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
    url: '/api/tasks/metric-templates',
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
    url: '/api/tasks/draft',
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
    url: `/api/tasks/draft/${draftId}`,
    method: 'get'
  })
}

/**
 * 批量操作任务
 * @param {string} action 操作类型：start, pause, stop, delete
 * @param {Array} ids 任务ID数组
 * @returns {Promise} 请求Promise
 */
export function batchTaskAction(action, ids) {
  return request({
    url: `/api/tasks/batch/${action}`,
    method: 'put',
    data: { ids }
  })
} 