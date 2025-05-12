<template>
  <div class="forgot-container">
    <div class="forgot-box">
      <div class="header">
        <div class="logo">
          <el-icon><Monitor /></el-icon> SkyEye 智能监控
        </div>
        <h2>找回密码</h2>
      </div>
      
      <el-form ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" @keyup.enter="handleSubmit">
        <el-form-item prop="username">
          <el-input v-model="forgotForm.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="forgotForm.email" placeholder="注册邮箱" prefix-icon="Message" size="large" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" class="submit-button" :loading="loading" @click="handleSubmit">提交</el-button>
        </el-form-item>
        
        <div class="back-to-login">
          <el-button type="text" @click="backToLogin">返回登录</el-button>
        </div>
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
import { forgotPassword } from '@/api/auth'

// 表单引用
const forgotFormRef = ref(null)

// 路由实例
const router = useRouter()

// 加载状态
const loading = ref(false)

// 表单数据
const forgotForm = reactive({
  username: '',
  email: ''
})

// 表单验证规则
const forgotRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 处理提交
const handleSubmit = () => {
  forgotFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 调用忘记密码接口
      await forgotPassword(forgotForm)
      
      ElMessage({
        message: '密码重置链接已发送到您的邮箱，请查收',
        type: 'success'
      })
      
      // 跳转到登录页
      router.push('/login')
    } catch (error) {
      console.error('找回密码失败:', error)
      ElMessage.error(error.message || '找回密码失败，请稍后再试')
    } finally {
      loading.value = false
    }
  })
}

// 返回登录页
const backToLogin = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.forgot-container {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.forgot-box {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 15px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  padding: 40px;
  width: 400px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    font-size: 24px;
    color: #333;
    margin-top: 15px;
  }
}

.logo {
  font-size: 22px;
  font-weight: bold;
  color: #1e3c72;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  .el-icon {
    font-size: 26px;
  }
}

.submit-button {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  border: none;
  padding: 12px;
  width: 100%;
  font-weight: 600;
  font-size: 16px;
  height: 48px;
}

.back-to-login {
  text-align: center;
  margin-top: 10px;
}

.copyright {
  margin-top: 40px;
  text-align: center;
  color: #666;
  font-size: 14px;
}
</style> 