<template>
  <div class="settings-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">
            <el-icon class="title-icon"><Setting /></el-icon>
            系统设置
          </h2>
          <p class="page-subtitle">管理和配置系统的各项参数</p>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <div class="stat-value">{{ settingsStats.totalModules }}</div>
            <div class="stat-label">配置模块</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ settingsStats.activeModules }}</div>
            <div class="stat-label">已启用</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 设置分类卡片 -->
    <div class="settings-sections">
      <!-- 基础配置区域 -->
      <div class="section-group">
        <div class="section-header">
          <h3>基础配置</h3>
          <span class="section-desc">系统核心参数设置</span>
        </div>
        <div class="settings-grid">
          <!-- 系统配置卡片 -->
          <div class="settings-card basic-config" @click="navigateTo('SystemSettings')">
            <div class="card-background">
              <div class="gradient-bg system-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon system-icon">
                  <el-icon><Setting /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">系统配置</h4>
                <p class="settings-description">基本系统参数、界面主题、语言设置</p>
                <div class="settings-features">
                  <span class="feature-tag">系统名称</span>
                  <span class="feature-tag">主题配置</span>
                  <span class="feature-tag">时区设置</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  配置管理
                </el-button>
              </div>
            </div>
          </div>

          <!-- 网络配置卡片 -->
          <div class="settings-card network-config" @click="navigateTo('NetworkSettings')">
            <div class="card-background">
              <div class="gradient-bg network-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon network-icon">
                  <el-icon><Connection /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">网络配置</h4>
                <p class="settings-description">IP地址、端口、代理服务器设置</p>
                <div class="settings-features">
                  <span class="feature-tag">网络诊断</span>
                  <span class="feature-tag">带宽管理</span>
                  <span class="feature-tag">防火墙</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  网络管理
                </el-button>
              </div>
            </div>
          </div>

          <!-- 存储管理卡片 -->
          <div class="settings-card storage-config" @click="navigateTo('StorageSettings')">
            <div class="card-background">
              <div class="gradient-bg storage-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon storage-icon">
                  <el-icon><FolderOpened /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">存储管理</h4>
                <p class="settings-description">存储策略、空间管理、云存储配置</p>
                <div class="settings-features">
                  <span class="feature-tag">存储池</span>
                  <span class="feature-tag">自动清理</span>
                  <span class="feature-tag">数据冗余</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  存储管理
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 安全与监控区域 -->
      <div class="section-group">
        <div class="section-header">
          <h3>安全与监控</h3>
          <span class="section-desc">安全策略和监控配置</span>
        </div>
        <div class="settings-grid">
          <!-- 安全设置卡片 -->
          <div class="settings-card security-config" @click="navigateTo('SecuritySettings')">
            <div class="card-background">
              <div class="gradient-bg security-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon security-icon">
                  <el-icon><Lock /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">安全设置</h4>
                <p class="settings-description">密码策略、访问控制、安全审计</p>
                <div class="settings-features">
                  <span class="feature-tag">多因素认证</span>
                  <span class="feature-tag">权限控制</span>
                  <span class="feature-tag">安全审计</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  安全管理
                </el-button>
              </div>
            </div>
          </div>

          <!-- 报警规则卡片 -->
          <div class="settings-card alert-config" @click="navigateTo('AlertSettings')">
            <div class="card-background">
              <div class="gradient-bg alert-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon alert-icon">
                  <el-icon><Bell /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">智能报警</h4>
                <p class="settings-description">报警规则、通知策略、升级机制</p>
                <div class="settings-features">
                  <span class="feature-tag">智能分析</span>
                  <span class="feature-tag">多级报警</span>
                  <span class="feature-tag">自动处理</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  报警管理
                </el-button>
              </div>
            </div>
          </div>

          <!-- 指标配置卡片 -->
          <div class="settings-card metrics-config" @click="navigateTo('SettingsMetrics')">
            <div class="card-background">
              <div class="gradient-bg metrics-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon metrics-icon">
                  <el-icon><Histogram /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">指标配置</h4>
                <p class="settings-description">监控指标、采集器、模板管理</p>
                <div class="settings-features">
                  <span class="feature-tag">自定义指标</span>
                  <span class="feature-tag">采集策略</span>
                  <span class="feature-tag">数据分析</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  指标管理
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 高级功能区域 -->
      <div class="section-group">
        <div class="section-header">
          <h3>高级功能</h3>
          <span class="section-desc">扩展功能和集成服务</span>
        </div>
        <div class="settings-grid">
          <!-- 智能分析卡片 -->
          <div class="settings-card ai-config" @click="navigateTo('AISettings')">
            <div class="card-background">
              <div class="gradient-bg ai-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon ai-icon">
                  <el-icon><Document /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">智能分析</h4>
                <p class="settings-description">AI算法、人脸识别、行为分析</p>
                <div class="settings-features">
                  <span class="feature-tag">深度学习</span>
                  <span class="feature-tag">实时分析</span>
                  <span class="feature-tag">模型训练</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  AI管理
                </el-button>
              </div>
            </div>
          </div>

          <!-- 通知设置卡片 -->
          <div class="settings-card notification-config" @click="navigateTo('NotificationSettings')">
            <div class="card-background">
              <div class="gradient-bg notification-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon notification-icon">
                  <el-icon><Message /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">通知设置</h4>
                <p class="settings-description">邮件、短信、APP推送、微信通知</p>
                <div class="settings-features">
                  <span class="feature-tag">多渠道</span>
                  <span class="feature-tag">模板管理</span>
                  <span class="feature-tag">发送统计</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  通知管理
                </el-button>
              </div>
            </div>
          </div>

          <!-- 采集器管理卡片 -->
          <div class="settings-card collector-config" @click="navigateTo('CollectorManagement')">
            <div class="card-background">
              <div class="gradient-bg collector-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon collector-icon">
                  <el-icon><Cpu /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">采集器管理</h4>
                <p class="settings-description">采集器配置、状态监控、性能优化</p>
                <div class="settings-features">
                  <span class="feature-tag">分布式采集</span>
                  <span class="feature-tag">负载均衡</span>
                  <span class="feature-tag">故障转移</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  采集管理
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 系统维护区域 -->
      <div class="section-group">
        <div class="section-header">
          <h3>系统维护</h3>
          <span class="section-desc">日志、备份和维护工具</span>
        </div>
        <div class="settings-grid">
          <!-- 日志管理卡片 -->
          <div class="settings-card log-config" @click="navigateTo('LogSettings')">
            <div class="card-background">
              <div class="gradient-bg log-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon log-icon">
                  <el-icon><Document /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">日志管理</h4>
                <p class="settings-description">日志级别、保留期限、分析工具</p>
                <div class="settings-features">
                  <span class="feature-tag">实时日志</span>
                  <span class="feature-tag">日志分析</span>
                  <span class="feature-tag">告警集成</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  日志管理
                </el-button>
              </div>
            </div>
          </div>

          <!-- 备份恢复卡片 -->
          <div class="settings-card backup-config" @click="navigateTo('BackupSettings')">
            <div class="card-background">
              <div class="gradient-bg backup-gradient"></div>
            </div>
            <div class="settings-card-content">
              <div class="settings-icon-wrapper">
                <div class="settings-icon backup-icon">
                  <el-icon><RefreshRight /></el-icon>
                </div>
              </div>
              <div class="card-info">
                <h4 class="settings-title">备份恢复</h4>
                <p class="settings-description">数据备份、系统恢复、重置功能</p>
                <div class="settings-features">
                  <span class="feature-tag">增量备份</span>
                  <span class="feature-tag">快速恢复</span>
                  <span class="feature-tag">数据校验</span>
                </div>
              </div>
              <div class="card-action">
                <el-button type="primary" size="small" round>
                  <el-icon><Tools /></el-icon>
                  备份管理
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 系统状态总览 -->
    <div class="system-overview">
      <el-card class="system-info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <el-icon class="header-icon"><Monitor /></el-icon>
              <span class="header-title">系统状态总览</span>
            </div>
            <div class="header-right">
              <el-tag type="success" size="small">
                <el-icon><CircleCheck /></el-icon>
                系统正常
              </el-tag>
            </div>
          </div>
        </template>
        
        <div class="system-info-content">
          <el-row :gutter="24">
            <!-- 系统信息 -->
            <el-col :xs="24" :sm="12" :lg="6">
              <div class="info-section">
                <h4 class="section-title">
                  <el-icon><Platform /></el-icon>
                  系统信息
                </h4>
                <div class="info-items">
                  <div class="info-item">
                    <span class="info-label">系统版本</span>
                    <span class="info-value">SkyEye v2.5.3</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">操作系统</span>
                    <span class="info-value">Linux CentOS 8.4</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">运行时间</span>
                    <span class="info-value">10天 05小时 22分钟</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">最后更新</span>
                    <span class="info-value">2023-11-15 08:30</span>
                  </div>
                </div>
              </div>
            </el-col>

            <!-- 性能指标 -->
            <el-col :xs="24" :sm="12" :lg="6">
              <div class="info-section">
                <h4 class="section-title">
                  <el-icon><Odometer /></el-icon>
                  性能指标
                </h4>
                <div class="performance-metrics">
                  <div class="metric-item">
                    <div class="metric-header">
                      <span class="metric-label">CPU使用率</span>
                      <span class="metric-value">25%</span>
                    </div>
                    <el-progress :percentage="25" :stroke-width="6" color="#67C23A" :show-text="false" />
                  </div>
                  <div class="metric-item">
                    <div class="metric-header">
                      <span class="metric-label">内存使用率</span>
                      <span class="metric-value">60%</span>
                    </div>
                    <el-progress :percentage="60" :stroke-width="6" color="#409EFF" :show-text="false" />
                  </div>
                  <div class="metric-item">
                    <div class="metric-header">
                      <span class="metric-label">存储空间</span>
                      <span class="metric-value">75%</span>
                    </div>
                    <el-progress :percentage="75" :stroke-width="6" color="#E6A23C" :show-text="false" />
                    <div class="metric-detail">已使用: 3.75 TB / 总量: 5 TB</div>
                  </div>
                </div>
              </div>
            </el-col>

            <!-- 服务状态 -->
            <el-col :xs="24" :sm="12" :lg="6">
              <div class="info-section">
                <h4 class="section-title">
                  <el-icon><Connection /></el-icon>
                  服务状态
                </h4>
                <div class="service-status">
                  <div class="status-item">
                    <span class="status-label">数据库连接</span>
                    <el-tag type="success" size="small">正常</el-tag>
                  </div>
                  <div class="status-item">
                    <span class="status-label">视频流服务</span>
                    <el-tag type="success" size="small">正常</el-tag>
                  </div>
                  <div class="status-item">
                    <span class="status-label">AI分析服务</span>
                    <el-tag type="success" size="small">正常</el-tag>
                  </div>
                  <div class="status-item">
                    <span class="status-label">报警服务</span>
                    <el-tag type="warning" size="small">维护中</el-tag>
                  </div>
                </div>
              </div>
            </el-col>

            <!-- 快速操作 -->
            <el-col :xs="24" :sm="12" :lg="6">
              <div class="info-section">
                <h4 class="section-title">
                  <el-icon><Operation /></el-icon>
                  快速操作
                </h4>
                <div class="quick-actions">
                  <el-button size="small" @click="handleSystemRestart">
                    <el-icon><RefreshRight /></el-icon>
                    重启服务
                  </el-button>
                  <el-button size="small" @click="handleSystemBackup">
                    <el-icon><Download /></el-icon>
                    立即备份
                  </el-button>
                  <el-button size="small" @click="handleSystemOptimize">
                    <el-icon><MagicStick /></el-icon>
                    系统优化
                  </el-button>
                  <el-button size="small" @click="handleSystemDiagnose">
                    <el-icon><View /></el-icon>
                    系统诊断
                  </el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, Tools, Connection, FolderOpened, Lock, Bell, Histogram,
  Document, Message, Cpu, RefreshRight, Monitor,
  CircleCheck, Platform, Odometer, Operation, Download, MagicStick, View
} from '@element-plus/icons-vue'

const router = useRouter()

// 设置统计数据
const settingsStats = reactive({
  totalModules: 11,
  activeModules: 10
})

const navigateTo = (route) => {
  router.push({ name: route })
}

// 快速操作处理函数
const handleSystemRestart = async () => {
  try {
    await ElMessageBox.confirm('确定要重启系统服务吗？', '确认重启', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('系统服务重启成功')
  } catch {
    // 用户取消
  }
}

const handleSystemBackup = () => {
  ElMessage.success('备份任务已启动')
}

const handleSystemOptimize = () => {
  ElMessage.success('系统优化已开始')
}

const handleSystemDiagnose = () => {
  ElMessage.info('系统诊断中...')
}
</script>

<style lang="scss" scoped>
.settings-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 32px;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    padding: 24px 32px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.15);
    
    .header-left {
      .page-title {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 28px;
        font-weight: 700;
        color: white;
        margin: 0 0 8px 0;
        
        .title-icon {
          color: #ffd700;
          filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
        }
      }
      
      .page-subtitle {
        color: rgba(255, 255, 255, 0.8);
        margin: 0;
        font-size: 16px;
      }
    }
    
    .header-stats {
      display: flex;
      gap: 24px;
      
      .stat-item {
        text-align: center;
        color: white;
        
        .stat-value {
          font-size: 24px;
          font-weight: 700;
          line-height: 1;
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 12px;
          opacity: 0.8;
        }
      }
    }
  }
}

.settings-sections {
  .section-group {
    margin-bottom: 32px;
    
    .section-header {
      margin-bottom: 20px;
      padding: 16px 0;
      border-bottom: 2px solid #e4e7ed;
      
      h3 {
        color: #2c3e50;
        font-size: 20px;
        font-weight: 600;
        margin: 0 0 4px 0;
      }
      
      .section-desc {
        color: #7f8c8d;
        font-size: 14px;
      }
    }
  }
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: 20px;
}

.settings-card {
  position: relative;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
    border-color: #409eff;
    
    .gradient-bg {
      opacity: 0.8;
    }
    
    .settings-icon {
      transform: scale(1.1) rotate(5deg);
    }
  }
  
  .card-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 120px;
    overflow: hidden;
    
    .gradient-bg {
      width: 100%;
      height: 100%;
      opacity: 0.6;
      transition: opacity 0.3s;
    }
  }

  .settings-card-content {
    position: relative;
    padding: 24px;
    display: flex;
    flex-direction: column;
    height: 280px;
  }
  
  .settings-icon-wrapper {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
    margin-top: 20px;
  }

  .settings-icon {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 28px;
    transition: transform 0.3s;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  .card-info {
    flex: 1;
    text-align: center;

    .settings-title {
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;
      margin: 0 0 8px 0;
    }

    .settings-description {
      color: #7f8c8d;
      font-size: 14px;
      line-height: 1.5;
      margin-bottom: 16px;
    }
    
    .settings-features {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      justify-content: center;
      
      .feature-tag {
        background: rgba(64, 158, 255, 0.1);
        color: #409eff;
        font-size: 11px;
        padding: 2px 8px;
        border-radius: 12px;
        border: 1px solid rgba(64, 158, 255, 0.3);
      }
    }
  }
  
  .card-action {
    display: flex;
    justify-content: center;
  }
}

// 渐变背景样式
.system-gradient { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.network-gradient { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.storage-gradient { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.security-gradient { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.alert-gradient { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.metrics-gradient { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }
.ai-gradient { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%); }
.notification-gradient { background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); }
.collector-gradient { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }
.log-gradient { background: linear-gradient(135deg, #fad0c4 0%, #ffd1ff 100%); }
.backup-gradient { background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%); }

// 图标背景色
.system-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.network-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.storage-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.security-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.alert-icon { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.metrics-icon { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }
.ai-icon { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%); }
.notification-icon { background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); }
.collector-icon { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }
.log-icon { background: linear-gradient(135deg, #fad0c4 0%, #ffd1ff 100%); }
.backup-icon { background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%); }

.system-overview {
  margin-top: 32px;

  .system-info-card {
    border-radius: 12px;
    background: white;
    border: 1px solid #e4e7ed;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .header-icon {
          color: #409eff;
        }
        
        .header-title {
          font-size: 16px;
          font-weight: 600;
          color: #2c3e50;
        }
      }
    }

    .system-info-content {
      .info-section {
        .section-title {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          font-weight: 600;
          color: #2c3e50;
          margin: 0 0 16px 0;
          
          .el-icon {
            color: #409eff;
          }
        }
        
        .info-items {
          .info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;
            border-bottom: 1px solid #f0f2f5;
            
            &:last-child {
              border-bottom: none;
            }

            .info-label {
              color: #7f8c8d;
              font-size: 13px;
            }
            
            .info-value {
              color: #2c3e50;
              font-size: 13px;
              font-weight: 500;
            }
          }
        }
        
        .performance-metrics {
          .metric-item {
            margin-bottom: 16px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            .metric-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 8px;
              
              .metric-label {
                color: #7f8c8d;
                font-size: 13px;
              }
              
              .metric-value {
                color: #2c3e50;
                font-size: 13px;
                font-weight: 600;
              }
            }
            
            .metric-detail {
              color: #bdc3c7;
              font-size: 11px;
              margin-top: 4px;
            }
          }
        }
        
        .service-status {
          .status-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;
            border-bottom: 1px solid #f0f2f5;
            
            &:last-child {
              border-bottom: none;
            }
            
            .status-label {
              color: #7f8c8d;
              font-size: 13px;
            }
          }
        }
        
        .quick-actions {
          display: flex;
          flex-direction: column;
          gap: 8px;
          
          .el-button {
            justify-content: flex-start;
          }
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .settings-grid {
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  }
}

@media (max-width: 768px) {
  .settings-container {
    padding: 12px;
  }
  
  .page-header .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: center;
    text-align: center;
    padding: 20px;
  }
  
  .settings-grid {
    grid-template-columns: 1fr;
  }
  
  .settings-card {
    .settings-card-content {
      height: auto;
      min-height: 280px;
    }
  }
}
</style> 