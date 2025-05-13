package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.service.DeviceMonitoringService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备监控控制器
 */
@Slf4j
@RestController
@RequestMapping("/devices")
@Api(tags = "设备监控")
public class DeviceMonitoringController {

    @Autowired
    private DeviceMonitoringService deviceMonitoringService;

    /**
     * 获取设备流地址
     */
    @GetMapping("/{id}/stream")
    @ApiOperation("获取设备流地址")
    public ApiResponse<String> getDeviceStreamUrl(
            @ApiParam("设备ID") @PathVariable("id") Long deviceId) {
        String streamUrl = deviceMonitoringService.getDeviceStreamUrl(deviceId);
        if (streamUrl != null) {
            return ApiResponse.success(streamUrl, "获取设备流地址成功");
        } else {
            return ApiResponse.error("获取设备流地址失败");
        }
    }

    /**
     * 启动设备监控
     */
    @PostMapping("/{id}/monitoring/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @ApiOperation("启动设备监控")
    public ApiResponse<Boolean> startDeviceMonitoring(
            @ApiParam("设备ID") @PathVariable("id") Long deviceId) {
        boolean result = deviceMonitoringService.startDeviceMonitoring(deviceId);
        if (result) {
            return ApiResponse.success(true, "启动设备监控成功");
        } else {
            return ApiResponse.error("启动设备监控失败");
        }
    }

    /**
     * 停止设备监控
     */
    @PostMapping("/{id}/monitoring/stop")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @ApiOperation("停止设备监控")
    public ApiResponse<Boolean> stopDeviceMonitoring(
            @ApiParam("设备ID") @PathVariable("id") Long deviceId) {
        boolean result = deviceMonitoringService.stopDeviceMonitoring(deviceId);
        if (result) {
            return ApiResponse.success(true, "停止设备监控成功");
        } else {
            return ApiResponse.error("停止设备监控失败");
        }
    }

    /**
     * 获取设备监控状态
     */
    @GetMapping("/{id}/monitoring/status")
    @ApiOperation("获取设备监控状态")
    public ApiResponse<Map<String, Boolean>> getDeviceMonitoringStatus(
            @ApiParam("设备ID") @PathVariable("id") Long deviceId) {
        Map<String, Boolean> status = deviceMonitoringService.getDeviceMonitoringStatus(deviceId);
        return ApiResponse.success(status, "获取设备监控状态成功");
    }

    /**
     * 截取监控画面
     */
    @PostMapping("/{id}/snapshot")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @ApiOperation("截取监控画面")
    public ApiResponse<String> captureSnapshot(
            @ApiParam("设备ID") @PathVariable("id") Long deviceId) {
        String snapshotPath = deviceMonitoringService.captureSnapshot(deviceId);
        if (snapshotPath != null) {
            return ApiResponse.success(snapshotPath, "截取监控画面成功");
        } else {
            return ApiResponse.error("截取监控画面失败");
        }
    }

    /**
     * 开始录制
     */
    @PostMapping("/{id}/recording/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @ApiOperation("开始录制")
    public ApiResponse<Boolean> startRecording(
            @ApiParam("设备ID") @PathVariable("id") Long deviceId) {
        boolean result = deviceMonitoringService.startRecording(deviceId);
        if (result) {
            return ApiResponse.success(true, "开始录制成功");
        } else {
            return ApiResponse.error("开始录制失败");
        }
    }

    /**
     * 停止录制
     */
    @PostMapping("/{id}/recording/stop")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @ApiOperation("停止录制")
    public ApiResponse<String> stopRecording(
            @ApiParam("设备ID") @PathVariable("id") Long deviceId) {
        String recordingPath = deviceMonitoringService.stopRecording(deviceId);
        if (recordingPath != null) {
            return ApiResponse.success(recordingPath, "停止录制成功");
        } else {
            return ApiResponse.error("停止录制失败");
        }
    }
} 