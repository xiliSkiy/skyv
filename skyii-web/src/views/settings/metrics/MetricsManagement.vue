<template>
  <div class="metrics-management">
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="指标名称">
          <el-input v-model="queryParams.metricName" placeholder="指标名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="指标类型">
          <el-select v-model="queryParams.metricType" placeholder="指标类型" clearable>
            <el-option label="系统性能" value="system_perf"></el-option>
            <el-option label="系统状态" value="system_status"></el-option>
            <el-option label="视频状态" value="video_status"></el-option>
            <el-option label="视频分析" value="video_analysis"></el-option>
            <el-option label="传感器数据" value="sensor_data"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="queryParams.deviceType" placeholder="设备类型" clearable>
            <el-option label="摄像机" value="camera"></el-option>
            <el-option label="服务器" value="server"></el-option>
            <el-option label="传感器" value="sensor"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable>
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计信息 -->
    <div class="stat-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">指标总数</div>
            <div class="stat-value">{{ statistics.total || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card success-card">
            <div class="stat-title">启用</div>
            <div class="stat-value">{{ statistics.active || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card warning-card">
            <div class="stat-title">禁用</div>
            <div class="stat-value">{{ statistics.inactive || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">最近采集</div>
            <div class="stat-value">{{ statistics.recentCollection || 0 }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作按钮 -->
    <div class="action-container">
      <el-button type="primary" @click="handleAdd">新增指标</el-button>
      <el-button type="success" @click="handleCreateTemplates">批量创建模板</el-button>
      <el-button type="primary" @click="handleImportFromTemplates">从模板导入</el-button>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
      <el-button type="primary" @click="handleRefresh"><i class="el-icon-refresh"></i> 刷新</el-button>
    </div>

    <!-- 表格区域 -->
    <el-table 
      v-loading="loading" 
      :data="metricList" 
      border 
      stripe 
      @selection-change="handleSelectionChange"
      style="width: 100%; margin-top: 15px;">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="metricName" label="指标名称" width="160" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="metricKey" label="指标标识" width="160" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="metricType" label="指标类型" width="120">
        <template #default="scope">
          <el-tag>{{ formatMetricType(scope.row.metricType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="applicableDeviceType" label="设备类型" width="100"></el-table-column>
      <el-table-column prop="collectionMethod" label="采集方式" width="100"></el-table-column>
      <el-table-column prop="collectionInterval" label="采集间隔(秒)" width="120"></el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)">
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="lastCollectionTime" label="最后采集时间" width="180"></el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="success" @click="handleCreateTemplate(scope.row)">创建模板</el-button>
          <el-button size="small" type="info" @click="handleTrigger(scope.row)">触发采集</el-button>
          <el-button size="small" type="primary" @click="handleViewHistory(scope.row)">采集历史</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页区域 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="queryParams.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="queryParams.limit"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="800px">
      <el-tabs v-model="activeTab" class="metric-tabs">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="metricForm" :model="metricFormData" :rules="rules" label-width="120px">
            <el-form-item label="指标名称" prop="metricName">
              <el-input v-model="metricFormData.metricName" placeholder="请输入指标名称"></el-input>
            </el-form-item>
            <el-form-item label="指标标识" prop="metricKey">
              <el-input v-model="metricFormData.metricKey" placeholder="请输入指标标识">
                <template #append>
                  <el-button @click="generateMetricKey">生成</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="指标类型" prop="metricType">
              <el-select v-model="metricFormData.metricType" placeholder="请选择指标类型" style="width: 100%;">
                <el-option label="系统性能" value="system_perf"></el-option>
                <el-option label="系统状态" value="system_status"></el-option>
                <el-option label="视频状态" value="video_status"></el-option>
                <el-option label="视频分析" value="video_analysis"></el-option>
                <el-option label="传感器数据" value="sensor_data"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input v-model="metricFormData.description" type="textarea" placeholder="请输入描述"></el-input>
            </el-form-item>
            <el-form-item label="启用状态" prop="status">
              <el-switch v-model="metricFormData.status" :active-value="1" :inactive-value="0"></el-switch>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 采集设置 -->
        <el-tab-pane label="采集设置" name="collection">
          <el-form ref="collectionForm" :model="collectionFormData" :rules="collectionRules" label-width="120px">
            <el-form-item label="采集方式" prop="collectionMethod">
              <el-select v-model="collectionFormData.collectionMethod" placeholder="请选择采集方式" style="width: 100%;" @change="handleCollectionMethodChange">
                <el-option label="SNMP" value="snmp"></el-option>
                <el-option label="HTTP API" value="http"></el-option>
                <el-option label="JMX" value="jmx"></el-option>
                <el-option label="SSH命令" value="ssh"></el-option>
                <el-option label="Agent采集" value="agent"></el-option>
                <el-option label="自定义脚本" value="custom"></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="采集频率" prop="collectionInterval">
              <el-input-number v-model="collectionFormData.collectionInterval" :min="1" :max="86400" style="width: 100%;">
                <template #append>
                  <el-select v-model="collectionFormData.intervalUnit" style="width: 80px">
                    <el-option label="秒" value="s"></el-option>
                    <el-option label="分钟" value="m"></el-option>
                    <el-option label="小时" value="h"></el-option>
                  </el-select>
                </template>
              </el-input-number>
            </el-form-item>

            <!-- SNMP配置 -->
            <template v-if="collectionFormData.collectionMethod === 'snmp'">
              <el-form-item label="OID" prop="snmpOid">
                <el-input v-model="collectionFormData.snmpOid" placeholder="例如: .1.3.6.1.2.1.25.3.3.1.2"></el-input>
              </el-form-item>
              <el-form-item label="SNMP版本" prop="snmpVersion">
                <el-select v-model="collectionFormData.snmpVersion" style="width: 100%;">
                  <el-option label="v1" value="v1"></el-option>
                  <el-option label="v2c" value="v2c"></el-option>
                  <el-option label="v3" value="v3"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="社区名" prop="snmpCommunity">
                <el-input v-model="collectionFormData.snmpCommunity" placeholder="例如: public"></el-input>
              </el-form-item>
              <!-- SNMPv3特有配置 -->
              <template v-if="collectionFormData.snmpVersion === 'v3'">
                <el-form-item label="安全级别" prop="snmpSecLevel">
                  <el-select v-model="collectionFormData.snmpSecLevel" style="width: 100%;">
                    <el-option label="noAuthNoPriv" value="noAuthNoPriv"></el-option>
                    <el-option label="authNoPriv" value="authNoPriv"></el-option>
                    <el-option label="authPriv" value="authPriv"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="认证协议" prop="snmpAuthProto">
                  <el-select v-model="collectionFormData.snmpAuthProto" style="width: 100%;">
                    <el-option label="MD5" value="MD5"></el-option>
                    <el-option label="SHA" value="SHA"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="认证密码" prop="snmpAuthPass">
                  <el-input v-model="collectionFormData.snmpAuthPass" type="password"></el-input>
                </el-form-item>
                <el-form-item label="加密协议" prop="snmpPrivProto">
                  <el-select v-model="collectionFormData.snmpPrivProto" style="width: 100%;">
                    <el-option label="DES" value="DES"></el-option>
                    <el-option label="AES" value="AES"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="加密密码" prop="snmpPrivPass">
                  <el-input v-model="collectionFormData.snmpPrivPass" type="password"></el-input>
                </el-form-item>
              </template>
            </template>

            <!-- HTTP API配置 -->
            <template v-if="collectionFormData.collectionMethod === 'http'">
              <el-form-item label="API URL" prop="httpUrl">
                <el-input v-model="collectionFormData.httpUrl" placeholder="例如: http://server:8080/api/metrics/cpu"></el-input>
              </el-form-item>
              <el-form-item label="请求方法" prop="httpMethod">
                <el-select v-model="collectionFormData.httpMethod" style="width: 100%;">
                  <el-option label="GET" value="GET"></el-option>
                  <el-option label="POST" value="POST"></el-option>
                  <el-option label="PUT" value="PUT"></el-option>
                  <el-option label="DELETE" value="DELETE"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="请求头" prop="httpHeaders">
                <el-input v-model="collectionFormData.httpHeaders" type="textarea" rows="3" placeholder='{"Content-Type": "application/json", "Authorization": "Bearer token"}'></el-input>
              </el-form-item>
              <el-form-item label="请求体" prop="httpBody" v-if="['POST', 'PUT'].includes(collectionFormData.httpMethod)">
                <el-input v-model="collectionFormData.httpBody" type="textarea" rows="3" placeholder='{"param": "value"}'></el-input>
              </el-form-item>
              <el-form-item label="数据提取路径" prop="httpDataPath">
                <el-input v-model="collectionFormData.httpDataPath" placeholder="例如: $.data.cpu.usage"></el-input>
              </el-form-item>
            </template>

            <!-- JMX配置 -->
            <template v-if="collectionFormData.collectionMethod === 'jmx'">
              <el-form-item label="MBean名称" prop="jmxMBean">
                <el-input v-model="collectionFormData.jmxMBean" placeholder="例如: java.lang:type=Memory"></el-input>
              </el-form-item>
              <el-form-item label="属性名称" prop="jmxAttribute">
                <el-input v-model="collectionFormData.jmxAttribute" placeholder="例如: HeapMemoryUsage"></el-input>
              </el-form-item>
              <el-form-item label="复合数据路径" prop="jmxPath">
                <el-input v-model="collectionFormData.jmxPath" placeholder="例如: used"></el-input>
              </el-form-item>
              <el-form-item label="JMX端口" prop="jmxPort">
                <el-input-number v-model="collectionFormData.jmxPort" :min="1" :max="65535" style="width: 100%;"></el-input-number>
              </el-form-item>
              <el-form-item label="使用SSL" prop="jmxSsl">
                <el-switch v-model="collectionFormData.jmxSsl"></el-switch>
              </el-form-item>
            </template>

            <!-- SSH命令配置 -->
            <template v-if="collectionFormData.collectionMethod === 'ssh'">
              <el-form-item label="执行命令" prop="sshCommand">
                <el-input v-model="collectionFormData.sshCommand" type="textarea" rows="3" placeholder="例如: top -bn1 | grep 'Cpu(s)' | awk '{print $2 + $4}'"></el-input>
              </el-form-item>
              <el-form-item label="执行用户" prop="sshUser">
                <el-input v-model="collectionFormData.sshUser" placeholder="例如: root"></el-input>
              </el-form-item>
              <el-form-item label="认证方式" prop="sshAuth">
                <el-select v-model="collectionFormData.sshAuth" style="width: 100%;">
                  <el-option label="密码认证" value="password"></el-option>
                  <el-option label="密钥认证" value="key"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="密码" prop="sshPassword" v-if="collectionFormData.sshAuth === 'password'">
                <el-input v-model="collectionFormData.sshPassword" type="password"></el-input>
              </el-form-item>
              <el-form-item label="密钥文件" prop="sshKeyFile" v-if="collectionFormData.sshAuth === 'key'">
                <el-input v-model="collectionFormData.sshKeyFile" placeholder="例如: /root/.ssh/id_rsa"></el-input>
              </el-form-item>
            </template>

            <!-- 自定义脚本配置 -->
            <template v-if="collectionFormData.collectionMethod === 'custom'">
              <el-form-item label="脚本类型" prop="scriptType">
                <el-select v-model="collectionFormData.scriptType" style="width: 100%;">
                  <el-option label="Shell脚本" value="shell"></el-option>
                  <el-option label="Python脚本" value="python"></el-option>
                  <el-option label="JavaScript" value="javascript"></el-option>
                  <el-option label="Groovy" value="groovy"></el-option>
                  <el-option label="PowerShell" value="powershell"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="脚本内容" prop="scriptContent">
                <el-input v-model="collectionFormData.scriptContent" type="textarea" rows="8" placeholder="请输入脚本内容..."></el-input>
              </el-form-item>
              <el-form-item label="脚本参数" prop="scriptParams">
                <el-input v-model="collectionFormData.scriptParams" placeholder="例如: -f config.json -t 30"></el-input>
              </el-form-item>
              <el-form-item label="执行超时" prop="scriptTimeout">
                <el-input-number v-model="collectionFormData.scriptTimeout" :min="1" :max="3600" style="width: 100%;">
                  <template #append>秒</template>
                </el-input-number>
              </el-form-item>
              <el-form-item label="脚本编码" prop="scriptEncoding">
                <el-select v-model="collectionFormData.scriptEncoding" style="width: 100%;">
                  <el-option label="UTF-8" value="UTF-8"></el-option>
                  <el-option label="GBK" value="GBK"></el-option>
                  <el-option label="ISO-8859-1" value="ISO-8859-1"></el-option>
                </el-select>
              </el-form-item>
            </template>
          </el-form>
        </el-tab-pane>

        <!-- 阈值告警 -->
        <el-tab-pane label="阈值告警" name="threshold">
          <el-form ref="thresholdForm" :model="thresholdFormData" :rules="thresholdRules" label-width="120px">
            <el-form-item label="启用告警" prop="enableAlert">
              <el-switch v-model="thresholdFormData.enableAlert"></el-switch>
            </el-form-item>
            <template v-if="thresholdFormData.enableAlert">
              <el-form-item label="告警级别" prop="alertLevel">
                <el-select v-model="thresholdFormData.alertLevel" style="width: 100%;">
                  <el-option label="信息" value="info"></el-option>
                  <el-option label="警告" value="warning"></el-option>
                  <el-option label="错误" value="error"></el-option>
                  <el-option label="严重" value="critical"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="告警条件" prop="alertCondition">
                <el-select v-model="thresholdFormData.alertCondition" style="width: 100%;">
                  <el-option label="大于" value="gt"></el-option>
                  <el-option label="大于等于" value="gte"></el-option>
                  <el-option label="小于" value="lt"></el-option>
                  <el-option label="小于等于" value="lte"></el-option>
                  <el-option label="等于" value="eq"></el-option>
                  <el-option label="不等于" value="ne"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="阈值" prop="threshold">
                <el-input-number v-model="thresholdFormData.threshold" style="width: 100%;"></el-input-number>
              </el-form-item>
              <el-form-item label="持续时间" prop="duration">
                <el-input-number v-model="thresholdFormData.duration" :min="1" style="width: 100%;">
                  <template #append>
                    <el-select v-model="thresholdFormData.durationUnit" style="width: 80px">
                      <el-option label="秒" value="s"></el-option>
                      <el-option label="分钟" value="m"></el-option>
                      <el-option label="小时" value="h"></el-option>
                    </el-select>
                  </template>
                </el-input-number>
              </el-form-item>
              <el-form-item label="告警通知" prop="notifyChannels">
                <el-checkbox-group v-model="thresholdFormData.notifyChannels">
                  <el-checkbox label="email">邮件</el-checkbox>
                  <el-checkbox label="sms">短信</el-checkbox>
                  <el-checkbox label="webhook">Webhook</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </template>
          </el-form>
        </el-tab-pane>

        <!-- 高级设置 -->
        <el-tab-pane label="高级设置" name="advanced">
          <el-form ref="advancedForm" :model="advancedFormData" :rules="advancedRules" label-width="120px">
            <el-form-item label="数据类型" prop="dataType">
              <el-select v-model="advancedFormData.dataType" style="width: 100%;">
                <el-option label="数值型" value="number"></el-option>
                <el-option label="字符串" value="string"></el-option>
                <el-option label="布尔值" value="boolean"></el-option>
                <el-option label="JSON" value="json"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="单位" prop="unit">
              <el-input v-model="advancedFormData.unit" placeholder="例如: %"></el-input>
            </el-form-item>
            <el-form-item label="数据保留" prop="retention">
              <el-input-number v-model="advancedFormData.retention" :min="1" style="width: 100%;">
                <template #append>
                  <el-select v-model="advancedFormData.retentionUnit" style="width: 80px">
                    <el-option label="天" value="d"></el-option>
                    <el-option label="周" value="w"></el-option>
                    <el-option label="月" value="m"></el-option>
                  </el-select>
                </template>
              </el-input-number>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="advancedFormData.remark" type="textarea" rows="3" placeholder="请输入备注信息"></el-input>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="dialog.loading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 从模板导入指标对话框 -->
    <el-dialog title="从模板导入指标" v-model="importDialog.visible" width="650px">
      <el-form ref="importForm" :model="importForm" :rules="importRules" label-width="120px">
        <el-form-item label="选择模板" prop="templateIds">
          <el-select 
            v-model="importForm.templateIds" 
            multiple 
            placeholder="请选择模板" 
            style="width: 100%;">
            <el-option 
              v-for="item in templateOptions" 
              :key="item.id" 
              :label="item.templateName" 
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitImportForm" :loading="importDialog.loading">确定</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getMetrics,
  getMetricById,
  createMetric,
  updateMetric,
  deleteMetric,
  batchDeleteMetrics,
  updateMetricStatus,
  batchUpdateMetricStatus,
  getMetricStatistics,
  checkMetricKey,
  exportMetrics,
  importMetrics
} from '@/api/metrics'
import { getAllMetricTemplates } from '@/api/metrics'
import bus from '@/utils/eventBus'

export default {
  name: 'MetricsManagement',
  setup() {
    // 查询参数
    const queryParams = reactive({
      page: 1,
      limit: 10,
      metricName: '',
      metricType: '',
      deviceType: '',
      status: null
    })

    // 表单数据
    const metricFormData = ref({
      id: null,
      metricName: '',
      metricKey: '',
      metricType: 'system_perf',
      description: '',
      applicableDeviceType: 'server',
      status: 1,
      protocolConfig: '{}'
    })

    // 采集设置表单
    const collectionFormData = ref({
      collectionMethod: '',
      collectionInterval: 60,
      intervalUnit: 's',
      // SNMP配置
      snmpOid: '',
      snmpVersion: 'v2c',
      snmpCommunity: 'public',
      snmpSecLevel: 'noAuthNoPriv',
      snmpAuthProto: 'MD5',
      snmpAuthPass: '',
      snmpPrivProto: 'DES',
      snmpPrivPass: '',
      // HTTP配置
      httpUrl: '',
      httpMethod: 'GET',
      httpHeaders: '',
      httpBody: '',
      httpDataPath: '',
      // JMX配置
      jmxMBean: '',
      jmxAttribute: '',
      jmxPath: '',
      jmxPort: 9999,
      jmxSsl: false,
      // SSH配置
      sshCommand: '',
      sshUser: '',
      sshAuth: 'password',
      sshPassword: '',
      sshKeyFile: '',
      // 自定义脚本配置
      scriptContent: '',
      scriptType: 'shell',
      scriptParams: '',
      scriptTimeout: 30,
      scriptEncoding: 'UTF-8'
    })

    // 阈值告警表单
    const thresholdFormData = ref({
      enableAlert: false,
      alertLevel: 'warning',
      alertCondition: 'gt',
      threshold: 0,
      duration: 1,
      durationUnit: 'm',
      notifyChannels: []
    })

    // 高级设置表单
    const advancedFormData = ref({
      dataType: 'number',
      unit: '',
      retention: 30,
      retentionUnit: 'd',
      remark: ''
    })

    // 表单校验规则
    const rules = {
      metricName: [
        { required: true, message: '请输入指标名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      metricKey: [
        { required: true, message: '请输入指标标识', trigger: 'blur' },
        { pattern: /^[a-z][a-z0-9_]*$/, message: '只能包含小写字母、数字和下划线，且必须以字母开头', trigger: 'blur' }
      ],
      metricType: [
        { required: true, message: '请选择指标类型', trigger: 'change' }
      ]
    }

    const collectionRules = {
      collectionMethod: [
        { required: true, message: '请选择采集方式', trigger: 'change' }
      ],
      collectionInterval: [
        { required: true, message: '请输入采集频率', trigger: 'blur' }
      ],
      snmpOid: [
        { required: true, message: '请输入OID', trigger: 'blur', validator: (rule, value, callback) => {
          if (collectionFormData.collectionMethod === 'snmp' && !value) {
            callback(new Error('请输入OID'))
          } else {
            callback()
          }
        }}
      ],
      httpUrl: [
        { required: true, message: '请输入API URL', trigger: 'blur', validator: (rule, value, callback) => {
          if (collectionFormData.collectionMethod === 'http' && !value) {
            callback(new Error('请输入API URL'))
          } else {
            callback()
          }
        }}
      ],
      jmxMBean: [
        { required: true, message: '请输入MBean名称', trigger: 'blur', validator: (rule, value, callback) => {
          if (collectionFormData.collectionMethod === 'jmx' && !value) {
            callback(new Error('请输入MBean名称'))
          } else {
            callback()
          }
        }}
      ],
      sshCommand: [
        { required: true, message: '请输入执行命令', trigger: 'blur', validator: (rule, value, callback) => {
          if (collectionFormData.collectionMethod === 'ssh' && !value) {
            callback(new Error('请输入执行命令'))
          } else {
            callback()
          }
        }}
      ],
      scriptContent: [
        { required: true, message: '请输入脚本内容', trigger: 'blur', validator: (rule, value, callback) => {
          if (collectionFormData.collectionMethod === 'custom' && !value) {
            callback(new Error('请输入脚本内容'))
          } else {
            callback()
          }
        }}
      ],
      scriptType: [
        { required: true, message: '请选择脚本类型', trigger: 'change', validator: (rule, value, callback) => {
          if (collectionFormData.collectionMethod === 'custom' && !value) {
            callback(new Error('请选择脚本类型'))
          } else {
            callback()
          }
        }}
      ]
    }

    const thresholdRules = {
      alertLevel: [
        { required: true, message: '请选择告警级别', trigger: 'change', validator: (rule, value, callback) => {
          if (thresholdFormData.enableAlert && !value) {
            callback(new Error('请选择告警级别'))
          } else {
            callback()
          }
        }}
      ],
      alertCondition: [
        { required: true, message: '请选择告警条件', trigger: 'change', validator: (rule, value, callback) => {
          if (thresholdFormData.enableAlert && !value) {
            callback(new Error('请选择告警条件'))
          } else {
            callback()
          }
        }}
      ],
      threshold: [
        { required: true, message: '请输入阈值', trigger: 'blur', validator: (rule, value, callback) => {
          if (thresholdFormData.enableAlert && value === undefined) {
            callback(new Error('请输入阈值'))
          } else {
            callback()
          }
        }}
      ]
    }

    const advancedRules = {
      dataType: [
        { required: true, message: '请选择数据类型', trigger: 'change' }
      ]
    }

    // 当前激活的标签页
    const activeTab = ref('basic')

    // 数据
    const loading = ref(false)
    const metricList = ref([])
    const total = ref(0)
    const selectedIds = ref([])
    const statistics = ref({})

    // 对话框
    const dialog = reactive({
      visible: false,
      title: '',
      loading: false
    })

    // 从模板导入对话框
    const importDialog = reactive({
      visible: false,
      loading: false
    })

    // 表单引用
    const metricForm = ref(null)
    const collectionForm = ref(null)
    const thresholdForm = ref(null)
    const advancedForm = ref(null)
    
    // 模板选项
    const templateOptions = ref([])
    
    // 从模板导入表单
    const importForm = reactive({
      templateIds: []
    })
    
    // 导入规则
    const importRules = {
      templateIds: [
        { required: true, message: '请选择至少一个模板', trigger: 'change' }
      ]
    }

    // 格式化指标类型
    const formatMetricType = (type) => {
      const typeMap = {
        'system_perf': '系统性能',
        'system_status': '系统状态',
        'video_status': '视频状态',
        'video_analysis': '视频分析',
        'sensor_data': '传感器数据'
      }
      return typeMap[type] || type
    }

    // 生成指标标识
    const generateMetricKey = () => {
      if (!metricFormData.metricName) {
        ElMessage.warning('请先输入指标名称')
        return
      }
      // 将中文转换为拼音，并转换为小写
      const pinyin = metricFormData.metricName
        .toLowerCase()
        .replace(/[^a-z0-9]/g, '_')
        .replace(/_+/g, '_')
        .replace(/^_|_$/g, '')
      metricFormData.metricKey = pinyin
    }

    // 查询指标列表
    const fetchData = async () => {
      loading.value = true
      try {
        const params = {
          page: queryParams.page - 1,
          size: queryParams.limit,
          metricName: queryParams.metricName || null,
          metricType: queryParams.metricType || null,
          applicableDeviceType: queryParams.deviceType || null,
          status: queryParams.status
        }
        const response = await getMetrics(params)
        metricList.value = response.data.content
        total.value = response.data.totalElements
      } catch (error) {
        console.error('获取指标列表失败', error)
        ElMessage.error('获取指标列表失败')
      } finally {
        loading.value = false
      }
    }

    // 查询统计数据
    const fetchStatistics = async () => {
      try {
        const response = await getMetricStatistics()
        statistics.value = response.data
      } catch (error) {
        console.error('获取统计数据失败', error)
      }
    }

    // 查询
    const handleQuery = () => {
      queryParams.page = 1
      fetchData()
    }

    // 重置查询
    const resetQuery = () => {
      queryParams.metricName = ''
      queryParams.metricType = ''
      queryParams.deviceType = ''
      queryParams.status = null
      handleQuery()
    }

    // 表格选择变化
    const handleSelectionChange = (selection) => {
      selectedIds.value = selection.map(item => item.id)
    }

    // 每页数量变化
    const handleSizeChange = (val) => {
      queryParams.limit = val
      fetchData()
    }

    // 当前页变化
    const handleCurrentChange = (val) => {
      queryParams.page = val
      fetchData()
    }

    // 新增指标
    const handleAdd = () => {
      dialog.title = '新增指标'
      dialog.visible = true
      activeTab.value = 'basic'
      
      // 重置表单数据 - 使用reactive数据时，应当修改属性而不是整个替换
      Object.assign(metricFormData, {
        id: null,
        metricName: '',
        metricKey: '',
        metricType: 'system_perf',
        description: '',
        applicableDeviceType: 'server',
        status: 1,
        protocolConfig: '{}'
      })
      
      Object.assign(collectionFormData, {
        collectionMethod: '',
        collectionInterval: 60,
        intervalUnit: 's',
        // 其他字段重置为空值
        snmpOid: '',
        snmpVersion: 'v2c',
        snmpCommunity: 'public',
        // ...其他字段
      })
      
      Object.assign(thresholdFormData, {
        enableAlert: false,
        alertLevel: 'warning',
        alertCondition: 'gt',
        threshold: 0,
        duration: 1,
        durationUnit: 'm',
        notifyChannels: []
      })
      
      Object.assign(advancedFormData, {
        dataType: 'number',
        unit: '',
        retention: 30,
        retentionUnit: 'd',
        remark: ''
      })
    }

    // 编辑指标
    const handleEdit = async (row) => {
      if (!row) {
        ElMessage.warning('编辑数据无效')
        return
      }
      dialog.title = '编辑指标'
      dialog.visible = true
      dialog.loading = true
      
      try {
        const response = await getMetricById(row.id)
        
        // 过滤掉collectionMethod和collectionInterval，避免重复
        Object.keys(metricFormData).forEach(key => {
          if (key !== 'collectionMethod' && key !== 'collectionInterval') {
            metricFormData[key] = response.data[key] !== undefined ? response.data[key] : metricFormData[key]
          }
        })
        
        // 使用Object.assign更新对象属性
        Object.assign(collectionFormData, row.collectionConfig || {})
        
        // 更新阈值告警表单
        if (row.thresholdConfig) {
          Object.assign(thresholdFormData, row.thresholdConfig)
        } else {
          Object.assign(thresholdFormData, {
            enableAlert: false,
            alertLevel: 'warning',
            alertCondition: 'gt',
            threshold: 0,
            duration: 1,
            durationUnit: 'm',
            notifyChannels: []
          })
        }
        
        // 更新高级设置表单
        Object.assign(advancedFormData, row.advancedConfig || {})
      } catch (error) {
        console.error('获取指标详情失败', error)
        ElMessage.error('获取指标详情失败')
      } finally {
        dialog.loading = false
      }
    }

    // 删除指标
    const handleDelete = (row) => {
      if (!row) {
        ElMessage.warning('删除数据无效')
        return
      }
      ElMessageBox.confirm('确定要删除此指标吗？该操作不可逆', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteMetric(row.id).then(() => {
          ElMessage.success('删除成功')
          fetchData()
          fetchStatistics()
        }).catch(error => {
          console.error('删除失败', error)
          ElMessage.error('删除失败')
        })
      }).catch(() => {})
    }

    // 批量删除
    const handleBatchDelete = () => {
      if (selectedIds.value.length === 0) {
        ElMessage.warning('请选择要删除的指标')
        return
      }
      
      ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个指标吗？该操作不可逆`, '批量删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        batchDeleteMetrics(selectedIds.value).then(() => {
          ElMessage.success('批量删除成功')
          fetchData()
          fetchStatistics()
        }).catch(error => {
          console.error('批量删除失败', error)
          ElMessage.error('批量删除失败')
        })
      }).catch(() => {})
    }

    // 提交表单
    const submitForm = async () => {
      try {
        // 表单验证
        const promises = []
        
        if (metricForm.value) {
          promises.push(metricForm.value.validate())
        } else {
          console.warn('metricForm not defined')
        }
        
        if (collectionForm.value) {
          promises.push(collectionForm.value.validate())
        } else {
          console.warn('collectionForm not defined')
        }
        
        if (thresholdForm.value) {
          promises.push(thresholdForm.value.validate())
        } else {
          console.warn('thresholdForm not defined')
        }
        
        if (advancedForm.value) {
          promises.push(advancedForm.value.validate())
        } else {
          console.warn('advancedForm not defined')
        }
        
        await Promise.all(promises)

        dialog.loading = true

        // 确保所有表单数据都存在
        if (!metricFormData || !collectionFormData || !thresholdFormData || !advancedFormData) {
          throw new Error('表单数据不完整')
        }

        // 构建提交数据
        const submitData = {
          ...metricFormData,
          collectionMethod: collectionFormData.collectionMethod,
          collectionInterval: collectionFormData.collectionInterval,
          collectionConfig: {
            ...collectionFormData
          },
          thresholdConfig: thresholdFormData.enableAlert ? {
            ...thresholdFormData
          } : null,
          advancedConfig: {
            ...advancedFormData
          }
        }

        if (dialog.title === '新增指标') {
          await createMetric(submitData)
        } else {
          await updateMetric(submitData.id, submitData)
        }

        ElMessage.success(dialog.title === '新增指标' ? '新增成功' : '更新成功')
        dialog.visible = false
        fetchData()
        fetchStatistics()
      } catch (error) {
        console.error('表单验证失败:', error)
        ElMessage.error('提交失败: ' + (error.message || '表单验证未通过'))
      } finally {
        dialog.loading = false
      }
    }

    // 处理状态变更
    const handleStatusChange = async (row) => {
      if (!row || row.id === undefined) {
        ElMessage.warning('状态更新数据无效')
        return
      }
      try {
        await updateMetricStatus(row.id, row.status)
        ElMessage.success(row.status === 1 ? '启用成功' : '禁用成功')
        fetchStatistics()
      } catch (error) {
        console.error('状态更新失败', error)
        ElMessage.error('状态更新失败')
        // 回滚UI状态
        row.status = row.status === 1 ? 0 : 1
      }
    }

    // 查看历史
    const handleViewHistory = (row) => {
      if (!row || !row.id) {
        ElMessage.warning('采集历史数据无效')
        return
      }
      // 跳转到采集历史页面，并设置查询参数
      const params = {
        activeTab: 'history',
        metricId: row.id
      }
      // 使用事件通知父组件
      bus.emit('switchToHistory', params)
    }

    // 刷新
    const handleRefresh = () => {
      fetchData()
      fetchStatistics()
      ElMessage.success('刷新成功')
    }

    // 从模板导入指标
    const handleImportFromTemplates = async () => {
      importDialog.visible = true
      try {
        const response = await getAllMetricTemplates()
        templateOptions.value = response.data
      } catch (error) {
        console.error('获取模板列表失败', error)
        ElMessage.error('获取模板列表失败')
      }
    }

    // 提交导入表单
    const submitImportForm = async () => {
      if (!importForm.templateIds || importForm.templateIds.length === 0) {
        ElMessage.warning('请选择至少一个模板')
        return
      }
      
      importDialog.loading = true
      try {
        const response = await importMetrics({ templateIds: importForm.templateIds })
        ElMessage.success(`成功导入${response.data}个指标`)
        importDialog.visible = false
        fetchData()
        fetchStatistics()
      } catch (error) {
        console.error('导入失败', error)
        ElMessage.error('导入失败')
      } finally {
        importDialog.loading = false
      }
    }

    // 批量创建模板
    const handleCreateTemplates = () => {
      if (selectedIds.value.length === 0) {
        ElMessage.warning('请选择要创建模板的指标')
        return
      }
      
      ElMessageBox.confirm(`确定要为选中的 ${selectedIds.value.length} 个指标创建模板吗？`, '批量创建模板', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        // 这里调用批量创建模板的API
        ElMessage.info('批量创建模板功能正在开发中')
      }).catch(() => {})
    }

    // 创建模板
    const handleCreateTemplate = (row) => {
      if (!row) {
        ElMessage.warning('模板创建数据无效')
        return
      }
      ElMessageBox.prompt('请输入模板名称', '创建模板', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^.{2,50}$/,
        inputErrorMessage: '名称长度在2-50个字符'
      }).then(({ value }) => {
        // 调用创建模板的API
        ElMessage.info(`模板创建功能正在开发中: ${value}`)
      }).catch(() => {})
    }

    // 触发采集
    const handleTrigger = (row) => {
      if (!row || !row.metricName) {
        ElMessage.info('手动触发采集功能正在开发中')
        return
      }
      ElMessage.info(`手动触发采集功能正在开发中: ${row.metricName}`)
    }

    // 处理采集方式变更
    const handleCollectionMethodChange = (value) => {
      // 重置相关字段，使用Object.assign更新对象属性
      Object.assign(collectionFormData, {
        collectionMethod: value,
        collectionInterval: 60,
        intervalUnit: 's'
      })

      // 根据选择的采集方式设置默认值
      if (value === 'custom') {
        collectionFormData.scriptType = 'shell';
        collectionFormData.scriptTimeout = 30;
        collectionFormData.scriptEncoding = 'UTF-8';
      } else if (value === 'snmp') {
        collectionFormData.snmpVersion = 'v2c';
        collectionFormData.snmpCommunity = 'public';
      } else if (value === 'jmx') {
        collectionFormData.jmxPort = 9999;
        collectionFormData.jmxSsl = false;
      } else if (value === 'http') {
        collectionFormData.httpMethod = 'GET';
      }
    }

    onMounted(() => {
      fetchData()
      fetchStatistics()
    })

    return {
      queryParams,
      metricFormData,
      rules,
      loading,
      metricList,
      total,
      selectedIds,
      statistics,
      dialog,
      importDialog,
      importForm,
      importRules,
      templateOptions,
      generateMetricKey,
      fetchData,
      handleQuery,
      resetQuery,
      handleSelectionChange,
      handleSizeChange,
      handleCurrentChange,
      handleAdd,
      handleEdit,
      handleDelete,
      handleBatchDelete,
      submitForm,
      handleStatusChange,
      handleViewHistory,
      handleRefresh,
      handleCreateTemplate,
      handleCreateTemplates,
      handleImportFromTemplates,
      submitImportForm,
      formatMetricType,
      collectionFormData,
      collectionRules,
      thresholdFormData,
      thresholdRules,
      advancedFormData,
      advancedRules,
      activeTab,
      handleCollectionMethodChange,
      handleTrigger
    }
  }
}
</script>

<style scoped>
.metrics-management {
  padding: 10px;
}

.search-container {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.stat-container {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 10px;
}

.success-card .stat-value {
  color: #67C23A;
}

.warning-card .stat-value {
  color: #E6A23C;
}

.stat-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.action-container {
  margin-bottom: 15px;
}

.pagination-container {
  margin-top: 15px;
  text-align: right;
}

/* 新增样式 */
.metric-tabs {
  padding: 0 20px;
}

.metric-tabs :deep(.el-tabs__nav) {
  width: 100%;
  display: flex;
}

.metric-tabs :deep(.el-tabs__item) {
  flex: 1;
  text-align: center;
}

.metric-tabs :deep(.el-form-item) {
  margin-bottom: 22px;
}

.metric-tabs :deep(.el-input-number) {
  width: 100%;
}

.metric-tabs :deep(.el-select) {
  width: 100%;
}

.metric-tabs :deep(.el-textarea__inner) {
  font-family: monospace;
}

.metric-tabs :deep(.el-form-item__label) {
  font-weight: 500;
}

.metric-tabs :deep(.el-switch) {
  margin-right: 10px;
}

.metric-tabs :deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.metric-tabs :deep(.el-checkbox) {
  margin-right: 0;
  margin-left: 0;
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}
</style> 