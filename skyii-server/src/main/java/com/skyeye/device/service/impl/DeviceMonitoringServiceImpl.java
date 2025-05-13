package com.skyeye.device.service.impl;

import com.skyeye.device.entity.Device;
import com.skyeye.device.repository.DeviceRepository;
import com.skyeye.device.service.DeviceMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备监控服务实现类
 */
@Slf4j
@Service
public class DeviceMonitoringServiceImpl implements DeviceMonitoringService {

    @Autowired
    private DeviceRepository deviceRepository;
    
    // 存储设备监控状态
    private final Map<Long, Boolean> monitoringStatusMap = new ConcurrentHashMap<>();
    
    // 存储设备录制状态
    private final Map<Long, Boolean> recordingStatusMap = new ConcurrentHashMap<>();

    @Override
    public String getDeviceStreamUrl(Long deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            log.error("设备不存在: {}", deviceId);
            return null;
        }
        
        Device device = deviceOpt.get();
        if (device.getStatus() != 1) {
            log.error("设备不在线: {}", deviceId);
            return null;
        }
        
        // 如果设备有RTSP地址，直接返回
        if (device.getRtspUrl() != null && !device.getRtspUrl().isEmpty()) {
            return device.getRtspUrl();
        }
        
        // 否则根据设备信息构造RTSP地址
        // 这里只是示例，实际项目中应该根据设备类型和协议生成对应的URL
        String rtspUrl = String.format("rtsp://%s:%s@%s:%d/stream", 
                device.getUsername(), 
                device.getPassword(), 
                device.getIpAddress(), 
                device.getPort());
                
        // 在实际项目中，这里可能需要调用流媒体服务器的API，将RTSP转为HLS或WebRTC
        // 这里简单返回一个模拟的HLS地址
        return "http://example.com/live/" + deviceId + "/index.m3u8";
    }

    @Override
    public boolean startDeviceMonitoring(Long deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            log.error("设备不存在: {}", deviceId);
            return false;
        }
        
        Device device = deviceOpt.get();
        if (device.getStatus() != 1) {
            log.error("设备不在线: {}", deviceId);
            return false;
        }
        
        // 在实际项目中，这里应该调用流媒体服务器API启动流转发
        // 这里简单记录设备监控状态
        monitoringStatusMap.put(deviceId, true);
        log.info("启动设备监控: {}", deviceId);
        return true;
    }

    @Override
    public boolean stopDeviceMonitoring(Long deviceId) {
        // 在实际项目中，这里应该调用流媒体服务器API停止流转发
        // 这里简单记录设备监控状态
        monitoringStatusMap.put(deviceId, false);
        
        // 如果设备正在录制，也停止录制
        if (recordingStatusMap.getOrDefault(deviceId, false)) {
            stopRecording(deviceId);
        }
        
        log.info("停止设备监控: {}", deviceId);
        return true;
    }

    @Override
    public Map<String, Boolean> getDeviceMonitoringStatus(Long deviceId) {
        Map<String, Boolean> status = new HashMap<>();
        status.put("isMonitoring", monitoringStatusMap.getOrDefault(deviceId, false));
        status.put("isRecording", recordingStatusMap.getOrDefault(deviceId, false));
        return status;
    }

    @Override
    public String captureSnapshot(Long deviceId) {
        if (!monitoringStatusMap.getOrDefault(deviceId, false)) {
            log.error("设备未启动监控，无法截图: {}", deviceId);
            return null;
        }
        
        // 在实际项目中，这里应该调用流媒体服务器API获取截图
        // 这里简单返回一个模拟的截图路径
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String snapshotPath = "/snapshots/" + deviceId + "_" + timestamp + ".jpg";
        log.info("设备截图: {}, 保存路径: {}", deviceId, snapshotPath);
        return snapshotPath;
    }

    @Override
    public boolean startRecording(Long deviceId) {
        if (!monitoringStatusMap.getOrDefault(deviceId, false)) {
            log.error("设备未启动监控，无法录制: {}", deviceId);
            return false;
        }
        
        // 在实际项目中，这里应该调用流媒体服务器API启动录制
        // 这里简单记录设备录制状态
        recordingStatusMap.put(deviceId, true);
        log.info("开始录制: {}", deviceId);
        return true;
    }

    @Override
    public String stopRecording(Long deviceId) {
        if (!recordingStatusMap.getOrDefault(deviceId, false)) {
            log.error("设备未在录制: {}", deviceId);
            return null;
        }
        
        // 在实际项目中，这里应该调用流媒体服务器API停止录制
        // 这里简单记录设备录制状态
        recordingStatusMap.put(deviceId, false);
        
        // 返回一个模拟的录制文件路径
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String recordingPath = "/recordings/" + deviceId + "_" + timestamp + ".mp4";
        log.info("停止录制: {}, 保存路径: {}", deviceId, recordingPath);
        return recordingPath;
    }
} 