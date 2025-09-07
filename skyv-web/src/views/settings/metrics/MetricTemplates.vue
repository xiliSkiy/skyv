<template>
  <div class="metric-templates">
    <div class="row">
      <!-- 左侧模板列表 -->
      <div class="col-md-4">
        <el-card class="template-list-card mb-4">
          <template #header>
            <div class="d-flex justify-content-between align-items-center">
              <span><i class="el-icon-s-grid me-2"></i> 模板列表</span>
              <div>
                <el-button type="primary" size="small" @click="handleAdd">
                  <i class="el-icon-plus me-1"></i> 新建模板
                </el-button>
              </div>
            </div>
          </template>
          
          <!-- 搜索框 -->
          <div class="mb-3">
            <el-input 
              v-model="searchInput" 
              placeholder="搜索模板..." 
              size="small" 
              clearable
              @input="filterTemplates">
              <template #suffix>
                <el-icon class="el-input__icon"><el-icon-search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <!-- 模板卡片列表 -->
          <div class="template-list" v-loading="loading">
            <template v-if="templateList.length > 0">
              <div 
                v-for="template in filteredTemplates" 
                :key="template.id"
                :class="['template-card', activeTemplate && activeTemplate.id === template.id ? 'active' : '']"
                @click="handleSelectTemplate(template)">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <h6 class="mb-0">{{ template.templateName }}</h6>
                  <el-tag size="small" :type="template.isSystem ? 'primary' : 'success'">
                    {{ template.isSystem ? '系统模板' : '自定义模板' }}
                  </el-tag>
                </div>
                <div class="small text-muted mb-2">{{ template.description || '暂无描述' }}</div>
                <div class="d-flex justify-content-between align-items-center">
                  <el-tag size="small" type="info">{{ template.metricCount || 0 }}个指标</el-tag>
                  <small class="text-muted">使用次数: {{ template.usageCount || 0 }}</small>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无模板数据"></el-empty>
          </div>
        </el-card>
      </div>
      
      <!-- 右侧模板详情和应用记录 -->
      <div class="col-md-8">
        <template v-if="activeTemplate">
          <!-- 模板详情卡片 -->
          <el-card class="mb-4">
            <template #header>
              <div class="d-flex justify-content-between align-items-center">
                <span><i class="el-icon-info me-2"></i> 模板详情</span>
                <div>
                  <el-button 
                    type="primary" 
                    plain 
                    size="small" 
                    class="me-2" 
                    @click="handleEdit(activeTemplate)"
                    :disabled="activeTemplate.isSystem">
                    <i class="el-icon-edit me-1"></i> 编辑模板
                  </el-button>
                  <el-button 
                    type="primary" 
                    size="small"
                    @click="handleApplyTemplate(activeTemplate)">
                    <i class="el-icon-check me-1"></i> 应用模板
                  </el-button>
                </div>
              </div>
            </template>
            
            <div class="template-detail">
              <h5 class="mb-3">{{ activeTemplate.templateName }}</h5>
              
              <div class="row mb-4">
                <div class="col-md-6">
                  <div class="mb-2">
                    <strong>模板类型:</strong> {{ activeTemplate.isSystem ? '系统模板' : '自定义模板' }}
                  </div>
                  <div class="mb-2">
                    <strong>适用设备:</strong> {{ formatDeviceType(activeTemplate.deviceType) }}
                  </div>
                  <div class="mb-2">
                    <strong>指标数量:</strong> {{ activeTemplate.metricCount || 0 }}个
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="mb-2">
                    <strong>创建时间:</strong> {{ formatDate(activeTemplate.createdAt) }}
                  </div>
                  <div class="mb-2">
                    <strong>最后更新:</strong> {{ formatDate(activeTemplate.updatedAt) }}
                  </div>
                  <div class="mb-2">
                    <strong>使用次数:</strong> {{ activeTemplate.usageCount || 0 }}
                  </div>
                </div>
              </div>

              <div class="mb-4">
                <h6 class="mb-2">模板描述</h6>
                <p class="text-muted">{{ activeTemplate.description || '暂无描述' }}</p>
              </div>

              <div class="mb-4">
                <h6 class="mb-3">包含指标列表</h6>
                <el-table 
                  :data="activeTemplate.metrics || []"
                  style="width: 100%"
                  border 
                  size="small">
                  <el-table-column prop="metricName" label="指标名称" width="180"></el-table-column>
                  <el-table-column prop="metricType" label="类型" width="120">
                    <template #default="scope">
                      <el-tag size="small">{{ formatMetricType(scope.row.metricType) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="collectionMethod" label="采集方式" width="120"></el-table-column>
                  <el-table-column prop="description" label="描述"></el-table-column>
                </el-table>
                
                <div v-if="!activeTemplate.metrics || activeTemplate.metrics.length === 0" class="text-center py-4">
                  <el-empty description="暂无指标数据"></el-empty>
                </div>
              </div>
              
              <el-alert
                type="info"
                show-icon
                :closable="false">
                <i class="el-icon-info me-2"></i>
                应用此模板将添加或更新上述所有指标配置。您可以在应用后根据需要调整各指标的具体参数。
              </el-alert>
            </div>
          </el-card>
          
          <!-- 模板应用记录卡片 -->
          <el-card>
            <template #header>
              <div>
                <i class="el-icon-time me-2"></i> 模板应用记录
              </div>
            </template>
            
            <el-table
              :data="applicationHistory"
              style="width: 100%"
              size="small">
              <el-table-column prop="applyTime" label="应用时间" width="180">
                <template #default="scope">
                  {{ formatDateTime(scope.row.applyTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="templateName" label="模板名称" width="180"></el-table-column>
              <el-table-column prop="applyUser" label="应用用户" width="120"></el-table-column>
              <el-table-column prop="affectedCount" label="影响指标数" width="120"></el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag 
                    :type="scope.row.status === 'SUCCESS' ? 'success' : (scope.row.status === 'PARTIAL' ? 'warning' : 'danger')">
                    {{ formatStatus(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            
            <div v-if="!applicationHistory || applicationHistory.length === 0" class="text-center py-4">
              <el-empty description="暂无应用记录"></el-empty>
            </div>
          </el-card>
        </template>
        
        <el-empty v-else description="请选择模板查看详情"></el-empty>
      </div>
    </div>

    <!-- 新增/编辑模板对话框 -->
    <el-dialog 
      :title="dialog.title" 
      v-model="dialog.visible"
      width="700px">
      <el-form ref="templateForm_ref" :model="templateForm" :rules="rules" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input 
            v-model="templateForm.templateName" 
            placeholder="请输入模板名称"
            :disabled="dialog.loading">
          </el-input>
        </el-form-item>
        
        <el-form-item label="模板描述" prop="description">
          <el-input 
            v-model="templateForm.description" 
            type="textarea" 
            rows="3"
            placeholder="描述该模板的用途和适用场景..."></el-input>
        </el-form-item>
        
        <el-form-item label="适用设备" prop="deviceType">
          <el-select v-model="templateForm.deviceType" placeholder="请选择设备类型" style="width: 100%;">
            <el-option label="服务器" value="server"></el-option>
            <el-option label="数据库服务器" value="database"></el-option>
            <el-option label="中间件服务器" value="middleware"></el-option>
            <el-option label="网络设备" value="network"></el-option>
            <el-option label="摄像机" value="camera"></el-option>
            <el-option label="传感器" value="sensor"></el-option>
            <el-option label="自定义设备" value="custom"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="指标类型" prop="metricType">
          <el-select v-model="templateForm.metricType" placeholder="请选择指标类型" style="width: 100%;">
            <el-option label="系统性能" value="system_perf"></el-option>
            <el-option label="系统状态" value="system_status"></el-option>
            <el-option label="视频状态" value="video_status"></el-option>
            <el-option label="视频分析" value="video_analysis"></el-option>
            <el-option label="传感器数据" value="sensor_data"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="选择指标" prop="metricIds">
          <el-alert
            type="info"
            :closable="false"
            class="mb-3">
            选择要包含在模板中的指标项。您可以从现有指标中选择，或在应用模板时创建新指标。
          </el-alert>
          
          <div class="d-flex justify-content-between mb-3">
            <el-input 
              v-model="metricSearchInput"
              placeholder="搜索指标..."
              style="width: 250px"
              size="small"
              clearable>
              <template #suffix>
                <el-icon class="el-input__icon"><el-icon-search /></el-icon>
              </template>
            </el-input>
            
            <el-select 
              v-model="metricFilter" 
              placeholder="全部指标" 
              size="small"
              @change="filterAvailableMetrics">
              <el-option label="全部指标" value=""></el-option>
              <el-option label="服务器指标" value="server"></el-option>
              <el-option label="中间件指标" value="middleware"></el-option>
              <el-option label="数据库指标" value="database"></el-option>
              <el-option label="摄像机指标" value="camera"></el-option>
              <el-option label="网络指标" value="network"></el-option>
            </el-select>
          </div>
          
          <el-table
            ref="metricsTable"
            :data="filteredAvailableMetrics"
            style="width: 100%"
            border
            height="300"
            size="small"
            @selection-change="handleMetricsSelection">
            <el-table-column
              type="selection"
              width="55">
            </el-table-column>
            <el-table-column prop="metricName" label="指标名称" width="180"></el-table-column>
            <el-table-column prop="metricType" label="类型" width="120">
              <template #default="scope">
                <el-tag size="small">{{ formatMetricType(scope.row.metricType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="collectionMethod" label="采集方式" width="120"></el-table-column>
            <el-table-column prop="description" label="描述" :show-overflow-tooltip="true"></el-table-column>
          </el-table>
          
          <div class="d-flex justify-content-between mt-2">
            <span class="text-muted small">已选择 {{selectedMetricIds.length}} 项</span>
            <el-button 
              type="text" 
              size="small" 
              @click="clearMetricsSelection">
              清空选择
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="dialog.loading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 应用模板对话框 -->
    <el-dialog 
      title="应用模板" 
      v-model="applyDialog.visible"
      width="600px">
      <el-form :model="applyForm" :rules="applyRules" ref="applyForm_ref" label-width="100px">
        <div class="alert alert-info mb-4">
          <i class="el-icon-info me-2"></i>
          您将要应用 <strong>{{ activeTemplate?.templateName }}</strong> 模板，包含 
          <strong>{{ activeTemplate?.metricCount || 0 }}</strong> 个指标配置。
          请选择要应用的设备。
        </div>
        
        <el-form-item label="应用方式" prop="applyMode">
          <el-radio-group v-model="applyForm.applyMode">
            <el-radio :label="'selected'">选择设备</el-radio>
            <el-radio :label="'all'">全部适用设备</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="选择设备" prop="deviceIds" v-if="applyForm.applyMode === 'selected'">
          <el-select 
            v-model="applyForm.deviceIds" 
            multiple 
            filterable 
            placeholder="请选择设备" 
            style="width: 100%;">
            <el-option 
              v-for="device in filteredDevices" 
              :key="device.id" 
              :label="device.name" 
              :value="device.id">
              <span>{{ device.name }}</span>
              <span class="text-muted ms-2">({{ device.type }})</span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="更新策略" prop="updateStrategy">
          <el-radio-group v-model="applyForm.updateStrategy">
            <el-radio :label="'merge'">合并（保留现有配置）</el-radio>
            <el-radio :label="'overwrite'">覆盖（替换现有配置）</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="applyDialog.visible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmApplyTemplate" 
            :loading="applyDialog.loading">
            应用模板
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getMetricTemplates,
  getMetricTemplateById,
  createMetricTemplate,
  updateMetricTemplate,
  deleteMetricTemplate,
  applyMetricTemplate,
  getTemplateApplicationHistory
} from '@/api/metrics'
import { getAllMetrics } from '@/api/metrics'
import { getAllDevices } from '@/api/device'
import { formatDateTime as formatDateUtil } from '@/utils/date'

export default {
  name: 'MetricTemplates',
  setup() {
    const router = useRouter();

    // 数据状态
    const loading = ref(false);
    const templateList = ref([]);
    const activeTemplate = ref(null);
    const searchInput = ref('');
    const metricSearchInput = ref('');
    const metricFilter = ref('');
    const availableMetrics = ref([]);
    const selectedMetricIds = ref([]);
    const applicationHistory = ref([]);
    const devices = ref([]);

    // 计算属性
    const filteredTemplates = computed(() => {
      if (!searchInput.value) return templateList.value;
      
      const searchTerm = searchInput.value.toLowerCase();
      return templateList.value.filter(template => 
        template.templateName.toLowerCase().includes(searchTerm) ||
        (template.description && template.description.toLowerCase().includes(searchTerm))
      );
    });

    const filteredAvailableMetrics = computed(() => {
      let result = availableMetrics.value;
      
      if (metricSearchInput.value) {
        const searchTerm = metricSearchInput.value.toLowerCase();
        result = result.filter(metric => 
          metric.metricName.toLowerCase().includes(searchTerm) ||
          (metric.description && metric.description.toLowerCase().includes(searchTerm))
        );
      }
      
      if (metricFilter.value) {
        result = result.filter(metric => 
          metric.applicableDeviceType === metricFilter.value
        );
      }
      
      return result;
    });

    const filteredDevices = computed(() => {
      if (!activeTemplate.value) return [];
      
      // 根据模板的deviceType筛选设备
      return devices.value.filter(device => 
        !activeTemplate.value.deviceType || 
        device.type === activeTemplate.value.deviceType
      );
    });

    // 对话框状态
    const dialog = reactive({
      visible: false,
      title: '',
      loading: false
    });

    const applyDialog = reactive({
      visible: false,
      loading: false
    });

    // 表单数据
    const templateForm = reactive({
      id: null,
      templateName: '',
      description: '',
      deviceType: 'server',
      metricType: 'system_perf', // 添加默认指标类型
      metricIds: []
    });

    const applyForm = reactive({
      deviceIds: [],
      applyMode: 'selected',
      updateStrategy: 'merge'
    });

    // 表单规则
    const applyRules = {
      deviceIds: [
        { required: true, message: '请选择设备', trigger: 'change' }
      ]
    };

    const rules = {
      templateName: [
        { required: true, message: '请输入模板名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      deviceType: [
        { required: true, message: '请选择适用设备类型', trigger: 'change' }
      ],
      metricType: [
        { required: true, message: '请选择指标类型', trigger: 'change' }
      ],
      metricIds: [
        { required: true, message: '请至少选择一个指标', trigger: 'change', 
          validator: (rule, value, callback) => {
            if (selectedMetricIds.value.length === 0) {
              callback(new Error('请至少选择一个指标'));
            } else {
              callback();
            }
          }
        }
      ]
    };

    // 表单引用
    const templateForm_ref = ref(null);
    const applyForm_ref = ref(null);
    const metricsTable = ref(null);

    // 获取模板列表
    const fetchTemplates = async () => {
      loading.value = true;
      try {
        const res = await getMetricTemplates({
          page: 0,
          size: 100
        });
        templateList.value = res.data.content || [];
        
        // 如果有activeTemplate，刷新它的数据
        if (activeTemplate.value) {
          const found = templateList.value.find(t => t.id === activeTemplate.value.id);
          if (found) {
            fetchTemplateDetail(found.id);
          } else {
            activeTemplate.value = null;
          }
        }
      } catch (error) {
        console.error('获取模板列表失败', error);
        ElMessage.error('获取模板列表失败');
      } finally {
        loading.value = false;
      }
    };

    // 获取模板详情
    const fetchTemplateDetail = async (templateId) => {
      try {
        const res = await getMetricTemplateById(templateId);
        activeTemplate.value = res.data;
        
        // 获取应用历史
        fetchApplicationHistory(templateId);
      } catch (error) {
        console.error('获取模板详情失败', error);
        ElMessage.error('获取模板详情失败');
      }
    };

    // 获取应用历史
    const fetchApplicationHistory = async (templateId) => {
      try {
        const res = await getTemplateApplicationHistory(templateId);
        applicationHistory.value = res.data || [];
      } catch (error) {
        console.error('获取应用历史失败', error);
        applicationHistory.value = [];
      }
    };

    // 获取所有指标
    const fetchAllMetrics = async () => {
      try {
        const res = await getAllMetrics();
        availableMetrics.value = res.data || [];
      } catch (error) {
        console.error('获取指标列表失败', error);
        ElMessage.error('获取指标列表失败');
      }
    };

    // 获取所有设备
    const fetchAllDevices = async () => {
      try {
        const res = await getAllDevices();
        devices.value = res.data || [];
      } catch (error) {
        console.error('获取设备列表失败', error);
        ElMessage.error('获取设备列表失败');
      }
    };

    // 为了保持与MetricsView中调用的一致性，添加fetchData方法
    const fetchData = () => {
      return fetchTemplates();
    };

    // 选择模板
    const handleSelectTemplate = (template) => {
      fetchTemplateDetail(template.id);
    };

    // 筛选模板
    const filterTemplates = () => {
      // 由计算属性filteredTemplates处理
    };

    // 筛选可用指标
    const filterAvailableMetrics = () => {
      // 由计算属性filteredAvailableMetrics处理
    };

    // 处理指标选择变化
    const handleMetricSelectionChange = (selection) => {
      selectedMetricIds.value = selection.map(item => item.id);
      templateForm.metricIds = selectedMetricIds.value;
    }

    // 改正函数名称匹配表格中的引用
    const handleMetricsSelection = (selection) => {
      selectedMetricIds.value = selection.map(item => item.id);
      templateForm.metricIds = selectedMetricIds.value;
    }

    // 清除指标选择
    const clearMetricsSelection = () => {
      if (metricsTable.value) {
        metricsTable.value.clearSelection();
      }
      selectedMetricIds.value = [];
      templateForm.metricIds = [];
    }

    // 新增模板
    const handleAdd = () => {
      dialog.title = '新增模板';
      dialog.visible = true;
      
      // 重置表单 - 使用更明确的方式重置
      templateForm.id = null;
      templateForm.templateName = '';
      templateForm.description = '';
      templateForm.deviceType = 'server';
      templateForm.metricType = 'system_perf';
      templateForm.metricIds = [];
      
      // 加载可用指标
      fetchAllMetrics();
      
      // 清除之前的选择
      selectedMetricIds.value = [];
      metricSearchInput.value = '';
      metricFilter.value = '';
      
      // 在下一个tick，表格会被重置，不需要手动清除selection
    };

    // 编辑模板
    const handleEdit = (template) => {
      if (template.isSystem) {
        ElMessage.warning('系统模板不允许编辑');
        return;
      }
      
      dialog.title = '编辑模板';
      dialog.visible = true;
      dialog.loading = true;
      
      // 加载可用指标
      fetchAllMetrics().then(() => {
        // 获取模板详情
        getMetricTemplateById(template.id).then(res => {
          const templateData = res.data;
          
          // 设置表单数据
          templateForm.id = templateData.id;
          templateForm.templateName = templateData.templateName;
          templateForm.description = templateData.description || '';
          templateForm.deviceType = templateData.deviceType || 'server';
          
          // 设置已选指标
          const metricIds = (templateData.metrics || []).map(m => m.id);
          templateForm.metricIds = metricIds;
          selectedMetricIds.value = metricIds;
          
          // 在下一个tick后，设置表格选中状态
          setTimeout(() => {
            if (metricsTable.value) {
              availableMetrics.value.forEach(row => {
                if (metricIds.includes(row.id)) {
                  metricsTable.value.toggleRowSelection(row, true);
                }
              });
            }
            dialog.loading = false;
          }, 100);
        }).catch(error => {
          console.error('获取模板详情失败', error);
          ElMessage.error('获取模板详情失败');
          dialog.loading = false;
        });
      });
    };

    // 应用模板
    const handleApplyTemplate = (template) => {
      if (!template) return;
      
      applyDialog.visible = true;
      applyForm.deviceIds = [];
      
      // 加载设备列表
      fetchAllDevices();
    };

    // 确认应用模板
    const confirmApplyTemplate = async () => {
      if (!activeTemplate.value) return;
      
      if (applyForm.deviceIds.length === 0) {
        ElMessage.warning('请至少选择一个设备');
        return;
      }
      
      applyDialog.loading = true;
      try {
        await applyMetricTemplate(activeTemplate.value.id, {
          deviceIds: applyForm.deviceIds
        });
        
        ElMessage.success('模板应用成功');
        applyDialog.visible = false;
        
        // 刷新应用历史
        fetchApplicationHistory(activeTemplate.value.id);
      } catch (error) {
        console.error('应用模板失败', error);
        ElMessage.error('应用模板失败: ' + (error.message || '未知错误'));
      } finally {
        applyDialog.loading = false;
      }
    };

    // 提交表单
    const submitForm = () => {
      if (!templateForm_ref.value) {
        console.error('表单引用获取失败');
        return;
      }
      
      templateForm_ref.value.validate(async (valid) => {
        if (!valid) return;
        
        dialog.loading = true;
        try {
          const isEdit = !!templateForm.id;
          
          const formData = {
            templateName: templateForm.templateName,
            description: templateForm.description,
            deviceType: templateForm.deviceType,
            metricType: templateForm.metricType,
            metricIds: selectedMetricIds.value
          };
          
          if (isEdit) {
            await updateMetricTemplate(templateForm.id, formData);
            ElMessage.success('编辑模板成功');
          } else {
            await createMetricTemplate(formData);
            ElMessage.success('创建模板成功');
          }
          
          dialog.visible = false;
          fetchTemplates();
        } catch (error) {
          console.error('保存模板失败', error);
          ElMessage.error('保存模板失败: ' + (error.message || '未知错误'));
        } finally {
          dialog.loading = false;
        }
      });
    };

    // 跳转到创建指标页面
    const navigateToCreateMetric = () => {
      router.push('/settings/metrics');
      ElMessage.info('请在指标管理页面创建新指标');
    };

    // 格式化函数
    const formatDeviceType = (type) => {
      const typeMap = {
        'server': '服务器',
        'database': '数据库服务器',
        'middleware': '中间件服务器',
        'network': '网络设备',
        'camera': '摄像机',
        'sensor': '传感器',
        'custom': '自定义设备'
      };
      return typeMap[type] || type;
    };

    const formatMetricType = (type) => {
      const typeMap = {
        'system_perf': '系统性能',
        'system_status': '系统状态',
        'video_status': '视频状态',
        'video_analysis': '视频分析',
        'sensor_data': '传感器数据'
      };
      return typeMap[type] || type;
    };

    const formatStatus = (status) => {
      const statusMap = {
        'SUCCESS': '成功',
        'PARTIAL': '部分成功',
        'FAILED': '失败'
      };
      return statusMap[status] || status;
    };

    const formatDate = (date) => {
      if (!date) return '未知';
      try {
        if (typeof date === 'string') {
          return formatDateUtil(new Date(date), 'YYYY-MM-DD');
        }
        return formatDateUtil(date, 'YYYY-MM-DD');
      } catch (e) {
        console.error('日期格式化错误', e);
        return '未知';
      }
    };

    const formatDateTime = (date) => {
      if (!date) return '未知';
      try {
        if (typeof date === 'string') {
          // 避免递归调用，直接使用导入的formatDateTime函数，添加别名
          return formatDateUtil(new Date(date), 'YYYY-MM-DD HH:mm:ss');
        }
        return formatDateUtil(date, 'YYYY-MM-DD HH:mm:ss');
      } catch (e) {
        console.error('日期时间格式化错误', e);
        return '未知';
      }
    };

    // 初始化
    onMounted(() => {
      fetchTemplates();
    });

    return {
      loading,
      templateList,
      activeTemplate,
      searchInput,
      metricSearchInput,
      metricFilter,
      availableMetrics,
      selectedMetricIds,
      applicationHistory,
      devices,
      filteredTemplates,
      filteredAvailableMetrics,
      filteredDevices,
      dialog,
      applyDialog,
      templateForm,
      applyForm,
      applyRules,
      rules,
      templateForm_ref,
      applyForm_ref,
      metricsTable,
      fetchTemplates,
      fetchTemplateDetail,
      fetchApplicationHistory,
      fetchAllMetrics,
      fetchAllDevices,
      handleSelectTemplate,
      filterTemplates,
      filterAvailableMetrics,
      handleMetricSelectionChange,
      handleMetricsSelection,
      clearMetricsSelection,
      handleAdd,
      handleEdit,
      handleApplyTemplate,
      confirmApplyTemplate,
      submitForm,
      navigateToCreateMetric,
      formatDeviceType,
      formatMetricType,
      formatStatus,
      formatDate,
      formatDateTime,
      fetchData
    };
  }
};
</script>

<style scoped>
.metric-templates {
  padding: 10px;
}

.row {
  display: flex;
  flex-wrap: wrap;
  margin-right: -10px;
  margin-left: -10px;
}

.col-md-4 {
  flex: 0 0 33.333333%;
  max-width: 33.333333%;
  padding-right: 10px;
  padding-left: 10px;
  position: relative;
  width: 100%;
  box-sizing: border-box;
}

.col-md-6 {
  flex: 0 0 50%;
  max-width: 50%;
  padding-right: 10px;
  padding-left: 10px;
  position: relative;
  width: 100%;
  box-sizing: border-box;
}

.col-md-8 {
  flex: 0 0 66.666667%;
  max-width: 66.666667%;
  padding-right: 10px;
  padding-left: 10px;
  position: relative;
  width: 100%;
  box-sizing: border-box;
}

.template-list-card {
  height: 100%;
}

.template-list {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 5px;
}

.template-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  transition: all 0.3s;
  cursor: pointer;
}

.template-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  border-color: #409EFF;
}

.template-card.active {
  border-color: #409EFF;
  background-color: rgba(64, 158, 255, 0.05);
}

.mb-4 {
  margin-bottom: 20px;
}

.mb-3 {
  margin-bottom: 15px;
}

.mb-2 {
  margin-bottom: 10px;
}

.me-2 {
  margin-right: 10px;
}

.d-flex {
  display: flex;
}

.justify-content-between {
  justify-content: space-between;
}

.align-items-center {
  align-items: center;
}

.text-muted {
  color: #6c757d;
}

.text-center {
  text-align: center;
}

.py-4 {
  padding-top: 20px;
  padding-bottom: 20px;
}

h5 {
  font-size: 1.25rem;
  font-weight: 600;
}

h6 {
  font-size: 1rem;
  font-weight: 600;
}

.small {
  font-size: 0.875rem;
}

.alert {
  position: relative;
  padding: 0.75rem 1.25rem;
  margin-bottom: 1rem;
  border: 1px solid transparent;
  border-radius: 0.25rem;
}

.alert-info {
  color: #0c5460;
  background-color: #d1ecf1;
  border-color: #bee5eb;
}

.ms-2 {
  margin-left: 10px;
}

/* 解决el-card在el-dialog中的样式问题 */
:deep(.el-card__header) {
  padding: 15px 20px;
  font-weight: 600;
}
</style> 