package com.skyeye.collector.repository;

import com.skyeye.collector.entity.TaskStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务统计Repository
 * 
 * @author SkyEye Team
 */
@Repository
public interface TaskStatisticsRepository extends JpaRepository<TaskStatistics, Long>, JpaSpecificationExecutor<TaskStatistics> {

    /**
     * 根据任务ID和统计日期查找统计记录
     */
    Optional<TaskStatistics> findByTaskIdAndStatDate(Long taskId, String statDate);

    /**
     * 根据任务ID查找所有统计记录
     */
    List<TaskStatistics> findByTaskIdOrderByStatDateDesc(Long taskId);

    /**
     * 根据统计日期查找统计记录
     */
    List<TaskStatistics> findByStatDate(String statDate);

    /**
     * 根据统计日期范围查找统计记录
     */
    List<TaskStatistics> findByStatDateBetweenOrderByStatDateDesc(String startDate, String endDate);

    /**
     * 查找指定日期范围内的统计记录
     */
    @Query("SELECT ts FROM TaskStatistics ts WHERE ts.statDate BETWEEN :startDate AND :endDate ORDER BY ts.statDate DESC")
    List<TaskStatistics> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 查找指定任务在日期范围内的统计记录
     */
    @Query("SELECT ts FROM TaskStatistics ts WHERE ts.taskId = :taskId AND ts.statDate BETWEEN :startDate AND :endDate ORDER BY ts.statDate DESC")
    List<TaskStatistics> findByTaskIdAndDateRange(@Param("taskId") Long taskId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 统计指定日期范围内的总执行次数
     */
    @Query("SELECT SUM(ts.executionCount) FROM TaskStatistics ts WHERE ts.statDate BETWEEN :startDate AND :endDate")
    Long sumExecutionCountByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 统计指定日期范围内的总成功次数
     */
    @Query("SELECT SUM(ts.successCount) FROM TaskStatistics ts WHERE ts.statDate BETWEEN :startDate AND :endDate")
    Long sumSuccessCountByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 统计指定日期范围内的总失败次数
     */
    @Query("SELECT SUM(ts.failureCount) FROM TaskStatistics ts WHERE ts.statDate BETWEEN :startDate AND :endDate")
    Long sumFailureCountByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 计算指定日期范围内的平均成功率
     */
    @Query("SELECT AVG(ts.successRate) FROM TaskStatistics ts WHERE ts.statDate BETWEEN :startDate AND :endDate")
    Double averageSuccessRateByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 计算指定日期范围内的平均执行时间
     */
    @Query("SELECT AVG(ts.averageExecutionTime) FROM TaskStatistics ts WHERE ts.statDate BETWEEN :startDate AND :endDate")
    Double averageExecutionTimeByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 查找成功率最高的任务统计
     */
    @Query("SELECT ts FROM TaskStatistics ts WHERE ts.statDate = :statDate ORDER BY ts.successRate DESC")
    List<TaskStatistics> findTopSuccessRateTasks(@Param("statDate") String statDate, org.springframework.data.domain.Pageable pageable);

    /**
     * 查找执行时间最长的任务统计
     */
    @Query("SELECT ts FROM TaskStatistics ts WHERE ts.statDate = :statDate ORDER BY ts.averageExecutionTime DESC")
    List<TaskStatistics> findTopExecutionTimeTasks(@Param("statDate") String statDate, org.springframework.data.domain.Pageable pageable);

    /**
     * 查找执行次数最多的任务统计
     */
    @Query("SELECT ts FROM TaskStatistics ts WHERE ts.statDate = :statDate ORDER BY ts.executionCount DESC")
    List<TaskStatistics> findTopExecutionCountTasks(@Param("statDate") String statDate, org.springframework.data.domain.Pageable pageable);

    /**
     * 更新任务统计记录
     */
    @Modifying
    @Query("UPDATE TaskStatistics ts SET " +
           "ts.executionCount = :executionCount, " +
           "ts.successCount = :successCount, " +
           "ts.failureCount = :failureCount, " +
           "ts.totalExecutionTime = :totalExecutionTime, " +
           "ts.averageExecutionTime = :averageExecutionTime, " +
           "ts.successRate = :successRate, " +
           "ts.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ts.id = :id")
    int updateStatistics(@Param("id") Long id,
                        @Param("executionCount") Long executionCount,
                        @Param("successCount") Long successCount,
                        @Param("failureCount") Long failureCount,
                        @Param("totalExecutionTime") Long totalExecutionTime,
                        @Param("averageExecutionTime") Long averageExecutionTime,
                        @Param("successRate") Double successRate);

    /**
     * 删除指定日期之前的统计记录
     */
    @Modifying
    @Query("DELETE FROM TaskStatistics ts WHERE ts.statDate < :beforeDate")
    int deleteStatisticsBefore(@Param("beforeDate") String beforeDate);

    /**
     * 统计各任务的成功率排名
     */
    @Query("SELECT ts.taskId, ts.successRate FROM TaskStatistics ts " +
           "WHERE ts.statDate = :statDate AND ts.executionCount > 0 " +
           "ORDER BY ts.successRate DESC")
    List<Object[]> getSuccessRateRanking(@Param("statDate") String statDate);

    /**
     * 统计各任务的执行时间排名
     */
    @Query("SELECT ts.taskId, ts.averageExecutionTime FROM TaskStatistics ts " +
           "WHERE ts.statDate = :statDate AND ts.averageExecutionTime IS NOT NULL " +
           "ORDER BY ts.averageExecutionTime ASC")
    List<Object[]> getExecutionTimeRanking(@Param("statDate") String statDate);

    /**
     * 统计各任务的执行次数排名
     */
    @Query("SELECT ts.taskId, ts.executionCount FROM TaskStatistics ts " +
           "WHERE ts.statDate = :statDate " +
           "ORDER BY ts.executionCount DESC")
    List<Object[]> getExecutionCountRanking(@Param("statDate") String statDate);

    /**
     * 查找指定任务的最新统计记录
     */
    @Query("SELECT ts FROM TaskStatistics ts WHERE ts.taskId = :taskId ORDER BY ts.statDate DESC")
    List<TaskStatistics> findLatestStatisticsByTaskId(@Param("taskId") Long taskId, org.springframework.data.domain.Pageable pageable);

    /**
     * 检查指定任务在指定日期是否有统计记录
     */
    boolean existsByTaskIdAndStatDate(Long taskId, String statDate);

    /**
     * 根据任务ID删除统计记录
     */
    @Modifying
    @Query("DELETE FROM TaskStatistics ts WHERE ts.taskId = :taskId")
    int deleteByTaskId(@Param("taskId") Long taskId);
}
