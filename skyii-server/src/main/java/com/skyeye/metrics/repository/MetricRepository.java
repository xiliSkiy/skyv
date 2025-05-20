package com.skyeye.metrics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skyeye.metrics.entity.Metric;

/**
 * 指标配置数据访问层
 */
public interface MetricRepository extends JpaRepository<Metric, Long> {
    
    /**
     * 根据指标Key查找指标
     * @param metricKey 指标Key
     * @return 指标对象
     */
    Optional<Metric> findByMetricKey(String metricKey);
    
    /**
     * 根据指标类型查找指标
     * @param metricType 指标类型
     * @return 指标列表
     */
    List<Metric> findByMetricType(String metricType);
    
    /**
     * 根据适用设备类型查找指标
     * @param deviceType 设备类型
     * @return 指标列表
     */
    List<Metric> findByApplicableDeviceType(String deviceType);
    
    /**
     * 根据采集方式查找指标
     * @param collectionMethod 采集方式
     * @return 指标列表
     */
    List<Metric> findByCollectionMethod(String collectionMethod);
    
    /**
     * 根据指标名称模糊查询
     * @param metricName 指标名称
     * @param pageable 分页参数
     * @return 分页指标列表
     */
    Page<Metric> findByMetricNameContaining(String metricName, Pageable pageable);
    
    /**
     * 根据状态查找指标
     * @param status 状态
     * @return 指标列表
     */
    List<Metric> findByStatus(Integer status);
    
    /**
     * 综合查询指标
     * @param metricName 指标名称（模糊匹配）
     * @param metricType 指标类型
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页指标列表
     */
    @Query("SELECT m FROM Metric m WHERE " +
           "(:metricName IS NULL OR m.metricName LIKE %:metricName%) AND " +
           "(:metricType IS NULL OR m.metricType = :metricType) AND " +
           "(:status IS NULL OR m.status = :status)")
    Page<Metric> findByConditions(
            @Param("metricName") String metricName,
            @Param("metricType") String metricType,
            @Param("status") Integer status,
            Pageable pageable);
            
    /**
     * 统计指定状态的指标数量
     * @param status 状态值
     * @return 指定状态的指标数量
     */
    long countByStatus(Integer status);
    
    /**
     * 统计配置了报警规则的指标数量
     * @return 配置了报警规则的指标数量
     */
    @Query("SELECT COUNT(m) FROM Metric m WHERE m.alertRuleConfig IS NOT NULL AND m.alertRuleConfig <> ''")
    long countByHasAlertRule();
} 