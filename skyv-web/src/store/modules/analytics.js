import { defineStore } from 'pinia'
import { 
  getAnalyticsOverview,
  getTrafficTrends,
  getBehaviorPatterns,
  getAbnormalEvents,
  getDeviceFailurePredictions,
  getHeatmapAnalysis,
  getDataDiscoveries,
  getDeviceAnalytics,
  submitAIQuery,
  createCustomAnalysis,
  exportAnalyticsReport,
  setAbnormalAlert,
  createMaintenanceOrder
} from '@/api/analytics'

// 导入Mock数据（开发阶段使用）
import {
  overviewData,
  trafficTrendsData,
  behaviorPatternsData,
  abnormalEventsData,
  deviceFailurePredictionsData,
  heatmapData,
  dataDiscoveriesData,
  deviceAnalyticsData,
  aiAssistantData
} from '@/api/mock/analytics'

export const useAnalyticsStore = defineStore('analytics', {
  state: () => ({
    // 加载状态
    loading: {
      overview: false,
      trafficTrends: false,
      behaviorPatterns: false,
      abnormalEvents: false,
      deviceFailure: false,
      heatmap: false,
      dataDiscoveries: false,
      deviceAnalytics: false
    },
    // 数据
    overview: null,
    trafficTrends: null,
    behaviorPatterns: null,
    abnormalEvents: null,
    deviceFailure: null,
    heatmap: null,
    dataDiscoveries: null,
    deviceAnalytics: null,
    // AI助手
    aiAssistant: {
      conversations: [],
      loading: false,
      suggestions: [
        "分析最近一周的异常事件趋势",
        "预测下周人流量峰值时段",
        "比较各区域报警频率",
        "分析设备故障原因和预防措施",
        "识别高风险区域并提供安全建议"
      ]
    },
    // 筛选条件
    filters: {
      timeRange: '最近7天',
      deviceGroup: '所有设备',
      analysisType: '全部'
    }
  }),
  
  actions: {
    // 获取概览数据
    async fetchOverview() {
      this.loading.overview = true
      try {
        // 实际项目中使用API请求
        // const { data } = await getAnalyticsOverview()
        // this.overview = data
        
        // 使用Mock数据
        this.overview = overviewData
      } catch (error) {
        console.error('获取概览数据失败:', error)
      } finally {
        this.loading.overview = false
      }
    },
    
    // 获取人流量趋势与预测数据
    async fetchTrafficTrends(params = {}) {
      this.loading.trafficTrends = true
      try {
        // 实际项目中使用API请求
        // const { data } = await getTrafficTrends(params)
        // this.trafficTrends = data
        
        // 使用Mock数据
        this.trafficTrends = trafficTrendsData
      } catch (error) {
        console.error('获取人流量趋势与预测数据失败:', error)
      } finally {
        this.loading.trafficTrends = false
      }
    },
    
    // 获取人员行为模式分析数据
    async fetchBehaviorPatterns(params = {}) {
      this.loading.behaviorPatterns = true
      try {
        // 实际项目中使用API请求
        // const { data } = await getBehaviorPatterns(params)
        // this.behaviorPatterns = data
        
        // 使用Mock数据
        this.behaviorPatterns = behaviorPatternsData
      } catch (error) {
        console.error('获取人员行为模式分析数据失败:', error)
      } finally {
        this.loading.behaviorPatterns = false
      }
    },
    
    // 获取异常事件智能分析数据
    async fetchAbnormalEvents(params = {}) {
      this.loading.abnormalEvents = true
      try {
        // 实际项目中使用API请求
        // const { data } = await getAbnormalEvents(params)
        // this.abnormalEvents = data
        
        // 使用Mock数据
        // 增强异常事件数据，添加原因分析和处理建议
        this.abnormalEvents = {
          ...abnormalEventsData,
          analysis: {
            reasons: [
              {
                title: "人员聚集异常",
                description: "检测到多个区域出现人员密集聚集现象，可能与工作时间安排有关，建议错峰安排工作时间。",
                eventCount: 15,
                confidence: 87,
                impact: "中等"
              },
              {
                title: "未授权访问",
                description: "检测到多起未授权人员进入限制区域事件，可能与门禁系统故障或安全意识不足有关。",
                eventCount: 8,
                confidence: 92,
                impact: "高"
              },
              {
                title: "异常行为模式",
                description: "识别到部分区域出现可疑行为模式，如重复徘徊、异常停留等，建议加强巡逻。",
                eventCount: 6,
                confidence: 78,
                impact: "中等"
              }
            ]
          },
          recommendations: [
            {
              title: "升级门禁系统",
              description: "建议升级仓库区域门禁系统，增加生物识别功能，减少未授权访问事件。",
              priority: "高",
              id: "rec-001"
            },
            {
              title: "优化人员流动路径",
              description: "重新规划办公区人员流动路径，避免人员聚集，提高通行效率。",
              priority: "中",
              id: "rec-002"
            },
            {
              title: "增加巡逻频次",
              description: "在异常行为高发区域增加安保巡逻频次，尤其是在14:00-16:00时段。",
              priority: "中",
              id: "rec-003"
            }
          ]
        }
      } catch (error) {
        console.error('获取异常事件智能分析数据失败:', error)
      } finally {
        this.loading.abnormalEvents = false
      }
    },
    
    // 获取设备故障预测数据
    async fetchDeviceFailure(params = {}) {
      this.loading.deviceFailure = true
      try {
        // 实际项目中使用API请求
        // const { data } = await getDeviceFailurePredictions(params)
        // this.deviceFailure = data
        
        // 使用Mock数据
        // 增强设备故障预测数据，添加健康评分和预测准确率
        this.deviceFailure = {
          ...deviceFailurePredictionsData,
          healthScore: 92,
          accuracy: 95,
          warning: {
            ...deviceFailurePredictionsData.warning,
            probability: 87,
            impact: "监控盲区增加，可能导致安全隐患"
          }
        }
      } catch (error) {
        console.error('获取设备故障预测数据失败:', error)
      } finally {
        this.loading.deviceFailure = false
      }
    },
    
    // 获取热力图分析数据
    async fetchHeatmap(params = {}) {
      this.loading.heatmap = true
      try {
        // 实际项目中使用API请求
        // const { data } = await getHeatmapAnalysis(params)
        // this.heatmap = data
        
        // 使用Mock数据
        this.heatmap = heatmapData
      } catch (error) {
        console.error('获取热力图分析数据失败:', error)
      } finally {
        this.loading.heatmap = false
      }
    },
    
    // 获取智能数据探索结果
    async fetchDataDiscoveries(params = {}) {
      this.loading.dataDiscoveries = true
      try {
        // 实际项目中使用API请求
        // const { data } = await getDataDiscoveries(params)
        // this.dataDiscoveries = data
        
        // 使用Mock数据
        this.dataDiscoveries = dataDiscoveriesData
      } catch (error) {
        console.error('获取智能数据探索结果失败:', error)
      } finally {
        this.loading.dataDiscoveries = false
      }
    },
    
    // 获取设备分析明细数据
    async fetchDeviceAnalytics(params = {}) {
      this.loading.deviceAnalytics = true
      try {
        // 实际项目中使用API请求
        // const { data } = await getDeviceAnalytics(params)
        // this.deviceAnalytics = data
        
        // 使用Mock数据
        this.deviceAnalytics = deviceAnalyticsData
      } catch (error) {
        console.error('获取设备分析明细数据失败:', error)
      } finally {
        this.loading.deviceAnalytics = false
      }
    },
    
    // 提交AI分析问题
    async submitAIQuery(query) {
      this.aiAssistant.loading = true
      try {
        // 添加用户问题到对话列表
        const userMessage = {
          id: this.aiAssistant.conversations.length + 1,
          role: 'user',
          content: query
        }
        this.aiAssistant.conversations.push(userMessage)
        
        // 实际项目中使用API请求
        // const { data } = await submitAIQuery({ query })
        // const aiResponse = {
        //   id: this.aiAssistant.conversations.length + 1,
        //   role: 'ai',
        //   content: data
        // }
        
        // 使用Mock数据
        // 模拟API延迟
        await new Promise(resolve => setTimeout(resolve, 1500))
        
        // 增强AI回复，添加更多交互式内容和可视化数据
        let aiResponse
        if (query.includes('异常事件')) {
          aiResponse = {
            id: this.aiAssistant.conversations.length + 1,
            role: 'ai',
            content: {
              text: '分析结果如下:',
              points: [
                '最近一周共检测到37起异常事件，比上周减少12%',
                '主要异常类型为人员聚集(42%)和未授权访问(28%)',
                '发现周三14:00-16:00是异常高发时段',
                '异常事件与人流量峰值存在87%的相关性'
              ],
              chart: true,
              actions: ['查看详细分析', '导出报告', '设置预警']
            }
          }
        } else if (query.includes('人流量')) {
          aiResponse = {
            id: this.aiAssistant.conversations.length + 1,
            role: 'ai',
            content: {
              text: '人流量分析结果:',
              points: [
                '工作日平均人流量为312人次/小时，周末降低至78人次/小时',
                '预测下周人流量将增加15%，主要集中在周二和周四',
                '办公区域人流量分布不均，东区比西区高出32%',
                'AI预测下周峰值时段为周四10:00-11:30，建议做好人员疏导'
              ],
              chart: true,
              actions: ['查看详细分析', '导出报告', '查看热力图']
            }
          }
        } else if (query.includes('设备故障') || query.includes('设备健康')) {
          aiResponse = {
            id: this.aiAssistant.conversations.length + 1,
            role: 'ai',
            content: {
              text: '设备健康分析结果:',
              points: [
                '系统整体健康评分为92分，处于良好状态',
                '预测到3台设备在未来30天内可能发生故障',
                '仓库摄像头-03的存储卡读写错误率上升，建议更换',
                '办公区监控主机散热风扇转速异常，建议清理灰尘',
                '前门摄像头-01电源波动频繁，建议更换电源适配器'
              ],
              chart: true,
              actions: ['查看详细分析', '创建维护计划', '导出报告']
            }
          }
        } else if (query.includes('安全') || query.includes('风险')) {
          aiResponse = {
            id: this.aiAssistant.conversations.length + 1,
            role: 'ai',
            content: {
              text: '安全风险评估结果:',
              points: [
                '系统识别出3个高风险区域和5个中风险区域',
                '仓库后门区域未授权访问事件频发，建议加强监控',
                '办公区走廊在非工作时间存在可疑活动',
                '停车场西北角监控存在盲区，建议调整摄像头角度'
              ],
              chart: true,
              actions: ['查看风险地图', '生成安全报告', '制定防范措施']
            }
          }
        } else {
          aiResponse = {
            id: this.aiAssistant.conversations.length + 1,
            role: 'ai',
            content: `我已分析您的问题"${query}"。请问您需要了解哪方面的具体数据？您可以询问关于人流量趋势、异常事件分析、设备健康状况或安全风险评估等方面的信息。`
          }
        }
        
        this.aiAssistant.conversations.push(aiResponse)
        return Promise.resolve()
      } catch (error) {
        console.error('提交AI分析问题失败:', error)
        return Promise.reject(error)
      } finally {
        this.aiAssistant.loading = false
      }
    },
    
    // 加载AI助手数据
    loadAIAssistant() {
      // 实际项目中可以从API获取初始化数据
      // 这里使用Mock数据
      this.aiAssistant.suggestions = [
        "分析最近一周的异常事件趋势",
        "预测下周人流量峰值时段",
        "比较各区域报警频率",
        "分析设备故障原因和预防措施",
        "识别高风险区域并提供安全建议",
        "生成本月安全态势报告"
      ]
    },
    
    // 清空AI对话
    clearAIConversation() {
      this.aiAssistant.conversations = []
    },
    
    // 更新筛选条件
    updateFilters(filters) {
      this.filters = {
        ...this.filters,
        ...filters
      }
    },
    
    // 创建维护工单
    async createMaintenanceOrder(data) {
      try {
        // 实际项目中使用API请求
        // const response = await createMaintenanceOrder(data)
        // return response
        
        // 模拟API延迟
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 返回模拟响应
        return {
          success: true,
          message: '维护工单创建成功',
          data: {
            id: 'MO-' + Date.now(),
            ...data,
            status: '待处理',
            createdAt: new Date().toISOString()
          }
        }
      } catch (error) {
        console.error('创建维护工单失败:', error)
        return {
          success: false,
          message: '创建维护工单失败: ' + error.message
        }
      }
    },
    
    // 导出报告
    async exportReport(params) {
      try {
        // 实际项目中使用API请求
        // const response = await exportAnalyticsReport(params)
        // return response
        
        // 模拟API延迟
        await new Promise(resolve => setTimeout(resolve, 1500))
        
        // 返回模拟响应
        return {
          success: true,
          message: `报告 "${params.name}" 已导出成功`,
          data: {
            reportId: 'RPT-' + Date.now(),
            reportUrl: '/reports/sample-report.pdf',
            ...params
          }
        }
      } catch (error) {
        console.error('导出报告失败:', error)
        return {
          success: false,
          message: '导出报告失败: ' + error.message
        }
      }
    },
    
    // 设置异常预警
    async setAbnormalAlert(data) {
      try {
        // 实际项目中使用API请求
        // const response = await setAbnormalAlert(data)
        // return response
        
        // 模拟API延迟
        await new Promise(resolve => setTimeout(resolve, 800))
        
        // 返回模拟响应
        return {
          success: true,
          message: `异常事件预警已设置，阈值: ${data.threshold}`,
          data: {
            id: 'ALT-' + Date.now(),
            ...data,
            status: '已启用',
            createdAt: new Date().toISOString()
          }
        }
      } catch (error) {
        console.error('设置异常预警失败:', error)
        return {
          success: false,
          message: '设置异常预警失败: ' + error.message
        }
      }
    },
    
    // 创建自定义分析
    async createCustomAnalysis(data) {
      try {
        // 实际项目中使用API请求
        // const response = await createCustomAnalysis(data)
        // return response
        
        // 模拟API延迟
        await new Promise(resolve => setTimeout(resolve, 1200))
        
        // 返回模拟响应
        return {
          success: true,
          message: `分析任务 "${data.name}" 已创建，正在处理中`,
          data: {
            id: 'ANL-' + Date.now(),
            ...data,
            status: '处理中',
            progress: 0,
            createdAt: new Date().toISOString(),
            estimatedCompletionTime: new Date(Date.now() + 5 * 60 * 1000).toISOString() // 5分钟后
          }
        }
      } catch (error) {
        console.error('创建自定义分析失败:', error)
        return {
          success: false,
          message: '创建自定义分析失败: ' + error.message
        }
      }
    },
    
    // 初始化所有数据
    async initAllData() {
      this.fetchOverview()
      this.fetchTrafficTrends()
      this.fetchBehaviorPatterns()
      this.fetchAbnormalEvents()
      this.fetchDeviceFailure()
      this.fetchHeatmap()
      this.fetchDataDiscoveries()
      this.fetchDeviceAnalytics()
      this.loadAIAssistant()
    }
  }
}) 