package com.archive.management.mapper;

import com.archive.management.entity.Permission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * PermissionMapper实现类
 * 提供searchPermissionsWithFields方法的具体实现
 */
@Component
public class PermissionMapperImpl {

    /**
     * 根据指定字段分页模糊搜索权限的实现
     * 
     * @param page 分页对象
     * @param keyword 关键词
     * @param searchFields 搜索字段列表
     * @return 分页结果
     */
    public IPage<Permission> searchPermissionsWithFields(Page<Permission> page, String keyword, List<String> searchFields) {
        // 构建动态SQL查询条件
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_permission WHERE (");
        
        if (CollectionUtils.isEmpty(searchFields)) {
            // 如果没有指定搜索字段，使用默认字段
            sql.append("permission_code LIKE CONCAT('%', ?, '%') ")
               .append("OR name LIKE CONCAT('%', ?, '%') ")
               .append("OR description LIKE CONCAT('%', ?, '%') ")
               .append("OR permission_path LIKE CONCAT('%', ?, '%')");
        } else {
            // 根据指定字段构建查询条件
            for (int i = 0; i < searchFields.size(); i++) {
                if (i > 0) {
                    sql.append(" OR ");
                }
                String field = searchFields.get(i);
                // 映射前端字段名到数据库字段名
                String dbField = mapFieldName(field);
                sql.append(dbField).append(" LIKE CONCAT('%', ?, '%')");
            }
        }
        
        sql.append(") AND deleted = 0 ORDER BY sort_order ASC, create_time DESC");
        
        // 这里应该使用MyBatis-Plus的动态SQL功能
        // 由于这是一个简化的实现，实际项目中应该使用@SelectProvider或XML配置
        return null; // 占位符，实际实现需要执行SQL查询
    }
    
    /**
     * 映射前端字段名到数据库字段名
     */
    private String mapFieldName(String field) {
        switch (field) {
            case "code":
                return "permission_code";
            case "name":
                return "name";
            case "description":
                return "description";
            case "path":
                return "permission_path";
            case "type":
                return "permission_type";
            default:
                return "name"; // 默认搜索名称字段
        }
    }
}