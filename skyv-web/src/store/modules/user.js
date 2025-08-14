import { defineStore } from 'pinia'
import { login, logout, getCurrentUser, getUserInfoTest } from '@/api/auth'
import { 
  getAccessToken, 
  setAccessToken, 
  removeAccessToken,
  getRefreshToken,
  setRefreshToken,
  removeRefreshToken,
  getUserInfo,
  setUserInfo,
  removeUserInfo,
  clearAuth
} from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    // Token信息
    accessToken: getAccessToken(),
    refreshToken: getRefreshToken(),
    
    // 用户基本信息
    userId: null,
    username: '',
    realName: '',
    email: '',
    phone: '',
    avatar: '',
    isAdmin: false,
    
    // 权限信息
    roles: [],
    permissions: [],
    
    // 登录信息
    lastLoginTime: null,
    lastLoginIp: null
  }),
  
  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.accessToken,
    
    // 是否为管理员
    isAdminUser: (state) => state.isAdmin || state.roles.includes('ADMIN'),
    
    // 获取用户显示名称
    displayName: (state) => state.realName || state.username,
    
    // 检查是否有指定权限
    hasPermission: (state) => (permission) => {
      if (!permission || !state.permissions.length) return false
      if (Array.isArray(permission)) {
        return permission.some(p => state.permissions.includes(p))
      }
      return state.permissions.includes(permission)
    },
    
    // 检查是否有指定角色
    hasRole: (state) => (role) => {
      if (!role || !state.roles.length) return false
      if (Array.isArray(role)) {
        return role.some(r => state.roles.includes(r))
      }
      return state.roles.includes(role)
    }
  },
  
  actions: {
    // 登录
    async login(loginData) {
      try {
        console.log('Attempting login with:', { username: loginData.username })
        
        const response = await login(loginData)
        console.log('Login response:', response)
        
        if (response.code === 200 && response.data) {
          const { accessToken, refreshToken, userInfo } = response.data
          
          // 保存Token
          this.accessToken = accessToken
          this.refreshToken = refreshToken
          setAccessToken(accessToken)
          setRefreshToken(refreshToken)
          
          // 保存用户信息
          this.setUserData(userInfo)
          setUserInfo(userInfo)
          
          console.log('Login successful, user info:', userInfo)
          return response.data
        } else {
          throw new Error(response.message || '登录失败')
        }
      } catch (error) {
        console.error('Login failed:', error)
        this.resetState()
        throw error
      }
    },
    
    // 设置用户数据
    setUserData(userInfo) {
      if (!userInfo) return
      
      this.userId = userInfo.id
      this.username = userInfo.username
      this.realName = userInfo.realName || ''
      this.email = userInfo.email || ''
      this.phone = userInfo.phone || ''
      this.avatar = userInfo.avatar || ''
      this.isAdmin = userInfo.isAdmin || false
      this.roles = userInfo.roles || []
      this.permissions = userInfo.permissions || []
      this.lastLoginTime = userInfo.lastLoginTime
      this.lastLoginIp = userInfo.lastLoginIp
    },
    
    // 获取用户信息（从后端获取最新信息）
    async getInfo() {
      try {
        // 优先使用测试接口获取用户信息
        const response = await getUserInfoTest()
        console.log('Get user info response:', response)
        
        if (response.code === 200 && response.data) {
          // 从测试接口返回的数据中提取用户信息
          const userInfo = {
            id: this.userId,
            username: response.data.username,
            realName: this.realName,
            email: this.email,
            phone: this.phone,
            avatar: this.avatar,
            isAdmin: this.isAdmin,
            roles: this.roles,
            permissions: this.permissions,
            lastLoginTime: this.lastLoginTime,
            lastLoginIp: this.lastLoginIp
          }
          
          this.setUserData(userInfo)
          setUserInfo(userInfo)
          
          return userInfo
        } else {
          throw new Error(response.message || '获取用户信息失败')
        }
      } catch (error) {
        console.error('Get user info failed:', error)
        // 如果获取用户信息失败，尝试从本地存储恢复
        const localUserInfo = getUserInfo()
        if (localUserInfo) {
          this.setUserData(localUserInfo)
          return localUserInfo
        }
        throw error
      }
    },
    
    // 登出
    async logout() {
      try {
        console.log('Attempting logout...')
        
        // 调用后端登出接口
        await logout()
        console.log('Logout API call successful')
      } catch (error) {
        console.error('Logout API failed:', error)
        // 即使后端登出失败，也要清除本地状态
      } finally {
        // 清除本地状态和存储
        this.resetState()
        clearAuth()
        console.log('Logout completed, local state cleared')
      }
    },
    
    // 刷新Token
    async refreshToken() {
      try {
        if (!this.refreshToken) {
          throw new Error('No refresh token available')
        }
        
        const response = await refreshToken({ refreshToken: this.refreshToken })
        
        if (response.code === 200 && response.data) {
          const { accessToken, refreshToken: newRefreshToken, userInfo } = response.data
          
          // 更新Token
          this.accessToken = accessToken
          this.refreshToken = newRefreshToken
          setAccessToken(accessToken)
          setRefreshToken(newRefreshToken)
          
          // 更新用户信息
          if (userInfo) {
            this.setUserData(userInfo)
            setUserInfo(userInfo)
          }
          
          return response.data
        } else {
          throw new Error(response.message || 'Token刷新失败')
        }
      } catch (error) {
        console.error('Refresh token failed:', error)
        // Token刷新失败，清除所有状态
        this.resetState()
        clearAuth()
        throw error
      }
    },
    
    // 初始化用户状态（从本地存储恢复）
    initializeFromStorage() {
      const localUserInfo = getUserInfo()
      if (localUserInfo && this.accessToken) {
        this.setUserData(localUserInfo)
        console.log('User state initialized from storage:', localUserInfo)
      }
    },
    
    // 重置状态
    resetState() {
      this.accessToken = ''
      this.refreshToken = ''
      this.userId = null
      this.username = ''
      this.realName = ''
      this.email = ''
      this.phone = ''
      this.avatar = ''
      this.isAdmin = false
      this.roles = []
      this.permissions = []
      this.lastLoginTime = null
      this.lastLoginIp = null
    }
  }
}) 