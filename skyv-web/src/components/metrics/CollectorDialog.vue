<template>
  <el-dialog 
    :title="isEdit ? '编辑采集器' : '添加采集器'" 
    :visible="visible" 
    @update:visible="updateVisible"
    width="60%"
    destroy-on-close>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
          <el-form-item label="采集器名称" prop="collectorName">
            <el-input v-model="form.collectorName" placeholder="例如: 主数据中心采集器"></el-input>
          </el-form-item>
          
          <el-form-item label="主机地址" prop="hostname">
            <el-input v-model="form.hostname" placeholder="例如: 192.168.1.100 或 collector.example.com"></el-input>
          </el-form-item>
          
          <el-form-item label="端口" prop="port">
            <el-input-number v-model="form.port" :min="1" :max="65535"></el-input-number>
          </el-form-item>
          
          <el-form-item label="采集器类型" prop="collectorType">
            <el-select v-model="form.collectorType" placeholder="选择采集器类型" style="width: 100%">
              <el-option label="中心节点" value="master"></el-option>
              <el-option label="边缘节点" value="edge"></el-option>
              <el-option label="代理节点" value="proxy"></el-option>
              <el-option label="备份节点" value="backup"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="采集区域" prop="collectionZone">
            <el-input v-model="form.collectionZone" placeholder="例如: 华东区域"></el-input>
          </el-form-item>
          
          <el-form-item label="描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="描述该采集器的用途和部署位置..."></el-input>
          </el-form-item>
          
          <el-form-item label="设为主采集器">
            <el-switch v-model="form.isPrimary" :active-value="1" :inactive-value="0"></el-switch>
            <span class="ms-2">{{ form.isPrimary === 1 ? '是' : '否' }}</span>
            <div class="form-text text-muted">主采集器将作为默认的指标采集节点，每个区域仅允许一个主采集器</div>
          </el-form-item>
          
          <el-form-item label="启用状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0"></el-switch>
            <span class="ms-2">{{ form.status === 1 ? '启用' : '禁用' }}</span>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="连接设置" name="connection">
        <el-form :model="form" ref="connectionFormRef" label-width="120px">
          <el-form-item label="认证方式" prop="authType">
            <el-select v-model="form.authType" placeholder="选择认证方式" style="width: 100%" @change="handleAuthTypeChange">
              <el-option label="无认证" value="none"></el-option>
              <el-option label="用户名密码" value="basic"></el-option>
              <el-option label="API密钥" value="apikey"></el-option>
              <el-option label="证书认证" value="certificate"></el-option>
            </el-select>
          </el-form-item>
          
          <div v-if="form.authType === 'basic'">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="form.username"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="密码" prop="password">
                  <el-input v-model="form.password" type="password"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
          
          <div v-if="form.authType === 'apikey'">
            <el-form-item label="API密钥" prop="apiKey">
              <el-input v-model="form.apiKey" type="password"></el-input>
            </el-form-item>
          </div>
          
          <div v-if="form.authType === 'certificate'">
            <el-form-item label="证书文件" prop="certificatePath">
              <el-input v-model="form.certificatePath"></el-input>
            </el-form-item>
            <el-form-item label="私钥文件" prop="privateKeyPath">
              <el-input v-model="form.privateKeyPath"></el-input>
            </el-form-item>
            <el-form-item label="证书密码" prop="certificatePassword">
              <el-input v-model="form.certificatePassword" type="password"></el-input>
            </el-form-item>
          </div>
          
          <el-divider>连接参数</el-divider>
          
          <el-form-item label="连接超时" prop="connectionTimeout">
            <div class="d-flex align-items-center">
              <el-input-number v-model="form.connectionTimeout" :min="1" :max="60"></el-input-number>
              <span class="ms-2">秒</span>
            </div>
          </el-form-item>
          
          <el-form-item label="重试次数" prop="retryCount">
            <el-input-number v-model="form.retryCount" :min="0" :max="10"></el-input-number>
          </el-form-item>
          
          <el-form-item label="重试间隔" prop="retryInterval">
            <div class="d-flex align-items-center">
              <el-input-number v-model="form.retryInterval" :min="1" :max="60"></el-input-number>
              <span class="ms-2">秒</span>
            </div>
          </el-form-item>
          
          <el-form-item label="并发采集数" prop="concurrentCollections">
            <el-input-number v-model="form.concurrentCollections" :min="1" :max="100"></el-input-number>
            <div class="form-text text-muted">同时进行的最大采集任务数量</div>
          </el-form-item>
          
          <div class="d-flex justify-content-end mt-4">
            <el-button type="primary" @click="testConnection" :loading="testingConnection">
              <i class="fas fa-link me-1"></i> 测试连接
            </el-button>
          </div>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="高级设置" name="advanced">
        <el-form :model="form" ref="advancedFormRef" label-width="120px">
          <el-form-item label="采集器版本" prop="version">
            <el-input v-model="form.version" disabled></el-input>
            <div class="form-text text-muted">采集器软件版本，自动检测</div>
          </el-form-item>
          
          <el-form-item label="心跳间隔" prop="heartbeatInterval">
            <div class="d-flex align-items-center">
              <el-input-number v-model="form.heartbeatInterval" :min="5" :max="300"></el-input-number>
              <span class="ms-2">秒</span>
            </div>
            <div class="form-text text-muted">采集器向管理中心发送心跳的时间间隔</div>
          </el-form-item>
          
          <el-form-item label="数据压缩">
            <el-switch v-model="form.enableCompression" :active-value="true" :inactive-value="false"></el-switch>
            <div class="form-text text-muted">启用数据传输压缩以减少网络带宽消耗</div>
          </el-form-item>
          
          <el-form-item label="压缩级别" prop="compressionLevel" v-if="form.enableCompression">
            <el-slider v-model="form.compressionLevel" :min="1" :max="9" :marks="{1:'低', 5:'中', 9:'高'}"></el-slider>
            <div class="form-text text-muted">压缩级别越高，CPU占用越高，带宽消耗越低</div>
          </el-form-item>
          
          <el-form-item label="启用数据缓存">
            <el-switch v-model="form.enableDataCache" :active-value="true" :inactive-value="false"></el-switch>
            <div class="form-text text-muted">在网络中断时缓存采集数据，网络恢复后自动上传</div>
          </el-form-item>
          
          <el-form-item label="缓存容量" prop="dataCacheSize" v-if="form.enableDataCache">
            <div class="d-flex align-items-center">
              <el-input-number v-model="form.dataCacheSize" :min="1" :max="10000"></el-input-number>
              <span class="ms-2">MB</span>
            </div>
            <div class="form-text text-muted">本地数据缓存的最大容量</div>
          </el-form-item>
          
          <el-form-item label="自动更新">
            <el-switch v-model="form.enableAutoUpdate" :active-value="true" :inactive-value="false"></el-switch>
            <div class="form-text text-muted">自动更新采集器软件版本</div>
          </el-form-item>
          
          <el-form-item label="日志级别" prop="logLevel">
            <el-select v-model="form.logLevel" style="width: 100%">
              <el-option label="ERROR" value="error"></el-option>
              <el-option label="WARN" value="warn"></el-option>
              <el-option label="INFO" value="info"></el-option>
              <el-option label="DEBUG" value="debug"></el-option>
              <el-option label="TRACE" value="trace"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="updateVisible(false)">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="loading">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { defineComponent, ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createCollector, updateCollector, checkCollector, testCollectorConnection } from '@/api/metrics'

export default defineComponent({
  name: 'CollectorDialog',
  props: {
    visible: {
      type: Boolean,
      required: true
    },
    collector: {
      type: Object,
      default: () => ({})
    },
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:visible', 'success'],
  setup(props, { emit }) {
    const activeTab = ref('basic')
    const formRef = ref(null)
    const loading = ref(false)
    const testingConnection = ref(false)
    
    // 表单数据
    const form = reactive({
      id: null,
      collectorName: '',
      hostname: '',
      port: 8086,
      collectorType: 'edge',
      collectionZone: '',
      description: '',
      isPrimary: 0,
      status: 1,
      
      // 连接设置
      authType: 'none',
      username: '',
      password: '',
      apiKey: '',
      certificatePath: '',
      privateKeyPath: '',
      certificatePassword: '',
      connectionTimeout: 30,
      retryCount: 3,
      retryInterval: 5,
      concurrentCollections: 10,
      
      // 高级设置
      version: '1.0.0',
      heartbeatInterval: 60,
      enableCompression: true,
      compressionLevel: 5,
      enableDataCache: true,
      dataCacheSize: 100,
      enableAutoUpdate: true,
      logLevel: 'info'
    })
    
    // 表单验证规则
    const rules = reactive({
      collectorName: [
        { required: true, message: '请输入采集器名称', trigger: 'blur' }
      ],
      hostname: [
        { required: true, message: '请输入主机地址', trigger: 'blur' }
      ],
      port: [
        { required: true, message: '请输入端口号', trigger: 'blur' },
        { type: 'number', min: 1, max: 65535, message: '端口范围为1-65535', trigger: 'blur' }
      ],
      collectorType: [
        { required: true, message: '请选择采集器类型', trigger: 'change' }
      ],
      collectionZone: [
        { required: true, message: '请输入采集区域', trigger: 'blur' }
      ]
    })
    
    // 监听对话框可见性变化，初始化表单数据
    watch(() => props.visible, (val) => {
      if (val && props.collector) {
        initFormData()
      }
    })
    
    // 初始化表单数据
    const initFormData = () => {
      if (props.isEdit) {
        Object.keys(form).forEach(key => {
          if (props.collector[key] !== undefined) {
            form[key] = props.collector[key]
          }
        })
      }
    }
    
    // 更新对话框可见性
    const updateVisible = (val) => {
      emit('update:visible', val)
    }
    
    // 处理认证方式变化
    const handleAuthTypeChange = (authType) => {
      console.log('认证方式变更为:', authType)
    }
    
    // 测试采集器连接
    const testConnection = async () => {
      if (!form.hostname || !form.port) {
        ElMessage.warning('请先填写主机地址和端口号')
        return
      }
      
      testingConnection.value = true
      
      try {
        const params = {
          hostname: form.hostname,
          port: form.port,
          authType: form.authType,
          username: form.username,
          password: form.password,
          apiKey: form.apiKey,
          certificatePath: form.certificatePath,
          privateKeyPath: form.privateKeyPath,
          connectionTimeout: form.connectionTimeout
        }
        
        const response = await testCollectorConnection(params)
        
        if (response.code === 200) {
          ElMessage.success('连接测试成功')
          
          // 如果返回了版本信息，更新表单
          if (response.data && response.data.version) {
            form.version = response.data.version
          }
        } else {
          ElMessage.error(`连接测试失败: ${response.message}`)
        }
      } catch (error) {
        ElMessage.error(`连接测试出错: ${error.message || '未知错误'}`)
      } finally {
        testingConnection.value = false
      }
    }
    
    // 提交表单
    const submitForm = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
        
        loading.value = true
        
        // 检查采集器名称是否重复
        if (!props.isEdit || (props.isEdit && form.collectorName !== props.collector.collectorName)) {
          const response = await checkCollector(form.collectorName, props.isEdit ? form.id : null)
          if (response.data) {
            ElMessage.error('采集器名称已存在，请更换')
            loading.value = false
            return
          }
        }
        
        // 保存采集器
        if (props.isEdit) {
          await updateCollector(form.id, form)
          ElMessage.success('采集器更新成功')
        } else {
          await createCollector(form)
          ElMessage.success('采集器创建成功')
        }
        
        emit('success')
        updateVisible(false)
      } catch (error) {
        console.error('表单验证失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    return {
      activeTab,
      formRef,
      form,
      rules,
      loading,
      testingConnection,
      updateVisible,
      handleAuthTypeChange,
      testConnection,
      submitForm
    }
  }
})
</script>

<style scoped>
.form-text {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 