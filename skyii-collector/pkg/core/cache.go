package core

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"sync"
	"time"
)

// MetricData 指标数据
type MetricData struct {
	DeviceID   string      `json:"device_id"`
	MetricType string      `json:"metric_type"`
	Value      interface{} `json:"value"`
	Timestamp  int64       `json:"timestamp"`
	Status     string      `json:"status"`
}

// DataCache 数据缓存
type DataCache struct {
	data      []*MetricData
	capacity  int
	mu        sync.RWMutex
	cachePath string
}

// NewDataCache 创建新的数据缓存
func NewDataCache(capacity int) *DataCache {
	if capacity <= 0 {
		capacity = 1000 // 默认容量
	}

	cache := &DataCache{
		data:      make([]*MetricData, 0, capacity),
		capacity:  capacity,
		cachePath: "data/cache",
	}

	// 确保缓存目录存在
	os.MkdirAll(cache.cachePath, 0755)

	// 加载持久化的缓存数据
	cache.load()

	return cache
}

// Add 添加数据到缓存
func (c *DataCache) Add(data *MetricData) {
	c.mu.Lock()
	defer c.mu.Unlock()

	// 检查容量
	if len(c.data) >= c.capacity {
		// 如果已满，移除最旧的数据
		c.data = c.data[1:]
	}

	// 添加新数据
	c.data = append(c.data, data)

	// 定期持久化
	if len(c.data)%100 == 0 {
		go c.Flush()
	}
}

// GetBatch 获取一批数据
func (c *DataCache) GetBatch(size int) []*MetricData {
	c.mu.Lock()
	defer c.mu.Unlock()

	if size <= 0 || len(c.data) == 0 {
		return nil
	}

	// 调整批量大小
	if size > len(c.data) {
		size = len(c.data)
	}

	// 提取数据
	batch := make([]*MetricData, size)
	copy(batch, c.data[:size])

	// 更新缓存
	c.data = c.data[size:]

	return batch
}

// Clear 清空缓存
func (c *DataCache) Clear() {
	c.mu.Lock()
	defer c.mu.Unlock()

	c.data = make([]*MetricData, 0, c.capacity)
}

// Size 获取当前缓存大小
func (c *DataCache) Size() int {
	c.mu.RLock()
	defer c.mu.RUnlock()

	return len(c.data)
}

// Flush 将缓存数据持久化到磁盘
func (c *DataCache) Flush() error {
	c.mu.RLock()
	// 复制一份数据，避免长时间持有锁
	dataCopy := make([]*MetricData, len(c.data))
	copy(dataCopy, c.data)
	c.mu.RUnlock()

	if len(dataCopy) == 0 {
		return nil
	}

	// 序列化数据
	data, err := json.Marshal(dataCopy)
	if err != nil {
		return fmt.Errorf("序列化缓存数据失败: %v", err)
	}

	// 生成缓存文件名
	timestamp := time.Now().Unix()
	filename := fmt.Sprintf("%s/cache_%d.json", c.cachePath, timestamp)

	// 写入文件
	if err := ioutil.WriteFile(filename, data, 0644); err != nil {
		return fmt.Errorf("写入缓存文件失败: %v", err)
	}

	return nil
}

// load 从磁盘加载持久化的缓存数据
func (c *DataCache) load() {
	// 读取缓存目录中的所有文件
	files, err := ioutil.ReadDir(c.cachePath)
	if err != nil {
		log.Printf("读取缓存目录失败: %v", err)
		return
	}

	for _, file := range files {
		if file.IsDir() {
			continue
		}

		// 读取缓存文件
		data, err := ioutil.ReadFile(fmt.Sprintf("%s/%s", c.cachePath, file.Name()))
		if err != nil {
			log.Printf("读取缓存文件 %s 失败: %v", file.Name(), err)
			continue
		}

		// 反序列化数据
		var cachedData []*MetricData
		if err := json.Unmarshal(data, &cachedData); err != nil {
			log.Printf("反序列化缓存数据失败: %v", err)
			continue
		}

		// 添加到内存缓存
		c.mu.Lock()
		for _, item := range cachedData {
			if len(c.data) < c.capacity {
				c.data = append(c.data, item)
			}
		}
		c.mu.Unlock()

		// 处理完后删除缓存文件
		os.Remove(fmt.Sprintf("%s/%s", c.cachePath, file.Name()))
	}

	log.Printf("从磁盘加载了 %d 条缓存数据", c.Size())
} 