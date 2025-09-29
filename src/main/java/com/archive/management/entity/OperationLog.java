package com.archive.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 对应数据库表：sys_operation_log
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 操作类型
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 操作描述
     */
    @TableField("operation_desc")
    private String operationDesc;

    /**
     * 目标类型
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标ID
     */
    @TableField("target_id")
    private String targetId;

    /**
     * 请求方法
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 响应结果
     */
    @TableField("response_result")
    private String responseResult;

    /**
     * 客户端IP
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 执行时间(毫秒)
     */
    @TableField("execution_time")
    private Integer executionTime;

    /**
     * 状态(0:失败 1:成功)
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    // ==================== 构造方法 ====================

    public OperationLog() {
        this.createTime = LocalDateTime.now();
        this.status = 1; // 默认成功
    }

    public OperationLog(String operationType, String operationDesc) {
        this();
        this.operationType = operationType;
        this.operationDesc = operationDesc;
    }

    public OperationLog(Long userId, String username, String operationType, String operationDesc) {
        this(operationType, operationDesc);
        this.userId = userId;
        this.username = username;
    }

    // ==================== 业务方法 ====================

    /**
     * 检查操作是否成功
     */
    public boolean isSuccess() {
        return this.status != null && this.status == 1;
    }

    /**
     * 检查操作是否失败
     */
    public boolean isFailure() {
        return this.status != null && this.status == 0;
    }

    /**
     * 设置操作成功
     */
    public OperationLog setSuccess() {
        this.status = 1;
        this.errorMsg = null;
        return this;
    }

    /**
     * 设置操作失败
     */
    public OperationLog setFailure(String errorMsg) {
        this.status = 0;
        this.errorMsg = errorMsg;
        return this;
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (this.status == null) {
            return "未知";
        }
        return this.status == 1 ? "成功" : "失败";
    }

    /**
     * 检查是否为查询操作
     */
    public boolean isQueryOperation() {
        return this.operationType != null && 
               (this.operationType.contains("查询") || 
                this.operationType.contains("SELECT") || 
                this.operationType.contains("GET"));
    }

    /**
     * 检查是否为修改操作
     */
    public boolean isModifyOperation() {
        return this.operationType != null && 
               (this.operationType.contains("新增") || 
                this.operationType.contains("修改") || 
                this.operationType.contains("删除") || 
                this.operationType.contains("INSERT") || 
                this.operationType.contains("UPDATE") || 
                this.operationType.contains("DELETE") ||
                this.operationType.contains("POST") ||
                this.operationType.contains("PUT") ||
                this.operationType.contains("PATCH"));
    }

    /**
     * 检查是否为敏感操作
     */
    public boolean isSensitiveOperation() {
        return this.operationType != null && 
               (this.operationType.contains("删除") || 
                this.operationType.contains("密码") || 
                this.operationType.contains("权限") || 
                this.operationType.contains("角色") ||
                this.operationType.contains("DELETE"));
    }

    /**
     * 检查执行时间是否过长
     */
    public boolean isSlowOperation() {
        return this.executionTime != null && this.executionTime > 5000; // 超过5秒
    }

    /**
     * 设置请求信息
     */
    public OperationLog setRequestInfo(String method, String url, String params) {
        this.requestMethod = method;
        this.requestUrl = url;
        this.requestParams = params;
        return this;
    }

    /**
     * 设置目标信息
     */
    public OperationLog setTargetInfo(String targetType, String targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
        return this;
    }

    /**
     * 设置客户端信息
     */
    public OperationLog setClientInfo(String clientIp, String userAgent) {
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        return this;
    }

    /**
     * 计算并设置执行时间
     */
    public OperationLog setExecutionTime(long startTime) {
        this.executionTime = (int) (System.currentTimeMillis() - startTime);
        return this;
    }

    /**
     * 创建查询日志
     */
    public static OperationLog createQueryLog(Long userId, String username, String desc, String targetType, String targetId) {
        OperationLog log = new OperationLog(userId, username, "查询", desc);
        log.setTargetInfo(targetType, targetId);
        return log;
    }

    /**
     * 创建新增日志
     */
    public static OperationLog createInsertLog(Long userId, String username, String desc, String targetType, String targetId) {
        OperationLog log = new OperationLog(userId, username, "新增", desc);
        log.setTargetInfo(targetType, targetId);
        return log;
    }

    /**
     * 创建修改日志
     */
    public static OperationLog createUpdateLog(Long userId, String username, String desc, String targetType, String targetId) {
        OperationLog log = new OperationLog(userId, username, "修改", desc);
        log.setTargetInfo(targetType, targetId);
        return log;
    }

    /**
     * 创建删除日志
     */
    public static OperationLog createDeleteLog(Long userId, String username, String desc, String targetType, String targetId) {
        OperationLog log = new OperationLog(userId, username, "删除", desc);
        log.setTargetInfo(targetType, targetId);
        return log;
    }

    /**
     * 创建登录日志
     */
    public static OperationLog createLoginLog(Long userId, String username, String clientIp) {
        OperationLog log = new OperationLog(userId, username, "登录", "用户登录系统");
        log.setClientInfo(clientIp, null);
        return log;
    }

    /**
     * 创建登出日志
     */
    public static OperationLog createLogoutLog(Long userId, String username, String clientIp) {
        OperationLog log = new OperationLog(userId, username, "登出", "用户退出系统");
        log.setClientInfo(clientIp, null);
        return log;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", operationType='" + operationType + '\'' +
                ", operationDesc='" + operationDesc + '\'' +
                ", targetType='" + targetType + '\'' +
                ", targetId='" + targetId + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}