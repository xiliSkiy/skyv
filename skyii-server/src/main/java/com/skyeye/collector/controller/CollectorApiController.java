package com.skyeye.collector.controller;

import com.skyeye.collector.dto.CollectorRegisterDTO;
import com.skyeye.collector.dto.CollectorRegisterResponseDTO;
import com.skyeye.collector.dto.HeartbeatDTO;
import com.skyeye.collector.dto.HeartbeatResponseDTO;
import com.skyeye.collector.service.CollectorService;
import com.skyeye.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 采集端API控制器
 * 处理采集端与服务端的交互
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/collectors")
public class CollectorApiController {

    @Autowired
    private CollectorService collectorService;

    /**
     * 采集端注册
     *
     * @param registerDTO 注册请求DTO
     * @return 注册响应
     */
    @PostMapping("/register")
    public ApiResponse<CollectorRegisterResponseDTO> register(@RequestBody @Valid CollectorRegisterDTO registerDTO) {
        log.info("采集端注册请求: {}", registerDTO);
        CollectorRegisterResponseDTO response = collectorService.registerCollectorWithResponse(registerDTO);
        return ApiResponse.success(response);
    }

    /**
     * 采集端心跳
     *
     * @param collectorId 采集端ID
     * @param heartbeatDTO 心跳请求DTO
     * @return 心跳响应
     */
    @PostMapping("/{collectorId}/heartbeat")
    public ApiResponse<HeartbeatResponseDTO> heartbeat(
            @PathVariable String collectorId,
            @RequestBody @Valid HeartbeatDTO heartbeatDTO) {
        log.debug("接收到采集端[{}]心跳", collectorId);
        HeartbeatResponseDTO response = collectorService.processHeartbeat(collectorId, heartbeatDTO);
        return ApiResponse.success(response);
    }
} 