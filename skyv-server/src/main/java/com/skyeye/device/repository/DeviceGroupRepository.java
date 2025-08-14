package com.skyeye.device.repository;

import com.skyeye.device.entity.DeviceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备分组Repository
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface DeviceGroupRepository extends JpaRepository<DeviceGroup, Long>, JpaSpecificationExecutor<DeviceGroup> {

    /**
     * 查找所有未删除的设备分组
     */
    @Query("SELECT dg FROM DeviceGroup dg WHERE dg.deletedAt IS NULL ORDER BY dg.sortOrder ASC, dg.id ASC")
    List<DeviceGroup> findAllActiveGroups();

    /**
     * 根据名称查找设备分组
     */
    @Query("SELECT dg FROM DeviceGroup dg WHERE dg.name = :name AND dg.deletedAt IS NULL")
    Optional<DeviceGroup> findByName(@Param("name") String name);

    /**
     * 检查名称是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(dg) > 0 FROM DeviceGroup dg WHERE dg.name = :name AND dg.id != :excludeId AND dg.deletedAt IS NULL")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 检查名称是否存在
     */
    @Query("SELECT COUNT(dg) > 0 FROM DeviceGroup dg WHERE dg.name = :name AND dg.deletedAt IS NULL")
    boolean existsByName(@Param("name") String name);

    /**
     * 根据类型查找设备分组
     */
    @Query("SELECT dg FROM DeviceGroup dg WHERE dg.type = :type AND dg.deletedAt IS NULL ORDER BY dg.sortOrder ASC, dg.id ASC")
    List<DeviceGroup> findByType(@Param("type") String type);

    /**
     * 根据是否重要查找设备分组
     */
    @Query("SELECT dg FROM DeviceGroup dg WHERE dg.isImportant = :important AND dg.deletedAt IS NULL ORDER BY dg.sortOrder ASC, dg.id ASC")
    List<DeviceGroup> findByIsImportant(@Param("important") Boolean important);

    /**
     * 统计启用的设备分组数量
     */
    @Query("SELECT COUNT(dg) FROM DeviceGroup dg WHERE dg.isEnabled = true AND dg.deletedAt IS NULL")
    long countEnabledGroups();

    /**
     * 查找指定ID列表的设备分组（排除软删除）
     */
    @Query("SELECT dg FROM DeviceGroup dg WHERE dg.id IN :ids AND dg.deletedAt IS NULL")
    List<DeviceGroup> findByIdInAndNotDeleted(@Param("ids") List<Long> ids);
} 