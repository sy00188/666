package com.archive.management.mapper;

import com.archive.management.entity.Config;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统配置Mapper接口
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
public interface ConfigMapper extends BaseMapper<Config> {

    /**
     * 根据配置键查询配置
     */
    @Select("SELECT * FROM sys_config WHERE config_key = #{configKey} AND deleted = 0")
    Config selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据配置名称查询配置
     */
    @Select("SELECT * FROM sys_config WHERE config_name = #{configName} AND deleted = 0")
    Config selectByConfigName(@Param("configName") String configName);

    /**
     * 根据配置分组查询配置列表
     */
    @Select("SELECT * FROM sys_config WHERE config_group = #{configGroup} AND deleted = 0 ORDER BY sort_order ASC, create_time ASC")
    List<Config> selectByConfigGroup(@Param("configGroup") String configGroup);

    /**
     * 根据配置类型查询配置列表
     */
    @Select("SELECT * FROM sys_config WHERE config_type = #{configType} AND deleted = 0 ORDER BY config_group ASC, sort_order ASC")
    List<Config> selectByConfigType(@Param("configType") String configType);

    /**
     * 查询系统配置（config_type = 'system'）
     */
    @Select("SELECT * FROM sys_config WHERE config_type = 'system' AND deleted = 0 ORDER BY config_group ASC, sort_order ASC")
    List<Config> selectSystemConfigs();

    /**
     * 查询业务配置（config_type = 'business'）
     */
    @Select("SELECT * FROM sys_config WHERE config_type = 'business' AND deleted = 0 ORDER BY config_group ASC, sort_order ASC")
    List<Config> selectBusinessConfigs();

    /**
     * 查询用户配置（config_type = 'user'）
     */
    @Select("SELECT * FROM sys_config WHERE config_type = 'user' AND deleted = 0 ORDER BY config_group ASC, sort_order ASC")
    List<Config> selectUserConfigs();

    /**
     * 查询启用的配置列表
     */
    @Select("SELECT * FROM sys_config WHERE status = 1 AND deleted = 0 ORDER BY config_group ASC, sort_order ASC")
    List<Config> selectEnabledConfigs();

    /**
     * 查询禁用的配置列表
     */
    @Select("SELECT * FROM sys_config WHERE status = 0 AND deleted = 0 ORDER BY config_group ASC, sort_order ASC")
    List<Config> selectDisabledConfigs();

    /**
     * 根据是否内置查询配置
     */
    @Select("SELECT * FROM sys_config WHERE is_builtin = #{isBuiltin} AND deleted = 0 ORDER BY config_group ASC, sort_order ASC")
    List<Config> selectByBuiltin(@Param("isBuiltin") Boolean isBuiltin);

    /**
     * 查询内置配置
     */
    @Select("SELECT * FROM sys_config WHERE is_builtin = 1 AND deleted = 0 ORDER BY config_group ASC, sort_order ASC")
    List<Config> selectBuiltinConfigs();

    /**
     * 查询自定义配置
     */
    @Select("SELECT * FROM sys_config WHERE is_builtin = 0 AND deleted = 0 ORDER BY config_group ASC, sort_order ASC")
    List<Config> selectCustomConfigs();

    /**
     * 分页查询配置（带搜索条件）
     */
    IPage<Config> selectPageWithConditions(Page<Config> page,
                                         @Param("configName") String configName,
                                         @Param("configKey") String configKey,
                                         @Param("configGroup") String configGroup,
                                         @Param("configType") String configType,
                                         @Param("status") Integer status,
                                         @Param("isBuiltin") Boolean isBuiltin);

    /**
     * 检查配置键是否唯一
     */
    @Select("SELECT COUNT(*) FROM sys_config WHERE config_key = #{configKey} AND config_id != #{excludeId} AND deleted = 0")
    int checkConfigKeyUnique(@Param("configKey") String configKey, @Param("excludeId") Long excludeId);

    /**
     * 检查配置名称是否唯一
     */
    @Select("SELECT COUNT(*) FROM sys_config WHERE config_name = #{configName} AND config_id != #{excludeId} AND deleted = 0")
    int checkConfigNameUnique(@Param("configName") String configName, @Param("excludeId") Long excludeId);

    /**
     * 获取配置分组的最大排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM sys_config WHERE config_group = #{configGroup} AND deleted = 0")
    Integer getMaxSortOrderByGroup(@Param("configGroup") String configGroup);

    /**
     * 更新配置排序
     */
    @Update("UPDATE sys_config SET sort_order = #{sortOrder}, update_time = NOW() WHERE config_id = #{configId}")
    int updateSortOrder(@Param("configId") Long configId, @Param("sortOrder") Integer sortOrder);

    /**
     * 更新配置状态
     */
    @Update("UPDATE sys_config SET status = #{status}, update_time = NOW() WHERE config_id = #{configId}")
    int updateStatus(@Param("configId") Long configId, @Param("status") Integer status);

    /**
     * 批量更新配置状态
     */
    @Update("<script>" +
            "UPDATE sys_config SET status = #{status}, update_time = NOW() " +
            "WHERE config_id IN " +
            "<foreach collection='configIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("configIds") List<Long> configIds, @Param("status") Integer status);

    /**
     * 更新配置值
     */
    @Update("UPDATE sys_config SET config_value = #{configValue}, update_time = NOW() WHERE config_key = #{configKey}")
    int updateConfigValue(@Param("configKey") String configKey, @Param("configValue") String configValue);

    /**
     * 批量更新配置值
     */
    @Update("<script>" +
            "<foreach collection='configs' item='config' separator=';'>" +
            "UPDATE sys_config SET config_value = #{config.configValue}, update_time = NOW() " +
            "WHERE config_key = #{config.configKey}" +
            "</foreach>" +
            "</script>")
    int batchUpdateConfigValues(@Param("configs") List<Config> configs);

    /**
     * 软删除配置
     */
    @Update("UPDATE sys_config SET deleted = 1, update_time = NOW() WHERE config_id = #{configId}")
    int softDelete(@Param("configId") Long configId);

    /**
     * 批量软删除配置
     */
    @Update("<script>" +
            "UPDATE sys_config SET deleted = 1, update_time = NOW() " +
            "WHERE config_id IN " +
            "<foreach collection='configIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchSoftDelete(@Param("configIds") List<Long> configIds);

    /**
     * 获取所有配置分组
     */
    @Select("SELECT DISTINCT config_group FROM sys_config WHERE deleted = 0 ORDER BY config_group")
    List<String> selectAllConfigGroups();

    /**
     * 获取所有配置类型
     */
    @Select("SELECT DISTINCT config_type FROM sys_config WHERE deleted = 0 ORDER BY config_type")
    List<String> selectAllConfigTypes();

    /**
     * 统计配置数量
     */
    @Select("SELECT COUNT(*) FROM sys_config WHERE deleted = 0")
    int countConfigs();

    /**
     * 统计启用的配置数量
     */
    @Select("SELECT COUNT(*) FROM sys_config WHERE status = 1 AND deleted = 0")
    int countEnabledConfigs();

    /**
     * 统计各分组配置数量
     */
    @Select("SELECT " +
            "config_group, " +
            "COUNT(*) as count " +
            "FROM sys_config " +
            "WHERE deleted = 0 " +
            "GROUP BY config_group " +
            "ORDER BY config_group")
    List<Map<String, Object>> countByConfigGroup();

    /**
     * 统计各类型配置数量
     */
    @Select("SELECT " +
            "config_type, " +
            "COUNT(*) as count " +
            "FROM sys_config " +
            "WHERE deleted = 0 " +
            "GROUP BY config_type " +
            "ORDER BY config_type")
    List<Map<String, Object>> countByConfigType();

    /**
     * 统计各状态配置数量
     */
    @Select("SELECT " +
            "status, " +
            "COUNT(*) as count " +
            "FROM sys_config " +
            "WHERE deleted = 0 " +
            "GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 获取配置创建统计（按月）
     */
    @Select("SELECT " +
            "DATE_FORMAT(create_time, '%Y-%m') as month, " +
            "COUNT(*) as count " +
            "FROM sys_config " +
            "WHERE deleted = 0 AND create_time >= #{startTime} " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY month DESC")
    List<Map<String, Object>> getConfigCreateStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 查询最近创建的配置
     */
    @Select("SELECT * FROM sys_config WHERE deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<Config> selectRecentCreated(@Param("limit") int limit);

    /**
     * 查询最近更新的配置
     */
    @Select("SELECT * FROM sys_config WHERE deleted = 0 AND update_time IS NOT NULL ORDER BY update_time DESC LIMIT #{limit}")
    List<Config> selectRecentUpdated(@Param("limit") int limit);

    /**
     * 根据关键字搜索配置（配置名称、键或描述）
     */
    @Select("SELECT * FROM sys_config " +
            "WHERE (config_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR config_key LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 " +
            "ORDER BY config_group ASC, sort_order ASC")
    List<Config> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取配置的历史版本（如果有版本控制）
     */
    @Select("SELECT * FROM sys_config_history WHERE config_key = #{configKey} ORDER BY create_time DESC")
    List<Map<String, Object>> selectConfigHistory(@Param("configKey") String configKey);

    /**
     * 导出配置数据
     */
    @Select("SELECT config_key, config_value, config_name, config_group, config_type, description " +
            "FROM sys_config WHERE deleted = 0 ORDER BY config_group ASC, sort_order ASC")
    List<Map<String, Object>> exportConfigs();

    /**
     * 根据配置分组导出配置
     */
    @Select("SELECT config_key, config_value, config_name, config_group, config_type, description " +
            "FROM sys_config WHERE config_group = #{configGroup} AND deleted = 0 ORDER BY sort_order ASC")
    List<Map<String, Object>> exportConfigsByGroup(@Param("configGroup") String configGroup);

    /**
     * 获取配置键值对映射（用于缓存）
     */
    @Select("SELECT config_key, config_value FROM sys_config WHERE status = 1 AND deleted = 0")
    List<Map<String, String>> selectConfigKeyValueMap();

    /**
     * 根据配置分组获取键值对映射
     */
    @Select("SELECT config_key, config_value FROM sys_config WHERE config_group = #{configGroup} AND status = 1 AND deleted = 0")
    List<Map<String, String>> selectConfigKeyValueMapByGroup(@Param("configGroup") String configGroup);

    /**
     * 检查配置是否被引用（业务逻辑中使用）
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_config_usage WHERE config_key = #{configKey}")
    boolean isConfigInUse(@Param("configKey") String configKey);

    /**
     * 获取配置使用统计
     */
    @Select("SELECT " +
            "c.config_key, " +
            "c.config_name, " +
            "COUNT(cu.usage_id) as usage_count, " +
            "MAX(cu.last_used_time) as last_used_time " +
            "FROM sys_config c " +
            "LEFT JOIN sys_config_usage cu ON c.config_key = cu.config_key " +
            "WHERE c.deleted = 0 " +
            "GROUP BY c.config_key, c.config_name " +
            "ORDER BY usage_count DESC")
    List<Map<String, Object>> getConfigUsageStatistics();

    /**
     * 重置配置为默认值
     */
    @Update("UPDATE sys_config SET config_value = default_value, update_time = NOW() WHERE config_key = #{configKey}")
    int resetToDefault(@Param("configKey") String configKey);

    /**
     * 批量重置配置为默认值
     */
    @Update("<script>" +
            "UPDATE sys_config SET config_value = default_value, update_time = NOW() " +
            "WHERE config_key IN " +
            "<foreach collection='configKeys' item='key' open='(' separator=',' close=')'>" +
            "#{key}" +
            "</foreach>" +
            "</script>")
    int batchResetToDefault(@Param("configKeys") List<String> configKeys);
}