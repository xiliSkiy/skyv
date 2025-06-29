<template>
  <div class="task-create-metrics-container">
    <!-- 标题区域 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4>创建采集任务</h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">控制台</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/task' }">采集任务调度</el-breadcrumb-item>
          <el-breadcrumb-item>创建任务</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="action-buttons">
        <el-button plain size="small" @click="saveDraft" class="me-2">
          <el-icon><Document /></el-icon> 保存草稿
        </el-button>
        <el-button plain size="small" @click="cancel" type="danger">
          <el-icon><Close /></el-icon> 取消
        </el-button>
      </div>
    </div>

    <!-- 步骤指示器 -->
    <div class="step-indicator mb-4">
      <el-steps :active="currentStep" finish-status="success">
        <el-step title="基本信息" />
        <el-step title="设备选择" />
        <el-step title="指标配置" />
        <el-step title="调度设置" />
      </el-steps>
    </div>

    <!-- 指标配置内容 -->
    <el-card class="metrics-panel">
      <template #header>
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <el-icon><DataLine /></el-icon> 指标配置方式
          </div>
        </div>
      </template>
      
      <!-- 配置方式导航标签 -->
      <el-tabs v-model="activeConfigTab" class="config-tabs">
        <el-tab-pane label="使用模板" name="template">
          <div class="row mb-4">
            <div class="col-md-6">
              <el-input v-model="templateSearch" placeholder="搜索模板名称或指标..." clearable>
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            <div class="col-md-3">
              <el-select v-model="deviceTypeFilter" placeholder="所有设备类型" style="width: 100%">
                <el-option label="所有设备类型" value="" />
                <el-option label="摄像头" value="camera" />
                <el-option label="传感器" value="sensor" />
                <el-option label="控制器" value="controller" />
              </el-select>
            </div>
            <div class="col-md-3">
              <el-select v-model="templateSort" placeholder="排序方式" style="width: 100%">
                <el-option label="按名称排序" value="name" />
                <el-option label="按最近使用排序" value="recent" />
                <el-option label="按指标数量排序" value="count" />
              </el-select>
            </div>
          </div>
          
          <div class="row template-list">
            <div v-for="template in filteredTemplates" :key="template.id" class="col-md-4 mb-4">
              <div 
                class="template-card" 
                :class="{ selected: selectedTemplate === template.id }"
                @click="selectTemplate(template)"
              >
                <div class="template-header">
                  <div class="template-title">{{ template.name }}</div>
                  <div class="template-metrics">{{ template.metricCount || 0 }}个指标</div>
                  <div class="template-tags">
                    <el-tag size="small" v-for="tag in template.tags" :key="tag" class="me-1">{{ tag }}</el-tag>
                  </div>
                  <i :class="['template-icon', 'fas', getTemplateIcon(template.category)]"></i>
                </div>
                <div class="template-body">
                  <div class="small text-muted mb-3">{{ template.description || '暂无描述' }}</div>
                  <ul class="small ps-3 mb-0">
                    <li v-for="(metric, index) in template.metrics?.slice(0, 5)" :key="index">
                      {{ metric.name || metric.metricName }}
                    </li>
                    <li v-if="template.metrics?.length > 5">...</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
          
          <el-alert
            v-if="selectedTemplate"
            type="info"
            show-icon
            :closable="false"
            class="mt-2"
          >
            <template #title>
              <strong>已选择"{{ getSelectedTemplateName() }}"模板</strong>
            </template>
            <p class="mb-0">该模板包含{{ getSelectedTemplateMetricCount() }}个指标，将应用于所有{{ getSelectedDeviceCount() }}台已选设备。您可以在下一步中调整采集频率和报警阈值。</p>
          </el-alert>
        </el-tab-pane>
        
        <el-tab-pane label="自定义配置" name="custom">
          <div class="row">
            <!-- 左侧指标选择 -->
            <div class="col-md-5">
              <el-card class="border-0 mb-3">
                <template #header>
                  <div class="d-flex justify-content-between align-items-center">
                    <span><el-icon><List /></el-icon> 可用指标</span>
                    <el-input
                      v-model="metricSearch"
                      placeholder="搜索指标..."
                      size="small"
                      style="width: 200px"
                      clearable
                    >
                      <template #prefix>
                        <el-icon><Search /></el-icon>
                      </template>
                    </el-input>
                  </div>
                </template>
                
                <div class="metrics-list">
                  <!-- 指标分组 -->
                  <div v-for="(group, groupName) in groupedAvailableMetrics" :key="groupName">
                    <div class="list-group-item bg-light">
                      <el-icon><InfoCircle /></el-icon> {{ groupName }}
                    </div>
                    
                    <div 
                      v-for="metric in group" 
                      :key="metric.id"
                      class="metric-item"
                      :class="{ selected: isMetricSelected(metric.id) }"
                    >
                      <div class="metric-header">
                        <div class="metric-title">
                          <el-icon :class="getMetricIcon(metric.type)"></el-icon> {{ metric.name }}
                        </div>
                        <span class="metric-type" :class="getMetricTypeClass(metric.type)">
                          {{ getMetricTypeLabel(metric.type) }}
                        </span>
                      </div>
                      <div class="metric-description">
                        {{ metric.description || '暂无描述' }}
                      </div>
                      <div class="metric-details">
                        <span class="metric-detail-item">
                          <el-icon><Tag /></el-icon> {{ getMetricCategoryLabel(metric.category) }}
                        </span>
                        <span class="metric-detail-item">
                          <el-icon><Clock /></el-icon> 默认间隔: {{ metric.defaultInterval || '5分钟' }}
                        </span>
                        <span v-if="metric.unit" class="metric-detail-item">
                          <el-icon><Ruler /></el-icon> 单位: {{ metric.unit }}
                        </span>
                      </div>
                      <div class="d-flex justify-content-end mt-2">
                        <el-button 
                          v-if="!isMetricSelected(metric.id)"
                          type="primary" 
                          size="small"
                          @click="addMetricFromAvailable(metric)"
                        >
                          <el-icon><Plus /></el-icon> 添加
                        </el-button>
                        <el-button 
                          v-else
                          type="danger" 
                          size="small"
                          @click="removeMetricById(metric.id)"
                        >
                          <el-icon><Minus /></el-icon> 移除
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
            
            <!-- 右侧已选指标配置 -->
            <div class="col-md-7">
              <el-card class="border-0">
                <template #header>
                  <div class="d-flex justify-content-between align-items-center">
                    <span>
                      <el-icon><Check /></el-icon> 已选指标配置 ({{ metrics.length }}/10)
                    </span>
                    <el-button 
                      v-if="metrics.length > 0"
                      type="danger" 
                      size="small"
                      plain
                      @click="clearAllMetrics"
                    >
                      <el-icon><Delete /></el-icon> 清除所有选择
                    </el-button>
                  </div>
                </template>
                
                <div v-if="metrics.length === 0" class="empty-metrics">
                  <el-empty description="暂无指标配置">
                    <el-button type="primary" @click="showAddMetricDialog">添加指标</el-button>
                  </el-empty>
                </div>
                
                <DraggableMetrics
                  v-else
                  v-model="metrics"
                  @end="handleDragEnd"
                >
                  <template #item="{element}">
                    <div class="metric-card">
                      <div class="metric-header">
                        <div>
                          <h5 class="mb-0">{{ element.name }}</h5>
                          <el-tag size="small" :type="getMetricTypeTag(element.type)" class="me-1">
                            {{ getMetricTypeLabel(element.type) }}
                          </el-tag>
                          <small class="text-muted">适用于: {{ element.deviceCount }}台{{ getDeviceTypeLabel(element.deviceType) }}</small>
                        </div>
                        <div>
                          <el-icon class="drag-handle me-2"><ArrowUp /><ArrowDown /></el-icon>
                          <el-button plain size="small" circle @click="editMetric(element)">
                            <el-icon><Setting /></el-icon>
                          </el-button>
                          <el-button plain size="small" circle type="danger" @click="removeMetric(element)">
                            <el-icon><Delete /></el-icon>
                          </el-button>
                        </div>
                      </div>
                
                <div class="metric-body mt-3">
                  <!-- 人脸识别指标 -->
                  <template v-if="element.type === 'face_recognition'">
                    <div class="row">
                      <div class="col-md-6">
                        <div class="mb-3">
                          <label class="form-label">识别置信度阈值</label>
                          <el-slider
                            v-model="element.config.confidence"
                            :min="0"
                            :max="100"
                            :format-tooltip="value => `${value}%`"
                          />
                          <div class="slider-labels">
                            <span>低 (0%)</span>
                            <span>中 (50%)</span>
                            <span>高 (100%)</span>
                          </div>
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="mb-3">
                          <label class="form-label">人脸库选择</label>
                          <el-select v-model="element.config.faceDatabase" style="width: 100%">
                            <el-option label="员工人脸库" value="employee" />
                            <el-option label="访客人脸库" value="visitor" />
                            <el-option label="黑名单人脸库" value="blacklist" />
                          </el-select>
                        </div>
                      </div>
                    </div>
                    <el-switch
                      v-model="element.config.storeUnknownFaces"
                      active-text="保存未识别人脸"
                    />
                  </template>

                  <!-- 物体检测指标 -->
                  <template v-else-if="element.type === 'object_detection'">
                    <div class="mb-3">
                      <label class="form-label">检测对象</label>
                      <div class="row">
                        <div class="col-md-3 col-6">
                          <el-checkbox v-model="element.config.detectPerson">人员</el-checkbox>
                        </div>
                        <div class="col-md-3 col-6">
                          <el-checkbox v-model="element.config.detectVehicle">车辆</el-checkbox>
                        </div>
                        <div class="col-md-3 col-6">
                          <el-checkbox v-model="element.config.detectAnimal">动物</el-checkbox>
                        </div>
                        <div class="col-md-3 col-6">
                          <el-checkbox v-model="element.config.detectBag">包裹</el-checkbox>
                        </div>
                      </div>
                    </div>
                    <div class="row">
                      <div class="col-md-6">
                        <div class="mb-3">
                          <label class="form-label">检测置信度阈值</label>
                          <el-slider
                            v-model="element.config.confidence"
                            :min="0"
                            :max="100"
                            :format-tooltip="value => `${value}%`"
                          />
                          <div class="slider-labels">
                            <span>低 (0%)</span>
                            <span>中 (50%)</span>
                            <span>高 (100%)</span>
                          </div>
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="mb-3">
                          <label class="form-label">检测频率</label>
                          <el-select v-model="element.config.frequency" style="width: 100%">
                            <el-option label="每帧" value="every_frame" />
                            <el-option label="每秒1次" value="1_per_second" />
                            <el-option label="每5秒一次" value="5_per_second" />
                            <el-option label="每10秒一次" value="10_per_second" />
                          </el-select>
                        </div>
                      </div>
                    </div>
                  </template>

                  <!-- 温湿度数据指标 -->
                  <template v-else-if="element.type === 'temperature_humidity'">
                    <div class="row">
                      <div class="col-md-6">
                        <div class="mb-3">
                          <label class="form-label">温度正常范围</label>
                          <el-input-number
                            v-model="element.config.tempMin"
                            :min="-50"
                            :max="100"
                            class="me-2"
                          />
                          -
                          <el-input-number
                            v-model="element.config.tempMax"
                            :min="-50"
                            :max="100"
                            class="ms-2"
                          />
                          °C
                        </div>
                      </div>
                      <div class="col-md-6">
                        <div class="mb-3">
                          <label class="form-label">湿度正常范围</label>
                          <el-input-number
                            v-model="element.config.humidityMin"
                            :min="0"
                            :max="100"
                            class="me-2"
                          />
                          -
                          <el-input-number
                            v-model="element.config.humidityMax"
                            :min="0"
                            :max="100"
                            class="ms-2"
                          />
                          %
                        </div>
                      </div>
                    </div>
                    <div class="mb-3">
                      <label class="form-label">采样间隔</label>
                      <el-select v-model="element.config.samplingInterval" style="width: 100%">
                        <el-option label="10秒" value="10s" />
                        <el-option label="30秒" value="30s" />
                        <el-option label="1分钟" value="1m" />
                        <el-option label="5分钟" value="5m" />
                        <el-option label="10分钟" value="10m" />
                      </el-select>
                    </div>
                  </template>
                </div>
              </div>
            </template>
          </DraggableMetrics>
        </el-card>
      </el-col>

                <div class="collection-settings mt-3">
                  <div class="row mb-3">
                    <div class="col-md-6">
                      <label class="form-label">采集方式</label>
                      <el-select v-model="element.config.collectionMethod" style="width: 100%">
                        <el-option label="轮询获取" value="polling" />
                        <el-option label="定时采集" value="scheduled" />
                        <el-option label="变化触发" value="event" />
                      </el-select>
                    </div>
                    <div class="col-md-6">
                      <label class="form-label">采集频率</label>
                      <el-select v-model="element.config.frequency" style="width: 100%">
                        <el-option label="每分钟" value="1m" />
                        <el-option label="每5分钟" value="5m" />
                        <el-option label="每15分钟" value="15m" />
                        <el-option label="每30分钟" value="30m" />
                        <el-option label="每小时" value="1h" />
                      </el-select>
                    </div>
                  </div>
                  
                  <!-- 阈值配置 -->
                  <div class="threshold-config">
                    <div class="d-flex justify-content-between align-items-center">
                      <label class="form-label mb-0">报警阈值设置</label>
                      <el-switch v-model="element.config.enableAlert" active-text="启用报警" />
                    </div>
                    
                    <template v-if="element.config.enableAlert">
                      <div class="mt-3">
                        <label class="form-label">阈值范围</label>
                        <div class="threshold-ranges">
                          <div class="threshold-range range-normal"></div>
                          <div class="threshold-range range-warning"></div>
                          <div class="threshold-range range-danger"></div>
                        </div>
                        <div class="threshold-labels">
                          <span>{{ getMinThreshold(element) }}</span>
                          <span>{{ getLowThreshold(element) }}</span>
                          <span>{{ getMidThreshold(element) }}</span>
                          <span>{{ getHighThreshold(element) }}</span>
                          <span>{{ getMaxThreshold(element) }}</span>
                        </div>
                      </div>
                      
                      <div class="row mt-3">
                        <div class="col-md-4">
                          <label class="form-label small">正常范围</label>
                          <div class="input-group">
                            <el-input-number v-model="element.config.normalMin" :min="getMinPossibleValue(element)" :max="element.config.normalMax" size="small" />
                            <span class="input-group-text">-</span>
                            <el-input-number v-model="element.config.normalMax" :min="element.config.normalMin" :max="getMaxPossibleValue(element)" size="small" />
                            <span class="input-group-text">{{ getMetricUnit(element) }}</span>
                          </div>
                        </div>
                        <div class="col-md-4">
                          <label class="form-label small">报警等级</label>
                          <el-select v-model="element.config.alertLevel" style="width: 100%" size="small">
                            <el-option label="低 - 仅记录" value="low" />
                            <el-option label="中 - 通知" value="medium" />
                            <el-option label="高 - 警报" value="high" />
                            <el-option label="紧急 - 联动" value="urgent" />
                          </el-select>
                        </div>
                        <div class="col-md-4">
                          <label class="form-label small">报警延迟</label>
                          <el-select v-model="element.config.alertDelay" style="width: 100%" size="small">
                            <el-option label="立即报警" value="immediate" />
                            <el-option label="连续2次超过阈值" value="2_times" />
                            <el-option label="连续3次超过阈值" value="3_times" />
                            <el-option label="超过5分钟" value="5_minutes" />
                          </el-select>
                        </div>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </template>
          </DraggableMetrics>
          
          <!-- 批量设置 -->
          <div v-if="metrics.length > 0" class="d-flex justify-content-between align-items-center mt-4">
            <el-button plain @click="showBatchConfigDialog">
              <el-icon><Setting /></el-icon> 批量配置所有指标
            </el-button>
            <el-button type="success" @click="showAddMetricDialog">
              <el-icon><Plus /></el-icon> 添加自定义指标
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
    
        <!-- 已选指标计数器 -->
    <div class="selected-metrics-counter" v-if="metrics.length > 0">
      <el-icon><Check /></el-icon>
      <span class="number">{{ metrics.length }}</span> 已选指标
      <el-button size="small" @click="nextStep">下一步</el-button>
    </div>

    <!-- 添加指标对话框 -->
    <el-dialog
      v-model="addMetricDialogVisible"
      title="添加指标"
      width="600px"
    >
      <el-form :model="newMetric" label-width="120px">
        <el-form-item label="指标名称" required>
          <el-input v-model="newMetric.name" placeholder="请输入指标名称" />
        </el-form-item>

        <el-form-item label="指标类型" required>
          <el-select v-model="newMetric.type" placeholder="请选择指标类型" style="width: 100%">
            <el-option-group label="视频分析">
              <el-option label="人脸识别" value="face_recognition" />
              <el-option label="物体检测" value="object_detection" />
              <el-option label="行为分析" value="behavior_analysis" />
              <el-option label="越界检测" value="boundary_detection" />
            </el-option-group>
            <el-option-group label="传感器数据">
              <el-option label="温湿度数据" value="temperature_humidity" />
              <el-option label="能耗数据" value="energy_consumption" />
              <el-option label="空气质量" value="air_quality" />
            </el-option-group>
          </el-select>
        </el-form-item>

        <el-form-item label="适用设备类型" required>
          <el-select v-model="newMetric.deviceType" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="摄像头" value="camera" />
            <el-option label="传感器" value="sensor" />
            <el-option label="控制器" value="controller" />
          </el-select>
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="newMetric.description"
            type="textarea"
            :rows="2"
            placeholder="请输入指标描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addMetricDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addMetric">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Close, DataLine, Plus, Collection, InfoFilled, Setting, Delete, 
  ArrowUp, ArrowDown, ArrowLeft, ArrowRight, Search, List, Check, Tag, Clock, Ruler, 
  InfoCircle, Minus } from '@element-plus/icons-vue'
import { getMetricTemplates, saveTaskDraft } from '@/api/task'
import DraggableMetrics from '@/components/DraggableMetrics.vue'

const router = useRouter()
const route = useRoute()

// 判断是否为编辑模式
const isEdit = ref(false)
const taskId = ref(null)

// 当前步骤
const currentStep = ref(2)

// 指标列表
const metrics = ref([])

// 模板搜索
const templateSearch = ref('')
const selectedTemplate = ref(null)

// 模板数据
const templates = ref([])

// 添加指标对话框
const addMetricDialogVisible = ref(false)
const newMetric = reactive({
  id: '',
  name: '',
  type: '',
  deviceType: '',
  description: '',
  deviceCount: 0,
  config: {}
})

// 新增配置选项
const activeConfigTab = ref('template')
const deviceTypeFilter = ref('')
const templateSort = ref('name')
const metricSearch = ref('')

// 过滤后的模板
const filteredTemplates = computed(() => {
  return templates.value
    .filter(t => {
      if (deviceTypeFilter.value && t.deviceType !== deviceTypeFilter.value) {
        return false
      }
      return templateSearch.value === '' || 
        t.name?.toLowerCase().includes(templateSearch.value.toLowerCase()) ||
        t.description?.toLowerCase().includes(templateSearch.value.toLowerCase())
    })
    .sort((a, b) => {
      if (templateSort.value === 'name') {
        return a.name?.localeCompare(b.name)
      } else if (templateSort.value === 'recent') {
        return (b.lastUsed || 0) - (a.lastUsed || 0)
      } else if (templateSort.value === 'count') {
        return (b.metricCount || 0) - (a.metricCount || 0)
      }
      return 0
    })
})

// 分组后的可用指标
const groupedAvailableMetrics = computed(() => {
  const groups = {}
  const availableMetrics = templates.value.flatMap(t => t.metrics || [])
  
  availableMetrics.forEach(metric => {
    const groupName = getMetricCategoryLabel(metric.category || 'other')
    if (!groups[groupName]) {
      groups[groupName] = []
    }
    if (!groups[groupName].find(m => m.id === metric.id)) {
      groups[groupName].push(metric)
    }
  })
  
  return groups
})

// 获取模板数据
const fetchTemplates = async () => {
  try {
    const res = await getMetricTemplates()
    templates.value = res.data || []
  } catch (error) {
    console.error('获取指标模板失败', error)
    ElMessage.error('获取指标模板失败')
  }
}

// 选择模板
const selectTemplate = (template) => {
  if (selectedTemplate.value === template.id) {
    selectedTemplate.value = null
    return
  }
  
  selectedTemplate.value = template.id
  
  // 根据模板类型预设新指标
  newMetric.name = template.name
  newMetric.type = template.type
  newMetric.description = template.description
  
  // 设置默认设备类型
  if (template.category === 'video') {
    newMetric.deviceType = 'camera'
  } else if (template.category === 'sensor') {
    newMetric.deviceType = 'sensor'
  }
  
  // 打开添加对话框
  showAddMetricDialog()
}

// 获取选中模板名称
const getSelectedTemplateName = () => {
  const template = templates.value.find(t => t.id === selectedTemplate.value)
  return template ? template.name : '未知模板'
}

// 获取选中模板指标数量
const getSelectedTemplateMetricCount = () => {
  const template = templates.value.find(t => t.id === selectedTemplate.value)
  return template ? (template.metricCount || template.metrics?.length || 0) : 0
}

// 获取选中设备数量
const getSelectedDeviceCount = () => {
  const taskData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
  return taskData.devices?.length || 0
}

// 获取模板图标
const getTemplateIcon = (category) => {
  const iconMap = {
    'video': 'fa-video',
    'sensor': 'fa-thermometer-half',
    'system': 'fa-server',
    'network': 'fa-network-wired'
  }
  return iconMap[category] || 'fa-chart-line'
}

// 获取指标图标
const getMetricIcon = (type) => {
  const iconMap = {
    'face_recognition': 'face',
    'object_detection': 'aim',
    'temperature_humidity': 'thermometer',
    'energy_consumption': 'lightning',
    'air_quality': 'wind'
  }
  return iconMap[type] || 'data-line'
}

// 获取指标类型样式
const getMetricTypeClass = (type) => {
  if (type?.includes('recognition') || type?.includes('detection') || type?.includes('analysis')) {
    return 'type-status'
  }
  return 'type-number'
}

// 获取指标单位
const getMetricUnit = (metric) => {
  const unitMap = {
    'temperature_humidity': '°C/%',
    'energy_consumption': 'kWh',
    'air_quality': 'ppm'
  }
  return unitMap[metric.type] || ''
}

// 获取指标分类标签
const getMetricCategoryLabel = (category) => {
  const categoryMap = {
    'video': '视频分析',
    'sensor': '传感器数据',
    'system': '系统性能',
    'network': '网络状态',
    'other': '其他指标'
  }
  return categoryMap[category] || '其他指标'
}

// 检查指标是否已选择
const isMetricSelected = (metricId) => {
  return metrics.value.some(m => m.id === metricId)
}

// 从可用指标添加
const addMetricFromAvailable = (metric) => {
  // 创建配置对象
  const config = createDefaultConfig(metric.type)
  
  // 创建新指标
  const newMetric = {
    id: metric.id,
    name: metric.name,
    metricName: metric.name,
    type: metric.type,
    metricType: metric.type,
    deviceType: metric.deviceType || 'sensor',
    description: metric.description,
    deviceCount: getSelectedDeviceCount(),
    config
  }
  
  metrics.value.push(newMetric)
  ElMessage.success(`已添加指标: ${metric.name}`)
}

// 根据ID移除指标
const removeMetricById = (metricId) => {
  metrics.value = metrics.value.filter(m => m.id !== metricId)
  ElMessage.success('已移除指标')
}

// 清除所有指标
const clearAllMetrics = () => {
  ElMessageBox.confirm('确认清除所有已选指标吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    metrics.value = []
    ElMessage.success('已清除所有指标')
  }).catch(() => {})
}

// 显示批量配置对话框
const showBatchConfigDialog = () => {
  ElMessage.info('批量配置功能开发中')
}

// 获取阈值范围
const getMinThreshold = (element) => '0'
const getLowThreshold = (element) => '25'
const getMidThreshold = (element) => '50'
const getHighThreshold = (element) => '75'
const getMaxThreshold = (element) => '100'

// 获取可能的最小值
const getMinPossibleValue = (element) => {
  if (element.type === 'temperature_humidity') return -50
  return 0
}

// 获取可能的最大值
const getMaxPossibleValue = (element) => {
  if (element.type === 'temperature_humidity') return 100
  return 100
}

// 显示添加指标对话框
const showAddMetricDialog = () => {
  // 如果没有通过模板选择，重置表单
  if (!selectedTemplate.value) {
    resetNewMetricForm()
  }
  addMetricDialogVisible.value = true
}

// 重置新指标表单
const resetNewMetricForm = () => {
  newMetric.id = ''
  newMetric.name = ''
  newMetric.type = ''
  newMetric.deviceType = ''
  newMetric.description = ''
  selectedTemplate.value = null
}

// 添加指标
const addMetric = () => {
  // 表单验证
  if (!newMetric.name || !newMetric.type || !newMetric.deviceType) {
    ElMessage.warning('请填写必填项')
    return
  }

  // 计算适用设备数量
  const taskData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
  const devices = taskData.devices || []
  const deviceCount = devices.filter(d => d.deviceType === newMetric.deviceType).length

  if (deviceCount === 0) {
    ElMessage.warning(`没有选择类型为${getDeviceTypeLabel(newMetric.deviceType)}的设备`)
    return
  }

  // 创建配置对象
  const config = createDefaultConfig(newMetric.type)

  // 创建新指标
  const metric = {
    id: newMetric.id || Date.now().toString(),
    name: newMetric.name,
    metricName: newMetric.name, // 添加metricName字段
    type: newMetric.type,
    metricType: newMetric.type, // 添加metricType字段
    deviceType: newMetric.deviceType,
    description: newMetric.description,
    deviceCount,
    config
  }

  // 如果是编辑，则替换原有指标
  const index = metrics.value.findIndex(m => m.id === metric.id)
  if (index !== -1) {
    metrics.value.splice(index, 1, metric)
  } else {
    metrics.value.push(metric)
  }

  // 关闭对话框并重置表单
  addMetricDialogVisible.value = false
  resetNewMetricForm()
}

// 创建默认配置
const createDefaultConfig = (type) => {
  switch (type) {
    case 'face_recognition':
      return {
        confidence: 85,
        faceDatabase: 'employee',
        storeUnknownFaces: true
      }
    case 'object_detection':
      return {
        detectPerson: true,
        detectVehicle: true,
        detectAnimal: false,
        detectBag: false,
        confidence: 75,
        frequency: '1_per_second'
      }
    case 'temperature_humidity':
      return {
        tempMin: 18,
        tempMax: 28,
        humidityMin: 30,
        humidityMax: 70,
        samplingInterval: '1m'
      }
    case 'behavior_analysis':
      return {
        detectLoitering: true,
        detectCrowding: true,
        detectFalling: true,
        sensitivity: 70
      }
    case 'boundary_detection':
      return {
        direction: 'both',
        sensitivity: 80,
        linePositions: []
      }
    case 'energy_consumption':
      return {
        electricityThreshold: 100,
        waterThreshold: 50,
        gasThreshold: 30,
        samplingInterval: '5m'
      }
    case 'air_quality':
      return {
        pm25Threshold: 75,
        co2Threshold: 1000,
        tvocThreshold: 500,
        samplingInterval: '5m'
      }
    default:
      return {}
  }
}

// 编辑指标
const editMetric = (metric) => {
  // 复制指标数据到表单
  newMetric.id = metric.id
  newMetric.name = metric.name
  newMetric.type = metric.type
  newMetric.deviceType = metric.deviceType
  newMetric.description = metric.description

  // 打开对话框
  addMetricDialogVisible.value = true
}

// 删除指标
const removeMetric = (metric) => {
  ElMessageBox.confirm(`确认删除指标"${metric.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    metrics.value = metrics.value.filter(m => m.id !== metric.id)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 处理拖拽结束
const handleDragEnd = () => {
  // 拖拽结束后可以更新指标优先级
  console.log('指标优先级已更新')
}

// 获取指标类型标签
const getMetricTypeTag = (type) => {
  // 检查type是否为undefined或null
  if (!type) {
    return 'primary'; // 返回默认标签类型
  }
  
  if (type.includes('recognition') || type.includes('detection') || type.includes('analysis')) {
    return 'info'
  }
  return 'primary'
}

// 获取指标类型标签文本
const getMetricTypeLabel = (type) => {
  // 检查type是否为undefined或null
  if (!type) {
    return '未知类型'; // 返回默认标签文本
  }
  
  const map = {
    face_recognition: '人脸识别',
    object_detection: '物体检测',
    behavior_analysis: '行为分析',
    boundary_detection: '越界检测',
    temperature_humidity: '温湿度数据',
    energy_consumption: '能耗数据',
    air_quality: '空气质量'
  }
  return map[type] || type
}

// 获取设备类型标签文本
const getDeviceTypeLabel = (type) => {
  // 检查type是否为undefined或null
  if (!type) {
    return '未知设备'; // 返回默认设备类型文本
  }
  
  const map = {
    camera: '摄像头',
    sensor: '传感器',
    controller: '控制器'
  }
  return map[type] || type
}

// 上一步
const prevStep = () => {
  // 保存当前指标配置
  saveCurrentStepData()
  
  // 根据是否为编辑模式决定跳转路径
  router.push('/task/create/device')
}

// 下一步
const nextStep = () => {
  if (metrics.value.length === 0) {
    ElMessage.warning('请至少添加一个指标')
    return
  }

  // 保存当前指标配置
  saveCurrentStepData()
  router.push('/task/create/schedule')
}

// 保存当前步骤数据
const saveCurrentStepData = () => {
  // 从本地存储获取之前的数据
  const prevData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
  
  // 确保所有指标都有名称和类型
  const validMetrics = metrics.value.map(metric => {
    return {
      ...metric,
      name: metric.name || `未命名指标_${Date.now()}`, // 确保name字段不为空
      metricName: metric.name || `未命名指标_${Date.now()}`, // 添加metricName字段
      type: metric.type || 'unknown', // 确保type字段不为空
      metricType: metric.type || 'unknown' // 添加metricType字段
    }
  }).filter(metric => metric.name && metric.type); // 过滤无效指标
  
  // 合并数据并保存
  const currentData = {
    ...prevData,
    metrics: validMetrics,
    step: 3
  }
  
  localStorage.setItem('taskCreateData', JSON.stringify(currentData))
}

// 保存草稿
const saveDraft = async () => {
  try {
    // 从本地存储获取之前的数据
    const prevData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
    
    // 确保所有指标都有名称和类型
    const validMetrics = metrics.value.map(metric => {
      return {
        ...metric,
        name: metric.name || `未命名指标_${Date.now()}`,
        metricName: metric.name || `未命名指标_${Date.now()}`,
        type: metric.type || 'unknown',
        metricType: metric.type || 'unknown'
      }
    }).filter(metric => metric.name && metric.type);
    
    // 合并数据
    const draftData = {
      ...prevData,
      metrics: validMetrics,
      step: 2
    }
    
    const res = await saveTaskDraft(draftData)
    ElMessage.success('草稿保存成功')
    
    // 保存草稿ID，用于后续恢复
    if (res.data && res.data.draftId) {
      localStorage.setItem('taskDraftId', res.data.draftId)
    }
  } catch (error) {
    console.error('保存草稿失败', error)
    ElMessage.error('保存草稿失败')
  }
}

// 取消
const cancel = () => {
  ElMessage.info('已取消创建任务')
  router.push('/task')
}

// 恢复之前的配置
const restorePreviousConfig = () => {
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      if (data.metrics && Array.isArray(data.metrics)) {
        // 确保恢复的指标数据包含必要字段
        metrics.value = data.metrics.map(metric => ({
          ...metric,
          name: metric.name || metric.metricName || `未命名指标_${Date.now()}`,
          metricName: metric.metricName || metric.name || `未命名指标_${Date.now()}`,
          type: metric.type || metric.metricType || 'unknown',
          metricType: metric.metricType || metric.type || 'unknown',
          config: metric.config || {}, // 确保config对象存在
          deviceCount: metric.deviceCount || 0 // 确保deviceCount存在
        }));
      }
    } catch (error) {
      console.error('解析本地存储数据失败', error)
    }
  }
}

// 修复本地存储中的设备数据格式
const fixLocalStorageDeviceData = () => {
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      if (data.devices && Array.isArray(data.devices)) {
        // 检查并修复设备数据格式
        const fixedDevices = data.devices.map(device => {
          // 如果有id但没有deviceId，则将id转为deviceId
          if (device.id && !device.deviceId) {
            return {
              ...device,
              deviceId: device.id
            }
          }
          return device
        })
        
        // 更新本地存储
        localStorage.setItem('taskCreateData', JSON.stringify({
          ...data,
          devices: fixedDevices
        }))
      }
    } catch (error) {
      console.error('修复本地存储数据失败', error)
    }
  }
}

// 页面初始化
onMounted(() => {
  // 获取指标模板
  fetchTemplates()
  
  // 从本地存储获取之前步骤的数据
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      currentStep.value = data.step || 2
      
      // 检查是否是编辑模式
      isEdit.value = data.isEdit || false
      taskId.value = data.taskId || null
      
      console.log('当前模式:', isEdit.value ? '编辑任务' : '创建任务', '任务ID:', taskId.value)
      
      // 恢复已保存的指标
      if (data.metrics && Array.isArray(data.metrics)) {
        // 确保每个指标都有有效的type和name属性
        metrics.value = data.metrics.map(metric => ({
          ...metric,
          name: metric.name || metric.metricName || `未命名指标_${Date.now()}`,
          metricName: metric.metricName || metric.name || `未命名指标_${Date.now()}`,
          type: metric.type || metric.metricType || 'unknown',
          metricType: metric.metricType || metric.type || 'unknown',
          config: metric.config || {} // 确保config对象存在
        }))
      }
    } catch (error) {
      console.error('解析本地存储数据失败', error)
    }
  }
  
  // 如果是编辑模式，尝试从taskEditData中获取指标数据
  if (isEdit.value && taskId.value) {
    const editData = localStorage.getItem('taskEditData')
    if (editData) {
      try {
        const taskData = JSON.parse(editData)
        if (taskData.metrics && Array.isArray(taskData.metrics)) {
          console.log('从taskEditData中恢复指标数据:', taskData.metrics)
          // 确保每个指标都有有效的type和name属性
          metrics.value = taskData.metrics.map(metric => ({
            ...metric,
            name: metric.name || metric.metricName || `未命名指标_${Date.now()}`,
            metricName: metric.metricName || metric.name || `未命名指标_${Date.now()}`,
            type: metric.type || metric.metricType || 'unknown',
            metricType: metric.metricType || metric.type || 'unknown',
            config: metric.config || {} // 确保config对象存在
          }))
          
          // 更新本地存储中的指标数据
          const currentData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
          localStorage.setItem('taskCreateData', JSON.stringify({
            ...currentData,
            metrics: metrics.value,
            isEdit: true,
            taskId: taskId.value
          }))
        }
      } catch (error) {
        console.error('解析任务编辑数据失败', error)
      }
    } else if (taskId.value) {
      // 如果本地没有编辑数据但有任务ID，尝试从服务器获取
      console.log('本地无编辑数据，尝试从服务器获取任务详情')
      import('@/api/task').then(({ getTaskDetail }) => {
        getTaskDetail(taskId.value).then(res => {
          if (res.data && res.data.metrics) {
            console.log('从服务器获取到指标数据:', res.data.metrics)
            // 处理指标数据
            metrics.value = res.data.metrics.map(metric => ({
              ...metric,
              name: metric.name || metric.metricName || `未命名指标_${Date.now()}`,
              metricName: metric.metricName || metric.name || `未命名指标_${Date.now()}`,
              type: metric.type || metric.metricType || 'unknown',
              metricType: metric.metricType || metric.type || 'unknown',
              config: metric.config || {} // 确保config对象存在
            }))
            
            // 保存到本地存储
            localStorage.setItem('taskEditData', JSON.stringify(res.data))
            
            // 更新taskCreateData
            const currentData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
            localStorage.setItem('taskCreateData', JSON.stringify({
              ...currentData,
              metrics: metrics.value,
              isEdit: true,
              taskId: taskId.value
            }))
          }
        }).catch(error => {
          console.error('获取任务详情失败', error)
        })
      }).catch(error => {
        console.error('导入API模块失败', error)
      })
    }
  }
})
</script>

<style scoped>
.metric-card {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  transition: all 0.2s ease;
}

.metric-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.drag-handle {
  color: #adb5bd;
  cursor: move;
}

.template-card {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.template-card:hover {
  border-color: #409EFF;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.template-card.selected {
  border-color: #409EFF;
  background-color: rgba(64, 158, 255, 0.05);
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
  font-size: 12px;
  color: #6c757d;
}

.empty-metrics {
  padding: 30px 0;
}

.action-buttons {
  display: flex;
  align-items: center;
}

.action-footer {
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 30px;
}
</style> 