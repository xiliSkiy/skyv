/**
 * 数据分析模块的Mock数据
 */

// 概览数据
export const overviewData = {
  totalTraffic: {
    value: 8547,
    trend: 12.5,
    prediction: '预测下周增长15%'
  },
  alertEvents: {
    value: 156,
    trend: 8.3,
    warning: '异常增长需关注'
  },
  avgStayTime: {
    value: 24.5,
    unit: '分钟',
    trend: -2.1,
    status: '符合安全标准'
  },
  deviceHealth: {
    value: 96.8,
    unit: '%',
    trend: 1.2,
    warning: '3台设备需维护'
  }
}

// 人流量趋势与预测数据
export const trafficTrendsData = {
  accuracy: 92.7,
  xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日', '下周一', '下周二', '下周三'],
  series: [
    {
      name: '历史数据',
      type: 'line',
      data: [1200, 1350, 1800, 1420, 1650, 2100, 1820, null, null, null],
      itemStyle: {
        color: '#1e3c72'
      }
    },
    {
      name: 'AI预测',
      type: 'line',
      data: [null, null, null, null, null, null, null, 1900, 2250, 2400],
      itemStyle: {
        color: '#ff6b6b'
      },
      lineStyle: {
        type: 'dashed'
      }
    },
    {
      name: '置信区间上限',
      type: 'line',
      data: [null, null, null, null, null, null, null, 2100, 2450, 2600],
      lineStyle: {
        width: 0
      },
      stack: 'confidence',
      symbol: 'none'
    },
    {
      name: '置信区间下限',
      type: 'line',
      data: [null, null, null, null, null, null, null, 1700, 2050, 2200],
      lineStyle: {
        width: 0
      },
      areaStyle: {
        color: 'rgba(255,107,107,0.2)'
      },
      stack: 'confidence',
      symbol: 'none'
    }
  ],
  insight: '预计下周二至周四人流量将增加25%，建议增加安保人员配置。'
}

// 人员行为模式分析数据
export const behaviorPatternsData = {
  title: '人员行为模式分析',
  subtitle: '基于深度学习',
  data: [
    { value: 42, name: '正常通行' },
    { value: 28, name: '短暂停留' },
    { value: 15, name: '长时间逗留' },
    { value: 10, name: '异常聚集' },
    { value: 5, name: '其他行为' }
  ],
  abnormalDetection: '检测到3处异常聚集行为，可能存在安全风险。'
}

// 异常事件智能分析数据
export const abnormalEventsData = {
  title: '异常事件智能分析',
  subtitle: '自动风险评估',
  xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  series: [
    {
      name: '高风险',
      type: 'bar',
      stack: 'total',
      data: [2, 1, 3, 2, 1, 2, 1],
      itemStyle: {
        color: '#f56c6c'
      }
    },
    {
      name: '中风险',
      type: 'bar',
      stack: 'total',
      data: [3, 4, 5, 3, 4, 3, 3],
      itemStyle: {
        color: '#e6a23c'
      }
    },
    {
      name: '低风险',
      type: 'bar',
      stack: 'total',
      data: [5, 6, 4, 5, 3, 4, 5],
      itemStyle: {
        color: '#67c23a'
      }
    }
  ],
  summary: {
    highRisk: 12,
    mediumRisk: 25
  }
}

// 设备故障预测数据
export const deviceFailurePredictionsData = {
  title: '设备故障预测',
  subtitle: '预防性维护建议',
  xAxis: ['摄像头', '门禁', '传感器', '报警器', '其他设备'],
  series: [
    {
      name: '健康状态',
      type: 'bar',
      data: [92, 95, 88, 96, 93],
      itemStyle: {
        color: '#67c23a'
      }
    },
    {
      name: '故障风险',
      type: 'line',
      yAxisIndex: 1,
      data: [8, 5, 12, 4, 7],
      itemStyle: {
        color: '#f56c6c'
      }
    }
  ],
  warning: {
    device: '停车场摄像头#03',
    days: 5,
    component: '电源模块'
  }
}

// 热力图分析数据
export const heatmapData = {
  title: 'AI增强热力图分析',
  subtitle: '智能识别关注区域',
  areas: ['一楼大厅', '二楼办公区', '仓库区域', '停车场'],
  currentArea: '一楼大厅',
  modes: ['人流量', '停留时间', '异常行为'],
  currentMode: '停留时间',
  hotspots: [
    {
      id: 1,
      x: 25,
      y: 30,
      radius: 40,
      level: '高',
      duration: '15分钟'
    },
    {
      id: 2,
      x: 60,
      y: 45,
      radius: 30,
      level: '中',
      duration: '8分钟'
    }
  ],
  analysis: {
    hotspots: '入口处与电梯口',
    peakTimes: '09:00-10:30, 17:00-18:30',
    suggestion: '增加入口处安保人员配置'
  }
}

// 智能数据探索结果
export const dataDiscoveriesData = {
  discoveries: [
    {
      title: '人流量与报警事件关联',
      confidence: 95,
      insight: '人流量超过每小时2000人次时，报警事件增加率达到173%',
      data: {
        xAxis: [1000, 1500, 2000, 2500, 3000],
        series: [
          {
            name: '报警事件数',
            type: 'line',
            data: [5, 8, 15, 41, 68],
            itemStyle: {
              color: '#f56c6c'
            }
          }
        ]
      }
    },
    {
      title: '天气因素影响',
      confidence: 92,
      insight: '雨天时停车场区域异常事件增加62%，主要为车辆碰撞事件',
      data: {
        xAxis: ['晴天', '多云', '小雨', '大雨', '雾天'],
        series: [
          {
            name: '异常事件数',
            type: 'bar',
            data: [12, 15, 28, 42, 35],
            itemStyle: {
              color: '#e6a23c'
            }
          }
        ]
      }
    }
  ]
}

// 设备分析明细数据
export const deviceAnalyticsData = {
  total: 24,
  pageSize: 5,
  currentPage: 1,
  data: [
    {
      name: '前门监控',
      location: '一楼大厅',
      traffic: 2453,
      trafficTrend: 12,
      alerts: 12,
      alertLevel: null,
      peakTime: '08:30 - 09:30',
      status: '正常',
      aiScore: 85,
      aiWarning: null
    },
    {
      name: '后门监控',
      location: '后门通道',
      traffic: 1248,
      trafficTrend: -5,
      alerts: 8,
      alertLevel: null,
      peakTime: '12:00 - 13:00',
      status: '正常',
      aiScore: 92,
      aiWarning: null
    },
    {
      name: '办公区监控',
      location: '二楼办公区',
      traffic: 3125,
      trafficTrend: 18,
      alerts: 5,
      alertLevel: null,
      peakTime: '09:00 - 10:00',
      status: '正常',
      aiScore: 95,
      aiWarning: null
    },
    {
      name: '停车场监控',
      location: '地下停车场',
      traffic: 1721,
      trafficTrend: 7,
      alerts: 15,
      alertLevel: '高',
      peakTime: '18:00 - 19:00',
      status: '信号弱',
      aiScore: 65,
      aiWarning: '预计5天内故障'
    },
    {
      name: '仓库监控',
      location: '仓库区域',
      traffic: 876,
      trafficTrend: -2,
      alerts: 2,
      alertLevel: null,
      peakTime: '14:00 - 15:00',
      status: '正常',
      aiScore: 89,
      aiWarning: null
    }
  ]
}

// AI分析助手对话数据
export const aiAssistantData = {
  conversations: [
    {
      id: 1,
      role: 'ai',
      content: '您好，我可以帮您分析哪些数据?'
    },
    {
      id: 2,
      role: 'user',
      content: '分析最近一周的异常事件趋势'
    },
    {
      id: 3,
      role: 'ai',
      content: {
        text: '分析结果如下:',
        points: [
          '最近一周共检测到37起异常事件，比上周减少12%',
          '主要异常类型为人员聚集(42%)和未授权访问(28%)',
          '发现周三14:00-16:00是异常高发时段'
        ],
        actions: ['查看详细分析', '导出报告', '设置预警']
      }
    }
  ],
  suggestions: [
    '分析最近一周的异常事件趋势',
    '预测下周人流量',
    '比较各区域报警频率',
    '分析设备健康状态',
    '识别异常行为模式'
  ]
} 