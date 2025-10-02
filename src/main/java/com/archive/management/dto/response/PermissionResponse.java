package com.archive.management.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限响应DTO
 * 封装权限信息的输出数据
 */
public class PermissionResponse {

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限编码
     */
    private String code;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 权限类型（MENU-菜单权限，BUTTON-按钮权限，API-接口权限）
     */
    private String type;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 父权限信息
     */
    private ParentPermissionInfo parentPermission;

    /**
     * 权限路径
     */
    private String path;

    /**
     * HTTP方法（GET、POST、PUT、DELETE等）
     */
    private String method;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否显示
     */
    private Boolean visible;

    /**
     * 状态（ACTIVE-启用，INACTIVE-禁用）
     */
    private String status;

    /**
     * 子权限列表
     */
    private List<PermissionResponse> children;

    /**
     * 权限级别
     */
    private Integer level;

    /**
     * 权限路径（从根到当前节点）
     */
    private String fullPath;

    /**
     * 创建者信息
     */
    private CreatorInfo creator;

    /**
     * 更新者信息
     */
    private UpdaterInfo updater;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 父权限信息内部类
     */
    public static class ParentPermissionInfo {
        private Long id;
        private String name;
        private String code;
        private String type;

        public ParentPermissionInfo() {}

        public ParentPermissionInfo(Long id, String name, String code, String type) {
            this.id = id;
            this.name = name;
            this.code = code;
            this.type = type;
        }

        // Getter和Setter方法
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     * 创建者信息内部类
     */
    public static class CreatorInfo {
        private Long id;
        private String username;
        private String realName;

        public CreatorInfo() {}

        public CreatorInfo(Long id, String username, String realName) {
            this.id = id;
            this.username = username;
            this.realName = realName;
        }

        // Getter和Setter方法
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }
    }

    /**
     * 更新者信息内部类
     */
    public static class UpdaterInfo {
        private Long id;
        private String username;
        private String realName;

        public UpdaterInfo() {}

        public UpdaterInfo(Long id, String username, String realName) {
            this.id = id;
            this.username = username;
            this.realName = realName;
        }

        // Getter和Setter方法
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }
    }

    // 构造函数
    public PermissionResponse() {}

    public PermissionResponse(Long id, String name, String code, String description, String type,
                             Long parentId, ParentPermissionInfo parentPermission, String path,
                             String method, String icon, Integer sortOrder, Boolean visible,
                             String status, List<PermissionResponse> children, Integer level,
                             String fullPath, CreatorInfo creator, UpdaterInfo updater,
                             LocalDateTime createTime, LocalDateTime updateTime, String remark) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.type = type;
        this.parentId = parentId;
        this.parentPermission = parentPermission;
        this.path = path;
        this.method = method;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.visible = visible;
        this.status = status;
        this.children = children;
        this.level = level;
        this.fullPath = fullPath;
        this.creator = creator;
        this.updater = updater;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.remark = remark;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public ParentPermissionInfo getParentPermission() {
        return parentPermission;
    }

    public void setParentPermission(ParentPermissionInfo parentPermission) {
        this.parentPermission = parentPermission;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PermissionResponse> getChildren() {
        return children;
    }

    public void setChildren(List<PermissionResponse> children) {
        this.children = children;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public CreatorInfo getCreator() {
        return creator;
    }

    public void setCreator(CreatorInfo creator) {
        this.creator = creator;
    }

    public UpdaterInfo getUpdater() {
        return updater;
    }

    public void setUpdater(UpdaterInfo updater) {
        this.updater = updater;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "PermissionResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", parentId=" + parentId +
                ", parentPermission=" + parentPermission +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", icon='" + icon + '\'' +
                ", sortOrder=" + sortOrder +
                ", visible=" + visible +
                ", status='" + status + '\'' +
                ", children=" + children +
                ", level=" + level +
                ", fullPath='" + fullPath + '\'' +
                ", creator=" + creator +
                ", updater=" + updater +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", remark='" + remark + '\'' +
                '}';
    }
}