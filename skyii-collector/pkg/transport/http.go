package transport

import (
	"bytes"
	"crypto/tls"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"time"

	"github.com/skyeye/skyii-collector/internal/models"
	"github.com/skyeye/skyii-collector/pkg/config"
)

// HTTPTransport HTTP传输实现
type HTTPTransport struct {
	client  *http.Client
	baseURL string
	apiKey  string
}

// NewHTTPTransport 创建HTTP传输
func NewHTTPTransport(config config.ServerConfig) (Transport, error) {
	// 配置HTTP客户端
	tlsConfig := &tls.Config{
		InsecureSkipVerify: !config.VerifySSL,
	}

	transport := &http.Transport{
		TLSClientConfig: tlsConfig,
	}

	client := &http.Client{
		Transport: transport,
		Timeout:   time.Duration(config.Timeout) * time.Second,
	}

	return &HTTPTransport{
		client:  client,
		baseURL: config.URL,
		apiKey:  config.APIKey,
	}, nil
}

// Register 注册采集器
func (t *HTTPTransport) Register(request *models.RegisterRequest) (*models.RegisterResponse, error) {
	resp, err := t.post("/collectors/register", request)
	if err != nil {
		return nil, err
	}

	var response models.RegisterResponse
	if err := json.Unmarshal(resp, &response); err != nil {
		return nil, fmt.Errorf("解析注册响应失败: %v", err)
	}

	return &response, nil
}

// Heartbeat 发送心跳
func (t *HTTPTransport) Heartbeat(request *models.HeartbeatRequest) (*models.HeartbeatResponse, error) {
	resp, err := t.post("/collectors/heartbeat", request)
	if err != nil {
		return nil, err
	}

	var response models.HeartbeatResponse
	if err := json.Unmarshal(resp, &response); err != nil {
		return nil, fmt.Errorf("解析心跳响应失败: %v", err)
	}

	return &response, nil
}

// SendMetrics 发送指标数据
func (t *HTTPTransport) SendMetrics(data *models.MetricsData) error {
	_, err := t.post("/collectors/metrics", data)
	return err
}

// GetTasks 获取任务
func (t *HTTPTransport) GetTasks() (*models.TasksResponse, error) {
	resp, err := t.get("/collectors/tasks")
	if err != nil {
		return nil, err
	}

	var response models.TasksResponse
	if err := json.Unmarshal(resp, &response); err != nil {
		return nil, fmt.Errorf("解析任务响应失败: %v", err)
	}

	return &response, nil
}

// ReportTaskStatus 报告任务状态
func (t *HTTPTransport) ReportTaskStatus(taskID string, status string, message string) error {
	req := map[string]interface{}{
		"task_id": taskID,
		"status":  status,
		"message": message,
	}
	_, err := t.post("/collectors/tasks/status", req)
	return err
}

// GetPlugins 获取插件列表
func (t *HTTPTransport) GetPlugins() (*models.PluginsResponse, error) {
	resp, err := t.get("/collectors/plugins")
	if err != nil {
		return nil, err
	}

	var response models.PluginsResponse
	if err := json.Unmarshal(resp, &response); err != nil {
		return nil, fmt.Errorf("解析插件响应失败: %v", err)
	}

	return &response, nil
}

// DownloadPlugin 下载插件
func (t *HTTPTransport) DownloadPlugin(pluginID string) ([]byte, error) {
	url := fmt.Sprintf("%s/collectors/plugins/%s/download", t.baseURL, pluginID)
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("创建请求失败: %v", err)
	}

	t.addAuthHeader(req)

	resp, err := t.client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("发送请求失败: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("下载插件失败, 状态码: %d", resp.StatusCode)
	}

	return ioutil.ReadAll(resp.Body)
}

// GetCollectorConfig 获取采集器配置
func (t *HTTPTransport) GetCollectorConfig() (*models.CollectorConfig, error) {
	resp, err := t.get("/collectors/config")
	if err != nil {
		return nil, err
	}

	var config models.CollectorConfig
	if err := json.Unmarshal(resp, &config); err != nil {
		return nil, fmt.Errorf("解析配置响应失败: %v", err)
	}

	return &config, nil
}

// post 发送POST请求
func (t *HTTPTransport) post(path string, data interface{}) ([]byte, error) {
	// 序列化请求数据
	jsonData, err := json.Marshal(data)
	if err != nil {
		return nil, fmt.Errorf("序列化请求数据失败: %v", err)
	}

	// 创建请求
	url := t.baseURL + path
	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("创建请求失败: %v", err)
	}

	// 设置请求头
	req.Header.Set("Content-Type", "application/json")
	t.addAuthHeader(req)

	// 发送请求
	resp, err := t.client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("发送请求失败: %v", err)
	}
	defer resp.Body.Close()

	// 读取响应
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("读取响应失败: %v", err)
	}

	// 检查状态码
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("请求失败, 状态码: %d, 响应: %s", resp.StatusCode, string(body))
	}

	return body, nil
}

// get 发送GET请求
func (t *HTTPTransport) get(path string) ([]byte, error) {
	// 创建请求
	url := t.baseURL + path
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("创建请求失败: %v", err)
	}

	// 设置请求头
	t.addAuthHeader(req)

	// 发送请求
	resp, err := t.client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("发送请求失败: %v", err)
	}
	defer resp.Body.Close()

	// 读取响应
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("读取响应失败: %v", err)
	}

	// 检查状态码
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("请求失败, 状态码: %d, 响应: %s", resp.StatusCode, string(body))
	}

	return body, nil
}

// addAuthHeader 添加认证头
func (t *HTTPTransport) addAuthHeader(req *http.Request) {
	if t.apiKey != "" {
		req.Header.Set("X-API-Key", t.apiKey)
	}
} 