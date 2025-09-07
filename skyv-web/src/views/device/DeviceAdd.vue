<template>
  <div class="device-add-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">{{ isEdit ? '编辑设备' : '添加设备' }}</div>
          <div class="header-actions">
            <el-button @click="$router.push('/device')">
              <el-icon><Back /></el-icon>返回设备列表
            </el-button>
          </div>
        </div>
      </template>

      <!-- 步骤条 -->
      <div class="wizard-steps">
        <div class="step-item" :class="{ 'active': activeStep >= 0, 'completed': activeStep > 0 }">
          <div class="step-icon">1</div>
          <div class="step-text">基本信息</div>
          <div class="step-line" v-if="activeStep > 0"></div>
        </div>
        <div class="step-item" :class="{ 'active': activeStep >= 1, 'completed': activeStep > 1 }">
          <div class="step-icon">2</div>
          <div class="step-text">网络设置</div>
          <div class="step-line" v-if="activeStep > 1"></div>
        </div>
        <div class="step-item" :class="{ 'active': activeStep >= 2, 'completed': activeStep > 2 }">
          <div class="step-icon">3</div>
          <div class="step-text">高级配置</div>
          <div class="step-line" v-if="activeStep > 2"></div>
        </div>
        <div class="step-item" :class="{ 'active': activeStep >= 3 }">
          <div class="step-icon">4</div>
          <div class="step-text">确认信息</div>
        </div>
      </div>

      <!-- 表单内容 -->
      <div v-loading="loading">
        <!-- 步骤1：基本信息 -->
        <div v-show="activeStep === 0">
          <!-- 模板选择 -->
          <div class="form-section">
            <div class="form-section-title">
              <el-icon><Document /></el-icon> 从模板创建
            </div>
            <el-row :gutter="20">
              <el-col :span="6" v-for="template in deviceTemplates" :key="template.id">
                <div class="template-card" :class="{ 'selected': selectedTemplate === template.id }" @click="selectTemplate(template)">
                  <div class="template-header">
                    <el-icon v-if="template.type === 'CAMERA'"><VideoCamera /></el-icon>
                    <el-icon v-else-if="template.type === 'SENSOR'"><Odometer /></el-icon>
                    <el-icon v-else-if="template.type === 'ACCESS'"><Key /></el-icon>
                    <el-icon v-else><Monitor /></el-icon>
                    {{ template.name }}
                  </div>
                  <div class="template-body">
                    <p><strong>类型:</strong> {{ getDeviceTypeText(template.type) }}</p>
                    <p><strong>适用:</strong> {{ template.applicableScenario }}</p>
                    <p><strong>描述:</strong> {{ template.description }}</p>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <el-divider>
            <el-icon><Edit /></el-icon> 手动配置
          </el-divider>

          <el-form ref="basicFormRef" :model="deviceForm" :rules="basicRules" label-width="100px">
            <div class="form-section">
              <div class="form-section-title">
                <el-icon><InfoFilled /></el-icon> 基本信息
              </div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="设备名称" prop="name">
                    <el-input v-model="deviceForm.name" placeholder="请输入设备名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="设备编码" prop="code">
                    <el-input v-model="deviceForm.code" placeholder="请输入设备编码" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                          <el-form-item label="设备类型" prop="deviceTypeId">
          <el-select v-model="deviceForm.deviceTypeId" placeholder="请选择设备类型" style="width: 100%;">
            <el-option
              v-for="type in deviceTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="设备型号" prop="model">
                    <el-input v-model="deviceForm.model" placeholder="请输入设备型号" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            
            <div class="form-section">
              <div class="form-section-title">
                <el-icon><Management /></el-icon> 分组与区域
              </div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="设备分组" prop="groupId">
                    <el-select v-model="deviceForm.groupId" placeholder="请选择设备分组" style="width: 100%;">
                      <el-option 
                        v-for="group in deviceGroups" 
                        :key="group.id" 
                        :label="group.name" 
                        :value="group.id" 
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所属区域" prop="areaId">
                    <el-select v-model="deviceForm.areaId" placeholder="请选择所属区域" style="width: 100%;">
                      <el-option 
                        v-for="area in deviceAreas" 
                        :key="area.id" 
                        :label="area.name" 
                        :value="area.id" 
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="设备标签" prop="tags">
                    <el-select v-model="deviceForm.tags" multiple placeholder="请选择标签" style="width: 100%;">
                      <el-option 
                        v-for="tag in deviceTags" 
                        :key="tag.id" 
                        :label="tag.name" 
                        :value="tag.id" 
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="设备位置" prop="location">
                    <el-input v-model="deviceForm.location" placeholder="请输入设备位置" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            
            <div class="form-section">
              <div class="form-section-title">
                <el-icon><DocumentCopy /></el-icon> 描述信息
              </div>
              <el-form-item label="设备描述" prop="description">
                <el-input v-model="deviceForm.description" type="textarea" rows="3" placeholder="请输入设备描述" />
              </el-form-item>
            </div>
          </el-form>
        </div>

        <!-- 步骤2：网络设置 -->
        <div v-show="activeStep === 1">
          <el-form ref="networkFormRef" :model="deviceForm" :rules="networkRules" label-width="100px">
            <div class="form-section">
              <div class="form-section-title">
                <el-icon><Connection /></el-icon> 网络连接
              </div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="IP地址" prop="ipAddress">
                    <el-input v-model="deviceForm.ipAddress" placeholder="请输入IP地址" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口" prop="port">
                    <el-input-number v-model="deviceForm.port" :min="1" :max="65535" placeholder="请输入端口" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="MAC地址" prop="macAddress">
                    <el-input v-model="deviceForm.macAddress" placeholder="请输入MAC地址" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="连接方式" prop="connectionType">
                    <el-select v-model="deviceForm.connectionType" placeholder="请选择连接方式" style="width: 100%;">
                      <el-option label="有线" value="WIRED" />
                      <el-option label="无线" value="WIRELESS" />
                      <el-option label="蜂窝网络" value="CELLULAR" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            
            <div class="form-section">
              <div class="form-section-title">
                <el-icon><Lock /></el-icon> 认证信息
              </div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="deviceForm.username" placeholder="请输入用户名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="密码" prop="password">
                    <el-input v-model="deviceForm.password" type="password" placeholder="请输入密码" show-password />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="认证方式" prop="authType">
                    <el-select v-model="deviceForm.authType" placeholder="请选择认证方式" style="width: 100%;">
                      <el-option label="基本认证" value="BASIC" />
                      <el-option label="摘要认证" value="DIGEST" />
                      <el-option label="令牌认证" value="TOKEN" />
                      <el-option label="无认证" value="NONE" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="自动重连" prop="autoReconnect">
                    <el-switch v-model="deviceForm.autoReconnect" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            
            <div class="form-section">
              <div class="form-section-title">
                <el-icon><Monitor /></el-icon> 网络参数
              </div>
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="子网掩码" prop="subnetMask">
                    <el-input v-model="deviceForm.subnetMask" placeholder="例如：255.255.255.0" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="默认网关" prop="gateway">
                    <el-input v-model="deviceForm.gateway" placeholder="请输入默认网关" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="DNS" prop="dns">
                    <el-input v-model="deviceForm.dns" placeholder="请输入DNS" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-form>
        </div>

        <!-- 步骤3：高级配置 -->
        <div v-show="activeStep === 2">
          <el-form ref="advancedFormRef" :model="deviceForm" label-width="100px">
            <div class="form-section">
              <div class="form-section-title">
                <el-icon><Setting /></el-icon> 设备配置
              </div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="设备状态" prop="status">
                    <el-select v-model="deviceForm.status" placeholder="请选择设备状态" style="width: 100%;">
                      <el-option label="离线" :value="0" />
                      <el-option label="在线" :value="1" />
                      <el-option label="故障" :value="2" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="自动检测" prop="autoDetect">
                    <el-switch v-model="deviceForm.autoDetect" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <div v-if="deviceForm.type === 'CAMERA'" class="camera-params">
                <el-divider content-position="left">摄像头参数</el-divider>
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-form-item label="分辨率" prop="resolution">
                      <el-select v-model="deviceForm.resolution" placeholder="请选择分辨率" style="width: 100%;">
                        <el-option label="1920x1080" value="1920x1080" />
                        <el-option label="1280x720" value="1280x720" />
                        <el-option label="640x480" value="640x480" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="帧率" prop="frameRate">
                      <el-input v-model="deviceForm.frameRate" placeholder="例如：25fps" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="码率" prop="bitRate">
                      <el-input v-model="deviceForm.bitRate" placeholder="例如：4Mbps" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-form-item label="图像格式" prop="imageFormat">
                      <el-select v-model="deviceForm.imageFormat" placeholder="请选择图像格式" style="width: 100%;">
                        <el-option label="H.264" value="H.264" />
                        <el-option label="H.265" value="H.265" />
                        <el-option label="MJPEG" value="MJPEG" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="云台控制" prop="ptzControl">
                      <el-switch v-model="deviceForm.ptzControl" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="红外功能" prop="infrared">
                      <el-switch v-model="deviceForm.infrared" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </div>
            
            <div class="form-section">
              <div class="form-section-title">
                <el-icon><SetUp /></el-icon> 协议配置
              </div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="协议类型" prop="protocol">
                    <el-select v-model="deviceForm.protocol" placeholder="请选择协议类型" style="width: 100%;">
                      <el-option label="RTSP" value="RTSP" />
                      <el-option label="ONVIF" value="ONVIF" />
                      <el-option label="MODBUS" value="MODBUS" />
                      <el-option label="HTTP" value="HTTP" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="协议版本" prop="protocolVersion">
                    <el-input v-model="deviceForm.protocolVersion" placeholder="请输入协议版本" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="传输方式" prop="transportProtocol">
                    <el-select v-model="deviceForm.transportProtocol" placeholder="请选择传输方式" style="width: 100%;">
                      <el-option label="TCP" value="TCP" />
                      <el-option label="UDP" value="UDP" />
                      <el-option label="HTTP" value="HTTP" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="数据路径" prop="dataPath">
                    <el-input v-model="deviceForm.dataPath" placeholder="请输入数据路径" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            
            <div class="form-section">
              <div class="form-section-title">
                <el-icon><Opportunity /></el-icon> 高级选项
              </div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="心跳间隔" prop="heartbeatInterval">
                    <el-input-number v-model="deviceForm.heartbeatInterval" :min="5" :max="3600" placeholder="单位：秒" style="width: 100%;" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="超时时间" prop="timeout">
                    <el-input-number v-model="deviceForm.timeout" :min="1" :max="60" placeholder="单位：秒" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-form>
        </div>

        <!-- 步骤4：确认信息 -->
        <div v-show="activeStep === 3">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card class="info-card">
                <template #header>
                  <div class="card-title">基本信息</div>
                </template>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="设备名称">{{ deviceForm.name }}</el-descriptions-item>
                  <el-descriptions-item label="设备编码">{{ deviceForm.code }}</el-descriptions-item>
                  <el-descriptions-item label="设备类型">{{ getDeviceTypeText(deviceForm.deviceTypeId) }}</el-descriptions-item>
                  <el-descriptions-item label="设备型号">{{ deviceForm.model || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="设备分组">{{ getGroupText(deviceForm.groupId) }}</el-descriptions-item>
                  <el-descriptions-item label="所属区域">{{ getAreaText(deviceForm.areaId) }}</el-descriptions-item>
                  <el-descriptions-item label="设备位置">{{ deviceForm.location || '-' }}</el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="info-card">
                <template #header>
                  <div class="card-title">网络信息</div>
                </template>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="IP地址">{{ deviceForm.ipAddress }}</el-descriptions-item>
                  <el-descriptions-item label="端口">{{ deviceForm.port }}</el-descriptions-item>
                  <el-descriptions-item label="MAC地址">{{ deviceForm.macAddress || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="连接方式">{{ getConnectionTypeText(deviceForm.connectionType) }}</el-descriptions-item>
                  <el-descriptions-item label="认证方式">{{ getAuthTypeText(deviceForm.authType) }}</el-descriptions-item>
                  <el-descriptions-item label="协议类型">{{ deviceForm.protocol || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="设备状态">
                    <el-tag :type="getDeviceStatusType(deviceForm.status)">
                      {{ getDeviceStatusText(deviceForm.status) }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-col>
          </el-row>
          
          <el-card class="info-card">
            <template #header>
              <div class="card-title">设备标签</div>
            </template>
            <div class="tag-list">
              <el-tag 
                v-for="(tagId, index) in deviceForm.tags" 
                :key="tagId"
                :type="getTagType(index)" 
                class="tag-item"
                effect="plain"
              >
                {{ getTagText(tagId) }}
              </el-tag>
              <el-empty v-if="!deviceForm.tags || deviceForm.tags.length === 0" description="暂无标签" />
            </div>
          </el-card>
          
          <el-card class="info-card">
            <template #header>
              <div class="card-title">设备描述</div>
            </template>
            <div class="device-description">
              {{ deviceForm.description || '暂无描述' }}
            </div>
          </el-card>
          
          <el-alert
            v-if="deviceForm.type === 'CAMERA'"
            type="warning"
            :closable="false"
            title="特别注意"
            description="请确保摄像头的使用符合当地法律法规，并在安装前告知相关人员。"
            show-icon
            class="device-alert"
          />
        </div>

        <!-- 步骤按钮 -->
        <div class="step-actions">
          <el-button @click="prevStep" v-if="activeStep > 0">
            <el-icon><ArrowLeft /></el-icon>上一步
          </el-button>
          <el-button type="primary" @click="nextStep" v-if="activeStep < 3">
            下一步<el-icon><ArrowRight /></el-icon>
          </el-button>
          <el-button type="success" @click="submitForm" v-if="activeStep === 3">
            <el-icon><Check /></el-icon>{{ isEdit ? '保存修改' : '确认提交' }}
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/date'
import { 
  createDevice, 
  updateDevice, 
  getDeviceById,
  getDeviceTypes,
  getAllDeviceGroups,
  getDeviceAreaTree,
  getAllDeviceTags,
  getDeviceTemplates
} from '@/api/device'
import {
  Back, Edit, VideoCamera, Connection, Monitor, Document, Check,
  InfoFilled, Management, DocumentCopy, Lock, Setting, SetUp,
  Opportunity, Odometer, Key, ArrowLeft, ArrowRight
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 是否为编辑模式
const isEdit = computed(() => route.meta.isEdit || false)

// 当前步骤
const activeStep = ref(0)

// 加载状态
const loading = ref(false)

// 设备模板
const deviceTemplates = ref([
  {
    id: 1,
    name: '会议室摄像头',
    type: 'CAMERA',
    typeCode: 'CAMERA',
    applicableScenario: '会议室、办公室',
    description: '高清会议室摄像头，适用于中小型会议室',
    model: 'HK-DS2CD2032-I',
    ipAddress: '192.168.1.100',
    port: 554,
    protocol: 'ONVIF'
  },
  {
    id: 2,
    name: '温湿度传感器',
    type: 'SENSOR',
    typeCode: 'SENSOR',
    applicableScenario: '机房、仓库',
    description: '监测环境温湿度，支持远程报警',
    model: 'TH-100',
    ipAddress: '192.168.1.101',
    port: 80,
    protocol: 'MODBUS'
  },
  {
    id: 3,
    name: '门禁控制器',
    type: 'ACCESS',
    typeCode: 'ACCESS',
    applicableScenario: '出入口、重要区域',
    description: '支持刷卡、密码、人脸识别多种验证方式',
    model: 'AC-200',
    ipAddress: '192.168.1.102',
    port: 8000,
    protocol: 'HTTP'
  },
  {
    id: 4,
    name: '停车场摄像头',
    type: 'CAMERA',
    typeCode: 'CAMERA',
    applicableScenario: '停车场、出入口',
    description: '支持车牌识别，适用于室外环境',
    model: 'HK-DS2CD4A26FWD-IZS',
    ipAddress: '192.168.1.103',
    port: 554,
    protocol: 'RTSP'
  }
])

// 选中的模板ID
const selectedTemplate = ref(null)

// 基础数据
const deviceGroups = ref([])
const deviceAreas = ref([])
const deviceTags = ref([])
const deviceTypes = ref([])

// 表单引用
const basicFormRef = ref(null)
const networkFormRef = ref(null)
const advancedFormRef = ref(null)

// 设备表单数据
const deviceForm = reactive({
  name: '',
  code: '',
  deviceTypeId: null,  // 改为与后端实体一致的字段名
  model: '',
  location: '',
  description: '',
  ipAddress: '',
  port: 554,
  macAddress: '',
  subnetMask: '',
  gateway: '',
  dns: '',
  connectionType: 'WIRED',
  username: 'admin',
  password: '',
  authType: 'DIGEST',
  autoReconnect: true,
  groupId: null,
  areaId: null,
  tags: [],
  status: 1,
  autoDetect: true,
  protocol: 'ONVIF',
  protocolVersion: '2.0',
  transportProtocol: 'RTSP',
  dataPath: '',
  resolution: '1920x1080',
  frameRate: '25fps',
  bitRate: '4Mbps',
  imageFormat: 'H.264',
  ptzControl: false,
  infrared: false,
  heartbeatInterval: 30,
  timeout: 5
})

// 基本信息验证规则
const basicRules = {
  name: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入设备编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  deviceTypeId: [  // 改为与后端实体一致的字段名
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ]
}

// 网络设置验证规则
const networkRules = {
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        // 支持IPv4和IPv6格式
        const ipv4Pattern = /^(\d{1,3}\.){3}\d{1,3}$/
        const ipv6Pattern = /^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::1$|^::$/
        
        if (ipv4Pattern.test(value) || ipv6Pattern.test(value)) {
          callback()
        } else {
          callback(new Error('IP地址格式不正确，支持IPv4和IPv6格式'))
        }
      },
      trigger: 'blur'
    }
  ],
  port: [
    { required: true, message: '请输入端口号', trigger: 'blur' },
    { type: 'number', min: 1, max: 65535, message: '端口范围为1-65535', trigger: 'blur' }
  ],
  macAddress: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        // MAC地址格式验证 (XX:XX:XX:XX:XX:XX)
        const macPattern = /^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/
        if (macPattern.test(value)) {
          callback()
        } else {
          callback(new Error('MAC地址格式不正确，格式：XX:XX:XX:XX:XX:XX'))
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取设备状态类型
const getDeviceStatusType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取设备状态文本
const getDeviceStatusText = (status) => {
  const textMap = {
    0: '离线',
    1: '在线',
    2: '故障'
  }
  return textMap[status] || '未知'
}

// 获取设备类型文本
const getDeviceTypeText = (deviceTypeId) => {  // 改为与后端实体一致的字段名
  if (!deviceTypeId) return '-'
  const type = deviceTypes.value.find(t => t.id === deviceTypeId)
  return type ? type.name : '未知类型'
}

// 获取分组文本
const getGroupText = (groupId) => {
  if (!groupId) return '-'
  const group = deviceGroups.value.find(g => g.id === groupId)
  return group ? group.name : '未知分组'
}

// 获取区域文本
const getAreaText = (areaId) => {
  if (!areaId) return '-'
  const area = deviceAreas.value.find(a => a.id === areaId)
  return area ? area.name : '未知区域'
}

// 获取标签文本
const getTagText = (tagId) => {
  if (!tagId) return ''
  const tag = deviceTags.value.find(t => t.id === tagId)
  return tag ? tag.name : '未知标签'
}

// 获取连接方式文本
const getConnectionTypeText = (type) => {
  const typeMap = {
    'WIRED': '有线',
    'WIRELESS': '无线',
    'CELLULAR': '蜂窝网络'
  }
  return typeMap[type] || type || '-'
}

// 获取认证方式文本
const getAuthTypeText = (type) => {
  const typeMap = {
    'BASIC': '基本认证',
    'DIGEST': '摘要认证',
    'TOKEN': '令牌认证',
    'NONE': '无认证'
  }
  return typeMap[type] || type || '-'
}

// 获取标签类型
const getTagType = (index) => {
  const types = ['', 'success', 'warning', 'danger', 'info']
  return types[index % types.length]
}

// 选择模板
const selectTemplate = (template) => {
  selectedTemplate.value = template.id
  
  // 根据typeCode找到对应的设备类型ID
  const deviceType = deviceTypes.value.find(t => t.code === template.typeCode)
  const deviceTypeId = deviceType ? deviceType.id : null  // 改为与后端实体一致的字段名
  
  // 填充表单数据
  deviceForm.name = template.name
  deviceForm.code = `DEV${new Date().getTime().toString().substr(-8)}`
  deviceForm.deviceTypeId = deviceTypeId  // 改为与后端实体一致的字段名
  deviceForm.model = template.model
  deviceForm.description = template.description
  deviceForm.ipAddress = template.ipAddress
  deviceForm.port = template.port
  deviceForm.protocol = template.protocol
  
  // 根据设备类型设置默认值
  if (template.type === 'CAMERA') {
    deviceForm.resolution = '1920x1080'
    deviceForm.frameRate = '25fps'
    deviceForm.transportProtocol = 'RTSP'
  } else if (template.type === 'SENSOR') {
    deviceForm.heartbeatInterval = 60
  } else if (template.type === 'ACCESS') {
    deviceForm.authType = 'TOKEN'
  }
}

// 上一步
const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

// 下一步
const nextStep = async () => {
  if (activeStep.value === 0) {
    // 验证基本信息
    await basicFormRef.value.validate((valid) => {
      if (valid) {
        activeStep.value++
      }
    })
  } else if (activeStep.value === 1) {
    // 验证网络设置
    await networkFormRef.value.validate((valid) => {
      if (valid) {
        activeStep.value++
      }
    })
  } else if (activeStep.value < 3) {
    activeStep.value++
  }
}

// 提交表单
const submitForm = async () => {
  loading.value = true
  try {
    let response
    if (isEdit.value) {
      // 更新设备
      response = await updateDevice(route.params.id, deviceForm)
    } else {
      // 创建设备
      response = await createDevice(deviceForm)
    }
    
    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '设备更新成功' : '设备添加成功')
      router.push('/device')
    } else {
      ElMessage.error(response.message || (isEdit.value ? '设备更新失败' : '设备添加失败'))
    }
  } catch (error) {
    console.error('提交设备数据失败', error)
    ElMessage.error(isEdit.value ? '设备更新失败：' + (error.message || '网络错误') : '设备添加失败：' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 扁平化区域树形结构
const flattenAreas = (areas, result = []) => {
  areas.forEach(area => {
    result.push(area)
    if (area.children && area.children.length > 0) {
      flattenAreas(area.children, result)
    }
  })
  return result
}

// 加载基础数据
const loadBasicData = async () => {
  try {
    // 并行加载设备分组、区域、标签、类型数据
    const [groupsRes, areasRes, tagsRes, typesRes] = await Promise.all([
      getAllDeviceGroups(),
      getDeviceAreaTree(),
      getAllDeviceTags(),
      getDeviceTypes({})  // 使用getDeviceTypes并传递空参数
    ])
    
    if (groupsRes.code === 200) {
      deviceGroups.value = groupsRes.data || []
    }
    
    if (areasRes.code === 200) {
      // 扁平化树形结构用于选择器
      const areaTree = areasRes.data || []
      deviceAreas.value = flattenAreas(areaTree)
    }
    
    if (tagsRes.code === 200) {
      deviceTags.value = tagsRes.data || []
    }
    
    if (typesRes.code === 200) {
      deviceTypes.value = typesRes.data || []
    }
  } catch (error) {
    console.error('加载基础数据失败', error)
    ElMessage.warning('部分基础数据加载失败，可能影响部分功能')
  }
}



// 重置表单数据
const resetForm = () => {
  deviceForm.name = ''
  deviceForm.code = ''
  deviceForm.deviceTypeId = null  // 改为与后端实体一致的字段名
  deviceForm.location = ''
  deviceForm.description = ''
  deviceForm.ipAddress = ''
  deviceForm.port = 554
  deviceForm.username = ''
  deviceForm.password = ''
  deviceForm.groupId = null
  deviceForm.areaId = null
  deviceForm.tags = []
  deviceForm.status = 0
  
  // 重置步骤
  activeStep.value = 0
  
  // 重置表单校验
  if (basicFormRef.value) basicFormRef.value.resetFields()
  if (networkFormRef.value) networkFormRef.value.resetFields()
  if (advancedFormRef.value) advancedFormRef.value.resetFields()
}

// 获取设备详情
const getDevice = async (id) => {
  loading.value = true
  try {
    const res = await getDeviceById(id)
    if (res.data) {
      Object.assign(deviceForm, res.data)
    }
  } catch (error) {
    console.error('获取设备详情失败', error)
    ElMessage.error('获取设备详情失败')
  } finally {
    loading.value = false
  }
}

// 初始化
const initForm = async () => {
  // 先加载基础数据
  await loadBasicData()
  
  if (isEdit.value && route.params.id) {
    await getDevice(route.params.id)
  } else {
    resetForm()
  }
}

// 监听路由变化
watch(
  () => route.path,
  () => {
    initForm()
  }
)

// 组件挂载时初始化
onMounted(() => {
  initForm()
})
</script>

<style scoped>
.device-add-container {
  padding: 6px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

/* 步骤条样式 */
.wizard-steps {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
  position: relative;
}

.step-item {
  flex: 1;
  text-align: center;
  position: relative;
  z-index: 1;
}

.step-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #e9ecef;
  color: #909399;
  margin: 0 auto 8px;
  font-size: 14px;
  font-weight: bold;
}

.step-text {
  font-size: 14px;
  color: #909399;
}

.step-line {
  position: absolute;
  top: 15px;
  left: 50%;
  width: 100%;
  height: 2px;
  background-color: var(--el-color-primary);
  z-index: -1;
}

/* 活动步骤样式 */
.step-item.active .step-icon {
  background-color: var(--el-color-primary);
  color: #fff;
}

.step-item.active .step-text {
  color: var(--el-color-primary);
  font-weight: bold;
}

/* 完成步骤样式 */
.step-item.completed .step-icon {
  background-color: var(--el-color-success);
  color: #fff;
}

/* 表单区域样式 */
.form-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.form-section:last-child {
  border-bottom: none;
}

.form-section-title {
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
}

.form-section-title .el-icon {
  margin-right: 6px;
  font-size: 18px;
  color: var(--el-color-primary);
}

/* 模板卡片样式 */
.template-card {
  cursor: pointer;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  transition: all 0.3s;
  margin-bottom: 20px;
  overflow: hidden;
}

.template-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.template-card.selected {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 3px rgba(var(--el-color-primary-rgb), 0.25);
}

.template-header {
  background-color: #f8f9fa;
  padding: 12px;
  text-align: center;
  font-weight: bold;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.template-body {
  padding: 15px;
  font-size: 14px;
}

.template-body p {
  margin: 8px 0;
}

/* 信息确认卡片 */
.info-card {
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px 0;
}

.tag-item {
  margin-right: 5px;
}

.device-description {
  padding: 10px 0;
  min-height: 60px;
}

.device-alert {
  margin-bottom: 20px;
}

.step-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.camera-params {
  margin-top: 20px;
}
</style> 