package com.skyeye.monitoring.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.skyeye.monitoring.dto.ZoneDTO;
import com.skyeye.monitoring.entity.Zone;

/**
 * 监控区域服务接口
 */
public interface ZoneService {
    
    /**
     * 根据ID获取区域
     * @param id 区域ID
     * @return 区域信息
     */
    ZoneDTO getById(Long id);
    
    /**
     * 获取所有区域列表
     * @return 区域列表
     */
    List<ZoneDTO> listAll();
    
    /**
     * 分页查询区域
     * @param pageable 分页参数
     * @return 分页区域列表
     */
    Page<ZoneDTO> page(Pageable pageable);
    
    /**
     * 条件查询区域
     * @param zoneName 区域名称（模糊查询）
     * @param parentId 父ID
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页区域列表
     */
    Page<ZoneDTO> findByConditions(String zoneName, Long parentId, Integer status, Pageable pageable);
    
    /**
     * 根据父ID获取区域列表
     * @param parentId 父ID
     * @return 区域列表
     */
    List<ZoneDTO> findByParentId(Long parentId);
    
    /**
     * 保存区域配置
     * @param zoneDTO 区域信息
     * @return 保存后的区域信息
     */
    ZoneDTO save(ZoneDTO zoneDTO);
    
    /**
     * 更新区域配置
     * @param id 区域ID
     * @param zoneDTO 区域信息
     * @return 更新后的区域信息
     */
    ZoneDTO update(Long id, ZoneDTO zoneDTO);
    
    /**
     * 删除区域配置
     * @param id 区域ID
     */
    void delete(Long id);
    
    /**
     * 检查区域编码是否已存在
     * @param zoneCode 区域编码
     * @param id 排除的区域ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isZoneCodeExist(String zoneCode, Long id);
    
    /**
     * 获取区域统计信息
     * @return 统计信息
     */
    Map<String, Object> getZoneStatistics();
    
    /**
     * 更新区域状态
     * @param id 区域ID
     * @param status 状态
     * @return 更新后的区域信息
     */
    ZoneDTO updateStatus(Long id, Integer status);
    
    /**
     * 获取区域树形结构
     * @return 区域树形结构
     */
    List<Map<String, Object>> getZoneTree();
    
    /**
     * 实体转DTO
     * @param zone 实体对象
     * @return DTO对象
     */
    ZoneDTO convertToDTO(Zone zone);
    
    /**
     * DTO转实体
     * @param zoneDTO DTO对象
     * @return 实体对象
     */
    Zone convertToEntity(ZoneDTO zoneDTO);
} 