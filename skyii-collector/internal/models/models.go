package models

import (
	"fmt"
	"net"
	"os"
	"runtime"
	"time"
)

// RegisterRequest 注册请求
type RegisterRequest struct {
	CollectorID  string   `json:"collector_id,omitempty"` // 采集器ID，初次注册为空
	Token        string   `json:"token,omitempty"`        // 注册令牌，初次注册使用
	APIKey       string   `json:"api_key,omitempty"`      // API密钥，重新注册使用
	Hostname     string   `json:"hostname"`               // 主机名
	IPAddresses  []string `json:"ip_addresses"`           // IP地址列表
	OSInfo       string   `json:"os_info"`                // 操作系统信息
	Architecture string   `json:"architecture"`           // 系统架构
	Version      string   `json:"version"`                // 采集器版本
	Capabilities []string `json:"capabilities"`           // 支持的能力
}

// RegisterResponse 注册响应
type RegisterResponse struct {
	CollectorID string `json:"collector_id"` // 服务器分配的采集器ID
	APIKey      string `json:"api_key"`      // API密钥
	ServerTime  int64  `json:"server_time"`  // 服务器时间
	Status      string `json:"status"`       // 注册状态
}

// HeartbeatRequest 心跳请求
type HeartbeatRequest struct {
	CollectorID  string  `json:"collector_id"` // 采集器ID
	Timestamp    int64   `json:"timestamp"`    // 客户端时间戳
	Status       string  `json:"status"`       // 状态：RUNNING, IDLE, ERROR
	CPUUsage     float64 `json:"cpu_usage"`    // CPU使用率
	MemoryUsage  int64   `json:"memory_usage"` // 内存使用量
	TaskCount    int     `json:"task_count"`   // 当前任务数
	ErrorCount   int     `json:"error_count"`  // 错误数
	Uptime       int64   `json:"uptime"`       // 运行时间(秒)
}

// HeartbeatResponse 心跳响应
type HeartbeatResponse struct {
	ServerTime int64      `json:"server_time"` // 服务器时间
	Commands   []Command  `json:"commands"`    // 服务器下发的命令
}

// Command 服务器命令
type Command struct {
	Type   string                 `json:"type"`   // 命令类型：RESTART, UPDATE_CONFIG, COLLECT_NOW
	Params map[string]interface{} `json:"params"` // 命令参数
}

// MetricsData 指标数据
type MetricsData struct {
	CollectorID string   `json:"collector_id"` // 采集器ID
	Timestamp   int64    `json:"timestamp"`    // 发送时间戳
	Metrics     []Metric `json:"metrics"`      // 指标数据列表
}

// Metric 单个指标数据
type Metric struct {
	DeviceID   string      `json:"device_id"`    // 设备ID
	MetricType string      `json:"metric_type"`  // 指标类型
	Value      interface{} `json:"value"`        // 指标值
	Timestamp  int64       `json:"timestamp"`    // 采集时间戳
	Status     string      `json:"status"`       // 状态：success, warning, error
}

// TasksResponse 任务响应
type TasksResponse struct {
	Tasks []Task `json:"tasks"` // 任务列表
}

// Task 任务信息
type Task struct {
	TaskID     string                 `json:"task_id"`     // 任务ID
	DeviceID   string                 `json:"device_id"`   // 设备ID
	MetricType string                 `json:"metric_type"` // 指标类型
	Params     map[string]interface{} `json:"params"`      // 任务参数
	Schedule   int64                  `json:"schedule"`    // 计划执行时间
	Priority   int                    `json:"priority"`    // 优先级
}

// PluginsResponse 插件响应
type PluginsResponse struct {
	Plugins []PluginInfo `json:"plugins"` // 插件列表
}

// PluginInfo 插件信息
type PluginInfo struct {
	ID          string   `json:"id"`           // 插件ID
	Name        string   `json:"name"`         // 插件名称
	Version     string   `json:"version"`      // 插件版本
	Description string   `json:"description"`  // 插件描述
	URL         string   `json:"url"`          // 下载URL
	Protocols   []string `json:"protocols"`    // 支持的协议
	Metrics     []string `json:"metrics"`      // 支持的指标类型
}

// CollectorConfig 采集器配置
type CollectorConfig struct {
	CollectorID  string                 `json:"collector_id"`  // 采集器ID
	MaxTasks     int                    `json:"max_tasks"`     // 最大任务数
	HeartbeatInterval int               `json:"heartbeat_interval"` // 心跳间隔
	LogLevel     string                 `json:"log_level"`     // 日志级别
	PluginConfig map[string]interface{} `json:"plugin_config"` // 插件配置
}

// 以下是一些辅助函数

// GetHostname 获取主机名
func GetHostname() (string, error) {
	return os.Hostname()
}

// GetIPAddresses 获取IP地址列表
func GetIPAddresses() ([]string, error) {
	interfaces, err := net.Interfaces()
	if err != nil {
		return nil, err
	}

	var addresses []string
	for _, iface := range interfaces {
		// 跳过本地回环接口和未启用接口
		if iface.Flags&net.FlagLoopback != 0 || iface.Flags&net.FlagUp == 0 {
			continue
		}

		addrs, err := iface.Addrs()
		if err != nil {
			continue
		}

		for _, addr := range addrs {
			if ipnet, ok := addr.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
				if ipnet.IP.To4() != nil || ipnet.IP.To16() != nil {
					addresses = append(addresses, ipnet.IP.String())
				}
			}
		}
	}

	return addresses, nil
}

// GetOSInfo 获取操作系统信息
func GetOSInfo() string {
	return fmt.Sprintf("%s %s", runtime.GOOS, runtime.GOARCH)
}

// GetArchitecture 获取系统架构
func GetArchitecture() string {
	return runtime.GOARCH
}

// GetSystemUsage 获取系统资源使用情况
func GetSystemUsage() (float64, int64) {
	// 简化实现，实际应使用如github.com/shirou/gopsutil等库获取精确信息
	var m runtime.MemStats
	runtime.ReadMemStats(&m)
	
	// 简单返回一个模拟的CPU使用率和内存使用量
	cpuUsage := 10.5 // 模拟值
	memUsage := int64(m.Alloc)
	
	return cpuUsage, memUsage
}

// FormatTime 格式化时间
func FormatTime(t time.Time) string {
	return t.Format("2006-01-02 15:04:05")
} 