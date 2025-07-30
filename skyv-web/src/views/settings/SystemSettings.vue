<template>
  <div class="system-settings-container">
    <div class="page-header">
      <div>
        <h4>系统配置</h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/settings' }">系统设置</el-breadcrumb-item>
          <el-breadcrumb-item>系统配置</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div>
        <el-button type="primary" @click="saveSettings">
          <el-icon><Save /></el-icon> 保存配置
        </el-button>
      </div>
    </div>
    
    <!-- 系统配置页面导航标签 -->
    <el-tabs v-model="activeTab" class="settings-tabs">
      <el-tab-pane label="基本设置" name="general">
        <div class="settings-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 基本信息配置卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><InfoFilled /></el-icon> 基本信息
                  </div>
                </template>
                <el-form :model="basicForm" label-width="100px">
                  <el-form-item label="系统名称">
                    <el-input v-model="basicForm.systemName" placeholder="请输入系统名称"></el-input>
                    <div class="form-tip">显示在浏览器标题栏和系统界面中的名称</div>
                  </el-form-item>
                  <el-form-item label="系统描述">
                    <el-input v-model="basicForm.systemDescription" type="textarea" :rows="2" placeholder="请输入系统描述"></el-input>
                    <div class="form-tip">简短描述系统的主要功能和用途</div>
                  </el-form-item>
                  <el-form-item label="系统Logo">
                    <el-upload
                      class="logo-uploader"
                      action="#"
                      :http-request="uploadLogo"
                      :show-file-list="false"
                      :before-upload="beforeLogoUpload">
                      <img v-if="logoUrl" :src="logoUrl" class="logo-preview" />
                      <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
                    </el-upload>
                    <div class="form-tip">建议尺寸：200x50像素，支持PNG、SVG格式（透明背景）</div>
                    <div class="mt-10">
                      <el-button size="small" type="danger" @click="resetLogo">
                        <el-icon><Delete /></el-icon> 恢复默认
                      </el-button>
                    </div>
                  </el-form-item>
                  <el-form-item label="Favicon图标">
                    <el-upload
                      class="favicon-uploader"
                      action="#"
                      :http-request="uploadFavicon"
                      :show-file-list="false"
                      :before-upload="beforeFaviconUpload">
                      <img v-if="faviconUrl" :src="faviconUrl" class="favicon-preview" />
                      <el-icon v-else class="favicon-uploader-icon"><Plus /></el-icon>
                    </el-upload>
                    <div class="form-tip">显示在浏览器标签页的小图标，建议尺寸：32x32像素</div>
                  </el-form-item>
                </el-form>
              </el-card>
              
              <!-- 时间设置卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Clock /></el-icon> 时间设置
                  </div>
                </template>
                <el-form :model="timeForm" label-width="100px">
                  <el-form-item label="时区">
                    <el-select v-model="timeForm.timezone" placeholder="请选择时区" style="width: 100%">
                      <el-option label="(GMT+08:00) 北京，重庆，香港，乌鲁木齐" value="UTC+8"></el-option>
                      <el-option label="(GMT+00:00) 格林威治标准时间，都柏林，爱丁堡，伦敦" value="UTC+0"></el-option>
                      <el-option label="(GMT-05:00) 东部时间（美国和加拿大）" value="UTC-5"></el-option>
                      <el-option label="(GMT-08:00) 太平洋时间（美国和加拿大）" value="UTC-8"></el-option>
                      <el-option label="(GMT+01:00) 布鲁塞尔，哥本哈根，马德里，巴黎" value="UTC+1"></el-option>
                      <el-option label="(GMT+09:00) 大阪，札幌，东京" value="UTC+9"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="日期格式">
                    <el-select v-model="timeForm.dateFormat" placeholder="请选择日期格式" style="width: 100%">
                      <el-option label="YYYY-MM-DD (2023-11-20)" value="YYYY-MM-DD"></el-option>
                      <el-option label="DD/MM/YYYY (20/11/2023)" value="DD/MM/YYYY"></el-option>
                      <el-option label="MM/DD/YYYY (11/20/2023)" value="MM/DD/YYYY"></el-option>
                      <el-option label="YYYY年MM月DD日 (2023年11月20日)" value="YYYY年MM月DD日"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="时间格式">
                    <el-select v-model="timeForm.timeFormat" placeholder="请选择时间格式" style="width: 100%">
                      <el-option label="24小时制 (14:30)" value="24h"></el-option>
                      <el-option label="12小时制 (2:30 PM)" value="12h"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item>
                    <el-checkbox v-model="timeForm.syncNTP">启用NTP时间同步</el-checkbox>
                    <div class="form-tip">通过网络时间协议自动同步系统时间</div>
                  </el-form-item>
                  <el-form-item label="NTP服务器" v-if="timeForm.syncNTP">
                    <el-input v-model="timeForm.ntpServer" placeholder="请输入NTP服务器地址"></el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="info" @click="syncTime">
                      <el-icon><RefreshRight /></el-icon> 立即同步时间
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <!-- 系统状态卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Monitor /></el-icon> 系统状态
                  </div>
                </template>
                <div class="system-status">
                  <div class="status-item">
                    <span class="status-label">系统版本</span>
                    <span class="status-value">v2.5.3</span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">上次更新</span>
                    <span class="status-value">2023-11-15</span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">许可证状态</span>
                    <span class="status-value"><el-tag type="success" size="small">有效</el-tag></span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">许可证有效期</span>
                    <span class="status-value">2024-12-31</span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">授权设备数</span>
                    <span class="status-value">50 / 100</span>
                  </div>
                  <el-divider></el-divider>
                  <div class="status-actions">
                    <el-button type="primary" plain @click="checkUpdate">
                      <el-icon><Download /></el-icon> 检查更新
                    </el-button>
                    <el-button type="info" plain @click="manageLicense">
                      <el-icon><Key /></el-icon> 管理许可证
                    </el-button>
                  </div>
                </div>
              </el-card>
              
              <!-- 操作记录卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Tickets /></el-icon> 最近操作记录
                  </div>
                </template>
                <div class="operation-logs">
                  <el-timeline>
                    <el-timeline-item
                      v-for="(log, index) in operationLogs"
                      :key="index"
                      :timestamp="log.time"
                      size="small">
                      <div class="log-content">
                        <div class="log-title">{{ log.title }}</div>
                        <div class="log-user">{{ log.user }}</div>
                      </div>
                    </el-timeline-item>
                  </el-timeline>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="界面外观" name="appearance">
        <div class="settings-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 界面主题配置卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Brush /></el-icon> 界面主题
                  </div>
                </template>
                <el-form :model="themeForm" label-width="100px">
                  <el-form-item label="主题配色">
                    <el-row :gutter="20">
                      <el-col :span="6">
                        <div class="theme-option">
                          <el-radio v-model="themeForm.themeColor" label="blue">
                            <div class="color-preview blue-theme"></div>
                            <span>蓝色渐变</span>
                          </el-radio>
                        </div>
                      </el-col>
                      <el-col :span="6">
                        <div class="theme-option">
                          <el-radio v-model="themeForm.themeColor" label="green">
                            <div class="color-preview green-theme"></div>
                            <span>绿色渐变</span>
                          </el-radio>
                        </div>
                      </el-col>
                      <el-col :span="6">
                        <div class="theme-option">
                          <el-radio v-model="themeForm.themeColor" label="purple">
                            <div class="color-preview purple-theme"></div>
                            <span>紫色渐变</span>
                          </el-radio>
                        </div>
                      </el-col>
                      <el-col :span="6">
                        <div class="theme-option">
                          <el-radio v-model="themeForm.themeColor" label="dark">
                            <div class="color-preview dark-theme"></div>
                            <span>深色渐变</span>
                          </el-radio>
                        </div>
                      </el-col>
                    </el-row>
                  </el-form-item>
                  <el-form-item label="自定义颜色">
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <el-form-item label="主色调" label-width="80px">
                          <el-color-picker v-model="themeForm.primaryColor" show-alpha></el-color-picker>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="次色调" label-width="80px">
                          <el-color-picker v-model="themeForm.secondaryColor" show-alpha></el-color-picker>
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </el-form-item>
                  <el-form-item label="布局选项">
                    <el-checkbox v-model="themeForm.fixedHeader">固定顶部导航栏</el-checkbox>
                    <el-checkbox v-model="themeForm.fixedSidebar">固定侧边导航栏</el-checkbox>
                    <el-checkbox v-model="themeForm.compactSidebar">紧凑侧边栏</el-checkbox>
                    <el-checkbox v-model="themeForm.darkMode">深色模式</el-checkbox>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="applyThemeSetting">
                      <el-icon><View /></el-icon> 预览效果
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
              
              <!-- 界面元素配置 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Grid /></el-icon> 界面元素
                  </div>
                </template>
                <el-form label-width="100px">
                  <el-form-item label="表格密度">
                    <el-radio-group v-model="themeForm.tableDensity">
                      <el-radio label="default">默认</el-radio>
                      <el-radio label="medium">中等</el-radio>
                      <el-radio label="small">紧凑</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="菜单动画">
                    <el-switch v-model="themeForm.menuAnimation"></el-switch>
                    <div class="form-tip">启用菜单展开/收起动画效果</div>
                  </el-form-item>
                  <el-form-item label="页面动画">
                    <el-switch v-model="themeForm.pageAnimation"></el-switch>
                    <div class="form-tip">启用页面切换过渡动画</div>
                  </el-form-item>
                  <el-form-item label="按钮圆角">
                    <el-slider v-model="themeForm.buttonRadius" :min="0" :max="20" :step="1"></el-slider>
                    <div class="form-tip">调整按钮圆角大小 (0-20px)</div>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <!-- 系统预览卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><View /></el-icon> 界面预览
                  </div>
                </template>
                <div class="theme-preview-container">
                  <div class="theme-preview" :class="{ 'active': true, 'dark-preview': themeForm.darkMode }">
                    <img src="https://via.placeholder.com/400x300/f8f9fa/1e3c72?text=系统预览" class="preview-image" alt="系统预览">
                  </div>
                  <div class="preview-tip">当前选择的主题预览效果</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="语言与区域" name="language">
        <div class="settings-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 语言设置卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><ChatDotRound /></el-icon> 语言设置
                  </div>
                </template>
                <el-form :model="languageForm" label-width="120px">
                  <el-form-item label="当前语言">
                    <el-select v-model="languageForm.currentLanguage" style="width: 100%">
                      <el-option 
                        v-for="lang in languageForm.availableLanguages" 
                        :key="lang.code" 
                        :label="lang.name" 
                        :value="lang.code">
                        <div class="language-option">
                          <img :src="lang.flag" class="language-flag" v-if="lang.flag" />
                          <span>{{ lang.name }}</span>
                        </div>
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="默认语言">
                    <el-select v-model="languageForm.defaultLanguage" style="width: 100%">
                      <el-option 
                        v-for="lang in languageForm.availableLanguages" 
                        :key="lang.code" 
                        :label="lang.name" 
                        :value="lang.code">
                        <div class="language-option">
                          <img :src="lang.flag" class="language-flag" v-if="lang.flag" />
                          <span>{{ lang.name }}</span>
                        </div>
                      </el-option>
                    </el-select>
                    <div class="form-tip">新用户首次访问时使用的默认语言</div>
                  </el-form-item>
                  <el-form-item>
                    <el-checkbox v-model="languageForm.enableMultiLanguage">启用多语言支持</el-checkbox>
                    <div class="form-tip">允许用户在不同语言间切换</div>
                  </el-form-item>
                  <el-form-item v-if="languageForm.enableMultiLanguage">
                    <el-checkbox v-model="languageForm.autoDetect">自动检测浏览器语言</el-checkbox>
                    <div class="form-tip">根据用户浏览器设置自动选择语言</div>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="applyLanguageSetting">
                      <el-icon><Check /></el-icon> 应用语言设置
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
              
              <!-- 区域设置卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Location /></el-icon> 区域设置
                  </div>
                </template>
                <el-form label-width="120px">
                  <el-form-item label="数字格式">
                    <el-select v-model="languageForm.numberFormat" style="width: 100%">
                      <el-option label="1,234,567.89" value="en-US"></el-option>
                      <el-option label="1 234 567,89" value="fr-FR"></el-option>
                      <el-option label="1.234.567,89" value="de-DE"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="货币符号">
                    <el-select v-model="languageForm.currencySymbol" style="width: 100%">
                      <el-option label="¥ (人民币)" value="CNY"></el-option>
                      <el-option label="$ (美元)" value="USD"></el-option>
                      <el-option label="€ (欧元)" value="EUR"></el-option>
                      <el-option label="£ (英镑)" value="GBP"></el-option>
                    </el-select>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
            
            <el-col :span="8">
              <!-- 语言预览卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Document /></el-icon> 语言示例
                  </div>
                </template>
                <div class="language-preview">
                  <h4>界面元素示例</h4>
                  <div class="preview-item">
                    <div class="preview-label">菜单项:</div>
                    <div class="preview-value">仪表盘、设备管理、报警中心</div>
                  </div>
                  <div class="preview-item">
                    <div class="preview-label">按钮:</div>
                    <div class="preview-value">保存、取消、确认、删除</div>
                  </div>
                  <div class="preview-item">
                    <div class="preview-label">提示信息:</div>
                    <div class="preview-value">操作成功、请输入必填项</div>
                  </div>
                  
                  <h4>日期时间格式示例</h4>
                  <div class="preview-item">
                    <div class="preview-label">日期:</div>
                    <div class="preview-value">2023-11-20</div>
                  </div>
                  <div class="preview-item">
                    <div class="preview-label">时间:</div>
                    <div class="preview-value">14:30:00</div>
                  </div>
                  
                  <h4>数字格式示例</h4>
                  <div class="preview-item">
                    <div class="preview-label">数字:</div>
                    <div class="preview-value">1,234,567.89</div>
                  </div>
                  <div class="preview-item">
                    <div class="preview-label">货币:</div>
                    <div class="preview-value">¥1,234,567.89</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="许可证管理" name="license">
        <div class="settings-content">
          <el-row :gutter="20">
            <el-col :span="16">
              <!-- 许可证信息卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Ticket /></el-icon> 许可证信息
                  </div>
                </template>
                <div class="license-info">
                  <div class="license-header">
                    <div class="license-title">
                      <h3>{{ licenseInfo.licenseType }}</h3>
                      <el-tag type="success" v-if="licenseInfo.expiryDate > new Date().toISOString().split('T')[0]">有效</el-tag>
                      <el-tag type="danger" v-else>已过期</el-tag>
                    </div>
                    <div class="license-key">许可证密钥: {{ licenseInfo.licenseKey }}</div>
                  </div>
                  
                  <el-descriptions :column="2" border>
                    <el-descriptions-item label="授权公司">{{ licenseInfo.company }}</el-descriptions-item>
                    <el-descriptions-item label="联系人">{{ licenseInfo.contactPerson }}</el-descriptions-item>
                    <el-descriptions-item label="联系邮箱">{{ licenseInfo.contactEmail }}</el-descriptions-item>
                    <el-descriptions-item label="发行日期">{{ licenseInfo.issueDate }}</el-descriptions-item>
                    <el-descriptions-item label="到期日期">{{ licenseInfo.expiryDate }}</el-descriptions-item>
                    <el-descriptions-item label="设备授权">
                      {{ licenseInfo.currentDevices }} / {{ licenseInfo.maxDevices }}
                      <el-progress :percentage="(licenseInfo.currentDevices / licenseInfo.maxDevices) * 100" :stroke-width="10"></el-progress>
                    </el-descriptions-item>
                  </el-descriptions>
                  
                  <div class="license-features">
                    <h4>授权功能</h4>
                    <el-tag
                      v-for="feature in licenseInfo.features"
                      :key="feature"
                      class="feature-tag">
                      {{ feature }}
                    </el-tag>
                  </div>
                </div>
              </el-card>
              
              <!-- 许可证激活卡片 -->
              <el-card class="settings-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <el-icon><Key /></el-icon> 许可证激活
                  </div>
                </template>
                <el-form :model="activationForm" label-width="120px">
                  <el-form-item label="许可证文件">
                    <el-upload
                      class="license-uploader"
                      action="#"
                      :http-request="uploadLicenseFile"
                      :show-file-list="false">
                      <el-button type="primary">
                        <el-icon><Upload /></el-icon> 上传许可证文件
                      </el-button>
                      <div class="form-tip">上传由厂商提供的.lic或.key格式的许可证文件</div>
                    </el-upload>
                  </el-form-item>
                  <el-divider content-position="center">或者</el-divider>
                  <el-form-item label="许可证密钥">
                    <el-input v-model="activationForm.licenseKey" placeholder="请输入许可证密钥"></el-input>
                  </el-form-item>
                  <el-form-item label="激活码">
                    <el-input v-model="activationForm.activationCode" placeholder="请输入激活码"></el-input>
                  </el-form-item>
                  <el-form-item label="公司名称">
                    <el-input v-model="activationForm.companyName" placeholder="请输入公司名称"></el-input>
                  </el-form-item>
                  <el-form-item label="联系邮箱">
                    <el-input v-model="activationForm.contactEmail" placeholder="请输入联系邮箱"></el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="activateLicenseAction">
                      <el-icon><Check /></el-icon> 激活许可证
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
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSettingsByGroup, updateSetting, batchUpdateSettings, uploadLogo as uploadLogoApi, uploadFavicon as uploadFaviconApi, resetDefaultLogo, getOperationLogs, checkSystemUpdate, getLicenseInfo, uploadLicense, activateLicense, getAvailableLanguages, getCurrentLanguage, setSystemLanguage } from '@/api/settings'

// 当前激活的标签页
const activeTab = ref('general')

// 基本信息表单
const basicForm = reactive({
  systemName: 'SkyEye 智能监控系统',
  systemDescription: '智能视频监控与安防管理系统',
})

// Logo和Favicon
const logoUrl = ref('')
const faviconUrl = ref('')

// 主题表单
const themeForm = reactive({
  themeColor: 'blue',
  primaryColor: '#1e3c72',
  secondaryColor: '#2a5298',
  fixedHeader: true,
  fixedSidebar: true,
  compactSidebar: false,
  darkMode: false,
  tableDensity: 'default',
  menuAnimation: true,
  pageAnimation: true,
  buttonRadius: 4
})

// 时间设置表单
const timeForm = reactive({
  timezone: 'UTC+8',
  dateFormat: 'YYYY-MM-DD',
  timeFormat: '24h',
  syncNTP: true,
  ntpServer: 'pool.ntp.org'
})

// 语言设置表单
const languageForm = reactive({
  currentLanguage: 'zh_CN',
  defaultLanguage: 'zh_CN',
  enableMultiLanguage: true,
  autoDetect: true,
  availableLanguages: [],
  numberFormat: 'en-US',
  currencySymbol: 'CNY'
})

// 许可证信息
const licenseInfo = reactive({
  licenseType: '企业版',
  licenseKey: 'SKYEYE-ENT-1234-5678-90AB-CDEF',
  company: '智能安防科技有限公司',
  contactPerson: '张三',
  contactEmail: 'zhangsan@example.com',
  issueDate: '2023-01-01',
  expiryDate: '2024-12-31',
  maxDevices: 100,
  currentDevices: 50,
  features: ['基础监控', '智能分析', 'AI识别', '多用户管理', '高级报表']
})

// 许可证激活表单
const activationForm = reactive({
  licenseKey: '',
  activationCode: '',
  companyName: '',
  contactEmail: ''
})

// 操作日志
const operationLogs = ref([
  { title: '修改系统名称', time: '今天 10:23', user: '管理员 (admin)' },
  { title: '更新系统Logo', time: '昨天 14:05', user: '管理员 (admin)' },
  { title: '修改时区设置', time: '2023-11-18', user: '系统管理员 (sysadmin)' },
  { title: '系统升级至v2.5.3', time: '2023-11-15', user: '系统管理员 (sysadmin)' }
])

// 监听标签页变化
watch(activeTab, (newTab) => {
  if (newTab === 'language' && languageForm.availableLanguages.length === 0) {
    loadLanguageSettings()
  } else if (newTab === 'license') {
    loadLicenseInfo()
  }
})

// 加载设置数据
onMounted(async () => {
  try {
    // 加载基本设置
    const basicSettings = await getSettingsByGroup('basic')
    if (basicSettings.data) {
      basicForm.systemName = basicSettings.data.systemName || basicForm.systemName
      basicForm.systemDescription = basicSettings.data.systemDescription || basicForm.systemDescription
      logoUrl.value = basicSettings.data.logoUrl || ''
      faviconUrl.value = basicSettings.data.faviconUrl || ''
    }
    
    // 加载主题设置
    const themeSettings = await getSettingsByGroup('theme')
    if (themeSettings.data) {
      themeForm.themeColor = themeSettings.data.themeColor || themeForm.themeColor
      themeForm.primaryColor = themeSettings.data.primaryColor || themeForm.primaryColor
      themeForm.secondaryColor = themeSettings.data.secondaryColor || themeForm.secondaryColor
      themeForm.fixedHeader = themeSettings.data.fixedHeader !== undefined ? themeSettings.data.fixedHeader : themeForm.fixedHeader
      themeForm.fixedSidebar = themeSettings.data.fixedSidebar !== undefined ? themeSettings.data.fixedSidebar : themeForm.fixedSidebar
      themeForm.compactSidebar = themeSettings.data.compactSidebar !== undefined ? themeSettings.data.compactSidebar : themeForm.compactSidebar
      themeForm.darkMode = themeSettings.data.darkMode !== undefined ? themeSettings.data.darkMode : themeForm.darkMode
      themeForm.tableDensity = themeSettings.data.tableDensity || themeForm.tableDensity
      themeForm.menuAnimation = themeSettings.data.menuAnimation !== undefined ? themeSettings.data.menuAnimation : themeForm.menuAnimation
      themeForm.pageAnimation = themeSettings.data.pageAnimation !== undefined ? themeSettings.data.pageAnimation : themeForm.pageAnimation
      themeForm.buttonRadius = themeSettings.data.buttonRadius || themeForm.buttonRadius
    }
    
    // 加载时间设置
    const timeSettings = await getSettingsByGroup('time')
    if (timeSettings.data) {
      timeForm.timezone = timeSettings.data.timezone || timeForm.timezone
      timeForm.dateFormat = timeSettings.data.dateFormat || timeForm.dateFormat
      timeForm.timeFormat = timeSettings.data.timeFormat || timeForm.timeFormat
      timeForm.syncNTP = timeSettings.data.syncNTP !== undefined ? timeSettings.data.syncNTP : timeForm.syncNTP
      timeForm.ntpServer = timeSettings.data.ntpServer || timeForm.ntpServer
    }
    
    // 加载操作日志
    const logs = await getOperationLogs({ limit: 4 })
    if (logs.data && logs.data.length > 0) {
      operationLogs.value = logs.data
    }
  } catch (error) {
    console.error('加载设置失败:', error)
    ElMessage.error('加载设置数据失败，请刷新页面重试')
  }
})

// 加载语言设置
const loadLanguageSettings = async () => {
  try {
    // 获取可用语言列表
    const languages = await getAvailableLanguages()
    if (languages.data) {
      languageForm.availableLanguages = languages.data
    }
    
    // 获取当前语言设置
    const currentLang = await getCurrentLanguage()
    if (currentLang.data) {
      languageForm.currentLanguage = currentLang.data.currentLanguage || languageForm.currentLanguage
      languageForm.defaultLanguage = currentLang.data.defaultLanguage || languageForm.defaultLanguage
      languageForm.enableMultiLanguage = currentLang.data.enableMultiLanguage !== undefined ? 
        currentLang.data.enableMultiLanguage : languageForm.enableMultiLanguage
      languageForm.autoDetect = currentLang.data.autoDetect !== undefined ? 
        currentLang.data.autoDetect : languageForm.autoDetect
    }
  } catch (error) {
    console.error('加载语言设置失败:', error)
    ElMessage.error('加载语言设置失败，请重试')
  }
}

// 加载许可证信息
const loadLicenseInfo = async () => {
  try {
    const license = await getLicenseInfo()
    if (license.data) {
      Object.assign(licenseInfo, license.data)
    }
  } catch (error) {
    console.error('加载许可证信息失败:', error)
    ElMessage.error('加载许可证信息失败，请重试')
  }
}

// 保存设置
const saveSettings = async () => {
  try {
    // 根据当前激活的标签页保存不同的设置
    let settings = {}
    
    if (activeTab.value === 'general') {
      settings = {
        basic: {
          systemName: basicForm.systemName,
          systemDescription: basicForm.systemDescription,
          logoUrl: logoUrl.value,
          faviconUrl: faviconUrl.value
        },
        time: {
          timezone: timeForm.timezone,
          dateFormat: timeForm.dateFormat,
          timeFormat: timeForm.timeFormat,
          syncNTP: timeForm.syncNTP,
          ntpServer: timeForm.ntpServer
        }
      }
    } else if (activeTab.value === 'appearance') {
      settings = {
        theme: {
          themeColor: themeForm.themeColor,
          primaryColor: themeForm.primaryColor,
          secondaryColor: themeForm.secondaryColor,
          fixedHeader: themeForm.fixedHeader,
          fixedSidebar: themeForm.fixedSidebar,
          compactSidebar: themeForm.compactSidebar,
          darkMode: themeForm.darkMode,
          tableDensity: themeForm.tableDensity,
          menuAnimation: themeForm.menuAnimation,
          pageAnimation: themeForm.pageAnimation,
          buttonRadius: themeForm.buttonRadius
        }
      }
    } else if (activeTab.value === 'language') {
      settings = {
        language: {
          currentLanguage: languageForm.currentLanguage,
          defaultLanguage: languageForm.defaultLanguage,
          enableMultiLanguage: languageForm.enableMultiLanguage,
          autoDetect: languageForm.autoDetect
        }
      }
      
      // 设置系统语言
      await setSystemLanguage({
        language: languageForm.currentLanguage
      })
    }
    
    await batchUpdateSettings(settings)
    ElMessage.success('系统设置保存成功')
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败，请重试')
  }
}

// 上传许可证文件
const uploadLicenseFile = async (options) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const res = await uploadLicense(formData)
    
    if (res.data && res.data.licenseKey) {
      activationForm.licenseKey = res.data.licenseKey
      ElMessage.success('许可证文件上传成功，请完成激活')
    }
  } catch (error) {
    console.error('上传许可证失败:', error)
    ElMessage.error('上传许可证失败，请重试')
  }
}

// 激活许可证
const activateLicenseAction = async () => {
  try {
    if (!activationForm.licenseKey) {
      ElMessage.warning('请先上传许可证文件或输入许可证密钥')
      return
    }
    
    await activateLicense(activationForm)
    ElMessage.success('许可证激活成功')
    
    // 重新加载许可证信息
    loadLicenseInfo()
    
    // 重置表单
    activationForm.licenseKey = ''
    activationForm.activationCode = ''
    activationForm.companyName = ''
    activationForm.contactEmail = ''
  } catch (error) {
    console.error('激活许可证失败:', error)
    ElMessage.error('激活许可证失败，请检查激活信息是否正确')
  }
}

// 应用语言设置
const applyLanguageSetting = async () => {
  try {
    await setSystemLanguage({
      language: languageForm.currentLanguage
    })
    ElMessage.success('语言设置已应用，部分更改可能需要刷新页面才能生效')
  } catch (error) {
    console.error('应用语言设置失败:', error)
    ElMessage.error('应用语言设置失败，请重试')
  }
}

// 应用主题设置
const applyThemeSetting = () => {
  // 这里可以实现实时预览主题效果的逻辑
  ElMessage.success('主题设置已应用')
}

// 同步时间
const syncTime = () => {
  ElMessage.success('时间同步成功')
}

// 上传Logo
const uploadLogo = async (options) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const res = await uploadLogoApi(formData)
    
    if (res.data && res.data.logoUrl) {
      logoUrl.value = res.data.logoUrl
      ElMessage.success('Logo上传成功')
    }
  } catch (error) {
    console.error('上传Logo失败:', error)
    ElMessage.error('上传Logo失败，请重试')
  }
}

// 上传Favicon
const uploadFavicon = async (options) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const res = await uploadFaviconApi(formData)
    
    if (res.data && res.data.faviconUrl) {
      faviconUrl.value = res.data.faviconUrl
      ElMessage.success('Favicon上传成功')
    }
  } catch (error) {
    console.error('上传Favicon失败:', error)
    ElMessage.error('上传Favicon失败，请重试')
  }
}

// 重置Logo
const resetLogo = async () => {
  try {
    await resetDefaultLogo()
    logoUrl.value = ''
    ElMessage.success('Logo已重置为默认')
  } catch (error) {
    console.error('重置Logo失败:', error)
    ElMessage.error('重置Logo失败，请重试')
  }
}

// 文件上传前验证
const beforeLogoUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const beforeFaviconUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt1M = file.size / 1024 / 1024 < 1

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt1M) {
    ElMessage.error('图片大小不能超过 1MB!')
    return false
  }
  return true
}

// 检查更新
const checkUpdate = async () => {
  try {
    const res = await checkSystemUpdate()
    if (res.data && res.data.hasUpdate) {
      ElMessage.info(`发现新版本：${res.data.version}，请前往下载更新`)
    } else {
      ElMessage.success('当前已是最新版本')
    }
  } catch (error) {
    console.error('检查更新失败:', error)
    ElMessage.error('检查更新失败，请重试')
  }
}

// 管理许可证
const manageLicense = () => {
  ElMessage.info('即将跳转到许可证管理页面')
}
</script>

<style scoped>
.system-settings-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.settings-tabs {
  margin-bottom: 20px;
}

.settings-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
}

.card-header .el-icon {
  margin-right: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.logo-uploader,
.favicon-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.logo-uploader:hover,
.favicon-uploader:hover {
  border-color: #409EFF;
}

.logo-uploader-icon,
.favicon-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 50px;
  line-height: 50px;
  text-align: center;
}

.favicon-uploader-icon {
  width: 32px;
  height: 32px;
  line-height: 32px;
}

.logo-preview {
  width: 200px;
  height: 50px;
  display: block;
}

.favicon-preview {
  width: 32px;
  height: 32px;
  display: block;
}

.color-preview {
  width: 30px;
  height: 30px;
  border-radius: 5px;
  display: inline-block;
  margin-right: 10px;
  border: 1px solid #ddd;
}

.blue-theme {
  background: linear-gradient(180deg, #1e3c72, #2a5298);
}

.green-theme {
  background: linear-gradient(180deg, #134e5e, #71b280);
}

.purple-theme {
  background: linear-gradient(180deg, #4a00e0, #8e2de2);
}

.dark-theme {
  background: linear-gradient(180deg, #232526, #414345);
}

.theme-option {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.theme-preview-container {
  text-align: center;
}

.theme-preview {
  border: 2px solid transparent;
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.3s;
  margin-bottom: 10px;
}

.theme-preview.active {
  border-color: #1e3c72;
}

.theme-preview.dark-preview {
  background-color: #1a1a1a;
}

.preview-image {
  width: 100%;
  height: auto;
  display: block;
}

.preview-tip {
  font-size: 12px;
  color: #909399;
}

.system-status {
  padding: 10px 0;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.status-label {
  font-weight: bold;
  color: #606266;
}

.status-value {
  color: #303133;
}

.status-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.operation-logs {
  max-height: 300px;
  overflow-y: auto;
}

.log-content {
  padding: 5px 0;
}

.log-title {
  font-weight: bold;
  margin-bottom: 3px;
}

.log-user {
  font-size: 12px;
  color: #909399;
}

.mt-10 {
  margin-top: 10px;
}

/* 语言设置相关样式 */
.language-option {
  display: flex;
  align-items: center;
}

.language-flag {
  width: 20px;
  height: 15px;
  margin-right: 8px;
}

.language-preview {
  padding: 10px;
}

.preview-item {
  margin-bottom: 10px;
}

.preview-label {
  font-weight: bold;
  margin-bottom: 5px;
}

.preview-value {
  color: #606266;
}

/* 许可证相关样式 */
.license-info {
  padding: 10px 0;
}

.license-header {
  margin-bottom: 20px;
}

.license-title {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.license-title h3 {
  margin: 0;
  margin-right: 10px;
}

.license-key {
  font-family: monospace;
  background-color: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}

.license-features {
  margin-top: 20px;
}

.feature-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.license-uploader {
  display: flex;
  flex-direction: column;
}
</style>