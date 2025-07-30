import request from '@/utils/request'

// 网络接口管理
export function getNetworkInterfaces() {
  return request({
    url: '/network/interfaces',
    method: 'get'
  })
}

export function updateNetworkInterface(id, data) {
  return request({
    url: `/network/interfaces/${id}`,
    method: 'put',
    data
  })
}

export function enableNetworkInterface(id) {
  return request({
    url: `/network/interfaces/${id}/enable`,
    method: 'post'
  })
}

export function disableNetworkInterface(id) {
  return request({
    url: `/network/interfaces/${id}/disable`,
    method: 'post'
  })
}

export function reconnectNetworkInterface(id) {
  return request({
    url: `/network/interfaces/${id}/reconnect`,
    method: 'post'
  })
}

export function addNetworkInterface(data) {
  return request({
    url: '/network/interfaces',
    method: 'post',
    data
  })
}

export function deleteNetworkInterface(id) {
  return request({
    url: `/network/interfaces/${id}`,
    method: 'delete'
  })
}

// DNS设置
export function getDnsSettings() {
  return request({
    url: '/network/dns',
    method: 'get'
  })
}

export function updateDnsSettings(data) {
  return request({
    url: '/network/dns',
    method: 'put',
    data
  })
}

// 代理设置
export function getProxySettings() {
  return request({
    url: '/network/proxy',
    method: 'get'
  })
}

export function updateProxySettings(data) {
  return request({
    url: '/network/proxy',
    method: 'put',
    data
  })
}

export function testProxyConnection(data) {
  return request({
    url: '/network/proxy/test',
    method: 'post',
    data
  })
}

// 防火墙设置
export function getFirewallSettings() {
  return request({
    url: '/network/firewall',
    method: 'get'
  })
}

export function updateFirewallSettings(data) {
  return request({
    url: '/network/firewall',
    method: 'put',
    data
  })
}

export function getFirewallRules() {
  return request({
    url: '/network/firewall/rules',
    method: 'get'
  })
}

export function addFirewallRule(data) {
  return request({
    url: '/network/firewall/rules',
    method: 'post',
    data
  })
}

export function updateFirewallRule(id, data) {
  return request({
    url: `/network/firewall/rules/${id}`,
    method: 'put',
    data
  })
}

export function deleteFirewallRule(id) {
  return request({
    url: `/network/firewall/rules/${id}`,
    method: 'delete'
  })
}

export function enableFirewallRule(id) {
  return request({
    url: `/network/firewall/rules/${id}/enable`,
    method: 'post'
  })
}

export function disableFirewallRule(id) {
  return request({
    url: `/network/firewall/rules/${id}/disable`,
    method: 'post'
  })
}

// 网络状态
export function getNetworkStatus() {
  return request({
    url: '/network/status',
    method: 'get'
  })
}

export function refreshNetworkStatus() {
  return request({
    url: '/network/status/refresh',
    method: 'post'
  })
}

export function getNetworkStats() {
  return request({
    url: '/network/stats',
    method: 'get'
  })
}

// 端口设置
export function getPortSettings() {
  return request({
    url: '/network/ports',
    method: 'get'
  })
}

export function updatePortSettings(data) {
  return request({
    url: '/network/ports',
    method: 'put',
    data
  })
}

// 网络诊断
export function runNetworkDiagnosis(data) {
  return request({
    url: '/network/diagnosis',
    method: 'post',
    data
  })
}

export function getNetworkDiagnosisHistory() {
  return request({
    url: '/network/diagnosis/history',
    method: 'get'
  })
}

export function exportNetworkDiagnosisReport(data) {
  return request({
    url: '/network/diagnosis/export',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

// 网络测速
export function runSpeedTest() {
  return request({
    url: '/network/speedtest',
    method: 'post'
  })
}

export function getSpeedTestHistory() {
  return request({
    url: '/network/speedtest/history',
    method: 'get'
  })
} 