package config

import (
	"encoding/json"
	"os"
	"path/filepath"
)

// Config 表示采集器配置
type Config struct {
	// 基本配置
	ServerURL     string `json:"serverUrl"`
	CollectorID   string `json:"collectorId"`
	CollectorName string `json:"collectorName"`

	// 安全配置
	TLSEnabled  bool   `json:"tlsEnabled"`
	TLSCertFile string `json:"tlsCertFile"`
	TLSKeyFile  string `json:"tlsKeyFile"`
	TLSCAFile   string `json:"tlsCAFile"`

	// 采集器配置
	HeartbeatInterval int      `json:"heartbeatInterval"` // 心跳间隔(秒)
	MaxTasks          int      `json:"maxTasks"`          // 最大并行任务数
	TaskInterval      int      `json:"taskInterval"`      // 任务获取间隔(秒)
	Capabilities      []string `json:"capabilities"`      // 支持的采集类型
	Tags              []string `json:"tags"`              // 标签

	// 日志配置
	LogLevel string `json:"logLevel"`
	LogPath  string `json:"logPath"`

	// 缓存配置
	CachePath     string `json:"cachePath"`     // 本地缓存路径
	MaxCacheSize  int    `json:"maxCacheSize"`  // 最大缓存大小(MB)
	MaxResultSize int    `json:"maxResultSize"` // 最大结果批次大小

	// 重试配置
	MaxRetries     int `json:"maxRetries"`     // 最大重试次数
	RetryInterval  int `json:"retryInterval"`  // 初始重试间隔(秒)
	MaxBackoffTime int `json:"maxBackoffTime"` // 最大退避时间(秒)
}

// Load 从文件加载配置
func Load() (*Config, error) {
	// 默认配置
	config := &Config{
		ServerURL:         "http://localhost:8080",
		HeartbeatInterval: 60,
		MaxTasks:          10,
		TaskInterval:      30,
		LogLevel:          "info",
		MaxCacheSize:      100,
		MaxResultSize:     50,
		MaxRetries:        3,
		RetryInterval:     5,
		MaxBackoffTime:    300,
		Capabilities:      []string{"http", "snmp", "ping", "tcp"},
		Tags:              []string{"default"},
	}

	// 查找配置文件
	configPath := os.Getenv("COLLECTOR_CONFIG")
	if configPath == "" {
		// 默认配置路径
		homeDir, err := os.UserHomeDir()
		if err == nil {
			configPath = filepath.Join(homeDir, ".skyeye", "collector.json")
		} else {
			configPath = "collector.json"
		}
	}

	// 读取配置文件
	data, err := os.ReadFile(configPath)
	if err != nil {
		// 如果配置文件不存在，使用默认配置
		if os.IsNotExist(err) {
			return config, nil
		}
		return nil, err
	}

	// 解析配置
	if err := json.Unmarshal(data, config); err != nil {
		return nil, err
	}

	return config, nil
} 