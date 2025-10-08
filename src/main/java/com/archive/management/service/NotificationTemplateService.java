package com.archive.management.service;

import com.archive.management.entity.NotificationTemplate;

import java.util.List;
import java.util.Map;

/**
 * 通知模板服务接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface NotificationTemplateService {

    /**
     * 创建通知模板
     */
    NotificationTemplate createTemplate(NotificationTemplate template);

    /**
     * 更新通知模板
     */
    NotificationTemplate updateTemplate(Long templateId, NotificationTemplate template);

    /**
     * 删除通知模板
     */
    boolean deleteTemplate(Long templateId);

    /**
     * 获取模板详情
     */
    NotificationTemplate getTemplate(Long templateId);

    /**
     * 根据模板代码获取
     */
    NotificationTemplate getTemplateByCode(String templateCode);

    /**
     * 获取模板列表
     */
    List<NotificationTemplate> getTemplateList(String templateType);

    /**
     * 渲染模板
     */
    String renderTemplate(String templateCode, Map<String, Object> variables);

    /**
     * 渲染标题
     */
    String renderTitle(String templateCode, Map<String, Object> variables);

    /**
     * 启用/禁用模板
     */
    boolean toggleTemplateStatus(Long templateId, boolean enabled);

    /**
     * 验证模板语法
     */
    Map<String, Object> validateTemplate(String content, String titleTemplate);
}

