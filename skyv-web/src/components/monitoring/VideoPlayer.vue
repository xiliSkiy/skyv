<template>
  <div class="video-player-container">
    <video
      ref="videoRef"
      class="video-player"
      autoplay
      muted
      @error="handleError"
    ></video>
    <div v-if="loading" class="loading-overlay">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <div class="loading-text">加载中...</div>
    </div>
    <div v-if="error" class="error-overlay">
      <el-icon class="error-icon"><WarningFilled /></el-icon>
      <div class="error-text">{{ errorMessage }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import Hls from 'hls.js'
import { ElMessage } from 'element-plus'

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  deviceId: {
    type: Number,
    required: true
  },
  autoplay: {
    type: Boolean,
    default: true
  }
})

const videoRef = ref(null)
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('视频加载失败')
let hls = null

// 初始化视频播放
const initPlayer = () => {
  if (!props.src) {
    loading.value = false
    error.value = true
    errorMessage.value = '无效的视频源'
    return
  }

  loading.value = true
  error.value = false

  const video = videoRef.value
  if (!video) return

  // 销毁之前的实例
  destroyPlayer()

  // 检查视频格式
  if (props.src.includes('.m3u8')) {
    // HLS 格式
    if (Hls.isSupported()) {
      hls = new Hls({
        debug: false,
        enableWorker: true,
        lowLatencyMode: true
      })
      
      hls.attachMedia(video)
      hls.on(Hls.Events.MEDIA_ATTACHED, () => {
        hls.loadSource(props.src)
        hls.on(Hls.Events.MANIFEST_PARSED, () => {
          if (props.autoplay) {
            video.play().catch(e => {
              console.error('自动播放失败', e)
            })
          }
          loading.value = false
        })
      })
      
      hls.on(Hls.Events.ERROR, (event, data) => {
        if (data.fatal) {
          switch (data.type) {
            case Hls.ErrorTypes.NETWORK_ERROR:
              console.error('网络错误', data)
              hls.startLoad()
              break
            case Hls.ErrorTypes.MEDIA_ERROR:
              console.error('媒体错误', data)
              hls.recoverMediaError()
              break
            default:
              destroyPlayer()
              error.value = true
              errorMessage.value = '视频加载失败'
              break
          }
        }
      })
    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
      // 原生支持 HLS
      video.src = props.src
      video.addEventListener('loadedmetadata', () => {
        if (props.autoplay) {
          video.play().catch(e => {
            console.error('自动播放失败', e)
          })
        }
        loading.value = false
      })
    } else {
      error.value = true
      errorMessage.value = '浏览器不支持 HLS 格式'
      loading.value = false
    }
  } else {
    // 其他格式 (mp4, webm等)
    video.src = props.src
    video.addEventListener('loadeddata', () => {
      if (props.autoplay) {
        video.play().catch(e => {
          console.error('自动播放失败', e)
        })
      }
      loading.value = false
    })
  }
}

// 销毁播放器
const destroyPlayer = () => {
  if (hls) {
    hls.destroy()
    hls = null
  }
  
  if (videoRef.value) {
    videoRef.value.removeAttribute('src')
    videoRef.value.load()
  }
}

// 处理错误
const handleError = (e) => {
  console.error('视频播放错误', e)
  loading.value = false
  error.value = true
  errorMessage.value = '视频加载失败'
}

// 监听src变化
watch(() => props.src, (newSrc) => {
  if (newSrc) {
    initPlayer()
  } else {
    destroyPlayer()
    loading.value = false
    error.value = true
    errorMessage.value = '无效的视频源'
  }
})

// 组件挂载时初始化
onMounted(() => {
  initPlayer()
})

// 组件销毁前清理
onBeforeUnmount(() => {
  destroyPlayer()
})
</script>

<style lang="scss" scoped>
.video-player-container {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #000;
  overflow: hidden;
}

.video-player {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.loading-overlay,
.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
}

.loading-icon,
.error-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

.loading-text,
.error-text {
  font-size: 14px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style> 