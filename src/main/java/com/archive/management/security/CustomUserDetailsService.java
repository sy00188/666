package com.archive.management.security;

import com.archive.management.entity.User;
import com.archive.management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户名加载用户详情
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("正在加载用户详情: {}", username);

        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> {
                    logger.warn("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });

        // 检查用户状态
        if (!user.getEnabled()) {
            logger.warn("用户已被禁用: {}", username);
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        // 构建用户权限
        Collection<GrantedAuthority> authorities = buildUserAuthorities(user);

        logger.debug("用户 {} 加载成功，权限数量: {}", username, authorities.size());

        return new CustomUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRealName(),
                user.getPhone(),
                user.getDepartmentId(),
                user.getRoleId(),
                user.getEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities,
                user.getLastLoginTime(),
                user.getPasswordUpdateTime()
        );
    }

    /**
     * 根据用户ID加载用户详情
     * @param userId 用户ID
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        logger.debug("正在根据ID加载用户详情: {}", userId);

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> {
                    logger.warn("用户不存在，ID: {}", userId);
                    return new UsernameNotFoundException("用户不存在，ID: " + userId);
                });

        // 检查用户状态
        if (!user.getEnabled()) {
            logger.warn("用户已被禁用，ID: {}", userId);
            throw new UsernameNotFoundException("用户已被禁用，ID: " + userId);
        }

        // 构建用户权限
        Collection<GrantedAuthority> authorities = buildUserAuthorities(user);

        logger.debug("用户 {} 加载成功，权限数量: {}", user.getUsername(), authorities.size());

        return new CustomUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRealName(),
                user.getPhone(),
                user.getDepartmentId(),
                user.getRoleId(),
                user.getEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities,
                user.getLastLoginTime(),
                user.getPasswordUpdateTime()
        );
    }

    /**
     * 根据邮箱加载用户详情
     * @param email 邮箱
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        logger.debug("正在根据邮箱加载用户详情: {}", email);

        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> {
                    logger.warn("用户不存在，邮箱: {}", email);
                    return new UsernameNotFoundException("用户不存在，邮箱: " + email);
                });

        // 检查用户状态
        if (!user.getEnabled()) {
            logger.warn("用户已被禁用，邮箱: {}", email);
            throw new UsernameNotFoundException("用户已被禁用，邮箱: " + email);
        }

        // 构建用户权限
        Collection<GrantedAuthority> authorities = buildUserAuthorities(user);

        logger.debug("用户 {} 加载成功，权限数量: {}", user.getUsername(), authorities.size());

        return new CustomUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRealName(),
                user.getPhone(),
                user.getDepartmentId(),
                user.getRoleId(),
                user.getEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities,
                user.getLastLoginTime(),
                user.getPasswordUpdateTime()
        );
    }

    /**
     * 构建用户权限
     * @param user 用户实体
     * @return 权限集合
     */
    private Collection<GrantedAuthority> buildUserAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        try {
            // 添加角色权限（以ROLE_前缀）
            if (user.getRoleId() != null) {
                // 这里可以根据roleId查询角色名称，暂时使用ID
                authorities.add(new SimpleGrantedAuthority("ROLE_USER_" + user.getRoleId()));
                logger.debug("为用户 {} 添加角色权限: ROLE_USER_{}", user.getUsername(), user.getRoleId());
            }

            // 添加基础权限
            authorities.add(new SimpleGrantedAuthority("USER"));

            // 根据用户角色添加特定权限
            if (user.getRoleId() != null) {
                switch (user.getRoleId().intValue()) {
                    case 1: // 超级管理员
                        authorities.add(new SimpleGrantedAuthority("ADMIN"));
                        authorities.add(new SimpleGrantedAuthority("SYSTEM_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("USER_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("ARCHIVE_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("BORROW_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("CATEGORY_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("DEPARTMENT_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("ROLE_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("CONFIG_MANAGE"));
                        break;
                    case 2: // 档案管理员
                        authorities.add(new SimpleGrantedAuthority("ARCHIVE_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("BORROW_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("CATEGORY_MANAGE"));
                        break;
                    case 3: // 部门管理员
                        authorities.add(new SimpleGrantedAuthority("DEPARTMENT_USER_MANAGE"));
                        authorities.add(new SimpleGrantedAuthority("ARCHIVE_VIEW"));
                        authorities.add(new SimpleGrantedAuthority("BORROW_APPLY"));
                        break;
                    case 4: // 普通用户
                        authorities.add(new SimpleGrantedAuthority("ARCHIVE_VIEW"));
                        authorities.add(new SimpleGrantedAuthority("BORROW_APPLY"));
                        break;
                    default:
                        // 默认只有基础权限
                        break;
                }
            }

            logger.debug("用户 {} 权限构建完成，权限列表: {}", user.getUsername(), authorities);

        } catch (Exception e) {
            logger.error("构建用户权限时发生错误: {}", e.getMessage(), e);
            // 发生错误时至少保证基础权限
            authorities.clear();
            authorities.add(new SimpleGrantedAuthority("USER"));
        }

        return authorities;
    }

    /**
     * 检查用户是否存在且启用
     * @param username 用户名
     * @return 是否存在且启用
     */
    public boolean isUserExistsAndEnabled(String username) {
        try {
            return userRepository.findByUsernameAndDeletedFalse(username)
                    .map(User::getEnabled)
                    .orElse(false);
        } catch (Exception e) {
            logger.error("检查用户状态时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查用户密码是否需要更新
     * @param username 用户名
     * @param lastPasswordResetTime 最后密码重置时间
     * @return 是否需要更新密码
     */
    public boolean isPasswordUpdateRequired(String username, Long lastPasswordResetTime) {
        try {
            return userRepository.findByUsernameAndDeletedFalse(username)
                    .map(user -> {
                        if (user.getPasswordUpdateTime() == null) {
                            return true; // 从未更新过密码，需要更新
                        }
                        return user.getPasswordUpdateTime().getTime() < lastPasswordResetTime;
                    })
                    .orElse(true);
        } catch (Exception e) {
            logger.error("检查密码更新状态时发生错误: {}", e.getMessage(), e);
            return true; // 发生错误时要求更新密码
        }
    }

    /**
     * 获取用户的角色名称
     * @param userId 用户ID
     * @return 角色名称
     */
    public String getUserRoleName(Long userId) {
        try {
            return userRepository.findByIdAndDeletedFalse(userId)
                    .map(user -> {
                        if (user.getRoleId() == null) {
                            return "未分配角色";
                        }
                        // 这里可以根据roleId查询具体角色名称
                        switch (user.getRoleId().intValue()) {
                            case 1:
                                return "超级管理员";
                            case 2:
                                return "档案管理员";
                            case 3:
                                return "部门管理员";
                            case 4:
                                return "普通用户";
                            default:
                                return "未知角色";
                        }
                    })
                    .orElse("用户不存在");
        } catch (Exception e) {
            logger.error("获取用户角色名称时发生错误: {}", e.getMessage(), e);
            return "获取失败";
        }
    }

    /**
     * 获取用户的部门名称
     * @param userId 用户ID
     * @return 部门名称
     */
    public String getUserDepartmentName(Long userId) {
        try {
            return userRepository.findByIdAndDeletedFalse(userId)
                    .map(user -> {
                        if (user.getDepartmentId() == null) {
                            return "未分配部门";
                        }
                        // 这里可以根据departmentId查询具体部门名称
                        return "部门_" + user.getDepartmentId();
                    })
                    .orElse("用户不存在");
        } catch (Exception e) {
            logger.error("获取用户部门名称时发生错误: {}", e.getMessage(), e);
            return "获取失败";
        }
    }

    /**
     * 刷新用户权限
     * @param username 用户名
     * @return 更新后的用户详情
     */
    public UserDetails refreshUserAuthorities(String username) {
        logger.debug("刷新用户权限: {}", username);
        return loadUserByUsername(username);
    }

    /**
     * 验证用户凭据
     * @param username 用户名
     * @param rawPassword 原始密码
     * @return 是否验证成功
     */
    public boolean validateUserCredentials(String username, String rawPassword) {
        try {
            User user = userRepository.findByUsernameAndDeletedFalse(username)
                    .orElse(null);
            
            if (user == null || !user.getEnabled()) {
                return false;
            }

            // 这里应该使用密码编码器验证密码
            // 暂时简单比较，实际应该使用BCryptPasswordEncoder
            return user.getPassword().equals(rawPassword);
            
        } catch (Exception e) {
            logger.error("验证用户凭据时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
}