package api

import (
	"bytes"
	"context"
	"crypto/tls"
	"crypto/x509"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"

	"github.com/skyeye/skyii-collector/config"
	"github.com/skyeye/skyii-collector/pkg/models"
)

// ApiClient 定义API客户端接口
type ApiClient interface {
	// 注册采集器
	Register(ctx context.Context, info *models.CollectorInfo) (*models.RegisterResponse, error)
	// 发送心跳
	Heartbeat(ctx context.Context, status *models.HeartbeatRequest) (*models.HeartbeatResponse, error)
	// 获取批次
	GetBatches(ctx context.Context, limit int) ([]*models.TaskBatch, error)
	// 获取任务
	GetTasks(ctx context.Context, batchId int64) ([]*models.CollectionTask, error)
	// 更新批次状态
	UpdateBatchStatus(ctx context.Context, batchId int64, status *models.StatusUpdate) error
	// 更新任务状态
	UpdateTaskStatus(ctx context.Context, taskId int64, status *models.StatusUpdate) error
	// 上传结果
	UploadResults(ctx context.Context, results []*models.CollectionResult) error
	// 上传日志
	UploadLogs(ctx context.Context, logs []*models.LogEntry) error
}

// Client 实现API客户端接口
type Client struct {
	httpClient  *http.Client
	baseURL     string
	collectorID string
	token       string
}

// NewClient 创建新的API客户端
func NewClient(cfg *config.Config) (*Client, error) {
	// 创建HTTP客户端
	httpClient := &http.Client{
		Timeout: 30 * time.Second,
	}

	// 如果启用TLS，配置TLS
	if cfg.TLSEnabled {
		tlsConfig := &tls.Config{}

		// 添加CA证书
		if cfg.TLSCAFile != "" {
			caCert, err := os.ReadFile(cfg.TLSCAFile)
			if err != nil {
				return nil, fmt.Errorf("读取CA证书失败: %w", err)
			}

			caCertPool := x509.NewCertPool()
			caCertPool.AppendCertsFromPEM(caCert)
			tlsConfig.RootCAs = caCertPool
		}

		// 添加客户端证书
		if cfg.TLSCertFile != "" && cfg.TLSKeyFile != "" {
			cert, err := tls.LoadX509KeyPair(cfg.TLSCertFile, cfg.TLSKeyFile)
			if err != nil {
				return nil, fmt.Errorf("加载客户端证书失败: %w", err)
			}
			tlsConfig.Certificates = []tls.Certificate{cert}
		}

		httpClient.Transport = &http.Transport{
			TLSClientConfig: tlsConfig,
		}
	}

	return &Client{
		httpClient:  httpClient,
		baseURL:     cfg.ServerURL,
		collectorID: cfg.CollectorID,
	}, nil
}

// Register 注册采集器
func (c *Client) Register(ctx context.Context, info *models.CollectorInfo) (*models.RegisterResponse, error) {
	url := fmt.Sprintf("%s/api/v1/collectors/register", c.baseURL)
	
	// 构建请求体
	body, err := json.Marshal(info)
	if err != nil {
		return nil, fmt.Errorf("序列化请求体失败: %w", err)
	}
	
	// 发送请求
	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewReader(body))
	if err != nil {
		return nil, fmt.Errorf("创建请求失败: %w", err)
	}
	
	req.Header.Set("Content-Type", "application/json")
	
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("发送请求失败: %w", err)
	}
	defer resp.Body.Close()
	
	// 读取响应
	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("读取响应失败: %w", err)
	}
	
	// 检查状态码
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("服务端返回错误: %d %s", resp.StatusCode, string(respBody))
	}
	
	// 解析响应
	var apiResp struct {
		Code    int                     `json:"code"`
		Message string                  `json:"message"`
		Data    models.RegisterResponse `json:"data"`
	}
	
	if err := json.Unmarshal(respBody, &apiResp); err != nil {
		return nil, fmt.Errorf("解析响应失败: %w", err)
	}
	
	// 保存Token和CollectorID
	c.token = apiResp.Data.Token
	c.collectorID = apiResp.Data.CollectorID
	
	return &apiResp.Data, nil
}

// Heartbeat 发送心跳
func (c *Client) Heartbeat(ctx context.Context, status *models.HeartbeatRequest) (*models.HeartbeatResponse, error) {
	url := fmt.Sprintf("%s/api/v1/collectors/%s/heartbeat", c.baseURL, c.collectorID)
	
	// 构建请求体
	body, err := json.Marshal(status)
	if err != nil {
		return nil, fmt.Errorf("序列化请求体失败: %w", err)
	}
	
	// 发送请求
	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewReader(body))
	if err != nil {
		return nil, fmt.Errorf("创建请求失败: %w", err)
	}
	
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", c.token))
	
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("发送请求失败: %w", err)
	}
	defer resp.Body.Close()
	
	// 读取响应
	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("读取响应失败: %w", err)
	}
	
	// 检查状态码
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("服务端返回错误: %d %s", resp.StatusCode, string(respBody))
	}
	
	// 解析响应
	var apiResp struct {
		Code    int                      `json:"code"`
		Message string                   `json:"message"`
		Data    models.HeartbeatResponse `json:"data"`
	}
	
	if err := json.Unmarshal(respBody, &apiResp); err != nil {
		return nil, fmt.Errorf("解析响应失败: %w", err)
	}
	
	return &apiResp.Data, nil
}

// GetBatches 获取批次
func (c *Client) GetBatches(ctx context.Context, limit int) ([]*models.TaskBatch, error) {
	url := fmt.Sprintf("%s/api/v1/collectors/%s/batches?limit=%d", c.baseURL, c.collectorID, limit)
	
	// 发送请求
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("创建请求失败: %w", err)
	}
	
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", c.token))
	
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("发送请求失败: %w", err)
	}
	defer resp.Body.Close()
	
	// 读取响应
	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("读取响应失败: %w", err)
	}
	
	// 检查状态码
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("服务端返回错误: %d %s", resp.StatusCode, string(respBody))
	}
	
	// 解析响应
	var apiResp struct {
		Code    int                `json:"code"`
		Message string             `json:"message"`
		Data    []*models.TaskBatch `json:"data"`
	}
	
	if err := json.Unmarshal(respBody, &apiResp); err != nil {
		return nil, fmt.Errorf("解析响应失败: %w", err)
	}
	
	return apiResp.Data, nil
}

// GetTasks 获取任务
func (c *Client) GetTasks(ctx context.Context, batchId int64) ([]*models.CollectionTask, error) {
	url := fmt.Sprintf("%s/api/v1/collectors/%s/batches/%d/tasks", c.baseURL, c.collectorID, batchId)
	
	// 发送请求
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("创建请求失败: %w", err)
	}
	
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", c.token))
	
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("发送请求失败: %w", err)
	}
	defer resp.Body.Close()
	
	// 读取响应
	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("读取响应失败: %w", err)
	}
	
	// 检查状态码
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("服务端返回错误: %d %s", resp.StatusCode, string(respBody))
	}
	
	// 解析响应
	var apiResp struct {
		Code    int                     `json:"code"`
		Message string                  `json:"message"`
		Data    []*models.CollectionTask `json:"data"`
	}
	
	if err := json.Unmarshal(respBody, &apiResp); err != nil {
		return nil, fmt.Errorf("解析响应失败: %w", err)
	}
	
	return apiResp.Data, nil
}

// UpdateBatchStatus 更新批次状态
func (c *Client) UpdateBatchStatus(ctx context.Context, batchId int64, status *models.StatusUpdate) error {
	url := fmt.Sprintf("%s/api/v1/collectors/%s/batches/%d/status", c.baseURL, c.collectorID, batchId)
	
	// 构建请求体
	body, err := json.Marshal(status)
	if err != nil {
		return fmt.Errorf("序列化请求体失败: %w", err)
	}
	
	// 发送请求
	req, err := http.NewRequestWithContext(ctx, "PUT", url, bytes.NewReader(body))
	if err != nil {
		return fmt.Errorf("创建请求失败: %w", err)
	}
	
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", c.token))
	
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("发送请求失败: %w", err)
	}
	defer resp.Body.Close()
	
	// 检查状态码
	if resp.StatusCode != http.StatusOK {
		respBody, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("服务端返回错误: %d %s", resp.StatusCode, string(respBody))
	}
	
	return nil
}

// UpdateTaskStatus 更新任务状态
func (c *Client) UpdateTaskStatus(ctx context.Context, taskId int64, status *models.StatusUpdate) error {
	url := fmt.Sprintf("%s/api/v1/collectors/%s/tasks/%d/status", c.baseURL, c.collectorID, taskId)
	
	// 构建请求体
	body, err := json.Marshal(status)
	if err != nil {
		return fmt.Errorf("序列化请求体失败: %w", err)
	}
	
	// 发送请求
	req, err := http.NewRequestWithContext(ctx, "PUT", url, bytes.NewReader(body))
	if err != nil {
		return fmt.Errorf("创建请求失败: %w", err)
	}
	
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", c.token))
	
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("发送请求失败: %w", err)
	}
	defer resp.Body.Close()
	
	// 检查状态码
	if resp.StatusCode != http.StatusOK {
		respBody, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("服务端返回错误: %d %s", resp.StatusCode, string(respBody))
	}
	
	return nil
}

// UploadResults 上传采集结果
func (c *Client) UploadResults(ctx context.Context, results []*models.CollectionResult) error {
	url := fmt.Sprintf("%s/api/v1/collectors/%s/results", c.baseURL, c.collectorID)
	
	// 构建请求体
	body, err := json.Marshal(results)
	if err != nil {
		return fmt.Errorf("序列化请求体失败: %w", err)
	}
	
	// 发送请求
	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewReader(body))
	if err != nil {
		return fmt.Errorf("创建请求失败: %w", err)
	}
	
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", c.token))
	
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("发送请求失败: %w", err)
	}
	defer resp.Body.Close()
	
	// 检查状态码
	if resp.StatusCode != http.StatusOK {
		respBody, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("服务端返回错误: %d %s", resp.StatusCode, string(respBody))
	}
	
	return nil
}

// UploadLogs 上传日志
func (c *Client) UploadLogs(ctx context.Context, logs []*models.LogEntry) error {
	url := fmt.Sprintf("%s/api/v1/collectors/%s/logs", c.baseURL, c.collectorID)
	
	// 构建请求体
	body, err := json.Marshal(logs)
	if err != nil {
		return fmt.Errorf("序列化请求体失败: %w", err)
	}
	
	// 发送请求
	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewReader(body))
	if err != nil {
		return fmt.Errorf("创建请求失败: %w", err)
	}
	
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", c.token))
	
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("发送请求失败: %w", err)
	}
	defer resp.Body.Close()
	
	// 检查状态码
	if resp.StatusCode != http.StatusOK {
		respBody, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("服务端返回错误: %d %s", resp.StatusCode, string(respBody))
	}
	
	return nil
} 