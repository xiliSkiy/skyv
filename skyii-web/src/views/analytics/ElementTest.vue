<template>
  <div class="element-test-container">
    <h2>Element Plus组件测试页面</h2>
    
    <div class="test-section">
      <h3>基本输入框测试</h3>
      <el-input v-model="input1" placeholder="请输入内容" clearable></el-input>
      <div class="mt-2">输入的内容: {{ input1 }}</div>
    </div>
    
    <div class="test-section">
      <h3>带图标的输入框</h3>
      <el-input v-model="input2" placeholder="请输入内容">
        <template #prefix>
          <el-icon><search /></el-icon>
        </template>
      </el-input>
      <div class="mt-2">输入的内容: {{ input2 }}</div>
    </div>
    
    <div class="test-section">
      <h3>带前缀和后缀的输入框</h3>
      <el-input v-model="input3" placeholder="请输入内容">
        <template #prepend>
          <el-button>
            <el-icon><search /></el-icon>
          </el-button>
        </template>
        <template #append>
          <el-button type="primary">
            <el-icon><check /></el-icon>
          </el-button>
        </template>
      </el-input>
      <div class="mt-2">输入的内容: {{ input3 }}</div>
    </div>
    
    <div class="test-section">
      <h3>文本域</h3>
      <el-input v-model="textarea" type="textarea" rows="3" placeholder="请输入多行内容"></el-input>
      <div class="mt-2">输入的内容: {{ textarea }}</div>
    </div>
    
    <div class="test-section">
      <h3>按钮测试</h3>
      <el-button>默认按钮</el-button>
      <el-button type="primary">主要按钮</el-button>
      <el-button type="success">成功按钮</el-button>
      <el-button type="warning">警告按钮</el-button>
      <el-button type="danger">危险按钮</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search, Check } from '@element-plus/icons-vue'

const input1 = ref('')
const input2 = ref('')
const input3 = ref('')
const textarea = ref('')
</script>

<style scoped>
.element-test-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-section {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.mt-2 {
  margin-top: 10px;
}
</style> 