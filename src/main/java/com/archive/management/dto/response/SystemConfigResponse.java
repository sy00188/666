package com.archive.management.dto.response;

/**
 * 系统配置响应DTO（ConfigResponse的别名）
 * 为了兼容性保留此类
 */
public class SystemConfigResponse extends ConfigResponse {
    
    public SystemConfigResponse() {
        super();
    }
    
    public SystemConfigResponse(Long id, String configKey, String configValue, String configName,
                                String description, String configGroup, String configType,
                                Boolean isSystem, Boolean encrypted, Boolean editable,
                                Integer sortOrder, String status, String defaultValue,
                                String valueRange, String validationRule, CreatorInfo creator,
                                UpdaterInfo updater, java.time.LocalDateTime createTime, 
                                java.time.LocalDateTime updateTime, String remark) {
        super(id, configKey, configValue, configName, description, configGroup, configType,
              isSystem, encrypted, editable, sortOrder, status, defaultValue,
              valueRange, validationRule, creator, updater, createTime, updateTime, remark);
    }
}

