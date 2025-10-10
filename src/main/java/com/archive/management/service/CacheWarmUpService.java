package com.archive.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import com.archive.management.entity.User;
import com.archive.management.entity.Permission;
import com.archive.management.entity.Role;
import com.archive.management.entity.Department;
import com.archive.management.entity.Archive;
import com.archive.management.repository.UserRepository;
import com.archive.management.repository.PermissionRepository;
import com.archive.management.repository.RoleRepository;
import com.archive.management.repository.DepartmentRepository;
import com.archive.management.repository.ArchiveRepository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热服务类
 * 在应用启动时和定时任务中预热常用缓存
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Service
public class CacheWarmUpService implements ApplicationRunner {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ArchiveRepository archiveRepository;

    /**
     * 应用启动时预热缓存
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 异步预热缓存，避免影响应用启动
        warmUpCachesAsync();
    }

    /**
     * 异步预热缓存
     */
    @Async
    public void warmUpCachesAsync() {
        try {
            // 预热用户缓存
            warmUpUserCache();
            
            // 预热权限缓存
            warmUpPermissionCache();
            
            // 预热角色缓存
            warmUpRoleCache();
            
            // 预热部门缓存
            warmUpDepartmentCache();
            
            // 预热配置缓存
            warmUpConfigCache();
            
            System.out.println("缓存预热完成");
        } catch (Exception e) {
            System.err.println("缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 预热用户缓存
     */
    private void warmUpUserCache() {
        try {
            // 缓存活跃用户
            List<User> activeUsers = userRepository.findByStatus(1);
            Map<String, Object> userCacheData = new HashMap<>();
            
            for (User user : activeUsers) {
                String key = "user:" + user.getId();
                userCacheData.put(key, user);
                
                String usernameKey = "user:username:" + user.getUsername();
                userCacheData.put(usernameKey, user);
            }
            
            cacheService.warmUpCache("users", userCacheData);
            System.out.println("用户缓存预热完成，缓存了 " + activeUsers.size() + " 个用户");
        } catch (Exception e) {
            System.err.println("用户缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 预热权限缓存
     */
    private void warmUpPermissionCache() {
        try {
            List<Permission> permissions = permissionRepository.findAll();
            Map<String, Object> permissionCacheData = new HashMap<>();
            
            for (Permission permission : permissions) {
                String key = "permission:" + permission.getId();
                permissionCacheData.put(key, permission);
                
                String codeKey = "permission:code:" + permission.getCode();
                permissionCacheData.put(codeKey, permission);
            }
            
            cacheService.warmUpCache("permissions", permissionCacheData);
            System.out.println("权限缓存预热完成，缓存了 " + permissions.size() + " 个权限");
        } catch (Exception e) {
            System.err.println("权限缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 预热角色缓存
     */
    private void warmUpRoleCache() {
        try {
            List<Role> roles = roleRepository.findAll();
            Map<String, Object> roleCacheData = new HashMap<>();
            
            for (Role role : roles) {
                String key = "role:" + role.getId();
                roleCacheData.put(key, role);
                
                String codeKey = "role:code:" + role.getCode();
                roleCacheData.put(codeKey, role);
            }
            
            cacheService.warmUpCache("roles", roleCacheData);
            System.out.println("角色缓存预热完成，缓存了 " + roles.size() + " 个角色");
        } catch (Exception e) {
            System.err.println("角色缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 预热部门缓存
     */
    private void warmUpDepartmentCache() {
        try {
            List<Department> departments = departmentRepository.findAll();
            Map<String, Object> departmentCacheData = new HashMap<>();
            
            for (Department department : departments) {
                String key = "department:" + department.getId();
                departmentCacheData.put(key, department);
                
                String codeKey = "department:code:" + department.getCode();
                departmentCacheData.put(codeKey, department);
            }
            
            cacheService.warmUpCache("departments", departmentCacheData);
            System.out.println("部门缓存预热完成，缓存了 " + departments.size() + " 个部门");
        } catch (Exception e) {
            System.err.println("部门缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 预热配置缓存
     */
    private void warmUpConfigCache() {
        try {
            Map<String, Object> configCacheData = new HashMap<>();
            
            // 系统配置
            configCacheData.put("system.name", "档案管理系统");
            configCacheData.put("system.version", "1.0.0");
            configCacheData.put("system.environment", "production");
            
            // 文件上传配置
            configCacheData.put("file.max.size", "104857600"); // 100MB
            configCacheData.put("file.allowed.types", "pdf,doc,docx,xls,xlsx,ppt,pptx,txt,jpg,jpeg,png,gif,zip,rar");
            
            // 安全配置
            configCacheData.put("security.jwt.expiration", "86400000"); // 24小时
            configCacheData.put("security.password.min.length", "8");
            
            cacheService.warmUpCache("config", configCacheData);
            System.out.println("配置缓存预热完成");
        } catch (Exception e) {
            System.err.println("配置缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 定时预热热点数据缓存
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void warmUpHotDataCache() {
        try {
            // 预热最近创建的档案
            List<Archive> recentArchives = archiveRepository.findTop10ByOrderByCreateTimeDesc();
            Map<String, Object> archiveCacheData = new HashMap<>();
            
            for (Archive archive : recentArchives) {
                String key = "archive:" + archive.getId();
                archiveCacheData.put(key, archive);
            }
            
            cacheService.warmUpCache("archives", archiveCacheData);
            System.out.println("热点数据缓存预热完成，缓存了 " + recentArchives.size() + " 个档案");
        } catch (Exception e) {
            System.err.println("热点数据缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 定时预热统计缓存
     * 每2分钟执行一次
     */
    @Scheduled(fixedRate = 120000) // 2分钟
    public void warmUpStatisticsCache() {
        try {
            Map<String, Object> statisticsCacheData = new HashMap<>();
            
            // 用户统计
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByStatus(1);
            statisticsCacheData.put("users.total", totalUsers);
            statisticsCacheData.put("users.active", activeUsers);
            
            // 档案统计
            long totalArchives = archiveRepository.count();
            long activeArchives = archiveRepository.countByStatus(1);
            statisticsCacheData.put("archives.total", totalArchives);
            statisticsCacheData.put("archives.active", activeArchives);
            
            // 部门统计
            long totalDepartments = departmentRepository.count();
            statisticsCacheData.put("departments.total", totalDepartments);
            
            // 角色统计
            long totalRoles = roleRepository.count();
            statisticsCacheData.put("roles.total", totalRoles);
            
            cacheService.warmUpCache("statistics", statisticsCacheData);
            System.out.println("统计缓存预热完成");
        } catch (Exception e) {
            System.err.println("统计缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 手动预热指定缓存
     */
    public void warmUpSpecificCache(String cacheName) {
        switch (cacheName) {
            case "users":
                warmUpUserCache();
                break;
            case "permissions":
                warmUpPermissionCache();
                break;
            case "roles":
                warmUpRoleCache();
                break;
            case "departments":
                warmUpDepartmentCache();
                break;
            case "config":
                warmUpConfigCache();
                break;
            default:
                System.out.println("未知的缓存类型: " + cacheName);
        }
    }

    /**
     * 清空并重新预热所有缓存
     */
    public void refreshAllCaches() {
        try {
            // 清空所有缓存
            cacheService.clearAllCache();
            
            // 重新预热
            warmUpCachesAsync();
            
            System.out.println("所有缓存已刷新");
        } catch (Exception e) {
            System.err.println("缓存刷新失败: " + e.getMessage());
        }
    }
}
