package com.skyeye.device.repository;

import com.skyeye.device.entity.DeviceArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备区域数据访问层
 */
@Repository
public interface DeviceAreaRepository extends JpaRepository<DeviceArea, Long> {

    /**
     * 根据名称查找区域
     */
    Optional<DeviceArea> findByName(String name);

    /**
     * 根据编码查找区域
     */
    Optional<DeviceArea> findByCode(String code);

    /**
     * 根据父区域ID查找子区域
     */
    List<DeviceArea> findByParentIdOrderBySortOrderAsc(Long parentId);

    /**
     * 查找根区域（父区域为空）
     */
    List<DeviceArea> findByParentIdIsNullOrderBySortOrderAsc();

    /**
     * 根据层级查找区域
     */
    List<DeviceArea> findByLevelOrderBySortOrderAsc(Integer level);

    /**
     * 分页查询区域
     */
    @Query("SELECT da FROM DeviceArea da WHERE " +
           "(:name IS NULL OR da.name LIKE %:name%) AND " +
           "(:code IS NULL OR da.code LIKE %:code%) AND " +
           "(:parentId IS NULL OR da.parentId = :parentId) AND " +
           "(:level IS NULL OR da.level = :level)")
    Page<DeviceArea> findByConditions(@Param("name") String name,
                                      @Param("code") String code,
                                      @Param("parentId") Long parentId,
                                      @Param("level") Integer level,
                                      Pageable pageable);

    /**
     * 统计区域下的设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.areaId = :areaId")
    Long countDevicesByAreaId(@Param("areaId") Long areaId);

    /**
     * 检查名称是否唯一（排除自身）
     */
    @Query("SELECT COUNT(da) FROM DeviceArea da WHERE da.name = :name AND (:id IS NULL OR da.id != :id)")
    long countByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * 检查编码是否唯一（排除自身）
     */
    @Query("SELECT COUNT(da) FROM DeviceArea da WHERE da.code = :code AND (:id IS NULL OR da.id != :id)")
    long countByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);

    /**
     * 查找某个区域的所有子区域（递归）
     */
    @Query(value = "WITH RECURSIVE area_tree AS (" +
                   "    SELECT id, name, parent_id, level FROM tb_device_areas WHERE id = :areaId " +
                   "    UNION ALL " +
                   "    SELECT da.id, da.name, da.parent_id, da.level " +
                   "    FROM tb_device_areas da " +
                   "    JOIN area_tree at ON da.parent_id = at.id" +
                   ") SELECT id FROM area_tree WHERE id != :areaId",
           nativeQuery = true)
    List<Long> findChildAreaIds(@Param("areaId") Long areaId);
} 