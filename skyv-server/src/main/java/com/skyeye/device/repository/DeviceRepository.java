package com.skyeye.device.repository;

import com.skyeye.device.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓库接口
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {

    /**
     * 根据名称查找设备
     */
    Optional<Device> findByName(String name);

    /**
     * 根据IP地址查找设备
     */
    Optional<Device> findByIpAddress(String ipAddress);

    /**
     * 根据MAC地址查找设备
     */
    Optional<Device> findByMacAddress(String macAddress);

    /**
     * 根据序列号查找设备
     */
    Optional<Device> findBySerialNumber(String serialNumber);

    /**
     * 根据设备类型查找设备
     */
    List<Device> findByDeviceTypeId(Long deviceTypeId);

    /**
     * 根据区域查找设备
     */
    List<Device> findByAreaId(Long areaId);

    /**
     * 根据分组查找设备
     */
    List<Device> findByGroupId(Long groupId);

    /**
     * 根据状态查找设备
     */
    List<Device> findByStatus(Integer status);

    /**
     * 统计各状态设备数量
     */
    @Query("SELECT d.status, COUNT(d) FROM Device d GROUP BY d.status")
    List<Object[]> countByStatus();

    /**
     * 根据名称模糊查询设备
     */
    Page<Device> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 查找在线设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 1")
    long countOnlineDevices();

    /**
     * 查找离线设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 2")
    long countOfflineDevices();

    /**
     * 查找故障设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 3")
    long countFaultDevices();

    /**
     * 根据健康评分范围查找设备
     */
    List<Device> findByHealthScoreBetween(Integer minScore, Integer maxScore);

    /**
     * 查找指定ID列表的设备（排除软删除）
     */
    @Query("SELECT d FROM Device d WHERE d.id IN :ids AND d.deletedAt IS NULL")
    List<Device> findByIdInAndNotDeleted(@Param("ids") List<Long> ids);
} 