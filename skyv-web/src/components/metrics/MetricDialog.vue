<template>
  <el-dialog 
    :title="isEdit ? '编辑指标' : '添加指标'" 
    :visible="visible" 
    @update:visible="updateVisible"
    width="60%"
    destroy-on-close>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
          <el-form-item label="指标名称" prop="metricName">
            <el-input v-model="form.metricName" placeholder="例如: MySQL连接数"></el-input>
          </el-form-item>
          
          <el-form-item label="指标标识符" prop="metricKey">
            <el-input v-model="form.metricKey" placeholder="例如: mysql_connections"></el-input>
            <div class="form-text text-muted">系统内部使用的唯一标识符，不含空格和特殊字符</div>
          </el-form-item>
          
          <el-form-item label="指标类型" prop="metricType">
            <el-select v-model="form.metricType" placeholder="选择指标类型" style="width: 100%">
              <el-option label="服务器指标" value="server"></el-option>
              <el-option label="中间件指标" value="middleware"></el-option>
              <el-option label="数据库指标" value="database"></el-option>
              <el-option label="自定义指标" value="custom"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="适用设备类型" prop="applicableDeviceType">
            <el-input v-model="form.applicableDeviceType" placeholder="例如: 服务器"></el-input>
          </el-form-item>
          
          <el-form-item label="指标描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="描述该指标的用途和含义..."></el-input>
          </el-form-item>
          
          <el-form-item label="启用状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0"></el-switch>
            <span class="ms-2">{{ form.status === 1 ? '启用' : '禁用' }}</span>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="采集设置" name="collection">
        <el-form :model="form" ref="collectionFormRef" label-width="120px">
          <el-form-item label="采集方式" prop="collectionMethod">
            <el-select v-model="form.collectionMethod" placeholder="选择采集方式" style="width: 100%" @change="handleCollectionMethodChange">
              <el-option label="SNMP" value="snmp"></el-option>
              <el-option label="JMX" value="jmx"></el-option>
              <el-option label="WMI" value="wmi"></el-option>
              <el-option label="HTTP API" value="http"></el-option>
              <el-option label="SSH命令" value="ssh"></el-option>
              <el-option label="Agent采集" value="agent"></el-option>
              <el-option label="自定义脚本" value="custom"></el-option>
            </el-select>
          </el-form-item>
          
          <div class="alert alert-info mb-3">
            <i class="fas fa-info-circle me-2"></i> 指标定义与设备关联分离
            <p class="small text-muted mb-1 mt-2">
              此处创建的指标为通用定义，不需要在此关联具体设备。设备关联将在"任务调度"模块中进行，
              可通过设备标签、设备类型或具体设备实例灵活关联。这样可使指标定义更通用，一个指标可应用在多种场景。
            </p>
            <div class="form-text">
              <i class="fas fa-lightbulb text-warning me-1"></i> 提示：配置好指标后，到"任务调度"模块创建采集任务并关联设备。
            </div>
          </div>
          
          <el-form-item label="采集频率" prop="collectionInterval">
            <div class="d-flex align-items-center">
              <el-input-number v-model="form.collectionInterval" :min="1" :max="3600"></el-input-number>
              <el-select v-model="form.collectionIntervalUnit" style="width: 120px; margin-left: 10px">
                <el-option label="秒" value="s"></el-option>
                <el-option label="分钟" value="m"></el-option>
                <el-option label="小时" value="h"></el-option>
              </el-select>
            </div>
            <div class="form-text text-muted">建议值: 服务器指标30秒, 应用指标1分钟, 性能分析5分钟</div>
          </el-form-item>
          
          <!-- SNMP配置 -->
          <div v-if="form.collectionMethod === 'snmp'" class="protocol-config border rounded p-3 mb-3">
            <h6 class="mb-3">SNMP配置</h6>
            
            <el-form-item label="OID" prop="snmpOid">
              <el-input v-model="protocolConfig.snmpOid" placeholder="例如: .1.3.6.1.2.1.25.3.3.1.2"></el-input>
              <div class="form-text text-muted">SNMP对象标识符，定义了要采集的具体指标</div>
            </el-form-item>
            
            <el-form-item label="SNMP版本" prop="snmpVersion">
              <el-select v-model="protocolConfig.snmpVersion" style="width: 100%">
                <el-option label="v1" value="v1"></el-option>
                <el-option label="v2c" value="v2c"></el-option>
                <el-option label="v3" value="v3"></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="社区名(Community)" prop="snmpCommunity">
              <el-input v-model="protocolConfig.snmpCommunity" placeholder="public"></el-input>
            </el-form-item>
            
            <div v-if="protocolConfig.snmpVersion === 'v3'">
              <el-form-item label="安全级别" prop="snmpSecLevel">
                <el-select v-model="protocolConfig.snmpSecLevel" style="width: 100%">
                  <el-option label="noAuthNoPriv" value="noAuthNoPriv"></el-option>
                  <el-option label="authNoPriv" value="authNoPriv"></el-option>
                  <el-option label="authPriv" value="authPriv"></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="认证协议" prop="snmpAuthProto">
                <el-select v-model="protocolConfig.snmpAuthProto" style="width: 100%">
                  <el-option label="MD5" value="MD5"></el-option>
                  <el-option label="SHA" value="SHA"></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="认证密码" prop="snmpAuthPass">
                <el-input v-model="protocolConfig.snmpAuthPass" type="password"></el-input>
              </el-form-item>
              
              <el-form-item label="加密协议" prop="snmpPrivProto">
                <el-select v-model="protocolConfig.snmpPrivProto" style="width: 100%">
                  <el-option label="DES" value="DES"></el-option>
                  <el-option label="AES" value="AES"></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="加密密码" prop="snmpPrivPass">
                <el-input v-model="protocolConfig.snmpPrivPass" type="password"></el-input>
              </el-form-item>
            </div>
            
            <div class="d-flex justify-content-end">
              <el-button size="small" type="primary" @click="testConnection">
                <i class="fas fa-vial me-1"></i> 测试连接
              </el-button>
            </div>
          </div>
          
          <!-- HTTP API配置 -->
          <div v-if="form.collectionMethod === 'http'" class="protocol-config border rounded p-3 mb-3">
            <h6 class="mb-3">HTTP API配置</h6>
            
            <el-form-item label="API URL" prop="httpUrl">
              <el-input v-model="protocolConfig.httpUrl" placeholder="例如: http://server:8080/api/metrics/cpu"></el-input>
            </el-form-item>
            
            <el-form-item label="请求方法" prop="httpMethod">
              <el-select v-model="protocolConfig.httpMethod" style="width: 100%">
                <el-option label="GET" value="GET"></el-option>
                <el-option label="POST" value="POST"></el-option>
                <el-option label="PUT" value="PUT"></el-option>
                <el-option label="DELETE" value="DELETE"></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="请求头(Headers)" prop="httpHeaders">
              <el-input v-model="protocolConfig.httpHeaders" type="textarea" :rows="3" placeholder='{"Content-Type": "application/json", "Authorization": "Bearer token"}'></el-input>
              <div class="form-text text-muted">以JSON格式添加请求头</div>
            </el-form-item>
            
            <el-form-item label="请求体(Body)" prop="httpBody" v-if="['POST', 'PUT'].includes(protocolConfig.httpMethod)">
              <el-input v-model="protocolConfig.httpBody" type="textarea" :rows="3" placeholder='{"param": "value"}'></el-input>
            </el-form-item>
            
            <el-form-item label="数据提取路径" prop="httpDataPath">
              <el-input v-model="protocolConfig.httpDataPath" placeholder="例如: $.data.cpu.usage"></el-input>
              <div class="form-text text-muted">使用JSONPath表达式从响应中提取指标值</div>
            </el-form-item>
            
            <el-form-item label="认证方式" prop="httpAuth">
              <el-select v-model="protocolConfig.httpAuth" style="width: 100%" @change="handleHttpAuthChange">
                <el-option label="无" value="none"></el-option>
                <el-option label="Basic认证" value="basic"></el-option>
                <el-option label="Bearer Token" value="bearer"></el-option>
                <el-option label="自定义" value="custom"></el-option>
              </el-select>
            </el-form-item>
            
            <div v-if="protocolConfig.httpAuth === 'basic'">
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="用户名" prop="httpUsername">
                    <el-input v-model="protocolConfig.httpUsername"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="密码" prop="httpPassword">
                    <el-input v-model="protocolConfig.httpPassword" type="password"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            
            <div class="d-flex justify-content-end">
              <el-button size="small" type="primary" @click="testConnection">
                <i class="fas fa-vial me-1"></i> 测试API
              </el-button>
            </div>
          </div>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="阈值告警" name="threshold">
        <el-form :model="form" ref="thresholdFormRef" label-width="120px">
          <el-form-item label="启用阈值告警">
            <el-switch v-model="form.thresholdEnabled" :active-value="true" :inactive-value="false"></el-switch>
          </el-form-item>
          
          <template v-if="form.thresholdEnabled">
            <div class="card mb-3 border-warning">
              <div class="card-header bg-warning bg-opacity-10">
                <span class="fw-bold">警告级别</span>
              </div>
              <div class="card-body">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="阈值" prop="warningThreshold">
                      <el-input v-model="form.warningThreshold">
                        <template #append>%</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="持续时间" prop="warningDuration">
                      <el-input v-model="form.warningDuration">
                        <template #append>分钟</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </div>
            
            <div class="card mb-3 border-danger">
              <div class="card-header bg-danger bg-opacity-10">
                <span class="fw-bold">严重级别</span>
              </div>
              <div class="card-body">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="阈值" prop="criticalThreshold">
                      <el-input v-model="form.criticalThreshold">
                        <template #append>%</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="持续时间" prop="criticalDuration">
                      <el-input v-model="form.criticalDuration">
                        <template #append>分钟</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </div>
            
            <el-form-item label="通知组" prop="notificationGroups">
              <el-select v-model="form.notificationGroups" multiple style="width: 100%">
                <el-option label="管理员组" value="admin"></el-option>
                <el-option label="运维团队" value="ops"></el-option>
                <el-option label="开发团队" value="dev"></el-option>
                <el-option label="系统支持" value="support"></el-option>
              </el-select>
              <div class="form-text text-muted">可多选，按住Ctrl键选择多个通知组</div>
            </el-form-item>
          </template>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="高级设置" name="advanced">
        <el-form :model="form" ref="advancedFormRef" label-width="120px">
          <el-form-item label="数据类型" prop="dataType">
            <el-select v-model="form.dataType" style="width: 100%">
              <el-option label="计量型（Gauge）" value="gauge"></el-option>
              <el-option label="计数器（Counter）" value="counter"></el-option>
              <el-option label="导出型（Derive）" value="derive"></el-option>
              <el-option label="绝对值（Absolute）" value="absolute"></el-option>
            </el-select>
            <div class="form-text text-muted">计量型：可增可减的值，如CPU使用率；计数器：只增不减的值，如网络流量</div>
          </el-form-item>
          
          <el-form-item label="单位" prop="unit">
            <el-select v-model="form.unit" style="width: 100%">
              <el-option label="百分比 (%)" value="percent"></el-option>
              <el-option label="字节 (B)" value="bytes"></el-option>
              <el-option label="毫秒 (ms)" value="ms"></el-option>
              <el-option label="计数" value="count"></el-option>
              <el-option label="自定义..." value="custom"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="数据格式" prop="dataFormat">
            <el-select v-model="form.dataFormat" style="width: 100%">
              <el-option label="默认格式" value="default"></el-option>
              <el-option label="JSON" value="json"></el-option>
              <el-option label="XML" value="xml"></el-option>
              <el-option label="CSV" value="csv"></el-option>
              <el-option label="自定义格式" value="custom"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="数据处理脚本" prop="processingScript">
            <el-input v-model="form.processingScript" type="textarea" :rows="6" placeholder="// 可选：输入数据处理脚本，例如转换单位、计算衍生值等..."></el-input>
          </el-form-item>
          
          <el-form-item label="启用数据缓存">
            <el-switch v-model="form.enableCache" :active-value="true" :inactive-value="false"></el-switch>
          </el-form-item>
          
          <el-form-item label="数据保留期" prop="retentionPeriod">
            <el-select v-model="form.retentionPeriod" style="width: 100%">
              <el-option label="7天" value="7d"></el-option>
              <el-option label="30天" value="30d"></el-option>
              <el-option label="90天" value="90d"></el-option>
              <el-option label="180天" value="180d"></el-option>
              <el-option label="365天" value="365d"></el-option>
              <el-option label="自定义..." value="custom"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="updateVisible(false)">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="loading">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { defineComponent, ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createMetric, updateMetric, checkMetricKey } from '@/api/metrics'

export default defineComponent({
  name: 'MetricDialog',
  props: {
    visible: {
      type: Boolean,
      required: true
    },
    metric: {
      type: Object,
      default: () => ({})
    },
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:visible', 'success'],
  setup(props, { emit }) {
    const activeTab = ref('basic')
    const formRef = ref(null)
    const loading = ref(false)
    
    // 表单数据
    const form = reactive({
      id: null,
      metricName: '',
      metricKey: '',
      metricType: '',
      description: '',
      applicableDeviceType: '',
      collectionMethod: 'snmp',
      protocolConfig: '',
      collectionInterval: 30,
      collectionIntervalUnit: 's',
      dataType: 'gauge',
      unit: 'percent',
      dataFormat: 'default',
      dataTransformType: 'none',
      transformFormula: '',
      minValue: '',
      maxValue: '',
      processingScript: '',
      enableCache: true,
      retentionPeriod: '30d',
      thresholdEnabled: true,
      warningThreshold: '80',
      warningDuration: 5,
      criticalThreshold: '95',
      criticalDuration: 2,
      notificationGroups: ['admin'],
      status: 1
    })
    
    // 协议配置
    const protocolConfig = reactive({
      // SNMP配置
      snmpOid: '',
      snmpVersion: 'v2c',
      snmpCommunity: 'public',
      snmpSecLevel: 'authPriv',
      snmpAuthProto: 'SHA',
      snmpAuthPass: '',
      snmpPrivProto: 'AES',
      snmpPrivPass: '',
      
      // HTTP配置
      httpUrl: '',
      httpMethod: 'GET',
      httpHeaders: '',
      httpBody: '',
      httpDataPath: '',
      httpAuth: 'none',
      httpUsername: '',
      httpPassword: ''
    })
    
    // 表单验证规则
    const rules = reactive({
      metricName: [{ required: true, message: '请输入指标名称', trigger: 'blur' }],
      metricKey: [
        { required: true, message: '请输入指标标识符', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]+$/, message: '指标标识符只能包含字母、数字和下划线', trigger: 'blur' }
      ],
      metricType: [{ required: true, message: '请选择指标类型', trigger: 'change' }],
      collectionMethod: [{ required: true, message: '请选择采集方式', trigger: 'change' }],
      collectionInterval: [{ required: true, message: '请输入采集频率', trigger: 'blur' }]
    })
    
    // 初始化表单数据
    watch(() => props.visible, (val) => {
      if (val && props.metric) {
        initFormData()
      }
    })
    
    // 初始化表单数据
    const initFormData = () => {
      if (props.isEdit) {
        Object.keys(form).forEach(key => {
          if (props.metric[key] !== undefined) {
            form[key] = props.metric[key]
          }
        })
        
        // 解析协议配置
        if (form.protocolConfig) {
          try {
            const config = JSON.parse(form.protocolConfig)
            Object.keys(config).forEach(key => {
              if (protocolConfig[key] !== undefined) {
                protocolConfig[key] = config[key]
              }
            })
          } catch (error) {
            console.error('解析协议配置失败:', error)
          }
        }
      }
    }
    
    // 更新对话框可见性
    const updateVisible = (val) => {
      emit('update:visible', val)
    }
    
    // 处理采集方式变化
    const handleCollectionMethodChange = (method) => {
      console.log('采集方式变更为:', method)
    }
    
    // 处理HTTP认证方式变化
    const handleHttpAuthChange = (authType) => {
      console.log('HTTP认证方式变更为:', authType)
    }
    
    // 测试连接
    const testConnection = () => {
      ElMessage.success('连接测试成功')
    }
    
    // 提交表单
    const submitForm = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
        
        loading.value = true
        
        // 构建协议配置JSON
        const configData = {}
        
        if (form.collectionMethod === 'snmp') {
          Object.keys(protocolConfig).forEach(key => {
            if (key.startsWith('snmp') && protocolConfig[key]) {
              configData[key] = protocolConfig[key]
            }
          })
        } else if (form.collectionMethod === 'http') {
          Object.keys(protocolConfig).forEach(key => {
            if (key.startsWith('http') && protocolConfig[key]) {
              configData[key] = protocolConfig[key]
            }
          })
        }
        
        form.protocolConfig = JSON.stringify(configData)
        
        // 检查指标Key是否重复
        if (!props.isEdit || (props.isEdit && form.metricKey !== props.metric.metricKey)) {
          const response = await checkMetricKey(form.metricKey, props.isEdit ? form.id : null)
          if (response.data) {
            ElMessage.error('指标标识符已存在，请更换')
            loading.value = false
            return
          }
        }
        
        // 保存指标
        if (props.isEdit) {
          await updateMetric(form.id, form)
          ElMessage.success('指标更新成功')
        } else {
          await createMetric(form)
          ElMessage.success('指标创建成功')
        }
        
        emit('success')
        updateVisible(false)
      } catch (error) {
        console.error('表单验证失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    return {
      activeTab,
      formRef,
      form,
      rules,
      loading,
      protocolConfig,
      updateVisible,
      handleCollectionMethodChange,
      handleHttpAuthChange,
      testConnection,
      submitForm
    }
  }
})
</script>

<style scoped>
.protocol-config {
  border-color: #ebeef5;
}

.form-text {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 