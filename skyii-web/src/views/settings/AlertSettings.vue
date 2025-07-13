<template>
  <div class="alert-settings-container">
    <div class="page-header">
      <div>
        <h4>
          <el-icon><Bell /></el-icon>
          智能报警规则管理
        </h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/settings' }">系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>智能报警规则</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-actions">
        <el-button-group>
          <el-button type="primary" @click="showAIRecommendations">
            <el-icon><MagicStick /></el-icon> AI 智能推荐
          </el-button>
          <el-button @click="showSystemStatus">
            <el-icon><Monitor /></el-icon> 系统状态
          </el-button>
        </el-button-group>
        <el-button-group>
          <el-button @click="batchOperations">
            <el-icon><Operation /></el-icon> 批量操作
          </el-button>
          <el-button @click="importRules">
            <el-icon><Upload /></el-icon> 导入
          </el-button>
          <el-button @click="exportRules">
            <el-icon><Download /></el-icon> 导出
          </el-button>
        </el-button-group>
        <el-button type="warning" @click="createNewRule">
          <el-icon><Plus /></el-icon> 创建规则
        </el-button>
      </div>
    </div>
    
    <!-- 统计概览 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon success">
                <el-icon><Shield /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.activeRules }}</div>
                <div class="stat-label">活跃规则</div>
                <div class="stat-status success">
                  <el-icon><CircleCheck /></el-icon>
                  <span>运行良好</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon warning">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.todayTriggers }}</div>
                <div class="stat-label">今日触发</div>
                <div class="stat-status warning">
                  <el-icon><Warning /></el-icon>
                  <span>需要关注</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon info">
                <el-icon><MagicStick /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.aiRecommendations }}</div>
                <div class="stat-label">AI 推荐</div>
                <div class="stat-status info">
                  <el-icon><MagicStick /></el-icon>
                  <span>智能优化</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon danger">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.errorRules }}</div>
                <div class="stat-label">异常规则</div>
                <div class="stat-status danger">
                  <el-icon><CircleClose /></el-icon>
                  <span>需要修复</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 监控类型选择 -->
    <el-card class="monitoring-types-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Grid /></el-icon>
          <span>监控类型</span>
          <div class="card-actions">
            <small class="text-muted">选择要管理的监控类型</small>
          </div>
        </div>
      </template>
      <div class="monitoring-types">
        <el-tag
          v-for="type in monitoringTypes"
          :key="type.value"
          :type="activeType === type.value ? '' : 'info'"
          :effect="activeType === type.value ? 'dark' : 'plain'"
          size="large"
          class="type-tag"
          @click="switchMonitoringType(type.value)"
        >
          <el-icon>
            <component :is="type.icon" />
          </el-icon>
          {{ type.label }}
        </el-tag>
      </div>
    </el-card>
    
    <!-- AI 智能推荐 -->
    <el-card class="ai-recommendation-card" shadow="hover">
      <template #header>
        <div class="card-header ai-header">
          <el-icon><MagicStick /></el-icon>
          <span>AI 智能推荐</span>
          <div class="card-actions">
            <el-button type="primary" size="small" @click="showAIRecommendations">
              <el-icon><Plus /></el-icon> 应用推荐规则
            </el-button>
          </div>
        </div>
      </template>
      <div class="ai-recommendations">
        <p class="recommendation-intro">基于系统学习和历史数据分析，为您推荐优化的报警规则</p>
        <el-row :gutter="20">
          <el-col :span="12" v-for="recommendation in aiRecommendations" :key="recommendation.id">
            <div class="recommendation-item">
              <div class="recommendation-title">{{ recommendation.title }}</div>
              <div class="recommendation-description">{{ recommendation.description }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <el-row :gutter="20">
      <el-col :span="6">
        <!-- 智能筛选器 -->
        <el-card class="filter-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Filter /></el-icon>
              <span>智能筛选</span>
            </div>
          </template>
          <div class="filter-sections">
            <div class="filter-section">
              <div class="filter-title">规则状态</div>
              <div class="filter-chips">
                <el-tag
                  v-for="status in statusFilters"
                  :key="status.value"
                  :type="activeFilters.status === status.value ? 'primary' : 'info'"
                  :effect="activeFilters.status === status.value ? 'dark' : 'plain'"
                  size="small"
                  class="filter-chip"
                  @click="setFilter('status', status.value)"
                >
                  {{ status.label }}
                </el-tag>
              </div>
            </div>
            
            <div class="filter-section">
              <div class="filter-title">优先级</div>
              <div class="filter-chips">
                <el-tag
                  v-for="priority in priorityFilters"
                  :key="priority.value"
                  :type="activeFilters.priority === priority.value ? 'primary' : 'info'"
                  :effect="activeFilters.priority === priority.value ? 'dark' : 'plain'"
                  size="small"
                  class="filter-chip"
                  @click="setFilter('priority', priority.value)"
                >
                  {{ priority.label }}
                </el-tag>
              </div>
            </div>
            
            <div class="filter-section">
              <div class="filter-title">触发频率</div>
              <div class="filter-chips">
                <el-tag
                  v-for="frequency in frequencyFilters"
                  :key="frequency.value"
                  :type="activeFilters.frequency === frequency.value ? 'primary' : 'info'"
                  :effect="activeFilters.frequency === frequency.value ? 'dark' : 'plain'"
                  size="small"
                  class="filter-chip"
                  @click="setFilter('frequency', frequency.value)"
                >
                  {{ frequency.label }}
                </el-tag>
              </div>
            </div>
            
            <div class="filter-section">
              <div class="filter-title">创建时间</div>
              <div class="filter-chips">
                <el-tag
                  v-for="time in timeFilters"
                  :key="time.value"
                  :type="activeFilters.time === time.value ? 'primary' : 'info'"
                  :effect="activeFilters.time === time.value ? 'dark' : 'plain'"
                  size="small"
                  class="filter-chip"
                  @click="setFilter('time', time.value)"
                >
                  {{ time.label }}
                </el-tag>
              </div>
            </div>
            
            <div class="filter-actions">
              <el-button type="primary" size="small" @click="applyFilters">
                <el-icon><Search /></el-icon> 应用筛选
              </el-button>
              <el-button size="small" @click="resetFilters">
                <el-icon><Refresh /></el-icon> 重置筛选
              </el-button>
            </div>
          </div>
        </el-card>
        
        <!-- 规则模板 -->
        <el-card class="template-card" shadow="hover">
          <template #header>
            <div class="card-header template-header">
              <el-icon><Document /></el-icon>
              <span>快速模板</span>
            </div>
          </template>
          <div class="rule-templates">
            <p class="template-intro">使用预设模板快速创建常用报警规则</p>
            <div class="template-grid">
              <div
                v-for="template in ruleTemplates"
                :key="template.id"
                class="template-item"
                @click="applyTemplate(template)"
              >
                <div class="template-title">{{ template.name }}</div>
                <div class="template-description">{{ template.description }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <!-- 报警规则列表 -->
        <el-card class="rules-list-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Bell /></el-icon>
              <span>智能报警规则列表</span>
              <div class="card-actions">
                <el-button-group size="small">
                  <el-button :type="viewMode === 'list' ? 'primary' : ''" @click="viewMode = 'list'">
                    <el-icon><List /></el-icon>
                  </el-button>
                  <el-button :type="viewMode === 'grid' ? 'primary' : ''" @click="viewMode = 'grid'">
                    <el-icon><Grid /></el-icon>
                  </el-button>
                </el-button-group>
                <el-select v-model="sortBy" size="small" style="width: 150px; margin-left: 10px;">
                  <el-option label="按创建时间排序" value="created_at"></el-option>
                  <el-option label="按触发次数排序" value="trigger_count"></el-option>
                  <el-option label="按优先级排序" value="priority"></el-option>
                  <el-option label="按名称排序" value="name"></el-option>
                </el-select>
              </div>
            </div>
          </template>
          
          <div class="rules-list">
            <div
              v-for="rule in filteredRules"
              :key="rule.id"
              class="rule-card"
              :class="{ 'rule-card-grid': viewMode === 'grid' }"
            >
              <div class="rule-header">
                <div class="rule-title">
                  <el-icon :color="rule.iconColor">
                    <component :is="rule.icon" />
                  </el-icon>
                  {{ rule.name }}
                </div>
                <div class="rule-status">
                  <div class="status-indicator" :class="rule.status"></div>
                  <span :class="rule.status === 'active' ? 'text-success' : 'text-warning'">
                    {{ rule.status === 'active' ? '运行中' : '已暂停' }}
                  </span>
                  <el-switch
                    v-model="rule.enabled"
                    size="small"
                    @change="toggleRule(rule)"
                  />
                </div>
              </div>
              
              <div class="rule-meta">
                <el-tag
                  :type="getPriorityType(rule.priority)"
                  size="small"
                  effect="light"
                >
                  {{ getPriorityLabel(rule.priority) }}
                </el-tag>
                <el-tag
                  v-for="tag in rule.tags"
                  :key="tag"
                  type="info"
                  size="small"
                  effect="light"
                >
                  {{ tag }}
                </el-tag>
              </div>
              
              <div class="rule-description">
                {{ rule.description }}
              </div>
              
              <div class="rule-targets">
                <el-icon><Aim /></el-icon>
                <strong>监控目标:</strong> {{ rule.targets }} |
                <strong>最近触发:</strong> {{ rule.lastTrigger }} |
                <strong>触发次数:</strong> 今日{{ rule.todayTriggers }}次
              </div>
              
              <div class="rule-actions">
                <el-button-group size="small">
                  <el-button type="primary" @click="editRule(rule)">
                    <el-icon><Edit /></el-icon> 编辑
                  </el-button>
                  <el-button type="info" @click="viewTrend(rule)">
                    <el-icon><TrendCharts /></el-icon> 查看趋势
                  </el-button>
                  <el-button type="success" @click="cloneRule(rule)">
                    <el-icon><CopyDocument /></el-icon> 克隆
                  </el-button>
                  <el-button
                    :type="rule.enabled ? 'warning' : 'success'"
                    @click="toggleRule(rule)"
                  >
                    <el-icon>
                      <component :is="rule.enabled ? 'VideoPause' : 'VideoPlay'" />
                    </el-icon>
                    {{ rule.enabled ? '暂停' : '启用' }}
                  </el-button>
                  <el-button type="danger" @click="deleteRule(rule)">
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </el-button-group>
              </div>
            </div>
          </div>
          
          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 创建规则对话框 -->
    <el-dialog
      v-model="createRuleDialog"
      title="创建智能报警规则"
      width="60%"
      :before-close="handleCloseCreateDialog"
    >
      <el-form :model="newRule" :rules="ruleFormRules" ref="ruleFormRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则名称" prop="name">
              <el-input v-model="newRule.name" placeholder="请输入规则名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="监控类型" prop="type">
              <el-select v-model="newRule.type" placeholder="请选择监控类型">
                <el-option
                  v-for="type in monitoringTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="newRule.priority" placeholder="请选择优先级">
                <el-option label="高优先级" value="high" />
                <el-option label="中优先级" value="medium" />
                <el-option label="低优先级" value="low" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用状态">
              <el-switch v-model="newRule.enabled" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="规则描述">
          <el-input v-model="newRule.description" type="textarea" :rows="3" placeholder="请输入规则描述" />
        </el-form-item>
        
        <el-form-item label="监控目标" prop="targets">
          <el-input v-model="newRule.targets" placeholder="输入监控目标，多个目标用逗号分隔">
            <template #append>
              <el-button @click="selectTargets">
                <el-icon><Search /></el-icon> 选择
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="监控指标" prop="metric">
              <el-select v-model="newRule.metric" placeholder="请选择监控指标">
                <el-option label="CPU使用率" value="cpu" />
                <el-option label="内存使用率" value="memory" />
                <el-option label="磁盘使用率" value="disk" />
                <el-option label="网络流量" value="network" />
                <el-option label="响应时间" value="response_time" />
                <el-option label="错误率" value="error_rate" />
                <el-option label="自定义指标" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="阈值" prop="threshold">
              <el-input v-model="newRule.threshold" type="number">
                <template #prepend>
                  <el-select v-model="newRule.operator" style="width: 80px;">
                    <el-option label=">" value=">" />
                    <el-option label=">=" value=">=" />
                    <el-option label="<" value="<" />
                    <el-option label="<=" value="<=" />
                    <el-option label="=" value="=" />
                    <el-option label="!=" value="!=" />
                  </el-select>
                </template>
                <template #append>%</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="通知方式">
          <el-checkbox-group v-model="newRule.notificationMethods">
            <el-checkbox label="email">邮件通知</el-checkbox>
            <el-checkbox label="sms">短信通知</el-checkbox>
            <el-checkbox label="wechat">微信通知</el-checkbox>
            <el-checkbox label="dingtalk">钉钉通知</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="通知联系人">
          <el-input v-model="newRule.contacts" placeholder="输入联系人邮箱或手机号，多个用逗号分隔" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createRuleDialog = false">取消</el-button>
          <el-button type="primary" @click="saveRule">
            <el-icon><Save /></el-icon> 保存规则
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- AI推荐对话框 -->
    <el-dialog
      v-model="aiRecommendDialog"
      title="AI 智能推荐规则"
      width="50%"
    >
      <el-alert
        title="基于您的系统运行数据和行业最佳实践，AI为您推荐以下优化规则"
        type="info"
        :closable="false"
        show-icon
      />
      
      <div class="ai-recommendations-list">
        <div
          v-for="recommendation in detailedRecommendations"
          :key="recommendation.id"
          class="recommendation-card"
        >
          <el-checkbox v-model="recommendation.selected">
            <div class="recommendation-content">
              <h6>{{ recommendation.title }}</h6>
              <p>{{ recommendation.description }}</p>
              <small class="text-success">{{ recommendation.benefit }}</small>
            </div>
          </el-checkbox>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="aiRecommendDialog = false">取消</el-button>
          <el-button type="primary" @click="applyAIRecommendations">
            <el-icon><MagicStick /></el-icon> 应用选中推荐
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteConfirmDialog"
      title="确认删除"
      width="400px"
    >
      <div class="delete-confirm-content">
        <el-icon class="delete-icon" color="#F56C6C" size="48">
          <WarningFilled />
        </el-icon>
        <h5>您确定要删除此报警规则吗？</h5>
        <p>规则名称: <strong>{{ selectedRule?.name }}</strong></p>
        <p class="text-danger">此操作不可恢复，请谨慎操作！</p>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteConfirmDialog = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">
            <el-icon><Delete /></el-icon> 确认删除
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const activeType = ref('all')
const viewMode = ref('list')
const sortBy = ref('created_at')
const createRuleDialog = ref(false)
const aiRecommendDialog = ref(false)
const deleteConfirmDialog = ref(false)
const selectedRule = ref(null)
const ruleFormRef = ref(null)

// 统计数据
const stats = reactive({
  activeRules: 156,
  todayTriggers: 23,
  aiRecommendations: 8,
  errorRules: 3
})

// 筛选器状态
const activeFilters = reactive({
  status: 'all',
  priority: '',
  frequency: '',
  time: ''
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 新规则表单
const newRule = reactive({
  name: '',
  type: '',
  priority: '',
  description: '',
  targets: '',
  metric: '',
  operator: '>',
  threshold: '',
  notificationMethods: ['email'],
  contacts: '',
  enabled: true
})

// 表单验证规则
const ruleFormRules = {
  name: [
    { required: true, message: '请输入规则名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择监控类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  targets: [
    { required: true, message: '请输入监控目标', trigger: 'blur' }
  ],
  metric: [
    { required: true, message: '请选择监控指标', trigger: 'change' }
  ],
  threshold: [
    { required: true, message: '请输入阈值', trigger: 'blur' }
  ]
}

// 监控类型
const monitoringTypes = [
  { value: 'all', label: '全部监控', icon: 'Globe' },
  { value: 'infrastructure', label: '基础设施', icon: 'Monitor' },
  { value: 'network', label: '网络设备', icon: 'Connection' },
  { value: 'application', label: '应用服务', icon: 'Platform' },
  { value: 'middleware', label: '中间件', icon: 'Box' },
  { value: 'database', label: '数据库', icon: 'Coin' },
  { value: 'business', label: '业务监控', icon: 'BriefCase' },
  { value: 'security', label: '安全监控', icon: 'Shield' }
]

// 筛选器选项
const statusFilters = [
  { value: 'all', label: '全部' },
  { value: 'active', label: '运行中' },
  { value: 'paused', label: '已暂停' },
  { value: 'error', label: '异常' }
]

const priorityFilters = [
  { value: 'high', label: '高优先级' },
  { value: 'medium', label: '中优先级' },
  { value: 'low', label: '低优先级' }
]

const frequencyFilters = [
  { value: 'high', label: '频繁触发' },
  { value: 'normal', label: '正常' },
  { value: 'low', label: '很少触发' }
]

const timeFilters = [
  { value: 'today', label: '今天' },
  { value: 'week', label: '本周' },
  { value: 'month', label: '本月' }
]

// AI推荐数据
const aiRecommendations = ref([
  {
    id: 1,
    title: '数据库连接池优化',
    description: '检测到数据库连接池使用率持续偏高，建议添加连接池监控规则'
  },
  {
    id: 2,
    title: '业务响应时间异常',
    description: '用户登录接口响应时间波动较大，建议设置响应时间监控'
  }
])

const detailedRecommendations = ref([
  {
    id: 1,
    title: '数据库连接池监控优化',
    description: '检测到您的数据库连接池使用率经常超过75%，建议设置连接池监控规则',
    benefit: '预计可减少30%的数据库性能问题',
    selected: true
  },
  {
    id: 2,
    title: 'API响应时间异常检测',
    description: '用户登录接口响应时间波动较大，建议设置智能异常检测',
    benefit: '预计可提升20%的用户体验',
    selected: true
  },
  {
    id: 3,
    title: '磁盘空间预警优化',
    description: '基于历史增长趋势，建议将磁盘空间预警阈值调整为75%',
    benefit: '预计可提前3天发现存储问题',
    selected: false
  }
])

// 规则模板
const ruleTemplates = ref([
  {
    id: 1,
    name: '服务器监控',
    description: 'CPU、内存、磁盘使用率监控',
    type: 'infrastructure',
    priority: 'high'
  },
  {
    id: 2,
    name: '数据库监控',
    description: '连接数、查询性能、锁等待',
    type: 'database',
    priority: 'high'
  },
  {
    id: 3,
    name: '应用监控',
    description: '响应时间、错误率、吞吐量',
    type: 'application',
    priority: 'medium'
  },
  {
    id: 4,
    name: '业务监控',
    description: '订单量、用户活跃度、转化率',
    type: 'business',
    priority: 'high'
  }
])

// Mock规则数据
const mockRules = ref([
  {
    id: 1,
    name: '生产服务器资源监控',
    type: 'infrastructure',
    priority: 'high',
    status: 'active',
    enabled: true,
    description: '监控生产环境服务器的CPU使用率、内存使用率和磁盘空间。当CPU使用率超过80%或内存使用率超过85%时立即发送报警通知。',
    targets: '生产服务器集群 (10台)',
    lastTrigger: '2小时前',
    todayTriggers: 3,
    tags: ['基础设施', 'CPU监控', '内存监控'],
    icon: 'Monitor',
    iconColor: '#1e3c72'
  },
  {
    id: 2,
    name: 'MySQL 数据库性能监控',
    type: 'database',
    priority: 'high',
    status: 'active',
    enabled: true,
    description: '监控MySQL数据库的连接数、查询响应时间、锁等待时间和慢查询数量。当连接数超过80%或出现长时间锁等待时触发报警。',
    targets: 'MySQL主从集群 (3台)',
    lastTrigger: '30分钟前',
    todayTriggers: 1,
    tags: ['数据库', 'MySQL', '性能监控'],
    icon: 'Coin',
    iconColor: '#17a2b8'
  },
  {
    id: 3,
    name: 'Web应用响应时间监控',
    type: 'application',
    priority: 'medium',
    status: 'active',
    enabled: true,
    description: '监控Web应用的API响应时间、错误率和吞吐量。当API响应时间超过2秒或错误率超过5%时发送报警通知。',
    targets: 'Web应用集群 (5个实例)',
    lastTrigger: '1小时前',
    todayTriggers: 2,
    tags: ['应用监控', '响应时间', 'Web服务'],
    icon: 'Platform',
    iconColor: '#28a745'
  },
  {
    id: 4,
    name: '订单处理业务监控',
    type: 'business',
    priority: 'high',
    status: 'active',
    enabled: true,
    description: '通过自定义脚本监控订单处理业务的关键指标：订单创建成功率、支付成功率、发货及时率。当任一指标低于预设阈值时立即报警。',
    targets: '订单处理流程 (自定义脚本)',
    lastTrigger: '未触发',
    todayTriggers: 0,
    tags: ['业务监控', '订单处理', '自定义脚本'],
    icon: 'BriefCase',
    iconColor: '#ffc107'
  },
  {
    id: 5,
    name: 'Redis 缓存集群监控',
    type: 'middleware',
    priority: 'medium',
    status: 'paused',
    enabled: false,
    description: '监控Redis缓存集群的内存使用率、命中率、连接数和主从同步状态。当内存使用率超过90%或命中率低于85%时发送报警。',
    targets: 'Redis集群 (6个节点)',
    lastTrigger: '昨天',
    todayTriggers: 0,
    tags: ['中间件', 'Redis', '缓存监控'],
    icon: 'Box',
    iconColor: '#dc3545'
  }
])

// 计算属性
const filteredRules = computed(() => {
  let rules = mockRules.value

  // 按监控类型筛选
  if (activeType.value !== 'all') {
    rules = rules.filter(rule => rule.type === activeType.value)
  }

  // 按状态筛选
  if (activeFilters.status !== 'all') {
    rules = rules.filter(rule => rule.status === activeFilters.status)
  }

  // 按优先级筛选
  if (activeFilters.priority) {
    rules = rules.filter(rule => rule.priority === activeFilters.priority)
  }

  // 更新分页总数
  pagination.total = rules.length

  // 分页
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return rules.slice(start, end)
})

// 方法
const switchMonitoringType = (type) => {
  activeType.value = type
  pagination.currentPage = 1
}

const setFilter = (type, value) => {
  if (activeFilters[type] === value) {
    activeFilters[type] = type === 'status' ? 'all' : ''
  } else {
    activeFilters[type] = value
  }
  pagination.currentPage = 1
}

const applyFilters = () => {
  ElMessage.success('筛选器已应用')
}

const resetFilters = () => {
  activeFilters.status = 'all'
  activeFilters.priority = ''
  activeFilters.frequency = ''
  activeFilters.time = ''
  pagination.currentPage = 1
  ElMessage.success('筛选器已重置')
}

const createNewRule = () => {
  createRuleDialog.value = true
}

const handleCloseCreateDialog = () => {
  ruleFormRef.value?.resetFields()
  Object.assign(newRule, {
    name: '',
    type: '',
    priority: '',
    description: '',
    targets: '',
    metric: '',
    operator: '>',
    threshold: '',
    notificationMethods: ['email'],
    contacts: '',
    enabled: true
  })
  createRuleDialog.value = false
}

const saveRule = () => {
  ruleFormRef.value?.validate((valid) => {
    if (valid) {
      // 模拟保存
      setTimeout(() => {
        ElMessage.success('规则创建成功！')
        handleCloseCreateDialog()
        
        // 添加到规则列表
        const newRuleItem = {
          id: Date.now(),
          name: newRule.name,
          type: newRule.type,
          priority: newRule.priority,
          status: newRule.enabled ? 'active' : 'paused',
          enabled: newRule.enabled,
          description: newRule.description,
          targets: newRule.targets,
          lastTrigger: '未触发',
          todayTriggers: 0,
          tags: ['新建规则'],
          icon: getTypeIcon(newRule.type),
          iconColor: '#1e3c72'
        }
        mockRules.value.unshift(newRuleItem)
        stats.activeRules++
      }, 1000)
    }
  })
}

const editRule = (rule) => {
  ElMessage.success(`正在编辑规则: ${rule.name}`)
}

const viewTrend = (rule) => {
  ElMessage.success(`正在查看 ${rule.name} 的趋势分析`)
}

const cloneRule = (rule) => {
  setTimeout(() => {
    ElMessage.success(`规则 "${rule.name}" 已克隆成功！`)
  }, 500)
}

const toggleRule = (rule) => {
  rule.enabled = !rule.enabled
  rule.status = rule.enabled ? 'active' : 'paused'
  ElMessage.success(`规则已${rule.enabled ? '启用' : '暂停'}`)
}

const deleteRule = (rule) => {
  selectedRule.value = rule
  deleteConfirmDialog.value = true
}

const confirmDelete = () => {
  const index = mockRules.value.findIndex(r => r.id === selectedRule.value.id)
  if (index > -1) {
    mockRules.value.splice(index, 1)
    stats.activeRules--
    ElMessage.success('规则已删除')
  }
  deleteConfirmDialog.value = false
  selectedRule.value = null
}

const showAIRecommendations = () => {
  aiRecommendDialog.value = true
}

const applyAIRecommendations = () => {
  const selectedRecommendations = detailedRecommendations.value.filter(r => r.selected)
  if (selectedRecommendations.length === 0) {
    ElMessage.error('请至少选择一个推荐规则')
    return
  }
  
  setTimeout(() => {
    aiRecommendDialog.value = false
    ElMessage.success(`已应用 ${selectedRecommendations.length} 个AI推荐规则`)
  }, 1000)
}

const showSystemStatus = () => {
  ElMessage.success('系统状态：所有服务运行正常')
}

const batchOperations = () => {
  ElMessage.success('批量操作功能正在开发中...')
}

const importRules = () => {
  ElMessage.success('规则导入功能正在开发中...')
}

const exportRules = () => {
  ElMessage.success('规则导出功能正在开发中...')
}

const applyTemplate = (template) => {
  newRule.name = template.name
  newRule.type = template.type
  newRule.priority = template.priority
  newRule.description = template.description
  
  createNewRule()
  ElMessage.success(`已应用 "${template.name}" 模板`)
}

const selectTargets = () => {
  ElMessage.success('目标选择功能正在开发中...')
}

const getPriorityType = (priority) => {
  const types = {
    high: 'danger',
    medium: 'warning',
    low: 'success'
  }
  return types[priority] || 'info'
}

const getPriorityLabel = (priority) => {
  const labels = {
    high: '高优先级',
    medium: '中优先级',
    low: '低优先级'
  }
  return labels[priority] || priority
}

const getTypeIcon = (type) => {
  const icons = {
    infrastructure: 'Monitor',
    network: 'Connection',
    application: 'Platform',
    middleware: 'Box',
    database: 'Coin',
    business: 'BriefCase',
    security: 'Shield'
  }
  return icons[type] || 'Monitor'
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
}

// 初始化
onMounted(() => {
  pagination.total = mockRules.value.length
})
</script>

<style lang="scss" scoped>
.alert-settings-container {
  padding: 20px;
  background-color: #f8f9fc;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;

  h4 {
    margin: 0;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .el-breadcrumb {
    margin-top: 5px;
    
    :deep(.el-breadcrumb__item) {
      .el-breadcrumb__inner {
        color: rgba(255, 255, 255, 0.8);
        
        &:hover {
          color: white;
        }
      }
      
      &.is-link {
        .el-breadcrumb__inner {
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.stats-overview {
  margin-bottom: 20px;

  .stat-card {
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
    }

    .stat-content {
      display: flex;
      align-items: center;
      padding: 20px;

      .stat-icon {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        font-size: 20px;
        color: white;

        &.success {
          background: var(--el-color-success);
        }

        &.warning {
          background: var(--el-color-warning);
        }

        &.info {
          background: var(--el-color-info);
        }

        &.danger {
          background: var(--el-color-danger);
        }
      }

      .stat-info {
        flex: 1;

        .stat-number {
          font-size: 24px;
          font-weight: bold;
          color: #2c3e50;
          margin-bottom: 5px;
        }

        .stat-label {
          color: #6c757d;
          font-size: 14px;
          margin-bottom: 10px;
        }

        .stat-status {
          display: flex;
          align-items: center;
          font-size: 12px;
          gap: 4px;

          &.success {
            color: var(--el-color-success);
          }

          &.warning {
            color: var(--el-color-warning);
          }

          &.info {
            color: var(--el-color-info);
          }

          &.danger {
            color: var(--el-color-danger);
          }
        }
      }
    }
  }
}

.monitoring-types-card {
  margin-bottom: 20px;

  .monitoring-types {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;

    .type-tag {
      cursor: pointer;
      padding: 8px 16px;
      font-size: 14px;
      display: flex;
      align-items: center;
      gap: 6px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-1px);
      }
    }
  }
}

.ai-recommendation-card {
  margin-bottom: 20px;

  .ai-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    margin: -20px -20px 20px -20px;
    padding: 20px;
    border-radius: 8px 8px 0 0;
  }

  .ai-recommendations {
    .recommendation-intro {
      margin-bottom: 15px;
      color: #6c757d;
    }

    .recommendation-item {
      background: rgba(102, 126, 234, 0.1);
      border-radius: 8px;
      padding: 15px;
      border-left: 4px solid rgba(102, 126, 234, 0.3);

      .recommendation-title {
        font-weight: 600;
        margin-bottom: 5px;
      }

      .recommendation-description {
        font-size: 13px;
        color: #6c757d;
      }
    }
  }
}

.filter-card {
  margin-bottom: 20px;

  .filter-sections {
    .filter-section {
      margin-bottom: 20px;

      .filter-title {
        font-weight: 600;
        margin-bottom: 10px;
        color: #2c3e50;
      }

      .filter-chips {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .filter-chip {
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            transform: translateY(-1px);
          }
        }
      }
    }

    .filter-actions {
      display: flex;
      gap: 10px;

      .el-button {
        flex: 1;
      }
    }
  }
}

.template-card {
  .template-header {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
    margin: -20px -20px 20px -20px;
    padding: 20px;
    border-radius: 8px 8px 0 0;
  }

  .rule-templates {
    .template-intro {
      margin-bottom: 15px;
      color: #6c757d;
    }

    .template-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 15px;

      .template-item {
        background: rgba(240, 147, 251, 0.1);
        border-radius: 8px;
        padding: 15px;
        cursor: pointer;
        transition: all 0.3s;
        border: 1px solid rgba(240, 147, 251, 0.2);

        &:hover {
          background: rgba(240, 147, 251, 0.2);
          transform: translateY(-2px);
        }

        .template-title {
          font-weight: 600;
          margin-bottom: 8px;
        }

        .template-description {
          font-size: 13px;
          color: #6c757d;
        }
      }
    }
  }
}

.rules-list-card {
  .rules-list {
    .rule-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 15px;
      border-left: 4px solid #1e3c72;
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
      }

      .rule-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;

        .rule-title {
          font-weight: 600;
          font-size: 16px;
          color: #2c3e50;
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .rule-status {
          display: flex;
          align-items: center;
          gap: 8px;

          .status-indicator {
            width: 12px;
            height: 12px;
            border-radius: 50%;

            &.active {
              background: var(--el-color-success);
              animation: pulse 2s infinite;
            }

            &.paused {
              background: #6c757d;
            }
          }
        }
      }

      .rule-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 15px;
      }

      .rule-description {
        color: #6c757d;
        font-size: 14px;
        line-height: 1.5;
        margin-bottom: 15px;
      }

      .rule-targets {
        background: #f8f9fa;
        border-radius: 8px;
        padding: 12px;
        margin-bottom: 15px;
        font-size: 13px;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .rule-actions {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;

  .card-actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.ai-recommendations-list {
  margin-top: 20px;

  .recommendation-card {
    border: 1px solid #e1e8ed;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 15px;

    .recommendation-content {
      margin-left: 25px;

      h6 {
        margin-bottom: 8px;
        font-weight: 600;
      }

      p {
        margin-bottom: 8px;
        color: #6c757d;
      }
    }
  }
}

.delete-confirm-content {
  text-align: center;

  .delete-icon {
    margin-bottom: 15px;
  }

  h5 {
    margin-bottom: 10px;
  }

  p {
    margin-bottom: 10px;
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(40, 167, 69, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(40, 167, 69, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(40, 167, 69, 0);
  }
}

.text-success {
  color: var(--el-color-success);
}

.text-warning {
  color: var(--el-color-warning);
}

.text-danger {
  color: var(--el-color-danger);
}

.text-muted {
  color: #6c757d;
}
</style> 