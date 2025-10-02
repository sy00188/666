package com.archive.management.service.impl;

import com.archive.management.dto.request.*;
import com.archive.management.dto.response.*;
import com.archive.management.entity.*;
import com.archive.management.mapper.*;
import com.archive.management.service.SystemService;
import com.archive.management.common.PageResult;
import com.archive.management.common.PermissionTreeNode;
import com.archive.management.common.Result;
import com.archive.management.util.ValidationUtil;
import com.archive.management.exception.BusinessException;
import com.archive.management.exception.ValidationException;
import com.archive.management.util.SecurityUtils;
import com.archive.management.util.AuditLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统管理服务实现类
 */
@Service
@Transactional
public class SystemServiceImpl implements SystemService {

    @Autowired
    private DepartmentMapper departmentMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ArchiveMapper archiveMapper;
    
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    // ==================== 部门管理 ====================

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
        // 参数验证
        ValidationUtil.ValidationResult result = ValidationUtil.createValidationResult();
        ValidationUtil.validateRequired(result, request.getName(), "部门名称");
        ValidationUtil.validateLength(result, request.getName(), "部门名称", 1, 50);
        ValidationUtil.validateRequired(result, request.getCode(), "部门编码");
        ValidationUtil.validateLength(result, request.getCode(), "部门编码", 1, 20);
        
        if (!result.isValid()) {
            Map<String, List<String>> errorMap = new HashMap<>();
            errorMap.put("validation", result.getErrors());
            throw new ValidationException(errorMap);
        }

        // 检查部门编码是否已存在
        if (departmentMapper.existsByCode(request.getCode())) {
            throw new BusinessException("部门编码已存在");
        }

        // 检查父部门是否存在
        if (request.getParentId() != null && request.getParentId() > 0) {
            Department parent = departmentMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new BusinessException("父部门不存在");
            }
        }

        // 创建部门
        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setParentId(request.getParentId());
        department.setDescription(request.getDescription());
        department.setSort(request.getSortOrder() != null ? request.getSortOrder() : 0);
        department.setStatus(1); // 默认启用
        department.setCreateTime(LocalDateTime.now());
        department.setCreateBy(SecurityUtils.getCurrentUserId());

        departmentMapper.insert(department);

        // 记录审计日志
        AuditLogUtil.log("CREATE_DEPARTMENT", "创建部门: " + department.getName(), department.getId());

        return convertToDepartmentResponse(department);
    }

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentResponse updateDepartment(Long departmentId, DepartmentUpdateRequest request) {
        // 参数验证
        ValidationUtil.ValidationResult result = ValidationUtil.createValidationResult();
        ValidationUtil.validateRequired(result, departmentId, "部门ID");
        ValidationUtil.validateRequired(result, request.getName(), "部门名称");
        ValidationUtil.validateLength(result, request.getName(), "部门名称", 1, 50);
        
        if (!result.isValid()) {
            Map<String, List<String>> errorMap = new HashMap<>();
            errorMap.put("validation", result.getErrors());
            throw new ValidationException(errorMap);
        }

        // 检查部门是否存在
        Department department = departmentMapper.selectById(departmentId);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查父部门循环引用
        if (request.getParentId() != null && request.getParentId() > 0) {
            if (request.getParentId().equals(departmentId)) {
                throw new BusinessException("不能将自己设为父部门");
            }
            // 检查是否会形成循环引用
            if (isCircularReference(departmentId, request.getParentId())) {
                throw new BusinessException("不能形成循环引用");
            }
        }

        // 更新部门信息
        department.setName(request.getName());
        department.setParentId(request.getParentId());
        department.setDescription(request.getDescription());
        department.setSort(request.getSortOrder() != null ? request.getSortOrder() : department.getSort());
        department.setUpdateTime(LocalDateTime.now());
        department.setUpdateBy(SecurityUtils.getCurrentUserId());

        departmentMapper.updateById(department);

        // 记录审计日志
        AuditLogUtil.log("UPDATE_DEPARTMENT", "更新部门: " + department.getName(), department.getId());

        return convertToDepartmentResponse(department);
    }

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public void deleteDepartment(Long departmentId) {
        // 参数验证
        if (departmentId == null || departmentId <= 0) {
            throw new ValidationException("部门ID不能为空");
        }

        // 检查部门是否存在
        Department department = departmentMapper.selectById(departmentId);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查是否有子部门
        if (departmentMapper.countByParentId(departmentId) > 0) {
            throw new BusinessException("存在子部门，无法删除");
        }

        // 检查是否有用户
        if (userMapper.countByDepartmentId(departmentId) > 0) {
            throw new BusinessException("部门下存在用户，无法删除");
        }

        // 删除部门
        departmentMapper.deleteById(departmentId);

        // 记录审计日志
        AuditLogUtil.log("DELETE_DEPARTMENT", "删除部门: " + department.getName(), departmentId);
    }

    @Override
    public DepartmentResponse getDepartmentById(Long departmentId) {
        if (departmentId == null || departmentId <= 0) {
            throw new ValidationException("部门ID不能为空");
        }

        Department department = departmentMapper.selectById(departmentId);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }

        return convertToDepartmentResponse(department);
    }

    @Override
    public PageResult<DepartmentResponse> queryDepartments(DepartmentQueryRequest request) {
        // 构建查询条件
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(request.getName())) {
            params.put("name", request.getName());
        }
        if (StringUtils.hasText(request.getCode())) {
            params.put("code", request.getCode());
        }
        if (request.getParentId() != null) {
            params.put("parentId", request.getParentId());
        }
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            params.put("statuses", request.getStatuses());
        }

        // 查询总数
        long total = departmentMapper.countByParams(params);
        
        // 查询数据
        List<Department> departments = departmentMapper.selectByParams(params, 
            (request.getPageNum() - 1) * request.getPageSize(), request.getPageSize());

        // 转换响应
        List<DepartmentResponse> responses = departments.stream()
            .map(this::convertToDepartmentResponse)
            .collect(Collectors.toList());

        return new PageResult<>(request.getPageNum(), request.getPageSize(), total, responses);
    }

    @Override
    @Cacheable(value = "departments", key = "'tree'")
    public List<DepartmentResponse> getDepartmentTree() {
        List<Department> allDepartments = departmentMapper.selectAllEnabled();
        return buildDepartmentTree(allDepartments, null);
    }

    @Override
    public List<UserResponse> getDepartmentUsers(Long departmentId) {
        if (departmentId == null || departmentId <= 0) {
            throw new ValidationException("部门ID不能为空");
        }

        List<User> users = userMapper.selectByDepartmentId(departmentId);
        return users.stream()
            .map(this::convertToUserResponse)
            .collect(Collectors.toList());
    }

    // ==================== 角色管理 ====================

    @Override
    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        // 参数验证
        ValidationUtil.ValidationResult result = ValidationUtil.createValidationResult();
        ValidationUtil.validateRequired(result, request.getName(), "角色名称");
        ValidationUtil.validateLength(result, request.getName(), "角色名称", 1, 50);
        ValidationUtil.validateRequired(result, request.getCode(), "角色编码");
        ValidationUtil.validateLength(result, request.getCode(), "角色编码", 1, 20);
        
        if (!result.isValid()) {
            Map<String, List<String>> errorMap = new HashMap<>();
            errorMap.put("validation", result.getErrors());
            throw new ValidationException(errorMap);
        }

        // 检查角色编码是否已存在
        if (roleMapper.existsByCode(request.getCode())) {
            throw new BusinessException("角色编码已存在");
        }

        // 创建角色
        Role role = new Role();
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setSort(request.getSortOrder() != null ? request.getSortOrder() : 0);
        role.setStatus(1); // 默认启用
        role.setCreateTime(LocalDateTime.now());
        role.setCreateBy(SecurityUtils.getCurrentUserId());

        roleMapper.insert(role);

        // 分配权限
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            assignPermissionsToRole(role.getId(), request.getPermissionIds());
        }

        // 记录审计日志
        AuditLogUtil.log("CREATE_ROLE", "创建角色: " + role.getName(), role.getId());

        return convertToRoleResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long roleId, RoleUpdateRequest request) {
        // 参数验证
        ValidationUtil.ValidationResult result = ValidationUtil.createValidationResult();
        ValidationUtil.validateRequired(result, roleId, "角色ID");
        ValidationUtil.validateRequired(result, request.getName(), "角色名称");
        ValidationUtil.validateLength(result, request.getName(), "角色名称", 1, 50);
        
        if (!result.isValid()) {
            Map<String, List<String>> errorMap = new HashMap<>();
            errorMap.put("validation", result.getErrors());
            throw new ValidationException(errorMap);
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 更新角色信息
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setSort(request.getSortOrder() != null ? request.getSortOrder() : role.getSort());
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy(SecurityUtils.getCurrentUserId());

        roleMapper.updateById(role);

        // 更新权限分配
        if (request.getPermissionIds() != null) {
            // 先删除原有权限
            roleMapper.deleteRolePermissions(roleId);
            // 重新分配权限
            if (!request.getPermissionIds().isEmpty()) {
                assignPermissionsToRole(roleId, request.getPermissionIds());
            }
        }

        // 记录审计日志
        AuditLogUtil.log("UPDATE_ROLE", "更新角色: " + role.getName(), role.getId());

        return convertToRoleResponse(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        // 参数验证
        if (roleId == null || roleId <= 0) {
            throw new ValidationException("角色ID不能为空");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 检查是否有用户使用该角色
        if (userMapper.countByRoleId(roleId) > 0) {
            throw new BusinessException("角色正在使用中，无法删除");
        }

        // 删除角色权限关联
        roleMapper.deleteRolePermissions(roleId);

        // 删除角色
        roleMapper.deleteById(roleId);

        // 记录审计日志
        AuditLogUtil.log("DELETE_ROLE", "删除角色: " + role.getName(), roleId);
    }

    @Override
    public RoleResponse getRoleById(Long roleId) {
        if (roleId == null || roleId <= 0) {
            throw new ValidationException("角色ID不能为空");
        }

        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        return convertToRoleResponse(role);
    }

    @Override
    public PageResult<RoleResponse> queryRoles(RoleQueryRequest request) {
        // 构建查询条件
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(request.getName())) {
            params.put("name", request.getName());
        }
        if (StringUtils.hasText(request.getCode())) {
            params.put("code", request.getCode());
        }
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            params.put("statuses", request.getStatuses());
        }

        // 查询总数
        long total = roleMapper.countByParams(params);
        
        // 查询数据
        List<Role> roles = roleMapper.selectByParams(params, 
            (request.getPageNum() - 1) * request.getPageSize(), request.getPageSize());

        // 转换响应
        List<RoleResponse> responses = roles.stream()
            .map(this::convertToRoleResponse)
            .collect(Collectors.toList());

        return new PageResult<>(request.getPageNum(), request.getPageSize(), total, responses);
    }

    @Override
    public List<RoleResponse> getAllAvailableRoles() {
        List<Role> roles = roleMapper.selectAllEnabled();
        return roles.stream()
            .map(this::convertToRoleResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 参数验证
        if (roleId == null || roleId <= 0) {
            throw new ValidationException("角色ID不能为空");
        }
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new ValidationException("权限ID列表不能为空");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 检查权限是否存在
        for (Long permissionId : permissionIds) {
            Permission permission = permissionMapper.selectById(permissionId);
            if (permission == null) {
                throw new BusinessException("权限不存在: " + permissionId);
            }
        }

        // 删除原有权限关联
        roleMapper.deleteRolePermissions(roleId);

        // 批量插入新的权限关联
        roleMapper.batchInsertRolePermissions(roleId, permissionIds);

        // 记录审计日志
        AuditLogUtil.log("ASSIGN_PERMISSIONS", "为角色分配权限: " + role.getName(), roleId);
    }

    @Override
    public List<PermissionResponse> getRolePermissions(Long roleId) {
        if (roleId == null || roleId <= 0) {
            throw new ValidationException("角色ID不能为空");
        }

        List<Permission> permissions = permissionMapper.selectByRoleId(roleId);
        return permissions.stream()
            .map(this::convertToPermissionResponse)
            .collect(Collectors.toList());
    }

    // ==================== 权限管理 ====================

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionCreateRequest request) {
        // 参数验证
        ValidationUtil.ValidationResult result = ValidationUtil.createValidationResult();
        ValidationUtil.validateRequired(result, request.getName(), "权限名称");
        ValidationUtil.validateLength(result, request.getName(), "权限名称", 1, 50);
        ValidationUtil.validateRequired(result, request.getCode(), "权限编码");
        ValidationUtil.validateLength(result, request.getCode(), "权限编码", 1, 100);
        
        if (!result.isValid()) {
            Map<String, List<String>> errorMap = new HashMap<>();
            errorMap.put("validation", result.getErrors());
            throw new ValidationException(errorMap);
        }

        // 检查权限编码是否已存在
        if (permissionMapper.existsByCode(request.getCode())) {
            throw new BusinessException("权限编码已存在");
        }

        // 创建权限
        Permission permission = new Permission();
        permission.setPermissionName(request.getName());
        permission.setPermissionCode(request.getCode());
        permission.setPermissionType(request.getType());
        permission.setParentId(request.getParentId());
        permission.setPermissionPath(request.getPath());
        permission.setHttpMethod(request.getMethod());
        permission.setDescription(request.getDescription());
        permission.setSort(request.getSortOrder() != null ? request.getSortOrder() : 0);
        permission.setStatus(1); // 默认启用
        permission.setCreateTime(LocalDateTime.now());
        permission.setCreateBy(SecurityUtils.getCurrentUserId());

        permissionMapper.insert(permission);

        // 记录审计日志
        AuditLogUtil.log("CREATE_PERMISSION", "创建权限: " + permission.getPermissionName(), permission.getId());

        return convertToPermissionResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long permissionId, PermissionUpdateRequest request) {
        // 参数验证
        ValidationUtil.ValidationResult result = ValidationUtil.createValidationResult();
        ValidationUtil.validateRequired(result, permissionId, "权限ID");
        ValidationUtil.validateRequired(result, request.getName(), "权限名称");
        ValidationUtil.validateLength(result, request.getName(), "权限名称", 1, 50);
        
        if (!result.isValid()) {
            Map<String, List<String>> errorMap = new HashMap<>();
            errorMap.put("validation", result.getErrors());
            throw new ValidationException(errorMap);
        }

        // 检查权限是否存在
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        // 更新权限信息
        permission.setPermissionName(request.getName());
        permission.setPermissionType(request.getType());
        permission.setParentId(request.getParentId());
        permission.setPermissionPath(request.getPath());
        permission.setHttpMethod(request.getMethod());
        permission.setDescription(request.getDescription());
        permission.setSort(request.getSortOrder() != null ? request.getSortOrder() : permission.getSort());
        permission.setUpdateTime(LocalDateTime.now());
        permission.setUpdateBy(SecurityUtils.getCurrentUserId());

        permissionMapper.updateById(permission);

        // 记录审计日志
        AuditLogUtil.log("UPDATE_PERMISSION", "更新权限: " + permission.getPermissionName(), permission.getId());

        return convertToPermissionResponse(permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long permissionId) {
        // 参数验证
        if (permissionId == null || permissionId <= 0) {
            throw new ValidationException("权限ID不能为空");
        }

        // 检查权限是否存在
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        // 检查是否有子权限
        if (permissionMapper.countByParentId(permissionId) > 0) {
            throw new BusinessException("存在子权限，无法删除");
        }

        // 检查是否有角色使用该权限
        if (roleMapper.countByPermissionId(permissionId) > 0) {
            throw new BusinessException("权限正在使用中，无法删除");
        }

        // 删除权限
        permissionMapper.deleteById(permissionId);

        // 记录审计日志
        AuditLogUtil.log("DELETE_PERMISSION", "删除权限: " + permission.getPermissionName(), permissionId);
    }

    @Override
    public PermissionResponse getPermissionById(Long permissionId) {
        if (permissionId == null || permissionId <= 0) {
            throw new ValidationException("权限ID不能为空");
        }

        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        return convertToPermissionResponse(permission);
    }

    @Override
    public PageResult<PermissionResponse> queryPermissions(PermissionQueryRequest request) {
        // 构建查询条件
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(request.getName())) {
            params.put("name", request.getName());
        }
        if (StringUtils.hasText(request.getCode())) {
            params.put("code", request.getCode());
        }
        if (StringUtils.hasText(request.getType())) {
            params.put("type", request.getType());
        }
        if (request.getParentId() != null) {
            params.put("parentId", request.getParentId());
        }
        if (request.getStatus() != null) {
            params.put("status", request.getStatus());
        }

        // 查询总数
        long total = permissionMapper.countByParams(params);
        
        // 查询数据
        List<Permission> permissions = permissionMapper.selectByParams(params, 
            (request.getPageNum() - 1) * request.getPageSize(), request.getPageSize());

        // 转换响应
        List<PermissionResponse> responses = permissions.stream()
            .map(this::convertToPermissionResponse)
            .collect(Collectors.toList());

        return new PageResult<>(request.getPageNum(), request.getPageSize(), total, responses);
    }

    @Override
    @Cacheable(value = "permissions", key = "'tree'")
    public Result<List<PermissionTreeNode>> getPermissionTree() {
        try {
            List<Permission> allPermissions = permissionMapper.selectAllEnabled();
            List<PermissionTreeNode> tree = buildPermissionTree(allPermissions, null);
            return Result.success(tree);
        } catch (Exception e) {
            return Result.error("获取权限树失败: " + e.getMessage());
        }
    }

    @Override
    public List<PermissionResponse> getUserPermissions(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("用户ID不能为空");
        }

        List<Permission> permissions = permissionMapper.selectByUserId(userId);
        return permissions.stream()
            .map(this::convertToPermissionResponse)
            .collect(Collectors.toList());
    }

    // ==================== 系统配置管理 ====================

    @Override
    @Transactional
    public SystemConfigResponse createSystemConfig(SystemConfigCreateRequest request) {
        // 参数验证
        ValidationUtil.ValidationResult result = ValidationUtil.createValidationResult();
        ValidationUtil.validateRequired(result, request.getConfigKey(), "配置键");
        ValidationUtil.validateLength(result, request.getConfigKey(), "配置键", 1, 100);
        ValidationUtil.validateRequired(result, request.getConfigValue(), "配置值");
        
        if (!result.isValid()) {
            Map<String, List<String>> errorMap = new HashMap<>();
            errorMap.put("validation", result.getErrors());
            throw new ValidationException(errorMap);
        }

        // 检查配置键是否已存在
        if (systemConfigMapper.existsByKey(request.getConfigKey())) {
            throw new BusinessException("配置键已存在");
        }

        // 创建配置
        SystemConfig config = new SystemConfig();
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigGroup(request.getConfigGroup());
        config.setDescription(request.getDescription());
        config.setIsSystem(request.getIsSystem() != null ? request.getIsSystem() : false);
        config.setStatus(1); // 默认启用
        config.setCreateTime(LocalDateTime.now());
        config.setCreateBy(SecurityUtils.getCurrentUserId());

        systemConfigMapper.insert(config);

        // 记录审计日志
        AuditLogUtil.log("CREATE_CONFIG", "创建系统配置: " + config.getConfigKey(), config.getId());

        return convertToSystemConfigResponse(config);
    }

    @Override
    @Transactional
    public SystemConfigResponse updateSystemConfig(Long configId, SystemConfigUpdateRequest request) {
        // 参数验证
        ValidationUtil.ValidationResult result = ValidationUtil.createValidationResult();
        ValidationUtil.validateRequired(result, configId, "配置ID");
        ValidationUtil.validateRequired(result, request.getConfigValue(), "配置值");
        
        if (!result.isValid()) {
            Map<String, List<String>> errorMap = new HashMap<>();
            errorMap.put("validation", result.getErrors());
            throw new ValidationException(errorMap);
        }

        // 检查配置是否存在
        SystemConfig config = systemConfigMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }

        // 更新配置信息
        config.setConfigValue(request.getConfigValue());
        config.setConfigGroup(request.getConfigGroup());
        config.setDescription(request.getDescription());
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdateBy(SecurityUtils.getCurrentUserId());

        systemConfigMapper.updateById(config);

        // 记录审计日志
        AuditLogUtil.log("UPDATE_CONFIG", "更新系统配置: " + config.getConfigKey(), config.getId());

        return convertToSystemConfigResponse(config);
    }

    @Override
    @Transactional
    public void deleteSystemConfig(Long configId) {
        // 参数验证
        if (configId == null || configId <= 0) {
            throw new ValidationException("配置ID不能为空");
        }

        // 检查配置是否存在
        SystemConfig config = systemConfigMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }

        // 检查是否为系统配置
        if (config.getIsSystem()) {
            throw new BusinessException("系统配置不能删除");
        }

        // 删除配置
        systemConfigMapper.deleteById(configId);

        // 记录审计日志
        AuditLogUtil.log("DELETE_CONFIG", "删除系统配置: " + config.getConfigKey(), configId);
    }

    @Override
    public String getConfigValue(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            throw new ValidationException("配置键不能为空");
        }

        SystemConfig config = systemConfigMapper.selectByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public SystemConfigResponse getConfigByKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            throw new ValidationException("配置键不能为空");
        }

        SystemConfig config = systemConfigMapper.selectByKey(configKey);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }

        return convertToSystemConfigResponse(config);
    }

    @Override
    public PageResult<SystemConfigResponse> querySystemConfigs(SystemConfigQueryRequest request) {
        // 构建查询条件
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(request.getConfigKey())) {
            params.put("configKey", request.getConfigKey());
        }
        if (StringUtils.hasText(request.getConfigGroup())) {
            params.put("configGroup", request.getConfigGroup());
        }
        if (request.getIsSystem() != null) {
            params.put("isSystem", request.getIsSystem());
        }
        if (request.getStatus() != null) {
            params.put("status", request.getStatus());
        }

        // 查询总数
        long total = systemConfigMapper.countByParams(params);
        
        // 查询数据
        List<SystemConfig> configs = systemConfigMapper.selectByParams(params, 
            (request.getPageNum() - 1) * request.getPageSize(), request.getPageSize());

        // 转换响应
        List<SystemConfigResponse> responses = configs.stream()
            .map(this::convertToSystemConfigResponse)
            .collect(Collectors.toList());

        return new PageResult<>(request.getPageNum(), request.getPageSize(), total, responses);
    }

    @Override
    @Transactional
    public void batchUpdateConfigs(Map<String, String> configs) {
        if (configs == null || configs.isEmpty()) {
            throw new ValidationException("配置不能为空");
        }

        for (Map.Entry<String, String> entry : configs.entrySet()) {
            SystemConfig config = systemConfigMapper.selectByKey(entry.getKey());
            if (config != null) {
                config.setConfigValue(entry.getValue());
                config.setUpdateTime(LocalDateTime.now());
                config.setUpdateBy(SecurityUtils.getCurrentUserId());
                systemConfigMapper.updateById(config);
            }
        }

        // 记录审计日志
        AuditLogUtil.log("BATCH_UPDATE_CONFIG", "批量更新系统配置", null);
    }

    @Override
    public List<SystemConfigResponse> getConfigsByGroup(String group) {
        if (!StringUtils.hasText(group)) {
            throw new ValidationException("配置组不能为空");
        }

        List<SystemConfig> configs = systemConfigMapper.selectByGroup(group);
        return configs.stream()
            .map(this::convertToSystemConfigResponse)
            .collect(Collectors.toList());
    }

    // ==================== 统计信息 ====================

    @Override
    public SystemStatistics getSystemStatistics() {
        Long totalUsers = userMapper.countTotal();
        Long totalDepartments = departmentMapper.countTotal();
        Long totalRoles = roleMapper.countTotal();
        Long totalPermissions = permissionMapper.countTotal();
        Long totalArchives = archiveMapper.countTotal();
        Long totalBorrows = borrowRecordMapper.countTotal();
        Long activeUsers = userMapper.countActive();
        Long onlineUsers = userMapper.countOnline();

        return new SystemStatistics(totalUsers, totalDepartments, totalRoles, 
            totalPermissions, totalArchives, totalBorrows, activeUsers, onlineUsers);
    }

    @Override
    public List<UserActivityStatistics> getUserActivityStatistics(int days) {
        if (days <= 0 || days > 365) {
            throw new ValidationException("天数必须在1-365之间");
        }

        return userMapper.selectActivityStatistics(days);
    }

    @Override
    public List<DepartmentStatistics> getDepartmentStatistics() {
        return departmentMapper.selectDepartmentStatistics();
    }

    // ==================== 私有辅助方法 ====================

    private boolean isCircularReference(Long departmentId, Long parentId) {
        if (parentId == null) {
            return false;
        }
        
        Set<Long> visited = new HashSet<>();
        Long currentId = parentId;
        
        while (currentId != null && !visited.contains(currentId)) {
            if (currentId.equals(departmentId)) {
                return true;
            }
            visited.add(currentId);
            Department parent = departmentMapper.selectById(currentId);
            currentId = parent != null ? parent.getParentId() : null;
        }
        
        return false;
    }

    private List<DepartmentResponse> buildDepartmentTree(List<Department> departments, Long parentId) {
        return departments.stream()
            .filter(dept -> Objects.equals(dept.getParentId(), parentId))
            .map(dept -> {
                DepartmentResponse response = convertToDepartmentResponse(dept);
                response.setChildren(buildDepartmentTree(departments, dept.getId()));
                return response;
            })
            .collect(Collectors.toList());
    }

    private List<PermissionTreeNode> buildPermissionTree(List<Permission> permissions, Long parentId) {
        return permissions.stream()
            .filter(perm -> Objects.equals(perm.getParentId(), parentId))
            .map(perm -> {
                PermissionTreeNode node = new PermissionTreeNode();
                node.setId(perm.getId());
                node.setName(perm.getName());
                node.setCode(perm.getCode());
                node.setType(perm.getType());
                node.setPath(perm.getPath());
                node.setMethod(perm.getMethod());
                node.setChildren(buildPermissionTree(permissions, perm.getId()));
                return node;
            })
            .collect(Collectors.toList());
    }

    private DepartmentResponse convertToDepartmentResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setCode(department.getCode());
        response.setParentId(department.getParentId());
        response.setDescription(department.getDescription());
        response.setSort(department.getSort());
        response.setStatus(department.getStatus());
        response.setCreateTime(department.getCreateTime());
        response.setUpdateTime(department.getUpdateTime());
        return response;
    }

    private RoleResponse convertToRoleResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setCode(role.getCode());
        response.setDescription(role.getDescription());
        response.setSort(role.getSort());
        response.setStatus(role.getStatus());
        response.setCreateTime(role.getCreateTime());
        response.setUpdateTime(role.getUpdateTime());
        return response;
    }

    private PermissionResponse convertToPermissionResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setName(permission.getPermissionName());
        response.setCode(permission.getPermissionCode());
        response.setType(permission.getPermissionType());
        response.setParentId(permission.getParentId());
        response.setPath(permission.getPermissionPath());
        response.setMethod(permission.getHttpMethod());
        response.setDescription(permission.getDescription());
        response.setSort(permission.getSort());
        response.setStatus(permission.getStatus());
        response.setCreateTime(permission.getCreateTime());
        response.setUpdateTime(permission.getUpdateTime());
        return response;
    }

    private SystemConfigResponse convertToSystemConfigResponse(SystemConfig config) {
        SystemConfigResponse response = new SystemConfigResponse();
        response.setId(config.getId());
        response.setConfigKey(config.getConfigKey());
        response.setConfigValue(config.getConfigValue());
        response.setConfigGroup(config.getConfigGroup());
        response.setDescription(config.getDescription());
        response.setIsSystem(config.getIsSystem());
        response.setStatus(config.getStatus());
        response.setCreateTime(config.getCreateTime());
        response.setUpdateTime(config.getUpdateTime());
        return response;
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setDepartmentId(user.getDepartmentId());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        return response;
    }
}