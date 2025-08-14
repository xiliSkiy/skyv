package com.skyeye.device.repository;

import com.skyeye.device.entity.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备类型Repository
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceType, Long>, JpaSpecificationExecutor<DeviceType> {

    /**
     * 查找所有未删除的设备类型
     */
    @Query("SELECT dt FROM DeviceType dt WHERE dt.deletedAt IS NULL ORDER BY dt.sortOrder ASC, dt.id ASC")
    List<DeviceType> findAllActiveTypes();

    /**
     * 根据父类型ID查找子类型
     */
    @Query("SELECT dt FROM DeviceType dt WHERE dt.parentId = :parentId AND dt.deletedAt IS NULL ORDER BY dt.sortOrder ASC, dt.id ASC")
    List<DeviceType> findByParentId(@Param("parentId") Long parentId);

    /**
     * 查找所有顶级类型（父类型为空）
     */
    @Query("SELECT dt FROM DeviceType dt WHERE dt.parentId IS NULL AND dt.deletedAt IS NULL ORDER BY dt.sortOrder ASC, dt.id ASC")
    List<DeviceType> findRootTypes();

    /**
     * 根据编码查找设备类型
     */
    @Query("SELECT dt FROM DeviceType dt WHERE dt.code = :code AND dt.deletedAt IS NULL")
    Optional<DeviceType> findByCode(@Param("code") String code);

    /**
     * 检查编码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(dt) > 0 FROM DeviceType dt WHERE dt.code = :code AND dt.id != :excludeId AND dt.deletedAt IS NULL")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("excludeId") Long excludeId);

    /**
     * 检查编码是否存在
     */
    @Query("SELECT COUNT(dt) > 0 FROM DeviceType dt WHERE dt.code = :code AND dt.deletedAt IS NULL")
    boolean existsByCode(@Param("code") String code);

    /**
     * 检查名称是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(dt) > 0 FROM DeviceType dt WHERE dt.name = :name AND dt.id != :excludeId AND dt.deletedAt IS NULL")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 检查名称是否存在
     */
    @Query("SELECT COUNT(dt) > 0 FROM DeviceType dt WHERE dt.name = :name AND dt.deletedAt IS NULL")
    boolean existsByName(@Param("name") String name);

    /**
     * 根据名称模糊查询
     */
    @Query("SELECT dt FROM DeviceType dt WHERE dt.name LIKE %:name% AND dt.deletedAt IS NULL ORDER BY dt.sortOrder ASC, dt.id ASC")
    List<DeviceType> findByNameContaining(@Param("name") String name);

    /**
     * 获取某个类型下的所有子类型（递归）
     */
    @Query(value = "WITH RECURSIVE type_tree AS (" +
            "  SELECT id, name, code, parent_id, 1 as level " +
            "  FROM tb_device_types " +
            "  WHERE parent_id = :parentId AND deleted_at IS NULL " +
            "  UNION ALL " +
            "  SELECT t.id, t.name, t.code, t.parent_id, tt.level + 1 " +
            "  FROM tb_device_types t " +
            "  INNER JOIN type_tree tt ON t.parent_id = tt.id " +
            "  WHERE t.deleted_at IS NULL " +
            ") " +
            "SELECT * FROM type_tree ORDER BY level, id", nativeQuery = true)
    List<Object[]> findDescendantTypes(@Param("parentId") Long parentId);

    /**
     * 统计启用的设备类型数量
     */
    @Query("SELECT COUNT(dt) FROM DeviceType dt WHERE dt.isEnabled = true AND dt.deletedAt IS NULL")
    long countEnabledTypes();
} 