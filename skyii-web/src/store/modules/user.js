import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userId: null,
    username: '',
    name: '',
    avatar: '',
    roles: [],
    permissions: []
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.roles.includes('ADMIN')
  },
  
  actions: {
    // 登录
    async login(loginData) {
      try {
        const { data } = await login(loginData)
        const { token } = data
        this.token = token
        setToken(token)
        return data
      } catch (error) {
        console.error('Login failed:', error)
        throw error
      }
    },
    
    // 获取用户信息
    async getInfo() {
      try {
        const { data } = await getUserInfo()
        const { id, username, name, avatar, roles, permissions } = data
        
        this.userId = id
        this.username = username
        this.name = name
        this.avatar = avatar || ''
        this.roles = roles || []
        this.permissions = permissions || []
        
        return data
      } catch (error) {
        console.error('Get user info failed:', error)
        throw error
      }
    },
    
    // 登出
    async logout() {
      try {
        await logout()
      } catch (error) {
        console.error('Logout failed:', error)
      } finally {
        this.resetState()
        removeToken()
      }
    },
    
    // 重置状态
    resetState() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.name = ''
      this.avatar = ''
      this.roles = []
      this.permissions = []
    }
  }
}) 