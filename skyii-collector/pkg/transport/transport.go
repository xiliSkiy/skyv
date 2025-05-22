package transport

import (
	"github.com/skyeye/skyii-collector/internal/models"
)

// Transport 传输层接口
type Transport interface {
	// Register 向服务器注册采集器
	Register(request *models.RegisterRequest) (*models.RegisterResponse, error)

	// Heartbeat 发送心跳
	Heartbeat(request *models.HeartbeatRequest) (*models.HeartbeatResponse, error)

	// SendMetrics 发送指标数据
	SendMetrics(data *models.MetricsData) error

	// GetTasks 获取任务列表
	GetTasks() (*models.TasksResponse, error)

	// ReportTaskStatus 报告任务状态
	ReportTaskStatus(taskID string, status string, message string) error

	// GetPlugins 获取插件列表
	GetPlugins() (*models.PluginsResponse, error)

	// DownloadPlugin 下载插件
	DownloadPlugin(pluginID string) ([]byte, error)

	// GetCollectorConfig 获取采集器配置
	GetCollectorConfig() (*models.CollectorConfig, error)
} 