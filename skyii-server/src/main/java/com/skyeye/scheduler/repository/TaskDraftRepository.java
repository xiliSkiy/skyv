package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.TaskDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务草稿数据访问接口
 */
@Repository
public interface TaskDraftRepository extends JpaRepository<TaskDraft, Long> {

    /**
     * 根据草稿ID查找草稿
     * @param draftId 草稿ID
     * @return 草稿
     */
    Optional<TaskDraft> findByDraftId(String draftId);

    /**
     * 根据创建人ID查询草稿列表
     * @param createdBy 创建人ID
     * @return 草稿列表
     */
    List<TaskDraft> findByCreatedByOrderByUpdatedAtDesc(Long createdBy);

    /**
     * 根据草稿ID删除草稿
     * @param draftId 草稿ID
     */
    void deleteByDraftId(String draftId);
} 