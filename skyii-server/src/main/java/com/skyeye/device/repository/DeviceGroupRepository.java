package com.skyeye.device.repository;

import com.skyeye.device.entity.DeviceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备分组仓库接口
 */
@Repository
public interface DeviceGroupRepository extends JpaRepository<DeviceGroup, Long> {
    
    /**
     * 根据分组编码查找分组
     */
    Optional<DeviceGroup> findByCode(String code);
    
    /**
     * 检查分组编码是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据分组名称模糊查询
     */
    List<DeviceGroup> findByNameContaining(String name);
    
    /**
     * 根据父分组ID查询
     */
    List<DeviceGroup> findByParentId(Long parentId);
    
    /**
     * 查询所有顶级分组
     */
    @Query("SELECT g FROM DeviceGroup g WHERE g.parentId IS NULL")
    List<DeviceGroup> findAllTopGroups();
    
    /**
     * 查询分组树
     */
    @Query("SELECT g FROM DeviceGroup g ORDER BY g.sort ASC")
    List<DeviceGroup> findAllOrderBySort();
    
    /**
     * 根据名称和父ID查询
     */
    @Query("SELECT g FROM DeviceGroup g WHERE " +
           "(:name IS NULL OR g.name LIKE %:name%) AND " +
           "(:parentId IS NULL OR g.parentId = :parentId)")
    List<DeviceGroup> findByConditions(
            @Param("name") String name,
            @Param("parentId") Long parentId);
} 