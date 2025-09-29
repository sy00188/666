package com.archive.management.service;

import com.archive.management.entity.SystemConfig;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统配置业务服务接口
 * 定义系统配置管理的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface SystemConfigService {

    /**
     * 创建系统配置
     * @param systemConfig 系统配置信息
     * @return 创建的系统配置
     */
    SystemConfig createSystemConfig(SystemConfig systemConfig);

    /**
     * 根据ID获取系统配置
     * @param id 配置ID
     * @return 系统配置信息
     */
    SystemConfig getSystemConfigById(Long id);

    /**
     * 根据配置键获取系统配置
     * @param configKey 配置键
     * @return 系统配置信息
     */
    SystemConfig getSystemConfigByKey(String configKey);

    /**
     * 根据配置名称获取系统配置
     * @param configName 配置名称
     * @return 系统配置信息
     */
    SystemConfig getSystemConfigByName(String configName);

    /**
     * 更新系统配置
     * @param systemConfig 系统配置信息
     * @return 更新后的系统配置
     */
    SystemConfig updateSystemConfig(SystemConfig systemConfig);

    /**
     * 根据配置键更新配置值
     * @param configKey 配置键
     * @param configValue 配置值
     * @param updatedBy 更新人ID
     * @return 是否更新成功
     */
    boolean updateConfigValue(String configKey, String configValue, Long updatedBy);

    /**
     * 批量更新配置值
     * @param configMap 配置映射（键 -> 值）
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdateConfigValues(Map<String, String> configMap, Long updatedBy);

    /**
     * 删除系统配置（软删除）
     * @param id 配置ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteSystemConfig(Long id, Long deletedBy);

    /**
     * 根据配置键删除系统配置
     * @param configKey 配置键
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteSystemConfigByKey(String configKey, Long deletedBy);

    /**
     * 批量删除系统配置（软删除）
     * @param ids 配置ID列表
     * @param deletedBy 删除人ID
     * @return 删除成功的数量
     */
    int batchDeleteSystemConfigs(List<Long> ids, Long deletedBy);

    /**
     * 物理删除系统配置
     * @param id 配置ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean physicalDeleteSystemConfig(Long id, Long deletedBy);

    /**
     * 启用系统配置
     * @param id 配置ID
     * @param updatedBy 更新人ID
     * @return 是否启用成功
     */
    boolean enableSystemConfig(Long id, Long updatedBy);

    /**
     * 禁用系统配置
     * @param id 配置ID
     * @param updatedBy 更新人ID
     * @return 是否禁用成功
     */
    boolean disableSystemConfig(Long id, Long updatedBy);

    /**
     * 根据配置键启用配置
     * @param configKey 配置键
     * @param updatedBy 更新人ID
     * @return 是否启用成功
     */
    boolean enableConfigByKey(String configKey, Long updatedBy);

    /**
     * 根据配置键禁用配置
     * @param configKey 配置键
     * @param updatedBy 更新人ID
     * @return 是否禁用成功
     */
    boolean disableConfigByKey(String configKey, Long updatedBy);

    /**
     * 批量更新配置状态
     * @param ids 配置ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdateConfigStatus(List<Long> ids, Integer status, Long updatedBy);

    /**
     * 检查配置键是否存在
     * @param configKey 配置键
     * @return 是否存在
     */
    boolean existsByConfigKey(String configKey);

    /**
     * 检查配置名称是否存在
     * @param configName 配置名称
     * @return 是否存在
     */
    boolean existsByConfigName(String configName);

    /**
     * 分页查询系统配置
     * @param page 分页参数
     * @param configKey 配置键（可选）
     * @param configName 配置名称（可选）
     * @param category 配置分类（可选）
     * @param status 状态（可选）
     * @param isSystem 是否系统配置（可选）
     * @param isEncrypted 是否加密（可选）
     * @param createdBy 创建人ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    IPage<SystemConfig> findSystemConfigsWithPagination(Page<SystemConfig> page, String configKey, 
                                                        String configName, String category, Integer status, 
                                                        Boolean isSystem, Boolean isEncrypted, Long createdBy, 
                                                        LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 根据分类查找配置列表
     * @param category 配置分类
     * @return 配置列表
     */
    List<SystemConfig> findSystemConfigsByCategory(String category);

    /**
     * 根据状态查找配置列表
     * @param status 状态
     * @return 配置列表
     */
    List<SystemConfig> findSystemConfigsByStatus(Integer status);

    /**
     * 查找系统配置列表
     * @return 系统配置列表
     */
    List<SystemConfig> findSystemConfigs();

    /**
     * 查找用户配置列表
     * @return 用户配置列表
     */
    List<SystemConfig> findUserConfigs();

    /**
     * 查找加密配置列表
     * @return 加密配置列表
     */
    List<SystemConfig> findEncryptedConfigs();

    /**
     * 根据创建人查找配置列表
     * @param createdBy 创建人ID
     * @return 配置列表
     */
    List<SystemConfig> findSystemConfigsByCreatedBy(Long createdBy);

    /**
     * 根据更新时间范围查找配置列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 配置列表
     */
    List<SystemConfig> findSystemConfigsByUpdateDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计系统配置总数
     * @return 总数
     */
    long countSystemConfigs();

    /**
     * 根据分类统计配置数量
     * @param category 配置分类
     * @return 数量
     */
    long countSystemConfigsByCategory(String category);

    /**
     * 根据状态统计配置数量
     * @param status 状态
     * @return 数量
     */
    long countSystemConfigsByStatus(Integer status);

    /**
     * 统计系统配置数量
     * @return 数量
     */
    long countSystemConfigsOnly();

    /**
     * 统计用户配置数量
     * @return 数量
     */
    long countUserConfigs();

    /**
     * 统计加密配置数量
     * @return 数量
     */
    long countEncryptedConfigs();

    /**
     * 获取配置分类统计
     * @return 分类统计结果
     */
    List<Map<String, Object>> getCategoryStatistics();

    /**
     * 获取配置状态统计
     * @return 状态统计结果
     */
    List<Map<String, Object>> getStatusStatistics();

    /**
     * 获取配置类型统计
     * @return 类型统计结果
     */
    List<Map<String, Object>> getTypeStatistics();

    /**
     * 获取配置更新趋势统计
     * @param days 统计天数
     * @return 趋势统计结果
     */
    List<Map<String, Object>> getUpdateTrend(int days);

    /**
     * 获取配置值（字符串类型）
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getStringValue(String configKey, String defaultValue);

    /**
     * 获取配置值（整数类型）
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Integer getIntValue(String configKey, Integer defaultValue);

    /**
     * 获取配置值（长整数类型）
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Long getLongValue(String configKey, Long defaultValue);

    /**
     * 获取配置值（双精度浮点数类型）
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Double getDoubleValue(String configKey, Double defaultValue);

    /**
     * 获取配置值（布尔类型）
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Boolean getBooleanValue(String configKey, Boolean defaultValue);

    /**
     * 获取配置值（JSON对象类型）
     * @param configKey 配置键
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 配置值
     */
    <T> T getJsonValue(String configKey, Class<T> clazz);

    /**
     * 获取配置值（列表类型）
     * @param configKey 配置键
     * @param separator 分隔符
     * @return 配置值列表
     */
    List<String> getListValue(String configKey, String separator);

    /**
     * 获取配置值（映射类型）
     * @param configKey 配置键
     * @return 配置值映射
     */
    Map<String, String> getMapValue(String configKey);

    /**
     * 设置配置值（字符串类型）
     * @param configKey 配置键
     * @param value 配置值
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setStringValue(String configKey, String value, Long updatedBy);

    /**
     * 设置配置值（整数类型）
     * @param configKey 配置键
     * @param value 配置值
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setIntValue(String configKey, Integer value, Long updatedBy);

    /**
     * 设置配置值（长整数类型）
     * @param configKey 配置键
     * @param value 配置值
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setLongValue(String configKey, Long value, Long updatedBy);

    /**
     * 设置配置值（双精度浮点数类型）
     * @param configKey 配置键
     * @param value 配置值
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setDoubleValue(String configKey, Double value, Long updatedBy);

    /**
     * 设置配置值（布尔类型）
     * @param configKey 配置键
     * @param value 配置值
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setBooleanValue(String configKey, Boolean value, Long updatedBy);

    /**
     * 设置配置值（JSON对象类型）
     * @param configKey 配置键
     * @param value 配置值
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setJsonValue(String configKey, Object value, Long updatedBy);

    /**
     * 设置配置值（列表类型）
     * @param configKey 配置键
     * @param values 配置值列表
     * @param separator 分隔符
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setListValue(String configKey, List<String> values, String separator, Long updatedBy);

    /**
     * 设置配置值（映射类型）
     * @param configKey 配置键
     * @param values 配置值映射
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setMapValue(String configKey, Map<String, String> values, Long updatedBy);

    /**
     * 加密配置值
     * @param id 配置ID
     * @param updatedBy 更新人ID
     * @return 是否加密成功
     */
    boolean encryptConfigValue(Long id, Long updatedBy);

    /**
     * 解密配置值
     * @param id 配置ID
     * @param updatedBy 更新人ID
     * @return 是否解密成功
     */
    boolean decryptConfigValue(Long id, Long updatedBy);

    /**
     * 根据配置键加密配置值
     * @param configKey 配置键
     * @param updatedBy 更新人ID
     * @return 是否加密成功
     */
    boolean encryptConfigValueByKey(String configKey, Long updatedBy);

    /**
     * 根据配置键解密配置值
     * @param configKey 配置键
     * @param updatedBy 更新人ID
     * @return 是否解密成功
     */
    boolean decryptConfigValueByKey(String configKey, Long updatedBy);

    /**
     * 批量加密配置值
     * @param ids 配置ID列表
     * @param updatedBy 更新人ID
     * @return 加密成功的数量
     */
    int batchEncryptConfigValues(List<Long> ids, Long updatedBy);

    /**
     * 批量解密配置值
     * @param ids 配置ID列表
     * @param updatedBy 更新人ID
     * @return 解密成功的数量
     */
    int batchDecryptConfigValues(List<Long> ids, Long updatedBy);

    /**
     * 验证配置值
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 验证结果
     */
    Map<String, Object> validateConfigValue(String configKey, String configValue);

    /**
     * 批量验证配置值
     * @param configMap 配置映射
     * @return 验证结果
     */
    Map<String, Object> batchValidateConfigValues(Map<String, String> configMap);

    /**
     * 重置配置为默认值
     * @param configKey 配置键
     * @param updatedBy 更新人ID
     * @return 是否重置成功
     */
    boolean resetConfigToDefault(String configKey, Long updatedBy);

    /**
     * 批量重置配置为默认值
     * @param configKeys 配置键列表
     * @param updatedBy 更新人ID
     * @return 重置成功的数量
     */
    int batchResetConfigsToDefault(List<String> configKeys, Long updatedBy);

    /**
     * 重置所有配置为默认值
     * @param updatedBy 更新人ID
     * @return 重置成功的数量
     */
    int resetAllConfigsToDefault(Long updatedBy);

    /**
     * 导入配置
     * @param configData 配置数据
     * @param importMode 导入模式（merge/replace/skip）
     * @param importedBy 导入人ID
     * @return 导入结果
     */
    Map<String, Object> importConfigs(Map<String, String> configData, String importMode, Long importedBy);

    /**
     * 导出配置
     * @param configKeys 配置键列表（可选，为空则导出所有）
     * @param format 导出格式（json/properties/yaml）
     * @param includeEncrypted 是否包含加密配置
     * @return 导出结果
     */
    Map<String, Object> exportConfigs(List<String> configKeys, String format, boolean includeEncrypted);

    /**
     * 备份配置
     * @param backupName 备份名称
     * @param description 备份描述
     * @param createdBy 创建人ID
     * @return 备份结果
     */
    Map<String, Object> backupConfigs(String backupName, String description, Long createdBy);

    /**
     * 恢复配置
     * @param backupId 备份ID
     * @param restoreMode 恢复模式（merge/replace）
     * @param restoredBy 恢复人ID
     * @return 恢复结果
     */
    Map<String, Object> restoreConfigs(Long backupId, String restoreMode, Long restoredBy);

    /**
     * 获取配置备份列表
     * @return 备份列表
     */
    List<Map<String, Object>> getConfigBackups();

    /**
     * 删除配置备份
     * @param backupId 备份ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteConfigBackup(Long backupId, Long deletedBy);

    /**
     * 同步配置到缓存
     * @param configKeys 配置键列表（可选，为空则同步所有）
     * @return 同步结果
     */
    Map<String, Object> syncConfigsToCache(List<String> configKeys);

    /**
     * 从缓存刷新配置
     * @param configKeys 配置键列表（可选，为空则刷新所有）
     * @return 刷新结果
     */
    Map<String, Object> refreshConfigsFromCache(List<String> configKeys);

    /**
     * 清理配置缓存
     * @param configKeys 配置键列表（可选，为空则清理所有）
     * @return 清理结果
     */
    Map<String, Object> clearConfigCache(List<String> configKeys);

    /**
     * 预热配置缓存
     * @return 预热结果
     */
    Map<String, Object> warmupConfigCache();

    /**
     * 获取配置变更历史
     * @param configKey 配置键
     * @param limit 数量限制
     * @return 变更历史
     */
    List<Map<String, Object>> getConfigChangeHistory(String configKey, int limit);

    /**
     * 比较配置版本
     * @param configKey 配置键
     * @param version1 版本1
     * @param version2 版本2
     * @return 比较结果
     */
    Map<String, Object> compareConfigVersions(String configKey, Long version1, Long version2);

    /**
     * 回滚配置到指定版本
     * @param configKey 配置键
     * @param versionId 版本ID
     * @param rolledBackBy 回滚人ID
     * @return 是否回滚成功
     */
    boolean rollbackConfigToVersion(String configKey, Long versionId, Long rolledBackBy);

    /**
     * 搜索配置
     * @param keyword 关键词
     * @param searchFields 搜索字段
     * @param filters 过滤条件
     * @param page 分页参数
     * @return 搜索结果
     */
    IPage<SystemConfig> searchConfigs(String keyword, List<String> searchFields, 
                                     Map<String, Object> filters, Page<SystemConfig> page);

    /**
     * 获取配置依赖关系
     * @param configKey 配置键
     * @return 依赖关系
     */
    Map<String, Object> getConfigDependencies(String configKey);

    /**
     * 检查配置一致性
     * @return 一致性检查结果
     */
    Map<String, Object> checkConfigConsistency();

    /**
     * 修复配置不一致
     * @param fixOptions 修复选项
     * @param fixedBy 修复人ID
     * @return 修复结果
     */
    Map<String, Object> fixConfigInconsistency(Map<String, Object> fixOptions, Long fixedBy);

    /**
     * 生成配置报告
     * @param reportType 报告类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告数据
     */
    Map<String, Object> generateConfigReport(String reportType, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 监控配置变更
     * @param configKeys 监控的配置键列表
     * @param callback 变更回调
     * @return 是否设置成功
     */
    boolean monitorConfigChanges(List<String> configKeys, String callback);

    /**
     * 停止监控配置变更
     * @param configKeys 配置键列表
     * @return 是否停止成功
     */
    boolean stopMonitoringConfigChanges(List<String> configKeys);

    /**
     * 获取配置监控状态
     * @return 监控状态
     */
    Map<String, Object> getConfigMonitoringStatus();

    /**
     * 刷新配置缓存
     * @param configKey 配置键（可选，为空则刷新所有）
     * @return 是否刷新成功
     */
    boolean refreshConfigCache(String configKey);

    /**
     * 重建配置索引
     * @return 重建结果
     */
    Map<String, Object> rebuildConfigIndex();
}