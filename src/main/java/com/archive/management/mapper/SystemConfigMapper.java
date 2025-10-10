package com.archive.management.mapper;

import com.archive.management.entity.SystemConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统配置数据访问层接口
 * 基于MyBatis-Plus实现
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键查找系统配置
     * @param configKey 配置键
     * @return 系统配置
     */
    @Select("SELECT * FROM system_configs WHERE config_key = #{configKey} AND deleted = 0")
    SystemConfig findByConfigKey(@Param("configKey") String configKey);

    /**
     * 检查配置键是否存在 (SystemService调用)
     * @param configKey 配置键
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM system_configs WHERE config_key = #{configKey} AND deleted = 0")
    boolean existsByKey(@Param("configKey") String configKey);

    /**
     * 根据配置键查询系统配置 (SystemService调用)
     * @param configKey 配置键
     * @return 系统配置
     */
    @Select("SELECT * FROM system_configs WHERE config_key = #{configKey} AND deleted = 0")
    SystemConfig selectByKey(@Param("configKey") String configKey);

    /**
     * 根据配置名称查找系统配置
     * @param configName 配置名称
     * @return 系统配置
     */
    @Select("SELECT * FROM system_configs WHERE config_name = #{configName} AND deleted = 0")
    SystemConfig findByConfigName(@Param("configName") String configName);

    /**
     * 检查配置键是否存在
     * @param configKey 配置键
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM system_configs WHERE config_key = #{configKey} AND deleted = 0")
    boolean existsByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据配置类型查找系统配置列表
     * @param configType 配置类型
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE config_type = #{configType} AND deleted = 0 ORDER BY sort_order, config_key")
    List<SystemConfig> findByConfigType(@Param("configType") String configType);

    /**
     * 根据配置分组查找系统配置列表
     * @param configGroup 配置分组
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE config_group = #{configGroup} AND deleted = 0 ORDER BY sort_order, config_key")
    List<SystemConfig> findByConfigGroup(@Param("configGroup") String configGroup);

    /**
     * 根据状态查找系统配置列表
     * @param status 状态
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE status = #{status} AND deleted = 0 ORDER BY sort_order, config_key")
    List<SystemConfig> findByStatus(@Param("status") Integer status);

    /**
     * 根据是否可编辑查找系统配置列表
     * @param editable 是否可编辑
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE editable = #{editable} AND deleted = 0 ORDER BY sort_order, config_key")
    List<SystemConfig> findByEditable(@Param("editable") Boolean editable);

    /**
     * 根据是否敏感查找系统配置列表
     * @param sensitive 是否敏感
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE sensitive = #{sensitive} AND deleted = 0 ORDER BY sort_order, config_key")
    List<SystemConfig> findBySensitive(@Param("sensitive") Boolean sensitive);

    /**
     * 查找有效期内的系统配置列表
     * @param currentTime 当前时间
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE " +
            "(valid_from IS NULL OR valid_from <= #{currentTime}) " +
            "AND (valid_until IS NULL OR valid_until >= #{currentTime}) " +
            "AND deleted = 0 ORDER BY sort_order, config_key")
    List<SystemConfig> findValidConfigs(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找已过期的系统配置列表
     * @param currentTime 当前时间
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE valid_until IS NOT NULL AND valid_until < #{currentTime} " +
            "AND deleted = 0 ORDER BY sort_order, config_key")
    List<SystemConfig> findExpiredConfigs(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 分页查询系统配置
     * @param page 分页参数
     * @param configType 配置类型（可选）
     * @param configGroup 配置分组（可选）
     * @param status 状态（可选）
     * @param editable 是否可编辑（可选）
     * @param sensitive 是否敏感（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM system_configs WHERE deleted = 0" +
            "<if test='configType != null and configType != \"\"'> AND config_type = #{configType}</if>" +
            "<if test='configGroup != null and configGroup != \"\"'> AND config_group = #{configGroup}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='editable != null'> AND editable = #{editable}</if>" +
            "<if test='sensitive != null'> AND sensitive = #{sensitive}</if>" +
            " ORDER BY sort_order, config_key" +
            "</script>")
    IPage<SystemConfig> findSystemConfigsWithPagination(Page<SystemConfig> page,
                                                        @Param("configType") String configType,
                                                        @Param("configGroup") String configGroup,
                                                        @Param("status") Integer status,
                                                        @Param("editable") Boolean editable,
                                                        @Param("sensitive") Boolean sensitive);

    /**
     * 统计系统配置总数
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM system_configs WHERE deleted = 0")
    long countSystemConfigs();

    /**
     * 根据配置类型统计系统配置数量
     * @param configType 配置类型
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM system_configs WHERE config_type = #{configType} AND deleted = 0")
    long countByConfigType(@Param("configType") String configType);

    /**
     * 根据配置分组统计系统配置数量
     * @param configGroup 配置分组
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM system_configs WHERE config_group = #{configGroup} AND deleted = 0")
    long countByConfigGroup(@Param("configGroup") String configGroup);

    /**
     * 根据状态统计系统配置数量
     * @param status 状态
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM system_configs WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") Integer status);

    /**
     * 获取配置类型统计
     * @return 配置类型统计结果
     */
    @Select("SELECT config_type, COUNT(*) as count FROM system_configs WHERE deleted = 0 " +
            "GROUP BY config_type ORDER BY count DESC")
    List<Map<String, Object>> getConfigTypeStatistics();

    /**
     * 获取配置分组统计
     * @return 配置分组统计结果
     */
    @Select("SELECT config_group, COUNT(*) as count FROM system_configs WHERE deleted = 0 " +
            "GROUP BY config_group ORDER BY count DESC")
    List<Map<String, Object>> getConfigGroupStatistics();

    /**
     * 获取状态统计
     * @return 状态统计结果
     */
    @Select("SELECT status, COUNT(*) as count FROM system_configs WHERE deleted = 0 " +
            "GROUP BY status ORDER BY status")
    List<Map<String, Object>> getStatusStatistics();

    /**
     * 批量更新系统配置状态
     * @param ids ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE system_configs SET status = #{status}, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    int batchUpdateStatus(@Param("ids") List<Long> ids, 
                         @Param("status") Integer status,
                         @Param("updatedBy") Long updatedBy,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 批量软删除系统配置
     * @param ids ID列表
     * @param deletedBy 删除人ID
     * @param deletedAt 删除时间
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE system_configs SET deleted = 1, deleted_by = #{deletedBy}, deleted_at = #{deletedAt} " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    int batchSoftDelete(@Param("ids") List<Long> ids, 
                       @Param("deletedBy") Long deletedBy,
                       @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 更新配置值
     * @param configKey 配置键
     * @param configValue 配置值
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("UPDATE system_configs SET config_value = #{configValue}, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE config_key = #{configKey} AND deleted = 0")
    int updateConfigValue(@Param("configKey") String configKey,
                         @Param("configValue") String configValue,
                         @Param("updatedBy") Long updatedBy,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新配置的有效期
     * @param id 配置ID
     * @param validFrom 有效开始时间
     * @param validUntil 有效结束时间
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("UPDATE system_configs SET valid_from = #{validFrom}, valid_until = #{validUntil}, " +
            "updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE id = #{id} AND deleted = 0")
    int updateValidPeriod(@Param("id") Long id,
                         @Param("validFrom") LocalDateTime validFrom,
                         @Param("validUntil") LocalDateTime validUntil,
                         @Param("updatedBy") Long updatedBy,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新配置排序
     * @param id 配置ID
     * @param sortOrder 排序值
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("UPDATE system_configs SET sort_order = #{sortOrder}, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE id = #{id} AND deleted = 0")
    int updateSortOrder(@Param("id") Long id,
                       @Param("sortOrder") Integer sortOrder,
                       @Param("updatedBy") Long updatedBy,
                       @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 根据关键词搜索系统配置
     * @param keyword 关键词
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE " +
            "(config_key LIKE CONCAT('%', #{keyword}, '%') OR " +
            "config_name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "config_description LIKE CONCAT('%', #{keyword}, '%') OR " +
            "config_value LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY sort_order, config_key")
    List<SystemConfig> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 根据关键词和过滤条件搜索系统配置 (SystemConfigService调用)
     * @param keyword 关键词
     * @param configType 配置类型（可选）
     * @param configGroup 配置分组（可选）
     * @return 系统配置列表
     */
    @Select("<script>" +
            "SELECT * FROM system_configs WHERE deleted = 0" +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND (config_key LIKE CONCAT('%', #{keyword}, '%') OR " +
            "config_name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "config_description LIKE CONCAT('%', #{keyword}, '%') OR " +
            "config_value LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='configType != null and configType != \"\"'> AND config_type = #{configType}</if>" +
            "<if test='configGroup != null and configGroup != \"\"'> AND config_group = #{configGroup}</if>" +
            " ORDER BY sort_order, config_key" +
            "</script>")
    List<SystemConfig> searchByKeyword(@Param("keyword") String keyword,
                                      @Param("configType") String configType,
                                      @Param("configGroup") String configGroup);

    /**
     * 获取所有配置类型
     * @return 配置类型列表
     */
    @Select("SELECT DISTINCT config_type FROM system_configs WHERE deleted = 0 ORDER BY config_type")
    List<String> getAllConfigTypes();

    /**
     * 获取所有配置分组
     * @return 配置分组列表
     */
    @Select("SELECT DISTINCT config_group FROM system_configs WHERE deleted = 0 ORDER BY config_group")
    List<String> getAllConfigGroups();

    /**
     * 获取指定分组下的最大排序值
     * @param configGroup 配置分组
     * @return 最大排序值
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM system_configs " +
            "WHERE config_group = #{configGroup} AND deleted = 0")
    Integer getMaxSortOrderByGroup(@Param("configGroup") String configGroup);

    /**
     * 批量插入系统配置
     * @param configs 系统配置列表
     * @return 插入数量
     */
    @Insert("<script>" +
            "INSERT INTO system_configs (config_key, config_name, config_value, config_type, " +
            "config_group, config_description, default_value, editable, sensitive, " +
            "sort_order, status, valid_from, valid_until, tags, created_by, created_at) VALUES " +
            "<foreach collection='configs' item='config' separator=','>" +
            "(#{config.configKey}, #{config.configName}, #{config.configValue}, #{config.configType}, " +
            "#{config.configGroup}, #{config.configDescription}, #{config.defaultValue}, #{config.editable}, " +
            "#{config.sensitive}, #{config.sortOrder}, #{config.status}, #{config.validFrom}, " +
            "#{config.validUntil}, #{config.tags}, #{config.createdBy}, #{config.createdAt})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("configs") List<SystemConfig> configs);

    /**
     * 重置配置为默认值
     * @param configKey 配置键
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("UPDATE system_configs SET config_value = default_value, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE config_key = #{configKey} AND deleted = 0")
    int resetToDefault(@Param("configKey") String configKey,
                      @Param("updatedBy") Long updatedBy,
                      @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 批量重置配置为默认值
     * @param configKeys 配置键列表
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE system_configs SET config_value = default_value, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE config_key IN " +
            "<foreach collection='configKeys' item='key' open='(' separator=',' close=')'>" +
            "#{key}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    int batchResetToDefault(@Param("configKeys") List<String> configKeys,
                           @Param("updatedBy") Long updatedBy,
                           @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 根据参数统计系统配置数量 (SystemService调用)
     * @param params 查询参数 (支持: configKey, configGroup, isSystem)
     * @return 配置数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM system_configs WHERE deleted = 0" +
            "<if test='configKey != null and configKey != \"\"'> AND config_key LIKE CONCAT('%', #{configKey}, '%')</if>" +
            "<if test='configGroup != null and configGroup != \"\"'> AND config_group = #{configGroup}</if>" +
            "<if test='isSystem != null'> AND is_system = #{isSystem}</if>" +
            "</script>")
    long countByParams(@Param("configKey") String configKey,
                      @Param("configGroup") String configGroup,
                      @Param("isSystem") Boolean isSystem);

    /**
     * 根据参数统计系统配置数量 (使用Map参数)
     * @param params 查询参数Map
     * @return 配置数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM system_configs WHERE deleted = 0" +
            "<if test='params.configKey != null and params.configKey != \"\"'> AND config_key LIKE CONCAT('%', #{params.configKey}, '%')</if>" +
            "<if test='params.configGroup != null and params.configGroup != \"\"'> AND config_group = #{params.configGroup}</if>" +
            "<if test='params.isSystem != null'> AND is_system = #{params.isSystem}</if>" +
            "</script>")
    long countByParams(@Param("params") Map<String, Object> params);

    /**
     * 根据参数分页查询系统配置 (SystemService调用)
     * @param params 查询参数 (支持: configKey, configGroup, isSystem)
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 系统配置列表
     */
    @Select("<script>" +
            "SELECT * FROM system_configs WHERE deleted = 0" +
            "<if test='configKey != null and configKey != \"\"'> AND config_key LIKE CONCAT('%', #{configKey}, '%')</if>" +
            "<if test='configGroup != null and configGroup != \"\"'> AND config_group = #{configGroup}</if>" +
            "<if test='isSystem != null'> AND is_system = #{isSystem}</if>" +
            " ORDER BY config_group, sort_order, config_key" +
            " LIMIT #{offset}, #{limit}" +
            "</script>")
    List<SystemConfig> selectByParams(@Param("configKey") String configKey,
                                     @Param("configGroup") String configGroup,
                                     @Param("isSystem") Boolean isSystem,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 根据参数分页查询系统配置 (使用Map参数)
     * @param params 查询参数Map
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 系统配置列表
     */
    @Select("<script>" +
            "SELECT * FROM system_configs WHERE deleted = 0" +
            "<if test='params.configKey != null and params.configKey != \"\"'> AND config_key LIKE CONCAT('%', #{params.configKey}, '%')</if>" +
            "<if test='params.configGroup != null and params.configGroup != \"\"'> AND config_group = #{params.configGroup}</if>" +
            "<if test='params.isSystem != null'> AND is_system = #{params.isSystem}</if>" +
            " ORDER BY config_group, sort_order, config_key" +
            " LIMIT #{offset}, #{limit}" +
            "</script>")
    List<SystemConfig> selectByParams(@Param("params") Map<String, Object> params,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 根据配置分组查询系统配置列表 (SystemService调用)
     * @param group 配置分组
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE config_group = #{group} AND deleted = 0 " +
            "ORDER BY sort_order, config_key")
    List<SystemConfig> selectByGroup(@Param("group") String group);

    /**
     * 根据配置键列表查询系统配置 (SystemConfigService调用)
     * @param configKeys 配置键列表
     * @return 系统配置列表
     */
    @Select("<script>" +
            "SELECT * FROM system_configs WHERE deleted = 0 AND config_key IN " +
            "<foreach collection='configKeys' item='key' open='(' separator=',' close=')'>" +
            "#{key}" +
            "</foreach>" +
            " ORDER BY config_group, sort_order, config_key" +
            "</script>")
    List<SystemConfig> findByConfigKeys(@Param("configKeys") List<String> configKeys);

    /**
     * 查询必填的系统配置 (SystemConfigService调用)
     * @param required 是否必填
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE required = #{required} AND deleted = 0 " +
            "ORDER BY config_group, sort_order, config_key")
    List<SystemConfig> findByRequired(@Param("required") Boolean required);

    /**
     * 查询无效的系统配置 (SystemConfigService调用)
     * 无效配置定义: 必填但配置值为空或null
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_configs WHERE required = 1 AND " +
            "(config_value IS NULL OR config_value = '') AND deleted = 0 " +
            "ORDER BY config_group, sort_order, config_key")
    List<SystemConfig> findInvalidConfigs();
}