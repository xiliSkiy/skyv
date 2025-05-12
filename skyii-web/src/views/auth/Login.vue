<template>
  <div class="login-container">
    <div class="login-sidebar">
      <div class="logo">
        <el-icon><Monitor /></el-icon> SkyEye 智能监控
      </div>
      <h4 class="mt-4">智能化监控新体验</h4>
      <p class="text-light opacity-75">先进的AI识别，全方位保障安全</p>
      
      <div class="feature-list">
        <div class="feature-item">
          <div class="feature-icon">
            <el-icon><VideoCamera /></el-icon>
          </div>
          <div>实时视频分析</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">
            <el-icon><Bell /></el-icon>
          </div>
          <div>智能报警提醒</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div>数据统计分析</div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">
            <el-icon><Iphone /></el-icon>
          </div>
          <div>移动端远程控制</div>
        </div>
      </div>
    </div>
    <div class="login-form">
      <h2 class="login-title">登录系统</h2>
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <div class="remember-forgot">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <el-button type="text" @click="toForgotPassword">忘记密码?</el-button>
        </div>
        <el-form-item>
          <el-button type="primary" class="login-button" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="copyright">
        <p>© 2023 SkyEye 智能监控系统 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

// 表单引用
const loginFormRef = ref(null)

// 路由实例
const router = useRouter()

// 用户状态
const userStore = useUserStore()

// 加载状态
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 调用登录接口
      await userStore.login(loginForm)
      
      // 获取用户信息
      await userStore.getInfo()
      
      ElMessage({
        message: '登录成功',
        type: 'success'
      })
      
      // 跳转到首页或者重定向页面
      const redirect = router.currentRoute.value.query.redirect || '/'
      router.push(redirect)
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}

// 跳转到忘记密码页面
const toForgotPassword = () => {
  router.push('/forgot-password')
}
</script>

<style lang="scss" scoped>
.login-container {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-container {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 15px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  width: 900px;
  display: flex;
}

.login-sidebar {
  background: linear-gradient(to bottom, #1a237e, #283593);
  color: white;
  padding: 40px;
  width: 40%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-form {
  width: 60%;
  padding: 40px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  
  .el-icon {
    font-size: 28px;
  }
}

.login-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #333;
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.login-button {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  border: none;
  padding: 12px;
  width: 100%;
  font-weight: 600;
  font-size: 16px;
  height: 48px;
}

.feature-list {
  margin-top: 30px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.feature-icon {
  background-color: rgba(255, 255, 255, 0.2);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  
  .el-icon {
    font-size: 20px;
  }
}

.copyright {
  margin-top: 40px;
  text-align: center;
  color: #666;
  font-size: 14px;
}

.mt-4 {
  margin-top: 1.5rem;
}

.text-light {
  color: rgba(255, 255, 255, 0.8);
}

.opacity-75 {
  opacity: 0.75;
}
</style> 