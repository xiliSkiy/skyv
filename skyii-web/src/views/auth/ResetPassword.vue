<template>
  <div class="reset-container">
    <div class="reset-box">
      <div class="header">
        <div class="logo">
          <el-icon><Monitor /></el-icon> SkyEye 智能监控
        </div>
        <h2>重置密码</h2>
      </div>
      
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" @keyup.enter="handleSubmit">
        <el-form-item prop="password">
          <el-input v-model="resetForm.password" type="password" placeholder="新密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="resetForm.confirmPassword" type="password" placeholder="确认新密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" class="submit-button" :loading="loading" @click="handleSubmit">重置密码</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { resetPassword } from '@/api/auth'

// 表单引用
const resetFormRef = ref(null)

// 路由实例
const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(false)

// 获取token
const token = ref('')

// 表单数据
const resetForm = reactive({
  password: '',
  confirmPassword: '',
  token: ''
})

// 表单验证规则
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (resetForm.confirmPassword !== '') {
      resetFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== resetForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  password: [
    { validator: validatePassword, trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 处理提交
const handleSubmit = () => {
  resetFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 调用重置密码接口
      await resetPassword({
        password: resetForm.password,
        token: resetForm.token
      })
      
      ElMessage({
        message: '密码重置成功，请使用新密码登录',
        type: 'success'
      })
      
      // 跳转到登录页
      router.push('/login')
    } catch (error) {
      console.error('重置密码失败:', error)
      ElMessage.error(error.message || '重置密码失败，请稍后再试')
    } finally {
      loading.value = false
    }
  })
}

// 返回登录页
const backToLogin = () => {
  router.push('/login')
}

// 页面加载时获取token参数
onMounted(() => {
  const urlToken = route.query.token
  if (urlToken) {
    resetForm.token = urlToken
  } else {
    ElMessage.error('无效的重置链接')
    router.push('/login')
  }
})
</script>

<style lang="scss" scoped>
.reset-container {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reset-box {
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