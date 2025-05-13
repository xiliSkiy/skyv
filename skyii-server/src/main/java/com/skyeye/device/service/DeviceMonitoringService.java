package com.skyeye.device.service;

import java.util.Map;

/**
 * 设备监控服务接口
 */
public interface DeviceMonitoringService {

    /**
     * 获取设备流地址
     * @param deviceId 设备ID
     * @return 设备流地址
     */
    String getDeviceStreamUrl(Long deviceId);

    /**
     * 启动设备监控
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean startDeviceMonitoring(Long deviceId);

    /**
     * 停止设备监控
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean stopDeviceMonitoring(Long deviceId);

    /**
     * 获取设备监控状态
     * @param deviceId 设备ID
     * @return 监控状态
     */
    Map<String, Boolean> getDeviceMonitoringStatus(Long deviceId);

    /**
     * 截取监控画面
     * @param deviceId 设备ID
     * @return 截图保存路径
     */
    String captureSnapshot(Long deviceId);

    /**
     * 开始录制
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean startRecording(Long deviceId);

    /**
     * 停止录制
     * @param deviceId 设备ID
     * @return 录制文件保存路径
     */
    String stopRecording(Long deviceId);
} 