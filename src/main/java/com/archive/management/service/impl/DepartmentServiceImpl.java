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
        // TODO: 实现部门导出功能
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "导出功能暂未实现");
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> importDepartments(List<Department> departments, String importMode, Long importedBy) {
        // TODO: 实现部门导入功能
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "导入功能暂未实现");
        return result;
    }

    @Override
    @Transactional
    public int batchImportDepartments(List<Department> departments, Long createdBy) {
        // TODO: 实现批量导入功能
        return 0;
    }

    // ==================== 权限管理操作 ====================

    @Override
    public List<String> getDepartmentPermissions(Long departmentId) {
        // TODO: 实现获取部门权限功能
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public int assignPermissionsToDepartment(Long departmentId, List<Long> permissionIds, Long assignedBy) {
        // TODO: 实现分配权限功能
        return 0;
    }

    @Override
    public boolean hasPermission(Long departmentId, String permission) {
        // TODO: 实现权限检查功能
        return false;
    }

    // ==================== 通知操作 ====================

    @Override
    @Transactional
    public boolean sendNotificationToDepartment(Long departmentId, String title, String content, String type) {
        // TODO: 实现部门通知功能
        return false;
    }

    @Override
    public Map<String, Object> generateDepartmentReport(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: 实现部门报告生成功能
        Map<String, Object> report = new HashMap<>();
        report.put("success", false);
        report.put("message", "报告生成功能暂未实现");
        return report;
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
}