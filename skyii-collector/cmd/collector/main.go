package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/skyeye/skyii-collector/config"
	"github.com/skyeye/skyii-collector/internal/api"
	"github.com/skyeye/skyii-collector/pkg/models"
	"github.com/skyeye/skyii-collector/pkg/logger"
	"github.com/skyeye/skyii-collector/pkg/utils"
)

// 全局API客户端
var client api.ApiClient

func main() {
	// 初始化日志
	logger.InitLogger("info", "logs/collector.log", nil)
	logger.Info("采集器启动中...")

	// 加载配置
	cfg, err := config.Load()
	if err != nil {
		logger.Fatal("加载配置失败: ", err)
		os.Exit(1)
	}

	// 创建上下文
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	// 创建API客户端
	client, err = api.NewClient(cfg)
	if err != nil {
		logger.Fatal("创建API客户端失败: ", err)
		os.Exit(1)
	}

	// 注册采集器
	err = registerCollector(ctx, cfg)
	if err != nil {
		logger.Fatal("注册采集器失败: ", err)
		os.Exit(1)
	}

	// 启动心跳协程
	heartbeatCtx, heartbeatCancel := context.WithCancel(ctx)
	defer heartbeatCancel()
	go startHeartbeat(heartbeatCtx, cfg.HeartbeatInterval)

	// 启动任务获取协程
	taskCtx, taskCancel := context.WithCancel(ctx)
	defer taskCancel()
	go startTaskFetcher(taskCtx, cfg.TaskInterval)

	// 等待信号
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)
	<-sigChan

	logger.Info("采集器正在关闭...")
	
	// 给其他协程一些时间清理资源
	time.Sleep(1 * time.Second)
	logger.Info("采集器已关闭")
}

// 注册采集器
func registerCollector(ctx context.Context, cfg *config.Config) error {
	logger.Info("正在注册采集器...")

	// 获取主机名
	hostname := utils.GetHostname()

	// 准备注册信息
	info := &models.CollectorInfo{
		CollectorID:  cfg.CollectorID,
		Hostname:     hostname,
		IP:           getLocalIP(),
		Version:      "1.0.0",
		Capabilities: cfg.Capabilities,
		Tags:         cfg.Tags,
	}

	// 发送注册请求
	resp, err := client.Register(ctx, info)
	if err != nil {
		return fmt.Errorf("注册失败: %w", err)
	}

	logger.Info("注册成功，采集器ID: ", resp.CollectorID)
	return nil
}

// 启动心跳
func startHeartbeat(ctx context.Context, intervalSeconds int) {
	ticker := time.NewTicker(time.Duration(intervalSeconds) * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			sendHeartbeat(ctx)
		}
	}
}

// 发送心跳
func sendHeartbeat(ctx context.Context) {
	// 构建心跳请求
	heartbeat := &models.HeartbeatRequest{
		Timestamp: time.Now().Format(time.RFC3339),
		Status:    "ONLINE",
		Metrics: models.HeartbeatMetrics{
			CPU:          getCPUUsage(),
			Memory:       getMemoryUsage(),
			Disk:         getDiskUsage(),
			RunningTasks: 0, // TODO: 实际任务数
		},
	}

	// 发送心跳
	_, err := client.Heartbeat(ctx, heartbeat)
	if err != nil {
		fmt.Printf("发送心跳失败: %v\n", err)
		return
	}

	fmt.Println("心跳发送成功")
}

// 启动任务获取
func startTaskFetcher(ctx context.Context, intervalSeconds int) {
	ticker := time.NewTicker(time.Duration(intervalSeconds) * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			fetchAndExecuteTasks(ctx)
		}
	}
}

// 获取并执行任务
func fetchAndExecuteTasks(ctx context.Context) {
	// 获取任务批次
	batches, err := client.GetBatches(ctx, 5) // 每次最多获取5个批次
	if err != nil {
		fmt.Printf("获取任务批次失败: %v\n", err)
		return
	}

	if len(batches) == 0 {
		fmt.Println("当前没有待执行的任务批次")
		return
	}

	fmt.Printf("获取到 %d 个任务批次\n", len(batches))

	// 处理每个批次
	for _, batch := range batches {
		processBatch(ctx, batch)
	}
}

// 处理批次
func processBatch(ctx context.Context, batch *models.TaskBatch) {
	fmt.Printf("开始处理批次 %d: %s\n", batch.BatchID, batch.BatchName)

	// 更新批次状态为运行中
	err := client.UpdateBatchStatus(ctx, batch.BatchID, &models.StatusUpdate{
		Status:    "RUNNING",
		StartTime: time.Now().Format(time.RFC3339),
	})
	if err != nil {
		fmt.Printf("更新批次状态失败: %v\n", err)
		return
	}

	// 获取批次中的任务
	tasks, err := client.GetTasks(ctx, batch.BatchID)
	if err != nil {
		fmt.Printf("获取批次任务失败: %v\n", err)
		// 更新批次状态为失败
		client.UpdateBatchStatus(ctx, batch.BatchID, &models.StatusUpdate{
			Status:  "FAILED",
			EndTime: time.Now().Format(time.RFC3339),
			Message: fmt.Sprintf("获取任务失败: %v", err),
		})
		return
	}

	fmt.Printf("批次 %d 包含 %d 个任务\n", batch.BatchID, len(tasks))

	// 执行任务
	results := make([]*models.CollectionResult, 0, len(tasks))
	for _, task := range tasks {
		// 更新任务状态为运行中
		client.UpdateTaskStatus(ctx, task.TaskID, &models.StatusUpdate{
			Status:    "RUNNING",
			StartTime: time.Now().Format(time.RFC3339),
		})

		// 执行任务
		result := executeTask(ctx, task)
		results = append(results, result)

		// 更新任务状态为已完成
		client.UpdateTaskStatus(ctx, task.TaskID, &models.StatusUpdate{
			Status:        "COMPLETED",
			EndTime:       time.Now().Format(time.RFC3339),
			ExecutionTime: result.ExecutionTime,
		})
	}

	// 上传结果
	if len(results) > 0 {
		err = client.UploadResults(ctx, results)
		if err != nil {
			fmt.Printf("上传结果失败: %v\n", err)
		}
	}

	// 更新批次状态为已完成
	client.UpdateBatchStatus(ctx, batch.BatchID, &models.StatusUpdate{
		Status:  "COMPLETED",
		EndTime: time.Now().Format(time.RFC3339),
	})

	fmt.Printf("批次 %d 处理完成\n", batch.BatchID)
}

// 执行任务
func executeTask(ctx context.Context, task *models.CollectionTask) *models.CollectionResult {
	start := time.Now()
	fmt.Printf("执行任务 %d: 设备=%s, 指标=%s\n", task.TaskID, task.DeviceName, task.MetricName)

	// TODO: 根据不同的采集类型调用不同的采集插件
	// 这里简单模拟采集结果
	time.Sleep(500 * time.Millisecond)

	executionTime := int(time.Since(start).Milliseconds())
	return &models.CollectionResult{
		TaskID:        task.TaskID,
		BatchID:       task.BatchID,
		DeviceID:      task.DeviceID,
		MetricID:      task.MetricID,
		ResultValue:   fmt.Sprintf(`{"value": %.2f, "unit": "ms"}`, float64(executionTime)),
		ResultType:    "json",
		Status:        "SUCCESS",
		ExecutionTime: executionTime,
		Timestamp:     time.Now().Format(time.RFC3339),
	}
}

// 获取本地IP (简化实现)
func getLocalIP() string {
	return "127.0.0.1"
}

// 获取CPU使用率 (简化实现)
func getCPUUsage() float64 {
	return 30.0 + 20.0*float64(time.Now().Second()%3)/3.0
}

// 获取内存使用率 (简化实现)
func getMemoryUsage() float64 {
	return 40.0 + 10.0*float64(time.Now().Second()%5)/5.0
}

// 获取磁盘使用率 (简化实现)
func getDiskUsage() float64 {
	return 50.0 + 5.0*float64(time.Now().Second()%10)/10.0
}
