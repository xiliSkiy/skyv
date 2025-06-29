<template>
  <div class="input-test-container">
    <h2>输入框测试页面</h2>
    <div class="test-description">
      <p>这是一个测试页面，用于验证Element Plus的输入框组件是否正常显示。</p>
    </div>
    
    <div class="test-section">
      <h3>基本输入框</h3>
      <el-input v-model="input1" placeholder="请输入内容"></el-input>
      <div class="mt-2">输入的内容: {{ input1 }}</div>
    </div>
    
    <div class="test-section">
      <h3>带前缀和后缀的输入框</h3>
      <el-input v-model="input2" placeholder="请输入内容">
        <template #prepend>
          <el-button>前缀按钮</el-button>
        </template>
        <template #append>
          <el-button type="primary">后缀按钮</el-button>
        </template>
      </el-input>
      <div class="mt-2">输入的内容: {{ input2 }}</div>
    </div>
    
    <div class="test-section">
      <h3>禁用状态的输入框</h3>
      <el-input v-model="input3" placeholder="禁用状态" disabled></el-input>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const input1 = ref('')
const input2 = ref('')
const input3 = ref('')
</script>

<style scoped>
.input-test-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-description {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f0f9ff;
  border-radius: 4px;
  border-left: 4px solid #409eff;
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