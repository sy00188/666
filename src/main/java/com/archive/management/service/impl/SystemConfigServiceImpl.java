package com.archive.management.service.impl;

import com.archive.management.entity.SystemConfig;
import com.archive.management.mapper.SystemConfigMapper;
import com.archive.management.service.SystemConfigService;
import com.archive.management.exception.BusinessException;
import com.archive.management.util.JsonUtil;
import com.archive.management.util.EncryptionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现类
 * 提供系统配置的完整管理功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;
    
    @Override
    public SystemConfigMapper getBaseMapper() {
        return systemConfigMapper;
    }
    
    // 缓存键常量
    private static final String CACHE_CONFIG_BY_ID = "system_config:id:";
    private static final String CACHE_CONFIG_BY_KEY = "system_config:key:";
    private static final String CACHE_CONFIG_BY_NAME = "system_config:name:";
    private static final String CACHE_CONFIG_BY_TYPE = "system_config:type:";
    private static final String CACHE_CONFIG_BY_GROUP = "system_config:group:";
    private static final String CACHE_CONFIG_STATISTICS = "system_config:statistics";
    private static final String CACHE_CONFIG_TYPES = "system_config:types";
    private static final String CACHE_CONFIG_GROUPS = "system_config:groups";

    // ==================== 基础CRUD操作 ====================

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, CACHE_CONFIG_BY_TYPE, 
                        CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS, CACHE_CONFIG_TYPES, 
                        CACHE_CONFIG_GROUPS}, allEntries = true)
    public SystemConfig createSystemConfig(SystemConfig config) {
        log.info("创建系统配置: {}", config.getConfigKey());
        
        // 验证配置
        validateConfigForCreate(config);
        
        // 设置默认值
        setDefaultValues(config);
        
        // 加密敏感配置
        if (Boolean.TRUE.equals(config.getSensitive())) {
            config.setConfigValue(EncryptionUtil.encrypt(config.getConfigValue()));
        }
        
        // 保存配置
        if (!save(config)) {
            throw new BusinessException("创建系统配置失败");
        }
        
        log.info("系统配置创建成功: {}", config.getConfigKey());
        return config;
    }

    @Override
    @Cacheable(value = CACHE_CONFIG_BY_ID, key = "#id")
    public SystemConfig getSystemConfigById(Long id) {
        log.debug("根据ID获取系统配置: {}", id);
        
        SystemConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("系统配置不存在: " + id);
        }
        
        // 解密敏感配置
        if (Boolean.TRUE.equals(config.getSensitive()) && StringUtils.hasText(config.getConfigValue())) {
            config.setConfigValue(EncryptionUtil.decrypt(config.getConfigValue()));
        }
        
        return config;
    }

    @Override
    @Cacheable(value = CACHE_CONFIG_BY_KEY, key = "#configKey")
    public SystemConfig getConfigByKey(String configKey) {
        log.debug("根据配置键获取系统配置: {}", configKey);
        
        SystemConfig config = systemConfigMapper.findByConfigKey(configKey);
        if (config == null) {
            throw new BusinessException("系统配置不存在: " + configKey);
        }
        
        // 解密敏感配置
        if (Boolean.TRUE.equals(config.getSensitive()) && StringUtils.hasText(config.getConfigValue())) {
            config.setConfigValue(EncryptionUtil.decrypt(config.getConfigValue()));
        }
        
        return config;
    }

    @Override
    @Cacheable(value = CACHE_CONFIG_BY_NAME, key = "#configName")
    public SystemConfig getConfigByName(String configName) {
        log.debug("根据配置名称获取系统配置: {}", configName);
        
        SystemConfig config = systemConfigMapper.findByConfigName(configName);
        if (config == null) {
            throw new BusinessException("系统配置不存在: " + configName);
        }
        
        // 解密敏感配置
        if (Boolean.TRUE.equals(config.getSensitive()) && StringUtils.hasText(config.getConfigValue())) {
            config.setConfigValue(EncryptionUtil.decrypt(config.getConfigValue()));
        }
        
        return config;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, 
                        CACHE_CONFIG_BY_TYPE, CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS}, allEntries = true)
    public SystemConfig updateSystemConfig(SystemConfig config) {
        log.info("更新系统配置: {}", config.getConfigKey());
        
        // 验证配置
        validateConfigForUpdate(config);
        
        // 检查配置是否存在
        SystemConfig existingConfig = getById(config.getId());
        if (existingConfig == null) {
            throw new BusinessException("系统配置不存在: " + config.getId());
        }
        
        // 检查是否为只读配置
        if (Boolean.TRUE.equals(existingConfig.getReadonly())) {
            throw new BusinessException("只读配置不允许修改: " + config.getConfigKey());
        }
        
        // 加密敏感配置
        if (Boolean.TRUE.equals(config.getSensitive())) {
            config.setConfigValue(EncryptionUtil.encrypt(config.getConfigValue()));
        }
        
        // 更新配置
        config.setUpdatedAt(LocalDateTime.now());
        if (!updateById(config)) {
            throw new BusinessException("更新系统配置失败");
        }
        
        log.info("系统配置更新成功: {}", config.getConfigKey());
        return config;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, 
                        CACHE_CONFIG_BY_TYPE, CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS}, allEntries = true)
    public boolean deleteSystemConfig(Long id, Long deletedBy) {
        log.info("删除系统配置: {}", id);
        
        SystemConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("系统配置不存在: " + id);
        }
        
        // 检查是否为系统内置配置
        if (Boolean.TRUE.equals(config.getBuiltIn())) {
            throw new BusinessException("系统内置配置不允许删除: " + config.getConfigKey());
        }
        
        // 软删除
        config.setIsDeleted(1);
        config.setDeletedBy(deletedBy);
        config.setDeletedAt(LocalDateTime.now());
        
        boolean result = updateById(config);
        if (result) {
            log.info("系统配置删除成功: {}", config.getConfigKey());
        } else {
            throw new BusinessException("删除系统配置失败");
        }
        
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, 
                        CACHE_CONFIG_BY_TYPE, CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS}, allEntries = true)
    public boolean physicalDeleteSystemConfig(Long id) {
        log.info("物理删除系统配置: {}", id);
        
        SystemConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("系统配置不存在: " + id);
        }
        
        // 检查是否为系统内置配置
        if (Boolean.TRUE.equals(config.getBuiltIn())) {
            throw new BusinessException("系统内置配置不允许删除: " + config.getConfigKey());
        }
        
        boolean result = removeById(id);
        if (result) {
            log.info("系统配置物理删除成功: {}", config.getConfigKey());
        } else {
            throw new BusinessException("物理删除系统配置失败");
        }
        
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, 
                        CACHE_CONFIG_BY_TYPE, CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS}, allEntries = true)
    public boolean enableSystemConfig(Long id, Long updatedBy) {
        log.info("启用系统配置: {}", id);
        return updateConfigStatus(id, 1, updatedBy);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, 
                        CACHE_CONFIG_BY_TYPE, CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS}, allEntries = true)
    public boolean disableSystemConfig(Long id, Long updatedBy) {
        log.info("禁用系统配置: {}", id);
        return updateConfigStatus(id, 0, updatedBy);
    }

    // ==================== 查询操作 ====================

    @Override
    public IPage<SystemConfig> getSystemConfigsWithPagination(Page<SystemConfig> page, String configType, 
                                                              String configGroup, Integer status, 
                                                              Boolean editable, Boolean sensitive) {
        log.debug("分页查询系统配置");
        
        IPage<SystemConfig> result = systemConfigMapper.findSystemConfigsWithPagination(
            page, configType, configGroup, status, editable, sensitive);
        
        // 解密敏感配置
        result.getRecords().forEach(config -> {
            if (Boolean.TRUE.equals(config.getSensitive()) && StringUtils.hasText(config.getConfigValue())) {
                config.setConfigValue(EncryptionUtil.decrypt(config.getConfigValue()));
            }
        });
        
        return result;
    }

    @Override
    @Cacheable(value = CACHE_CONFIG_BY_TYPE, key = "#configType")
    public List<SystemConfig> getConfigsByType(String configType) {
        log.debug("根据配置类型获取系统配置列表: {}", configType);
        
        List<SystemConfig> configs = systemConfigMapper.findByConfigType(configType);
        
        // 解密敏感配置
        configs.forEach(config -> {
            if (Boolean.TRUE.equals(config.getSensitive()) && StringUtils.hasText(config.getConfigValue())) {
                config.setConfigValue(EncryptionUtil.decrypt(config.getConfigValue()));
            }
        });
        
        return configs;
    }

    @Override
    @Cacheable(value = CACHE_CONFIG_BY_GROUP, key = "#configGroup")
    public List<SystemConfig> getConfigsByGroup(String configGroup) {
        log.debug("根据配置分组获取系统配置列表: {}", configGroup);
        
        List<SystemConfig> configs = systemConfigMapper.findByConfigGroup(configGroup);
        
        // 解密敏感配置
        configs.forEach(config -> {
            if (Boolean.TRUE.equals(config.getSensitive()) && StringUtils.hasText(config.getConfigValue())) {
                config.setConfigValue(EncryptionUtil.decrypt(config.getConfigValue()));
            }
        });
        
        return configs;
    }

    @Override
    public List<SystemConfig> getConfigsByStatus(Integer status) {
        log.debug("根据状态获取系统配置列表: {}", status);
        
        List<SystemConfig> configs = systemConfigMapper.findByStatus(status);
        
        // 解密敏感配置
        configs.forEach(config -> {
            if (Boolean.TRUE.equals(config.getSensitive()) && StringUtils.hasText(config.getConfigValue())) {
                config.setConfigValue(EncryptionUtil.decrypt(config.getConfigValue()));
            }
        });
        
        return configs;
    }

    // ==================== 统计操作 ====================

    @Override
    @Cacheable(value = CACHE_CONFIG_STATISTICS, key = "'type_stats'")
    public Map<String, Long> getConfigCountByType() {
        log.debug("获取配置类型统计");
        
        List<Map<String, Object>> stats = systemConfigMapper.getConfigTypeStatistics();
        return stats.stream().collect(Collectors.toMap(
            stat -> (String) stat.get("config_type"),
            stat -> ((Number) stat.get("count")).longValue()
        ));
    }

    @Override
    @Cacheable(value = CACHE_CONFIG_STATISTICS, key = "'group_stats'")
    public Map<String, Long> getConfigCountByGroup() {
        log.debug("获取配置分组统计");
        
        List<Map<String, Object>> stats = systemConfigMapper.getConfigGroupStatistics();
        return stats.stream().collect(Collectors.toMap(
            stat -> (String) stat.get("config_group"),
            stat -> ((Number) stat.get("count")).longValue()
        ));
    }

    @Override
    @Cacheable(value = CACHE_CONFIG_STATISTICS, key = "'status_stats'")
    public Map<String, Long> getConfigCountByStatus() {
        log.debug("获取配置状态统计");
        
        List<Map<String, Object>> stats = systemConfigMapper.getStatusStatistics();
        return stats.stream().collect(Collectors.toMap(
            stat -> String.valueOf(stat.get("status")),
            stat -> ((Number) stat.get("count")).longValue()
        ));
    }

    @Override
    public Map<String, Object> getConfigUsageStatistics() {
        log.debug("获取配置使用统计");
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalConfigs", systemConfigMapper.countSystemConfigs());
        statistics.put("enabledConfigs", systemConfigMapper.countByStatus(1));
        statistics.put("disabledConfigs", systemConfigMapper.countByStatus(0));
        statistics.put("sensitiveConfigs", systemConfigMapper.findBySensitive(true).size());
        statistics.put("readonlyConfigs", systemConfigMapper.findByEditable(false).size());
        statistics.put("builtInConfigs", count(new QueryWrapper<SystemConfig>().eq("built_in", true)));
        statistics.put("expiredConfigs", systemConfigMapper.findExpiredConfigs(LocalDateTime.now()).size());
        
        return statistics;
    }

    // ==================== 配置值类型转换 ====================

    @Override
    public String getStringValue(String configKey) {
        SystemConfig config = getConfigByKey(configKey);
        return config.getConfigValue();
    }

    @Override
    public String getStringValue(String configKey, String defaultValue) {
        try {
            return getStringValue(configKey);
        } catch (Exception e) {
            log.warn("获取配置值失败，使用默认值: {} -> {}", configKey, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public Integer getIntValue(String configKey) {
        String value = getStringValue(configKey);
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("配置值不是有效的整数: " + configKey + " = " + value);
        }
    }

    @Override
    public Integer getIntValue(String configKey, Integer defaultValue) {
        try {
            return getIntValue(configKey);
        } catch (Exception e) {
            log.warn("获取配置值失败，使用默认值: {} -> {}", configKey, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public Long getLongValue(String configKey) {
        String value = getStringValue(configKey);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("配置值不是有效的长整数: " + configKey + " = " + value);
        }
    }

    @Override
    public Long getLongValue(String configKey, Long defaultValue) {
        try {
            return getLongValue(configKey);
        } catch (Exception e) {
            log.warn("获取配置值失败，使用默认值: {} -> {}", configKey, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public Double getDoubleValue(String configKey) {
        String value = getStringValue(configKey);
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("配置值不是有效的双精度浮点数: " + configKey + " = " + value);
        }
    }

    @Override
    public Double getDoubleValue(String configKey, Double defaultValue) {
        try {
            return getDoubleValue(configKey);
        } catch (Exception e) {
            log.warn("获取配置值失败，使用默认值: {} -> {}", configKey, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanValue(String configKey) {
        String value = getStringValue(configKey);
        return Boolean.valueOf(value);
    }

    @Override
    public Boolean getBooleanValue(String configKey, Boolean defaultValue) {
        try {
            return getBooleanValue(configKey);
        } catch (Exception e) {
            log.warn("获取配置值失败，使用默认值: {} -> {}", configKey, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public <T> T getJsonValue(String configKey, Class<T> clazz) {
        String value = getStringValue(configKey);
        try {
            return JsonUtil.fromJson(value, clazz);
        } catch (Exception e) {
            throw new BusinessException("配置值不是有效的JSON: " + configKey + " = " + value);
        }
    }

    @Override
    public <T> T getJsonValue(String configKey, Class<T> clazz, T defaultValue) {
        try {
            return getJsonValue(configKey, clazz);
        } catch (Exception e) {
            log.warn("获取配置值失败，使用默认值: {} -> {}", configKey, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public <T> T getJsonValue(String configKey, TypeReference<T> typeReference) {
        String value = getStringValue(configKey);
        try {
            return JsonUtil.fromJson(value, typeReference);
        } catch (Exception e) {
            throw new BusinessException("配置值不是有效的JSON: " + configKey + " = " + value);
        }
    }

    @Override
    public <T> T getJsonValue(String configKey, TypeReference<T> typeReference, T defaultValue) {
        try {
            return getJsonValue(configKey, typeReference);
        } catch (Exception e) {
            log.warn("获取配置值失败，使用默认值: {} -> {}", configKey, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public List<String> getListValue(String configKey) {
        return getJsonValue(configKey, new TypeReference<List<String>>() {});
    }

    @Override
    public List<String> getListValue(String configKey, List<String> defaultValue) {
        return getJsonValue(configKey, new TypeReference<List<String>>() {}, defaultValue);
    }

    @Override
    public Map<String, Object> getMapValue(String configKey) {
        return getJsonValue(configKey, new TypeReference<Map<String, Object>>() {});
    }

    @Override
    public Map<String, Object> getMapValue(String configKey, Map<String, Object> defaultValue) {
        return getJsonValue(configKey, new TypeReference<Map<String, Object>>() {}, defaultValue);
    }

    // ==================== 配置值设置 ====================

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_NAME}, allEntries = true)
    public boolean setStringValue(String configKey, String value, Long updatedBy) {
        log.info("设置配置值: {} = {}", configKey, value);
        
        int result = systemConfigMapper.updateConfigValue(configKey, value, updatedBy, LocalDateTime.now());
        if (result == 0) {
            throw new BusinessException("配置不存在或更新失败: " + configKey);
        }
        
        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_NAME}, allEntries = true)
    public boolean setIntValue(String configKey, Integer value, Long updatedBy) {
        return setStringValue(configKey, String.valueOf(value), updatedBy);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_NAME}, allEntries = true)
    public boolean setLongValue(String configKey, Long value, Long updatedBy) {
        return setStringValue(configKey, String.valueOf(value), updatedBy);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_NAME}, allEntries = true)
    public boolean setDoubleValue(String configKey, Double value, Long updatedBy) {
        return setStringValue(configKey, String.valueOf(value), updatedBy);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_NAME}, allEntries = true)
    public boolean setBooleanValue(String configKey, Boolean value, Long updatedBy) {
        return setStringValue(configKey, String.valueOf(value), updatedBy);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_NAME}, allEntries = true)
    public boolean setJsonValue(String configKey, Object value, Long updatedBy) {
        try {
            String jsonValue = JsonUtil.toJson(value);
            return setStringValue(configKey, jsonValue, updatedBy);
        } catch (Exception e) {
            throw new BusinessException("对象序列化为JSON失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_NAME}, allEntries = true)
    public boolean setListValue(String configKey, List<String> value, Long updatedBy) {
        return setJsonValue(configKey, value, updatedBy);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_NAME}, allEntries = true)
    public boolean setMapValue(String configKey, Map<String, Object> value, Long updatedBy) {
        return setJsonValue(configKey, value, updatedBy);
    }

    // ==================== 批量操作 ====================

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, 
                        CACHE_CONFIG_BY_TYPE, CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS}, allEntries = true)
    public boolean batchUpdateValues(Map<String, String> configValues, Long updatedBy) {
        log.info("批量更新配置值，数量: {}", configValues.size());
        
        LocalDateTime now = LocalDateTime.now();
        int totalUpdated = 0;
        
        for (Map.Entry<String, String> entry : configValues.entrySet()) {
            String configKey = entry.getKey();
            String configValue = entry.getValue();
            
            // 检查配置是否存在
            SystemConfig config = systemConfigMapper.findByConfigKey(configKey);
            if (config == null) {
                log.warn("配置不存在，跳过更新: {}", configKey);
                continue;
            }
            
            // 检查是否为只读配置
            if (Boolean.TRUE.equals(config.getReadonly())) {
                log.warn("只读配置不允许修改，跳过更新: {}", configKey);
                continue;
            }
            
            // 加密敏感配置
            if (Boolean.TRUE.equals(config.getSensitive())) {
                configValue = EncryptionUtil.encrypt(configValue);
            }
            
            int result = systemConfigMapper.updateConfigValue(configKey, configValue, updatedBy, now);
            if (result > 0) {
                totalUpdated++;
            }
        }
        
        log.info("批量更新配置值完成，成功更新: {}/{}", totalUpdated, configValues.size());
        return totalUpdated > 0;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证配置创建
     */
    private void validateConfigForCreate(SystemConfig config) {
        if (!StringUtils.hasText(config.getConfigKey())) {
            throw new BusinessException("配置键不能为空");
        }
        
        if (!StringUtils.hasText(config.getConfigName())) {
            throw new BusinessException("配置名称不能为空");
        }
        
        // 检查配置键是否已存在
        if (systemConfigMapper.existsByConfigKey(config.getConfigKey())) {
            throw new BusinessException("配置键已存在: " + config.getConfigKey());
        }
        
        // 验证配置值格式
        validateConfigValue(config);
    }

    /**
     * 验证配置更新
     */
    private void validateConfigForUpdate(SystemConfig config) {
        if (config.getId() == null) {
            throw new BusinessException("配置ID不能为空");
        }
        
        // 验证配置值格式
        validateConfigValue(config);
    }

    /**
     * 验证配置值格式
     */
    private void validateConfigValue(SystemConfig config) {
        if (!StringUtils.hasText(config.getConfigValue())) {
            return;
        }
        
        String dataType = config.getDataType();
        String value = config.getConfigValue();
        
        try {
            switch (dataType) {
                case "INTEGER":
                    Integer.valueOf(value);
                    break;
                case "LONG":
                    Long.valueOf(value);
                    break;
                case "DOUBLE":
                    Double.valueOf(value);
                    break;
                case "BOOLEAN":
                    Boolean.valueOf(value);
                    break;
                case "JSON":
                    JsonUtil.fromJson(value, Object.class);
                    break;
                default:
                    // STRING类型不需要验证
                    break;
            }
        } catch (Exception e) {
            throw new BusinessException("配置值格式不正确: " + dataType + " = " + value);
        }
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(SystemConfig config) {
        LocalDateTime now = LocalDateTime.now();
        
        if (config.getStatus() == null) {
            config.setStatus(1); // 默认启用
        }
        
        if (config.getEditable() == null) {
            config.setEditable(true); // 默认可编辑
        }
        
        if (config.getSensitive() == null) {
            config.setSensitive(false); // 默认非敏感
        }
        
        if (config.getBuiltIn() == null) {
            config.setBuiltIn(false); // 默认非内置
        }
        
        if (config.getReadonly() == null) {
            config.setReadonly(false); // 默认非只读
        }
        
        if (config.getSortOrder() == null) {
            // 获取同分组下的最大排序值
            Integer maxOrder = systemConfigMapper.getMaxSortOrderByGroup(config.getConfigGroup());
            config.setSortOrder(maxOrder + 10);
        }
        
        if (!StringUtils.hasText(config.getDataType())) {
            config.setDataType("STRING"); // 默认字符串类型
        }
        
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        config.setIsDeleted(0);
    }

    // ==================== 导出操作 ====================

    @Override
    public String exportConfigs(List<String> configKeys) {
        log.info("导出系统配置，数量: {}", configKeys.size());
        
        List<SystemConfig> configs;
        if (configKeys == null || configKeys.isEmpty()) {
            // 导出所有配置
            configs = list(new QueryWrapper<SystemConfig>().eq("is_deleted", 0));
        } else {
            // 导出指定配置
            configs = systemConfigMapper.findByConfigKeys(configKeys);
        }
        
        // 过滤敏感配置
        configs = configs.stream()
            .filter(config -> !Boolean.TRUE.equals(config.getSensitive()))
            .collect(Collectors.toList());
        
        try {
            return JsonUtil.toJson(configs);
        } catch (Exception e) {
            throw new BusinessException("导出配置失败: " + e.getMessage());
        }
    }

    // ==================== 备份恢复操作 ====================

    @Override
    @Transactional
    public String backupConfigs() {
        log.info("备份系统配置");
        
        List<SystemConfig> configs = list(new QueryWrapper<SystemConfig>().eq("is_deleted", 0));
        
        Map<String, Object> backup = new HashMap<>();
        backup.put("timestamp", LocalDateTime.now());
        backup.put("version", "1.0");
        backup.put("configs", configs);
        
        try {
            return JsonUtil.toJson(backup);
        } catch (Exception e) {
            throw new BusinessException("备份配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, 
                        CACHE_CONFIG_BY_TYPE, CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS}, allEntries = true)
    public boolean restoreConfigs(String backupData, Long restoredBy) {
        log.info("恢复系统配置");
        
        try {
            Map<String, Object> backup = JsonUtil.fromJson(backupData, new TypeReference<Map<String, Object>>() {});
            List<SystemConfig> configs = JsonUtil.fromJson(
                JsonUtil.toJson(backup.get("configs")), 
                new TypeReference<List<SystemConfig>>() {}
            );
            
            LocalDateTime now = LocalDateTime.now();
            int restored = 0;
            
            for (SystemConfig config : configs) {
                // 检查配置是否存在
                SystemConfig existingConfig = systemConfigMapper.findByConfigKey(config.getConfigKey());
                
                if (existingConfig != null) {
                    // 更新现有配置
                    existingConfig.setConfigValue(config.getConfigValue());
                    existingConfig.setConfigName(config.getConfigName());
                    existingConfig.setDescription(config.getDescription());
                    existingConfig.setUpdatedBy(restoredBy);
                    existingConfig.setUpdatedAt(now);
                    
                    if (updateById(existingConfig)) {
                        restored++;
                    }
                } else {
                    // 创建新配置
                    config.setId(null);
                    config.setCreatedBy(restoredBy);
                    config.setUpdatedBy(restoredBy);
                    config.setCreatedAt(now);
                    config.setUpdatedAt(now);
                    config.setIsDeleted(0);
                    
                    if (save(config)) {
                        restored++;
                    }
                }
            }
            
            log.info("恢复系统配置完成，成功恢复: {}/{}", restored, configs.size());
            return restored > 0;
            
        } catch (Exception e) {
            throw new BusinessException("恢复配置失败: " + e.getMessage());
        }
    }

    // ==================== 缓存管理 ====================

    @Override
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, 
                        CACHE_CONFIG_BY_TYPE, CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS, 
                        CACHE_CONFIG_TYPES, CACHE_CONFIG_GROUPS}, allEntries = true)
    public void syncConfigs() {
        log.info("同步系统配置");
        // 清除所有缓存，下次访问时重新加载
    }

    @Override
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, 
                        CACHE_CONFIG_BY_TYPE, CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS, 
                        CACHE_CONFIG_TYPES, CACHE_CONFIG_GROUPS}, allEntries = true)
    public void refreshCache() {
        log.info("刷新系统配置缓存");
        // 清除所有缓存，下次访问时重新加载
    }

    @Override
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME, 
                        CACHE_CONFIG_BY_TYPE, CACHE_CONFIG_BY_GROUP, CACHE_CONFIG_STATISTICS, 
                        CACHE_CONFIG_TYPES, CACHE_CONFIG_GROUPS}, allEntries = true)
    public void clearCache() {
        log.info("清理系统配置缓存");
        // 清除所有缓存
    }

    // ==================== 高级功能 ====================

    @Override
    public List<Map<String, Object>> getChangeHistory(String configKey, int limit) {
        log.debug("获取配置变更历史: {}", configKey);
        
        // 这里应该从审计日志中获取变更历史
        // 暂时返回空列表，实际实现需要结合审计日志服务
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> compareVersions(String configKey, String version1, String version2) {
        log.debug("比较配置版本: {} - {} vs {}", configKey, version1, version2);
        
        // 这里应该实现版本比较逻辑
        // 暂时返回空Map，实际实现需要结合版本管理
        return new HashMap<>();
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_NAME}, allEntries = true)
    public boolean rollbackToVersion(String configKey, String version, Long updatedBy) {
        log.info("回滚配置到指定版本: {} -> {}", configKey, version);
        
        // 这里应该实现版本回滚逻辑
        // 暂时返回false，实际实现需要结合版本管理
        return false;
    }

    @Override
    public List<SystemConfig> searchConfigs(String keyword, String configType, String configGroup) {
        log.debug("搜索系统配置: {}", keyword);
        
        List<SystemConfig> configs = systemConfigMapper.searchByKeyword(keyword, configType, configGroup);
        
        // 解密敏感配置
        configs.forEach(config -> {
            if (Boolean.TRUE.equals(config.getSensitive()) && StringUtils.hasText(config.getConfigValue())) {
                config.setConfigValue(EncryptionUtil.decrypt(config.getConfigValue()));
            }
        });
        
        return configs;
    }

    @Override
    public Map<String, Object> getConfigDependencies(String configKey) {
        log.debug("获取配置依赖: {}", configKey);
        
        // 这里应该实现依赖分析逻辑
        // 返回配置的依赖关系映射
        Map<String, Object> dependencies = new HashMap<>();
        dependencies.put("configKey", configKey);
        dependencies.put("dependencies", new ArrayList<String>());
        dependencies.put("dependents", new ArrayList<String>());
        return dependencies;
    }

    @Override
    public Map<String, Object> checkConsistency() {
        log.info("检查配置一致性");
        
        Map<String, Object> result = new HashMap<>();
        List<String> issues = new ArrayList<>();
        
        // 检查必需配置是否存在
        List<SystemConfig> requiredConfigs = systemConfigMapper.findByRequired(true);
        for (SystemConfig config : requiredConfigs) {
            if (!StringUtils.hasText(config.getConfigValue())) {
                issues.add("必需配置缺少值: " + config.getConfigKey());
            }
        }
        
        // 检查配置值格式
        List<SystemConfig> allConfigs = list(new QueryWrapper<SystemConfig>().eq("is_deleted", 0));
        for (SystemConfig config : allConfigs) {
            try {
                validateConfigValue(config);
            } catch (Exception e) {
                issues.add("配置值格式错误: " + config.getConfigKey() + " - " + e.getMessage());
            }
        }
        
        // 检查过期配置
        List<SystemConfig> expiredConfigs = systemConfigMapper.findExpiredConfigs(LocalDateTime.now());
        for (SystemConfig config : expiredConfigs) {
            issues.add("配置已过期: " + config.getConfigKey());
        }
        
        result.put("consistent", issues.isEmpty());
        result.put("issues", issues);
        result.put("checkTime", LocalDateTime.now());
        
        return result;
    }

    @Override
    @Transactional
    public boolean fixInconsistencies() {
        log.info("修复配置不一致问题");
        
        int fixed = 0;
        
        // 重置无效配置为默认值
        List<SystemConfig> invalidConfigs = systemConfigMapper.findInvalidConfigs();
        for (SystemConfig config : invalidConfigs) {
            if (StringUtils.hasText(config.getDefaultValue())) {
                config.setConfigValue(config.getDefaultValue());
                config.setUpdatedAt(LocalDateTime.now());
                
                if (updateById(config)) {
                    fixed++;
                    log.info("重置配置为默认值: {} = {}", config.getConfigKey(), config.getDefaultValue());
                }
            }
        }
        
        log.info("修复配置不一致问题完成，修复数量: {}", fixed);
        return fixed > 0;
    }

    @Override
    public Map<String, Object> generateReport() {
        log.info("生成系统配置报告");
        
        Map<String, Object> report = new HashMap<>();
        
        // 基础统计
        report.put("totalConfigs", count());
        report.put("enabledConfigs", systemConfigMapper.countByStatus(1));
        report.put("disabledConfigs", systemConfigMapper.countByStatus(0));
        
        // 类型统计
        report.put("typeStatistics", getConfigCountByType());
        
        // 分组统计
        report.put("groupStatistics", getConfigCountByGroup());
        
        // 状态统计
        report.put("statusStatistics", getConfigCountByStatus());
        
        // 特殊配置统计
        report.put("sensitiveConfigs", systemConfigMapper.findBySensitive(true).size());
        report.put("readonlyConfigs", systemConfigMapper.findByEditable(false).size());
        report.put("builtInConfigs", count(new QueryWrapper<SystemConfig>().eq("built_in", true)));
        
        // 问题配置
        report.put("expiredConfigs", systemConfigMapper.findExpiredConfigs(LocalDateTime.now()).size());
        report.put("invalidConfigs", systemConfigMapper.findInvalidConfigs().size());
        
        // 生成时间
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    @Override
    public void monitorConfigChanges(String configKey, Object listener) {
        log.info("监控配置变更: {}", configKey);
        
        // 这里应该实现配置变更监控逻辑
        // 可以使用观察者模式或事件机制
        // 暂时只记录日志
    }

    // ==================== 其他辅助方法 ====================

    @Override
    @Cacheable(value = CACHE_CONFIG_TYPES)
    public List<String> getAllConfigTypes() {
        log.debug("获取所有配置类型");
        return systemConfigMapper.getAllConfigTypes();
    }

    @Override
     @Cacheable(value = CACHE_CONFIG_GROUPS)
     public List<String> getAllConfigGroups() {
         log.debug("获取所有配置分组");
         return systemConfigMapper.getAllConfigGroups();
     }

    /**
     * 更新配置状态
     */
    private boolean updateConfigStatus(Long id, Integer status, Long updatedBy) {
        SystemConfig config = getById(id);
        if (config == null) {
            throw new BusinessException("系统配置不存在: " + id);
        }
        
        config.setStatus(status);
        config.setUpdatedBy(updatedBy);
        config.setUpdatedAt(LocalDateTime.now());
        
        boolean result = updateById(config);
        if (result) {
            log.info("系统配置状态更新成功: {} -> {}", config.getConfigKey(), status);
        } else {
            throw new BusinessException("更新系统配置状态失败");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> rebuildConfigIndex() {
        log.info("重建配置索引");
        
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取所有配置
            List<SystemConfig> allConfigs = list();
            
            // 重建索引逻辑（实际项目中应使用Elasticsearch等）
            // 这里简单返回统计信息
            result.put("success", true);
            result.put("totalConfigs", allConfigs.size());
            result.put("indexedConfigs", allConfigs.size());
            result.put("rebuildTime", LocalDateTime.now().toString());
            
            log.info("配置索引重建完成: 总数={}", allConfigs.size());
        } catch (Exception e) {
            log.error("重建配置索引失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    @CacheEvict(value = {CACHE_CONFIG_BY_KEY, CACHE_CONFIG_BY_ID, CACHE_CONFIG_BY_NAME}, allEntries = true)
    public boolean refreshConfigCache(String configKey) {
        log.info("刷新配置缓存: {}", configKey);
        
        try {
            if (configKey == null || configKey.trim().isEmpty()) {
                // 刷新所有缓存
                log.info("刷新所有配置缓存");
                return true;
            } else {
                // 刷新指定配置的缓存
                SystemConfig config = getConfigByKey(configKey);
                if (config != null) {
                    log.info("刷新配置缓存成功: {}", configKey);
                    return true;
                } else {
                    log.warn("配置不存在，无法刷新缓存: {}", configKey);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("刷新配置缓存失败: {}", configKey, e);
            return false;
        }
    }
}