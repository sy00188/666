package com.archive.management.service.impl;

import com.archive.management.entity.NotificationTemplate;
import com.archive.management.repository.NotificationTemplateRepository;
import com.archive.management.service.NotificationTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通知模板服务实现
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    @Autowired
    private NotificationTemplateRepository templateRepository;

    // 模板变量匹配模式 ${variableName}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    @Override
    @Transactional
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        log.info("创建通知模板: code={}, type={}", template.getTemplateCode(), template.getTemplateType());
        
        // 检查模板代码是否已存在
        if (templateRepository.existsByTemplateCodeAndDeletedFalse(template.getTemplateCode())) {
            throw new RuntimeException("模板代码已存在: " + template.getTemplateCode());
        }
        
        return templateRepository.save(template);
    }

    @Override
    @Transactional
    public NotificationTemplate updateTemplate(Long templateId, NotificationTemplate template) {
        log.info("更新通知模板: templateId={}", templateId);
        
        NotificationTemplate existing = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
        
        // 更新字段
        existing.setTemplateName(template.getTemplateName());
        existing.setTemplateType(template.getTemplateType());
        existing.setContent(template.getContent());
        existing.setTitleTemplate(template.getTitleTemplate());
        existing.setVariables(template.getVariables());
        existing.setDescription(template.getDescription());
        existing.setEnabled(template.getEnabled());
        existing.setPriority(template.getPriority());
        existing.setChannelConfig(template.getChannelConfig());
        existing.setUpdatedBy(template.getUpdatedBy());
        
        return templateRepository.save(existing);
    }

    @Override
    @Transactional
    public boolean deleteTemplate(Long templateId) {
        log.info("删除通知模板: templateId={}", templateId);
        
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
        
        template.setDeleted(true);
        templateRepository.save(template);
        
        return true;
    }

    @Override
    public NotificationTemplate getTemplate(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
    }

    @Override
    public NotificationTemplate getTemplateByCode(String templateCode) {
        return templateRepository.findByTemplateCodeAndDeletedFalse(templateCode)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + templateCode));
    }

    @Override
    public List<NotificationTemplate> getTemplateList(String templateType) {
        if (templateType != null && !templateType.isEmpty()) {
            return templateRepository.findByTemplateTypeAndDeletedFalseOrderByCreatedAtDesc(templateType);
        }
        return templateRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    @Override
    public String renderTemplate(String templateCode, Map<String, Object> variables) {
        log.debug("渲染模板: code={}", templateCode);
        
        NotificationTemplate template = getTemplateByCode(templateCode);
        
        if (!template.getEnabled()) {
            throw new RuntimeException("模板已禁用: " + templateCode);
        }
        
        return replaceVariables(template.getContent(), variables);
    }

    @Override
    public String renderTitle(String templateCode, Map<String, Object> variables) {
        log.debug("渲染标题: code={}", templateCode);
        
        NotificationTemplate template = getTemplateByCode(templateCode);
        
        if (template.getTitleTemplate() == null || template.getTitleTemplate().isEmpty()) {
            return template.getTemplateName();
        }
        
        return replaceVariables(template.getTitleTemplate(), variables);
    }

    @Override
    @Transactional
    public boolean toggleTemplateStatus(Long templateId, boolean enabled) {
        log.info("切换模板状态: templateId={}, enabled={}", templateId, enabled);
        
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
        
        template.setEnabled(enabled);
        templateRepository.save(template);
        
        return true;
    }

    @Override
    public Map<String, Object> validateTemplate(String content, String titleTemplate) {
        log.debug("验证模板语法");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 提取内容中的变量
            List<String> contentVars = extractVariables(content);
            
            // 提取标题中的变量
            List<String> titleVars = titleTemplate != null ? extractVariables(titleTemplate) : List.of();
            
            result.put("valid", true);
            result.put("contentVariables", contentVars);
            result.put("titleVariables", titleVars);
            result.put("message", "模板语法正确");
            
        } catch (Exception e) {
            log.error("模板验证失败", e);
            result.put("valid", false);
            result.put("message", "模板语法错误: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 替换模板变量
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        
        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = variables.get(varName);
            
            if (value != null) {
                result = result.replace("${" + varName + "}", value.toString());
            } else {
                log.warn("模板变量未提供值: {}", varName);
                result = result.replace("${" + varName + "}", "");
            }
        }
        
        return result;
    }

    /**
     * 提取模板中的变量
     */
    private List<String> extractVariables(String template) {
        if (template == null || template.isEmpty()) {
            return List.of();
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        List<String> variables = new java.util.ArrayList<>();
        
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return variables;
    }
}

