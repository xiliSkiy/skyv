package com.skyeye.settings.repository;

import com.skyeye.common.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统设置Repository
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    
    /**
     * 根据设置键查找设置
     * @param settingKey 设置键
     * @return 设置对象
     */
    Optional<SystemSetting> findBySettingKey(String settingKey);
    
    /**
     * 根据设置键前缀查找设置列表
     * @param keyPrefix 设置键前缀
     * @return 设置列表
     */
    @Query("SELECT s FROM SystemSetting s WHERE s.settingKey LIKE :keyPrefix%")
    List<SystemSetting> findBySettingKeyStartingWith(String keyPrefix);
    
    /**
     * 根据设置键删除设置
     * @param settingKey 设置键
     */
    void deleteBySettingKey(String settingKey);
}