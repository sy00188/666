package com.archive.management.service.impl;

import com.archive.management.entity.AuditLog;
import com.archive.management.mapper.AuditLogMapper;
import com.archive.management.service.AuditLogService;
import com.archive.management.exception.BusinessException;
import com.archive.management.constant.SystemConstants;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审计日志服务实现类
 * 提供审计日志管理的核心业务功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    private final AuditLogMapper auditLogMapper;
    
    @Override
    public AuditLogMapper getBaseMapper() {
        return auditLogMapper;
    }

    // ==================== 基础日志记录功能 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public AuditLog createAuditLog(AuditLog auditLog) {
        try {
            // 验证必填字段
            validateAuditLog(auditLog);
            
            // 设置默认值
            setDefaultValues(auditLog);
            
            // 保存审计日志
            boolean success = save(auditLog);
            if (!success) {
                throw new BusinessException("创建审计日志失败");
            }
            
            log.info("创建审计日志成功，ID: {}", auditLog.getId());
            return auditLog;
        } catch (Exception e) {
            log.error("创建审计日志失败", e);
            throw new BusinessException("创建审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public int batchCreateAuditLogs(List<AuditLog> auditLogs) {
        try {
            if (auditLogs == null || auditLogs.isEmpty()) {
                throw new BusinessException("审计日志列表不能为空");
            }
            
            // 验证和设置默认值
            for (AuditLog auditLog : auditLogs) {
                validateAuditLog(auditLog);
                setDefaultValues(auditLog);
            }
            
            // 批量插入
            int result = auditLogMapper.batchInsert(auditLogs);
            log.info("批量创建审计日志成功，数量: {}", result);
            return result;
        } catch (Exception e) {
            log.error("批量创建审计日志失败", e);
            throw new BusinessException("批量创建审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public AuditLog logUserAction(Long userId, String action, String resourceType, Long resourceId, 
                                 String description, String ipAddress, String userAgent, 
                                 String result, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .operationType(getOperationTypeByAction(action))
                    .module(resourceType)
                    .function(action)
                    .operationDescription(description)
                    .operationDetails(details)
                    .operationResult(getOperationResult(result))
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .relatedEntityType(resourceType)
                    .relatedEntityId(resourceId)
                    .riskLevel(calculateRiskLevel(action, result))
                    .isSensitive(isSensitiveOperation(action, resourceType))
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return createAuditLog(auditLog);
        } catch (Exception e) {
            log.error("记录用户操作日志失败", e);
            throw new BusinessException("记录用户操作日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public AuditLog logSystemAction(String action, String resourceType, Long resourceId, 
                                   String description, String result, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(0L) // 系统操作用户ID为0
                    .username("SYSTEM")
                    .operationType(getOperationTypeByAction(action))
                    .module(resourceType)
                    .function(action)
                    .operationDescription(description)
                    .operationDetails(details)
                    .operationResult(getOperationResult(result))
                    .ipAddress("127.0.0.1")
                    .userAgent("SYSTEM")
                    .relatedEntityType(resourceType)
                    .relatedEntityId(resourceId)
                    .riskLevel(calculateRiskLevel(action, result))
                    .isSensitive(isSensitiveOperation(action, resourceType))
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return createAuditLog(auditLog);
        } catch (Exception e) {
            log.error("记录系统操作日志失败", e);
            throw new BusinessException("记录系统操作日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public AuditLog logLogin(Long userId, String username, String ipAddress, String userAgent, 
                            boolean success, String failureReason) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .username(username)
                    .operationType(1) // 登录
                    .module("用户管理")
                    .function("用户登录")
                    .operationDescription(success ? "用户登录成功" : "用户登录失败")
                    .operationDetails(success ? "登录成功" : "登录失败: " + failureReason)
                    .operationResult(success ? 1 : 0)
                    .errorMessage(success ? null : failureReason)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .riskLevel(success ? 1 : 3) // 登录失败风险等级较高
                    .isSensitive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return createAuditLog(auditLog);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
            throw new BusinessException("记录登录日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public AuditLog logLogout(Long userId, String username, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .username(username)
                    .operationType(2) // 登出
                    .module("用户管理")
                    .function("用户登出")
                    .operationDescription("用户登出")
                    .operationDetails("用户正常登出系统")
                    .operationResult(1)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .riskLevel(1)
                    .isSensitive(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return createAuditLog(auditLog);
        } catch (Exception e) {
            log.error("记录登出日志失败", e);
            throw new BusinessException("记录登出日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public AuditLog logArchiveAction(Long userId, String action, Long archiveId, String archiveTitle, 
                                    String ipAddress, String userAgent, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .operationType(getOperationTypeByAction(action))
                    .module("档案管理")
                    .function("档案" + action)
                    .operationDescription("档案操作: " + action + " - " + archiveTitle)
                    .operationDetails(details)
                    .operationResult(1)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .relatedEntityType("Archive")
                    .relatedEntityId(archiveId)
                    .riskLevel(calculateArchiveRiskLevel(action))
                    .isSensitive(isSensitiveArchiveOperation(action))
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return createAuditLog(auditLog);
        } catch (Exception e) {
            log.error("记录档案操作日志失败", e);
            throw new BusinessException("记录档案操作日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public AuditLog logFileAction(Long userId, String action, Long fileId, String fileName, 
                                 String ipAddress, String userAgent, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .operationType(getOperationTypeByAction(action))
                    .module("文件管理")
                    .function("文件" + action)
                    .operationDescription("文件操作: " + action + " - " + fileName)
                    .operationDetails(details)
                    .operationResult(1)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .relatedEntityType("File")
                    .relatedEntityId(fileId)
                    .riskLevel(calculateFileRiskLevel(action))
                    .isSensitive(isSensitiveFileOperation(action))
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return createAuditLog(auditLog);
        } catch (Exception e) {
            log.error("记录文件操作日志失败", e);
            throw new BusinessException("记录文件操作日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public AuditLog logPermissionAction(Long userId, String action, String resourceType, Long resourceId, 
                                       String permission, String ipAddress, String userAgent, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .operationType(getOperationTypeByAction(action))
                    .module("权限管理")
                    .function("权限" + action)
                    .operationDescription("权限操作: " + action + " - " + permission)
                    .operationDetails(details)
                    .operationResult(1)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .relatedEntityType(resourceType)
                    .relatedEntityId(resourceId)
                    .riskLevel(4) // 权限操作风险等级较高
                    .isSensitive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return createAuditLog(auditLog);
        } catch (Exception e) {
            log.error("记录权限操作日志失败", e);
            throw new BusinessException("记录权限操作日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public AuditLog logConfigChange(Long userId, String configKey, String oldValue, String newValue, 
                                   String ipAddress, String userAgent, String reason) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .operationType(4) // 更新
                    .module("系统配置")
                    .function("配置变更")
                    .operationDescription("系统配置变更: " + configKey)
                    .operationDetails("配置项: " + configKey + ", 原值: " + oldValue + ", 新值: " + newValue + ", 原因: " + reason)
                    .operationResult(1)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .relatedEntityType("SystemConfig")
                    .relatedEntityId(0L)
                    .riskLevel(3) // 配置变更风险等级中等
                    .isSensitive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return createAuditLog(auditLog);
        } catch (Exception e) {
            log.error("记录配置变更日志失败", e);
            throw new BusinessException("记录配置变更日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public AuditLog logSecurityEvent(Long userId, String eventType, String description, String ipAddress, 
                                    String userAgent, Integer riskLevel, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .operationType(10) // 其他
                    .module("安全管理")
                    .function("安全事件")
                    .operationDescription("安全事件: " + eventType + " - " + description)
                    .operationDetails(details)
                    .operationResult(0) // 安全事件通常标记为异常
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .riskLevel(riskLevel != null ? riskLevel : 5) // 安全事件默认高风险
                    .isSensitive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            return createAuditLog(auditLog);
        } catch (Exception e) {
            log.error("记录安全事件日志失败", e);
            throw new BusinessException("记录安全事件日志失败: " + e.getMessage());
        }
    }

    // ==================== 日志查询功能 ====================

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLog:' + #id")
    public AuditLog getAuditLogById(Long id) {
        try {
            if (id == null || id <= 0) {
                throw new BusinessException("审计日志ID不能为空或小于等于0");
            }
            
            AuditLog auditLog = getById(id);
            if (auditLog == null) {
                throw new BusinessException("审计日志不存在，ID: " + id);
            }
            
            return auditLog;
        } catch (Exception e) {
            log.error("获取审计日志失败，ID: {}", id, e);
            throw new BusinessException("获取审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLogs:page:' + #page.current + ':' + #page.size")
    public IPage<AuditLog> getAuditLogsWithPagination(Page<AuditLog> page, Long userId, String operationType, 
                                                      String moduleName, Integer operationResult, Integer riskLevel, 
                                                      LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return auditLogMapper.findAuditLogsWithPagination(page, userId, operationType, moduleName, 
                    operationResult, riskLevel, startTime, endTime);
        } catch (Exception e) {
            log.error("分页查询审计日志失败", e);
            throw new BusinessException("分页查询审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLogs:user:' + #userId")
    public List<AuditLog> getAuditLogsByUserId(Long userId) {
        try {
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空或小于等于0");
            }
            
            return auditLogMapper.findByUserId(userId);
        } catch (Exception e) {
            log.error("根据用户ID查询审计日志失败，用户ID: {}", userId, e);
            throw new BusinessException("根据用户ID查询审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLogs:operationType:' + #operationType")
    public List<AuditLog> getAuditLogsByOperationType(String operationType) {
        try {
            if (!StringUtils.hasText(operationType)) {
                throw new BusinessException("操作类型不能为空");
            }
            
            return auditLogMapper.findByOperationType(operationType);
        } catch (Exception e) {
            log.error("根据操作类型查询审计日志失败，操作类型: {}", operationType, e);
            throw new BusinessException("根据操作类型查询审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLogs:module:' + #moduleName")
    public List<AuditLog> getAuditLogsByModuleName(String moduleName) {
        try {
            if (!StringUtils.hasText(moduleName)) {
                throw new BusinessException("模块名称不能为空");
            }
            
            return auditLogMapper.findByModuleName(moduleName);
        } catch (Exception e) {
            log.error("根据模块名称查询审计日志失败，模块名称: {}", moduleName, e);
            throw new BusinessException("根据模块名称查询审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLogs:result:' + #operationResult")
    public List<AuditLog> getAuditLogsByOperationResult(Integer operationResult) {
        try {
            if (operationResult == null) {
                throw new BusinessException("操作结果不能为空");
            }
            
            return auditLogMapper.findByOperationResult(operationResult);
        } catch (Exception e) {
            log.error("根据操作结果查询审计日志失败，操作结果: {}", operationResult, e);
            throw new BusinessException("根据操作结果查询审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLogs:ip:' + #ipAddress")
    public List<AuditLog> getAuditLogsByIpAddress(String ipAddress) {
        try {
            if (!StringUtils.hasText(ipAddress)) {
                throw new BusinessException("IP地址不能为空");
            }
            
            return auditLogMapper.findByIpAddress(ipAddress);
        } catch (Exception e) {
            log.error("根据IP地址查询审计日志失败，IP地址: {}", ipAddress, e);
            throw new BusinessException("根据IP地址查询审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLogs:timeRange:' + #startTime + ':' + #endTime")
    public List<AuditLog> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (startTime == null || endTime == null) {
                throw new BusinessException("开始时间和结束时间不能为空");
            }
            
            if (startTime.isAfter(endTime)) {
                throw new BusinessException("开始时间不能晚于结束时间");
            }
            
            return auditLogMapper.findByTimeRange(startTime, endTime);
        } catch (Exception e) {
            log.error("根据时间范围查询审计日志失败", e);
            throw new BusinessException("根据时间范围查询审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLogs:failed:' + #limit")
    public List<AuditLog> getFailedOperations(Integer limit) {
        try {
            int queryLimit = (limit != null && limit > 0) ? limit : 100;
            return auditLogMapper.findFailedOperations(queryLimit);
        } catch (Exception e) {
            log.error("查询失败操作日志失败", e);
            throw new BusinessException("查询失败操作日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLogs:suspicious:' + #limit")
    public List<AuditLog> getSuspiciousOperations(Integer limit) {
        try {
            int queryLimit = (limit != null && limit > 0) ? limit : 100;
            // 查询高风险等级的操作（风险等级 >= 4）
            return auditLogMapper.findAbnormalOperations(4, queryLimit);
        } catch (Exception e) {
            log.error("查询可疑操作日志失败", e);
            throw new BusinessException("查询可疑操作日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'auditLogs:highRisk:' + #limit")
    public List<AuditLog> getHighRiskOperations(Integer limit) {
        try {
            int queryLimit = (limit != null && limit > 0) ? limit : 100;
            // 查询最高风险等级的操作（风险等级 = 5）
            return auditLogMapper.findAbnormalOperations(5, queryLimit);
        } catch (Exception e) {
            log.error("查询高风险操作日志失败", e);
            throw new BusinessException("查询高风险操作日志失败: " + e.getMessage());
        }
    }

    // ==================== 统计分析功能 ====================

    @Override
    @Cacheable(value = "auditLogCache", key = "'count:total'")
    public long getTotalAuditLogCount() {
        try {
            return auditLogMapper.countAuditLogs();
        } catch (Exception e) {
            log.error("统计审计日志总数失败", e);
            throw new BusinessException("统计审计日志总数失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'count:user:' + #userId")
    public long getAuditLogCountByUserId(Long userId) {
        try {
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空或小于等于0");
            }
            
            return auditLogMapper.countOperationsByUserId(userId);
        } catch (Exception e) {
            log.error("统计用户审计日志数量失败，用户ID: {}", userId, e);
            throw new BusinessException("统计用户审计日志数量失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'count:operationType:' + #operationType")
    public long getAuditLogCountByOperationType(String operationType) {
        try {
            if (!StringUtils.hasText(operationType)) {
                throw new BusinessException("操作类型不能为空");
            }
            
            return auditLogMapper.countByOperationType(operationType);
        } catch (Exception e) {
            log.error("统计操作类型审计日志数量失败，操作类型: {}", operationType, e);
            throw new BusinessException("统计操作类型审计日志数量失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'count:module:' + #moduleName")
    public long getAuditLogCountByModuleName(String moduleName) {
        try {
            if (!StringUtils.hasText(moduleName)) {
                throw new BusinessException("模块名称不能为空");
            }
            
            return auditLogMapper.countByModuleName(moduleName);
        } catch (Exception e) {
            log.error("统计模块审计日志数量失败，模块名称: {}", moduleName, e);
            throw new BusinessException("统计模块审计日志数量失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'analysis:abnormalLogin:' + #days")
    public List<Map<String, Object>> analyzeAbnormalLogin(Integer days) {
        try {
            int queryDays = (days != null && days > 0) ? days : 7;
            LocalDateTime startTime = LocalDateTime.now().minusDays(queryDays);
            
            // 查询登录失败的日志
            QueryWrapper<AuditLog> wrapper = new QueryWrapper<>();
            wrapper.eq("operation_type", 1) // 登录操作
                    .eq("operation_result", 0) // 失败结果
                    .ge("created_at", startTime);
            
            List<AuditLog> failedLogins = list(wrapper);
            
            // 按IP地址分组统计
            Map<String, Long> ipFailureCount = failedLogins.stream()
                    .collect(Collectors.groupingBy(AuditLog::getIpAddress, Collectors.counting()));
            
            // 转换为结果格式
            return ipFailureCount.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("ipAddress", entry.getKey());
                        result.put("failureCount", entry.getValue());
                        result.put("riskLevel", entry.getValue() > 10 ? "高" : entry.getValue() > 5 ? "中" : "低");
                        return result;
                    })
                    .sorted((a, b) -> Long.compare((Long) b.get("failureCount"), (Long) a.get("failureCount")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("分析异常登录失败", e);
            throw new BusinessException("分析异常登录失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'analysis:frequentOperations:' + #userId + ':' + #hours")
    public List<Map<String, Object>> analyzeFrequentOperations(Long userId, Integer hours) {
        try {
            int queryHours = (hours != null && hours > 0) ? hours : 24;
            LocalDateTime startTime = LocalDateTime.now().minusHours(queryHours);
            
            QueryWrapper<AuditLog> wrapper = new QueryWrapper<>();
            if (userId != null && userId > 0) {
                wrapper.eq("user_id", userId);
            }
            wrapper.ge("created_at", startTime);
            
            List<AuditLog> recentLogs = list(wrapper);
            
            // 按操作类型分组统计
            Map<String, Long> operationCount = recentLogs.stream()
                    .collect(Collectors.groupingBy(log -> log.getModule() + ":" + log.getFunction(), Collectors.counting()));
            
            // 转换为结果格式
            return operationCount.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("operation", entry.getKey());
                        result.put("count", entry.getValue());
                        result.put("frequency", entry.getValue() / (double) queryHours);
                        return result;
                    })
                    .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("分析频繁操作失败", e);
            throw new BusinessException("分析频繁操作失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'analysis:abnormalAccess:' + #days")
    public List<Map<String, Object>> analyzeAbnormalAccess(Integer days) {
        try {
            int queryDays = (days != null && days > 0) ? days : 7;
            
            // 获取IP地址统计
            List<Map<String, Object>> ipStats = auditLogMapper.getIpAddressStatistics(100);
            
            // 分析异常访问模式
            return ipStats.stream()
                    .map(stat -> {
                        Map<String, Object> result = new HashMap<>(stat);
                        Long count = (Long) stat.get("count");
                        
                        // 判断异常程度
                        if (count > 1000) {
                            result.put("abnormalLevel", "高");
                            result.put("description", "访问频率异常高，可能存在攻击行为");
                        } else if (count > 500) {
                            result.put("abnormalLevel", "中");
                            result.put("description", "访问频率较高，需要关注");
                        } else {
                            result.put("abnormalLevel", "低");
                            result.put("description", "访问频率正常");
                        }
                        
                        return result;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("分析异常访问失败", e);
            throw new BusinessException("分析异常访问失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'analysis:privilegeEscalation:' + #days")
    public List<Map<String, Object>> analyzePrivilegeEscalation(Integer days) {
        try {
            int queryDays = (days != null && days > 0) ? days : 30;
            LocalDateTime startTime = LocalDateTime.now().minusDays(queryDays);
            
            // 查询权限相关操作
            QueryWrapper<AuditLog> wrapper = new QueryWrapper<>();
            wrapper.eq("module", "权限管理")
                    .ge("created_at", startTime)
                    .orderByDesc("created_at");
            
            List<AuditLog> permissionLogs = list(wrapper);
            
            // 按用户分组分析权限变更
            Map<Long, List<AuditLog>> userPermissionLogs = permissionLogs.stream()
                    .collect(Collectors.groupingBy(AuditLog::getUserId));
            
            return userPermissionLogs.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("userId", entry.getKey());
                        result.put("permissionChanges", entry.getValue().size());
                        result.put("riskLevel", entry.getValue().size() > 10 ? "高" : entry.getValue().size() > 5 ? "中" : "低");
                        result.put("details", entry.getValue().stream()
                                .map(AuditLog::getOperationDescription)
                                .collect(Collectors.toList()));
                        return result;
                    })
                    .sorted((a, b) -> Integer.compare((Integer) b.get("permissionChanges"), (Integer) a.get("permissionChanges")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("分析权限提升失败", e);
            throw new BusinessException("分析权限提升失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'analysis:dataLeakage:' + #days")
    public List<Map<String, Object>> analyzeDataLeakageRisk(Integer days) {
        try {
            int queryDays = (days != null && days > 0) ? days : 30;
            LocalDateTime startTime = LocalDateTime.now().minusDays(queryDays);
            
            // 查询敏感操作
            QueryWrapper<AuditLog> wrapper = new QueryWrapper<>();
            wrapper.eq("is_sensitive", true)
                    .ge("created_at", startTime)
                    .orderByDesc("created_at");
            
            List<AuditLog> sensitiveLogs = list(wrapper);
            
            // 按用户和操作类型分析
            Map<String, Long> riskOperations = sensitiveLogs.stream()
                    .collect(Collectors.groupingBy(log -> log.getUserId() + ":" + log.getFunction(), Collectors.counting()));
            
            return riskOperations.entrySet().stream()
                    .map(entry -> {
                        String[] parts = entry.getKey().split(":");
                        Map<String, Object> result = new HashMap<>();
                        result.put("userId", Long.parseLong(parts[0]));
                        result.put("operation", parts[1]);
                        result.put("count", entry.getValue());
                        result.put("riskLevel", entry.getValue() > 50 ? "高" : entry.getValue() > 20 ? "中" : "低");
                        return result;
                    })
                    .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("分析数据泄露风险失败", e);
            throw new BusinessException("分析数据泄露风险失败: " + e.getMessage());
        }
    }

    // ==================== 报告生成功能 ====================

    @Override
    @Cacheable(value = "auditLogCache", key = "'report:security:' + #startTime + ':' + #endTime")
    public Map<String, Object> generateSecurityReport(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (startTime == null || endTime == null) {
                throw new BusinessException("开始时间和结束时间不能为空");
            }
            
            Map<String, Object> report = new HashMap<>();
            
            // 基础统计
            long totalLogs = auditLogMapper.countByTimeRange(startTime, endTime);
            long failedOperations = auditLogMapper.findByTimeRange(startTime, endTime).stream()
                    .mapToLong(log -> log.getOperationResult() == 0 ? 1 : 0)
                    .sum();
            
            report.put("reportType", "安全报告");
            report.put("reportPeriod", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + 
                      " 至 " + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            report.put("totalLogs", totalLogs);
            report.put("failedOperations", failedOperations);
            report.put("successRate", totalLogs > 0 ? (double)(totalLogs - failedOperations) / totalLogs * 100 : 0);
            
            // 风险分析
            report.put("abnormalLogin", analyzeAbnormalLogin(7));
            report.put("highRiskOperations", getHighRiskOperations(20));
            report.put("privilegeEscalation", analyzePrivilegeEscalation(30));
            report.put("dataLeakageRisk", analyzeDataLeakageRisk(30));
            
            // 统计信息
            report.put("operationTypeStats", auditLogMapper.getOperationTypeStatistics());
            report.put("moduleStats", auditLogMapper.getModuleStatistics());
            report.put("ipStats", auditLogMapper.getIpAddressStatistics(10));
            
            report.put("generatedAt", LocalDateTime.now());
            
            return report;
        } catch (Exception e) {
            log.error("生成安全报告失败", e);
            throw new BusinessException("生成安全报告失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'report:compliance:' + #startTime + ':' + #endTime")
    public Map<String, Object> generateComplianceReport(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (startTime == null || endTime == null) {
                throw new BusinessException("开始时间和结束时间不能为空");
            }
            
            Map<String, Object> report = new HashMap<>();
            
            // 合规性检查
            List<AuditLog> logs = auditLogMapper.findByTimeRange(startTime, endTime);
            
            report.put("reportType", "合规报告");
            report.put("reportPeriod", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + 
                      " 至 " + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 操作完整性检查
            long totalOperations = logs.size();
            long loggedOperations = logs.stream()
                    .mapToLong(log -> StringUtils.hasText(log.getOperationDescription()) ? 1 : 0)
                    .sum();
            
            report.put("totalOperations", totalOperations);
            report.put("loggedOperations", loggedOperations);
            report.put("loggingCompleteness", totalOperations > 0 ? (double)loggedOperations / totalOperations * 100 : 0);
            
            // 敏感操作审计
            long sensitiveOperations = logs.stream()
                    .mapToLong(log -> Boolean.TRUE.equals(log.getIsSensitive()) ? 1 : 0)
                    .sum();
            
            report.put("sensitiveOperations", sensitiveOperations);
            report.put("sensitiveOperationRate", totalOperations > 0 ? (double)sensitiveOperations / totalOperations * 100 : 0);
            
            // 用户操作分布
            Map<Long, Long> userOperationCount = logs.stream()
                    .collect(Collectors.groupingBy(AuditLog::getUserId, Collectors.counting()));
            
            report.put("userOperationDistribution", userOperationCount);
            report.put("activeUsers", userOperationCount.size());
            
            // 时间分布分析
            report.put("dailyStats", auditLogMapper.getDailyOperationStatistics(30));
            report.put("hourlyStats", auditLogMapper.getHourlyOperationStatistics(24));
            
            report.put("generatedAt", LocalDateTime.now());
            
            return report;
        } catch (Exception e) {
            log.error("生成合规报告失败", e);
            throw new BusinessException("生成合规报告失败: " + e.getMessage());
        }
    }

    // ==================== 日志管理功能 ====================

    @Override
    public List<Map<String, Object>> exportAuditLogs(LocalDateTime startTime, LocalDateTime endTime, 
                                                     List<String> fields, String format) {
        try {
            if (startTime == null || endTime == null) {
                throw new BusinessException("开始时间和结束时间不能为空");
            }
            
            List<AuditLog> logs = auditLogMapper.findByTimeRange(startTime, endTime);
            
            return logs.stream()
                    .map(log -> {
                        Map<String, Object> exportData = new HashMap<>();
                        
                        if (fields == null || fields.isEmpty() || fields.contains("id")) {
                            exportData.put("id", log.getId());
                        }
                        if (fields == null || fields.isEmpty() || fields.contains("userId")) {
                            exportData.put("userId", log.getUserId());
                        }
                        if (fields == null || fields.isEmpty() || fields.contains("username")) {
                            exportData.put("username", log.getUsername());
                        }
                        if (fields == null || fields.isEmpty() || fields.contains("operationType")) {
                            exportData.put("operationType", log.getOperationType());
                        }
                        if (fields == null || fields.isEmpty() || fields.contains("module")) {
                            exportData.put("module", log.getModule());
                        }
                        if (fields == null || fields.isEmpty() || fields.contains("function")) {
                            exportData.put("function", log.getFunction());
                        }
                        if (fields == null || fields.isEmpty() || fields.contains("operationDescription")) {
                            exportData.put("operationDescription", log.getOperationDescription());
                        }
                        if (fields == null || fields.isEmpty() || fields.contains("operationResult")) {
                            exportData.put("operationResult", log.getOperationResult());
                        }
                        if (fields == null || fields.isEmpty() || fields.contains("ipAddress")) {
                            exportData.put("ipAddress", log.getIpAddress());
                        }
                        if (fields == null || fields.isEmpty() || fields.contains("createdAt")) {
                            exportData.put("createdAt", log.getCreatedAt());
                        }
                        
                        return exportData;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("导出审计日志失败", e);
            throw new BusinessException("导出审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public int archiveAuditLogs(LocalDateTime beforeTime, String archivePath) {
        try {
            if (beforeTime == null) {
                throw new BusinessException("归档时间不能为空");
            }
            
            // 查询需要归档的日志
            QueryWrapper<AuditLog> wrapper = new QueryWrapper<>();
            wrapper.lt("created_at", beforeTime);
            
            List<AuditLog> logsToArchive = list(wrapper);
            
            if (logsToArchive.isEmpty()) {
                log.info("没有需要归档的审计日志");
                return 0;
            }
            
            // 这里可以实现具体的归档逻辑，比如导出到文件或备份数据库
            log.info("开始归档审计日志，数量: {}, 归档路径: {}", logsToArchive.size(), archivePath);
            
            // 删除已归档的日志
            int deletedCount = Math.toIntExact(remove(wrapper));
            
            log.info("审计日志归档完成，归档数量: {}", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("归档审计日志失败", e);
            throw new BusinessException("归档审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "auditLogCache", allEntries = true)
    public int cleanExpiredAuditLogs(Integer retentionDays) {
        try {
            int days = (retentionDays != null && retentionDays > 0) ? retentionDays : 90; // 默认保留90天
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(days);
            
            int deletedCount = auditLogMapper.cleanExpiredLogs(expiredTime);
            
            log.info("清理过期审计日志完成，清理数量: {}, 保留天数: {}", deletedCount, days);
            return deletedCount;
        } catch (Exception e) {
            log.error("清理过期审计日志失败", e);
            throw new BusinessException("清理过期审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int compressAuditLogs(LocalDateTime beforeTime) {
        try {
            if (beforeTime == null) {
                throw new BusinessException("压缩时间不能为空");
            }
            
            // 查询需要压缩的日志
            QueryWrapper<AuditLog> wrapper = new QueryWrapper<>();
            wrapper.lt("created_at", beforeTime);
            
            List<AuditLog> logsToCompress = list(wrapper);
            
            if (logsToCompress.isEmpty()) {
                log.info("没有需要压缩的审计日志");
                return 0;
            }
            
            // 这里可以实现具体的压缩逻辑
            // 比如将详细信息字段压缩或移除非关键信息
            int compressedCount = 0;
            for (AuditLog auditLog : logsToCompress) {
                // 压缩操作详情，只保留关键信息
                if (StringUtils.hasText(auditLog.getOperationDetails()) && auditLog.getOperationDetails().length() > 500) {
                    auditLog.setOperationDetails(auditLog.getOperationDetails().substring(0, 500) + "...[已压缩]");
                    updateById(auditLog);
                    compressedCount++;
                }
            }
            
            log.info("审计日志压缩完成，压缩数量: {}", compressedCount);
            return compressedCount;
        } catch (Exception e) {
            log.error("压缩审计日志失败", e);
            throw new BusinessException("压缩审计日志失败: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyAuditLogIntegrity(Long logId) {
        try {
            if (logId == null || logId <= 0) {
                throw new BusinessException("日志ID不能为空或小于等于0");
            }
            
            AuditLog auditLog = getById(logId);
            if (auditLog == null) {
                return false;
            }
            
            // 验证必要字段完整性
            if (auditLog.getUserId() == null || 
                auditLog.getOperationType() == null || 
                !StringUtils.hasText(auditLog.getModule()) || 
                !StringUtils.hasText(auditLog.getFunction()) || 
                auditLog.getCreatedAt() == null) {
                return false;
            }
            
            // 验证数据合理性
            if (auditLog.getOperationType() < 1 || auditLog.getOperationType() > 10) {
                return false;
            }
            
            if (auditLog.getOperationResult() != null && 
                auditLog.getOperationResult() != 0 && auditLog.getOperationResult() != 1) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("验证审计日志完整性失败，日志ID: {}", logId, e);
            return false;
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'search:' + #keyword + ':' + #limit")
    public List<AuditLog> searchAuditLogs(String keyword, Integer limit) {
        try {
            if (!StringUtils.hasText(keyword)) {
                throw new BusinessException("搜索关键词不能为空");
            }
            
            int queryLimit = (limit != null && limit > 0) ? limit : 100;
            return auditLogMapper.searchByKeyword(keyword, queryLimit);
        } catch (Exception e) {
            log.error("搜索审计日志失败，关键词: {}", keyword, e);
            throw new BusinessException("搜索审计日志失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "auditLogCache", key = "'userTrail:' + #userId + ':' + #days")
    public List<AuditLog> getUserOperationTrail(Long userId, Integer days) {
        try {
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空或小于等于0");
            }
            
            int queryDays = (days != null && days > 0) ? days : 30;
            LocalDateTime startTime = LocalDateTime.now().minusDays(queryDays);
            
            QueryWrapper<AuditLog> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                    .ge("created_at", startTime)
                    .orderByDesc("created_at");
            
            return list(wrapper);
        } catch (Exception e) {
            log.error("获取用户操作轨迹失败，用户ID: {}", userId, e);
            throw new BusinessException("获取用户操作轨迹失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证审计日志必填字段
     */
    private void validateAuditLog(AuditLog auditLog) {
        if (auditLog == null) {
            throw new BusinessException("审计日志对象不能为空");
        }
        
        if (auditLog.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        if (auditLog.getOperationType() == null) {
            throw new BusinessException("操作类型不能为空");
        }
        
        if (!StringUtils.hasText(auditLog.getModule())) {
            throw new BusinessException("操作模块不能为空");
        }
        
        if (!StringUtils.hasText(auditLog.getFunction())) {
            throw new BusinessException("操作功能不能为空");
        }
    }

    /**
     * 设置审计日志默认值
     */
    private void setDefaultValues(AuditLog auditLog) {
        if (auditLog.getCreatedAt() == null) {
            auditLog.setCreatedAt(LocalDateTime.now());
        }
        
        if (auditLog.getOperationResult() == null) {
            auditLog.setOperationResult(1); // 默认成功
        }
        
        if (auditLog.getRiskLevel() == null) {
            auditLog.setRiskLevel(1); // 默认低风险
        }
        
        if (auditLog.getIsSensitive() == null) {
            auditLog.setIsSensitive(false); // 默认非敏感
        }
        
        if (!StringUtils.hasText(auditLog.getIpAddress())) {
            auditLog.setIpAddress("unknown");
        }
        
        if (!StringUtils.hasText(auditLog.getUserAgent())) {
            auditLog.setUserAgent("unknown");
        }
    }

    /**
     * 根据操作名称获取操作类型
     */
    private Integer getOperationTypeByAction(String action) {
        if (!StringUtils.hasText(action)) {
            return 10; // 其他
        }
        
        String lowerAction = action.toLowerCase();
        if (lowerAction.contains("login") || lowerAction.contains("登录")) {
            return 1;
        } else if (lowerAction.contains("logout") || lowerAction.contains("登出")) {
            return 2;
        } else if (lowerAction.contains("create") || lowerAction.contains("add") || lowerAction.contains("创建") || lowerAction.contains("新增")) {
            return 3;
        } else if (lowerAction.contains("update") || lowerAction.contains("edit") || lowerAction.contains("modify") || lowerAction.contains("更新") || lowerAction.contains("修改")) {
            return 4;
        } else if (lowerAction.contains("delete") || lowerAction.contains("remove") || lowerAction.contains("删除")) {
            return 5;
        } else if (lowerAction.contains("query") || lowerAction.contains("search") || lowerAction.contains("view") || lowerAction.contains("查询") || lowerAction.contains("查看")) {
            return 6;
        } else if (lowerAction.contains("import") || lowerAction.contains("导入")) {
            return 7;
        } else if (lowerAction.contains("export") || lowerAction.contains("download") || lowerAction.contains("导出") || lowerAction.contains("下载")) {
            return 8;
        } else if (lowerAction.contains("approve") || lowerAction.contains("审批")) {
            return 9;
        } else {
            return 10; // 其他
        }
    }

    /**
     * 根据结果字符串获取操作结果
     */
    private Integer getOperationResult(String result) {
        if (!StringUtils.hasText(result)) {
            return 1; // 默认成功
        }
        
        String lowerResult = result.toLowerCase();
        if (lowerResult.contains("success") || lowerResult.contains("成功") || lowerResult.equals("1")) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 计算操作风险等级
     */
    private Integer calculateRiskLevel(String action, String result) {
        int riskLevel = 1; // 默认低风险
        
        // 根据操作类型调整风险等级
        if (StringUtils.hasText(action)) {
            String lowerAction = action.toLowerCase();
            if (lowerAction.contains("delete") || lowerAction.contains("删除")) {
                riskLevel = 3;
            } else if (lowerAction.contains("permission") || lowerAction.contains("权限")) {
                riskLevel = 4;
            } else if (lowerAction.contains("config") || lowerAction.contains("配置")) {
                riskLevel = 3;
            }
        }
        
        // 根据操作结果调整风险等级
        if (StringUtils.hasText(result)) {
            String lowerResult = result.toLowerCase();
            if (lowerResult.contains("fail") || lowerResult.contains("error") || lowerResult.contains("失败") || lowerResult.contains("错误")) {
                riskLevel = Math.min(riskLevel + 2, 5);
            }
        }
        
        return riskLevel;
    }

    /**
     * 判断是否为敏感操作
     */
    private Boolean isSensitiveOperation(String action, String resourceType) {
        if (!StringUtils.hasText(action) || !StringUtils.hasText(resourceType)) {
            return false;
        }
        
        String lowerAction = action.toLowerCase();
        String lowerResourceType = resourceType.toLowerCase();
        
        // 敏感操作类型
        if (lowerAction.contains("delete") || lowerAction.contains("permission") || 
            lowerAction.contains("config") || lowerAction.contains("export") ||
            lowerAction.contains("删除") || lowerAction.contains("权限") || 
            lowerAction.contains("配置") || lowerAction.contains("导出")) {
            return true;
        }
        
        // 敏感资源类型
        if (lowerResourceType.contains("user") || lowerResourceType.contains("permission") || 
            lowerResourceType.contains("config") || lowerResourceType.contains("system") ||
            lowerResourceType.contains("用户") || lowerResourceType.contains("权限") || 
            lowerResourceType.contains("配置") || lowerResourceType.contains("系统")) {
            return true;
        }
        
        return false;
    }

    /**
     * 计算档案操作风险等级
     */
    private Integer calculateArchiveRiskLevel(String action) {
        if (!StringUtils.hasText(action)) {
            return 1;
        }
        
        String lowerAction = action.toLowerCase();
        if (lowerAction.contains("delete") || lowerAction.contains("删除")) {
            return 4;
        } else if (lowerAction.contains("export") || lowerAction.contains("download") || 
                   lowerAction.contains("导出") || lowerAction.contains("下载")) {
            return 3;
        } else if (lowerAction.contains("update") || lowerAction.contains("modify") || 
                   lowerAction.contains("更新") || lowerAction.contains("修改")) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * 判断是否为敏感档案操作
     */
    private Boolean isSensitiveArchiveOperation(String action) {
        if (!StringUtils.hasText(action)) {
            return false;
        }
        
        String lowerAction = action.toLowerCase();
        return lowerAction.contains("delete") || lowerAction.contains("export") || 
               lowerAction.contains("download") || lowerAction.contains("删除") || 
               lowerAction.contains("导出") || lowerAction.contains("下载");
    }

    /**
     * 计算文件操作风险等级
     */
    private Integer calculateFileRiskLevel(String action) {
        if (!StringUtils.hasText(action)) {
            return 1;
        }
        
        String lowerAction = action.toLowerCase();
        if (lowerAction.contains("delete") || lowerAction.contains("删除")) {
            return 4;
        } else if (lowerAction.contains("download") || lowerAction.contains("下载")) {
            return 3;
        } else if (lowerAction.contains("upload") || lowerAction.contains("上传")) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * 判断是否为敏感文件操作
     */
    private Boolean isSensitiveFileOperation(String action) {
        if (!StringUtils.hasText(action)) {
            return false;
        }
        
        String lowerAction = action.toLowerCase();
        return lowerAction.contains("delete") || lowerAction.contains("download") || 
               lowerAction.contains("删除") || lowerAction.contains("下载");
    }
}