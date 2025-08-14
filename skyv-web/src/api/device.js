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
 * 获取所有设备（不分页）
 * @returns {Promise}
 */
export function getAllDevices() {
  return request({
    url: '/api/devices/all',
    method: 'get'
  })
}

/**
 * 根据ID获取设备详情
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function getDeviceById(id) {
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
 * 批量删除设备
 * @param {Array} ids 设备ID数组
 * @returns {Promise}
 */
export function batchDeleteDevices(ids) {
  return request({
    url: '/api/devices/batch',
    method: 'delete',
    data: { ids }
  })
}

/**
 * 设备连接测试
 * @param {Number} id 设备ID
 * @returns {Promise}
 */
export function testDeviceConnection(id) {
  return request({
    url: `/api/devices/${id}/test`,
    method: 'post'
  })
}

/**
 * 获取设备统计数据
 * @returns {Promise}
 */
export function getDeviceStatsData() {
  return request({
    url: '/api/devices/stats',
    method: 'get'
  })
}

/**
 * 验证设备名称唯一性
 * @param {string} name 设备名称
 * @param {number} id 排除的设备ID
 * @returns {Promise}
 */
export function validateDeviceName(name, id = null) {
  return request({
    url: '/api/devices/validate/name',
    method: 'get',
    params: { name, id }
  })
}

/**
 * 验证设备IP地址唯一性
 * @param {string} ipAddress IP地址
 * @param {number} id 排除的设备ID
 * @returns {Promise}
 */
export function validateDeviceIp(ipAddress, id = null) {
  return request({
    url: '/api/devices/validate/ip',
    method: 'get',
    params: { ipAddress, id }
  })
}

// ==================== 设备类型管理 ====================

/**
 * 获取设备类型树形列表
 * @returns {Promise}
 */
export function getDeviceTypeTree() {
  return request({
    url: '/api/device-types/tree',
    method: 'get'
  })
}

/**
 * 获取设备类型列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getDeviceTypes(params) {
  return request({
    url: '/api/device-types',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取设备类型详情
 * @param {Number} id 设备类型ID
 * @returns {Promise}
 */
export function getDeviceTypeById(id) {
  return request({
    url: `/api/device-types/${id}`,
    method: 'get'
  })
}

/**
 * 创建设备类型
 * @param {Object} data 设备类型数据
 * @returns {Promise}
 */
export function createDeviceType(data) {
  return request({
    url: '/api/device-types',
    method: 'post',
    data
  })
}

/**
 * 更新设备类型
 * @param {Number} id 设备类型ID
 * @param {Object} data 设备类型数据
 * @returns {Promise}
 */
export function updateDeviceType(id, data) {
  return request({
    url: `/api/device-types/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除设备类型
 * @param {Number} id 设备类型ID
 * @returns {Promise}
 */
export function deleteDeviceType(id) {
  return request({
    url: `/api/device-types/${id}`,
    method: 'delete'
  })
}

// ==================== 设备分组管理 ====================

/**
 * 获取设备分组列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getDeviceGroups(params) {
  return request({
    url: '/api/devices/groups',
    method: 'get',
    params
  })
}

// ==================== 设备分组（新接口） ====================

/**
 * 获取所有设备分组
 * @returns {Promise}
 */
export function getAllDeviceGroups() {
  return request({
    url: '/api/device-groups',
    method: 'get'
  })
}

/**
 * 创建设备分组
 * @param {Object} data 分组数据
 * @returns {Promise}
 */
export function createDeviceGroup(data) {
  return request({
    url: '/api/device-groups',
    method: 'post',
    data
  })
}

/**
 * 更新设备分组
 * @param {number} id 分组ID
 * @param {Object} data 分组数据
 * @returns {Promise}
 */
export function updateDeviceGroup(id, data) {
  return request({
    url: `/api/device-groups/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除设备分组
 * @param {number} id 分组ID
 * @returns {Promise}
 */
export function deleteDeviceGroup(id) {
  return request({
    url: `/api/device-groups/${id}`,
    method: 'delete'
  })
}

/**
 * 检查设备分组名称是否存在
 * @param {string} name 分组名称
 * @param {number|null} excludeId 排除的分组ID
 * @returns {Promise}
 */
export function checkDeviceGroupNameExists(name, excludeId = null) {
  return request({
    url: '/api/device-groups/check/name',
    method: 'get',
    params: { name, excludeId }
  })
}

// ==================== 设备区域管理 ====================

/**
 * 获取设备区域列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getDeviceAreas(params) {
  return request({
    url: '/api/device-areas',
    method: 'get',
    params
  })
}

/**
 * 获取设备区域树形结构
 * @returns {Promise}
 */
export function getDeviceAreaTree() {
  return request({
    url: '/api/device-areas/tree',
    method: 'get'
  })
}

/**
 * 根据ID获取设备区域详情
 * @param {Number} id 区域ID
 * @returns {Promise}
 */
export function getDeviceAreaById(id) {
  return request({
    url: `/api/device-areas/${id}`,
    method: 'get'
  })
}

/**
 * 创建设备区域
 * @param {Object} data 区域数据
 * @returns {Promise}
 */
export function createDeviceArea(data) {
  return request({
    url: '/api/device-areas',
    method: 'post',
    data
  })
}

/**
 * 更新设备区域
 * @param {Number} id 区域ID
 * @param {Object} data 区域数据
 * @returns {Promise}
 */
export function updateDeviceArea(id, data) {
  return request({
    url: `/api/device-areas/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除设备区域
 * @param {Number} id 区域ID
 * @returns {Promise}
 */
export function deleteDeviceArea(id) {
  return request({
    url: `/api/device-areas/${id}`,
    method: 'delete'
  })
}

/**
 * 验证区域名称唯一性
 * @param {string} name 区域名称
 * @param {number} id 排除的区域ID
 * @returns {Promise}
 */
export function validateAreaName(name, id = null) {
  return request({
    url: '/api/device-areas/validate/name',
    method: 'get',
    params: { name, id }
  })
}

/**
 * 验证区域编码唯一性
 * @param {string} code 区域编码
 * @param {number} id 排除的区域ID
 * @returns {Promise}
 */
export function validateAreaCode(code, id = null) {
  return request({
    url: '/api/device-areas/validate/code',
    method: 'get',
    params: { code, id }
  })
}

// ==================== 设备标签管理 ====================

/**
 * 获取设备标签列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getDeviceTags(params) {
  return request({
    url: '/api/device-tags',
    method: 'get',
    params
  })
}

/**
 * 获取所有设备标签
 * @returns {Promise}
 */
export function getAllDeviceTags() {
  return request({
    url: '/api/device-tags/all',
    method: 'get'
  })
}

/**
 * 获取热门标签
 * @returns {Promise}
 */
export function getPopularTags() {
  return request({
    url: '/api/device-tags/popular',
    method: 'get'
  })
}

/**
 * 根据ID获取设备标签详情
 * @param {Number} id 标签ID
 * @returns {Promise}
 */
export function getDeviceTagById(id) {
  return request({
    url: `/api/device-tags/${id}`,
    method: 'get'
  })
}

/**
 * 创建设备标签
 * @param {Object} data 标签数据
 * @returns {Promise}
 */
export function createDeviceTag(data) {
  return request({
    url: '/api/device-tags',
    method: 'post',
    data
  })
}

/**
 * 更新设备标签
 * @param {Number} id 标签ID
 * @param {Object} data 标签数据
 * @returns {Promise}
 */
export function updateDeviceTag(id, data) {
  return request({
    url: `/api/device-tags/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除设备标签
 * @param {Number} id 标签ID
 * @returns {Promise}
 */
export function deleteDeviceTag(id) {
  return request({
    url: `/api/device-tags/${id}`,
    method: 'delete'
  })
}

/**
 * 验证标签名称唯一性
 * @param {string} name 标签名称
 * @param {number} id 排除的标签ID
 * @returns {Promise}
 */
export function validateTagName(name, id = null) {
  return request({
    url: '/api/device-tags/validate/name',
    method: 'get',
    params: { name, id }
  })
}

// ==================== 设备模板管理 ====================

/**
 * 获取设备模板列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getDeviceTemplates(params) {
  return request({
    url: '/api/device-templates',
    method: 'get',
    params
  })
}

/**
 * 获取所有启用的设备模板
 * @returns {Promise}
 */
export function getAllEnabledTemplates() {
  return request({
    url: '/api/device-templates/enabled',
    method: 'get'
  })
}

/**
 * 根据设备类型获取模板
 * @param {Number} deviceTypeId 设备类型ID
 * @returns {Promise}
 */
export function getTemplatesByDeviceType(deviceTypeId) {
  return request({
    url: `/api/device-templates/by-device-type/${deviceTypeId}`,
    method: 'get'
  })
}

/**
 * 根据协议获取模板
 * @param {Number} protocolId 协议ID
 * @returns {Promise}
 */
export function getTemplatesByProtocol(protocolId) {
  return request({
    url: `/api/device-templates/by-protocol/${protocolId}`,
    method: 'get'
  })
}

/**
 * 根据ID获取设备模板详情
 * @param {Number} id 模板ID
 * @returns {Promise}
 */
export function getDeviceTemplateById(id) {
  return request({
    url: `/api/device-templates/${id}`,
    method: 'get'
  })
}

/**
 * 创建设备模板
 * @param {Object} data 模板数据
 * @returns {Promise}
 */
export function createDeviceTemplate(data) {
  return request({
    url: '/api/device-templates',
    method: 'post',
    data
  })
}

/**
 * 更新设备模板
 * @param {Number} id 模板ID
 * @param {Object} data 模板数据
 * @returns {Promise}
 */
export function updateDeviceTemplate(id, data) {
  return request({
    url: `/api/device-templates/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除设备模板
 * @param {Number} id 模板ID
 * @returns {Promise}
 */
export function deleteDeviceTemplate(id) {
  return request({
    url: `/api/device-templates/${id}`,
    method: 'delete'
  })
}

/**
 * 验证模板编码唯一性
 * @param {string} code 模板编码
 * @param {number} id 排除的模板ID
 * @returns {Promise}
 */
export function validateTemplateCode(code, id = null) {
  return request({
    url: '/api/device-templates/validate/code',
    method: 'get',
    params: { code, id }
  })
}

/**
 * 验证模板名称唯一性
 * @param {string} name 模板名称
 * @param {number} id 排除的模板ID
 * @returns {Promise}
 */
export function validateTemplateName(name, id = null) {
  return request({
    url: '/api/device-templates/validate/name',
    method: 'get',
    params: { name, id }
  })
}

// ==================== 设备协议管理 ====================

/**
 * 获取设备协议列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getDeviceProtocols(params) {
  return request({
    url: '/api/device-protocols',
    method: 'get',
    params
  })
}

/**
 * 获取所有启用的设备协议
 * @returns {Promise}
 */
export function getAllEnabledProtocols() {
  return request({
    url: '/api/device-protocols/enabled',
    method: 'get'
  })
}

/**
 * 根据ID获取设备协议详情
 * @param {Number} id 协议ID
 * @returns {Promise}
 */
export function getDeviceProtocolById(id) {
  return request({
    url: `/api/device-protocols/${id}`,
    method: 'get'
  })
}

/**
 * 创建设备协议
 * @param {Object} data 协议数据
 * @returns {Promise}
 */
export function createDeviceProtocol(data) {
  return request({
    url: '/api/device-protocols',
    method: 'post',
    data
  })
}

/**
 * 更新设备协议
 * @param {Number} id 协议ID
 * @param {Object} data 协议数据
 * @returns {Promise}
 */
export function updateDeviceProtocol(id, data) {
  return request({
    url: `/api/device-protocols/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除设备协议
 * @param {Number} id 协议ID
 * @returns {Promise}
 */
export function deleteDeviceProtocol(id) {
  return request({
    url: `/api/device-protocols/${id}`,
    method: 'delete'
  })
}

/**
 * 验证协议编码唯一性
 * @param {string} code 协议编码
 * @param {number} id 排除的协议ID
 * @returns {Promise}
 */
export function validateProtocolCode(code, id = null) {
  return request({
    url: '/api/device-protocols/validate/code',
    method: 'get',
    params: { code, id }
  })
}

/**
 * 验证协议名称唯一性
 * @param {string} name 协议名称
 * @param {number} id 排除的协议ID
 * @returns {Promise}
 */
export function validateProtocolName(name, id = null) {
  return request({
    url: '/api/device-protocols/validate/name',
    method: 'get',
    params: { name, id }
  })
}