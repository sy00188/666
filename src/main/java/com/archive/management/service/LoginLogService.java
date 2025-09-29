package com.archive.management.service;

import com.archive.management.dto.*;
import com.archive.management.entity.LoginLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 登录日志业务服务接口
 * 定义登录日志管理的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface LoginLogService {

    /**
     * 记录登录日志
     * @param request 登录日志记录请求
     * @return 登录日志响应信息
     */
    LoginLogResponse recordLoginLog(CreateLoginLogRequest request);

    /**
     * 根据ID获取登录日志
     * @param id 登录日志ID
     * @return 登录日志信息
     */
    LoginLog getLoginLogById(Long id);

    /**
     * 根据ID获取登录日志响应信息
     * @param id 登录日志ID
     * @return 登录日志响应信息
     */
    LoginLogResponse getLoginLogResponseById(Long id);

    /**
     * 根据用户ID获取登录日志列表
     * @param userId 用户ID
     * @return 登录日志列表
     */
    List<LoginLog> getLoginLogsByUserId(Long userId);

    /**
     * 根据用户名获取登录日志列表
     * @param username 用户名
     * @return 登录日志列表
     */
    List<LoginLog> getLoginLogsByUsername(String username);

    /**
     * 根据登录状态获取登录日志列表
     * @param status 登录状态（0-失败，1-成功）
     * @return 登录日志列表
     */
    List<LoginLog> getLoginLogsByStatus(Integer status);

    /**
     * 根据IP地址获取登录日志列表
     * @param clientIp 客户端IP地址
     * @return 登录日志列表
     */
    List<LoginLog> getLoginLogsByClientIp(String clientIp);

    /**
     * 根据登录类型获取登录日志列表
     * @param loginType 登录类型（web、mobile、api等）
     * @return 登录日志列表
     */
    List<LoginLog> getLoginLogsByLoginType(String loginType);

    /**
     * 根据时间范围获取登录日志列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录日志列表
     */
    List<LoginLog> getLoginLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取最近的登录记录
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近登录记录列表
     */
    List<LoginLog> getRecentLoginLogs(Long userId, int limit);

    /**
     * 获取用户最后一次登录记录
     * @param userId 用户ID
     * @return 最后一次登录记录
     */
    LoginLog getLastLoginLog(Long userId);

    /**
     * 获取用户失败登录次数
     * @param userId 用户ID
     * @param hours 时间范围（小时）
     * @return 失败登录次数
     */
    int getFailedLoginCount(Long userId, int hours);

    /**
     * 分页查询登录日志
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param username 用户名（可选）
     * @param status 登录状态（可选）
     * @param clientIp 客户端IP（可选）
     * @param loginType 登录类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    IPage<LoginLog> findLoginLogsWithPagination(Page<LoginLog> page, Long userId, String username, 
                                               Integer status, String clientIp, String loginType,
                                               LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 删除登录日志（物理删除）
     * @param id 登录日志ID
     * @return 是否删除成功
     */
    boolean deleteLoginLog(Long id);

    /**
     * 批量删除登录日志（物理删除）
     * @param ids 登录日志ID列表
     * @return 删除成功的数量
     */
    int batchDeleteLoginLogs(List<Long> ids);

    /**
     * 清理过期的登录日志
     * @param days 保留天数
     * @return 清理的记录数量
     */
    int cleanupExpiredLoginLogs(int days);

    /**
     * 统计登录成功率
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录成功率统计
     */
    Map<String, Object> getLoginSuccessRateStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户登录情况
     * @param userId 用户ID
     * @param days 统计天数
     * @return 用户登录统计
     */
    Map<String, Object> getUserLoginStatistics(Long userId, int days);

    /**
     * 统计活跃用户数量
     * @param days 活跃天数
     * @return 活跃用户统计
     */
    Map<String, Object> getActiveUserStatistics(int days);

    /**
     * 获取每日登录统计
     * @param days 统计天数
     * @return 每日登录统计列表
     */
    List<Map<String, Object>> getDailyLoginStatistics(int days);

    /**
     * 获取登录类型统计
     * @param days 统计天数
     * @return 登录类型统计列表
     */
    List<Map<String, Object>> getLoginTypeStatistics(int days);

    /**
     * 获取浏览器统计
     * @param days 统计天数
     * @return 浏览器统计列表
     */
    List<Map<String, Object>> getBrowserStatistics(int days);

    /**
     * 获取操作系统统计
     * @param days 统计天数
     * @return 操作系统统计列表
     */
    List<Map<String, Object>> getOperatingSystemStatistics(int days);

    /**
     * 获取地理位置统计
     * @param days 统计天数
     * @return 地理位置统计列表
     */
    List<Map<String, Object>> getLocationStatistics(int days);

    /**
     * 识别可疑IP地址
     * @param failedAttempts 失败尝试次数阈值
     * @param hours 时间范围（小时）
     * @return 可疑IP地址列表
     */
    List<String> identifySuspiciousIPs(int failedAttempts, int hours);

    /**
     * 获取登录趋势分析
     * @param days 分析天数
     * @return 登录趋势分析结果
     */
    Map<String, Object> getLoginTrendAnalysis(int days);

    /**
     * 导出登录日志
     * @param request 导出请求参数
     * @return 导出文件路径或数据
     */
    String exportLoginLogs(ExportLoginLogRequest request);

    /**
     * 搜索登录日志
     * @param keyword 搜索关键词
     * @param limit 限制数量
     * @return 搜索结果列表
     */
    List<LoginLog> searchLoginLogs(String keyword, int limit);

    /**
     * 获取登录安全报告
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 安全报告数据
     */
    Map<String, Object> generateSecurityReport(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 检查用户是否存在异常登录行为
     * @param userId 用户ID
     * @param hours 检查时间范围（小时）
     * @return 是否存在异常行为
     */
    boolean hasAbnormalLoginBehavior(Long userId, int hours);

    /**
     * 获取登录失败详情
     * @param userId 用户ID
     * @param hours 时间范围（小时）
     * @return 登录失败详情列表
     */
    List<LoginLog> getLoginFailureDetails(Long userId, int hours);

    /**
     * 统计总登录次数
     * @return 总登录次数
     */
    long countTotalLogins();

    /**
     * 统计成功登录次数
     * @return 成功登录次数
     */
    long countSuccessfulLogins();

    /**
     * 统计失败登录次数
     * @return 失败登录次数
     */
    long countFailedLogins();

    /**
     * 根据时间范围统计登录次数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录次数统计
     */
    Map<String, Long> countLoginsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
}