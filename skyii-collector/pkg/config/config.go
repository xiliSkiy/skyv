package config

import (
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"

	"gopkg.in/yaml.v3"
)

// Config 采集器配置结构
type Config struct {
	// 基本配置
	CollectorID string `yaml:"collector_id"`
	Name        string `yaml:"name"`
	LogLevel    string `yaml:"log_level"`
	LogPath     string `yaml:"log_path"`

	// 服务器连接配置
	Server ServerConfig `yaml:"server"`

	// 采集配置
	Collection CollectionConfig `yaml:"collection"`

	// 系统配置
	System SystemConfig `yaml:"system"`

	// 插件配置
	Plugins map[string]map[string]interface{} `yaml:"plugins"`
}

// ServerConfig 服务器连接配置
type ServerConfig struct {
	URL           string `yaml:"url"`            // 服务器地址
	APIKey        string `yaml:"api_key"`        // API密钥
	HeartbeatInterval int    `yaml:"heartbeat_interval"` // 心跳间隔（秒）
	Timeout       int    `yaml:"timeout"`        // 请求超时（秒）
	SSL           bool   `yaml:"ssl"`            // 是否启用SSL
	VerifySSL     bool   `yaml:"verify_ssl"`     // 是否验证SSL证书
}

// CollectionConfig 采集配置
type CollectionConfig struct {
	MaxConcurrency int               `yaml:"max_concurrency"` // 最大并发采集任务数
	RetryCount     int               `yaml:"retry_count"`     // 失败重试次数
	RetryInterval  int               `yaml:"retry_interval"`  // 重试间隔（秒）
	BufferSize     int               `yaml:"buffer_size"`     // 数据缓冲区大小
	BatchSize      int               `yaml:"batch_size"`      // 批量发送大小
	DataTTL        int               `yaml:"data_ttl"`        // 数据缓存时间（秒）
	Protocols      map[string]bool   `yaml:"protocols"`       // 启用的协议
}

// SystemConfig 系统配置
type SystemConfig struct {
	CPULimit    int    `yaml:"cpu_limit"`     // CPU使用限制（百分比）
	MemoryLimit int    `yaml:"memory_limit"`  // 内存使用限制（MB）
	DiskPath    string `yaml:"disk_path"`     // 磁盘缓存路径
	DiskLimit   int    `yaml:"disk_limit"`    // 磁盘使用限制（MB）
}

// DefaultConfig 返回默认配置
func DefaultConfig() *Config {
	return &Config{
		CollectorID: "",
		Name:        "skyii-collector",
		LogLevel:    "info",
		LogPath:     "logs",
		Server: ServerConfig{
			URL:               "http://localhost:8080/api",
			HeartbeatInterval: 60,
			Timeout:           10,
			SSL:               false,
			VerifySSL:         true,
		},
		Collection: CollectionConfig{
			MaxConcurrency: 10,
			RetryCount:     3,
			RetryInterval:  5,
			BufferSize:     1000,
			BatchSize:      100,
			DataTTL:        3600,
			Protocols: map[string]bool{
				"snmp": true,
				"http": true,
				"ssh":  false,
				"jmx":  false,
			},
		},
		System: SystemConfig{
			CPULimit:    80,
			MemoryLimit: 512,
			DiskPath:    "data",
			DiskLimit:   1024,
		},
		Plugins: make(map[string]map[string]interface{}),
	}
}

// LoadConfig 从文件加载配置
func LoadConfig(path string) (*Config, error) {
	// 使用默认配置
	config := DefaultConfig()

	// 检查配置文件是否存在
	if _, err := os.Stat(path); os.IsNotExist(err) {
		// 创建默认配置文件
		err = SaveConfig(config, path)
		if err != nil {
			return nil, fmt.Errorf("创建默认配置文件失败: %v", err)
		}
		return config, nil
	}

	// 读取配置文件
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("读取配置文件失败: %v", err)
	}

	// 解析配置
	err = yaml.Unmarshal(data, config)
	if err != nil {
		return nil, fmt.Errorf("解析配置文件失败: %v", err)
	}

	// 配置校验和补充
	if config.CollectorID == "" {
		config.CollectorID = generateCollectorID()
	}

	// 确保路径存在
	ensureDirExists(config.LogPath)
	ensureDirExists(config.System.DiskPath)

	return config, nil
}

// SaveConfig 保存配置到文件
func SaveConfig(config *Config, path string) error {
	// 确保目录存在
	dir := filepath.Dir(path)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return fmt.Errorf("创建配置目录失败: %v", err)
	}

	// 序列化配置
	data, err := yaml.Marshal(config)
	if err != nil {
		return fmt.Errorf("序列化配置失败: %v", err)
	}

	// 写入文件
	if err := ioutil.WriteFile(path, data, 0644); err != nil {
		return fmt.Errorf("写入配置文件失败: %v", err)
	}

	return nil
}

// UpdateConfig 更新部分配置
func UpdateConfig(config *Config, updates map[string]interface{}) error {
	// 这里可以实现更复杂的配置更新逻辑
	// 简单示例仅作参考
	for key, value := range updates {
		switch key {
		case "name":
			if v, ok := value.(string); ok {
				config.Name = v
			}
		case "log_level":
			if v, ok := value.(string); ok {
				config.LogLevel = v
			}
		// 添加其他配置项的更新
		}
	}
	return nil
}

// 生成采集器ID
func generateCollectorID() string {
	// 简单实现，实际应该更复杂
	hostname, _ := os.Hostname()
	timestamp := fmt.Sprintf("%d", os.Getpid())
	return fmt.Sprintf("collector-%s-%s", strings.ReplaceAll(hostname, ".", "-"), timestamp)
}

// 确保目录存在
func ensureDirExists(path string) {
	if path == "" {
		return
	}
	os.MkdirAll(path, 0755)
} 