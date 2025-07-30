<template>
  <div class="test-container">
    <h2>API测试</h2>
    
    <div class="test-section">
      <h3>环境变量</h3>
      <p>API基础URL: {{ apiBaseUrl }}</p>
      <p>应用标题: {{ appTitle }}</p>
    </div>
    
    <div class="test-section">
      <h3>测试GET请求</h3>
      <el-button type="primary" @click="testGet">测试GET</el-button>
      <pre v-if="getResult">{{ getResult }}</pre>
    </div>
    
    <div class="test-section">
      <h3>测试登录</h3>
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="testLogin">测试登录</el-button>
        </el-form-item>
      </el-form>
      <pre v-if="loginResult">{{ loginResult }}</pre>
    </div>
    
    <div class="test-section">
      <h3>测试直接请求</h3>
      <el-form :model="directForm" label-width="80px">
        <el-form-item label="URL">
          <el-input v-model="directForm.url"></el-input>
        </el-form-item>
        <el-form-item label="方法">
          <el-select v-model="directForm.method">
            <el-option label="GET" value="get"></el-option>
            <el-option label="POST" value="post"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数据" v-if="directForm.method === 'post'">
          <el-input v-model="directForm.data" type="textarea"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="testDirect">测试请求</el-button>
        </el-form-item>
      </el-form>
      <pre v-if="directResult">{{ directResult }}</pre>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'TestApi',
  data() {
    return {
      apiBaseUrl: import.meta.env.VITE_APP_API_BASE_URL || '未设置',
      appTitle: import.meta.env.VITE_APP_TITLE || '未设置',
      getResult: null,
      loginResult: null,
      directResult: null,
      loginForm: {
        username: 'admin',
        password: 'admin123'
      },
      directForm: {
        url: 'http://localhost:8080/api/auth/login',
        method: 'post',
        data: '{"username":"admin","password":"admin123"}'
      }
    }
  },
  mounted() {
    console.log('环境变量:', import.meta.env)
  },
  methods: {
    async testGet() {
      try {
        console.log('测试GET请求')
        console.log('API基础URL:', import.meta.env.VITE_APP_API_BASE_URL)
        
        const response = await axios.get(`${import.meta.env.VITE_APP_API_BASE_URL}/test/hello`)
        this.getResult = {
          status: response.status,
          data: response.data
        }
      } catch (error) {
        console.error('GET请求错误:', error)
        this.getResult = {
          error: error.message,
          response: error.response?.data
        }
      }
    },
    async testLogin() {
      try {
        console.log('测试登录请求')
        console.log('API基础URL:', import.meta.env.VITE_APP_API_BASE_URL)
        
        const response = await axios.post(`${import.meta.env.VITE_APP_API_BASE_URL}/auth/login`, this.loginForm)
        this.loginResult = {
          status: response.status,
          data: response.data
        }
      } catch (error) {
        console.error('登录请求错误:', error)
        this.loginResult = {
          error: error.message,
          response: error.response?.data
        }
      }
    },
    async testDirect() {
      try {
        console.log('测试直接请求')
        
        let data = null
        if (this.directForm.method === 'post' && this.directForm.data) {
          try {
            data = JSON.parse(this.directForm.data)
          } catch (e) {
            data = this.directForm.data
          }
        }
        
        const response = await axios({
          method: this.directForm.method,
          url: this.directForm.url,
          data: data,
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        this.directResult = {
          status: response.status,
          data: response.data
        }
      } catch (error) {
        console.error('直接请求错误:', error)
        this.directResult = {
          error: error.message,
          response: error.response?.data
        }
      }
    }
  }
}
</script>

<style scoped>
.test-container {
  padding: 20px;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

pre {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-top: 15px;
  white-space: pre-wrap;
}
</style> 