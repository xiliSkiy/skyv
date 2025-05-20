package com.skyeye.monitoring.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skyeye.common.response.ResponseResult;
import com.skyeye.monitoring.dto.ZoneDTO;
import com.skyeye.monitoring.service.ZoneService;

/**
 * 监控区域控制器
 */
@RestController
@RequestMapping("/v1/zones")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;
    
    /**
     * 获取区域列表
     * @param page 页码
     * @param size 每页大小
     * @param zoneName 区域名称（模糊查询）
     * @param parentId 父ID
     * @param status 状态
     * @param sortBy 排序字段
     * @param order 排序方式
     * @return 区域列表
     */
    @GetMapping
    public ResponseEntity<ResponseResult<Page<ZoneDTO>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String zoneName,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        
        Sort sort = "desc".equalsIgnoreCase(order) 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ZoneDTO> zones = zoneService.findByConditions(zoneName, parentId, status, pageable);
        
        return ResponseEntity.ok(ResponseResult.success(zones));
    }
    
    /**
     * 获取所有区域
     * @return 区域列表
     */
    @GetMapping("/all")
    public ResponseEntity<ResponseResult<List<ZoneDTO>>> listAll() {
        List<ZoneDTO> zones = zoneService.listAll();
        return ResponseEntity.ok(ResponseResult.success(zones));
    }
    
    /**
     * 根据ID获取区域
     * @param id 区域ID
     * @return 区域信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseResult<ZoneDTO>> getById(@PathVariable Long id) {
        ZoneDTO zone = zoneService.getById(id);
        return ResponseEntity.ok(ResponseResult.success(zone));
    }
    
    /**
     * 根据父ID获取区域列表
     * @param parentId 父ID
     * @return 区域列表
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ResponseResult<List<ZoneDTO>>> getByParentId(@PathVariable Long parentId) {
        List<ZoneDTO> zones = zoneService.findByParentId(parentId);
        return ResponseEntity.ok(ResponseResult.success(zones));
    }
    
    /**
     * 创建区域
     * @param zoneDTO 区域信息
     * @return 创建的区域信息
     */
    @PostMapping
    public ResponseEntity<ResponseResult<ZoneDTO>> create(@Valid @RequestBody ZoneDTO zoneDTO) {
        ZoneDTO createdZone = zoneService.save(zoneDTO);
        return ResponseEntity.ok(ResponseResult.success(createdZone));
    }
    
    /**
     * 更新区域
     * @param id 区域ID
     * @param zoneDTO 区域信息
     * @return 更新后的区域信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseResult<ZoneDTO>> update(
            @PathVariable Long id, 
            @Valid @RequestBody ZoneDTO zoneDTO) {
        
        ZoneDTO updatedZone = zoneService.update(id, zoneDTO);
        return ResponseEntity.ok(ResponseResult.success(updatedZone));
    }
    
    /**
     * 删除区域
     * @param id 区域ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseResult<Void>> delete(@PathVariable Long id) {
        zoneService.delete(id);
        return ResponseEntity.ok(ResponseResult.success());
    }
    
    /**
     * 更新区域状态
     * @param id 区域ID
     * @param status 状态
     * @return 更新后的区域信息
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseResult<ZoneDTO>> updateStatus(
            @PathVariable Long id, 
            @RequestParam Integer status) {
        
        ZoneDTO updatedZone = zoneService.updateStatus(id, status);
        return ResponseEntity.ok(ResponseResult.success(updatedZone));
    }
    
    /**
     * 获取区域统计信息
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ResponseResult<Map<String, Object>>> getStatistics() {
        Map<String, Object> statistics = zoneService.getZoneStatistics();
        return ResponseEntity.ok(ResponseResult.success(statistics));
    }
    
    /**
     * 获取区域树形结构
     * @return 区域树形结构
     */
    @GetMapping("/tree")
    public ResponseEntity<ResponseResult<List<Map<String, Object>>>> getZoneTree() {
        List<Map<String, Object>> tree = zoneService.getZoneTree();
        return ResponseEntity.ok(ResponseResult.success(tree));
    }
    
    /**
     * 检查区域编码是否存在
     * @param zoneCode 区域编码
     * @param id 排除的区域ID（用于更新时检查）
     * @return 是否存在
     */
    @GetMapping("/check-code")
    public ResponseEntity<ResponseResult<Boolean>> checkZoneCode(
            @RequestParam String zoneCode, 
            @RequestParam(required = false) Long id) {
        
        boolean exists = zoneService.isZoneCodeExist(zoneCode, id);
        return ResponseEntity.ok(ResponseResult.success(exists));
    }
    
    /**
     * 获取区域概览数据
     * @return 概览数据
     */
    @GetMapping("/overview")
    public ResponseEntity<ResponseResult<Map<String, Object>>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 从ZoneService获取统计数据
        Map<String, Object> statistics = zoneService.getZoneStatistics();
        
        // 提取并格式化数据
        overview.put("total", statistics.getOrDefault("totalCount", 0));
        overview.put("active", statistics.getOrDefault("activeCount", 0));
        overview.put("inactive", statistics.getOrDefault("inactiveCount", 0));
        
        return ResponseEntity.ok(ResponseResult.success(overview));
    }
} 