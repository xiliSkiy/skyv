package models

// CollectorInfo 采集端信息
type CollectorInfo struct {
	CollectorID   string   `json:"collectorId"`
	Hostname      string   `json:"hostname"`
	IP            string   `json:"ip"`
	Version       string   `json:"version"`
	Capabilities  []string `json:"capabilities"`
	Tags          []string `json:"tags,omitempty"`
}

// RegisterResponse 注册响应
type RegisterResponse struct {
	CollectorID string `json:"collectorId"`
	Token       string `json:"token"`
	ServerTime  string `json:"serverTime"`
}

// HeartbeatMetrics 心跳性能指标
type HeartbeatMetrics struct {
	CPU          float64 `json:"cpu"`
	Memory       float64 `json:"memory"`
	Disk         float64 `json:"disk"`
	RunningTasks int     `json:"runningTasks"`
}

// HeartbeatRequest 心跳请求
type HeartbeatRequest struct {
	Timestamp string          `json:"timestamp"`
	Status    string          `json:"status"`
	Metrics   HeartbeatMetrics `json:"metrics"`
	Error     string          `json:"error,omitempty"`
}

// HeartbeatResponse 心跳响应
type HeartbeatResponse struct {
	ServerTime string `json:"serverTime"`
	Action     string `json:"action"`
}

// TaskBatch 任务批次
type TaskBatch struct {
	BatchID       int64  `json:"batchId"`
	TaskID        int64  `json:"taskId"`
	BatchName     string `json:"batchName"`
	Status        string `json:"status"`
	Priority      int    `json:"priority"`
	ScheduledTime string `json:"scheduledTime"`
}

// CollectionTask 采集任务
type CollectionTask struct {
	TaskID        int64  `json:"taskId"`
	BatchID       int64  `json:"batchId"`
	DeviceID      int64  `json:"deviceId"`
	MetricID      int64  `json:"metricId"`
	DeviceName    string `json:"deviceName"`
	MetricName    string `json:"metricName"`
	TargetAddress string `json:"targetAddress"`
	TargetPort    int    `json:"targetPort"`
	CollectType   string `json:"collectType"`
	CollectParams string `json:"collectParams"`
}

// StatusUpdate 状态更新
type StatusUpdate struct {
	Status        string `json:"status"`
	StartTime     string `json:"startTime,omitempty"`
	EndTime       string `json:"endTime,omitempty"`
	ExecutionTime int    `json:"executionTime,omitempty"`
	Message       string `json:"message,omitempty"`
}

// CollectionResult 采集结果
type CollectionResult struct {
	TaskID        int64  `json:"taskId"`
	BatchID       int64  `json:"batchId"`
	DeviceID      int64  `json:"deviceId"`
	MetricID      int64  `json:"metricId"`
	ResultValue   string `json:"resultValue"`
	ResultType    string `json:"resultType"`
	Status        string `json:"status"`
	ExecutionTime int    `json:"executionTime"`
	Timestamp     string `json:"timestamp"`
}

// LogEntry 日志条目
type LogEntry struct {
	Level     string                 `json:"level"`
	Message   string                 `json:"message"`
	Timestamp string                 `json:"timestamp"`
	TaskID    int64                  `json:"taskId,omitempty"`
	BatchID   int64                  `json:"batchId,omitempty"`
	Context   map[string]interface{} `json:"context,omitempty"`
} 