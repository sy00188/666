package com.archive.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色响应DTO
 * 封装角色信息的输出数据
 */
public class RoleResponse {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色类型（SYSTEM-系统角色，CUSTOM-自定义角色）
     */
    private String type;

    /**
     * 数据权限范围（ALL-全部数据，DEPT-本部门数据，DEPT_AND_SUB-本部门及子部门数据，SELF-仅本人数据，CUSTOM-自定义数据权限）
     */
    private String dataScope;

    /**
     * 自定义数据权限部门列表
     */
    private List<DataScopeDeptInfo> dataScopeDepts;

    /**
     * 权限列表
     */
    private List<PermissionInfo> permissions;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（ACTIVE-启用，INACTIVE-禁用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

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
     * 用户数量（使用该角色的用户数量）
     */
    private Integer userCount;

    // 内部类：数据权限部门信息
    public static class DataScopeDeptInfo {
        private Long deptId;
        private String deptName;
        private String deptCode;

        public DataScopeDeptInfo() {}

        public DataScopeDeptInfo(Long deptId, String deptName, String deptCode) {
            this.deptId = deptId;
            this.deptName = deptName;
            this.deptCode = deptCode;
        }

        // Getter和Setter方法
        public Long getDeptId() {
            return deptId;
        }

        public void setDeptId(Long deptId) {
            this.deptId = deptId;
        }

        public String getDeptName() {
            return deptName;
        }

        public void setDeptName(String deptName) {
            this.deptName = deptName;
        }

        public String getDeptCode() {
            return deptCode;
        }

        public void setDeptCode(String deptCode) {
            this.deptCode = deptCode;
        }
    }

    // 内部类：权限信息
    public static class PermissionInfo {
        private Long permissionId;
        private String permissionName;
        private String permissionCode;
        private String permissionType;

        public PermissionInfo() {}

        public PermissionInfo(Long permissionId, String permissionName, String permissionCode, String permissionType) {
            this.permissionId = permissionId;
            this.permissionName = permissionName;
            this.permissionCode = permissionCode;
            this.permissionType = permissionType;
        }

        // Getter和Setter方法
        public Long getPermissionId() {
            return permissionId;
        }

        public void setPermissionId(Long permissionId) {
            this.permissionId = permissionId;
        }

        public String getPermissionName() {
            return permissionName;
        }

        public void setPermissionName(String permissionName) {
            this.permissionName = permissionName;
        }

        public String getPermissionCode() {
            return permissionCode;
        }

        public void setPermissionCode(String permissionCode) {
            this.permissionCode = permissionCode;
        }

        public String getPermissionType() {
            return permissionType;
        }

        public void setPermissionType(String permissionType) {
            this.permissionType = permissionType;
        }
    }

    // 内部类：创建者信息
    public static class CreatorInfo {
        private Long userId;
        private String username;
        private String realName;

        public CreatorInfo() {}

        public CreatorInfo(Long userId, String username, String realName) {
            this.userId = userId;
            this.username = username;
            this.realName = realName;
        }

        // Getter和Setter方法
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
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

    // 内部类：更新者信息
    public static class UpdaterInfo {
        private Long userId;
        private String username;
        private String realName;

        public UpdaterInfo() {}

        public UpdaterInfo(Long userId, String username, String realName) {
            this.userId = userId;
            this.username = username;
            this.realName = realName;
        }

        // Getter和Setter方法
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
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
    public RoleResponse() {}

    public RoleResponse(Long id, String name, String code, String description, String type,
                       String dataScope, List<DataScopeDeptInfo> dataScopeDepts, List<PermissionInfo> permissions,
                       Integer sortOrder, String status, String remark, CreatorInfo creator,
                       UpdaterInfo updater, LocalDateTime createTime, LocalDateTime updateTime, Integer userCount) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.type = type;
        this.dataScope = dataScope;
        this.dataScopeDepts = dataScopeDepts;
        this.permissions = permissions;
        this.sortOrder = sortOrder;
        this.status = status;
        this.remark = remark;
        this.creator = creator;
        this.updater = updater;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.userCount = userCount;
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

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public List<DataScopeDeptInfo> getDataScopeDepts() {
        return dataScopeDepts;
    }

    public void setDataScopeDepts(List<DataScopeDeptInfo> dataScopeDepts) {
        this.dataScopeDepts = dataScopeDepts;
    }

    public List<PermissionInfo> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionInfo> permissions) {
        this.permissions = permissions;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    @Override
    public String toString() {
        return "RoleResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", dataScope='" + dataScope + '\'' +
                ", dataScopeDepts=" + dataScopeDepts +
                ", permissions=" + permissions +
                ", sortOrder=" + sortOrder +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                ", creator=" + creator +
                ", updater=" + updater +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", userCount=" + userCount +
                '}';
    }
}