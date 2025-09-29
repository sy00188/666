package com.archive.management.service;

import com.archive.management.dto.*;
import com.archive.management.entity.Department;
import com.archive.management.entity.User;
import com.archive.management.common.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 部门业务服务接口
 * 定义部门管理的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface DepartmentService {

    // ==================== 部门CRUD操作 ====================

    /**
     * 创建部门
     * @param request 创建部门请求
     * @return 部门响应信息
     */
    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    /**
     * 根据ID获取部门
     * @param id 部门ID
     * @return 部门信息
     */
    Department getDepartmentById(Long id);

    /**
     * 根据ID获取部门响应信息
     * @param id 部门ID
     * @return 部门响应信息
     */
    DepartmentResponse getDepartmentResponseById(Long id);

    /**
     * 根据部门编码获取部门
     * @param code 部门编码
     * @return 部门信息
     */
    Department getDepartmentByCode(String code);

    /**
     * 根据部门名称获取部门
     * @param name 部门名称
     * @return 部门信息
     */
    Department getDepartmentByName(String name);

    /**
     * 更新部门信息
     * @param id 部门ID
     * @param request 更新部门请求
     * @return 部门响应信息
     */
    DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request);

    /**
     * 删除部门（软删除）
     * @param id 部门ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteDepartment(Long id, Long deletedBy);

    /**
     * 恢复已删除的部门
     * @param id 部门ID
     * @return 是否恢复成功
     */
    boolean restoreDepartment(Long id);

    /**
     * 批量删除部门
     * @param ids 部门ID列表
     * @param deletedBy 删除人ID
     * @return 删除成功的数量
     */
    int batchDeleteDepartments(List<Long> ids, Long deletedBy);

    // ==================== 部门查询操作 ====================

    /**
     * 分页查询部门列表
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    Page<DepartmentResponse> getDepartmentPage(Pageable pageable);

    /**
     * 分页查询部门
     * @param request 查询条件
     * @param pageable 分页参数
     * @return 分页结果
     */
    PageResult<DepartmentResponse> searchDepartments(DepartmentSearchRequest request, Pageable pageable);

    /**
     * 获取所有根部门
     * @return 根部门列表
     */
    List<DepartmentResponse> getRootDepartments();

    /**
     * 获取指定部门的子部门列表
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<DepartmentResponse> getChildDepartments(Long parentId);

    /**
     * 获取部门树形结构
     * @return 部门树形结构
     */
    List<DepartmentTreeResponse> getDepartmentTree();

    /**
     * 获取指定部门的部门树
     * @param rootId 根部门ID
     * @return 部门树形结构
     */
    List<DepartmentTreeResponse> getDepartmentTree(Long rootId);

    // ==================== 部门层级关系管理 ====================

    /**
     * 获取部门的所有祖先部门
     * @param departmentId 部门ID
     * @return 祖先部门列表
     */
    List<DepartmentResponse> getAncestors(Long departmentId);

    /**
     * 获取部门的所有后代部门
     * @param departmentId 部门ID
     * @return 后代部门列表
     */
    List<DepartmentResponse> getDescendants(Long departmentId);

    /**
     * 移动部门到新的父部门下
     * @param departmentId 部门ID
     * @param newParentId 新父部门ID
     * @param updatedBy 更新人ID
     * @return 是否移动成功
     */
    boolean moveDepartment(Long departmentId, Long newParentId, Long updatedBy);

    /**
     * 调整部门排序
     * @param departmentId 部门ID
     * @param newSortOrder 新排序号
     * @param updatedBy 更新人ID
     * @return 是否调整成功
     */
    boolean adjustDepartmentSort(Long departmentId, Integer newSortOrder, Long updatedBy);

    // ==================== 部门成员管理 ====================

    /**
     * 获取部门成员列表
     * @param departmentId 部门ID
     * @param pageable 分页参数
     * @return 成员分页列表
     */
    Page<UserResponse> getDepartmentMembers(Long departmentId, Pageable pageable);

    /**
     * 获取部门成员数量
     * @param departmentId 部门ID
     * @return 成员数量
     */
    long getDepartmentMemberCount(Long departmentId);

    /**
     * 获取部门及其子部门的总成员数量
     * @param departmentId 部门ID
     * @return 总成员数量
     */
    long getTotalMemberCount(Long departmentId);

    /**
     * 设置部门负责人
     * @param departmentId 部门ID
     * @param managerId 负责人ID
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setDepartmentManager(Long departmentId, Long managerId, Long updatedBy);

    /**
     * 批量分配用户到部门
     * @param departmentId 部门ID
     * @param userIds 用户ID列表
     * @param assignedBy 分配人ID
     * @return 分配成功的数量
     */
    int assignUsersToDepart(Long departmentId, List<Long> userIds, Long assignedBy);

    // ==================== 部门状态管理 ====================

    /**
     * 启用部门
     * @param departmentId 部门ID
     * @param updatedBy 更新人ID
     * @return 是否启用成功
     */
    boolean enableDepartment(Long departmentId, Long updatedBy);

    /**
     * 禁用部门
     * @param departmentId 部门ID
     * @param updatedBy 更新人ID
     * @return 是否禁用成功
     */
    boolean disableDepartment(Long departmentId, Long updatedBy);

    /**
     * 批量更新部门状态
     * @param departmentIds 部门ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdateDepartmentStatus(List<Long> departmentIds, Integer status, Long updatedBy);

    // ==================== 部门验证操作 ====================

    /**
     * 检查部门编码是否存在
     * @param code 部门编码
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查部门名称是否存在
     * @param name 部门名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查部门编码是否存在（排除指定ID）
     * @param code 部门编码
     * @param excludeId 排除的部门ID
     * @return 是否存在
     */
    boolean existsByCodeExcludeId(String code, Long excludeId);

    /**
     * 检查部门名称是否存在（排除指定ID）
     * @param name 部门名称
     * @param excludeId 排除的部门ID
     * @return 是否存在
     */
    boolean existsByNameExcludeId(String name, Long excludeId);

    /**
     * 检查部门是否有子部门
     * @param departmentId 部门ID
     * @return 是否有子部门
     */
    boolean hasChildren(Long departmentId);

    /**
     * 检查部门是否有用户
     * @param departmentId 部门ID
     * @return 是否有用户
     */
    boolean hasUsers(Long departmentId);

    /**
     * 验证部门层级关系是否合法
     * @param departmentId 部门ID
     * @param parentId 父部门ID
     * @return 是否合法
     */
    boolean validateDepartmentHierarchy(Long departmentId, Long parentId);

    // ==================== 部门统计操作 ====================

    /**
     * 获取部门统计信息
     * @return 统计信息
     */
    Map<String, Object> getDepartmentStatistics();

    /**
     * 按级别统计部门数量
     * @return 级别统计结果
     */
    Map<Integer, Long> countDepartmentsByLevel();

    /**
     * 按状态统计部门数量
     * @return 状态统计结果
     */
    Map<Integer, Long> countDepartmentsByStatus();

    /**
     * 按类型统计部门数量
     * @return 类型统计结果
     */
    Map<String, Long> countDepartmentsByType();

    /**
     * 获取部门创建趋势
     * @param days 天数
     * @return 创建趋势数据
     */
    List<Map<String, Object>> getDepartmentCreationTrend(int days);

    // ==================== 部门导入导出 ====================

    /**
     * 导出部门数据
     * @param departmentIds 部门ID列表（为空则导出所有）
     * @param format 导出格式
     * @param includeFields 包含字段
     * @return 导出结果
     */
    Map<String, Object> exportDepartments(List<Long> departmentIds, String format, List<String> includeFields);

    /**
     * 导入部门数据
     * @param departments 部门列表
     * @param importMode 导入模式
     * @param importedBy 导入人ID
     * @return 导入结果
     */
    Map<String, Object> importDepartments(List<Department> departments, String importMode, Long importedBy);

    /**
     * 批量导入部门
     * @param departments 部门列表
     * @param createdBy 创建人ID
     * @return 导入成功的数量
     */
    int batchImportDepartments(List<Department> departments, Long createdBy);

    // ==================== 部门权限管理 ====================

    /**
     * 获取部门权限列表
     * @param departmentId 部门ID
     * @return 权限列表
     */
    List<String> getDepartmentPermissions(Long departmentId);

    /**
     * 为部门分配权限
     * @param departmentId 部门ID
     * @param permissionIds 权限ID列表
     * @param assignedBy 分配人ID
     * @return 分配成功的数量
     */
    int assignPermissionsToDepartment(Long departmentId, List<Long> permissionIds, Long assignedBy);

    /**
     * 检查部门是否有指定权限
     * @param departmentId 部门ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long departmentId, String permission);

    // ==================== 部门通知操作 ====================

    /**
     * 向部门发送通知
     * @param departmentId 部门ID
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return 是否发送成功
     */
    boolean sendNotificationToDepartment(Long departmentId, String title, String content, String type);

    /**
     * 生成部门报告
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告数据
     */
    Map<String, Object> generateDepartmentReport(LocalDateTime startDate, LocalDateTime endDate);
}