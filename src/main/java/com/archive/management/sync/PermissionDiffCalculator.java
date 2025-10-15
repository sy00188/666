package com.archive.management.sync;

import com.archive.management.entity.Permission;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限差异对比器
 * 对比数据库现有权限和新权限定义，计算需要新增、更新、删除的权限
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@Component
public class PermissionDiffCalculator {

    /**
     * 计算权限差异
     * @param existingPermissions 数据库现有权限
     * @param newDefinitions 新权限定义
     * @return 差异结果
     */
    public DiffResult calculateDiff(List<Permission> existingPermissions, 
                                   List<PermissionDefinition> newDefinitions) {
        log.info("开始计算权限差异...");
        
        DiffResult result = new DiffResult();
        
        Map<String, Permission> existingMap = existingPermissions.stream()
            .collect(Collectors.toMap(Permission::getPermissionCode, p -> p));
        
        Map<String, PermissionDefinition> newMap = newDefinitions.stream()
            .collect(Collectors.toMap(PermissionDefinition::getCode, d -> d));
        
        // 找出需要新增的权限
        for (PermissionDefinition def : newDefinitions) {
            if (!existingMap.containsKey(def.getCode())) {
                result.getToCreate().add(def);
            }
        }
        
        // 找出需要更新的权限
        for (PermissionDefinition def : newDefinitions) {
            if (existingMap.containsKey(def.getCode())) {
                Permission existing = existingMap.get(def.getCode());
                if (needsUpdate(existing, def)) {
                    result.getToUpdate().put(existing, def);
                }
            }
        }
        
        // 找出需要标记删除的权限（在数据库中但不在新定义中，且不是系统内置）
        for (Permission existing : existingPermissions) {
            if (!newMap.containsKey(existing.getPermissionCode()) 
                && !existing.isSystemPermission()) {
                result.getToDelete().add(existing);
            }
        }
        
        log.info("权限差异计算完成：新增{}, 更新{}, 删除{}", 
                 result.getToCreate().size(), 
                 result.getToUpdate().size(), 
                 result.getToDelete().size());
        
        return result;
    }

    /**
     * 判断是否需要更新
     */
    private boolean needsUpdate(Permission existing, PermissionDefinition def) {
        return !Objects.equals(existing.getPermissionName(), def.getName())
            || !Objects.equals(existing.getDescription(), def.getDescription())
            || !Objects.equals(existing.getPath(), def.getPath())
            || !Objects.equals(existing.getIcon(), def.getIcon());
    }

    /**
     * 差异结果
     */
    @Data
    public static class DiffResult {
        private List<PermissionDefinition> toCreate = new ArrayList<>();
        private Map<Permission, PermissionDefinition> toUpdate = new HashMap<>();
        private List<Permission> toDelete = new ArrayList<>();
    }
}

