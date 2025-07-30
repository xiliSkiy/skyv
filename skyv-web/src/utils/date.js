/**
 * 格式化日期时间
 * @param {String|Date|Array} dateTime 日期时间字符串、Date对象或LocalDateTime数组
 * @param {Object} options 格式化选项
 * @returns {String} 格式化后的日期时间字符串
 */
export function formatDateTime(dateTime, options = {}) {
  if (!dateTime) return '-'
  
  try {
    let parsedDate
    
    if (dateTime instanceof Date) {
      parsedDate = dateTime
    } else if (Array.isArray(dateTime)) {
      // 处理Java LocalDateTime数组格式 [year, month, day, hour, minute, second, nano]
      const [year, month, day, hour, minute, second] = dateTime
      // 注意：JavaScript月份是从0开始的，而Java是从1开始的
      parsedDate = new Date(year, month - 1, day, hour, minute, second)
    } else if (typeof dateTime === 'string') {
      // 处理Java LocalDateTime格式 (2023-11-20T10:30:45)
      // 或其他常见格式
      if (dateTime.includes('T')) {
        // ISO格式
        parsedDate = new Date(dateTime)
      } else if (dateTime.includes(' ')) {
        // 标准格式 (2023-11-20 10:30:45)
        parsedDate = new Date(dateTime.replace(' ', 'T'))
      } else {
        // 尝试直接解析
        parsedDate = new Date(dateTime)
      }
    } else {
      // 尝试处理时间戳
      parsedDate = new Date(dateTime)
    }
    
    // 检查日期是否有效
    if (isNaN(parsedDate.getTime())) {
      return String(dateTime)
    }
    
    const defaultOptions = {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }
    
    const formatOptions = { ...defaultOptions, ...options }
    
    return parsedDate.toLocaleString('zh-CN', formatOptions)
  } catch (error) {
    console.error('日期格式化错误:', error, dateTime)
    return String(dateTime)
  }
}

/**
 * 格式化日期
 * @param {String|Date} date 日期字符串或Date对象
 * @returns {String} 格式化后的日期字符串
 */
export function formatDate(date) {
  return formatDateTime(date, {
    hour: undefined,
    minute: undefined,
    second: undefined
  })
}

/**
 * 格式化时间
 * @param {String|Date} time 时间字符串或Date对象
 * @returns {String} 格式化后的时间字符串
 */
export function formatTime(time) {
  return formatDateTime(time, {
    year: undefined,
    month: undefined,
    day: undefined
  })
}

/**
 * 获取相对时间描述
 * @param {String|Date} dateTime 日期时间字符串或Date对象
 * @returns {String} 相对时间描述
 */
export function getRelativeTime(dateTime) {
  if (!dateTime) return '-'
  
  try {
    const parsedDate = dateTime instanceof Date ? dateTime : new Date(dateTime)
    const now = new Date()
    const diffMs = now - parsedDate
    const diffSec = Math.floor(diffMs / 1000)
    
    if (diffSec < 60) {
      return '刚刚'
    } else if (diffSec < 3600) {
      return `${Math.floor(diffSec / 60)}分钟前`
    } else if (diffSec < 86400) {
      return `${Math.floor(diffSec / 3600)}小时前`
    } else if (diffSec < 604800) {
      return `${Math.floor(diffSec / 86400)}天前`
    } else {
      return formatDateTime(parsedDate)
    }
  } catch (error) {
    console.error('相对时间计算错误:', error)
    return dateTime
  }
} 