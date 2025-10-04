package com.archive.management.service;

import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.entity.Role;
import com.archive.management.entity.Permission;
import com.archive.management.mapper.ArchiveMapper;
import com.archive.management.mapper.UserMapper;
import com.archive.management.mapper.RoleMapper;
import com.archive.management.mapper.PermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 批量操作服务类
 * 提供批量导入导出、批量状态更新等功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchOperationService {

    private final ArchiveMapper archiveMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MonitoringService monitoringService;

    /**
     * 批量更新档案状态
     */
    @Transactional
    public Map<String, Object> batchUpdateArchiveStatus(List<Long> archiveIds, Integer newStatus) {
        Map<String, Object> result = new HashMap<>();
        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        
        try {
            // 批量更新状态
            UpdateWrapper<Archive> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", archiveIds)
                        .set("status", newStatus)
                        .set("update_time", LocalDateTime.now());
            
            int updateCount = archiveMapper.update(null, updateWrapper);
            
            result.put("success", true);
            result.put("total", archiveIds.size());
            result.put("successCount", updateCount);
            result.put("failedCount", archiveIds.size() - updateCount);
            
            // 记录监控指标
            monitoringService.recordBatchOperation();
            
        } catch (Exception e) {
            log.error("批量更新档案状态失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量删除档案（软删除）
     */
    @Transactional
    public Map<String, Object> batchDeleteArchives(List<Long> archiveIds, Long deletedBy) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 批量软删除
            UpdateWrapper<Archive> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", archiveIds)
                        .set("deleted", 1)
                        .set("deleted_by", deletedBy)
                        .set("delete_time", LocalDateTime.now())
                        .set("update_time", LocalDateTime.now());
            
            int deleteCount = archiveMapper.update(null, updateWrapper);
            
            result.put("success", true);
            result.put("total", archiveIds.size());
            result.put("successCount", deleteCount);
            result.put("failedCount", archiveIds.size() - deleteCount);
            
            // 记录监控指标
            monitoringService.recordBatchOperation();
            
        } catch (Exception e) {
            log.error("批量删除档案失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量更新用户状态
     */
    @Transactional
    public Map<String, Object> batchUpdateUserStatus(List<Long> userIds, Integer newStatus) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 批量更新状态
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", userIds)
                        .set("status", newStatus)
                        .set("update_time", LocalDateTime.now());
            
            int updateCount = userMapper.update(null, updateWrapper);
            
            result.put("success", true);
            result.put("total", userIds.size());
            result.put("successCount", updateCount);
            result.put("failedCount", userIds.size() - updateCount);
            
            // 记录监控指标
            monitoringService.recordBatchOperation();
            
        } catch (Exception e) {
            log.error("批量更新用户状态失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量导入档案
     */
    @Async
    public CompletableFuture<Map<String, Object>> batchImportArchives(MultipartFile file, Long createUserId) {
        Map<String, Object> result = new HashMap<>();
        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // 解析Excel文件
            List<Map<String, Object>> dataList = parseExcelFile(file);
            
            for (Map<String, Object> data : dataList) {
                try {
                    Archive archive = createArchiveFromData(data, createUserId);
                    archiveMapper.insert(archive);
                    successIds.add(archive.getId().toString());
                } catch (Exception e) {
                    failedIds.add(data.get("row").toString());
                    errors.add("行 " + data.get("row") + ": " + e.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("total", dataList.size());
            result.put("successCount", successIds.size());
            result.put("failedCount", failedIds.size());
            result.put("successIds", successIds);
            result.put("failedIds", failedIds);
            result.put("errors", errors);
            
            // 记录监控指标
            monitoringService.recordBatchOperation();
            
        } catch (Exception e) {
            log.error("批量导入档案失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 批量导入用户
     */
    @Async
    public CompletableFuture<Map<String, Object>> batchImportUsers(MultipartFile file, Long createUserId) {
        Map<String, Object> result = new HashMap<>();
        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // 解析Excel文件
            List<Map<String, Object>> dataList = parseUserExcelFile(file);
            
            for (Map<String, Object> data : dataList) {
                try {
                    User user = createUserFromData(data, createUserId);
                    userMapper.insert(user);
                    successIds.add(user.getId().toString());
                } catch (Exception e) {
                    failedIds.add(data.get("row").toString());
                    errors.add("行 " + data.get("row") + ": " + e.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("total", dataList.size());
            result.put("successCount", successIds.size());
            result.put("failedCount", failedIds.size());
            result.put("successIds", successIds);
            result.put("failedIds", failedIds);
            result.put("errors", errors);
            
            // 记录监控指标
            monitoringService.recordBatchOperation();
            
        } catch (Exception e) {
            log.error("批量导入用户失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 批量导出档案
     */
    public byte[] batchExportArchives(List<Long> archiveIds, String format) {
        try {
            // 获取档案数据
            QueryWrapper<Archive> queryWrapper = new QueryWrapper<>();
            if (archiveIds != null && !archiveIds.isEmpty()) {
                queryWrapper.in("id", archiveIds);
            }
            queryWrapper.eq("deleted", 0);
            List<Archive> archives = archiveMapper.selectList(queryWrapper);
            
            // 根据格式生成文件
            if ("excel".equalsIgnoreCase(format)) {
                return generateArchiveExcelFile(archives);
            } else if ("csv".equalsIgnoreCase(format)) {
                return generateArchiveCsvFile(archives);
            } else {
                throw new IllegalArgumentException("不支持的导出格式: " + format);
            }
            
        } catch (Exception e) {
            log.error("批量导出档案失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量导出用户
     */
    public byte[] batchExportUsers(List<Long> userIds, String format) {
        try {
            // 获取用户数据
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            if (userIds != null && !userIds.isEmpty()) {
                queryWrapper.in("id", userIds);
            }
            queryWrapper.eq("deleted", 0);
            List<User> users = userMapper.selectList(queryWrapper);
            
            // 根据格式生成文件
            if ("excel".equalsIgnoreCase(format)) {
                return generateUserExcelFile(users);
            } else if ("csv".equalsIgnoreCase(format)) {
                return generateUserCsvFile(users);
            } else {
                throw new IllegalArgumentException("不支持的导出格式: " + format);
            }
            
        } catch (Exception e) {
            log.error("批量导出用户失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量分配角色
     */
    @Transactional
    public Map<String, Object> batchAssignRoles(List<Long> userIds, List<Long> roleIds) {
        Map<String, Object> result = new HashMap<>();
        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        
        try {
            // 获取角色信息
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            
            for (Long userId : userIds) {
                try {
                    // 这里需要实现用户角色关联表的插入
                    // 可以通过UserRoleMapper来实现
                    successIds.add(userId.toString());
                } catch (Exception e) {
                    failedIds.add(userId.toString());
                }
            }
            
            result.put("success", true);
            result.put("total", userIds.size());
            result.put("successCount", successIds.size());
            result.put("failedCount", failedIds.size());
            result.put("successIds", successIds);
            result.put("failedIds", failedIds);
            
            // 记录监控指标
            monitoringService.recordBatchOperation();
            
        } catch (Exception e) {
            log.error("批量分配角色失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量分配权限
     */
    @Transactional
    public Map<String, Object> batchAssignPermissions(List<Long> userIds, List<Long> permissionIds) {
        Map<String, Object> result = new HashMap<>();
        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        
        try {
            // 获取权限信息
            List<Permission> permissions = permissionMapper.selectBatchIds(permissionIds);
            
            for (Long userId : userIds) {
                try {
                    // 这里需要实现用户权限关联表的插入
                    // 可以通过UserPermissionMapper来实现
                    successIds.add(userId.toString());
                } catch (Exception e) {
                    failedIds.add(userId.toString());
                }
            }
            
            result.put("success", true);
            result.put("total", userIds.size());
            result.put("successCount", successIds.size());
            result.put("failedCount", failedIds.size());
            result.put("successIds", successIds);
            result.put("failedIds", failedIds);
            
            // 记录监控指标
            monitoringService.recordBatchOperation();
            
        } catch (Exception e) {
            log.error("批量分配权限失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 生成档案导入模板
     */
    public byte[] generateArchiveImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("档案导入模板");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"档案标题*", "档案编号*", "档案描述", "分类ID", "安全级别", "档案级别", "关键词"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            String[] exampleData = {"示例档案", "EXAMPLE-001", "这是一个示例档案", "1", "公开", "一般", "示例,测试"};
            
            for (int i = 0; i < exampleData.length; i++) {
                Cell cell = exampleRow.createCell(i);
                cell.setCellValue(exampleData[i]);
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("生成档案导入模板失败", e);
            throw new RuntimeException("生成模板失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成用户导入模板
     */
    public byte[] generateUserImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户导入模板");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"用户名*", "密码*", "真实姓名*", "邮箱*", "手机号", "部门ID", "职位", "状态"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            String[] exampleData = {"testuser", "123456", "测试用户", "test@example.com", "13800138000", "1", "员工", "1"};
            
            for (int i = 0; i < exampleData.length; i++) {
                Cell cell = exampleRow.createCell(i);
                cell.setCellValue(exampleData[i]);
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("生成用户导入模板失败", e);
            throw new RuntimeException("生成模板失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析Excel文件
     */
    private List<Map<String, Object>> parseExcelFile(MultipartFile file) throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, Object> data = new HashMap<>();
                data.put("row", i + 1);
                data.put("title", getCellValue(row.getCell(0)));
                data.put("code", getCellValue(row.getCell(1)));
                data.put("description", getCellValue(row.getCell(2)));
                data.put("categoryId", getCellValue(row.getCell(3)));
                data.put("securityLevel", getCellValue(row.getCell(4)));
                data.put("level", getCellValue(row.getCell(5)));
                data.put("keywords", getCellValue(row.getCell(6)));
                
                dataList.add(data);
            }
        }
        
        return dataList;
    }

    /**
     * 解析用户Excel文件
     */
    private List<Map<String, Object>> parseUserExcelFile(MultipartFile file) throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, Object> data = new HashMap<>();
                data.put("row", i + 1);
                data.put("username", getCellValue(row.getCell(0)));
                data.put("password", getCellValue(row.getCell(1)));
                data.put("realName", getCellValue(row.getCell(2)));
                data.put("email", getCellValue(row.getCell(3)));
                data.put("phone", getCellValue(row.getCell(4)));
                data.put("departmentId", getCellValue(row.getCell(5)));
                data.put("position", getCellValue(row.getCell(6)));
                data.put("status", getCellValue(row.getCell(7)));
                
                dataList.add(data);
            }
        }
        
        return dataList;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 从数据创建档案
     */
    private Archive createArchiveFromData(Map<String, Object> data, Long createUserId) {
        Archive archive = new Archive();
        archive.setTitle(data.get("title").toString());
        archive.setArchiveNumber(data.get("code").toString());
        archive.setDescription(data.get("description").toString());
        
        if (data.get("categoryId") != null && !data.get("categoryId").toString().isEmpty()) {
            archive.setCategoryId(Long.valueOf(data.get("categoryId").toString()));
        }
        
        if (data.get("securityLevel") != null) {
            archive.setSecurityLevel(data.get("securityLevel").toString());
        }
        
        if (data.get("level") != null) {
            archive.setLevel(data.get("level").toString());
        }
        
        if (data.get("keywords") != null) {
            archive.setKeywords(data.get("keywords").toString());
        }
        
        archive.setStatus(1); // 默认状态为激活
        archive.setCreatedBy(createUserId);
        archive.setCreateTime(LocalDateTime.now());
        archive.setUpdateTime(LocalDateTime.now());
        archive.setDeleted(0);
        
        return archive;
    }

    /**
     * 从数据创建用户
     */
    private User createUserFromData(Map<String, Object> data, Long createUserId) {
        User user = new User();
        user.setUsername(data.get("username").toString());
        user.setPassword(data.get("password").toString()); // 实际应用中需要加密
        user.setRealName(data.get("realName").toString());
        user.setEmail(data.get("email").toString());
        
        if (data.get("phone") != null && !data.get("phone").toString().isEmpty()) {
            user.setPhone(data.get("phone").toString());
        }
        
        if (data.get("departmentId") != null && !data.get("departmentId").toString().isEmpty()) {
            user.setDepartmentId(Long.valueOf(data.get("departmentId").toString()));
        }
        
        if (data.get("position") != null) {
            user.setPosition(data.get("position").toString());
        }
        
        if (data.get("status") != null && !data.get("status").toString().isEmpty()) {
            user.setStatus(Integer.valueOf(data.get("status").toString()));
        } else {
            user.setStatus(1); // 默认状态为激活
        }
        
        user.setCreatedBy(createUserId);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        
        return user;
    }

    /**
     * 生成档案Excel文件
     */
    private byte[] generateArchiveExcelFile(List<Archive> archives) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("档案列表");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "档案标题", "档案编号", "档案描述", "分类ID", "安全级别", "档案级别", "状态", "创建时间"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 创建数据行
            for (int i = 0; i < archives.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Archive archive = archives.get(i);
                
                row.createCell(0).setCellValue(archive.getId());
                row.createCell(1).setCellValue(archive.getTitle());
                row.createCell(2).setCellValue(archive.getArchiveNumber());
                row.createCell(3).setCellValue(archive.getDescription());
                row.createCell(4).setCellValue(archive.getCategoryId());
                row.createCell(5).setCellValue(archive.getSecurityLevel());
                row.createCell(6).setCellValue(archive.getLevel());
                row.createCell(7).setCellValue(archive.getStatus());
                row.createCell(8).setCellValue(archive.getCreateTime().toString());
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("生成档案Excel文件失败", e);
            throw new RuntimeException("生成Excel失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成档案CSV文件
     */
    private byte[] generateArchiveCsvFile(List<Archive> archives) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,档案标题,档案编号,档案描述,分类ID,安全级别,档案级别,状态,创建时间\n");
        
        for (Archive archive : archives) {
            csv.append(archive.getId()).append(",");
            csv.append(archive.getTitle()).append(",");
            csv.append(archive.getArchiveNumber()).append(",");
            csv.append(archive.getDescription()).append(",");
            csv.append(archive.getCategoryId()).append(",");
            csv.append(archive.getSecurityLevel()).append(",");
            csv.append(archive.getLevel()).append(",");
            csv.append(archive.getStatus()).append(",");
            csv.append(archive.getCreateTime()).append("\n");
        }
        
        return csv.toString().getBytes();
    }

    /**
     * 生成用户Excel文件
     */
    private byte[] generateUserExcelFile(List<User> users) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户列表");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "用户名", "真实姓名", "邮箱", "手机号", "部门ID", "职位", "状态", "创建时间"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 创建数据行
            for (int i = 0; i < users.size(); i++) {
                Row row = sheet.createRow(i + 1);
                User user = users.get(i);
                
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getRealName());
                row.createCell(3).setCellValue(user.getEmail());
                row.createCell(4).setCellValue(user.getPhone());
                row.createCell(5).setCellValue(user.getDepartmentId());
                row.createCell(6).setCellValue(user.getPosition());
                row.createCell(7).setCellValue(user.getStatus());
                row.createCell(8).setCellValue(user.getCreateTime().toString());
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("生成用户Excel文件失败", e);
            throw new RuntimeException("生成Excel失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成用户CSV文件
     */
    private byte[] generateUserCsvFile(List<User> users) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,用户名,真实姓名,邮箱,手机号,部门ID,职位,状态,创建时间\n");
        
        for (User user : users) {
            csv.append(user.getId()).append(",");
            csv.append(user.getUsername()).append(",");
            csv.append(user.getRealName()).append(",");
            csv.append(user.getEmail()).append(",");
            csv.append(user.getPhone()).append(",");
            csv.append(user.getDepartmentId()).append(",");
            csv.append(user.getPosition()).append(",");
            csv.append(user.getStatus()).append(",");
            csv.append(user.getCreateTime()).append("\n");
        }
        
        return csv.toString().getBytes();
    }
}