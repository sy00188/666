package com.archive.management.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门响应DTO
 * 封装部门信息的输出数据
 */
public class DepartmentResponse {

    /**
     * 部门ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门编码
     */
    private String code;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 父部门名称
     */
    private String parentName;

    /**
     * 部门描述
     */
    private String description;

    /**
     * 负责人信息
     */
    private ManagerInfo manager;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 部门地址
     */
    private String address;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 部门状态
     */
    private String status;

    /**
     * 部门状态描述
     */
    private String statusDesc;

    /**
     * 用户数量
     */
    private Long userCount;

    /**
     * 子部门列表
     */
    private List<DepartmentResponse> children;

    /**
     * 部门层级
     */
    private Integer level;

    /**
     * 部门路径
     */
    private String path;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人信息
     */
    private CreatorInfo creator;

    /**
     * 更新人信息
     */
    private UpdaterInfo updater;

    // 构造函数
    public DepartmentResponse() {}

    public DepartmentResponse(Long id, String name, String code, Long parentId, String parentName,
                             String description, ManagerInfo manager, String phone, String address,
                             Integer sortOrder, String status, String statusDesc, Long userCount,
                             List<DepartmentResponse> children, Integer level, String path,
                             String remark, LocalDateTime createTime, LocalDateTime updateTime,
                             CreatorInfo creator, UpdaterInfo updater) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.parentId = parentId;
        this.parentName = parentName;
        this.description = description;
        this.manager = manager;
        this.phone = phone;
        this.address = address;
        this.sortOrder = sortOrder;
        this.status = status;
        this.statusDesc = statusDesc;
        this.userCount = userCount;
        this.children = children;
        this.level = level;
        this.path = path;
        this.remark = remark;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.creator = creator;
        this.updater = updater;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ManagerInfo getManager() {
        return manager;
    }

    public void setManager(ManagerInfo manager) {
        this.manager = manager;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public List<DepartmentResponse> getChildren() {
        return children;
    }

    public void setChildren(List<DepartmentResponse> children) {
        this.children = children;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    /**
     * 负责人信息
     */
    public static class ManagerInfo {
        private Long id;
        private String username;
        private String realName;
        private String email;
        private String phone;

        public ManagerInfo() {}

        public ManagerInfo(Long id, String username, String realName, String email, String phone) {
            this.id = id;
            this.username = username;
            this.realName = realName;
            this.email = email;
            this.phone = phone;
        }

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        @Override
        public String toString() {
            return "ManagerInfo{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", realName='" + realName + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    '}';
        }
    }

    /**
     * 创建人信息
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
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }

        @Override
        public String toString() {
            return "CreatorInfo{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", realName='" + realName + '\'' +
                    '}';
        }
    }

    /**
     * 更新人信息
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
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }

        @Override
        public String toString() {
            return "UpdaterInfo{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", realName='" + realName + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "DepartmentResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", parentId=" + parentId +
                ", parentName='" + parentName + '\'' +
                ", description='" + description + '\'' +
                ", manager=" + manager +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", sortOrder=" + sortOrder +
                ", status='" + status + '\'' +
                ", statusDesc='" + statusDesc + '\'' +
                ", userCount=" + userCount +
                ", children=" + children +
                ", level=" + level +
                ", path='" + path + '\'' +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", creator=" + creator +
                ", updater=" + updater +
                '}';
    }
}