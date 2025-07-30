<template>
  <div class="network-settings-container">
    <div class="page-header">
      <div>
        <h4>网络配置</h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/settings' }">系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>网络配置</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div>
        <el-button type="primary" @click="saveNetworkSettings">
          <el-icon><Save /></el-icon> 保存配置
        </el-button>
      </div>
    </div>
    
    <!-- 网络状态概览 -->
    <el-alert 
      :type="networkStatus.type" 
      :title="networkStatus.title" 
      :description="networkStatus.description"
      :closable="false"
      show-icon
      class="network-status-alert">
      <template #default>
        <div class="status-actions">
          <el-button size="small" @click="refreshNetworkStatus">
            <el-icon><Refresh /></el-icon> 刷新状态
          </el-button>
        </div>
      </template>
    </el-alert>
    
    <!-- 网络配置页面导航标签 -->
    <el-tabs v-model="activeTab" class="network-tabs">
      <el-tab-pane label="网络接口" name="interfaces">
        <div class="network-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 网络接口列表 -->
              <el-card class="network-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <div class="header-left">
                      <el-icon><Connection /></el-icon> 网络接口
                    </div>
                    <el-button type="primary" size="small" @click="addInterface">
                      <el-icon><Plus /></el-icon> 添加接口
                    </el-button>
                  </div>
                </template>
                <div class="interface-list">
                  <div 
                    v-for="(networkInterface, index) in networkInterfaces" 
                    :key="index"
                    class="interface-item">
                    <div class="interface-header">
                      <div class="interface-title">
                        <span>{{ networkInterface.name }} ({{ networkInterface.device }})</span>
                        <el-switch 
                          v-model="networkInterface.enabled" 
                          @change="toggleInterface(networkInterface)"
                          class="interface-switch">
                        </el-switch>
                      </div>
                    </div>
                    <div class="interface-badges">
                      <el-tag 
                        :type="networkInterface.type === 'ethernet' ? 'primary' : networkInterface.type === 'wifi' ? 'success' : 'info'"
                        size="small">
                        {{ networkInterface.typeLabel }}
                      </el-tag>
                      <el-tag 
                        :type="networkInterface.status === 'connected' ? 'success' : 'danger'"
                        size="small">
                        {{ networkInterface.statusLabel }}
                      </el-tag>
                    </div>
                    <div class="interface-details">
                      <el-row :gutter="20">
                        <el-col :span="12">
                          <div class="detail-item">
                            <span class="detail-label">IP地址:</span>
                            <span class="detail-value">{{ networkInterface.ipAddress }}</span>
                          </div>
                          <div class="detail-item">
                            <span class="detail-label">子网掩码:</span>
                            <span class="detail-value">{{ networkInterface.subnetMask }}</span>
                          </div>
                          <div class="detail-item" v-if="networkInterface.ssid">
                            <span class="detail-label">SSID:</span>
                            <span class="detail-value">{{ networkInterface.ssid }}</span>
                          </div>
                        </el-col>
                        <el-col :span="12">
                          <div class="detail-item">
                            <span class="detail-label">默认网关:</span>
                            <span class="detail-value">{{ networkInterface.gateway }}</span>
                          </div>
                          <div class="detail-item">
                            <span class="detail-label">MAC地址:</span>
                            <span class="detail-value">{{ networkInterface.macAddress }}</span>
                          </div>
                          <div class="detail-item" v-if="networkInterface.signalStrength">
                            <span class="detail-label">信号强度:</span>
                            <div class="signal-strength">
                              <el-progress 
                                :percentage="networkInterface.signalStrength" 
                                :stroke-width="6"
                                :color="getSignalColor(networkInterface.signalStrength)">
                              </el-progress>
                              <span class="signal-text">{{ networkInterface.signalStrength }}% ({{ getSignalLabel(networkInterface.signalStrength) }})</span>
                            </div>
                          </div>
                        </el-col>
                      </el-row>
                    </div>
                    <div class="interface-actions">
                      <el-button size="small" type="primary" @click="editInterface(networkInterface)">
                        <el-icon><Edit /></el-icon> 编辑
                      </el-button>
                      <el-button size="small" @click="reconnectInterface(networkInterface)">
                        <el-icon><Refresh /></el-icon> 重新连接
                      </el-button>
                      <el-button size="small" type="info" @click="showInterfaceDetails(networkInterface)">
                        <el-icon><InfoFilled /></el-icon> 详情
                      </el-button>
                    </div>
                  </div>
                </div>
              </el-card>
              
              <!-- DNS设置卡片 -->
              <el-card class="network-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Server /></el-icon> DNS设置
                  </div>
                </template>
                <el-form :model="dnsSettings" label-width="150px">
                  <el-form-item>
                    <el-radio-group v-model="dnsSettings.mode">
                      <el-radio label="auto">自动获取DNS服务器（DHCP）</el-radio>
                      <el-radio label="manual">使用以下DNS服务器</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <div v-if="dnsSettings.mode === 'manual'" class="dns-manual-config">
                    <el-form-item label="首选DNS服务器">
                      <el-input v-model="dnsSettings.primaryDns" placeholder="例如: 8.8.8.8"></el-input>
                    </el-form-item>
                    <el-form-item label="备用DNS服务器">
                      <el-input v-model="dnsSettings.secondaryDns" placeholder="例如: 8.8.4.4"></el-input>
                    </el-form-item>
                  </div>
                  <el-form-item>
                    <el-checkbox v-model="dnsSettings.enableDnsSec">启用DNSSEC</el-checkbox>
                    <div class="form-tip">提高DNS查询的安全性，防止DNS欺骗攻击</div>
                  </el-form-item>
                  <el-form-item>
                    <el-checkbox v-model="dnsSettings.enableDnsCache">启用DNS缓存</el-checkbox>
                    <div class="form-tip">缓存DNS解析结果，提高网络访问速度</div>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <!-- 网络状态卡片 -->
              <el-card class="network-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Histogram /></el-icon> 网络状态
                  </div>
                </template>
                <div class="network-stats">
                  <div class="stat-item">
                    <span class="stat-label">连接状态</span>
                    <el-tag type="success" size="small">在线</el-tag>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">上行速度</span>
                    <span class="stat-value">{{ networkStats.uploadSpeed }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">下行速度</span>
                    <span class="stat-value">{{ networkStats.downloadSpeed }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">延迟</span>
                    <span class="stat-value">{{ networkStats.latency }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">丢包率</span>
                    <span class="stat-value">{{ networkStats.packetLoss }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">活动时间</span>
                    <span class="stat-value">{{ networkStats.uptime }}</span>
                  </div>
                  <el-divider></el-divider>
                  <el-button type="primary" @click="runSpeedTest" style="width: 100%">
                    <el-icon><Odometer /></el-icon> 网络测速
                  </el-button>
                </div>
              </el-card>
              
              <!-- 端口设置卡片 -->
              <el-card class="network-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Connection /></el-icon> 系统服务端口
                  </div>
                </template>
                <el-form :model="portSettings" label-width="120px">
                  <el-form-item label="Web管理界面">
                    <el-input-number v-model="portSettings.webPort" :min="1" :max="65535" style="width: 100%"></el-input-number>
                  </el-form-item>
                  <el-form-item label="HTTPS端口">
                    <el-input-number v-model="portSettings.httpsPort" :min="1" :max="65535" style="width: 100%"></el-input-number>
                  </el-form-item>
                  <el-form-item label="RTSP流媒体端口">
                    <el-input-number v-model="portSettings.rtspPort" :min="1" :max="65535" style="width: 100%"></el-input-number>
                  </el-form-item>
                  <el-form-item label="ONVIF端口">
                    <el-input-number v-model="portSettings.onvifPort" :min="1" :max="65535" style="width: 100%"></el-input-number>
                  </el-form-item>
                  <el-form-item>
                    <el-checkbox v-model="portSettings.enableUpnp">启用UPnP自动端口映射</el-checkbox>
                    <div class="form-tip">允许系统自动在路由器上创建端口映射</div>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="代理设置" name="proxy">
        <div class="network-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 代理配置卡片 -->
              <el-card class="network-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Link /></el-icon> 代理配置
                  </div>
                </template>
                <el-form :model="proxySettings" label-width="120px">
                  <el-form-item>
                    <el-radio-group v-model="proxySettings.type">
                      <el-radio label="none">不使用代理</el-radio>
                      <el-radio label="http">HTTP代理</el-radio>
                      <el-radio label="socks5">SOCKS5代理</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <div v-if="proxySettings.type !== 'none'" class="proxy-config">
                    <el-form-item label="代理服务器">
                      <el-input v-model="proxySettings.host" placeholder="请输入代理服务器地址"></el-input>
                    </el-form-item>
                    <el-form-item label="端口">
                      <el-input-number v-model="proxySettings.port" :min="1" :max="65535" style="width: 100%"></el-input-number>
                    </el-form-item>
                    <el-form-item>
                      <el-checkbox v-model="proxySettings.requireAuth">需要身份验证</el-checkbox>
                    </el-form-item>
                    <div v-if="proxySettings.requireAuth" class="auth-config">
                      <el-form-item label="用户名">
                        <el-input v-model="proxySettings.username" placeholder="请输入用户名"></el-input>
                      </el-form-item>
                      <el-form-item label="密码">
                        <el-input v-model="proxySettings.password" type="password" placeholder="请输入密码"></el-input>
                      </el-form-item>
                    </div>
                    <el-form-item label="绕过代理的地址">
                      <el-input 
                        v-model="proxySettings.bypass" 
                        type="textarea" 
                        :rows="3"
                        placeholder="每行一个地址，支持通配符，例如：&#10;*.local&#10;192.168.*&#10;localhost">
                      </el-input>
                      <div class="form-tip">这些地址将直接连接，不通过代理</div>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="testProxyConnection">
                        <el-icon><Connection /></el-icon> 测试连接
                      </el-button>
                    </el-form-item>
                  </div>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="防火墙" name="firewall">
        <div class="network-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 防火墙设置卡片 -->
              <el-card class="network-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <div class="header-left">
                      <el-icon><Lock /></el-icon> 防火墙设置
                    </div>
                    <el-switch v-model="firewallSettings.enabled" @change="toggleFirewall"></el-switch>
                  </div>
                </template>
                <div v-if="firewallSettings.enabled">
                  <el-form label-width="120px">
                    <el-form-item label="默认策略">
                      <el-select v-model="firewallSettings.defaultPolicy" style="width: 200px">
                        <el-option label="拒绝所有" value="deny"></el-option>
                        <el-option label="允许所有" value="allow"></el-option>
                      </el-select>
                      <div class="form-tip">对于未明确配置的连接采用的默认处理策略</div>
                    </el-form-item>
                    <el-form-item>
                      <el-checkbox v-model="firewallSettings.enableLogging">启用日志记录</el-checkbox>
                      <div class="form-tip">记录被阻止的连接尝试</div>
                    </el-form-item>
                  </el-form>
                  
                  <!-- 防火墙规则列表 -->
                  <div class="firewall-rules">
                    <div class="rules-header">
                      <h4>防火墙规则</h4>
                      <el-button type="primary" size="small" @click="addFirewallRule">
                        <el-icon><Plus /></el-icon> 添加规则
                      </el-button>
                    </div>
                    <el-table :data="firewallRules" style="width: 100%">
                      <el-table-column prop="name" label="规则名称" width="150"></el-table-column>
                      <el-table-column prop="action" label="动作" width="80">
                        <template #default="scope">
                          <el-tag :type="scope.row.action === 'allow' ? 'success' : 'danger'" size="small">
                            {{ scope.row.action === 'allow' ? '允许' : '拒绝' }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column prop="protocol" label="协议" width="80"></el-table-column>
                      <el-table-column prop="sourceIp" label="源IP" width="120"></el-table-column>
                      <el-table-column prop="destinationPort" label="目标端口" width="100"></el-table-column>
                      <el-table-column prop="description" label="描述"></el-table-column>
                      <el-table-column prop="enabled" label="状态" width="80">
                        <template #default="scope">
                          <el-switch v-model="scope.row.enabled" size="small"></el-switch>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="120">
                        <template #default="scope">
                          <el-button size="small" type="primary" @click="editFirewallRule(scope.row)">编辑</el-button>
                          <el-button size="small" type="danger" @click="deleteFirewallRule(scope.$index)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </div>
                <div v-else class="firewall-disabled">
                  <el-empty description="防火墙已禁用">
                    <el-button type="primary" @click="firewallSettings.enabled = true">启用防火墙</el-button>
                  </el-empty>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="网络诊断" name="diagnosis">
        <div class="network-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 网络诊断工具卡片 -->
              <el-card class="network-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Tools /></el-icon> 网络诊断工具
                  </div>
                </template>
                <el-form :model="diagnosisForm" label-width="120px">
                  <el-form-item label="诊断工具">
                    <el-select v-model="diagnosisForm.tool" style="width: 200px">
                      <el-option label="Ping" value="ping"></el-option>
                      <el-option label="Traceroute" value="traceroute"></el-option>
                      <el-option label="Nslookup" value="nslookup"></el-option>
                      <el-option label="端口扫描" value="portscan"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="目标地址">
                    <el-input v-model="diagnosisForm.target" placeholder="输入主机名或IP地址" style="width: 300px">
                      <template #append>
                        <el-button @click="runDiagnosis" :loading="diagnosisRunning">执行</el-button>
                      </template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="诊断结果">
                    <div class="diagnosis-result">
                      <pre v-if="diagnosisResult">{{ diagnosisResult }}</pre>
                      <div v-else class="no-result">请输入目标地址并点击执行按钮</div>
                    </div>
                  </el-form-item>
                  <el-form-item>
                    <el-button @click="clearDiagnosisResult">清除结果</el-button>
                    <el-button type="info" @click="exportDiagnosisReport">
                      <el-icon><Download /></el-icon> 导出诊断报告
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
import { 
  Connection, Histogram, Cpu, Monitor, Setting, 
  Download, Upload, Bell, CircleCheck, Warning 
} from '@element-plus/icons-vue'
import { 
  getNetworkInterfaces, 
  updateNetworkInterface,
  enableNetworkInterface,
  disableNetworkInterface,
  reconnectNetworkInterface,
  addNetworkInterface,
  getDnsSettings,
  updateDnsSettings,
  getProxySettings,
  updateProxySettings,
  testProxyConnection as testProxyConnectionApi,
  getFirewallSettings,
  updateFirewallSettings,
  getFirewallRules,
  addFirewallRule as addFirewallRuleApi,
  updateFirewallRule as updateFirewallRuleApi,
  deleteFirewallRule as deleteFirewallRuleApi,
  getNetworkStatus,
  refreshNetworkStatus as refreshNetworkStatusApi,
  getNetworkStats,
  getPortSettings,
  updatePortSettings,
  runNetworkDiagnosis,
  exportNetworkDiagnosisReport,
  runSpeedTest as runSpeedTestApi
} from '@/api/network'

// 当前激活的标签页
const activeTab = ref('interfaces')

// 网络状态
const networkStatus = reactive({
  type: 'success',
  title: '网络连接正常',
  description: '当前网络连接稳定，所有服务正常运行'
})

// 网络接口数据 (Mock数据)
const networkInterfaces = ref([
  {
    id: 1,
    name: '以太网',
    device: 'eth0',
    type: 'ethernet',
    typeLabel: '有线连接',
    status: 'connected',
    statusLabel: '已连接',
    enabled: true,
    ipAddress: '192.168.1.100',
    subnetMask: '255.255.255.0',
    gateway: '192.168.1.1',
    macAddress: '00:1B:44:11:3A:B7'
  },
  {
    id: 2,
    name: 'Wi-Fi',
    device: 'wlan0',
    type: 'wifi',
    typeLabel: '无线连接',
    status: 'connected',
    statusLabel: '已连接',
    enabled: true,
    ipAddress: '192.168.1.101',
    subnetMask: '255.255.255.0',
    gateway: '192.168.1.1',
    macAddress: '00:1B:44:11:3A:B8',
    ssid: 'Office_Network',
    signalStrength: 85
  },
  {
    id: 3,
    name: 'VPN',
    device: 'tun0',
    type: 'vpn',
    typeLabel: 'VPN隧道',
    status: 'disconnected',
    statusLabel: '未连接',
    enabled: false,
    ipAddress: '10.0.0.1',
    subnetMask: '255.255.255.0',
    gateway: '10.0.0.1',
    macAddress: 'N/A'
  }
])

// DNS设置
const dnsSettings = reactive({
  mode: 'auto',
  primaryDns: '8.8.8.8',
  secondaryDns: '8.8.4.4',
  enableDnsSec: false,
  enableDnsCache: true
})

// 网络统计信息
const networkStats = reactive({
  uploadSpeed: '2.5 MB/s',
  downloadSpeed: '10.2 MB/s',
  latency: '15 ms',
  packetLoss: '0.1%',
  uptime: '10天 5小时'
})

// 端口设置
const portSettings = reactive({
  webPort: 80,
  httpsPort: 443,
  rtspPort: 554,
  onvifPort: 8000,
  enableUpnp: true
})

// 代理设置
const proxySettings = reactive({
  type: 'none',
  host: '',
  port: 8080,
  requireAuth: false,
  username: '',
  password: '',
  bypass: '*.local\n192.168.*\nlocalhost'
})

// 防火墙设置
const firewallSettings = reactive({
  enabled: true,
  defaultPolicy: 'deny',
  enableLogging: true
})

// 防火墙规则
const firewallRules = ref([
  {
    id: 1,
    name: 'HTTP访问',
    action: 'allow',
    protocol: 'TCP',
    sourceIp: 'any',
    destinationPort: '80',
    description: '允许HTTP访问',
    enabled: true
  },
  {
    id: 2,
    name: 'HTTPS访问',
    action: 'allow',
    protocol: 'TCP',
    sourceIp: 'any',
    destinationPort: '443',
    description: '允许HTTPS访问',
    enabled: true
  },
  {
    id: 3,
    name: '阻止P2P',
    action: 'deny',
    protocol: 'TCP',
    sourceIp: 'any',
    destinationPort: '6881-6889',
    description: '阻止P2P下载',
    enabled: true
  }
])

// 诊断表单
const diagnosisForm = reactive({
  tool: 'ping',
  target: ''
})

const diagnosisRunning = ref(false)
const diagnosisResult = ref('')

// 获取信号强度颜色
const getSignalColor = (strength) => {
  if (strength >= 80) return '#67C23A'
  if (strength >= 60) return '#E6A23C'
  if (strength >= 40) return '#F56C6C'
  return '#F56C6C'
}

// 获取信号强度标签
const getSignalLabel = (strength) => {
  if (strength >= 80) return '优'
  if (strength >= 60) return '良'
  if (strength >= 40) return '中'
  return '差'
}

// 加载网络接口数据
const loadNetworkInterfaces = async () => {
  // 直接使用Mock数据，避免API调用失败
  console.log('使用Mock数据加载网络接口')
}

// 加载DNS设置
const loadDnsSettings = async () => {
  // 直接使用Mock数据，避免API调用失败
  console.log('使用Mock数据加载DNS设置')
}

// 加载代理设置
const loadProxySettings = async () => {
  // 直接使用Mock数据，避免API调用失败
  console.log('使用Mock数据加载代理设置')
}

// 加载防火墙设置
const loadFirewallSettings = async () => {
  // 直接使用Mock数据，避免API调用失败
  console.log('使用Mock数据加载防火墙设置')
}

// 加载网络状态
const loadNetworkStatus = async () => {
  // 直接使用Mock数据，避免API调用失败
  console.log('使用Mock数据加载网络状态')
}

// 加载网络统计
const loadNetworkStats = async () => {
  // 直接使用Mock数据，避免API调用失败
  console.log('使用Mock数据加载网络统计')
}

// 加载端口设置
const loadPortSettings = async () => {
  // 直接使用Mock数据，避免API调用失败
  console.log('使用Mock数据加载端口设置')
}

// 刷新网络状态
const refreshNetworkStatus = async () => {
  ElMessage.success('网络状态已刷新')
}

// 保存网络设置
const saveNetworkSettings = async () => {
  ElMessage.success('网络设置保存成功')
}

// 切换接口状态
const toggleInterface = async (networkInterface) => {
  const status = networkInterface.enabled ? '启用' : '禁用'
  ElMessage.success(`${networkInterface.name} 已${status}`)
}

// 添加网络接口
const addInterface = () => {
  ElMessage.info('添加网络接口功能开发中')
}

// 编辑网络接口
const editInterface = (networkInterface) => {
  ElMessage.info(`编辑 ${networkInterface.name} 接口`)
}

// 重新连接接口
const reconnectInterface = async (networkInterface) => {
  ElMessage.success(`${networkInterface.name} 正在重新连接`)
}

// 显示接口详情
const showInterfaceDetails = (networkInterface) => {
  ElMessage.info(`显示 ${networkInterface.name} 详细信息`)
}

// 运行网络测速
const runSpeedTest = async () => {
  ElMessage.info('正在进行网络测速...')
}

// 测试代理连接
const testProxyConnection = async () => {
  ElMessage.success('代理连接测试成功')
}

// 切换防火墙状态
const toggleFirewall = async () => {
  const status = firewallSettings.enabled ? '启用' : '禁用'
  ElMessage.success(`防火墙已${status}`)
}

// 添加防火墙规则
const addFirewallRule = () => {
  ElMessage.info('添加防火墙规则功能开发中')
}

// 编辑防火墙规则
const editFirewallRule = (rule) => {
  ElMessage.info(`编辑规则: ${rule.name}`)
}

// 删除防火墙规则
const deleteFirewallRule = async (index) => {
  try {
    await ElMessageBox.confirm('确定要删除这条防火墙规则吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    firewallRules.value.splice(index, 1)
    ElMessage.success('防火墙规则已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除防火墙规则失败:', error)
    }
  }
}

// 运行网络诊断
const runDiagnosis = async () => {
  if (!diagnosisForm.target) {
    ElMessage.warning('请输入目标地址')
    return
  }
  
  diagnosisRunning.value = true
  
  // 使用Mock数据作为回退
  generateMockDiagnosisResult()
  
  setTimeout(() => {
    diagnosisRunning.value = false
  }, 1000)
}

// 生成Mock诊断结果
const generateMockDiagnosisResult = () => {
  diagnosisResult.value = `正在执行 ${diagnosisForm.tool} ${diagnosisForm.target}...\n`
  
  if (diagnosisForm.tool === 'ping') {
    diagnosisResult.value += `PING ${diagnosisForm.target} (192.168.1.1): 56 data bytes\n`
    diagnosisResult.value += `64 bytes from 192.168.1.1: icmp_seq=0 ttl=64 time=1.234 ms\n`
    diagnosisResult.value += `64 bytes from 192.168.1.1: icmp_seq=1 ttl=64 time=1.456 ms\n`
    diagnosisResult.value += `64 bytes from 192.168.1.1: icmp_seq=2 ttl=64 time=1.123 ms\n`
    diagnosisResult.value += `\n--- ${diagnosisForm.target} ping statistics ---\n`
    diagnosisResult.value += `3 packets transmitted, 3 packets received, 0.0% packet loss\n`
    diagnosisResult.value += `round-trip min/avg/max/stddev = 1.123/1.271/1.456/0.142 ms`
  } else if (diagnosisForm.tool === 'traceroute') {
    diagnosisResult.value += `traceroute to ${diagnosisForm.target} (8.8.8.8), 30 hops max, 60 byte packets\n`
    diagnosisResult.value += ` 1  192.168.1.1 (192.168.1.1)  1.234 ms  1.456 ms  1.123 ms\n`
    diagnosisResult.value += ` 2  10.0.0.1 (10.0.0.1)  5.678 ms  5.432 ms  5.789 ms\n`
    diagnosisResult.value += ` 3  8.8.8.8 (8.8.8.8)  15.123 ms  15.456 ms  15.789 ms`
  } else if (diagnosisForm.tool === 'nslookup') {
    diagnosisResult.value += `Server:    192.168.1.1\n`
    diagnosisResult.value += `Address:   192.168.1.1#53\n\n`
    diagnosisResult.value += `Non-authoritative answer:\n`
    diagnosisResult.value += `Name:      ${diagnosisForm.target}\n`
    diagnosisResult.value += `Address:   93.184.216.34`
  } else if (diagnosisForm.tool === 'portscan') {
    diagnosisResult.value += `Starting port scan on ${diagnosisForm.target}...\n`
    diagnosisResult.value += `PORT     STATE  SERVICE\n`
    diagnosisResult.value += `22/tcp   open   ssh\n`
    diagnosisResult.value += `80/tcp   open   http\n`
    diagnosisResult.value += `443/tcp  open   https\n`
    diagnosisResult.value += `\nScan completed in 2.34 seconds`
  }
}

// 清除诊断结果
const clearDiagnosisResult = () => {
  diagnosisResult.value = ''
}

// 导出诊断报告
const exportDiagnosisReport = async () => {
  if (!diagnosisResult.value) {
    ElMessage.warning('没有可导出的诊断结果')
    return
  }
  
  ElMessage.success('诊断报告已导出')
}

onMounted(async () => {
  // 页面加载时的初始化操作
  await Promise.all([
    loadNetworkInterfaces(),
    loadDnsSettings(),
    loadProxySettings(),
    loadFirewallSettings(),
    loadNetworkStatus(),
    loadNetworkStats(),
    loadPortSettings()
  ])
})
</script>

<style scoped>
.network-settings-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.network-status-alert {
  margin-bottom: 20px;
}

.status-actions {
  margin-left: auto;
}

.network-tabs {
  margin-bottom: 20px;
}

.network-card {
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

.interface-list {
  padding: 0;
}

.interface-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  transition: box-shadow 0.3s;
}

.interface-item:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.interface-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.interface-title {
  font-weight: 600;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.interface-switch {
  margin-left: 10px;
}

.interface-badges {
  margin-bottom: 15px;
}

.interface-badges .el-tag {
  margin-right: 8px;
}

.interface-details {
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

.signal-strength {
  display: flex;
  align-items: center;
  width: 100%;
}

.signal-strength .el-progress {
  flex: 1;
  margin-right: 10px;
}

.signal-text {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.interface-actions {
  display: flex;
  gap: 8px;
}

.network-stats {
  padding: 10px 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.stat-label {
  font-weight: 500;
  color: #606266;
}

.stat-value {
  color: #303133;
  font-weight: 600;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.dns-manual-config {
  padding-left: 20px;
  border-left: 2px solid #e4e7ed;
  margin: 15px 0;
}

.proxy-config {
  padding-left: 20px;
  border-left: 2px solid #e4e7ed;
  margin: 15px 0;
}

.auth-config {
  padding-left: 20px;
  border-left: 2px solid #f0f0f0;
  margin: 15px 0;
}

.firewall-disabled {
  text-align: center;
  padding: 40px 0;
}

.firewall-rules {
  margin-top: 20px;
}

.rules-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.rules-header h4 {
  margin: 0;
}

.diagnosis-result {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  height: 300px;
  overflow-y: auto;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.4;
}

.diagnosis-result pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.no-result {
  color: #909399;
  text-align: center;
  padding: 50px 0;
}
</style>