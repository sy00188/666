package com.archive.management.service.impl;

import com.archive.management.dto.*;
import com.archive.management.entity.LoginLog;
import com.archive.management.mapper.LoginLogMapper;
import com.archive.management.service.LoginLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录日志服务实现类
 * 提供用户登录日志记录和查询相关的操作
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional
public class LoginLogServiceImpl implements LoginLogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Override
    @Transactional
    public LoginLogResponse recordLoginLog(CreateLoginLogRequest request) {
        log.info("记录登录日志: request={}", request);
        
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(request.getUserId());
        loginLog.setUsername(request.getUsername());
        loginLog.setClientIp(request.getClientIp());
        loginLog.setUserAgent(request.getUserAgent());
        loginLog.setLoginType(request.getLoginType());
        loginLog.setStatus(request.getStatus());
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setErrorMessage(request.getErrorMessage());
        
        loginLogMapper.insert(loginLog);
        
        LoginLogResponse response = new LoginLogResponse();
        response.setId(loginLog.getId());
        response.setUserId(loginLog.getUserId());
        response.setUsername(loginLog.getUsername());
        response.setClientIp(loginLog.getClientIp());
        response.setLoginTime(loginLog.getLoginTime());
        response.setStatus(loginLog.getStatus());
        
        return response;
    }

    @Override
    public LoginLog getLoginLogById(Long id) {
        log.info("根据ID获取登录日志: id={}", id);
        return loginLogMapper.selectById(id);
    }

    @Override
    public LoginLogResponse getLoginLogResponseById(Long id) {
        log.info("根据ID获取登录日志响应: id={}", id);
        LoginLog loginLog = loginLogMapper.selectById(id);
        if (loginLog == null) {
            return null;
        }
        
        LoginLogResponse response = new LoginLogResponse();
        response.setId(loginLog.getId());
        response.setUserId(loginLog.getUserId());
        response.setUsername(loginLog.getUsername());
        response.setClientIp(loginLog.getClientIp());
        response.setLoginTime(loginLog.getLoginTime());
        response.setStatus(loginLog.getStatus());
        response.setErrorMessage(loginLog.getErrorMessage());
        
        return response;
    }

    @Override
    public List<LoginLog> getLoginLogsByUserId(Long userId) {
        log.info("根据用户ID获取登录日志: userId={}", userId);
        return loginLogMapper.selectByUserId(userId);
    }

    @Override
    public List<LoginLog> getLoginLogsByUsername(String username) {
        log.info("根据用户名获取登录日志: username={}", username);
        return loginLogMapper.selectByUsername(username);
    }

    @Override
    public List<LoginLog> getLoginLogsByStatus(Integer status) {
        log.info("根据状态获取登录日志: status={}", status);
        return loginLogMapper.selectByStatus(status);
    }

    @Override
    public List<LoginLog> getLoginLogsByClientIp(String clientIp) {
        log.info("根据IP地址获取登录日志: clientIp={}", clientIp);
        return loginLogMapper.selectByClientIp(clientIp);
    }

    @Override
    public List<LoginLog> getLoginLogsByLoginType(String loginType) {
        log.info("根据登录类型获取登录日志: loginType={}", loginType);
        return loginLogMapper.selectByLoginType(loginType);
    }

    @Override
    public List<LoginLog> getLoginLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("根据时间范围获取登录日志: startTime={}, endTime={}", startTime, endTime);
        return loginLogMapper.selectByTimeRange(startTime, endTime);
    }

    @Override
    public List<LoginLog> getRecentLoginLogs(Long userId, int limit) {
        log.info("获取最近登录记录: userId={}, limit={}", userId, limit);
        return loginLogMapper.selectRecentByUserId(userId, limit);
    }

    @Override
    public LoginLog getLastLoginLog(Long userId) {
        log.info("获取最后一次登录记录: userId={}", userId);
        return loginLogMapper.selectLastByUserId(userId);
    }

    @Override
    public int getFailedLoginCount(Long userId, int hours) {
        log.info("获取失败登录次数: userId={}, hours={}", userId, hours);
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        return loginLogMapper.countFailedLogins(userId, startTime);
    }

    @Override
    public IPage<LoginLog> findLoginLogsWithPagination(Page<LoginLog> page, Long userId, String username, 
                                                      Integer status, String clientIp, String loginType,
                                                      LocalDateTime startTime, LocalDateTime endTime) {
        log.info("分页查询登录日志: userId={}, username={}, status={}", userId, username, status);
        return loginLogMapper.selectPageWithConditions(page, userId, username, status, clientIp, loginType, startTime, endTime);
    }

    @Override
    @Transactional
    public boolean deleteLoginLog(Long id) {
        log.info("删除登录日志: id={}", id);
        return loginLogMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public int batchDeleteLoginLogs(List<Long> ids) {
        log.info("批量删除登录日志: ids={}", ids);
        return loginLogMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional
    public int cleanupExpiredLoginLogs(int days) {
        log.info("清理过期登录日志: days={}", days);
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        return loginLogMapper.deleteExpiredLogs(expireTime);
    }

    @Override
    public Map<String, Object> getLoginSuccessRateStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("统计登录成功率: startTime={}, endTime={}", startTime, endTime);
        return loginLogMapper.getLoginSuccessRateStatistics(startTime, endTime);
    }

    @Override
    public Map<String, Object> getUserLoginStatistics(Long userId, int days) {
        log.info("统计用户登录情况: userId={}, days={}", userId, days);
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return loginLogMapper.getUserLoginStatistics(userId, startTime);
    }

    @Override
    public Map<String, Object> getActiveUserStatistics(int days) {
        log.info("统计活跃用户数量: days={}", days);
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return loginLogMapper.getActiveUserStatistics(startTime);
    }

    @Override
    public List<Map<String, Object>> getDailyLoginStatistics(int days) {
        log.info("获取每日登录统计: days={}", days);
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return loginLogMapper.getDailyLoginStatistics(startTime);
    }

    @Override
    public List<Map<String, Object>> getLoginTypeStatistics(int days) {
        log.info("获取登录类型统计: days={}", days);
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return loginLogMapper.getLoginTypeStatistics(startTime);
    }

    @Override
    public List<Map<String, Object>> getBrowserStatistics(int days) {
        log.info("获取浏览器统计: days={}", days);
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return loginLogMapper.getBrowserStatistics(startTime);
    }

    @Override
    public List<Map<String, Object>> getOperatingSystemStatistics(int days) {
        log.info("获取操作系统统计: days={}", days);
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return loginLogMapper.getOperatingSystemStatistics(startTime);
    }

    @Override
    public List<Map<String, Object>> getLocationStatistics(int days) {
        log.info("获取地理位置统计: days={}", days);
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return loginLogMapper.getLocationStatistics(startTime);
    }

    @Override
    public List<String> identifySuspiciousIPs(int failedAttempts, int hours) {
        log.info("识别可疑IP地址: failedAttempts={}, hours={}", failedAttempts, hours);
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        return loginLogMapper.identifySuspiciousIPs(failedAttempts, startTime);
    }

    @Override
    public Map<String, Object> getLoginTrendAnalysis(int days) {
        log.info("获取登录趋势分析: days={}", days);
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return loginLogMapper.getLoginTrendAnalysis(startTime);
    }

    @Override
    public String exportLoginLogs(ExportLoginLogRequest request) {
        log.info("导出登录日志: request={}", request);
        // 这里应该实现Excel导出逻辑，暂时返回模拟路径
        String fileName = "login_logs_" + System.currentTimeMillis() + ".xlsx";
        // 实现实际的Excel导出逻辑
        try {
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("登录日志");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"用户ID", "用户名", "登录时间", "IP地址", "登录状态", "浏览器信息"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 填充数据
            int rowNum = 1;
            for (LoginLog log : logs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(log.getUserId());
                row.createCell(1).setCellValue(log.getUsername());
                row.createCell(2).setCellValue(log.getLoginTime().toString());
                row.createCell(3).setCellValue(log.getIpAddress());
                row.createCell(4).setCellValue(log.getStatus());
                row.createCell(5).setCellValue(log.getUserAgent());
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            workbook.close();
            
            log.info("登录日志Excel导出成功: {}", filePath);
            return true;
            
        } catch (Exception e) {
            log.error("登录日志Excel导出失败", e);
            return false;
        }
        return "/exports/" + fileName;
    }

    @Override
    public List<LoginLog> searchLoginLogs(String keyword, int limit) {
        log.info("搜索登录日志: keyword={}, limit={}", keyword, limit);
        return loginLogMapper.searchByKeyword(keyword, limit);
    }

    @Override
    public Map<String, Object> generateSecurityReport(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取登录安全报告: startTime={}, endTime={}", startTime, endTime);
        return loginLogMapper.generateSecurityReport(startTime, endTime);
    }

    @Override
    public boolean hasAbnormalLoginBehavior(Long userId, int hours) {
        log.info("检查异常登录行为: userId={}, hours={}", userId, hours);
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        return loginLogMapper.hasAbnormalLoginBehavior(userId, startTime);
    }

    @Override
    public List<LoginLog> getLoginFailureDetails(Long userId, int hours) {
        log.info("获取登录失败详情: userId={}, hours={}", userId, hours);
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        return loginLogMapper.getLoginFailureDetails(userId, startTime);
    }

    @Override
    public long countTotalLogins() {
        log.info("统计总登录次数");
        return loginLogMapper.countTotalLogins();
    }

    @Override
    public long countSuccessfulLogins() {
        log.info("统计成功登录次数");
        return loginLogMapper.countSuccessfulLogins();
    }

    @Override
    public long countFailedLogins() {
        log.info("统计失败登录次数");
        return loginLogMapper.countFailedLogins();
    }

    @Override
    public Map<String, Long> countLoginsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("根据时间范围统计登录次数: startTime={}, endTime={}", startTime, endTime);
        return loginLogMapper.countLoginsByTimeRange(startTime, endTime);
    }
}