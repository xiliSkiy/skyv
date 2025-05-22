<template>
  <div class="collector-add">
    <div class="page-header">
      <h4>{{ isEdit ? '编辑采集器' : '添加采集器' }}</h4>
    </div>

    <el-card shadow="never">
      <!-- 步骤指示器 - 只在添加模式显示 -->
      <div v-if="!isEdit" class="steps-container">
        <el-steps :active="activeStep" finish-status="success">
          <el-step title="创建采集器" description="填写基本信息"></el-step>
          <el-step title="配置与部署" description="生成令牌并部署"></el-step>
          <el-step title="激活与验证" description="等待采集器连接"></el-step>
        </el-steps>
      </div>

      <!-- 步骤1: 创建采集器 -->
      <div v-if="activeStep === 1 || isEdit">
        <el-form 
          ref="collectorFormRef" 
          :model="collectorForm" 
          :rules="rules" 
          label-width="120px" 
          label-position="right"
          status-icon>
          
          <!-- 基本信息 -->
          <div class="form-section">
            <h5 class="section-title">基本信息</h5>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="采集器名称" prop="collectorName">
                  <el-input v-model="collectorForm.collectorName" placeholder="请输入采集器名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="采集器类型" prop="collectorType">
                  <el-select v-model="collectorForm.collectorType" placeholder="请选择采集器类型" style="width: 100%">
                    <el-option v-for="type in collectorTypes" :key="type.value" :label="type.label" :value="type.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="描述" prop="description">
              <el-input
                v-model="collectorForm.description"
                type="textarea"
                placeholder="请输入采集器描述"
                :autosize="{ minRows: 2, maxRows: 4 }"
              />
            </el-form-item>

            <el-form-item label="设为主采集器">
              <el-switch v-model="collectorForm.isMain" />
              <span class="form-tip">主采集器将负责协调其他采集器并执行系统级任务</span>
            </el-form-item>
          </div>

          <!-- 网络配置 -->
          <div class="form-section">
            <h5 class="section-title">网络配置</h5>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="主机地址" prop="host">
                  <el-input v-model="collectorForm.host" placeholder="请输入IP地址或主机名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="端口" prop="port">
                  <el-input-number v-model="collectorForm.port" controls-position="right" :min="1" :max="65535" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="网络区域" prop="networkZone">
                  <el-select v-model="collectorForm.networkZone" placeholder="请选择网络区域" style="width: 100%">
                    <el-option v-for="zone in networkZones" :key="zone" :label="zone" :value="zone" />
                    <el-option value="custom" label="自定义" />
                  </el-select>
                  <el-input 
                    v-if="collectorForm.networkZone === 'custom'" 
                    v-model="collectorForm.customNetworkZone" 
                    placeholder="请输入自定义网络区域" 
                    style="margin-top: 10px"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="认证方式" prop="authType">
                  <el-select v-model="collectorForm.authType" placeholder="请选择认证方式" style="width: 100%">
                    <el-option label="无认证" value="none" />
                    <el-option label="用户名/密码" value="basic" />
                    <el-option label="API密钥" value="apikey" />
                    <el-option label="证书" value="cert" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 认证信息，根据认证方式显示不同表单 -->
            <div v-if="collectorForm.authType === 'basic'">
                              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="collectorForm.username" placeholder="请输入用户名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="密码" prop="password">
                    <el-input v-model="collectorForm.password" type="password" placeholder="请输入密码" show-password />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <div v-if="collectorForm.authType === 'apikey'">
              <el-form-item label="API密钥" prop="apiKey">
                <el-input v-model="collectorForm.apiKey" placeholder="请输入API密钥" />
              </el-form-item>
            </div>

            <div v-if="collectorForm.authType === 'cert'">
              <el-form-item label="证书文件" prop="certFile">
                <el-upload
                  class="cert-upload"
                  action="#"
                  :auto-upload="false"
                  :on-change="handleCertFileChange"
                  :limit="1"
                >
                  <el-button type="primary">选择证书文件</el-button>
                  <template #tip>
                    <div class="el-upload__tip">支持.pem、.crt、.key格式</div>
                  </template>
                </el-upload>
              </el-form-item>
              <el-form-item label="密钥文件" prop="keyFile">
                <el-upload
                  class="key-upload"
                  action="#"
                  :auto-upload="false"
                  :on-change="handleKeyFileChange"
                  :limit="1"
                >
                  <el-button type="primary">选择密钥文件</el-button>
                  <template #tip>
                    <div class="el-upload__tip">支持.pem、.key格式</div>
                  </template>
                </el-upload>
              </el-form-item>
            </div>

            <!-- 测试连接按钮 -->
            <el-form-item>
              <el-button type="primary" @click="testConnection" :loading="testingConnection">
                <el-icon><Connection /></el-icon> 测试连接
              </el-button>
              <span v-if="connectionTestResult" :class="['test-result', connectionTestResult.success ? 'success' : 'failed']">
                <el-icon v-if="connectionTestResult.success"><CircleCheckFilled /></el-icon>
                <el-icon v-else><CircleCloseFilled /></el-icon>
                {{ connectionTestResult.message }}
              </span>
            </el-form-item>
          </div>

          <!-- 高级配置 -->
          <div class="form-section">
            <h5 class="section-title">高级配置</h5>
            
            <!-- 高级配置折叠面板 -->
            <el-collapse>
              <el-collapse-item title="性能设置" name="performance">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="最大连接数" prop="maxConnections">
                      <el-input-number 
                        v-model="collectorForm.advancedConfig.maxConnections" 
                        :min="1" 
                        :max="1000" 
                        style="width: 100%" 
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="连接超时(秒)" prop="connectionTimeout">
                      <el-input-number 
                        v-model="collectorForm.advancedConfig.connectionTimeout" 
                        :min="1" 
                        :max="300" 
                        style="width: 100%" 
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="并发任务数" prop="concurrentTasks">
                  <el-slider 
                    v-model="collectorForm.advancedConfig.concurrentTasks" 
                    :min="1" 
                    :max="50" 
                    :marks="{1: '1', 10: '10', 25: '25', 50: '50'}" 
                  />
                </el-form-item>
              </el-collapse-item>
              
              <el-collapse-item title="心跳设置" name="heartbeat">
                <el-form-item label="心跳间隔(秒)" prop="heartbeatInterval">
                  <el-input-number 
                    v-model="collectorForm.advancedConfig.heartbeatInterval" 
                    :min="5" 
                    :max="300" 
                    style="width: 100%" 
                  />
                </el-form-item>
                <el-form-item label="失联阈值" prop="missedHeartbeatsThreshold">
                  <el-input-number 
                    v-model="collectorForm.advancedConfig.missedHeartbeatsThreshold" 
                    :min="1" 
                    :max="10" 
                    style="width: 100%" 
                  />
                  <span class="form-tip">连续失联次数超过此阈值时将标记为离线</span>
                </el-form-item>
              </el-collapse-item>
              
              <el-collapse-item title="日志设置" name="logging">
                <el-form-item label="日志级别" prop="logLevel">
                  <el-select 
                    v-model="collectorForm.advancedConfig.logLevel" 
                    style="width: 100%"
                  >
                    <el-option label="调试" value="DEBUG" />
                    <el-option label="信息" value="INFO" />
                    <el-option label="警告" value="WARN" />
                    <el-option label="错误" value="ERROR" />
                    <el-option label="严重错误" value="FATAL" />
                  </el-select>
                </el-form-item>
                <el-form-item label="日志保留天数" prop="logRetentionDays">
                  <el-input-number 
                    v-model="collectorForm.advancedConfig.logRetentionDays" 
                    :min="1" 
                    :max="365" 
                    style="width: 100%" 
                  />
                </el-form-item>
              </el-collapse-item>
              
              <el-collapse-item title="数据设置" name="data">
                <el-form-item label="缓存大小(MB)" prop="cacheSize">
                  <el-input-number 
                    v-model="collectorForm.advancedConfig.cacheSize" 
                    :min="10" 
                    :max="1024" 
                    style="width: 100%" 
                  />
                </el-form-item>
                <el-form-item label="本地存储路径" prop="localStoragePath">
                  <el-input 
                    v-model="collectorForm.advancedConfig.localStoragePath" 
                    placeholder="请输入本地存储路径" 
                  />
                </el-form-item>
                <el-form-item label="数据压缩">
                  <el-switch v-model="collectorForm.advancedConfig.enableCompression" />
                  <span class="form-tip">启用数据压缩可以减少网络传输量，但会增加CPU负载</span>
                </el-form-item>
              </el-collapse-item>
              
              <el-collapse-item title="自定义参数" name="custom">
                <el-form-item>
                  <div v-for="(param, index) in collectorForm.customParams" :key="index" class="custom-param-item">
                    <el-input v-model="param.key" placeholder="参数名" class="param-key" />
                    <el-input v-model="param.value" placeholder="参数值" class="param-value" />
                    <el-button type="danger" circle @click="removeCustomParam(index)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <el-button type="primary" plain @click="addCustomParam">
                    <el-icon><Plus /></el-icon> 添加参数
                  </el-button>
                </el-form-item>
              </el-collapse-item>
            </el-collapse>
          </div>

          <!-- 表单按钮 -->
          <div class="form-actions">
            <el-button @click="cancel">取消</el-button>
            <el-button v-if="isEdit" type="primary" @click="submitForm" :loading="submitting">
              保存修改
            </el-button>
            <el-button v-else type="primary" @click="nextStep" :loading="submitting">
              下一步 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-form>
      </div>

      <!-- 步骤2: 配置与部署 -->
      <div v-if="activeStep === 2 && !isEdit" class="step-content">
        <div class="deployment-container">
          <h5 class="section-title">采集器注册令牌</h5>
          <div class="token-section">
            <div class="token-box">
              <div class="token-value">{{ registrationToken }}</div>
              <div class="token-actions">
                <el-button type="primary" plain @click="copyToken" size="small">
                  <el-icon><CopyDocument /></el-icon> 复制令牌
                </el-button>
                <el-button type="info" plain @click="regenerateToken" size="small">
                  <el-icon><Refresh /></el-icon> 重新生成
                </el-button>
              </div>
            </div>
            <div class="token-expire" v-if="tokenExpiryTime">
              <el-icon><Timer /></el-icon> 令牌有效期至 {{ tokenExpiryTime }}
            </div>
          </div>

          <div class="deployment-section">
            <h5 class="section-title">部署说明</h5>
            <div class="installation-steps">
              <p>1. 下载并安装采集器软件包</p>
              <div class="download-options">
                <el-button type="primary">
                  <el-icon><Download /></el-icon> 下载 Linux 版本 (x64)
                </el-button>
                <el-button type="primary">
                  <el-icon><Download /></el-icon> 下载 Windows 版本 (x64)
                </el-button>
              </div>

              <p>2. 使用以下命令安装采集器</p>
              <div class="code-block">
                <div class="code-header">
                  <span>Linux 安装命令</span>
                  <el-button type="info" plain size="small" @click="copyInstallCommand('linux')">
                    <el-icon><CopyDocument /></el-icon> 复制
                  </el-button>
                </div>
                <pre>chmod +x skyeye-collector-linux-x64.sh
./skyeye-collector-linux-x64.sh --token={{ registrationToken }} --server={{ serverUrl }}</pre>
              </div>

              <div class="code-block">
                <div class="code-header">
                  <span>Windows 安装命令</span>
                  <el-button type="info" plain size="small" @click="copyInstallCommand('windows')">
                    <el-icon><CopyDocument /></el-icon> 复制
                  </el-button>
                </div>
                <pre>skyeye-collector-windows-x64.exe --token={{ registrationToken }} --server={{ serverUrl }}</pre>
              </div>

              <p>3. 远程部署（SSH）</p>
              <div class="remote-deploy-section">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="主机地址">
                      <el-input v-model="deployHost" placeholder="服务器IP或主机名" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="SSH端口">
                      <el-input-number v-model="deployPort" :min="1" :max="65535" :step="1" :stepStrictly="true" :controls="false" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="用户名">
                      <el-input v-model="deployUser" placeholder="SSH用户名" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="认证方式">
                      <el-radio-group v-model="deployAuthType">
                        <el-radio label="password">密码</el-radio>
                        <el-radio label="key">密钥</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item v-if="deployAuthType === 'password'" label="密码">
                  <el-input v-model="deployPassword" type="password" placeholder="SSH密码" show-password />
                </el-form-item>
                <el-form-item v-if="deployAuthType === 'key'" label="私钥文件">
                  <el-upload
                    class="key-file-upload"
                    action="#"
                    :auto-upload="false"
                    :on-change="handleDeployKeyFileChange"
                    :limit="1">
                    <el-button type="primary">选择密钥文件</el-button>
                  </el-upload>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="remoteDeploy" :loading="deploying">
                    <el-icon><Connection /></el-icon> 开始远程部署
                  </el-button>
                </el-form-item>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <el-button @click="prevStep">
              <el-icon class="el-icon--left"><ArrowLeft /></el-icon> 上一步
            </el-button>
            <el-button type="primary" @click="nextStep">
              下一步 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 步骤3: 激活与验证 -->
      <div v-if="activeStep === 3 && !isEdit" class="step-content">
        <div class="activation-container">
          <div class="activation-status">
            <div v-if="activationStatus === 'waiting'" class="waiting-section">
              <el-icon class="is-loading"><Loading /></el-icon>
              <h5>等待采集器连接...</h5>
              <p class="countdown">{{ activationCountdown }}</p>
            </div>
            <div v-else-if="activationStatus === 'success'" class="success-section">
              <el-icon><CircleCheckFilled /></el-icon>
              <h5>采集器已成功连接并激活！</h5>
              <div class="activation-details">
                <p><strong>采集器名称:</strong> {{ collectorForm.collectorName }}</p>
                <p><strong>主机地址:</strong> {{ activationDetails.host }}</p>
                <p><strong>连接时间:</strong> {{ activationDetails.connectedAt }}</p>
                <p><strong>版本信息:</strong> {{ activationDetails.version }}</p>
              </div>
            </div>
            <div v-else-if="activationStatus === 'failed'" class="failed-section">
              <el-icon><CircleCloseFilled /></el-icon>
              <h5>采集器连接失败</h5>
              <div class="error-details">
                <p>{{ activationError }}</p>
                <div class="troubleshooting">
                  <h6>请检查以下几点:</h6>
                  <ul>
                    <li>确保采集器软件已正确安装</li>
                    <li>验证注册令牌输入正确</li>
                    <li>检查网络连接是否畅通</li>
                    <li>确认防火墙已允许相关端口</li>
                    <li>检查服务器时间是否同步</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <el-button @click="prevStep">
              <el-icon class="el-icon--left"><ArrowLeft /></el-icon> 上一步
            </el-button>
            <el-button type="info" @click="checkActivationStatus" :disabled="activationStatus === 'success'">
              <el-icon><Refresh /></el-icon> 检查连接状态
            </el-button>
            <el-button v-if="activationStatus === 'success'" type="primary" @click="finishSetup">
              完成设置
            </el-button>
            <el-button v-else @click="finishSetup">
              稍后再试
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  createCollector, 
  updateCollector, 
  getCollectorById, 
  getCollectorNetworkZones,
  testCollectorConnection,
  generateRegistrationToken,
  activateCollector
} from '@/api/collector'

const route = useRoute()
const router = useRouter()

// 判断是新增还是编辑
const isEdit = computed(() => Boolean(route.query.id))

// 表单引用
const collectorFormRef = ref(null)

// 状态变量
const submitting = ref(false)
const testingConnection = ref(false)
const connectionTestResult = ref(null)
const activeStep = ref(1)
const createdCollectorId = ref(null)
const registrationToken = ref('')
const tokenExpiryTime = ref('')
const serverUrl = ref(window.location.origin + '/api')
const deploying = ref(false)
const deployHost = ref('')
const deployPort = ref(22)
const deployUser = ref('root')
const deployAuthType = ref('password')
const deployPassword = ref('')
const deployKeyFile = ref(null)
const activationStatus = ref('waiting') // waiting, success, failed
const activationCountdown = ref('00:00 / 03:00')
const activationTimer = ref(null)
const activationTimeLeft = ref(180) // 3分钟倒计时
const activationDetails = reactive({
  host: '',
  connectedAt: '',
  version: ''
})
const activationError = ref('')
const activationCheckInterval = ref(null)

// 采集器类型列表
const collectorTypes = [
  { label: 'SNMP', value: 'snmp' },
  { label: 'HTTP/API', value: 'http' },
  { label: 'JMX', value: 'jmx' },
  { label: 'SSH', value: 'ssh' },
  { label: 'WMI', value: 'wmi' },
  { label: '自定义', value: 'custom' }
]

// 网络区域列表
const networkZones = ref([
  '主数据中心A区',
  '主数据中心B区',
  '备份数据中心',
  '边缘节点',
  '云端节点'
])

// 表单数据
const collectorForm = reactive({
  collectorName: '',
  collectorType: '',
  description: '',
  isMain: false,
  host: '',
  port: 8080,
  networkZone: '',
  customNetworkZone: '',
  authType: 'none',
  username: '',
  password: '',
  apiKey: '',
  certFile: null,
  keyFile: null,
  advancedConfig: {
    maxConnections: 100,
    connectionTimeout: 30,
    concurrentTasks: 5,
    heartbeatInterval: 60,
    missedHeartbeatsThreshold: 3,
    logLevel: 'INFO',
    logRetentionDays: 30,
    cacheSize: 100,
    localStoragePath: '/var/log/skyeye/collectors',
    enableCompression: true
  },
  customParams: []
})

// 表单验证规则
const rules = {
  collectorName: [
    { required: true, message: '请输入采集器名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  collectorType: [
    { required: true, message: '请选择采集器类型', trigger: 'change' }
  ],
  host: [
    { required: true, message: '请输入主机地址', trigger: 'blur' }
  ],
  port: [
    { required: true, message: '请输入端口号', trigger: 'blur' },
    { type: 'integer', min: 1, max: 65535, message: '端口号必须在 1-65535 之间', trigger: 'blur' }
  ],
  networkZone: [
    { required: true, message: '请选择网络区域', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur', validator: (rule, value, callback) => {
      if (collectorForm.authType === 'basic' && !value) {
        callback(new Error('请输入用户名'))
      } else {
        callback()
      }
    }}
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur', validator: (rule, value, callback) => {
      if (collectorForm.authType === 'basic' && !value) {
        callback(new Error('请输入密码'))
      } else {
        callback()
      }
    }}
  ],
  apiKey: [
    { required: true, message: '请输入API密钥', trigger: 'blur', validator: (rule, value, callback) => {
      if (collectorForm.authType === 'apikey' && !value) {
        callback(new Error('请输入API密钥'))
      } else {
        callback()
      }
    }}
  ]
}

// 证书文件上传
const handleCertFileChange = (file) => {
  collectorForm.certFile = file.raw
}

// 密钥文件上传
const handleKeyFileChange = (file) => {
  collectorForm.keyFile = file.raw
}

// 部署密钥文件上传
const handleDeployKeyFileChange = (file) => {
  deployKeyFile.value = file.raw
}

// 添加自定义参数
const addCustomParam = () => {
  collectorForm.customParams.push({ key: '', value: '' })
}

// 删除自定义参数
const removeCustomParam = (index) => {
  collectorForm.customParams.splice(index, 1)
}

// 测试连接
const testConnection = async () => {
  try {
    // 验证必要的连接字段
    const validateFields = ['host', 'port']
    const validationErrors = validateFields.filter(field => !collectorForm[field])
    
    if (validationErrors.length > 0) {
      ElMessage.warning('请填写主机地址和端口后再测试连接')
      return
    }

    testingConnection.value = true
    connectionTestResult.value = null

    const testData = {
      host: collectorForm.host,
      port: collectorForm.port,
      authType: collectorForm.authType
    }

    // 根据认证方式添加认证信息
    if (collectorForm.authType === 'basic') {
      testData.username = collectorForm.username
      testData.password = collectorForm.password
    } else if (collectorForm.authType === 'apikey') {
      testData.apiKey = collectorForm.apiKey
    }

    const res = await testCollectorConnection(testData)
    if (res.code === 200) {
      connectionTestResult.value = {
        success: true,
        message: '连接成功!'
      }
    } else {
      connectionTestResult.value = {
        success: false,
        message: res.message || '连接失败'
      }
    }
  } catch (error) {
    connectionTestResult.value = {
      success: false,
      message: error.message || '测试连接时发生错误'
    }
  } finally {
    testingConnection.value = false
  }
}

// 获取网络区域
const fetchNetworkZones = async () => {
  try {
    const res = await getCollectorNetworkZones()
    if (res.code === 200 && Array.isArray(res.data)) {
      networkZones.value = res.data
    }
  } catch (error) {
    console.error('获取网络区域失败:', error)
  }
}

// 提交表单
const submitForm = async () => {
  if (!collectorFormRef.value) return
  
  await collectorFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      submitting.value = true
      try {
        // 处理自定义网络区域
        if (collectorForm.networkZone === 'custom' && collectorForm.customNetworkZone) {
          collectorForm.networkZone = collectorForm.customNetworkZone
        }
        
        // 准备提交的数据
        const submitData = {
          collectorName: collectorForm.collectorName,
          collectorType: collectorForm.collectorType,
          description: collectorForm.description,
          isMain: collectorForm.isMain ? 1 : 0,
          host: collectorForm.host,
          port: collectorForm.port,
          networkZone: collectorForm.networkZone,
          configParams: JSON.stringify({
            authType: collectorForm.authType,
            advancedConfig: collectorForm.advancedConfig,
            customParams: collectorForm.customParams.reduce((acc, item) => {
              if (item.key && item.value) {
                acc[item.key] = item.value
              }
              return acc
            }, {})
          })
        }

        // 根据认证方式添加认证信息
        if (collectorForm.authType === 'basic') {
          submitData.username = collectorForm.username
          submitData.password = collectorForm.password
        } else if (collectorForm.authType === 'apikey') {
          submitData.apiKey = collectorForm.apiKey
        }

        let res
        if (isEdit.value) {
          submitData.id = route.query.id
          res = await updateCollector(submitData)
        } else {
          res = await createCollector(submitData)
        }

        if (res.code === 200) {
          if (isEdit.value) {
            ElMessage.success('更新成功')
            router.push({ name: 'CollectorManagement' })
          } else {
            // 保存创建的采集器ID，用于后续生成令牌
            createdCollectorId.value = res.data.id
            // 直接设置激活步骤为2，而不是调用nextStep
            activeStep.value = 2
          }
        } else {
          ElMessage.error(res.message || (isEdit.value ? '更新失败' : '创建失败'))
        }
      } catch (error) {
        console.error('提交表单失败:', error)
        ElMessage.error(isEdit.value ? '更新采集器时发生错误' : '创建采集器时发生错误')
      } finally {
        submitting.value = false
      }
    } else {
      console.log('表单验证失败:', fields)
      ElMessage.warning('请完善表单信息')
    }
  })
}

// 下一步
const nextStep = async () => {
  if (activeStep.value === 1 && !isEdit.value) {
    // 从第一步到第二步，需要先创建采集器
    // 防止重复提交
    if (submitting.value) return
    submitForm()
  } else if (activeStep.value === 2) {
    // 从第二步到第三步
    activeStep.value = 3
    // 开始监测采集器激活状态
    startActivationMonitoring()
  } else if (activeStep.value < 3) {
    activeStep.value++
  }
}

// 上一步
const prevStep = () => {
  if (activeStep.value > 1) {
    activeStep.value--
  }
}

// 生成注册令牌
const generateToken = async () => {
  if (!createdCollectorId.value) {
    ElMessage.error('采集器ID无效，无法生成令牌')
    return
  }

  try {
    const res = await generateRegistrationToken(createdCollectorId.value, 24)
    if (res.code === 200 && res.data) {
      registrationToken.value = res.data.token
      // 计算过期时间
      const now = new Date()
      const expiry = new Date(now.getTime() + parseInt(res.data.expireHours) * 60 * 60 * 1000)
      tokenExpiryTime.value = expiry.toLocaleString()
    } else {
      ElMessage.error(res.message || '生成令牌失败')
    }
  } catch (error) {
    console.error('生成令牌失败:', error)
    ElMessage.error('生成令牌失败')
  }
}

// 重新生成令牌
const regenerateToken = async () => {
  await generateToken()
  ElMessage.success('令牌已重新生成')
}

// 复制令牌
const copyToken = () => {
  if (!registrationToken.value) {
    ElMessage.warning('没有可用的令牌')
    return
  }

  navigator.clipboard.writeText(registrationToken.value).then(() => {
    ElMessage.success('令牌已复制到剪贴板')
  }, () => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 复制安装命令
const copyInstallCommand = (type) => {
  const command = type === 'linux' 
    ? `chmod +x skyeye-collector-linux-x64.sh\n./skyeye-collector-linux-x64.sh --token=${registrationToken.value} --server=${serverUrl.value}`
    : `skyeye-collector-windows-x64.exe --token=${registrationToken.value} --server=${serverUrl.value}`

  navigator.clipboard.writeText(command).then(() => {
    ElMessage.success('命令已复制到剪贴板')
  }, () => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 远程部署
const remoteDeploy = async () => {
  // 检查必填字段
  if (!deployHost.value || !deployUser.value || 
      (deployAuthType.value === 'password' && !deployPassword.value) ||
      (deployAuthType.value === 'key' && !deployKeyFile.value)) {
    ElMessage.warning('请填写完整的远程部署信息')
    return
  }
  
  deploying.value = true
  try {
    // 这里模拟远程部署的后端API，实际项目中需实现真实的部署接口
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 模拟部署成功
    ElMessage.success('已成功发送部署命令到远程服务器')
    // 实际项目中可以添加详细的部署状态和日志输出
  } catch (error) {
    console.error('远程部署失败:', error)
    ElMessage.error('远程部署失败: ' + (error.message || '未知错误'))
  } finally {
    deploying.value = false
  }
}

// 开始监测采集器激活状态
const startActivationMonitoring = () => {
  activationStatus.value = 'waiting'
  activationTimeLeft.value = 180 // 3分钟
  
  // 更新倒计时显示
  const updateCountdown = () => {
    const minutes = Math.floor(activationTimeLeft.value / 60)
    const seconds = activationTimeLeft.value % 60
    activationCountdown.value = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')} / 03:00`
    activationTimeLeft.value--
    
    // 倒计时结束
    if (activationTimeLeft.value < 0) {
      clearInterval(activationTimer.value)
      clearInterval(activationCheckInterval.value)
      // 如果还未成功激活，则设置为失败状态
      if (activationStatus.value === 'waiting') {
        activationStatus.value = 'failed'
        activationError.value = '等待超时，采集器未能成功连接'
      }
    }
  }
  
  // 定时检查采集器激活状态
  const checkActivation = async () => {
    try {
      // 这里应该调用后端API检查采集器状态
      // 模拟API调用
      const collectorStatus = await getCollectorById(createdCollectorId.value)
      
      // 检查采集器是否激活（status=1表示在线/已激活）
      if (collectorStatus.code === 200 && collectorStatus.data.status === 1) {
        // 采集器已激活
        clearInterval(activationTimer.value)
        clearInterval(activationCheckInterval.value)
        
        activationStatus.value = 'success'
        activationDetails.host = collectorStatus.data.host
        activationDetails.connectedAt = new Date().toLocaleString()
        activationDetails.version = collectorStatus.data.version || '1.0.0'
      }
    } catch (error) {
      console.error('检查采集器状态失败:', error)
    }
  }
  
  // 每秒更新倒计时
  activationTimer.value = setInterval(updateCountdown, 1000)
  // 每10秒检查一次激活状态
  activationCheckInterval.value = setInterval(checkActivation, 10000)
  
  // 立即执行一次检查
  updateCountdown()
  checkActivation()
}

// 手动检查激活状态
const checkActivationStatus = async () => {
  try {
    ElMessage.info('正在检查采集器连接状态...')
    const collectorStatus = await getCollectorById(createdCollectorId.value)
    
    if (collectorStatus.code === 200) {
      if (collectorStatus.data.status === 1) {
        activationStatus.value = 'success'
        activationDetails.host = collectorStatus.data.host
        activationDetails.connectedAt = new Date().toLocaleString()
        activationDetails.version = collectorStatus.data.version || '1.0.0'
        
        // 清除定时器
        clearInterval(activationTimer.value)
        clearInterval(activationCheckInterval.value)
        
        ElMessage.success('采集器已成功连接!')
      } else {
        ElMessage.warning('采集器尚未连接，请稍后再试')
      }
    } else {
      ElMessage.error(collectorStatus.message || '检查连接状态失败')
    }
  } catch (error) {
    console.error('检查采集器状态失败:', error)
    ElMessage.error('检查连接状态失败: ' + (error.message || '未知错误'))
  }
}

// 完成设置
const finishSetup = () => {
  // 清除所有定时器
  clearInterval(activationTimer.value)
  clearInterval(activationCheckInterval.value)
  
  router.push({ name: 'CollectorManagement' })
}

// 取消
const cancel = () => {
  ElMessageBox.confirm('确定要取消操作吗？未保存的数据将会丢失', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清除所有定时器
    clearInterval(activationTimer.value)
    clearInterval(activationCheckInterval.value)
    
    router.push({ name: 'CollectorManagement' })
  }).catch(() => {})
}

// 获取采集器详情（编辑模式）
const fetchCollectorDetail = async (id) => {
  try {
    const res = await getCollectorById(id)
    if (res.code === 200 && res.data) {
      const data = res.data
      
      // 填充基本信息
      collectorForm.collectorName = data.collectorName
      collectorForm.collectorType = data.collectorType
      collectorForm.description = data.description
      collectorForm.isMain = data.isMain === 1
      collectorForm.host = data.host
      collectorForm.port = data.port
      collectorForm.networkZone = data.networkZone
      
      // 解析配置参数
      if (data.configParams) {
        try {
          const configParams = JSON.parse(data.configParams)
          if (configParams) {
            // 填充认证信息
            collectorForm.authType = configParams.authType || 'none'
            collectorForm.username = data.username || ''
            collectorForm.password = data.password ? '********' : '' // 出于安全考虑，不显示实际密码
            collectorForm.apiKey = data.apiKey ? '********' : ''
            
            // 填充高级配置
            if (configParams.advancedConfig) {
              Object.assign(collectorForm.advancedConfig, configParams.advancedConfig)
            }
            
            // 填充自定义参数
            if (configParams.customParams) {
              collectorForm.customParams = Object.entries(configParams.customParams).map(([key, value]) => ({ key, value }))
            }
          }
        } catch (e) {
          console.error('解析配置参数失败:', e)
        }
      }
    } else {
      ElMessage.error('获取采集器详情失败')
      router.push({ name: 'CollectorManagement' })
    }
  } catch (error) {
    console.error('获取采集器详情失败:', error)
    ElMessage.error('获取采集器详情失败')
    router.push({ name: 'CollectorManagement' })
  }
}

// 初始化
onMounted(async () => {
  await fetchNetworkZones()
  
  if (isEdit.value && route.query.id) {
    await fetchCollectorDetail(route.query.id)
  } else {
    // 新增模式，设置默认值
    deployHost.value = collectorForm.host // 默认使用采集器主机地址
  }
})

// 在第二步时，生成注册令牌
watch(activeStep, async (newStep) => {
  if (newStep === 2 && !isEdit.value) {
    await generateToken()
  }
})
</script>

<style scoped>
.collector-add {
  padding: 10px;
}

.page-header {
  margin-bottom: 20px;
}

.steps-container {
  margin-bottom: 30px;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #e0e0e0;
}

.section-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-weight: 600;
  color: #606266;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.form-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.test-result {
  margin-left: 15px;
  display: inline-flex;
  align-items: center;
}

.test-result .el-icon {
  margin-right: 5px;
}

.test-result.success {
  color: #67C23A;
}

.test-result.failed {
  color: #F56C6C;
}

.custom-param-item {
  display: flex;
  margin-bottom: 10px;
  align-items: center;
}

.param-key {
  width: 200px;
  margin-right: 10px;
}

.param-value {
  flex: 1;
  margin-right: 10px;
}

.cert-upload,
.key-upload,
.key-file-upload {
  width: 100%;
}

/* 步骤式内容 */
.step-content {
  min-height: 400px;
}

/* 令牌区域 */
.token-section {
  margin-bottom: 30px;
}

.token-box {
  background-color: #f8f9fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.token-value {
  flex: 1;
  font-family: monospace;
  word-break: break-all;
  padding-right: 15px;
}

.token-actions {
  white-space: nowrap;
}

.token-expire {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.token-expire .el-icon {
  margin-right: 5px;
}

/* 部署说明 */
.deployment-section {
  margin-bottom: 30px;
}

.installation-steps {
  background-color: #f8f9fa;
  border-left: 3px solid #409EFF;
  padding: 20px;
  margin-bottom: 20px;
}

.download-options {
  display: flex;
  gap: 10px;
  margin: 15px 0;
  flex-wrap: wrap;
}

.code-block {
  background-color: #f8f9fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 15px;
  overflow: hidden;
}

.code-header {
  background-color: #ebeef5;
  padding: 8px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.code-block pre {
  margin: 0;
  padding: 15px;
  font-family: monospace;
  white-space: pre-wrap;
  color: #333;
}

.remote-deploy-section {
  background-color: #f8f9fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 20px;
  margin-top: 15px;
}

/* 激活状态 */
.activation-container {
  padding: 20px;
}

.activation-status {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  margin-bottom: 30px;
}

.waiting-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.waiting-section .el-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 20px;
}

.waiting-section h5 {
  font-size: 20px;
  margin-bottom: 10px;
}

.countdown {
  font-size: 16px;
  color: #909399;
}

.success-section {
  color: #67C23A;
}

.success-section .el-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.success-section h5 {
  font-size: 20px;
  margin-bottom: 20px;
}

.activation-details {
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
  padding: 15px;
  border-radius: 4px;
  text-align: left;
  margin-top: 20px;
  min-width: 300px;
}

.failed-section {
  color: #F56C6C;
}

.failed-section .el-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.failed-section h5 {
  font-size: 20px;
  margin-bottom: 20px;
}

.error-details {
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  padding: 15px;
  border-radius: 4px;
  text-align: left;
  margin-top: 20px;
  min-width: 300px;
}

.troubleshooting {
  margin-top: 15px;
}

.troubleshooting h6 {
  font-size: 14px;
  margin-bottom: 10px;
}

.troubleshooting ul {
  padding-left: 20px;
}

.troubleshooting li {
  margin-bottom: 5px;
}
</style> 