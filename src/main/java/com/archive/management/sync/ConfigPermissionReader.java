package com.archive.management.sync;

import com.archive.management.config.PermissionSyncConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置文件权限读取器
 * 从permissions.yml读取权限配置
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigPermissionReader {

    private final PermissionSyncConfig permissionSyncConfig;

    /**
     * 读取配置文件中的权限
     * @return 权限定义列表
     */
    public List<PermissionDefinition> readPermissions() {
        log.info("开始读取配置文件权限...");
        List<PermissionDefinition> permissions = new ArrayList<>();
        
        for (PermissionSyncConfig.PermissionConfig config : permissionSyncConfig.getPermissions()) {
            PermissionDefinition def = new PermissionDefinition();
            def.setCode(config.getCode());
            def.setName(config.getName());
            def.setDescription(config.getDescription());
            def.setType(config.getType());
            def.setPath(config.getPath());
            def.setIcon(config.getIcon());
            def.setSort(config.getSort());
            def.setParentCode(config.getParentCode());
            def.setSource("CONFIG");
            permissions.add(def);
        }
        
        log.info("配置文件权限读取完成，共{}个权限", permissions.size());
        return permissions;
    }
}

