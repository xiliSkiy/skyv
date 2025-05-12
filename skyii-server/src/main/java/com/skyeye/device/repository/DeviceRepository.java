package com.skyeye.device.repository;

import com.skyeye.device.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓库接口
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    /**
     * 根据设备编码查找设备
     */
    Optional<Device> findByCode(String code);
    
    /**
     * 检查设备编码是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据设备名称模糊查询
     */
    Page<Device> findByNameContaining(String name, Pageable pageable);
    
    /**
     * 根据设备状态查询
     */
    Page<Device> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据分组ID查询
     */
    List<Device> findByGroupId(Long groupId);
    
    /**
     * 根据分组ID分页查询
     */
    Page<Device> findByGroupId(Long groupId, Pageable pageable);
    
    /**
     * 统计各状态设备数量
     */
    @Query("SELECT d.status, COUNT(d) FROM Device d GROUP BY d.status")
    List<Object[]> countByStatus();
    
    /**
     * 根据IP地址查找设备
     */
    Optional<Device> findByIpAddress(String ipAddress);
    
    /**
     * 根据多个条件查询设备
     */
    @Query("SELECT d FROM Device d WHERE " +
           "(:name IS NULL OR d.name LIKE %:name%) AND " +
           "(:type IS NULL OR d.type = :type) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:groupId IS NULL OR d.groupId = :groupId)")
    Page<Device> findByConditions(
            @Param("name") String name,
            @Param("type") String type,
            @Param("status") Integer status,
            @Param("groupId") Long groupId,
            Pageable pageable);
} 