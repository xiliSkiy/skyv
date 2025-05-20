<template>
  <div class="settings-container">
    <div class="page-header">
      <h4>系统设置</h4>
    </div>
    
    <div class="settings-grid">
      <!-- 系统配置卡片 -->
      <el-card class="settings-card" shadow="hover">
        <div class="settings-card-content">
          <div class="settings-icon">
            <el-icon :size="40" color="#1e3c72"><Setting /></el-icon>
          </div>
          <h5 class="settings-title">系统配置</h5>
          <p class="settings-description">基本系统参数配置，包括系统名称、Logo、界面主题等全局设置。</p>
          <el-button type="primary" @click="navigateTo('SystemSettings')">
            <el-icon><Tools /></el-icon> 管理设置
          </el-button>
        </div>
      </el-card>
      
      <!-- 网络配置卡片 -->
      <el-card class="settings-card" shadow="hover">
        <div class="settings-card-content">
          <div class="settings-icon">
            <el-icon :size="40" color="#1e3c72"><Connection /></el-icon>
          </div>
          <h5 class="settings-title">网络配置</h5>
          <p class="settings-description">设置网络参数，包括IP地址配置、端口设置、代理服务器等。</p>
          <el-button type="primary" @click="navigateTo('NetworkSettings')">
            <el-icon><Tools /></el-icon> 管理设置
          </el-button>
        </div>
      </el-card>
      
      <!-- 存储管理卡片 -->
      <el-card class="settings-card" shadow="hover">
        <div class="settings-card-content">
          <div class="settings-icon">
            <el-icon :size="40" color="#1e3c72"><DataLine /></el-icon>
          </div>
          <h5 class="settings-title">存储管理</h5>
          <p class="settings-description">配置视频存储策略、存储空间管理、云存储配置等。</p>
          <el-button type="primary" @click="navigateTo('StorageSettings')">
            <el-icon><Tools /></el-icon> 管理设置
          </el-button>
        </div>
      </el-card>
      
      <!-- 报警规则卡片 -->
      <el-card class="settings-card" shadow="hover">
        <div class="settings-card-content">
          <div class="settings-icon">
            <el-icon :size="40" color="#1e3c72"><AlarmClock /></el-icon>
          </div>
          <h5 class="settings-title">报警规则</h5>
          <p class="settings-description">设置报警触发条件、报警级别、通知方式等警报相关配置。</p>
          <el-button type="primary" @click="navigateTo('AlertSettings')">
            <el-icon><Tools /></el-icon> 管理设置
          </el-button>
        </div>
      </el-card>
      
      <!-- 日志管理卡片 -->
      <el-card class="settings-card" shadow="hover">
        <div class="settings-card-content">
          <div class="settings-icon">
            <el-icon :size="40" color="#1e3c72"><Document /></el-icon>
          </div>
          <h5 class="settings-title">日志管理</h5>
          <p class="settings-description">配置系统日志记录策略、日志级别、保留期限等日志相关设置。</p>
          <el-button type="primary" @click="navigateTo('LogSettings')">
            <el-icon><Tools /></el-icon> 管理设置
          </el-button>
        </div>
      </el-card>
      
      <!-- 备份恢复卡片 -->
      <el-card class="settings-card" shadow="hover">
        <div class="settings-card-content">
          <div class="settings-icon">
            <el-icon :size="40" color="#1e3c72"><RefreshRight /></el-icon>
          </div>
          <h5 class="settings-title">备份与恢复</h5>
          <p class="settings-description">系统配置备份、数据备份、系统恢复与重置等功能设置。</p>
          <el-button type="primary" @click="navigateTo('BackupSettings')">
            <el-icon><Tools /></el-icon> 管理设置
          </el-button>
        </div>
      </el-card>
      
      <!-- 安全设置卡片 -->
      <el-card class="settings-card" shadow="hover">
        <div class="settings-card-content">
          <div class="settings-icon">
            <el-icon :size="40" color="#1e3c72"><Lock /></el-icon>
          </div>
          <h5 class="settings-title">安全设置</h5>
          <p class="settings-description">密码策略、访问控制、安全审计、加密设置等安全相关配置。</p>
          <el-button type="primary" @click="navigateTo('SecuritySettings')">
            <el-icon><Tools /></el-icon> 管理设置
          </el-button>
        </div>
      </el-card>
      
      <!-- 通知设置卡片 -->
      <el-card class="settings-card" shadow="hover">
        <div class="settings-card-content">
          <div class="settings-icon">
            <el-icon :size="40" color="#1e3c72"><Message /></el-icon>
          </div>
          <h5 class="settings-title">通知设置</h5>
          <p class="settings-description">邮件通知、短信通知、APP推送、微信通知等通知渠道配置。</p>
          <el-button type="primary" @click="navigateTo('NotificationSettings')">
            <el-icon><Tools /></el-icon> 管理设置
          </el-button>
        </div>
      </el-card>
      
      <!-- 智能分析卡片 -->
      <el-card class="settings-card" shadow="hover">
        <div class="settings-card-content">
          <div class="settings-icon">
            <el-icon :size="40" color="#1e3c72"><Cpu /></el-icon>
          </div>
          <h5 class="settings-title">智能分析</h5>
          <p class="settings-description">人脸识别、行为分析、目标检测等AI算法相关配置设置。</p>
          <el-button type="primary" @click="navigateTo('AISettings')">
            <el-icon><Tools /></el-icon> 管理设置
          </el-button>
        </div>
      </el-card>
      <!-- 指标配置卡片 -->
      <el-card class="settings-card" shadow="hover">
        <div class="settings-card-content">
          <div class="settings-icon">
            <el-icon :size="40" color="#1e3c72"><Message /></el-icon>
          </div>
          <h5 class="settings-title">指标配置</h5>
          <p class="settings-description">管理监控指标、采集器、指标模板和采集历史记录。</p>
          <el-button type="primary" @click="navigateTo('SettingsMetrics')">
            <el-icon><Tools /></el-icon> 管理设置
          </el-button>
        </div>
      </el-card>
    </div>
    
    <!-- 系统信息 -->
    <el-card class="system-info-card">
      <template #header>
        <div class="card-header">
          <span>系统信息</span>
        </div>
      </template>
      <div class="system-info-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">系统版本：</span>
              <span>SkyEye v2.5.3</span>
            </div>
            <div class="info-item">
              <span class="info-label">操作系统：</span>
              <span>Linux CentOS 8.4</span>
            </div>
            <div class="info-item">
              <span class="info-label">CPU使用率：</span>
              <div class="progress-container">
                <el-progress :percentage="25" :stroke-width="8" color="#67C23A"></el-progress>
              </div>
            </div>
            <div class="info-item">
              <span class="info-label">内存使用率：</span>
              <div class="progress-container">
                <el-progress :percentage="60" :stroke-width="8" color="#409EFF"></el-progress>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">存储空间：</span>
              <div class="progress-container">
                <el-progress :percentage="75" :stroke-width="8" color="#E6A23C"></el-progress>
                <div class="small text-muted">已使用: 3.75 TB / 总量: 5 TB</div>
              </div>
            </div>
            <div class="info-item">
              <span class="info-label">数据库连接：</span>
              <el-tag type="success" size="small">正常</el-tag>
            </div>
            <div class="info-item">
              <span class="info-label">上次更新时间：</span>
              <span>2023-11-15 08:30:00</span>
            </div>
            <div class="info-item">
              <span class="info-label">运行时间：</span>
              <span>10天 05小时 22分钟</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateTo = (route) => {
  router.push({ name: route })
}
</script>

<style scoped>
.settings-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.settings-card {
  height: 100%;
  transition: transform 0.3s, box-shadow 0.3s;
}

.settings-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
}

.settings-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20px;
}

.settings-icon {
  margin-bottom: 20px;
}

.settings-title {
  font-weight: 600;
  margin-bottom: 10px;
  font-size: 18px;
}

.settings-description {
  color: #6c757d;
  margin-bottom: 20px;
  flex-grow: 1;
}

.system-info-card {
  margin-top: 20px;
}

.system-info-content {
  padding: 10px;
}

.info-item {
  margin-bottom: 15px;
}

.info-label {
  font-weight: bold;
  display: inline-block;
  width: 120px;
}

.progress-container {
  margin-top: 5px;
}

.text-muted {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

@media (max-width: 1200px) {
  .settings-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style> 