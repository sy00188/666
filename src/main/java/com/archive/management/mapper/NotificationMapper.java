package com.archive.management.mapper;

import com.archive.management.entity.Notification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知数据访问层
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询用户通知
     * 
     * @param page 分页参数
     * @param userId 用户ID
     * @param notificationType 通知类型（可选）
     * @param status 通知状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 通知分页数据
     */
    @Select("<script>" +
            "SELECT * FROM arc_notification " +
            "WHERE deleted = 0 " +
            "AND (user_id = #{userId} OR user_id IS NULL) " +
            "<if test='notificationType != null'>" +
            "AND notification_type = #{notificationType} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND create_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND create_time <= #{endTime} " +
            "</if>" +
            "<if test='expireTime != null'>" +
            "AND (expire_time IS NULL OR expire_time > #{expireTime}) " +
            "</if>" +
            "ORDER BY priority DESC, create_time DESC" +
            "</script>")
    IPage<Notification> selectUserNotifications(
            Page<Notification> page,
            @Param("userId") Long userId,
            @Param("notificationType") Integer notificationType,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("expireTime") LocalDateTime expireTime
    );

    /**
     * 查询用户未读通知数量
     * 
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Select("SELECT COUNT(*) FROM arc_notification " +
            "WHERE deleted = 0 " +
            "AND (user_id = #{userId} OR user_id IS NULL) " +
            "AND status = 0 " +
            "AND (expire_time IS NULL OR expire_time > NOW())")
    Long countUnreadNotifications(@Param("userId") Long userId);

    /**
     * 批量标记通知为已读
     * 
     * @param userId 用户ID
     * @param notificationIds 通知ID列表
     * @param readTime 阅读时间
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE arc_notification SET " +
            "status = 1, " +
            "read_time = #{readTime}, " +
            "update_time = NOW() " +
            "WHERE deleted = 0 " +
            "AND (user_id = #{userId} OR user_id IS NULL) " +
            "AND notification_id IN " +
            "<foreach collection='notificationIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchMarkAsRead(
            @Param("userId") Long userId,
            @Param("notificationIds") List<Long> notificationIds,
            @Param("readTime") LocalDateTime readTime
    );

    /**
     * 标记用户所有通知为已读
     * 
     * @param userId 用户ID
     * @param readTime 阅读时间
     * @return 更新数量
     */
    @Update("UPDATE arc_notification SET " +
            "status = 1, " +
            "read_time = #{readTime}, " +
            "update_time = NOW() " +
            "WHERE deleted = 0 " +
            "AND (user_id = #{userId} OR user_id IS NULL) " +
            "AND status = 0")
    int markAllAsRead(@Param("userId") Long userId, @Param("readTime") LocalDateTime readTime);

    /**
     * 查询过期的通知
     * 
     * @param currentTime 当前时间
     * @param limit 限制数量
     * @return 过期通知列表
     */
    @Select("SELECT * FROM arc_notification " +
            "WHERE deleted = 0 " +
            "AND expire_time IS NOT NULL " +
            "AND expire_time <= #{currentTime} " +
            "ORDER BY expire_time ASC " +
            "LIMIT #{limit}")
    List<Notification> selectExpiredNotifications(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("limit") Integer limit
    );

    /**
     * 删除过期的通知（逻辑删除）
     * 
     * @param currentTime 当前时间
     * @return 删除数量
     */
    @Update("UPDATE arc_notification SET " +
            "deleted = 1, " +
            "update_time = NOW() " +
            "WHERE deleted = 0 " +
            "AND expire_time IS NOT NULL " +
            "AND expire_time <= #{currentTime}")
    int deleteExpiredNotifications(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询需要发送邮件的通知
     * 
     * @param limit 限制数量
     * @return 通知列表
     */
    @Select("SELECT * FROM arc_notification " +
            "WHERE deleted = 0 " +
            "AND email_notify = 1 " +
            "AND status = 0 " +
            "AND (expire_time IS NULL OR expire_time > NOW()) " +
            "ORDER BY priority DESC, create_time ASC " +
            "LIMIT #{limit}")
    List<Notification> selectEmailNotifications(@Param("limit") Integer limit);

    /**
     * 查询需要发送短信的通知
     * 
     * @param limit 限制数量
     * @return 通知列表
     */
    @Select("SELECT * FROM arc_notification " +
            "WHERE deleted = 0 " +
            "AND sms_notify = 1 " +
            "AND status = 0 " +
            "AND (expire_time IS NULL OR expire_time > NOW()) " +
            "ORDER BY priority DESC, create_time ASC " +
            "LIMIT #{limit}")
    List<Notification> selectSmsNotifications(@Param("limit") Integer limit);

    /**
     * 查询需要WebSocket推送的通知
     * 
     * @param limit 限制数量
     * @return 通知列表
     */
    @Select("SELECT * FROM arc_notification " +
            "WHERE deleted = 0 " +
            "AND websocket_notify = 1 " +
            "AND status = 0 " +
            "AND (expire_time IS NULL OR expire_time > NOW()) " +
            "ORDER BY priority DESC, create_time ASC " +
            "LIMIT #{limit}")
    List<Notification> selectWebsocketNotifications(@Param("limit") Integer limit);

    /**
     * 根据业务ID和类型查询通知
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 通知列表
     */
    @Select("SELECT * FROM arc_notification " +
            "WHERE deleted = 0 " +
            "AND business_id = #{businessId} " +
            "AND business_type = #{businessType} " +
            "ORDER BY create_time DESC")
    List<Notification> selectByBusiness(
            @Param("businessId") String businessId,
            @Param("businessType") String businessType
    );

    /**
     * 统计各类型通知数量
     * 
     * @param userId 用户ID
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计结果
     */
    @Select("<script>" +
            "SELECT " +
            "notification_type, " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as unread_count " +
            "FROM arc_notification " +
            "WHERE deleted = 0 " +
            "AND (user_id = #{userId} OR user_id IS NULL) " +
            "<if test='startTime != null'>" +
            "AND create_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND create_time <= #{endTime} " +
            "</if>" +
            "GROUP BY notification_type " +
            "ORDER BY notification_type" +
            "</script>")
    List<java.util.Map<String, Object>> countByType(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 清理指定天数前的已读通知
     * 
     * @param days 天数
     * @return 清理数量
     */
    @Update("UPDATE arc_notification SET " +
            "deleted = 1, " +
            "update_time = NOW() " +
            "WHERE deleted = 0 " +
            "AND status = 1 " +
            "AND read_time IS NOT NULL " +
            "AND read_time <= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int cleanupReadNotifications(@Param("days") Integer days);
}