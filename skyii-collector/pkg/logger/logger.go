package logger

import (
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"strings"

	"github.com/sirupsen/logrus"
)

var (
	log           *logrus.Logger
	logLevelMap   map[string]logrus.Level
	defaultFields logrus.Fields
)

func init() {
	logLevelMap = map[string]logrus.Level{
		"trace": logrus.TraceLevel,
		"debug": logrus.DebugLevel,
		"info":  logrus.InfoLevel,
		"warn":  logrus.WarnLevel,
		"error": logrus.ErrorLevel,
		"fatal": logrus.FatalLevel,
		"panic": logrus.PanicLevel,
	}

	defaultFields = logrus.Fields{
		"collector": "skyii-collector",
	}

	log = logrus.New()
	log.SetFormatter(&logrus.TextFormatter{
		FullTimestamp:   true,
		TimestampFormat: "2006-01-02 15:04:05",
	})
	log.SetOutput(os.Stdout)
	log.SetLevel(logrus.InfoLevel)
}

// InitLogger 初始化日志配置
func InitLogger(level string, logFile string, addFields logrus.Fields) {
	// 设置日志级别
	if logLevel, exists := logLevelMap[strings.ToLower(level)]; exists {
		log.SetLevel(logLevel)
	}

	// 合并默认字段
	for k, v := range addFields {
		defaultFields[k] = v
	}

	// 如果指定了日志文件，则同时输出到文件和控制台
	if logFile != "" {
		logDir := filepath.Dir(logFile)
		if _, err := os.Stat(logDir); os.IsNotExist(err) {
			if err := os.MkdirAll(logDir, 0755); err != nil {
				log.Errorf("创建日志目录失败: %v", err)
				return
			}
		}

		file, err := os.OpenFile(logFile, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0666)
		if err != nil {
			log.Errorf("打开日志文件失败: %v", err)
			return
		}

		log.SetOutput(file)
	}
}

// Fields 创建带有默认字段的Entry
func Fields(fields logrus.Fields) *logrus.Entry {
	mergedFields := make(logrus.Fields)
	for k, v := range defaultFields {
		mergedFields[k] = v
	}
	for k, v := range fields {
		mergedFields[k] = v
	}
	return log.WithFields(mergedFields)
}

// Trace 跟踪日志
func Trace(args ...interface{}) {
	_, file, line, _ := runtime.Caller(1)
	log.WithFields(defaultFields).WithField("caller", fmt.Sprintf("%s:%d", filepath.Base(file), line)).Trace(args...)
}

// Debug 调试日志
func Debug(args ...interface{}) {
	_, file, line, _ := runtime.Caller(1)
	log.WithFields(defaultFields).WithField("caller", fmt.Sprintf("%s:%d", filepath.Base(file), line)).Debug(args...)
}

// Info 信息日志
func Info(args ...interface{}) {
	_, file, line, _ := runtime.Caller(1)
	log.WithFields(defaultFields).WithField("caller", fmt.Sprintf("%s:%d", filepath.Base(file), line)).Info(args...)
}

// Warn 警告日志
func Warn(args ...interface{}) {
	_, file, line, _ := runtime.Caller(1)
	log.WithFields(defaultFields).WithField("caller", fmt.Sprintf("%s:%d", filepath.Base(file), line)).Warn(args...)
}

// Error 错误日志
func Error(args ...interface{}) {
	_, file, line, _ := runtime.Caller(1)
	log.WithFields(defaultFields).WithField("caller", fmt.Sprintf("%s:%d", filepath.Base(file), line)).Error(args...)
}

// Fatal 致命错误日志
func Fatal(args ...interface{}) {
	_, file, line, _ := runtime.Caller(1)
	log.WithFields(defaultFields).WithField("caller", fmt.Sprintf("%s:%d", filepath.Base(file), line)).Fatal(args...)
}

// Panic 异常日志
func Panic(args ...interface{}) {
	_, file, line, _ := runtime.Caller(1)
	log.WithFields(defaultFields).WithField("caller", fmt.Sprintf("%s:%d", filepath.Base(file), line)).Panic(args...)
} 