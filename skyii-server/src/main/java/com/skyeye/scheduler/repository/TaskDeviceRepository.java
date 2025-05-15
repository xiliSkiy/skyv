package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.TaskDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务设备数据访问接口
 */
@Repository
public interface TaskDeviceRepository extends JpaRepository<TaskDevice, Long> {

    /**
     * 根据任务ID查询任务设备
     * @param taskId 任务ID
     * @return 任务设备列表
     */
    List<TaskDevice> findByTaskId(Long taskId);

    /**
     * 根据任务ID删除任务设备
     * @param taskId 任务ID
     */
    void deleteByTaskId(Long taskId);

    /**
     * 根据设备ID查询任务设备
     * @param deviceId 设备ID
     * @return 任务设备列表
     */
    List<TaskDevice> findByDeviceId(Long deviceId);

    /**
     * 根据任务ID统计设备数量
     * @param taskId 任务ID
     * @return 设备数量
     */
    Long countByTaskId(Long taskId);

    /**
     * 根据任务ID和设备ID查询任务设备
     * @param taskId 任务ID
     * @param deviceId 设备ID
     * @return 任务设备
     */
    TaskDevice findByTaskIdAndDeviceId(Long taskId, Long deviceId);
} 