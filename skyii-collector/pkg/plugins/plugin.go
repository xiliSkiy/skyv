package plugins

import (
	"fmt"
	"plugin"
	"sync"
)

// Plugin 插件接口
type Plugin interface {
	// Initialize 初始化插件
	Initialize(config map[string]interface{}) error

	// Collect 采集指标数据
	Collect(deviceID string, params map[string]interface{}) (*CollectResult, error)

	// Validate 验证设备连接
	Validate(params map[string]interface{}) (bool, string, error)

	// GetMetadata 获取插件元数据
	GetMetadata() PluginMetadata

	// Shutdown 关闭插件
	Shutdown() error
}

// CollectResult 采集结果
type CollectResult struct {
	Value  interface{} // 采集到的值
	Status string      // 采集状态: success, warning, error
	Extra  map[string]interface{} // 额外信息
}

// PluginMetadata 插件元数据
type PluginMetadata struct {
	ID          string   // 插件ID
	Name        string   // 插件名称
	Version     string   // 插件版本
	Description string   // 插件描述
	Author      string   // 作者
	Protocols   []string // 支持的协议
	Metrics     []string // 支持的指标类型
}

// PluginManager 插件管理器
type PluginManager struct {
	plugins     map[string]Plugin // 已加载的插件
	configs     map[string]map[string]interface{} // 插件配置
	mu          sync.RWMutex
}

// NewPluginManager 创建插件管理器
func NewPluginManager(configs map[string]map[string]interface{}) (*PluginManager, error) {
	return &PluginManager{
		plugins: make(map[string]Plugin),
		configs: configs,
	}, nil
}

// Initialize 初始化所有插件
func (pm *PluginManager) Initialize() error {
	pm.mu.Lock()
	defer pm.mu.Unlock()

	// 加载内置插件
	if err := pm.loadBuiltinPlugins(); err != nil {
		return err
	}

	// 加载动态插件
	if err := pm.loadDynamicPlugins(); err != nil {
		return err
	}

	// 初始化插件
	for id, p := range pm.plugins {
		config := pm.configs[id]
		if config == nil {
			config = make(map[string]interface{})
		}
		if err := p.Initialize(config); err != nil {
			return fmt.Errorf("初始化插件 %s 失败: %v", id, err)
		}
	}

	return nil
}

// GetPlugin 获取插件
func (pm *PluginManager) GetPlugin(pluginType string) (Plugin, error) {
	pm.mu.RLock()
	defer pm.mu.RUnlock()

	plugin, ok := pm.plugins[pluginType]
	if !ok {
		return nil, fmt.Errorf("未找到插件: %s", pluginType)
	}
	return plugin, nil
}

// RegisterPlugin 注册插件
func (pm *PluginManager) RegisterPlugin(pluginType string, plugin Plugin) {
	pm.mu.Lock()
	defer pm.mu.Unlock()

	pm.plugins[pluginType] = plugin
}

// Shutdown 关闭所有插件
func (pm *PluginManager) Shutdown() {
	pm.mu.Lock()
	defer pm.mu.Unlock()

	for id, p := range pm.plugins {
		if err := p.Shutdown(); err != nil {
			fmt.Printf("关闭插件 %s 失败: %v\n", id, err)
		}
	}
}

// LoadPlugin 加载动态插件
func (pm *PluginManager) LoadPlugin(path string) error {
	pm.mu.Lock()
	defer pm.mu.Unlock()

	// 加载动态库
	p, err := plugin.Open(path)
	if err != nil {
		return fmt.Errorf("加载插件文件失败: %v", err)
	}

	// 获取导出的插件符号
	symPlugin, err := p.Lookup("Plugin")
	if err != nil {
		return fmt.Errorf("插件未导出Plugin符号: %v", err)
	}

	// 转换为插件接口
	plugin, ok := symPlugin.(Plugin)
	if !ok {
		return fmt.Errorf("导出的Plugin符号不是有效的插件接口")
	}

	// 获取插件元数据
	metadata := plugin.GetMetadata()

	// 注册插件
	pm.plugins[metadata.ID] = plugin

	return nil
}

// GetAllPlugins 获取所有已加载的插件
func (pm *PluginManager) GetAllPlugins() map[string]PluginMetadata {
	pm.mu.RLock()
	defer pm.mu.RUnlock()

	result := make(map[string]PluginMetadata)
	for id, p := range pm.plugins {
		result[id] = p.GetMetadata()
	}
	return result
}

// 加载内置插件
func (pm *PluginManager) loadBuiltinPlugins() error {
	// 注册SNMP插件
	pm.RegisterPlugin("snmp", NewSNMPPlugin())

	// 注册HTTP插件
	pm.RegisterPlugin("http", NewHTTPPlugin())

	// 注册系统插件
	pm.RegisterPlugin("system", NewSystemPlugin())

	return nil
}

// 加载动态插件
func (pm *PluginManager) loadDynamicPlugins() error {
	// 实际实现中，这里可以扫描插件目录
	// 为了简化，这里不做实现
	return nil
}

// 以下是内置插件的实现示例

// SNMPPlugin SNMP协议插件
type SNMPPlugin struct {
	config map[string]interface{}
}

func NewSNMPPlugin() *SNMPPlugin {
	return &SNMPPlugin{}
}

func (p *SNMPPlugin) Initialize(config map[string]interface{}) error {
	p.config = config
	return nil
}

func (p *SNMPPlugin) Collect(deviceID string, params map[string]interface{}) (*CollectResult, error) {
	// 示例实现
	return &CollectResult{
		Value:  "SNMP采集结果示例",
		Status: "success",
	}, nil
}

func (p *SNMPPlugin) Validate(params map[string]interface{}) (bool, string, error) {
	// 示例实现
	return true, "连接成功", nil
}

func (p *SNMPPlugin) GetMetadata() PluginMetadata {
	return PluginMetadata{
		ID:          "snmp",
		Name:        "SNMP插件",
		Version:     "1.0.0",
		Description: "用于SNMP协议数据采集",
		Author:      "SkyEye Team",
		Protocols:   []string{"snmp"},
		Metrics:     []string{"cpu", "memory", "disk", "network"},
	}
}

func (p *SNMPPlugin) Shutdown() error {
	return nil
}

// HTTPPlugin HTTP协议插件
type HTTPPlugin struct {
	config map[string]interface{}
}

func NewHTTPPlugin() *HTTPPlugin {
	return &HTTPPlugin{}
}

func (p *HTTPPlugin) Initialize(config map[string]interface{}) error {
	p.config = config
	return nil
}

func (p *HTTPPlugin) Collect(deviceID string, params map[string]interface{}) (*CollectResult, error) {
	// 示例实现
	return &CollectResult{
		Value:  "HTTP采集结果示例",
		Status: "success",
	}, nil
}

func (p *HTTPPlugin) Validate(params map[string]interface{}) (bool, string, error) {
	// 示例实现
	return true, "连接成功", nil
}

func (p *HTTPPlugin) GetMetadata() PluginMetadata {
	return PluginMetadata{
		ID:          "http",
		Name:        "HTTP插件",
		Version:     "1.0.0",
		Description: "用于HTTP协议数据采集",
		Author:      "SkyEye Team",
		Protocols:   []string{"http", "https"},
		Metrics:     []string{"api", "web", "service"},
	}
}

func (p *HTTPPlugin) Shutdown() error {
	return nil
}

// SystemPlugin 系统监控插件
type SystemPlugin struct {
	config map[string]interface{}
}

func NewSystemPlugin() *SystemPlugin {
	return &SystemPlugin{}
}

func (p *SystemPlugin) Initialize(config map[string]interface{}) error {
	p.config = config
	return nil
}

func (p *SystemPlugin) Collect(deviceID string, params map[string]interface{}) (*CollectResult, error) {
	// 示例实现
	return &CollectResult{
		Value:  "系统监控采集结果示例",
		Status: "success",
	}, nil
}

func (p *SystemPlugin) Validate(params map[string]interface{}) (bool, string, error) {
	// 示例实现
	return true, "连接成功", nil
}

func (p *SystemPlugin) GetMetadata() PluginMetadata {
	return PluginMetadata{
		ID:          "system",
		Name:        "系统监控插件",
		Version:     "1.0.0",
		Description: "用于系统资源监控",
		Author:      "SkyEye Team",
		Protocols:   []string{"local"},
		Metrics:     []string{"cpu", "memory", "disk", "network"},
	}
}

func (p *SystemPlugin) Shutdown() error {
	return nil
} 