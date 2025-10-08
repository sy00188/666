package com.archive.management.repository;

import com.archive.management.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 通知模板Repository
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * 根据模板代码查询
     */
    Optional<NotificationTemplate> findByTemplateCodeAndDeletedFalse(String templateCode);

    /**
     * 根据模板类型查询
     */
    List<NotificationTemplate> findByTemplateTypeAndDeletedFalseOrderByCreatedAtDesc(String templateType);

    /**
     * 查询启用的模板
     */
    List<NotificationTemplate> findByEnabledTrueAndDeletedFalseOrderByCreatedAtDesc();

    /**
     * 查询所有未删除的模板
     */
    List<NotificationTemplate> findByDeletedFalseOrderByCreatedAtDesc();

    /**
     * 检查模板代码是否存在
     */
    boolean existsByTemplateCodeAndDeletedFalse(String templateCode);
}

