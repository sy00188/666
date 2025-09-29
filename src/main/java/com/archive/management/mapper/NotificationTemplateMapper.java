package com.archive.management.mapper;

import com.archive.management.entity.NotificationTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 通知模板数据访问层接口
 * 提供通知模板的CRUD操作和版本管理功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplate> {

    /**
     * 根据模板编码获取当前版本的模板
     * 
     * @param templateCode 模板编码
     * @return 当前版本的模板
     */
    @Select("SELECT * FROM sys_notification_template " +
            "WHERE template_code = #{templateCode} AND is_current = 1 AND deleted = 0 " +
            "ORDER BY template_version DESC LIMIT 1")
    NotificationTemplate selectCurrentByCode(@Param("templateCode") String templateCode);

    /**
     * 根据模板编码获取所有版本的模板
     * 
     * @param templateCode 模板编码
     * @return 所有版本的模板列表
     */
    @Select("SELECT * FROM sys_notification_template " +
            "WHERE template_code = #{templateCode} AND deleted = 0 " +
            "ORDER BY template_version DESC")
    List<NotificationTemplate> selectAllVersionsByCode(@Param("templateCode") String templateCode);

    /**
     * 根据模板编码和版本号获取特定版本的模板
     * 
     * @param templateCode 模板编码
     * @param templateVersion 版本号
     * @return 特定版本的模板
     */
    @Select("SELECT * FROM sys_notification_template " +
            "WHERE template_code = #{templateCode} AND template_version = #{templateVersion} AND deleted = 0")
    NotificationTemplate selectByCodeAndVersion(@Param("templateCode") String templateCode, 
                                               @Param("templateVersion") Integer templateVersion);

    /**
     * 根据模板类型获取所有当前版本的模板
     * 
     * @param templateType 模板类型
     * @return 当前版本的模板列表
     */
    @Select("SELECT * FROM sys_notification_template " +
            "WHERE template_type = #{templateType} AND is_current = 1 AND deleted = 0 " +
            "ORDER BY create_time DESC")
    List<NotificationTemplate> selectCurrentByType(@Param("templateType") String templateType);

    /**
     * 获取模板编码的最大版本号
     * 
     * @param templateCode 模板编码
     * @return 最大版本号
     */
    @Select("SELECT COALESCE(MAX(template_version), 0) FROM sys_notification_template " +
            "WHERE template_code = #{templateCode} AND deleted = 0")
    Integer selectMaxVersionByCode(@Param("templateCode") String templateCode);

    /**
     * 将指定模板编码的所有版本设置为非当前版本
     * 
     * @param templateCode 模板编码
     * @return 更新的记录数
     */
    @Update("UPDATE sys_notification_template SET is_current = 0, update_time = NOW() " +
            "WHERE template_code = #{templateCode} AND deleted = 0")
    int updateAllVersionsToNonCurrent(@Param("templateCode") String templateCode);

    /**
     * 设置指定版本为当前版本
     * 
     * @param templateCode 模板编码
     * @param templateVersion 版本号
     * @return 更新的记录数
     */
    @Update("UPDATE sys_notification_template SET is_current = 1, update_time = NOW() " +
            "WHERE template_code = #{templateCode} AND template_version = #{templateVersion} AND deleted = 0")
    int updateVersionToCurrent(@Param("templateCode") String templateCode, 
                              @Param("templateVersion") Integer templateVersion);

    /**
     * 根据状态获取所有当前版本的模板
     * 
     * @param status 模板状态
     * @return 当前版本的模板列表
     */
    @Select("SELECT * FROM sys_notification_template " +
            "WHERE status = #{status} AND is_current = 1 AND deleted = 0 " +
            "ORDER BY create_time DESC")
    List<NotificationTemplate> selectCurrentByStatus(@Param("status") String status);

    /**
     * 检查模板编码是否已存在
     * 
     * @param templateCode 模板编码
     * @return 存在的记录数
     */
    @Select("SELECT COUNT(*) FROM sys_notification_template " +
            "WHERE template_code = #{templateCode} AND deleted = 0")
    int countByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 获取所有启用状态的当前版本模板
     * 
     * @return 启用的模板列表
     */
    @Select("SELECT * FROM sys_notification_template " +
            "WHERE status = 'ACTIVE' AND is_current = 1 AND deleted = 0 " +
            "ORDER BY template_type, create_time DESC")
    List<NotificationTemplate> selectAllActiveCurrentTemplates();
}