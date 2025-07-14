<template>
  <div class="history-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">
            <el-icon class="title-icon"><Clock /></el-icon>
            历史记录
          </h2>
          <p class="page-subtitle">查看和回放历史数据</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
          <el-button @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </div>

    <!-- 过滤器 -->
    <el-card shadow="hover" class="filter-card">
      <div class="filter-content">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <div class="filter-item">
              <label class="filter-label">日期范围</label>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                size="default"
                style="width: 100%"
                @change="handleDateChange"
              />
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="filter-item">
              <label class="filter-label">记录类型</label>
              <el-select 
                v-model="filters.recordType" 
                placeholder="选择类型" 
                style="width: 100%"
                @change="handleFilterChange"
              >
                <el-option label="所有类型" value="" />
                <el-option label="报警事件" value="alarm" />
                <el-option label="警告" value="warning" />
                <el-option label="系统事件" value="system" />
                <el-option label="用户操作" value="user" />
                <el-option label="设备状态" value="device" />
              </el-select>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="filter-item">
              <label class="filter-label">设备筛选</label>
              <el-select 
                v-model="filters.deviceId" 
                placeholder="选择设备" 
                style="width: 100%"
                @change="handleFilterChange"
              >
                <el-option label="所有设备" value="" />
                <el-option 
                  v-for="device in deviceOptions" 
                  :key="device.id" 
                  :label="device.name" 
                  :value="device.id" 
                />
              </el-select>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="filter-item">
              <label class="filter-label">关键词搜索</label>
              <el-input
                v-model="filters.keyword"
                placeholder="输入关键词"
                @input="handleSearch"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </el-col>
        </el-row>
        <el-row class="filter-actions">
          <el-col :span="24">
            <el-button type="primary" @click="applyFilters">
              <el-icon><Setting /></el-icon>
              应用筛选
            </el-button>
            <el-button @click="resetFilters">
              重置
            </el-button>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 历史记录内容 -->
    <el-card shadow="hover" class="history-content-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="card-title">历史记录列表</span>
            <el-tag class="record-count">共 {{ total }} 条记录</el-tag>
          </div>
          <div class="view-toggle">
            <el-button-group>
              <el-button 
                :type="viewMode === 'timeline' ? 'primary' : ''"
                @click="viewMode = 'timeline'"
                size="small"
              >
                <el-icon><Clock /></el-icon>
                时间线
              </el-button>
              <el-button 
                :type="viewMode === 'table' ? 'primary' : ''"
                @click="viewMode = 'table'"
                size="small"
              >
                <el-icon><List /></el-icon>
                表格
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <!-- 时间线视图 -->
      <div v-if="viewMode === 'timeline'" class="timeline-view" v-loading="loading">
        <div class="timeline-container">
          <template v-for="(group, date) in groupedRecords" :key="date">
            <div class="timeline-date-header">
              <div class="date-line"></div>
              <div class="date-badge">{{ formatDateHeader(date) }}</div>
              <div class="date-line"></div>
            </div>
            
            <div class="timeline-items">
              <div 
                v-for="record in group" 
                :key="record.id"
                class="timeline-item"
                :class="getTimelineItemClass(record.type)"
                @click="handleRecordClick(record)"
              >
                <div class="timeline-time">{{ formatTime(record.time) }}</div>
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <div class="timeline-card">
                    <div class="timeline-header">
                      <div class="timeline-title">{{ record.title }}</div>
                      <el-tag :type="getRecordTypeTag(record.type)" size="small">
                        {{ getRecordTypeText(record.type) }}
                      </el-tag>
                    </div>
                    <div class="timeline-description">{{ record.description }}</div>
                    <div class="timeline-footer">
                      <div class="timeline-device">
                        <el-icon><VideoCamera /></el-icon>
                        <span>{{ record.deviceName }}</span>
                        <span class="device-location">{{ record.location }}</span>
                      </div>
                      <div class="timeline-actions">
                        <el-button 
                          v-if="record.type === 'alarm'"
                          size="small" 
                          type="primary" 
                          @click.stop="handleRecordAction(record, 'handle')"
                        >
                          处理
                        </el-button>
                        <el-button 
                          size="small" 
                          @click.stop="handleRecordAction(record, 'detail')"
                        >
                          详情
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore">
          <el-button @click="loadMore" :loading="loadingMore">
            <el-icon><Refresh /></el-icon>
            加载更多记录
          </el-button>
        </div>
      </div>

      <!-- 表格视图 -->
      <div v-if="viewMode === 'table'" class="table-view" v-loading="loading">
        <el-table :data="historyRecords" style="width: 100%" height="600">
          <el-table-column prop="time" label="时间" width="180" fixed="left">
            <template #default="{ row }">
              <div class="time-cell">
                <div class="date">{{ formatDate(row.time) }}</div>
                <div class="time">{{ formatTime(row.time) }}</div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="type" label="类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getRecordTypeTag(row.type)" size="small">
                {{ getRecordTypeText(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="title" label="事件" min-width="200" show-overflow-tooltip />
          
          <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
          
          <el-table-column prop="deviceName" label="设备" width="150" show-overflow-tooltip />
          
          <el-table-column prop="location" label="位置" width="150" show-overflow-tooltip />
          
          <el-table-column prop="operator" label="操作员" width="120" />
          
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleRecordAction(row, 'detail')">
                详情
              </el-button>
              <el-button 
                v-if="row.type === 'alarm' && row.status === 'pending'"
                type="warning" 
                link 
                @click="handleRecordAction(row, 'handle')"
              >
                处理
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialog.visible" :title="detailDialog.title" width="800px">
      <div class="detail-content" v-if="detailDialog.record">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="事件类型">
            <el-tag :type="getRecordTypeTag(detailDialog.record.type)">
              {{ getRecordTypeText(detailDialog.record.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发生时间">
            {{ formatDateTime(detailDialog.record.time) }}
          </el-descriptions-item>
          <el-descriptions-item label="设备名称">
            {{ detailDialog.record.deviceName }}
          </el-descriptions-item>
          <el-descriptions-item label="设备位置">
            {{ detailDialog.record.location }}
          </el-descriptions-item>
          <el-descriptions-item label="事件标题" :span="2">
            {{ detailDialog.record.title }}
          </el-descriptions-item>
          <el-descriptions-item label="详细描述" :span="2">
            {{ detailDialog.record.description }}
          </el-descriptions-item>
          <el-descriptions-item label="操作员" v-if="detailDialog.record.operator">
            {{ detailDialog.record.operator }}
          </el-descriptions-item>
          <el-descriptions-item label="处理状态" v-if="detailDialog.record.type === 'alarm'">
            <el-tag :type="detailDialog.record.status === 'handled' ? 'success' : 'warning'">
              {{ detailDialog.record.status === 'handled' ? '已处理' : '待处理' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 相关图片或视频 -->
        <div class="media-section" v-if="detailDialog.record.media">
          <h4>相关媒体</h4>
          <div class="media-grid">
            <div v-for="media in detailDialog.record.media" :key="media.id" class="media-item">
              <el-image
                v-if="media.type === 'image'"
                :src="media.url"
                :preview-src-list="[media.url]"
                class="media-preview"
              />
              <video 
                v-if="media.type === 'video'"
                :src="media.url"
                controls
                class="media-preview"
              />
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock, Download, Refresh, Search, Setting, List, VideoCamera
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const loadingMore = ref(false)
const viewMode = ref('timeline') // timeline | table
const dateRange = ref([])
const total = ref(0)
const hasMore = ref(true)

// 过滤器
const filters = reactive({
  recordType: '',
  deviceId: '',
  keyword: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20
})

// 详情对话框
const detailDialog = reactive({
  visible: false,
  title: '',
  record: null
})

// 设备选项
const deviceOptions = ref([
  { id: 'CAM-2023001', name: '前门监控' },
  { id: 'CAM-2023002', name: '后门监控' },
  { id: 'CAM-2023003', name: '办公区监控' },
  { id: 'CAM-2023004', name: '停车场监控' },
  { id: 'CAM-2023005', name: '仓库监控' },
  { id: 'CAM-2023006', name: '大门入口监控' }
])

// Mock 历史记录数据
const historyRecords = ref([
  {
    id: 1,
    time: '2024-01-20 10:45:23',
    type: 'alarm',
    title: '检测到异常行为',
    description: '在前门区域检测到可疑人员徘徊行为，已触发警报',
    deviceName: '前门监控',
    deviceId: 'CAM-2023001',
    location: '一楼大厅',
    operator: '',
    status: 'pending',
    media: [
      { id: 1, type: 'image', url: '/api/placeholder/400/300' }
    ]
  },
  {
    id: 2,
    time: '2024-01-20 10:32:10',
    type: 'user',
    title: '用户登录系统',
    description: '系统管理员 (admin) 登录了系统。IP: 192.168.1.55',
    deviceName: '管理终端',
    deviceId: 'TERMINAL-001',
    location: '控制室',
    operator: '系统管理员',
    status: 'completed'
  },
  {
    id: 3,
    time: '2024-01-20 10:15:45',
    type: 'device',
    title: '设备状态变更',
    description: '前门门禁设备恢复在线状态，连接正常',
    deviceName: '前门门禁',
    deviceId: 'DOOR-2023001',
    location: '一楼前门',
    operator: '系统',
    status: 'completed'
  },
  {
    id: 4,
    time: '2024-01-20 09:22:37',
    type: 'warning',
    title: '高人流量警告',
    description: '一楼大厅检测到高人流量，当前人数：35人，超过预设阈值 (30人)',
    deviceName: '前门监控',
    deviceId: 'CAM-2023001',
    location: '一楼大厅',
    operator: '',
    status: 'resolved'
  },
  {
    id: 5,
    time: '2024-01-20 08:30:00',
    type: 'system',
    title: '系统维护',
    description: '系统完成每日数据备份和清理操作',
    deviceName: '系统',
    deviceId: 'SYS-001',
    location: '服务器机房',
    operator: '系统',
    status: 'completed'
  },
  {
    id: 6,
    time: '2024-01-19 18:45:12',
    type: 'warning',
    title: '设备连接异常',
    description: '大门入口监控设备连接不稳定，信号质量下降',
    deviceName: '大门入口监控',
    deviceId: 'CAM-2023006',
    location: '大门入口',
    operator: '',
    status: 'resolved'
  },
  {
    id: 7,
    time: '2024-01-19 16:30:45',
    type: 'alarm',
    title: '非授权访问',
    description: '仓库区域检测到非授权人员进入，已触发报警',
    deviceName: '仓库监控',
    deviceId: 'CAM-2023005',
    location: '仓库区域',
    operator: '',
    status: 'handled'
  },
  {
    id: 8,
    time: '2024-01-19 14:22:10',
    type: 'user',
    title: '设备配置更新',
    description: '安全管理员 (zhangsan) 更新了办公区监控的灵敏度设置',
    deviceName: '办公区监控',
    deviceId: 'CAM-2023003',
    location: '办公区',
    operator: '安全管理员',
    status: 'completed'
  }
])

// 按日期分组的记录
const groupedRecords = computed(() => {
  const groups = {}
  historyRecords.value.forEach(record => {
    const date = record.time.split(' ')[0]
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(record)
  })
  
  // 按时间排序
  Object.keys(groups).forEach(date => {
    groups[date].sort((a, b) => new Date(b.time) - new Date(a.time))
  })
  
  return groups
})

// 获取记录类型标签类型
const getRecordTypeTag = (type) => {
  const tagMap = {
    'alarm': 'danger',
    'warning': 'warning', 
    'user': 'info',
    'device': 'success',
    'system': ''
  }
  return tagMap[type] || 'info'
}

// 获取记录类型文本
const getRecordTypeText = (type) => {
  const textMap = {
    'alarm': '报警事件',
    'warning': '警告',
    'user': '用户操作',
    'device': '设备状态',
    'system': '系统事件'
  }
  return textMap[type] || type
}

// 获取时间线项目样式类
const getTimelineItemClass = (type) => {
  return `timeline-${type}`
}

// 日期时间格式化
const formatDateTime = (datetime) => {
  return new Date(datetime).toLocaleString('zh-CN')
}

const formatDate = (datetime) => {
  return new Date(datetime).toLocaleDateString('zh-CN')
}

const formatTime = (datetime) => {
  return new Date(datetime).toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit', 
    second: '2-digit' 
  })
}

const formatDateHeader = (date) => {
  const today = new Date().toISOString().split('T')[0]
  const yesterday = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  
  if (date === today) return '今天'
  if (date === yesterday) return '昨天'
  return new Date(date).toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
}

// 事件处理函数
const handleDateChange = () => {
  applyFilters()
}

const handleFilterChange = () => {
  applyFilters()
}

const handleSearch = () => {
  // 实现搜索逻辑
}

const applyFilters = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    loading.value = false
    ElMessage.success('筛选完成')
  }, 500)
}

const resetFilters = () => {
  filters.recordType = ''
  filters.deviceId = ''
  filters.keyword = ''
  dateRange.value = []
  applyFilters()
}

const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出历史记录数据吗？', '确认导出', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    ElMessage.success('导出任务已启动，请稍候...')
    // 模拟导出过程
    setTimeout(() => {
      ElMessage.success('数据导出完成')
    }, 2000)
  } catch {
    // 用户取消
  }
}

const handleRefresh = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('数据已刷新')
  }, 800)
}

const handleRecordClick = (record) => {
  detailDialog.record = record
  detailDialog.title = record.title
  detailDialog.visible = true
}

const handleRecordAction = (record, action) => {
  switch (action) {
    case 'detail':
      handleRecordClick(record)
      break
    case 'handle':
      ElMessageBox.confirm('确定要处理这个报警事件吗？', '确认处理', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        record.status = 'handled'
        ElMessage.success('报警事件已处理')
      }).catch(() => {
        // 用户取消
      })
      break
  }
}

const loadMore = () => {
  loadingMore.value = true
  setTimeout(() => {
    loadingMore.value = false
    // 模拟加载更多数据
    ElMessage.info('已加载更多记录')
  }, 1000)
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  applyFilters()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  applyFilters()
}

// 初始化
onMounted(() => {
  total.value = historyRecords.value.length
  // 设置默认日期范围为最近7天
  const now = new Date()
  const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
  dateRange.value = [
    sevenDaysAgo.toISOString().split('T')[0],
    now.toISOString().split('T')[0]
  ]
})
</script>

<style lang="scss" scoped>
.history-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    
    .header-left {
      .page-title {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
        
        .title-icon {
          color: #409eff;
        }
      }
      
      .page-subtitle {
        color: #909399;
        margin: 0;
        font-size: 14px;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
  
  .filter-content {
    .filter-item {
      margin-bottom: 16px;
      
      .filter-label {
        display: block;
        font-size: 14px;
        font-weight: 600;
        color: #606266;
        margin-bottom: 8px;
      }
    }
    
    .filter-actions {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #ebeef5;
    }
  }
}

.history-content-card {
  border-radius: 8px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
      
      .record-count {
        background-color: #f0f2f5;
        color: #909399;
        border: none;
      }
    }
  }
}

// 时间线样式
.timeline-view {
  .timeline-container {
    position: relative;
    
    .timeline-date-header {
      display: flex;
      align-items: center;
      margin: 32px 0 24px 0;
      
      .date-line {
        flex: 1;
        height: 1px;
        background: linear-gradient(to right, transparent, #e4e7ed, transparent);
      }
      
      .date-badge {
        padding: 8px 16px;
        background: #409eff;
        color: white;
        border-radius: 16px;
        font-size: 14px;
        font-weight: 600;
        margin: 0 16px;
      }
    }
    
    .timeline-items {
      .timeline-item {
        display: flex;
        margin-bottom: 24px;
        position: relative;
        
        &::before {
          content: '';
          position: absolute;
          left: 68px;
          top: 40px;
          bottom: -24px;
          width: 2px;
          background: #e4e7ed;
        }
        
        &:last-child::before {
          display: none;
        }
        
        .timeline-time {
          width: 60px;
          text-align: right;
          font-size: 13px;
          color: #909399;
          font-weight: 600;
          padding-top: 20px;
          flex-shrink: 0;
        }
        
        .timeline-dot {
          width: 16px;
          height: 16px;
          border-radius: 50%;
          margin: 0 16px;
          margin-top: 20px;
          flex-shrink: 0;
          border: 3px solid #fff;
          box-shadow: 0 0 0 2px #e4e7ed;
        }
        
        .timeline-content {
          flex: 1;
          
          .timeline-card {
            background: white;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
            border: 1px solid #ebeef5;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
              transform: translateY(-2px);
            }
            
            .timeline-header {
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              margin-bottom: 12px;
              
              .timeline-title {
                font-weight: 600;
                color: #303133;
                font-size: 16px;
                flex: 1;
              }
            }
            
            .timeline-description {
              color: #606266;
              line-height: 1.6;
              margin-bottom: 16px;
            }
            
            .timeline-footer {
              display: flex;
              justify-content: space-between;
              align-items: center;
              
              .timeline-device {
                display: flex;
                align-items: center;
                gap: 8px;
                color: #909399;
                font-size: 13px;
                
                .device-location {
                  color: #c0c4cc;
                  
                  &::before {
                    content: '•';
                    margin: 0 6px;
                  }
                }
              }
              
              .timeline-actions {
                display: flex;
                gap: 8px;
              }
            }
          }
        }
        
        // 不同类型的颜色
        &.timeline-alarm .timeline-dot {
          background: #f56c6c;
          box-shadow: 0 0 0 2px #f56c6c;
        }
        
        &.timeline-warning .timeline-dot {
          background: #e6a23c;
          box-shadow: 0 0 0 2px #e6a23c;
        }
        
        &.timeline-user .timeline-dot {
          background: #409eff;
          box-shadow: 0 0 0 2px #409eff;
        }
        
        &.timeline-device .timeline-dot {
          background: #67c23a;
          box-shadow: 0 0 0 2px #67c23a;
        }
        
        &.timeline-system .timeline-dot {
          background: #909399;
          box-shadow: 0 0 0 2px #909399;
        }
      }
    }
  }
  
  .load-more {
    text-align: center;
    margin-top: 32px;
    padding-top: 32px;
    border-top: 1px solid #ebeef5;
  }
}

// 表格样式
.table-view {
  .time-cell {
    .date {
      font-weight: 600;
      color: #303133;
    }
    
    .time {
      font-size: 12px;
      color: #909399;
      margin-top: 2px;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}

// 详情对话框样式
.detail-content {
  .media-section {
    margin-top: 24px;
    
    h4 {
      margin-bottom: 16px;
      color: #303133;
    }
    
    .media-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;
      
      .media-item {
        .media-preview {
          width: 100%;
          height: 150px;
          object-fit: cover;
          border-radius: 6px;
          border: 1px solid #ebeef5;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .history-container {
    padding: 12px;
  }
  
  .page-header .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .timeline-view .timeline-container .timeline-items .timeline-item {
    flex-direction: column;
    
    .timeline-time {
      width: auto;
      text-align: left;
      padding-top: 0;
      margin-bottom: 8px;
    }
    
    .timeline-dot {
      margin: 0 0 12px 0;
    }
    
    &::before {
      display: none;
    }
  }
}
</style> 