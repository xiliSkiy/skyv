package utils

import (
	"net"
	"os"
	"runtime"
	"time"
)

// GetLocalIP 获取本机IP地址
func GetLocalIP() (string, error) {
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		return "", err
	}
	
	for _, addr := range addrs {
		if ipnet, ok := addr.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if ipnet.IP.To4() != nil {
				return ipnet.IP.String(), nil
			}
		}
	}
	
	return "127.0.0.1", nil
}

// GetHostname 获取主机名
func GetHostname() string {
	hostname, err := os.Hostname()
	if err != nil {
		return "unknown"
	}
	return hostname
}

// GetOSInfo 获取操作系统信息
func GetOSInfo() string {
	return runtime.GOOS + "-" + runtime.GOARCH
}

// GetSystemMetrics 获取系统指标
func GetSystemMetrics() map[string]float64 {
	metrics := make(map[string]float64)
	
	// 获取CPU使用率
	metrics["cpu"] = 0.0
	
	// 获取内存使用率
	var memStats runtime.MemStats
	runtime.ReadMemStats(&memStats)
	metrics["memory"] = float64(memStats.Alloc) / float64(memStats.Sys)
	
	// 模拟磁盘使用率
	metrics["disk"] = 0.4
	
	return metrics
}

// FormatTime 格式化时间
func FormatTime(t time.Time) string {
	return t.Format("2006-01-02T15:04:05Z07:00")
} 