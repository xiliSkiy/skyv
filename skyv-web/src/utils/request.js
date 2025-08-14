import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAccessToken, getRefreshToken, clearAuth } from '@/utils/auth'
import { refreshToken as refreshTokenApi } from '@/api/auth'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 是否正在刷新Token
let isRefreshing = false
// 失败队列
let failedQueue = []

// 处理队列
const processQueue = (error, token = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  
  failedQueue = []
}

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在请求发送前做一些处理
    const token = getAccessToken()
    if (token) {
      // 让每个请求携带token
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 打印请求信息（开发环境）
    if (import.meta.env.MODE === 'development') {
      console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`, {
        headers: config.headers,
        data: config.data,
        params: config.params
      })
    }
    
    return config
  },
  error => {
    // 处理请求错误
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 打印响应信息（开发环境）
    if (import.meta.env.MODE === 'development') {
      console.log(`✅ API Response: ${response.config.method?.toUpperCase()} ${response.config.url}`, {
        status: response.status,
        data: response.data
      })
    }
    
    const res = response.data
    
    // 如果返回的状态码是200，直接返回数据
    if (res.code === 200) {
      return res
    }
    
    // 处理业务错误
    let errorMessage = res.message || '请求失败'
    
    // 根据错误码显示不同的错误信息
    switch (res.code) {
      case 401:
        errorMessage = '未授权访问，请重新登录'
        break
      case 403:
        errorMessage = '权限不足，拒绝访问'
        break
      case 404:
        errorMessage = '请求的资源不存在'
        break
      case 500:
        errorMessage = '服务器内部错误'
        break
      case 608:
        errorMessage = '登录失败，用户名或密码错误'
        break
      case 609:
        errorMessage = 'Token已过期'
        break
      case 610:
        errorMessage = 'Token无效'
        break
    }
    
    // 显示错误消息
    ElMessage({
      message: errorMessage,
      type: 'error',
      duration: 5 * 1000
    })
    
    // 401错误特殊处理
    if (res.code === 401) {
      handleUnauthorized()
    }
    
    return Promise.reject(new Error(errorMessage))
  },
  async error => {
    console.error('Response error:', error)
    
    const { response, config } = error
    
    // 如果是401错误，尝试刷新Token
    if (response?.status === 401) {
      return handleTokenRefresh(config, error)
    }
    
    // 处理其他HTTP错误
    let message = '网络错误，请检查您的网络连接'
    
    if (response) {
      switch (response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 403:
          message = '权限不足，拒绝访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 408:
          message = '请求超时'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '服务器无响应'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = `请求失败: ${response.status}`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接失败，请检查网络设置'
    }
    
    // 显示错误消息
    ElMessage({
      message: message,
      type: 'error',
      duration: 5 * 1000
    })
    
    return Promise.reject(error)
  }
)

// 处理Token刷新
async function handleTokenRefresh(originalConfig, originalError) {
  const refreshTokenValue = getRefreshToken()
  
  if (!refreshTokenValue) {
    // 没有刷新Token，直接跳转到登录页
    handleUnauthorized()
    return Promise.reject(originalError)
  }
  
  if (isRefreshing) {
    // 正在刷新Token，将请求加入队列
    return new Promise((resolve, reject) => {
      failedQueue.push({ resolve, reject })
    }).then(token => {
      originalConfig.headers['Authorization'] = `Bearer ${token}`
      return service(originalConfig)
    }).catch(err => {
      return Promise.reject(err)
    })
  }
  
  isRefreshing = true
  
  try {
    console.log('Attempting to refresh token...')
    
    // 创建一个新的axios实例来刷新Token，避免拦截器循环
    const refreshService = axios.create({
      baseURL: service.defaults.baseURL,
      timeout: service.defaults.timeout
    })
    
    const response = await refreshService.post('/api/auth/refresh', {
      refreshToken: refreshTokenValue
    })
    
    if (response.data.code === 200) {
      const { accessToken, refreshToken: newRefreshToken } = response.data.data
      
      // 更新本地存储
      import('@/utils/auth').then(authUtils => {
        authUtils.setAccessToken(accessToken)
        authUtils.setRefreshToken(newRefreshToken)
      })
      
      // 更新用户状态
      import('@/store/modules/user').then(userModule => {
        const userStore = userModule.useUserStore()
        userStore.accessToken = accessToken
        userStore.refreshToken = newRefreshToken
      })
      
      console.log('Token refresh successful')
      
      // 处理队列中的请求
      processQueue(null, accessToken)
      
      // 重新发送原始请求
      originalConfig.headers['Authorization'] = `Bearer ${accessToken}`
      return service(originalConfig)
    } else {
      throw new Error(response.data.message || 'Token刷新失败')
    }
  } catch (refreshError) {
    console.error('Token refresh failed:', refreshError)
    
    // Token刷新失败，清除所有认证信息并跳转到登录页
    processQueue(refreshError, null)
    handleUnauthorized()
    
    return Promise.reject(originalError)
  } finally {
    isRefreshing = false
  }
}

// 处理未授权访问
function handleUnauthorized() {
  // 清除本地认证信息
  clearAuth()
  
  // 清除用户状态
  import('@/store/modules/user').then(userModule => {
    const userStore = userModule.useUserStore()
    userStore.resetState()
  })
  
  // 显示提示信息
  ElMessageBox.confirm(
    '登录状态已过期，请重新登录',
    '系统提示',
    {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 跳转到登录页，并保存当前页面路径
    const currentPath = router.currentRoute.value.fullPath
    router.push(`/login?redirect=${encodeURIComponent(currentPath)}`)
  }).catch(() => {
    // 用户取消，也跳转到登录页
    router.push('/login')
  })
}

export default service 