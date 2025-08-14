package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.DeviceDto;
import com.skyeye.device.dto.DeviceQueryRequest;
import com.skyeye.device.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 设备管理控制器
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * 分页查询设备列表
     */
    @GetMapping
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<Page<DeviceDto>> getDeviceList(DeviceQueryRequest request) {
        log.info("Getting device list with request: {}", request);
        
        Page<DeviceDto> result = deviceService.getDeviceList(request);
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 获取所有设备列表（不分页）
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<DeviceDto>> getAllDevices() {
        log.info("Getting all devices");
        
        List<DeviceDto> result = deviceService.getAllDevices();
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 根据ID获取设备详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<DeviceDto> getDeviceById(@PathVariable Long id) {
        log.info("Getting device by id: {}", id);
        
        DeviceDto result = deviceService.getDeviceById(id);
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 创建设备
     */
    @PostMapping
    // @PreAuthorize("hasAuthority('device:create')")
    public ApiResponse<DeviceDto> createDevice(@Valid @RequestBody DeviceDto deviceDto) {
        log.info("Creating device: {}", deviceDto.getName());
        
        DeviceDto result = deviceService.createDevice(deviceDto);
        return ApiResponse.success("创建成功", result);
    }

    /**
     * 更新设备
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('device:update')")
    public ApiResponse<DeviceDto> updateDevice(@PathVariable Long id, 
                                             @Valid @RequestBody DeviceDto deviceDto) {
        log.info("Updating device with id: {}", id);
        
        DeviceDto result = deviceService.updateDevice(id, deviceDto);
        return ApiResponse.success("更新成功", result);
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('device:delete')")
    public ApiResponse<Void> deleteDevice(@PathVariable Long id) {
        log.info("Deleting device with id: {}", id);
        
        deviceService.deleteDevice(id);
        return ApiResponse.success("删除成功", (Void) null);
    }

    /**
     * 批量删除设备
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('device:delete')")
    public ApiResponse<Void> batchDeleteDevices(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        log.info("Batch deleting devices with ids: {}", ids);
        
        deviceService.batchDeleteDevices(ids);
        return ApiResponse.success("批量删除成功", (Void) null);
    }

    /**
     * 测试设备连接
     */
    @PostMapping("/{id}/test")
    @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<Map<String, Object>> testDeviceConnection(@PathVariable Long id) {
        log.info("Testing device connection for id: {}", id);
        
        Map<String, Object> result = deviceService.testDeviceConnection(id);
        return ApiResponse.success("连接测试完成", result);
    }

    /**
     * 获取设备状态统计
     */
    @GetMapping("/stats")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<Map<String, Object>> getDeviceStats() {
        log.info("Getting device statistics");
        
        Map<String, Object> result = deviceService.getDeviceStats();
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 检查设备名称是否存在
     */
    @GetMapping("/check/name")
    @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<Boolean> checkNameExists(@RequestParam String name) {
        log.debug("Checking if device name exists: {}", name);
        
        boolean exists = deviceService.existsByName(name);
        return ApiResponse.success("检查完成", exists);
    }

    /**
     * 检查IP地址是否存在
     */
    @GetMapping("/check/ip")
    @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<Boolean> checkIpExists(@RequestParam String ipAddress) {
        log.debug("Checking if IP address exists: {}", ipAddress);
        
        boolean exists = deviceService.existsByIpAddress(ipAddress);
        return ApiResponse.success("检查完成", exists);
    }

    /**
     * 获取设备分组列表
     */
    @GetMapping("/groups")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<Map<String, Object>>> getDeviceGroups() {
        log.info("Getting device groups - redirecting to DeviceGroupController");
        
        // 提示：此接口已迁移到 /api/device-groups
        // 为了保持兼容性，这里仍返回Mock数据
        // 建议前端更新为使用 /api/device-groups 接口
        List<Map<String, Object>> groups = Arrays.asList(
            Map.of(
                "id", 1L,
                "name", "前端摄像头",
                "type", "normal",
                "description", "负责监控园区前门区域的摄像设备",
                "deviceCount", 12,
                "onlineCount", 10,
                "alertCount", 1
            ),
            Map.of(
                "id", 2L,
                "name", "后门摄像头", 
                "type", "normal",
                "description", "负责监控园区后门区域的摄像设备",
                "deviceCount", 8,
                "onlineCount", 7,
                "alertCount", 0
            ),
            Map.of(
                "id", 3L,
                "name", "温湿度传感器",
                "type", "normal", 
                "description", "所有温湿度传感器设备",
                "deviceCount", 6,
                "onlineCount", 6,
                "alertCount", 1
            )
        );
        
        return ApiResponse.success("查询成功", groups);
    }

    /**
     * 获取设备区域列表
     */
    @GetMapping("/areas")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<Map<String, Object>>> getDeviceAreas() {
        log.info("Getting device areas");
        
        // 临时Mock数据，实际项目中应从数据库查询
        List<Map<String, Object>> areas = Arrays.asList(
            Map.of(
                "id", 1L,
                "name", "总部大楼",
                "type", "building",
                "path", "/总部大楼",
                "status", "正常",
                "address", "北京市朝阳区科技园区88号",
                "deviceCount", 120,
                "onlineCount", 115,
                "alertCount", 2
            ),
            Map.of(
                "id", 2L,
                "name", "研发中心",
                "type", "building",
                "path", "/研发中心", 
                "status", "正常",
                "address", "北京市海淀区中关村南大街5号",
                "deviceCount", 80,
                "onlineCount", 78,
                "alertCount", 0
            ),
            Map.of(
                "id", 3L,
                "name", "生产基地",
                "type", "building",
                "path", "/生产基地",
                "status", "维护中",
                "address", "河北省廊坊市开发区创业路18号", 
                "deviceCount", 150,
                "onlineCount", 130,
                "alertCount", 5
            )
        );
        
        return ApiResponse.success("查询成功", areas);
    }

    /**
     * 获取设备标签列表
     */
    @GetMapping("/tags")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<Map<String, Object>>> getDeviceTags() {
        log.info("Getting device tags");
        
        // 临时Mock数据，实际项目中应从数据库查询
        List<Map<String, Object>> tags = Arrays.asList(
            Map.of(
                "id", 1L,
                "name", "重要设备",
                "color", "#E6A23C",
                "description", "关键业务相关的重要监控设备",
                "deviceCount", 25
            ),
            Map.of(
                "id", 2L,
                "name", "高清",
                "color", "#67C23A", 
                "description", "1080P及以上分辨率的高清设备",
                "deviceCount", 45
            ),
            Map.of(
                "id", 3L,
                "name", "夜视",
                "color", "#409EFF",
                "description", "具备夜视功能的监控设备",
                "deviceCount", 32
            ),
            Map.of(
                "id", 4L,
                "name", "智能分析",
                "color", "#F56C6C",
                "description", "支持AI智能分析功能的设备", 
                "deviceCount", 18
            )
        );
        
        return ApiResponse.success("查询成功", tags);
    }

    /**
     * 获取设备模板列表
     */
    @GetMapping("/templates")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<Map<String, Object>>> getDeviceTemplates() {
        log.info("Getting device templates");
        
        // 临时Mock数据，实际项目中应从数据库查询
        List<Map<String, Object>> templates = new ArrayList<>();
        
        Map<String, Object> template1 = new HashMap<>();
        template1.put("id", 1L);
        template1.put("name", "海康摄像头标准模板");
        template1.put("subtitle", "适用于海康威视网络摄像头");
        template1.put("description", "海康威视网络摄像头标准配置模板，包含基本参数和网络设置");
        template1.put("isOfficial", true);
        template1.put("icon", "VideoCamera");
        template1.put("iconBg", "#ecf5ff");
        template1.put("iconColor", "#409EFF");
        template1.put("usageCount", 15);
        template1.put("updateTime", "2023-03-10");
        template1.put("tags", Arrays.asList("RTSP", "ONVIF", "H.264"));
        templates.add(template1);
        
        Map<String, Object> template2 = new HashMap<>();
        template2.put("id", 2L);
        template2.put("name", "大华摄像头标准模板");
        template2.put("subtitle", "适用于大华网络摄像头");
        template2.put("description", "大华网络摄像头标准配置模板，包含基本参数和网络设置");
        template2.put("isOfficial", true);
        template2.put("icon", "VideoCamera");
        template2.put("iconBg", "#f0f9eb");
        template2.put("iconColor", "#67C23A");
        template2.put("usageCount", 8);
        template2.put("updateTime", "2023-03-12");
        template2.put("tags", Arrays.asList("RTSP", "ONVIF", "H.264"));
        templates.add(template2);
        
        Map<String, Object> template3 = new HashMap<>();
        template3.put("id", 3L);
        template3.put("name", "温湿度传感器模板");
        template3.put("subtitle", "适用于环境监测传感器");
        template3.put("description", "标准温湿度传感器配置模板");
        template3.put("isOfficial", true);
        template3.put("icon", "Document");
        template3.put("iconBg", "#fef0f0");
        template3.put("iconColor", "#F56C6C");
        template3.put("usageCount", 12);
        template3.put("updateTime", "2023-03-15");
        template3.put("tags", Arrays.asList("TCP", "Modbus", "RS485"));
        templates.add(template3);
        
        return ApiResponse.success("查询成功", templates);
    }

    /**
     * 获取设备协议列表
     */
    @GetMapping("/protocols")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<Map<String, Object>>> getDeviceProtocols() {
        log.info("Getting device protocols");
        
        // 临时Mock数据，实际项目中应从数据库查询
        List<Map<String, Object>> protocols = Arrays.asList(
            Map.of(
                "id", 1L,
                "name", "RTSP",
                "fullName", "Real Time Streaming Protocol",
                "description", "实时流传输协议，主要用于视频流传输",
                "category", "video",
                "defaultPort", 554,
                "supportedBy", Arrays.asList("海康威视", "大华", "宇视"),
                "deviceCount", 45
            ),
            Map.of(
                "id", 2L,
                "name", "ONVIF",
                "fullName", "Open Network Video Interface Forum",
                "description", "开放网络视频接口论坛协议，用于IP摄像机互操作",
                "category", "video",
                "defaultPort", 80,
                "supportedBy", Arrays.asList("海康威视", "大华", "宇视", "安讯士"),
                "deviceCount", 38
            ),
            Map.of(
                "id", 3L,
                "name", "Modbus",
                "fullName", "Modbus Protocol",
                "description", "串行通信协议，主要用于传感器和控制设备",
                "category", "sensor",
                "defaultPort", 502,
                "supportedBy", Arrays.asList("施耐德", "西门子", "ABB"),
                "deviceCount", 22
            ),
            Map.of(
                "id", 4L,
                "name", "HTTP",
                "fullName", "HyperText Transfer Protocol",
                "description", "超文本传输协议，用于Web接口通信",
                "category", "general",
                "defaultPort", 80,
                "supportedBy", Arrays.asList("通用"),
                "deviceCount", 15
            )
        );
        
        return ApiResponse.success("查询成功", protocols);
    }
} 