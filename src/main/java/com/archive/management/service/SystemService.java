package com.archive.management.service;

import com.archive.management.dto.request.*;
import com.archive.management.dto.response.*;
import com.archive.management.entity.*;
import com.archive.management.common.PageResult;
import com.archive.management.common.PermissionTreeNode;
import com.archive.management.common.Result;
import java.util.List;
import java.util.Map;

/**
 * 系统管理服务接口
 * 提供部门、角色、权限、配置管理等支撑服务
 */
public interface SystemService {

    // ==================== 部门管理 ====================
    
    /**
     * 创建部门
     * @param request 部门创建请求
     * @return 部门响应信息
     */
    DepartmentResponse createDepartment(DepartmentCreateRequest request);
    
    /**
     * 更新部门信息
     * @param departmentId 部门ID
     * @param request 部门更新请求
     * @return 部门响应信息
     */
    DepartmentResponse updateDepartment(Long departmentId, DepartmentUpdateRequest request);
    
    /**
     * 删除部门
     * @param departmentId 部门ID
     */
    void deleteDepartment(Long departmentId);
    
    /**
     * 根据ID获取部门信息
     * @param departmentId 部门ID
     * @return 部门响应信息
     */
    DepartmentResponse getDepartmentById(Long departmentId);
    
    /**
     * 分页查询部门列表
     * @param request 查询请求
     * @return 分页部门列表
     */
    PageResult<DepartmentResponse> queryDepartments(DepartmentQueryRequest request);
    
    /**
     * 获取部门树形结构
     * @return 部门树形列表
     */
    List<DepartmentResponse> getDepartmentTree();
    
    /**
     * 获取部门用户列表
     * @param departmentId 部门ID
     * @return 用户列表
     */
    List<UserResponse> getDepartmentUsers(Long departmentId);

    // ==================== 角色管理 ====================
    
    /**
     * 创建角色
     * @param request 角色创建请求
     * @return 角色响应信息
     */
    RoleResponse createRole(RoleCreateRequest request);
    
    /**
     * 更新角色信息
     * @param roleId 角色ID
     * @param request 角色更新请求
     * @return 角色响应信息
     */
    RoleResponse updateRole(Long roleId, RoleUpdateRequest request);
    
    /**
     * 删除角色
     * @param roleId 角色ID
     */
    void deleteRole(Long roleId);
    
    /**
     * 根据ID获取角色信息
     * @param roleId 角色ID
     * @return 角色响应信息
     */
    RoleResponse getRoleById(Long roleId);
    
    /**
     * 分页查询角色列表
     * @param request 查询请求
     * @return 分页角色列表
     */
    PageResult<RoleResponse> queryRoles(RoleQueryRequest request);
    
    /**
     * 获取所有可用角色
     * @return 角色列表
     */
    List<RoleResponse> getAllAvailableRoles();
    
    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);
    
    /**
     * 获取角色的权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<PermissionResponse> getRolePermissions(Long roleId);

    // ==================== 权限管理 ====================
    
    /**
     * 创建权限
     * @param request 权限创建请求
     * @return 权限响应信息
     */
    PermissionResponse createPermission(PermissionCreateRequest request);
    
    /**
     * 更新权限信息
     * @param permissionId 权限ID
     * @param request 权限更新请求
     * @return 权限响应信息
     */
    PermissionResponse updatePermission(Long permissionId, PermissionUpdateRequest request);
    
    /**
     * 删除权限
     * @param permissionId 权限ID
     */
    void deletePermission(Long permissionId);
    
    /**
     * 根据ID获取权限信息
     * @param permissionId 权限ID
     * @return 权限响应信息
     */
    PermissionResponse getPermissionById(Long permissionId);
    
    /**
     * 分页查询权限列表
     * @param request 查询请求
     * @return 分页权限列表
     */
    PageResult<PermissionResponse> queryPermissions(PermissionQueryRequest request);
    
    /**
     * 获取权限树形列表
     * @return 权限树形列表
     */
    Result<List<PermissionTreeNode>> getPermissionTree();
    
    /**
     * 根据用户ID获取用户权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<PermissionResponse> getUserPermissions(Long userId);

    // ==================== 系统配置管理 ====================
    
    /**
     * 创建系统配置
     * @param request 配置创建请求
     * @return 配置响应信息
     */
    ConfigResponse createSystemConfig(ConfigCreateRequest request);
    
    /**
     * 更新系统配置
     * @param configId 配置ID
     * @param request 配置更新请求
     * @return 配置响应信息
     */
    ConfigResponse updateSystemConfig(Long configId, ConfigUpdateRequest request);
    
    /**
     * 删除系统配置
     * @param configId 配置ID
     */
    void deleteSystemConfig(Long configId);
    
    /**
     * 根据配置键获取配置值
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 根据配置键获取配置信息
     * @param configKey 配置键
     * @return 配置响应信息
     */
    ConfigResponse getConfigByKey(String configKey);
    
    /**
     * 分页查询系统配置
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<ConfigResponse> querySystemConfigs(ConfigQueryRequest request);
    
    /**
     * 批量更新配置
     * @param configs 配置键值对
     */
    void batchUpdateConfigs(Map<String, String> configs);
    
    /**
     * 根据分组获取配置列表
     * @param group 配置分组
     * @return 配置列表
     */
    List<ConfigResponse> getConfigsByGroup(String group);

    // ==================== 系统统计 ====================
    
    /**
     * 获取系统统计信息
     * @return 系统统计数据
     */
    SystemStatistics getSystemStatistics();
    
    /**
     * 获取用户活跃度统计
     * @param days 统计天数
     * @return 用户活跃度数据
     */
    List<UserActivityStatistics> getUserActivityStatistics(int days);
    
    /**
     * 获取部门统计信息
     * @return 部门统计数据
     */
    List<DepartmentStatistics> getDepartmentStatistics();

    // ==================== 内部类定义 ====================
    
    /**
     * 系统统计信息
     */
    class SystemStatistics {
        private Long totalUsers;
        private Long totalDepartments;
        private Long totalRoles;
        private Long totalPermissions;
        private Long totalArchives;
        private Long totalBorrows;
        private Long activeUsers;
        private Long onlineUsers;
        
        // 构造函数、getter和setter方法
        public SystemStatistics() {}
        
        public SystemStatistics(Long totalUsers, Long totalDepartments, Long totalRoles, 
                               Long totalPermissions, Long totalArchives, Long totalBorrows,
                               Long activeUsers, Long onlineUsers) {
            this.totalUsers = totalUsers;
            this.totalDepartments = totalDepartments;
            this.totalRoles = totalRoles;
            this.totalPermissions = totalPermissions;
            this.totalArchives = totalArchives;
            this.totalBorrows = totalBorrows;
            this.activeUsers = activeUsers;
            this.onlineUsers = onlineUsers;
        }
        
        // Getter和Setter方法
        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
        
        public Long getTotalDepartments() { return totalDepartments; }
        public void setTotalDepartments(Long totalDepartments) { this.totalDepartments = totalDepartments; }
        
        public Long getTotalRoles() { return totalRoles; }
        public void setTotalRoles(Long totalRoles) { this.totalRoles = totalRoles; }
        
        public Long getTotalPermissions() { return totalPermissions; }
        public void setTotalPermissions(Long totalPermissions) { this.totalPermissions = totalPermissions; }
        
        public Long getTotalArchives() { return totalArchives; }
        public void setTotalArchives(Long totalArchives) { this.totalArchives = totalArchives; }
        
        public Long getTotalBorrows() { return totalBorrows; }
        public void setTotalBorrows(Long totalBorrows) { this.totalBorrows = totalBorrows; }
        
        public Long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }
        
        public Long getOnlineUsers() { return onlineUsers; }
        public void setOnlineUsers(Long onlineUsers) { this.onlineUsers = onlineUsers; }
        
        @Override
        public String toString() {
            return "SystemStatistics{" +
                    "totalUsers=" + totalUsers +
                    ", totalDepartments=" + totalDepartments +
                    ", totalRoles=" + totalRoles +
                    ", totalPermissions=" + totalPermissions +
                    ", totalArchives=" + totalArchives +
                    ", totalBorrows=" + totalBorrows +
                    ", activeUsers=" + activeUsers +
                    ", onlineUsers=" + onlineUsers +
                    '}';
        }
    }
    
    /**
     * 用户活跃度统计
     */
    class UserActivityStatistics {
        private String date;
        private Long activeCount;
        private Long loginCount;
        private Long newUserCount;
        
        public UserActivityStatistics() {}
        
        public UserActivityStatistics(String date, Long activeCount, Long loginCount, Long newUserCount) {
            this.date = date;
            this.activeCount = activeCount;
            this.loginCount = loginCount;
            this.newUserCount = newUserCount;
        }
        
        // Getter和Setter方法
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public Long getActiveCount() { return activeCount; }
        public void setActiveCount(Long activeCount) { this.activeCount = activeCount; }
        
        public Long getLoginCount() { return loginCount; }
        public void setLoginCount(Long loginCount) { this.loginCount = loginCount; }
        
        public Long getNewUserCount() { return newUserCount; }
        public void setNewUserCount(Long newUserCount) { this.newUserCount = newUserCount; }
        
        @Override
        public String toString() {
            return "UserActivityStatistics{" +
                    "date='" + date + '\'' +
                    ", activeCount=" + activeCount +
                    ", loginCount=" + loginCount +
                    ", newUserCount=" + newUserCount +
                    '}';
        }
    }
    
    /**
     * 部门统计信息
     */
    class DepartmentStatistics {
        private Long departmentId;
        private String departmentName;
        private Long userCount;
        private Long archiveCount;
        private Long borrowCount;
        
        public DepartmentStatistics() {}
        
        public DepartmentStatistics(Long departmentId, String departmentName, 
                                   Long userCount, Long archiveCount, Long borrowCount) {
            this.departmentId = departmentId;
            this.departmentName = departmentName;
            this.userCount = userCount;
            this.archiveCount = archiveCount;
            this.borrowCount = borrowCount;
        }
        
        // Getter和Setter方法
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
        
        public Long getUserCount() { return userCount; }
        public void setUserCount(Long userCount) { this.userCount = userCount; }
        
        public Long getArchiveCount() { return archiveCount; }
        public void setArchiveCount(Long archiveCount) { this.archiveCount = archiveCount; }
        
        public Long getBorrowCount() { return borrowCount; }
        public void setBorrowCount(Long borrowCount) { this.borrowCount = borrowCount; }
        
        @Override
        public String toString() {
            return "DepartmentStatistics{" +
                    "departmentId=" + departmentId +
                    ", departmentName='" + departmentName + '\'' +
                    ", userCount=" + userCount +
                    ", archiveCount=" + archiveCount +
                    ", borrowCount=" + borrowCount +
                    '}';
        }
    }
}