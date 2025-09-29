package com.archive.management.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 权限树节点类
 * 用于构建权限树结构，支持多级权限层次
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionTreeNode {

    /**
     * 节点ID
     */
    private String id;

    /**
     * 父节点ID
     */
    private String parentId;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限标识/编码
     */
    private String code;

    /**
     * 权限类型（如：menu, button, api等）
     */
    private String type;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 权限级别/层级
     */
    private Integer level;

    /**
     * 权限路径（如菜单路径、API路径等）
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 是否选中（用于权限分配时）
     */
    private Boolean checked;

    /**
     * 是否展开（用于树形展示）
     */
    private Boolean expanded;

    /**
     * 子节点列表
     */
    private List<PermissionTreeNode> children;

    /**
     * 扩展属性（用于存储额外信息）
     */
    private Object extra;

    /**
     * 默认构造函数
     */
    public PermissionTreeNode() {
        this.children = new ArrayList<>();
        this.enabled = true;
        this.checked = false;
        this.expanded = false;
        this.level = 0;
    }

    /**
     * 构造函数
     * 
     * @param id 节点ID
     * @param parentId 父节点ID
     * @param name 权限名称
     * @param code 权限编码
     */
    public PermissionTreeNode(String id, String parentId, String name, String code) {
        this();
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.code = code;
    }

    /**
     * 构造函数（完整参数）
     * 
     * @param id 节点ID
     * @param parentId 父节点ID
     * @param name 权限名称
     * @param code 权限编码
     * @param type 权限类型
     * @param level 权限级别
     */
    public PermissionTreeNode(String id, String parentId, String name, String code, String type, Integer level) {
        this(id, parentId, name, code);
        this.type = type;
        this.level = level;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 创建根节点
     * 
     * @param id 节点ID
     * @param name 节点名称
     * @return PermissionTreeNode
     */
    public static PermissionTreeNode createRoot(String id, String name) {
        PermissionTreeNode root = new PermissionTreeNode();
        root.setId(id);
        root.setName(name);
        root.setParentId(null);
        root.setLevel(0);
        root.setExpanded(true);
        return root;
    }

    /**
     * 创建菜单节点
     * 
     * @param id 节点ID
     * @param parentId 父节点ID
     * @param name 菜单名称
     * @param code 菜单编码
     * @param path 菜单路径
     * @return PermissionTreeNode
     */
    public static PermissionTreeNode createMenu(String id, String parentId, String name, String code, String path) {
        PermissionTreeNode menu = new PermissionTreeNode(id, parentId, name, code);
        menu.setType("menu");
        menu.setPath(path);
        return menu;
    }

    /**
     * 创建按钮节点
     * 
     * @param id 节点ID
     * @param parentId 父节点ID
     * @param name 按钮名称
     * @param code 按钮编码
     * @return PermissionTreeNode
     */
    public static PermissionTreeNode createButton(String id, String parentId, String name, String code) {
        PermissionTreeNode button = new PermissionTreeNode(id, parentId, name, code);
        button.setType("button");
        return button;
    }

    /**
     * 创建API节点
     * 
     * @param id 节点ID
     * @param parentId 父节点ID
     * @param name API名称
     * @param code API编码
     * @param path API路径
     * @return PermissionTreeNode
     */
    public static PermissionTreeNode createApi(String id, String parentId, String name, String code, String path) {
        PermissionTreeNode api = new PermissionTreeNode(id, parentId, name, code);
        api.setType("api");
        api.setPath(path);
        return api;
    }

    // ========== 便捷方法 ==========

    /**
     * 添加子节点
     * 
     * @param child 子节点
     */
    public void addChild(PermissionTreeNode child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        if (child != null) {
            child.setParentId(this.id);
            if (child.getLevel() == null || child.getLevel() <= this.level) {
                child.setLevel(this.level + 1);
            }
            this.children.add(child);
        }
    }

    /**
     * 移除子节点
     * 
     * @param childId 子节点ID
     * @return 是否移除成功
     */
    public boolean removeChild(String childId) {
        if (this.children == null || childId == null) {
            return false;
        }
        return this.children.removeIf(child -> childId.equals(child.getId()));
    }

    /**
     * 查找子节点
     * 
     * @param childId 子节点ID
     * @return 子节点或null
     */
    public PermissionTreeNode findChild(String childId) {
        if (this.children == null || childId == null) {
            return null;
        }
        return this.children.stream()
                .filter(child -> childId.equals(child.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断是否有子节点
     * 
     * @return 是否有子节点
     */
    public boolean hasChildren() {
        return this.children != null && !this.children.isEmpty();
    }

    /**
     * 获取子节点数量
     * 
     * @return 子节点数量
     */
    public int getChildrenCount() {
        return this.children != null ? this.children.size() : 0;
    }

    /**
     * 判断是否为根节点
     * 
     * @return 是否为根节点
     */
    public boolean isRoot() {
        return this.parentId == null || this.parentId.isEmpty();
    }

    /**
     * 判断是否为叶子节点
     * 
     * @return 是否为叶子节点
     */
    public boolean isLeaf() {
        return !hasChildren();
    }

    /**
     * 设置选中状态（递归设置子节点）
     * 
     * @param checked 是否选中
     * @param recursive 是否递归设置子节点
     */
    public void setCheckedRecursive(boolean checked, boolean recursive) {
        this.checked = checked;
        if (recursive && this.children != null) {
            this.children.forEach(child -> child.setCheckedRecursive(checked, true));
        }
    }

    /**
     * 设置展开状态（递归设置子节点）
     * 
     * @param expanded 是否展开
     * @param recursive 是否递归设置子节点
     */
    public void setExpandedRecursive(boolean expanded, boolean recursive) {
        this.expanded = expanded;
        if (recursive && this.children != null) {
            this.children.forEach(child -> child.setExpandedRecursive(expanded, true));
        }
    }

    // ========== Getter and Setter ==========

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }

    public List<PermissionTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<PermissionTreeNode> children) {
        this.children = children;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionTreeNode that = (PermissionTreeNode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PermissionTreeNode{" +
                "id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", type='" + type + '\'' +
                ", level=" + level +
                ", path='" + path + '\'' +
                ", enabled=" + enabled +
                ", checked=" + checked +
                ", childrenCount=" + getChildrenCount() +
                '}';
    }
}