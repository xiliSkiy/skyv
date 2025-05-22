package core

import (
	"context"
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/skyeye/skyii-collector/internal/models"
	"github.com/skyeye/skyii-collector/pkg/config"
	"github.com/skyeye/skyii-collector/pkg/plugins"
	"github.com/skyeye/skyii-collector/pkg/transport"
)

// Collector 采集器核心结构
type Collector struct {
	config        *config.Config         // 配置信息
	transport     transport.Transport    // 传输模块
	pluginManager *plugins.PluginManager // 插件管理器
	scheduler     *Scheduler             // 任务调度器
	dataCache     *DataCache             // 数据缓存

	// 系统状态
	status   CollectorStatus
	statusMu sync.RWMutex

	// 控制通道
	ctx       context.Context
	cancelCtx context.CancelFunc
	wg        sync.WaitGroup
}

// CollectorStatus 采集器状态
type CollectorStatus struct {
	Running      bool      `json:"running"`
	StartTime    time.Time `json:"start_time"`
	TaskCount    int       `json:"task_count"`
	CPUUsage     float64   `json:"cpu_usage"`
	MemoryUsage  int64     `json:"memory_usage"`
	LastHeartbeat time.Time `json:"last_heartbeat"`
	ErrorCount   int       `json:"error_count"`
}

// NewCollector 创建新的采集器实例
func NewCollector(cfg *config.Config) (*Collector, error) {
	// 创建上下文
	ctx, cancel := context.WithCancel(context.Background())

	// 创建传输模块
	tr, err := transport.NewHTTPTransport(cfg.Server)
	if err != nil {
		cancel()
		return nil, fmt.Errorf("创建传输模块失败: %v", err)
	}

	// 创建插件管理器
	pm, err := plugins.NewPluginManager(cfg.Plugins)
	if err != nil {
		cancel()
		return nil, fmt.Errorf("创建插件管理器失败: %v", err)
	}

	// 创建数据缓存
	cache := NewDataCache(cfg.Collection.BufferSize)

	// 创建任务调度器
	scheduler := NewScheduler(cfg.Collection.MaxConcurrency)

	collector := &Collector{
		config:        cfg,
		transport:     tr,
		pluginManager: pm,
		scheduler:     scheduler,
		dataCache:     cache,
		status: CollectorStatus{
			Running:      false,
			ErrorCount:   0,
		},
		ctx:       ctx,
		cancelCtx: cancel,
	}

	return collector, nil
}

// Start 启动采集器
func (c *Collector) Start() error {
	c.statusMu.Lock()
	if c.status.Running {
		c.statusMu.Unlock()
		return fmt.Errorf("采集器已在运行中")
	}
	c.status.Running = true
	c.status.StartTime = time.Now()
	c.statusMu.Unlock()

	// 初始化插件
	if err := c.pluginManager.Initialize(); err != nil {
		return fmt.Errorf("初始化插件失败: %v", err)
	}

	// 启动各个组件
	c.wg.Add(3)

	// 启动心跳服务
	go c.runHeartbeat()

	// 启动任务处理服务
	go c.runTaskProcessor()

	// 启动数据发送服务
	go c.runDataSender()

	// 初始注册
	if err := c.registerCollector(); err != nil {
		log.Printf("初始注册失败: %v", err)
		// 继续运行，稍后会通过心跳重试
	}

	return nil
}

// Stop 停止采集器
func (c *Collector) Stop() {
	c.statusMu.Lock()
	if !c.status.Running {
		c.statusMu.Unlock()
		return
	}
	c.status.Running = false
	c.statusMu.Unlock()

	// 取消上下文
	c.cancelCtx()

	// 等待所有协程结束
	c.wg.Wait()

	// 关闭插件
	c.pluginManager.Shutdown()

	// 持久化缓存的数据
	c.dataCache.Flush()
}

// GetStatus 获取采集器状态
func (c *Collector) GetStatus() CollectorStatus {
	c.statusMu.RLock()
	defer c.statusMu.RUnlock()
	return c.status
}

// RegisterCollector 使用令牌注册采集器
func RegisterCollector(token string, serverURL string) error {
	// 创建临时传输模块
	transport, err := transport.NewHTTPTransport(config.ServerConfig{
		URL:       serverURL,
		Timeout:   30,
		SSL:       false, // 根据URL判断
		VerifySSL: true,
	})
	if err != nil {
		return fmt.Errorf("创建传输模块失败: %v", err)
	}

	// 获取系统信息
	hostname, _ := models.GetHostname()
	ipAddresses, _ := models.GetIPAddresses()

	// 创建注册请求
	registerRequest := &models.RegisterRequest{
		Token:        token,
		Hostname:     hostname,
		IPAddresses:  ipAddresses,
		OSInfo:       models.GetOSInfo(),
		Architecture: models.GetArchitecture(),
		Version:      "1.0.0", // 采集器版本
		Capabilities: []string{"snmp", "http", "ssh"}, // 支持的协议
	}

	// 发送注册请求
	resp, err := transport.Register(registerRequest)
	if err != nil {
		return fmt.Errorf("注册请求失败: %v", err)
	}

	// 保存配置
	cfg := config.DefaultConfig()
	cfg.CollectorID = resp.CollectorID
	cfg.Server.URL = serverURL
	cfg.Server.APIKey = resp.APIKey

	// 保存配置文件
	if err := config.SaveConfig(cfg, "config.yaml"); err != nil {
		return fmt.Errorf("保存配置失败: %v", err)
	}

	return nil
}

// 心跳服务
func (c *Collector) runHeartbeat() {
	defer c.wg.Done()

	interval := time.Duration(c.config.Server.HeartbeatInterval) * time.Second
	ticker := time.NewTicker(interval)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			c.sendHeartbeat()
		case <-c.ctx.Done():
			return
		}
	}
}

// 发送心跳
func (c *Collector) sendHeartbeat() {
	// 获取系统状态
	cpuUsage, memoryUsage := models.GetSystemUsage()

	c.statusMu.Lock()
	c.status.CPUUsage = cpuUsage
	c.status.MemoryUsage = memoryUsage
	c.status.LastHeartbeat = time.Now()
	status := c.status
	c.statusMu.Unlock()

	// 创建心跳请求
	heartbeat := &models.HeartbeatRequest{
		CollectorID:  c.config.CollectorID,
		Timestamp:    time.Now().Unix(),
		Status:       "RUNNING",
		CPUUsage:     cpuUsage,
		MemoryUsage:  memoryUsage,
		TaskCount:    status.TaskCount,
		ErrorCount:   status.ErrorCount,
		Uptime:       int64(time.Since(status.StartTime).Seconds()),
	}

	// 发送心跳
	resp, err := c.transport.Heartbeat(heartbeat)
	if err != nil {
		log.Printf("发送心跳失败: %v", err)
		return
	}

	// 处理服务器命令
	if resp != nil && len(resp.Commands) > 0 {
		c.processCommands(resp.Commands)
	}
}

// 处理服务器下发的命令
func (c *Collector) processCommands(commands []models.Command) {
	for _, cmd := range commands {
		switch cmd.Type {
		case "RESTART":
			log.Println("收到重启命令，准备重启...")
			go func() {
				c.Stop()
				c.Start()
			}()
		case "UPDATE_CONFIG":
			log.Println("收到配置更新命令")
			if err := config.UpdateConfig(c.config, cmd.Params); err != nil {
				log.Printf("更新配置失败: %v", err)
			}
		case "COLLECT_NOW":
			log.Println("收到立即采集命令")
			deviceID, ok := cmd.Params["device_id"].(string)
			if ok {
				metricType, _ := cmd.Params["metric_type"].(string)
				c.scheduler.ScheduleTask(&Task{
					DeviceID:   deviceID,
					MetricType: metricType,
					Priority:   1,
				})
			}
		default:
			log.Printf("未知命令类型: %s", cmd.Type)
		}
	}
}

// 初始注册
func (c *Collector) registerCollector() error {
	// 获取系统信息
	hostname, _ := models.GetHostname()
	ipAddresses, _ := models.GetIPAddresses()

	// 创建注册请求
	registerRequest := &models.RegisterRequest{
		CollectorID:  c.config.CollectorID,
		Hostname:     hostname,
		IPAddresses:  ipAddresses,
		OSInfo:       models.GetOSInfo(),
		Architecture: models.GetArchitecture(),
		APIKey:       c.config.Server.APIKey,
		Version:      "1.0.0", // 采集器版本
		Capabilities: []string{"snmp", "http", "ssh"}, // 支持的协议
	}

	// 发送注册请求
	_, err := c.transport.Register(registerRequest)
	return err
}

// 任务处理服务
func (c *Collector) runTaskProcessor() {
	defer c.wg.Done()

	for {
		select {
		case <-c.ctx.Done():
			return
		default:
			// 获取任务
			task := c.scheduler.GetNextTask()
			if task == nil {
				// 无任务，等待一段时间
				time.Sleep(100 * time.Millisecond)
				continue
			}

			// 处理任务
			go c.processTask(task)
		}
	}
}

// 处理单个任务
func (c *Collector) processTask(task *Task) {
	// 任务完成后更新计数
	defer func() {
		c.statusMu.Lock()
		c.status.TaskCount--
		c.statusMu.Unlock()
	}()

	c.statusMu.Lock()
	c.status.TaskCount++
	c.statusMu.Unlock()

	// 根据任务类型选择合适的插件
	plugin, err := c.pluginManager.GetPlugin(task.MetricType)
	if err != nil {
		log.Printf("获取插件失败: %v", err)
		c.recordError()
		return
	}

	// 执行采集
	result, err := plugin.Collect(task.DeviceID, task.Params)
	if err != nil {
		log.Printf("采集失败: %v", err)
		c.recordError()
		return
	}

	// 缓存结果
	c.dataCache.Add(&MetricData{
		DeviceID:   task.DeviceID,
		MetricType: task.MetricType,
		Value:      result.Value,
		Timestamp:  time.Now().Unix(),
		Status:     result.Status,
	})
}

// 记录错误
func (c *Collector) recordError() {
	c.statusMu.Lock()
	c.status.ErrorCount++
	c.statusMu.Unlock()
}

// 数据发送服务
func (c *Collector) runDataSender() {
	defer c.wg.Done()

	interval := time.Duration(5) * time.Second // 可配置
	ticker := time.NewTicker(interval)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			c.sendMetricsData()
		case <-c.ctx.Done():
			// 发送剩余数据
			c.sendMetricsData()
			return
		}
	}
}

// 发送指标数据
func (c *Collector) sendMetricsData() {
	// 获取缓存的数据
	metrics := c.dataCache.GetBatch(c.config.Collection.BatchSize)
	if len(metrics) == 0 {
		return
	}

	// 转换为传输格式
	metricsData := &models.MetricsData{
		CollectorID: c.config.CollectorID,
		Timestamp:   time.Now().Unix(),
		Metrics:     make([]models.Metric, 0, len(metrics)),
	}

	for _, m := range metrics {
		metricsData.Metrics = append(metricsData.Metrics, models.Metric{
			DeviceID:   m.DeviceID,
			MetricType: m.MetricType,
			Value:      m.Value,
			Timestamp:  m.Timestamp,
			Status:     m.Status,
		})
	}

	// 发送数据
	if err := c.transport.SendMetrics(metricsData); err != nil {
		log.Printf("发送指标数据失败: %v", err)
		// 失败时重新加入缓存
		for _, m := range metrics {
			c.dataCache.Add(m)
		}
		return
	}

	log.Printf("成功发送 %d 条指标数据", len(metrics))
} 