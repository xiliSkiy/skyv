package com.skyeye.monitoring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skyeye.monitoring.entity.Zone;

/**
 * 监控区域数据访问层
 */
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    
    /**
     * 根据区域编码查找区域
     * @param zoneCode 区域编码
     * @return 区域对象
     */
    Optional<Zone> findByZoneCode(String zoneCode);
    
    /**
     * 根据父ID查找区域列表
     * @param parentId 父ID
     * @return 区域列表
     */
    List<Zone> findByParentId(Long parentId);
    
    /**
     * 根据父ID查找区域列表（分页）
     * @param parentId 父ID
     * @param pageable 分页参数
     * @return 分页区域列表
     */
    Page<Zone> findByParentId(Long parentId, Pageable pageable);
    
    /**
     * 根据区域名称模糊查询
     * @param zoneName 区域名称
     * @param pageable 分页参数
     * @return 分页区域列表
     */
    Page<Zone> findByZoneNameContaining(String zoneName, Pageable pageable);
    
    /**
     * 根据状态查找区域
     * @param status 状态
     * @return 区域列表
     */
    List<Zone> findByStatus(Integer status);
    
    /**
     * 综合查询区域
     * @param zoneName 区域名称（模糊匹配）
     * @param parentId 父ID
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页区域列表
     */
    @Query("SELECT z FROM Zone z WHERE " +
           "(:zoneName IS NULL OR z.zoneName LIKE %:zoneName%) AND " +
           "(:parentId IS NULL OR z.parentId = :parentId) AND " +
           "(:status IS NULL OR z.status = :status)")
    Page<Zone> findByConditions(
            @Param("zoneName") String zoneName,
            @Param("parentId") Long parentId,
            @Param("status") Integer status,
            Pageable pageable);
    
    /**
     * 统计指定状态的区域数量
     * @param status 状态值
     * @return 指定状态的区域数量
     */
    long countByStatus(Integer status);
} 