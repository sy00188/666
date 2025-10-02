package com.archive.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 部门更新请求DTO
 * 封装部门更新所需的输入信息
 */
public class DepartmentUpdateRequest {

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String name;

    /**
     * 部门编码
     */
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    private String code;

    /**
     * 父部门ID（可选，为空表示顶级部门）
     */
    private Long parentId;

    /**
     * 部门描述
     */
    @Size(max = 500, message = "部门描述长度不能超过500个字符")
    private String description;

    /**
     * 负责人ID
     */
    private Long managerId;

    /**
     * 联系电话
     */
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String phone;

    /**
     * 部门地址
     */
    @Size(max = 200, message = "部门地址长度不能超过200个字符")
    private String address;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 部门状态（ACTIVE-启用，INACTIVE-禁用）
     */
    private String status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    // 构造函数
    public DepartmentUpdateRequest() {}

    public DepartmentUpdateRequest(String name, String code, Long parentId, String description,
                                  Long managerId, String phone, String address, Integer sortOrder,
                                  String status, String remark) {
        this.name = name;
        this.code = code;
        this.parentId = parentId;
        this.description = description;
        this.managerId = managerId;
        this.phone = phone;
        this.address = address;
        this.sortOrder = sortOrder;
        this.status = status;
        this.remark = remark;
    }

    // Getter和Setter方法
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "DepartmentUpdateRequest{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", parentId=" + parentId +
                ", description='" + description + '\'' +
                ", managerId=" + managerId +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", sortOrder=" + sortOrder +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}