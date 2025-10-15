package com.archive.management.sync;

import com.archive.annotation.RequirePermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 注解权限扫描器
 * 扫描所有Controller和Service中的@RequirePermission注解
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnnotationPermissionScanner {

    private final ApplicationContext applicationContext;

    /**
     * 扫描所有注解权限
     * @return 权限定义列表
     */
    public List<PermissionDefinition> scanPermissions() {
        log.info("开始扫描注解权限...");
        List<PermissionDefinition> permissions = new ArrayList<>();
        
        // 获取所有Controller bean
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
        controllers.putAll(applicationContext.getBeansWithAnnotation(RequestMapping.class));
        
        for (Object controller : controllers.values()) {
            scanControllerPermissions(controller, permissions);
        }
        
        log.info("注解权限扫描完成，共找到{}个权限", permissions.size());
        return permissions;
    }

    /**
     * 扫描Controller中的权限
     */
    private void scanControllerPermissions(Object controller, List<PermissionDefinition> permissions) {
        Class<?> clazz = controller.getClass();
        if (clazz.getName().contains("$$")) {
            clazz = clazz.getSuperclass();
        }
        
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            RequirePermission annotation = method.getAnnotation(RequirePermission.class);
            if (annotation != null) {
                PermissionDefinition def = new PermissionDefinition();
                def.setCode(annotation.value());
                def.setName(annotation.name());
                def.setDescription(annotation.description());
                def.setType("API");
                def.setSource("ANNOTATION");
                permissions.add(def);
                
                log.debug("发现注解权限：{} - {}", def.getCode(), def.getName());
            }
        }
    }
}

