<template>
  <div class="storage-settings-container">
    <div class="page-header">
      <div>
        <h4>存储管理</h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/settings' }">系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>存储管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div>
        <el-button type="primary" @click="saveStorageSettings">
          <el-icon><Save /></el-icon> 保存配置
        </el-button>
      </div>
    </div>
    
    <!-- 存储状态概览 -->
    <el-alert 
      :type="storageStatus.type" 
      :title="storageStatus.title" 
      :description="storageStatus.description"
      :closable="false"
      show-icon
      class="storage-status-alert">
      <template #default>
        <div class="status-actions">
          <el-button size="small" @click="refreshStorageStatus">
            <el-icon><Refresh /></el-icon> 刷新状态
          </el-button>
        </div>
      </template>
    </el-alert>
    
    <!-- 存储管理页面导航标签 -->
    <el-tabs v-model="activeTab" class="storage-tabs">
      <el-tab-pane label="存储设备" name="devices">
        <div class="storage-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 存储设备列表 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <div class="header-left">
                      <el-icon><HardDisk /></el-icon> 存储设备
                    </div>
                    <el-button type="primary" size="small" @click="addStorageDevice">
                      <el-icon><Plus /></el-icon> 添加设备
                    </el-button>
                  </div>
                </template>
                <div class="device-list">
                  <div 
                    v-for="(device, index) in storageDevices" 
                    :key="index"
                    class="device-item">
                    <div class="device-header">
                      <div class="device-title">
                        <span>{{ device.name }} ({{ device.type }})</span>
                        <el-switch 
                          v-model="device.enabled" 
                          @change="toggleDevice(device)"
                          class="device-switch">
                        </el-switch>
                      </div>
                    </div>
                    <div class="device-badges">
                      <el-tag 
                        :type="device.type === 'SSD' ? 'primary' : 'success'"
                        size="small">
                        {{ device.type }}
                      </el-tag>
                      <el-tag 
                        :type="device.status === 'normal' ? 'success' : 'danger'"
                        size="small">
                        {{ device.statusLabel }}
                      </el-tag>
                    </div>
                    <div class="device-details">
                      <el-row :gutter="20">
                        <el-col :span="12">
                          <div class="detail-item">
                            <span class="detail-label">总容量:</span>
                            <span class="detail-value">{{ device.totalCapacity }}</span>
                          </div>
                          <div class="detail-item">
                            <span class="detail-label">已用空间:</span>
                            <span class="detail-value">{{ device.usedSpace }}</span>
                          </div>
                          <div class="detail-item">
                            <span class="detail-label">挂载点:</span>
                            <span class="detail-value">{{ device.mountPoint }}</span>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="detail-item">
                            <span class="detail-label">可用空间:</span>
                            <span class="detail-value">{{ device.availableSpace }}</span>
                          </div>
                          <div class="detail-item">
                            <span class="detail-label">使用率:</span>
                            <div class="usage-progress">
                              <el-progress 
                                :percentage="device.usagePercent" 
                                :stroke-width="6"
                                :color="getUsageColor(device.usagePercent)">
                              </el-progress>
                              <span class="usage-text">{{ device.usagePercent }}%</span>
                            </div>
                          </div>
                          <div class="detail-item">
                            <span class="detail-label">文件系统:</span>
                            <span class="detail-value">{{ device.filesystem }}</span>
                          </div>
                        </el-col>
                      </el-row>
                    </div>
                    <div class="device-actions">
                      <el-button size="small" type="primary" @click="editDevice(device)">
                        <el-icon><Edit /></el-icon> 编辑
                      </el-button>
                      <el-button size="small" @click="checkDevice(device)">
                        <el-icon><Refresh /></el-icon> 检查
                      </el-button>
                      <el-button size="small" type="info" @click="showDeviceDetails(device)">
                        <el-icon><InfoFilled /></el-icon> 详情
                      </el-button>
                      <el-dropdown>
                        <el-button size="small" type="text">
                          更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item @click="formatDevice(device)">
                              <el-icon><Delete /></el-icon> 格式化
                            </el-dropdown-item>
                            <el-dropdown-item @click="mountDevice(device)" v-if="!device.mounted">
                              <el-icon><Connection /></el-icon> 挂载
                            </el-dropdown-item>
                            <el-dropdown-item @click="unmountDevice(device)" v-if="device.mounted">
                              <el-icon><Close /></el-icon> 卸载
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <!-- 存储概览卡片 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><PieChart /></el-icon> 存储概览
                  </div>
                </template>
                <div class="storage-overview">
                  <div class="overview-item">
                    <span class="overview-label">总存储空间</span>
                    <span class="overview-value">{{ storageOverview.totalSpace }}</span>
                  </div>
                  <div class="overview-item">
                    <span class="overview-label">已用空间</span>
                    <span class="overview-value">{{ storageOverview.usedSpace }}</span>
                  </div>
                  <div class="overview-item">
                    <span class="overview-label">可用空间</span>
                    <span class="overview-value">{{ storageOverview.availableSpace }}</span>
                  </div>
                  <div class="overview-item">
                    <span class="overview-label">使用率</span>
                    <span class="overview-value">{{ storageOverview.usagePercent }}%</span>
                  </div>
                  <div class="overview-item">
                    <span class="overview-label">设备数量</span>
                    <span class="overview-value">{{ storageOverview.deviceCount }}</span>
                  </div>
                  <el-divider></el-divider>
                  <el-button type="primary" @click="refreshStorageStatus" style="width: 100%">
                    <el-icon><Refresh /></el-icon> 刷新状态
                  </el-button>
                </div>
              </el-card>
              
              <!-- 存储性能卡片 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><TrendCharts /></el-icon> 存储性能
                  </div>
                </template>
                <div class="storage-performance">
                  <div class="performance-item">
                    <span class="performance-label">读取速度</span>
                    <span class="performance-value">{{ storagePerformance.readSpeed }}</span>
                  </div>
                  <div class="performance-item">
                    <span class="performance-label">写入速度</span>
                    <span class="performance-value">{{ storagePerformance.writeSpeed }}</span>
                  </div>
                  <div class="performance-item">
                    <span class="performance-label">IOPS</span>
                    <span class="performance-value">{{ storagePerformance.iops }}</span>
                  </div>
                  <div class="performance-item">
                    <span class="performance-label">延迟</span>
                    <span class="performance-value">{{ storagePerformance.latency }}</span>
                  </div>
                  <div class="performance-item">
                    <span class="performance-label">队列深度</span>
                    <span class="performance-value">{{ storagePerformance.queueDepth }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="备份管理" name="backup">
        <div class="storage-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 备份设置卡片 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><DocumentCopy /></el-icon> 备份设置
                  </div>
                </template>
                <el-form :model="backupSettings" label-width="150px">
                  <el-form-item>
                    <el-checkbox v-model="backupSettings.autoBackup">启用自动备份</el-checkbox>
                  </el-form-item>
                  <div v-if="backupSettings.autoBackup" class="backup-config">
                    <el-form-item label="备份频率">
                      <el-select v-model="backupSettings.frequency" style="width: 200px">
                        <el-option label="每天" value="daily"></el-option>
                        <el-option label="每周" value="weekly"></el-option>
                        <el-option label="每月" value="monthly"></el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item label="备份时间">
                      <el-time-picker v-model="backupSettings.backupTime" format="HH:mm" style="width: 200px"></el-time-picker>
                    </el-form-item>
                    <el-form-item label="保留备份数量">
                      <el-input-number v-model="backupSettings.retentionCount" :min="1" :max="30" style="width: 200px"></el-input-number>
                    </el-form-item>
                    <el-form-item label="备份位置">
                      <el-select v-model="backupSettings.location" style="width: 200px">
                        <el-option label="本地存储" value="local"></el-option>
                        <el-option label="云存储" value="cloud"></el-option>
                        <el-option label="外部设备" value="external"></el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item label="备份内容">
                      <el-checkbox-group v-model="backupSettings.backupContent">
                        <el-checkbox label="config">系统配置</el-checkbox>
                        <el-checkbox label="data">监控数据</el-checkbox>
                        <el-checkbox label="logs">系统日志</el-checkbox>
                        <el-checkbox label="recordings">录像文件</el-checkbox>
                      </el-checkbox-group>
                    </el-form-item>
                  </div>
                  <el-form-item>
                    <el-button type="primary" @click="saveBackupSettings">
                      <el-icon><Save /></el-icon> 保存设置
                    </el-button>
                    <el-button @click="createManualBackup">
                      <el-icon><Upload /></el-icon> 立即备份
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
              
              <!-- 备份历史列表 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><FolderOpened /></el-icon> 备份历史
                  </div>
                </template>
                <el-table :data="backupHistory" style="width: 100%">
                  <el-table-column prop="name" label="备份名称" width="200"></el-table-column>
                  <el-table-column prop="type" label="类型" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.type === 'auto' ? 'success' : 'primary'" size="small">
                        {{ scope.row.type === 'auto' ? '自动' : '手动' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="size" label="大小" width="100"></el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getBackupStatusType(scope.row.status)" size="small">
                        {{ getBackupStatusText(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="200">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="restoreBackup(scope.row)" :disabled="scope.row.status !== 'completed'">
                        <el-icon><RefreshRight /></el-icon> 恢复
                      </el-button>
                      <el-button size="small" @click="downloadBackup(scope.row)" :disabled="scope.row.status !== 'completed'">
                        <el-icon><Download /></el-icon> 下载
                      </el-button>
                      <el-button size="small" type="danger" @click="deleteBackup(scope.row)">
                        <el-icon><Delete /></el-icon> 删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <!-- 备份状态卡片 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Clock /></el-icon> 备份状态
                  </div>
                </template>
                <div class="backup-status">
                  <div class="status-item">
                    <span class="status-label">最近备份</span>
                    <span class="status-value">{{ backupStatus.lastBackup }}</span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">下次备份</span>
                    <span class="status-value">{{ backupStatus.nextBackup }}</span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">备份总数</span>
                    <span class="status-value">{{ backupStatus.totalBackups }}</span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">占用空间</span>
                    <span class="status-value">{{ backupStatus.totalSize }}</span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">成功率</span>
                    <span class="status-value">{{ backupStatus.successRate }}%</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="数据清理" name="cleanup">
        <div class="storage-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 数据清理设置 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Delete /></el-icon> 数据清理
                  </div>
                </template>
                <el-form :model="cleanupSettings" label-width="120px">
                  <el-form-item label="清理类型">
                    <el-select v-model="cleanupSettings.type" style="width: 200px">
                      <el-option label="临时文件" value="temp"></el-option>
                      <el-option label="日志文件" value="logs"></el-option>
                      <el-option label="缓存数据" value="cache"></el-option>
                      <el-option label="过期数据" value="expired"></el-option>
                      <el-option label="录像文件" value="recordings"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="时间范围">
                    <el-select v-model="cleanupSettings.timeRange" style="width: 200px">
                      <el-option label="最近7天" value="7d"></el-option>
                      <el-option label="最近30天" value="30d"></el-option>
                      <el-option label="最近90天" value="90d"></el-option>
                      <el-option label="自定义" value="custom"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="预估清理空间" v-if="cleanupAnalysis.estimatedSize">
                    <el-tag type="success" size="large">{{ cleanupAnalysis.estimatedSize }}</el-tag>
                  </el-form-item>
                  <el-form-item>
                    <el-button @click="analyzeCleanup">
                      <el-icon><Search /></el-icon> 分析可清理数据
                    </el-button>
                    <el-button type="danger" @click="startCleanup" :disabled="!cleanupAnalysis.estimatedSize">
                      <el-icon><Delete /></el-icon> 开始清理
                    </el-button>
                  </el-form-item>
                </el-form>
                
                <el-alert 
                  title="注意事项" 
                  type="warning" 
                  :closable="false"
                  show-icon>
                  <ul>
                    <li>清理操作不可恢复</li>
                    <li>建议在系统负载较低时执行</li>
                    <li>请确保已进行必要的备份</li>
                  </ul>
                </el-alert>
              </el-card>
              
              <!-- 清理历史 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><List /></el-icon> 清理历史
                  </div>
                </template>
                <el-table :data="cleanupHistory" style="width: 100%">
                  <el-table-column prop="type" label="清理类型" width="120"></el-table-column>
                  <el-table-column prop="cleanedSize" label="清理大小" width="120"></el-table-column>
                  <el-table-column prop="duration" label="耗时" width="100"></el-table-column>
                  <el-table-column prop="startTime" label="开始时间" width="180"></el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.status === 'completed' ? 'success' : 'danger'" size="small">
                        {{ scope.row.status === 'completed' ? '完成' : '失败' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <!-- 自动清理设置 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Timer /></el-icon> 自动清理
                  </div>
                </template>
                <el-form :model="autoCleanupSettings" label-width="120px">
                  <el-form-item>
                    <el-checkbox v-model="autoCleanupSettings.enabled">启用自动清理</el-checkbox>
                  </el-form-item>
                  <div v-if="autoCleanupSettings.enabled" class="auto-cleanup-config">
                    <el-form-item label="清理频率">
                      <el-select v-model="autoCleanupSettings.frequency" style="width: 100%">
                        <el-option label="每天" value="daily"></el-option>
                        <el-option label="每周" value="weekly"></el-option>
                        <el-option label="每月" value="monthly"></el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item label="清理时间">
                      <el-time-picker v-model="autoCleanupSettings.cleanupTime" format="HH:mm" style="width: 100%"></el-time-picker>
                    </el-form-item>
                    <el-form-item label="存储阈值">
                      <el-input-number v-model="autoCleanupSettings.threshold" :min="50" :max="95" style="width: 100%"></el-input-number>
                      <div class="form-tip">当存储使用率超过此值时自动清理</div>
                    </el-form-item>
                    <el-form-item label="清理类型">
                      <el-checkbox-group v-model="autoCleanupSettings.cleanupTypes">
                        <el-checkbox label="temp">临时文件</el-checkbox>
                        <el-checkbox label="logs">日志文件</el-checkbox>
                        <el-checkbox label="cache">缓存数据</el-checkbox>
                      </el-checkbox-group>
                    </el-form-item>
                  </div>
                  <el-form-item>
                    <el-button type="primary" @click="saveAutoCleanupSettings">
                      <el-icon><Save /></el-icon> 保存设置
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="配额管理" name="quota">
        <div class="storage-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 配额设置 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Coin /></el-icon> 配额设置
                  </div>
                </template>
                <el-form :model="quotaSettings" label-width="150px">
                  <el-form-item>
                    <el-checkbox v-model="quotaSettings.enabled">启用存储配额</el-checkbox>
                  </el-form-item>
                  <div v-if="quotaSettings.enabled" class="quota-config">
                    <el-form-item label="默认用户配额">
                      <el-input-number v-model="quotaSettings.defaultUserQuota" :min="1" :max="1000" style="width: 200px"></el-input-number>
                      <span class="unit">GB</span>
                    </el-form-item>
                    <el-form-item label="默认设备配额">
                      <el-input-number v-model="quotaSettings.defaultDeviceQuota" :min="1" :max="1000" style="width: 200px"></el-input-number>
                      <span class="unit">GB</span>
                    </el-form-item>
                    <el-form-item label="告警阈值">
                      <el-input-number v-model="quotaSettings.alertThreshold" :min="50" :max="95" style="width: 200px"></el-input-number>
                      <span class="unit">%</span>
                    </el-form-item>
                    <el-form-item label="配额策略">
                      <el-radio-group v-model="quotaSettings.policy">
                        <el-radio label="soft">软限制（超出时警告）</el-radio>
                        <el-radio label="hard">硬限制（超出时拒绝）</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </div>
                  <el-form-item>
                    <el-button type="primary" @click="saveQuotaSettings">
                      <el-icon><Save /></el-icon> 保存设置
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
              
              <!-- 用户配额管理 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><User /></el-icon> 用户配额
                  </div>
                </template>
                <el-table :data="userQuotas" style="width: 100%">
                  <el-table-column prop="username" label="用户名" width="120"></el-table-column>
                  <el-table-column prop="quota" label="配额" width="100">
                    <template #default="scope">
                      {{ scope.row.quota }}GB
                    </template>
                  </el-table-column>
                  <el-table-column prop="used" label="已用" width="100">
                    <template #default="scope">
                      {{ scope.row.used }}GB
                    </template>
                  </el-table-column>
                  <el-table-column prop="usagePercent" label="使用率" width="120">
                    <template #default="scope">
                      <el-progress 
                        :percentage="scope.row.usagePercent" 
                        :stroke-width="6"
                        :color="getUsageColor(scope.row.usagePercent)">
                      </el-progress>
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getQuotaStatusType(scope.row.status)" size="small">
                        {{ getQuotaStatusText(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="editUserQuota(scope.row)">
                        <el-icon><Edit /></el-icon> 编辑
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <!-- 配额使用概览 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><DataAnalysis /></el-icon> 配额使用概览
                  </div>
                </template>
                <div class="quota-overview">
                  <div class="quota-item">
                    <span class="quota-label">总配额</span>
                    <span class="quota-value">{{ quotaOverview.totalQuota }}GB</span>
                  </div>
                  <div class="quota-item">
                    <span class="quota-label">已分配</span>
                    <span class="quota-value">{{ quotaOverview.allocatedQuota }}GB</span>
                  </div>
                  <div class="quota-item">
                    <span class="quota-label">实际使用</span>
                    <span class="quota-value">{{ quotaOverview.actualUsed }}GB</span>
                  </div>
                  <div class="quota-item">
                    <span class="quota-label">用户数量</span>
                    <span class="quota-value">{{ quotaOverview.userCount }}</span>
                  </div>
                  <div class="quota-item">
                    <span class="quota-label">设备数量</span>
                    <span class="quota-value">{{ quotaOverview.deviceCount }}</span>
                  </div>
                </div>
              </el-card>
              
              <!-- 存储告警设置 -->
              <el-card class="storage-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Bell /></el-icon> 存储告警
                  </div>
                </template>
                <el-form :model="alertSettings" label-width="120px">
                  <el-form-item label="告警阈值">
                    <el-input-number v-model="alertSettings.threshold" :min="50" :max="95" style="width: 100%"></el-input-number>
                    <div class="form-tip">当使用率超过此百分比时发出告警</div>
                  </el-form-item>
                  <el-form-item label="告警通知">
                    <el-checkbox-group v-model="alertSettings.notifications">
                      <el-checkbox label="email">邮件通知</el-checkbox>
                      <el-checkbox label="sms">短信通知</el-checkbox>
                      <el-checkbox label="webhook">Webhook通知</el-checkbox>
                    </el-checkbox-group>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="saveAlertSettings">
                      <el-icon><Save /></el-icon> 保存设置
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 移除API导入，因为我们使用mock数据
// import {
//   getStorageDevices,
//   getStorageOverview,
//   refreshStorageStatus,
//   ...
// } from '@/api/storage'

// 当前激活的标签页
const activeTab = ref('devices')

// 存储状态
const storageStatus = reactive({
  type: 'success',
  title: '存储状态正常',
  description: '所有存储设备运行正常，存储空间充足'
})

// 存储设备数据 (Mock数据)
const storageDevices = ref([
  {
    id: 1,
    name: '主存储',
    type: 'SSD',
    status: 'normal',
    statusLabel: '正常',
    enabled: true,
    totalCapacity: '1.0 TB',
    usedSpace: '600 GB',
    availableSpace: '400 GB',
    usagePercent: 60,
    filesystem: 'ext4',
    mountPoint: '/storage/main',
    mounted: true
  },
  {
    id: 2,
    name: '备份存储',
    type: 'HDD',
    status: 'normal',
    statusLabel: '正常',
    enabled: true,
    totalCapacity: '2.0 TB',
    usedSpace: '800 GB',
    availableSpace: '1.2 TB',
    usagePercent: 40,
    filesystem: 'ext4',
    mountPoint: '/storage/backup',
    mounted: true
  },
  {
    id: 3,
    name: '临时存储',
    type: 'SSD',
    status: 'warning',
    statusLabel: '警告',
    enabled: true,
    totalCapacity: '500 GB',
    usedSpace: '450 GB',
    availableSpace: '50 GB',
    usagePercent: 90,
    filesystem: 'ext4',
    mountPoint: '/storage/temp',
    mounted: true
  }
])

// 存储概览数据
const storageOverview = reactive({
  totalSpace: '3.5 TB',
  usedSpace: '1.85 TB',
  availableSpace: '1.65 TB',
  usagePercent: 53,
  deviceCount: 3
})

// 存储性能数据
const storagePerformance = reactive({
  readSpeed: '250 MB/s',
  writeSpeed: '180 MB/s',
  iops: '1,200',
  latency: '2.5 ms',
  queueDepth: '8'
})

// 备份设置
const backupSettings = reactive({
  autoBackup: true,
  frequency: 'daily',
  backupTime: '02:00',
  retentionCount: 7,
  location: 'local',
  backupContent: ['config', 'data', 'logs']
})

// 备份状态
const backupStatus = reactive({
  lastBackup: '2024-01-15 02:00:00',
  nextBackup: '2024-01-16 02:00:00',
  totalBackups: 15,
  totalSize: '2.5 GB',
  successRate: 98
})

// 备份历史
const backupHistory = ref([
  {
    id: 1,
    name: 'backup_20240115_020000',
    type: 'auto',
    size: '180 MB',
    createTime: '2024-01-15 02:00:00',
    status: 'completed'
  },
  {
    id: 2,
    name: 'backup_20240114_020000',
    type: 'auto',
    size: '175 MB',
    createTime: '2024-01-14 02:00:00',
    status: 'completed'
  },
  {
    id: 3,
    name: 'manual_backup_20240113',
    type: 'manual',
    size: '200 MB',
    createTime: '2024-01-13 14:30:00',
    status: 'completed'
  }
])

// 清理设置
const cleanupSettings = reactive({
  type: 'temp',
  timeRange: '30d'
})

// 清理分析结果
const cleanupAnalysis = reactive({
  estimatedSize: ''
})

// 自动清理设置
const autoCleanupSettings = reactive({
  enabled: false,
  frequency: 'weekly',
  cleanupTime: '03:00',
  threshold: 80,
  cleanupTypes: ['temp', 'logs', 'cache']
})

// 清理历史
const cleanupHistory = ref([
  {
    id: 1,
    type: '临时文件',
    cleanedSize: '2.5 GB',
    duration: '5分钟',
    startTime: '2024-01-15 03:00:00',
    status: 'completed'
  },
  {
    id: 2,
    type: '日志文件',
    cleanedSize: '1.8 GB',
    duration: '3分钟',
    startTime: '2024-01-14 03:00:00',
    status: 'completed'
  }
])

// 配额设置
const quotaSettings = reactive({
  enabled: true,
  defaultUserQuota: 50,
  defaultDeviceQuota: 100,
  alertThreshold: 80,
  policy: 'soft'
})

// 配额概览
const quotaOverview = reactive({
  totalQuota: 1000,
  allocatedQuota: 600,
  actualUsed: 450,
  userCount: 12,
  deviceCount: 6
})

// 用户配额
const userQuotas = ref([
  {
    id: 1,
    username: 'admin',
    quota: 100,
    used: 65,
    usagePercent: 65,
    status: 'normal'
  },
  {
    id: 2,
    username: 'operator1',
    quota: 50,
    used: 45,
    usagePercent: 90,
    status: 'warning'
  },
  {
    id: 3,
    username: 'operator2',
    quota: 50,
    used: 25,
    usagePercent: 50,
    status: 'normal'
  }
])

// 告警设置
const alertSettings = reactive({
  threshold: 80,
  notifications: ['email', 'sms']
})

// 获取使用率颜色
const getUsageColor = (percentage) => {
  if (percentage >= 90) return '#F56C6C'
  if (percentage >= 80) return '#E6A23C'
  if (percentage >= 60) return '#409EFF'
  return '#67C23A'
}

// 获取备份状态类型
const getBackupStatusType = (status) => {
  switch (status) {
    case 'completed': return 'success'
    case 'running': return 'warning'
    case 'failed': return 'danger'
    default: return 'info'
  }
}

// 获取备份状态文本
const getBackupStatusText = (status) => {
  switch (status) {
    case 'completed': return '完成'
    case 'running': return '运行中'
    case 'failed': return '失败'
    default: return '未知'
  }
}

// 获取配额状态类型
const getQuotaStatusType = (status) => {
  switch (status) {
    case 'normal': return 'success'
    case 'warning': return 'warning'
    case 'exceeded': return 'danger'
    default: return 'info'
  }
}

// 获取配额状态文本
const getQuotaStatusText = (status) => {
  switch (status) {
    case 'normal': return '正常'
    case 'warning': return '警告'
    case 'exceeded': return '超限'
    default: return '未知'
  }
}

// Mock数据加载函数
const loadStorageDevices = () => {
  // 直接使用已定义的mock数据
  console.log('已加载存储设备数据')
}

const loadStorageOverview = () => {
  // 直接使用已定义的mock数据
  console.log('已加载存储概览数据')
}

const loadStoragePerformance = () => {
  // 直接使用已定义的mock数据
  console.log('已加载存储性能数据')
}

const loadBackupSettings = () => {
  // 直接使用已定义的mock数据
  console.log('已加载备份设置')
}

const loadBackupHistory = () => {
  // 直接使用已定义的mock数据
  console.log('已加载备份历史')
}

const loadCleanupHistory = () => {
  // 直接使用已定义的mock数据
  console.log('已加载清理历史')
}

const loadQuotaSettings = () => {
  // 直接使用已定义的mock数据
  console.log('已加载配额设置')
}

const loadUserQuotas = () => {
  // 直接使用已定义的mock数据
  console.log('已加载用户配额')
}

// 刷新存储状态
const refreshStorageStatus = () => {
  // 模拟刷新状态
  storageStatus.type = 'success'
  storageStatus.title = '存储状态正常'
  storageStatus.description = '所有存储设备运行正常，存储空间充足'
  
  // 模拟更新存储性能数据
  storagePerformance.readSpeed = `${Math.floor(Math.random() * 50) + 200} MB/s`
  storagePerformance.writeSpeed = `${Math.floor(Math.random() * 50) + 150} MB/s`
  storagePerformance.iops = `${Math.floor(Math.random() * 300) + 1000}`
  storagePerformance.latency = `${(Math.random() * 2 + 1.5).toFixed(1)} ms`
  
  ElMessage.success('存储状态已刷新')
}

// 保存存储设置
const saveStorageSettings = () => {
  // 模拟保存操作
  ElMessage.success('存储设置保存成功')
}

// 添加存储设备
const addStorageDevice = () => {
  ElMessage.info('添加存储设备功能开发中')
}

// 切换设备状态
const toggleDevice = (device) => {
  // 直接更新本地数据
  const status = device.enabled ? '启用' : '禁用'
  ElMessage.success(`${device.name} 已${status}`)
}

// 编辑设备
const editDevice = (device) => {
  ElMessage.info(`编辑设备: ${device.name}`)
}

// 检查设备
const checkDevice = (device) => {
  // 模拟检查过程
  ElMessage.info(`正在检查设备: ${device.name}`)
  setTimeout(() => {
    ElMessage.success(`设备 ${device.name} 检查完成，状态正常`)
  }, 1500)
}

// 显示设备详情
const showDeviceDetails = (device) => {
  ElMessage.info(`显示设备详情: ${device.name}`)
}

// 格式化设备
const formatDevice = async (device) => {
  try {
    await ElMessageBox.confirm(
      `确定要格式化设备 ${device.name} 吗？此操作将删除所有数据且不可恢复！`,
      '危险操作',
      {
        confirmButtonText: '确定格式化',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    // 模拟格式化过程
    ElMessage.info('正在格式化设备...')
    setTimeout(() => {
      ElMessage.success(`设备 ${device.name} 格式化成功`)
    }, 2000)
  } catch (error) {
    // 用户取消操作
  }
}

// 挂载设备
const mountDevice = (device) => {
  device.mounted = true
  ElMessage.success(`设备 ${device.name} 挂载成功`)
}

// 卸载设备
const unmountDevice = (device) => {
  device.mounted = false
  ElMessage.success(`设备 ${device.name} 卸载成功`)
}

// 保存备份设置
const saveBackupSettings = () => {
  // 模拟保存操作
  ElMessage.success('备份设置保存成功')
}

// 创建手动备份
const createManualBackup = () => {
  // 模拟创建备份
  const newBackup = {
    id: Date.now(),
    name: `manual_backup_${new Date().toISOString().slice(0, 10).replace(/-/g, '')}`,
    type: 'manual',
    size: `${Math.floor(Math.random() * 50) + 150} MB`,
    createTime: new Date().toLocaleString(),
    status: 'running'
  }
  
  backupHistory.value.unshift(newBackup)
  ElMessage.success('手动备份任务已创建')
  
  // 模拟备份完成
  setTimeout(() => {
    newBackup.status = 'completed'
    ElMessage.success('备份任务完成')
  }, 3000)
}

// 恢复备份
const restoreBackup = async (backup) => {
  try {
    await ElMessageBox.confirm(
      `确定要恢复备份 ${backup.name} 吗？当前数据将被覆盖！`,
      '确认恢复',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    // 模拟恢复过程
    ElMessage.info('正在恢复备份...')
    setTimeout(() => {
      ElMessage.success(`备份 ${backup.name} 恢复成功`)
    }, 2000)
  } catch (error) {
    // 用户取消操作
  }
}

// 下载备份
const downloadBackup = (backup) => {
  // 模拟下载过程
  ElMessage.info(`正在下载备份: ${backup.name}`)
  setTimeout(() => {
    ElMessage.success('备份下载完成')
  }, 1500)
}

// 删除备份
const deleteBackup = async (backup) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除备份 ${backup.name} 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const index = backupHistory.value.findIndex(item => item.id === backup.id)
    if (index > -1) {
      backupHistory.value.splice(index, 1)
    }
    ElMessage.success(`备份 ${backup.name} 删除成功`)
  } catch (error) {
    // 用户取消操作
  }
}

// 分析可清理数据
const analyzeCleanup = () => {
  ElMessage.info('正在分析可清理数据...')
  setTimeout(() => {
    const sizes = ['1.5 GB', '2.3 GB', '3.2 GB', '4.1 GB', '5.7 GB']
    cleanupAnalysis.estimatedSize = sizes[Math.floor(Math.random() * sizes.length)]
    ElMessage.success(`分析完成，预计可清理 ${cleanupAnalysis.estimatedSize} 数据`)
  }, 1000)
}

// 开始清理
const startCleanup = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要开始清理吗？此操作不可恢复！',
      '确认清理',
      {
        confirmButtonText: '开始清理',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟清理过程
    ElMessage.info('清理任务已开始')
    
    // 添加清理记录
    const newCleanup = {
      id: Date.now(),
      type: cleanupSettings.type === 'temp' ? '临时文件' : 
            cleanupSettings.type === 'logs' ? '日志文件' : 
            cleanupSettings.type === 'cache' ? '缓存数据' : '其他',
      cleanedSize: cleanupAnalysis.estimatedSize,
      duration: `${Math.floor(Math.random() * 5) + 2}分钟`,
      startTime: new Date().toLocaleString(),
      status: 'completed'
    }
    
    cleanupHistory.value.unshift(newCleanup)
    cleanupAnalysis.estimatedSize = ''
    
    setTimeout(() => {
      ElMessage.success('清理任务完成')
    }, 2000)
  } catch (error) {
    // 用户取消操作
  }
}

// 保存自动清理设置
const saveAutoCleanupSettings = () => {
  // 模拟保存操作
  ElMessage.success('自动清理设置保存成功')
}

// 保存配额设置
const saveQuotaSettings = () => {
  // 模拟保存操作
  ElMessage.success('配额设置保存成功')
}

// 编辑用户配额
const editUserQuota = (user) => {
  ElMessage.info(`编辑用户 ${user.username} 的配额`)
}

// 保存告警设置
const saveAlertSettings = () => {
  // 模拟保存操作
  ElMessage.success('告警设置保存成功')
}

onMounted(() => {
  // 页面加载时的初始化操作
  console.log('存储管理页面已加载')
  
  // 加载所有mock数据
  loadStorageDevices()
  loadStorageOverview()
  loadStoragePerformance()
  loadBackupSettings()
  loadBackupHistory()
  loadCleanupHistory()
  loadQuotaSettings()
  loadUserQuotas()
})
</script>

<style scoped>
.storage-settings-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.storage-status-alert {
  margin-bottom: 20px;
}

.status-actions {
  margin-left: auto;
}

.storage-tabs {
  margin-bottom: 20px;
}

.storage-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left .el-icon {
  margin-right: 8px;
}

.device-list {
  padding: 0;
}

.device-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  transition: box-shadow 0.3s;
}

.device-item:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.device-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.device-title {
  font-weight: 600;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.device-switch {
  margin-left: 10px;
}

.device-badges {
  margin-bottom: 15px;
}

.device-badges .el-tag {
  margin-right: 8px;
}

.device-details {
  margin-bottom: 15px;
}

.detail-item {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.detail-label {
  font-weight: 500;
  color: #606266;
  margin-right: 10px;
  min-width: 80px;
}

.detail-value {
  color: #303133;
}

.usage-progress {
  display: flex;
  align-items: center;
  width: 100%;
}

.usage-progress .el-progress {
  flex: 1;
  margin-right: 10px;
}

.usage-text {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.device-actions {
  display: flex;
  gap: 8px;
}

.storage-overview,
.storage-performance,
.backup-status,
.quota-overview {
  padding: 10px 0;
}

.overview-item,
.performance-item,
.status-item,
.quota-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.overview-label,
.performance-label,
.status-label,
.quota-label {
  font-weight: 500;
  color: #606266;
}

.overview-value,
.performance-value,
.status-value,
.quota-value {
  color: #303133;
  font-weight: 600;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.backup-config,
.quota-config,
.auto-cleanup-config {
  padding-left: 20px;
  border-left: 2px solid #e4e7ed;
  margin: 15px 0;
}

.unit {
  margin-left: 8px;
  color: #909399;
}
</style> 