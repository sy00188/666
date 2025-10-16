package com.archive.management.service.impl;

import com.archive.management.dto.*;
import com.archive.management.entity.Department;
import com.archive.management.entity.User;
import com.archive.management.repository.DepartmentRepository;
import com.archive.management.repository.UserRepository;
import com.archive.management.service.DepartmentService;
import com.archive.management.service.UserService;
import com.archive.management.common.PageResult;
import com.archive.management.exception.BusinessException;
import com.archive.management.common.enums.ResponseCode;
import com.archive.management.util.ValidationUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门业务服务实现类
 * 实现部门管理的核心业务逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    // ==================== 部门CRUD操作 ====================

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("创建部门: {}", request.getName());
        
        // 验证部门编码唯一性
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new BusinessException(ResponseCode.DEPARTMENT_CODE_EXISTS);
        }
        
        // 验证部门名称唯一性（同级别下）
        if (request.getParentId() != null) {
            if (departmentRepository.existsByNameAndParentId(request.getName(), request.getParentId())) {
                throw new BusinessException(ResponseCode.DEPARTMENT_CODE_EXISTS);
            }
        } else {
            if (departmentRepository.existsByNameAndParentIdIsNull(request.getName())) {
                throw new BusinessException(ResponseCode.DEPARTMENT_CODE_EXISTS);
            }
        }
        
        // 验证父部门存在性
        Department parentDepartment = null;
        if (request.getParentId() != null) {
            parentDepartment = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.DEPARTMENT_NOT_FOUND));
            
            if (!parentDepartment.isEnabled()) {
                throw new BusinessException(ResponseCode.USER_DISABLED);
            }
        }
        
        // 创建部门实体
        Department department = new Department();
        BeanUtils.copyProperties(request, department);
        
        // 设置层级和路径
        if (parentDepartment != null) {
            department.setLevel(parentDepartment.getLevel() + 1);
        } else {
            department.setLevel(1);
        }
        
        // 设置排序号
        Integer maxSortOrder;
        if (request.getParentId() != null) {
            maxSortOrder = departmentRepository.getMaxSortOrderByParent(request.getParentId());
        } else {
            maxSortOrder = departmentRepository.getMaxSortOrderForRoot();
        }
        department.setSortOrder(maxSortOrder + 1);
        
        // 设置默认值
        department.setStatus(1); // 启用状态
        department.setCreateTime(LocalDateTime.now());
        department.setUpdateTime(LocalDateTime.now());
        department.setDeleted(false);
        
        // 保存部门
        Department savedDepartment = departmentRepository.save(department);
        
        // 更新路径
        if (parentDepartment != null) {
            savedDepartment.buildPath(parentDepartment.getPath());
        } else {
            savedDepartment.buildPath(null);
        }
        departmentRepository.save(savedDepartment);
        
        log.info("部门创建成功: ID={}, 名称={}", savedDepartment.getId(), savedDepartment.getName());
        return convertToDepartmentResponse(savedDepartment);
    }

    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.DEPARTMENT_NOT_FOUND));
    }

    @Override
    public DepartmentResponse getDepartmentResponseById(Long id) {
        Department department = getDepartmentById(id);
        return convertToDepartmentResponse(department);
    }

    @Override
    public Department getDepartmentByCode(String code) {
        return departmentRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ResponseCode.DEPARTMENT_NOT_FOUND));
    }

    @Override
    public Department getDepartmentByName(String name) {
        return departmentRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ResponseCode.DEPARTMENT_NOT_FOUND));
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        log.info("更新部门: ID={}, 名称={}", id, request.getName());
        
        Department department = getDepartmentById(id);
        
        // 验证部门编码唯一性（排除自身）
        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(department.getCode())) {
            if (departmentRepository.existsByCode(request.getCode())) {
                throw new BusinessException(ResponseCode.DEPARTMENT_CODE_EXISTS);
            }
        }
        
        // 验证部门名称唯一性（同级别下，排除自身）
        if (StringUtils.hasText(request.getName()) && !request.getName().equals(department.getName())) {
            if (request.getParentId() != null) {
                if (departmentRepository.existsByNameAndParentId(request.getName(), request.getParentId())) {
                    throw new BusinessException(ResponseCode.DEPARTMENT_CODE_EXISTS);
                }
            } else {
                if (departmentRepository.existsByNameAndParentIdIsNull(request.getName())) {
                    throw new BusinessException(ResponseCode.DEPARTMENT_CODE_EXISTS);
                }
            }
        }
        
        // 更新部门信息
        if (StringUtils.hasText(request.getName())) {
            department.setName(request.getName());
        }
        if (StringUtils.hasText(request.getCode())) {
            department.setCode(request.getCode());
        }
        if (StringUtils.hasText(request.getDescription())) {
            department.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getContact())) {
            department.setContact(request.getContact());
        }
        if (StringUtils.hasText(request.getPhone())) {
            department.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            department.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getAddress())) {
            department.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getType())) {
            department.setType(request.getType());
        }
        if (StringUtils.hasText(request.getRemark())) {
            department.setRemark(request.getRemark());
        }
        if (request.getManagerId() != null) {
            department.setManagerId(request.getManagerId());
        }
        if (request.getSortOrder() != null) {
            department.setSortOrder(request.getSortOrder());
        }
        
        department.setUpdateTime(LocalDateTime.now());
        
        Department updatedDepartment = departmentRepository.save(department);
        log.info("部门更新成功: ID={}", updatedDepartment.getId());
        
        return convertToDepartmentResponse(updatedDepartment);
    }

    @Override
    @Transactional
    public boolean deleteDepartment(Long id, Long deletedBy) {
        log.info("删除部门: ID={}, 删除人={}", id, deletedBy);
        
        Department department = getDepartmentById(id);
        
        // 检查是否有子部门
        if (hasChildren(id)) {
            throw new BusinessException(ResponseCode.DEPARTMENT_HAS_CHILDREN);
        }
        
        // 检查是否有用户
        if (hasUsers(id)) {
            throw new BusinessException(ResponseCode.DEPARTMENT_HAS_USERS);
        }
        
        // 软删除
        department.softDelete(deletedBy);
        departmentRepository.save(department);
        
        log.info("部门删除成功: ID={}", id);
        return true;
    }

    @Override
    @Transactional
    public boolean restoreDepartment(Long id) {
        log.info("恢复部门: ID={}", id);
        
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.DEPARTMENT_NOT_FOUND));
        
        if (!department.getDeleted()) {
            throw new BusinessException(ResponseCode.DEPARTMENT_NOT_FOUND);
        }
        
        department.restore();
        departmentRepository.save(department);
        
        log.info("部门恢复成功: ID={}", id);
        return true;
    }

    @Override
    @Transactional
    public int batchDeleteDepartments(List<Long> ids, Long deletedBy) {
        log.info("批量删除部门: IDs={}, 删除人={}", ids, deletedBy);
        
        int deletedCount = 0;
        for (Long id : ids) {
            try {
                if (deleteDepartment(id, deletedBy)) {
                    deletedCount++;
                }
            } catch (BusinessException e) {
                log.warn("删除部门失败: ID={}, 原因={}", id, e.getMessage());
            }
        }
        
        log.info("批量删除部门完成: 成功删除{}个", deletedCount);
        return deletedCount;
    }

    // ==================== 部门查询操作 ====================

    @Override
    public Page<DepartmentResponse> getDepartmentPage(Pageable pageable) {
        Page<Department> page = departmentRepository.findByDeletedFalse(pageable);
        return page.map(this::convertToDepartmentResponse);
    }

    @Override
    public PageResult<DepartmentResponse> searchDepartments(DepartmentSearchRequest request, Pageable pageable) {
        Specification<Department> spec = buildDepartmentSpecification(request);
        Page<Department> page = departmentRepository.findAll(spec, pageable);
        
        List<DepartmentResponse> responses = page.getContent().stream()
                .map(this::convertToDepartmentResponse)
                .collect(Collectors.toList());
        
        return new PageResult<>(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                page.getTotalElements(),
                responses
        );
    }

    @Override
    public List<DepartmentResponse> getRootDepartments() {
        List<Department> rootDepartments = departmentRepository.findAllRootDepartments();
        return rootDepartments.stream()
                .filter(dept -> !dept.getDeleted())
                .map(this::convertToDepartmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentResponse> getChildDepartments(Long parentId) {
        List<Department> children = departmentRepository.findChildrenByParentId(parentId);
        return children.stream()
                .filter(dept -> !dept.getDeleted())
                .map(this::convertToDepartmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentTreeResponse> getDepartmentTree() {
        List<Department> allDepartments = departmentRepository.findByDeletedFalse();
        return buildDepartmentTree(allDepartments, null);
    }

    @Override
    public List<DepartmentTreeResponse> getDepartmentTree(Long rootId) {
        List<Department> departments = new ArrayList<>();
        
        if (rootId != null) {
            Department rootDept = getDepartmentById(rootId);
            departments.add(rootDept);
            departments.addAll(departmentRepository.findDescendants(rootId));
        } else {
            departments = departmentRepository.findByDeletedFalse();
        }
        
        return buildDepartmentTree(departments, rootId);
    }

    // ==================== 部门层级关系管理 ====================

    @Override
    public List<DepartmentResponse> getAncestors(Long departmentId) {
        List<Department> ancestors = departmentRepository.findAncestors(departmentId);
        return ancestors.stream()
                .filter(dept -> !dept.getDeleted())
                .map(this::convertToDepartmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentResponse> getDescendants(Long departmentId) {
        List<Department> descendants = departmentRepository.findDescendants(departmentId);
        return descendants.stream()
                .filter(dept -> !dept.getDeleted())
                .map(this::convertToDepartmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean moveDepartment(Long departmentId, Long newParentId, Long updatedBy) {
        log.info("移动部门: ID={}, 新父部门={}, 操作人={}", departmentId, newParentId, updatedBy);
        
        Department department = getDepartmentById(departmentId);
        
        // 验证不能移动到自己或自己的子部门下
        if (!validateDepartmentHierarchy(departmentId, newParentId)) {
            throw new BusinessException(ResponseCode.PARAM_ERROR);
        }
        
        // 验证新父部门存在且启用
        if (newParentId != null) {
            Department newParent = getDepartmentById(newParentId);
            if (!newParent.isEnabled()) {
                throw new BusinessException(ResponseCode.USER_DISABLED);
            }
        }
        
        // 更新部门信息
        department.setParentId(newParentId);
        
        // 重新计算层级
        if (newParentId != null) {
            Department newParent = getDepartmentById(newParentId);
            department.setLevel(newParent.getLevel() + 1);
            // 重新构建路径，传入新父部门的路径
            department.buildPath(newParent.getPath());
        } else {
            department.setLevel(1);
            // 重新构建路径，根部门传入null
            department.buildPath(null);
        }
        department.setUpdateTime(LocalDateTime.now());
        department.setUpdateBy(updatedBy);
        
        departmentRepository.save(department);
        
        // 更新所有子部门的层级和路径
        updateDescendantsHierarchy(departmentId);
        
        log.info("部门移动成功: ID={}", departmentId);
        return true;
    }

    @Override
    @Transactional
    public boolean adjustDepartmentSort(Long departmentId, Integer newSortOrder, Long updatedBy) {
        log.info("调整部门排序: ID={}, 新排序={}, 操作人={}", departmentId, newSortOrder, updatedBy);
        
        Department department = getDepartmentById(departmentId);
        department.setSortOrder(newSortOrder);
        department.setUpdateTime(LocalDateTime.now());
        department.setUpdateBy(updatedBy);
        
        departmentRepository.save(department);
        
        log.info("部门排序调整成功: ID={}", departmentId);
        return true;
    }

    // ==================== 部门成员管理 ====================

    @Override
    public Page<UserResponse> getDepartmentMembers(Long departmentId, Pageable pageable) {
        Page<User> userPage = userRepository.findByDepartmentId(departmentId, pageable);
        return userPage.map(user -> {
            UserResponse response = new UserResponse();
            BeanUtils.copyProperties(user, response);
            return response;
        });
    }

    @Override
    public long getDepartmentMemberCount(Long departmentId) {
        return departmentRepository.getUserCount(departmentId);
    }

    @Override
    public long getTotalMemberCount(Long departmentId) {
        return departmentRepository.getTotalUserCount(departmentId);
    }

    @Override
    @Transactional
    public boolean setDepartmentManager(Long departmentId, Long managerId, Long updatedBy) {
        log.info("设置部门负责人: 部门ID={}, 负责人ID={}, 操作人={}", departmentId, managerId, updatedBy);
        
        Department department = getDepartmentById(departmentId);
        
        // 验证负责人存在
        if (managerId != null) {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND));
            
            if (manager.getDeleted() != null && manager.getDeleted() == 1) {
                throw new BusinessException(ResponseCode.USER_DISABLED);
            }
        }
        
        department.setManagerId(managerId);
        department.setUpdateTime(LocalDateTime.now());
        department.setUpdateBy(updatedBy);
        
        departmentRepository.save(department);
        
        log.info("部门负责人设置成功: 部门ID={}", departmentId);
        return true;
    }

    @Override
    @Transactional
    public int assignUsersToDepart(Long departmentId, List<Long> userIds, Long assignedBy) {
        log.info("批量分配用户到部门: 部门ID={}, 用户IDs={}, 操作人={}", departmentId, userIds, assignedBy);
        
        Department department = getDepartmentById(departmentId);
        
        if (!department.isEnabled()) {
            throw new BusinessException(ResponseCode.USER_DISABLED);
        }
        
        int assignedCount = 0;
        for (Long userId : userIds) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND));
                
                if (user.getDeleted() == null || user.getDeleted() == 0) {
                    user.setDepartmentId(departmentId);
                    user.setUpdateTime(LocalDateTime.now());
                    user.setUpdateUserId(assignedBy);
                    userRepository.save(user);
                    assignedCount++;
                }
            } catch (BusinessException e) {
                log.warn("分配用户失败: 用户ID={}, 原因={}", userId, e.getMessage());
            }
        }
        
        log.info("批量分配用户完成: 成功分配{}个", assignedCount);
        return assignedCount;
    }

    // ==================== 部门状态管理 ====================

    @Override
    @Transactional
    public boolean enableDepartment(Long departmentId, Long updatedBy) {
        log.info("启用部门: ID={}, 操作人={}", departmentId, updatedBy);
        
        Department department = getDepartmentById(departmentId);
        department.setStatus(1);
        department.setUpdateTime(LocalDateTime.now());
        department.setUpdateBy(updatedBy);
        
        departmentRepository.save(department);
        
        log.info("部门启用成功: ID={}", departmentId);
        return true;
    }

    @Override
    @Transactional
    public boolean disableDepartment(Long departmentId, Long updatedBy) {
        log.info("禁用部门: ID={}, 操作人={}", departmentId, updatedBy);
        
        Department department = getDepartmentById(departmentId);
        department.setStatus(0);
        department.setUpdateTime(LocalDateTime.now());
        department.setUpdateBy(updatedBy);
        
        departmentRepository.save(department);
        
        log.info("部门禁用成功: ID={}", departmentId);
        return true;
    }

    @Override
    @Transactional
    public int batchUpdateDepartmentStatus(List<Long> departmentIds, Integer status, Long updatedBy) {
        log.info("批量更新部门状态: IDs={}, 状态={}, 操作人={}", departmentIds, status, updatedBy);
        
        departmentRepository.batchUpdateStatus(departmentIds, status);
        
        log.info("批量更新部门状态完成: 更新{}个", departmentIds.size());
        return departmentIds.size();
    }

    // ==================== 部门验证操作 ====================

    @Override
    public boolean existsByCode(String code) {
        return departmentRepository.existsByCode(code);
    }

    @Override
    public boolean existsByName(String name) {
        return departmentRepository.findByName(name).isPresent();
    }

    @Override
    public boolean existsByCodeExcludeId(String code, Long excludeId) {
        return departmentRepository.existsByCodeAndIdNot(code, excludeId);
    }

    @Override
    public boolean existsByNameExcludeId(String name, Long excludeId) {
        return departmentRepository.existsByNameAndIdNot(name, excludeId);
    }

    @Override
    public boolean hasChildren(Long departmentId) {
        return departmentRepository.hasChildren(departmentId);
    }

    @Override
    public boolean hasUsers(Long departmentId) {
        return departmentRepository.getUserCount(departmentId) > 0;
    }

    @Override
    public boolean validateDepartmentHierarchy(Long departmentId, Long parentId) {
        if (parentId == null) {
            return true;
        }
        
        if (departmentId.equals(parentId)) {
            return false;
        }
        
        // 检查是否会形成循环引用
        List<Department> descendants = departmentRepository.findDescendants(departmentId);
        return descendants.stream().noneMatch(dept -> dept.getId().equals(parentId));
    }

    // ==================== 部门统计操作 ====================

    @Override
    public Map<String, Object> getDepartmentStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("totalCount", departmentRepository.count());
        statistics.put("enabledCount", departmentRepository.countByStatus(1));
        statistics.put("disabledCount", departmentRepository.countByStatus(0));
        statistics.put("rootCount", departmentRepository.countRootDepartments());
        
        return statistics;
    }

    @Override
    public Map<Integer, Long> countDepartmentsByLevel() {
        List<Object[]> results = departmentRepository.countDepartmentsByLevel();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Map<Integer, Long> countDepartmentsByStatus() {
        List<Object[]> results = departmentRepository.countDepartmentsByStatus();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Map<String, Long> countDepartmentsByType() {
        List<Object[]> results = departmentRepository.countDepartmentsByType();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public List<Map<String, Object>> getDepartmentCreationTrend(int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<Object[]> results = departmentRepository.getDepartmentCreationTrend(startDate, endDate);
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", result[0]);
                    item.put("count", result[1]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    // ==================== 导入导出操作 ====================

    @Override
    public Map<String, Object> exportDepartments(List<Long> departmentIds, String format, List<String> includeFields) {
        log.info("开始导出部门数据，部门ID列表：{}，格式：{}，包含字段：{}", departmentIds, format, includeFields);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 查询部门数据
            List<Department> departments;
            if (departmentIds != null && !departmentIds.isEmpty()) {
                // 导出指定部门
                departments = departmentRepository.findAllById(departmentIds);
            } else {
                // 导出所有部门
                departments = departmentRepository.findByDeletedFalse();
            }
            
            if (departments.isEmpty()) {
                result.put("success", false);
                result.put("message", "没有找到要导出的部门数据");
                return result;
            }
            
            // 2. 根据格式处理数据
            String exportData;
            String fileName;
            String contentType;
            
            switch (format.toUpperCase()) {
                case "JSON":
                    exportData = exportToJson(departments, includeFields);
                    fileName = "departments_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
                    contentType = "application/json";
                    break;
                case "CSV":
                    exportData = exportToCsv(departments, includeFields);
                    fileName = "departments_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
                    contentType = "text/csv";
                    break;
                case "EXCEL":
                    exportData = exportToExcel(departments, includeFields);
                    fileName = "departments_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "不支持的导出格式：" + format);
                    return result;
            }
            
            // 3. 构建返回结果
            result.put("success", true);
            result.put("data", exportData);
            result.put("fileName", fileName);
            result.put("contentType", contentType);
            result.put("recordCount", departments.size());
            result.put("exportTime", LocalDateTime.now());
            
            log.info("部门数据导出成功，共导出 {} 条记录", departments.size());
            return result;
            
        } catch (Exception e) {
            log.error("导出部门数据失败", e);
            result.put("success", false);
            result.put("message", "导出失败：" + e.getMessage());
            return result;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> importDepartments(List<Department> departments, String importMode, Long importedBy) {
        log.info("开始导入部门数据，部门数量：{}，导入模式：{}，导入人：{}", departments.size(), importMode, importedBy);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (departments == null || departments.isEmpty()) {
                result.put("success", false);
                result.put("message", "没有要导入的部门数据");
                return result;
            }
            
            int successCount = 0;
            int skipCount = 0;
            int errorCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            for (Department department : departments) {
                try {
                    // 验证部门数据
                    validateDepartmentForImport(department);
                    
                    // 检查部门是否已存在（根据编码）
                    Optional<Department> existingDept = departmentRepository.findByCode(department.getCode());
                    
                    if (existingDept.isPresent()) {
                        if ("SKIP".equalsIgnoreCase(importMode)) {
                            skipCount++;
                            log.info("跳过已存在的部门：{}", department.getCode());
                            continue;
                        } else if ("UPDATE".equalsIgnoreCase(importMode)) {
                            // 更新现有部门
                            Department existing = existingDept.get();
                            updateExistingDepartment(existing, department, importedBy);
                            successCount++;
                            log.info("更新部门成功：{}", department.getCode());
                        } else {
                            // 默认模式：跳过
                            skipCount++;
                            log.info("跳过已存在的部门：{}", department.getCode());
                            continue;
                        }
                    } else {
                        // 创建新部门
                        createNewDepartment(department, importedBy);
                        successCount++;
                        log.info("创建部门成功：{}", department.getCode());
                    }
                    
                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = String.format("部门 %s 导入失败：%s", department.getCode(), e.getMessage());
                    errorMessages.add(errorMsg);
                    log.error("部门导入失败：{}", department.getCode(), e);
                }
            }
            
            // 构建返回结果
            result.put("success", true);
            result.put("totalCount", departments.size());
            result.put("successCount", successCount);
            result.put("skipCount", skipCount);
            result.put("errorCount", errorCount);
            result.put("errorMessages", errorMessages);
            result.put("importTime", LocalDateTime.now());
            
            log.info("部门导入完成，成功：{}，跳过：{}，失败：{}", successCount, skipCount, errorCount);
            return result;
            
        } catch (Exception e) {
            log.error("部门导入失败", e);
            result.put("success", false);
            result.put("message", "导入失败：" + e.getMessage());
            return result;
        }
    }

    @Override
    @Transactional
    public int batchImportDepartments(List<Department> departments, Long createdBy) {
        log.info("开始批量导入部门数据，部门数量：{}，创建人：{}", departments.size(), createdBy);
        
        if (departments == null || departments.isEmpty()) {
            log.warn("没有要批量导入的部门数据");
            return 0;
        }
        
        int successCount = 0;
        
        try {
            // 按父部门ID分组，确保父部门先创建
            Map<Long, List<Department>> groupedByParent = departments.stream()
                    .collect(Collectors.groupingBy(dept -> dept.getParentId() != null ? dept.getParentId() : 0L));
            
            // 先处理根部门（parentId为null或0）
            List<Department> rootDepartments = groupedByParent.getOrDefault(0L, new ArrayList<>());
            for (Department dept : rootDepartments) {
                try {
                    validateDepartmentForImport(dept);
                    createNewDepartment(dept, createdBy);
                    successCount++;
                    log.debug("批量导入根部门成功：{}", dept.getCode());
                } catch (Exception e) {
                    log.error("批量导入根部门失败：{}", dept.getCode(), e);
                }
            }
            
            // 然后处理有父部门的部门
            for (Map.Entry<Long, List<Department>> entry : groupedByParent.entrySet()) {
                Long parentId = entry.getKey();
                if (parentId == 0L) continue; // 跳过根部门
                
                List<Department> childDepartments = entry.getValue();
                for (Department dept : childDepartments) {
                    try {
                        validateDepartmentForImport(dept);
                        createNewDepartment(dept, createdBy);
                        successCount++;
                        log.debug("批量导入子部门成功：{}", dept.getCode());
                    } catch (Exception e) {
                        log.error("批量导入子部门失败：{}", dept.getCode(), e);
                    }
                }
            }
            
            log.info("批量导入部门完成，成功导入 {} 个部门", successCount);
            return successCount;
            
        } catch (Exception e) {
            log.error("批量导入部门失败", e);
            throw new RuntimeException("批量导入失败：" + e.getMessage(), e);
        }
    }

    // ==================== 权限管理操作 ====================

    @Override
    public List<String> getDepartmentPermissions(Long departmentId) {
        log.info("获取部门权限，部门ID：{}", departmentId);
        
        if (departmentId == null) {
            log.warn("部门ID不能为空");
            return new ArrayList<>();
        }
        
        try {
            // 1. 获取部门下的所有用户
            List<User> departmentUsers = userRepository.findByDepartmentIdAndDeletedFalse(departmentId);
            
            if (departmentUsers.isEmpty()) {
                log.info("部门 {} 下没有用户", departmentId);
                return new ArrayList<>();
            }
            
            // 2. 获取所有用户的权限（通过角色）
            Set<String> allPermissions = new HashSet<>();
            
            for (User user : departmentUsers) {
                // 获取用户的角色权限
                List<String> userPermissions = getUserPermissionsByRoles(user.getId());
                allPermissions.addAll(userPermissions);
            }
            
            List<String> result = new ArrayList<>(allPermissions);
            log.info("部门 {} 共有 {} 个权限", departmentId, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("获取部门权限失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public int assignPermissionsToDepartment(Long departmentId, List<Long> permissionIds, Long assignedBy) {
        log.info("为部门分配权限，部门ID：{}，权限数量：{}，分配人：{}", departmentId, permissionIds.size(), assignedBy);
        
        if (departmentId == null || permissionIds == null || permissionIds.isEmpty()) {
            log.warn("部门ID或权限列表不能为空");
            return 0;
        }
        
        try {
            // 1. 获取部门下的所有用户
            List<User> departmentUsers = userRepository.findByDepartmentIdAndDeletedFalse(departmentId);
            
            if (departmentUsers.isEmpty()) {
                log.warn("部门 {} 下没有用户，无法分配权限", departmentId);
                return 0;
            }
            
            // 2. 为每个用户分配权限（通过角色）
            int totalAssigned = 0;
            
            for (User user : departmentUsers) {
                // 获取用户的主要角色（这里简化处理，实际可能需要更复杂的逻辑）
                Long userRoleId = getUserPrimaryRoleId(user.getId());
                
                if (userRoleId != null) {
                    // 为角色分配权限
                    int assigned = assignPermissionsToRole(userRoleId, permissionIds, assignedBy);
                    totalAssigned += assigned;
                    log.debug("为用户 {} 的角色 {} 分配了 {} 个权限", user.getId(), userRoleId, assigned);
                }
            }
            
            log.info("为部门 {} 分配权限完成，共分配 {} 个权限", departmentId, totalAssigned);
            return totalAssigned;
            
        } catch (Exception e) {
            log.error("为部门分配权限失败", e);
            throw new RuntimeException("分配权限失败：" + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasPermission(Long departmentId, String permission) {
        log.debug("检查部门权限，部门ID：{}，权限：{}", departmentId, permission);
        
        if (departmentId == null || !StringUtils.hasText(permission)) {
            log.warn("部门ID或权限不能为空");
            return false;
        }
        
        try {
            // 获取部门的所有权限
            List<String> departmentPermissions = getDepartmentPermissions(departmentId);
            
            // 检查是否包含指定权限
            boolean hasPermission = departmentPermissions.contains(permission);
            
            log.debug("部门 {} 权限检查结果：{}", departmentId, hasPermission);
            return hasPermission;
            
        } catch (Exception e) {
            log.error("检查部门权限失败", e);
            return false;
        }
    }

    // ==================== 通知操作 ====================

    @Override
    @Transactional
    public boolean sendNotificationToDepartment(Long departmentId, String title, String content, String type) {
        log.info("发送部门通知，部门ID：{}，标题：{}，类型：{}", departmentId, title, type);
        
        if (departmentId == null || !StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            log.warn("部门ID、标题或内容不能为空");
            return false;
        }
        
        try {
            // 1. 获取部门下的所有用户
            List<User> departmentUsers = userRepository.findByDepartmentIdAndDeletedFalse(departmentId);
            
            if (departmentUsers.isEmpty()) {
                log.warn("部门 {} 下没有用户，无法发送通知", departmentId);
                return false;
            }
            
            // 2. 为每个用户发送通知
            int successCount = 0;
            
            for (User user : departmentUsers) {
                try {
                    // 这里应该调用通知服务发送通知
                    // notificationService.sendNotification(user.getId(), title, content, type);
                    
                    // 暂时记录日志，实际项目中需要集成通知服务
                    log.info("发送通知给用户 {}：{} - {}", user.getId(), title, content);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("发送通知给用户 {} 失败", user.getId(), e);
                }
            }
            
            boolean success = successCount > 0;
            log.info("部门通知发送完成，成功：{}/{}", successCount, departmentUsers.size());
            return success;
            
        } catch (Exception e) {
            log.error("发送部门通知失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> generateDepartmentReport(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("生成部门报告，时间范围：{} 至 {}", startDate, endDate);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            // 1. 基本统计信息
            long totalDepartments = departmentRepository.countByDeletedFalse();
            long enabledDepartments = departmentRepository.countByEnabledTrueAndDeletedFalse();
            long disabledDepartments = totalDepartments - enabledDepartments;
            
            // 2. 用户统计
            long totalUsers = userRepository.countByDeletedFalse();
            long departmentUsers = userRepository.countByDepartmentIdNotNullAndDeletedFalse();
            
            // 3. 部门层级统计
            long rootDepartments = departmentRepository.countByParentIdIsNullAndDeletedFalse();
            long childDepartments = totalDepartments - rootDepartments;
            
            // 4. 时间范围内的变化统计
            long createdInPeriod = departmentRepository.countByCreateTimeBetweenAndDeletedFalse(startDate, endDate);
            long updatedInPeriod = departmentRepository.countByUpdateTimeBetweenAndDeletedFalse(startDate, endDate);
            
            // 5. 构建报告
            report.put("success", true);
            report.put("reportPeriod", startDate + " 至 " + endDate);
            report.put("generatedTime", LocalDateTime.now());
            
            // 基本统计
            Map<String, Object> basicStats = new HashMap<>();
            basicStats.put("totalDepartments", totalDepartments);
            basicStats.put("enabledDepartments", enabledDepartments);
            basicStats.put("disabledDepartments", disabledDepartments);
            basicStats.put("rootDepartments", rootDepartments);
            basicStats.put("childDepartments", childDepartments);
            report.put("basicStatistics", basicStats);
            
            // 用户统计
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("totalUsers", totalUsers);
            userStats.put("departmentUsers", departmentUsers);
            userStats.put("usersWithoutDepartment", totalUsers - departmentUsers);
            report.put("userStatistics", userStats);
            
            // 时间范围统计
            Map<String, Object> periodStats = new HashMap<>();
            periodStats.put("createdInPeriod", createdInPeriod);
            periodStats.put("updatedInPeriod", updatedInPeriod);
            report.put("periodStatistics", periodStats);
            
            // 部门列表（前10个）
            List<Department> topDepartments = departmentRepository.findTop10ByDeletedFalseOrderByCreateTimeDesc();
            List<Map<String, Object>> departmentList = topDepartments.stream()
                    .map(dept -> {
                        Map<String, Object> deptInfo = new HashMap<>();
                        deptInfo.put("id", dept.getId());
                        deptInfo.put("name", dept.getName());
                        deptInfo.put("code", dept.getCode());
                        deptInfo.put("enabled", dept.isEnabled());
                        deptInfo.put("userCount", departmentRepository.getUserCount(dept.getId()));
                        deptInfo.put("createTime", dept.getCreateTime());
                        return deptInfo;
                    })
                    .collect(Collectors.toList());
            report.put("topDepartments", departmentList);
            
            log.info("部门报告生成成功");
            return report;
            
        } catch (Exception e) {
            log.error("生成部门报告失败", e);
            report.put("success", false);
            report.put("message", "报告生成失败：" + e.getMessage());
            return report;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建部门查询条件
     */
    private Specification<Department> buildDepartmentSpecification(DepartmentSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 基本条件：未删除
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));
            
            if (request == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
            
            // 名称模糊查询
            if (StringUtils.hasText(request.getName())) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + request.getName() + "%"));
            }
            
            // 编码精确查询
            if (StringUtils.hasText(request.getCode())) {
                predicates.add(criteriaBuilder.equal(root.get("code"), request.getCode()));
            }
            
            // 父部门ID
            if (request.getParentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("parentId"), request.getParentId()));
            }
            
            // 状态
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            
            // 层级
            if (request.getLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("level"), request.getLevel()));
            }
            
            // 类型
            if (StringUtils.hasText(request.getType())) {
                predicates.add(criteriaBuilder.equal(root.get("type"), request.getType()));
            }
            
            // 负责人ID
            if (request.getManagerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("managerId"), request.getManagerId()));
            }
            
            // 联系人
            if (StringUtils.hasText(request.getContact())) {
                predicates.add(criteriaBuilder.like(root.get("contact"), "%" + request.getContact() + "%"));
            }
            
            // 联系电话
            if (StringUtils.hasText(request.getPhone())) {
                predicates.add(criteriaBuilder.like(root.get("phone"), "%" + request.getPhone() + "%"));
            }
            
            // 邮箱
            if (StringUtils.hasText(request.getEmail())) {
                predicates.add(criteriaBuilder.like(root.get("email"), "%" + request.getEmail() + "%"));
            }
            
            // 关键词搜索（名称、描述、备注）
            if (StringUtils.hasText(request.getKeyword())) {
                Predicate namePredicate = criteriaBuilder.like(root.get("name"), "%" + request.getKeyword() + "%");
                Predicate descPredicate = criteriaBuilder.like(root.get("description"), "%" + request.getKeyword() + "%");
                Predicate remarkPredicate = criteriaBuilder.like(root.get("remark"), "%" + request.getKeyword() + "%");
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate, remarkPredicate));
            }
            
            // 创建时间范围
            if (request.getCreateTimeStart() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), request.getCreateTimeStart()));
            }
            if (request.getCreateTimeEnd() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), request.getCreateTimeEnd()));
            }
            
            // 更新时间范围
            if (request.getUpdateTimeStart() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("updateTime"), request.getUpdateTimeStart()));
            }
            if (request.getUpdateTimeEnd() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updateTime"), request.getUpdateTimeEnd()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 构建部门树形结构
     */
    private List<DepartmentTreeResponse> buildDepartmentTree(List<Department> departments, Long parentId) {
        Map<Long, List<Department>> departmentMap = departments.stream()
                .collect(Collectors.groupingBy(dept -> dept.getParentId() == null ? 0L : dept.getParentId()));
        
        return buildTreeRecursive(departmentMap, parentId == null ? 0L : parentId);
    }

    /**
     * 递归构建树形结构
     */
    private List<DepartmentTreeResponse> buildTreeRecursive(Map<Long, List<Department>> departmentMap, Long parentId) {
        List<Department> children = departmentMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        
        return children.stream()
                .map(dept -> {
                    DepartmentTreeResponse response = convertToDepartmentTreeResponse(dept);
                    List<DepartmentTreeResponse> childrenTree = buildTreeRecursive(departmentMap, dept.getId());
                    response.setChildren(childrenTree);
                    return response;
                })
                .sorted(Comparator.comparing(DepartmentTreeResponse::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    /**
     * 更新后代部门的层级和路径
     */
    private void updateDescendantsHierarchy(Long departmentId) {
        List<Department> descendants = departmentRepository.findDescendants(departmentId);
        
        for (Department descendant : descendants) {
            // 重新计算层级
            if (descendant.getParentId() != null) {
                Department parent = departmentRepository.findById(descendant.getParentId()).orElse(null);
                if (parent != null) {
                    descendant.setLevel(parent.getLevel() + 1);
                    // 重新构建路径，传入父部门的路径
                    descendant.buildPath(parent.getPath());
                } else {
                    // 如果找不到父部门，设为根部门
                    descendant.setLevel(1);
                    descendant.buildPath(null);
                }
            } else {
                // 没有父部门，是根部门
                descendant.setLevel(1);
                descendant.buildPath(null);
            }
            descendant.setUpdateTime(LocalDateTime.now());
        }
        
        departmentRepository.saveAll(descendants);
    }

    /**
     * 转换为部门响应DTO
     */
    private DepartmentResponse convertToDepartmentResponse(Department department) {
        if (department == null) {
            return null;
        }
        
        DepartmentResponse response = new DepartmentResponse();
        BeanUtils.copyProperties(department, response);
        
        // 设置父部门名称
        if (department.getParentId() != null) {
            departmentRepository.findById(department.getParentId())
                    .ifPresent(parent -> response.setParentName(parent.getName()));
        }
        
        // 设置负责人信息
        if (department.getManagerId() != null) {
            try {
                UserResponse manager = userService.getUserResponseById(department.getManagerId());
                if (manager != null) {
                    response.setManagerName(manager.getDisplayName());
                }
            } catch (Exception e) {
                log.warn("获取部门负责人信息失败: 部门ID={}, 负责人ID={}", department.getId(), department.getManagerId());
            }
        }
        
        // 设置用户数量
        response.setUserCount(departmentRepository.getUserCount(department.getId()));
        response.setTotalUserCount(departmentRepository.getTotalUserCount(department.getId()));
        
        // 设置状态标识
        response.setIsRoot(department.isRoot());
        response.setIsEnabled(department.isEnabled());
        response.setHasChildren(departmentRepository.hasChildren(department.getId()));
        response.setDisplayName(department.getDisplayName());
        
        return response;
    }

    /**
     * 转换为部门树响应DTO
     */
    private DepartmentTreeResponse convertToDepartmentTreeResponse(Department department) {
        if (department == null) {
            return null;
        }
        
        DepartmentTreeResponse response = new DepartmentTreeResponse();
        BeanUtils.copyProperties(department, response);
        
        // 设置负责人信息
        if (department.getManagerId() != null) {
            try {
                UserResponse manager = userService.getUserResponseById(department.getManagerId());
                if (manager != null) {
                    response.setManagerName(manager.getDisplayName());
                }
            } catch (Exception e) {
                log.warn("获取部门负责人信息失败: 部门ID={}, 负责人ID={}", department.getId(), department.getManagerId());
            }
        }
        
        // 设置用户数量
        response.setUserCount(departmentRepository.getUserCount(department.getId()));
        response.setTotalUserCount(departmentRepository.getTotalUserCount(department.getId()));
        
        // 设置状态标识
        response.setIsRoot(department.isRoot());
        response.setIsEnabled(department.isEnabled());
        response.setHasChildren(departmentRepository.hasChildren(department.getId()));
        response.setDisplayName(department.getDisplayName());
        
        return response;
    }

    // ==================== 导出辅助方法 ====================

    /**
     * 导出为JSON格式
     */
    private String exportToJson(List<Department> departments, List<String> includeFields) {
        try {
            List<Map<String, Object>> exportData = departments.stream()
                    .map(dept -> convertToExportMap(dept, includeFields))
                    .collect(Collectors.toList());
            
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.registerModule(new JavaTimeModule());
            
            return objectMapper.writeValueAsString(exportData);
        } catch (Exception e) {
            log.error("JSON导出失败", e);
            throw new RuntimeException("JSON导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出为CSV格式
     */
    private String exportToCsv(List<Department> departments, List<String> includeFields) {
        StringBuilder csv = new StringBuilder();
        
        // 添加BOM以支持中文
        csv.append("\uFEFF");
        
        // 添加表头
        List<String> headers = getExportHeaders(includeFields);
        csv.append(String.join(",", headers)).append("\n");
        
        // 添加数据行
        for (Department dept : departments) {
            Map<String, Object> data = convertToExportMap(dept, includeFields);
            List<String> row = headers.stream()
                    .map(header -> escapeCsvValue(String.valueOf(data.getOrDefault(header, ""))))
                    .collect(Collectors.toList());
            csv.append(String.join(",", row)).append("\n");
        }
        
        return csv.toString();
    }

    /**
     * 导出为Excel格式（简化实现）
     */
    private String exportToExcel(List<Department> departments, List<String> includeFields) {
        // 这里简化实现，实际项目中可以使用Apache POI
        // 暂时返回CSV格式，前端可以处理为Excel
        return exportToCsv(departments, includeFields);
    }

    /**
     * 转换为导出Map
     */
    private Map<String, Object> convertToExportMap(Department department, List<String> includeFields) {
        Map<String, Object> map = new HashMap<>();
        
        // 默认包含的字段
        List<String> defaultFields = Arrays.asList(
                "id", "name", "code", "parentId", "managerId", "description", 
                "enabled", "sortOrder", "createTime", "updateTime"
        );
        
        List<String> fieldsToInclude = (includeFields != null && !includeFields.isEmpty()) 
                ? includeFields : defaultFields;
        
        for (String field : fieldsToInclude) {
            switch (field) {
                case "id":
                    map.put("部门ID", department.getId());
                    break;
                case "name":
                    map.put("部门名称", department.getName());
                    break;
                case "code":
                    map.put("部门编码", department.getCode());
                    break;
                case "parentId":
                    map.put("父部门ID", department.getParentId());
                    break;
                case "managerId":
                    map.put("负责人ID", department.getManagerId());
                    break;
                case "description":
                    map.put("部门描述", department.getDescription());
                    break;
                case "enabled":
                    map.put("是否启用", department.isEnabled() ? "是" : "否");
                    break;
                case "sortOrder":
                    map.put("排序", department.getSortOrder());
                    break;
                case "createTime":
                    map.put("创建时间", department.getCreateTime());
                    break;
                case "updateTime":
                    map.put("更新时间", department.getUpdateTime());
                    break;
            }
        }
        
        return map;
    }

    /**
     * 获取导出表头
     */
    private List<String> getExportHeaders(List<String> includeFields) {
        List<String> defaultFields = Arrays.asList(
                "id", "name", "code", "parentId", "managerId", "description", 
                "enabled", "sortOrder", "createTime", "updateTime"
        );
        
        List<String> fieldsToInclude = (includeFields != null && !includeFields.isEmpty()) 
                ? includeFields : defaultFields;
        
        return fieldsToInclude.stream()
                .map(field -> {
                    switch (field) {
                        case "id": return "部门ID";
                        case "name": return "部门名称";
                        case "code": return "部门编码";
                        case "parentId": return "父部门ID";
                        case "managerId": return "负责人ID";
                        case "description": return "部门描述";
                        case "enabled": return "是否启用";
                        case "sortOrder": return "排序";
                        case "createTime": return "创建时间";
                        case "updateTime": return "更新时间";
                        default: return field;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * CSV值转义
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // 如果包含逗号、引号或换行符，需要用引号包围并转义引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    // ==================== 导入辅助方法 ====================

    /**
     * 验证导入的部门数据
     */
    private void validateDepartmentForImport(Department department) {
        if (department == null) {
            throw new IllegalArgumentException("部门数据不能为空");
        }
        
        if (!StringUtils.hasText(department.getName())) {
            throw new IllegalArgumentException("部门名称不能为空");
        }
        
        if (!StringUtils.hasText(department.getCode())) {
            throw new IllegalArgumentException("部门编码不能为空");
        }
        
        // 验证编码格式
        if (!department.getCode().matches("^[A-Z0-9_]+$")) {
            throw new IllegalArgumentException("部门编码只能包含大写字母、数字和下划线");
        }
        
        // 验证父部门是否存在
        if (department.getParentId() != null) {
            Optional<Department> parent = departmentRepository.findById(department.getParentId());
            if (!parent.isPresent()) {
                throw new IllegalArgumentException("父部门不存在，ID：" + department.getParentId());
            }
        }
        
        // 验证负责人是否存在
        if (department.getManagerId() != null) {
            Optional<User> manager = userRepository.findById(department.getManagerId());
            if (!manager.isPresent()) {
                throw new IllegalArgumentException("负责人不存在，ID：" + department.getManagerId());
            }
        }
    }

    /**
     * 更新现有部门
     */
    private void updateExistingDepartment(Department existing, Department newData, Long updatedBy) {
        // 保留原有ID和创建信息
        Long originalId = existing.getId();
        LocalDateTime originalCreateTime = existing.getCreateTime();
        Long originalCreatedBy = existing.getCreatedBy();
        
        // 更新字段
        existing.setName(newData.getName());
        existing.setDescription(newData.getDescription());
        existing.setParentId(newData.getParentId());
        existing.setManagerId(newData.getManagerId());
        existing.setEnabled(newData.isEnabled());
        existing.setSortOrder(newData.getSortOrder());
        
        // 设置更新信息
        existing.setUpdateTime(LocalDateTime.now());
        existing.setUpdatedBy(updatedBy);
        
        // 保存
        departmentRepository.save(existing);
    }

    /**
     * 创建新部门
     */
    private void createNewDepartment(Department department, Long createdBy) {
        // 设置创建信息
        department.setId(null); // 确保是新记录
        department.setCreateTime(LocalDateTime.now());
        department.setCreatedBy(createdBy);
        department.setUpdateTime(LocalDateTime.now());
        department.setUpdatedBy(createdBy);
        department.setDeleted(false);
        
        // 设置默认值
        if (department.getSortOrder() == null) {
            department.setSortOrder(0);
        }
        
        // 保存
        departmentRepository.save(department);
    }

    // ==================== 权限辅助方法 ====================

    /**
     * 获取用户通过角色的权限
     */
    private List<String> getUserPermissionsByRoles(Long userId) {
        // 这里需要实现获取用户角色权限的逻辑
        // 暂时返回空列表，实际项目中需要查询用户角色和角色权限
        log.debug("获取用户 {} 的权限（通过角色）", userId);
        return new ArrayList<>();
    }

    /**
     * 获取用户的主要角色ID
     */
    private Long getUserPrimaryRoleId(Long userId) {
        // 这里需要实现获取用户主要角色的逻辑
        // 暂时返回null，实际项目中需要查询用户角色关系
        log.debug("获取用户 {} 的主要角色ID", userId);
        return null;
    }

    /**
     * 为角色分配权限
     */
    private int assignPermissionsToRole(Long roleId, List<Long> permissionIds, Long assignedBy) {
        // 这里需要实现为角色分配权限的逻辑
        // 暂时返回0，实际项目中需要操作角色权限关联表
        log.debug("为角色 {} 分配 {} 个权限", roleId, permissionIds.size());
        return 0;
    }
}