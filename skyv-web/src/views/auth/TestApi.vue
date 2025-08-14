<template>
  <div class="test-api-container">
    <div class="test-header">
      <h2>API接口测试</h2>
      <p>测试前后端接口对接情况</p>
    </div>

    <div class="test-sections">
      <!-- 登录测试 -->
      <el-card class="test-card" header="登录测试">
        <el-form :model="loginForm" label-width="100px">
          <el-form-item label="用户名">
            <el-input v-model="loginForm.username" placeholder="输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="loginForm.password" type="password" placeholder="输入密码" />
          </el-form-item>
          <el-form-item label="记住我">
            <el-checkbox v-model="loginForm.rememberMe" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loginLoading" @click="testLogin">
              测试登录
            </el-button>
            <el-button @click="testLogout">测试登出</el-button>
          </el-form-item>
        </el-form>
        <div v-if="loginResult" class="result-box">
          <h4>登录结果:</h4>
          <pre>{{ JSON.stringify(loginResult, null, 2) }}</pre>
        </div>
      </el-card>

      <!-- API接口测试 -->
      <el-card class="test-card" header="API接口测试">
        <el-space wrap>
          <el-button @click="testPublicApi" :loading="apiLoading.public">
            测试公开接口
          </el-button>
          <el-button @click="testProtectedApi" :loading="apiLoading.protected">
            测试受保护接口
          </el-button>
          <el-button @click="testAdminApi" :loading="apiLoading.admin">
            测试管理员接口
          </el-button>
          <el-button @click="testUserInfoApi" :loading="apiLoading.userInfo">
            测试用户信息接口
          </el-button>
          <el-button @click="testCheckToken" :loading="apiLoading.check">
            检查Token有效性
          </el-button>
          <el-button @click="testRefreshToken" :loading="apiLoading.refresh">
            测试Token刷新
          </el-button>
        </el-space>
        <div v-if="apiResult" class="result-box">
          <h4>API测试结果:</h4>
          <pre>{{ JSON.stringify(apiResult, null, 2) }}</pre>
        </div>
      </el-card>

      <!-- 用户状态 -->
      <el-card class="test-card" header="用户状态">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="登录状态">
            <el-tag :type="userStore.isLoggedIn ? 'success' : 'danger'">
              {{ userStore.isLoggedIn ? '已登录' : '未登录' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ userStore.username || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="真实姓名">
            {{ userStore.realName || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ userStore.email || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="是否管理员">
            <el-tag :type="userStore.isAdminUser ? 'success' : 'info'">
              {{ userStore.isAdminUser ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag v-for="role in userStore.roles" :key="role" style="margin-right: 5px;">
              {{ role }}
            </el-tag>
            <span v-if="!userStore.roles.length">无</span>
          </el-descriptions-item>
          <el-descriptions-item label="权限数量">
            {{ userStore.permissions.length }}
          </el-descriptions-item>
          <el-descriptions-item label="最后登录时间">
            {{ userStore.lastLoginTime || '未设置' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;">
          <el-button @click="refreshUserInfo" :loading="refreshing">
            刷新用户信息
          </el-button>
          <el-button @click="clearUserState" type="danger">
            清除用户状态
          </el-button>
        </div>
      </el-card>

      <!-- Token信息 -->
      <el-card class="test-card" header="Token信息">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="访问Token">
            <div class="token-display">
              {{ accessTokenDisplay }}
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="刷新Token">
            <div class="token-display">
              {{ refreshTokenDisplay }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 日志输出 -->
      <el-card class="test-card" header="日志输出">
        <div class="log-container" ref="logContainer">
          <div v-for="(log, index) in logs" :key="index" class="log-item">
            <span class="log-time">{{ log.time }}</span>
            <span :class="['log-level', `log-${log.level}`]">{{ log.level.toUpperCase() }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
        <el-button @click="clearLogs" size="small" style="margin-top: 10px;">
          清除日志
        </el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import { 
  testPublicApi as testPublicApiCall,
  testProtectedApi as testProtectedApiCall,
  testAdminApi as testAdminApiCall,
  getUserInfoTest,
  checkToken,
  refreshToken
} from '@/api/auth'
import { getAccessToken, getRefreshToken } from '@/utils/auth'

// 用户状态
const userStore = useUserStore()

// 登录表单
const loginForm = reactive({
  username: 'admin',
  password: 'admin123456',
  rememberMe: false
})

// 加载状态
const loginLoading = ref(false)
const refreshing = ref(false)
const apiLoading = reactive({
  public: false,
  protected: false,
  admin: false,
  userInfo: false,
  check: false,
  refresh: false
})

// 测试结果
const loginResult = ref(null)
const apiResult = ref(null)

// 日志
const logs = ref([])
const logContainer = ref(null)

// Token显示
const accessTokenDisplay = computed(() => {
  const token = getAccessToken()
  return token ? `${token.substring(0, 20)}...` : '无'
})

const refreshTokenDisplay = computed(() => {
  const token = getRefreshToken()
  return token ? `${token.substring(0, 20)}...` : '无'
})

// 添加日志
const addLog = (level, message) => {
  logs.value.push({
    time: new Date().toLocaleTimeString(),
    level,
    message
  })
  
  // 自动滚动到底部
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
}

// 测试登录
const testLogin = async () => {
  loginLoading.value = true
  loginResult.value = null
  
  try {
    addLog('info', `尝试登录: ${loginForm.username}`)
    
    const result = await userStore.login(loginForm)
    loginResult.value = result
    
    addLog('success', '登录成功')
    ElMessage.success('登录成功')
    
    // 获取用户信息
    await userStore.getInfo()
    addLog('info', '用户信息获取成功')
    
  } catch (error) {
    addLog('error', `登录失败: ${error.message}`)
    ElMessage.error(error.message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

// 测试登出
const testLogout = async () => {
  try {
    addLog('info', '尝试登出')
    
    await userStore.logout()
    loginResult.value = null
    apiResult.value = null
    
    addLog('success', '登出成功')
    ElMessage.success('登出成功')
  } catch (error) {
    addLog('error', `登出失败: ${error.message}`)
    ElMessage.error(error.message || '登出失败')
  }
}

// 测试公开接口
const testPublicApi = async () => {
  apiLoading.public = true
  
  try {
    addLog('info', '测试公开接口')
    
    const result = await testPublicApiCall()
    apiResult.value = result
    
    addLog('success', '公开接口测试成功')
    ElMessage.success('公开接口测试成功')
  } catch (error) {
    addLog('error', `公开接口测试失败: ${error.message}`)
    ElMessage.error(error.message || '公开接口测试失败')
  } finally {
    apiLoading.public = false
  }
}

// 测试受保护接口
const testProtectedApi = async () => {
  apiLoading.protected = true
  
  try {
    addLog('info', '测试受保护接口')
    
    const result = await testProtectedApiCall()
    apiResult.value = result
    
    addLog('success', '受保护接口测试成功')
    ElMessage.success('受保护接口测试成功')
  } catch (error) {
    addLog('error', `受保护接口测试失败: ${error.message}`)
    ElMessage.error(error.message || '受保护接口测试失败')
  } finally {
    apiLoading.protected = false
  }
}

// 测试管理员接口
const testAdminApi = async () => {
  apiLoading.admin = true
  
  try {
    addLog('info', '测试管理员接口')
    
    const result = await testAdminApiCall()
    apiResult.value = result
    
    addLog('success', '管理员接口测试成功')
    ElMessage.success('管理员接口测试成功')
  } catch (error) {
    addLog('error', `管理员接口测试失败: ${error.message}`)
    ElMessage.error(error.message || '管理员接口测试失败')
  } finally {
    apiLoading.admin = false
  }
}

// 测试用户信息接口
const testUserInfoApi = async () => {
  apiLoading.userInfo = true
  
  try {
    addLog('info', '测试用户信息接口')
    
    const result = await getUserInfoTest()
    apiResult.value = result
    
    addLog('success', '用户信息接口测试成功')
    ElMessage.success('用户信息接口测试成功')
  } catch (error) {
    addLog('error', `用户信息接口测试失败: ${error.message}`)
    ElMessage.error(error.message || '用户信息接口测试失败')
  } finally {
    apiLoading.userInfo = false
  }
}

// 检查Token有效性
const testCheckToken = async () => {
  apiLoading.check = true
  
  try {
    addLog('info', '检查Token有效性')
    
    const result = await checkToken()
    apiResult.value = result
    
    addLog('success', 'Token有效')
    ElMessage.success('Token有效')
  } catch (error) {
    addLog('error', `Token检查失败: ${error.message}`)
    ElMessage.error(error.message || 'Token检查失败')
  } finally {
    apiLoading.check = false
  }
}

// 测试Token刷新
const testRefreshToken = async () => {
  apiLoading.refresh = true
  
  try {
    addLog('info', '测试Token刷新')
    
    const result = await userStore.refreshToken()
    apiResult.value = result
    
    addLog('success', 'Token刷新成功')
    ElMessage.success('Token刷新成功')
  } catch (error) {
    addLog('error', `Token刷新失败: ${error.message}`)
    ElMessage.error(error.message || 'Token刷新失败')
  } finally {
    apiLoading.refresh = false
  }
}

// 刷新用户信息
const refreshUserInfo = async () => {
  refreshing.value = true
  
  try {
    addLog('info', '刷新用户信息')
    
    await userStore.getInfo()
    
    addLog('success', '用户信息刷新成功')
    ElMessage.success('用户信息刷新成功')
  } catch (error) {
    addLog('error', `用户信息刷新失败: ${error.message}`)
    ElMessage.error(error.message || '用户信息刷新失败')
  } finally {
    refreshing.value = false
  }
}

// 清除用户状态
const clearUserState = () => {
  userStore.resetState()
  loginResult.value = null
  apiResult.value = null
  
  addLog('info', '用户状态已清除')
  ElMessage.info('用户状态已清除')
}

// 清除日志
const clearLogs = () => {
  logs.value = []
}

// 初始化
const init = () => {
  addLog('info', 'API测试页面已加载')
  
  // 如果已登录，尝试获取用户信息
  if (userStore.isLoggedIn) {
    userStore.initializeFromStorage()
    addLog('info', '从本地存储恢复用户状态')
  }
}

// 页面加载时初始化
init()
</script>

<style lang="scss" scoped>
.test-api-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    color: #333;
    margin-bottom: 10px;
  }
  
  p {
    color: #666;
    font-size: 14px;
  }
}

.test-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.test-card {
  .result-box {
    margin-top: 20px;
    padding: 15px;
    background-color: #f5f5f5;
    border-radius: 4px;
    border: 1px solid #e0e0e0;
    
    h4 {
      margin: 0 0 10px 0;
      color: #333;
      font-size: 14px;
    }
    
    pre {
      margin: 0;
      font-size: 12px;
      line-height: 1.4;
      color: #666;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
}

.token-display {
  font-family: monospace;
  font-size: 12px;
  color: #666;
  word-break: break-all;
}

.log-container {
  height: 300px;
  overflow-y: auto;
  background-color: #1e1e1e;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
}

.log-item {
  display: flex;
  margin-bottom: 5px;
  
  .log-time {
    color: #888;
    margin-right: 10px;
    min-width: 80px;
  }
  
  .log-level {
    margin-right: 10px;
    min-width: 60px;
    font-weight: bold;
    
    &.log-info {
      color: #17a2b8;
    }
    
    &.log-success {
      color: #28a745;
    }
    
    &.log-error {
      color: #dc3545;
    }
    
    &.log-warn {
      color: #ffc107;
    }
  }
  
  .log-message {
    color: #f8f9fa;
    flex: 1;
  }
}
</style> 