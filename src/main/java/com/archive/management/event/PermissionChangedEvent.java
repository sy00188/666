package com.archive.management.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 权限变更事件
 * 当权限数据发生变更时触发此事件
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Getter
public class PermissionChangedEvent extends ApplicationEvent {

    /**
     * 权限ID
     */
    private final Long permissionId;

    /**
     * 操作类型：CREATE, UPDATE, DELETE, SYNC
     */
    private final String action;

    /**
     * 操作人ID
     */
    private final Long operatorId;

    /**
     * 变更描述
     */
    private final String description;

    public PermissionChangedEvent(Object source, Long permissionId, 
                                 String action, Long operatorId, String description) {
        super(source);
        this.permissionId = permissionId;
        this.action = action;
        this.operatorId = operatorId;
        this.description = description;
    }
}

