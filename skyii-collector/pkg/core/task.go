package core

import (
	"container/heap"
	"sync"
	"time"
)

// Task 采集任务
type Task struct {
	DeviceID   string                 // 设备ID
	MetricType string                 // 指标类型
	Params     map[string]interface{} // 采集参数
	Schedule   time.Time              // 计划执行时间
	Priority   int                    // 优先级 (1最高，5最低)
	index      int                    // 堆索引
}

// 任务优先级队列
type priorityQueue []*Task

func (pq priorityQueue) Len() int { return len(pq) }

func (pq priorityQueue) Less(i, j int) bool {
	// 首先按优先级排序
	if pq[i].Priority != pq[j].Priority {
		return pq[i].Priority < pq[j].Priority
	}
	// 优先级相同，按计划时间排序
	return pq[i].Schedule.Before(pq[j].Schedule)
}

func (pq priorityQueue) Swap(i, j int) {
	pq[i], pq[j] = pq[j], pq[i]
	pq[i].index = i
	pq[j].index = j
}

func (pq *priorityQueue) Push(x interface{}) {
	n := len(*pq)
	task := x.(*Task)
	task.index = n
	*pq = append(*pq, task)
}

func (pq *priorityQueue) Pop() interface{} {
	old := *pq
	n := len(old)
	task := old[n-1]
	old[n-1] = nil  // 避免内存泄漏
	task.index = -1 // 标记为已移除
	*pq = old[0 : n-1]
	return task
}

// Scheduler 任务调度器
type Scheduler struct {
	tasks        priorityQueue    // 任务队列
	maxConcurrent int             // 最大并发任务数
	currentTasks int              // 当前正在执行的任务数
	mu           sync.Mutex       // 互斥锁
	cond         *sync.Cond       // 条件变量
}

// NewScheduler 创建新的任务调度器
func NewScheduler(maxConcurrent int) *Scheduler {
	s := &Scheduler{
		tasks:        make(priorityQueue, 0),
		maxConcurrent: maxConcurrent,
		currentTasks: 0,
	}
	s.cond = sync.NewCond(&s.mu)
	heap.Init(&s.tasks)
	return s
}

// ScheduleTask 调度新任务
func (s *Scheduler) ScheduleTask(task *Task) {
	s.mu.Lock()
	defer s.mu.Unlock()

	// 设置默认值
	if task.Schedule.IsZero() {
		task.Schedule = time.Now()
	}
	if task.Priority == 0 {
		task.Priority = 3 // 默认优先级
	}

	heap.Push(&s.tasks, task)
	s.cond.Signal() // 通知等待的消费者
}

// ScheduleTaskWithDelay 延迟调度任务
func (s *Scheduler) ScheduleTaskWithDelay(task *Task, delay time.Duration) {
	task.Schedule = time.Now().Add(delay)
	s.ScheduleTask(task)
}

// GetNextTask 获取下一个可执行的任务
func (s *Scheduler) GetNextTask() *Task {
	s.mu.Lock()
	defer s.mu.Unlock()

	// 等待有任务可执行
	for s.tasks.Len() == 0 || s.currentTasks >= s.maxConcurrent {
		s.cond.Wait()
	}

	// 检查是否有任务可执行
	if s.tasks.Len() == 0 {
		return nil
	}

	// 检查任务执行时间
	task := s.tasks[0]
	if task.Schedule.After(time.Now()) {
		return nil // 任务尚未到执行时间
	}

	// 取出任务
	task = heap.Pop(&s.tasks).(*Task)
	s.currentTasks++
	return task
}

// TaskComplete 标记任务完成
func (s *Scheduler) TaskComplete() {
	s.mu.Lock()
	defer s.mu.Unlock()

	s.currentTasks--
	s.cond.Signal() // 通知等待的任务可以开始执行
}

// GetPendingTaskCount 获取待处理任务数量
func (s *Scheduler) GetPendingTaskCount() int {
	s.mu.Lock()
	defer s.mu.Unlock()
	return s.tasks.Len()
}

// GetRunningTaskCount 获取正在执行的任务数量
func (s *Scheduler) GetRunningTaskCount() int {
	s.mu.Lock()
	defer s.mu.Unlock()
	return s.currentTasks
} 