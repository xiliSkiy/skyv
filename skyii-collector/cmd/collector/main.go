package main

import (
	"flag"
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/skyeye/skyii-collector/pkg/config"
	"github.com/skyeye/skyii-collector/pkg/core"
)

var (
	configFile = flag.String("config", "config.yaml", "配置文件路径")
	version    = flag.Bool("version", false, "显示版本信息")
	register   = flag.Bool("register", false, "使用令牌注册采集器")
	token      = flag.String("token", "", "注册令牌")
	server     = flag.String("server", "", "服务器地址")
)

// 版本信息，构建时注入
var (
	Version   = "dev"
	BuildTime = "unknown"
	GitCommit = "unknown"
)

func main() {
	flag.Parse()

	// 显示版本信息
	if *version {
		fmt.Printf("SkyEye Collector\n")
		fmt.Printf("Version: %s\n", Version)
		fmt.Printf("Build Time: %s\n", BuildTime)
		fmt.Printf("Git Commit: %s\n", GitCommit)
		return
	}

	// 加载配置
	cfg, err := config.LoadConfig(*configFile)
	if err != nil {
		log.Fatalf("加载配置失败: %v", err)
	}

	// 注册模式
	if *register {
		if *token == "" || *server == "" {
			log.Fatalf("注册模式需要指定token和server参数")
		}
		if err := core.RegisterCollector(*token, *server); err != nil {
			log.Fatalf("注册失败: %v", err)
		}
		log.Println("注册成功！")
		return
	}

	// 创建采集器实例
	collector, err := core.NewCollector(cfg)
	if err != nil {
		log.Fatalf("创建采集器失败: %v", err)
	}

	// 启动采集器
	if err := collector.Start(); err != nil {
		log.Fatalf("启动采集器失败: %v", err)
	}
	log.Printf("采集器已启动，版本: %s", Version)

	// 等待退出信号
	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, syscall.SIGINT, syscall.SIGTERM)
	<-sigCh

	// 优雅关闭
	log.Println("正在关闭采集器...")
	collector.Stop()
	log.Println("采集器已关闭")
} 