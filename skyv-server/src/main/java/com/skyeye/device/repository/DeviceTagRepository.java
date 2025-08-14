package com.skyeye.device.repository;

import com.skyeye.device.entity.DeviceTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备标签数据访问层
 */
@Repository
public interface DeviceTagRepository extends JpaRepository<DeviceTag, Long> {

    /**
     * 根据名称查找标签
     */
    Optional<DeviceTag> findByName(String name);

    /**
     * 根据使用次数排序获取热门标签
     */
    List<DeviceTag> findTop10ByOrderByUsageCountDesc();

    /**
     * 分页查询标签
     */
    @Query("SELECT dt FROM DeviceTag dt WHERE " +
           "(:name IS NULL OR dt.name LIKE %:name%) AND " +
           "(:description IS NULL OR dt.description LIKE %:description%)")
    Page<DeviceTag> findByConditions(@Param("name") String name,
                                     @Param("description") String description,
                                     Pageable pageable);

    /**
     * 检查名称是否唯一（排除自身）
     */
    @Query("SELECT COUNT(dt) FROM DeviceTag dt WHERE dt.name = :name AND (:id IS NULL OR dt.id != :id)")
    long countByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * 获取标签使用的设备数量
     */
    @Query(value = "SELECT COUNT(*) FROM tb_device_tag_relations WHERE tag_id = :tagId", nativeQuery = true)
    Long countDevicesByTagId(@Param("tagId") Long tagId);
} 