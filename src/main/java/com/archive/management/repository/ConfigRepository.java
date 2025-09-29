package com.archive.management.repository;

import com.archive.management.entity.Config;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 系统配置数据访问层接口
 * 提供系统配置实体的数据库操作方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Repository
public interface ConfigRepository extends JpaRepository<Config, Long>, JpaSpecificationExecutor<Config> {

    /**
     * 根据配置键查找配置
     * @param configKey 配置键
     * @return 配置信息
     */
    Optional<Config> findByConfigKey(String configKey);

    /**
     * 根据配置名称查找配置
     * @param configName 配置名称
     * @return 配置信息
     */
    Optional<Config> findByConfigName(String configName);

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
     * 根据配置分组查找配置列表
     * @param configGroup 配置分组
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByConfigGroup(String configGroup, Pageable pageable);

    /**
     * 根据配置类型查找配置列表
     * @param configType 配置类型
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByConfigType(String configType, Pageable pageable);

    /**
     * 根据配置状态查找配置列表
     * @param status 配置状态
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据是否可编辑查找配置列表
     * @param editable 是否可编辑
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByEditable(Boolean editable, Pageable pageable);

    /**
     * 根据是否加密存储查找配置列表
     * @param encrypted 是否加密存储
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByEncrypted(Boolean encrypted, Pageable pageable);

    /**
     * 模糊查询配置（根据键、名称、描述）
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    @Query("SELECT c FROM Config c WHERE c.configKey LIKE %:keyword% OR c.configName LIKE %:keyword% OR c.description LIKE %:keyword%")
    Page<Config> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据配置键模糊查询
     * @param configKey 配置键
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByConfigKeyContaining(String configKey, Pageable pageable);

    /**
     * 根据配置名称模糊查询
     * @param configName 配置名称
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByConfigNameContaining(String configName, Pageable pageable);

    /**
     * 查找指定时间范围内创建的配置
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找指定时间范围内更新的配置
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByUpdateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据排序号排序查找配置
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByOrderBySortOrderAsc(Pageable pageable);

    /**
     * 查找指定分组下的所有配置（按排序号排序）
     * @param configGroup 配置分组
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.configGroup = :configGroup ORDER BY c.sortOrder ASC")
    List<Config> findByGroupOrderBySortOrder(@Param("configGroup") String configGroup);

    /**
     * 查找所有启用状态的配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.status = 1 ORDER BY c.configGroup, c.sortOrder ASC")
    List<Config> findAllActiveConfigs();

    /**
     * 查找系统内置配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.isSystem = true ORDER BY c.configGroup, c.sortOrder ASC")
    List<Config> findSystemConfigs();

    /**
     * 查找非系统内置配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.isSystem = false ORDER BY c.configGroup, c.sortOrder ASC")
    List<Config> findCustomConfigs();

    /**
     * 查找可编辑的配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.editable = true AND c.status = 1 ORDER BY c.configGroup, c.sortOrder ASC")
    List<Config> findEditableConfigs();

    /**
     * 查找加密存储的配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.encrypted = true ORDER BY c.configGroup, c.sortOrder ASC")
    List<Config> findEncryptedConfigs();

    /**
     * 根据配置键列表查找配置
     * @param configKeys 配置键列表
     * @return 配置列表
     */
    List<Config> findByConfigKeyIn(List<String> configKeys);

    /**
     * 根据配置分组列表查找配置
     * @param configGroups 配置分组列表
     * @return 配置列表
     */
    List<Config> findByConfigGroupIn(List<String> configGroups);

    /**
     * 根据配置类型列表查找配置
     * @param configTypes 配置类型列表
     * @return 配置列表
     */
    List<Config> findByConfigTypeIn(List<String> configTypes);

    /**
     * 统计配置总数
     * @return 配置总数
     */
    @Query("SELECT COUNT(c) FROM Config c")
    long countAllConfigs();

    /**
     * 统计指定状态的配置数量
     * @param status 配置状态
     * @return 配置数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定分组的配置数量
     * @param configGroup 配置分组
     * @return 配置数量
     */
    long countByConfigGroup(String configGroup);

    /**
     * 统计指定类型的配置数量
     * @param configType 配置类型
     * @return 配置数量
     */
    long countByConfigType(String configType);

    /**
     * 统计可编辑的配置数量
     * @return 配置数量
     */
    @Query("SELECT COUNT(c) FROM Config c WHERE c.editable = true")
    long countEditableConfigs();

    /**
     * 统计加密存储的配置数量
     * @return 配置数量
     */
    @Query("SELECT COUNT(c) FROM Config c WHERE c.encrypted = true")
    long countEncryptedConfigs();

    /**
     * 统计系统内置配置数量
     * @return 配置数量
     */
    @Query("SELECT COUNT(c) FROM Config c WHERE c.isSystem = true")
    long countSystemConfigs();

    /**
     * 统计自定义配置数量
     * @return 配置数量
     */
    @Query("SELECT COUNT(c) FROM Config c WHERE c.isSystem = false")
    long countCustomConfigs();

    /**
     * 统计指定时间范围内创建的配置数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 配置数量
     */
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定时间范围内更新的配置数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 配置数量
     */
    long countByUpdateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 检查配置键是否已被其他配置使用（排除指定配置）
     * @param configKey 配置键
     * @param excludeId 排除的配置ID
     * @return 是否已被使用
     */
    @Query("SELECT COUNT(c) > 0 FROM Config c WHERE c.configKey = :configKey AND c.id != :excludeId")
    boolean existsByConfigKeyAndIdNot(@Param("configKey") String configKey, @Param("excludeId") Long excludeId);

    /**
     * 检查配置名称是否已被其他配置使用（排除指定配置）
     * @param configName 配置名称
     * @param excludeId 排除的配置ID
     * @return 是否已被使用
     */
    @Query("SELECT COUNT(c) > 0 FROM Config c WHERE c.configName = :configName AND c.id != :excludeId")
    boolean existsByConfigNameAndIdNot(@Param("configName") String configName, @Param("excludeId") Long excludeId);

    /**
     * 根据配置ID列表批量查询配置
     * @param configIds 配置ID列表
     * @return 配置列表
     */
    List<Config> findByIdIn(List<Long> configIds);

    /**
     * 批量更新配置状态
     * @param configIds 配置ID列表
     * @param status 新状态
     */
    @Query("UPDATE Config c SET c.status = :status WHERE c.id IN :configIds")
    void batchUpdateStatus(@Param("configIds") List<Long> configIds, @Param("status") Integer status);

    /**
     * 批量更新配置值
     * @param configKey 配置键
     * @param configValue 新配置值
     */
    @Query("UPDATE Config c SET c.configValue = :configValue WHERE c.configKey = :configKey")
    void updateConfigValue(@Param("configKey") String configKey, @Param("configValue") String configValue);

    /**
     * 更新配置的排序号
     * @param configId 配置ID
     * @param sortOrder 新排序号
     */
    @Query("UPDATE Config c SET c.sortOrder = :sortOrder WHERE c.id = :configId")
    void updateSortOrder(@Param("configId") Long configId, @Param("sortOrder") Integer sortOrder);

    /**
     * 获取指定分组下的最大排序号
     * @param configGroup 配置分组
     * @return 最大排序号
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM Config c WHERE c.configGroup = :configGroup")
    Integer getMaxSortOrderByGroup(@Param("configGroup") String configGroup);

    /**
     * 软删除配置（更新删除标记）
     * @param configId 配置ID
     * @param deletedBy 删除人ID
     * @param deleteTime 删除时间
     */
    @Query("UPDATE Config c SET c.deleted = true, c.deletedBy = :deletedBy, c.deleteTime = :deleteTime WHERE c.id = :configId")
    void softDeleteConfig(@Param("configId") Long configId, 
                         @Param("deletedBy") Long deletedBy, 
                         @Param("deleteTime") LocalDateTime deleteTime);

    /**
     * 查找未删除的配置
     * @param pageable 分页参数
     * @return 配置分页列表
     */
    Page<Config> findByDeletedFalse(Pageable pageable);

    /**
     * 根据配置键查找未删除的配置
     * @param configKey 配置键
     * @return 配置信息
     */
    Optional<Config> findByConfigKeyAndDeletedFalse(String configKey);

    /**
     * 根据配置名称查找未删除的配置
     * @param configName 配置名称
     * @return 配置信息
     */
    Optional<Config> findByConfigNameAndDeletedFalse(String configName);

    /**
     * 查找指定分组下未删除的配置
     * @param configGroup 配置分组
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.configGroup = :configGroup AND c.deleted = false ORDER BY c.sortOrder ASC")
    List<Config> findByGroupNotDeleted(@Param("configGroup") String configGroup);

    /**
     * 按分组统计配置数量
     * @return 分组统计结果
     */
    @Query("SELECT c.configGroup as configGroup, COUNT(c) as count FROM Config c WHERE c.deleted = false GROUP BY c.configGroup ORDER BY configGroup")
    List<Object[]> countConfigsByGroup();

    /**
     * 按类型统计配置数量
     * @return 类型统计结果
     */
    @Query("SELECT c.configType as configType, COUNT(c) as count FROM Config c WHERE c.deleted = false GROUP BY c.configType ORDER BY configType")
    List<Object[]> countConfigsByType();

    /**
     * 按状态统计配置数量
     * @return 状态统计结果
     */
    @Query("SELECT c.status as status, COUNT(c) as count FROM Config c WHERE c.deleted = false GROUP BY c.status ORDER BY status")
    List<Object[]> countConfigsByStatus();

    /**
     * 获取配置分组列表
     * @return 分组列表
     */
    @Query("SELECT DISTINCT c.configGroup FROM Config c WHERE c.deleted = false ORDER BY c.configGroup")
    List<String> findAllConfigGroups();

    /**
     * 获取配置类型列表
     * @return 类型列表
     */
    @Query("SELECT DISTINCT c.configType FROM Config c WHERE c.deleted = false ORDER BY c.configType")
    List<String> findAllConfigTypes();

    /**
     * 查找系统配置（按分组和排序号排序）
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.isSystem = true AND c.deleted = false ORDER BY c.configGroup, c.sortOrder")
    List<Config> findSystemConfigsOrdered();

    /**
     * 查找用户配置（按分组和排序号排序）
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.isSystem = false AND c.deleted = false ORDER BY c.configGroup, c.sortOrder")
    List<Config> findUserConfigsOrdered();

    /**
     * 查找邮件配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.configGroup = 'EMAIL' AND c.status = 1 AND c.deleted = false ORDER BY c.sortOrder")
    List<Config> findEmailConfigs();

    /**
     * 查找数据库配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.configGroup = 'DATABASE' AND c.status = 1 AND c.deleted = false ORDER BY c.sortOrder")
    List<Config> findDatabaseConfigs();

    /**
     * 查找安全配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.configGroup = 'SECURITY' AND c.status = 1 AND c.deleted = false ORDER BY c.sortOrder")
    List<Config> findSecurityConfigs();

    /**
     * 查找文件配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.configGroup = 'FILE' AND c.status = 1 AND c.deleted = false ORDER BY c.sortOrder")
    List<Config> findFileConfigs();

    /**
     * 查找缓存配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.configGroup = 'CACHE' AND c.status = 1 AND c.deleted = false ORDER BY c.sortOrder")
    List<Config> findCacheConfigs();

    /**
     * 查找日志配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.configGroup = 'LOG' AND c.status = 1 AND c.deleted = false ORDER BY c.sortOrder")
    List<Config> findLogConfigs();

    /**
     * 查找应用配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.configGroup = 'APPLICATION' AND c.status = 1 AND c.deleted = false ORDER BY c.sortOrder")
    List<Config> findApplicationConfigs();

    /**
     * 根据配置值查找配置
     * @param configValue 配置值
     * @return 配置列表
     */
    List<Config> findByConfigValue(String configValue);

    /**
     * 根据配置值模糊查询
     * @param configValue 配置值
     * @return 配置列表
     */
    List<Config> findByConfigValueContaining(String configValue);

    /**
     * 查找需要验证的配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.validationRule IS NOT NULL AND c.validationRule != '' AND c.deleted = false ORDER BY c.configGroup, c.sortOrder")
    List<Config> findConfigsWithValidation();

    /**
     * 查找有默认值的配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.defaultValue IS NOT NULL AND c.defaultValue != '' AND c.deleted = false ORDER BY c.configGroup, c.sortOrder")
    List<Config> findConfigsWithDefaultValue();

    /**
     * 查找有值范围限制的配置
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.valueRange IS NOT NULL AND c.valueRange != '' AND c.deleted = false ORDER BY c.configGroup, c.sortOrder")
    List<Config> findConfigsWithValueRange();

    /**
     * 获取配置的最后更新时间
     * @param configKey 配置键
     * @return 最后更新时间
     */
    @Query("SELECT c.updateTime FROM Config c WHERE c.configKey = :configKey AND c.deleted = false")
    Optional<LocalDateTime> getLastUpdateTime(@Param("configKey") String configKey);

    /**
     * 检查配置是否为系统内置配置
     * @param configKey 配置键
     * @return 是否为系统配置
     */
    @Query("SELECT c.isSystem FROM Config c WHERE c.configKey = :configKey AND c.deleted = false")
    Optional<Boolean> isSystemConfig(@Param("configKey") String configKey);

    /**
     * 检查配置是否可编辑
     * @param configKey 配置键
     * @return 是否可编辑
     */
    @Query("SELECT c.editable FROM Config c WHERE c.configKey = :configKey AND c.deleted = false")
    Optional<Boolean> isConfigEditable(@Param("configKey") String configKey);

    /**
     * 检查配置是否加密存储
     * @param configKey 配置键
     * @return 是否加密存储
     */
    @Query("SELECT c.encrypted FROM Config c WHERE c.configKey = :configKey AND c.deleted = false")
    Optional<Boolean> isConfigEncrypted(@Param("configKey") String configKey);
}