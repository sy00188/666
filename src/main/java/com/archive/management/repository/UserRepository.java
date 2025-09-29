package com.archive.management.repository;

import com.archive.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层接口
 * 提供用户实体的数据库操作方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱地址
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户信息
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据员工编号查找用户
     * @param employeeId 员工编号
     * @return 用户信息
     */
    Optional<User> findByEmployeeId(String employeeId);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱地址
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查员工编号是否存在
     * @param employeeId 员工编号
     * @return 是否存在
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * 根据部门ID查找用户列表
     * @param departmentId 部门ID
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByDepartmentId(Long departmentId, Pageable pageable);

    /**
     * 根据角色ID查找用户列表
     * @param roleId 角色ID
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    Page<User> findByRoleId(@Param("roleId") Long roleId, Pageable pageable);

    /**
     * 根据用户状态查找用户列表
     * @param status 用户状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据用户类型查找用户列表
     * @param userType 用户类型
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByUserType(Integer userType, Pageable pageable);

    /**
     * 模糊查询用户（根据用户名、真实姓名、邮箱）
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR u.email LIKE %:keyword%")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查找指定时间范围内创建的用户
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找指定时间范围内最后登录的用户
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByLastLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找长时间未登录的用户
     * @param lastLoginTime 最后登录时间阈值
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByLastLoginTimeBefore(LocalDateTime lastLoginTime, Pageable pageable);

    /**
     * 统计用户总数
     * @return 用户总数
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    /**
     * 统计指定状态的用户数量
     * @param status 用户状态
     * @return 用户数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定部门的用户数量
     * @param departmentId 部门ID
     * @return 用户数量
     */
    long countByDepartmentId(Long departmentId);

    /**
     * 统计指定角色的用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countByRoleId(@Param("roleId") Long roleId);

    /**
     * 统计指定时间范围内注册的用户数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户数量
     */
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找所有管理员用户
     * @return 管理员用户列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.code = 'ADMIN'")
    List<User> findAllAdmins();

    /**
     * 查找指定部门的所有用户
     * @param departmentId 部门ID
     * @return 用户列表
     */
    List<User> findByDepartmentId(Long departmentId);

    /**
     * 根据用户ID列表批量查询用户
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    List<User> findByIdIn(List<Long> userIds);

    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     * @param lastLoginTime 最后登录时间
     * @param lastLoginIp 最后登录IP
     */
    @Query("UPDATE User u SET u.lastLoginTime = :lastLoginTime, u.lastLoginIp = :lastLoginIp WHERE u.id = :userId")
    void updateLastLoginInfo(@Param("userId") Long userId, 
                           @Param("lastLoginTime") LocalDateTime lastLoginTime,
                           @Param("lastLoginIp") String lastLoginIp);

    /**
     * 批量更新用户状态
     * @param userIds 用户ID列表
     * @param status 新状态
     */
    @Query("UPDATE User u SET u.status = :status WHERE u.id IN :userIds")
    void batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Integer status);

    /**
     * 软删除用户（更新删除标记）
     * @param userId 用户ID
     * @param deletedBy 删除人ID
     * @param deleteTime 删除时间
     */
    @Query("UPDATE User u SET u.deleted = true, u.deletedBy = :deletedBy, u.deleteTime = :deleteTime WHERE u.id = :userId")
    void softDeleteUser(@Param("userId") Long userId, 
                       @Param("deletedBy") Long deletedBy, 
                       @Param("deleteTime") LocalDateTime deleteTime);

    /**
     * 查找未删除的用户
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByDeletedFalse(Pageable pageable);

    /**
     * 根据用户名查找未删除的用户
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsernameAndDeletedFalse(String username);

    /**
     * 根据邮箱查找未删除的用户
     * @param email 邮箱地址
     * @return 用户信息
     */
    Optional<User> findByEmailAndDeletedFalse(String email);
}